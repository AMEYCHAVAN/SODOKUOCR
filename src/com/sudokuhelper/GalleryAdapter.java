
package com.sudokuhelper;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class GalleryAdapter extends BaseAdapter{


	private Context		mContext;

	private Integer[]	mImageIds	= 
	{ 
			R.drawable.sudoku_template1, 
			R.drawable.sudoku_template2,
	};
	public GalleryAdapter(Context c)
	{
		mContext = c;
	}

	public int getCount()
	{
		return mImageIds.length;
	}

	public Object getItem(int position)
	{
		return position;
	}

	public long getItemId(int position)
	{
		return position;
	}

	public Integer getcheckedImageIDPosition(int position) {
		return mImageIds[position];
	}
	public View getView(int position, View convertView, ViewGroup parent)
	{
		ImageView imageView;
		if (convertView == null)
		{

			imageView = new ImageView(mContext);

			imageView.setLayoutParams(new GridView.LayoutParams(200, 200));

			imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
		}
		else
		{
			imageView = (ImageView) convertView;
		}
		imageView.setImageResource(mImageIds[position]);
		return imageView;
	}
}
