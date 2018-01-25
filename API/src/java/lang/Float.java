package java.lang;

public final class Float extends Number implements Comparable {
	private static final long serialVersionUID = -2671257302660747028L;

	public static final float MAX_VALUE = 3.4028235e+38f;

	public static final float MIN_VALUE = 1.4e-45f;

	public static final float NEGATIVE_INFINITY = -1.0f / 0.0f;

	public static final float POSITIVE_INFINITY = 1.0f / 0.0f;

	public static final float NaN = 0.0f / 0.0f;

	// TODO: Get primitive class for F
	public static final Class TYPE = null;

	private final float value;

	public Float (float value) {
		this.value = value;
	}

	public Float (double value) {
		this.value = (float) value;
	}

	public Float (String s) {
		value = parseFloat (s);
	}

	/**
	 * IMPROVISED toString method
	 * @param f Float to convert
	 * @return Converted float
	 */
	public static String toString (float f) {
		if (f == POSITIVE_INFINITY)
			return "Infinity";
		if(f == NEGATIVE_INFINITY)
			return "-Infinity";
		if(f == NaN)
			return "NaN";
		
		StringBuffer result = new StringBuffer ();
		if (f < 0)
			result.append ('-');
		if(f == 0){
			result.append("0.0");
			return result.toString();
		}
		float tmp = Math.abs (f);	
		if (tmp > 0.001f && tmp < 10000000.f) { //if f is greater than 10⁻3 but less than 10⁷
			result.append (Integer.toString ((int) tmp));
			 tmp = tmp - (int)tmp;
			result.append ('.');
			int it = 0;
			do {
				it++;
				tmp = (float)tmp * 10;
				result.append ((int)tmp);
				tmp = tmp - (int)tmp;
			} while (tmp > 0.000001f*pow(10, it) && it < 8); //TODO: check
		}else{ //computerized scientific notation
			int n = log10(tmp);
			double pow = ((n>=0)? pow(10, n-1): pow(10, -n-1));
			tmp = (float)((n>=0)? tmp/pow: tmp*pow);
			result.append (Integer.toString ((int) tmp));
			tmp = (tmp - ((int)tmp));
			result.append ('.');
			int it = 0;
			do {
				it++;
				tmp = (float)tmp * 10;
				result.append ((int)tmp);
				tmp = tmp - (int)tmp;
			} while (tmp > 0.000001f*pow(10, it) && it < 8); //TODO: check
			
			result.append("E");
			result.append(Integer.toString(n));
		}
		
		return result.toString();
	}
	
	private static double pow(int a, int b){
		double res = a;
		if(b == 0)
			return 1;
		for(int i = 0; i < b; i++){
			res*= a;
		}
		return res;
	}
	
	private static int log10(float d){
		int val = 0;

		if( d < 1){
			long pow = 1;
			val = -1;
			for(int i = 0; i < 47; i++){
				if(pow == 1000000000000000000l){
					d*= pow;
					pow = 10;
				}else
					pow *= 10;
				if(d*pow >= 1){
					return val;
				}
				
				val-=1;
			}
		}else{ //d > 1
			long pow = 1;
		for(int i = 0; i < 40; i++){
			if(pow == 1000000000000000000l){
				d = d/ pow;
				pow = 10;
			}else
				pow *= 10;
			if(pow> d)
				return val;
			else
				val+=1;
		}
		}
		return Integer.MAX_VALUE;
	}

	public static Float valueOf (String s) {
		return new Float (parseFloat (s));
	}

	public static float parseFloat (String str) {
		String[] ee = str.split("e");
		if(ee.length >1){
			return parseFloat(ee[0])*(float)Math.pow(10, Integer.parseInt(ee[1]));
		}
		
		
		float whole = 0, frac = 0;
		byte[] digits = str.getBytes();
		float sign = 1;
		
		int cnt = 0;
		if(digits[0] == '-'){
			sign = -1;
			cnt = 1;
		}
		
		while(cnt < str.length() && digits[cnt] != '.'){
			whole = whole*10 + (digits[cnt++] -'0');
		}
		if(cnt < str.length()){
			cnt = str.length()-1;
			while(digits[cnt] != '.'){
				frac = frac/10 + (digits[cnt--] - '0');
			}
		}
		return sign*(whole + frac/10);
		
//		return (float) Double.parseDouble (str);
	}

	public static boolean isNaN (float v) {
		return v != v;
	}

	public static boolean isInfinite (float v) {
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
		return value;
	}

	public double doubleValue () {
		return value;
	}

	public int hashCode () {
		return floatToIntBits (value);
	}

	public boolean equals (Object obj) {
		if (!(obj instanceof Float))
			return false;

		float f = ((Float) obj).value;

		if (value == f)
			return (value != 0) || (1 / value == 1 / f);
		return isNaN (value) && isNaN (f);
	}

	public static int floatToIntBits (float value) {
		if (Float.isNaN (value)) {
			return 0x7fc00000;
		} else {
			return floatToRawIntBits (value);
		}
	}

	// TODO: Native because this operation will simply be stripped from the
	// bytecode. It is simply providing a means of telling the compiler that
	// the given float value on the stack will be considered as a int.
	public native static int floatToRawIntBits (float value);

	// TODO: Native because this operation will simply be stripped from the
	// bytecode. It is simply providing a means of telling the compiler that
	// the given int value on the stack will be considered as a float.
	public native static float intBitsToFloat (int bits);

	public int compareTo (Float f) {
		return compare (value, f.value);
	}

	public int compareTo (Object o) {
		return compare (value, ((Float) o).value);
	}

	public static int compare (float x, float y) {
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
