package com.sudokuhelper;

import android.R.string;
import android.content.Context;
import android.widget.Toast;

public class backtrack {
	String[][] allowedPosition;
	String[][] finalNumber;
	int counter22=0;
	static int[][] integerArray2 = new int[20][20]; 
	static int[][] integerArray222 = new int[20][20]; 
	
		

    String soans="";
    String soraw="";

public  String  algo(String soraw) {
 
System.out.print("%%%%raw "+soraw);

	
 	phase1(soraw);
 	phase3();
 	phasecheak();
     solve(0,0);
	
	
	 
	 //soans="999999999123123123123123123123123123123123123123123123123123123123123123123123123";
System.out.print("%%%%answer"+soans);
	
	
	return soans;
}

 
//=================
public void phase4() {
	 
	//canver tabch to string

	 int i=0;
    for(int j=1;j<10;j++)
    {for (int k=1;k<10;k++)
    {
         
 
     soans=soans+integerArray222[j-1][k-1];
    
    }
    
 }
	
}

//===========================

public void phase1(String soraw) {
//soraw="123000000000000000000000000000000000000000000000000000000000000000000000000000000";
 
	 int i=0;
     for(int j=1;j<10;j++)
     {for (int k=1;k<10;k++)
     {
          
  
   //   char a=s.charAt(i);
      //String b=""+a;
      int xx=Integer.parseInt(""+soraw.charAt(i++));
    // System.out.println(xx*2);
     
      integerArray2[j][k]=xx;
     
     }
     
  }
	
}

//================================================
public void phasecheak() {
	int couu=0;
	for(int i=0;i<9;i++)    	{    		
		couu=0;
    	for(int i22=0;i22<9;i22++){couu=0;
    		for(int i222=0;i222<9;i222++){
    	if(couu==2)
    	{phase4();
    	Toast.makeText(getApplicationContext(), ">>Invalid Sudoku<", Toast.LENGTH_LONG).show();	
}
    	if(integerArray222[i][i22]==integerArray222[i][i222] &&integerArray222[i][i22]!=0 &&integerArray222[i][i222]!=0 )
    	{couu++; }
	
	}}    	}
	
	
	
	for(int i=0;i<9;i++)    	{    		
		couu=0;
    	for(int i22=0;i22<9;i22++){couu=0;
    		for(int i222=0;i222<9;i222++){
    	if(couu==2)
    	{phase4();
    	Toast.makeText(getApplicationContext(), ">>Invalid Sudoku<", Toast.LENGTH_LONG).show();	
}
    	if(integerArray222[i22][i]==integerArray222[i222][i] &&integerArray222[i22][i]!=0 &&integerArray222[i222][i]!=0 )
    	{couu++; }
	
	}}    	}
	
	
	
	
	
	
}

public Context getApplicationContext() {
	// TODO Auto-generated method stub
	return null;
}


public void phase3() 
{   for(int i = 1; i<10; i++){
		for(int j = 1; j<10; j++){
	integerArray222[i-1][j-1]=integerArray2[i][j];
		}}    	
}

//=================================================
public int safe(int row, int col, int n)
{

int r, c, br, bc;
if (integerArray222[row][col] == n) return 1;
if (integerArray222[row][col] != 0) return 0;

for (c = 0; c < 9; c++)
if (integerArray222[row][c] == n) return 0;

for (r = 0; r < 9; r++)
if (integerArray222[r][col] == n) return 0;

br = row / 3;
bc = col / 3;

for (r = br * 3; r < ((br + 1) * 3); r++)
for (c = bc * 3; c < ((bc + 1) * 3); c++)
if (integerArray222[r][c] == n) return 0;


 
return 1;
}

public int solve(int row, int col)
{
  
int n, t;
 
if (row == 9 && counter22==0 )
{ 

    counter22++;
 	 
    phase4(); 
     return 0; 
}
if(row != 9 && counter22==0)
{
			    	
	for (n = 1; n <= 9; n++){
if (safe(row,col,n)==1) 
 		{
		t = integerArray222[row][col];
		integerArray222[row][col] = n;
		if (col == 8)
			solve(row + 1, 0);
		else
			solve(row, col + 1);

		integerArray222[row][col] = t;
 		}				}
}
    

 
  return 0;
}

 


//==============================================




}
