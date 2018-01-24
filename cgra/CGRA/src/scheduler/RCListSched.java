package scheduler;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import cgramodel.ActorInterface;
import cgramodel.CgraModel;
import cgramodel.CgraModelUltrasynth;
import cgramodel.ContextMaskCBoxEvaluationBlock;
import cgramodel.ContextMaskContextControlUnit;
import cgramodel.ContextMaskPE;
import cgramodel.IDCrange;
import cgramodel.InterfaceContext;
import cgramodel.LocationInformation;
import cgramodel.LogGlobalContext;
import cgramodel.OCMInterface;
import cgramodel.PEModel;
import cgramodel.RegfileMuxSource;
import graph.ANode;
import graph.ANode.Constant;
import graph.ANode.HostInput;
import graph.ANode.Parameter;
import graph.CDFG;
import graph.IDP;
import graph.LG;
import graph.Loop;
import graph.Node;
import operator.Operator;
import scheduler.representation.ScheduleRepresentationCBox;
import scheduler.representation.ScheduleRepresentationPE;
import target.Amidar;
import target.Processor;

/**
 * @author ruschke
 *
 */
public class RCListSched implements Scheduler {
	/**
	 * The graph to work on
	 */
	CDFG graph;

	/**
	 * The loop graph to work on
	 */
	LG lg;

	/**
	 * The CGRA configuration to schedule on
	 */
	CgraModel model;
	
	
	private Map<Operator, Integer>[] operations;
	private Map<Operator, Integer> outDelays;
	private Map<Operator, Integer> inDelays;
	
	LinkedList<Integer>[] interconnect;
	
	private boolean [] isPrefetch;
	
	
	/**
	 * The priority criterion to use for list scheduling
	 */
	SchedPrio prio;

	/**
	 * The number of PEs and the size of the C-Box
	 */
	int nrOfPEs, cBoxSize;

	/**
	 * Path-matrix for PE connection
	 */
	int[][] P = null;
	
	/**
	 * Distance-matrix for PE connection
	 */
	int[][] D = null;
	
	/**
	 * The schedule
	 */
	Schedule sched = null;
	
	/**
	 * Maps addresses of local variables to be stored to the node the PE can be
	 * retrieved from
	 */
	Map<Integer, Node> storeMap = null;
	
	/**
	 * Maps a loop to all nodes that belong to this loop
	 */
	Map<Loop, Set<Node>> loopNodes = null;
	
	/**
	 * Maps a loop to all nodes that provide values to be used inside the loop
	 */
	Map<Loop, Set<Node>> loopPreds = null;
	
	/**
	 * Maps a node to all nodes that have been fused into it
	 */
	Map<Node, Set<Node>> vNodes = null;
	
	/**
	 * Maps store nodes to the nodes they have been fused into
	 */
	Map<Node, Node> sFuseNodes = null;
	
	/**
	 * Set of all IF-operation nodes
	 */
	Set<Node> ifNodes = null;

	/**
	 * Set off all Loop controllers
	 */
	Set<Node> loopControllers = null;

	/**
	 * Maps loops to their beginning (int[0]) and end (int[1]) in the contexts
	 */
	Map<Loop, int[]> loopTimes = null;

	public Map<Loop, int[]> getLoopTimes() {
		return loopTimes;
	}

	/**
	 * The attraction property for each node
	 */
	Map<Node, int[]> nodeAttr;
	Map<Node, Integer> nodeAttrTime;
	
	/**
	 * Maps time steps to an array telling if the corresponding PE is busy at
	 * that time
	 */
	Map<Integer, boolean[]> busyMap;
	
	/**
	 * Maps nodes to the PE they are scheduled on
	 */
	Map<Node, Integer> peMap;

	public Map<Node, Integer> getPeMap() {
		return peMap;
	}
	
	/**
	 * Declares the time on each PE from which on a copy is available. -1 if no
	 * copy is available. 0 if the copy is produced during CGRA initialization
	 */
	Map<Node, LinkedHashSet<Integer>[]> copyMap;

	/**
	 * Maps the address of a local variable to the PE it is assigned to
	 */
	Map<Integer, Integer> lvarPEMap;
	
	/**
	 * Declares which node will be present at the out port of the regfile at any
	 * given time
	 */
	Map<Integer, Node[]> outNodeMap;
	
	/**
	 * Declares which node will be present at the internal port of the regfile
	 * at any given time
	 */
	Map<Integer, Node[]> intNodeMap;
	
	/**
	 * Declares which node will be present at the dma base address port of the
	 * regfile at any given time
	 */
	Map<Integer, Node[]> dmaNodeMap;

	/**
	 * A set of integers where the C-Box write is blocked
	 */
	Set<Integer>[] cBoxWrite;
	
	/**
	 * A set of integers where the actuator write is blocked
	 */
	Set<Integer> actuatorWrite;
	
	/**
	 * A set of integers where the OCM write is blocked
	 */
	Set<Integer> OCMWrite;
	
	/**
	 * A set of integers where the sensor read is blocked
	 */
	Set<Integer> sensorRead;
	
	/**
	 * A Map which IF-Node is present at the C-Box read at any given time
	 */
	Map<Integer, Node> cBoxRead[];

	/**
	 * Maps IF nodes to their address in the C-Box
	 */
	public Map<Node, Integer> cBoxAlloc;
	//TODO remove public

//	/**
//	 * Local variable addresses that have a force set
//	 */
//	Set<Integer> forcedLoadMemAddr;

	/**
	 * Maps nodes to their slot in the regfile for each PE
	 */
	Map<Node, Integer>[] peBindMap;
	
	/**
	 * Maps the address of a local variable to the node in the peBindMap for
	 * each PE
	 */
	Map<Integer, Node>[] lvarNodeMap;
	
	/**
	 * Used to get the second slot of a 64-bit operation
	 */
	Map<Node, Node> twinNodes;
	
	
	Map<Node, Map<Integer, Integer>> cboxSelectRead;
	
	Map<Node, Integer> cboxSelectWrite;
	
	Map<Integer,Node>[] prefetchBlocked;

	/**
	 * used in placeNode to identify if the int port is already used for this
	 * operation
	 */
	boolean placeIntUsed;
	
	/**
	 * used in placeNode to identify the PE providing a value
	 */
	int placePEfin;
	
	/**
	 * The PE for each operand in placeNode
	 */
	int[] placePE;
	
	/**
	 * The operands in placeNode
	 */
	Node[] placeNode;

	/**
	 * Reverse connection between PEs
	 */
	Set<Integer>[] peDests; // Possible destination PEs
	
	int maxNrOfLocalVariables = 0;

	Map<Node, Boolean>[] highLowTrackerIntern;
	Map<Node, Boolean>[] highLowTrackerExtern;
	
	int cboxOutputs;
	int cBoxOutputsPerBox;
	int nrOfCBoxes;
	
	boolean isUltrasynth = false;

	public RCListSched(CDFG graph, LG lg) {
		this.graph = graph;
		this.lg = lg;
	}
	
	public RCListSched(CDFG graph, LG lg, int maxNrOfLocalVariables, CgraModel newcgra) throws MissingOperationException {
		this(graph, lg, maxNrOfLocalVariables, newcgra, new LinkedHashMap<Object,LinkedHashMap<Integer,LinkedHashSet<Integer>>>(), null);
	}

	@SuppressWarnings("unchecked")
	public RCListSched(CDFG graph, LG lg, int maxNrOfLocalVariables, CgraModel newcgra, LinkedHashMap<Object,LinkedHashMap<Integer,LinkedHashSet<Integer>>> handleToPeMap, boolean[] isPrefetch ) throws MissingOperationException {
		this.graph = graph;
		this.lg = lg;
		model = newcgra;
		this.maxNrOfLocalVariables = maxNrOfLocalVariables;
		this.handleToPeMap = handleToPeMap;
		this.isPrefetch = isPrefetch;

		nrOfPEs = model.getNrOfPEs();
		cBoxSize = model.getcBoxModel().getMemorySlots();

		operations = new LinkedHashMap[nrOfPEs];
		outDelays = new LinkedHashMap<>();
		inDelays = new LinkedHashMap<>();
		
		Set<Operator> missingOps = new LinkedHashSet<>();
		
		int outDelay;

		for (Node nd : graph) {
			missingOps.add(nd.getOperation());
			if (nd instanceof ANode.Output) {
				isUltrasynth = true;
			}
		}

		nrOfCBoxes = newcgra.getcBoxModel().getNrOfEvaluationBlocks();
		cBoxOutputsPerBox = newcgra.getcBoxModel().getCBoxPredicationOutputsPerBox();
		cboxOutputs = cBoxOutputsPerBox * nrOfCBoxes;



		for (int i = 0; i < nrOfPEs; i++){
			operations[i] = new LinkedHashMap<>();
			for(Operator op : model.getPEs().get(i).getAvailableOperators().keySet()){
				missingOps.remove(op);
				operations[i].put(op, model.getPEs().get(i).getAvailableOperators().get(op).getLatency());
				outDelay = model.getPEs().get(i).getAvailableOperators().get(op).getOutputLatency();
				outDelays.put(op, outDelay);
				
				outDelay = model.getPEs().get(i).getAvailableOperators().get(op).getInputLatency();
				inDelays.put(op, outDelay);
			}
		}
		
		
		if (missingOps.size() > 0) {
			throw new MissingOperationException("CGRA composition is missing the following operations: " + missingOps);
		}

		interconnect = new LinkedList[nrOfPEs];
				
		for (int pe = 0; pe < nrOfPEs; pe++) {
			interconnect[pe] = new LinkedList<Integer>();
			for(PEModel source : model.getPEs().get(pe).getInputs()){
				interconnect[pe].add(source.getID());
			}
		}
		peDests = new LinkedHashSet[nrOfPEs];
		
		for (int i = 0; i < nrOfPEs; i++) {
			peDests[i] = new LinkedHashSet<Integer>();
		}

		for (int i = 0; i < nrOfPEs; i++) {
			for (PEModel source : model.getPEs().get(i).getInputs()) {
				peDests[source.getID()].add(i);
			}
		}
	}

	@Override
	public void setGraphs(CDFG graph, LG lg) {
		this.graph = graph;
		this.lg = lg;
	}

	/**
	 * Set the priority criterion to be used
	 * 
	 * @param prio
	 *            - The new criterion
	 */
	public void setPriorityCritereon(SchedPrio prio) {
		this.prio = prio;
	}

	long start, stop;

	@SuppressWarnings("unchecked")
	@Override
	public Schedule schedule() throws NotSchedulableException {
		start = System.nanoTime();
		if (!prepare()){
			System.out.println("Problem in prepare : something wrong with some input of the rc");
			return null; // stop scheduling if prerequisites are not fulfilled
		}
		
		
		sched = new Schedule();

		Set<Node> candidates = new LinkedHashSet<Node>();
		Set<Node> constNodes = new LinkedHashSet<Node>();

		Loop ndLoop;

		loopNodes = new LinkedHashMap<Loop, Set<Node>>();


		nodeAttr = new LinkedHashMap<Node, int[]>();
		nodeAttrTime = new LinkedHashMap<>();
		
		busyMap = new LinkedHashMap<Integer, boolean[]>();
		peMap = new LinkedHashMap<Node, Integer>();

		loopTimes = new LinkedHashMap<>();

		outNodeMap = new LinkedHashMap<Integer, Node[]>();
		intNodeMap = new LinkedHashMap<Integer, Node[]>();
		dmaNodeMap = new LinkedHashMap<Integer, Node[]>();
		ifNodes = new LinkedHashSet<>();
		
		actuatorWrite = new LinkedHashSet<>();;
		OCMWrite = new LinkedHashSet<>();
		sensorRead = new LinkedHashSet<>();

		cBoxWrite = new LinkedHashSet[nrOfCBoxes];
		for(int i = 0; i < nrOfCBoxes; i++){
			cBoxWrite[i] = new LinkedHashSet<Integer>();
		}
			
		cBoxRead = new LinkedHashMap[cboxOutputs];
		for(int i = 0; i < cboxOutputs; i++){
			cBoxRead[i] = new LinkedHashMap<>();
		}

		copyMap = new LinkedHashMap<Node, LinkedHashSet<Integer>[]>();

		lvarPEMap = new LinkedHashMap<>();

		storeMap = new LinkedHashMap<Integer, Node>();
		
		cboxSelectRead = new LinkedHashMap<Node, Map<Integer, Integer>>();
		cboxSelectWrite = new LinkedHashMap<>();
		
		prefetchBlocked = new LinkedHashMap[nrOfPEs];
		for(int pe = 0; pe < nrOfPEs; pe++){
			prefetchBlocked[pe] = new LinkedHashMap<>();
		}

		for (Node nd : graph) {
			if (nd.getOperation().isConst()) {
				constNodes.add(nd);
			}
			
			if (nd.getOperation().isRegfileStore()) {
				storeMap.put(nd.getValue(), nd);
			}

			if (isIf(nd) & !isDMAPrefetch(nd)) {
//				if(graph.getSuccessors(nd) != null && graph.getSuccessors(nd).size()>0){
					// Die Bedingung muss hier sein für Prefechtes - die können nämlich keine control nachfolger haben
					// Dann müssen sie auch nicht in die ifnodes aufgenommen werden
					ifNodes.add(nd);
//				}
			}
			
			if(isDMAPrefetch(nd)){
				LinkedHashSet<Node> controlSuccs = graph.getSuccessors(nd);
				if(!(controlSuccs == null || controlSuccs.size() == 0)){
					ifNodes.add(nd);
				}
				
			}
			

			if (graph.getAllPredecessors(nd) == null
					|| graph.getAllPredecessors(nd).isEmpty()) {
				candidates.add(nd); // There shouldn't be any root-nodes that are not value-nodes
			}

			ndLoop = lg.getLoop(nd);

			if (!loopNodes.containsKey(ndLoop)) {
				loopNodes.put(ndLoop, new LinkedHashSet<Node>());
			}
			loopNodes.get(ndLoop).add(nd); // map loops to nodes
			
		}

		loopControllers = new LinkedHashSet<Node>();
		for (Loop lp : lg.loops) {
			loopControllers.addAll(lp.getController());
		}

		setLoopPreds();

		vNodes = new LinkedHashMap<Node, Set<Node>>();

		Set<Node> handled = new LinkedHashSet<Node>();

		for (Node nd : constNodes) {		// const-Nodes can always be fused into successor
			candidates.remove(nd);
			handled.add(nd);
			for (Node nds : graph.getAllSuccessors(nd)) {
				if (handled.containsAll(graph.getAllPredecessors(nds))) {
					candidates.add(nds);
				}
			}
		}

		Set<Node> pending = new LinkedHashSet<Node>();
		Set<Node> pend;
		Set<Node> sCand; // candidates sorted by priority
		Set<Node> cNodes;

		Set<Integer> peSet;

		sFuseNodes = new LinkedHashMap<Node, Node>();

		boolean moreToCheck;

		int fPE;

		int t = 0;

		int start;
		int dur;

		int[] lpTime;

		Loop loopRoot = lg.getRoot();
		Loop lp = loopRoot;		//lp will be the loop currently being scheduled
		Loop curScheduling;

		loopTimes.put(loopRoot, new int[] { 0, -1 });		//root loop always starts at zero and ends with schedule

		if (lp == null) {
			System.err.println("Loopgraph has no root!");
			throw new NotSchedulableException();
		}

		Set<Node> prevs, prev;
		boolean dontFuse;
		
		while (candidates.size() > 0 || pending.size() > 0) {

			if (pending.size() > 0) { // check if pending nodes are finished
				pend = new LinkedHashSet<Node>(pending);

				for (Node nd : pending) {
					moreToCheck = true;
					if (sched.slot(nd).ubound < t) {	// if current time is after finishtime
						pend.remove(nd); 				// remove from pending list
						handled.add(nd);				// and add to handled list

						cNodes = graph.getConsumers(nd);
						if (!isStore(nd) && (cNodes != null) && (cNodes.size() == 1)) {
							for (Node nds : cNodes) {
								dontFuse = false;
								if ((isStore(nds)) && (handled.containsAll(graph.getAllPredecessors(nds)))) {
									fPE = checkForcedPE(nds);
									if ((fPE < 0) || ((operations[fPE].containsKey(nd.getOperation()) && (peMap.get(nd) == fPE)))) {		//check forced PE compatibility
										prev = sched.nodes(t-1);
										prevs = null;
										if (prev != null) {
											prevs = new LinkedHashSet<Node>(prev);
										}

										if (input64(nds)) {
											prev = sched.nodes(t - 2);
											if (prev != null) {
												if (prevs == null) {
													prevs = new LinkedHashSet<Node>(prev);
												} else {
													prevs.addAll(prev);
												}
											}
										}

										if (prevs != null) {
											for (Node pred : graph.getAllPredecessors(nds)) {
												if (pred == nd) {
													continue;
												}
												if (prevs.contains(pred)) {		// if any other predecessors are planned at the same time, store cannot be fused
													dontFuse = true;
													break;
												}
											}
										}
										if (dontFuse) {
											continue;
										}
										if (isConditional(nds)) {
											if (input64(nds)) {
												int slot = getCBoxReadSlot(nds, t-2, 2);
												if (slot == -1) {
													continue;
												} else {
													blockCBoxReadSlot(nds, t - 2, 2,slot);
												}
											} else {
												int slot = getCBoxReadSlot(nds, t - 1, 1);
												if (slot == -1) {
													continue;
												} else {
													blockCBoxReadSlot(nds, t - 1, 1,slot);
												}
											}
										}

										if (!vNodes.containsKey(nd)) {
											vNodes.put(nd, new LinkedHashSet<Node>());
										}
										vNodes.get(nd).add(nds);
										handled.add(nds);
										sFuseNodes.put(nds, nd);

										addToCopyMap(nds, peMap.get(nd), sched.slot(nd).ubound + 1);
										if (!lvarPEMap.containsKey(nds.getValue())) {
											setForce(nds, peMap.get(nd));
										}

										if (graph.getSuccessors(nd) != null) {
											for (Node nd2 : graph.getSuccessors(nd)) { // add successors of current node
												if (handled.containsAll(graph.getAllPredecessors(nd2))) { 					// if all predecessors
													candidates.add(nd2); 													// have been handled
												}
											}
										}
										for (Node nd2 : graph.getAllSuccessors(nds)) { 					// add successors of fused store node
											if (handled.containsAll(graph.getAllPredecessors(nd2))) { 	// if all predecessors
												candidates.add(nd2); 									// have been handled
											}
										}
										moreToCheck = false;
										candidates.remove(nds);
									}
								}
							}
						}
						if (moreToCheck) {
							for (Node nds : graph.getAllSuccessors(nd)) { 	// add successors
								if (handled.containsAll(graph.getAllPredecessors(nds))) { 	// if all predecessors
									candidates.add(nds); 									// have been handled
								}
							}
						}
					}
				}
				pending = pend;
			}
			
			boolean changed;

			do { 		// if we fuse a load we might add a new load to the candidates list
				changed = false;

				sCand = new LinkedHashSet<Node>(candidates);

				// fuse load nodes
				for (Node nd : sCand) {
					if (isLoad(nd)) {
						if ((graph.getAllPredecessors(nd) != null) && (handled.containsAll(graph.getAllPredecessors(nd)))) {
							if (isLoad(nd) && storeMap.keySet().contains(nd.getValue()) && (lvarPEMap.containsKey(nd.getValue()))) { // This is a load after stores. We have to add it to the copymap
								addToCopyMap(nd, lvarPEMap.get(nd.getValue()), t);
							}
							candidates.remove(nd);
							handled.add(nd);
							for (Node nds : graph.getAllSuccessors(nd)) {
								if (handled.containsAll(graph.getAllPredecessors(nds))) {
									candidates.add(nds);
									changed = true;
								}
							}
							if (graph.getConsumers(nd) != null) {
								for (Node nds : graph.getConsumers(nd)) {
									if (!vNodes.containsKey(nds)) {
										vNodes.put(nds, new LinkedHashSet<Node>());
									}
									vNodes.get(nds).add(nd);
								}
							}
						}
					}
				}
			} while (changed);

			curScheduling = null;
			
			sCand = prio.getSortedNodes(candidates).keySet();
			
			cand: for (Node nd : sCand) {
				if (nd instanceof ANode.Output) continue;	//peripheral stores only affect out_l -> they're handled later
				
				ndLoop = lg.getLoop(nd);
				if (!ndLoop.equals(lp)) { 				// current Node is not part of this Loop, so either sub-loop or super-loop
					if (lg.isChildOf(ndLoop, lp)) { 	// it's a sub-schedule. we need to check if it is allowed to be scheduled now
						if ((curScheduling == null) && (handled.containsAll(loopPreds.get(ndLoop))) && (pending.size() == 0)) { 	// we are allowed to schedule the sub-schedule now
							lp = ndLoop; 		// so set current loop to this nodes loop
							lpTime = new int[] { t, -1 }; 		// subloop starts at current time
							loopTimes.put(lp, lpTime);
						} else {
							continue; 		// try scheduling the next node
						}
					} else { 		// it's a super-loop or another sub-schedule on the same level -> check if prev. sub-schedule is fully scheduled
						if ((curScheduling == null) && (handled.containsAll(loopNodes.get(lp))) && (handled.containsAll(loopPreds.get(ndLoop)))) { 		// all nodes of current sub-schedule are scheduled and all predecessors of the new one are handled
							for (Loop sameOrHigherLvlLoops : lg.loops) {
								Loop father = lg.getFather(ndLoop);
								if (father != null && !(lg.isChildOf(sameOrHigherLvlLoops, father)) || ndLoop.equals(sameOrHigherLvlLoops)) {
									continue;
								}
								LinkedHashSet<Node> handledCopy = new LinkedHashSet<Node>();
								handledCopy.addAll(handled);
								handledCopy.retainAll(loopNodes.get(sameOrHigherLvlLoops));
								int handledCopySize = handledCopy.size(); // count how many nodes of the child loops are scheduled
								for (Node hdld : handledCopy) {
									if (isConst(hdld) || isLoad(hdld)) {
										handledCopySize--; // consts do not count as they're not really part of the loop
									}
								} // / TODO keep track of all pending loops - easier than this code to check pending loops
								if (handledCopySize != 0 && !handled.containsAll(loopNodes.get(sameOrHigherLvlLoops))) { // if we started scheduling a childloops we have to finish it
									continue cand;
								}
							}

							for (Loop lpChild : lg.getChildren(lp)) { // the child loops of the current loop have to be finished
								if (!handled.containsAll(loopNodes.get(lpChild))) {
									continue cand;
								}
							}

							if (lp != loopRoot) {
								lpTime = loopTimes.get(lp);
								lpTime[1] = t - 1; // finish time of last subloop was one time step before
							}
							if (!lg.isChildOf(lp, ndLoop)&& lg.getFather(lp).equals(lg.getFather(ndLoop))) { // a new sub-loop on the same level as the last starts here
								if (ndLoop != loopRoot) {
									lpTime = new int[] { t, -1 }; // subloop starts at current time
									loopTimes.put(ndLoop, lpTime);
								}
							} else if (lg.getFather(lp) != ndLoop) { // close all loops between lp and ndloop
								Loop father = lg.getFather(lp);
								while (father != ndLoop) {
									if (!handled.containsAll(loopNodes.get(father))) {
										continue cand;
									}
									lpTime = loopTimes.get(father);
									lpTime[1] = t - 1;
									father = lg.getFather(father);
								}
							}
							lp = ndLoop; // so set current loop to this nodes loop
						} else {
							Set<Node> fad = new LinkedHashSet<Node>(loopNodes.get(lp));
							fad.removeAll(handled);
							continue; // skip otherwise
						}
					}
				}
				peSet = findPEforNode(nd, t); // get a List of PEs sorted by
												// priority
				int cnt = 0;
				PPEE: for (int pe : peSet) {
					cnt++;
					if(cnt > peSet.size()){
						break;
					}
					if (pe == -1){
						continue;
					}
					if (!operations[pe].containsKey(nd.getOperation())){
						continue; // This PE is not compatible with the required OP
					}
					dur = operations[pe].get(nd.getOperation());
					
					
					if (peBusy(pe, t, dur)){
						continue; // PE is busy, try another one
					}

					boolean isdirectlyPlaceable = false;
					if (isConst(nd) || (isLoad(nd) && !storeMap.keySet().contains(nd.getValue()))) {
						isdirectlyPlaceable = true;
					}
//
					if (isLoad(nd) && storeMap.keySet().contains(nd.getValue())) {
						if (!lvarPEMap.containsKey(nd.getValue())) {
							isdirectlyPlaceable = true;
						}
					}
					
					if(!isdirectlyPlaceable && peBusy(pe, t+dur+5, 20)){
						continue;
					}
					
					
					if(isDMAPrefetch(nd)){
						for(int future = 0; future < 10; future++){
							if(prefetchBlocked[pe].containsKey(t+future) && !(prefetchBlocked[pe].get(t+future) == nd.getController())){
								continue PPEE;
							}
						}
					}
					
					
					Double ratio = null; 
					if(isDMA(nd)|| isDMAPrefetch(nd)){
						
						
						Node handle = nd.getPredecessor(0);
						ratio = handleReadWriteRatio.get(handle);
						if(ratio == null){
							ratio = getHandleReadWriteRatio(handle, lg.getLoop(nd));
							handleReadWriteRatio.put(handle, ratio);
						}
					}
					
					Node handle = nd.getPredecessor(0);
					Object identifier;
					if(isLoadStore(handle)){
						identifier = handle.getValue();
					} else {
						identifier = handle;
					}
					
					
					if((isDMAstore(nd)|| isDMAload(nd)) && ratio < 2 || isDMAPrefetch(nd) ){
						LinkedHashSet<Integer> prevHandlePE = null;

						LinkedHashMap<Integer,LinkedHashSet<Integer>> offsetModToPE = handleToPeMap.get(identifier);

						if(offsetModToPE != null){
							prevHandlePE = offsetModToPE.get(getOffsetModulo(nd));
						}

						
						if(prevHandlePE != null){
							if(!prevHandlePE.contains(pe) && peSet.size()>1){ // zweite bedingung muss sein, falls der Node auf dieses pe gescheduled werden MUSS
								continue;
							}
						}
					}
					
					if (nd.isSensorInput()) {
						if (!sensorAvailable(t, dur)) {
							continue;
						}
						blockSensor(t, dur);
					}
					
					
					start = placeNode(nd, pe, t); // checks and resolves data dependencies

					curScheduling = ndLoop;

					Interval ival = new Interval(start, start + dur - 1);

					if (isStore(nd)) {
						setForce(nd, pe);
					}

					if ((isIf(nd) || isDMAPrefetch(nd)) && ifNodes.contains(nd)) {
						blockCBoxWrite(nd, start, dur, currCBoxWriteSlot);
					}
					if (isConditional(nd)) {
						blockCBoxReadSlot(nd, start, dur,currCboxReadSlot);
					}

					sched.add(nd, ival);
					peMap.put(nd, pe);
					
					if(isDMA(nd)|| isDMAPrefetch(nd)){
						LinkedHashMap<Integer,LinkedHashSet<Integer>> offsetModToPE = handleToPeMap.get(identifier);

						if(offsetModToPE == null){
							offsetModToPE = new LinkedHashMap<>();
							handleToPeMap.put(identifier,offsetModToPE);
						}
					
					
						if(isDMAstore(nd)){
							LinkedHashSet<Integer> newSet = new LinkedHashSet<>();
							newSet.add(pe);
							offsetModToPE.put(getOffsetModulo(nd), newSet);
						} else if(isDMAload(nd)|| isDMAPrefetch(nd)){
							LinkedHashSet<Integer> sset = offsetModToPE.get(getOffsetModulo(nd));
							if(sset == null){
								sset = new LinkedHashSet<>();
								offsetModToPE.put(getOffsetModulo(nd),sset);
							}
							sset.add(pe);
						}
					}
					
					setBusy(pe, start, dur);
					updateAttr(nd, pe, start + dur);
					

					candidates.remove(nd);
					pending.add(nd);
					break;
				}
			}
			
			Set<Node> handledPeriStores = placePeriStores(candidates,t);
			
			candidates.removeAll(handledPeriStores);
			pending.addAll(handledPeriStores);

			t++;
		}
		
		t = sched.length() - 1;

		for (Loop loop : loopTimes.keySet()) { // loops may end at the same time -> set to sched.length-1
			lpTime = loopTimes.get(loop);
			if (lpTime[1] < 0) {
				lpTime[1] = t;
			}
			
		}
		return sched;
	}

