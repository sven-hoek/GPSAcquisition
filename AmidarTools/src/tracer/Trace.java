package tracer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Serializable;
import java.util.IllegalFormatException;


/**
 * The IOFacility gives access to the input and output streams in a transparent
 * way, such that methods producing output or reading input must not know where
 * it comes from.
 * @author Michael Raitza
 * @version - 06.03.2011
 */
public class Trace implements Serializable{

	/**
	 * The character stream writer the output goes to.
	 */
	private PrintWriter out;
	private PrintWriter act_out;
	private String oname;

	/**
	 * The character stream reader the input comes from.
	 */
	private BufferedReader in;
	private String iname;

	/**
	 * This IOFacility is active or not
	 */
	private Boolean active = true;

	/**
	 * This IOFacility is special
	 */
	private Boolean special;

	private boolean prefixed = false;
	private String prefix = null;

	private static class NullOutputStream extends OutputStream {

		private NullOutputStream() { }

		private static class NOSHolder {
			public static final NullOutputStream INSTANCE = new NullOutputStream();
		}

		public static NullOutputStream getInstance() {
			return NOSHolder.INSTANCE;
		}

		public void close() {
		}

		public void flush() {
		}

		public void write(byte[] b) {
		}

		public void write(byte[] b, int off, int len) {
		}

		public void write(int b) {
		}
	}

	private static class NullInputStream extends InputStream {

		private static class NISHolder {
			public static final NullInputStream INSTANCE = new NullInputStream();
		}

		public static NullInputStream getInstance() {
			return NISHolder.INSTANCE;
		}

		public int read() {
			return 0;
		}

		public void close() {
		}

		public void reset() {
		}

		public void mark(int readlimit) {
		}

		public int available() {
			return 1;
		}
	}

	private String prefix() {
		if (!prefixed)
			return "";

		if (prefix == null) {

		    StackTraceElement str;
		    try {
		    	throw new RuntimeException("blafasel");
		    } catch (RuntimeException e) {
		    	str = e.getStackTrace()[2];
		    }
		    return "[ " + str.getMethodName() + ":" + str.getFileName() + ":" +  str.getLineNumber() + " ] ";
		}
		else 
		    return "[ " + prefix + " ] ";
	}

	public Trace() {
		out = new PrintWriter(NullOutputStream.getInstance());
		act_out = out;
		in = new BufferedReader(new InputStreamReader(NullInputStream.getInstance()));
		oname = "nullout";
		iname = "nullin";
		special = false;
		prefixed = false;
	}

	/**
	 * Constructs a new <code>IOFacility</code> taking a writer and a
	 * reader. The reader is converted into a buffered reader.
	 * @param wr a writer
	 * @param rd a reader
	 * @param wrname the name of the output stream
	 * @param rdname the name of the input stream
	 */
	public Trace(PrintWriter wr, Reader rd, String wrname, String rdname) {
		this(wr, rd, wrname, rdname, false);
	}

	/**
	 * Constructs a new <code>IOFacility</code> taking a writer and a
	 * reader. The reader is converted into a buffered reader.
	 * @param wr a writer
	 * @param rd a reader
	 * @param wrname the name of the output stream
	 * @param rdname the name of the input stream
	 * @param special this IOFacility is special
	 */
	public Trace(PrintWriter wr, Reader rd, String wrname, String rdname, Boolean special) {
		out = wr;
		act_out = out;
		oname = wrname;
		iname = rdname;
		try {
			in = (BufferedReader)rd;
		}
		catch (ClassCastException e) {
			in = new BufferedReader(rd);
		}
		this.special = special;
	}

	/**
	 * Constructs a new <code>IOFacility</code> taking an output and
	 * an input stream.
	 * @param wr an output stream
	 * @param rd an input stream
	 * @param wrname the name of the output stream
	 * @param rdname the name of the input stream
	 */
	public Trace(OutputStream wr, InputStream rd, String wrname, String rdname) {
		this(wr, rd, wrname, rdname, false);
	}

	/**
	 * Constructs a new <code>IOFacility</code> taking an output and
	 * an input stream.
	 * @param wr an output stream
	 * @param rd an input stream
	 * @param wrname the name of the output stream
	 * @param rdname the name of the input stream
	 * @param special this IOFacility is special
	 */
	public Trace(OutputStream wr, InputStream rd, String wrname, String rdname, Boolean special) {
		out = new PrintWriter(wr, true);
		act_out = out;;
		in = new BufferedReader(new InputStreamReader(rd));
		oname = wrname;
		iname = rdname;
		this.special = special;
	}

	/**
	 * Returns the standard reader.
	 * @return the standard reader
	 */
	public static BufferedReader stdin() {
		return new BufferedReader(new InputStreamReader(NullInputStream.getInstance()));
	}

	/**
	 * Returns the standard writer.
	 * @return the standard writer
	 */
	public static PrintWriter stdout() {
		return new PrintWriter(NullOutputStream.getInstance());
	}

	/**
	 * Prints a formatted string to the current output stream.
	 * @param arg a format string
	 * @param args the arguments to the format string
	 * @return the writer the output has gone to
	 * @throws IllegalFormatException if the formatstring is illegal {@link java.io.PrintWriter}
	 * @throws NullPointerException {@link java.io.PrintWriter}
	 */
	public PrintWriter printf(String arg, Object... args) throws IllegalFormatException, NullPointerException {
		return act_out.printf(prefix() + arg, args);
	}

	/**
	 * Convenience method; prints a line of text to the current output stream.
	 * @param arg an object
	 */
	public void println(Object arg) {
		try {
			act_out.println(prefix() + arg);
		}
		catch (Throwable e) { }
	}

