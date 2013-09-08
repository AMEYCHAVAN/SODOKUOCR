
package com.sudokuhelper;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.Toast;

public class GalleryActivity extends Activity {
	/** Called when the activity is first created. */
	private GalleryAdapter imageAdapter;
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sudoku_gallery);


		GridView gridview = (GridView) findViewById(R.id.gridview);

		imageAdapter = new GalleryAdapter(this);
		gridview.setAdapter(imageAdapter);
		gridview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int position, long id)
			{
				Toast.makeText(GalleryActivity.this, "Wait" + (position + 1) + " Notification", Toast.LENGTH_SHORT).show();
				Intent intent = new Intent();
				intent.setClass(GalleryActivity.this, GalleryInputActivity.class);
				Integer drawablePosition = imageAdapter.getcheckedImageIDPosition(position);
				Resources resource = getResources();
				Uri uri =  Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
					    + resource.getResourcePackageName(drawablePosition) + "/"
					    + resource.getResourceTypeName(drawablePosition) + "/"
					    + resource.getResourceEntryName(drawablePosition));
				intent.setDataAndType(uri, "image/**");
				GalleryActivity.this.setResult(RESULT_OK, intent);
				GalleryActivity.this.finish();
			}
		});
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
    
	
}