	private Set<Node> placePeriStores(Set<Node> candidates, int t) {
		TreeSet<Node> periStore = new TreeSet<>(new periStoreComp());
		Set<Node> handledPeriStore = new LinkedHashSet<>();
		
		for (Node nd : candidates) {
			if (nd instanceof ANode.Output) {
				periStore.add(nd);
			}
		}
		
		Node pred;
		int dur;
		
		Set<Integer> pes;
		
		Set<Integer> peSet;
		
		periNodes: for (Node nd : periStore) {
			pred = nd.getPredecessor(0);
			pes = new LinkedHashSet<>();
			
			if (copyMap.containsKey(pred)) {
				for (int pe = 0; pe < nrOfPEs; pe++) {
					peSet = copyMap.get(pred)[pe];
					if (peSet != null) {
						pes.add(pe);
					}
				}
			}

			if (peMap.containsKey(pred)) {
				pes.add(peMap.get(pred));
			}
			
			if (pes.size() == 0) {
//				System.out.println("Node " + nd + " is unused. No log planned!");
				sched.add(nd, new Interval(-1, -1));
				peMap.put(nd, -1);
				handledPeriStore.add(nd);
				continue periNodes;
			}
			
			for (int predPE : pes) {
				dur = model.getPEs().get(predPE).getAvailableOperators().get(nd.getOperation()).getLatency();
				
				if (extCompatible(pred, predPE, t, dur)) {
					if (nd.getResultAddress() >= 0) {
						if (OCMAvailable(t, dur)) {
							blockOCM(t, dur);
						} else {
							continue;			//OCM already in use. Plan peri store next time
						}
					}
					if (nd.getActuatorAddress() >= 0) {
						if (actuatorAvailable(t, dur)) {
							blockActuator(t, dur);
						} else {
							continue;			//Actuator not available peri store will be done later
						}
					}
					blockExt(pred, predPE, t, dur);
					
					Interval ival = new Interval(t, t + dur - 1);
					sched.add(nd, ival);
					peMap.put(nd, predPE);
	
//					System.out.println("PE compatible for periStore: " + predPE + " - " + nd + " - " + t + " ActAddr:" + nd.getActuatorAddress());
					handledPeriStore.add(nd);
					continue periNodes;
				}
			}
		}
		
		return handledPeriStore;
	}
	
	private class periStoreComp implements Comparator<Node> {
		public int compare(Node o1, Node o2) {
			int cmp = Integer.compare(o1.getActuatorAddress(), o2.getActuatorAddress());
			if (cmp == 0) cmp = 1;
			return -cmp;
		}
	}
	
	private boolean sensorAvailable(int sTime, int dur) {
		for (int t = sTime; t < sTime + dur; t++) {
			if (sensorRead.contains(t)) {
				return false;
			}
		}
		
		return true;
	}
	
	private void blockSensor(int sTime, int dur) {
		for (int t = sTime; t < sTime + dur; t++) {
			sensorRead.add(t);
		}
	}
	
	private boolean OCMAvailable(int sTime, int dur) {
		for (int t = sTime; t < sTime + dur; t++) {
			if (OCMWrite.contains(t)) {
				return false;
			}
		}
		
		return true;
	}
	
	private void blockOCM(int sTime, int dur) {
		for (int t = sTime; t < sTime + dur; t++) {
			OCMWrite.add(t);
		}
	}
	
	private boolean actuatorAvailable(int sTime, int dur) {
		for (int t = sTime; t < sTime + dur; t++) {
			if (actuatorWrite.contains(t)) {
				return false;
			}
		}
		
		return true;
	}
	
	private void blockActuator(int sTime, int dur) {
		for (int t = sTime; t < sTime + dur; t++) {
			actuatorWrite.add(t);
		}
	}
	
	/**
	 * Array of Entries for the IDC
	 * @return the idcEntries
	 */
	public long[] getIdcEntries() {
		return idcEntries;
	}

	/**
	 * Gives the range of IDC-entries that correspond to HostInputs. Lower bound inclusive, upper bound exclusive.
	 * @return the rangeHost
	 */
	public IDCrange getRangeHost() {
		return rangeHost;
	}

	/**
	 * Gives the range of IDC-entries that correspond to ParamInputs. Lower bound inclusive, upper bound exclusive.
	 * @return the rangeParam
	 */
	public IDCrange getRangeParam() {
		return rangeParam;
	}

	/**
	 * Gives the range of IDC-entries that correspond to constants. Lower bound inclusive, upper bound exclusive.
	 * @return the rangeConst
	 */
	public IDCrange getRangeConst() {
		return rangeConst;
	}

	/**
	 * Gives the range of IDC-entries that correspond to the integration step size. Lower bound inclusive, upper bound exclusive.
	 * @return the rangeStepSize
	 */
	public IDCrange getRangeStepSize() {
		return rangeStepSize;
	}

	/**
	 * Returns a map indexed with UltraSynth-JSON-IDs for params that returns the IDC range for the given param.
	 * @return the paramRanges
	 */
	public Map<Integer, IDCrange> getParamRanges() {
		return paramRanges;
	}

	/**
	 * Returns a map indexed with UltraSynth-JSON-IDs for host results that returns the IDC range for the given result.
	 * @return the hostRanges
	 */
	public Map<Integer, IDCrange> getHostRanges() {
		return hostRanges;
	}

	/**
	 * Initialization array for all parameters
	 * @return the initParams
	 */
	public int[] getInitParams() {
		return initParams;
	}

	/**
	 * Initialization array for all Constants
	 * @return the initConsts
	 */
	public int[] getInitConsts() {
		return initConsts;
	}

	long[] idcEntries;
	private IDCrange rangeHost;
	private IDCrange rangeParam;
	private IDCrange rangeConst;
	private IDCrange rangeStepSize;
	private Map<Integer,IDCrange> paramRanges;
	private Map<Integer,IDCrange> hostRanges;
	private int[] initParams;
	private int[] initConsts;

	long[][] contextsLocalLog;
	long[] contextsGlobalLog;
	long[] contextsActuator;
	long[] contextsOCMAddr;
	long[] contextsOCM;
	long[] contextsSensor;
	
	public long[][] getContextsLocalLog() {
		return contextsLocalLog;
	}

	public long[] getContextsGlobalLog() {
		return contextsGlobalLog;
	}

	public long[] getContextsActuator() {
		return contextsActuator;
	}
	
	public long[] getContextsOCMAddr(){
		return contextsOCMAddr;
	}
	
	public long[] getContextsOCM(){
		return contextsOCM;
	}
	
	public long[] getContextsSensor(){
		return contextsSensor;
	}
	
	
	long[][] contextsPE;
	long[][] contextsCBox;
	long[] contextsControlUnit;
	long[] contextsHandleCompare;
	
	public long[][] getContextsPE() {
		return contextsPE;
	}

	public long[][] getContextsCBox() {
		return contextsCBox;
	}

	public long[] getContextsControlUnit() {
		return contextsControlUnit;
	}
	
	public long[] getContextsHandleCompare(){
		return contextsHandleCompare;
	}
	
	ScheduleRepresentationPE[][] schedRepPE;
	ScheduleRepresentationCBox[][] schedRepCBox;
	LinkedHashMap<Integer,LinkedHashSet<Integer>>[] schedRepConnections;

	private static final int AXIBurstLength = 255;	//256 AXI Burst minus start address
	
	
	/**
	 * @return An ID-indexed array containing the offset in the OCM
	 */
	public int[] getOcmOffsets() {
		return ocmOffsets;
	}

	/**
	 * @return An ID-indexed array containing the offset in the log memory
	 */
	public int[] getLogOffsets() {
		return logOffsets;
	}

	/**
	 * Maps Ultrasynth OCM IDs to address offsets in OCM
	 */
	int[] ocmOffsets;
	
	/**
	 * Maps Ultrasynth Log IDs to address offsets in external Memory
	 */
	int[] logOffsets;
	
	
	/**
	 * Generate the additional Contexts for UltraSynth
	 */
	public void ctxtGenerationUltrasynth() {
		CgraModelUltrasynth modelU = (CgraModelUltrasynth) model;
		
		LogGlobalContext maskLogAddr = modelU.getLogInterface().getGlobalContext();
		InterfaceContext[] maskLocalLog = new InterfaceContext[nrOfPEs];
		
		OCMInterface.Context maskOcm = modelU.getOcmInterface().getGatherContext();
		LogGlobalContext maskOcmAddr = modelU.getOcmInterface().getOutputContext();
		
		ActorInterface.Context maskAct = modelU.getActorInterface().getContext();
		
		InterfaceContext maskSensor = modelU.getSensorInterface().getContext();
		
		ContextMaskPE[] maskPE = new ContextMaskPE[nrOfPEs];
		
		contextsLocalLog = new long[nrOfPEs][sched.length()];
		contextsGlobalLog = new long[modelU.getLogInterface().getGlobalContextSize()];
		contextsActuator = new long[sched.length()];
		contextsOCMAddr = new long[modelU.getOcmInterface().getOutputContextSize()];
		contextsOCM = new long[sched.length()];
		contextsSensor = new long[sched.length()];
		
		Set<Node> nds;
		int addr,regAddr;
		
		int[] ocmValuesBuffered = new int[sched.length()];
		int[] logValuesPerPE = new int[nrOfPEs];
		int[] totalLogValuesBuffered = new int[sched.length()];
		
		int[] peLogWriteAddr = new int[nrOfPEs];
		
		int numAct = 0;
		int numOcm = 0;
		int numLog = 0;
		
		int maxOcmAddr = 0;
		int maxLogAddr = 0;
		
		int numConsCopies = 0;
		int numHostCopies = 0;
		int numParaCopies = 0;
		
		HashMap<Node,Map<Integer,Integer>> ndsCons = new LinkedHashMap<>();
		HashMap<Node,Map<Integer,Integer>> ndsHost = new LinkedHashMap<>();
		HashMap<Node,Map<Integer,Integer>> ndsPara = new LinkedHashMap<>();
		
		TreeSet<ANode> ndsHostSort = new TreeSet<>(new NodeArrayIDComp());
		TreeSet<ANode> ndsParaSort = new TreeSet<>(new NodeArrayIDComp());
		
		int regFileSlot;
		
		// for (int t = 0; t < sched.length(); t++) {
		// nds = sched.nodes(t);
		for (Node nd : graph) {
			if (!sched.nodes().contains(nd)) {		// If this node has not been scheduled
				if (nd instanceof Parameter) {				// Add to Maps for input sorting
					numParaCopies++;
					addToConstMap(ndsPara, nd, -1, -1);
					ndsParaSort.add((ANode) nd);
				}
				if (nd instanceof Constant) {
					numConsCopies++;
					addToConstMap(ndsCons, nd, -1, -1);
				}
				if (nd instanceof HostInput) {
					numHostCopies++;
					addToConstMap(ndsHost, nd, -1, -1);
					ndsHostSort.add((ANode) nd);
				}
				continue;							// Skip it
			}
			for (int pe = 0; pe < nrOfPEs; pe++) {
				if (copyMap.get(nd) != null && copyMap.get(nd)[pe] != null && copyMap.get(nd)[pe].contains(0)) { // At time = 0 local vars and constants are declared in copyMap
					regFileSlot = peBindMap[pe].get(nd);

					if (nd instanceof Parameter) {
						numParaCopies++;
						addToConstMap(ndsPara, nd, pe, regFileSlot);
						ndsParaSort.add((ANode) nd);
					}
					if (nd instanceof Constant) {
						numConsCopies++;
						addToConstMap(ndsCons, nd, pe, regFileSlot);
					}
					if (nd instanceof HostInput) {
						numHostCopies++;
						addToConstMap(ndsHost, nd, pe, regFileSlot);
						ndsHostSort.add((ANode) nd);
					}
				}
			}

			if (nd instanceof ANode.Output) {
				// Actuator stores
				addr = nd.getActuatorAddress();
				if (addr >= 0) {
					numAct++;
				}

				// OCM stores
				addr = nd.getResultAddress();
				if (addr >= 0) {
					numOcm++;
					if (addr > maxOcmAddr) maxOcmAddr = addr;
				}

				// Log stores
				addr = nd.getLogAddress();
				if (addr >= 0) {
					numLog++;
					if (addr > maxLogAddr) maxLogAddr = addr;
				}
			}
		}
		// }

		IDP uGraph = (IDP) graph;
		
		ocmOffsets = new int[maxOcmAddr+1];
		logOffsets = new int[maxLogAddr+1];
		
		int regAddrWidth,maxRegAddrWidth=0;
		
		for (int pe=0; pe < nrOfPEs; pe++) {
			//Arrays are initialized to 0 by language spec
//			peLogWriteAddr[pe] = 0;
			maskLocalLog[pe] = modelU.getPeComponents().get(pe).getLogContext();
			maskPE[pe] = model.getPEs().get(pe).getContextMaskPE();
			regAddrWidth = maskPE[pe].getRegAddrWidthRead();
			if (maxRegAddrWidth < regAddrWidth) {
				maxRegAddrWidth = regAddrWidth;
			}
		}
		
		Node stepsizeNode = uGraph.getStepsize();

		idcEntries = new long[numConsCopies + numHostCopies + numParaCopies];
		rangeHost = new IDCrange(0, numHostCopies);
		rangeParam = new IDCrange(numHostCopies, numHostCopies+numParaCopies);
		rangeConst = new IDCrange(numHostCopies+numParaCopies, numHostCopies+numParaCopies+numConsCopies);
		
		paramRanges = new LinkedHashMap<>();
		hostRanges = new LinkedHashMap<>();

		int idcIdx = 0;
		int rangeLower = 0;
		int rangeUpper = 0;

		Map<Integer,Integer> peSlot;
		
		for (Node nd : ndsHostSort) {
			peSlot = ndsHost.get(nd);
			for (int pe : peSlot.keySet()) {
				if (pe < 0) {		//If no pe is assigned, we simply skip this node
					idcIdx++;
				} else {
					idcEntries[idcIdx++] = (pe << maxRegAddrWidth) | peSlot.get(pe);
					rangeUpper++;
				}
			}
			hostRanges.put(((ANode) nd).getArrayID(), new IDCrange(rangeLower, rangeUpper));
			if (rangeLower == rangeUpper) {		//This value is not used in CGRA
				rangeLower++;					//increase ranges, because they're sent anyways
				rangeUpper++;
			} else {
				rangeLower = rangeUpper;
			}
		}
		
		rangeLower = numHostCopies;
		rangeUpper = rangeLower;
		
		initParams = new int[ndsPara.size()];
		initConsts = new int[ndsCons.size()];
		
		int initIdx = 0;
		
		for (Node nd : ndsParaSort) {
			initParams[initIdx++] = nd.getValue();
			
			peSlot = ndsPara.get(nd);
			for (int pe : peSlot.keySet()) {
				if (pe < 0) {		//If no pe is assigned, we simply skip this node
					idcIdx++;
				} else {
					idcEntries[idcIdx++] = (pe << maxRegAddrWidth) | peSlot.get(pe);
					rangeUpper++;
				}
			}
			paramRanges.put(((ANode) nd).getArrayID(), new IDCrange(rangeLower, rangeUpper));
			if (rangeLower == rangeUpper) {		//This value is not used in CGRA
				rangeLower++;					//increase ranges, because they're sent anyways
				rangeUpper++;
			} else {
				rangeLower = rangeUpper;
			}
		}
		
		initIdx = 0;
		
		for (Node nd : ndsCons.keySet()) {
			initConsts[initIdx++] = nd.getValue();
			
			rangeLower = idcIdx;
			peSlot = ndsCons.get(nd);
			for (int pe : peSlot.keySet()) {
				if (pe < 0) {		//If no pe is assigned, we simply skip this node
					idcIdx++;
				} else {
					idcEntries[idcIdx++] = (pe << maxRegAddrWidth) | peSlot.get(pe);
				}
			}
			if (nd == stepsizeNode) {
				rangeStepSize = new IDCrange(rangeLower, idcIdx);
			}
		}
		
		int actuatorValuesWritten = 0;
		
		int ocmIdx = 0;
		int ocmCtxIdx = 0;
		
		int ocmToBurst = 0;
		int ocmValuesWrittenOut = 0;
		int ocmCCNTstart;
		
		int logIdx = 0;
		int logCtxIdx = 0;
		
		int logToBurst = 0;
		int logValuesWrittenOut = 0;
		int logCCNTstart = 0;
		int logCCNTnewStart;
		
		for (int t=0; t < sched.length(); t++) {
			if (t<sched.length()-1) {
				ocmValuesBuffered[t+1] = ocmValuesBuffered[t];	//make sure the number of written values is copied
				totalLogValuesBuffered[t+1] = totalLogValuesBuffered[t];
			}
			//Initialize contexts to zero
			//Arrays are initialized to 0 by language spec
//			contextsOCM[t] = 0;
//			contextsOCMAddr[t] = 0;
//			contextsActuator[t] = 0;
//			contextsGlobalLog[t] = 0;
//			contextsSensor[t] = 0;
			
			logCCNTnewStart = t-totalLogValuesBuffered[t] + 1;						//In every time step a new CCNT-start is compared
			if (logCCNTnewStart > logCCNTstart) logCCNTstart = logCCNTnewStart;		//if we have to start later shift CCNTstart
			
			//Get nodes for current time step
			nds = sched.nodes(t);
			
			for (int pe=0; pe < nrOfPEs; pe++) {
				//Initialize context to zero
				//Arrays are initialized to 0 by language spec
//				contextsLocalLog[pe][t] = 0;
				
				for (Node nd : nds) {
					if (peMap.get(nd) == pe) {
						if (nd instanceof ANode.Output) {
							
							//Actuator stores
							addr = nd.getActuatorAddress();
							if (addr >= 0) {
								contextsActuator[t] = maskAct.setAddr(contextsActuator[t], addr);	//Set Actuator address
								contextsActuator[t] = maskAct.setEnable(contextsActuator[t], 1);	//Set Enable
								contextsActuator[t] = maskAct.setPeID(contextsActuator[t], pe);		//Set PE ID to take value from
								actuatorValuesWritten++;
								if (actuatorValuesWritten >= numAct) {
									contextsActuator[t] = maskAct.setSyncOut(contextsActuator[t], 1);	//Mark last value to be written out
								}
							}
							
							//OCM stores
							addr = nd.getResultAddress();
							if (addr >= 0) {
								contextsOCM[t] = maskOcm.setPeID(contextsOCM[t], pe);				//Set PE ID to take OCM value from
								contextsOCM[t] = maskOcm.setEnable(contextsOCM[t], 1);				//Set Enable to write to buffer
//								ocmValuesBuffered[t+1]++;											//Value will be in Buffer in the next time step
//								OCMNodeTime.put(nd, t+1);
//								OCMBuffer.add(nd);
								ocmOffsets[addr] = ocmIdx;											//Store the order of appearance in OffsetArray to map OCM-IDs to Offset in OCM
								if (ocmToBurst == 0) {
									ocmToBurst = Math.max(AXIBurstLength, numOcm-ocmValuesWrittenOut);
									contextsOCMAddr[ocmCtxIdx] = maskOcmAddr.setAWvalid(contextsOCMAddr[ocmCtxIdx], 1);
									contextsOCMAddr[ocmCtxIdx] = maskOcmAddr.setBurstLength(contextsOCMAddr[ocmCtxIdx], ocmToBurst-1);	//Burst length n means transmit n+1 values
									ocmCtxIdx++;
								}
								if (ocmToBurst > 0) {
									contextsOCMAddr[ocmCtxIdx] = maskOcmAddr.setReadAddr(contextsOCMAddr[ocmCtxIdx], ocmIdx++);			//Set the read address for the context
									ocmToBurst--;
									ocmValuesWrittenOut++;
									if (numOcm <= ocmValuesWrittenOut) {
										contextsOCMAddr[ocmCtxIdx] = maskOcmAddr.setDone(contextsOCMAddr[ocmCtxIdx], 1);				//Set done when it was the last value
										ocmCtxIdx++;
										
										ocmCCNTstart = t+1-numOcm;																		//Only one OCM per time step -> start in time to be finished when last value is present
										for (; ocmCtxIdx < contextsOCMAddr.length; ocmCtxIdx++) {										//Fill remaining slots with CCNT start value
											contextsOCMAddr[ocmCtxIdx] = maskOcmAddr.setCcntStart(contextsOCMAddr[ocmCtxIdx], ocmCCNTstart);
										}
									}
									ocmCtxIdx++;
								}
							}
							
							//Log stores
							addr = nd.getLogAddress();
							if (addr >= 0) {
								contextsLocalLog[pe][t] = maskLocalLog[pe].setAddr(contextsLocalLog[pe][t], peLogWriteAddr[pe]++);		//Set local log address
								contextsLocalLog[pe][t] = maskLocalLog[pe].setEnable(contextsLocalLog[pe][t], 1);						//Set enable to write to PE's log buffer
								logOffsets[addr] = logIdx++;
								totalLogValuesBuffered[t+1]++;																			//in the next time step it will be in buffer
								
								if (logToBurst == 0) {
									logToBurst = Math.max(AXIBurstLength, numLog - logValuesWrittenOut);
									contextsGlobalLog[logCtxIdx] = maskLogAddr.setAWvalid(contextsGlobalLog[logCtxIdx], 1);					
									contextsGlobalLog[logCtxIdx] = maskLogAddr.setBurstLength(contextsGlobalLog[logCtxIdx], logToBurst-1);			//set the burst length
									logCtxIdx++;
								}
								if (logToBurst > 0) {
									contextsGlobalLog[logCtxIdx] = maskLogAddr.setReadAddr(contextsGlobalLog[logCtxIdx], logValuesPerPE[pe]++);		//set read address per PE
									contextsGlobalLog[logCtxIdx] = maskLogAddr.setLogID(contextsGlobalLog[logCtxIdx], pe);
									logToBurst--;
									logValuesWrittenOut++;
									if (numLog <= logValuesWrittenOut) {
										contextsGlobalLog[logCtxIdx] = maskLogAddr.setDone(contextsGlobalLog[logCtxIdx], 1);				//Set done when it was the last value
										logCtxIdx++;
										
										for (; logCtxIdx < contextsGlobalLog.length; logCtxIdx++) {											//Fill remaining slots with CCNT start value
											contextsGlobalLog[logCtxIdx] = maskLogAddr.setCcntStart(contextsGlobalLog[logCtxIdx], logCCNTstart);
										}
									}
									logCtxIdx++;
								}
							}
							
							//Sensor reads
							addr = nd.getSensorAddress();
							if (addr >= 0) {
								contextsSensor[t] = maskSensor.setEnable(contextsSensor[t], 1);			//Set read enable
								contextsSensor[t] = maskSensor.setAddr(contextsSensor[t], addr);		//Set sensor address
								
								if (sched.slot(nd).ubound == t+1-model.getPEs().get(pe).getAvailableOperators().get(nd.getOperation()).getOutputLatency()) {
									regAddr = getAddress(nd, pe);
									contextsPE[pe][t] = maskPE[pe].setAddrWr(contextsPE[pe][t], regAddr);
									contextsPE[pe][t] = maskPE[pe].setWriteEnable(contextsPE[pe][t], true);
									schedRepPE[pe][t].setOperationRFaddr(regAddr);
									contextsPE[pe][t] = maskPE[pe].setMuxRegSourceBased(contextsPE[pe][t], RegfileMuxSource.LIVEIN);
								}
							}
						}
					}
				}
			}		//pe
		}		//t
	}
	
