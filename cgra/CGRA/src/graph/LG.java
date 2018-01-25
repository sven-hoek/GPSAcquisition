package graph;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Represents a loop graph
 * @author jung
 *
 */
public class LG {
	
	/**
	 * Set of all loops
	 */
	public LinkedHashSet<Loop> loops;

	/**
	 * Contains all nested loops of the key loop
	 */
	LinkedHashMap<Loop, LinkedHashSet<Loop>> children;
	/**
	 * Contains the wrapping loop
	 */
	LinkedHashMap<Loop, Loop> parent;
	/**
	 * Contains the loopCounter for the key loop
	 */
	public LinkedHashMap<Loop, Integer> loopCounter;
	
	/**
	 * Creates a new loop graph
	 */
	public LG(){
		children = new LinkedHashMap<Loop, LinkedHashSet<Loop>>();
		parent = new LinkedHashMap<Loop, Loop>();
		loops = new LinkedHashSet<Loop>();
		loopCounter = new LinkedHashMap<Loop, Integer>();
	}
	
	/**
	 * Adds a new Loop to the loop graph
	 * @param l
	 * @param children
	 * @param parent
	 */
	public void addLoop(Loop l,LinkedHashSet<Loop> children, Loop parent){
		//System.out.println("add "+l);
		loops.add(l);
		this.children.put(l, children);
		this.parent.put(l, parent);
	}
	
	public String toString(){
		StringBuffer erg = new StringBuffer();
		erg.append("digraph depgraph {\n");
		for(Loop l: loops){
			Loop par = parent.get(l);
			if(par != null){
				erg.append("\""+ par + "\" -> \""+l+"\"\n");
			} else{
				erg.append("\""+l+"\"\n");
			}
		}
		
		
		erg.append("}");
		return erg.toString();
	}

	/**
	 * Get the root (main loop) of the loop graph
	 * @return the root loop
	 */
	public Loop getRoot() {
		for (Loop lp : parent.keySet()) {
			if (parent.get(lp)==null) return lp;
		}
		return null;
	}

	/**
	 * Get the innermost loop that belongs to the given node.
	 * @param nd - The node of interest
	 * @return Its loop
	 */
	public Loop getLoop(Node nd) {
		

		int addr = nd.getAddress();
		return getLoop(addr);
		
	}
	
	public Loop getLoop(int addr){
		
		Loop rt = getRoot();
		Loop lp = null;
		LinkedHashSet<Loop> childs;

		do {
			lp = rt;

			childs = children.get(lp);

			for (Loop nl : childs) {
				if (nl.start <= addr && addr <= nl.stop) {
					rt = nl;
				}
			}
		} while (lp != rt);

		return lp;
	}
	
	
	/**
	 * Get the children of any given loop.
	 * @param lp - The loop of interest
	 * @return A Set of loops. If none exist, an empty Set will be returned
	 */
	public Set<Loop> getChildren(Loop lp) {
		if (children.containsKey(lp)) {
			return children.get(lp);
		} else {
			return new LinkedHashSet<Loop>();
		}
	}

	/**
	 * Check if two loops have a parent-child relationship.
	 * @param child - Possible child loop
	 * @param parent - Possible parent loop
	 * @return True iff parent surrounds child and child != parent
	 */
	public boolean isChildOf(Loop child, Loop parent) {
		return ((parent.start <= child.start) && (parent.stop >= child.stop) && (parent != child));
	}
	
	/**
	 * Returns the surrounding loop of the given loop	
	 * @param lp the given loop	
	 * @return the surrounding loop
	 */
	public Loop getFather(Loop lp){
		return parent.get(lp);
	}
	
	/**
	 * Returns the number of loop iterations of the given loop
	 * @param lp the given loop
	 * @return the number of iterations, null if unknown
	 */
	public Integer getLoopCounter(Loop lp){
		return loopCounter.get(lp);
	}
	
	/**
	 * Returns the nesting level of the given loop. If there 
	 * is only the root loop, the level is 0
	 * @param lp the given loop
	 * @return the nesting level
	 */
	public Integer getNestingLevel(Loop lp){
		int i=0; 
		while(!lp.equals(getRoot())){
			lp=getFather(lp);
			i++;
		}
		return i;
	}
	
	/**
	 * Returns the maximum nesting level of the LG. If there 
	 * is only the root loop, the level is 0
	 * @return the nesting level
	 */
	public Integer getMaxNestingLevel(){
		int i=-1;
		for(Loop lp:loops){
			int temp = getNestingLevel(lp);
			if(temp>i)i=temp;
		}
		return i;
	}
}
