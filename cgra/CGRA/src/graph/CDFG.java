package graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.sound.midi.ControllerEventListener;

import target.Amidar;

/**
 * Class representing the Data and Control Flow
 * 
 * @author jung
 *
 */
public class CDFG implements Iterable<Node> {

	/**
	 * Contains all nodes in the DCFG
	 */
	protected LinkedHashSet<Node> nodes;
	
	
	public LinkedHashSet<Node> getNodes(){
		return nodes;
	}
	
	/**
	 * Contains all control dependencies (forward)
	 */
	protected LinkedHashMap<Node, LinkedHashSet<Node>> controlPredecessors;
	/**
	 * Contains all control dependencies (backwards)
	 */
	protected LinkedHashMap<Node, LinkedHashSet<Node>> controlSuccessors;

	/**
	 * Contains data dependencies in one way (successors). The other way
	 * (predecessors) is stored in the nodes directly as Nodes need to identify
	 * the operands
	 */
	protected LinkedHashMap<Node, LinkedHashSet<Node>> dataSuccessors;

	/**
	 * Contains all successors (both data and control dependencies
	 */
	protected LinkedHashMap<Node, LinkedHashSet<Node>> allSuccessors;

	/**
	 * Contains all predecessors (both data and control dependencies
	 */
	protected LinkedHashMap<Node, LinkedHashSet<Node>> allPredecessors;

	/**
	 * Constructs a new DCFG
	 */
	public CDFG() {
		nodes = new LinkedHashSet<Node>();
		controlPredecessors = new LinkedHashMap<Node, LinkedHashSet<Node>>();
		controlSuccessors = new LinkedHashMap<Node, LinkedHashSet<Node>>();
		dataSuccessors = new LinkedHashMap<Node, LinkedHashSet<Node>>();
		allSuccessors = new LinkedHashMap<Node, LinkedHashSet<Node>>();
		allPredecessors = new LinkedHashMap<Node, LinkedHashSet<Node>>();
	}

	/**
	 * Adds a data dependency between two nodes. The nodes are added to the list
	 * of all nodes
	 * 
	 * @param from
	 *            Data Producing node
	 * @param to
	 *            Data Consuming node
	 * @param pos
	 *            The identifier of the operation of the consuming node
	 */
	public void setDataDependency(Node from, Node to, int pos) {
		nodes.add(from);
		nodes.add(to);
		LinkedHashSet<Node> dsucc = dataSuccessors.get(from);
		if (dsucc == null) {
			dsucc = new LinkedHashSet<Node>();
			dataSuccessors.put(from, dsucc);
		}
		dsucc.add(to);

		LinkedHashSet<Node> asucc = allSuccessors.get(from);
		if (asucc == null) {
			asucc = new LinkedHashSet<Node>();
			allSuccessors.put(from, asucc);
		}
		asucc.add(to);

		LinkedHashSet<Node> apred = allPredecessors.get(to);
		if (apred == null) {
			apred = new LinkedHashSet<Node>();
			allPredecessors.put(to, apred);
		}
		apred.add(from);

		to.setPredecessor(pos, from);
	}