	private class NodeArrayIDComp implements Comparator<ANode> {

		@Override
		public int compare(ANode o1, ANode o2) {
			return Integer.compare(o1.getArrayID(), o2.getArrayID());
		}
	}

	private void addToConstMap(HashMap<Node, Map<Integer, Integer>> ndsConst, Node nd, int pe, int slot) {
		Map<Integer, Integer> peSlot;
		
		peSlot = ndsConst.get(nd);
		
		if (peSlot == null) {
			peSlot = new TreeMap<>();
			ndsConst.put(nd, peSlot);
		}
		
		peSlot.put(pe, slot);
	}

	/**
	 * Textually generate the contexts required for CGRA execution
	 */
	@SuppressWarnings("unchecked")
	public void ctxtGeneration() {
		
		 schedRepPE = new ScheduleRepresentationPE[nrOfPEs][sched.length()];
		 schedRepCBox= new ScheduleRepresentationCBox[nrOfCBoxes][sched.length()];
		 schedRepConnections = new LinkedHashMap[sched.length()];
		
		Set<Node> nds;
		Node[] operands;
		Node n;

		int regAddr, cBoxAddr;
		boolean makeCopy;
		ContextMaskCBoxEvaluationBlock maskcbox = model.getcBoxModel().getContextmaskEvaLuationBlocks();
		ContextMaskContextControlUnit maskccu = model.getContextmaskccu();
		
		contextsPE = new long[nrOfPEs][sched.length()];
		ContextMaskPE[] mask = new ContextMaskPE[nrOfPEs];
		for (int i = 0; i < nrOfPEs; i++) {
			mask[i] = model.getPEs().get(i).getContextMaskPE();
			for (int j = 0; j < sched.length(); j++) {
				contextsPE[i][j] = 0;
				schedRepPE[i][j] = new ScheduleRepresentationPE();
			}
		}
		
		contextsCBox = new long[nrOfCBoxes][sched.length()];
		for(int box = 0; box < nrOfCBoxes; box++){
			for (int j = 0; j < sched.length(); j++) {
				contextsCBox[box][j] = 0;
				schedRepCBox[box][j] = new ScheduleRepresentationCBox();
				
			}}

		contextsControlUnit = new long[sched.length()];
		for (int j = 0; j < sched.length(); j++) {
			contextsControlUnit[j] = model.getContextMemorySize() - 1;
			schedRepConnections[j] = new LinkedHashMap<>();
			for(int pe = 0; pe < nrOfPEs; pe++){
				schedRepConnections[j].put(pe, new LinkedHashSet<>());
			}
		}
		
		int inpLatency;
		int ndStart;
		
		Node ctrl,findCtrl;

		for (int t = 0; t < sched.length(); t++) {
			// System.out.println("t=" + t + ":");
			nds = sched.nodes(t);
			for (int pe = 0; pe < nrOfPEs; pe++) {
				// System.out.println("\tPE" + pe + ":");
				LinkedList<Integer> connections = interconnect[pe];

				n = null;

				for (Node nd : nds) {
					if (peMap.get(nd) == pe) {
						
						if (!nd.getOperation().isRegfileStore()) {
							contextsPE[pe][t] = mask[pe].setOperation(contextsPE[pe][t],
									model.getPEs().get(pe).getAvailableNonNativeOperators().get(nd.getOperation()).getOpcode());
						} else {
							contextsPE[pe][t] = mask[pe].setOperation(contextsPE[pe][t],
									model.getPEs().get(pe).getAvailableNonNativeOperators().get(Processor.Instance.getNOP()).getOpcode()); // This is actually no op but a copy
						}
						
						schedRepPE[pe][t].setOperation(nd);
						operands = nd.getPredecessors();
//						if (((sched.slot(nd).lbound == t) || (input64(nd) && sched.slot(nd).lbound + 1 == t)) && (operands != null)) { // in addresses are only required during first time step (first 2 for 64 bit)
						inpLatency = model.getPEs().get(pe).getAvailableOperators().get(nd.getOperation()).getInputLatency();
						ndStart = sched.slot(nd).lbound;
						if (((ndStart <= t) && (t < ndStart + inpLatency)) && (operands != null)) {		//operands are required for inputLatency
							schedRepPE[pe][t].setDuration(sched.slot(nd).length()+1);
							for (int i = 0; i < operands.length; i++) {
								boolean assigned = false;
//								if ((nd.getOperation() == Processor.Instance.getOperatorByName("DMA_STORE")
//										|| nd.getOperation() == Processor.Instance.getOperatorByName("DMA_STORE64")) && i == 2) {
								if (nd.getOperation().isCacheStore() && (outDelays.get(nd.getOperation()) == 1 || i == 2)) {
									for (int outpe = 0; outpe < nrOfPEs; outpe++) {
										if (pe != outpe && outNodeMap.containsKey(t) && outNodeMap.get(t)[outpe] == operands[2]) {
											boolean isConnected = false;
											int src = 0;
											for (src = 0; src < connections.size(); src++) {
												if (connections.get(src) == outpe) {
													isConnected = true;
													break;
												}
											}

											if (!isConnected){
												continue;		// The data may be available at outpe but outpe is not connected to pe -> search further
											}

											contextsPE[pe][t] = mask[pe].setMuxA(contextsPE[pe][t], src);
											schedRepPE[pe][t].setIn2PE(outpe);
											schedRepPE[pe][t].setIn2Node(operands[2]);
											schedRepPE[pe][t].setIn2RFAddr(getAddress(operands[2], outpe));
											assigned = true;
											if(pe != outpe){
												schedRepConnections[t].get(outpe).add(pe);
											}
											break;
										}
									}
									if (intNodeMap.containsKey(t) && intNodeMap.get(t)[pe] == operands[2]) {
										contextsPE[pe][t] = mask[pe].setMuxA(contextsPE[pe][t], connections.size());
										schedRepPE[pe][t].setIn2PE(pe);
										schedRepPE[pe][t].setIn2Node(operands[2]);
										schedRepPE[pe][t].setIn2RFAddr(getAddress(operands[2], pe));
										assigned = true;
									}
									
								} else {
									if (intNodeMap.containsKey(t) && intNodeMap.get(t)[pe] == operands[i]) {
										int src = connections.size();
										if (i == 0) {
											contextsPE[pe][t] = mask[pe].setMuxA(contextsPE[pe][t], src);
											schedRepPE[pe][t].setIn0PE(pe);
											schedRepPE[pe][t].setIn0Node(operands[i]);
											schedRepPE[pe][t].setIn0RFAddr(getAddress(operands[i], pe));
											assigned = true;
										} else {
											contextsPE[pe][t] = mask[pe].setMuxB(contextsPE[pe][t], src);
											schedRepPE[pe][t].setIn1PE(pe);
											schedRepPE[pe][t].setIn1Node(operands[i]);
											schedRepPE[pe][t].setIn1RFAddr(getAddress(operands[i], pe));
											assigned = true;
										}

										continue;		//next operand
									}
									for (int outpe = 0; outpe < nrOfPEs; outpe++) {
										if (pe != outpe && outNodeMap.containsKey(t) && outNodeMap.get(t)[outpe] == operands[i]) {

											boolean isConnected = false;
											int src = 0;
											for (src = 0; src < connections.size(); src++) {
												if (connections.get(src) == outpe) {
													isConnected = true;
													break;
												}
											}

											if (!isConnected){
												continue;		// The data may be available at outpe but outpe is not connected to pe -> search further
											}

											if (i == 0) {
												contextsPE[pe][t] = mask[pe].setMuxA(contextsPE[pe][t], src);
												schedRepPE[pe][t].setIn0PE(outpe);
												schedRepPE[pe][t].setIn0Node(operands[i]);
												schedRepPE[pe][t].setIn0RFAddr(getAddress(operands[i], outpe));
												schedRepConnections[t].get(outpe).add(pe);
												assigned = true;
											} else {
												contextsPE[pe][t] = mask[pe].setMuxB(contextsPE[pe][t], src);
												schedRepPE[pe][t].setIn1PE(outpe);
												schedRepPE[pe][t].setIn1Node(operands[i]);
												schedRepPE[pe][t].setIn1RFAddr(getAddress(operands[i], outpe));
												schedRepConnections[t].get(outpe).add(pe);
												assigned = true;
											}

											break;		//PE
										}
									}
								}
								if (assigned == false && i != 0) {
									System.err.println("Operand " + operands[i] + " for Node " + nd + " no context assigend");
								}
							}
						}

						LinkedHashSet<Node> consumerss = graph.getConsumers(nd);

						if (!isIf(nd) && !isDMAstore(nd) || isDMAPrefetch(nd) && consumerss!= null && consumerss.size()!=0) {
							if (output64(nd)) {
								if (sched.slot(nd).ubound - 1 == t) {
									regAddr = getAddress(nd, pe);
									contextsPE[pe][t] = mask[pe].setAddrWr(contextsPE[pe][t], regAddr);
									contextsPE[pe][t] = mask[pe].setWriteEnable(contextsPE[pe][t], true);
									schedRepPE[pe][t].setOperationRFaddr(regAddr);
								} else if (sched.slot(nd).ubound == t) {
									regAddr = getHighAddress(nd, pe);
									contextsPE[pe][t] = mask[pe].setAddrWr(contextsPE[pe][t], regAddr);
									contextsPE[pe][t] = mask[pe].setWriteEnable(contextsPE[pe][t], true);
									schedRepPE[pe][t].setOperationRFaddr(regAddr);
								}
							} else {
								if (sched.slot(nd).ubound == t) {
									regAddr = getAddress(nd, pe);
									contextsPE[pe][t] = mask[pe].setAddrWr(contextsPE[pe][t], regAddr);
									contextsPE[pe][t] = mask[pe].setWriteEnable(contextsPE[pe][t], true);
									schedRepPE[pe][t].setOperationRFaddr(regAddr);
								}
							}
						}
						if (isDMAload(nd) || isDMAPrefetch(nd)) {
							contextsPE[pe][t] = mask[pe].setMuxReg(contextsPE[pe][t], 2); // TODO!!!! nicht hardkodieren, sondern über parameter holen
						}

						if (isConditional(nd) && ((sched.slot(nd).ubound == t) || (output64(nd) && sched.slot(nd).ubound -1 == t)|| isDMA(nd))) {
							ctrl = nd.getController();
							if (ctrl == null) {				//get controller from fused store
								findCtrl = nd;
								if (sFuseNodes.containsValue(nd)) {
									for (Node ndkey : sFuseNodes.keySet()) {
										if (sFuseNodes.get(ndkey) == findCtrl) {
											ctrl = ndkey.getController();
											break;
										}
									}
								}
							}
							cBoxAddr = cBoxAlloc.get(ctrl);
							int cboxPort = getCBoxSelect(nd,t);
							schedRepCBox[cboxPort/cBoxOutputsPerBox][t].setOutAddr(cBoxAddr, cboxPort%cBoxOutputsPerBox);
							schedRepCBox[cboxPort/cBoxOutputsPerBox][t].setOutputDecision(nd.getDecision(), cboxPort%cBoxOutputsPerBox);
							schedRepCBox[cboxPort/cBoxOutputsPerBox][t].setOutputToPEs(nd.getController(), cboxPort%cBoxOutputsPerBox);
							schedRepPE[pe][t].setOperationConditional(true);
							schedRepPE[pe][t].setCBoxSelect(cboxPort);
							
							if (!nd.getDecision()) {
								cBoxAddr += cBoxSize >> 1;
							}
							contextsCBox[cboxPort/cBoxOutputsPerBox][t] = maskcbox.setReadAddressPredication(contextsCBox[cboxPort/cBoxOutputsPerBox][t], cBoxAddr, cboxPort%cBoxOutputsPerBox);
							contextsPE[pe][t] = mask[pe].setWriteEnableConditional(contextsPE[pe][t], true);
							contextsPE[pe][t] = mask[pe].setCBoxSel(contextsPE[pe][t], cboxPort);
							
							if (isDMA(nd) || isDMAPrefetch(nd)) {
								contextsPE[pe][t] = mask[pe].setDmaConditional(contextsPE[pe][t], true);
							}
						}

						if (((isIf(nd) || isDMAPrefetch(nd)) && ifNodes.contains(nd)) && (sched.slot(nd).ubound == t)) {
							int cboxWriteSelect = cboxSelectWrite.get(nd);
							
							
							if (maskcbox.getInputMuxWidth() > 1) {
								contextsCBox[cboxWriteSelect][t] = maskcbox.setInputMux(contextsCBox[cboxWriteSelect][t], maskcbox.getSlot(pe));
							}
							contextsCBox[cboxWriteSelect][t] = maskcbox.setWriteEnable(contextsCBox[cboxWriteSelect][t], true);
							cBoxAddr = cBoxAlloc.get(nd);
							
							schedRepCBox[cboxWriteSelect][t].setInAddr(cBoxAddr);
							schedRepCBox[cboxWriteSelect][t].setInPE(pe);
							schedRepCBox[cboxWriteSelect][t].setInput(nd);
							contextsCBox[cboxWriteSelect][t] = maskcbox.setWriteAddressPositive(contextsCBox[cboxWriteSelect][t], cBoxAddr);
							contextsCBox[cboxWriteSelect][t] = maskcbox.setWriteAddressNegative(contextsCBox[cboxWriteSelect][t], cBoxAddr + (cBoxSize >> 1));
							for (Loop lp : loopTimes.keySet()) {
								for (Node lpNd : lp.getController()) {
									if (lpNd.equals(nd)) {
										int jumpval = loopTimes.get(lp)[1] + 1;
										if (jumpval >= sched.length()) {
											Loop father = lg.getFather(lp);
											if (father == null) {
												jumpval = model.getContextMemorySize() - 1;
												contextsControlUnit[t] = maskccu.setCounter(contextsControlUnit[t],jumpval);
												contextsControlUnit[t] = maskccu.setJump(contextsControlUnit[t], true);
												contextsControlUnit[t] = maskccu.setConditional(contextsControlUnit[t],true);
												contextsControlUnit[t] = maskccu.setRelative(contextsControlUnit[t],false);//THIS Marks an absolute Conditional jump
											} else {
												jumpval = loopTimes.get(father)[0];
												contextsControlUnit[t] = maskccu.setCounter(contextsControlUnit[t],jumpval-t);
												contextsControlUnit[t] = maskccu.setJump(contextsControlUnit[t], true);
												contextsControlUnit[t] = maskccu.setConditional(contextsControlUnit[t],true);
												contextsControlUnit[t] = maskccu.setRelative(contextsControlUnit[t],true);
											}
										} else {
											Loop father = lg.getFather(lp); // father and child finish at the same time -> child has to jump to start of father after exit 
											int[] lpTFather = loopTimes.get(father);
											if(jumpval-1==lpTFather[1]){
												jumpval = lpTFather[0];
											}
											contextsControlUnit[t] = maskccu.setCounter(contextsControlUnit[t],jumpval-t);
											contextsControlUnit[t] = maskccu.setJump(contextsControlUnit[t], true);
											contextsControlUnit[t] = maskccu.setConditional(contextsControlUnit[t],true);
											contextsControlUnit[t] = maskccu.setRelative(contextsControlUnit[t],true);
										}
									}
								}
							}
							

							if (nd.getController() == null) {
								contextsCBox[cboxWriteSelect][t] = maskcbox.setBypassAndPositive(contextsCBox[cboxWriteSelect][t], true);
								contextsCBox[cboxWriteSelect][t] = maskcbox.setBypassAndNegative(contextsCBox[cboxWriteSelect][t], true);
								contextsCBox[cboxWriteSelect][t] = maskcbox.setBypassOrPositive(contextsCBox[cboxWriteSelect][t], true);
								contextsCBox[cboxWriteSelect][t] = maskcbox.setBypassOrNegative(contextsCBox[cboxWriteSelect][t], true);
							} else {
								cBoxAddr = cBoxAlloc.get(nd.getController());
								if (!nd.getDecision()) { // decision for this ifNode is false
									cBoxAddr += cBoxSize >> 1; // address must be in upper half
								}
								contextsCBox[cboxWriteSelect][t] = maskcbox.setReadAddressPredication(contextsCBox[cboxWriteSelect][t], cBoxAddr, getCBoxSelect(nd,t)%cBoxOutputsPerBox);

								if (nd.getShortCircuitEvaluationTrueBranch()) {
									cBoxAddr = cBoxAlloc.get(nd.getShortCircuitEvaluationTrueBranchController());
									if (!nd.getShortCircuitEvaluationTrueBranchControllerDecision()) {
										cBoxAddr += cBoxSize >> 1;
									}
									contextsCBox[cboxWriteSelect][t] = maskcbox.setReadAddressOrPositive(contextsCBox[cboxWriteSelect][t], cBoxAddr);
								} else {
									contextsCBox[cboxWriteSelect][t] = maskcbox.setBypassOrPositive(contextsCBox[cboxWriteSelect][t], true);
								}

								if (nd.getShortCircuitEvaluationFalseBranch()) {
									cBoxAddr = cBoxAlloc.get(nd.getShortCircuitEvaluationFalseBranchController());
									if (!nd.getShortCircuitEvaluationFalseBranchControllerDecision()) {
										cBoxAddr += cBoxSize >> 1;
									}
									contextsCBox[cboxWriteSelect][t] = maskcbox.setReadAddressOrNegative(contextsCBox[cboxWriteSelect][t], cBoxAddr);
								} else {
									contextsCBox[cboxWriteSelect][t] = maskcbox.setBypassOrNegative(contextsCBox[cboxWriteSelect][t], true);
								}
							}
						}
						break; // not more than one Node can be scheduled on a pe
						
					}
				}

				copyMap: for (Node nd : copyMap.keySet()) {
					if (copyMap.get(nd) != null && copyMap.get(nd)[pe] != null){
						for (Integer tcopy : copyMap.get(nd)[pe]) {
							makeCopy = false;

							if (output64(nd)) {
								if (tcopy == t + 1 || tcopy == t + 2){
									makeCopy = true; // copymap declares available copies -> must look into future to find out when to make a copy
								}
							} else {
								if (tcopy == t + 1){
									makeCopy = true;
								}
							}

							if (makeCopy) {
								if (isLoadStore(nd) && lvarPEMap.containsKey(nd.getValue()) && lvarPEMap.get(nd.getValue()) == pe){
									continue; // this node is a load after store and is only redeclared -> not a real copy to produce
								}

								for (int outpe = 0; outpe < nrOfPEs; outpe++) {
									if (outNodeMap.containsKey(t) && outNodeMap.get(t)[outpe] == nd) {
										boolean isConnected = false;
										int src = 0;
										for (src = 0; src < connections.size(); src++) {
											if (connections.get(src) == outpe) {
												isConnected = true;
												break;
											}
										}

										if (!isConnected){
											continue;// The data may be available at outpe but outpe is not connected to pe -> search further
										}
										contextsPE[pe][t] = mask[pe].setMuxA(contextsPE[pe][t], src);

										contextsPE[pe][t] = mask[pe].setOperation(contextsPE[pe][t],
												model.getPEs().get(pe).getAvailableNonNativeOperators().get(Processor.Instance.getNOP()).getOpcode());
										schedRepPE[pe][t].setOperation(nd);
										schedRepPE[pe][t].setCopying(true);
										
										schedRepConnections[t].get(outpe).add(pe);
										// Do nothing except write the value

										break;
									}
								}

								LinkedHashSet<Node> consumerss = graph.getConsumers(nd);
								
								if (!isIf(nd) && !isDMAstore(nd) || isDMAPrefetch(nd) && consumerss!= null && consumerss.size()!=0) if (output64(nd)) {
									if (tcopy == t + 2) {
										regAddr = getAddress(nd, pe);
										contextsPE[pe][t] = mask[pe].setAddrWr(contextsPE[pe][t], regAddr);
										contextsPE[pe][t] = mask[pe].setWriteEnable(contextsPE[pe][t], true);
										schedRepPE[pe][t].setOperationRFaddr(regAddr);
									} else if (tcopy == t + 1) {
										regAddr = getHighAddress(nd, pe);
										contextsPE[pe][t] = mask[pe].setAddrWr(contextsPE[pe][t], regAddr);
										contextsPE[pe][t] = mask[pe].setWriteEnable(contextsPE[pe][t], true);
										schedRepPE[pe][t].setOperationRFaddr(regAddr);
									}
								} else {
									regAddr = getAddress(nd, pe);
									contextsPE[pe][t] = mask[pe].setAddrWr(contextsPE[pe][t], regAddr);
									contextsPE[pe][t] = mask[pe].setWriteEnable(contextsPE[pe][t], true);
									schedRepPE[pe][t].setOperationRFaddr(regAddr);
								}

								break copyMap;
							}
						}
					}
				}

				if (intNodeMap.containsKey(t) && ((n = intNodeMap.get(t)[pe]) != null)) {
					if (output64(n)) {
						if (highLowTrackerIntern[pe] == null) {
							highLowTrackerIntern[pe] = new LinkedHashMap<Node, Boolean>();
						}
						if (highLowTrackerIntern[pe].get(n) == null || !highLowTrackerIntern[pe].get(n)) {
							highLowTrackerIntern[pe].put(n, true);
							regAddr = getAddress(n, pe);
						} else {
							regAddr = getHighAddress(n, pe);
							highLowTrackerIntern[pe].put(n, false);
						}
						contextsPE[pe][t] = mask[pe].setAddrMux(contextsPE[pe][t], regAddr);
					} else {
						regAddr = getAddress(n, pe);
						contextsPE[pe][t] = mask[pe].setAddrMux(contextsPE[pe][t], regAddr);
					}
				}

				Node outNode = null;
				if (outNodeMap.containsKey(t) && ((n = outNodeMap.get(t)[pe]) != null)) {
					if (output64(n)) {
						if (highLowTrackerExtern[pe] == null) {
							highLowTrackerExtern[pe] = new LinkedHashMap<Node, Boolean>();
						}
						if (highLowTrackerExtern[pe].get(n) == null || !highLowTrackerExtern[pe].get(n)) {
							highLowTrackerExtern[pe].put(n, true);
							regAddr = getAddress(n, pe);
						} else {
							regAddr = getHighAddress(n, pe);
							highLowTrackerExtern[pe].put(n, false);
						}
						contextsPE[pe][t] = mask[pe].setAddrDo(contextsPE[pe][t], regAddr);
					} else {
						regAddr = getAddress(n, pe);
						contextsPE[pe][t] = mask[pe].setAddrDo(contextsPE[pe][t], regAddr);
						outNode = n;
						schedRepPE[pe][t].setOutNode(outNode);
						schedRepPE[pe][t].setOutAddr(regAddr);
					}
				}

				if (dmaNodeMap.containsKey(t) && ((n = dmaNodeMap.get(t)[pe]) != null)) {
					regAddr = getAddress(n, pe);
					contextsPE[pe][t] = mask[pe].setAddrCache(contextsPE[pe][t], regAddr);
					schedRepPE[pe][t].setIn0PE(pe);
					schedRepPE[pe][t].setIn0Node(n);
					schedRepPE[pe][t].setIn0RFAddr(getAddress(n, pe));
				}
			}
		}

		LinkedHashMap<Integer, Integer> innerMostmapping = new LinkedHashMap<Integer, Integer>(); // When nested loops have the same stop (key), the back jump goes to the start of the innermost. The innermost is identified by the hightest start (value)
		for (Loop lp : loopNodes.keySet()) {
			int start = loopTimes.get(lp)[0];
			int stop = loopTimes.get(lp)[1];
			Integer startVal = innerMostmapping.get(stop);
			if (startVal == null || startVal.intValue() < start) {
				contextsControlUnit[stop] = maskccu.setCounter(contextsControlUnit[stop],start-stop);
				contextsControlUnit[stop] = maskccu.setJump(contextsControlUnit[stop], true);
				contextsControlUnit[stop] = maskccu.setConditional(contextsControlUnit[stop],false);
				contextsControlUnit[stop] = maskccu.setRelative(contextsControlUnit[stop],true);
				innerMostmapping.put(stop, start);
			}
		}

		stop = System.nanoTime();


	}

	LinkedHashMap<Integer,Node> lvRepresenter = new LinkedHashMap<>();
	TreeMap<Integer, LinkedHashSet<Node>> dmas = new TreeMap<>();
	
	public void aliasCheck(LinkedHashMap<Node, LinkedHashSet<Node>> potentialAliases, AliasingSpeculation aliasingSpeculation) throws NotSchedulableException{
		switch(aliasingSpeculation){
		case EXACT_CHECK:
			aliasCheckExact(potentialAliases);
			break;
		case INDEX_CHECK:
			aliasCheckIndex(potentialAliases);
			break;
		case PREDICATION_CHECK:
			aliasCheckPredication(potentialAliases);
			break;
		case PESSIMISTIC_CHECK:
			aliasCheckPessimistic(potentialAliases);
			break;
		default:
			break;	
		}
	}
	
	
	private void aliasCheckExact(LinkedHashMap<Node, LinkedHashSet<Node>> potentialAliases){
	}
	
	private void aliasCheckIndex(LinkedHashMap<Node, LinkedHashSet<Node>> potentialAliases){
		
	}
	
	private void aliasCheckPredication(LinkedHashMap<Node, LinkedHashSet<Node>> potentialAliases){

	}

