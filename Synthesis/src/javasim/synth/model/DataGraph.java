package javasim.synth.model;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

import functions.HashCalculator;
import javasim.synth.DEdge;
import javasim.synth.DataEdges;
import javasim.synth.InstrEdges;
import javasim.synth.SynthData;
import javasim.synth.model.LoopGraph.Loop;
import javasim.synth.model.datum.ConstDatum;
import javasim.synth.model.datum.Datum;
import javasim.synth.model.datum.LReadDatum;
import javasim.synth.model.datum.LWriteDatum;
import javasim.synth.model.datum.MergerDatum;
import javasim.synth.model.instruction.PHIInstr;

/**
 * The data flow graph.
 * 
 * @author Michael Raitza
 * @version 14.04.2011
 */
public class DataGraph {

	/**
	 * All nodes in the graph
	 */
	private LinkedHashSet<Datum> nodes;
	
	
	/**
	 * All 32Bit constants
	 */
	private LinkedHashMap<Integer,Datum> constants;
	
	/**
	 * All 64Bit constants
	 */
	private LinkedHashMap<Long,Datum> constantsLong;
	
	/**
	 * All nodes in the graph that are also operations (like IADD)
	 */
	private LinkedHashSet<Datum> ops;
	
	/** 
	 * Data flow dependencies
	 */
	private DataEdges succ_edges;
	
	/**
	 * Data flow dependencies
	 */
	private DataEdges pred_edges;
	
	/**
	 * Control flow dependencies
	 */
	private InstrEdges<Datum> succ_sedges;
	
	/**
	 * Control flow dependencies
	 */
	private InstrEdges<Datum> pred_sedges;
	
	/**
	 * Stores all post increment couples (originate for example from: if( a[i++] == 4){...} )
	 * In those cases the dependencies are inverted:
	 * old version(s) of variable i is still on the stack while the new value is already stored in the local variable memory
	 */
	private LinkedHashMap<Datum, Datum> postIncrementCouple;


	/**
	 * Helper Class to describe an Edge from a Datum to another.
	 **/
	public static class Edge {
		public final Datum from;
		public final Datum dest;

		public Edge(Datum f, Datum d) {
			from = f;
			dest = d;
		}

		public boolean equals(Object ob) {
			Edge e = (Edge) ob;
			if (e.from.equals(from) && e.dest.equals(dest))
				return true;
			else
				return false;
		}

		public int hashCode() {
			return HashCalculator.hashCode(from.hashCode(), dest.hashCode());
		}
	}

	/**
	 * Creates a new data flow graph.
	 */
	public DataGraph() {
		nodes = new LinkedHashSet<Datum>();
		ops = new LinkedHashSet<Datum>();
		constants = new LinkedHashMap<>();
		constantsLong = new LinkedHashMap<>();
		succ_edges = new DataEdges();
		pred_edges = new DataEdges();
		succ_sedges = new InstrEdges<Datum>();
		pred_sedges = new InstrEdges<Datum>();
		postIncrementCouple = new LinkedHashMap<Datum, Datum>();
	}

	/**
	 * Adds a datum node to the graph.
	 * 
	 * @param d
	 *            the node to add
	 */
	public void add_node(Datum d) {
		nodes.add(d);
	}

	/**
	 * Adds a datum node, that represents a piece of hardware to the graph.
	 * 
	 * @param d
	 *            the node to add
	 */
	public void add_op(Datum d) {
		nodes.add(d);
		ops.add(d);
		d.attr("shape", "box");
	}
	
	public void remove_op(Datum d){
		ops.remove(d);
		d.attr("shape", "ellipse");
	}

	/**
	 * Adds edges from <code>d</code> to all nodes referenced in <code>ld</code>
	 * to the set of data flow edges.
	 * 
	 * @param ld
	 *            a list of outward nodes to connect <code>d</code> to
	 * @param d
	 *            the inward node to connect
	 */
	public void connect(LinkedList<Datum> ld, Datum d) {
		if (ld != null) {
			add_edge(ld.peek(), d);
		}
	}

	/**
	 * Adds edges from <code>d</code> to all nodes referenced in <code>ld</code>
	 * to the set of scheduling dependences.
	 * 
	 * @param ld
	 *            a list of outward nodes to connect <code>d</code> to
	 * @param d
	 *            the inward node to connect
	 */
	public void sconnect(LinkedList<Datum> ld, Datum d) {
		if (ld == null)
			return;
		add_sedge(ld.peek(), d);
	}

