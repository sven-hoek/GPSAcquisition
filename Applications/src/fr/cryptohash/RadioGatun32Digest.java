package fr.cryptohash;

public class RadioGatun32Digest {

	private int[] a, b;

        private int digestLen, blockLen, inputLen;
        private byte[] inputBuf, outputBuf;
        private long blockCount;

        protected final int flush()
        {
                return inputLen;
        }

        protected final byte[] getBlockBuffer()
        {
                return inputBuf;
        }

      public void update(byte[] input, int offset, int len)
        {
                while (len > 0) {
                        int copyLen = blockLen - inputLen;
                        if (copyLen > len)
                                copyLen = len;
                        System.arraycopy(input, offset, inputBuf, inputLen,
                                copyLen);
                        offset += copyLen;
                        inputLen += copyLen;
                        len -= copyLen;
                        if (inputLen == blockLen) {
                                processBlock(inputBuf);
                                blockCount ++;
                                inputLen = 0;
                        }
                }
        }

	/**
	 * Build the object.
	 */
	public RadioGatun32Digest()
	{

                doInit();
                digestLen = getDigestLength();
                blockLen = getInternalBlockLength();
                inputBuf = new byte[blockLen];
                outputBuf = new byte[digestLen];
                inputLen = 0;
                blockCount = 0;

	}

	/** @see Digest */
	public int getDigestLength()
	{
		return 32;
	}

	/** @see DigestEngine */
	protected int getInternalBlockLength()
	{
		return 156;
	}

	/** @see Digest */
	public int getBlockLength()
	{
		return -12;
	}

	/** @see DigestEngine */
	protected void engineReset()
	{
		for (int i = 0; i < a.length; i ++)
			a[i] = 0;
		for (int i = 0; i < b.length; i ++)
			b[i] = 0;
	}

	/** @see DigestEngine */
	protected void doInit()
	{
		a = new int[19];
		b = new int[39];
		engineReset();
	}

	/**
	 * Encode the 32-bit word {@code val} into the array
	 * {@code buf} at offset {@code off}, in little-endian
	 * convention (least significant byte first).
	 *
	 * @param val   the value to encode
	 * @param buf   the destination buffer
	 * @param off   the destination offset
	 */
	private static final void encodeLEInt(int val, byte[] buf, int off)
	{
		buf[off + 0] = (byte)val;
		buf[off + 1] = (byte)(val >>> 8);
		buf[off + 2] = (byte)(val >>> 16);
		buf[off + 3] = (byte)(val >>> 24);
	}

	/**
	 * Decode a 32-bit little-endian word from the array {@code buf}
	 * at offset {@code off}.
	 *
	 * @param buf   the source buffer
	 * @param off   the source offset
	 * @return  the decoded value
	 */
	private static final int decodeLEInt(byte[] buf, int off)
	{
		return ((buf[off + 3] & 0xFF) << 24)
			| ((buf[off + 2] & 0xFF) << 16)
			| ((buf[off + 1] & 0xFF) << 8)
			| (buf[off] & 0xFF);
	}

