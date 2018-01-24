package de.amidar;

import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class HashMapInt {
	
	/**
	 * Default number of buckets. This is the value the JDK 1.3 uses. Some early
	 * documentation specified this value as 101. That is incorrect. Package
	 * visible for use by HashSet.
	 */
	static final int DEFAULT_CAPACITY = 11;

	/**
	 * The default load factor; this is explicitly specified by the spec.
	 * Package visible for use by HashSet.
	 */
	static final float DEFAULT_LOAD_FACTOR = 0.75f;

	/**
	 * The rounded product of the capacity and the load factor; when the number
	 * of elements exceeds the threshold, the HashMap calls
	 * <code>rehash()</code>.
	 * 
	 * @serial the threshold for rehashing
	 */
	private int threshold;

	/**
	 * Load factor of this HashMap: used in computing the threshold. Package
	 * visible for use by HashSet.
	 * 
	 * @serial the load factor
	 */
	final float loadFactor;

	/**
	 * Array containing the actual key-value mappings. Package visible for use
	 * by nested and subclasses.
	 */
	transient HashEntryInt[] buckets;

	/**
	 * Counts the number of modifications this HashMap has undergone, used by
	 * Iterators to know when to throw ConcurrentModificationExceptions. Package
	 * visible for use by nested and subclasses.
	 */
	transient int modCount;

	/**
	 * The size of this HashMap: denotes the number of key-value pairs. Package
	 * visible for use by nested and subclasses.
	 */
	transient int size;

	/**
	 * Class to represent an entry in the hash table. Holds a single key-value
	 * pair. Package visible for use by subclass.
	 *
	 * @author Eric Blake (ebb9@email.byu.edu)
	 */
	static class HashEntryInt {
		/**
		 * The key. Package visible for direct manipulation.
		 */
		int key;

		/**
		 * The value. Package visible for direct manipulation.
		 */
		Object value;

		/**
		 * The next entry in the linked list. Package visible for use by
		 * subclass.
		 */
		HashEntryInt next;

		/**
		 * Get the key corresponding to this entry.
		 *
		 * @return the key
		 */
		public final int getKey () {
			return key;
		}

		/**
		 * Get the value corresponding to this entry. If you already called
		 * Iterator.remove(), the behavior undefined, but in this case it works.
		 *
		 * @return the value
		 */
		public final Object getValue () {
			return value;
		}

		/**
		 * Returns the hash code of the entry. This is defined as the
		 * exclusive-or of the hashcodes of the key and value (using 0 for
		 * null). In other words, this must be:<br>
		 * 
		 * <pre>
		 * getKey ()
		 * 		&circ; (getValue () == null ? 0 : getValue ().hashCode ())
		 * </pre>
		 *
		 * @return the hash code
		 */
		public final int hashCode () {
			return key ^ (value == null ? 0 : value.hashCode ());
		}

		/**
		 * Replaces the value with the specified object. This writes through to
		 * the map, unless you have already called Iterator.remove(). It may be
		 * overridden to restrict a null value.
		 *
		 * @param newVal
		 *            the new value to store
		 * @return the old value
		 * @throws NullPointerException
		 *             if the map forbids null values.
		 * @throws UnsupportedOperationException
		 *             if the map doesn't support <code>put()</code>.
		 * @throws ClassCastException
		 *             if the value is of a type unsupported by the map.
		 * @throws IllegalArgumentException
		 *             if something else about this value prevents it being
		 *             stored in the map.
		 */
		public Object setValue (Object newVal) {
			Object r = value;
			value = newVal;
			return r;
		}

		/**
		 * This provides a string representation of the entry. It is of the form
		 * "key=value", where string concatenation is used on key and value.
		 *
		 * @return the string representation
		 */
		public final String toString () {
			return key + "=" + value;
		}

		/**
		 * Simple constructor.
		 * 
		 * @param key
		 *            the key
		 * @param value
		 *            the value
		 */
		HashEntryInt (int key, Object value) {
			this.key = key;
			this.value = value;
		}

	}

	/**
	 * Construct a new HashMap with the default capacity (11) and the default
	 * load factor (0.75).
	 */
	public HashMapInt () {
		this (DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR);
	}

	/**
	 * Construct a new HashMap with a specific inital capacity and default load
	 * factor of 0.75.
	 *
	 * @param initialCapacity
	 *            the initial capacity of this HashMap (&gt;=0)
	 * @throws IllegalArgumentException
	 *             if (initialCapacity &lt; 0)
	 */
	public HashMapInt (int initialCapacity) {
		this (initialCapacity, DEFAULT_LOAD_FACTOR);
	}

	/**
	 * Construct a new HashMap with a specific inital capacity and load factor.
	 *
	 * @param initialCapacity
	 *            the initial capacity (&gt;=0)
	 * @param loadFactor
	 *            the load factor (&gt; 0, not NaN)
	 * @throws IllegalArgumentException
	 *             if (initialCapacity &lt; 0) || ! (loadFactor &gt; 0.0)
	 */
	public HashMapInt (int initialCapacity, float loadFactor) {
		if (initialCapacity < 0)
			throw new IllegalArgumentException ("Illegal Capacity: "
					+ initialCapacity);
		if (!(loadFactor > 0)) // check for NaN too
			throw new IllegalArgumentException ("Illegal Load: " + loadFactor);

		if (initialCapacity == 0)
			initialCapacity = 1;
		buckets = new HashEntryInt[initialCapacity];
		this.loadFactor = loadFactor;
		threshold = (int) (initialCapacity * loadFactor);
	}

	/**
	 * Returns the number of kay-value mappings currently in this Map.
	 *
	 * @return the size
	 */
	public int size () {
		return size;
	}

	/**
	 * Returns true if there are no key-value mappings currently in this Map.
	 *
	 * @return <code>size() == 0</code>
	 */
	public boolean isEmpty () {
		return size == 0;
	}

	/**
	 * Return the value in this HashMap associated with the supplied key, or
	 * <code>null</code> if the key maps to nothing. NOTE: Since the value could
	 * also be null, you must use containsKey to see if this key actually maps
	 * to something.
	 *
	 * @param key
	 *            the key for which to fetch an associated value
	 * @return what the key maps to, if present
	 * @see #put(int, Object)
	 * @see #containsKey(int)
	 */
	public Object get (int key) {
		int idx = hash (key);
		HashEntryInt e = buckets[idx];
		while (e != null) {
			if (key == e.key)
				return e.value;
			e = e.next;
		}
		return null;
	}

	/**
	 * Returns true if the supplied key equals a key in this
	 * HashMap.
	 *
	 * @param key
	 *            the key to search for in this HashMap
	 * @return true if the key is in the table
	 * @see #containsValue(Object)
	 */
	public boolean containsKey (int key) {
		int idx = hash (key);
		HashEntryInt e = buckets[idx];
		while (e != null) {
			if (key == e.key)
				return true;
			e = e.next;
		}
		return false;
	}

	/**
	 * Puts the supplied value into the Map, mapped by the supplied key.
	 *
	 * @param key
	 *            the key used to locate the value
	 * @param value
	 *            the value to be stored in the HashMap
	 * @return the prior mapping of the key, or null if there was none
	 * @see #get(int)
	 */
	public Object put (int key, Object value) {
		int idx = hash (key);
		HashEntryInt e = buckets[idx];

		while (e != null) {
			if (key == e.key) {
				Object r = e.value;
				e.value = value;
				return r;
			} else
				e = e.next;
		}

		// At this point, we know we need to add a new entry.
		modCount++;
		if (++size > threshold) {
			rehash ();
			// Need a new hash value to suit the bigger table.
			idx = hash (key);
		}

		HashEntryInt eNew = new HashEntryInt (key, value);
		eNew.next = buckets[idx];
		buckets[idx] = eNew;
		return null;
	}

	/**
	 * Removes from the HashMap and returns the value which is mapped by the
	 * supplied key. If the key maps to nothing, then the HashMap remains
	 * unchanged, and <code>null</code> is returned. NOTE: Since the value could
	 * also be null, you must use containsKey to see if you are actually
	 * removing a mapping.
	 *
	 * @param key
	 *            the key used to locate the value to remove
	 * @return whatever the key mapped to, if present
	 */
	public Object remove (int key) {
		int idx = hash (key);
		HashEntryInt e = buckets[idx];
		HashEntryInt last = null;

		while (e != null) {
			if (key == e.key) {
				modCount++;
				if (last == null)
					buckets[idx] = e.next;
				else
					last.next = e.next;
				size--;
				return e.getValue ();
			}
			last = e;
			e = e.next;
		}
		return null;
	}

	/**
	 * Clears the Map so it has no keys. This is O(1).
	 */
	public void clear () {
		if (size != 0) {
			modCount++;
			Arrays.fill (buckets, null);
			size = 0;
		}
	}

	/**
	 * Returns true if this HashMap contains a value <code>o</code>, such that
	 * <code>o.equals(value)</code>.
	 *
	 * @param value
	 *            the value to search for in this HashMap
	 * @return true if at least one key maps to the value
	 * @see #containsKey(Object)
	 */
	public boolean containsValue (Object value) {
		for (int i = buckets.length - 1; i >= 0; i--) {
			HashEntryInt e = buckets[i];
			while (e != null) {
				if (value == e.value || (value != null && value.equals (e.value)))
					return true;
				e = e.next;
			}
		}
		return false;
	}

	/**
	 * Helper method that returns an index in the buckets array for `key' based
	 * on its hashCode(). Package visible for use by subclasses.
	 *
	 * @param key
	 *            the key
	 * @return the bucket number
	 */
	final int hash (int key) {
		return Math.abs (key % buckets.length);
	}

	/**
	 * Generates a iterator.
	 *
	 * @return the iterator
	 */
	Iterator iterator () {
		return new HashIterator ();
	}

	/**
	 * Increases the size of the HashMap and rehashes all keys to new array
	 * indices; this is called when the addition of a new value would cause
	 * size() &gt; threshold. Note that the existing Entry objects are reused in
	 * the new hash table.
	 *
	 * <p>
	 * This is not specified, but the new size is twice the current size plus
	 * one; this number is not always prime, unfortunately.
	 */
	private void rehash () {
		HashEntryInt[] oldBuckets = buckets;

		int newcapacity = (buckets.length * 2) + 1;
		threshold = (int) (newcapacity * loadFactor);
		buckets = new HashEntryInt[newcapacity];

		for (int i = oldBuckets.length - 1; i >= 0; i--) {
			HashEntryInt e = oldBuckets[i];
			while (e != null) {
				int idx = hash (e.key);
				HashEntryInt next = e.next;
				e.next = buckets[idx];
				buckets[idx] = e;
				e = next;
			}
		}
	}

	/**
	 * Iterate over HashMap's entries.
	 */
	private final class HashIterator implements Iterator {
		/**
		 * The number of modifications to the backing HashMap that we know
		 * about.
		 */
		private int knownMod = modCount;
		/** The number of elements remaining to be returned by next(). */
		private int count = size;
		/** Current index in the physical hash table. */
		private int idx = buckets.length;
		/** The last Entry returned by a next() call. */
		private HashEntryInt last;
		/**
		 * The next entry that should be returned by next(). It is set to
		 * something if we're iterating through a bucket that contains multiple
		 * linked entries. It is null if next() needs to find a new bucket.
		 */
		private HashEntryInt next;

		/**
		 * Construct a new HashIterator.
		 */
		HashIterator () {
		}

		/**
		 * Returns true if the Iterator has more elements.
		 * 
		 * @return true if there are more elements
		 * @throws ConcurrentModificationException
		 *             if the HashMap was modified
		 */
		public boolean hasNext () {
			if (knownMod != modCount)
				throw new ConcurrentModificationException ();
			return count > 0;
		}

		/**
		 * Returns the next element in the Iterator's sequential view.
		 * 
		 * @return the next element
		 * @throws ConcurrentModificationException
		 *             if the HashMap was modified
		 * @throws NoSuchElementException
		 *             if there is none
		 */
		public Object next () {
			if (knownMod != modCount)
				throw new ConcurrentModificationException ();
			if (count == 0)
				throw new NoSuchElementException ();
			count--;
			HashEntryInt e = next;

			while (e == null)
				e = buckets[--idx];

			next = e.next;
			last = e;
			return e;
		}

		/**
		 * Removes from the backing HashMap the last element which was fetched
		 * with the <code>next()</code> method.
		 * 
		 * @throws ConcurrentModificationException
		 *             if the HashMap was modified
		 * @throws IllegalStateException
		 *             if called when there is no last element
		 */
		public void remove () {
			if (knownMod != modCount)
				throw new ConcurrentModificationException ();
			if (last == null)
				throw new IllegalStateException ();

			HashMapInt.this.remove (last.key);
			last = null;
			knownMod++;
		}
	}
}
