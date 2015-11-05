package edu.colorado.instrumentation.helper.subjects;


import java.util.Arrays;

public class Confidentiality_example_1 {
	public static void main(String[] args){
		int x = Integer.parseInt(args[0]);
		int n =  Integer.parseInt(args[1]);
		boolean[] e = new boolean[n];
		Arrays.fill(e, Boolean.TRUE);
		int N =  Integer.parseInt(args[2]);
		squareMult(x, e, n, N);
	}
	
	 // The execution time is directly proportional to the number of '1's in e, which reveals sensitive information to attackers.
		public static int squareMult(int x, boolean[] e, int n, int N) {
			int y = 1;
			for(int i = n - 1; i >= 0; --i) {
				y = y * y;
				y = y % N;
				if(e[i] == true) {
					y = y * x;
					y = y % N;
				}
			}
			return y;
		}
}

