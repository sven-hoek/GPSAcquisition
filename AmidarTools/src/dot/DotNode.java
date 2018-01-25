package dot;


/**
 * A DotNode represents a single node in a DOT graph. (See http://graphviz.org)
 * This includes node-specific attributes and edges to other nodes. The whole syntactic
 * construct representing the node and its edges are accessed via a call to the toString()
 * method.
 *
 * @author Michael Raitza
 * @version - 02.04.2011
 */
public class DotNode  extends DotNodeT<DotNode> {

	private DotNode() throws IllegalArgumentException { super(null); }

	/**
	 * Constructs a new DOT graph node.
	 * @param id the ID of this node
	 * @throws IllegalArgumentException if the <code>id</code> is NULL
	 */
	public DotNode(String id) throws IllegalArgumentException {
		super(id);
	}

	/**
	 * Constructs a new DOT graph node.
	 * @param id the ID of this node
	 * @param label the 'label' attribute value
	 * @throws IllegalArgumentException if the <code>id</code> is NULL
	 */
	public DotNode(String id, String label) throws IllegalArgumentException {
		super(id, label);
	}
}
