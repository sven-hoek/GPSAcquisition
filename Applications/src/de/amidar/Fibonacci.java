package de.amidar;

public class Fibonacci {

	public static void main(String[] args) {
		
		int N = 10;
		
		int [] fibo = new int [N];
		
		fibo[0] = 0;
		fibo[1] = 1;
		
		for(int i = 2; i < N; i++){
			fibo[i] = fibo[i-1] + fibo[i-2];
		}
		
		
		for(int i = 0; i < N; i++){
			System.out.println(fibo[i]);
		}
		

	}

}
