package de.amidar.crypto;

import org.bouncycastle.crypto.engines.SerpentEngine;
import org.bouncycastle.crypto.params.KeyParameter;

public class SerpentTest_correctness {
	 private byte[] key;

	    public static void main(String[] args) {

	        byte[] key = {
		    (byte)61,  (byte)182, (byte)188, (byte)145, 
		    (byte)64,  (byte)118, (byte)78,  (byte)3,
		    (byte)42,  (byte)171, (byte)130, (byte)235, 
		    (byte)249, (byte)88,  (byte)208, (byte)21
		    ,(byte)79,  (byte)63,  (byte)210, (byte)74, 
		    (byte)41,  (byte)88,  (byte)203, (byte)45
		    ,(byte)236, (byte)141, (byte)92,  (byte)219,
		    (byte)1,   (byte)63,  (byte)9,   (byte)108
		};

		byte[] data = {	
		    (byte)17,  (byte)234, (byte)58, (byte)177,
		    (byte)143, (byte)222, (byte)27, (byte)162, 
		    (byte)155, (byte)39,  (byte)49, (byte)174,
		    (byte)241, (byte)10,  (byte)43, (byte)99
		};
		
		SerpentTest_correctness serpenttest = new SerpentTest_correctness(key);
		serpenttest.run(data);

	    }

	    public SerpentTest_correctness(byte[] key) {
		this.key = key;
	    }

	    public void run(byte[] data) {

		byte[] encrypted = new byte[data.length];

		SerpentEngine engine = new SerpentEngine();

		KeyParameter param = new KeyParameter(key);
 
		engine.init(true, param);

		for(int i = 0; i< encrypted.length; i++){
			System.out.print(data[i]);
			System.out.print(',');
		}System.out.println();

		engine.processBlock(data, 0, encrypted, 0);
		engine.processBlock(data, 0, encrypted, 0);
		engine.processBlock(data, 0, encrypted, 0);

		for(int i = 0; i< encrypted.length; i++){
			System.out.print(encrypted[i]);
			System.out.print(',');
		}
		System.out.println();
	    }
}