package de.amidar.filter;

import com.jhlabs.image.GrayscaleFilter;

public class GrayscaleFilterTest_pixel_correctness {

    public static void main(String[] args) throws Exception {

	GrayscaleFilterTest_pixel_correctness grayscale = new GrayscaleFilterTest_pixel_correctness();
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
	
	for(int i=0; i< grey.length; i++){
		System.out.println(grey[i]);			///correctnesscode
	}
	

    }

}
