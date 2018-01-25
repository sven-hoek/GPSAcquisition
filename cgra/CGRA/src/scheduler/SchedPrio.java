package scheduler;

import java.util.Set;
import java.util.TreeMap;

import graph.Node;

/**
 * @author Tony
 * Interface for the priority criterion to use with the RCListScheduler.
 */
public interface SchedPrio {
	/**
	 * Get a sorted TreeMap of the Nodes provided. A value must be provided for any Node in the graph.
	 * @param nds - Set of nodes to be sorted
	 * @return A TreeMap containing the given nodes in descending order of priority
	 */
	public TreeMap<Node, Integer> getSortedNodes(Set<Node> nds);
}