	/**
	 * Adds a control dependency between two nodes. The nodes are added to the
	 * list of all nodes.
	 * 
	 * @param from
	 *            Node that has to be executed first
	 * @param to
	 *            Node that has to be executed secondly
	 */
	public void setControlDependency(Node from, Node to) {
		if (from.address == to.address && from.operation.equals(to.operation) && from.value.equals(to.value)) {
			return;
		}

		nodes.add(from);
		nodes.add(to);

		LinkedHashSet<Node> csucc = controlSuccessors.get(from);
		if (csucc == null) {
			csucc = new LinkedHashSet<Node>();
			controlSuccessors.put(from, csucc);
		}
		csucc.add(to);

		LinkedHashSet<Node> cpred = controlPredecessors.get(to);
		if (cpred == null) {
			cpred = new LinkedHashSet<Node>();
			controlPredecessors.put(to, cpred);
		}
		cpred.add(from);

		LinkedHashSet<Node> asucc = allSuccessors.get(from);
		if (asucc == null) {
			asucc = new LinkedHashSet<Node>();
			allSuccessors.put(from, asucc);
		}
		asucc.add(to);

		LinkedHashSet<Node> apred = allPredecessors.get(to);
		if (apred == null) {
			apred = new LinkedHashSet<Node>();
			allPredecessors.put(to, apred);
		}
		apred.add(from);
	}
	
	
	public void deleteDependency(Node pre, Node succ){
		LinkedHashSet<Node> ctrlPreds = controlPredecessors.get(succ);
		if(ctrlPreds != null){
			ctrlPreds.remove(pre);
		}
		
		LinkedHashSet<Node> ctrlSuccs = controlSuccessors.get(pre);
		if(ctrlSuccs != null){
			ctrlSuccs.remove(succ);
		}
		
		
		LinkedHashSet<Node> dataSuccs = dataSuccessors.get(pre);
		if(dataSuccs != null){
			dataSuccs.remove(succ);
		}
		
		
		LinkedHashSet<Node> allPreds = allPredecessors.get(succ);
		if(allPreds != null){
			allPreds.remove(pre);
		}
		
		LinkedHashSet<Node> allSuccs = allSuccessors.get(pre);
		if(allSuccs != null){
			allSuccs.remove(succ);
		}
		
	}

	/**
	 * Returns a set of all Nodes that have to be executed before the given node
	 * n (both control and data dependencies).
	 * 
	 * @param n
	 *            The node
	 * @return all predecessors of the node n (both control and data
	 *         dependencies)
	 */
	public LinkedHashSet<Node> getAllPredecessors(Node n) {
		LinkedHashSet<Node> aPred = allPredecessors.get(n);
		if (aPred == null) {
			return new LinkedHashSet<Node>();
		}
		return aPred;
	}

	/**
	 * Returns a set of all Nodes that have to be executed after the given node
	 * n (both control and data dependencies).
	 * 
	 * @param n
	 *            The node
	 * @return all successors of the node n (both control and data dependencies)
	 */
	public LinkedHashSet<Node> getAllSuccessors(Node n) {
		LinkedHashSet<Node> aSucc = allSuccessors.get(n);
		if (aSucc == null) {
			return new LinkedHashSet<Node>();
		}
		return aSucc;
	}

	/**
	 * Returns a set of all Nodes that have to executed before the given node n
	 * (Control dependencies).
	 * 
	 * @param n
	 *            The node
	 * @return all predecessors of the node n (control dependencies)
	 */
	public LinkedHashSet<Node> getPredecessors(Node n) {
		return controlPredecessors.get(n);
	}

	/**
	 * Returns a set of all Nodes that have to executed after the given node n
	 * (Control dependencies).
	 * 
	 * @param n
	 *            The node
	 * @return all successors of the node n (control dependencies)
	 */
	public LinkedHashSet<Node> getSuccessors(Node n) {
		Set<Node> gnampf = controlSuccessors.get(n);
		return (LinkedHashSet<Node>) gnampf;
	}

	/**
	 * Returns a set of all Nodes that consume the datum produced by the given
	 * node n (data dependencies)
	 * 
	 * @param n
	 *            The node
	 * @return all consumers of the node n (data dependency)
	 */
	public LinkedHashSet<Node> getConsumers(Node n) {
		return dataSuccessors.get(n);
	}

	/**
	 * Returns the node that produces an operand for the given node n (data
	 * dependencies)
	 * 
	 * @param n
	 *            the node
	 * @param pos
	 *            the operand identifier
	 * @return the producer node
	 */
	public Node getOperands(Node n, int pos) {
		return n.getPredecessor(pos);
	}

