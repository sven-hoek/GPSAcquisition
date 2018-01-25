package de.amidar.digests;

import org.bouncycastle.crypto.digests.WhirlpoolDigest;

public class WhirlpoolTest_orig_short {

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

	WhirlpoolTest_orig_short whirlpooltest = new WhirlpoolTest_orig_short();
	whirlpooltest.run(data);

    }

    public void run(byte[] data) {

	WhirlpoolDigest digest = new WhirlpoolDigest();

        digest.update(data, 0, 64);
        digest.update(data, 0, 64);

    }

}
