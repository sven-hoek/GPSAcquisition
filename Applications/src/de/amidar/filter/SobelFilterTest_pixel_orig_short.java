package de.amidar.filter;

public class SobelFilterTest_pixel_orig_short {

    public static void main(String[] args) throws Exception {

    	SobelFilterTest_pixel_orig_short sobel = new SobelFilterTest_pixel_orig_short();
    	sobel.run();

    }

    public void run() throws Exception {

    	int[] img = {
    			-1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1
       	};

    	int[] edgedImg;
	int X = 17;
	int Y = 3;
    	
    	SobelFilter filter = new SobelFilter();

	edgedImg = filter.sobelEdgeDetection(img, X, Y);
    	
    }

}
