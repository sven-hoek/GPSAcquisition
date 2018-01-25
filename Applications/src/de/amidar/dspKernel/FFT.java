package de.amidar.dspKernel;

/**
 * Created by Ramon on 01.04.2017.
 */
public class FFT {


	public static void main(String[] args) {
//		float[] aR = {0, 1, 2, 3, 2, 1, 0, 1, 2, 3, 2, 1, 0, 1, 2, 3};
//		float[] aR = new float[16];
//		for (int i = 0; i < aR.length; i++) {
//			aR[i] = (float) Math.cos(2*Math.PI * i/4);
//		}
//		float[] aI = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

		float[] aR = new float[256];
		float[] aI = new float[256];
		float[] resultR = new float[256];
		float[] resultI = new float[256];

		for (int i=0; i < 256; i++) {
			int j = i % 6;
			aR[i] = (j <= 3)? j : 6 - j;
			aI[i] = 0;
		}

		dspFft(aR, aI, 256, 8, resultR, resultI);

		for (int i = 0; i < resultR.length; i++) {
			System.out.println("res[" + i + "] = " + resultR[i] + " + "+ resultI[i]+" i");
		}

//		float[] recR = new float[16];
//		float[] recI = new float[16];
//		for (int i = 0; i < aR.length; i++) {
//			recR[i] = aR[i];
//			recI[i] = aI[i];
//		}
//
//		fftRecursive(recR, recI, 0, 15);
//
//		System.out.println("Recursive111!!");
//		for (int i = 0; i < resultR.length; i++) {
//			System.out.println("res[" + i + "] = " + recR[i]/4f + " + "+ recI[i]/4f+" i");
//		}

//		resultR = new float[16];
//		resultI = new float[16];
//		fftRecursive2(aR, aI, resultR, resultI, 16, 1, 0);
//
//		System.out.println("Recursive2222!!");
//		for (int i = 0; i < resultR.length; i++) {
//			System.out.println("res[" + i + "] = " + resultR[i] + " + "+ resultI[i]+" i");
//		}

//		System.out.println("REF!!");
//
//		float[] res = fftRefSolution(aR, aI);
//		for (int i = 0; i < res.length; i+=2) {
//			System.out.println("res[" + i/2 + "] = " + res[i] + " + "+ res[i+1]+" i");
//		}
	}

	/**
	 * FFT implementation
	 *
	 * <p>
	 * <b>Hook for pre-synthesized CGRA DSP Kernel</b>
	 * </p>
	 *
	 *
	 * @param inputR real parts of input sequence
	 * @param inputI imaginary part of input sequence
	 * @param n number of inputs, power of 2
	 * @param logN log2(n), known beforehand
	 * @param resultR real part of output sequence
	 * @param resultI imaginary part of output sequence
	 */
	public static void dspFft(float[] inputR, float[] inputI, int n, int logN, float[] resultR, float[] resultI) {
		float correction = (float) (1f/Math.sqrt(n));

		dspFft(inputR, inputI, n, logN, correction, resultR, resultI);
	}

	/**
	 * FFT implementation
	 *
	 * <p>
	 * <b>Hook for pre-synthesized CGRA DSP Kernel</b>
	 * </p>
	 *
	 *
	 * @param inputR real parts of input sequence
	 * @param inputI imaginary part of input sequence
	 * @param n number of inputs, power of 2
	 * @param logN log2(n), known beforehand
	 * @param correction 1/sqrt(n) if desired, otherwise 1
	 * @param resultR real part of output sequence
	 * @param resultI imaginary part of output sequence
	 */
	public static void dspFft(float[] inputR, float[] inputI, int n, int logN, float correction, float[] resultR, float[] resultI) {
		int dummy = 0;
		dummy--;
		dummy = 4;
		dummy++;
		fft(inputR, inputI, n, logN, correction, resultR, resultI);
		System.out.println("Computation complete!");
	}

	public static void fftRecursive(float[] resR, float[] resI, int first, int last) {
		if (first < last) {
			int middle = fftSplit(resR, resI, first, last);
			fftRecursive(resR, resI, first, middle);
			fftRecursive(resR, resI, middle+1, last);
			fftCombine(resR, resI, first, last);
		}
	}

