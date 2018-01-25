package de.amidar.filter;

import com.jhlabs.image.ContrastFilter;

public class ContrastFilterTest_pixel_correctness {


    public static void main(String[] args) throws Exception {

	ContrastFilterTest_pixel_correctness contrast = new ContrastFilterTest_pixel_correctness();
	contrast.run();

    }

    public void run() throws Exception {
	int[] rgb = {
	    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
	    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
	    1, 1, 1, 1, 1, 1, 67990, 1, 1, 1, 
	    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
	    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
	};

	ContrastFilter filter = new ContrastFilter();

	int[] contrast = filter.filter(rgb);
	for(int i=0; i< contrast.length; i++){
		System.out.println(contrast[i]);			///correctnesscode
	}
    }

}
