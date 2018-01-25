package de.amidar.digests;

import de.amidar.AmidarSystem;
import fr.cryptohash.ECOH256Digest;

public class ECOH256Test_correctness {
    public static void main(String[] args) {

//	byte[] data = {	
//	    (byte)17,  (byte)234, (byte)58, (byte)177,
//	    (byte)143, (byte)222, (byte)27, (byte)162, 
//	    (byte)155, (byte)39,  (byte)49, (byte)174,
//	    (byte)241, (byte)10,  (byte)43, (byte)99,
//	    (byte)17,  (byte)234, (byte)58, (byte)177,
//	    (byte)143, (byte)222, (byte)27, (byte)162, 
//	    (byte)155, (byte)39,  (byte)49, (byte)174,
//	    (byte)241, (byte)10,  (byte)43, (byte)99,
//	    (byte)17,  (byte)234, (byte)58, (byte)177,
//	    (byte)143, (byte)222, (byte)27, (byte)162, 
//	    (byte)155, (byte)39,  (byte)49, (byte)174,
//	    (byte)241, (byte)10,  (byte)43, (byte)99,
//	    (byte)17,  (byte)234, (byte)58, (byte)177,
//	    (byte)143, (byte)222, (byte)27, (byte)162, 
//	    (byte)155, (byte)39,  (byte)49, (byte)174,
//	    (byte)241, (byte)10,  (byte)43, (byte)99
//	};
    	
    	byte[] data = new byte[128];
//    	byte[] data = new byte[512];
//    	byte[] data = new byte[128];

	ECOH256Test_correctness ecoc256test = new ECOH256Test_correctness();
	ecoc256test.run(data);

    }

    public void run(byte[] data) {

	ECOH256Digest digest = new ECOH256Digest();

	
//	AmidarSystem.invalidateFlushAllCaches();
	
        digest.update(data, 0, data.length);
        digest.update(data, 0, 64);
        digest.update(data, 0, 64);
//
//        digest.update(data, 0, 64);
//        digest.update(data, 0, 64);
//        digest.update(data, 0, 64);

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
