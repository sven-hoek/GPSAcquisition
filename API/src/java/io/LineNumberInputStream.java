package java.io;

public class LineNumberInputStream extends FilterInputStream {
	
	 int pushBack = -1;
	    int lineNumber;
	    int markLineNumber;
	    int markPushBack = -1;

	protected LineNumberInputStream(InputStream in) {
		super(in);
	}
	
	
	 public int read() throws IOException {
	        int c = pushBack;

	        if (c != -1) {
	            pushBack = -1;
	        } else {
	            c = in.read();
	        }

	        switch (c) {
	          case '\r':
	            pushBack = in.read();
	            if (pushBack == '\n') {
	                pushBack = -1;
	            }
	          case '\n':
	            lineNumber++;
	            return '\n';
	        }
	        return c;
	    }
	 
	  public int read(byte b[], int off, int len) throws IOException {
	        if (b == null) {
	            throw new NullPointerException();
	        } else if ((off < 0) || (off > b.length) || (len < 0) ||
	                   ((off + len) > b.length) || ((off + len) < 0)) {
	            throw new IndexOutOfBoundsException();
	        } else if (len == 0) {
	            return 0;
	        }

	        int c = read();
	        if (c == -1) {
	            return -1;
	        }
	        b[off] = (byte)c;

	        int i = 1;
	        try {
	            for (; i < len ; i++) {
	                c = read();
	                if (c == -1) {
	                    break;
	                }
	                if (b != null) {
	                    b[off + i] = (byte)c;
	                }
	            }
	        } catch (IOException ee) {
	        }
	        return i;
	    }
	  
	  public long skip(long n) throws IOException {
	        int chunk = 2048;
	        long remaining = n;
	        byte data[];
	        int nr;

	        if (n <= 0) {
	            return 0;
	        }

	        data = new byte[chunk];
	        while (remaining > 0) {
	            nr = read(data, 0, (int) Math.min(chunk, remaining));
	            if (nr < 0) {
	                break;
	            }
	            remaining -= nr;
	        }

	        return n - remaining;
	    }

	    /**
	     * Sets the line number to the specified argument.
	     *
	     * @param      lineNumber   the new line number.
	     * @see #getLineNumber
	     */
	    public void setLineNumber(int lineNumber) {
	        this.lineNumber = lineNumber;
	    }

	    /**
	     * Returns the current line number.
	     *
	     * @return     the current line number.
	     * @see #setLineNumber
	     */
	    public int getLineNumber() {
	        return lineNumber;
	    }


	    /**
	     * Returns the number of bytes that can be read from this input
	     * stream without blocking.
	     * <p>
	     * Note that if the underlying input stream is able to supply
	     * <i>k</i> input characters without blocking, the
	     * {@code LineNumberInputStream} can guarantee only to provide
	     * <i>k</i>/2 characters without blocking, because the
	     * <i>k</i> characters from the underlying input stream might
	     * consist of <i>k</i>/2 pairs of {@code '\u005Cr'} and
	     * {@code '\u005Cn'}, which are converted to just
	     * <i>k</i>/2 {@code '\u005Cn'} characters.
	     *
	     * @return     the number of bytes that can be read from this input stream
	     *             without blocking.
	     * @exception  IOException  if an I/O error occurs.
	     * @see        java.io.FilterInputStream#in
	     */
	    public int available() throws IOException {
	        return (pushBack == -1) ? super.available()/2 : super.available()/2 + 1;
	    }

	    /**
	     * Marks the current position in this input stream. A subsequent
	     * call to the {@code reset} method repositions this stream at
	     * the last marked position so that subsequent reads re-read the same bytes.
	     * <p>
	     * The {@code mark} method of
	     * {@code LineNumberInputStream} remembers the current line
	     * number in a private variable, and then calls the {@code mark}
	     * method of the underlying input stream.
	     *
	     * @param   readlimit   the maximum limit of bytes that can be read before
	     *                      the mark position becomes invalid.
	     * @see     java.io.FilterInputStream#in
	     * @see     java.io.LineNumberInputStream#reset()
	     */
	    public void mark(int readlimit) {
	        markLineNumber = lineNumber;
	        markPushBack   = pushBack;
	        in.mark(readlimit);
	    }

	    /**
	     * Repositions this stream to the position at the time the
	     * {@code mark} method was last called on this input stream.
	     * <p>
	     * The {@code reset} method of
	     * {@code LineNumberInputStream} resets the line number to be
	     * the line number at the time the {@code mark} method was
	     * called, and then calls the {@code reset} method of the
	     * underlying input stream.
	     * <p>
	     * Stream marks are intended to be used in
	     * situations where you need to read ahead a little to see what's in
	     * the stream. Often this is most easily done by invoking some
	     * general parser. If the stream is of the type handled by the
	     * parser, it just chugs along happily. If the stream is not of
	     * that type, the parser should toss an exception when it fails,
	     * which, if it happens within readlimit bytes, allows the outer
	     * code to reset the stream and try another parser.
	     *
	     * @exception  IOException  if an I/O error occurs.
	     * @see        java.io.FilterInputStream#in
	     * @see        java.io.LineNumberInputStream#mark(int)
	     */
	    public void reset() throws IOException {
	        lineNumber = markLineNumber;
	        pushBack   = markPushBack;
	        in.reset();
	    }
	
}