	/**
	 * Adds a data flow edge from <code>pre</code> to <code>d</code>.
	 * 
	 * @param pre
	 *            the outward node of the edge
	 * @param d
	 *            the inward node of the edge
	 */
	public void add_edge(Datum pre, Datum d) {
		if (pre == null)
			return;
		add_simple_edge(pre, d);
	}

	/**
	 * Adds a data flow edge from <code>pre</code> to <code>d</code>.
	 * 
	 * @param pre
	 *            the outward node of the edge
	 * @param d
	 *            the inward node of the edge
	 * @param s
	 *            the attribute of the edge
	 */
	public void add_edge(Datum pre, Datum d, Integer s) {
		if (pre == null)
			return;
		add_simple_edge(pre, d, s);
	}

	/**
	 * Adds a scheduling dependence edge from <code>pre</code> to <code>d</code>
	 * .
	 * 
	 * @param pre
	 *            the outward node of the edge
	 * @param d
	 *            the inward node of the edge
	 */
	public void add_sedge(Datum pre, Datum d) {
		if (pre == null)
			return;
		add_simple_sedge(pre, d);
	}

	/**
	 * Adds a data flow edge from <code>pre</code> to <code>d</code>.
	 * 
	 * @param pre
	 *            the outward node of the edge
	 * @param d
	 *            the inward node of the edge
	 */
	public void add_simple_edge(Datum pre, Datum d) {
		succ_edges.put(pre, d, 1);
		pred_edges.put(d, pre, 1);
		d.depth(pre.depth());
		
		Datum postInc = postIncrementCouple.get(pre);
		if(postInc != null && d != null){
			if(!(d instanceof MergerDatum))
				add_simple_sedge(d, postInc);
			postIncrementCouple.remove(pre);
		}
	}

	/**
	 * Adds a data flow edge from <code>pre</code> to <code>d</code>.
	 * 
	 * @param pre
	 *            the outward node of the edge
	 * @param d
	 *            the inward node of the edge
	 * @param s
	 *            the attribute of the edge
	 */
	public void add_simple_edge(Datum pre, Datum d, Integer s) {
		succ_edges.put(pre, d, s);
		pred_edges.put(d, pre, s);
		d.depth(pre.depth());
		
		Datum postInc = postIncrementCouple.get(pre);
		if(postInc != null && d != null){
			if(!(d instanceof MergerDatum))
				add_simple_sedge(d, postInc);
			postIncrementCouple.remove(pre);
		}
	}

	/**
	 * Adds a control flow edge from <code>pre</code> to <code>d</code>
	 * .
	 * 
	 * @param pre
	 *            the outward node of the edge
	 * @param d
	 *            the inward node of the edge
	 */
	public void add_simple_sedge(Datum pre, Datum d) {
		
		succ_sedges.putEdge(pre, d);
		pred_sedges.putEdge(d, pre);
		d.depth(pre.depth());
	}
	
	/**
	 * Removes a dataflow edge
	 * @param pre
	 * @param d
	 * @return
	 */
	public boolean remove_simple_edge(Datum pre, Datum d){
		DEdge delete = null;
		boolean erg = true;
		for(DEdge de : this.preds(d)){
			if( de.sink == pre){
				delete = de;
				break;
			}
		}
		if(delete != null){
			erg &= preds(d).remove(delete);
			delete = null;
		}
		for(DEdge de : this.succs(pre)){
			if( de.sink == d){
				delete = de;
				break;
			}
		}
		if(delete != null ){
			erg &= succs(pre).remove(delete);
		}
		return erg;
	}
	
