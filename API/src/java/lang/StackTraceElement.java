package java.lang;

import java.io.Serializable;

public final class StackTraceElement implements Serializable {
	private static final long serialVersionUID = 6992337162326171013L;

	private final String fileName;

	private final int lineNumber;

	private final String declaringClass;

	private final String methodName;

	private final transient boolean isNative;

	StackTraceElement (String fileName, int lineNumber, String className,
			String methodName, boolean isNative) {
		this.fileName = fileName;
		this.lineNumber = lineNumber;
		this.declaringClass = className;
		this.methodName = methodName;
		this.isNative = isNative;
	}

	public String getFileName () {
		return fileName;
	}

	public int getLineNumber () {
		return lineNumber;
	}

	public String getClassName () {
		return declaringClass;
	}

	public String getMethodName () {
		return methodName;
	}

	public boolean isNativeMethod () {
		return isNative;
	}

	public String toString () {
		StringBuffer sb = new StringBuffer ();
		if (declaringClass != null) {
			sb.append (declaringClass);
			if (methodName != null)
				sb.append ('.');
		}
		if (methodName != null)
			sb.append (methodName);
		sb.append (" (");
		if (fileName != null)
			sb.append (fileName);
		else
			sb.append (isNative ? "Native Method" : "Unknown Source");
		if (lineNumber >= 0)
			sb.append (':').append (lineNumber);
		sb.append (')');
		return sb.toString ();
	}

	public boolean equals (Object o) {
		if (!(o instanceof StackTraceElement))
			return false;
		StackTraceElement e = (StackTraceElement) o;
		return equals (fileName, e.fileName) && lineNumber == e.lineNumber
				&& equals (declaringClass, e.declaringClass)
				&& equals (methodName, e.methodName);
	}

	public int hashCode () {
		return hashCode (fileName) ^ lineNumber ^ hashCode (declaringClass)
				^ hashCode (methodName);
	}

	private static boolean equals (Object o1, Object o2) {
		return o1 == null ? o2 == null : o1.equals (o2);
	}

	private static int hashCode (Object o) {
		return o == null ? 0 : o.hashCode ();
	}
}
