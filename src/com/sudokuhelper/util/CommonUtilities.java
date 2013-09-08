package com.sudokuhelper.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

public class CommonUtilities {
	private static final String TAG = "CommonUtilities";
	public static final String APP_NAME = "SudokuHelper";
	/**
	 * DCIM����App���ֵ��ļ���ZJUSudokuHelper
	 */
	public static final File DIRECTORY_SKH = getDcimDirectory(APP_NAME);

	/**
	 * ����DCIM�ļ��������MySudoku.jpg
	 * 
	 * @return imageFile
	 */
	public static File getNewImageFile() {
		String imageName = "MySudoku.jpg";
		File imageFile = new File(DIRECTORY_SKH, imageName);
		return imageFile;
	}

	/**
	 * ����DCIM�µ��ļ���dirName
	 * 
	 * @param dirName
	 * @return dcimDirectory
	 */
	public static File getDcimDirectory(String dirName) {
		if (dirName == null) {
			return null;
		}
		File dcimDirectory = null;
		String dcimPath = Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_DCIM).getPath();
		dcimDirectory = new File(dcimPath, dirName);
		if (!dcimDirectory.exists()) {
			dcimDirectory.mkdirs();
		}
		return dcimDirectory;
	}

	/**
	 * UriתͼƬ�ļ�·��
	 * 
	 * @param context
	 * @param uri
	 * @return imagePath
	 */
	public static String uriToImagePath(Context context, Uri uri) {
		if (context == null || uri == null) {
			return null;
		}
		String imagePath = null;
		String uriString = uri.toString();
		String uriSchema = uri.getScheme();
		if (uriSchema.equals(ContentResolver.SCHEME_FILE)) {
			imagePath = uriString.substring("file://".length());
		} else {// uriSchema.equals(ContentResolver.SCHEME_CONTENT)
			ContentResolver resolver = context.getContentResolver();
			Cursor cursor = resolver.query(uri, null, null, null, null);
			if (cursor.getCount() == 0) {
				Log.e(TAG, "Uri(" + uri.toString() + ") not found!");
				return null;
			}
			cursor.moveToFirst();
			imagePath = cursor.getString(1);
			uri = Uri.fromFile(new File(imagePath));
		}
		Log.v(TAG, "Final uri: " + uri.toString());
		return imagePath;
	}

	/**
	 * ��ͼƬUriת����Ӧ��SDͼƬ·����
	 * 
	 * @param context
	 * @param uri
	 * @return imagePath
	 */
	public static String templateUriToPath(Context context, Uri uri) {
		if (context == null || uri == null) {
			return null;
		}
		String imagePath = "/mnt/sdcard/sudokuHelper/sudoku_template/";
		String uriString = uri.toString();
		String fileName = uriString.substring(uriString.lastIndexOf("/") + 1);
		imagePath = imagePath + fileName + ".png";
		Log.v(TAG, "Final uri: " + uriString);
		return imagePath;
	}

	/**
	 * �����ļ� srcFile --> objFile
	 * 
	 * @param srcFile
	 * @param objFile
	 * @return true �ɹ� false ʧ��
	 */
	public static boolean fileCopy(File srcFile, File objFile) {
		if (srcFile == null || objFile == null) {
			return false;
		}
		boolean result;
		try {
			InputStream iStream = new FileInputStream(srcFile);
			OutputStream oStream = new FileOutputStream(objFile);
			streamCopy(iStream, oStream);
			iStream.close();
			oStream.close();
			result = true;
		} catch (IOException e) {
			e.printStackTrace();
			result = false;
		}
		return result;
	}

	/**
	 * �������������
	 * 
	 * @param is
	 * @param os
	 * @throws IOException
	 */
	public static void streamCopy(InputStream is, OutputStream os)
			throws IOException {
		if (is == null || os == null) {
			return;
		}
		byte[] buffer = new byte[1024];
		int len;
		while ((len = is.read(buffer)) > 0) {
			os.write(buffer, 0, len);
		}
	}

	/**
	 * �õ�imagePath�ļ�·����ͼƬ�ļ���Bitmap
	 * 
	 * @param imagePath
	 * @return bitmap
	 */
	public static final Bitmap getBitmap(String imagePath) {
		if (imagePath == null) {
			return null;
		}
		Bitmap bitmap = getBitmap(imagePath, 1);
		return bitmap;
	}

	/**
	 * �õ�imagePath�ļ�·����ͼƬ�ļ���Bitmap, inSamepleSize = output.width / 200
	 * Ĭ�ϴ����ͼƬ�ļ�Ϊ450
	 * 
	 * @param imagePath
	 * @param inSampleSize
	 * @return
	 */
	public static Bitmap getBitmap(String imagePath, int inSampleSize) {
		if (imagePath == null) {
			return null;
		}
		if (inSampleSize == 0) {
			inSampleSize = 1;
		}
		Bitmap bmp = null;
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inPreferredConfig = Bitmap.Config.ARGB_4444;
		opts.inSampleSize = inSampleSize;
		bmp = BitmapFactory.decodeFile(imagePath, opts);
		return bmp;
	}

	/**
	 * ͼƬ�������ѹ��
	 */
	public static boolean imgCondense(String imagePath) {
		// String path = srcFile.getAbsolutePath();
		// File objFile = new File(path);
		
		Bitmap bmp = null;
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inPreferredConfig = Bitmap.Config.ARGB_4444;
		int inSampleSize ;
		opts.inJustDecodeBounds = true;//���ص�bitmap�������ڴ�ռ䣬ֻ����������Ϣ
		bmp = BitmapFactory.decodeFile(imagePath, opts);//bmp���ؿգ�ֻ��Ϊ�˵õ�opts
		Log.d("opts.outWidth: ", Integer.toString(opts.outWidth));
		if(opts.outWidth > 1000) {
			inSampleSize = computeSampleSize(opts, -1, 450*450*4);
			if(inSampleSize < 0) {
				inSampleSize = 1;
			}
		} else {
			inSampleSize = 1;
		}
		Log.d("inSampleSize: ", Integer.toString(inSampleSize));
		opts.inSampleSize = inSampleSize;
		opts.inJustDecodeBounds = false;
		bmp = BitmapFactory.decodeFile(imagePath, opts);
		Log.d("bmp width: ", Integer.toString(bmp.getWidth()));
		Log.d("bmp height: ", Integer.toString(bmp.getHeight()));
		File file = new File(imagePath);
		try {
			FileOutputStream out = new FileOutputStream(file);
			if (bmp.compress(Bitmap.CompressFormat.PNG, 100, out)) {
				out.flush();
				out.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}

	public static int computeSampleSize(BitmapFactory.Options options,
			int minSideLength, int maxNumOfPixels) {
		int initialSize = computeInitialSampleSize(options, minSideLength,
				maxNumOfPixels);

		int roundedSize;
		if (initialSize <= 8) {
			roundedSize = 1;
			while (roundedSize < initialSize) {
				roundedSize <<= 1;
			}
		} else {
			roundedSize = (initialSize + 7) / 8 * 8;
		}

		return roundedSize;
	}

	private static int computeInitialSampleSize(BitmapFactory.Options options,
			int minSideLength, int maxNumOfPixels) {
		double w = options.outWidth;
		double h = options.outHeight;

		int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math
				.sqrt(w * h / maxNumOfPixels));
		int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(
				Math.floor(w / minSideLength), Math.floor(h / minSideLength));

		if (upperBound < lowerBound) {
			// return the larger one when there is no overlapping zone.
			return lowerBound;
		}

		if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
			return 1;
		} else if (minSideLength == -1) {
			return lowerBound;
		} else {
			return upperBound;
		}
	}
}