	/**
	 * Returns the graph as a String in dot-format
	 */
	@Override
	public String toString() {
		StringBuilder erg = new StringBuilder();
		erg.append("digraph depgraph {\n");
		for (Node n : nodes) {
			Node[] preds = n.getPredecessors();
			for (int i = 0; i < preds.length; i++) {
				// if(preds[i] != null)
				erg.append("\"" + preds[i] + "\" -> \"" + n + "\"[label=" + i + "];\n");
			}
			/*
			 * if(n.getController()!=null){ //den Controller einzeichnen ist
			 * nicht sinnvoll, wenn der Kontrollfluss nicht von ihm abhaengt,
			 * oder? if(n.getDecision()) erg.append("\""+ n.getController()+
			 * "\" -> \""+ n +"\"[color=green,style=dashed];\n"); else
			 * erg.append("\""+ n.getController()+ "\" -> \""+ n
			 * +"\"[color=red,style=dashed];\n"); }
			 */

			if (controlPredecessors.get(n) != null) {
				for (Node pre : controlPredecessors.get(n)) {
					if (pre != n.getController()) {
						erg.append("\"" + pre + "\" -> \"" + n + "\"[color=gray];\n");
					} else {
						if (n.getDecisionTristate() == null) {
							erg.append("\"" + n.getController() + "\" -> \"" + n + "\"[color=blue,style=dashed];\n");
						} else if (n.getDecision()) {
							erg.append("\"" + n.getController() + "\" -> \"" + n + "\"[color=green,style=dashed];\n");
							if (n.getController().getShortCircuitEvaluationTrueBranch()) {
								if (n.getController().getShortCircuitEvaluationTrueBranchControllerDecision()) {
									erg.append("\"" + n.getController().getShortCircuitEvaluationTrueBranchController()
											+ "\" -> \"" + n + "\"[color=green,style=dashed];\n");
								} else {
									erg.append("\"" + n.getController().getShortCircuitEvaluationTrueBranchController()
											+ "\" -> \"" + n + "\"[color=red,style=dashed];\n");
								}
							}
						} else {
							erg.append("\"" + n.getController() + "\" -> \"" + n + "\"[color=red,style=dashed];\n");
							if (n.getController().getShortCircuitEvaluationFalseBranch()) {
								if (n.getController().getShortCircuitEvaluationFalseBranchControllerDecision()) {
									erg.append("\"" + n.getController().getShortCircuitEvaluationFalseBranchController()
											+ "\" -> \"" + n + "\"[color=green,style=dashed];\n");
								} else {
									erg.append("\"" + n.getController().getShortCircuitEvaluationFalseBranchController()
											+ "\" -> \"" + n + "\"[color=red,style=dashed];\n");
								}
							}
						}

						// if (n.getController().getShortCircuitEvaluation() &&
						// (n.getController().getShortCircuitEvaluationBranch()
						// == n.getDecision())) {
						// if (!n.getController().getDecision()) {
						// erg.append("\""+ n.getController().getController()+
						// "\" -> \""+ n +"\"[color=green,style=dashed];\n");
						// } else {
						// erg.append("\""+ n.getController().getController()+
						// "\" -> \""+ n +"\"[color=red,style=dashed];\n");
						// }
						// }
					}
				}
			}

		}

		erg.append("}");
		return erg.toString();
	}

	@Override
	public Iterator<graph.Node> iterator() {
		return nodes.iterator();
	}