	/**
	 * Remove a control flow edge
	 * @param pre
	 * @param d
	 * @return
	 */
	public boolean remove_simple_sedge(Datum pre, Datum d){
		boolean erg = this.succ_sedges.removeEdge(pre, d);
		erg &= this.pred_sedges.removeEdge(d, pre);
		return erg;
	}
	
	
	/**
	 * Folds away the unnecessary store instructions. They are unnecessary when they are no loop (or branch) entry or exit
	 */
	public void foldLVMemInstructions(SynthData data){

		if(data.getAllLVStores().isEmpty()){
			return;
		}
		
		// Get the virtual stack of the whole kernel
		VStack vstack = data.stop().vstack();

		LWriteDatum [] lwrites = new LWriteDatum[1];
		lwrites = data.getAllLVStores().toArray(lwrites);
		Arrays.sort(lwrites, new DatumComparator());	// Sort according to the address of the creator instruction

		for(LWriteDatum localVariableWrite: lwrites){
			Datum source = data.dg().preds(localVariableWrite).iterator().next().sink;
			LinkedHashSet<Datum> toDelete = new LinkedHashSet<Datum>();
			LinkedHashSet<Datum> toDeletePre = new LinkedHashSet<Datum>();
			if(!(((source instanceof LReadDatum)||(source instanceof LWriteDatum))&& !source.value().equals(localVariableWrite.value()))){//Die hintere bedingung entspricht quasi blocking assignment
				//TODO die bedingung ist glabue ich unnötig - war nur bei dem alten bekakkten CGRA model nötig...
				
				
				if(succ_edges.get(localVariableWrite) != null){ 
					for(DEdge de: succ_edges.get(localVariableWrite)){
						Loop lwrLoop = data.lg().getLoop(localVariableWrite.creator().addr()); 
						Datum psdf = de.sink;
						if(!(de.sink instanceof MergerDatum) && lwrLoop.equals(data.lg().getLoop(de.sink.creator().addr()))){
							boolean fold = false;
							PHIInstr iiinst = de.sink.creator().branchpoint();
							Boolean decision = de.sink.creator().decision();
							do { // wirklich nur folden, wenn im gleichen branch - könnte man noch verbessern TODO (ist aber eher selten der fall)
								if(iiinst.equals(source.creator().branchpoint()) && decision == source.creator().decision() || source.creator().branchpoint() == null){
									fold = true;
									break;
								}else{
									decision = iiinst.decision();
									iiinst = iiinst.ifinstr().branchpoint();

								}
							} while (iiinst != null);


							if(fold){
								add_edge(source, de.sink, de.attr);
								toDelete.add(de.sink);
								toDeletePre.add(source);
							}else{
								localVariableWrite.defineAsNecessary();
							}


						}
					}
					for(Datum d: toDelete){
						remove_simple_edge(localVariableWrite, d);
					}
					if(!vstack.isLastAccesToLV(localVariableWrite) && !localVariableWrite.isNecessary()){// Delete all unnecessary stores except the last one
						// necessary stores are the ones before a branch (or loop) or at the end of a branch (or loop)
						remove_op(localVariableWrite);
						nodes.remove(localVariableWrite);
						for(Datum d: toDeletePre){
							remove_simple_edge(d,localVariableWrite);
						}

						LinkedHashSet<Datum> preds = pred_sedges.get(localVariableWrite);
						LinkedHashSet<Datum> succs = succ_sedges.get(localVariableWrite);

						for(Datum s: succs){
							for(Datum p: preds){
								add_simple_sedge(p, s);
							}
						}


					} 
				} else {
					if(!vstack.isLastAccesToLV(localVariableWrite) && !localVariableWrite.isNecessary()){// Delete all unnecessary stores except the last one
						// necessary stores are the ones before a branch (or loop) or at the end of a branch (or loop)
						remove_op(localVariableWrite);
						nodes.remove(localVariableWrite);
						for(Datum d: toDeletePre){
							remove_simple_edge(d,localVariableWrite);
						}

						LinkedHashSet<Datum> preds = pred_sedges.get(localVariableWrite);
						LinkedHashSet<Datum> succs = succ_sedges.get(localVariableWrite);

						for(Datum s: succs){
							for(Datum p: preds){
								add_simple_sedge(p, s);
							}
						}


					} 
				}
			}
		}
	}
	

	/**
	 * Cleans up merger nodes that are not read and so deliver no information.
	 */
	public void cleanup() {
		boolean modified = true;
		while (modified) {
			Iterator<Datum> dit = nodes.iterator();
			modified = false;
			while (dit.hasNext()) {
				Datum d = dit.next();
				if (d.type() == Datum.Type.MERGER || d.type() == Datum.Type.PIPE) {						// if there are no successors the nodes can be deleted
					if((succ_edges.get(d) == null || succ_edges.get(d).size() == 0)&& succ_sedges.get(d)!=null) { 	//if there's a node with no data dependencies but only control dependencies, the dependencies can be transferred to the predecessors. in the next step this node will be deleted
						for(Datum succ: succ_sedges.get(d)){
							if (pred_edges.get(d) != null)
								for (DEdge pd: pred_edges.get(d)){
									add_simple_sedge(pd.sink, succ);
								}
							if (pred_sedges.get(d) != null)
								for (Datum pd : pred_sedges.get(d)){
									add_simple_sedge(pd, succ);
								}
						}
						succ_sedges.remove(d);
						
						modified = true;
					}
					if ((succ_edges.get(d) == null || succ_edges.get(d).size() == 0) && (succ_sedges.get(d) == null || succ_sedges.get(d).size() == 0)) {
						if (pred_edges.get(d) != null)
							for (DEdge pd : pred_edges.get(d))
								succ_edges.remove(pd.sink, d);									// we only need to delete the succ_edges bc the function deps (below) only goes through succ_edges
						if (pred_sedges.get(d) != null)
							for (Datum pd : pred_sedges.get(d))
								succ_sedges.removeEdge(pd, d);
						modified = true;
						dit.remove();
						ops.remove(d);
					} 
				}
			}
		}
	}

