package de.amidar.dspKernel;

/**
 * Created by Ramon on 29.03.2017.
 */
public class Fir {
	
	public static void main(String[] args) {

		float[] values64 = {101,102,103,104,105,106,107,108,109,110,111,112,113,114,115,116,117,118,119,120,121,122,123,124,125,126,127,128,129,130,131,132,133,134,135,136,137,138,139,140,141,142,143,144,145,146,147,148,149,150,151,152,153,154,155,156,157,158,159,160,161,162,163,164};
		float[] factors64 = {57,58,59,60,61,62,63,64,49,50,51,52,53,54,55,56,41,42,43,44,45,46,47,48,33,34,35,36,37,38,39,40,25,26,27,28,29,30,31,32,17,18,19,20,21,22,23,24,9 ,10,11,12,13,14,15,16,1 ,2 ,3 ,4 ,5 ,6 ,7 ,8};
		float[] result64 = new float[128];
		dspFir64Float(values64,factors64,result64,64);
		for (int i = 0; i < result64.length; i++) {
			System.out.println("res["+i+"] = " + result64[i]);
		}

/*		float[] values128 = {101,102,103,104,105,106,107,108,109,110,111,112,113,114,115,116,117,118,119,120,121,122,123,124,125,126,127,128,129,130,131,132,133,134,135,136,137,138,139,140,141,142,143,144,145,146,147,148,149,150,151,152,153,154,155,156,157,158,159,160,161,162,163,164,101,102,103,104,105,106,107,108,109,110,111,112,113,114,115,116,117,118,119,120,121,122,123,124,125,126,127,128,129,130,131,132,133,134,135,136,137,138,139,140,141,142,143,144,145,146,147,148,149,150,151,152,153,154,155,156,157,158,159,160,161,162,163,164,101,102,103,104,105,106,107,108,109,110,111,112,113,114,115,116,117,118,119,120,121,122,123,124,125,126,127,128,129,130,131,132,133,134,135,136,137,138,139,140,141,142,143,144,145,146,147,148,149,150,151,152,153,154,155,156,157,158,159,160,161,162,163,164,101,102,103,104,105,106,107,108,109,110,111,112,113,114,115,116,117,118,119,120,121,122,123,124,125,126,127,128,129,130,131,132,133,134,135,136,137,138,139,140,141,142,143,144,145,146,147,148,149,150,151,152,153,154,155,156,157,158,159,160,161,162,163,164};
		float[] factors128 = {57,58,59,60,61,62,63,64,49,50,51,52,53,54,55,56,41,42,43,44,45,46,47,48,33,34,35,36,37,38,39,40,25,26,27,28,29,30,31,32,17,18,19,20,21,22,23,24,9 ,10,11,12,13,14,15,16,1 ,2 ,3 ,4 ,5 ,6 ,7 ,8,57,58,59,60,61,62,63,64,49,50,51,52,53,54,55,56,41,42,43,44,45,46,47,48,33,34,35,36,37,38,39,40,25,26,27,28,29,30,31,32,17,18,19,20,21,22,23,24,9 ,10,11,12,13,14,15,16,1 ,2 ,3 ,4 ,5 ,6 ,7 ,8};
		float[] result128 = new float[384];
		dspFir128Float(values128,factors128,result128,256);
		for (int i = 0; i < result128.length; i++) {
			System.out.println("res["+i+"] = " + result128[i]);
		}*/
	}
	
	public static void dspFir64Float(float[] values, float[] factors, float[] res, int n) {
		int dummy = 0;
		dummy--;
		dummy = 4;
		dummy++;
		fir64Float(values, factors, res, n);
		System.out.println("Computation complete!");
	}
	
	public static void fir64Float(float[] values, float[] factors, float[] result, int n) {
		conviFloat(values, factors, result, n);
	}

	public static void dspFir128Float(float[] values, float[] factors, float[] res, int n) {
		int dummy = 0;
		dummy--;
		dummy = 4;
		dummy++;
		fir128Float(values, factors, res, n);
		System.out.println("Computation complete!");
	}

	public static void fir128Float(float[] values, float[] factors, float[] result, int n) {
		conviFloat(values, factors, result, n);
	}

	public static void conviFloat(float[] a, float[] b, float[] result, int n) {
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
