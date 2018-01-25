package de.amidar.cacheBench.digests;

import de.amidar.AmidarSystem;
import de.amidar.cacheBench.CacheBenchParameters;
import fr.cryptohash.RadioGatun32Digest;

public class RadioGatun32_test {
	public static void main(String[] args) {


		int smallest = 156;
		int length = smallest * CacheBenchParameters.getBenchmarkScaleFactor();

		byte[] dataShort = new byte[512];

		RadioGatun32_test radiogatun32test = new RadioGatun32_test();
		radiogatun32test.run(dataShort);
	}

	public void run(byte[] data) {

		RadioGatun32Digest digest = new RadioGatun32Digest();

		AmidarSystem.invalidateFlushAllCaches();

		digest.update(data, 0, data.length);

		int [] erg = digest.getH();

		for(int i = 0; i< erg.length; i++){
			System.out.print(erg[i]);
			System.out.print(',');
		}

		System.out.println();

	}
}