	/**
	 * Returns the set of predecessing nodes from the set of data flow edges, if
	 * any.
	 * 
	 * @return the set of nodes preceding this node
	 */
	public Set<DEdge> preds(Datum d) {
		return pred_edges.get(d);
	}
	
	/**
	 * Return the set of predecessing nodes from the set of control flow edges if any
	 * @param d Datum whose predecessors are returned
	 * @return A set of Datums that contains all predecessors of d 
	 */
	public Set<Datum> preds_s(Datum d){
		return pred_sedges.get(d);
	}
	
	
	/**
	 * Collects all real predecessors (leaf nodes of the muxtree) of a Datum in the given set
	 * @param d the Datum
	 * @param realPreds	 the set that collects the predecessors
	 */
	public void realPreds(Datum d, Set<Datum> realPreds){
		for(DEdge de: preds(d)){
			if(de.sink.type().equals(Datum.Type.MERGER) || de.sink.type().equals(Datum.Type.PIPE)){
				realPreds(de.sink, realPreds);
			}else {
				realPreds.add(de.sink);
			}
		}
	}

	/**
	 * Returns the set of successing nodes from the set data flow edges, if any.
	 * 
	 * @return the set of nodes following this node
	 */
	public Set<DEdge> succs(Datum d) {
		return succ_edges.get(d);
	}
	
	public Set<Datum> succs_s(Datum d) {
		return succ_sedges.get(d);
	}

	public LinkedHashSet<Datum> nodes() {
		return nodes;
	}
	
	public LinkedHashSet<Datum> getOps(){
		return ops;
	}

	
	/**
	 * Reorders chains of associative operations: a + b + c + d [Three steps] -> (a + b ) + ( c + d ) [two steps]
	 */
	public void reorderChains() {
		LinkedHashMap<Datum, TreeSet<ChainElement>> chains = new LinkedHashMap<Datum,TreeSet<ChainElement>>();
		
		TreeSet<Datum> opsSorted = new TreeSet<Datum>();
		opsSorted.addAll(ops);
		
		
		//////////////////////////////////////////////////////////////////////////////////
		// Find the chains																//
		//////////////////////////////////////////////////////////////////////////////////
		for(Datum op: opsSorted){
			if( op.creator().i() == I.IOR ||  op.creator().i() == I.IAND || op.creator().i() == I.IADD ||  op.creator().i() == I.IMUL|| op.creator().i() == I.FADD){ // associative operations
				for(DEdge predE: this.preds(op)){
					Datum pred = predE.sink;
					// Find a chain of two same operations where the predecessor of op has only thee op iteself as successor.
					// If there are more than one successors this means that the exact result of that op is needed and reodering is not allowed
					if(pred.creator().i() == op.creator().i() && this.succs(pred).size()==1 ){
						// The predecessor is already part of a chain, we extend the chain
						if(chains.containsKey(pred)){
							TreeSet<ChainElement> c = chains.get(pred);
							c.add(new ChainElement(op, predE.attr));
							chains.remove(pred);
							chains.put(op, c);
						}else{
							//otherwise wie create a new chain
							TreeSet<ChainElement> c = new TreeSet<ChainElement>();
							c.add(new ChainElement(op, predE.attr));
							c.add(new ChainElement(pred, null));
							chains.put(op, c);
						}
						
						break;
					}
				}
				
			}
		}
		
		
		
		
		//////////////////////////////////////////////////////////////////////////////////
		// Reorder the chains															//
		//////////////////////////////////////////////////////////////////////////////////
		for(Datum head: chains.keySet()){
			if(chains.get(head).size()<3) // reordering chains of length 3 doesn't change anything
				continue;
			TreeSet<ChainElement> allThatAreNotYetPlaced = chains.get(head);                  // all who are not placed
			TreeSet<ChainElement> allThathaveNoOperandsYet = new TreeSet<ChainElement>(allThatAreNotYetPlaced); // all whose preds are not placed 
			TreeSet<Datum> leafOperands = new TreeSet<Datum>();
			
			// Delete old connections
			for(ChainElement ce: allThatAreNotYetPlaced){
				LinkedHashSet<Datum> toDelete = new LinkedHashSet<Datum>();
				for(DEdge de: this.preds(ce.d)){
					if(de.attr != ce.opAttribute){
						leafOperands.add(de.sink);  //find ops
					}
					toDelete.add(de.sink);
				}
				for(Datum pre: toDelete){
					this.remove_simple_edge(pre, ce.d); 
				}
				
			}
			
			// set new connections 
			allThatAreNotYetPlaced.pollFirst(); // the First one is placed directly (and has no operands yet)
			while(allThathaveNoOperandsYet.size()!=0){
				ChainElement hasNoOperandsYet = allThathaveNoOperandsYet.pollFirst();
				// Operands are either chain elements that are not yet placed or leaf operands if all chain elements are placed
				// By assigning chain elements as operands they are placed
				
				if(allThatAreNotYetPlaced.size()!=0){
					this.add_simple_edge(allThatAreNotYetPlaced.pollFirst().d, hasNoOperandsYet.d, 1);
				} else{
					this.add_simple_edge(leafOperands.pollFirst(), hasNoOperandsYet.d, 1);
				}
				if(allThatAreNotYetPlaced.size()!=0){
					this.add_simple_edge(allThatAreNotYetPlaced.pollFirst().d, hasNoOperandsYet.d, 2);
				} else{
					this.add_simple_edge(leafOperands.pollFirst(), hasNoOperandsYet.d, 2);
				}
			}
		}
		
	}
	
