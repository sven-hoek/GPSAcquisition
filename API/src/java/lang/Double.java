package java.lang;

public final class Double extends Number implements Comparable {
	private static final long serialVersionUID = -9172774392245257468L;

	public static final double MAX_VALUE = 1.7976931348623157e+308;

	public static final double MIN_VALUE = 5e-324;

	public static final double NEGATIVE_INFINITY = -1.0 / 0.0;

	public static final double POSITIVE_INFINITY = 1.0 / 0.0;

	public static final double NaN = 0.0 / 0.0;

	// TODO: Get primitive class for D
	public static final Class TYPE = null;

	private final double value;

	public Double (double value) {
		this.value = value;
	}

	public Double (String s) {
		value = parseDouble (s);
	}

	/**
	 * IMPROVISED toString method
	 * @param d Double to convert
	 * @return Converted double
	 */
	public static String toString (double d) {
		if (isNaN (d))
			return "NaN";
		if (isInfinite (d))
			return "INF";
		
		StringBuffer result = new StringBuffer ();
		if (d < 0)
			result.append ('-');
		
		d = Math.abs (d);
		
		result.append (Integer.toString ((int) d));
		
		d = d - (int)d;
		if (d > 0.00000001) {
			result.append ('.');
			do {
				d = d * 10;
				result.append ((int)d);
				d = d - (int)d;
			} while (d > 0.00000001);
		}
		
		return result.toString ();
	}

	public static Double valueOf (String s) {
		return new Double (parseDouble (s));
	}

	public static double parseDouble (String str) {
		throw new UnsupportedOperationException ();
	}

	public static boolean isNaN (double v) {
		return v != v;
	}

	public static boolean isInfinite (double v) {
		return v == POSITIVE_INFINITY || v == NEGATIVE_INFINITY;
	}

	public boolean isNaN () {
		return isNaN (value);
	}

	public boolean isInfinite () {
		return isInfinite (value);
	}

	public String toString () {
		return toString (value);
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
		return (long) value;
	}

	public float floatValue () {
		return (float) value;
	}

	public double doubleValue () {
		return value;
	}

	public int hashCode () {
		long v = doubleToLongBits (value);
		return (int) (v ^ (v >>> 32));
	}

	public boolean equals (Object obj) {
		if (!(obj instanceof Double))
			return false;

		double d = ((Double) obj).value;

		if (value == d)
			return (value != 0) || (1 / value == 1 / d);
		return isNaN (value) && isNaN (d);
	}

	public static long doubleToLongBits (double value) {
		if (Double.isNaN (value)) {
			return 0x7ff8000000000000L;
		} else {
			return doubleToRawLongBits (value);
		}
	}

	// TODO: Native because this operation will simply be stripped from the
	// bytecode. It is simply providing a means of telling the compiler that
	// the given double value on the stack will be considered as a long.
	public  static long doubleToRawLongBits (double value){
		System.out.println("IIIIIIIIIIIIxxxIIIIIIIIIIIIIIIIIIIIII");
		return 0;
	}

	// TODO: Native because this operation will simply be stripped from the
	// bytecode. It is simply providing a means of telling the compiler that
	// the given long value on the stack will be considered as a double.
	public  static double longBitsToDouble (long bits){
		System.out.println("PPPPPPPPPPPxxxxxxxxxxxxxxxxxxxPP");
		return 0;
	}

	public int compareTo (Double d) {
		return compare (value, d.value);
	}

	public int compareTo (Object o) {
		return compare (value, ((Double) o).value);
	}

	public static int compare (double x, double y) {
		if (isNaN (x))
			return isNaN (y) ? 0 : 1;
		if (isNaN (y))
			return -1;
		if (x == 0 && y == 0)
			return (int) (1 / x - 1 / y);
		if (x == y)
			return 0;

		return x > y ? 1 : -1;
	}
}
