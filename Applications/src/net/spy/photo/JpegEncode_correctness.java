package net.spy.photo;

public class JpegEncode_correctness {
	
	public static void main(String [] args){
		
		JpegEncoder enc = new JpegEncoder(80,false);
		enc.compress();
		enc.outStream.print();
		
	}


}
