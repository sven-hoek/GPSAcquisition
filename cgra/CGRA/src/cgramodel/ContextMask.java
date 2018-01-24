package cgramodel;

import java.io.Serializable;

/**
 * Root class for all context mask. Holds global method to write and read
 * contexts. the actual bit masks are to be created for the individual context.
 *
 * @author wolf
 *
 */
public class ContextMask implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 4283121957035593198L;

	protected int contextwidth;

	String name = "defaultContextMask";

	public ContextMask(){
	}

	public int getContextWidth() {
		return contextwidth;
	}

	public void setContextWidth(int width) {
		contextwidth = width;
	}
	
	/**
	 * Writes a given value to to context, depending on the lower index and
	 * width. Index and width come from the indivudal masks.
	 */
	public long writeBitSet(long context, int value, int indexLow, int width) {
		long low = context & ~(Long.MAX_VALUE << indexLow);
		long val = (((long)(value)) & ~(Long.MAX_VALUE << width)) << indexLow;
		long up = context & (Long.MAX_VALUE << (indexLow + width));
		
		
		return context = low | val | up;
	}

	/**
	 * Reads a given section - indicated by width and lower index - of a context
	 * and returns it as an integer.
	 */
	public int read(long context, int lsb, int width) {
		return (int) (~(Long.MAX_VALUE << (width)) & (context >> lsb));
	}

	/**
	 * Converts and given context and returns it as a String.
	 */
	public String getBitString(long context) {
		String val = Long.toBinaryString(context);
		int l = val.length();

		int diff = contextwidth - l;

		if (diff < 0) {
			System.err.println("getBitString Error in " + name);
			System.err.println(val);
			System.err.println(getContextWidth());
		} else {

			char[] padding = new char[diff];

			for (int i = 0; i < diff; i++) {
				padding[i] = '0';
			}

			val = new String(padding) + val;
		}

		return val;
	}

}
