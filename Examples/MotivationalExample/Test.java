public class Test {

	public static void main(String[] args){
		int N = Integer.parseInt(args[0]);
		int x = 0, a = 1, m = 0;
		if(N > 5) {
			// Basic block 1 starts
			x++;
			x = x * N;
			x &= (a << 1);
			int i = 0;
			// Basic block 1 ends
			do{
			// Basic block 2 starts
				x += i;
				m = (x >> (N - i) & 1);
				i++;
			// Basic block 2 ends
			}while(i < N);
			}
		}
}
