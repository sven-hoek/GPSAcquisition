package scheduler;

import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import graph.CDFG;
import graph.Node;
import operator.Operator;


/**
 * @author Tony
 * Implementation of the longest path weight
 */
public class LPW implements SchedPrio {
	
	/**
	 * Maps nodes to their corresponding weight
	 */
	protected Map<Node, Integer> nodeWeight;
	boolean[] isPrefetch;
	
	public LPW(CDFG g, boolean[] isPrefetch) {
		LinkedHashSet<Node> leafs = new LinkedHashSet<Node>();
		LinkedHashSet<Node> handled = new LinkedHashSet<Node>();
		
		nodeWeight = new LinkedHashMap<Node, Integer>();
		this.isPrefetch = isPrefetch;

		for (Node nd : g) {
			if ((g.getSuccessors(nd)==null || g.getSuccessors(nd).size() == 0) && (g.getConsumers(nd)==null || g.getConsumers(nd).size() == 0)) {
				leafs.add(nd);
			} 
		}
		
		if (leafs.size() == 0){
			System.out.println("No leaf in Graph found. Empty or cyclic graph");
		} 
		
		LinkedHashSet<Node> nl;
		int weight, ownWeight;
		boolean succHandled;

		while (leafs.size() > 0) {
			nl = new LinkedHashSet<Node>();
			nl.addAll(leafs);
			for (Node nd : leafs) {
				Operator op = nd.getOperation();
				if (op.isNative())		//natives should not be relevant for paths. they are usually fused
				//if (op == Amidar.OP.CONST || op == Amidar.OP.LOAD || op == Amidar.OP.LOAD64 || op == Amidar.OP.STORE || op == Amidar.OP.STORE64)
					ownWeight = 0;
				else
					ownWeight = 1;
				weight = successorWeight(g.getAllSuccessors(nd))+ownWeight;
				if (getWeight(nd) <= weight) {
					nodeWeight.put(nd, weight);
				}
				nl.remove(nd);
				
				handled.add(nd);
				
				//Add predecessors if all successors are handled
				for (Node nd2 : g.getAllPredecessors(nd)) {
					succHandled = true;
					for (Node nd3 : g.getAllSuccessors(nd2)) {
						//if (getWeight(nd3) == 0) succHandled = false;
						if (!handled.contains(nd3)) {
							succHandled = false;
							break;
						}
					}
					if (succHandled){
						nl.add(nd2);
					}
				}
			}
			leafs = nl;
		}
		
//		for(Node nd : nodeWeight.keySet()){
//			if(nd.getOperation() == Amidar.OP.DMA_LOAD || nd.getOperation() == Amidar.OP.DMA_STORE ){
//				
//				Integer weig = nodeWeight.get(nd);
//				weig = (int)(weig*2.5);
//				nodeWeight.put(nd, weig);
//			}
//			if(nd.getOperation() == Amidar.OP.FMUL || nd.getOperation() == Amidar.OP.FADD){
//				Integer weig = nodeWeight.get(nd);
//				weig = (int)(weig*4);
//				nodeWeight.put(nd, weig);
//			}
//		}
	}
	
	/**
	 * Returns the maximum weight of all successors
	 * @param successors - A set of successor nodes
	 * @return the maximum weight
	 */
	private int successorWeight(HashSet<Node> successors) {
		if (successors == null)	return 0;
		if (successors.size() == 0) return 0;
		
		int max = 0;
		int weight = 0;
		
		for (Node nd : successors) {
			weight = getWeight(nd);
			if (weight > max) max = weight;
		}
		
		return max;
	}
	
	/**
	 * Get the weight for any node in the graph.
	 * @param nd - Node of interest
	 * @return Its weight
	 */
	public int getWeight(Node nd) {
		if (nd == null) return 0;
		if (!nodeWeight.containsKey(nd)) {
			return 0;
		} else {
			return nodeWeight.get(nd);
		}
	}
	
	/**
	 * Produces a sorted Map of all Nodes and their weights
	 * @return A TreeMap containing each Node and its weight
	 */
	public TreeMap<Node, Integer> getSortedMap() {
		WeightComparator cmp = new WeightComparator(nodeWeight);
		TreeMap<Node, Integer> sortedWeight = new TreeMap<Node, Integer>(cmp);
		sortedWeight.putAll(this.nodeWeight);
		
		return sortedWeight;
	}

	@Override
	public TreeMap<Node, Integer> getSortedNodes(Set<Node> nds) {
		WeightComparator cmp = new WeightComparator(nodeWeight);
		TreeMap<Node, Integer> sortedWeight = new TreeMap<Node, Integer>(cmp);
		for (Node nd : nds){
			int weight = getWeight(nd);
//			System.err.println(nd+ " weight " + weight);
//			if(nd.getOperation() == Amidar.OP.DMA_LOAD || nd.getOperation() == Amidar.OP.DMA_STORE ){
//				weight = weight / 200;
//			}
//			if(isPrefetch[nd.getAddress()]){
//				weight += 100;
//			}
				
			sortedWeight.put(nd,weight);
		}
		return sortedWeight;
	}
	
	/**
	 * @author Tony
	 * Comparator to compare Node weights and allow TreeMap sorting
	 */
	private class WeightComparator implements Comparator<Node> {
	    Map<Node, Integer> base;
	    public WeightComparator(Map<Node, Integer> base) {
	        this.base = base;
	    }
	    
	    public int compare(Node a, Node b) {
	    	int a1,b1;
	    	if(base.get(a) == null){
	    		System.err.println("Yooo... " + a + " " + b + " " + base);
	    	}
	    	
//	    	if(isPrefetch != null){
//	    		if(isPrefetch[a.getAddress()] && !isPrefetch[b.getAddress()]){
//	    			return 1;
//	    		} else if(isPrefetch[a.getAddress()] && !isPrefetch[b.getAddress()]){
//	    			return -1;
//	    		}
//	    	}
	    	a1 = base.get(a);
	    	b1 = base.get(b);
//	    	if(isPrefetch[a.getAddress()]){
//	    		a1 += 100;
//	    	}
//	    	if(isPrefetch[b.getAddress()]){
//	    		b1 += 100;
//	    	}
	    	
	    	if (a1 < b1) {
	    		return 1;
	    	} else {
	    		return -1;
	    	}
	    }
	}
}
