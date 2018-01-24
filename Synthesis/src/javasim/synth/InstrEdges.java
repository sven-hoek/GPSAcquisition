package javasim.synth;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Iterator;

/**
 * This class represents the set of edges in a graph.
 * @author Michael Raitza
 * @version 15.04.2011
 */
public class InstrEdges<T> extends LinkedHashMap<T, LinkedHashSet<T>> implements Iterable<LinkedHashSet<T>> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2721195088717998937L;

	/**
	 * Puts a new edge between the outward node <code>key</code> and its
	 * inward node <code>value</code>.
	 * A node can have a list of nodes as successors.
	 * @param key the outward node of this edge. The successor in the successor edges set
	 * and the predecessor in the predecessor edges set.
	 * @param value the inward node of this edge. The predecessor in the successor
	 * edges set and the successor in the predecessor edges set.
	 * @return The list of inward nodes connected to the outward node
	 * <code>key</code> prior to the assignment of <code>value</code> or NULL if
	 * no <code>value</code> was assigned before.
	 */
	public LinkedHashSet<T> putEdge(T key, T value) throws IllegalArgumentException {
		LinkedHashSet<T> s = get(key);
		if (s == null)
			s = new LinkedHashSet<T>();

		if (value == null)
			return put(key, s);

		if (!s.add(value))
			return s;

		return put(key, s);
	}

	/**
	 * Removes the edge from the outward node <code>key</code> to the inward
	 * node <code>value</code>.
	 * @param key see put() for the meaning of key
	 * @param value see put() for the meaning of key
	 * @return TRUE iff the edge between the nodes could be removed.
	 * @see #put
	 */
	public boolean removeEdge(T key, T value) {
		LinkedHashSet<T> s = get(key);
		if (s == null)
			return false;

		return s.remove(value);
	}

	/**
	 * Returns the iterator for this edge set.
	 * @return the iterator for this edge set
	 */
	public Iterator<LinkedHashSet<T>> iterator() {
		return values().iterator();
	}
}
