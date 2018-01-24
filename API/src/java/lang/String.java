package java.lang;

import gnu.java.lang.CharData;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Comparator;
import java.util.Locale;
import java.util.regex.Pattern;

import de.amidar.ArrayCopy;

public final class String implements Serializable, Comparable, CharSequence {
	private static final long serialVersionUID = -6849794470754667710L;

	private static final char[] upperExpand = zeroBasedStringValue (CharData.UPPER_EXPAND);

	private static final char[] upperSpecial = zeroBasedStringValue (CharData.UPPER_SPECIAL);

	final char[] value;

	private int cachedHashCode;

	final int count;

	final int offset;

	private static final class CaseInsensitiveComparator implements Comparator,
			Serializable {
		private static final long serialVersionUID = 8575799808933029326L;

		CaseInsensitiveComparator () {
		}

		public int compare (Object o1, Object o2) {
			return ((String) o1).compareToIgnoreCase ((String) o2);
		}
	}

	public static final Comparator CASE_INSENSITIVE_ORDER = new CaseInsensitiveComparator ();

	public String () {
		value = "".value;
		offset = 0;
		count = 0;
	}

	public String (String str) {
		value = str.value;
		offset = str.offset;
		count = str.count;
		cachedHashCode = str.cachedHashCode;
	}

	public String (char[] data) {
		this (data, 0, data.length, false);
	}

	public String (char[] data, int offset, int count) {
		this (data, offset, count, false);
	}

	/**
	 * @deprecated use {@link #String(byte[], int, int, String)} to perform
	 *             correct encoding
	 */
	public String (byte[] ascii, int hibyte, int offset, int count) {
		if (offset < 0)
			throw new StringIndexOutOfBoundsException ("offset: " + offset);
		if (count < 0)
			throw new StringIndexOutOfBoundsException ("count: " + count);
		// equivalent to: offset + count < 0 || offset + count > ascii.length
		if (ascii.length - offset < count)
			throw new StringIndexOutOfBoundsException ("offset + count: "
					+ (offset + count));
		value = new char[count];
		this.offset = 0;
		this.count = count;
		hibyte <<= 8;
		offset += count;
		while (--count >= 0)
			value[count] = (char) (hibyte | (ascii[--offset] & 0xff));
	}

	/**
	 * @deprecated use {@link #String(byte[], String)} to perform correct
	 *             encoding
	 */
	public String (byte[] ascii, int hibyte) {
		this (ascii, hibyte, 0, ascii.length);
	}

	public String (byte[] data, int offset, int count, String encoding)
			throws UnsupportedEncodingException {
		throw new UnsupportedOperationException ();
	}

	public String (byte[] data, String encoding)
			throws UnsupportedEncodingException {
		this (data, 0, data.length, encoding);
	}

	// Hacked in might be wrong and stuff
	public String (byte[] data, int offset, int count) {
		if (offset < 0)
			throw new StringIndexOutOfBoundsException ("offset: " + offset);
		if (count < 0)
			throw new StringIndexOutOfBoundsException ("count: " + count);
		// equivalent to: offset + count < 0 || offset + count > data.length
		if (data.length - offset < count)
			throw new StringIndexOutOfBoundsException ("offset + count: "
					+ (offset + count));

		value = new char[count];
		int a = offset;
		for(int i = 0; i < count; i++) {
			value[i] = (char)(data[a]);
			a++;
		}
		this.offset = 0;
		this.count = count;		
	}

	public String (byte[] data) {
		this (data, 0, data.length);
	}

	public String (StringBuffer buffer) {
		synchronized (buffer) {
			offset = 0;
			count = buffer.count;
			if ((count << 2) < buffer. value.length) {
				value = new char[count];
				System.arraycopy (buffer.value, 0, value, 0, count);
			} else {
				buffer.shared = true;
				value = buffer.value;
			}
		}
	}

	String (char[] data, int offset, int count, boolean dont_copy) {
		if (offset < 0){
			System.out.println("OOO "+ offset);
			throw new StringIndexOutOfBoundsException ("offset: " + offset);
		}
		
		if (count < 0){
			System.out.println("CCC "+ count);
			throw new StringIndexOutOfBoundsException ("count: " + count);
		}
		// equivalent to: offset + count < 0 || offset + count > data.length
		if (data.length - offset < count){
			System.out.println("OC "+ offset +" "+ count + " " + data.length);
			System.out.println(data);
			throw new StringIndexOutOfBoundsException ("offset + count: "
					+ (offset + count));
		}
		if (dont_copy) {
			value = data;
			this.offset = offset;
		} else {
			value = new char[count];
			System.arraycopy (data, offset, value, 0, count);
			this.offset = 0;
		}
		this.count = count;
	}

