package javasim.synth;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;
import java.util.Formatter;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javasim.synth.model.I;
import javasim.synth.model.instruction.*;
import javasim.synth.model.instruction.AbstractIF;
import javasim.synth.model.instruction.GOTOInstr;
import javasim.synth.model.instruction.Instruction;
import javasim.synth.model.instruction.PHIInstr;
import javasim.synth.model.instruction.StartInstr;

/**
 * The InstrGraph class represents the control flow graph of the byte code
 * snippet to be synthesised. Insertion of nodes into the graph is handled by
 * the specific Instruction objects and should not be manipulated by hand. Thus
 * there's no possibility to remove components.
 * <p>
 * After all instructions are inserted into the graph clean_graph() should be
 * called to remove GOTO instructions and to insert Φ nodes to the corresponding
 * IF* instructions.
 */
public class InstrGraph {

	public int getInstructionCount() {
		return instructions.size() - 3 * phi_nodes.size();
	}

	private Instructions instructions;
	private EdgeSet edges;
	private Instructions gotos;
	private Instructions phi_nodes;
	private OrderedInstr merge_nodes;

	/*
	 * implements a hash set like list from a hash map storing instructions as
	 * keys and values and only storing the first instance of an instruction
	 */
	public class Instructions extends LinkedHashMap<Instruction, Instruction>
			implements Iterable<Instruction> {

		/**
		 * 
		 */
		private static final long serialVersionUID = -8132243773946978074L;

		public boolean add(Instruction i) {
			if (get(i) != null)
				return false;
			put(i, i);
			return true;
		}

		public Iterator<Instruction> iterator() {
			return keySet().iterator();
		}

		public Instruction get(Integer pos) {
			for (Instruction i : this.keySet())
				if (pos.equals(i.addr()))
					return i;
			return null;
		}
	}

	/*
	 * implements an ordered set in the same way Instructions does and thus
	 * basing on a TreeMap.
	 */
	private class OrderedInstr extends TreeMap<Instruction, Instruction>
			implements Iterable<Instruction> {

		/**
		 * 
		 */
		private static final long serialVersionUID = 3067721455514538883L;

		public boolean add(Instruction i) {
			if (get(i) != null)
				return false;
			put(i, i);
			return true;
		}

		public Iterator<Instruction> iterator() {
			return keySet().iterator();
		}
	}

	/* implements a set of bi-directional edges between two nodes */
	private class EdgeSet {
		private InstrEdges<Instruction> succ_edge, pred_edge;

		public EdgeSet() {
			succ_edge = new InstrEdges<Instruction>();
			pred_edge = new InstrEdges<Instruction>();
		}

		public void putEdge(Instruction key, Instruction value) {
			succ_edge.putEdge(key, value);
			pred_edge.putEdge(value, key);
		}

		public void removeEdge(Instruction key, Instruction value) {
			succ_edge.removeEdge(key, value);
			pred_edge.removeEdge(value, key);
		}

		public void remove(Instruction key) {
			succ_edge.remove(key);
			pred_edge.remove(key);
		}

		public LinkedHashSet<Instruction> succ_get(Instruction i) {
			return succ_edge.get(i);
		}
		
		public LinkedHashSet<Instruction> pred_get(Instruction i) {
			return pred_edge.get(i);
		}

		public InstrEdges<Instruction> succ_set() {
			return succ_edge;
		}

		public InstrEdges<Instruction> pred_set() {
			return pred_edge;
		}
	}

	/**
	 * Constructs a new graph.
	 */
	public InstrGraph() {
		instructions = new Instructions();
		edges = new EdgeSet();
		gotos = new Instructions();
		phi_nodes = new Instructions();
		merge_nodes = new OrderedInstr();
//		dbg = IOManager.get().getf("simdbg");
	}

	/**
	 * Inserts the start instruction into the graph.
	 * 
	 * @param i
	 *            the StartInstr object representing the start instruction
	 * @return The start instruction living in the graph, which need not be the
	 *         same as the one given in <code>i</code> if another instance of
	 *         the same instruction was tried to be inserted.
	 */
	public Instruction insert(StartInstr i) throws IllegalArgumentException {
		if (i == null)
			throw new IllegalArgumentException("No element to insert");

		instructions.add(i);
		i = (StartInstr) instructions.get(i);
		return i;
	}

