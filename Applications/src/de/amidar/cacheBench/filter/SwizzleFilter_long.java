package de.amidar.cacheBench.filter;

import com.jhlabs.image.SwizzleFilter;

import de.amidar.AmidarSystem;
import de.amidar.cacheBench.CacheBenchParameters;


public class SwizzleFilter_long {
	public static void main(String[] args) throws Exception {

		int smallest = 8;
		int length = smallest * CacheBenchParameters.getBenchmarkScaleFactor();
		
		
		int[] rgbShort = new int[51];
		int[] rgbLong = new int[length];
		
		SwizzleFilter_long swizzle = new SwizzleFilter_long();
		swizzle.run(rgbShort);
		swizzle.run(rgbLong);

	}

	public void run(int[] img) throws Exception {

		int[] swizzledImg;

		SwizzleFilter filter = new SwizzleFilter();
		
		AmidarSystem.invalidateFlushAllCaches();

		swizzledImg = filter.filterImage(img, 0, 0);

		//	for(int i=0; i< swizzledImg.length; i++){
		//		System.out.print(swizzledImg[i]);			///correctnesscode
		//		System.out.print(' ');
		//	}System.out.println();


	}
}
