package de.amidar.cacheBench.filter;

import de.amidar.AmidarSystem;
import de.amidar.cacheBench.CacheBenchParameters;
import de.amidar.filter.SobelFilter;

public class SobelFilter_short {
	
    public static void main(String[] args) throws Exception {
    	
		int smallest = 9;
		int factor =  CacheBenchParameters.getBenchmarkScaleFactor();
		int length = smallest * factor;
		int scale = CacheBenchParameters.getBenchmarkScale();
		
		int height = 1<<(scale/2);
		int width = factor /height;
    	
		int[] rgbShort = new int[51];
		int xShort = 17;
		int yShort = 3;
		int[] rgbLong = new int[length];
		int xLong = 3 * width;
		int yLong = 3 * height;

    	SobelFilter_short sobel = new SobelFilter_short();
    	sobel.run(rgbShort, xShort, yShort);
//    	sobel.run(rgbLong, xLong, yLong);

    }

    public void run(int [] img, int width, int height) throws Exception {


    	int[] edgedImg;
    	
    	SobelFilter filter = new SobelFilter();
    	
    	AmidarSystem.invalidateFlushAllCaches();

	edgedImg = filter.sobelEdgeDetection(img, width, height);

//	for(int i=0; i< edgedImg.length; i++){						///correctnesscode
//		System.out.print(edgedImg[i]);
//		System.out.print(',');
//	}
//	System.out.println();
    	
    }

}
