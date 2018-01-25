package de.amidar.cacheBench.digests;

import org.bouncycastle.crypto.digests.SHA1Digest;

import de.amidar.AmidarSystem;
import de.amidar.cacheBench.CacheBenchParameters;

public class SHA1_test {

	public static void main(String[] args) {


		int smallest = 16;
		int length = smallest * CacheBenchParameters.getBenchmarkScaleFactor();

		int[] dataShort = new int[512];

		SHA1_test sha1test = new SHA1_test();
		sha1test.run(dataShort);

	}

	public void run(int[] dataAll) {

		SHA1Digest digest = new SHA1Digest();

		int[] data = new int [16];


		AmidarSystem.invalidateFlushAllCaches();

		for(int index = 0; index < dataAll.length; index+=16){
			for(int i = 0; i < 16; i++){
				data[i] = dataAll[i+index];
			}
			digest.processBlock(data);

		}


		int [] erg = digest.getH();

		for(int i = 0; i< erg.length; i++){
			System.out.print(erg[i]);
			System.out.print(',');
		} System.out.println();
	}


}
