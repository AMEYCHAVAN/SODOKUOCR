
package com.sudokuhelper;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
//import com.sudokuhelper.db.SudokuDatabase;
import com.sudokuhelper.game.CellCollection;
import com.sudokuhelper.game.SudokuGame;
import com.sudokuhelper.inputmethod.IMControlPanel;
import com.sudokuhelper.inputmethod.IMControlPanelStatePersister;
import com.sudokuhelper.inputmethod.IMNumpad;
import com.sudokuhelper.inputmethod.IMPopup;
import com.sudokuhelper.inputmethod.IMSingleNumber;

@SuppressWarnings("unused")
public class SudokuPlayerActivity extends Activity{
	//private SudokuDatabase mDatabase;
	private SudokuBoardView mSudokuBoard;
	private long mSudokuGameID;
	private SudokuGame mSudokuGame;
	private String level;
	private SolveSudoku solveSudoku;
	private IMControlPanel mIMControlPanel;
	private IMControlPanelStatePersister mIMControlPanelStatePersister;
	private IMPopup mIMPopup;
	private IMSingleNumber mIMSingleNumber;
	private IMNumpad mIMNumpad;
	private HintsQueue mHintsQueue;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sudoku_play);
		//mDatabase = new SudokuDatabase(getApplicationContext());
		mSudokuBoard = (SudokuBoardView)findViewById(R.id.sudoku_board);
		mSudokuGameID = getIntent().getLongExtra("sudoku_id", 1);
      //  mSudokuGame = mDatabase.getSudoku(mSudokuGameID);
        level = mSudokuGame.getCells().getDataFromCellCollection();
        Log.d("mSudokuGameID", Long.toString(mSudokuGameID));
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
        solveSudoku = new SolveSudoku();
		
		Button solveButton = (Button)findViewById(R.id.solve);
		solveButton.setOnClickListener(
				new OnClickListener() {
					public void onClick(View v) {
						AlertDialog.Builder builder = new Builder(SudokuPlayerActivity.this);
						builder.setMessage("are you sure 25¿");
						builder.setTitle("information");
						builder.setPositiveButton("yes", new android.content.DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
								level = mSudokuBoard.getCells().getDataFromCellCollection();
								CellCollection cc = mSudokuBoard.getCells();
								System.out.println(level);
								if(!cc.isEmpty()) {
									if(cc.validate()) {
										if(!cc.isCompleted()) {
											String answer = solveSudoku.solve(level);
											if(answer.equals(SolveSudoku.LOAD_ERROR)) {
												mHintsQueue.showHint(R.string.load_error, R.string.load_error_info);
											} else if(answer.equals(SolveSudoku.SOLVE__ERROR)) {
												mHintsQueue.showHint(R.string.solve_error, R.string.solve_error_info);
											} else if(answer.equals(SolveSudoku.COMMON_ERROR)){
												mHintsQueue.showHint(R.string.common_error, R.string.common_error_info);
											} else {
												mSudokuGame.setCells(CellCollection.deserialize(answer));
												for(int i = 0; i < level.length(); i++)
												{
													if(level.charAt(i) == '0') {
														int row = i / 9 ;
													    int column = i % 9 ;
														mSudokuGame.getCells().getCell(row, column).setEditable(true);
													}
												}
												mSudokuBoard.setGame(mSudokuGame);
												mHintsQueue.showHint(R.string.complete_sudoku, R.string.complete_sudoku_info1);
												System.out.println("=============================");
												System.out.println(answer);
											}
											
										} else {
											mHintsQueue.showHint(R.string.complete_sudoku, R.string.complete_sudoku_info);
										}
										
									} else {
										mHintsQueue.showHint(R.string.wrong_sudoku, R.string.wrong_sudoku_info);
									}
								} else {
									mHintsQueue.showHint(R.string.empty_sudoku, R.string.empty_sudoku_info);
								}
							}
						});
						builder.setNegativeButton("no", new android.content.DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						});
						builder.create().show();
					}
				});
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
	//	mDatabase.close();
	}
	
}
