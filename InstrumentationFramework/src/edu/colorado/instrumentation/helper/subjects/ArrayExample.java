package edu.colorado.instrumentation.helper.subjects;


public class ArrayExample {
	public static void main(String[] args) {
   
   int x,y;
   int[] arr = new int[2];
   x = Integer.parseInt(args[0]);
   y = Integer.parseInt(args[1]);
   
   //if(x == 2)
   calculate(x, y, arr);
   
   System.out.print("arr[0] = " + x + y);
  }

public static void calculate(int x, int y, int[] arr) {
	
	 arr[0] = x + y;
	 arr[1] = y; 
	 System.out.println("arr[0]: " + arr[0]);
  }

}

