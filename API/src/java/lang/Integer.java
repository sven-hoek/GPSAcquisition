package java.lang;

public final class Integer extends Number implements Comparable {
	private static final long serialVersionUID = 1360826667806852920L;

	public static final int MIN_VALUE = 0x80000000;

	public static final int MAX_VALUE = 0x7fffffff;

	// TODO: Get primitive class for I
	public static final Class TYPE = null;

	private static final int MIN_CACHE = -128;
	private static final int MAX_CACHE = 127;
	private static Integer[] intCache = new Integer[MAX_CACHE - MIN_CACHE + 1];

	private final int value;
	
	public Integer (int value) {
		this.value = value;
	}

	public Integer (String s) {
		value = parseInt (s, 10, false);
	}

	public static String toString (int num, int radix) {
		if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX)
			radix = 10;
		char[] buffer = new char[33];
		int i = 33;
		boolean isNeg = false;
		if (num < 0) {
			isNeg = true;
			num = -num;

			if (num < 0) {
				buffer[--i] = digits[(int) (-(num + radix) % radix)];
				num = -(num / radix);
			}
		}

		do {
			buffer[--i] = digits[num % radix];
			num /= radix;
		} while (num > 0);

		if (isNeg){
			buffer[--i] = '-';
		}
			
		return new String (buffer, i, 33 - i, true);
	}

	public static String toHexString (int i) {
		return toUnsignedString (i, 4);
	}

	public static String toOctalString (int i) {
		return toUnsignedString (i, 3);
	}

	public static String toBinaryString (int i) {
		return toUnsignedString (i, 1);
	}

	public static String toString (int i) {
		return toString (i, 10);
	}

	public static int parseInt (String str, int radix) {
		return parseInt (str, radix, false);
	}

	public static int parseInt (String s) {
		return parseInt (s, 10, false);
	}

	public static Integer valueOf (String s, int radix) {
		return new Integer (parseInt (s, radix, false));
	}

	public static Integer valueOf (String s) {
		return new Integer (parseInt (s, 10, false));
	}

	public static Integer valueOf (int val) {
		if (val < MIN_CACHE || val > MAX_CACHE)
			return new Integer (val);
		synchronized (intCache) {
			if (intCache[val - MIN_CACHE] == null)
				intCache[val - MIN_CACHE] = new Integer (val);
			return intCache[val - MIN_CACHE];
		}
	}

	public byte byteValue () {
		return (byte) value;
	}

	public short shortValue () {
		return (short) value;
	}

	public int intValue () {
		return value;
	}

	public long longValue () {
		return value;
	}

	public float floatValue () {
		return value;
	}

	public double doubleValue () {
		return value;
	}

	public String toString () {
		return toString (value);
	}

	public int hashCode () {
		return value;
	}

	public boolean equals (Object obj) {
		return obj instanceof Integer && value == ((Integer) obj).value;
	}

	public static Integer getInteger (String nm) {
		return getInteger (nm, null);
	}

	public static Integer getInteger (String nm, int val) {
		Integer result = getInteger (nm, null);
		return result == null ? new Integer (val) : result;
	}

	public static Integer getInteger (String nm, Integer def) {
		if (nm == null || "".equals (nm))
			return def;
		nm = System.getProperty (nm);
		if (nm == null)
			return def;
		try {
			return decode (nm);
		} catch (NumberFormatException e) {
			return def;
		}
	}

	public static Integer decode (String str) {
		return new Integer (parseInt (str, 10, true));
	}

	public int compareTo (Integer i) {
		if (value == i.value)
			return 0;
		return value > i.value ? 1 : -1;
	}

	public int compareTo (Object o) {
		return compareTo ((Integer) o);
	}

	static String toUnsignedString (int num, int exp) {
		int mask = (1 << exp) - 1;
		char[] buffer = new char[32];
		int i = 32;
		do {
			buffer[--i] = digits[num & mask];
			num >>>= exp;
		} while (num != 0);

		return new String (buffer, i, 32 - i, true);
	}

	static int parseInt (String str, int radix, boolean decode) {
		if (!decode && str == null){
			System.out.println("ISNULLL " + str);
			throw new NumberFormatException ();
			
		}
		int index = 0;
		int len = str.length ();
		boolean isNeg = false;
		if (len == 0)
			throw new NumberFormatException ("string length is null");
		int ch = str.charAt (index);
		if (ch == '-') {
			if (len == 1)
				throw new NumberFormatException ("pure '-'");
			isNeg = true;
			ch = str.charAt (++index);
		}
		if (decode) {
			if (ch == '0') {
				if (++index == len)
					return 0;
				if ((str.charAt (index) & ~('x' ^ 'X')) == 'X') {
					radix = 16;
					index++;
				} else
					radix = 8;
			} else if (ch == '#') {
				radix = 16;
				index++;
			}
		}
		
		if (index == len)
			throw new NumberFormatException ("non terminated number: " + str);
		
		int max = MAX_VALUE / radix;
		if (isNeg && MAX_VALUE % radix == radix - 1)
			++max;
		
		int val = 0;
		while (index < len) {
			if (val < 0 || val > max)
				throw new NumberFormatException ("number overflow (pos="
						+ index + ") : " + str);
			// TODO: Fix Character.digit and use that instead!
			//ch = Character.digit (str.charAt (index++), radix);
			{
				char c = str.charAt (index++);
				if (c < '0') {
					ch = -1;
				} else if (c <= '9') {
					ch = c - '0';
				} else if (c < 'A') {
					ch = -1;
				} else if (c <= 'F') {
					ch = c - 'A' + 10;
				} else if (c < 'a') {
					ch = -1;
				} else if (c <= 'f') {
					ch = c - 'a' + 10;
				} else {
					ch = -1;
				}
				if (ch >= radix) {
					ch = -1;
				}
			}
			val = val * radix + ch;
			if (ch < 0 || (val < 0 && (!isNeg || val != MIN_VALUE))){
				throw new NumberFormatException (
						"invalid character at position " + index + " in " + str);
			}
		}
		return isNeg ? -val : val;
	}
	
	final static int [] sizeTable = { 9, 99, 999, 9999, 99999, 999999, 9999999,
		99999999, 999999999, Integer.MAX_VALUE };

	// Requires positive x
	public static int stringSize(int x) {
		int ret = -1;
		boolean found = false;
		for (int i=0; i< sizeTable.length ; i++){
			if (x <= sizeTable[i] && !found){
				ret = i+1;
				found = true;
			}
		}
		return ret;
	}
	
    final static char [] DigitTens = {
        '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
        '1', '1', '1', '1', '1', '1', '1', '1', '1', '1',
        '2', '2', '2', '2', '2', '2', '2', '2', '2', '2',
        '3', '3', '3', '3', '3', '3', '3', '3', '3', '3',
        '4', '4', '4', '4', '4', '4', '4', '4', '4', '4',
        '5', '5', '5', '5', '5', '5', '5', '5', '5', '5',
        '6', '6', '6', '6', '6', '6', '6', '6', '6', '6',
        '7', '7', '7', '7', '7', '7', '7', '7', '7', '7',
        '8', '8', '8', '8', '8', '8', '8', '8', '8', '8',
        '9', '9', '9', '9', '9', '9', '9', '9', '9', '9',
        } ;

    final static char [] DigitOnes = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        } ;
	
	
	/**
     * Places characters representing the integer i into the
     * character array buf. The characters are placed into
     * the buffer backwards starting with the least significant
     * digit at the specified index (exclusive), and working
     * backwards from there.
     *
     * Will fail if i == Integer.MIN_VALUE
     */
    static void getChars(int i, int index, char[] buf) {
        int q, r;
        int charPos = index;
        char sign = 0;

        if (i < 0) {
            sign = '-';
            i = -i;
        }

        // Generate two digits per iteration
        while (i >= 65536) {
            q = i / 100;
        // really: r = i - (q * 100);
            r = i - ((q << 6) + (q << 5) + (q << 2));
            i = q;
            buf [--charPos] = DigitOnes[r];
            buf [--charPos] = DigitTens[r];
        }

        // Fall thru to fast mode for smaller numbers
        // assert(i <= 65536, i);
        for (;;) {
            q = (i * 52429) >>> (16+3);
            r = i - ((q << 3) + (q << 1));  // r = i-(q*10) ...
            buf [--charPos] = digits [r];
            i = q;
            if (i == 0) break;
        }
        if (sign != 0) {
            buf [--charPos] = sign;
        }
    }
}
