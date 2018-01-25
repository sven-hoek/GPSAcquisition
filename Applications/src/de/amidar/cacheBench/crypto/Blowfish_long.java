package de.amidar.cacheBench.crypto;

import org.bouncycastle.crypto.engines.BlowfishEngine;
import org.bouncycastle.crypto.params.KeyParameter;

import de.amidar.AmidarSystem;
import de.amidar.cacheBench.CacheBenchParameters;


public class Blowfish_long {
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
    

		int smallest = 8;
		int length = smallest * CacheBenchParameters.getBenchmarkScaleFactor();

		byte[] dataShort = new byte[32];
		byte[] dataLong = new byte[length];
    
        Blowfish_long blowfishtest = new Blowfish_long(key);
        blowfishtest.run(dataShort); 
        blowfishtest.run(dataLong);
    
    }
    
    public Blowfish_long(byte[] key) {
        this.key = key;
    }
    
    public void run(byte[] data) {
    
        byte[] encrypted = new byte[data.length];
    
        BlowfishEngine engine = new BlowfishEngine();
    
        KeyParameter param = new KeyParameter(key);
        
        AmidarSystem.invalidateFlushAllCaches();
    
        engine.init(true, param);
        
        
        for(int index = 0; index < data.length; index +=8){
			engine.processBlock(data, index, encrypted, index);
			//Block chaining
			if(index+8 < data.length)
				for(int i = 0; i < 8; i++){
					data[index+8+i] ^= encrypted[index+i];
				}
		}
    
        
//        for(int i = 0; i< encrypted.length; i++){
//			System.out.print(encrypted[i]);
//			System.out.print(',');
//		}
//		System.out.println();
    
    }

}
