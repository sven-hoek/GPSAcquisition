package de.amidar.digests;

import fr.cryptohash.ECOH256Digest;

public class ECOH256Test_orig_short {

    public static void main(String[] args) {

	byte[] data = {	
	    (byte)17,  (byte)234, (byte)58, (byte)177,
	    (byte)143, (byte)222, (byte)27, (byte)162, 
	    (byte)155, (byte)39,  (byte)49, (byte)174,
	    (byte)241, (byte)10,  (byte)43, (byte)99,
	    (byte)17,  (byte)234, (byte)58, (byte)177,
	    (byte)143, (byte)222, (byte)27, (byte)162, 
	    (byte)155, (byte)39,  (byte)49, (byte)174,
	    (byte)241, (byte)10,  (byte)43, (byte)99,
	    (byte)17,  (byte)234, (byte)58, (byte)177,
	    (byte)143, (byte)222, (byte)27, (byte)162, 
	    (byte)155, (byte)39,  (byte)49, (byte)174,
	    (byte)241, (byte)10,  (byte)43, (byte)99,
	    (byte)17,  (byte)234, (byte)58, (byte)177,
	    (byte)143, (byte)222, (byte)27, (byte)162, 
	    (byte)155, (byte)39,  (byte)49, (byte)174,
	    (byte)241, (byte)10,  (byte)43, (byte)99
	};

	ECOH256Test_orig_short ecoc256test = new ECOH256Test_orig_short();
	ecoc256test.run(data);

    }

    public void run(byte[] data) {

	ECOH256Digest digest = new ECOH256Digest();

        digest.update(data, 0, 64);
        digest.update(data, 0, 64);
        digest.update(data, 0, 64);

        digest.update(data, 0, 64);
        digest.update(data, 0, 64);
        digest.update(data, 0, 64);

    }

}
