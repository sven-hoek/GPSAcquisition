package scheduler;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import graph.Node;

/**
 * @author Tony
 * Class to represent a schedule. Mostly stolen from SchedulerFramework
 */
public class Schedule {
	/**
	 * Each scheduled node and its interval
	 */
	private Map<Node, Interval> nodes;
	
	/**
	 * All nodes scheduled at any given time
	 */
	private Map<Integer, Set<Node>> slots;
	
	public Schedule() {
		nodes = new LinkedHashMap<Node, Interval>();
		slots = new TreeMap<Integer, Set<Node>>();
	}

	/**
	 * Add a node to the schedule. Duplicates will be overwritten.
	 * @param nd - The node to be scheduled
	 * @param i - Its interval
	 */
	public void add(Node nd, Interval i) {
		if (nodes.containsKey(nd))
			remove(nd);

		nodes.put(nd, i);

		for (int ii = i.lbound; ii <= i.ubound; ii++) {
			Set<Node> ss = slots.get(ii);
			if (ss == null)
				ss = new LinkedHashSet<Node>();
			ss.add(nd);
			slots.put(ii, ss);
		}
	}
	
	/**
	 * Rmoves a node from the schedule.
	 * @param nd - The node to be removed
	 */
	public void remove(Node nd) {
		Interval i = nodes.get(nd);
		if (i == null)
			return;
	
		nodes.remove(nd);
		for (int ii = i.lbound; ii <= i.ubound; ii++) {
			slots.get(ii).remove(nd);
		}
	}
	
	/**
	 * Returns the interval the given node is scheduled in.
	 * @param nd - Node of interest
	 * @return Its interval
	 */
	public Interval slot(Node nd) {
		return nodes.get(nd);
	}
	
	/**
	 * Produces a set of nodes scheduled in the given time step
	 * @param slot - The time step of interest
	 * @return A set of nodes scheduled at that time
	 */
	public Set<Node> nodes(int slot) {		
		if (slots.get(slot) == null) return new LinkedHashSet<Node>();
		return slots.get(slot);
	}
	
	/**
	 * Return all currently scheduled nodes.
	 * @return A set of all scheduled nodes
	 */
	public Set<Node> nodes() {
		return nodes.keySet();
	}
	
	/**
	 * Find the schedules length
	 * @return The length of the schedule
	 */
	public int length() {
		int min, max;
		min = Integer.MAX_VALUE;
		max = Integer.MIN_VALUE;
		for (int ii : slots.keySet()) {
			if (ii < min)
				min = ii;
			if (ii > max)
				max = ii;
		}
		return 1 + max - min;
	}
	
	/**
	 * Get the number of nodes currently scheduled.
	 * @return The number of nodes
	 */
	public Integer size() {
		return nodes.keySet().size();
	}
	
	@Override
	public String toString() {
		return slots.toString();
	}
}
