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

public class GrayscaleFilter {

	public int[] filter( int[] inpixels ) {
	
		int length = inpixels.length;
		int[] result = new int[length];
		int res = 0;

		for ( int index = 0; index < length; index++ ) {
			int rgb = inpixels[index];
			int a = rgb & 0xff000000;

			int r = (rgb >> 16) & 0xff;
			int g = (rgb >> 8) & 0xff;
			int b = rgb & 0xff;
			rgb = (r * 77 + g * 151 + b * 28) >> 8; // NTSC luma
			res = a | (rgb << 16) | (rgb << 8) | rgb;
			result[index] = res;
		}

		return result;

	}

}
