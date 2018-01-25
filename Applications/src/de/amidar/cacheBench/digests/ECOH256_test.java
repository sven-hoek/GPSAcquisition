package de.amidar.cacheBench.digests;

import de.amidar.AmidarSystem;
import de.amidar.cacheBench.CacheBenchParameters;
import fr.cryptohash.ECOH256Digest;

public class ECOH256_test {
	
	public static void main(String[] args) {

		int smallest = 192;
		int length = smallest * CacheBenchParameters.getBenchmarkScaleFactor();

		byte[] dataShort = new byte[512];

		ECOH256_test ecoc256test = new ECOH256_test();
		ecoc256test.run(dataShort);

	}

	public void run(byte[] data) {

		ECOH256Digest digest = new ECOH256Digest();

		AmidarSystem.invalidateFlushAllCaches();

		digest.update(data, 0, data.length);
		
		 int [] erg = digest.getH1();

		 for(int i = 0; i< erg.length; i++){
			 System.out.print(erg[i]);
			 System.out.print(',');
		 }

		 erg = digest.getH2();

		 for(int i = 0; i< erg.length; i++){
			 System.out.print(erg[i]);
			 System.out.print(',');
		 }System.out.println();

	}

}
