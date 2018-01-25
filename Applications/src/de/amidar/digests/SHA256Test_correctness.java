package de.amidar.digests;

import org.bouncycastle.crypto.digests.SHA256Digest;

public class SHA256Test_correctness {
    public static void main(String[] args) {

	int[] data = {	
	    17,  234, 58, 177,
	    143, 222, 27, 162, 
	    155, 39,  49, 174,
	    241, 10,  43, 99
	};

	SHA256Test_correctness sha256test = new SHA256Test_correctness();
	sha256test.run(data);

    }

    public void run(int[] data) {

	SHA256Digest digest = new SHA256Digest();
	
	digest.processBlock(data);
	digest.processBlock(data);
	    
	
	int [] erg = digest.getH();
	

	for(int i = 0; i< erg.length; i++){
		System.out.print(erg[i]);
		System.out.print(',');
	}
	System.out.println();
	
    }
}
