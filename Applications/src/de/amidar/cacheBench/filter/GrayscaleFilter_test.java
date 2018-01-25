package de.amidar.cacheBench.filter;

import com.jhlabs.image.GrayscaleFilter;

import de.amidar.AmidarSystem;
import de.amidar.cacheBench.CacheBenchParameters;


public class GrayscaleFilter_test {

	public static void main(String[] args) throws Exception {
		
		int smallest = 8;
		int length = smallest * CacheBenchParameters.getBenchmarkScaleFactor();
		
		
		int[] rgbShort = {
			    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
			    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
			    255, 255, 22234, 255, 1, 1, 1, 1, 1, 1, 
			    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
			    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
			};

		GrayscaleFilter_test grayscale = new GrayscaleFilter_test();
		grayscale.run(rgbShort);

	}

	public void run(int [] rgb) throws Exception {


		GrayscaleFilter filter = new GrayscaleFilter();
		
		AmidarSystem.invalidateFlushAllCaches();

		int[] grey = filter.filter(rgb);

		for(int i=0; i< grey.length; i++){
			System.out.print(grey[i]+", ");			///correctnesscode
		}System.out.println();


	}

}
