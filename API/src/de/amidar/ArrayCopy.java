package de.amidar;

public class ArrayCopy {
	
	public static void copy (boolean[] src, int srcOffset, boolean[] dest, int destOffset, int count) {
		for (int i = 0; i < count; i++) {
			dest [destOffset + i] = src [srcOffset + i];
		}
	}

	public static void copy (byte[] src, int srcOffset, byte[] dest, int destOffset, int count) {
		for (int i = 0; i < count; i++) {
			dest [destOffset + i] = src [srcOffset + i];
		}
	}

	public static void copy (char[] src, int srcOffset, char[] dest, int destOffset, int count) {
		for (int i = 0; i < count; i++) {
			dest [destOffset + i] = src [srcOffset + i];
		}
	}

	public static void copy (double[] src, int srcOffset, double[] dest, int destOffset, int count) {
		for (int i = 0; i < count; i++) {
			dest [destOffset + i] = src [srcOffset + i];
		}
	}

	public static void copy (float[] src, int srcOffset, float[] dest, int destOffset, int count) {
		for (int i = 0; i < count; i++) {
			dest [destOffset + i] = src [srcOffset + i];
		}
	}

	public static void copy (int[] src, int srcOffset, int[] dest, int destOffset, int count) {
		for (int i = 0; i < count; i++) {
			dest [destOffset + i] = src [srcOffset + i];
		}
	}

	public static void copy (long[] src, int srcOffset, long[] dest, int destOffset, int count) {
		for (int i = 0; i < count; i++) {
			dest [destOffset + i] = src [srcOffset + i];
		}
	}

	public static void copy (Object[] src, int srcOffset, Object[] dest, int destOffset, int count) {
		for (int i = 0; i < count; i++) {
			dest [destOffset + i] = src [srcOffset + i];
		}
	}

	public static void copy (short[] src, int srcOffset, short[] dest, int destOffset, int count) {
		for (int i = 0; i < count; i++) {
			dest [destOffset + i] = src [srcOffset + i];
		}
	}

}