	/**
	 * Inserts a new instruction into the graph and connects it to the old
	 * instruction.
	 * 
	 * @param pre
	 *            the old intruction
	 * @param i
	 *            the new instruction
	 * @return The instruction given in <code>i</code> living in the graph,
	 *         which need not be the same as the one given in <code>i</code> if
	 *         another instance of the same instruction was tried to be
	 *         inserted.
	 */
	public Instruction insert(Instruction pre, Instruction i)
			throws IllegalArgumentException {
		if (i == null)
			throw new IllegalArgumentException("No element to insert");

		if (pre == null) {
			throw new IllegalArgumentException("No predecessor to"
					+ " attach new instruction to");
		}
		instructions.add(i);

		i = instructions.get(i);
		pre = instructions.get(pre);

		edges.putEdge(pre, i);
		if (edges.pred_set().get(i).size() > 1)
			merge_nodes.add(i);

		return i;
	}

	public LinkedHashSet<Instruction> successors(Instruction i) {
		return edges.succ_get(i);
	}
	
	public LinkedHashSet<Instruction> predecessors(Instruction i){
		return edges.pred_get(i);
	}

	/**
	 * Registers a new goto statement. These are cleaned up afterwards.
	 * 
	 * @param gt
	 *            the goto statement to be registered
	 */
	public void reg_goto(GOTOInstr gt) {
		gotos.add(gt);
	}

	/**
	 * Registers a new Φ node. These are to be inserted to close the control
	 * flow in the graph.
	 * 
	 * @param phn
	 *            the Φ node to be inserted.
	 */
	public void reg_phi(Instruction phn) {
		phi_nodes.add(phn);
	}
	
	public Instructions phi_nodes(){
		return phi_nodes;
	}
	
	/**
	 * Java compiler makes use of Short circuit evaluation and creates instruction graph structures that are not mergable. the merger node has only two predecessors with different 
	 * branchpoints - so wie duplicate this merger node for each branchpoint 
	 * @param syn the current synthesis context
	 */
	private void restructureShortCircuitEvaluation(Instruction mn){
		//TODO maybe we have to reiterate a few times - has to be checked
		if(mn.i()!=I.SYNTH_STOP){
			LinkedHashSet<Instruction> preds = edges.pred_set().get(mn);
			if(preds.size() != 2){
				return;
			} else {
				Iterator<Instruction> it = preds.iterator();
				Instruction p1 = it.next();
				Instruction p2 = it.next();
				if(p1.branchpoint().equals(p2.branchpoint()) || !(p1.i() == I.SYNTH_DUMMY)&&(p2.i() == I.SYNTH_DUMMY) || !(p2.i() == I.SYNTH_DUMMY)&&(p1.i() == I.SYNTH_DUMMY)){
					return;
				}else{
					mn.branchpoint(p1.branchpoint());
					mn.decision(p1.decision());
					Instruction dupl =null;

					try {
						Constructor<Instruction> cc = mn.i().getCL().getConstructor(I.class, Integer.class);
						dupl = cc.newInstance(mn.i(), mn.addr());
					}catch(NoSuchMethodException e){
						System.out.println("EEEEEEEEEEEEEEEERRROR: "+e.getMessage());
					} catch (IllegalArgumentException e) {
						System.out.println("EEEEEEEEEEEEEEEERRROR: "+e.getMessage());
					} catch (InstantiationException e) {
						System.out.println("EEEEEEEEEEEEEEEERRROR: "+e.getMessage());
					} catch (IllegalAccessException e) {
						System.out.println("EEEEEEEEEEEEEEEERRROR: "+e.getMessage());
					} catch (InvocationTargetException e) {
						System.out.println("EEEEEEEEEEEEEEEERRROR: "+e.getMessage());
					}
					dupl.isDuplicate();
					dupl.branchpoint(p2.branchpoint());
					dupl.decision(p2.decision());
					instructions.add(dupl);
					for(Instruction succ: edges.succ_get(mn)){
						edges.putEdge(dupl, succ);
						restructureShortCircuitEvaluation(succ);
					}

					edges.removeEdge(p2, mn);
					edges.putEdge(p2, dupl);

				}
			}
		}

	}
	
