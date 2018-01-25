package java.lang;

import java.io.Serializable;

public abstract class Number implements Serializable {
	private static final long serialVersionUID = -8742448824652078965L;

	static final char[] digits = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
			'9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l',
			'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y',
			'z', };

	public Number () {
	}

	public abstract int intValue ();

	public abstract long longValue ();

	public abstract float floatValue ();

	public abstract double doubleValue ();

	public byte byteValue () {
		return (byte) intValue ();
	}

	public short shortValue () {
		return (short) intValue ();
	}
}
