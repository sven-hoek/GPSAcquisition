package java.lang;

import de.amidar.ArrayCopy;

import java.io.Serializable;

public final class StringBuffer implements Serializable, CharSequence {
	private static final long serialVersionUID = 3388685877147921107L;

	int count;

	char[] value;

	/**
	 * True if the buffer is shared with another object (StringBuffer or
	 * String); this means the buffer must be copied before writing to it again.
	 * Note that this has permissions set this way so that String can get the
	 * value.
	 */
	boolean shared;

	private static final int DEFAULT_CAPACITY = 16;

	public StringBuffer () {
		this (DEFAULT_CAPACITY);
	}

	public StringBuffer (int capacity) {
		value = new char[capacity];
	}

	public StringBuffer (String str) {
		count = str.count;
		value = new char[count + DEFAULT_CAPACITY];
		str.getChars (0, count, value, 0);
	}

	public int length () {
		return count;
	}

	public int capacity () {
		return value.length;
	}

	public void ensureCapacity (int minimumCapacity) {
		ensureCapacity_unsynchronized (minimumCapacity);
	}

	public void setLength (int newLength) {
		if (newLength < 0)
			throw new StringIndexOutOfBoundsException (newLength);

		int valueLength = value.length;

		ensureCapacity_unsynchronized (newLength);

		if (newLength < valueLength) {
			count = newLength;
		} else {
			while (count < newLength)
				value[count++] = '\0';
		}
	}

	public char charAt (int index) {
		if (index < 0 || index >= count)
			throw new StringIndexOutOfBoundsException (index);
		return value[index];
	}

	public void getChars (int srcOffset, int srcEnd, char[] dst,
			int dstOffset) {
		if (srcOffset < 0 || srcEnd > count || srcEnd < srcOffset)
			throw new StringIndexOutOfBoundsException ();
		System.arraycopy (value, srcOffset, dst, dstOffset, srcEnd
				- srcOffset);
	}

	public void setCharAt (int index, char ch) {
		if (index < 0 || index >= count)
			throw new StringIndexOutOfBoundsException (index);
		// Call ensureCapacity to enforce copy-on-write.
		ensureCapacity_unsynchronized (count);
		value[index] = ch;
	}

	public StringBuffer append (Object obj) {
		return append (obj == null ? "null" : obj.toString ());
	}

	public StringBuffer append (String str) {
//		System.out.println("AAAAAAAAAAppendAAAAAAAAAAAAAA");
//		System.out.println(str);
//		System.out.println(this);
//		System.out.println("BBBBBBBBBBBBBBBBBBBBBBBBBBBBB");
		if (str == null)
			str = "null";
		int len = str.count;
		ensureCapacity_unsynchronized (count + len);
		str.getChars (0, len, value, count);
		count += len;
		return this;
	}

	public StringBuffer append (StringBuffer stringBuffer) {
//		System.out.println("AAAAAAAAAAppendAAAAAAAAAAAAAA2");
//		System.out.println(stringBuffer);
//		System.out.println(this);
//		System.out.println("BBBBBBBBBBBBBBBBBBBBBBBBBBBBB2");
		if (stringBuffer == null)
			return append ("null");
		synchronized (stringBuffer) {
			int len = stringBuffer.count;
			ensureCapacity_unsynchronized (count + len);
			System.arraycopy (stringBuffer.value, 0, value, count, len);
			count += len;
		}
		return this;
	}

	public StringBuffer append (char[] data) {
		return append (data, 0, data.length);
	}

	public StringBuffer append (char[] data, int offset, int count) {
		if (offset < 0 || count < 0 || offset > data.length - count)
			throw new StringIndexOutOfBoundsException ();
		ensureCapacity_unsynchronized (this.count + count);
		System.arraycopy (data, offset, value, this.count, count);
		this.count += count;
		return this;
	}

	public StringBuffer append (boolean bool) {
		return append (bool ? "true" : "false");
	}

	public StringBuffer append (char ch) {
		ensureCapacity_unsynchronized (count + 1);
		value[count++] = ch;
		return this;
	}

	public StringBuffer append (int inum) {
		return append (Integer.toString (inum, 10));
	}

	public StringBuffer append (long lnum) {
		return append (Long.toString (lnum, 10));
	}

	public StringBuffer append (float fnum) {
		return append (Float.toString (fnum));
	}

	public StringBuffer append (double dnum) {
		return append (Double.toString (dnum));
	}

	public StringBuffer delete (int start, int end) {
		if (start < 0 || start > count || start > end)
			throw new StringIndexOutOfBoundsException (start);
		if (end > count)
			end = count;
		// This will unshare if required.
		ensureCapacity_unsynchronized (count);
		if (count - end != 0)
			System.arraycopy (value, end, value, start, count - end);
		count -= end - start;
		return this;
	}

	public StringBuffer deleteCharAt (int index) {
		return delete (index, index + 1);
	}