	private class InstCMP implements Comparator<Instruction> {

		@Override
		public int compare(Instruction o1, Instruction o2) {
			Instruction i1 = (Instruction)o1;
			Instruction i2 = (Instruction)o2;
			return Integer.compare(i1.branchpoint().ifinstr().addr(), i2.branchpoint().ifinstr().addr());
		}

		
		
	}

	/**
	 * Merges a set of branches connecting to a merge node. This function
	 * achieves this by constructing a Map of the Instructions preceding the
	 * merger node <code>mn</code> as values V and their corresponding branch
	 * points (which in fact are the Φ nodes of the IF they belong to) as keys
	 * K.
	 * <p>
	 * By constructing this map a fit map is also constructed holding the
	 * mergeable instances of pairs of edges. As long as there are mergeable
	 * instances we merge them. The branches at this node could be merge
	 * correctly iff the fit map is empty and the merger node <code>mn</code>
	 * has exactly one predecessor.
	 * 
	 * @param mn
	 *            the merge node the branches connect to
	 * @return TRUE iff the branches could be merged correctly, FALSE otherwise
	 */
	private boolean merge_branch(Instruction mn) {
		LinkedHashSet<Instruction> i_set = edges.pred_set().get(mn);
		InstrEdges<Instruction> fit_map = new InstrEdges<Instruction>();
		InstrEdges<Instruction> bp_map = new InstrEdges<Instruction>();
		Instruction mp1, mp2;
		Instruction mp;
		int counter = 0;
		
		
		
		for (Instruction i : i_set) {
			counter++;
			if (bp_map.putEdge(i.branchpoint(), i) != null)
				fit_map.put(i.branchpoint(), bp_map.get(i.branchpoint()));
		}
		
		while(i_set.size() >= 2 && fit_map.size() == 0){

			
			TreeSet<Instruction> sorted_preds = new TreeSet<Instruction>(new  InstCMP());
			sorted_preds.addAll(i_set);
			
			
			mp1 = sorted_preds.pollFirst();
			mp2 = sorted_preds.pollFirst();
			i_set.remove(mp1);
			i_set.remove(mp2);
			
			boolean mn_decision;
			boolean ctrl_decision;
			
			PHIInstr phi_node1 = mp1.branchpoint();
			PHIInstr phi_node2 = mp2.branchpoint();
			PHIInstr phi_node;
			PHIInstr phi_node_later; 
			if(phi_node1.ifinstr().addr()>phi_node2.ifinstr().addr()){
				phi_node = phi_node2;
				ctrl_decision = mp2.decision();
				phi_node_later = phi_node1;
				mn_decision  = mp1.decision();
			} else {
				phi_node = phi_node1;
				ctrl_decision = mp1.decision();
				phi_node_later = phi_node2;
				mn_decision  = mp2.decision();
			}
			
			phi_node_later.ifinstr().setShortcircuitevaluation(phi_node,mn_decision, ctrl_decision);
			
			
			edges.removeEdge(mp2, mn);
			edges.removeEdge(mp1, mn);

			/* insert Φ node */
			instructions.add(phi_node);
			edges.putEdge(phi_node, mn);
			edges.putEdge(mp1, phi_node);
			edges.putEdge(mp2, phi_node);
			
			mn.branchpoint(phi_node_later);
			mn.decision(mn_decision);
			
			phi_node_later.branchpoint2(phi_node.branchpoint(),phi_node.decision());
			
			phi_node.branchpoint(mn.branchpoint());
			phi_node.decision(mn.decision());
			
			sorted_preds.add(phi_node);	
			
			
			
			propagate_bp(mn);
			
			if(i_set.size()==0)
				return true;
			
		}
		

		while (fit_map.size() > 0) {
			mp = fit_map.keySet().iterator().next(); // next fit element
			counter++;

			Iterator<Instruction> mp_it = bp_map.get(mp).iterator();
			mp1 = mp_it.next();
			mp2 = mp_it.next();
			
			Instruction phi_node = mp2.branchpoint();
			edges.removeEdge(mp2, mn);
			edges.removeEdge(mp1, mn);

			/* insert Φ node */
			instructions.add(phi_node);
			edges.putEdge(phi_node, mn);
			edges.putEdge(mp1, phi_node);
			edges.putEdge(mp2, phi_node);
			mn.branchpoint(phi_node.branchpoint());
			mn.decision(phi_node.decision());

			/* update maps */
			fit_map.remove(mp);
			if (bp_map.putEdge(mp.branchpoint(), mp) != null)
				fit_map.put(mp.branchpoint(), bp_map.get(mp.branchpoint()));
		}
		propagate_bp(mn);

		if (i_set.size() != 1){
			return false;
		}

		return true;
	}