	public void cleanFromPrefetch(boolean [] isPrefetch, boolean [] isPrefetchFill) {
		
		
		
		
		
		Set<Node> prefetchNodes = new LinkedHashSet<Node>();
//		LinkedList<Node> prefetchNodesList = new LinkedList<Node>();
		
//		long time1 = System.nanoTime();
		
		for(Node nd: nodes){
			if(isPrefetch[nd.getAddress()]|| isPrefetchFill[nd.getAddress()]){
				prefetchNodes.add(nd);
				nd.setController(null, null);
			}
		}
		
		boolean changed = true;
		
//		Collections.sort(prefetchNodesList, new Comparator<Node>() {
//
//			@Override
//			public int compare(Node o1, Node o2) {
//				return Integer.compare(o2.address, o1.address);
//			}
//			
//			
//		});
		
//		long time2 = System.nanoTime();
		
		while(changed){
			changed = false;
			
			for(Node nd: prefetchNodes){
				if(!nodes.contains(nd)){
					continue; // Node was already deleted
				}
//				if(prefetchNodeRank.get(nd)>=RANK_THRESHOLD){
//					for(Node pred: nd.getPredecessors()){
//						deleteDependency(pred, nd);
//					}
//					
//					if(dataSuccessors.get(nd) != null){
//						LinkedHashSet<Node> dataSuccs = new LinkedHashSet<Node>(dataSuccessors.get(nd));
//						for(Node dataSucc: dataSuccs){
//							deleteDependency(nd, dataSucc);
//						}
//						
//					}
//					
//					
//					if(controlPredecessors.get(nd) != null){
//						LinkedHashSet<Node> ctrlPreds = new LinkedHashSet<>(controlPredecessors.get(nd));
//						
//						for(Node cPred: ctrlPreds){
//							deleteDependency(cPred, nd);
//						}
//					}
//					
//					if(controlSuccessors.get(nd) != null){
//						LinkedHashSet<Node> ctrlSuccs = new LinkedHashSet<>(controlSuccessors.get(nd));
//
//						for(Node cSucc: ctrlSuccs){
//							deleteDependency(nd, cSucc);
//						}
//					}
//					nodes.remove(nd);
//					changed = true;
//				} else 
				if(nd.getOperation().isCacheAccess() && !nd.getOperation().isCachePrefetch()){
					
					
					if(nd.getOperation().isCacheStore()){
						Node storeData = nd.getPredecessor(2);
						Node [] newPredecessors = new Node[2];
						newPredecessors[0] = nd.getPredecessor(0);
						newPredecessors[1] = nd.getPredecessor(1);
						nd.setPredecessors(newPredecessors);
						if(storeData != newPredecessors[1])
						deleteDependency(storeData, nd);
					}
					
					
					boolean isHTaccess = false;

					LinkedHashSet<Node> succs = dataSuccessors.get(nd);

					int constVal = nd.getPredecessor(1).getValue();
					if(nd.getPredecessor(1).getOperation() == Amidar.OP.CONST && (constVal == Integer.MAX_VALUE || constVal == Integer.MAX_VALUE-1)){
						isHTaccess = true;
					} 

					if(isHTaccess){
						// this is a getCTI or a arraylength instruction -> no Prefetch (vllt doch)
						if(succs == null || succs.size() == 0){
							for(Node pred: nd.getPredecessors()){
								deleteDependency(pred, nd);
							}

							if(controlPredecessors.get(nd) != null){
								LinkedHashSet<Node> ctrlPreds = new LinkedHashSet<>(controlPredecessors.get(nd));

								for(Node cPred: ctrlPreds){
									deleteDependency(cPred, nd);
								}
							}

							if(controlSuccessors.get(nd) != null){
								LinkedHashSet<Node> ctrlSuccs = new LinkedHashSet<>(controlSuccessors.get(nd));

								for(Node cSucc: ctrlSuccs){
									deleteDependency(nd, cSucc);
								}
							}
							nodes.remove(nd);
							changed = true;
						}
					} else {
						nd.setOperation(target.Processor.Instance.getOperatorByName("CACHE_FETCH")); 
						// TODO Amidar only
						                //nd.getOperation().getPrefetchOperation()

//						if(allSuccessors.get(nd) != null){
//							LinkedHashSet<Node> allsuccs = new LinkedHashSet<>(allSuccessors.get(nd));
//							for(Node succ: allsuccs){
//								deleteDependency(nd, succ);
//							}
//						}
						
						if(succs != null){
							for(Node succ : succs){
								if(!(succ.getOperation().isCacheStore() && succ.getPredecessor(2) == nd)){ // nicht an nachfolger weitergeben, die den Wert speichern
									succ.setController(nd, true);
									if(succ.getOperation().isCacheAccess() || succ.getOperation().isCachePrefetch()){

										setControlDependency(nd, succ);
									}
								}
							}
						}
						
						
						
						if(succs != null && succs.size() == 0 && (isPrefetchFill[nd.getAddress()] || nd.getPredecessor(1).getOperation() == Amidar.OP.CONST || !keepPrefetch(nd) )){
//							System.out.println("     raus -> " + nd);
//							raus!!!
							for(Node pred: nd.getPredecessors()){
								deleteDependency(pred, nd);
							}
							
							if(controlPredecessors.get(nd) != null){
								LinkedHashSet<Node> ctrlPreds = new LinkedHashSet<>(controlPredecessors.get(nd));
								
								for(Node cPred: ctrlPreds){
									deleteDependency(cPred, nd);
								}
							}
							
							if(controlSuccessors.get(nd) != null){
								LinkedHashSet<Node> ctrlSuccs = new LinkedHashSet<>(controlSuccessors.get(nd));

								for(Node cSucc: ctrlSuccs){
									deleteDependency(nd, cSucc);
								}
							}
							nodes.remove(nd);
						}
						
						
						changed = true;
					}
					
					
				} else if(nd.getOperation().isControlFlow() && !nd.getOperation().isCachePrefetch()){
//					System.out.println("contrCSSR " + controlSuccessors.get(nd));
					if(controlSuccessors.get(nd)!= null){
					LinkedHashSet<Node> succs = new LinkedHashSet<>(controlSuccessors.get(nd)) ;
					for(Node succ: succs){
						deleteDependency(nd, succ);
					}
					}
					LinkedHashSet<Node> preds = new LinkedHashSet<>(allPredecessors.get(nd)) ;
					for(Node pred: preds){
						deleteDependency(pred, nd);
					}
					
					
					nodes.remove(nd);
					changed = true;
				} else if(!nd.getOperation().isCachePrefetch() || isPrefetchFill[nd.getAddress()] || (nd.getOperation().isCachePrefetch() && (nd.getPredecessor(1).getOperation() == Amidar.OP.CONST || !keepPrefetch(nd)))){
					LinkedHashSet<Node> succs = dataSuccessors.get(nd);
					if(succs == null || succs.size() == 0){
						for(Node pred: nd.getPredecessors()){
							deleteDependency(pred, nd);
						}
						
						if(controlPredecessors.get(nd) != null){
							LinkedHashSet<Node> ctrlPreds = new LinkedHashSet<>(controlPredecessors.get(nd));
							
							for(Node cPred: ctrlPreds){
								deleteDependency(cPred, nd);
							}
						}
						
						if(controlSuccessors.get(nd) != null){
							LinkedHashSet<Node> ctrlSuccs = new LinkedHashSet<>(controlSuccessors.get(nd));

							for(Node cSucc: ctrlSuccs){
								deleteDependency(nd, cSucc);
							}
						}
						nodes.remove(nd);
						changed = true;
					} else{
						if(nd.getOperation() == Amidar.OP.STORE){
//							System.err.println(nd + " ----> " + succs);
							if(nd.getValue() < 99999){
								nd.setValue(nd.getValue() +99999);
							}
						}
						if(nd.getController()!= null){
							for(Node succ: succs){
								if(!(succ.getOperation().isCacheStore() && succ.getPredecessor(2) == nd)){
									succ.setController(nd.getController(), true);
									if(succ.getOperation().isCacheAccess() || succ.getOperation().isCachePrefetch()){
										setControlDependency(nd.getController(), succ);
									}
								}
							}
						}
					}
				}
				
				
			}
			
		}
		
		
//		long time3 = System.nanoTime();
		for(Node nd: prefetchNodes){
			if(controlPredecessors.get(nd) != null){
				
				LinkedHashSet<Node> toDelete = new LinkedHashSet<>();
				
				for(Node cPred : controlPredecessors.get(nd)){
					if(cPred != nd.getController()){
						toDelete.add(cPred);
					}
				}
				
				for(Node td : toDelete){
					
					controlPredecessors.get(nd).remove(td);
					controlSuccessors.get(td).remove(nd);
					
					boolean isAlsoDataPred = false;
					for(Node pred : nd.getPredecessors()){
						if(pred.equals(td)){
							isAlsoDataPred = true;
						}
					}
					if(!isAlsoDataPred){
						allPredecessors.get(nd).remove(td);
						allSuccessors.get(td).remove(nd);
					}
					
					
				}
				
				
			}
		}
//		long time4 = System.nanoTime();
		///////////////////////////////////////////// PART 2 GET RANK /////////////////////////////////////////
		LinkedHashMap<Node, Integer> prefetchNodeRank = new LinkedHashMap<>();
		
		int RANK_THRESHOLD = 3;
		
		for(Node nd: nodes){
			if(isPrefetch[nd.getAddress()]|| isPrefetchFill[nd.getAddress()]){
				prefetchNodeRank.put(nd, 0);
			}
		}
		
		changed = true;
		
		while(changed){
			changed = false;
			for(Node nd: prefetchNodeRank.keySet()){
				
				Node[] preds = nd.getPredecessors();
				int currentRank = prefetchNodeRank.get(nd);
				int[] rank = new int[preds.length];
				
				for(int i = 0; i < preds.length; i++){
					if(!prefetchNodeRank.containsKey(preds[i])){
						rank[i] = 1;
					} else {
						int add = 1;
//						if(preds[i].getOperation().equals(Amidar.OP.CACHE_FETCH)){
//							System.out.println("ASDF  ffff ");
//							add = 2;
//						}
						rank[i] = prefetchNodeRank.get(preds[i]) + add;
					}
					
					if(rank[i] > currentRank){
						currentRank = rank[i];
						prefetchNodeRank.put(nd, currentRank);
						changed = true;
					}
				}
			}
			
			
		}
		
//		long time5 = System.nanoTime();
		//////////////////////////////////// CLEANUP WHAT IS OVER RANK THRESHOLD ////////////////////////////////////////
		
		prefetchNodes = new TreeSet<Node>();
		
//		prefetchNodes.addAll(prefetchNodeRank.keySet());
		
		for(Node nd :nodes){
			if(isPrefetch[nd.getAddress()]|| isPrefetchFill[nd.getAddress()]){
				prefetchNodes.add(nd);
			}
		}
		
		int nrNodes = nodes.size();
		
		int nrPref = prefetchNodes.size();
		
		int ratio = 100*nrPref/nrNodes;
		
		
		
//		while(ratio > 4){
		
//			RANK_THRESHOLD--;
	
		
		
		
		changed = true;
		
		

//		
//		System.out.println(" -------------------------------------UZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ");
//		System.out.println("    " + (nrPref/(double)nrNodes));
//		
//		int RANK_TRESHOLD = 7 - 100*nrPref/nrNodes;
//		if(RANK_TRESHOLD < 3){
//			RANK_TRESHOLD = 3;
//		}
//		System.out.println("            ---> " + RANK_TRESHOLD);
//		
		int rankBuffer = RANK_THRESHOLD;
		
//		long time6 = System.nanoTime();
		while(changed){
			changed = false;
			for(Node nd : prefetchNodes){
				
				if(!nodes.contains(nd)){
					continue;
				}
				boolean delete = false;
				if(nd.getOperation().equals(target.Processor.Instance.getOperatorByName("CACHE_FETCH"))){
					if(prefetchNodeRank.get(nd) >= RANK_THRESHOLD){
						delete = true;
					}
				} else {
					if(dataSuccessors.get(nd) == null || dataSuccessors.get(nd).size() == 0){
						delete = true;
					}
				}
				
				if(delete){
//					nrPref--;
//					ratio = 100*nrPref/nrNodes;
//					
//					if(ratio <= 4){
//						RANK_THRESHOLD = 1000;
//					}
					
					
					
					for(Node pred: nd.getPredecessors()){
						deleteDependency(pred, nd);
					}

					if(controlPredecessors.get(nd) != null){
						LinkedHashSet<Node> ctrlPreds = new LinkedHashSet<>(controlPredecessors.get(nd));

						for(Node cPred: ctrlPreds){
							deleteDependency(cPred, nd);
						}
					}

					if(controlSuccessors.get(nd) != null){
						LinkedHashSet<Node> ctrlSuccs = new LinkedHashSet<>(controlSuccessors.get(nd));

						for(Node cSucc: ctrlSuccs){
							deleteDependency(nd, cSucc);
						}
					}
					nodes.remove(nd);
					changed = true;
				}
			}
		}
		
		
		prefetchNodes = new TreeSet<Node>();
		
//		prefetchNodes.addAll(prefetchNodeRank.keySet());
		
		for(Node nd :nodes){
			if(isPrefetch[nd.getAddress()]|| isPrefetchFill[nd.getAddress()]){
				prefetchNodes.add(nd);
			}
		}
		
		 nrNodes = nodes.size();
		 nrPref = prefetchNodes.size();
		
		 ratio = 100*nrPref/nrNodes;
		 RANK_THRESHOLD = rankBuffer;
		 
//		 long time7 = System.nanoTime();
		 
//		 System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAa");
//		 System.out.println(time2-time1);
//		 System.out.println(time3-time2);
//		 System.out.println(time4-time3);
//		 System.out.println(time5-time4);
//		 System.out.println(time6-time5);
//		 System.out.println(time7-time6);
		 
//		}
		
	}
	