	/**
	 * Holds an operation of a chain that can be reorderd
	 * @author jung
	 *
	 */
	private class ChainElement implements Comparable<ChainElement>{
		public Datum d;
		public Integer opAttribute = null;
		
		public ChainElement(Datum d, Integer opAttribute){
			this.d = d;
			this.opAttribute = opAttribute;
		}

		public int compareTo(ChainElement o) {
			return -this.d.compareTo(o.d);
		}
		
		
	}
	
	
	public boolean areDependent(Datum first, Datum second){
		boolean returnValue = false;
		LinkedHashSet<Datum> succs = new LinkedHashSet<Datum>();
		LinkedHashSet<Datum> newSuccs;
		if(succs_s(first) != null){
			succs.addAll(succs_s(first));
		}
		if(succs(first) != null){
			for(DEdge de: succs(first)){
				succs.add(de.sink);
			}
		}
		while(succs.size() != 0){
			newSuccs = new LinkedHashSet<>();
			for(Datum successor: succs){
				if(successor.equals(second)){
					return true;
				}
				if(succs_s(successor) != null){
					newSuccs.addAll(succs_s(successor));
				}
				if(succs(successor) != null){
					for(DEdge de: succs(successor)){
						newSuccs.add(de.sink);
					}
				}
			}
			
			succs = newSuccs;
			
			
		}
		
		
		
		return returnValue;
	}
	
	
	/**
	 * Adds a post increment couple to the list
	 * @param inc
	 * @param load
	 */
	public void addPostIncrementCouple(Datum inc, Datum load){
		postIncrementCouple.put(load, inc);
	}
	
	public Datum getConstant(Datum constant){
		if(!constant.creator().i().wdata()){
			int value = constant.value().intValue();
			if(constants.containsKey(value)){
				return constants.get(value);
			} else {
				constants.put(value, constant);
				return constant;
			}
		} else {
			long value = constant.value().longValue();
			if(constantsLong.containsKey(value)){
				return constantsLong.get(value);
			} else {
				constantsLong.put(value, constant);
				return constant;
			}
		}
	}
	
	
	private LinkedHashMap<Datum,Integer> ctiAssumption = new LinkedHashMap<>();

	public Integer getCTIAssumption(Datum src) {

		
		return ctiAssumption.get(src);
	}
	
	public void addCTIAssumption(Datum src, int cti){
		ctiAssumption.put(src, cti);
	}
}
