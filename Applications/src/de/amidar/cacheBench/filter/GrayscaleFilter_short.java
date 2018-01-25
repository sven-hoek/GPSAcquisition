package de.amidar.cacheBench.filter;

import com.jhlabs.image.GrayscaleFilter;

import de.amidar.AmidarSystem;
import de.amidar.cacheBench.CacheBenchParameters;


public class GrayscaleFilter_short {

	public static void main(String[] args) throws Exception {
		
		int smallest = 8;
		int length = smallest * CacheBenchParameters.getBenchmarkScaleFactor();
		
		
		int[] rgbShort = new int[51];
		int[] rgbLong = new int[length];

		GrayscaleFilter_short grayscale = new GrayscaleFilter_short();
		grayscale.run(rgbShort);
//		grayscale.run(rgbLong);

	}

	public void run(int [] rgb) throws Exception {


		GrayscaleFilter filter = new GrayscaleFilter();
		
		AmidarSystem.invalidateFlushAllCaches();

		int[] grey = filter.filter(rgb);

//		for(int i=0; i< grey.length; i++){
//			System.out.print(grey[i]+", ");			///correctnesscode
//		}System.out.println();


	}

}
