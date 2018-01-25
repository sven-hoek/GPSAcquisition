package dot;

import java.util.*;

/**
 * The DotGraph represents a full DOT graph. General graph, node, and edge
 * attributes are defined
 */
public class DotGraph extends DotNodeT<DotAttrNode> {

	private DotGraph() {
		super("");
	}

	public DotGraph(String id) {
		super(id);
	}

	public String toString() {
		Formatter f = new Formatter();

		try {
			f.format("digraph %s {\n", id());

			Iterator<String> it = attr().iterator();
			String key;
			while (it.hasNext()) {
				key = it.next();
				f.format("%s=%s;\n", key, attr(key));
			}

			f.format("\n");

			for (DotAttrNode node : connect())
				f.format("%s\n", node);

			f.format("}\n");
			return f.toString();
		} finally {
			f.close();
		}
	}
}
