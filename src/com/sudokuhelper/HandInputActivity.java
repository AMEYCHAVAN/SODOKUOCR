 

package com.sudokuhelper;
import android.R.string;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
//import com.sudokuhelper.db.SudokuDatabase;
import com.sudokuhelper.game.CellCollection;
import com.sudokuhelper.game.SudokuGame;
import com.sudokuhelper.inputmethod.IMControlPanel;
import com.sudokuhelper.inputmethod.IMControlPanelStatePersister;
import com.sudokuhelper.inputmethod.IMNumpad;
import com.sudokuhelper.inputmethod.IMPopup;
import com.sudokuhelper.inputmethod.IMSingleNumber;
@SuppressWarnings("unused")
public class HandInputActivity extends Activity{
	private SudokuBoardView mSudokuBoard;
	private SudokuGame mSudokuGame;
	private backtrack back;
	private String level;
	private SolveSudoku solveSudoku;
	private RelativeLayout rootLayout;
	private IMControlPanel mIMControlPanel;
	private IMControlPanelStatePersister mIMControlPanelStatePersister;
	private IMPopup mIMPopup;
	private IMSingleNumber mIMSingleNumber;
	private IMNumpad mIMNumpad;
	//private SudokuDatabase mDatabase;
	private HintsQueue mHintsQueue;
	private boolean isFirst;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hand_input);
		isFirst = true;
		mSudokuBoard = (SudokuBoardView)findViewById(R.id.sudoku_board);
		rootLayout = (RelativeLayout)findViewById(R.id.root_layout);
		String data;
		String null_data = "000000000000000000000000000000000000000000000000000000000000000000000000000000000";
		Bundle bundle = this.getIntent().getExtras();
		if(bundle != null) {
			String recognizedStr = bundle.getString("recognizedStr");
			data = recognizedStr;
			rootLayout.setBackgroundResource(R.drawable.sudokuplayer_bk);
		}else{
			data = null_data;
		}
		mSudokuGame = new SudokuGame();
		mSudokuGame.setId(0);
		mSudokuGame.setCells(CellCollection.deserialize(data));
		mSudokuGame.setState(0);
		mSudokuGame.setNote("");
		mSudokuGame.setTime(0);
		level = mSudokuGame.getCells().getDataFromCellCollection();
		for(int i = 0; i < level.length(); i++)
		{
			int row = i / 9 ;
		    int column = i % 9 ;
			mSudokuGame.getCells().getCell(row, column).setEditable(true);
		}
 
		mSudokuBoard.setGame(mSudokuGame);
		mHintsQueue = new HintsQueue(this);
		mHintsQueue.showOneTimeHint("welcome", R.string.welcome, R.string.first_run_hint);		
		mIMControlPanel = (IMControlPanel)findViewById(R.id.input_methods);
		mIMControlPanel.initialize(mSudokuBoard, mSudokuGame, mHintsQueue);
		//mIMControlPanel.activateFirstInputMethod();
		mIMControlPanel.activateInputMethod(1);
		mIMControlPanelStatePersister = new IMControlPanelStatePersister(this);
        mIMPopup = mIMControlPanel.getInputMethod(IMControlPanel.INPUT_METHOD_POPUP);
        mIMSingleNumber = mIMControlPanel.getInputMethod(IMControlPanel.INPUT_METHOD_SINGLE_NUMBER);
        mIMNumpad = mIMControlPanel.getInputMethod(IMControlPanel.INPUT_METHOD_NUMPAD);
    //    mDatabase = new SudokuDatabase(getApplicationContext());
        solveSudoku = new SolveSudoku();
	
		Button solveButton = (Button)findViewById(R.id.solve);
		solveButton.setOnClickListener(
				new OnClickListener() {
					public void onClick(View v) {
						AlertDialog.Builder builder = new Builder(HandInputActivity.this);
						builder.setMessage("Do you want to Solve soudoku Automatically");
						builder.setTitle("Information");
			//
						builder.setPositiveButton("Yes", new android.content.DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
								level = mSudokuBoard.getCells().getDataFromCellCollection();
								CellCollection cc = mSudokuBoard.getCells();
//level sring contain user edited sudoko
								System.out.println("usereditedsudoko::::"+level);
								backtrack b1=new backtrack();
								String answer=b1.algo(level);
								
								
						//		String answer="123123123123123123123123123123123123123123123123123123123123123123123123123123123";
								mSudokuGame.setCells(CellCollection.deserialize(answer));
							 
								mSudokuBoard.setGame(mSudokuGame);
								mHintsQueue.showHint(R.string.complete_sudoku, R.string.complete_sudoku_info1);
							 	
								System.out.println("#ameyanswer#"+answer);
							}
							
						});
						builder.setNegativeButton("NO", new android.content.DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
							
						});
						builder.create().show();
					}
				});
	
//======
		 
//===========
		
	}	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		 
	}
}
