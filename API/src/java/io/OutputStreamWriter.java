package java.io;

/**
 * Minified version of an OutputStreamWriter, printing chars directly as byte
 * values (i.e. basically only for ASCII encoding).
 */
public class OutputStreamWriter extends Writer {

	/**
	 * This is the byte-character encoder class that does the writing and
	 * translation of characters to bytes before writing to the underlying
	 * class.
	 */
	private OutputStream out;

	/**
	 * This method initializes a new instance of <code>OutputStreamWriter</code>
	 * to write to the specified stream using a caller supplied character
	 * encoding scheme. Note that due to a deficiency in the Java language
	 * design, there is no way to determine which encodings are supported.
	 *
	 * @param out
	 *            The <code>OutputStream</code> to write to
	 * @param encoding_scheme
	 *            The name of the encoding scheme to use for character to byte
	 *            translation <b> - unused in this implementation!</b>
	 */
	public OutputStreamWriter (OutputStream out, String encoding_scheme) {
		this.out = out;
	}

	/**
	 * This method initializes a new instance of <code>OutputStreamWriter</code>
	 * to write to the specified stream using the default encoding.
	 *
	 * @param out
	 *            The <code>OutputStream</code> to write to
	 */
	public OutputStreamWriter (OutputStream out) {
		this.out = out;
	}

	/**
	 * This method closes this stream, and the underlying
	 * <code>OutputStream</code>
	 *
	 * @exception IOException
	 *                If an error occurs
	 */
	public void close () throws IOException {
		out.close ();
	}

	/**
	 * This method returns the name of the character encoding scheme currently
	 * in use by this stream. If the stream has been closed, then this method
	 * may return <code>null</code>.
	 *
	 * @return The encoding scheme name <b> - unused in this implementation!</b>
	 */
	public String getEncoding () {
		return null;
	}

	/**
	 * This method flushes any buffered bytes to the underlying output sink.
	 *
	 * @exception IOException
	 *                If an error occurs
	 */
	public void flush () throws IOException {
		out.flush ();
	}

	/**
	 * This method writes <code>count</code> characters from the specified array
	 * to the output stream starting at position <code>offset</code> into the
	 * array.
	 *
	 * @param buf
	 *            The array of character to write from
	 * @param offset
	 *            The offset into the array to start writing chars from
	 * @param count
	 *            The number of chars to write.
	 *
	 * @exception IOException
	 *                If an error occurs
	 */
	public void write (char[] buf, int offset, int count) throws IOException {
	    if (offset < 0 || count < 0 || offset + count > buf.length)
	        throw new ArrayIndexOutOfBoundsException ();
	      for (int i = 0; i < count; ++i)
	        write (buf[offset + i]);
	}

	/**
	 * This method writes <code>count</code> bytes from the specified
	 * <code>String</code> starting at position <code>offset</code> into the
	 * <code>String</code>.
	 *
	 * @param str
	 *            The <code>String</code> to write chars from
	 * @param offset
	 *            The position in the <code>String</code> to start writing chars
	 *            from
	 * @param count
	 *            The number of chars to write
	 *
	 * @exception IOException
	 *                If an error occurs
	 */
	public void write (String str, int offset, int count) throws IOException {
		out.write (str.getBytes (), offset, count);
	}

	/**
	 * This method writes a single character to the output stream.
	 *
	 * @param c
	 *            The char to write, passed as an int.
	 *
	 * @exception IOException
	 *                If an error occurs
	 */
	public void write (int ch) throws IOException {
		out.write (ch);
	}

} // class OutputStreamWriter

