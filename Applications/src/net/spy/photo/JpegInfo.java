package net.spy.photo;

//import java.awt.Image;
//import java.awt.image.PixelGrabber;

/*
 * JpegInfo - Given an image, sets default information about it and divides
 * it into its constituant components, downsizing those that need to be.
 */

class JpegInfo {
//	String Comment;
//	public Image imageobj;
	public int jiHeight;
	public int jiWidth;
	public int BlockWidth[];
	public int BlockHeight[];
	// the following are set as the default
	public int Precision = 8;
	public int NumberOfComponents = 3;
	public float [][][] Components;
	public int[] CompID = { 1, 2, 3 };
	public int[] HsampFactor = { 1, 1, 1 };
	public int[] VsampFactor = { 1, 1, 1 };
	public int[] QtableNumber = { 0, 1, 1 };
	public int[] DCtableNumber = { 0, 1, 1 };
	public int[] ACtableNumber = { 0, 1, 1 };
	public boolean[] lastColumnIsDummy = { false, false, false };
	public boolean[] lastRowIsDummy = { false, false, false };
	public int Ss = 0;
	public int Se = 63;
	public int Ah = 0;
	public int Al = 0;
	public int compWidth[], compHeight[];
	public int MaxHsampFactor;
	public int MaxVsampFactor;
	PixelGrabber grabber;
	
	public JpegInfo(boolean longPic) {
//		Components = new float[NumberOf][][];
		grabber = new PixelGrabber(longPic);
		compWidth = new int[NumberOfComponents];
		compHeight = new int[NumberOfComponents];
		BlockWidth = new int[NumberOfComponents];
		BlockHeight = new int[NumberOfComponents];
//		imageobj = image;
		jiWidth = grabber.getWidth();//8;//image.getWidth(null);
		jiHeight = grabber.getHeight();//8;//image.getHeight(null);
		//		Comment = "JPEG Encoder Copyright 1998, James R. Weeks and BioElectroMech.	";
//		getYCCArray(grabber);
		initYCCArray(grabber);
	}

	/*public void setComment(String comment) {
		Comment.concat(comment);
	}

	public String getComment() {
		return Comment;
	}*/

	/*
	 * This method creates and fills three arrays, Y, Cb, and Cr using the
	 * input image.
	 */

	private void initYCCArray(PixelGrabber grabber) {
		int values[] = new int[jiWidth * jiHeight];
		int r, g, b, y, x;
		// In order to minimize the chance that grabPixels will throw an
		// exception
		// it may be necessary to grab some pixels every few scanlines and
		// process
		// those before going for more. The time expense may be prohibitive.
		// However, for a situation where memory overhead is a concern, this
		// may be
		// the only choice.
		
//		System.out.println("");
		
//		PixelGrabber grabber = new PixelGrabber(values);
		MaxHsampFactor = 1;
		MaxVsampFactor = 1;
		for(y = 0; y < NumberOfComponents; y++) {
//			MaxHsampFactor = Math.max(MaxHsampFactor, HsampFactor[y]);
//			MaxVsampFactor = Math.max(MaxVsampFactor, VsampFactor[y]);
			MaxHsampFactor = MaxHsampFactor > HsampFactor[y] ? MaxHsampFactor: HsampFactor[y];
			MaxVsampFactor = MaxVsampFactor > VsampFactor[y] ? MaxVsampFactor: VsampFactor[y];
			
		}
		for(y = 0; y < NumberOfComponents; y++) {
			compWidth[y] = (((jiWidth % 8 != 0)
					? ((int) (int)(jiWidth / 8.0f +1)) * 8 : jiWidth) / MaxHsampFactor)
					* HsampFactor[y];
			if(compWidth[y] != ((jiWidth / MaxHsampFactor) * HsampFactor[y])) {
				lastColumnIsDummy[y] = true;
			}
			// results in a multiple of 8 for compWidth
			// this will make the rest of the program fail for the unlikely
			// event that someone tries to compress an 16 x 16 pixel image
			// which would of course be worse than pointless
//			BlockWidth[y] = (int) Math.ceil(compWidth[y] / 8.0); // TODO
			BlockWidth[y] = (int) (compWidth[y] / 8.0f); // TODO
			compHeight[y] = (((jiHeight % 8 != 0)
					? ((int)(int)(jiHeight / 8.0f +1)) * 8 : jiHeight) / MaxVsampFactor)
					* VsampFactor[y];
			if(compHeight[y] != ((jiHeight / MaxVsampFactor) * VsampFactor[y])) {
				lastRowIsDummy[y] = true;
			}
//			BlockHeight[y] = (int) Math.ceil(compHeight[y] / 8.0); //TODO
			BlockHeight[y] = (int) (compHeight[y] / 8.0f); //TODO
		}
//		try {
		
//		System.out.println("ASFDASD");
			 Y = new float[compHeight[0]][compWidth[0]];
		 Cr1 = new float[compHeight[0]][compWidth[0]];
		 Cb1 = new float[compHeight[0]][compWidth[0]];
		Components = new float [compHeight[0]][compWidth[0]][NumberOfComponents];
		
		

	}
	
