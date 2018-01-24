package de.amidar.cacheBench.digests;

import de.amidar.AmidarSystem;
import de.amidar.cacheBench.CacheBenchParameters;
import fr.cryptohash.RadioGatun32Digest;

public class RadioGatun32_long {
	public static void main(String[] args) {


		int smallest = 156;
		int length = smallest * CacheBenchParameters.getBenchmarkScaleFactor();

		byte[] dataShort = new byte[512];
		byte[] dataLong = new byte[length];

		RadioGatun32_long radiogatun32test = new RadioGatun32_long();
		radiogatun32test.run(dataShort);
		radiogatun32test.run(dataLong);
	}

	public void run(byte[] data) {

		RadioGatun32Digest digest = new RadioGatun32Digest();

		AmidarSystem.invalidateFlushAllCaches();

		digest.update(data, 0, data.length);

//		int [] erg = digest.getH();
//
//		for(int i = 0; i< erg.length; i++){
//			System.out.print(erg[i]);
//			System.out.print(',');
//		}
//
//		System.out.println();

	}
}
