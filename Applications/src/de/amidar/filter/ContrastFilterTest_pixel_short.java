package de.amidar.filter;

import com.jhlabs.image.ContrastFilter;

public class ContrastFilterTest_pixel_short {

    public static void main(String[] args) throws Exception {

	ContrastFilterTest_pixel_short contrast = new ContrastFilterTest_pixel_short();
	contrast.run();

    }

    public void run() throws Exception {
//	int[] rgb = {
//	    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
//	    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
//	    1, 1, 1, 1, 1, 1, 67990, 1, 1, 1, 
//	    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
//	    -1, -1, -1, -1, -1, -1, -1, -1, -1, 1
//	};

	ContrastFilter filter = new ContrastFilter();
	
	int[] rgb = filter.getInput();

	filter.filter(rgb);
    }

}
