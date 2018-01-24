package dot;

import java.util.*;

/**
 * The DotAttrNode represents a pseudo node in the graph. The DOT graph language
 * supports the two pseudo nodes 'edge' and 'node' to specify common attributes
 * for nodes and edges in the scope of these pseudo nodes. This special node
 * cannot be linked to or have connections to other nodes.
 */
public class DotAttrNode {

	/**
	 * The ID of this node.
	 */
	protected String id;

	/**
	 * The map of attributes defined for this node.
	 */
	private Map<String, String> attributes;

	/**
	 * Constructs a new DOT attribute node. This type of node is used for the
	 * 'node', 'edge' pseudo nodes.
	 * 
	 * @param id
	 *            the ID of this node
	 * @throws IllegalArgumentException
	 *             if the <code>id</code> is NULL
	 */
	public DotAttrNode(String id) throws IllegalArgumentException {
		if (id == null)
			throw new IllegalArgumentException("Graph nodes must have an ID");
		this.id = id;
		attributes = new HashMap<String, String>();
	}

	/**
	 * Constructs a String representation of this pseudo node in the DOT graph
	 * language and returns it.
	 * 
	 * @return the DOT graph code snippet representing this pseudo node
	 */
	public String toString() {
		Formatter f = new Formatter();

		try {
			f.format("%s", id);
			if (attr().size() > 0) {
				f.format(" [");
				Iterator<String> it = attr().iterator();
				String attr;
				if (it.hasNext()) {
					attr = it.next();
					f.format("%s=%s", attr, attr(attr));
				}
				while (it.hasNext()) {
					attr = it.next();
					f.format(",%s=%s", attr, attr(attr));
				}
				f.format("]");
			}
			f.format(";\n");
			return f.toString();
		} finally {
			f.close();
		}
	}

	/**
	 * Sets the attribute identified by <code>key</code> to the specified
	 * <code>value</code>.
	 * 
	 * @param key
	 *            the attribute name
	 * @param value
	 *            the attribute value
	 */
	public void attr(String key, String value) {
		attributes.put(key, value);
	}

	/**
	 * Returns the value of the attribute identified by <code>key</code> or NULL
	 * if no such attribute is explicitly defined for this node.
	 * 
	 * @param key
	 *            the attribute name
	 * @return the attribute's value
	 */
	public String attr(String key) {
		return attributes.get(key);
	}

	/**
	 * Returns the Set of the names of defined attributes.
	 * 
	 * @return the Set of attribute names
	 */
	public HashSet<String> attr() {
		return new HashSet<String>(attributes.keySet());
	}

	/**
	 * Returns the ID of this node.
	 * 
	 * @return the ID of this node
	 */
	public String id() {
		return id;
	}
}