	public String (int[] codePoints, int offset, int count) {
		throw new UnsupportedOperationException ();
	}

	public int length () {
		return count;
	}

	public char charAt (int index) {
		if (index < 0 || index >= count)
			throw new StringIndexOutOfBoundsException (index);
		return value[offset + index];
	}

	public void getChars (int srcBegin, int srcEnd, char dst[], int dstBegin) {
		if (srcBegin < 0 || srcBegin > srcEnd || srcEnd > count)
			throw new StringIndexOutOfBoundsException ();
		System.arraycopy (value, srcBegin + offset, dst, dstBegin, srcEnd
				- srcBegin);
	}

	/**
	 * @deprecated use {@link #getBytes()}, which uses a char to byte encoder
	 */
	public void getBytes (int srcBegin, int srcEnd, byte dst[], int dstBegin) {
		if (srcBegin < 0 || srcBegin > srcEnd || srcEnd > count)
			throw new StringIndexOutOfBoundsException ();
		int i = srcEnd - srcBegin;
		srcBegin += offset;
		while (--i >= 0)
			dst[dstBegin++] = (byte) value[srcBegin++];
	}

	public byte[] getBytes (String enc) throws UnsupportedEncodingException {
		throw new UnsupportedOperationException ();
	}

	/**
	 * ASCII only
	 */
	public byte[] getBytes () {
		byte[] result = new byte[count];
		for (int i = 0; i < count; i++) {
			result [i] = (byte) value [offset + i];
		}
		return result;
	}

	public boolean equals (Object anObject) {
		if (anObject == this)
			return true;
		if (!(anObject instanceof String))
			return false;
		String str2 = (String) anObject;
		if (count != str2.count)
			return false;
		if (value == str2.value && offset == str2.offset)
			return true;
		int i = count;
		int x = offset;
		int y = str2.offset;
		while (--i >= 0)
			if (value[x++] != str2.value[y++])
				return false;
		return true;
	}

	public boolean contentEquals (StringBuffer buffer) {
		synchronized (buffer) {
			if (count != buffer.count)
				return false;
			if (value == buffer.value)
				return true;
			int i = count;
			int x = offset + count;
			while (--i >= 0)
				if (value[--x] != buffer.value[i])
					return false;
			return true;
		}
	}

	public boolean equalsIgnoreCase (String anotherString) {
		if (anotherString == null || count != anotherString.count)
			return false;
		int i = count;
		int x = offset;
		int y = anotherString.offset;
		while (--i >= 0) {
			char c1 = value[x++];
			char c2 = anotherString.value[y++];
			if (c1 != c2
					&& Character.toUpperCase (c1) != Character.toUpperCase (c2)
					&& Character.toLowerCase (c1) != Character.toLowerCase (c2))
				return false;
		}
		return true;
	}

	public int compareTo (String anotherString) {
		int i = Math.min (count, anotherString.count);
		int x = offset;
		int y = anotherString.offset;
		while (--i >= 0) {
			int result = value[x++] - anotherString.value[y++];
			if (result != 0)
				return result;
		}
		return count - anotherString.count;
	}

	public int compareTo (Object o) {
		return compareTo ((String) o);
	}

	public int compareToIgnoreCase (String str) {
		int i = Math.min (count, str.count);
		int x = offset;
		int y = str.offset;
		while (--i >= 0) {
			int result = Character.toLowerCase (Character
					.toUpperCase (value[x++]))
					- Character.toLowerCase (Character
							.toUpperCase (str.value[y++]));
			if (result != 0)
				return result;
		}
		return count - str.count;
	}

	public boolean regionMatches (int toffset, String other, int ooffset,
			int len) {
		return regionMatches (false, toffset, other, ooffset, len);
	}

	public boolean regionMatches (boolean ignoreCase, int toffset,
			String other, int ooffset, int len) {
		if (toffset < 0 || ooffset < 0 || toffset + len > count
				|| ooffset + len > other.count)
			return false;
		toffset += offset;
		ooffset += other.offset;
		while (--len >= 0) {
			char c1 = value[toffset++];
			char c2 = other.value[ooffset++];
			// Note that checking c1 != c2 is redundant when ignoreCase is true,
			// but it avoids method calls.
			if (c1 != c2
					&& (!ignoreCase || (Character.toLowerCase (c1) != Character
							.toLowerCase (c2) && (Character.toUpperCase (c1) != Character
							.toUpperCase (c2)))))
				return false;
		}
		return true;
	}

