package de.amidar.dspKernel;

/**
 * Created by Ramon on 01.04.2017.
 */
public class DCT {


	public static void main(String[] args) {
		float[] a = new float[256];
		for (int i = 0; i < 256; i++) {
			a[i] = i + 0.5f;
		}
		float[] result = new float[256];
		dspDct2(a, 256, result, 256);

		for (int i = 0; i < result.length; i++) {
			System.out.println("res[" + i + "] = " + result[i]);
		}
	}

	/**
	 * DCT-II implementation
	 *
	 * <p>
	 * <b>Hook for pre-synthesized CGRA DSP Kernel</b>
	 * </p>
	 *
	 * @see de.amidar.dspKernel.Convolution, as it is similar with basically b[] replaced by cosine results
	 *
	 * @param a input sequence
	 * @param n number of inputs
	 * @param result output sequence
	 * @param m number of outputs
	 */
	public static void dspDct2(float[] a, int n, float[] result, int m) {
		int dummy = 0;
		dummy--;
		dummy = 4;
		dummy++;
		dct2(a, n, result, m);
		System.out.println("Computation complete!");
	}

	/**
	 * DCT-II implementation
	 *
	 * @see de.amidar.dspKernel.Convolution, as it is similar with basically b[] replaced by cosine results
	 *
	 * <p>
	 * <b>Pure Software implementation, that will produce the same results as CGRA</b>
	 * </p>
	 *
	 * @param a input sequence
	 * @param n number of inputs
	 * @param result output sequence
	 * @param m number of outputs
	 */
	public static void dct2(float[] a, int n, float[] result, int m) {
		System.out.println("Software Impl!!");
		float pi_n = ((float) Math.PI)/n;
		float pi_2n = ((float) Math.PI)/(2*n);

		float cosKIdx = 0f;
		float cosK2Idx = 0f;

		for (int k = 0; k < m; ++k) {
			float sum = 0.0f;
//			System.out.println("k = "+k);
//			System.out.println("cosKIdx = "+cosKIdx);
//			System.out.println("cosK2Idx = "+cosK2Idx);

			float cosIdx = cosK2Idx;
			for (int i = 0; i < n; ++i) {
				float cos = ((float) Math.cos(cosIdx));
				float mul = a[i] * cos;
				sum += mul;

//				System.out.println("\ta["+ i +"] = " + a[i]);
//				System.out.println("\tcosIdx = " + cosIdx);
//				System.out.println("\tcos = " + cos);
//				System.out.println("\tmul = " + mul);
//				System.out.println("\t => sum = "+ sum +"\n");

				cosIdx += cosKIdx;
			}

			cosKIdx += pi_n;
			cosK2Idx += pi_2n;
			result[k] = sum;
		}
	}

}
