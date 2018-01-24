package dot;

import java.util.*;

/**
 * The DotSubGraph represents the syntactic construct of a DOT subgraph. To
 * create an anonymous subgraph, construct the DotSubGraph with an empty ID.
 * (But the ID must not be NULL!) Subgraphs can also carry attributes (graph,
 * node, and edge attributes). The nodes a subgraph is connected to are the
 * inner nodes of this subgraph.
 * 
 * @author Michael Raitza
 * @version - 02.04.2011
 */
public class DotSubGraph extends DotNodeT<DotAttrNode> {

	private DotSubGraph() {
		super("");
	}

	/**
	 * Constructs a new subgraph.
	 * 
	 * @param id
	 *            the id of the subgraph
	 */
	public DotSubGraph(String id) {
		super(id);
	}

	/**
	 * Constructs a String representation of this subgraph in the DOT graph
	 * language and returns it.
	 * 
	 * @return the DOT graph code snippet representing this subgraph
	 */
	public String toString() {
		Formatter f = new Formatter();

		try {
			if (id().equals(""))
				f.format("{\n");
			else
				f.format("subgraph %s {\n", id());

			Iterator<String> it = attr().iterator();
			String key;
			while (it.hasNext()) {
				key = it.next();
				f.format("%s=%s;\n", key, attr(key));
			}

			f.format("\n");

			for (DotAttrNode node : connect())
				f.format("%s", node);

			f.format("}\n");
			return f.toString();
		} finally {
			f.close();
		}
	}
}