	public boolean startsWith (String prefix, int toffset) {
		return regionMatches (false, toffset, prefix, 0, prefix.count);
	}

	public boolean startsWith (String prefix) {
		return regionMatches (false, 0, prefix, 0, prefix.count);
	}

	public boolean endsWith (String suffix) {
		return regionMatches (false, count - suffix.count, suffix, 0,
				suffix.count);
	}

	public int hashCode () {
		if (cachedHashCode != 0)
			return cachedHashCode;

		int hashCode = 0;
		int limit = count + offset;
		for (int i = offset; i < limit; i++)
			hashCode = hashCode * 31 + value[i];
		return cachedHashCode = hashCode;
	}

	public int indexOf (int ch) {
		return indexOf (ch, 0);
	}

	public int indexOf (int ch, int fromIndex) {
		if ((char) ch != ch)
			return -1;
		if (fromIndex < 0)
			fromIndex = 0;
		int i = fromIndex + offset;
		for (; fromIndex < count; fromIndex++)
			if (value[i++] == ch)
				return fromIndex;
		return -1;
	}

	public int lastIndexOf (int ch) {
		return lastIndexOf (ch, count - 1);
	}

	public int lastIndexOf (int ch, int fromIndex) {
		if ((char) ch != ch)
			return -1;
		if (fromIndex >= count)
			fromIndex = count - 1;
		int i = fromIndex + offset;
		for (; fromIndex >= 0; fromIndex--)
			if (value[i--] == ch)
				return fromIndex;
		return -1;
	}

	public int indexOf (String str) {
		return indexOf (str, 0);
	}

	public int indexOf (String str, int fromIndex) {
		if (fromIndex < 0)
			fromIndex = 0;
		int limit = count - str.count;
		for (; fromIndex <= limit; fromIndex++)
			if (regionMatches (fromIndex, str, 0, str.count))
				return fromIndex;
		return -1;
	}

	public int lastIndexOf (String str) {
		return lastIndexOf (str, count - str.count);
	}

	public int lastIndexOf (String str, int fromIndex) {
		fromIndex = Math.min (fromIndex, count - str.count);
		for (; fromIndex >= 0; fromIndex--)
			if (regionMatches (fromIndex, str, 0, str.count))
				return fromIndex;
		return -1;
	}

	public String substring (int begin) {
		return substring (begin, count);
	}

	public String substring (int beginIndex, int endIndex) {
		if (beginIndex < 0 || endIndex > count || beginIndex > endIndex)
			throw new StringIndexOutOfBoundsException ();
		if (beginIndex == 0 && endIndex == count)
			return this;
		int len = endIndex - beginIndex;
		return new String (value, beginIndex + offset, len,
				(len << 2) >= value.length);
	}

	public CharSequence subSequence (int begin, int end) {
		return substring (begin, end);
	}

	public String concat (String str) {
		if (str.count == 0)
			return this;
		if (count == 0)
			return str;
		char[] newStr = new char[count + str.count];
		System.arraycopy (value, offset, newStr, 0, count);
		System.arraycopy (str.value, str.offset, newStr, count, str.count);
		// Package constructor avoids an array copy.
		return new String (newStr, 0, newStr.length, true);
	}

	public String replace (char oldChar, char newChar) {
		if (oldChar == newChar)
			return this;
		int i = count;
		int x = offset - 1;
		while (--i >= 0)
			if (value[++x] == oldChar)
				break;
		if (i < 0)
			return this;
		
		char[] newStr = new char[count];
		System.arraycopy(value, offset, newStr, 0, count);
		newStr[x-offset] = newChar;
		while (--i >= 0){
			if (value[++x] == oldChar){
				newStr[x-offset] = newChar;
			}
		}
		// Package constructor avoids an array copy.
		return new String (newStr, 0, count, true);
	}

	public boolean matches (String regex) {
		return Pattern.matches (regex, this);
	}

	public String replaceFirst (String regex, String replacement) {
		return Pattern.compile (regex).matcher (this)
				.replaceFirst (replacement);
	}

	public String replaceAll (String regex, String replacement) {
		return Pattern.compile (regex).matcher (this).replaceAll (replacement);
	}

	public String[] split (String regex, int limit) {
		return Pattern.compile (regex).split (this, limit);
	}

