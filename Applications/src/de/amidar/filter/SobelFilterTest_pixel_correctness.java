package de.amidar.filter;

public class SobelFilterTest_pixel_correctness {

    public static void main(String[] args) throws Exception {

    	SobelFilterTest_pixel_correctness sobel = new SobelFilterTest_pixel_correctness();
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
    	
//    	int[] img = new int[8294400];

    	int[] edgedImg;
	int X = 17;
	int Y = 3;
//    	int X = 3840;
//    	int Y = 2160;
    	
    	SobelFilter filter = new SobelFilter();

//    	long start = System.currentTimeMillis();
	edgedImg = filter.sobelEdgeDetection(img, X, Y);
//	long stop = System.currentTimeMillis();
//	System.out.println("TiME : " + (stop-start));
	for(int i=0; i< edgedImg.length; i++){						///correctnesscode
		System.out.print(edgedImg[i]);
		System.out.print(',');
	}
	System.out.println();
    	
    }

}
