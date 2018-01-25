package de.amidar.digests;

import org.bouncycastle.crypto.digests.SHA1Digest;

public class SHA1Test_orig_short {

    public static void main(String[] args) {

	int[] data = {	
	    17,  234, 58, 177,
	    143, 222, 27, 162, 
	    155, 39,  49, 174,
	    241, 10,  43, 99
	};

	SHA1Test_orig_short sha1test = new SHA1Test_orig_short();
	sha1test.run(data);

    }

    public void run(int[] data) {

	SHA1Digest digest = new SHA1Digest();
	
	digest.processBlock(data);
        digest.processBlock(data);
	    
    }

}
