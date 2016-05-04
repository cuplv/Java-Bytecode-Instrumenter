import java.util.*;
public class ModularExponentiation {

	public static void main(String args[]) throws InterruptedException {
		
		
		Integer x = Integer.parseInt(args[0]);
		Integer e = Integer.parseInt(args[1]);
		Integer N = Integer.parseInt(args[2]);
		
		squareMult(x, e, N);
	}
	
	public static Integer squareMult(Integer x, Integer e, Integer N) throws InterruptedException{
		
		ArrayList<Integer> alist = new ArrayList<Integer>();
		Integer y = 1;
		int n = Integer.SIZE;
		
		int i = n;
		do {
			y = y * y;
			y = y % N;
			
			if(((e >> (n-i)) & 1) == 1){
				y = y * x;
				java.lang.Thread.sleep(10);
				y = y % N;
			}
		   i--;
           	   alist.add(y);
		} while(i >= 1);
		
		return y;
	}
}
