package de.amidar.dspKernel;

/**
 * Created by Ramon on 20.05.2016.
 */
public class Convolution {

	public static void main(String[] args) {
//		int[] a = {0,1,2,3,4,5,6,7,8,9};
//		int[] b = {0,1,2,3,4,5,6,7,8,9};
//		int[] result = new int[10];
//		dspConvInt(a,b,result,10);
//		float[] a = {0,1,2,3,4,5,6,7,8,9};
//		float[] b = {0,1,2,3,4,5,6,7,8,9};
		float[] a = new float[256];
		float[] b = new float[256];

		for (int i = 0; i < 256; i++) {
			a[i] = i;
			b[i] = i;
		}

		float[] result = new float[256];
		dspConvFloat(a,b,result,256);

		for (int i = 0; i < result.length; i++) {
			System.out.println("res["+i+"] = " + result[i]);
		}
	}

	/**
	 * Convolution, optimized not to generate known 0s.
	 * result will contain only n values, where a and be overlap.
	 * If the inputs have different sizes the longer one can be cut to the shorter ones length, as otherwise it would be Zero-Extended, and the results would be 0 anyway.
	 * If a is longer it has to be cut at the end, b on the other hand has to be cut at the start.
	 *
	 * <p>
	 * <b>Integer variant, no shifting for fixpoint is done.</b>
	 * </p>
	 *
	 * <p>
	 * <b>Hook for pre-synthesized CGRA DSP Kernel.</b>
	 * </p>
	 *
	 * @param a input sequence, n values
	 * @param b input sequence, n values
	 * @param result output sequence, n values
	 * @param n number of values in each sequences
	 */
	public static void dspConvInt(int[] a, int[] b, int[] result, int n) {
		int dummy = 0;
		dummy--;
		dummy = 4;
		dummy++;
		convInt(a, b, result, n);
		System.out.println("Computation complete!");
	}

	/**
	 * Convolution, optimized not to generate known 0s.
	 * result will contain only n values, where a and be overlap.
	 * If the inputs have different sizes the longer one can be cut to the shorter ones length, as otherwise it would be Zero-Extended, and the results would be 0 anyway.
	 * If a is longer it has to be cut at the end, b on the other hand has to be cut at the start.
	 *
	 * <p>
	 * <b>Hook for pre-synthesized CGRA DSP Kernel.</b>
	 * </p>
	 *
	 * @param a input sequence, n values
	 * @param b input sequence, n values
	 * @param result output sequence, n values
	 * @param n number of values in each sequences
	 */
	public static void dspConvFloat(float[] a, float[] b, float[] result, int n) {
		int dummy = 0;
		dummy--;
		dummy = 4;
		dummy++;
		convFloat(a, b, result, n);
		System.out.println("Computation complete!");
	}

	/**
	 * Convolution, optimized not to generate known 0s.
	 * result will contain only n values, where a and be overlap.
	 * If the inputs have different sizes the longer one can be cut to the shorter ones length, as otherwise it would be Zero-Extended, and the results would be 0 anyway.
	 * If a is longer it has to be cut at the end, b on the other hand has to be cut at the start.
	 *
	 * <p>
	 * <b>Integer variant, no shifting for fixpoint is done</b>
	 * </p>
	 *
	 * <p>
	 * <b>Pure Software implementation, that will produce the same results as CGRA</b>
	 * </p>
	 *
	 * @param a input sequence, n values
	 * @param b input sequence, n values
	 * @param result output sequence, n values
	 * @param n number of values in each sequences
	 */
	public static void convInt(int[] a, int[] b, int[] result, int n) {
		System.out.println("Software Impl!!");

		for (int k=0; k < n; ++k) {
			int sum = 0;
//			System.out.println("k="+k);
			for (int i = 0, j = k; j >= 0; ++i, --j) {
				sum += a[i] * b[j];
//				System.out.println("\ta["+ i +"] * b["+ j +"]:  "+ a[i] +" * "+ b[j] +" => sum: "+ sum +"\n");
			}
			result[k] = sum;
		}
	}

	/**
	 * Convolution, optimized not to generate known 0s.
	 * result will contain only n values, where a and be overlap.
	 * If the inputs have different sizes the longer one can be cut to the shorter ones length, as otherwise it would be Zero-Extended, and the results would be 0 anyway.
	 * If a is longer it has to be cut at the end, b on the other hand has to be cut at the start.
	 *
	 * <p>
	 * <b>Pure Software implementation, that will produce the same results as CGRA</b>
	 * </p>
	 *
	 * @param a input sequence, n values
	 * @param b input sequence, n values
	 * @param result output sequence, n values
	 * @param n number of values in each sequences
	 */
	public static void convFloat(float[] a, float[] b, float[] result, int n) {
		System.out.println("Software Impl!!");

		for (int k=0; k < n; ++k) {
			float sum = 0.0f;
//			System.out.println("k="+k);
			for (int i = 0, j = k; j >= 0; ++i, --j) {
				sum += a[i] * b[j];
//				System.out.println("\ta["+ i +"] * b["+ j +"]:  "+ a[i] +" * "+ b[j] +" => sum: "+ sum +"\n");
			}
			result[k] = sum;
		}
	}
}