	public String[] split (String regex) {
		return Pattern.compile (regex).split (this, 0);
	}
	
	public String toLowerCase (Locale loc) {
		int i = count;
		int x = offset - 1;
		while (--i >= 0) {
			char ch = value[++x];
			if (ch != Character.toLowerCase (ch))
				break;
		}
		if (i < 0)
			return this;

		char[] newStr = new char[count];
		System.arraycopy (value, offset, newStr, 0, count);
		do {
			char ch = value[x];
			newStr[x++] = Character.toLowerCase (ch);
		} while (--i >= 0);
		return new String (newStr, offset, count, true);
	}

	public String toLowerCase () {
		return toLowerCase (Locale.getDefault ());
	}

	public String toUpperCase (Locale loc) {
		int expand = 0;
		boolean unchanged = true;
		int i = count;
		int x = i + offset;
		while (--i >= 0) {
			char ch = value[--x];
			expand += upperCaseExpansion (ch);
			unchanged = (unchanged && expand == 0 && ch == Character
					.toUpperCase (ch));
		}
		if (unchanged)
			return this;

		i = count;
		if (expand == 0) {
			char[] newStr = new char[count];
			System.arraycopy (value, offset, newStr, 0, count);
			while (--i >= 0) {
				char ch = value[x];
				newStr[x++] = Character.toUpperCase (ch);
			}
			return new String (newStr, offset, count, true);
		}

		char[] newStr = new char[count + expand];
		int j = 0;
		while (--i >= 0) {
			char ch = value[x++];
			expand = upperCaseExpansion (ch);
			if (expand > 0) {
				int index = upperCaseIndex (ch);
				while (expand-- >= 0)
					newStr[j++] = upperExpand[index++];
			} else
				newStr[j++] = Character.toUpperCase (ch);
		}
		return new String (newStr, 0, newStr.length, true);
	}

	public String toUpperCase () {
		return toUpperCase (Locale.getDefault ());
	}

	public String trim () {
		int limit = count + offset;
		if (count == 0
				|| (value[offset] > '\u0020' && value[limit - 1] > '\u0020'))
			return this;
		int begin = offset;
		do
			if (begin == limit)
				return "";
		while (value[begin++] <= '\u0020');
		int end = limit;
		while (value[--end] <= '\u0020')
			;
		return substring (begin - offset - 1, end - offset + 1);
	}

	public String toString () {
		return this;
	}

	public char[] toCharArray () {
		char[] copy = new char[count];
		System.arraycopy (value, offset, copy, 0, count);
		return copy;
	}

	public static String valueOf (Object obj) {
		return obj == null ? "null" : obj.toString ();
	}

	public static String valueOf (char[] data) {
		return valueOf (data, 0, data.length);
	}

	public static String valueOf (char[] data, int offset, int count) {
		return new String (data, offset, count, false);
	}

	public static String copyValueOf (char[] data, int offset, int count) {
		return new String (data, offset, count, false);
	}

	public static String copyValueOf (char[] data) {
		return copyValueOf (data, 0, data.length);
	}

	public static String valueOf (boolean b) {
		return b ? "true" : "false";
	}

	public static String valueOf (char c) {
		return new String (new char[] { c }, 0, 1, true);
	}

	public static String valueOf (int i) {
		return Integer.toString (i, 10);
	}

	public static String valueOf (long l) {
		return Long.toString (l);
	}

	public static String valueOf (float f) {
		return Float.toString (f);
	}

	public static String valueOf (double d) {
		return Double.toString (d);
	}

	public String intern () {
		// TODO: intern
		return this;
	}

	private static int upperCaseExpansion (char ch) {
		return Character.direction[Character.readChar (ch) >> 7] & 3;
	}

	private static int upperCaseIndex (char ch) {
		int low = 0;
		int hi = upperSpecial.length - 2;
		int mid = ((low + hi) >> 2) << 1;
		char c = upperSpecial[mid];
		while (ch != c) {
			if (ch < c)
				hi = mid - 2;
			else
				low = mid + 2;
			mid = ((low + hi) >> 2) << 1;
			c = upperSpecial[mid];
		}
		return upperSpecial[mid + 1];
	}

	static char[] zeroBasedStringValue (String s) {
		char[] value;

		if (s.offset == 0 && s.count == s.value.length)
			value = s.value;
		else {
			int count = s.count;
			value = new char[count];
			System.arraycopy (s.value, s.offset, value, 0, count);
		}

		return value;
	}

}