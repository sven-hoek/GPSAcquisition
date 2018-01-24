package net.spy.photo;

public class JpegEncode_long {
	
	public static void main(String [] args){
		
		JpegEncoder enc = new JpegEncoder(80,false);
		enc.compress();
		enc.compress();
		
	}

}
