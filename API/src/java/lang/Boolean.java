package java.lang;

import java.io.Serializable;

public final class Boolean implements Serializable {
	private static final long serialVersionUID = -3665804199014368530L;

	public static final Boolean TRUE = new Boolean (true);

	public static final Boolean FALSE = new Boolean (false);

	// TODO: Get primitive class for Z
	public static final Class TYPE = null;

	private final boolean value;

	public Boolean (boolean value) {
		this.value = value;
	}

	public Boolean (String s) {
		value = "true".equalsIgnoreCase (s);
	}

	public boolean booleanValue () {
		return value;
	}

	public static Boolean valueOf (boolean b) {
		return b ? TRUE : FALSE;
	}

	public static Boolean valueOf (String s) {
		return "true".equalsIgnoreCase (s) ? TRUE : FALSE;
	}

	public static String toString (boolean b) {
		return b ? "true" : "false";
	}

	public String toString () {
		return value ? "true" : "false";
	}

	public int hashCode () {
		return value ? 1231 : 1237;
	}

	public boolean equals (Object obj) {
		return obj instanceof Boolean && value == ((Boolean) obj).value;
	}

	public static boolean getBoolean (String name) {
		if (name == null || "".equals (name))
			return false;
		return "true".equalsIgnoreCase (System.getProperty (name));
	}

}
