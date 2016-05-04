import java.util.*;

public class BasicBlockExample {

	public static void main(String[] args) throws InterruptedException{
		int N = Integer.parseInt(args[0]);
		int x = 0, a = 1, m = 0, i = 0;
		ArrayList<Integer> alist = new ArrayList<Integer>();
		
		if(N > 50) {
			// Basic block 1 starts
			x++;
			x = x * N;
			x &= (a << 1);
			alist.add(x);
			// Basic block 1 ends
		}  else {
			do{
			// Basic block 2 starts
				x += i;
				m = (x >> (N - i) & 1);
				i++;
				alist.add(x);
			// Basic block 2 ends
			}while(i < N);
		    }
		if(x % 2 == 0) {
			// Basic block 3 starts
			x = x * N;
			x %= N/3.14;
			x |= 34;
			java.lang.Thread.sleep(1);
			// Basic block 3 ends			
		}
	}
}
