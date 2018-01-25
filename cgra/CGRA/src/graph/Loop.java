package graph;

import java.util.LinkedHashSet;

/**
 * Representing a Loop in the synthesized code segment
 * @author jung
 *
 */
public class Loop {
	/**
	 * Start address of the loop
	 */
	int start;
	/**
	 * End address of the loop
	 */
	int stop;
	
	/**
	 * Stores all if nodes that controll the loop (these are jumps out of the loop) 
	 */
	LinkedHashSet<Node> controller;
	
	/**
	 * Creates a new loop
	 * @param start Start address of the loop
	 * @param stop Stop address of the loop
	 */
	public Loop(int start, int stop){
		this.start = start;
		this.stop = stop;
		controller = new LinkedHashSet<Node>();
	}
	
	/**
	 * Adds a new controller to the list
	 * @param c The new controller
	 */
	public void addController(Node c){
		controller.add(c);
	}
	
	/**
	 * Returns the list of controllers
	 * @return The controllers
	 */
	public LinkedHashSet<Node> getController(){
		return controller;
	}
	
	/**
	 * Determines whether a loop is nested in the current loop
	 * @param l the loop to be tested
	 * @return true if l is nested in the current loop
	 */
	public boolean contains(Loop l){
		return (start < l.start) && (stop > l.start);
	}
	
	
	public boolean contains(Node nd){
		return (nd.getAddress() >= start) && (nd.getAddress() <= stop);
	}
	
	public String toString() {
		if (controller.size() > 0) {
			return start + "-" + stop + "-" + controller.iterator().next();
		} else {
			return start + "-" + stop;
		}
	}

	public int getStop() {
		return stop;
	}
	
	public int getStart(){
		return start;
	}
}
