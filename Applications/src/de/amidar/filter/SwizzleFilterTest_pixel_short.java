package de.amidar.filter;

import com.jhlabs.image.SwizzleFilter;

public class SwizzleFilterTest_pixel_short {

    public static void main(String[] args) throws Exception {

    	SwizzleFilterTest_pixel_short swizzle = new SwizzleFilterTest_pixel_short();
    	swizzle.run();

    }

    public void run() throws Exception {

    	int[] img = {
        	    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
        	    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
        	    1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 
        	    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
        	    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 
        	};

    	int[] swizzledImg;
    	
    	SwizzleFilter filter = new SwizzleFilter();

	swizzledImg = filter.filterImage(img, 0, 0);
	

    }

}
