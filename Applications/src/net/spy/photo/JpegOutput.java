package net.spy.photo;

public class JpegOutput {
	
	int[] output = new int[131072];
	int pointer = 0;

	public void write(int c) {

			output[pointer] = c;
			pointer++;
		
	}
	
	
	public void print(){
		for(int i = 0; i< pointer; i++){
			System.out.print(output[i]);
			System.out.print(',');
		}System.out.println();
	}

}
