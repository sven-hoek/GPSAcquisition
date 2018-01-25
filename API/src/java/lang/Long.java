package java.lang;

public final class Long extends Number implements Comparable {
	private static final long serialVersionUID = 4290774380558885855L;

	public static final long MIN_VALUE = 0x8000000000000000L;

	public static final long MAX_VALUE = 0x7fffffffffffffffL;

	// TODO: Get primitive class for J
	public static final Class TYPE = null;

	private final long value;

	public Long (long value) {
		this.value = value;
	}

	public Long (String s) {
		value = parseLong (s, 10, false);
	}

	public static String toString (long num, int radix) {
		if ((int) num == num)
			return Integer.toString ((int) num, radix);

		if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX)
			radix = 10;

		char[] buffer = new char[65];
		int i = 65;
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
			buffer[--i] = digits[(int) (num % radix)];
			num /= radix;
		} while (num > 0);

		if (isNeg)
			buffer[--i] = '-';

		return new String (buffer, i, 65 - i, true);
	}

	public static String toHexString (long l) {
		return toUnsignedString (l, 4);
	}

	public static String toOctalString (long l) {
		return toUnsignedString (l, 3);
	}

	public static String toBinaryString (long l) {
		return toUnsignedString (l, 1);
	}

	public static String toString (long num) {
		return toString (num, 10);
	}

	public static long parseLong (String str, int radix) {
		return parseLong (str, radix, false);
	}

	public static long parseLong (String s) {
		return parseLong (s, 10, false);
	}

	public static Long valueOf (String s, int radix) {
		return new Long (parseLong (s, radix, false));
	}

	public static Long valueOf (String s) {
		return new Long (parseLong (s, 10, false));
	}

	public static Long decode (String str) {
		return new Long (parseLong (str, 10, true));
	}

	public byte byteValue () {
		return (byte) value;
	}

	public short shortValue () {
		return (short) value;
	}

	public int intValue () {
		return (int) value;
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
		return toString (value, 10);
	}

	public int hashCode () {
		return (int) (value ^ (value >>> 32));
	}

	public boolean equals (Object obj) {
		return obj instanceof Long && value == ((Long) obj).value;
	}

	public static Long getLong (String nm) {
		return getLong (nm, null);
	}

	public static Long getLong (String nm, long val) {
		Long result = getLong (nm, null);
		return result == null ? new Long (val) : result;
	}

	public static Long getLong (String nm, Long def) {
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

	public int compareTo (Long l) {
		if (value == l.value)
			return 0;
		return value > l.value ? 1 : -1;
	}

	public int compareTo (Object o) {
		return compareTo ((Long) o);
	}

	private static String toUnsignedString (long num, int exp) {
		if (num >= 0 && (int) num == num)
			return Integer.toUnsignedString ((int) num, exp);

		int mask = (1 << exp) - 1;
		char[] buffer = new char[64];
		int i = 64;
		do {
			buffer[--i] = digits[(int) num & mask];
			num >>>= exp;
		} while (num != 0);

		return new String (buffer, i, 64 - i, true);
	}

	private static long parseLong (String str, int radix, boolean decode) {
		if (!decode && str == null)
			throw new NumberFormatException ();
		int index = 0;
		int len = str.length ();
		boolean isNeg = false;
		if (len == 0)
			throw new NumberFormatException ();
		int ch = str.charAt (index);
		if (ch == '-') {
			if (len == 1)
				throw new NumberFormatException ();
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
			throw new NumberFormatException ();

		long max = MAX_VALUE / radix;
		if (isNeg && MAX_VALUE % radix == radix - 1)
			++max;

		long val = 0;
		while (index < len) {
			if (val < 0 || val > max)
				throw new NumberFormatException ();

			ch = Character.digit (str.charAt (index++), radix);
			val = val * radix + ch;
			if (ch < 0 || (val < 0 && (!isNeg || val != MIN_VALUE)))
				throw new NumberFormatException ();
		}
		return isNeg ? -val : val;
	}
}
