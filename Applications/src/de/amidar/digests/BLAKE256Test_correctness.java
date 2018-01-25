package de.amidar.digests;

import fr.cryptohash.BLAKE256Digest;

public class BLAKE256Test_correctness {
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

	BLAKE256Test_correctness blake256test = new BLAKE256Test_correctness();
	blake256test.run(data);

    }

    public void run(byte[] data) {

	BLAKE256Digest digest = new BLAKE256Digest();

	digest.update(data, 0, 64);
	digest.update(data, 0, 64);
	
	int [] erg = digest.getH();

	for(int i = 0; i< erg.length; i++){
		System.out.print(erg[i]);
		System.out.print(',');
	}System.out.println();
	
    }

}
