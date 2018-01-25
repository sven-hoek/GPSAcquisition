package de.amidar;


public class SimpleTest {
	
	int increment = 1;
	
	
	public static void main(String [] args){ 
		
		int [] b = new int[10];
//		int [][] c = new int [3][1000];
//		
//		int cnt = 0;
//		
//		
//		for(int i = 0; i < 1000; i++){
////			b[i] = 9*i;
//			c[i%3][i] = 9*i;
//		}
//		
		
		
//		char []  adsf = {'h', 'a', 'l', 'l', 'o'};
//		
//		System.out.println(adsf);
		
		accelerateMe2();
		
		 
		
//		System.out.println(accelerateMe());
//		for(int i = 0; i< b.length ; i++){
//			b[i] = i;
// 		}
//		System.out.println("FFFFFFFFI");
//		System.out.println(cnt);
//		for(int i = 0; i< b.length; i++){
//			System.out.println(b[i]);
//		}
//		
//		// TEST 1
//		int cnt = 0;
//		for(int i = 0; i < 10; i++) {
//			cnt++;
//			if(i == 1)
//				cnt ++;
//			if(i == 3)
//				cnt ++;
//			if(i == 5)
//				cnt ++;
//			if(i == 7)
//				cnt ++;
//			if(i == 9)
//				cnt ++;
//			
//			int b = 123123123;
//		}
//		System.out.println("cnt "+cnt);
//		
//		
//		// TEST 2
//		float a = 1.2f;
//		float b = 1;
//		for(int i = 0; i < 50; i++){
//			b *= a;
//			float c = a + b;
//			c = a - b;
//			c = a / b;
//		}
//		
//		// TEST 3
//		int  val = 0; 
//		for(int i = 0; i < 10; i++){
//			val += getSomeNumber(i);
//		}
//		
//		
//		// TEST 4
//		int[] arr = new int [20];
//		for(int i = 1; i < 20; i++){
//			arr[i] = i + arr[i-1];
//		}
//		
//		// TEST 5
//		int a1 = 0, b1 = 1;
//		for(int i = 0; i < 1000; i++){
//			int sum = a1 + b1;
//			a = b; 
//			b = sum;
//		}
//		
//		// TEST 6
//		SimpleTest peter = new SimpleTest();
//		int val1 = 0; 
//		for(int i = 0; i < 50; i++){
//			val1 += peter.getAnotherNumber(i);
//		}
//		
//		// TEST 7
//		int val2 = 0;
//		for(int i = 0; i< 50; i++){
//			val2 += 9999999;
//		}
//		
//		// TEST 8
//		for(int i = 0; i < 100; i++){
//			SimpleTest asdf = new SimpleTest();
//			asdf.increment += asdf.increment;
//		}
//		
//		// TEST 9
//		for(int i = 0; i < 100; i++){
//			int[] asd = new int [100];
//		}
//		
//		// TEST 10
//		for(int i = 0; i < 100; i++){
//			int[][] asd = new int[37][101];
//		}
//		
//		// TEST 11
//		float asdf = 1.2f;
//		for(int i = 0; i < 100; i++){
//			int p = (int)asdf;
//			asdf = (float)p;
//		}
//		
//		// TEST 12
//		float[] asdff = new float[100];
//		for(int i = 1; i < 100; i++){
//			SimpleTest tommy = new SimpleTest();
//			asdff[i] = asdff[i-1] + ((float)i)/getSomeNumber(tommy.increment);
//		}
//		 
//		// TEST 13
//		for(int i = 0; i < 100; i++){
//			int[][][] asd = new int[37][7][4];
//		}
	}
	
	public static int accelerateMe(){
		
		int cnt = 0;
		
		for(int i = 99999; i >= 12; i--){
			cnt ++;
			if(i >= 77){
				cnt--;
			}
			if(i >= 88){
				cnt++;
			}
		}
		
		return cnt;
		
	}
	
	public static void accelerateMe2(){
		int[] a = new int[10];
		for(int i = 9; i >= 0; i--){
			a[i] = i+i;
		}
		
		int sum = 0;
		
		for(int i = 9; i >= 0; i--){
			sum +=a [i];
		}
		System.out.println("sum "+sum);
		
//		for(int i = 0; i< 1024; i++){
//			System.out.println(a[i]);
//		}
		
//		int address = AmidarSystem.gcLock(a);
//		
//		AmidarSystem.FlushRef(a);
//		
//		// Read through the backdoor
//		// Always first and last word of a cacheline
//		for(int i=0; i <1024; i++){ 
//			SuccessPrinter.printMessageln(""+AmidarSystem.ReadAddress(address + i*4));
//		}
		
	}
	
	
	public static int getSomeNumber(int a ){
		return a * 34;
	}
	
	public int getAnotherNumber(int a ){
		return a * 34;
	}
	

}