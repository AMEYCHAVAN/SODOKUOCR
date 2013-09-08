
package com.sudokuhelper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

class Node {
	int count;
	int row;

	Node L;
	Node R;
	Node U;
	Node D;

	Node C;

}

public class SolveSudoku {
	public static final String COMMON_ERROR = "1";
	public static final String LOAD_ERROR = "2";
	public static final String SOLVE__ERROR = "3";
	ArrayList<Integer> partialSolution;
	ArrayList<Node> listHeaders;
	ArrayList<Node> rowHeaders;
	Node matrixHeader;
	private String answer;
	
	
	public SolveSudoku() {
		partialSolution = new ArrayList<Integer>();
		listHeaders = new ArrayList<Node>();
		rowHeaders = new ArrayList<Node>();
		matrixHeader = new Node();
		answer = new String("");
	}
	
	
	public String solve(String level) {
		if (!initMatrix(matrixHeader)) {
			return COMMON_ERROR;
		}
		if (!loadLevel(matrixHeader,level)) {
			return LOAD_ERROR;
		}
		if (solveLevel(matrixHeader)) {
			return answer;
		} else {
			return SOLVE__ERROR;
		}
	}
	boolean initMatrix(Node matrixHeader) {
		matrixHeader.count = 0;
		matrixHeader.U = matrixHeader.D = null;
		matrixHeader.L = matrixHeader.R = matrixHeader;
		Node cur = matrixHeader;
		for (int j = 1; j <= 324; j++) {
			Node tempNode = new Node();
			tempNode.count = 0;
			tempNode.U = tempNode.D = tempNode;
			tempNode.L = cur;
			cur.R = tempNode;
			tempNode.R = matrixHeader;
			matrixHeader.L = tempNode;
			cur = cur.R;
			listHeaders.add(tempNode);
		}
		int rcCursor = 1;
		int rnCursor = 82;
		int cnCursor = 163;
		int bnCursor = 244;
		int rnCursorHelper = rnCursor;
		int bnCursors[] = { 244, 253, 262, 244, 253, 262, 244, 253, 262, 271,
				280, 289, 271, 280, 289, 271, 280, 289, 298, 307, 316, 298,
				307, 316, 298, 307, 316 , 325};
		int index = 0;
		for (int i = 1; i <= 729; i++) {
			Node rcNode = new Node();
			rcNode.row = i;
			addNodeToColumn(listHeaders.get(rcCursor - 1), rcNode);
			rowHeaders.add(rcNode);
			Node rnNode = new Node();
			rnNode.row = i;
			addNodeToColumn(listHeaders.get(rnCursor - 1), rnNode);
			Node cnNode = new Node();
			cnNode.row = i;
			addNodeToColumn(listHeaders.get(cnCursor - 1), cnNode);
			Node bnNode = new Node();
			bnNode.row = i;
			addNodeToColumn(listHeaders.get(bnCursor - 1), bnNode);
			rcNode.L = bnNode;
			rcNode.R = rnNode;
			rnNode.L = rcNode;
			rnNode.R = cnNode;
			cnNode.L = rnNode;
			cnNode.R = bnNode;
			bnNode.L = cnNode;
			bnNode.R = rcNode;
			if (i % 9 == 0) {
				rcCursor++;
				if (82 == rcCursor)
					rcCursor = 1;
			}
			rnCursor++;
			if (rnCursor == rnCursorHelper + 9) {
				rnCursor = rnCursorHelper;
			}
			if (i % 81 == 0) {
				rnCursorHelper += 9;
				rnCursor = rnCursorHelper;
			}
			cnCursor++;
			if (244 == cnCursor)
				cnCursor = 163;
			bnCursor++;
			if (bnCursor == bnCursors[index] + 9)
				bnCursor = bnCursors[index];
			if (i % 27 == 0) {
				index++;
				bnCursor = bnCursors[index];
			}
		}
		return true;
	}
	
	boolean destroyMatrix(Node matrixHeader) {

		return true;
	}

	boolean addNodeToColumn(Node listHeader, Node nodeToAdd) {
		Node tailNode = listHeader.U;
		tailNode.D = nodeToAdd;
		nodeToAdd.U = tailNode;
		nodeToAdd.D = listHeader;
		listHeader.U = nodeToAdd;

		nodeToAdd.C = listHeader;
		listHeader.count++;

		return true;
	}
	@SuppressWarnings("unused")
	void delRow(Node rowHeader) {
		Node cur = rowHeader.R;
		Node pre;
		while (cur != rowHeader) {
			cur.U.D = cur.D;
			cur.D.U = cur.U;
			cur.C.count--;
			pre = cur;
			cur = cur.R;
			// delete cur;
		}

		pre = null;
		cur.U.D = cur.D;
		cur.D.U = cur.U;
		cur.C.count--;
		// delete cur;
		cur = null;
	}
	