	LinkedHashMap<Node, LinkedHashSet<PrefetchField>> handlePrefetchMap = new LinkedHashMap<>();
	
	
	LinkedHashMap<Node, PrefetchField> allFields = new LinkedHashMap<>();
	
	private boolean keepPrefetch(Node nd){
		boolean keep = true;
		
		Node handle = nd.getPredecessor(0);
	
//		return true;
		
//		Node pref = handlePrefetchMap.get(handle);
//		if(pref == null){
//			handlePrefetchMap.put(handle, nd);
//			return true;
//		} else if(nd == pref) {
//			return true;
//		} else {
////			System.out.println("whöööööööööp " );
////			System.out.println(handlePrefetchMap);
//			return false;
//		}
		
//		System.err.println(handlePrefetchMap);
		
		LinkedHashSet<PrefetchField> fields = handlePrefetchMap.get(handle);
		
		if(fields == null){
			fields = new LinkedHashSet<>();
			handlePrefetchMap.put(handle, fields);
		}
		
		
		PrefetchField currentField = allFields.get(nd);
		if(currentField == null){
			currentField = new PrefetchField(nd);
			allFields.put(nd, currentField);
		}
		if(currentField.isConstant()){
			return false;
		}
		
		boolean isBetter = false;
		PrefetchField toRemove = null;
		for(PrefetchField pf: fields){
			if(pf.samePrefetchField(currentField)){
				if(currentField.same(pf)){
					return true;
				}
				if(currentField.betterThan(pf)){
					isBetter = true;
					toRemove = pf;
//					System.out.println("better: ");
//					System.out.println(currentField);
//					System.out.println("      " + pf);

				} else {
					return false;
				}
				break;
			}
		}
		
		if(isBetter){
			fields.remove(toRemove);
		}
		
		fields.add(currentField);
		return true;
		
	}
	
