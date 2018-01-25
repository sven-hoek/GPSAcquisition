// Version 1.0a
// Copyright (C) 1998, James R. Weeks and BioElectroMech.
// Visit BioElectroMech at www.obrador.com.  Email James@obrador.com.

// See license.txt for details about the allowed used of this software.
// This software is based in part on the work of the Independent JPEG Group.
// See IJGreadme.txt for details about the Independent JPEG Group's license.

// This encoder is inspired by the Java Jpeg encoder by Florian Raemy,
// studwww.eurecom.fr/~raemy.
// It borrows a great deal of code and structure from the Independent
// Jpeg Group's Jpeg 6a library, Copyright Thomas G. Lane.
// See license.txt for details.
//

package net.spy.photo;

//import java.awt.Image;
//import java.awt.image.PixelGrabber;
//import java.io.BufferedOutputStream;
//import java.io.IOException;
//import java.io.OutputStream;
//import java.util.ArrayList;

//import net.spy.SpyObject;

/*
 * JpegEncoder - The JPEG main program which performs a jpeg compression of
 * an image.
 */
public class JpegEncoder {
//	private BufferedOutputStream outStream;
	public JpegOutput outStream;
	private JpegInfo JpegObj;
	private Huffman Huf;
	private DCT dct;
	private int imageHeight, imageWidth;
	private int quality;
	

	public JpegEncoder(int q, boolean longPic) {
		//		ImageWatcher watcher = new ImageWatcher(image);
		//		try {
		//			watcher.waitForImage();
		//		} catch(InterruptedException e) {
		//			getLogger().info("Excepting waiting or image", e);
		//			// Try again
		//			try {
		//				watcher.waitForImage();
		//			} catch(InterruptedException e2) {
		//				getLogger().warn("Giving up.", e2);
		//			}
		//		}
		/*
		 * Quality of the image. 0 to 100 and from bad image quality, high
		 * compression to good image quality low compression
		 */
		quality = q;
		outStream = new JpegOutput();

		/*
		 * Getting picture information It takes the Width, Height and RGB scans
		 * of the image.
		 */
		JpegObj = new JpegInfo(longPic);
		
		imageHeight = JpegObj.jiHeight;
		imageWidth = JpegObj.jiWidth;
		dct = new DCT(quality);
		Huf = new Huffman(imageWidth, imageHeight);

	}
	
	
	public static void main(String [] args){
		
		JpegEncoder enc = new JpegEncoder(80, false);
		enc.compress();
		enc.outStream.print();
		
	}

	public void setQuality(int q) {
		dct = new DCT(q);
	}

	public int getQuality() {
		return quality;
	}

	public void compress() {
//		JpegObj.init();
		JpegObj.getYCCArray();

//		writeHeaders(outStream);
		writeCompressedData(outStream);
//		writeEOI(outStream);
//		try {
//			outStream.flush();
//		} catch(IOException e) {
//			throw new RuntimeException("Error compressing", e);
//		}
	}

