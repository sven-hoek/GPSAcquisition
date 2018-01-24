package de.amidar.cacheBench;

public class CacheBenchParameters {
	
//	private static int dataLengthShort = 512;
//	
//	private static int widthShort = 32;
//	private static int heightShort = 16;
//
//	
//	private static int dataLengthLong = 131072;
//	
//	private static int widthLong = 512;
//	private static int heightLong = 256;
//	
//	
//	public static int getDataLengthShort() {
//		return dataLengthShort;
//	}
//	
//	public static void setDataLengthShort(int dataLengthShort) {
//		CacheBenchParameters.dataLengthShort = dataLengthShort;
//		heightShort = (int)(Math.sqrt(dataLengthShort/2));
//		widthShort = 2 * heightShort;
//	}
//	
//	
//	public static int getDataLengthLong() {
//		return dataLengthLong;
//	}
//	public static void setDataLengthLong(int dataLengthLong) {
//		CacheBenchParameters.dataLengthLong = dataLengthLong;
//		heightLong = (int)(Math.sqrt(dataLengthLong/2));
//		widthLong = 2 * heightLong;
//	}
//	public static int getWidthShort() {
//		return widthShort;
//	}
//	public static int getHeightShort() {
//		return heightShort;
//	}
//	public static int getWidthLong() {
//		return widthLong;
//	}
//	public static int getHeightLong() {
//		return heightLong;
//	}
	
	
	public static int getBenchmarkScale(){
		return 7;
	}
	
	
	public static int getBenchmarkScaleFactor(){
		return 1<<getBenchmarkScale();
	}
	
	
	
	
}
