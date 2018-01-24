package de.amidar.cacheBench.digests;

import org.bouncycastle.crypto.digests.MD5Digest;

import de.amidar.AmidarSystem;
import de.amidar.cacheBench.CacheBenchParameters;


public class MD5_long {

	public static void main(String[] args) {


		int smallest = 16;
		int length = smallest * CacheBenchParameters.getBenchmarkScaleFactor();

		int[] dataShort = new int[512];
		int[] dataLong = new int[length];


		MD5_long md5test = new MD5_long();
		md5test.run(dataShort);
		md5test.run(dataLong);

	}

	public void run(int[] dataAll) {

		MD5Digest digest = new MD5Digest();

		int[] data = new int [16];
		
		
		AmidarSystem.invalidateFlushAllCaches();

		for(int index = 0; index < dataAll.length; index+=16){
			for(int i = 0; i < 16; i++){
				data[i] = dataAll[i+index];
			}
			digest.processBlock(data);

		}



//		int [] erg = digest.getH();
//
//		for(int i = 0; i< erg.length; i++){
//			System.out.print(erg[i]);
//			System.out.print(',');
//		}System.out.println();

	}

}
