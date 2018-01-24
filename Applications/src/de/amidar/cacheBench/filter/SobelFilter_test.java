package de.amidar.cacheBench.filter;

import de.amidar.AmidarSystem;
import de.amidar.cacheBench.CacheBenchParameters;
import de.amidar.filter.SobelFilter;

public class SobelFilter_test {
	
    public static void main(String[] args) throws Exception {
    	
		
    	
        int[] rgbShort = {
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1
    };
		int xShort = 17;
		int yShort = 3;

    	SobelFilter_test sobel = new SobelFilter_test();
    	sobel.run(rgbShort, xShort, yShort);

    }

    public void run(int [] img, int width, int height) throws Exception {


    	int[] edgedImg;
    	
    	SobelFilter filter = new SobelFilter();
    	
    	AmidarSystem.invalidateFlushAllCaches();

	edgedImg = filter.sobelEdgeDetection(img, width, height);

	for(int i=0; i< edgedImg.length; i++){						///correctnesscode
		System.out.print(edgedImg[i]);
		System.out.print(',');
	}
	System.out.println();
    	
    }

}