	float Y[][];
float Cr1[][];
float Cb1[][];
	
	void getYCCArray(){
		int values[] = new int[jiWidth * jiHeight];
		int r, g, b, y, x;
		
		grabber.grabPixels(values);
		
		
//		} catch(InterruptedException e) {
//			e.printStackTrace();
//		}

		int index = 0;
		for(y = 0; y < jiHeight; ++y) {
			for(x = 0; x < jiWidth; ++x) {
				r = ((values[index] >> 16) & 0xff);
				g = ((values[index] >> 8) & 0xff);
				b = (values[index] & 0xff);

				// The following three lines are a more correct color
				// conversion but
				// the current conversion technique is sufficient and
				// results in a higher
				// compression rate.
				// Y[y][x] = 16 + (float)(0.8588*(0.299 * (float)r + 0.587 *
				// (float)g + 0.114 * (float)b ));
				// Cb1[y][x] = 128 + (float)(0.8784*(-0.16874 * (float)r -
				// 0.33126 * (float)g + 0.5 * (float)b));
				// Cr1[y][x] = 128 + (float)(0.8784*(0.5 * (float)r -
				// 0.41869 * (float)g - 0.08131 * (float)b));
				Y[y][x] = (float) ((0.299f * r + 0.587f * g + 0.114f * b));
				Cb1[y][x] = 128 + (float) ((-0.16874f * r - 0.33126f * g + 0.5f * b));
				Cr1[y][x] = 128 + (float) ((0.5f 	* r - 0.41869f * g - 0.08131f * b));
				index++;
			}
		}

		// Need a way to set the H and V sample factors before allowing
		// downsampling.
		// For now (04/04/98) downsampling must be hard coded.
		// Until a better downsampler is implemented, this will not be done.
		// Downsampling is currently supported. The downsampling method here
		// is a simple box filter.

		Components[2] = Cr1;
		
		Components[0] = Y;
		// Cb2 = DownSample(Cb1, 1);
		Components[1] = Cb1;
		// Cr2 = DownSample(Cr1, 2);
		
	}

	float[][] DownSample(float[][] C, int comp) {
		int inrow, incol;
		int outrow, outcol;
		float output[][];
		int bias;
		inrow = 0;
		incol = 0;
		output = new float[compHeight[comp]][compWidth[comp]];
		for(outrow = 0; outrow < compHeight[comp]; outrow++) {
			bias = 1;
			for(outcol = 0; outcol < compWidth[comp]; outcol++) {
				output[outrow][outcol] = (C[inrow][incol++]
						+ C[inrow++][incol--] + C[inrow][incol++]
								+ C[inrow--][incol++] + bias)
								/ (float) 4.0;
				bias ^= 3;
			}
			inrow += 2;
			incol = 0;
		}
		return output;
	}

//	public void init() {
//		getYCCArray(grabber);
//	}
}