package dot;

import java.util.*;

/**
 * A DotNodeT represents a single node in a DOT graph. (See http://graphviz.org)
 * This includes node-specific attributes and edges to other nodes. The whole
 * syntactic construct representing the node and its edges are accessed via a
 * call to the toString() method. You cannot use this class directly as it only
 * represents an archetype of a node, use DotNode instead.
 * 
 * @author Michael Raitza
 * @version - 02.04.2011
 */
public abstract class DotNodeT<T extends DotAttrNode> extends DotAttrNode {

	private class NA {
		public final T node;
		public final String attr;

		public NA(T n, String a) {
			node = n;
			attr = a;
		}
	}

	/**
	 * The list of nodes that are reached from this node
	 */
	private Map<T, String> connects;
	private List<NA> conns;

	private DotNodeT() throws IllegalArgumentException {
		super(null);
	}

	/**
	 * Constructs a new DOT graph node.
	 * 
	 * @param id
	 *            the ID of this node
	 * @throws IllegalArgumentException
	 *             if the <code>id</code> is NULL
	 */
	public DotNodeT(String id) throws IllegalArgumentException {
		super(id);
		connects = new HashMap<T, String>();
		conns = new LinkedList<NA>();
	}

	/**
	 * Constructs a new DOT graph node.
	 * 
	 * @param id
	 *            the ID of this node
	 * @param label
	 *            the 'label' attribute value
	 * @throws IllegalArgumentException if the <code>id</code> is NULL
	 */
	public DotNodeT(String id, String label) throws IllegalArgumentException {
		this(id);
		attr("label", label);
	}

	/**
	 * Constructs a String representation of this node and its connections to
	 * other nodes in the DOT graph language and returns it.
	 * 
	 * @return the DOT graph code snippet representing this node and its
	 *         connections to other nodes
	 */
	public String toString() {
		Formatter f = new Formatter();

		try {
			f.format(super.toString());

			for (NA node : conns) {
				f.format("%s -> %s", id(), node.node.id());
				if (!node.attr.equals(""))
					f.format("[%s]", node.attr);
				f.format(";\n");
			}

			return f.toString();
		} finally {
			f.close();
		}
	}

	/**
	 * Returns the List of nodes this node is connected to.
	 * 
	 * @return the List of nodes this node is connected to
	 */
	public Set<? extends T> connect() {
		// return connects.keySet();
		Set<T> tl = new HashSet<T>();
		for (NA n : conns)
			tl.add(n.node);
		return tl;
	}

	/**
	 * Connects this node to the node given in <code>node</code>.
	 * 
	 * @param node
	 *            the node to connect this node to.
	 */
	public void connect(T node) {
		connect(node, "");
	}

	/*
	 * FIXME: inconsistent, we should use a list of attribute nodes here, or
	 * sth. similar
	 */
	public void connect(T node, String attr) {
		connects.put(node, attr);
		conns.add(new NA(node, attr));
	}

	/**
	 * Disconnects this node from the node given in <code>node</code>.
	 * 
	 * @param node
	 *            the node to disconnect this node from.
	 */
	public void disconnect(T node) {
		connects.remove(node);
		conns.remove(node);
	}

	public void disconnect() {
		connects = new HashMap<T, String>();
		conns = new LinkedList<NA>();
	}
}