	private void writeCompressedData(JpegOutput os) {
		int i, j, r, c, a, b;
		int comp, xpos, ypos, xblockoffset, yblockoffset;
		float inputArray[][];
		float dctArray1[][] = new float[8][8];
		float dctArray2[][] = new float[8][8];
		int dctArray3[] = new int[8 * 8];

		/*
		 * This method controls the compression of the image. Starting at the
		 * upper left of the image, it compresses 8x8 blocks of data until the
		 * entire image has been compressed.
		 */

		int lastDCvalue[] = new int[JpegObj.NumberOfComponents];
		int MinBlockWidth, MinBlockHeight;
		// This initial setting of MinBlockWidth and MinBlockHeight is done to
		// ensure they start with values larger than will actually be the case.
		MinBlockWidth = ((imageWidth % 8 != 0)
				? (int) ((int)(imageWidth / 8.0) + 1) * 8 : imageWidth);
		MinBlockHeight = ((imageHeight % 8 != 0)
				? (int) ((int)(imageHeight / 8.0) + 1) * 8 : imageHeight);
		for(comp = 0; comp < JpegObj.NumberOfComponents; comp++) {
//			MinBlockWidth = Math.min(MinBlockWidth, JpegObj.BlockWidth[comp]);
//			MinBlockHeight = Math.min(MinBlockHeight, JpegObj.BlockHeight[comp]);
			MinBlockWidth = MinBlockWidth < JpegObj.BlockWidth[comp] ? MinBlockWidth : JpegObj.BlockWidth[comp];
			MinBlockHeight = MinBlockHeight < JpegObj.BlockHeight[comp] ? MinBlockHeight : JpegObj.BlockHeight[comp];
		}
		xpos = 0;
		for(r = 0; r < MinBlockHeight; r++) {
			for(c = 0; c < MinBlockWidth; c++) {
				xpos = c * 8;
				ypos = r * 8;
				for(comp = 0; comp < JpegObj.NumberOfComponents; comp++) {
					inputArray = JpegObj.Components[comp];
					
					for(i = 0; i < JpegObj.VsampFactor[comp]; i++) {
						for(j = 0; j < JpegObj.HsampFactor[comp]; j++) {
							xblockoffset = j * 8;
							yblockoffset = i * 8;
							
							for(a = 0; a < 8; a++) {
								
								for(b = 0; b < 8; b++) {
									// I believe this is where the dirty line at
									// the bottom of the image is
									// coming from. I need to do a check here to
									// make sure I'm not reading past
									// image data.
									// This seems to not be a big issue right
									// now. (04/04/98)
									
									dctArray1[a][b] = inputArray[ypos
									                             + yblockoffset + a][xpos
									                                                 + xblockoffset + b];
								}
							}
							// The following code commented out because on some
							// images this technique
							// results in poor right and bottom borders.
							// if ((!JpegObj.lastColumnIsDummy[comp] || c <
							// Width - 1) && (!JpegObj.lastRowIsDummy[comp] || r
							// < Height - 1)) {
							dctArray2 = dct.forwardDCT(dctArray1);
							dctArray3 = dct.quantizeBlock(
									dctArray2, JpegObj.QtableNumber[comp]);
							// }
							// else {
							// zeroArray[0] = dctArray3[0];
							// zeroArray[0] = lastDCvalue[comp];
							// dctArray3 = zeroArray;
							// }
//							for(int ff : dctArray3){
//								System.out.print(ff);
//								System.out.print(' ');
//							}
//							for(int k = 0; k< dctArray2.length; k++){
//								for(int l = 0; l< dctArray2[0].length; l ++){
//									System.out.print(dctArray2[k][l]);
//									System.out.print(' ');
//								}
//							}System.out.println();
							Huf.HuffmanBlockEncoder(
									os, dctArray3, lastDCvalue[comp],
									JpegObj.DCtableNumber[comp],
									JpegObj.ACtableNumber[comp]);
							lastDCvalue[comp] = dctArray3[0];
						}
					}
				}
			}
		}
		Huf.flushBuffer(os);
	}

/*	private void writeEOI(BufferedOutputStream out) {
		byte[] EOI = { (byte) 0xFF, (byte) 0xD9 };
		WriteMarker(EOI, out);
	}*/

/*	private void writeHeaders(BufferedOutputStream out) {
		int i, j, index, offset, length;
		int tempArray[];

		// the SOI marker
		byte[] SOI = { (byte) 0xFF, (byte) 0xD8 };
		WriteMarker(SOI, out);

		// The order of the following headers is quiet inconsequential.
		// the JFIF header
		byte JFIF[] = new byte[18];
		JFIF[0] = (byte) 0xff;
		JFIF[1] = (byte) 0xe0;
		JFIF[2] = (byte) 0x00;
		JFIF[3] = (byte) 0x10;
		JFIF[4] = (byte) 0x4a;
		JFIF[5] = (byte) 0x46;
		JFIF[6] = (byte) 0x49;
		JFIF[7] = (byte) 0x46;
		JFIF[8] = (byte) 0x00;
		JFIF[9] = (byte) 0x01;
		JFIF[10] = (byte) 0x00;
		JFIF[11] = (byte) 0x00;
		JFIF[12] = (byte) 0x00;
		JFIF[13] = (byte) 0x01;
		JFIF[14] = (byte) 0x00;
		JFIF[15] = (byte) 0x01;
		JFIF[16] = (byte) 0x00;
		JFIF[17] = (byte) 0x00;
		WriteArray(JFIF, out);

		// Comment Header
		String comment = new String();
		comment = JpegObj.getComment();
		length = comment.length();
		byte COM[] = new byte[length + 4];
		COM[0] = (byte) 0xFF;
		COM[1] = (byte) 0xFE;
		COM[2] = (byte) ((length >> 8) & 0xFF);
		COM[3] = (byte) (length & 0xFF);
		java.lang.System.arraycopy(
				JpegObj.Comment.getBytes(), 0, COM, 4, JpegObj.Comment.length());
		WriteArray(COM, out);

		// The DQT header
		// 0 is the luminance index and 1 is the chrominance index
		byte DQT[] = new byte[134];
		DQT[0] = (byte) 0xFF;
		DQT[1] = (byte) 0xDB;
		DQT[2] = (byte) 0x00;
		DQT[3] = (byte) 0x84;
		offset = 4;
		for(i = 0; i < 2; i++) {
			DQT[offset++] = (byte) ((0 << 4) + i);
			tempArray = (int[]) dct.quantum[i];
			for(j = 0; j < 64; j++) {
				DQT[offset++] = (byte) tempArray[jpegNaturalOrder[j]];
			}
		}
		WriteArray(DQT, out);

		// Start of Frame Header
		byte SOF[] = new byte[19];
		SOF[0] = (byte) 0xFF;
		SOF[1] = (byte) 0xC0;
		SOF[2] = (byte) 0x00;
		SOF[3] = (byte) 17;
		SOF[4] = (byte) JpegObj.Precision;
		SOF[5] = (byte) ((JpegObj.jiHeight >> 8) & 0xFF);
		SOF[6] = (byte) ((JpegObj.jiHeight) & 0xFF);
		SOF[7] = (byte) ((JpegObj.jiWidth >> 8) & 0xFF);
		SOF[8] = (byte) ((JpegObj.jiWidth) & 0xFF);
		SOF[9] = (byte) JpegObj.NumberOfComponents;
		index = 10;
		for(i = 0; i < SOF[9]; i++) {
			SOF[index++] = (byte) JpegObj.CompID[i];
			SOF[index++] = (byte) ((JpegObj.HsampFactor[i] << 4) + JpegObj.VsampFactor[i]);
			SOF[index++] = (byte) JpegObj.QtableNumber[i];
		}
		WriteArray(SOF, out);

		// The DHT Header
		byte DHT1[], DHT2[], DHT3[], DHT4[];
		int bytes, temp, oldindex, intermediateindex;
		length = 2;
		index = 4;
		oldindex = 4;
		DHT1 = new byte[17];
		DHT4 = new byte[4];
		DHT4[0] = (byte) 0xFF;
		DHT4[1] = (byte) 0xC4;
		for(i = 0; i < 4; i++) {
			bytes = 0;
			DHT1[index++ - oldindex] = (byte) Huf.bits.get(i)[0];
			for(j = 1; j < 17; j++) {
				temp = Huf.bits.get(i)[j];
				DHT1[index++ - oldindex] = (byte) temp;
				bytes += temp;
			}
			intermediateindex = index;
			DHT2 = new byte[bytes];
			for(j = 0; j < bytes; j++) {
				DHT2[index++ - intermediateindex] = (byte) Huf.val.get(i)[j];
			}
			DHT3 = new byte[index];
			java.lang.System.arraycopy(DHT4, 0, DHT3, 0, oldindex);
			java.lang.System.arraycopy(DHT1, 0, DHT3, oldindex, 17);
			java.lang.System.arraycopy(DHT2, 0, DHT3, oldindex + 17, bytes);
			DHT4 = DHT3;
			oldindex = index;
		}
		DHT4[2] = (byte) (((index - 2) >> 8) & 0xFF);
		DHT4[3] = (byte) ((index - 2) & 0xFF);
		WriteArray(DHT4, out);

		// Start of Scan Header
		byte SOS[] = new byte[14];
		SOS[0] = (byte) 0xFF;
		SOS[1] = (byte) 0xDA;
		SOS[2] = (byte) 0x00;
		SOS[3] = (byte) 12;
		SOS[4] = (byte) JpegObj.NumberOfComponents;
		index = 5;
		for(i = 0; i < SOS[4]; i++) {
			SOS[index++] = (byte) JpegObj.CompID[i];
			SOS[index++] = (byte) ((JpegObj.DCtableNumber[i] << 4)
					+ JpegObj.ACtableNumber[i]);
		}
		SOS[index++] = (byte) JpegObj.Ss;
		SOS[index++] = (byte) JpegObj.Se;
		SOS[index++] = (byte) ((JpegObj.Ah << 4) + JpegObj.Al);
		WriteArray(SOS, out);
	}*/

	/*void WriteMarker(byte[] data, BufferedOutputStream out) {
		try {
			out.write(data, 0, 2);
		} catch(IOException e) {
			throw new RuntimeException("Error writing marker", e);
		}
	}

	void WriteArray(byte[] data, BufferedOutputStream out) {
		int length;
		try {
			length = ((data[2] & 0xFF) << 8) + (data[3] & 0xFF) + 2;
			out.write(data, 0, length);
		} catch(IOException e) {
			throw new RuntimeException("Error writing array", e);
		}
	}*/




}
