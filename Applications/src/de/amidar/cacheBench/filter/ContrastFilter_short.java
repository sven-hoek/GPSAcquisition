package de.amidar.cacheBench.filter;

import com.jhlabs.image.ContrastFilter;

import de.amidar.AmidarSystem;
import de.amidar.cacheBench.CacheBenchParameters;


public class ContrastFilter_short {


	public static void main(String[] args) throws Exception {
		
		int smallest = 8;
		int length = smallest * CacheBenchParameters.getBenchmarkScaleFactor();
		
		
		int[] rgbShort = new int[51];
		int[] rgbLong = new int[length];

		ContrastFilter_short contrast = new ContrastFilter_short();
		contrast.run(rgbShort);
//		contrast.run(rgbLong);

	}

	public void run(int [] rgb) throws Exception {

		ContrastFilter filter = new ContrastFilter();
		
		AmidarSystem.invalidateFlushAllCaches();

		int[] contrast = filter.filter(rgb);
		
//		for(int i=0; i< contrast.length; i++){
//			System.out.println(contrast[i]);			///correctnesscode
//		}
	}

}