	public StringBuffer replace (int start, int end, String str) {
		if (start < 0 || start > count || start > end)
			throw new StringIndexOutOfBoundsException (start);

		int len = str.count;
		// Calculate the difference in 'count' after the replace.
		int delta = len - (end > count ? count : end) + start;
		ensureCapacity_unsynchronized (count + delta);

		if (delta != 0 && end < count)
			System.arraycopy (value, end, value, end + delta, count - end);

		str.getChars (0, len, value, start);
		count += delta;
		return this;
	}

	public String substring (int beginIndex) {
		return substring (beginIndex, count);
	}

	public CharSequence subSequence (int beginIndex, int endIndex) {
		return substring (beginIndex, endIndex);
	}

	public String substring (int beginIndex, int endIndex) {
		int len = endIndex - beginIndex;
		if (beginIndex < 0 || endIndex > count || endIndex < beginIndex)
			throw new StringIndexOutOfBoundsException ();
		if (len == 0)
			return "";
		// Don't copy unless substring is smaller than 1/4 of the buffer.
		boolean share_buffer = ((len << 2) >= value.length);
		if (share_buffer)
			this.shared = true;
		// Package constructor avoids an array copy.
		return new String (value, beginIndex, len, share_buffer);
	}

	public StringBuffer insert (int offset, char[] str,
			int str_offset, int len) {
		if (offset < 0 || offset > count || len < 0 || str_offset < 0
				|| str_offset > str.length - len)
			throw new StringIndexOutOfBoundsException ();
		ensureCapacity_unsynchronized (count + len);
		System.arraycopy (value, offset, value, offset + len, count - offset);
		System.arraycopy (str, str_offset, value, offset, len);
		count += len;
		return this;
	}

	public StringBuffer insert (int offset, Object obj) {
		return insert (offset, obj == null ? "null" : obj.toString ());
	}

	public StringBuffer insert (int offset, String str) {
		if (offset < 0 || offset > count)
			throw new StringIndexOutOfBoundsException (offset);
		if (str == null)
			str = "null";
		int len = str.count;
		ensureCapacity_unsynchronized (count + len);
		System.arraycopy (value, offset, value, offset + len, count - offset);
		str.getChars (0, len, value, offset);
		count += len;
		return this;
	}

	public StringBuffer insert (int offset, char[] data) {
		return insert (offset, data, 0, data.length);
	}

	public StringBuffer insert (int offset, boolean bool) {
		return insert (offset, bool ? "true" : "false");
	}

	public StringBuffer insert (int offset, char ch) {
		if (offset < 0 || offset > count)
			throw new StringIndexOutOfBoundsException (offset);
		ensureCapacity_unsynchronized (count + 1);
		System.arraycopy (value, offset, value, offset + 1, count - offset);
		value[offset] = ch;
		count++;
		return this;
	}

	public StringBuffer insert (int offset, int inum) {
		return insert (offset, Integer.toString (inum, 10));
	}

	public StringBuffer insert (int offset, long lnum) {
		return insert (offset, Long.toString (lnum, 10));
	}

	public StringBuffer insert (int offset, float fnum) {
		return insert (offset, Float.toString (fnum));
	}

	public StringBuffer insert (int offset, double dnum) {
		return insert (offset, Double.toString (dnum));
	}

	public int indexOf (String str) {
		return indexOf (str, 0);
	}

	public int indexOf (String str, int fromIndex) {
		if (fromIndex < 0)
			fromIndex = 0;
		int limit = count - str.count;
		for (; fromIndex <= limit; fromIndex++)
			if (regionMatches (fromIndex, str))
				return fromIndex;
		return -1;
	}

	public int lastIndexOf (String str) {
		return lastIndexOf (str, count - str.count);
	}

	public int lastIndexOf (String str, int fromIndex) {
		fromIndex = Math.min (fromIndex, count - str.count);
		for (; fromIndex >= 0; fromIndex--)
			if (regionMatches (fromIndex, str))
				return fromIndex;
		return -1;
	}

	public StringBuffer reverse () {
		// Call ensureCapacity to enforce copy-on-write.
		ensureCapacity_unsynchronized (count);
		for (int i = count >> 1, j = count - i; --i >= 0; ++j) {
			char c = value[i];
			value[i] = value[j];
			value[j] = c;
		}
		return this;
	}

	public String toString () {
		// The string will set this.shared = true.
		return new String (this);
	}

	private void ensureCapacity_unsynchronized (int minimumCapacity) {
		if (shared || minimumCapacity > value.length) {
			// We don't want to make a larger vector when `shared' is
			// set. If we do, then setLength becomes very inefficient
			// when repeatedly reusing a StringBuffer in a loop.
			int max = (minimumCapacity > value.length ? value.length * 2 + 2
					: value.length);
			minimumCapacity = (minimumCapacity < max ? max : minimumCapacity);
			char[] nb = new char[minimumCapacity];
			System.arraycopy (value, 0, nb, 0, count);
			value = nb;
			shared = false;
		}
	}

	private boolean regionMatches (int toffset, String other) {
		int len = other.count;
		int index = other.offset;
		while (--len >= 0)
			if (value[toffset++] != other.value[index++])
				return false;
		return true;
	}
}
