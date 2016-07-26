import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class MainClass {
	public static void main(String args[]) throws InterruptedException{
		
		int N = Integer.parseInt(args[0]);
		
		switch(N){
			case 1:
				func1(-10);
				func2(6);
				break;
			case 2:
				func2(0);
				func3(N*100);
				break;
			case 3:
				func2(6);
				func4();
				func1(6);
				break;
			case 4:
				func1(6);
				break;
			case 5:
				func2(-10);
				break;
			case 6:
				func4();
				break;
			case 7:
				func2(0);
				func4();
				break;
			case 8:
				func3(N*20);
				break;
			case 9:
				func3(N*50);
				func1(6);
				break;
			case 10:
				func1(6);
				func2(-10);
				break;
			default:
				func4();
				func4();
		}
	}
	
	public static int func1(int N) throws InterruptedException {
		int a;
		if(N > 5) {
			a = 17;
		        func6();
        		//a = (int)((double)a / 3.14);
        		a += N;
		}
		else {	
			a = 71;
                      func7();
                        //a = (int)((double)a / 3.14);
                        a += N;	
		}

		return a;
	}
	
	public static int func2(int N) throws InterruptedException {
		if(N > 5){
			int x = N*N;
			//Thread.sleep(10);
			x += N;
			//return x;
			int[] a = {31,48,59,63,28,1,68,8,49,0,60,98,5,82,0,80,46,86,68,25,32,95,76,48,39,65,40,24,76,17,99,11,78,37,71,43,95,71,74,26,26,16,17,54,27,85,88,30,59,53,27,33,19,11,59,53,62,12,33,84,41,55,55,18,11,9,65,60,22,16,34,39,67,70,1,71,98,33,61,48,9,74,69,60,81,66,76,33,87,72,46,88,68,91,48,63,80,15,36,81,58,68,29,19,87,26,26,5,38,4,82,69,73,85,32,82,71,47,67,51,64,38,95,66,86,56,94,62,60,32,6,39,49,39,78,50,0,32,37,96,81,71,68,68,12,95,23,23,31,12,17,35,20,79,40,2,42,1,25,18,1,30,40,80,51,91,80,20,4,78,57,0,15,43,53,36,83,13,2,74,64,19,52,38,0,48,91,39,10,11,26,62,3,73,92,45,66,14,70,39};
                	sort(a);
			return x;
		}
		else {
			int x = N*N;
			//Thread.sleep(60);
			x += N;
			//return x;
			int[] arr = {31,48,59,63,28,1,68,8,49,0,60,98,5,82,0,80,46,86,68,25,32,95,76,48,39,65,40,24,76,17,99,11,78,37,71,43,95,71,74,26,26,16,17,54,27,85,88,30,59,53,27,33,19,11,59,53,62,12,33,84,41,55,55,18,11,9,65,60,22,16,34,39,67,70,1,71,98,33,61,48,9,74,69,60,81,66,76,33,87,72,46,88,68,91,48,63,80,15,36,81,58,68,29,19,87,26,26,5,38,4,82,69,73,85,32,82,71,47,67,51,64,38,95,66,86,56,94,62,60,32,6,39,49,39,78,50,0,32,37,96,81,71,68,68,12,95,23,23,31,12,17,35,20,79,40,2,42,1,25,18,1,30,40,80,51,91,80,20,4,78,57,0,15,43,53,36,83,13,2,74,64,19,52,38,0,48,91,39,10,11,26,62,3,73,92,45,66,14,70,39};
			int low = 0;
	                int high = arr.length - 1;
        	        QuickSort(arr, low, high);
			return x;
		}
	}
	
	public static void func3(int N) throws InterruptedException {
		
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File("temp.txt")));
			
			for(int i = 0; i < N; i++ ) {
				bw.write(i + "\n");
			}
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void func5() throws InterruptedException {
		Thread.sleep(100);
	}
	
	public static void func4() throws InterruptedException {
		func5();
	}
	
	public static void func6() throws InterruptedException {
		Thread.sleep(20);
	}
	
	public static void func7() throws InterruptedException {
		Thread.sleep(20);
	}

	
    public static int partition (int[] arr, int l, int h)
    {
        int x = arr[h];
        int i = (l - 1);
 
        for (int j = l; j <= h- 1; j++)
        {
            if (arr[j] <= x)
            {
                i++;
                // swap arr[i] and arr[j]
                //swap(arr,i,j);
		int temp = arr[i];
        	arr[i] = arr[j];
        	arr[j] = temp;
            }
        }
        // swap arr[i+1] and arr[h]
        //swap(arr,i+1,h);
	int temp = arr[i+1];
        arr[i+1] = arr[h];
        arr[h] = temp;
        
	return (i + 1);
    }
 
    // Sorts arr[l..h] using iterative QuickSort
    public static void QuickSort(int[] arr, int l, int h)
    {
        // create auxiliary stack
        int stack[] = new int[h-l+1];
 
        // initialize top of stack
        int top = -1;
 
        // push initial values in the stack
        stack[++top] = l;
        stack[++top] = h;
 
        // keep popping elements until stack is not empty
        while (top >= 0)
        {
            // pop h and l
            h = stack[top--];
            l = stack[top--];
 
            // set pivot element at it's proper position
            int p = partition(arr, l, h);
 
            // If there are elements on left side of pivot,
            // then push left side to stack
            if ( p-1 > l )
            {
                stack[ ++top ] = l;
                stack[ ++top ] = p - 1;
            }
 
            // If there are elements on right side of pivot,
            // then push right side to stack
            if ( p+1 < h )
            {
                stack[ ++top ] = p + 1;
                stack[ ++top ] = h;
            }
       }
    }

	public static int sort(int[] a){
                int left = 0;
                int right = a.length-1;

                bubbleSort(a,left,right);
		left = 0;
		right = 0;
		return 0;
        }

        public static int bubbleSort(int[] a,int left,int right){
                // the outer loop, runs from right to left
                for(int i=right;i>1;i--){
                        // the inner loops, runs from left to the right, limited by the outer loop
                        for(int j=left;j<i;j++){
                                // if the left item is greater than the right one, swaps
                                if(a[j] > a[j+1]){
                                        //swap(a, j, j+1);
					int temp = a[left];
                			a[left] = a[right];
                			a[right] = temp;
                                }
                        }
                }
		return 0;
        }

        // This method is used to swap the values between the two given index
        /*public static void swap(int[] a, int left,int right){
                int temp = a[left];
                a[left] = a[right];
                a[right] = temp;
        }*/

       /* public static void printArray(int[] a){
                for(int i : a){
                        System.out.print(i+" ");
                }
        }*/

/*	public static int[] getArray(){
                int size=300;
                int []array = new int[size];
                int item = 0;
                for(int i=0;i<size;i++){
                        item = (int)(Math.random()*100); 
                        array[i] = item;
                }
                return array;
        }*/
}