	private void aliasCheckPessimistic(LinkedHashMap<Node, LinkedHashSet<Node>> potentialAliases) throws NotSchedulableException{
		
		
		LinkedHashMap<ReferenceComparePair,Node> comps = new LinkedHashMap<ReferenceComparePair,Node>();
		LinkedHashMap<Integer,TreeSet<Node>> stores = new LinkedHashMap<>();
		
		
		
		for(Node n : graph){
			if(n.getOperation().equals(Amidar.OP.STORE)){
				Integer value = n.getValue();
				TreeSet<Node> stor = stores.get(value);
				if(stor == null){
					stor = new TreeSet<>();
					stores.put(value, stor);
				}
				stor.add(n);
			}
			
			if(n.getOperation().equals(Amidar.OP.STORE) || n.getOperation().equals(Amidar.OP.LOAD)){
				if(!lvRepresenter.containsKey(n.getValue())){
					lvRepresenter.put(n.getValue(), n);
				}
			}
		}
		
		
		
		
		for(Node firstDMA: potentialAliases.keySet()){
			if(isPrefetch[firstDMA.getAddress()]){
				continue;
			}
			
			int tFirstDMA = sched.slot(firstDMA).lbound;
			

			
			for(Node secondDMA: potentialAliases.get(firstDMA)){
				if(isPrefetch[secondDMA.getAddress()]){
					continue;
				}
				
				int tSecondDMA = sched.slot(secondDMA).lbound;
				
				Node earlierDMA = secondDMA, laterDMA = firstDMA;
				int tEarlierDMA = tSecondDMA, tLaterDMA = tFirstDMA;
				if(tSecondDMA > tFirstDMA){
					continue;
				}
				
//				if(tEarlierDMA == tLaterDMA && earlierDMA.getOperation().equals(Amidar.OP.DMA_STORE) && laterDMA.getOperation().equals(Amidar.OP.DMA_LOAD) ){
//					continue; // the store has to be executed after the load -> happens bc load reads the old value -> no compare necessary // TODO NOT MODELLED IN SIM
//				}




				//////////////////////////////////////////// DMA1 /////////////////////////////////////////////////////
				Node ref1 = graph.getOperands(earlierDMA, 0);
				ReferenceCompare comp1;
				int start = 0;
				int stop = sched.length()-1;
				int t = sched.slot(earlierDMA).lbound;

				// when the reference is loaded from a local variable, we try to find the biggest interval in which the
				// value of the reference doesn't change
				if(isLoadStore(ref1)){
					TreeSet<Node> store = stores.get(ref1.getValue());
					if(store != null){
						for(Node nst: store){
							int tst = getSlotForNode(nst).ubound;
							if(t< tst){
								stop = tst;
								break;
							} else {
								start = tst+1;
							}
						}
					}
					comp1 = new ReferenceCompare(ref1, earlierDMA, start, stop);
				} else { // if not, the ref is loaded from the heap and thus is only valid at that point of time
					t = sched.slot(ref1).ubound+1;
					comp1 = new ReferenceCompare(ref1, earlierDMA, t, t);
				}


				//////////////////////////////////////////// DMA2 /////////////////////////////////////////////////////
				Node ref2 = graph.getOperands(laterDMA, 0);

				ReferenceCompare comp2;
				start = 0;
				stop = sched.length()-1;
				t = sched.slot(laterDMA).lbound;

				// when the reference is loaded from a local variable, we try to find the biggest interval in which the
				// value of the reference doesn't change
				if(isLoadStore(ref2)){
					TreeSet<Node> store = stores.get(ref2.getValue());
					if(store != null){
						for(Node nst: store){
							int tst = getSlotForNode(nst).ubound;
							if(t< tst){
								stop = tst;
								break;
							} else {
								start = tst+1;
							}
						}
					}
					comp2 = new ReferenceCompare(ref2, laterDMA, start, stop);
				} else { // if not, the ref is loaded from the heap and thus is only valid at that point of time
					t = sched.slot(ref2).ubound+1;
					comp2 = new ReferenceCompare(ref2, laterDMA, t, t);
				}
				

				
				Node criticalWrite = null;
				int tCriticalWrite = Integer.MAX_VALUE;
				if(earlierDMA.getOperation().equals(Amidar.OP.DMA_STORE)){
					criticalWrite = earlierDMA; 
				} else {
					LinkedHashSet<Node> successors = new LinkedHashSet<>();
					
					if(graph.getConsumers(earlierDMA) == null){
						continue;
					}
					
					successors.addAll(graph.getConsumers(earlierDMA));
					while(successors.size() != 0){
						LinkedHashSet<Node> toAdd = new LinkedHashSet<>();
						for(Node successor: successors){
							if(successor.getOperation().isCacheStore() || successor.getOperation().isRegfileStore() || successor.getOperation().isControlFlow() && loopControllers.contains(successor)){
								Interval slot = getSlotForNode(successor);
								if(slot != null && slot.lbound < tCriticalWrite){
									tCriticalWrite = slot.lbound;
									criticalWrite = successor;
								}
							} else if(successor.getOperation().isControlFlow()){
								toAdd.addAll(graph.getAllSuccessors(successor));
							}else {
								if(graph.getConsumers(successor) != null)
									toAdd.addAll(graph.getConsumers(successor));
							}
						}
						successors.clear();
						successors.addAll(toAdd);
						
					}
					
					
					
				}
				
				
				

				ReferenceComparePair pair = new ReferenceComparePair(comp1, comp2);
				
				
				Node oldCriticalWrite = comps.get(pair);
				
				if(oldCriticalWrite == null || (getSlotForNode(oldCriticalWrite).lbound > getSlotForNode(criticalWrite).lbound)){
					comps.put(pair, criticalWrite);
				}
			}
		}
		
		
		
		
		for(ReferenceComparePair pair: comps.keySet()){
			int tRef1 = pair.start1;
			int tRef2 = pair.start2;
			
			int tEarliest  = Math.max(tRef1, tRef2);
			

			int tLatest = getSlotForNode(comps.get(pair)).lbound -1; 

			
			
			if(tLatest< tEarliest){
				int diff = tEarliest - tLatest;
				insertEmptyTimesteps(tEarliest, diff);
				tLatest+=diff;
				if(diff > 1 ){
					// should never occur
					throw new RuntimeException();
				}
				
			}

			boolean loopComplications = false;
			
			for(Loop lp : loopTimes.keySet()){
				
				
				// First access to the handle is in a nested loop
				if(((loopTimes.get(lp)[0] > tEarliest) && (loopTimes.get(lp)[0] <= tLatest))){
					tLatest = loopTimes.get(lp)[0]-1;
					
				}
				
				// Handle is loaded in a subloop 
				if( ((loopTimes.get(lp)[1] >tEarliest) && (loopTimes.get(lp)[1] <= tLatest))){
					loopComplications = true;
				}
				
				if((loopTimes.get(lp)[0] == tEarliest) && !lp.equals(lg.getRoot())){
					insertEmptyTimesteps(tEarliest, 1);
					tLatest++;
				}
				
				
			}
			
			if(loopComplications){
				System.err.println("LOOOOOOOOOP");
			}
			
			int actualT = sched.length();
			int actualPE = 0;
			
			
			Integer pe1 = peMap.get(pair.ref1);
			Integer pe2 = peMap.get(pair.ref2);
			
			int tEarlierRef, tLaterRef;
			int peEarlierRef, peLaterRef;
			if(pe1!=null && pe2 != null){
				if(tRef1 < tRef2){
					tEarlierRef = tRef1;
					tLaterRef = tRef2;
					peEarlierRef = pe1;
					peLaterRef = pe2;
				} else {
					tEarlierRef = tRef2;
					tLaterRef = tRef1;
					peEarlierRef = pe2;
					peLaterRef = pe1;
				}


				for(int cmpPE = 0; cmpPE < nrOfPEs; cmpPE ++){

					List<Integer> path1 = getPath(peEarlierRef, cmpPE);
					List<Integer> path2 = getPath(peLaterRef, cmpPE);
					int currentT1 = tEarlierRef +1;
					if(path1.size()>0){
						int prevPE = path1.remove(0);
						while(!path1.isEmpty()){
							if(!peBusy(path1.get(0),currentT1, 1) && (outNodeMap.get(currentT1) == null || outNodeMap.get(currentT1)[prevPE] == null)){
								prevPE = path1.remove(0);
							}
							currentT1++;
						}
					} else {
						while(peBusy(peEarlierRef,currentT1, 1)){
							currentT1++;
						}
					}

					int currentT2  = tLaterRef +1 ;
					if(path2.size()>0){
						int prevPE = path2.remove(0);
						while(!path2.isEmpty()){
							if(!peBusy(path2.get(0),currentT2, 1) && (outNodeMap.get(currentT2) == null || outNodeMap.get(currentT2)[prevPE] == null)){
								prevPE = path2.remove(0);
							}
							currentT2++;
						}
					}else {
						while(peBusy(peLaterRef,currentT2, 1)){
							currentT2++;
						}
					}

					int tTestPossible = Math.max(currentT2, currentT1);
					if(tTestPossible < actualT){
						actualT = tTestPossible;
						actualPE = cmpPE;
					}
				}
			} else {
				int min = tLatest-tEarliest+1;
				int bestPE = 100;
				for(int pe = 0; pe < nrOfPEs; pe ++){
					int val = 0;
					for(int t = tEarliest; t <= tLatest; t++){
						if(peBusy(pe, t, 1)){
							val++;
						}
					}
					if(val <= min){
						min = val;
						bestPE = pe;
					}
					
				}
				actualPE = bestPE;
				actualT = tEarliest;
						
			}
			
				Node controller = null;
				Boolean decision = null;
				Node controllerRef1 = pair.ref1.getController();
				Boolean decisionRef1= pair.ref1.getDecisionTristate();
				Node controllerRef2 = pair.ref2.getController();
				Boolean decisionRef2= pair.ref2.getDecisionTristate();
				
				
				// we may not use controllers for local variables
				// as the handle may have been overwritten (or not) so we have to check anyways
				if(isLoadStore(pair.ref1)){
					controllerRef1 = null;
				}
				if(isLoadStore(pair.ref2)){
					controllerRef2 = null;
				}
				// only if the the handle is directly taken from a DMA load - then
				// the controller might prevent the DMA_LOAD and the handle is 0
				// so the handle is always zero which might lead to false positives
				
				
				if(controllerRef1 == null && controllerRef2 != null){
					controller = controllerRef2;
					decision = decisionRef2;
				} else if(controllerRef1 != null && controllerRef2 == null){
					controller = controllerRef1;
					decision = decisionRef1;
				} else if(controllerRef1 != null && controllerRef2 != null){
					Node controllerRef2copy = controllerRef2;
					
					while(controllerRef2copy != null){
						if(controllerRef1.equals(controllerRef2copy)){
							
							// In this case  controllerRef2 is the stronger condition
							controller = controllerRef2;
							decision = decisionRef2;
							break;
						} else {
							controllerRef2copy = controllerRef2copy.getController();
						}
					}
					if(controller == null){
						Node controllerRef1copy = controllerRef1;
						
						while(controllerRef1copy != null){
							if(controllerRef2.equals(controllerRef1copy)){
								
								// In this case  controllerRef1 is the stronger condition
								controller = controllerRef1;
								decision = decisionRef1;
								break;
							} else {
								controllerRef1copy = controllerRef1copy.getController();
							}
						}
						if(controller == null){
							// Both controllers are not correlated - we have to compute the and conjunction of both (using CBox)
							Node earlierController;
							Node laterController;
							Boolean earlierDecision;
							Boolean laterDecision;
							if(sched.slot(controllerRef1).lbound > sched.slot(controllerRef2).lbound){
								earlierController = controllerRef2;
								laterController = controllerRef1;
								earlierDecision = decisionRef2;
								laterDecision = decisionRef1;
							} else {
								earlierController = controllerRef1;
								laterController = controllerRef2;
								earlierDecision = decisionRef1;
								laterDecision = decisionRef2;
							}
							
							
							Node newController = new Node(laterController.getAddress()+1,laterController.getOperation(),earlierController, earlierDecision);
							graph.setDataDependency(graph.getOperands(laterController, 0), newController, 0);
							graph.setDataDependency(graph.getOperands(laterController, 1), newController, 1);
							graph.setControlDependency(earlierController, newController);
							maxCmpTime = tLatest - 1;
							
							ifNodes.add(newController);
							int cmptT = placeNode(newController, peMap.get(laterController), 0); // TODO FIND BETTER PE?
							
							blockCBoxReadSlot(newController	, cmptT, 1,currCboxReadSlot);
							blockCBoxWrite(newController, cmptT, 1, currCBoxWriteSlot);
							
							tLatest = maxCmpTime+1;
							maxCmpTime = Integer.MAX_VALUE;
							sched.add(newController, new Interval(cmptT, cmptT));
							peMap.put(newController, peMap.get(laterController));
							setBusy(peMap.get(laterController), cmptT, 1);
							
							
							
							controller = newController;
							decision = laterDecision;
						} 
					}
				}
				
				Loop lp1 = null;
				Loop lp2 = null;
				
				
				for(Loop loop: loopTimes.keySet()){
					int[] interval = loopTimes.get(loop);
					int startT = interval[0];
					int stopT = interval[1];
					
					if(pair.start1 >= startT &&  pair.start1 <= stopT && pair.stop1 >= startT &&  pair.stop1 <= stopT){
						if(lp1 == null || lp1.contains(loop)){
							lp1 = loop;
						}
					}
					
					if(pair.start2 >= startT &&  pair.start2 <= stopT && pair.stop2 >= startT &&  pair.stop2 <= stopT){
						if(lp2 == null || lp2.contains(loop)){
							lp2 = loop;
						}
					}
						
				}
				
				Loop lp;
				if(lp1.contains(lp2)){
					lp = lp2;
				} else if(lp2.contains(lp1)) {
					lp = lp1;
				} else if(lp2.equals(lp1)){
					lp = lp1;
				} else {
					lp = lg.getRoot();
				}
				int address = lp.getStart() + id++;
			
				
				Node cmparator = new Node(address, Amidar.OP.HANDLE_CMP, controller, decision);
				graph.setDataDependency(pair.ref1, cmparator, 0);
				graph.setDataDependency(pair.ref2, cmparator, 1);
				if(controller != null){
					graph.setControlDependency(controller, cmparator);
				}
				
				maxCmpTime = tLatest;
				actualT = placeNode(cmparator, actualPE, tEarliest);
				
				if (controller != null){
					blockCBoxReadSlot(cmparator	, actualT, 1, currCboxReadSlot);
				}
				
				
				tLatest = maxCmpTime;
				maxCmpTime = Integer.MAX_VALUE;
				sched.add(cmparator, new Interval(actualT, actualT));
				peMap.put(cmparator, actualPE);
				setBusy(actualPE, actualT, 1);
				
				
				if(actualT >= getSlotForNode(comps.get(pair)).lbound){
					System.err.println("************************************************** WRONG ******************++");
				}
				
		}
	}

	int id = 0;
	
	private class ReferenceCompare{
		
		private Node ref;
		
		private Node refRep;
		
		private Node dma;
		
		private int tStart = 0;
		private int tStop = 0;
		

		public ReferenceCompare(Node ref, Node dma, int start, int stop){
			this.ref = ref;
			this.dma = dma;
			tStart = start;
			tStop = stop;
			
			if(ref.getOperation().equals(Amidar.OP.STORE) || ref.getOperation().equals(Amidar.OP.LOAD)){
				refRep = lvRepresenter.get(ref.getValue());
			} else {
				refRep = ref;
			}
			
		}
		
		
		public boolean equals(Object o){
			boolean retVal;
			if(o instanceof ReferenceCompare){
				ReferenceCompare oRC = (ReferenceCompare)o;
				retVal = ((tStart == oRC.tStart) && (tStop == oRC.tStop) && (refRep.equals(oRC.refRep)));
			} else {
				retVal = false;
			}
			
			return retVal;
		}
		
		
		public int hashCode(){
			return LinkedHashCode(tStart, LinkedHashCode(tStop, refRep.hashCode()));
		}
		
		
		public String toString(){
			return "DMA: " + dma + "\tREF: " + ref+"  at PE "+ peMap.get(ref) + "\tT: " + tStart + "-"+ tStop + "\tSchedAt: " + sched.slot(dma).lbound ;
		}
		
		
		
	}
	
	private class ReferenceComparePair {
		
		Node ref1;
		Node ref2;
		
		int start1;
		int start2;
		
		int stop1;
		int stop2;
		
		ReferenceComparePair(ReferenceCompare comp1, ReferenceCompare comp2){
			ref1 = comp1.refRep;
			ref2 = comp2.refRep;
			start1 = comp1.tStart;
			start2 = comp2.tStart;
			stop1 = comp1.tStop;
			stop2 = comp2.tStop;
		}
		
		public boolean equals(Object o){
			boolean retVal;
			if(o instanceof ReferenceComparePair){
				ReferenceComparePair oRC = (ReferenceComparePair)o;
				retVal = (ref1 == oRC.ref1 && ref2 == oRC.ref2 && start1 == oRC.start1 && start2 == oRC.start2); 
			} else {
				retVal = false;
			}
			
			return retVal;
		}
		
		
		public int hashCode(){
			return LinkedHashCode(LinkedHashCode(start2,ref1.hashCode()), LinkedHashCode(start1,ref2.hashCode()));
		}
		
		
		public String toString(){
			return ref1.toString() + "\n  " + ref2.toString();
		}
		
		
		
		
		
	}
	
	public int LinkedHashCode(int v1, int v2) {
		return (v1 << 5) ^ ((v1 & 0xf8000000) >> 27) ^ v2;
	}
	
	public enum AliasingSpeculation{
		/**
		 * No speculation at all
		 */
		OFF,
		/**
		 * Check speculation exactly - same handle, same index and positive predication signal
		 */
		EXACT_CHECK,
		/**
		 * Check only if same handle and same index
		 */
		INDEX_CHECK,
		/**
		 * Check only if same handle and positive predication signal
		 */
		PREDICATION_CHECK,
		/**
		 * Check only if the handle is the same
		 */
		PESSIMISTIC_CHECK,
		/**
		 * Do not check at all - Programmer has to make sure that there is not aliasing
		 */
		NO_CHECK
	}
	
	/**
	 * Get the address inside the regfile for a given node
	 * 
	 * @param n
	 *            - Node of interest
	 * @param pe
	 *            - PE of interest
	 * @return The address if available, -1 otherwise
	 */
	private int getAddress(Node n, int pe) {
		
		if(!n.isIndirectConstant() && isConst(n) && Math.abs(n.getValue()) < model.getPEs().get(pe).getRegfilesize()/2){
			
			int regfileSize = model.getPEs().get(pe).getRegfilesize();
			
			int mask = -1 << (int)(Math.log(regfileSize)/Math.log(2));
			mask = ~mask;
			
			int val = n.getValue();
			val = val& mask;
			val +=regfileSize;
			
			return val;
		}
		
		if (peBindMap[pe].containsKey(n)){
			return peBindMap[pe].get(n);
		}

		if (vNodes.containsKey(n)) for (Node nd : vNodes.get(n)) {
			if (isStore(nd)) {
				n = nd;
				break;
			}
		}

		if (isLoad(n)) { // loads do not have store addresses. is this required?
			if (lvarNodeMap[pe].containsKey(n.getValue())) {
				n = lvarNodeMap[pe].get(n.getValue());
			}

			return peBindMap[pe].get(n);
		}

		Node nd;

		if (isStore(n)) {
			nd = lvarNodeMap[pe].get(n.getValue());

			if (nd == null) {
				nd = storeMap.get(n.getValue());
			}

			return peBindMap[pe].get(nd);
		}
		return -1;
	}

	/**
	 * Get the address of the higher address part of a 64-bit operation
	 * 
	 * @param n
	 *            - Node of interest
	 * @param pe
	 *            - PE of interest
	 * @return The address if available, -1 otherwise
	 */
	private int getHighAddress(Node n, int pe) {
		Node nd = null;

		if (twinNodes.containsKey(n)) {
			nd = twinNodes.get(n);
			if (peBindMap[pe].containsKey(nd)){
				return peBindMap[pe].get(nd);
			}
		}

		if (vNodes.containsKey(n)){
			for (Node ndV : vNodes.get(n)) {
				if (isStore(ndV)) {
					n = ndV;
					if (twinNodes.containsKey(ndV)){
						n = twinNodes.get(ndV);
					}
					break;
				}
			}
		}

		if (isLoad(n)) {

			if (lvarNodeMap[pe].containsKey(n.getValue())) {
				n = lvarNodeMap[pe].get(n.getValue());
			}
			if (twinNodes.containsKey(n)){
				n = twinNodes.get(n);
			}

			return peBindMap[pe].get(n);
		}

		if (isStore(n)) {
			nd = lvarNodeMap[pe].get(n.getValue());

			if (nd == null) {
				nd = storeMap.get(n.getValue());
			}

			if (twinNodes.containsKey(nd)){
				nd = twinNodes.get(nd);
			}
			return peBindMap[pe].get(nd);
		}

		return -1;
	}

	// Stores Tokens
	TreeMap<Integer, TreeSet<RegID>> constants = new TreeMap<Integer, TreeSet<RegID>>();

	TreeMap<Long, TreeSet<RegID>> longConstants = new TreeMap<Long, TreeSet<RegID>>();
	TreeMap<Long, TreeSet<RegID>> longConstantsHigh = new TreeMap<Long, TreeSet<RegID>>();

	TreeMap<Integer, TreeSet<RegID>> constantsInd = new TreeMap<Integer, TreeSet<RegID>>();

	TreeMap<Long, TreeSet<RegID>> longConstantsInd = new TreeMap<Long, TreeSet<RegID>>();
	TreeMap<Long, TreeSet<RegID>> longConstantsHighInd = new TreeMap<Long, TreeSet<RegID>>();

	TreeMap<Integer, TreeSet<RegID>> lVars = new TreeMap<Integer, TreeSet<RegID>>();

	TreeMap<Integer, TreeSet<RegID>> longLVars = new TreeMap<Integer, TreeSet<RegID>>();
	TreeMap<Integer, TreeSet<RegID>> longLVarsHigh = new TreeMap<Integer, TreeSet<RegID>>();

	TreeMap<Integer, TreeSet<RegID>> sendLVars = new TreeMap<Integer, TreeSet<RegID>>();

	TreeMap<Integer, TreeSet<RegID>> sendlongLVars = new TreeMap<Integer, TreeSet<RegID>>();
	TreeMap<Integer, TreeSet<RegID>> sendlongLVarsHigh = new TreeMap<Integer, TreeSet<RegID>>();

	private void addToken(TreeMap<Integer, TreeSet<RegID>> tokenList, int value, int PE, int addr) {
		TreeSet<RegID> c = tokenList.get(value);
		if (c == null) {
			c = new TreeSet<RegID>();
			tokenList.put(value, c);
		}
		c.add(new RegID(PE, addr));
	}

	private void addToken(TreeMap<Long, TreeSet<RegID>> tokenList, long value, int PE, int addr) {
		TreeSet<RegID> c = tokenList.get(value);
		if (c == null) {
			c = new TreeSet<RegID>();
			tokenList.put(value, c);
		}
		c.add(new RegID(PE, addr));
	}

	private class RegID implements Comparable<RegID> {
		int PE = 0;
		int addr = 0;

		RegID(int PE, int addr) {
			this.PE = PE;
			this.addr = addr;
		}

		public int compareTo(RegID o) {
			int ret = Integer.compare(this.addr, o.addr);
			if (ret == 0) {
				ret = Integer.compare(this.PE, o.PE);
			}
			return ret;
		}
	}

	int[] tokenSet;
	int nrLocalVarSend;
	int nrIndirectConst;
	int nrDirectConst;
	int nrLocalVarReceive;
	
	ArrayList<Integer> constantMemory = new ArrayList<>();
	ArrayList<Integer> constantMemoryIndirect = new ArrayList<>();
	ArrayList<LocationInformation> locationInformation = new ArrayList<>();
	ArrayList<Integer> localVariables = new ArrayList<>();

	public int getNrLocalVarSend() {
		return nrLocalVarSend;
	}

	public int getNrIndirectConst() {
		return nrIndirectConst;
	}

	public int getNrDirectConst() {
		return nrDirectConst;
	}

	public int getNrLocalVarReceive() {
		return nrLocalVarReceive;
	}
	
	public ArrayList<LocationInformation> getLocationInformation() {
		return locationInformation;
	}

	public ArrayList<Integer> getLocalVariables() {
		return localVariables;
	}

	public ArrayList<Integer> getConstantMemory() {
		return constantMemory;
	}
	
	public ArrayList<Integer> getConstantMemoryIndirect() {
		return constantMemoryIndirect;
	}

