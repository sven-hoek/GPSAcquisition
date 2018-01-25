package java.lang;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.NullPrintStream;
import java.io.PrintStream;
import java.util.Properties;

import de.amidar.AmidarSystem;
import de.amidar.ArrayCopy;
import de.amidar.scheduler.Scheduler;

public final class System {
	public static InputStream in = null;//new FileInputStream("ffföalsjfd");

	public static PrintStream out = new PrintStream(null); // new NullPrintStream ();

	public static PrintStream err = new NullPrintStream ();
	
	public static File dummy = new File("");

	public static void arraycopy (Object src, int srcOffset, Object dst,
			int dstOffset, int length) {
//		if(length>128){
			arraycopyN(src, srcOffset, dst, dstOffset, length);
//			return;
//		}
//		if(src instanceof byte[]){
//			byte[] s = (byte[])src;
//			byte[] d = (byte[])dst;
//			
//			for(int i = 0; i< length; i++){
//				d[i+dstOffset] = s[i+srcOffset];
//			}
//		} else if(src instanceof Object[]){
//			Object[] s = (Object[])src;
//			Object[] d = (Object[])dst;
//			
//			for(int i = 0; i< length; i++){
//				d[i+dstOffset] = s[i+srcOffset];
//			}
//		}else if(src instanceof int[]){
//			int[] s = (int[])src;
//			int[] d = (int[])dst;
//			
//			for(int i = 0; i< length; i++){
//				d[i+dstOffset] = s[i+srcOffset];
//			}
//		}else if(src instanceof char[]){
//			char[] s = (char[])src;
//			char[] d = (char[])dst;
//			
//			for(int i = 0; i< length; i++){
//				d[i+dstOffset] = s[i+srcOffset];
//			}
//		}else if(src instanceof short[]){
//			short[] s = (short[])src;
//			short[] d = (short[])dst;
//			
//			for(int i = 0; i< length; i++){
//				d[i+dstOffset] = s[i+srcOffset];
//			}
//		}else if(src instanceof long[]){
//			long[] s = (long[])src;
//			long[] d = (long[])dst;
//			
//			for(int i = 0; i< length; i++){
//				d[i+dstOffset] = s[i+srcOffset];
//			}
//		}else if(src instanceof double[]){
//			double[] s = (double[])src;
//			double[] d = (double[])dst;
//			
//			for(int i = 0; i< length; i++){
//				d[i+dstOffset] = s[i+srcOffset];
//			}
//		}else if(src instanceof float[]){
//			float[] s = (float[])src;
//			float[] d = (float[])dst;
//			
//			for(int i = 0; i< length; i++){
//				d[i+dstOffset] = s[i+srcOffset];
//			}
//		}else if(src instanceof boolean[]){
//			boolean[] s = (boolean[])src;
//			boolean[] d = (boolean[])dst;
//			
//			for(int i = 0; i< length; i++){
//				d[i+dstOffset] = s[i+srcOffset];
//			}
//		}
//		
//		
//		
//		else{
//		System.out.println("NOTHING FOUTND");
//		}
	}
	
	public static void arraycopyN (Object src, int srcOffset, Object dst,
			int dstOffset, int length) {
		System.out.println("sollte eigentlich nieee auf gerufen werden...");
	}
	
	
//	public static void arrayCopy(byte[] src, int srcOffset, byte[] dst, int dstOffset, int length){
//		for(int i = 0; i < length; i++){
//			dst[dstOffset + i ] = src[srcOffset + i];
//		}
//		
//		
//	}

	public static long currentTimeMillis () {
		int low = currentTimeMillisLow();
		int high = currentTimeMillisHigh();
		return (((int)high)<<32)+((long)low);
	}
	
	private static int currentTimeMillisLow () {
//		return Scheduler.Instance ().state.GetSystemUptimeMillis ();
		return -1;
	}
	
	private static int currentTimeMillisHigh () {
//		return Scheduler.Instance ().state.GetSystemUptimeMillis ();
		return -1;
	}


	public static long nanoTime () {
		int low = nanoTimeLow();
		int high = nanoTimeHigh();
		return (((int)high)<<32)+((long)low);
	}
	
	private static int nanoTimeLow () {
//		return Scheduler.Instance ().state.GetSystemUptimeMillis ();
		return -1;
	}
	
	private static int nanoTimeHigh () {
//		return Scheduler.Instance ().state.GetSystemUptimeMillis ();
		return -1;
	}
	

	public static void exit (int status) {
		// TODO Handle exit, maybe terminate threads and start an endless loop?
		// Or simply stick with UnsupportedOperationException?
		throw new UnsupportedOperationException ();
	}

	public static void gc () {
		// TODO Try forcing the GC
//		throw new UnsupportedOperationException ();
	}

	public static String getenv (String name) {
		// TODO Do we have any environment variables?
		throw new UnsupportedOperationException ();
	}

	public static Properties getProperties () {
		// TODO Get system properties
		throw new UnsupportedOperationException ();
	}

	public static String getProperty (String string) {
		// TODO Auto-generated method stub
		if (string.equals ("line.separator"))
			return "\n";
		throw new UnsupportedOperationException ();
	}

	public static int identityHashCode (Object object) {
		return AmidarSystem.RefToInt (object);
	}

	public static void setIn (InputStream in) {
		// TODO set (final) in field
	}

	public static void setOut (PrintStream out) {
		if (out == null) {
			System.out = new NullPrintStream ();
		} else {
			System.out = out;
		}
	}

	public static void setErr (PrintStream err) {
		if (err == null) {
			System.err = new NullPrintStream ();
		} else {
			System.err = err;
		}
	}

	private System () {
	}
	
	public static void runFinalization(){
		System.out.println("call java.lang.System.runFinalization()");
	}


}
