package org.bouncycastle.crypto.digests;

/**
 * implementation of SHA-1 as outlined in "Handbook of Applied Cryptography", pages 346 - 349.
 *
 * It is interesting to ponder why the, apart from the extra IV, the other difference here from MD5
 * is the "endienness" of the word processing!
 */
public class SHA1Digest
{
    private static final int    DIGEST_LENGTH = 20;

    private int     H1, H2, H3, H4, H5;

    /**
     * Standard constructor
     */
    public SHA1Digest()
    {
        reset();
    }

    public int getDigestSize()
    {
        return DIGEST_LENGTH;
    }

    public void reset()
    {

        H1 = 0x67452301;
        H2 = 0xefcdab89;
        H3 = 0x98badcfe;
        H4 = 0x10325476;
        H5 = 0xc3d2e1f0;

    }

    //
    // Additive constants
    //
    private static final int    Y1 = 0x5a827999;
    private static final int    Y2 = 0x6ed9eba1;
    private static final int    Y3 = 0x8f1bbcdc;
    private static final int    Y4 = 0xca62c1d6;
   
    private int f(
        int    u,
        int    v,
        int    w)
    {
        return ((u & v) | ((~u) & w));
    }
    
    /*
     * rotate int x left n bits.
     */
    private int rotateLeft(int x, int n) {
        return (x << n) | (x >>> (32 - n));
    }

    private int h(
        int    u,
        int    v,
        int    w)
    {
        return (u ^ v ^ w);
    }

    private int g(
        int    u,
        int    v,
        int    w)
    {
        return ((u & v) | (u & w) | (v & w));
    }

    public void processBlock(int[] data)
    {

	int[] X = new int[80];
	System.arraycopy(data, 0, X, 0, data.length);

        //
        // expand 16 word block into 80 word block.
        //
        for (int i = 16; i < 80; i++)
        {
            int t = X[i - 3] ^ X[i - 8] ^ X[i - 14] ^ X[i - 16];
            X[i] = t << 1 | t >>> 31;
        }

        //
        // set up working variables.
        //
        int     A = H1;
        int     B = H2;
        int     C = H3;
        int     D = H4;
        int     E = H5;

        //
        // round 1
        //
        int idx = 0;
        
        for (int j = 0; j < 4; j++)
        {
            E += rotateLeft(A, 5) + f(B, C, D) + X[idx++] + Y1;
            B = rotateLeft(B, 30);
        
            D += rotateLeft(E, 5) + f(A, B, C) + X[idx++] + Y1;
            A = rotateLeft(A, 30);
       
            C += rotateLeft(D, 5) + f(E, A, B) + X[idx++] + Y1;
            E = rotateLeft(E, 30);
       
            B += rotateLeft(C, 5) + f(D, E, A) + X[idx++] + Y1;
            D = rotateLeft(D, 30);

            A += rotateLeft(B, 5) + f(C, D, E) + X[idx++] + Y1;
            C = rotateLeft(C, 30);
        	
        }
        
        //
        // round 2
        //
        for (int j = 0; j < 4; j++)
        {
            E += rotateLeft(A, 5) + h(B, C, D) + X[idx++] + Y2;
            B = rotateLeft(B, 30);   
            
            D += rotateLeft(E, 5) + h(A, B, C) + X[idx++] + Y2;
            A = rotateLeft(A, 30);
            
            C += rotateLeft(D, 5) + h(E, A, B) + X[idx++] + Y2;
            E = rotateLeft(E, 30);
            
            B += rotateLeft(C, 5) + h(D, E, A) + X[idx++] + Y2;
            D = rotateLeft(D, 30);

            A += rotateLeft(B, 5) + h(C, D, E) + X[idx++] + Y2;
            C = rotateLeft(C, 30);
        }
        
        //
        // round 3
        //
        for (int j = 0; j < 4; j++)
        {
            E += rotateLeft(A, 5) + g(B, C, D) + X[idx++] + Y3;
            B = rotateLeft(B, 30);
            
            D += rotateLeft(E, 5) + g(A, B, C) + X[idx++] + Y3;
            A = rotateLeft(A, 30);
            
            C += rotateLeft(D, 5) + g(E, A, B) + X[idx++] + Y3;
            E = rotateLeft(E, 30);
            
            B += rotateLeft(C, 5) + g(D, E, A) + X[idx++] + Y3;
            D = rotateLeft(D, 30);

            A += rotateLeft(B, 5) + g(C, D, E) + X[idx++] + Y3;
            C = rotateLeft(C, 30);
        }

        //
        // round 4
        //
        for (int j = 0; j <= 3; j++)
        {
            E += rotateLeft(A, 5) + h(B, C, D) + X[idx++] + Y4;
            B = rotateLeft(B, 30);
            
            D += rotateLeft(E, 5) + h(A, B, C) + X[idx++] + Y4;
            A = rotateLeft(A, 30);
            
            C += rotateLeft(D, 5) + h(E, A, B) + X[idx++] + Y4;
            E = rotateLeft(E, 30);
            
            B += rotateLeft(C, 5) + h(D, E, A) + X[idx++] + Y4;
            D = rotateLeft(D, 30);

            A += rotateLeft(B, 5) + h(C, D, E) + X[idx++] + Y4;
            C = rotateLeft(C, 30);
        }


        H1 += A;
        H2 += B;
        H3 += C;
        H4 += D;
        H5 += E;

        //
        // reset start of the buffer.
        //
        for (int i = 0; i < 16; i++)
        {
            X[i] = 0;
        }
    }
    
    public int[] getH(){
    	int[] erg = {H1,H2,H3,H4,H5};
    	return erg;
    }
}