	/* propagate the branching and decision information to subsequent nodes */
	private void propagate_bp(Instruction i) {
		Instruction s;

		if ((edges.succ_set().get(i) == null)
				|| (edges.succ_set().get(i).size() == 0))
			return;

		s = edges.succ_set().get(i).iterator().next();

		while (edges.pred_set().get(s) != null
				&& edges.pred_set().get(s).size() == 1) {
			if (s == null)
				System.out.println(" s null");
			else if (s.branchpoint() == null)
				System.out.println(" branchpoint null");
			if (i.branchpoint() == null)
				System.out.println(" i branchpoint null");
			if (s.branchpoint().equals(i.branchpoint())){
				break;
			}
				
			s.branchpoint(i.branchpoint());
			s.decision(i.decision());

			if (edges.succ_set().get(s).size() != 1) {
				s = ((AbstractIF) s).phi_node();
				s.branchpoint(i.branchpoint());
				s.decision(i.decision());
				s.relabel();
				break;
			} else
				s = edges.succ_set().get(s).iterator().next();
		}
	}

	/**
	 * Returns TRUE iff the instruction given in <code>i</code> is already
	 * contained in the instruction graph.
	 * 
	 * @param i
	 *            the instruction to test against the contains relation.
	 * @return TRUE iff the instruction is in the instruction graph, FALSE
	 *         otherwise.
	 */
	public boolean contains(Instruction i) {
		return instructions.containsKey(i);
	}

	/**
	 * Modifies the graph structure by inserting the collected Φ nodes that
	 * complement the IF* nodes into the graph. This resolves, if possible, any
	 * spots where nodes have more than two predecessors.
	 * <p>
	 * This function achieves this by constructing a Map of the Instructions
	 * preceding the merger node <code>mn</code> as values V and their
	 * corresponding branch points (which in fact are the Φ nodes of the IF they
	 * belong to) as keys K.
	 * <p>
	 * By constructing this map a fit map is also constructed holding the
	 * mergeable instances of pairs of edges. As long as there are mergeable
	 * instances it merges them. The branches at this node could be merge
	 * correctly iff the fit map is empty and the merger node <code>mn</code>
	 * has exactly one predecessor.
	 * 
	 * see merge_nodes in the code
	 */
	public void restructure_graph(SynthData syn)
			throws SequenceNotSynthesizeableException {
		
		for (Instruction i : merge_nodes) {
			if (!merge_branch(i)){
				System.out.println("NOTMERGING: "+i);
				throw new SequenceNotSynthesizeableException(
						"Could not merge branches "+ syn.start_addr()+ " "+ syn.stop_addr());
			}
		}
		
		// This stuff is to correct the while loop bug
		Set<Instruction> allGotos = new LinkedHashSet<Instruction>(gotos.keySet());
		LinkedHashMap<Integer,Instruction> validGotos = new LinkedHashMap<Integer,Instruction>();
		for(Instruction gt: allGotos){
			Integer jumpTarget = syn.code_w(gt.addr()+1)+gt.addr();
			if(!validGotos.containsKey(jumpTarget)){
				validGotos.put(jumpTarget, gt);
			}else if(jumpTarget < gt.addr()) {
				Instruction alreadyKnown = validGotos.get(jumpTarget);
				Instruction realLoop, minorLoop;
				if(alreadyKnown.addr()<gt.addr()){
					validGotos.put(jumpTarget, gt);
					realLoop = gt;
					minorLoop = alreadyKnown;
				}else{
					realLoop = alreadyKnown;
					minorLoop = gt;					
				}
				
				/*phi node twiddeling*/
				Instruction phi = edges.succ_get(minorLoop).iterator().next();
//				System.out.println("phi: " + phi);
				Iterator<Instruction> it = edges.pred_get(phi).iterator();
				Instruction dummy = it.next();
				if(dummy == minorLoop)
					dummy = it.next();
				Instruction minorPred = edges.pred_get(minorLoop).iterator().next();
				Instruction phiSucc = edges.succ_get(phi).iterator().next();
				Instruction realPred = edges.pred_get(realLoop).iterator().next();
				
				edges.removeEdge(minorPred, minorLoop);
				edges.removeEdge(minorLoop, phi);
				edges.putEdge(minorPred, phi);
				edges.remove(minorLoop);
				gotos.remove(minorLoop);
				
				edges.removeEdge(dummy, phi);
				edges.removeEdge(phi, phiSucc);
				edges.putEdge(dummy, phiSucc);
				
				edges.removeEdge(realPred, realLoop);
				
				edges.putEdge(realPred, phi);
				edges.putEdge(phi, realLoop);
				
				
				do{
					if(realPred.branchpoint() == phiSucc.branchpoint()){ //means that there are no other branches on the way
						realPred.decision(!minorLoop.decision());
						realPred.branchpoint(minorLoop.branchpoint());
					}
					realPred = edges.pred_get(realPred).iterator().next();
				}while(realPred != dummy);
				
			}
		}
		
		TreeSet<Instruction> sortedGotos = new TreeSet<Instruction>();
		sortedGotos.addAll(gotos.keySet());
		
		Iterator<Instruction> it = sortedGotos.descendingIterator();
		
		while(it.hasNext()){
			Instruction i = it.next();
			((GOTOInstr) i).profile(syn);
		}
	}

