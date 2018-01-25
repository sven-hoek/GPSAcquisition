package de.amidar.cacheBench.filter;

import com.jhlabs.image.SwizzleFilter;

import de.amidar.AmidarSystem;
import de.amidar.cacheBench.CacheBenchParameters;


public class SwizzleFilter_test {
	public static void main(String[] args) throws Exception {

		int smallest = 8;
		int length = smallest * CacheBenchParameters.getBenchmarkScaleFactor();
		
		
    	int[] rgbShort = {
        	    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
        	    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
        	    1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 
        	    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
        	    -1, -1, -1, -1, -1, 556, -1, -1, -1, -1 
        	};

		SwizzleFilter_test swizzle = new SwizzleFilter_test();
		swizzle.run(rgbShort);

	}

	public void run(int[] img) throws Exception {

		int[] swizzledImg;

		SwizzleFilter filter = new SwizzleFilter();
		
		AmidarSystem.invalidateFlushAllCaches();

		swizzledImg = filter.filterImage(img, 0, 0);

			for(int i=0; i< swizzledImg.length; i++){
				System.out.print(swizzledImg[i]);			///correctnesscode
				System.out.print(' ');
			}System.out.println();


	}
}
