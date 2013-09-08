 

package com.sudokuhelper;
import static com.sudokuhelper.util.CommonUtilities.streamCopy;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.ml.CvKNearest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {
	private Button camInputButton;
	private Button handInputButton;
	private Button puzzleLibraryButton;
	private Button aboutButton;
	private Button galleryInputButton;
	private static final int DIALOG_ABOUT = 0;
	public static CvKNearest model;
    private static final String TAG = "MainActivity";
    private ProgressDialog progressDialog;
        public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressDialog = new ProgressDialog(this);
		progressDialog.setMessage("Loading Libraries...");
		progressDialog.setTitle("Wait ...");
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.show();
        TrainDataThread mythread = new TrainDataThread();
        mythread.start();
        galleryInputButton = (Button)findViewById(R.id.galleryInput);
        galleryInputButton.setOnClickListener(
        		new OnClickListener() {
					public void onClick(View v) {
						//galleryInputButton.setBackgroundResource(R.drawable.galleryinput_hover);
						Intent intent = new Intent();
						intent.setClass(MainActivity.this, GalleryInputActivity.class);
						MainActivity.this.startActivity(intent);
					}
				});
         //=========================== puzze
        puzzleLibraryButton = (Button)findViewById(R.id.about);
        puzzleLibraryButton.setOnClickListener(
        		new OnClickListener() {
					public void onClick(View v) {
						Intent intent = new Intent();
						intent.setClass(MainActivity.this, SudokuListActivity.class);
						MainActivity.this.startActivity(intent);
					}
				});
        ///////////////////=========================
       
        camInputButton = (Button)findViewById(R.id.camInput);
        camInputButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, CamInputActivity.class);
				MainActivity.this.startActivity(intent);
			}
		});
       
        handInputButton = (Button)findViewById(R.id.handInput);
        handInputButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, HandInputActivity.class);
				
				MainActivity.this.startActivity(intent);
			}
		});
         
        aboutButton = (Button)findViewById(R.id.about);
        aboutButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showDialog(DIALOG_ABOUT);
			}
		});
    }
    /**
  
    */
	@Override
	protected Dialog onCreateDialog(int id) {
		LayoutInflater factory = LayoutInflater.from(this);
		switch(id) {
			case DIALOG_ABOUT :
				final View aboutView = factory.inflate(R.layout.about, null);
	            return new AlertDialog.Builder(this)
	                //.setIcon(R.drawable.ic_launcher)
	            .setIcon(R.drawable.joker)
	                .setTitle(R.string.app_name)
	                .setView(aboutView)
	                .setPositiveButton("OK", null)
	                .create();
		}
		return super.onCreateDialog(id);
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	/**
	 
	 * 
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode==KeyEvent.KEYCODE_BACK && event.getRepeatCount()==0){
			AlertDialog.Builder builder = new Builder(MainActivity.this);
			builder.setMessage("Warning");
			builder.setTitle("Are you Sure you want to exit?");
			builder.setPositiveButton("yes", new android.content.DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,
						int which) {
					dialog.dismiss();
					MainActivity.this.finish();
				}
			});
			builder.setNegativeButton("no", new android.content.DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,
						int which) {
					dialog.dismiss();
				}
				
			});
			builder.create().show();
			return false;
		}
		return false;
	}
	/**
 
	 */
  	public Mat loadData(String fileName) {
  		int row = 0;
  		int col = 0;
  		BufferedReader in;
  		try {
  			in = new BufferedReader(new FileReader(fileName));
  			ArrayList<String> listStr = new ArrayList<String>();
  			String s;
  			try {
  				while(( s = in.readLine()) != null) {
  					listStr.add(s);
  				}
  				in.close();
  			} catch (IOException e) {
  				e.printStackTrace();
  			}
  			
  			row = listStr.size();
  			col = listStr.get(0).split(" ").length;
  			Mat m = new Mat(row, col,CvType.CV_32F);
  			if(listStr != null) {
  				for(int i = 0; i < listStr.size(); i++) {
  					String []str = listStr.get(i).split(" ");
  					
  					for(int j = 0; j < str.length ; j++) {
  						double val = Double.parseDouble(str[j]);
  						m.put(i, j, val);
  					}
  				}
  				return m;
  			} 
  		} catch (FileNotFoundException e) {
  			e.printStackTrace();
  		}
  		return null;
  	}
  	/**
   
  	 */
    public void trainData() {
 
    	if(!OpenCVLoader.initDebug()) {
			Log.e(TAG, "initDebugError!");
		}
     
        model = new CvKNearest();
		String recPath = "/mnt/sdcard/sudokuHelper/";
		String samplesName = "generalsamples.data";
		String responseName = "generalresponses.data";
		File recDataDir = new File(recPath, "train_data");
		if (!recDataDir.exists()) {
			recDataDir.mkdirs();
			// copy assets/generalsamples.data to /mnt/sdcard/sudokuHelper/data/
			try {
				InputStream is = getAssets().open(samplesName);
				File samplesDataFile = new File(recDataDir, samplesName);
				try {
					OutputStream os = new FileOutputStream(samplesDataFile);
					streamCopy(is, os);
					os.close();
					Log.d(TAG, "copying file generalsamples.data success!");
				} catch (IOException e) {
					e.printStackTrace();
				}
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
 			try {
				InputStream is = getAssets().open(responseName);
				File responsesDataFile = new File(recDataDir, responseName);
				try {
					OutputStream os = new FileOutputStream(responsesDataFile);
					streamCopy(is, os);
					os.close();
					Log.d(TAG, "copying file generalresponses.data success!");
				} catch (IOException e) {
					e.printStackTrace();
				}
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		Mat samples = loadData(recPath + "train_data/" + samplesName);
		Mat response = loadData(recPath + "train_data/" + responseName);
		model.train(samples, response);
    }
    /**
    
     */
    public void initTemplate() {
    	String templatePath = "/mnt/sdcard/sudokuHelper/";
    	File templateDataDir = new File(templatePath, "sudoku_template");
    	String []templateArr = {"sudoku_template1.png","sudoku_template2.png"};
    	Integer []templateId = {R.drawable.sudoku_template1, R.drawable.sudoku_template2};
    	if (!templateDataDir.exists()) {
    		templateDataDir.mkdirs();
			// copy assets/generalsamples.data to /mnt/sdcard/sudokuHelper/data/
    		for(int i = 0; i < 2; i ++) {
    			try {
					InputStream is = getResources().openRawResource(templateId[i]);
					File templateDataFile = new File(templateDataDir, templateArr[i]);
					try {
						OutputStream os = new FileOutputStream(templateDataFile);
						streamCopy(is, os);
						os.close();
						Log.d(TAG, "copying file " + templateArr[i]+ " success!");
					} catch (IOException e) {
						e.printStackTrace();
					}
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
        	}	
    	}
    }
    /**
 
     */
    public Handler myHandler = new Handler();
    /**
   
     */
    Runnable showButton = new Runnable() {
        public void run() {
        	try {
        		camInputButton.setVisibility(View.VISIBLE);
            	handInputButton.setVisibility(View.VISIBLE);
            	puzzleLibraryButton.setVisibility(View.VISIBLE);
            	aboutButton.setVisibility(View.VISIBLE);
            	galleryInputButton.setVisibility(View.VISIBLE);
        	}catch(Exception e) {
        		e.printStackTrace();
        	}
        	
        }
    };
    /**
     
     */
    Runnable finishActivity = new Runnable() {
    	public void run() {
    		AlertDialog.Builder builder = new Builder(MainActivity.this);
			builder.setMessage("please wait its closing");
			builder.setTitle("wait");
			builder.setPositiveButton("È·¶¨", new android.content.DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,
						int which) {
					dialog.dismiss();
					MainActivity.this.finish();
				}
			});
			builder.create().show();
    	}
    };
   

    
  	class TrainDataThread extends Thread {
  		public void run() {
  			Looper.prepare();
  			if(existSDcard()) {
  				trainData();
  	  			initTemplate();
  	  			progressDialog.dismiss();
  	  			myHandler.post(showButton);
  			} else {
  				myHandler.post(finishActivity);
  			}
  			Looper.loop();
  		}
      }
   
  	
  	
    public static boolean existSDcard()
    {
        if (android.os.Environment.MEDIA_MOUNTED.equals(android.os.Environment.getExternalStorageState()))
            return true;
        else
            return false;
    }
}
