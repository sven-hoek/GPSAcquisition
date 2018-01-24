package de.amidar.filter;

//import java.awt.image.BufferedImage;

public class SobelFilter {

	private int[] hx = new int[] {
			-1, 0, 1,
            -2, 0, 2,
            -1, 0, 1
            };
	
	private int[] hy = new int[]{
			-1,-2,-1,
            0, 0, 0,
            1, 2, 1
            };
	
	private int kernWidth = 3, kernHeight = 3;
	private int kernelLength = 9;
		
	public int[] sobelEdgeDetection(int[] img, int imgWidth, int imgHeight) {
		int[] edged = new int[img.length];
		
        int[] rgbX = new int[3]; 
        int[] rgbY = new int[3];
        
         //ignore border pixels strategy
        for(int x = 1; x < imgWidth - 1; x++){
            for(int y = 1; y < imgHeight - 1; y++) { 
                convolvePixel(hx, img, imgWidth, imgHeight, x, y, rgbX);
                convolvePixel(hy, img, imgWidth, imgHeight, x, y, rgbY);
                
                //instead of using sqrt function for eculidean distance
                //just do an estimation
                int r = abs(rgbX[0]) + abs(rgbY[0]);
                int g = abs(rgbX[1]) + abs(rgbY[1]);
                int b = abs(rgbX[2]) + abs(rgbY[2]);
                //range check
                if(r > 255) 
                	r = 255;
                if(g > 255) 
                	g = 255;
                if(b > 255) 
                	b = 255;
                setRGB(edged, imgWidth, x, y, (r<<16)|(g<<8)|b);
            }
        }
        
        return edged;
    }
	
    private int[] convolvePixel(int[] kernel, int[] img, int imgWidth, int imgHeight, int x, int y, int[] rgb) {
         
        int halfWidth = kernWidth/2;
        int halfHeight = kernHeight/2;
        
         /*this algorithm pretends as though the kernel is indexed from -halfWidth 
          *to halfWidth horizontally and -halfHeight to halfHeight vertically.  
          *This makes the center pixel indexed at row 0, column 0.*/
        
        
        
        for(int component = 0; component < 3; component++) {
            int sum = 0;
            for(int i = 0; i < kernelLength; i++) {
               int row = (i/kernWidth)-halfWidth;  //current row in kernel
               int column = (i-(kernWidth*(row + 1)))-halfHeight; //current column in kernel
               
               //range check
               if(x-row < 0 || x-row > imgWidth) 
            	   continue;
               if(y-column < 0 || y-column > imgHeight) 
            	   continue;
               
               int imgRGB = getRGB(img, imgWidth, x-row, y-column);
               
               sum = sum + kernel[i]*((imgRGB >> (16 - 8 * component)) & 0xff);
            }
            rgb[component] =  sum;
        }
       return rgb;
    }
    
    public int getRGB(int[] img, int imgWidth, int x, int y) {
    	return img[y * imgWidth + x];
    }
    
    public void setRGB(int[] img, int imgWidth, int x, int y, int rgb) {
    	img[y * imgWidth + x] = rgb;
    }
    
    private int abs(int v) {
    	return v < 0 ? -v : v;
    }
    
    /********************** original *************************/
//    public static BufferedImage sobelEdgeDetection(BufferedImage img) {
//        BufferedImage edged = new BufferedImage(img.getWidth(),img.getHeight(), BufferedImage.TYPE_INT_RGB);
//        
//        float[] hx = new float[]{-1,0,1,
//                                 -2,0,2,
//                                 -1,0,1};
//        
//        float[] hy = new float[]{-1,-2,-1,
//                                  0, 0, 0,
//                                  1, 2, 1};
//        
//        int[] rgbX = new int[3]; int[] rgbY = new int[3];
//        
//         //ignore border pixels strategy
//        for(int x = 1; x < img.getWidth()-1; x++)
//            for(int y = 1; y < img.getHeight()-1; y++) {
//                convolvePixel(hx,3,3, img, x, y, rgbX);
//                convolvePixel(hy,3,3, img, x, y, rgbY);
//                
//                //instead of using sqrt function for eculidean distance
//                //just do an estimation
//                int r = Math.abs(rgbX[0]) + Math.abs(rgbY[0]);
//                int g = Math.abs(rgbX[1]) + Math.abs(rgbY[1]);
//                int b = Math.abs(rgbX[2]) + Math.abs(rgbY[2]);
//                
//                //range check
//                if(r > 255) r = 255;
//                if(g > 255) g = 255;
//                if(b > 255) b = 255;
//                
//                edged.setRGB(x, y,(r<<16)|(g<<8)|b);
//            }
//        return edged;
//    }
//    
//    
//    public int[] getPixels(BufferedImage img) {
//    	int[] result = new int[img.getWidth() * img.getHeight()];
//    	
//    	for(int x = 0 ; x < img.getWidth(); x++) {
//    		for(int y = 0; y < img.getHeight(); y++) {
//    			setRGB(result, img.getWidth(), x, y, img.getRGB(x, y)); 
//    		}
//    	}
//    	
//    	return result;
//    }
//    
//    public void setPixels(int[] pixels, BufferedImage img) {
//    	
//    	for(int x = 0 ; x < img.getWidth(); x++) {
//    		for(int y = 0; y < img.getHeight(); y++) {
//    			img.setRGB(x, y, getRGB(pixels, img.getWidth(), x, y)); 
//    		}
//    	}
//    	
//    }
//    
//    private static int[] convolvePixel(float[] kernel, int kernWidth, int kernHeight, BufferedImage src, int x, int y, int[] rgb) {
//       if(rgb == null) rgb = new int[3];
//         
//        int halfWidth = kernWidth/2;
//        int halfHeight = kernHeight/2;
//        
//         /*this algorithm pretends as though the kernel is indexed from -halfWidth 
//                   *to halfWidth horizontally and -halfHeight to halfHeight vertically.  
//                   *This makes the center pixel indexed at row 0, column 0.*/
//        
//        for(int component = 0; component < 3; component++) {
//            float sum = 0;
//            for(int i = 0; i < kernel.length; i++) {
//               int row = (i/kernWidth)-halfWidth;  //current row in kernel
//               int column = (i-(kernWidth*(row + 1)))-halfHeight; //current column in kernel
//                
//               //range check
//               if(x-row < 0 || x-row > src.getWidth()) continue;
//               if(y-column < 0 || y-column > src.getHeight()) continue;
//                
//                int srcRGB =src.getRGB(x-row,y-column);
//                sum = sum + kernel[i]*((srcRGB>>(16-8*component))&0xff);
//            }
//            rgb[component] = (int) sum;
//        }
//       return rgb;
//    }

} 