        /**
         * Convenience method; prints a line of text to the current output stream.
         * @param prefix a prefix
         * @param arg a string
         */
        public void println(String prefix, String arg) {
                try {
                        act_out.println("[ " + prefix + " ] " + arg);
                }
                catch (Throwable e) { }
        }

	/**
	 * Convenience method; prints a line of text to the current output stream.
	 * @param arg a string
	 */
	public void println(String arg) {
		try {
			act_out.println(prefix() + arg);
		}
		catch (Throwable e) { }
	}

        /**
         * Convenience method; prints an empty line of text
         */
        public void println() {
                try {
                	act_out.println(prefix());
                }
                catch (Throwable e) { }
        }

	/**
	 * Prints its argument to the current output stream.
	 * @arg the argument to print
	 */
	public void print(String arg) {
		act_out.print(prefix() + arg);
	}

	/**
	 * Prints its argument to the current output stream.
	 * @arg the argument to print
	 */
	public void print(Object arg) {
		act_out.print(prefix() + arg);
	}

	/**
	 * Prints its argument to the current output stream.
	 * @arg the argument to print
	 */
	public void print(int arg) {
		act_out.print(prefix() + arg);
	}

	/**
	 * Prints its argument to the current output stream.
	 * @arg the argument to print
	 */
	public void print(float arg) {
		act_out.print(prefix() + arg);
	}

	/**
	 * Prints its argument to the current output stream.
	 * @arg the argument to print
	 */
	public void print(char arg) {
		act_out.print(prefix() + arg);
	}

	/**
	 * Prints its argument to the current output stream.
	 * @arg the argument to print
	 */
	public void print(char[] arg) {
		act_out.print(prefix() + String.valueOf(arg));
	}

	/**
	 * Prints its argument to the current output stream.
	 * @arg the argument to print
	 */
	public void print(long arg) {
		act_out.print(prefix() + arg);
	}

	/**
	 * Prints its argument to the current output stream.
	 * @arg the argument to print
	 */
	public void print(double arg) {
		act_out.print(prefix() + arg);
	}
	
	
	/**
	 * Flushes the output
	 */
	public void flush(){
		act_out.flush();
	}

	/**
	 * Reads a line of text from the current input stream.
	 * @return the line read.
	 * @throws IOException when deactivated or {@link java.io.BufferedReader}
	 */
	public String readline() throws IOException {
		if (!active)
			throw new IOException("Reading from inactive input stream");
		return in.readLine();
	}

	/**
	 * Prints a prompt and reads a line of text.
	 * @throws IOException when deactivated or {@link java.io.BufferedReader}
	 * @throws IllegalFormatException if the formatstring is illegal {@link java.io.PrintWriter}
	 * @throws NullPointerException {@link java.io.PrintWriter}
	 */
	public String readline(String fmt, Object... args) throws IOException, IllegalFormatException, NullPointerException {
		if (!active)
			throw new IOException("Reading from inactive input stream");
		printf(fmt, args);
		return in.readLine();
	}

	/**
	 * Returns the writer object associated with this IOFacility.
	 * @return the writer associated with this IOFacility
	 */
	public PrintWriter writer() {
		return out;
	}

	/**
	 * Returns the reader object associated with this IOFacility.
	 * @return the reader associated with this IOFacility
	 */
	public Reader reader() {
		return in;
	}

	/**
	 * Activates this IOFacility.
	 */
	public void activate() {
		active = true;
		act_out = out;
	}

	/**
	 * Deactivates this IOFacility. No input or output is possible until again activated.
	 */
	public void deactivate() {
		active = false;
		act_out = new PrintWriter(NullOutputStream.getInstance());
	}

	/**
	 * Returns the name of the associated output stream.
	 * @return the name of the associated output stream
	 */
	public String oname() {
		return oname;
	}

	/**
	 * Returns the name of the associated input stream.
	 * @return the name of the associated input stream
	 */
	public String iname() {
		return iname;
	}

	/**
	 * Returns the activation state of this IOFacility.
	 * @return TRUE iff this IOFacility is active, FALSE otherwise.
	 */
	public Boolean active() {
		return active;
	}

	/**
	 * Returns TRUE if this IOFacility has the special flag set, FALSE otherwise.
	 * The special flag is used by the IOManager to ignore activation de-activation
	 * requests.
	 * @return TRUE if special flag is set, FALSE otherwise.
	 */
	public Boolean special() {
		return special;
	}

	/**
	 * Returns TRUE iff this IOFacility will emit a prefix to all its output.
	 */
	public Boolean prefixed() {
	    return prefixed;
	}

	/**
	 * Selects to prefix all output of this IOFacility with the position in source code the
	 * output is emitted from.
	 * @param pr if TRUE activate prefix, deactivate otherwise
	 */
	public void prefixed(boolean pr) {
	    prefixed = pr;
	}
	
	public void setPrefix(String prefix) {
	    this.prefix = prefix;
	    if (prefix != null) prefixed = true;
	}
	
	
	private static final int STATS_TABLE_WIDTH = 96;

	private String repeatString(String str, int times) {
		return String.format(String.format("%%0%dd", times), 0).replace("0",
				str);
	}

	public void printTableHeader(String title) {
		println();
		int pad = STATS_TABLE_WIDTH - 2 - title.length();
		if (pad <= 1)
			pad = 2;
		println(repeatString("-", pad / 2) + " " + title + " " + repeatString("-", (pad + 1) / 2));
	}

}
