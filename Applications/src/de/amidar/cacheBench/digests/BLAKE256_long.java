package de.amidar.cacheBench.digests;

import de.amidar.AmidarSystem;
import de.amidar.cacheBench.CacheBenchParameters;
import fr.cryptohash.BLAKE256Digest;

public class BLAKE256_long {
	public static void main(String[] args) {

		int smallest = 64;
		int length = smallest * CacheBenchParameters.getBenchmarkScaleFactor();

		byte[] dataShort = new byte[512];
		byte[] dataLong = new byte[length];



		BLAKE256_long blake256test = new BLAKE256_long();

		blake256test.run(dataShort);
		blake256test.run(dataLong);

	}

	public void run(byte[] data) {

		BLAKE256Digest digest = new BLAKE256Digest();

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
