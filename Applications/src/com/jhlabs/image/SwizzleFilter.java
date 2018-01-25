/*
Copyright 2006 Jerry Huxtable

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.jhlabs.image;

/**
 * A filter which allows channels to be swapped. You provide a matrix with specifying the input channel for 
 * each output channel.
 */
public class SwizzleFilter { 

    private int[] matrix;

	public SwizzleFilter() {

	    int[] matrix = {
		1, 0, 0, 0, 0,
		0, 1, 0, 0, 0,
		0, 0, 1, 0, 0,
		0, 0, 0, 1, 0
	    };
	    this.matrix = matrix;

	}

	public int[] filterImage(int[] img, int imgWidth, int imgHeight) {

	    int length = img.length;

	    int[] result = new int[length];

	    for (int i = 0; i < length; i++) {

	    	int a = (img[i] >> 24) & 0xff;
	    	int r = (img[i] >> 16) & 0xff;
	    	int g = (img[i] >> 8) & 0xff;
	    	int b = img[i] & 0xff;

	    	a = matrix[0]*a + matrix[1]*r + matrix[2]*g + matrix[3]*b + matrix[4]*255;
	    	r = matrix[5]*a + matrix[6]*r + matrix[7]*g + matrix[8]*b + matrix[9]*255;
	    	g = matrix[10]*a + matrix[11]*r + matrix[12]*g + matrix[13]*b + matrix[14]*255;
	    	b = matrix[15]*a + matrix[16]*r + matrix[17]*g + matrix[18]*b + matrix[19]*255;

	    	 a = PixelUtils.clamp( a );
	    	 r = PixelUtils.clamp( r );
	    	 g = PixelUtils.clamp( g );
	    	 b = PixelUtils.clamp( b );

	    	result[i] = (a << 24) | (r << 16) | (g << 8) | b;

	    }

	    return result;

	}

}
