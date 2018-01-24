package java.lang;

public class StringIndexOutOfBoundsException extends IndexOutOfBoundsException {
	private static final long serialVersionUID = -6762910422159637258L;

	public StringIndexOutOfBoundsException () {
	}

	public StringIndexOutOfBoundsException (String s) {
		super (s);
	}

	public StringIndexOutOfBoundsException (int index) {
		super ("String index out of range: " + index);
	}
}
