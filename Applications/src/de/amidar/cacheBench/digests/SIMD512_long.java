package de.amidar.cacheBench.digests;

import de.amidar.AmidarSystem;
import de.amidar.cacheBench.CacheBenchParameters;
import fr.cryptohash.SIMD512Digest;

public class SIMD512_long {

	public static void main(String[] args) {

		int smallest = 128;
		int length = smallest * CacheBenchParameters.getBenchmarkScaleFactor();

		byte[] dataShort = new byte[512];
		byte[] dataLong = new byte[length];
		
		SIMD512_long simd512test = new SIMD512_long();
		simd512test.run(dataShort);
		simd512test.run(dataLong);

	}

	public void run(byte[] data) {

		SIMD512Digest digest = new SIMD512Digest();

		AmidarSystem.invalidateFlushAllCaches();
		
		digest.update(data, 0, data.length);

//		int [] erg = digest.getH();
//
//		for(int i = 0; i< erg.length; i++){
//			System.out.print(erg[i]);
//			System.out.print(',');
//		}System.out.println();

	}

}