	/**
	 * Cleans the graph from GOTO nodes. This code runs in linear time in the
	 * number of GOTO nodes.
	 * <p>
	 * N.b. <em>maybe</em> the GOTO cleanup can be done while constructing the
	 * graph, but I'm too lazy to think about the implications this has on graph
	 * generation code as up to now there are no assumptions about the node
	 * traversal order when generating the instruction graph.
	 */
	public void clean_graph() {

		for (Instruction i : gotos) {
			for (Instruction out : edges.succ_set().get(i)) {
				for (Instruction in : edges.pred_set().get(i)) {
					edges.putEdge(in, out);
					edges.succ_set().removeEdge(in, i);
				}
				edges.pred_set().removeEdge(out, i);
			}
			edges.remove(i);
			instructions.remove(i);
		}
	}

	/**
	 * For debugging purposes.
	 */
	public String print_instr() {
		Formatter f = new Formatter();
		try {
			f.format("\n\nINSTRUCTIONS\n\n");
			for (Instruction i : instructions) {
				f.format("\n%d:%s\n", i.addr(), i.i());
			}
			return f.toString();
		} finally {
			f.close();
		}
	}

	/**
	 * Returns the dot graph that represents the current configuration.
	 * 
	 * @return the string representation of the dot graph.
	 * @see javasim.application.DotGraph
	 */
	public String print_graph(boolean [] isPrefetch) {
		StringBuffer ret = new StringBuffer("digraph{\n");

		for (Instruction i : instructions) {
			if (edges.succ_get(i) == null)
				continue;
			
			if(i.addr() < isPrefetch.length &&  i.addr() > 0 && isPrefetch[i.addr()]){
				ret.append("\""+i+"\" [color = red];\n");
			}
			
			
			for (Instruction out : edges.succ_get(i)) {
				ret.append("\""+i+"\" -> \""+out+"\";\n");
			}
		}
		ret.append("}");

		return ret.toString();
	}

	/**
	 * Returns the instruction to be found at the given address. Watch out, this
	 * operation takes linear time in the number of instructions!
	 * 
	 * @param addr
	 *            the address to lookup the instruction at
	 * @return the instruction to be found at the given address
	 */
	public Instruction get(Integer addr) {
		return instructions.get(addr);
	}

}