	private static void fftCombine(float[] resR, float[] resI, int first, int last) {
		int half = (last - first + 1)/2;
		float v = ((float) (Math.PI / half));
		float wR = (float) Math.cos(v);
		float wI = (float) Math.sin(v);
		float wjR = 1f;
		float wjI = 0f;
		for (int j = 0; j < half; j++) {
			int even = first + j;
			int odd = even + half;

			float aoR = resR[odd];
			float aoI = resI[odd];
			float aeR = resR[even];
			float aeI = resI[even];

			float xR = complexMulR(wjR, wjI, aoR, aoI);
			float xI = complexMulI(wjR, wjI, aoR, aoI);
			aoR = aeR - xR;
			aoI = aeI - xI;

			aeR = aeR + xR;
			aeI = aeI + xI;

			resR[even] = aeR;
			resI[even] = aeI;
			resR[odd] = aoR;
			resI[odd] = aoI;

			xR = complexMulR(wjR, wjI, wR, wI);
			xI = complexMulI(wjR, wjI, wR, wI);

			wjR = xR;
			wjI = xI;
		}
	}

	public static int fftSplit(float[] resR, float[] resI, int first, int last) {
		int middle = (first + last) / 2;
		int size = last - first + 1;
		int half = size / 2;

		float[] bR = new float[resR.length];
		float[] bI = new float[resI.length];
		for (int j = 0; j < half; j++) {
			int even = first + 2*j;
			bR[j] = resR[even];
			bI[j] = resI[even];
			bR[j+half] = resR[even+1];
			bI[j+half] = resI[even+1];
		}
		for (int j = 0; j < size; j++) {
			resR[first + j] = bR[j];
			resI[first + j] = bI[j];
		}

		return middle;
	}

	/**
	 * Iterative FFT implementation, based on bit-reversal
	 *
	 * <p>
	 * <b>Pure Software implementation, that will produce the same results as CGRA</b>
	 * </p>
	 *
	 * @param inR real parts of input sequence
	 * @param inI imaginary part of input sequence
	 * @param n number of inputs, power of 2
	 * @param logN log2(n), known beforehand
	 * @param resR real part of output sequence
	 * @param resI imaginary part of output sequence
	 */
	public static void fft(float[] inR, float[] inI, int n, int logN, float correction, float[] resR, float[] resI) {
		System.out.println("Software Impl!!");

//		for (int i = 0; i < inR.length; i++) {
//			int reverse = reverse(i, logN);
//			resR[reverse] = inR[i];
//			resI[reverse] = inI[i];
//		}


		for (int m=2, m2=1; m<= n; m<<=1, m2<<=1) {
			float v = (float) (Math.PI / m2);
			float wR = (float) Math.cos(v);
			float wI = (float) Math.sin(v);

			for (int k=0; k < n; k+=m) {
				float wjR = 1f;
				float wjI = 0f;

				int even = k;
				int odd = k + m2;

				for (int j=0; j < m2; j++) {
//					int even = k + j;
//					int odd = k + j + m2;

					float aoR;
					float aoI;
					float aeR;
					float aeI;

					if (m2==1) {
						int revOdd = reverse(odd, logN);
						int revEven = reverse(even, logN);

						aoR = inR[revOdd];
						aoI = inI[revOdd];

						aeR = inR[revEven];
						aeI = inI[revEven];
					} else {
						aoR = resR[odd];
						aoI = resI[odd];

						aeR = resR[even];
						aeI = resI[even];
					}



					float tR = complexMulR(wjR, wjI, aoR, aoI);
					float tI = complexMulI(wjR, wjI, aoR, aoI);

					float newAoR = aeR - tR;
					float newAoI = aeI - tI;

					float newAeR = aeR + tR;
					float newAeI = aeI + tI;

					if (m==n) {
						newAoR *= correction;
						newAoI *= correction;
						newAeR *= correction;
						newAeI *= correction;
					}

					resR[odd] = newAoR;
					resI[odd] = newAoI;

					resR[even] = newAeR;
					resI[even] = newAeI;

					float newWjR = complexMulR(wjR, wjI, wR, wI);
					float newWjI = complexMulI(wjR, wjI, wR, wI);

					wjR = newWjR;
					wjI = newWjI;

					even++;
					odd++;
				}
			}
		}

	}

