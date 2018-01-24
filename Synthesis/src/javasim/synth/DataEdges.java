package javasim.synth;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Iterator;
import java.util.Set;

import javasim.synth.model.datum.Datum;

/**
 * This class represents the set of data flow edges in the data graph.
 *
 * @author Michael Raitza
 * @version 28.07.2011
 */
public class DataEdges extends LinkedHashMap<Datum, Set<DEdge>> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1615418192481865806L;

	/**
	 * Puts a new edge between the outward node <code>key</code> and its
	 * inward node <code>value</code>.
	 * A node can have a list of nodes as successors.
	 * @param key the outward node of this edge. The successor in the successor edges set
	 * and the predecessor in the predecessor edges set.
	 * @param value the inward node of this edge. The predecessor in the successor
	 * edges set and the successor in the predecessor edges set.
	 * @param attr the attribute to this edge, can be NULL.
	 * @return The list of inward nodes connected to the outward node
	 * <code>key</code> prior to the assignment of <code>value</code> or NULL if
	 * no <code>value</code> was assigned before.
	 */
	public Set<DEdge> put(Datum key, Datum value, Integer attr) throws IllegalArgumentException {
		Set<DEdge> s = super.get(key);
		if (s == null)
			s = new LinkedHashSet<DEdge>();

		if (attr == null) {
			throw new IllegalArgumentException("Null argument for attribute not allowed.");
		}

		DEdge e = new DEdge(value, attr);

		if (s.contains(e))
			return s;
		s.add(e);

		return put(key, s);
	}

	public Set<DEdge> get(Datum key) {
		if (containsKey(key))
			return super.get(key);
		return null;
	}

	public Integer get(Datum key, Datum value) {
		if (!containsKey(key))
			return null;

		for (DEdge e : super.get(key))
			if (e.sink.equals(value))
				return e.attr;
		return null;
	}

	/**
	 * Removes the edge from the outward node <code>key</code> to the inward
	 * node <code>value</code>.
	 * @param key see put() for the meaning of key
	 * @param value see put() for the meaning of value
	 * @see #put
	 */
	public void remove(Datum key, Datum value) {
		Set<DEdge> s = super.get(key);
		if (s == null)
			return;

		Iterator<DEdge> it = s.iterator();
		while (it.hasNext()) {
			DEdge ss = it.next();
			if (ss.sink.equals(value))
				it.remove();
		}
	}
}