	/**
	 * Generate the Data required for CGRA execution (constant memory, location information...)
	 */
	public void initDataGeneration() throws NotSchedulableException {


		Node allocNode;

		for (Node nd : copyMap.keySet()) {
			for (int pe = 0; pe < nrOfPEs; pe++) {
				if (copyMap.get(nd) != null && copyMap.get(nd)[pe] != null && copyMap.get(nd)[pe].contains(0)) { // At time = 0 local vars and constants are declared in copyMap
					if (isConst(nd)) {
						if (output64(nd)) {
							if (nd.isIndirectConstant()) {
								addToken(longConstantsInd, nd.getValueLong(), pe, peBindMap[pe].get(nd));
								addToken(longConstantsHighInd, nd.getValueLong(), pe, peBindMap[pe].get(twinNodes.get(nd)));
							} else {
								addToken(longConstants, nd.getValueLong(), pe, peBindMap[pe].get(nd));
								addToken(longConstantsHigh, nd.getValueLong(), pe, peBindMap[pe].get(twinNodes.get(nd)));
							}
						} else {
							if (nd.isIndirectConstant()) {
								addToken(constantsInd, nd.getValue(), pe, peBindMap[pe].get(nd));
							} else {
								if(Math.abs(nd.getValue()) >= model.getPEs().get(pe).getRegfilesize()/2){
									addToken(constants, nd.getValue(), pe, peBindMap[pe].get(nd));
								}
							}
						}
					}
					if (isLoad(nd)) {
						if (lvarNodeMap[pe].containsKey(nd.getValue())) {
							allocNode = lvarNodeMap[pe].get(nd.getValue());
						} else {
							allocNode = nd;
						}
						if (output64(nd)) {
							addToken(longLVars, nd.getValue(), pe, peBindMap[pe].get(allocNode));
							addToken(longLVarsHigh, nd.getValue(), pe, peBindMap[pe].get(twinNodes.get(allocNode)));
						} else {
							addToken(lVars, nd.getValue(), pe, peBindMap[pe].get(allocNode));
						}
					}
				}
			}
		}
		

		int pe = 0;
		Node nd;

		for (int addr : storeMap.keySet()) {
			pe = lvarPEMap.get(addr);
			nd = lvarNodeMap[pe].get(addr);

			if (nd == null) {
				nd = storeMap.get(addr);
			}

			if (output64(nd)) {
				if (addr < maxNrOfLocalVariables) {
					addToken(sendlongLVars, addr, pe, peBindMap[pe].get(nd));
					addToken(sendlongLVarsHigh, addr, pe, peBindMap[pe].get(twinNodes.get(nd)));
				}
			} else {
				if (addr < maxNrOfLocalVariables) {
					addToken(sendLVars, addr, pe, peBindMap[pe].get(nd));
				}
			}
		}
		
		//////////////////////////////////CONST //////////////////////////////
		for (Integer i : constants.keySet()) {
			int previousAddress = -1;
			LocationInformation locInfo = new LocationInformation(nrOfPEs);
			constantMemory.add(i);
			for (RegID rid : constants.get(i)) {
				if (previousAddress != rid.addr && previousAddress != -1) {
					locInfo.setRegisterFileAddress(previousAddress);
					locationInformation.add(locInfo);
					constantMemory.add(i);
					locInfo = new LocationInformation(nrOfPEs);
					previousAddress = rid.addr;
				}
				if (previousAddress == -1) {
					previousAddress = rid.addr;
				}
				locInfo.getPESelect()[rid.PE] = true;

			}
			locInfo.setRegisterFileAddress(previousAddress);
			locationInformation.add(locInfo);
		}

		for (Long i : longConstants.keySet()) {
			int previousAddress = -1;
			LocationInformation locInfo = new LocationInformation(nrOfPEs);
			constantMemory.add((int) (i & 0xFFFFFFFFL));
			TreeSet<RegID> longConst = longConstants.get(i);
			TreeSet<RegID> longConstHigh = longConstantsHigh.get(i);
			Iterator<RegID> longConstIt = longConst.iterator();
			Iterator<RegID> longConstItHigh = longConstHigh.iterator();

			while (longConstIt.hasNext()) {
				RegID low = longConstIt.next();

				if (previousAddress != low.addr && previousAddress != -1) {
					locInfo.setRegisterFileAddress(previousAddress);
					locationInformation.add(locInfo);
					constantMemory.add((int) (i & 0xFFFFFFFFL));
					locInfo = new LocationInformation(nrOfPEs);
					previousAddress = low.addr;
				}
				if (previousAddress == -1) {
					previousAddress = low.addr;
				}
				locInfo.getPESelect()[low.PE] = true;
			}
			locInfo.setRegisterFileAddress(previousAddress);
			locationInformation.add(locInfo);

			longConstItHigh = longConstHigh.iterator();
			locInfo = new LocationInformation(nrOfPEs);
			previousAddress = -1;
			constantMemory.add((int) ((i >> 32) & 0xFFFFFFFFL));
			while (longConstItHigh.hasNext()) {
				RegID high = longConstItHigh.next();

				if (previousAddress != high.addr && previousAddress != -1) {
					locInfo.setRegisterFileAddress(previousAddress);
					locationInformation.add(locInfo);
					constantMemory.add((int) ((i >> 32) & 0xFFFFFFFFL));
					locInfo = new LocationInformation(nrOfPEs);
					previousAddress = high.addr;
				}
				if (previousAddress == -1) {
					previousAddress = high.addr;
				}

				locInfo.getPESelect()[high.PE] = true;
			}

			locInfo.setRegisterFileAddress(previousAddress);
			locationInformation.add(locInfo);
		}

		nrDirectConst = constantMemory.size();
		 ////////////////////////////////// CONST IND /////////////////////////////////////////////////////////
		for (Integer i : constantsInd.keySet()) {
			int previousAddress = -1;
			LocationInformation locInfo = new LocationInformation(nrOfPEs);
			constantMemoryIndirect.add(i);
			for (RegID rid : constantsInd.get(i)) {
				if (previousAddress != rid.addr && previousAddress != -1) {
					locInfo.setRegisterFileAddress(previousAddress);
					locationInformation.add(locInfo);
					constantMemoryIndirect.add(i);
					locInfo = new LocationInformation(nrOfPEs);
					previousAddress = rid.addr;
				}
				if (previousAddress == -1) {
					previousAddress = rid.addr;
				}
				locInfo.getPESelect()[rid.PE] = true;

			}
			locInfo.setRegisterFileAddress(previousAddress);
			locationInformation.add(locInfo);
		}
//
//		// System.out.println("Receive indirect 64 bit constants:");
//		for (Long i : longConstantsInd.keySet()) {
//			int previousAddress = -1;
//			int tokenRid = 0;
//			tokenSet.add((int) (i & 0xFFFFFFFFL));
//			tokenSetPointer++;
//			TreeSet<RegID> longConstInd = longConstantsInd.get(i);
//			TreeSet<RegID> longConstHighInd = longConstantsHighInd.get(i);
//			Iterator<RegID> longConstIt = longConstInd.iterator();
//			Iterator<RegID> longConstItHigh = longConstHighInd.iterator();
//
//			while (longConstIt.hasNext()) {
//				RegID low = longConstIt.next();
//				RegID high = longConstItHigh.next();
//
//				if (previousAddress != low.addr && previousAddress != -1) {
//					tokenRid |= previousAddress << model.getNrOfPEs()+2;
//					tokenSet.add(tokenRid);
//					tokenSetPointer++;
//					tokenSet.add((int) (i & 0xFFFFFFFFL));
//					tokenSetPointer++;
//					tokenRid = 0;
//					previousAddress = low.addr;
//				}
//				if (previousAddress == -1) {
//					previousAddress = low.addr;
//				}
//				tokenRid |= 1 << low.PE;
//
//			}
//			tokenRid |= previousAddress << model.getNrOfPEs()+2;
//			tokenSet.add(tokenRid);
//			tokenSetPointer++;
//
//			longConstItHigh = longConstHighInd.iterator();
//			tokenRid = 0;
//			previousAddress = -1;
//			tokenSet.add((int) ((i >> 32) & 0xFFFFFFFFL));
//			tokenSetPointer++;
//			while (longConstItHigh.hasNext()) {
//				RegID high = longConstItHigh.next();
//
//				if (previousAddress != high.addr && previousAddress != -1) {
//					tokenRid |= previousAddress << model.getNrOfPEs()+2;
//					tokenSet.add(tokenRid);
//					tokenSetPointer++;
//					tokenSet.add((int) ((i >> 32) & 0xFFFFFFFFL));
//					tokenSetPointer++;
//					tokenRid = 0;
//					previousAddress = high.addr;
//				}
//				if (previousAddress == -1) {
//					previousAddress = high.addr;
//				}
//
//				tokenRid |= 1 << high.PE;
//			}
//
//			tokenRid |= previousAddress << model.getNrOfPEs()+2;
//			tokenSet.add(tokenRid);
//			tokenSetPointer++;
//		}
//
		nrIndirectConst = constantsInd.size();
		
		////////////////////////////////////// LOCAL VARS /////////////////////////////
		for (Integer i : lVars.keySet()) {
			int previousAddress = -1;
			LocationInformation locInfo = new LocationInformation(nrOfPEs);
			localVariables.add(i);

			for (RegID rid : lVars.get(i)) {
				if (previousAddress != rid.addr && previousAddress != -1) {
					locInfo.setRegisterFileAddress(previousAddress);
					locationInformation.add(locInfo);
					localVariables.add(i);
					locInfo = new LocationInformation(nrOfPEs);
					previousAddress = rid.addr;
				}
				if (previousAddress == -1) {
					previousAddress = rid.addr;
				}
				locInfo.getPESelect()[rid.PE] = true;
			}
			locInfo.setRegisterFileAddress(previousAddress);
			locationInformation.add(locInfo);
		}

		for (Integer i : longLVars.keySet()) {
			int previousAddress = -1;

			LocationInformation locInfo = new LocationInformation(nrOfPEs);
			localVariables.add(i);

			TreeSet<RegID> longLVar = longLVars.get(i);
			TreeSet<RegID> longLVarHigh = longLVarsHigh.get(i);
			Iterator<RegID> longLVarIt = longLVar.iterator();
			Iterator<RegID> longLVarItHigh = longLVarHigh.iterator();

			while (longLVarIt.hasNext()) {
				RegID low = longLVarIt.next();

				if (previousAddress != low.addr && previousAddress != -1) {
					locInfo.setRegisterFileAddress(previousAddress);
					locationInformation.add(locInfo);
					localVariables.add(i);
					locInfo = new LocationInformation(nrOfPEs);
					previousAddress = low.addr;
				}
				if (previousAddress == -1) {
					previousAddress = low.addr;
				}
				locInfo.getPESelect()[low.PE] = true;

			}
			locInfo.setRegisterFileAddress(previousAddress);
			locationInformation.add(locInfo);

			longLVarItHigh = longLVarHigh.iterator();
			locInfo = new LocationInformation(nrOfPEs);
			previousAddress = -1;
			localVariables.add(i + 1);
			while (longLVarItHigh.hasNext()) {
				RegID high = longLVarItHigh.next();

				if (previousAddress != high.addr && previousAddress != -1) {
					locInfo.setRegisterFileAddress(previousAddress);
					locationInformation.add(locInfo);
					localVariables.add(i + 1);
					locInfo = new LocationInformation(nrOfPEs);
					previousAddress = high.addr;
				}
				if (previousAddress == -1) {
					previousAddress = high.addr;
				}

				locInfo.getPESelect()[high.PE] = true;
			}

			locInfo.setRegisterFileAddress(previousAddress);
			locationInformation.add(locInfo);
		}

		nrLocalVarReceive = localVariables.size();

		//////////////////////////////////////////////////// SEND LVARS //////////////////////////////////////////////////////////////
		
		
		calcLiveOutVias();

		for (Integer i : sendLVars.keySet()) {

			for (RegID rid : sendLVars.get(i)) {
				
				LocationInformation locInfo = new LocationInformation(nrOfPEs);
				
				locInfo.setRegisterFileAddress(rid.addr);
				locInfo.setLiveOut(liveOutVias[rid.PE]);
				locInfo.setMux(liveOutViaMux[rid.PE]);
				locationInformation.add(locInfo);
				localVariables.add(i);
			}
		}

		for (Integer i : sendlongLVars.keySet()) {
			TreeSet<RegID> longLVars = sendlongLVars.get(i);
			TreeSet<RegID> longLVarsHigh = sendlongLVarsHigh.get(i);
			Iterator<RegID> longLVarsIt = longLVars.iterator();
			Iterator<RegID> longLVarsItHigh = longLVarsHigh.iterator();

			while (longLVarsIt.hasNext()) {
				RegID low = longLVarsIt.next();
				RegID high = longLVarsItHigh.next();
				LocationInformation locInfo = new LocationInformation(nrOfPEs);
				
				locInfo.setRegisterFileAddress(low.addr);
				locInfo.setLiveOut(liveOutVias[low.PE]);
				locInfo.setMux(liveOutViaMux[low.PE]);
				locationInformation.add(locInfo);
				
				localVariables.add(i);
				
				locInfo = new LocationInformation(nrOfPEs);
				
				locInfo.setRegisterFileAddress(high.addr);
				locInfo.setLiveOut(liveOutVias[high.PE]);
				locInfo.setMux(liveOutViaMux[high.PE]);
				locationInformation.add(locInfo);
				
				localVariables.add(i+1);
			}
		}
		
		nrLocalVarSend = localVariables.size() - nrLocalVarReceive;
	}

	private int[] liveOutVias;
	private int[] liveOutViaMux;
	private int[] liveOuts;

	private void calcLiveOutVias() throws NotSchedulableException {

		liveOuts = new int[nrOfPEs];
		int tmp = 0;
		for (int i = 0; i < nrOfPEs; i++) {
			if (model.getPEs().get(i).getLiveout()) {
				liveOuts[tmp] = i;
				tmp++;
			}
		}

		// liveOuts = cgra.getLiveOuts();

		liveOutVias = new int[nrOfPEs];
		liveOutViaMux = new int[nrOfPEs];
		// int[] liveOuts = cgra.getLiveOuts();
		for (int i = 0; i < nrOfPEs; i++) {

			boolean foundConection = false;
			for (int j = 0; j < liveOuts.length; j++) {
				LinkedList<Integer> connections = interconnect[liveOuts[j]];
				if (liveOuts[j] == i || connections.contains(i)) {
					int src = 0;
					for (src = 0; src < connections.size(); src++) {
						if (connections.get(src) == i) {
							break;
						}
					}

					liveOutVias[i] = j;
					liveOutViaMux[i] = src;
					foundConection = true;
					break;
				}

			}
			if (!foundConection) {
				System.err.println("NO LIVE OUT FOR PE " + i + ". THROW EXCEPTION HERE");
				throw new NotSchedulableException();
			}
		}
	}

	/**
	 * Generate the C-Box allocation
	 */
	public void cBoxAllocation() {
		cBoxAlloc = leftEdgeIf(getIfLifetimes());
		return;
	}

	/**
	 * Get the lifetimes for IF-nodes
	 * 
	 * @return A mapping between IF nodes and their lifetime
	 */
	private Map<Node, Interval> getIfLifetimes() {
		Map<Node, Interval> ltimes = new LinkedHashMap<Node, Interval>();
		int[] loopTime;
		int l, u;
		Interval slot;

		// Important: if nodes that control a loop controller have at least the
		// lifetime of the loop-contoller
		Set<Node> toHandleIfs = new LinkedHashSet<Node>(ifNodes);
		Set<Node> handleLater = new LinkedHashSet<Node>();
		while (toHandleIfs.size() != 0) {

			ifloop: for (Node ifNode : toHandleIfs) {
				l = sched.slot(ifNode).ubound + 1; // lifetime begins after node
													 // is finished
				u = l;
				if (graph.getSuccessors(ifNode) != null){
					for (Node ifSucc : graph.getSuccessors(ifNode)) {
						if (!ifNodes.contains(ifSucc) && ifSucc.getController() != ifNode) continue;
						if (isLoad(ifSucc)) continue; // load nodes shouldn't be conditional -> must be fixed in graph
						if (isLoopController(ifSucc)) { // if node, controlling a loop-controller, must live at least as long as the loop
							if (ltimes.containsKey(ifSucc)) {
								slot = ltimes.get(ifSucc);
							} else {
								handleLater.add(ifNode);
								continue ifloop;
							}
						} else
							slot = getSlotForNode(ifSucc);
						if (slot.ubound > u) u = slot.ubound;
						if (slot.lbound < l) l = slot.lbound;
					}
				}
				for (Loop lp : loopTimes.keySet()) {
					for (Node ctrl : lp.getController()) {
						if (ctrl == ifNode) {
							loopTime = loopTimes.get(lp);
							if (loopTime[1] > u){
								u = loopTime[1];
							}
							if (loopTime[0] < l){
								l = loopTime[0];
							}
						}
					}
				}
				ltimes.put(ifNode, new Interval(l, u));

			}
			toHandleIfs = handleLater;
			handleLater = new LinkedHashSet<Node>();
		}
		return ltimes;
	}

	// fused slots
	/**
	 * Get the slot for a fused store node
	 * 
	 * @param nd
	 *            - The store node which may or may not be fused
	 * @return Its scheduled interval
	 */
	private Interval getSlotForNode(Node nd) {
		if (sFuseNodes.containsKey(nd)){
			nd = sFuseNodes.get(nd);
		}

		return sched.slot(nd);
	}

	/**
	 * Allocate nodes to registers for all PEs
	 */
	@SuppressWarnings("unchecked")
	public void registerAllocation() {
		peBindMap = new LinkedHashMap[nrOfPEs];
		lvarNodeMap = new LinkedHashMap[nrOfPEs];
		twinNodes = new LinkedHashMap<Node, Node>();
		highLowTrackerIntern = new LinkedHashMap[nrOfPEs];
		highLowTrackerExtern = new LinkedHashMap[nrOfPEs];

		for (int pe = 0; pe < nrOfPEs; pe++) {
			peBindMap[pe] = leftEdge(getLifeTimes(pe), pe);
		}
	}

	public Map<Node, Integer>[] getPEBindMap() {
		return peBindMap;
	}

	/**
	 * Get the lifetimes of all scheduled nodes for register allocation
	 * 
	 * @param pe
	 *            - The PE of interest
	 * @return A mapping between nodes and their lifetimes
	 */
	private Map<Node, Interval> getLifeTimes(int pe) {
		Map<Node, Interval> ltimes = new LinkedHashMap<Node, Interval>();
		lvarNodeMap[pe] = new LinkedHashMap<Integer, Node>();

		int l, u;
		int max = sched.length() - 1;

		Set<Integer> usedAdd = new LinkedHashSet<Integer>();
		for (Node nd : copyMap.keySet()) {
			l = Integer.MAX_VALUE;
			if (copyMap.get(nd)[pe] != null){
				for (int i : copyMap.get(nd)[pe]) {
					if (i < l) l = i;
				}
			}else{
				l = -1;
			}
			if (l == Integer.MAX_VALUE){
				l = -1;
			}

			if (l > -1) {
				if (isLoadStore(nd)) {
					if (!lvarNodeMap[pe].containsKey(nd.getValue())) {
						// lvarNodeMap[pe].put(nd.getValue(), nd);
						if (lvarPEMap.containsKey(nd.getValue()) && lvarPEMap.get(nd.getValue()) != pe) { 	// if it is only a local copy to pass a value
							addToLifeTimes(ltimes, nd, l, getLastUsage(nd, pe, l));							//the lifetime is shorter
						} else {
							addToLifeTimes(ltimes, nd, 0, max);												//otherwise the lifetime spans the whole schedule
							lvarNodeMap[pe].put(nd.getValue(), nd);
						}
						usedAdd.add(nd.getValue());
					}
					continue;
				}
				if (isConst(nd) && !ltimes.containsKey(nd)) {
					if(!nd.isIndirectConstant() && Math.abs(nd.getValue()) < model.getPEs().get(pe).getRegfilesize()/2){ //CCOONNSSTT
						continue; // directly coded in Context
					}
					addToLifeTimes(ltimes, nd, 0, max);
					continue;
				}
				u = getLastUsage(nd, pe, l);
				addToLifeTimes(ltimes, nd, l, u);
			}
		}

		for (int addr : storeMap.keySet()) {
			if (usedAdd.contains(addr)){
				continue; // find out if it is a store only (no load in graph)
			}
			Node nd = storeMap.get(addr);
			if (peMap.containsKey(nd)) {
				if (peMap.get(nd) == pe) {
					addToLifeTimes(ltimes, nd, 0, max);
				}
			}
		}

		for (Node nd : peMap.keySet()) {
			if (isIf(nd) && !isDMAPrefetch(nd)){
				continue; // if-nodes are only necessary for pBox -> no lifetime
			}
			if (isLoadStore(nd)){
				continue; // loads and stores are handled differently
			}
			if (peMap.get(nd) == pe) {
				if (isStore(nd)) {
					l = 0;
					u = max;
				} else {
					l = sched.slot(nd).ubound + 1; // lifetime begins after node
													 // is finished
					u = getLastUsage(nd, pe, l);
					if (vNodes.containsKey(nd)){
						for (Node vNode : vNodes.get(nd)) {
							if (isStore(vNode)) { // stores are handled before
								l = -1;
								u = -1;
							}
						}
					}
				}
				if ((l < 0) || (u < 0)){
					continue;
				}
				addToLifeTimes(ltimes, nd, l, u);
			}
		}

		return ltimes;
	}

	/**
	 * Add a node to the lifetime map. Duplicate it if it is a 64-bit node
	 * 
	 * @param ltimes
	 *            - The lifetime map to add the node to
	 * @param nd
	 *            - The node to be added
	 * @param l
	 *            - The start of life
	 * @param u
	 *            - The end of life
	 */
	private void addToLifeTimes(Map<Node, Interval> ltimes, Node nd, int l, int u) {
		Node nd2;
		
		if (output64(nd)) {
			if (l == 0){
				l = 1;
			}
			if (!twinNodes.containsKey(nd)) {
				nd2 = new Node(nd.getAddress(), nd.getOperation(), nd.getValue(), nd.getController(), nd.getDecision()); // add a copy node to mark size of 64bit op
				twinNodes.put(nd, nd2);
			} else {
				nd2 = twinNodes.get(nd);
			}
			ltimes.put(nd, new Interval(l-1, u));
			ltimes.put(nd2, new Interval(l, u+1));
			return;
		}
		ltimes.put(nd, new Interval(l, u));
	}

	/**
	 * Find the last time a node is used. (int, ext, dma)
	 * 
	 * @param nd
	 *            - Node of interest
	 * @param pe
	 *            - PE of interest
	 * @param first
	 *            Usage - The first usage - necessary to find out loop carried life times
	 * @return Last time of use
	 */
	private int getLastUsage(Node nd, int pe, int firstUsage) {
		int lastTime = -1;
		Node[] nodes;

		if (isStore(nd) && lvarPEMap.containsKey(nd.getValue())) {
			return sched.length() - 1; // shouldn't be necessary
		}

		for (int t : intNodeMap.keySet()) {
			nodes = intNodeMap.get(t);
			if (nodes[pe] == nd) {
				if (t > lastTime){
					lastTime = t;
				}

				for (Loop lp : loopTimes.keySet()) {
					int[] lt = loopTimes.get(lp);
					if (t >= lt[0] && t <= lt[1] && firstUsage <= lt[0]) { // last access is in a inner loop - has to be alive during the whole loop
						if (lt[1] > lastTime) lastTime = lt[1];
					}
				}

			}
		}

		for (int t : outNodeMap.keySet()) {
			nodes = outNodeMap.get(t);
			if (nodes[pe] == nd) {
				if (t > lastTime){
					lastTime = t;
				}

				for (Loop lp : loopTimes.keySet()) {
					int[] lt = loopTimes.get(lp);
					if (t >= lt[0] && t <= lt[1] && firstUsage <= lt[0]) { // last access is in a inner loop - has to be alive during the whole loop
						if (lt[1] > lastTime){
							lastTime = lt[1];
						}
					}
				}
			}
		}

		for (int t : dmaNodeMap.keySet()) {
			nodes = dmaNodeMap.get(t);
			if (nodes[pe] == nd) {
				if (t > lastTime){
					lastTime = t;
				}

				for (Loop lp : loopTimes.keySet()) {
					int[] lt = loopTimes.get(lp);
					if (t >= lt[0] && t <= lt[1] && firstUsage <= lt[0]) { // last access is in a inner loop - has to be alive during the whole loop
						if (lt[1] > lastTime){
							lastTime = lt[1];
						}
					}
				}
			}
		}

		return lastTime;
	}

	
	int currCboxReadSlot = 0;
	int currCBoxWriteSlot  = 0;
	
