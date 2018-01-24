package de.amidar.filter;

import com.jhlabs.image.GrayscaleFilter;

public class GrayscaleFilterTest_pixel_short {

    public static void main(String[] args) throws Exception {

	GrayscaleFilterTest_pixel_short grayscale = new GrayscaleFilterTest_pixel_short();
	grayscale.run();

    }

    public void run() throws Exception {

	int[] rgb = {
	    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
	    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
	    255, 255, 22234, 255, 1, 1, 1, 1, 1, 1, 
	    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
	    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
	};

	GrayscaleFilter filter = new GrayscaleFilter();

	int[] grey = filter.filter(rgb);

	

    }

}
