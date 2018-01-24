package de.amidar.dspKernel;

/**
 * Created by Ramon on 31.03.2017.
 */
public class Sum {

	public static void main(String[] args) {
		int[] a = {0,1,2,3,4,5,6,7,8,9};
		int[] b = {8,9,0,1,2,3,4,5,6,7};
		int[] result = new int[10];
		dspSumInt(a,b,result,10);
		for (int i = 0; i < result.length; i++) {
			System.out.println("res["+i+"] = " + result[i]);
		}
	}

	public static void dspSumInt(int[] a, int[] b, int[] result, int n) {
		int dummy = 0;
		dummy--;
		dummy = 4;
		dummy++;
		sumInt(a, b, result, n);
		System.out.println("Computation complete!");
	}

	public static void sumInt(int[] a, int[] b, int[] result, int n) {
		System.out.println("Software Impl!!");
		for (int i = 0; i<n; ++i) {
			int sum = 0;
			result[i] = a[i] + b[i];
		}
	}

}