	/**
	 * Find a legal starting point for the given node on the given PE. Also
	 * fixes routing constraints and makes operands available to PE. This Method
	 * has lots of side-effects. The node must be scheduled at the returned
	 * time, once this method was called!
	 * 
	 * @param nd
	 *            - The node to be placed
	 * @param pe
	 *            - The PE to place the node on
	 * @param t
	 *            - The earliest starting time for the node
	 * @return The true starting time after making operands available
	 * @throws NotSchedulableException
	 */
	private int placeNode(Node nd, int pe, int t) throws NotSchedulableException {
		Node[] operands = nd.getPredecessors();
		if (operands == null) {
			return t; // if the node has no operands it can be scheduled immediately
		}
		int i = 0;

		int maxStartTime = -1;
		int currStartTime;

		Node operand;

		placeIntUsed = false;

		int tMin = findLoopStartTime(nd);

		if (isDMA(nd) || isDMAPrefetch(nd)) {
			for (i = 0; i < 1; i++) {
				operand = operands[i];
				currStartTime = makeAvailableOnPE(operand, pe, tMin);
				if (maxStartTime < currStartTime){
					maxStartTime = currStartTime;
				}
			}
		}
		currStartTime = (maxStartTime < 0) ? t : maxStartTime;

		placePE = new int[] { -1, -1 };
		placeNode = new Node[] { null, null };

		for (; i < operands.length; i++) {
			operand = operands[i];

			currStartTime = makeAvailableToPE(operand, pe, currStartTime, tMin);
			if (maxStartTime < currStartTime){
				maxStartTime = currStartTime;
			}
		}

		currStartTime = (maxStartTime < t) ? t : maxStartTime;

		boolean placeFound;
		int blocktime;

		
		int dur = operations[pe].get(nd.getOperation());

		// actually find a legal starting point: check required resources
		do {
			placeFound = true;

//			if (input64(nd)) {
//				blocktime = 2;
//			} else {
//				blocktime = 1;
//			}
			blocktime = model.getPEs().get(pe).getAvailableOperators().get(nd.getOperation()).getInputLatency();

			for (i = 0; i < 2; i++) {
				if (placePE[i] != -1) {
					if (placePE[i] == pe) {
						if (!intCompatible(placeNode[i], pe, currStartTime, blocktime)) {
							placeFound = false;
							break; // cancel inner for loop
						}
					} else {
						if (!extCompatible(placeNode[i], placePE[i], currStartTime, blocktime)) { // DMA store must use out-Port for offset
							placeFound = false;
							break; // cancel inner for loop
						}
					}
				}
			}

			if (peBusy(pe, currStartTime, dur)) {
				placeFound = false;
			}

			currCboxReadSlot = getCBoxReadSlot(nd, currStartTime, dur);
			if((isIf(nd)|| isDMAPrefetch(nd)) && ifNodes.contains(nd)){
				currCBoxWriteSlot = getCBoxWriteSlot(currStartTime, dur);
				int desiredSlot = currCBoxWriteSlot*cBoxOutputsPerBox;
				if(currCBoxWriteSlot != -1 && slotFree(nd, currStartTime, dur, desiredSlot)){
					currCboxReadSlot = desiredSlot;
				}else{
					currCboxReadSlot = -1;
				}
			}
			
			if(loopControllers.contains(nd) && currCBoxWriteSlot != 0){
				placeFound = false;
			}
			
			
			if (((isIf(nd) || isDMAPrefetch(nd)) && ifNodes.contains(nd)) && (currCBoxWriteSlot == -1 || currCboxReadSlot == -1)) {
				placeFound = false;
			}

			
			
			if (isConditional(nd) && currCboxReadSlot == -1) {
				placeFound = false;
			}

			if (!placeFound){
				currStartTime++;
				if(currStartTime > maxCmpTime){
					int diff = currStartTime - maxCmpTime;
					insertEmptyTimesteps(currStartTime, diff);
					maxCmpTime += diff;
				}
			}
		} while (!placeFound);

		for (i = 0; i < 2; i++) {
			if (placePE[i] != -1) {
//				if (input64(nd)) {
//					blocktime = 2;
//				} else {
//					blocktime = 1;
//				}
				
				blocktime = model.getPEs().get(pe).getAvailableOperators().get(nd.getOperation()).getInputLatency();

				if (placePE[i] == pe) {
					blockInt(placeNode[i], pe, currStartTime, blocktime);
				} else {
					blockExt(placeNode[i], placePE[i], currStartTime, blocktime);
				}
			}
		}

		if (isDMA(nd)||isDMAPrefetch(nd)) {
			blockDMA(operands[0], pe, currStartTime, blocktime); // mark nodes as being used for DMA access -> required for lifetime calculation
		}

		return currStartTime;
	}

	/**
	 * Find the starting time of the loop for a given node
	 * 
	 * @param nd
	 *            - Node of interest
	 * @return The starting time of the loop this node belongs to
	 */
	private int findLoopStartTime(Node nd) {
		Loop lp = lg.getLoop(nd);
		int tStart = loopTimes.get(lp)[0];
		int childTime;
		for (Loop child : lg.getChildren(lp)) {
			if (loopTimes.containsKey(child)) {
				childTime = loopTimes.get(child)[1] + 1;
				if(childTime < maxCmpTime){
					if (childTime > tStart){
						tStart = childTime;
					}
				}
			}
		}

		return tStart;
	}

	/**
	 * Check if this write is conditional
	 * 
	 * @param nd
	 *            - Node of interest
	 * @return True iff the result is only written conditionally
	 */
	private boolean isConditional(Node nd) {
		if (sFuseNodes.containsValue(nd)) {
			for (Node ndkey : sFuseNodes.keySet()) {
				if (sFuseNodes.get(ndkey) == nd) {
					nd = ndkey;
					break;
				}
			}
		}

		Node ctrl = nd.getController();
		if (ctrl == null){
			return false;
		}
		Set<Node> preds = graph.getPredecessors(nd);
		if (preds == null){
			return false;
		}
		if (preds.contains(ctrl)){
			return true;
		}
		return false;
	}

	/**
	 * Check if writing to the C-Box is possible in the given time
	 * 
	 * @param tStart
	 *            - Starting time to check
	 * @param dur
	 *            - Duration to check
	 * @return true iff C-Box is available at that time
	 */
	private int getCBoxWriteSlot(int tStart, int dur) {
		SLOT: for(int slot = 0; slot < nrOfCBoxes; slot++){
			for (int t = tStart; t < tStart + dur; t++) {
				if (cBoxWrite[slot].contains(t)) {
					continue SLOT;
				}
			}
			return slot;
		}
	
		return -1;
	}

	/**
	 * Block the C-Box write for the given duration starting at sTime
	 * 
	 * @param sTime
	 *            - Starting time to block
	 * @param dur
	 *            - Duration to block
	 */
	private void blockCBoxWrite(Node nd,int sTime, int dur, int slot) {
		for (int t = sTime; t < sTime + dur; t++) {
			cBoxWrite[slot].add(t);
		}
		cboxSelectWrite.put(nd, slot);
	}

	/**
	 * Check if the C-Box read is compatible with the given node at the given time
	 * 
	 * @param nd
	 *            - Node of interest
	 * @param sTime
	 *            - Starting time
	 * @param dur
	 *            - Duration
	 * @return -1 if no slot is free - slotnr otherwise
	 */
	private int getCBoxReadSlot(Node nd, int sTime, int dur) {
		SLOT: for(int slot = 0; slot < cboxOutputs; slot++){
			
			for (int t = sTime; t < sTime + dur; t++) {
				if (cBoxRead[slot].containsKey(t)) {
					if (conditionIncompatible(cBoxRead[slot].get(t), nd)){
						continue SLOT;
					}
				}
			}
			return slot;
		}
		return -1;
	}
	