	private static double complexMulR(double aR, double aI, double bR, double bI) {
		return aR*bR - aI*bI;
	}

	private static double complexMulI(double aR, double aI, double bR, double bI) {
		return aR*bI + aI*bR;
	}

	private static float complexMulR(float aR, float aI, float bR, float bI) {
		return aR*bR - aI*bI;
	}

	private static float complexMulI(float aR, float aI, float bR, float bI) {
		return aR*bI + aI*bR;
	}


	public static int reverse(int value, int count) {
		int res = 0;
		for (int i=0; i<count; i++) {
			res <<= 1;
			int bit = value & 0x1;
			value >>= 1;
			res += bit;
		}
		return res;
	}

	public static float[] fftRefSolution(final float[] inputReal, float[] inputImag) {
		// - n is the dimension of the problem
		// - nu is its logarithm in base e
		int n = inputReal.length;

		// If n is a power of 2, then ld is an integer (_without_ decimals)
		float ld = (float) (Math.log(n) / Math.log(2.0));

		// Here I check if n is a power of 2. If exist decimals in ld, I quit
		// from the function returning null.
		if (((int) ld) - ld != 0) {
			System.out.println("The number of elements is not a power of 2.");
			return null;
		}

		// Declaration and initialization of the variables
		// ld should be an integer, actually, so I don't lose any information in
		// the cast
		int logN = (int) ld;
		int n2 = n / 2;
		int logN1 = logN - 1;
		float[] xReal = new float[n];
		float[] xImag = new float[n];
		float tReal, tImag, p, arg, c, s;

		// Here I check if I'm going to do the direct transform or the inverse
		// transform.
		float constant;
		if (true) {
			constant = (float) (-2 * Math.PI);
		} else {
			constant = (float) (2 * Math.PI);
		}

		// I don't want to overwrite the input arrays, so here I copy them. This
		// choice adds \Theta(2n) to the complexity.
		for (int i = 0; i < n; i++) {
			xReal[i] = inputReal[i];
			xImag[i] = inputImag[i];
		}

		// First phase - calculation
		int k = 0;
		for (int l = 1; l <= logN; l++) {
			while (k < n) {
				for (int i = 1; i <= n2; i++) {
					p = reverse(k >> logN1, logN);
					// direct FFT or inverse FFT
					arg = constant * p / n;
					c = (float) Math.cos(arg);
					s = (float) Math.sin(arg);
					tReal = xReal[k + n2] * c + xImag[k + n2] * s;
					tImag = xImag[k + n2] * c - xReal[k + n2] * s;
					xReal[k + n2] = xReal[k] - tReal;
					xImag[k + n2] = xImag[k] - tImag;
					xReal[k] += tReal;
					xImag[k] += tImag;
					k++;
				}
				k += n2;
			}
			k = 0;
			logN1--;
			n2 /= 2;
		}

		// Second phase - recombination
		k = 0;
		int r;
		while (k < n) {
			r = reverse(k, logN);
			if (r > k) {
				tReal = xReal[k];
				tImag = xImag[k];
				xReal[k] = xReal[r];
				xImag[k] = xImag[r];
				xReal[r] = tReal;
				xImag[r] = tImag;
			}
			k++;
		}

		// Here I have to mix xReal and xImag to have an array (yes, it should
		// be possible to do this stuff in the earlier parts of the code, but
		// it's here to readibility).
		float[] newArray = new float[xReal.length * 2];
		float radice = (float) (1 / Math.sqrt(n));
		for (int i = 0; i < newArray.length; i += 2) {
			int i2 = i / 2;
			// I used Stephen Wolfram's Mathematica as a reference so I'm going
			// to normalize the output while I'm copying the elements.
			newArray[i] = xReal[i2] * radice;
			newArray[i + 1] = xImag[i2] * radice;
		}
		return newArray;
	}

}
