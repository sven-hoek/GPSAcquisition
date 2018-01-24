package de.amidar;

public class MutableBoolean {
	private boolean value;

	public MutableBoolean () {
	}

	public MutableBoolean (boolean value) {
		this.value = value;
	}

	public MutableBoolean (Boolean value) {
		this.value = value.booleanValue ();
	}

	public void setValue (boolean value) {
		this.value = value;
	}

	public boolean booleanValue () {
		return value;
	}

	public Boolean toBoolean () {
		return new Boolean (value);
	}

	public boolean equals (Object obj) {
		if (!(obj instanceof MutableBoolean)) {
			return false;
		}
		return value == ((MutableBoolean) obj).booleanValue ();
	}

	public int hashCode () {
		if (value)
			return Boolean.TRUE.hashCode ();
		else
			return Boolean.FALSE.hashCode ();
	}

	public String toString () {
		return String.valueOf (value);
	}
}