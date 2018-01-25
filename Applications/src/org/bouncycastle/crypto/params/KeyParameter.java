package org.bouncycastle.crypto.params;

public class KeyParameter { 

    private byte[] key;

    public KeyParameter(byte[]  key) {
        this.key = key;
    }

    public byte[] getKey() {
        return key;
    }

}