	/** @see DigestEngine */
	protected void processBlock(byte[] data)
	{
		int a00 = a[ 0];
		int a01 = a[ 1];
		int a02 = a[ 2];
		int a03 = a[ 3];
		int a04 = a[ 4];
		int a05 = a[ 5];
		int a06 = a[ 6];
		int a07 = a[ 7];
		int a08 = a[ 8];
		int a09 = a[ 9];
		int a10 = a[10];
		int a11 = a[11];
		int a12 = a[12];
		int a13 = a[13];
		int a14 = a[14];
		int a15 = a[15];
		int a16 = a[16];
		int a17 = a[17];
		int a18 = a[18];

		int dp = 0;
		for (int mk = 12; mk >= 0; mk --) {
			int p0 = decodeLEInt(data, dp + 0);
			int p1 = decodeLEInt(data, dp + 4);
			int p2 = decodeLEInt(data, dp + 8);
			dp += 12;
			int bj = (mk == 12) ? 0 : 3 * (mk + 1);
			b[bj + 0] ^= p0;
			b[bj + 1] ^= p1;
			b[bj + 2] ^= p2;
			a16 ^= p0;
			a17 ^= p1;
			a18 ^= p2;

			bj = mk * 3;
			if ((bj += 3) == 39)
				bj = 0;
			b[bj + 0] ^= a01;
			if ((bj += 3) == 39)
				bj = 0;
			b[bj + 1] ^= a02;
			if ((bj += 3) == 39)
				bj = 0;
			b[bj + 2] ^= a03;
			if ((bj += 3) == 39)
				bj = 0;
			b[bj + 0] ^= a04;
			if ((bj += 3) == 39)
				bj = 0;
			b[bj + 1] ^= a05;
			if ((bj += 3) == 39)
				bj = 0;
			b[bj + 2] ^= a06;
			if ((bj += 3) == 39)
				bj = 0;
			b[bj + 0] ^= a07;
			if ((bj += 3) == 39)
				bj = 0;
			b[bj + 1] ^= a08;
			if ((bj += 3) == 39)
				bj = 0;
			b[bj + 2] ^= a09;
			if ((bj += 3) == 39)
				bj = 0;
			b[bj + 0] ^= a10;
			if ((bj += 3) == 39)
				bj = 0;
			b[bj + 1] ^= a11;
			if ((bj += 3) == 39)
				bj = 0;
			b[bj + 2] ^= a12;

			int t00 = a00 ^ (a01 | ~a02);
			int t01 = a01 ^ (a02 | ~a03);
			int t02 = a02 ^ (a03 | ~a04);
			int t03 = a03 ^ (a04 | ~a05);
			int t04 = a04 ^ (a05 | ~a06);
			int t05 = a05 ^ (a06 | ~a07);
			int t06 = a06 ^ (a07 | ~a08);
			int t07 = a07 ^ (a08 | ~a09);
			int t08 = a08 ^ (a09 | ~a10);
			int t09 = a09 ^ (a10 | ~a11);
			int t10 = a10 ^ (a11 | ~a12);
			int t11 = a11 ^ (a12 | ~a13);
			int t12 = a12 ^ (a13 | ~a14);
			int t13 = a13 ^ (a14 | ~a15);
			int t14 = a14 ^ (a15 | ~a16);
			int t15 = a15 ^ (a16 | ~a17);
			int t16 = a16 ^ (a17 | ~a18);
			int t17 = a17 ^ (a18 | ~a00);
			int t18 = a18 ^ (a00 | ~a01);

			a00 = t00;
			a01 = (t07 << 31) | (t07 >>>  1);
			a02 = (t14 << 29) | (t14 >>>  3);
			a03 = (t02 << 26) | (t02 >>>  6);
			a04 = (t09 << 22) | (t09 >>> 10);
			a05 = (t16 << 17) | (t16 >>> 15);
			a06 = (t04 << 11) | (t04 >>> 21);
			a07 = (t11 <<  4) | (t11 >>> 28);
			a08 = (t18 << 28) | (t18 >>>  4);
			a09 = (t06 << 19) | (t06 >>> 13);
			a10 = (t13 <<  9) | (t13 >>> 23);
			a11 = (t01 << 30) | (t01 >>>  2);
			a12 = (t08 << 18) | (t08 >>> 14);
			a13 = (t15 <<  5) | (t15 >>> 27);
			a14 = (t03 << 23) | (t03 >>>  9);
			a15 = (t10 <<  8) | (t10 >>> 24);
			a16 = (t17 << 24) | (t17 >>>  8);
			a17 = (t05 <<  7) | (t05 >>> 25);
			a18 = (t12 << 21) | (t12 >>> 11);

			t00 = a00 ^ a01 ^ a04;
			t01 = a01 ^ a02 ^ a05;
			t02 = a02 ^ a03 ^ a06;
			t03 = a03 ^ a04 ^ a07;
			t04 = a04 ^ a05 ^ a08;
			t05 = a05 ^ a06 ^ a09;
			t06 = a06 ^ a07 ^ a10;
			t07 = a07 ^ a08 ^ a11;
			t08 = a08 ^ a09 ^ a12;
			t09 = a09 ^ a10 ^ a13;
			t10 = a10 ^ a11 ^ a14;
			t11 = a11 ^ a12 ^ a15;
			t12 = a12 ^ a13 ^ a16;
			t13 = a13 ^ a14 ^ a17;
			t14 = a14 ^ a15 ^ a18;
			t15 = a15 ^ a16 ^ a00;
			t16 = a16 ^ a17 ^ a01;
			t17 = a17 ^ a18 ^ a02;
			t18 = a18 ^ a00 ^ a03;

			a00 = t00 ^ 1;
			a01 = t01;
			a02 = t02;
			a03 = t03;
			a04 = t04;
			a05 = t05;
			a06 = t06;
			a07 = t07;
			a08 = t08;
			a09 = t09;
			a10 = t10;
			a11 = t11;
			a12 = t12;
			a13 = t13;
			a14 = t14;
			a15 = t15;
			a16 = t16;
			a17 = t17;
			a18 = t18;

			bj = mk * 3;
			a13 ^= b[bj + 0];
			a14 ^= b[bj + 1];
			a15 ^= b[bj + 2];
		}

		a[ 0] = a00;
		a[ 1] = a01;
		a[ 2] = a02;
		a[ 3] = a03;
		a[ 4] = a04;
		a[ 5] = a05;
		a[ 6] = a06;
		a[ 7] = a07;
		a[ 8] = a08;
		a[ 9] = a09;
		a[10] = a10;
		a[11] = a11;
		a[12] = a12;
		a[13] = a13;
		a[14] = a14;
		a[15] = a15;
		a[16] = a16;
		a[17] = a17;
		a[18] = a18;
	}
	
	public int[] getH(){
		return a;
	}

}
