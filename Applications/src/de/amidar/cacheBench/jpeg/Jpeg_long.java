package de.amidar.cacheBench.jpeg;

import de.amidar.AmidarSystem;
import net.spy.photo.JpegEncoder;

public class Jpeg_long {
	public static void main(String [] args){
		AmidarSystem.invalidateFlushAllCaches();
		JpegEncoder encShort = new JpegEncoder(80,false);
		
		encShort.compress();
		
		AmidarSystem.invalidateFlushAllCaches();
		JpegEncoder encLong = new JpegEncoder(80,true);

		encLong.compress();
		
	}


}
