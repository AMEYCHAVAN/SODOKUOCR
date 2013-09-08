 
package com.sudokuhelper;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;

//import com.sudokuhelper.db.SudokuDatabase;
import com.sudokuhelper.game.CellCollection;
import com.sudokuhelper.game.SudokuGame;

public class SudokuListActivity extends ListActivity{
	private SimpleCursorAdapter mAdapter;
	private Cursor mCursor;
	//private SudokuDatabase mDatabase;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sudoku_list);
		//Database = new SudokuDatabase(getApplicationContext());
		mAdapter = new SimpleCursorAdapterEx(this, R.layout.sudoku_list_item,
				null, new String[]{"data", "name","time", "state"},
				new int[]{R.id.sudoku_board ,R.id.name, R.id.time, R.id.state});
		mAdapter.setViewBinder(new SudokuListViewBinder(this));
		updateList();
		setListAdapter(mAdapter);
	}
	class SimpleCursorAdapterEx extends SimpleCursorAdapter{
		public SimpleCursorAdapterEx(Context context, int layout, Cursor c,
				String[] from, int[] to) {
			super(context, layout, c, from, to);
		}
		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			super.bindView(view, context, cursor);
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		//mDatabase.close();
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		playSudoku(id);
	}
	
	private void playSudoku(long sudokuID) {
		Intent i = new Intent(SudokuListActivity.this, SudokuPlayerActivity.class);
		i.putExtra("sudoku_id", sudokuID);
		startActivity(i);
	}
	
	/**
	 * Updates whole list.
	 */
	private void updateList() {
	//	mCursor = mDatabase.getSudokuList();
		startManagingCursor(mCursor);
		mAdapter.changeCursor(mCursor);
	}
	private static class SudokuListViewBinder implements ViewBinder {
		private Context mContext;
		public SudokuListViewBinder(Context context) {
			mContext = context;
		}
		public boolean setViewValue(View view, Cursor c, int columnIndex) {
			int state = c.getInt(c.getColumnIndex("state"));
			TextView label = null;
			switch (view.getId()) {
				case R.id.sudoku_board:
					String data = c.getString(columnIndex);
					CellCollection cells = null;
					try {
						cells = CellCollection.deserialize(data);
					} catch (Exception e) {
						long id = c.getLong(c.getColumnIndex("_id"));
						Log.e("SudokuListActivity", String.format("Exception occurred when deserializing puzzle with id %s.", id), e);
					}
					SudokuBoardView board = (SudokuBoardView) view;
					board.setReadOnly(true);
					board.setFocusable(false);
					((SudokuBoardView) view).setCells(cells);
					break;
				case R.id.name:
					String name = c.getString(columnIndex);
					label = ((TextView) view);
					if (name == null || name.trim() == "") {
						((TextView) view).setVisibility(View.GONE);
					} else {
						((TextView) view).setText(name);
					}
					label.setVisibility((name == null || name.trim().equals("")) ? View.GONE
									: View.VISIBLE);
					label.setText(name);
					break;
				case R.id.time:
					String timeString = c.getString(columnIndex);
					label = ((TextView) view);
					label.setVisibility(timeString == null ? View.GONE
							: View.VISIBLE);
					label.setText(timeString);
					if (state == SudokuGame.GAME_STATE_COMPLETED) {
						label.setTextColor(Color.rgb(187, 187, 187));
					} else {
						label.setTextColor(Color.rgb(255, 255, 255));
					}
					break;
				case R.id.state:
					label = ((TextView) view);
					String stateString = null;
					switch (state) {
						case SudokuGame.GAME_STATE_COMPLETED:
							stateString = mContext.getString(R.string.solved);
							break;
						case SudokuGame.GAME_STATE_PLAYING:
							stateString = mContext.getString(R.string.playing);
							break;
					}
					label.setVisibility(stateString == null ? View.GONE
							: View.VISIBLE);
					label.setText(stateString);
					if (state == SudokuGame.GAME_STATE_COMPLETED) {
						label.setTextColor(Color.rgb(187, 187, 187));
					} else {
						label.setTextColor(Color.rgb(255, 255, 255));
					}
					break;
			}
			return true;
		}
	}
}
