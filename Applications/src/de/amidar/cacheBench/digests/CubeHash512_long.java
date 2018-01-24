package de.amidar.cacheBench.digests;

import de.amidar.AmidarSystem;
import de.amidar.cacheBench.CacheBenchParameters;
import fr.cryptohash.CubeHash512Digest;

public class CubeHash512_long {
	public static void main(String[] args) {

		int smallest = 32;
		int length = smallest * CacheBenchParameters.getBenchmarkScaleFactor();

		byte[] dataShort = new byte[512];
		byte[] dataLong = new byte[length];

		CubeHash512_long cubehash512test = new CubeHash512_long();
		cubehash512test.run(dataShort);
		cubehash512test.run(dataLong);

	}

	public void run(byte[] data) {

		CubeHash512Digest digest = new CubeHash512Digest();

		AmidarSystem.invalidateFlushAllCaches();

		digest.update(data, 0, data.length);

//		int [] erg = digest.getH();

//		for(int i = 0; i< erg.length; i++){
//			System.out.print(erg[i]);
//			System.out.print(',');
//		} System.out.println();

	}
}
