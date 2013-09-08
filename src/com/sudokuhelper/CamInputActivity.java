package com.sudokuhelper;
import static com.sudokuhelper.util.CommonUtilities.getBitmap;
import static com.sudokuhelper.util.CommonUtilities.getDcimDirectory;
import static com.sudokuhelper.util.CommonUtilities.getNewImageFile;
import static com.sudokuhelper.util.CommonUtilities.templateUriToPath;
import static com.sudokuhelper.util.CommonUtilities.uriToImagePath;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class CamInputActivity extends Activity{
	private static final String TAG = "GalleryInputActivity";
	public static final int CODE_TAKE_PHOTO = 0;
	public static final int CODE_SELECT_PHOTO = 1;
	public static final int CODE_SELECT_TEMPLATE = 2;
	public static final String SAVE_IMAGE_PATH = getDcimDirectory("skh_save")
			.getPath() + File.separator;
	private File mSudokuImageFile;
	private Bitmap mPlateBitmap;
	private String recognizedStr;
	private Button okButton;
	private Button cancelButton;
	private boolean isSuccess;
	private Mat plateImage;
	private boolean isFirstClick;
	private HintsQueue mHintsQueue;
	private ProgressDialog progressDialog;
	private double cell_len_x;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.gallery_input);
        okButton = (Button)findViewById(R.id.query_ok);
        cancelButton = (Button)findViewById(R.id.query_cancel);
        okButton.setOnClickListener(new OnOkClickListener());
        okButton.setClickable(false);
        mHintsQueue = new HintsQueue(this);
        cancelButton.setOnClickListener(new OnCancelClickListener());
		takeSudokuImage();
		isFirstClick = true;
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.cam_input, menu);
		return result;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean result = super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		case R.id.menu_settings:
			break;
		case R.id.select_picture:
			selectSudokuImage();
			break;
		case R.id.select_template:
			openTemplate();
			break;
		case R.id.take_picture:
			takeSudokuImage();
			break;
		default:
			break;
		}

		return result;
	}
	
	private class OnOkClickListener implements OnClickListener {
		public void onClick(View v) {
			if(isFirstClick) {
				String result = recognizePlate(plateImage);
				recognizedStr = result;
				isFirstClick = false;
			}
			Log.i(TAG, "result: " + recognizedStr);
			Intent intent = new Intent();
			intent.setClass(CamInputActivity.this, HandInputActivity.class);
			intent.putExtra("recognizedStr", recognizedStr);
			CamInputActivity.this.startActivity(intent);
		}
		
	}
	
	private class OnCancelClickListener implements OnClickListener {
		public void onClick(View v) {
			finish();
		}

	}
	
	private void takeSudokuImage(){
		mSudokuImageFile = getNewImageFile();
		Intent takeSudokuImageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		Uri sudokuImageUri = Uri.fromFile(mSudokuImageFile);
		Log.v(TAG, "Taken image uri: " + sudokuImageUri.toString());
		takeSudokuImageIntent.putExtra(MediaStore.EXTRA_OUTPUT, sudokuImageUri);
		startActivityForResult(takeSudokuImageIntent, CODE_TAKE_PHOTO);
	}
	
	public void openTemplate() {
		Intent intent = new Intent();
		intent.setClass(CamInputActivity.this, GalleryActivity.class);
		//intent.putExtra("recognizedStr", recognizedStr);
		intent.setType("image/**");
		startActivityForResult(intent,CODE_SELECT_TEMPLATE);
	}
	
	private void selectSudokuImage(){
		Intent selectSudokuImageIntent = new Intent(Intent.ACTION_GET_CONTENT);
		selectSudokuImageIntent.setType("image/**");
		startActivityForResult(selectSudokuImageIntent, CODE_SELECT_PHOTO);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK) {
			switch(requestCode){
				case CODE_TAKE_PHOTO:
					break;
				case CODE_SELECT_PHOTO:
					Uri sudokuImageUri = data.getData();
					Log.v(TAG, "Selected image uri: " + sudokuImageUri.toString());
					String selectImageName = uriToImagePath(this, sudokuImageUri);
					File srcFile = new File(selectImageName);
					mSudokuImageFile = srcFile;
					break;
				case CODE_SELECT_TEMPLATE:
					Uri sudokuImageUri2 = data.getData();
					String selectImageName2 = templateUriToPath(this,sudokuImageUri2);
					Log.v(TAG, "imagePath: " + selectImageName2);
					File srcFile2 = new File(selectImageName2);
					mSudokuImageFile = srcFile2;
					break;
				default:
					break;
			}
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("Processing image..");
			progressDialog.setTitle("Information.");
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressDialog.show();
			LooperThread myRecThread = new LooperThread();
			myRecThread.start();
		} 
	}
	Handler myHandler = new Handler();
	
	Runnable updateUI = new Runnable() {
		public void run() {
			showResult();
		}
	};
	
	class LooperThread extends Thread {
		public Handler handle;
		public void run() {
			Looper.prepare();
			recogniseImage();
			progressDialog.dismiss();
			myHandler.post(updateUI);
			Looper.loop();
		}
	}
	
	private void recogniseImage(){
		String sudokuImagePath = mSudokuImageFile.getPath();
		plateImage = findPlateImage(sudokuImagePath);
		mPlateBitmap = Bitmap.createBitmap(plateImage.width(), plateImage.height(),Bitmap.Config.ARGB_8888);
		
		Utils.matToBitmap(plateImage, mPlateBitmap);
		if(isSuccess) {
			okButton.setClickable(true);
		} else {
			mHintsQueue.showHint(R.string.error, R.string.error_reason);
		}
	}
	
	public String recognizePlate(Mat plateImage){
		String recognizeResult = "";
		int rec[] = new int[81];
	
		Mat im = plateImage;
	
		Mat gray =  new Mat(plateImage.size(),CvType.CV_8UC1);
		Imgproc.cvtColor(plateImage, gray, Imgproc.COLOR_BGR2GRAY);
	
		Mat thresh =  new Mat(plateImage.size(),CvType.CV_8UC1);
		Imgproc.adaptiveThreshold(gray, thresh, 255, 1, 1, 11, 2);
	
		ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Mat hierarchy = new Mat();
		Imgproc.findContours(thresh, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
		Rect rect = new Rect();
		StringBuffer str = new StringBuffer();
		double area;
		double filter_h = 23;
		double filter_area_low = 100;
		double filter_area_high = 750;
		for (int i = 0; i < contours.size(); ++i)
		{
			area = Imgproc.contourArea(contours.get(i));
			if(area > filter_area_low && area < filter_area_high) {
				rect = Imgproc.boundingRect(contours.get(i));
				if(rect.height > filter_h && rect.height < 50) {
					//Core.rectangle(im, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0,255,0));
					Mat roi = thresh.rowRange(rect.y, rect.y + rect.height).colRange(rect.x, rect.x + rect.width);
					Mat roismall = new Mat(new Size(10,10),CvType.CV_32FC1);
					Imgproc.resize(roi, roismall, new Size(10,10));
					roismall = roismall.reshape(1, 1);
					Mat roismallFloat = new Mat(roismall.size(),CvType.CV_32FC1);
					Mat results = new Mat();
					Mat neighborResponses = new Mat();
					Mat dists = new Mat();
					roismall.convertTo(roismallFloat, CvType.CV_32FC1);
					MainActivity.model.find_nearest(roismallFloat, 1, results, neighborResponses, dists);
					int res = (int)(results.get(0, 0)[0]);
					int col = rect.x / 50;
					int row = rect.y / 50;
					rec[row * 9 + col] = res;
				}
			}
		}
		for(int i = 0; i < 81; i++) {
			str.append(Integer.toString(rec[i]));
		}
		recognizeResult = str.toString();
		return recognizeResult;
	}
	
	private Mat findPlateImage(String sudokuImagePath) {
						plateImage = Highgui.imread(sudokuImagePath);
		
				if(plateImage != null) {
					Log.v(TAG, "imread success!");
				Mat blur = new Mat(plateImage.size(),CvType.CV_8UC1);
					Imgproc.GaussianBlur(plateImage, blur, new Size(5.0,5.0), 0);
					//Imgproc.GaussianBlur(plateImage, plateImage, new Size(5.0,5.0), 0);
										Mat gray = new Mat(plateImage.size(),CvType.CV_8UC1);
					Imgproc.cvtColor(blur, gray, Imgproc.COLOR_BGR2GRAY);
					
					Mat mask = Mat.zeros(plateImage.size(), CvType.CV_8UC1);
					
					Mat kernel1 = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(11,11));
					Mat close = new Mat(plateImage.size(), CvType.CV_8UC1);
					
					Imgproc.morphologyEx(gray, close, Imgproc.MORPH_CLOSE, kernel1);
					kernel1.release();
						Mat div = new Mat(plateImage.size(), CvType.CV_8UC1);
					Core.divide(gray,close,div, 1,CvType.CV_32FC1);
					Mat res = new Mat(plateImage.size(), CvType.CV_8UC1);
					Core.normalize(div, div, 0, 255, Core.NORM_MINMAX);
					div.convertTo(res, CvType.CV_8UC1);
					div.release();
					Mat res2 = new Mat(plateImage.size(), CvType.CV_8UC1);
					Imgproc.cvtColor(res, res2, Imgproc.COLOR_GRAY2BGR);
					
					Mat thresh = new Mat(plateImage.size(), CvType.CV_8UC1);
					Imgproc.adaptiveThreshold(res, thresh, 255, 1, 1, 11, 2);	
					
					ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
					Mat hierarchy = new Mat();
					Imgproc.findContours(thresh, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
					double max_area = 0;
					int best_cnt = 0;
					double area;
					Log.d("TAG", "contours number: " + contours.size());
					for (int i = 0; i < contours.size(); ++i)
					{
						area = Imgproc.contourArea(contours.get(i));
						if (area > 1000)
						{
							if (area > max_area)
							{
								max_area = area;
								best_cnt = i;
							}
						}
					}
					Imgproc.drawContours(mask, contours, best_cnt, new Scalar(255,255,255), -1);
					Imgproc.drawContours(mask, contours, best_cnt, new Scalar(0,0,0), 2);


					Rect rect = new Rect();
					rect = Imgproc.boundingRect(contours.get(best_cnt));
					cell_len_x = rect.width / 9;
					double cell_len_y = rect.height / 9;
					double bound_x = rect.x;
					double bound_y = rect.y;
					boolean is_ortho;
					Log.d("rect.width: ", Integer.toString(rect.width));
					Log.d("rect.height: ", Integer.toString(rect.height));
					double width_height_radio = rect.width * 1.0/ rect.height;
					Log.d("width_height_radio: ", Double.toString(width_height_radio));
					if(  width_height_radio > 1.05 || width_height_radio  < 0.95) {
						is_ortho = false;
					} else {
						is_ortho = true;
					}
					double tempx;
					double tempy;
					Point [][]rectify_contours= new Point[10][10];
					Log.d("is_ortho: ", Boolean.toString(is_ortho));
					if(is_ortho) {
						for(int i = 0; i < 10; i++) 
						{
							for(int j = 0; j < 10; j++) {
								tempx = bound_x + i * cell_len_x;
								tempy = bound_y + j * cell_len_y;
								rectify_contours[i][j] = new Point(tempx, tempy);
								//Core.putText(plateImage, Integer.toString(i * 10 + j), rectify_contours[i][j], Core.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(255,0,0));
							}
						}
						Mat output = Mat.zeros(new Size(450,450), CvType.CV_8UC3);
						Mat src = new Mat(4,2,CvType.CV_32F);
						Mat dst = new Mat(4,2,CvType.CV_32F);
						Mat warp = new Mat(new Size(450, 450), CvType.CV_8UC1);
						src.put(0, 0, rectify_contours[0][0].x);
						src.put(0, 1, rectify_contours[0][0].y);
						src.put(1, 0, rectify_contours[9][0].x);
						src.put(1, 1, rectify_contours[9][0].y);
						src.put(2, 0, rectify_contours[0][9].x);
						src.put(2, 1, rectify_contours[0][9].y);
						src.put(3, 0, rectify_contours[9][9].x);
						src.put(3, 1, rectify_contours[9][9].y);
						dst.put(0, 0, 0);
						dst.put(0, 1, 0);
						dst.put(1, 0, 450);
						dst.put(1, 1, 0);
						dst.put(2, 0, 0);
						dst.put(2, 1, 450);
						dst.put(3, 0, 450);
						dst.put(3, 1, 450);
						Mat transform = Imgproc.getPerspectiveTransform(src, dst);
						//透视变换
						Imgproc.warpPerspective(plateImage, warp, transform, new Size(450, 450));
						warp.colRange(0, 449).rowRange(0, 449).copyTo(output.colRange(0, 449).rowRange(0, 449));
						/*
						plateImage = output;
						Log.d("ssafaf", "aaaasf");
						if(plateImage != null) {
							return plateImage;
						}
						*/
						/*
						Mat outputBlur = new Mat(plateImage.size(), CvType.CV_8UC1);
						Imgproc.GaussianBlur(output, outputBlur, new Size(5.0,5.0), 0);
						Core.addWeighted(output, 1.68, outputBlur, -0.3, 5, plateImage);//锐化
						*/
						plateImage = output;
						isSuccess = true;
					} else {
						contours.clear();


						Core.bitwise_and(res, mask, res);

						Mat kernelx = Imgproc.getStructuringElement(Imgproc.MORPH_RECT ,new Size(2,10));
						Mat dx = new Mat(plateImage.size(), CvType.CV_8UC1);
						Imgproc.Sobel(res, dx, CvType.CV_16S, 1, 0);
						Core.convertScaleAbs(dx, dx);
						Core.normalize(dx, dx, 0, 255, Core.NORM_MINMAX);
						Imgproc.threshold(dx, close, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
						Mat dx_closex = new Mat(plateImage.size(), CvType.CV_8UC1);
						Imgproc.morphologyEx(close, dx_closex, Imgproc.MORPH_DILATE, kernelx);
						kernelx.release();
						Imgproc.findContours(dx_closex, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
						Log.d("TAG", "contours number: " + contours.size());
						for (int i = 0; i < contours.size(); ++i)
						{
							rect = Imgproc.boundingRect(contours.get(i));
							if(rect.height / rect.width > 5) {
								Imgproc.drawContours(dx_closex, contours, i, new Scalar(255,255,255), -1);
							} else {
								Imgproc.drawContours(dx_closex, contours, i, new Scalar(0,0,0), -1);
							}
						}
						contours.clear();
						Mat kernelnull = new Mat();
						Mat dx_closexx = new Mat(plateImage.size(), CvType.CV_8UC1);
						Imgproc.morphologyEx(dx_closex, dx_closexx, Imgproc.MORPH_CLOSE, kernelnull,new Point(-1, -1), 2);
						dx.release();
						kernelnull.release();


						Mat kernely = Imgproc.getStructuringElement(Imgproc.MORPH_RECT ,new Size(10,2));
						Mat dy = new Mat(plateImage.size(), CvType.CV_8UC1);
						Imgproc.Sobel(res, dy, CvType.CV_16S, 0, 2);
						Core.convertScaleAbs(dy, dy);
						Core.normalize(dy, dy, 0, 255, Core.NORM_MINMAX);
						Imgproc.threshold(dy, close, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
						Mat dy_closey = new Mat(plateImage.size(), CvType.CV_8UC1);
						Imgproc.morphologyEx(close, dy_closey, Imgproc.MORPH_DILATE, kernely);
						kernely.release();
						Imgproc.findContours(dy_closey, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
						Log.d("TAG", "contours number: " + contours.size());
						for (int i = 0; i < contours.size(); ++i)
						{
							rect = Imgproc.boundingRect(contours.get(i));
							if(rect.width / rect.height  > 5) {
								Imgproc.drawContours(dy_closey, contours, i, new Scalar(255,255,255), -1);
							} else {
								Imgproc.drawContours(dy_closey, contours, i, new Scalar(0,0,0), -1);
							}
						}
						contours.clear();
						Mat kernelnullY = new Mat();
						Mat dy_closeyy = new Mat(plateImage.size(), CvType.CV_8UC1);
						Imgproc.morphologyEx(dy_closey, dy_closeyy, Imgproc.MORPH_CLOSE, kernelnullY,new Point(-1, -1), 2);
						kernelnullY.release();
						dy.release();
						dy_closey.release();


						Core.bitwise_and(dx_closexx, dy_closeyy, res);

						LinkedList<Point> centroids = new LinkedList<Point>();
						Imgproc.findContours(res, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
						res.release();
						hierarchy.release();
						Moments mom = new Moments();
						double x = 0;
						double y = 0;
						Log.v("TAG", "contours number: " + contours.size());
						for (int i = 0; i < contours.size(); ++i)
						{
							mom = Imgproc.moments(contours.get(i));
							x = mom.get_m10() / mom.get_m00();
							y = mom.get_m01() / mom.get_m00();
							//Core.circle(plateImage, new Point(x, y), 4, new Scalar(0, 255, 0), -1);
							centroids.addFirst(new Point(x, y));

							//Core.putText(plateImage, Integer.toString(i), new Point(x, y), Core.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(255,0,0));
						}
						
						if(centroids.size() != 100) {
							isSuccess = false;
							return plateImage;
						} else {
						
							LinkedList<Point> centroidsSorted = new LinkedList<Point>();
							Comparator<Point> comparator = new Comparator<Point>() {
								public int compare(Point p1, Point p2) {
									return (int)(p1.x - p2.x);
								}
							};
							for(int i = 0; i < 10; i++) {
								List<Point> sub = centroids.subList(10 *i , 10 * i + 10);
								Collections.sort(sub, comparator);
								centroidsSorted.addAll(sub);
							}
							Mat output = Mat.zeros(new Size(450,450), CvType.CV_8UC3);
							Mat src = new Mat(4,2,CvType.CV_32F);
							Mat dst = new Mat(4,2,CvType.CV_32F);
							Mat warp = new Mat(new Size(450, 450), CvType.CV_8UC1);
							src.put(0, 0, centroidsSorted.get(0).x);
							src.put(0, 1, centroidsSorted.get(0).y);
							src.put(1, 0, centroidsSorted.get(9).x);
							src.put(1, 1, centroidsSorted.get(9).y);
							src.put(2, 0, centroidsSorted.get(90).x);
							src.put(2, 1, centroidsSorted.get(90).y);
							src.put(3, 0, centroidsSorted.get(99).x);
							src.put(3, 1, centroidsSorted.get(99).y);
							dst.put(0, 0, 0);
							dst.put(0, 1, 0);
							dst.put(1, 0, 450);
							dst.put(1, 1, 0);
							dst.put(2, 0, 0);
							dst.put(2, 1, 450);
							dst.put(3, 0, 450);
							dst.put(3, 1, 450);
							Mat transform = Imgproc.getPerspectiveTransform(src, dst);
							//透视变换
							Imgproc.warpPerspective(res2, warp, transform, new Size(450, 450));
							warp.colRange(0, 449).rowRange(0, 449).copyTo(output.colRange(0, 449).rowRange(0, 449));
							Mat outputBlur = Mat.zeros(new Size(450,450), CvType.CV_8UC3);
							Imgproc.GaussianBlur(output, outputBlur, new Size(5.0,5.0), 0);
							Core.addWeighted(output, 1.70, outputBlur, -0.3, 5, plateImage);
							isSuccess = true;
						}
					}
				}
				
				return plateImage;
	}
	
	private void showResult(){
		ImageView extractedSudokuImageView  = (ImageView)findViewById(R.id.extracted_sudoku);
		extractedSudokuImageView.setImageBitmap(mPlateBitmap == null ? getBitmap(mSudokuImageFile.getPath()):mPlateBitmap);
	}
}


