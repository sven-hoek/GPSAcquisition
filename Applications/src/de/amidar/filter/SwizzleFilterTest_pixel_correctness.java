package de.amidar.filter;

import com.jhlabs.image.SwizzleFilter;

public class SwizzleFilterTest_pixel_correctness {

    public static void main(String[] args) throws Exception {

    	SwizzleFilterTest_pixel_correctness swizzle = new SwizzleFilterTest_pixel_correctness();
    	swizzle.run();

    }

    public void run() throws Exception {

    	int[] img = {
        	    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
        	    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
        	    1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 
        	    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
        	    -1, -1, -1, -1, -1, 556, -1, -1, -1, -1 
        	};

    	int[] swizzledImg;
    	
    	SwizzleFilter filter = new SwizzleFilter();

	swizzledImg = filter.filterImage(img, 0, 0);
	for(int i=0; i< swizzledImg.length; i++){
		System.out.print(swizzledImg[i]);			///correctnesscode
		System.out.print(' ');
	}System.out.println();
	

    }


}