	boolean loadLevel(Node matrixHeader,String level) {
		int length = level.length();
		if (length != 81) {
			System.out.println("error: level file corrupted");
			return false;
		} else {
			int count = 0;
			for (int i = 0; i < length; i++) {
				if (level.charAt(i) < '0' || level.charAt(i) > '9') {
					System.out.println("error: invalid character in level");
					return false;
				}
				if (level.charAt(i) > '0') {
					count++;
				}
			}
			System.out.println("#21amey# "+count);  //gives count of elements
			if (count < 17) {
				System.out.println("error: insufficient clues");
				return false;
			}
						int matrix_row = 1; 
			int num = 0;

			for (int puzzle_row = 1; puzzle_row <= 9; puzzle_row++) {
				for (int puzzle_col = 1; puzzle_col <= 9; puzzle_col++) {
					num = level.charAt((puzzle_row - 1) * 9 + puzzle_col - 1) - 48;
					if (num != 0) // if the cell already exists a number
					{
						matrix_row = (puzzle_row - 1) * 81 + (puzzle_col - 1)* 9 + num;
						
						for (int i = matrix_row - num + 1; i < matrix_row; i++) {
							delRow(rowHeaders.get(i - 1));
						}
						for (int i = matrix_row + 1; i <= matrix_row - num + 9; i++)
						{
							delRow(rowHeaders.get(i - 1));
						}
					}
				}
			}
		}

		return true;
	}

	boolean solveLevel(Node matrixHeader) {
		search(matrixHeader,0);
		return true;
	}

	void search(Node matrixHeader,int depth) {
		if (matrixHeader.R == matrixHeader) {
			getAnswer();
			return;
		} else {
			// optimization: find the column with the least '1's
			Node cur = matrixHeader.R;
			Node c = cur;
			int least = cur.count;
			while (cur != matrixHeader) {
				if (cur.count < least) {
					least = cur.count;
					c = cur;
				}
				cur = cur.R;
			}
			cover(c);
			Node curColNode = c.D;
			Node curRowNode = curColNode.R;
			while (curColNode != c) // vertically
			{
				partialSolution.add(curColNode.row);
				while (curRowNode != curColNode) {
					cover(curRowNode.C);
					curRowNode = curRowNode.R;
				}
				search(matrixHeader,depth + 1);
				partialSolution.remove(partialSolution.size() - 1);

				curRowNode = curColNode.L;

				while (curRowNode != curColNode) {
					uncover(curRowNode.C);
					curRowNode = curRowNode.L;
				}
				curColNode = curColNode.D;
				curRowNode = curColNode.R;
			}
			uncover(c);
		}
	}

	void cover(Node listHeader) {
		listHeader.R.L = listHeader.L;
		listHeader.L.R = listHeader.R;
		Node curColNode = listHeader.D;
		Node curRowNode = curColNode.R;

		while (curColNode != listHeader) {
			while (curRowNode != curColNode) {
				curRowNode.D.U = curRowNode.U;
				curRowNode.U.D = curRowNode.D;
				curRowNode.C.count--;
				curRowNode = curRowNode.R;
			}
			curColNode = curColNode.D;
			curRowNode = curColNode.R;
		}
	}

	void uncover(Node listHeader) {
		Node curColNode = listHeader.U;
		Node curRowNode = curColNode.L;

		while (curColNode != listHeader) {
			while (curRowNode != curColNode) {
				curRowNode.D.U = curRowNode;
				curRowNode.U.D = curRowNode;
				curRowNode.C.count++;
				curRowNode = curRowNode.L;
			}
			curColNode = curColNode.U;
			curRowNode = curColNode.L;
		}
		listHeader.R.L = listHeader;
		listHeader.L.R = listHeader;
	}

	@SuppressWarnings("unused")
	void getAnswer() {
		
		Comparator<Integer> comparator_asc = new Comparator<Integer>(){
			public int compare(Integer s1, Integer s2) {
				return s1 - s2;
			}
		};
		int num;
		int row, col;
		int remainder;
		StringBuilder str = new StringBuilder();
		
		Collections.sort(partialSolution,comparator_asc);
		for (int i = 0; i < partialSolution.size(); i++) {
			row = (partialSolution.get(i) - 1) / 81 + 1;
			remainder = (partialSolution.get(i) - 1) % 81;
			col = remainder / 9 + 1;
			num = remainder % 9 + 1;
			str.append(num);
		}
		answer = str.toString();
		System.out.println("##23#"+partialSolution);
	}
}