	private boolean slotFree(Node nd, int sTime, int dur, int slot){
		for (int t = sTime; t < sTime + dur; t++) {
			if (cBoxRead[slot].containsKey(t)) {
				if (conditionIncompatible(cBoxRead[slot].get(t), nd)){
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Check the condition compatibility of two nodes
	 * 
	 * @param n1
	 *            - Node 1
	 * @param n2
	 *            - Node 2
	 * @return true iff the condition the C-Box conditions of both nodes are compatible
	 */
	private boolean conditionIncompatible(Node n1, Node n2) {
		Node nd1, nd2;
		if (sFuseNodes.containsKey(n1)) {
			nd1 = sFuseNodes.get(n1);
		} else {
			nd1 = n1;
		}
		if (sFuseNodes.containsKey(n2)) {
			nd2 = sFuseNodes.get(n2);
		} else {
			nd2 = n2;
		}

		if (nd1.getController() != nd2.getController()){
			return true;
		}
		if (nd1.getDecision() != nd2.getDecision()){
			return true;
		}
		if (nd1.getShortCircuitEvaluationTrueBranch() != nd2.getShortCircuitEvaluationTrueBranch()){
			return true;
		}
		if (nd1.getShortCircuitEvaluationFalseBranch() != nd2.getShortCircuitEvaluationFalseBranch()){
			return true;
		}
		if (nd1.getShortCircuitEvaluationTrueBranchController() != nd2.getShortCircuitEvaluationTrueBranchController()){
			return true;
		}
		if (nd1.getShortCircuitEvaluationFalseBranchController() != nd2.getShortCircuitEvaluationFalseBranchController()){
			return true;
		}
		if (nd1.getShortCircuitEvaluationTrueBranchControllerDecision() != nd2.getShortCircuitEvaluationTrueBranchControllerDecision()){
			return true;
		}
		if (nd1.getShortCircuitEvaluationFalseBranchControllerDecision() != nd2.getShortCircuitEvaluationFalseBranchControllerDecision()){
			return true;
		}

		return false;
	}

	/**
	 * Block the C-Box read port for the given time with the given Node
	 * 
	 * @param nd
	 *            - Node that will require the C-Box read
	 * @param sTime
	 *            - Starting time
	 * @param dur
	 *            - Duration
	 */
	private void blockCBoxReadSlot(Node nd, int sTime, int dur, int slot) {
		for (int t = sTime; t < sTime + dur; t++) {
			cBoxRead[slot].put(t, nd);
		}
		
		Map<Integer, Integer> sel = cboxSelectRead.get(nd);
		if(sel == null){
			sel = new LinkedHashMap<>();
			cboxSelectRead.put(nd, sel);
		}
		for(int time = 0; time < dur; time++ ){
			sel.put(sTime+time, slot);
		}
	}

	/**
	 * Block the DMA base address port with the given node for the given duration
	 * 
	 * @param nd
	 *            - Node that will be assigned to the port
	 * @param pe
	 *            - PE to be used
	 * @param sTime
	 *            - Starting time
	 * @param dur
	 *            - Duration
	 */
	private void blockDMA(Node nd, int pe, int sTime, int dur) {
		Node[] block;

		for (int t = sTime; t < sTime + dur; t++) {
			if (!dmaNodeMap.containsKey(t)) {
				block = new Node[nrOfPEs];
				Arrays.fill(block, null);
				dmaNodeMap.put(t, block);
			}
			dmaNodeMap.get(t)[pe] = nd;
		}
	}

	/**
	 * Make the operand available to be accessed by the given PE
	 * 
	 * @param nd
	 *            - Node required
	 * @param pe
	 *            - PE that requires nd
	 * @param cStart
	 *            - currently planned starting time
	 * @param tMin
	 *            - earliest allowed starting time due to loop scope
	 * @return The time from which on nd will be accessible by PE
	 * @throws NotSchedulableException
	 */
	private int makeAvailableToPE(Node nd, int pe, int cStart, int tMin) throws NotSchedulableException {
		int pPE;
		LinkedHashSet<Integer>[] pPEs;

		if (isConst(nd) || (isLoad(nd) && !storeMap.keySet().contains(nd.getValue()))) {
			if (!placeIntUsed) {
				addToCopyMap(nd, pe, 0);
				placeIntUsed = true;
				if (placePE[0] == -1) {
					placePE[0] = pe;
					placeNode[0] = nd;
				} else {
					placePE[1] = pe;
					placeNode[1] = nd;
				}
				// System.err.println("Node "+nd+" copied to PE"+pe+"(int)");
				return cStart;
			} else {
				if (peMap.containsKey(nd)) {
					pPE = peMap.get(nd);
					if ((pPE != pe) && (peDests[pPE].contains(pe)) && placePE[0] != pPE) {
						if (placePE[0] == -1) {
							placePE[0] = pPE;
							placeNode[0] = nd;
						} else {
							placePE[1] = pPE;
							placeNode[1] = nd;
						}
						// System.err.println("Node "+nd+" on PE"+pPE+" provides value for PE"+pe);
						return cStart;
					}
				}
				if (copyMap.containsKey(nd)) {
					pPEs = copyMap.get(nd);
					for (int cPE = 0; cPE < nrOfPEs; cPE++) {
						if (pPEs[cPE] != null) availt: for (int availTime : pPEs[cPE]) {
							if (availTime < 0 || availTime > cStart){
								continue;
							}
							if (cPE == pe){
								continue;
							}
							if (!peDests[cPE].contains(pe)){
								continue;
							}
							if (placePE[0] == cPE){
								continue;
							}
							// check whether the copy is made in another
							// loop - if yes it might not be available cuz
							// the loop was not executed
							for (Loop lp : loopTimes.keySet()) {
								int[] lpTimes = loopTimes.get(lp);
								if (availTime > lpTimes[0] && availTime <= lpTimes[1]){
									continue availt;
								}
							}

							if (placePE[0] == -1) {
								placePE[0] = cPE;
								placeNode[0] = nd;
							} else {
								placePE[1] = cPE;
								placeNode[1] = nd;
							}

							// System.err.println("Node "+nd+" on PE"+cPE+" provides value for PE"+pe);
							return cStart;
						}
					}
				}
				LinkedList<Integer> tmp = interconnect[pe];
				for (int cPE : tmp) {
					if (cPE == pe){
						continue;
					}
					if (placePE[0] == cPE){
						continue;
					}
					addToCopyMap(nd, cPE, 0);
					if (placePE[0] == -1) {
						placePE[0] = cPE;
						placeNode[0] = nd;
					} else {
						placePE[1] = cPE;
						placeNode[1] = nd;
					}
					// System.err.println("Node "+nd+" forced to be provided by PE"+cPE+" for PE"+pe);
					return cStart;
				}
				throw new NotSchedulableException();
			}
		}

		if (isLoad(nd) && storeMap.keySet().contains(nd.getValue())) { // We need to set the Force
			if (!lvarPEMap.containsKey(nd.getValue())) { // if it hasn't been done already
				if (!placeIntUsed) {
					setForce(nd, pe);
					addToCopyMap(nd, pe, 0);
					placeIntUsed = true;
					if (placePE[0] == -1) {
						placePE[0] = pe;
						placeNode[0] = nd;
					} else {
						placePE[1] = pe;
						placeNode[1] = nd;
					}
					// System.err.println("Node "+nd+" copied to PE"+pe+"(int) and force set!");
					return cStart;
				} else {
					LinkedList<Integer> tmp = interconnect[pe];
					for (int cPE : tmp) {
						if (cPE == pe){
							continue;
						}
						if (placePE[0] == cPE){
							continue;
						}
						setForce(nd, cPE);
						addToCopyMap(nd, cPE, 0);
						if (placePE[0] == -1) {
							placePE[0] = cPE;
							placeNode[0] = nd;
						} else {
							placePE[1] = cPE;
							placeNode[1] = nd;
						}
						// System.err.println("Node "+nd+" forced to be provided by PE"+cPE+" for PE"+pe+" and force set!");
						return cStart;
					}
					throw new NotSchedulableException();
				}
			} else { // This may be a load after another load -> PE is already assigned. Add node to copyMap for scheduling
				if (!copyMap.containsKey(nd)) {
					addToCopyMap(nd, lvarPEMap.get(nd.getValue()), 0);
				}
				if(lvarPEMap.get(nd.getValue()) == pe && !placeIntUsed){
					placeIntUsed = true;
					if (placePE[0] == -1) {
						placePE[0] = pe;
						placeNode[0] = nd;
					} else {
						placePE[1] = pe;
						placeNode[1] = nd;
					}
					return cStart;
				}
			}
		}

		if ((peMap.containsKey(nd)) && (peMap.get(nd) == pe)) { // Value internally available
			if (!placeIntUsed) {
				if (placePE[0] == -1) {
					placePE[0] = pe;
					placeNode[0] = nd;
				} else {
					placePE[1] = pe;
					placeNode[1] = nd;
				}
				placeIntUsed = true;
				// System.err.println("Node "+nd+" internally used on PE"+pe);
				return sched.slot(nd).ubound + 1;
			}
		} else if ((copyMap.containsKey(nd)) && (copyMap.get(nd)[pe] != null)) {
			// check whether the copy is made in another loop - if yes it might not be available cuz the loop was not executed
			boolean validCopy = true;
			int copy = -1;
			for (int availTime : copyMap.get(nd)[pe]) {

				for (Loop lp : loopTimes.keySet()) {
					int[] lpTimes = loopTimes.get(lp);
					if (maxCmpTime< availTime || availTime > lpTimes[0] && availTime <= lpTimes[1] && !(cStart > lpTimes[0] && cStart <= lpTimes[1])) {
						validCopy = false;
						break;
					} else{
						copy = availTime;
					}

				}
			}

			if (validCopy && !placeIntUsed) {
				if (placePE[0] == -1) {
					placePE[0] = pe;
					placeNode[0] = nd;
				} else {
					placePE[1] = pe;
					placeNode[1] = nd;
				}
				placeIntUsed = true;
				// System.err.println("Node "+nd+" internally used on PE"+pe);
				return copy;
			}
		}

		int[] pes = new int[nrOfPEs];
		int[] sTimes = new int[nrOfPEs];
		Arrays.fill(pes, Integer.MAX_VALUE);
		LinkedHashSet<Integer>[] cpPE;

		if (peMap.containsKey(nd)) {
			pPE = peMap.get(nd);
			if (pPE != pe) {
				pes[pPE] = D[pPE][pe];
				sTimes[pPE] = sched.slot(nd).ubound + 1;
			}
		}

		if (copyMap.containsKey(nd)) {
			cpPE = copyMap.get(nd);
			for (int cPE = 0; cPE < nrOfPEs; cPE++) {

				// check whether the copy is made in another loop - if yes it might not be available cuz the loop was not executed

				if (cpPE[cPE] != null) {
					availT: for (int availTime : cpPE[cPE]) {
						for (Loop lp : loopTimes.keySet()) {
							int[] lpTimes = loopTimes.get(lp);
							if (maxCmpTime< availTime || availTime > lpTimes[0] && availTime <= lpTimes[1] && !(cStart > lpTimes[0] && cStart <= lpTimes[1])){
								continue availT;
							}

						}

						if (cPE != pe) {
							if (availTime > -1) {
								pes[cPE] = D[cPE][pe];
								sTimes[cPE] = availTime;
							}
						}
					}
				}
			}
		}

		int min = Integer.MAX_VALUE;
		int sourcePE = -1;

		for (int i = 0; i < nrOfPEs; i++) {
			if (min > pes[i]) { //TODO RAND
				sourcePE = i;
				min = pes[i];
			}
		}

		int t, minTime = Integer.MAX_VALUE;

		if (sourcePE < 0) {
			sourcePE = pe;
			t = 0;
			LinkedList<Integer> tmp = interconnect[pe];
			for (int target : tmp) {
				if (pe == target || placePE[0] == target){
					continue;
				}
				if (peMap.containsKey(nd)) {
					minTime = sched.slot(nd).ubound + 1;
				} else {
					if (copyMap.get(nd) == null) {
						System.out.println(nd);
						System.out.println(copyMap);
						System.out.println(lvarPEMap);
						System.out.println(storeMap);
					}
					if (copyMap.get(nd)[pe] != null){
						availT: for (int availTime : copyMap.get(nd)[pe]) {
							for (Loop lp : loopTimes.keySet()) {
								int[] lpTimes = loopTimes.get(lp);
								if (availTime > lpTimes[0] && availTime <= lpTimes[1]){
									continue availT;
								}

							}
							if (availTime < minTime){
								minTime = availTime;
							}
						}
					}
				}
				if (minTime == Integer.MAX_VALUE) { // This means none was found
					sourcePE = lvarPEMap.get(nd.getValue());
					addToCopyMap(nd, lvarPEMap.get(nd.getValue()), 0);
					minTime = 0;
				}

				if (minTime < tMin){
					minTime = tMin;
				}
				t = copyAlongPath(nd, sourcePE, target, minTime);
				placePEfin = target;
				break;
			}
		} else {
			List<Integer> path = getPath(sourcePE, pe);
			if (path.size() > 0){
				path.remove(path.size() - 1); // remove target pe, since it only has to be available on a connected PE
			}
			if (tMin < sTimes[sourcePE]) {
				if (path.get(path.size() - 1) == placePE[0]) { // the same pe already provides a value TO the target PE, so place required value ON PE
					t = copyAlongPath(nd, sourcePE, pe, sTimes[sourcePE]);
				} else {
					t = copyAlongPathSkipTarget(nd, sourcePE, pe,
							sTimes[sourcePE]);
				}
			} else {
				if (path.get(path.size() - 1) == placePE[0]) { // the same pe already provides a value TO the target PE, so place required value ON PE
					t = copyAlongPath(nd, sourcePE, pe, tMin);
				} else {
					t = copyAlongPathSkipTarget(nd, sourcePE, pe, tMin);
				}
			}
		}

		if (placePE[0] == -1) {
			placePE[0] = placePEfin;
			placeNode[0] = nd;
		} else {
			placePE[1] = placePEfin;
			placeNode[1] = nd;
		}
		
		if(placePE[0] == placePE[1]){
			System.err.println("UUUUNSCHÖN " + nd);
			System.err.println(placePE[0] + " - " + placeNode[0]);
			System.err.println(placePE[1] + " - " + placeNode[1]);
		}
		return t;
	}

	/**
	 * Make nd available ON pe by copying if necessary
	 * 
	 * @param nd
	 *            - Node required
	 * @param pe
	 *            - PE to make nd available on
	 * @param tMin
	 *            - earliest allowed starting time due to loop scope
	 * @return
	 */
	private int makeAvailableOnPE(Node nd, int pe, int tMin) {
		if (isConst(nd) || (isLoad(nd) && !storeMap.keySet().contains(nd.getValue()))) {
			addToCopyMap(nd, pe, 0);
			return 0;
		}

		if (isLoad(nd) && storeMap.keySet().contains(nd.getValue())) {
			if (!lvarPEMap.containsKey(nd.getValue())) {
				setForce(nd, pe);
				addToCopyMap(nd, pe, 0);
				return 0;
			} else if (!copyMap.containsKey(nd)) {
				addToCopyMap(nd, lvarPEMap.get(nd.getValue()), 0);
			}
		}

		int[] pes = new int[nrOfPEs];
		int[] sTimes = new int[nrOfPEs];
		Arrays.fill(pes, Integer.MAX_VALUE);
		LinkedHashSet<Integer>[] cpPE;
		int pPE;

		if (peMap.containsKey(nd)) {
			pPE = peMap.get(nd);
			pes[pPE] = D[pPE][pe];
			sTimes[pPE] = sched.slot(nd).ubound + 1;
		}
		if (copyMap.containsKey(nd)) {
			cpPE = copyMap.get(nd);
			for (int cPE = 0; cPE < nrOfPEs; cPE++) {

				// check whether the copy is made in another loop - if yes it might not be available cuz the loop was not executed
				if (cpPE[cPE] != null) availT: for (int availTime : cpPE[cPE]) {
					for (Loop lp : loopTimes.keySet()) {
						int[] lpTimes = loopTimes.get(lp);
						if (availTime > lpTimes[0] && availTime <= lpTimes[1]) {
							continue availT;
						}

					}

					if (availTime > -1) {
						pes[cPE] = D[cPE][pe];
						sTimes[cPE] = availTime;
					}
				}
			}
		}

		int min = Integer.MAX_VALUE;
		int sourcePE = -1;

		for (int i = 0; i < nrOfPEs; i++) {
			if (min > pes[i]) {
				sourcePE = i;
				min = pes[i];
			}
		}

		int t;

		if (sourcePE == -1 && isLoad(nd)) { // Load was fused into a node in a subloop and thus cant be used...
			sourcePE = lvarPEMap.get(nd.getValue());
			addToCopyMap(nd, sourcePE, 0);
		}

		if (tMin < sTimes[sourcePE]) {
			t = copyAlongPath(nd, sourcePE, pe, sTimes[sourcePE]);
		} else {
			t = copyAlongPath(nd, sourcePE, pe, tMin);
		}
		// System.err.println("Node "+nd+" available on PE"+pe+" at "+t);
		return t;
	}

	/**
	 * Copy the given node from source to target, but only make it available TO the target
	 * 
	 * @param nd
	 *            - Node to be copied
	 * @param sourcePE
	 *            - Source to get nd from
	 * @param pe
	 *            - Target to make nd available to
	 * @param sTime
	 *            - Time to start copying
	 * @return Time when nd is available TO pe
	 */
	private int copyAlongPathSkipTarget(Node nd, int sourcePE, int pe, int sTime) {
		List<Integer> path = getPath(sourcePE, pe);
		if (path.size() > 0){
			path.remove(path.size() - 1); // remove target pe, since it only has to be available on a connected PE
		}

		if (path.size() > 0) {
			placePEfin = path.get(path.size() - 1);
		}
		
		
		if(sTime>maxCmpTime){
			int diff = sTime-maxCmpTime;
			insertEmptyTimesteps(maxCmpTime, diff);
			maxCmpTime += diff;
		}

		int s, d;

		for (int i = 0; i < path.size() - 1; i++) {
			s = path.get(i);
			d = path.get(i + 1);
			
			int maxTime = maxCmpTime - path.size() + i;
			
			sTime = copyFromTo(nd, s, d, sTime, maxTime);
		}

		return sTime;
	}
	
	private int maxCmpTime = Integer.MAX_VALUE;

	/**
	 * Copy the given node from source to target, but only make it available ON the target
	 * 
	 * @param nd
	 *            - Node to be copied
	 * @param sourcePE
	 *            - Source to get nd from
	 * @param pe
	 *            - Target to make nd available on
	 * @param sTime
	 *            - Time to start copying
	 * @return Time when nd is available ON pe
	 */
	private int copyAlongPath(Node nd, int sourcePE, int pe, int sTime) {
		List<Integer> path = getPath(sourcePE, pe);
		
		
		if(sTime>maxCmpTime){
			int diff = sTime-maxCmpTime;
			insertEmptyTimesteps(maxCmpTime, diff);
			maxCmpTime += diff;
		}

		placePEfin = pe;

		int s, d;

		for (int i = 0; i < path.size() - 1; i++) {
			s = path.get(i);
			d = path.get(i + 1);

			int maxTime = maxCmpTime - path.size() + i;
			
			sTime = copyFromTo(nd, s, d, sTime, maxTime);
		}

		return sTime;
	}

	/**
	 * Copy nd from s to d. S and d must be connected. Checks if copying is possible and returns the time when the copy is available.
	 * 
	 * @param nd
	 *            - Node to be copied
	 * @param s
	 *            - Source PE
	 * @param d
	 *            - Destination PE
	 * @param sTime
	 *            - Starting time
	 * @return Time when copy is available
	 */
	private int copyFromTo(Node nd, int s, int d, int sTime, int maxTime) {
		int blocktime;
		if (output64(nd)) {
			blocktime = 2;
		} else {
			blocktime = 1;
		}

		boolean placeFound = false;
		
		if(sTime>maxTime){
			int diff = sTime-maxTime;
			insertEmptyTimesteps(maxCmpTime, diff);
			maxCmpTime += diff;
			maxTime += diff;
		}
		

		do {
			if (extCompatible(nd, s, sTime, blocktime) && (!peBusy(d, sTime, blocktime))) {
				blockExt(nd, s, sTime, blocktime);
				setBusy(d, sTime, blocktime);
				addToCopyMap(nd, d, sTime + blocktime);
				placeFound = true;
			} else {
				sTime++;
				if(sTime>maxTime){
					int diff = sTime-maxTime;
					insertEmptyTimesteps(sTime, diff);
					maxCmpTime += diff;
					maxTime += diff;
				}
			}
		} while (!placeFound);

		return sTime + blocktime;
	}

	/**
	 * Block the internal regfile port for the given time with the given node
	 * 
	 * @param nd
	 *            - Node to be assigned to int
	 * @param pe
	 *            - PE to use
	 * @param sTime
	 *            - Starting time
	 * @param dur
	 *            - Duration
	 */
	private void blockInt(Node nd, int pe, int sTime, int dur) {
		Node[] block;

		for (int t = sTime; t < sTime + dur; t++) {
			if (!intNodeMap.containsKey(t)) {
				block = new Node[nrOfPEs];
				Arrays.fill(block, null);
				intNodeMap.put(t, block);
			}
			intNodeMap.get(t)[pe] = nd;
		}
	}

	/**
	 * Block external read port with node nd
	 * 
	 * @param nd
	 *            - Node to be assigned to ext
	 * @param pe
	 *            - PE to use
	 * @param sTime
	 *            - Starting time
	 * @param dur
	 *            - Duration
	 */
	private void blockExt(Node nd, int pe, int sTime, int dur) {
		Node[] block = null;

		for (int t = sTime; t < sTime + dur; t++) {
			if (!outNodeMap.containsKey(t)) {
				block = new Node[nrOfPEs];
				Arrays.fill(block, null);
				outNodeMap.put(t, block);

			}
			outNodeMap.get(t)[pe] = nd;
		}
	}

	/**
	 * Check if internal port is compatible with nd
	 * 
	 * @param nd
	 *            - Node of interest
	 * @param pe
	 *            - PE of interest
	 * @param sTime
	 *            - Starting time
	 * @param dur
	 *            - Duration
	 * @return true iff nd is compatible
	 */
	private boolean intCompatible(Node nd, int pe, int sTime, int dur) {
		Node intNode;

		for (int t = sTime; t < sTime + dur; t++) {
			if (intNodeMap.containsKey(t)) {
				intNode = intNodeMap.get(t)[pe];
				if (intNode != nd && intNode != null) {
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * Check if external port is compatible with nd
	 * 
	 * @param nd
	 *            - Node of interest
	 * @param pe
	 *            - PE of interest
	 * @param sTime
	 *            - Starting time
	 * @param dur
	 *            - Duration
	 * @return true iff nd is compatible
	 */
	private boolean extCompatible(Node nd, int pe, int sTime, int dur) {
		Node outNode;

		for (int t = sTime; t < sTime + dur; t++) {
			if (outNodeMap.containsKey(t)) {
				outNode = outNodeMap.get(t)[pe];
				if (outNode != nd && outNode != null) {
					return false;
				}
			}
		}

		return true;
	}
	
	
	Random r = new Random(2);
	/**
	 * Get a list of PEs from source to destination
	 * 
	 * @param s
	 *            - Source PE
	 * @param d
	 *            - Destination PE
	 * @return The path between source and destination
	 */
	private List<Integer> getPath(int s, int d) {
		List<Integer> path = new LinkedList<Integer>();
		if (s == d){
			return path;
		}
		path.add(s);

		int i = s;
		while (P[i][d] != -1 ) {
			
			i =   P[i][d];path.add(i);
		}

		path.add(d);

		return path;
	}

	/**
	 * Declare a copy in the copymap
	 * 
	 * @param nd
	 *            - Node that has been copied
	 * @param pe
	 *            - PE it has been copied to
	 * @param t
	 *            - The time it is available from
	 */
	@SuppressWarnings("unchecked")
	private void addToCopyMap(Node nd, int pe, int t) {
		LinkedHashSet<Integer>[] cpTimes;
		LinkedHashSet<Integer> cpTime;
		if (!copyMap.containsKey(nd)) {
			cpTimes = new LinkedHashSet[nrOfPEs];
			copyMap.put(nd, cpTimes);
		}
		cpTime = copyMap.get(nd)[pe];
		if (cpTime == null) {
			cpTime = new LinkedHashSet<Integer>();

			copyMap.get(nd)[pe] = cpTime;
		}
		cpTime.add(t);
	}

	/**
	 * @param nd
	 *            - Node of interest
	 * @return true iff nd is a constant
	 */
	private boolean isConst(Node nd) {
		return nd.getOperation().isConst();
	}

	/**
	 * @param nd
	 *            - Node of interest
	 * @return true iff nd is a 64-bit node
	 */
	@Deprecated
	private boolean output64(Node nd) {
		int outDelay = outDelays.get(nd.getOperation());
		if(outDelay == 0){
			return input64(nd);
		}
		return (outDelays.get(nd.getOperation()) > 1);
	}
	
	/**
	 * @param nd
	 *            - Node of interest
	 * @return true iff nd is a 64-bit node
	 */
	@Deprecated
	private boolean input64(Node nd) {
		if(nd.getOperation() == Amidar.OP.DMA_LOAD64){
			return true; // because this are actually two dma accesses -> the inputs have to be there 2 cycles
		}
		int inDelay = inDelays.get(nd.getOperation());
		if(inDelay == 0){
			return output64(nd);
		}
		return (inDelays.get(nd.getOperation()) > 1);
	}
	
	/**
	 * @param nd
	 *            - Node of interest
	 * @return true iff nd is a DMA node
	 */
	private boolean isDMA(Node nd) {
		return nd.getOperation().isCacheAccess();
	}
	
	
	private boolean isDMAPrefetch(Node nd){
		return nd.getOperation().isCachePrefetch();
	}

	/**
	 * Sets the force for nd to pe
	 * 
	 * @param nd
	 *            - Node to be forced to PE
	 * @param pe
	 *            - PE it is forced to
	 */
	private void setForce(Node nd, int pe) {
		Set<Node> nodes = new LinkedHashSet<Node>();
		if (isLoadStore(nd)) {
			nodes.add(nd);
		} else if (vNodes.containsKey(nd)){
			nodes.addAll(vNodes.get(nd));
		}
		
		int vAddr;

		for (Node vNode : nodes) {
			vAddr = vNode.getValue();
			if (storeMap.keySet().contains(vAddr)) {
				if (!lvarPEMap.containsKey(vAddr)) {
					lvarPEMap.put(vAddr, pe);
				}
			}
		}
	}

	/**
	 * Update the attraction attribute for all consumers of nd
	 * 
	 * @param nd
	 *            - Node that has been scheduled
	 * @param pe
	 *            - PE it has been scheduled on
	 */
	private void updateAttr(Node nd, int pe, int tAvail) {
		if (graph.getConsumers(nd) != null) for (Node cons : graph.getConsumers(nd)) {
			if (!nodeAttr.containsKey(cons)) {
				int[] attr = new int[nrOfPEs];
				Arrays.fill(attr, 0);
				nodeAttr.put(cons, attr);
				nodeAttrTime.put(cons, tAvail);
			}
			for (int dest : peDests[pe]) {
				int inc =1;//tAvail - nodeAttrTime.get(cons) + 1;
				nodeAttr.get(cons)[dest] += inc;
				nodeAttrTime.put(cons, tAvail);
			}
		}
		if (vNodes.containsKey(nd)) for (Node vNode : vNodes.get(nd)) {
			if (graph.getConsumers(vNode) != null){
				for (Node cons : graph.getConsumers(vNode)) {
					if (!nodeAttr.containsKey(cons)) {
						int[] attr = new int[nrOfPEs];
						Arrays.fill(attr, 0);
						nodeAttr.put(cons, attr);
						nodeAttrTime.put(cons, tAvail);
					}
					for (int dest : peDests[pe]) {
						int inc =1;//tAvail - nodeAttrTime.get(cons) + 1;
						nodeAttr.get(cons)[dest] += inc;
						nodeAttrTime.put(cons, tAvail);
					}
				}
			}
		}
	}

	/**
	 * Set a PE to be busy during given time
	 * 
	 * @param pe
	 *            - PE
	 * @param t
	 *            - Starting time
	 * @param dur
	 *            - Duration
	 */
	private void setBusy(int pe, int t, int dur) {
		for (int i = t; i < t + dur; i++) {
			if (!busyMap.containsKey(i)) {
				boolean[] busy = new boolean[nrOfPEs];
				Arrays.fill(busy, false);
				busyMap.put(i, busy);
			}
			busyMap.get(i)[pe] = true;
		}
	}

	int opo= 0;
	
	LinkedHashMap<Object,Integer> handlePEmap = new LinkedHashMap<>();
	
	/**
	 * Returns a set of compatible PEs ordered by their Attraction
	 * 
	 * @param nd
	 *            - Node to find PEs for
	 * @param t
	 *            - starting time
	 * @return Set of sorted PEs
	 */
	private Set<Integer> findPEforNode(Node nd, int t) {
		int fPE = checkForcedPE(nd);
		int dur;

		LinkedHashSet<Integer> ret = new LinkedHashSet<Integer>();

		if (fPE > -1) {
			if (operations[fPE].containsKey(nd.getOperation())) {
				dur = operations[fPE].get(nd.getOperation());
				if (!peBusy(fPE, t, dur)) {
					ret.add(fPE);
				}
				return ret;
			}
		}
		
		
		int[] attr;
		if (nodeAttr.containsKey(nd)) {
			attr = nodeAttr.get(nd);
		} else {
			attr = new int[nrOfPEs];
			Arrays.fill(attr, 0);
		}

		Map<Integer, Integer> peAttr = new LinkedHashMap<>();

		int FUTURE = 50;
		int PAST = 0;
		
		LinkedHashMap<Integer, LinkedHashSet<Integer>> offsetModtoPEMap = null;
		LinkedHashSet<Integer> previousPeForHandle = null;
		if(isDMA(nd)){
			
			
			Node handle = nd.getPredecessor(0); 
			if(isLoadStore(handle)){
				offsetModtoPEMap = handleToPeMap.get(handle.getValue());
			} else {
				offsetModtoPEMap = handleToPeMap.get(handle);
			}
			
			if(offsetModtoPEMap != null){
				previousPeForHandle = offsetModtoPEMap.get(getOffsetModulo(nd));
			}
		}
		
		
		
		for (int i = 0; i < nrOfPEs; i++) {
			int cnt = 0;
			int past = PAST > t?-t:-PAST;
			for(int tt = past; tt < FUTURE; tt++){
				if(peBusy(i, t+tt, 1)){
					cnt++;
				}
			}
			if(previousPeForHandle != null && previousPeForHandle.contains(i)){
				cnt -= 1000;
			}
			peAttr.put(i, attr[i]-2*cnt);
		}

		AttrComparator aCmp = new AttrComparator(peAttr);
		Map<Integer, Integer> peAttrSorted = new TreeMap<Integer, Integer>(aCmp);
		peAttrSorted.putAll(peAttr);

		return peAttrSorted.keySet();
	}
	
	LinkedHashMap<Object,LinkedHashMap<Integer,LinkedHashSet<Integer>>> handleToPeMap;
	LinkedHashMap<Node,Integer> handleToPeStoreMap = new LinkedHashMap<Node, Integer>();
	LinkedHashMap<Node, Double> handleReadWriteRatio = new LinkedHashMap<Node, Double>();
	
	
	private double getHandleReadWriteRatio(Node handle, Loop lp){
		double ret = 0;
		double read = 0;
		double write = 0;
		for(Node succ: graph.getConsumers(handle)){
			Loop succLoop = lg.getLoop(succ);
			if(!(succLoop.equals(lp) || lp.contains(succLoop))){
				continue;
			}
			
			if(isDMAload(succ)){
				read++;
			}else if(isDMAstore(succ)){
				write++;
			}
		}
		
		if(isLoadStore(handle)){
			int lVarID = handle.getValue();
			for(Node nd: graph.getNodes()){
				Loop succLoop = lg.getLoop(nd);
				if(!(succLoop.equals(lp) || lp.contains(succLoop))){
					continue;
				}
				
				if(isDMA(nd)){
					Node otherHandle = nd.getPredecessor(0);
					if(otherHandle != handle && isLoadStore(otherHandle) && lVarID == otherHandle.getValue()){
						if(isDMAload(nd)){
							read++;
						}else if(isDMAstore(nd)){
							write++;
						}
					}
					
					
				}
				
				
			}
			
			
		}
		
		
		
		ret = read/write;
		
		return ret;
	}
	

	/**
	 * Check if PE is busy during given time
	 * 
	 * @param pe
	 *            - PE of interest
	 * @param t
	 *            - starting time
	 * @param dur
	 *            - Duration
	 * @return true iff PE is busy during given interval
	 */
	private boolean peBusy(int pe, int t, int dur) {
		for (int i = t; i < t + dur; i++) {
			if (busyMap.containsKey(i)) {
				if (busyMap.get(i)[pe]) return true;
			}
		}

		return false;
	}

	/**
	 * Fill loopPreds map with correct values.
	 */
	private void setLoopPreds() {
		loopPreds = new LinkedHashMap<Loop, Set<Node>>();
		Loop nodeLp;

		for (Loop lp : loopNodes.keySet()) {
			loopPreds.put(lp, new LinkedHashSet<Node>());
			for (Node nd : loopNodes.get(lp)) {
				for (Node pred : graph.getAllPredecessors(nd)) {
					nodeLp = lg.getLoop(pred);
					if (!lp.equals(nodeLp) && !lg.isChildOf(nodeLp, lp)) {
						loopPreds.get(lp).add(pred);
					}
				}
			}
		}
		
		expandLoopPreds(lg.getRoot());
	}
	
	/**
	 * Add all predecessors of child loops to the predecessors of the loop itself (but not the ones that are part of the loop itself or part of another child node)
	 * @param lp
	 */
	private void expandLoopPreds(Loop lp){
		Set<Node> lpPreds = this.loopPreds.get(lp);
		for(Loop child : lg.getChildren(lp)){
			expandLoopPreds(child);
			Set<Node> childsPreds = new LinkedHashSet<>();
			
			for(Node possiblePRED: loopPreds.get(child)){
				if(!(possiblePRED.getAddress()<= lp.getStop() && possiblePRED.getAddress()>= lp.getStart())){
					childsPreds.add(possiblePRED);
				}
				
			}
			
			
			childsPreds.removeAll(loopNodes.get(lp));
			
			lpPreds.addAll(childsPreds);
		}
	}
	

	/**
	 * Check if a Node is forced to a certain PE and return that PE
	 * 
	 * @param nd
	 *            - Node to check
	 * @return The PE this node must be assigned to, -1 if nd can be assigned to any PE
	 */
	private int checkForcedPE(Node nd) {
		int varAddr = Integer.MIN_VALUE;

		if (isStore(nd)) {
			varAddr = nd.getValue();
		} else if (isLoad(nd)) {
			varAddr = nd.getValue();
		} else if (vNodes.containsKey(nd)) {
			for (Node vNode : vNodes.get(nd)) {
				if (isStore(vNode)) {
					varAddr = vNode.getValue();

					if (lvarPEMap.containsKey(varAddr)) {
						return lvarPEMap.get(varAddr);
					}
				}
				if (isLoad(vNode)) {
					varAddr = vNode.getValue();

					if (lvarPEMap.containsKey(varAddr)) {
						return lvarPEMap.get(varAddr);
					}
				}
				varAddr = -1;
			}
		}

		if (varAddr == Integer.MIN_VALUE) return -1;

		if (lvarPEMap.containsKey(varAddr)) {
			return lvarPEMap.get(varAddr);
		}

		return -1;
	}

	/**
	 * @param nd
	 *            - Node of interest
	 * @return true iff nd is DMA_LOAD or DMA_LOAD64
	 */
	private boolean isDMAload(Node nd) {
		return nd.getOperation().isCacheLoad();
	}

	/**
	 * @param nd
	 *            - Node of interest
	 * @return true iff nd is DMA_STORE or DMA_STORE64
	 */
	private boolean isDMAstore(Node nd) {
		return nd.getOperation().isCacheStore();
	}

	/**
	 * @param nd
	 *            - Node of interest
	 * @return true iff nd is an IF node
	 */
	private boolean isIf(Node nd) {
		return nd.getOperation().isControlFlow();
	}

	/**
	 * @param nd
	 *            - Node of interest
	 * @return true iff nd is load or store
	 */
	private boolean isLoadStore(Node nd) {
		return nd.getOperation().isRegfileAccess();
	}

	/**
	 * @param nd
	 *            - Node of interest
	 * @return true iff nd is store
	 */
	private boolean isStore(Node nd) {
		return nd.getOperation().isRegfileStore();
	}

	/**
	 * @param nd
	 *            - Node of interest
	 * @return true iff nd is load
	 */
	private boolean isLoad(Node nd) {
		return nd.getOperation().isRegfileLoad();
	}

	private boolean isLoopController(Node nd) {
		if (loopControllers.contains(nd)){
			return true;
		} else{
			return false;
		}
	}

	/**
	 * Prepare scheduling.
	 * 
	 * @return false iff any prerequisites for scheduling are not fulfilled, true otherwise
	 */
	private boolean prepare() {
		if (graph == null){
			return false;
		}
		if (lg == null){
			return false;
		}
		if (model == null){
			return false;
		}
		if (prio == null) {
			prio = new LPW(graph, isPrefetch);
		}

		// initialize APSP matrices
		APSP();

		return true;
	}

	/**
	 * Initialize distance and path matrices and apply the Floyd-Warshall algorithm
	 */
	private void APSP() {
		int n = nrOfPEs;

		D = new int[n][n];
		P = new int[n][n];

		for (int s = 0; s < n; s++) {
			for (int d = 0; d < n; d++) {
				P[s][d] = -1;
				if (interconnect[d].contains(s)) {		// if source and destination are connected
					D[s][d] = 1;
				} else {
					D[s][d] = 1000;
				}
			}
			D[s][s] = 0;
		}

		for (int k = 0; k < n; k++) {
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					if (D[i][k] + D[k][j] < D[i][j]) {
						D[i][j] = D[i][k] + D[k][j];

						if (D[i][k] == 1){
							P[i][j] = k;
						} else{
							P[i][j] = P[i][k];
						}
					} else if (D[i][k] + D[k][j] < D[i][j]) {
					}
				}
			}
		}
	}

	static int cmpVal = 1;
	
	/**
	 * @author ruschke Compare attraction attribute
	 */
	private class AttrComparator implements Comparator<Integer> {
		Map<Integer, Integer> base;

		public AttrComparator(Map<Integer, Integer> base) {
			this.base = base;
		}
		
		

		@Override
		public int compare(Integer n1, Integer n2) {
			int cmp = base.get(n2).compareTo(base.get(n1)); // compare reversely -> high to low

			if (cmp == 0) {
				cmp = Integer.compare(peDests[n2].size(), peDests[n1].size());

				if (cmp == 0){
					cmp = 1; // duplicate values should be allowed!
				}
			}
			return cmp;
		}
	}

	/**
	 * @author ruschke Compare the lower bounds of Intervals
	 */
	private class LBComparator implements Comparator<Node> {
		Map<Node, Interval> base;

		public LBComparator(Map<Node, Interval> base) {
			this.base = base;
		}

		@Override
		public int compare(Node n1, Node n2) {
			int cmp = base.get(n1).lbound.compareTo(base.get(n2).lbound);
			if (cmp == 0){
				cmp = 1; // duplicate values should be allowed!
			}
			return cmp;
		}
	}

	private LinkedHashMap<Integer, Integer> constMap = new LinkedHashMap<Integer, Integer>();
	private LinkedHashMap<Integer, Integer> lVarMap = new LinkedHashMap<Integer, Integer>();

	private LinkedHashMap<Long, Integer> longConstMap = new LinkedHashMap<Long, Integer>();
	private LinkedHashMap<Long, Integer> longConstTwinMap = new LinkedHashMap<Long, Integer>();

	private LinkedHashMap<Integer, Integer> lVarTwinMap = new LinkedHashMap<Integer, Integer>();
	private int globalAllocAddr = 0;


	/**
	 * Apply the left edge algorithm to find node allocation
	 * 
	 * @param lifeMap
	 *            - Lifetime map for all nodes
	 * @param pe
	 *            - PE of current lifeTime map
	 * @return A mapping between nodes and their regfile slots
	 */
	private Map<Node, Integer> leftEdge(Map<Node, Interval> lifeMap, int pe) {
		Comparator<Node> lbcomp = new LBComparator(lifeMap);
		Map<Node, Interval> ltsort = new TreeMap<Node, Interval>(lbcomp);
		ltsort.putAll(lifeMap);

		Map<Node, Integer> regMap = new LinkedHashMap<Node, Integer>();

		int currmax;
		Set<Node> used = new LinkedHashSet<Node>();
		Set<Node> toUse = new LinkedHashSet<Node>(ltsort.keySet());

		int addr = 0;
		int regFileSize = model.getPEs().get(pe).getRegfilesize();
		boolean[] slotOccupied = new boolean[regFileSize];
		for (int i = 0; i < regFileSize; i++) {
			slotOccupied[i] = false;
		}

		int maxUsedGlobal = 0; // used to determine the max nr of regFile Entries

		for (Node n : toUse) {// / TODO use Priority so that nodes that are needed on more PEs are more likely to be scheduled on "their" global place
			if (isConst(n) && !output64(n)) {
				Integer address = constMap.get(n.getValue());
				if (address == null) {
					if (globalAllocAddr >= regFileSize) {
						// We can not use this any more
						continue;
					}
					constMap.put(n.getValue(), globalAllocAddr);
					slotOccupied[globalAllocAddr] = true;
					regMap.put(n, globalAllocAddr);
					used.add(n);
					maxUsedGlobal = globalAllocAddr;
					globalAllocAddr++;

				} else {
					if (address >= regFileSize) {
						// We can not use this any more
						continue;
					}
					if (address > maxUsedGlobal) maxUsedGlobal = address;
					slotOccupied[address] = true;
					regMap.put(n, address);
					used.add(n);
				}
			} else if (isConst(n)) {
				Integer address;
				Map<Long, Integer> mapToUse;

				if (twinNodes.containsKey(n)) {
					mapToUse = longConstMap;
				} else {
					mapToUse = longConstTwinMap;
				}

				address = mapToUse.get(n.getValueLong());
				if (address == null) {
					if (globalAllocAddr >= regFileSize) {
						// We can not use this any more
						continue;
					}
					mapToUse.put(n.getValueLong(), globalAllocAddr);
					slotOccupied[globalAllocAddr] = true;
					regMap.put(n, globalAllocAddr);
					used.add(n);
					maxUsedGlobal = globalAllocAddr;
					globalAllocAddr++;

				} else {
					if (address >= regFileSize) {
						// We can not use this any more
						continue;
					}
					if (address > maxUsedGlobal){
						maxUsedGlobal = address;
					}
					slotOccupied[address] = true;
					regMap.put(n, address);
					used.add(n);
				}
			}
			if (isLoadStore(n) && !(lvarPEMap.containsKey(n.getValue()) && lvarPEMap.get(n.getValue()) != pe)) { // Second condition checks whether this is a local copy
				Map<Integer, Integer> mapToUse;
				if (output64(n) && twinNodes.containsValue(n)) {
					mapToUse = lVarTwinMap;
				} else {
					mapToUse = lVarMap;
				}
				Integer address = mapToUse.get(n.getValue());
				if (address == null) {
					if (globalAllocAddr >= regFileSize) {
						// We can not use this any more
						continue;
					}
					mapToUse.put(n.getValue(), globalAllocAddr);
					slotOccupied[globalAllocAddr] = true;
					regMap.put(n, globalAllocAddr);
					used.add(n);
					maxUsedGlobal = globalAllocAddr;
					globalAllocAddr++;
				} else {
					if (address >= regFileSize) {
						// We can not use this any more
						continue;
					}
					if (address > maxUsedGlobal){
						maxUsedGlobal = address;
					}
					slotOccupied[address] = true;
					regMap.put(n, address);
					used.add(n);
				}
			}
		}

		toUse.removeAll(used);
		while (slotOccupied[addr]) {
			addr++;
			if (addr >= regFileSize && toUse.size() > 0) {
				throw new NotEnoughHardwareException("PE" + pe + " has not enough registers. More than " + regFileSize + " are required.");
			}
		}

		toUseLoop: while (toUse.size() > 0) {
			currmax = -1;
			used.clear();
			for (Node nd : toUse) {
				Interval life = lifeMap.get(nd);
				if (life.lbound > currmax) {
					currmax = life.ubound;
					regMap.put(nd, addr);
					used.add(nd);
				}
			}
			for (Node nd : used) {
				toUse.remove(nd);
			}

			slotOccupied[addr] = true;
			addr++;
			if (addr >= regFileSize) {
				if (toUse.size() > 0) {
					throw new NotEnoughHardwareException("PE" + pe + " has not enough registers. More than " + regFileSize + " are required.");
				}
				break;
			}
			while (slotOccupied[addr]) {
				addr++;
				if (addr >= regFileSize) {
					if (toUse.size() > 0) {
						throw new NotEnoughHardwareException("PE" + pe + " has not enough registers. More than " + regFileSize + " are required.");
					}
					break toUseLoop;
				}
			}
		}

		int maxRegFileSize = Math.max(addr, maxUsedGlobal);

		if (maxRegFileSize > model.getPEs().get(pe).getRegfilesize()) {
			throw new NotEnoughHardwareException("PE" + pe + " has not enough registers. More than " + regFileSize + " are required.");
		}

		return regMap;
	}

	/**
	 * Apply the left edge algorithm to find C-Box allocation
	 * 
	 * @param lifeMap
	 *            - Lifetime map for all IF nodes
	 * @param pe
	 *            - PE of current lifeTime map
	 * @return A mapping between IF nodes and their C-Box slots
	 */
	private Map<Node, Integer> leftEdgeIf(Map<Node, Interval> lifeMap) {
		Comparator<Node> lbcomp = new LBComparator(lifeMap);
		Map<Node, Interval> ltsort = new TreeMap<Node, Interval>(lbcomp);
		ltsort.putAll(lifeMap);

		Map<Node, Integer> regMap = new LinkedHashMap<Node, Integer>();

		int currmax;
		Set<Node> used = new LinkedHashSet<Node>();
		Set<Node> toUse = new LinkedHashSet<Node>(ltsort.keySet());

		int addr = 0;

		while (toUse.size() > 0) {
			currmax = -1;
			used.clear();
			for (Node nd : toUse) {
				Interval life = lifeMap.get(nd);
				if (life.lbound > currmax) {
					currmax = life.ubound;
					regMap.put(nd, addr);
					used.add(nd);
				}
			}
			for (Node nd : used) {
				toUse.remove(nd);
			}
			addr++;
		}


		if (addr * 2 > model.getcBoxModel().getMemorySlots()) {
			throw new NotEnoughHardwareException("C-Box too small! " + (addr * 2) + " slots required!");
		}

		return regMap;
	}

	/**
	 * Print the PE utilization after scheduling
	 */
	public void printPEutilization() {
		Set<Node> nds;
		int[] cTimes;

		int schLength = sched.length();

		boolean[][] peUsed = new boolean[schLength][nrOfPEs];

		for (int t = 0; t < schLength; t++) {
			for (int pe = 0; pe < nrOfPEs; pe++) {
				peUsed[t][pe] = false;
			}

			nds = sched.nodes(t);

			if (nds != null) for (Node nd : nds) {
				peUsed[t][peMap.get(nd)] = true;
			}
		}

		int t;

		int[] peUsage = new int[nrOfPEs];
		Arrays.fill(peUsage, 0);

		for (t = 0; t < schLength; t++) {
			for (int pe = 0; pe < nrOfPEs; pe++) {
				if (peUsed[t][pe]) peUsage[pe]++;
			}
		}

		double[] peUti = new double[nrOfPEs];

		for (int pe = 0; pe < nrOfPEs; pe++) {
			peUti[pe] = (double) peUsage[pe] / ((double) schLength);
		}

		Map<Loop, Double> loopUti = new LinkedHashMap<>();

		int start, end, cnt;

		for (Loop lp : loopTimes.keySet()) {
			cTimes = loopTimes.get(lp);
			start = cTimes[0];
			end = cTimes[1];

			cnt = 0;

			for (t = start; t <= end; t++) {
				for (int pe = 0; pe < nrOfPEs; pe++) {
					if (peUsed[t][pe]) cnt++;
				}
			}

			loopUti.put(lp, (double) (cnt / ((double) ((end - start + 1) * nrOfPEs))));
		}

		System.out.println("PE Utilization");
		System.out.println("Utilization per PE");
		for (int pe = 0; pe < nrOfPEs; pe++) {
			System.out.println("PE" + pe + ": " + peUti[pe] * 100.0 + "%");
		}

		System.out.println("\nUtilization per loop");
		for (Loop lp : loopUti.keySet()) {
			System.out.println(lp + ": " + loopUti.get(lp) * 100.0 + "%");
		}
		System.out.println("\n");
	}
	
	/**
	 * Produce a dotfile for graphviz neato that shows the schedule
	 * 
	 * @param dotFileName
	 *            - Filename to be written
	 */
	public void drawSched(String dotFileName) {
		if (sched == null) return;
		try {
			BufferedWriter dotFile = new BufferedWriter(new FileWriter(dotFileName));
			// int scaleY = 3;
			double scaleY = 2.5;
			double scaleX = 2.5;
			double maxY = sched.length() * scaleY;

			double X = 0;
			double Y = maxY;

			Map<Node, Interval> intMap = new LinkedHashMap<>();

			for (Node nd : sched.nodes()) {
				intMap.put(nd, sched.slot(nd));
			}

			dotFile.write("//do not use DOT to generate pdf use NEATO or FDP\n");
			dotFile.write("digraph{\n");
			dotFile.write("splines=\"ortho\";\n");

			double yoff;
			double height;

			// draw copynodes
			 LinkedHashSet<Integer>[] availtime;
			 LinkedHashSet<Integer> tt;

			for (int pe = 0; pe < nrOfPEs; pe++) {
				X = scaleX * pe;
				dotFile.write("\"PE" + pe + "\"[shape=\"box\", style=\"filled\", color=\"#00222222\", pos=\"" + X + "," + (maxY + scaleY)
						+ "!\", height=\"" + (1.5) + "\", width=\"" + (1.5) + "\"];\n");
			}

			int pe;

			for (Node nd : copyMap.keySet()) {
				availtime = copyMap.get(nd);
				for (int i = 0; i < nrOfPEs; i++) {
					tt = availtime[i];
					if(tt!= null)
					for(int t : tt){
						if (t > 0) {
							pe = i;
							X = scaleX * pe;
							Y = maxY - (t - 1) * scaleY; // copymap declares when copies are available -> they are produced 1 timestep earlier (actually 2 for 64 bit values)
							if (isLoadStore(nd) && lvarPEMap.get(nd.getValue()) == pe) continue;
							
							String name = nd.toString();
							if(name.length()>10){
								name = name.substring(0, 10);
							}
							if (output64(nd)) {
								Y = maxY - (t - 2) * scaleY; // copymap declares when copies are available -> they are produced 1 timestep earlier (actually 2 for 64 bit values)
								dotFile.write("\"C" + i + ":" + name + "\"[shape=\"ellipse\", style=\"filled\", color=\"#00111111\", pos=\"" + X
										+ "," + (Y - 1.25) + "!\", height=\"4.0\", width=\"1.5\"];\n");
							} else {
								Y = maxY - (t - 1) * scaleY; // copymap declares when copies are available -> they are produced 1 timestep earlier (actually 2 for 64 bit values)
								dotFile.write("\"C" + i + ":" + name + "\"[shape=\"circle\", style=\"filled\", color=\"#00111111\", pos=\"" + X
										+ "," + Y + "!\", width=\"1.5\"];\n");
							}
						}
					}
				}
			}
//			 end copynodes*/

			for (int i = 0; i < sched.length(); i++) {
				Y = maxY - i * scaleY;
				// X = 0;

				dotFile.write("\"" + i + "\"[shape=\"box\", style=\"filled\", color=\"#00222222\", pos=\"-2," + Y
						+ "!\", height=\"1.5\", width=\"1.5\"];\n");

				if (sched.nodes(i) != null) for (Node n : sched.nodes(i)) {
					// X += scaleX;
					pe = this.peMap.get(n);
					X = scaleX * pe;
					if (i == 0 || !sched.nodes(i - 1).contains(n)) {
						if (operations[pe].get(n.getOperation()) == 1) {
							dotFile.write("\"" + n.toString() + "\"[shape=\"circle\", style=\"filled\", color=\"#004E8ABF\", pos=\"" + X + "," + Y
									+ "!\", height=\"1.5\", width=\"1.5\"];\n");
						} else {
							height = operations[pe].get(n.getOperation()) * 2.5 - 1;
							yoff = height / 2 - 0.75;
							dotFile.write("\"" + n.toString() + "\"[shape=\"ellipse\", style=\"filled\", color=\"#004E8ABF\", pos=\"" + X + ","
									+ (Y - yoff) + "!\", height=\"" + height + "\", width=\"1.5\"];\n");
						}
						// for(Node suc : graph.getAllSuccessors(n)){
						if (graph.getConsumers(n) != null) for (Node suc : graph.getConsumers(n)) {
							if (sFuseNodes.keySet().contains(suc)) {
								if (graph.getConsumers(suc) != null) for (Node trueSucs : graph.getConsumers(suc)) {
									dotFile.write("\"" + n.toString() + "\" -> \"" + trueSucs.toString() + "\";\n");
								}
							} else {
								dotFile.write("\"" + n.toString() + "\" -> \"" + suc.toString() + "\";\n");
							}
						}
					}
				}
			}
			
			
			Loop root = lg.getRoot();
			LinkedHashSet<Loop> loops = new LinkedHashSet<>(), newLoops = new LinkedHashSet<>();
			loops.add(root);
			int lvl = 0;
			while(loops.size()!=0){
				for(Loop lp : loops){
					int[] lpTime = loopTimes.get(lp);
					int tSteps = lpTime[1]-lpTime[0];
					double length = tSteps*2.5 + 0.4;
//					length += (tSteps +1 ) + 1.5 + 0.4;
					double y =  maxY - (lpTime[1]+lpTime[0])/2.0 * scaleY;
					double x = -3.2 - lvl*0.4;

					if(lg.getChildren(lp) != null){
						newLoops.addAll(lg.getChildren(lp));
					}
					dotFile.write("\"" + lp + "\"[label=\"\", shape=\"box\", style=\"filled\", color=\"#00222222\", pos=\""+x+"," + y
							+ "!\", height=\""+ length +"\", width=\"0.2\"];\n");
				}
				lvl++;
				loops = newLoops;
				newLoops = new LinkedHashSet<>();
			}
			
			/*
			 * "loop1"[label="", shape="box", style="filled", color="#00222222", pos="-4,33.75!", height="4.9", width="0.1"];
			 */

			dotFile.write("}");
			dotFile.flush();
			dotFile.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	/**
	 * Produce a texfile that shows the schedule based on the contexts. Contexts have to be created first!
	 * 
	 * @param texFileName
	 *            - Filename to be written
	 */
	public void draw(String texFileName) {
		double WIDTH_PE = 5;
		double WIDTH_CBOX = 4;
		double WIDTH_STEP_LABEL = 3;
		double WIDTH_LOOP = 1;
		double MARGIN_X = 0.2;
		double MARGIN_Y = 0.2;
		double HEIGHT_OPERATION = 3;
		double HEIGHT_PE_LABEL = 2;
		double HEIGHT_OUT_NODE = 1;
		double HEIGHT_CONNECTIONS = 1;

		double HEIGHT_TIMESTEP = HEIGHT_OPERATION + HEIGHT_CONNECTIONS + HEIGHT_OUT_NODE;
		
		double totalWidth = WIDTH_STEP_LABEL + (nrOfCBoxes)*WIDTH_CBOX + (nrOfPEs + 1)*WIDTH_PE;
		double totalHeight = (HEIGHT_TIMESTEP)*sched.length() + HEIGHT_PE_LABEL;
		
		boolean printFull = true;
		
		if(totalHeight > 600){
			printFull = false;
			HEIGHT_OPERATION = 1;
			HEIGHT_CONNECTIONS = 0.5;
			HEIGHT_TIMESTEP = HEIGHT_OPERATION + HEIGHT_CONNECTIONS + HEIGHT_OUT_NODE;
			totalHeight = (HEIGHT_TIMESTEP)*sched.length() + HEIGHT_PE_LABEL;
		}
		
		

		
		texFileName = texFileName.replace('(', '-').replace(')', '-');
		texFileName = texFileName.replace(';', '-');
		
		try{
			BufferedWriter texFile = new BufferedWriter(new FileWriter(texFileName));
			

			int schedLength = sched.length();
			
			texFile.write("\\documentclass[pstricks,border=12pt]{standalone}\n \\usepackage{pstricks-add}\n\\usepackage{subscript}\n\\usepackage{pst-node}\n\\usepackage{pstricks}\n\\newrgbcolor{lightgreen}{0.89 1 0.78}\n\\newrgbcolor{lightblue}{0.67 0.86 1}\n\\newrgbcolor{lightred}{1 0.86 0.67}\n\\begin{document}\n\\sffamily\n\\begin{pspicture}[showgrid=false]("+(-WIDTH_LOOP*10)+",0)("+totalWidth+","+totalHeight+")\n");
			
			
			for (int t = 0; t < schedLength; t++) {
				for (int pe = 0; pe < nrOfPEs; pe++) {
					if(schedRepPE[pe][t].getDuration() > 1){
						String fillstyle="solid";
						if(isPrefetch != null && schedRepPE[pe][t].getOperation() != null && isPrefetch[schedRepPE[pe][t].getOperation().getAddress()]){
							fillstyle = "vlines, hatchcolor=lightblue";
						}
						
						texFile.write("\\psframe[linewidth = 1.1pt,  fillstyle="+fillstyle+", fillcolor="+schedRepPE[pe][t].getPStricksColor()+"]("+(WIDTH_STEP_LABEL+pe*WIDTH_PE+MARGIN_X)+","+(HEIGHT_TIMESTEP*(schedLength-t-schedRepPE[pe][t].getDuration())+MARGIN_Y)+")("+(WIDTH_STEP_LABEL+(pe+1)*WIDTH_PE-MARGIN_X)+","+(HEIGHT_TIMESTEP*(schedLength-1-t)+MARGIN_Y)+")\n");
					}
					texFile.write(schedRepPE[pe][t].getPEOutputPSTricks(WIDTH_STEP_LABEL+pe*WIDTH_PE+MARGIN_X, (schedLength-1-t)*HEIGHT_TIMESTEP+HEIGHT_CONNECTIONS+HEIGHT_OPERATION+MARGIN_Y, WIDTH_PE-2*MARGIN_X, HEIGHT_OUT_NODE-2*MARGIN_Y));
					texFile.write(schedRepPE[pe][t].getPEdescriptionPSTricks(WIDTH_STEP_LABEL+pe*WIDTH_PE+MARGIN_X, (schedLength-1-t)*HEIGHT_TIMESTEP+MARGIN_Y, WIDTH_PE-2*MARGIN_X, HEIGHT_OPERATION-2*MARGIN_Y,printFull, isPrefetch));
					for(Integer targetPE: schedRepConnections[t].get(pe)){
						texFile.write("\\psline[linewidth=3pt]{->}("+(WIDTH_STEP_LABEL+(pe+0.5)*WIDTH_PE)+","+( (schedLength-1-t)*HEIGHT_TIMESTEP+HEIGHT_CONNECTIONS+HEIGHT_OPERATION)+")("+(WIDTH_STEP_LABEL+(targetPE+0.5)*WIDTH_PE)+","+( (schedLength-1-t)*HEIGHT_TIMESTEP+HEIGHT_OPERATION)+")");
					}
				}
				
				for(int box = 0; box < nrOfCBoxes; box++){
					texFile.write(schedRepCBox[box][t].getCBoxDescriptionPSTricks(WIDTH_STEP_LABEL+nrOfPEs*WIDTH_PE+MARGIN_X +box*WIDTH_PE, (schedLength-1-t)*HEIGHT_TIMESTEP+MARGIN_Y, WIDTH_PE-2*MARGIN_X, HEIGHT_OUT_NODE-2*MARGIN_Y));
					for(int port = 0; port < cBoxOutputsPerBox; port++){
						texFile.write(schedRepCBox[box][t].getCBoxOutputPSTricks(WIDTH_STEP_LABEL+nrOfPEs*WIDTH_PE+MARGIN_X +box*WIDTH_PE, (schedLength-1-t)*HEIGHT_TIMESTEP+HEIGHT_CONNECTIONS+HEIGHT_OUT_NODE+(cBoxOutputsPerBox-port-1)*((HEIGHT_OPERATION-2*MARGIN_Y)/cBoxOutputsPerBox)+MARGIN_Y, WIDTH_PE-2*MARGIN_X, (HEIGHT_OPERATION-2*MARGIN_Y)/cBoxOutputsPerBox,port));
					}
				}
				
				texFile.write("\\psframe[linewidth = 1.1pt,  fillstyle=solid, fillcolor=lightgray]("+MARGIN_X+","+ ((schedLength-1-t)*HEIGHT_TIMESTEP+MARGIN_Y)+")("+(WIDTH_STEP_LABEL-MARGIN_X)+","+((schedLength-t)*HEIGHT_TIMESTEP-MARGIN_Y)+")\n");
				texFile.write("\\rput("+(WIDTH_STEP_LABEL/2.0)+","+((schedLength-0.5-t)*HEIGHT_TIMESTEP)+"){\\large"+t + "\\normalsize}\n");
			}
			
			for(int pe = 0; pe < nrOfPEs; pe++){
				texFile.write("\\psframe[linewidth = 1.1pt,  fillstyle=solid, fillcolor=lightgray]("+(WIDTH_STEP_LABEL+pe*WIDTH_PE+MARGIN_X)+","+(HEIGHT_TIMESTEP*schedLength+MARGIN_Y)+")("+(WIDTH_STEP_LABEL+(pe+1)*WIDTH_PE-MARGIN_X)+","+(HEIGHT_TIMESTEP*schedLength+HEIGHT_PE_LABEL-MARGIN_Y)+")\n");
				texFile.write("\\rput("+(WIDTH_STEP_LABEL+pe*WIDTH_PE+MARGIN_X + WIDTH_PE/2.0)+","+((schedLength)*HEIGHT_TIMESTEP+ HEIGHT_PE_LABEL/2.0)+"){\\large PE "+pe + "\\normalsize}\n");
			}
			
			texFile.write("\\psframe[linewidth = 1.1pt,  fillstyle=solid, fillcolor=lightblue]("+(WIDTH_STEP_LABEL+nrOfPEs*WIDTH_PE+MARGIN_X)+","+(HEIGHT_TIMESTEP*schedLength+MARGIN_Y)+")("+(WIDTH_STEP_LABEL+(nrOfPEs+1)*WIDTH_PE-MARGIN_X)+","+(HEIGHT_TIMESTEP*schedLength+HEIGHT_PE_LABEL-MARGIN_Y)+")\n");
			texFile.write("\\rput("+(WIDTH_STEP_LABEL+nrOfPEs*WIDTH_PE+MARGIN_X + WIDTH_PE/2.0)+","+((schedLength)*HEIGHT_TIMESTEP+ HEIGHT_PE_LABEL/2.0)+"){\\large C-Box\\normalsize}\n");
			
			Loop root = lg.getRoot();
			LinkedHashSet<Loop> loops = new LinkedHashSet<>(), newLoops = new LinkedHashSet<>();
			loops.add(root);
			int lvl = 0;
			while(loops.size()!=0){
				for(Loop lp : loops){
					int[] lpTime = loopTimes.get(lp);
					double x = - (lvl+1)*WIDTH_LOOP;
					
					

					if(lg.getChildren(lp) != null){
						newLoops.addAll(lg.getChildren(lp));
					}
					texFile.write("\\psframe[linewidth = 1.1pt,  fillstyle=solid, fillcolor=lightblue]("+(x+MARGIN_X)+","+(HEIGHT_TIMESTEP*(schedLength-lpTime[0])- MARGIN_Y)+")("+(x + WIDTH_LOOP -MARGIN_X)+","+(HEIGHT_TIMESTEP*(schedLength-1-lpTime[1]) + MARGIN_Y)+")\n");
				}
				lvl++;
				loops = newLoops;
				newLoops = new LinkedHashSet<>();
			}
			
			
			
			
			
			texFile.write("\\end{pspicture}\n\\end{document}");
			texFile.close();
		} catch(IOException e){
			System.out.println(e.getMessage());
		}
//////////////////////////////////////////////////////////////////////////
	
	
	}
	
	/**
	 * Inserts empty timesteps in the middle of an existing schedule
	 * @param atTime time at which the empty timesteps are inserted
	 * @param numberOfSteps 
	 */
	public void insertEmptyTimesteps(int atTime, int numberOfSteps){
		int oldSize = sched.length();
		
		LinkedHashSet<Integer> conditionalBorderNodes = new LinkedHashSet<>();
		
		LinkedHashMap<Node,Integer> borderNodes = new LinkedHashMap<>();
		
		LinkedHashMap<Node, Interval> newIntervals = new LinkedHashMap<>();
		
		for(Node nd: sched.nodes()){
			Interval interval = sched.slot(nd);
			if(interval.ubound>=atTime){
				if(interval.lbound<atTime){
					borderNodes.put(nd,peMap.get(nd));
					if(isConditional(nd)){
						for(int t = atTime; t<=interval.ubound; t++){
							conditionalBorderNodes.add(t);
						}
					}
					continue;
				}
				
				Map<Integer, Integer> cbsel = cboxSelectRead.get(nd);
				if(cbsel == null){
					if (sFuseNodes.containsValue(nd)) {
						for (Node ndkey : sFuseNodes.keySet()) {
							if (sFuseNodes.get(ndkey) == nd) {
								cbsel = cboxSelectRead.get(ndkey); 
								break;
							}
						}
					}
				}
				
				if(cbsel != null){
					for(int t = interval.ubound; t >= interval.lbound; t--)
					if(cbsel.containsKey(t)){
						int sel = cbsel.remove(t);
						cbsel.put(t+numberOfSteps, sel);
					}
					
				}
				
				newIntervals.put(nd,new Interval(interval.lbound+numberOfSteps, interval.ubound + numberOfSteps));				
			}
			
		}
		
		for(Node nd : newIntervals.keySet()){
			sched.add(nd, newIntervals.get(nd));
		}
		
		
		for(int t = oldSize-1; t >= atTime; t --){
			int newT = t + numberOfSteps;
			boolean [] bmEntry = busyMap.remove(t);
			if(bmEntry != null){
				busyMap.put(newT, bmEntry);
			}
			
			Node[] onmEntry = outNodeMap.remove(t);
			if(onmEntry != null){
				outNodeMap.put(newT, onmEntry);
			}
			
			Node[] inmEntry = intNodeMap.remove(t);
			if(inmEntry != null ){
				intNodeMap.put(newT, inmEntry);
			}
			
			Node[] dnmEntry = dmaNodeMap.remove(t);
			if(dnmEntry != null ){
				dmaNodeMap.put(newT, dnmEntry);
			}
			
			for(int slot = 0; slot < cboxOutputs; slot++){
				Node cbrEntry = cBoxRead[slot].remove(t);
				if(cbrEntry != null){
					if(conditionalBorderNodes.contains(t)){
						cBoxRead[slot].put(t, cbrEntry);
					}
					cBoxRead[slot].put(newT, cbrEntry);
				}
			}
			
			for(int slot = 0; slot < nrOfCBoxes; slot++){
				if(cBoxWrite[slot].remove(t)){
					cBoxWrite[slot].add(newT);
				}
			}
			
			for(Node nd: graph){
				Node fusedInto = sFuseNodes.get(nd);
				if(fusedInto != null && borderNodes.containsKey(fusedInto) && borderNodes.get(fusedInto) == t){
					continue; // TODO ONLY ON THE RIGHT PE!!!!!!
				}
				LinkedHashSet<Integer>[] cpm = copyMap.get(nd);
				if(cpm != null){
					for(int pe = 0; pe < nrOfPEs; pe++){
						if(cpm[pe] != null && cpm[pe].remove(t+1)){
							cpm[pe].add(newT+1);
						}
					}
				}
			}
				
			
				
			
		}
		
		for(Node borderNode: borderNodes.keySet()){
			int ubound = sched.slot(borderNode).ubound;
			for(int t = atTime; t<=ubound; t++){
				boolean[] bm = busyMap.get(t);
				if(bm == null){
					bm = new boolean[nrOfPEs];
					busyMap.put(t, bm);
				}
				bm[borderNodes.get(borderNode)] = true;
				
				
				int newT = t + numberOfSteps;
				
				bm = busyMap.get(newT);
				bm[borderNodes.get(borderNode)] = false;
				
				
			}
			
		}
		
		
		
		
		
		
		for(int[] ltime: loopTimes.values()){
			if(ltime[0]>=atTime){
				ltime[0] = ltime[0] + numberOfSteps;
				ltime[1] = ltime[1] + numberOfSteps;
			} else if(ltime[1] >= atTime){
				ltime[1] = ltime[1] + numberOfSteps;
			}
		}
		
		
		
	}
	
	
	public int getCBoxSelect(Node nd, int time){
		if(cboxSelectRead.containsKey(nd)){
			return cboxSelectRead.get(nd).get(time);
		}
		if (sFuseNodes.containsValue(nd)) {
			for (Node ndkey : sFuseNodes.keySet()) {
				if (sFuseNodes.get(ndkey) == nd) {
					nd = ndkey;
					break;
				}
			}
		}
		return cboxSelectRead.get(nd).get(time);
		
	}
	
	public int getOffsetModulo(Node nd){
		int mod = -1;
		
		
//		if(nd.getPredecessor(0).getOperation().equals(Amidar.OP.LOAD)){
//		
//		Node index = graph.getOperands(nd, 1);
////		System.err.println( "------------------ " + index);
//		
//		if(index.getOperation() == Amidar.OP.STORE || index.getOperation() == Amidar.OP.LOAD){
//			mod = 0;
//		} else if( index.getOperation() == Amidar.OP.IADD){
//			int constCnt = 0;
//			int lvCnt = 0;
//			int constValue = -33333;
//			if( graph.getOperands(index, 0).getOperation() == Amidar.OP.CONST){
//				constCnt++;
//				constValue = graph.getOperands(index, 0).getValue();
//			} else if(graph.getOperands(index, 0).getOperation() == Amidar.OP.STORE || graph.getOperands(index, 0).getOperation() == Amidar.OP.LOAD  ){
//				lvCnt++;
//			}
//			
//			if( graph.getOperands(index, 1).getOperation() == Amidar.OP.CONST){
//				constCnt++;
//				constValue = graph.getOperands(index, 1).getValue();
//			} else if(graph.getOperands(index, 1).getOperation() == Amidar.OP.STORE || graph.getOperands(index, 1).getOperation() == Amidar.OP.LOAD  ){
//				lvCnt++;
//			}
//			
//			if(constCnt == 1 && lvCnt == 1){
//				mod = constValue % 4;
//			}
//			
//		} else if(index.getOperation() == Amidar.OP.CONST){
////			mod = index.getValue()%4;
//		}
//		}
		
//		System.out.println("MOOOODDD " + mod);
		
		return mod;
	}
	
}
