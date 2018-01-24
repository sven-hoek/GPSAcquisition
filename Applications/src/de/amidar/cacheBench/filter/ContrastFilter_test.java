package de.amidar.cacheBench.filter;

import com.jhlabs.image.ContrastFilter;

import de.amidar.AmidarSystem;
import de.amidar.cacheBench.CacheBenchParameters;


public class ContrastFilter_test {


	public static void main(String[] args) throws Exception {
		
		int smallest = 8;
		int length = smallest * CacheBenchParameters.getBenchmarkScaleFactor();
		
		
		int[] rgbShort = {
			    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
			    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
			    1, 1, 1, 1, 1, 1, 67990, 1, 1, 1, 
			    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
			    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
			};

		ContrastFilter_test contrast = new ContrastFilter_test();
		contrast.run(rgbShort);

	}

	public void run(int [] rgb) throws Exception {

		ContrastFilter filter = new ContrastFilter();
		
		AmidarSystem.invalidateFlushAllCaches();

		int[] contrast = filter.filter(rgb);
		
		for(int i=0; i< contrast.length; i++){
			System.out.println(contrast[i]);			///correctnesscode
		}
	}

}
