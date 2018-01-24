package javasim.synth.model;

import javasim.synth.model.instruction.AbstractIF;
import javasim.synth.model.instruction.Instruction;

import java.util.*;

import dot.DotGraph;
import dot.DotNode;

/**
 * This represents the loop hierarchy graph.
 *
 * @author Michael Raitza
 * @version – 04.08.2011
 */
public class LoopGraph {

	/* Loops ordered in leftmost outermost order. */
	private TreeSet<Loop> lp_start;
	/* Loops ordered in leftmost innermost order. */
	private TreeSet<Loop> lp_stop;

	/* Comparator yielding the leftmost outermost order of loops */
	private class LpStartComp implements Comparator<Loop> {
		public int compare(Loop l, Loop p) {
			return l.start_addr - p.start_addr;
		}
	}
	/* Comparator yielding the leftmost innermost order of loops */
	private class LpStopComp implements Comparator<Loop> {
		public int compare(Loop l, Loop p) {
			return l.stop_addr - p.stop_addr;
		}
	}

	/**
	 * This represents a single loop in the loop hierarchy graph.
	 */
	public static class Loop {
		private Loop father;
		private Set<Loop> children;
		private DotNode nd;
		private DataGraph dg;

		/** The address of the first instruction concerning the loop. */
		public final Integer start_addr;

		/** The address of the last instruction concerning the loop. */
		public final Integer stop_addr;

		/** The IF* instruction deciding on loop entry. */
		public final AbstractIF ifinstr;

		/**
		 * Constructs a new loop for the loop hierarchy graph.
		 * @param ifinstr the IF* instruction dominating the inner loop instructions
		 * @param start the address of the first instruction concerning the loop
		 * @param stop the address of the last instruction concerning the loop
		 */
		public Loop(Instruction ifinstr, Integer start, Integer stop) {
			this.ifinstr = (AbstractIF)ifinstr;
			start_addr = start;
			stop_addr = stop;
			children = new LinkedHashSet<Loop>();
		}
		/**
		 * Sets the father of this loop.
		 * @param f the father of this loop
		 */
		public void father(Loop f) { father = f; }
		
		/**
		 * Compares the current loop with the loop l	
		 * @param l the loop that the current loop is compared to
		 * @return true if both loops cover the same bytecode sequence. false otherwise
		 */
		public boolean equals(Loop l){
			return (l.start_addr == this.start_addr && l.stop_addr == this.stop_addr);
		}

		/** Returns the father of this loop or NULL if this loop is an outer loop. */
		public Loop father() { return father; }
		public Set<Loop> children(){
			return children;
		}

		/** Returns TRUE iff this loop has no father. */
		public boolean is_top() { return (father == null); }

		/** Returns TRUE iff the given address is in the loop */
		public boolean contains(Integer addr) { return addr >= start_addr && addr < stop_addr; }

		/** Constructs and returns the DOT representation of this loop. */
		public DotNode nd() {
			if (nd == null)
				nd = new DotNode("d" + start_addr, "\"" + ifinstr.i() + " (" + start_addr + ":" + stop_addr + ")\"");
			return nd;
		}

		/** Returns the data graph associated with this loop. */
		public DataGraph dg() { return dg; }

		/** Associates a data graph with this loop. */
		/* TODO: not needed, delete */
		public void dg(DataGraph g) { dg = g; }

		public void child(Loop c) {
			children.add(c);
			c.father(this);
		}

		public boolean inner_p() { return children.size() == 0; }
		public boolean in_child_p(Integer addr) {
			for (Loop lp : children) {
				if (addr > lp.start_addr && addr < lp.stop_addr)
					return true;
			}
			return false;
		}
		public String toString() {
			return "Loop from " + start_addr + " to " + stop_addr + " instr " + ifinstr.attr("label");
		}
	}

	/**
	 * Constructs a new loop hierarchy graph.
	 */
	public LoopGraph() {
		lp_start = new TreeSet<Loop>(new LpStartComp());
		lp_stop = new TreeSet<Loop>(new LpStopComp());
	}

	/**
	 * Inserts a loop into the loop graph maintaining the loop hierarchy.
	 * @param lp the loop to insert.
	 */
	public void insert(Loop lp) {
		TreeSet<Loop> startset = new TreeSet<Loop>(lp_start.headSet(lp));
		TreeSet<Loop> stopset = new TreeSet<Loop>(lp_stop.tailSet(lp));
		TreeSet<Loop> insetstart = new TreeSet<Loop>(lp_start.tailSet(lp));
		TreeSet<Loop> insetstop = new TreeSet<Loop>(lp_stop.headSet(lp));
		startset.retainAll(stopset);
		insetstart.retainAll(insetstop);
		
		if (startset.size() != 0) {
			lp.father(startset.last());
			startset.last().child(lp);
		}
		for (Loop llp : insetstart)
			lp.child(llp);
		lp_start.add(lp);
		lp_stop.add(lp);
	}

	/**
	 * Returns an iterator over the loop hierarchy graph with the loops in leftmost innermost order.
	 */
	public Iterator<Loop> innermost() {
		return lp_stop.iterator();
	}

	public Iterator<Loop> outermost() {
		return lp_start.iterator();
	}

	/**
	 * Returns the string representation of the DOT graph representing
	 * the loop hierarchy graph.
	 */
	public String print_graph() {
		DotGraph g = new DotGraph("loophierarchy");
		if (lp_start.size() == 0)
			return g.toString();
		for (Loop lp : lp_start) {
			if (!lp.is_top())
				lp.father().nd().connect(lp.nd());
			g.connect(lp.nd());
		}
		return g.toString();
	}
	
	public Loop getLoop(int addr){
		Iterator<Loop> it = innermost();
		while(it.hasNext()){
			Loop l = it.next();
			if(l.contains(addr))
				return l;
		}
		return null;
	}
}
