package de.amidar.cacheBench.digests;

import de.amidar.AmidarSystem;
import de.amidar.cacheBench.CacheBenchParameters;
import fr.cryptohash.ECOH256Digest;

public class ECOH256_long {

	public static void main(String[] args) {

		int smallest = 192;
		int length = smallest * CacheBenchParameters.getBenchmarkScaleFactor();

		byte[] dataShort = new byte[512];
		byte[] dataLong = new byte[length];

		ECOH256_long ecoc256test = new ECOH256_long();
		ecoc256test.run(dataShort);
		ecoc256test.run(dataLong);

	}

	public void run(byte[] data) {

		ECOH256Digest digest = new ECOH256Digest();

		AmidarSystem.invalidateFlushAllCaches();

		digest.update(data, 0, data.length);

	}

}