	private class PrefetchField{
		static final int RANGE_WIDTH = 8; // Ideally the wordwidth
		
		Node nd;
		Node handle;
		Node indexBase;
		int range;

		PrefetchField(Node nd){
			this.nd = nd;
			this.handle = nd.getPredecessor(0);
			
			Node index = nd.getPredecessor(1);
			if(/*index.getOperation().getNumberOfOperands() == 2*/index.getOperation().equals(Amidar.OP.IADD)){
				Node op0 = index.getPredecessor(0);
				Node op1 = index.getPredecessor(1);
				
				if(op0.getOperation() == Amidar.OP.CONST ){
					range = op0.getValue();
					indexBase = op1;
				} else if(op1.getOperation() == Amidar.OP.CONST ){
					range = op1.getValue();
					indexBase = op0;
				} else {
//					System.err.println("notwithme2: " + index);
					indexBase = index;
					range = 0;
				}
				
			} else if(index.getOperation().isConst()){
//				System.err.println("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFUUUUUUUUUUUUUUUUUUU");
				range = index.getValue();
				indexBase = handle;
			} else {
//				System.err.println("notwithme: " + index);
				indexBase = index;
				range = 0;
			}
		}
		
		boolean samePrefetchField(PrefetchField pf){

			if(handle != pf.handle){
//				System.out.println("HANDLEXX " + handle + " pf: " +pf.handle);
				return false;
			}
			
			if(indexBase == null || pf.indexBase == null || indexBase != pf.indexBase){
//				System.out.println("INDEXX: " + indexBase + " pf: " + pf.indexBase);
				return false;
			}
			
			if(range/RANGE_WIDTH != pf.range/RANGE_WIDTH){
				return false;
			}
			
			return true;
		}
		
		Boolean betterThan(PrefetchField pf){
			if(!samePrefetchField(pf)){
				return null;
			}
			if(Math.abs(range) > Math.abs(pf.range)){
				return true;
			} else {
				return false;
			}
		}
		
		boolean same(PrefetchField pf){
			return nd == pf.nd;
		}
		
		
		boolean isConstant(){
			return indexBase == handle;
		}
		
		public String toString(){
			return "Handle: "+handle+"\tIndexBase: "+indexBase+"\tRange: " +range;
		}
		
	}
	
}
