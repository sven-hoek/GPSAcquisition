package java.lang;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;

public class Throwable implements Serializable {
	private static final long serialVersionUID = -3042686055658047285L;

	private final String detailMessage;

	private Throwable cause = this;

	private StackTraceElement[] stackTrace;

	public Throwable () {
		this ((String) null);
	}

	public Throwable (String message) {
		fillInStackTrace ();
		detailMessage = message;
	}

	public Throwable (String message, Throwable cause) {
		this (message);
		this.cause = cause;
	}

	public Throwable (Throwable cause) {
		this (cause == null ? null : cause.toString (), cause);
	}

	public String getMessage () {
		return detailMessage;
	}

	public String getLocalizedMessage () {
		return getMessage ();
	}

	public Throwable getCause () {
		return cause == this ? null : cause;
	}

	public Throwable initCause (Throwable cause) {
		if (cause == this)
			throw new IllegalArgumentException ();
		if (this.cause != this)
			throw new IllegalStateException ();
		this.cause = cause;
		return this;
	}

	public String toString () {
		String msg = getLocalizedMessage ();
		return getClass ().getName () + (msg == null ? "" : ": " + msg);
	}

	public void printStackTrace () {
		printStackTrace (System.err);
	}

	public void printStackTrace (PrintStream s) {
		s.print (stackTraceString ());
	}

	public void printStackTrace (PrintWriter pw) {
		pw.print (stackTraceString ());
	}

	private String stackTraceString () {
		StringBuffer sb = new StringBuffer ();

		// Main stacktrace
		StackTraceElement[] stack = getStackTrace ();
		stackTraceStringBuffer (sb, this.toString (), stack, 0);

		// The cause(s)
		Throwable cause = getCause ();
		while (cause != null) {
			// Cause start first line
			sb.append ("Caused by: ");

			// Cause stacktrace
			StackTraceElement[] parentStack = stack;
			stack = cause.getStackTrace ();
			if (parentStack == null || parentStack.length == 0)
				stackTraceStringBuffer (sb, cause.toString (), stack, 0);
			else {
				int equal = 0; // Count how many of the last stack frames are
								// equal
				int frame = stack.length - 1;
				int parentFrame = parentStack.length - 1;
				while (frame > 0 && parentFrame > 0) {
					if (stack[frame].equals (parentStack[parentFrame])) {
						equal++;
						frame--;
						parentFrame--;
					} else
						break;
				}
				stackTraceStringBuffer (sb, cause.toString (), stack, equal);
			}
			cause = cause.getCause ();
		}

		return sb.toString ();
	}

	private static void stackTraceStringBuffer (StringBuffer sb, String name,
			StackTraceElement[] stack, int equal) {
		String nl = "\n";
		// (finish) first line
		sb.append (name);
		sb.append (nl);

		// The stacktrace
		if (stack == null || stack.length == 0) {
			sb.append ("   <<No stacktrace available>>");
			sb.append (nl);
		} else {
			for (int i = 0; i < stack.length - equal; i++) {
				sb.append ("   at ");
				sb.append (stack[i] == null ? "<<Unknown>>" : stack[i]
						.toString ());
				sb.append (nl);
			}
			if (equal > 0) {
				sb.append ("   ...");
				sb.append (equal);
				sb.append (" more");
				sb.append (nl);
			}
		}
	}

	public Throwable fillInStackTrace () {
		// TODO: Fill stack trace

		return this;
	}

	public StackTraceElement[] getStackTrace () {
		if (stackTrace == null)
			stackTrace = new StackTraceElement[0];

		return stackTrace;
	}

	public void setStackTrace (StackTraceElement[] stackTrace) {
		int i = stackTrace.length;
		StackTraceElement[] st = new StackTraceElement[i];

		while (--i >= 0) {
			st[i] = stackTrace[i];
			if (st[i] == null)
				throw new NullPointerException ("Element " + i + " null");
		}

		this.stackTrace = st;
	}

}
