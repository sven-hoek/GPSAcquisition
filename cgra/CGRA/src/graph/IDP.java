package graph;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.sun.org.apache.xalan.internal.xsltc.runtime.Operators;

import accuracy.Range;
import graph.ANode.Predecessor;
import operator.Binary;
import operator.Implementation;
import operator.Operator;
import operator.Unary;
import target.UltraSynth;
import target.UltraSynth.OP;


/**
 * Iterative Data Path.
 * 
 * <h4>Node types</h4>
 * The iXtronics CAMeL-View IR models a periodically executed data path with inputs, parameters, constants, states, 
 * predecessors, and outputs (see {@link Type}). So far, the graph does not describe control dependencies (loops and
 * branches) explicitly. Instead, all back edges between successive loop bodies are modeled by {@link Type#predecessor}
 * {@link Node}s: The value of the {@link Type#predecessor} during a whole iteration (i.e., application cycle) equals 
 * the value of its data predecessor at the end of the last application cycle.
 * 
 * <h4>Graph generation</h4>
 * The CAMeL-View IR can be parsed from a JSON representation of the CAMeL-View DSC format. This initial IR contains
 * internal {@link Type#state}s, whose data predecessors are their first temporal derivatives. The current system time
 * is calculated as {@code (timeCnt++)*h}, if any internal computation is depending on the system time (i.e., the
 * {@link #time} {@link Node} is explicitly represented in the JSON input).
 *  
 * 
 * <h4>Graph modification</h4>
 * An integrator expansion has to be applied to calculate the values of the next states in the next application cycle 
 * based on their first temporal derivatives and the application period (i.e., stepsize h). The following integrator 
 * expansions are implemented so far
 * <ul>
 *   <li> {@link #expandEuler} - first order integrator
 *   <li> {@link #expandHeun}  - second order integrator
 * </ul>
 * Before expanding the integrators, the {@link #stepsize} has to be set (if not already done during the JSON import).
 * In addition, some optimization passes can be applied to reduce the overall complexity of the graph: 
 * <ul>
 *   <li> {@link #aliasOperation}
 *   <li> {@link #commonSubexpressionElimination}
 *   <li> {@link #constantPropagation}
 *   <li> {@link #deadCodeElimination}
 * </ul>
 * To better match the control flow scheme used for the {@link target.Amidar} {@link Processor}, multiplexers can be 
 * substituted by predicated stores (see {@link #muxToPredication()}). 
 * 
 * <h4>Bit width optimization</h4>
 * Before applying the CGRA scheduler, a {@link #bitwidthAnalysis} should be applied to optimize the {@link Range} and
 * precision associated with each {@link Operator}, as this effects the bit width and thus potentially the latency of 
 * the {@link Operators} {@link Implementation}s. 
 * 
 * <h4>Graph execution</h4>
 * To analyze the effects of the {@link Operator} quantization, the graph can be evaluated per application cycle by
 * <ul>
 *   <li> {@link #scheduleSimulation}       - once per graph
 *   <li> {@link #resetSimulation}          - once per execution
 *   <li> {@link ANode#setSimulationValue}  - once per application cycle (to set inputs and parameters)
 *   <li> {@link #stepSimulation}           - once per application cycle
 *   <li> {@link ANode#getSimulationValue}  - once per application cycle (to read outputs) 
 * </ul> 
 * 
 * <h4>Graph visualization</h4>
 * Finally, the graph can be analyzed (see {@link #getStatistics()} and exported in dot format (see {@link #toString}).
 * 
 * @author Andreas Engel [engel@esa.cs.tu-darmstadt.de]
 */
public class IDP extends CDFG implements Cloneable {
  
  
/*
 * Instance fields
 **********************************************************************************************************************/

  /**
   * Next ID to be assigned to auto-generated {@code Node}s
   */
  protected int  autoID = -1;
  
  /**
   * Reference to the {@code Node} representing the current system time
   */
  protected ANode.Operation time = null;
  
  /**
   * Reference to the {@code Node} representing the stepsize for incrementing the system time
   */
  protected ANode.Constant.Constant stepsize = null;
  
  public ANode.Constant.Constant getStepsize() {
	return stepsize;
}

/**
   * Mapping of {@link OP#LOAD}s in {@link ANode.Predecessor}s to the corresponding 
   * {@link OP#STORE} {@link ANode.Operation} from the previos integrator cycle.
   */
  protected LinkedHashMap<ANode.Predecessor, ANode> backedges = new LinkedHashMap<ANode.Predecessor, ANode>();
  
  /**
   * Mapping of constant values to their representing {@link ANode}s
   */
  protected LinkedHashMap<Double, ANode.Constant> constants = new LinkedHashMap<Double, ANode.Constant>();
  
  /**
   * Sequence of operations to be executed for graph simulation.
   */
  protected LinkedList<ANode> schedule = new LinkedList<ANode>();
  
  /**
   * Contains all predecessors (both data and control dependencies) of a {@link Node} and all of its predecessors
   */
  protected LinkedHashMap<Node, LinkedHashSet<Node>> allTransitivePredecessors = null;
  
  /**
   * Contains all successors (both data and control dependencies) of a {@link Node} and all of its successors
   */
  protected LinkedHashMap<Node, LinkedHashSet<Node>> allTransitiveSuccessors = null;

  
/*
 * Graph generation
 **********************************************************************************************************************/

  /**
   * Parse a graph from JSON, optimize structure and expand integrator.
   * @param file     path to JSON representation of CAMeL-View IR
   * @param heun     if set, second order Heun integrators are used (else first order Euler)
   * @param stepsize targeted cycle duration
   * @param alias    list of {@link Operator}s that should be substituted by equivalent expressions 
   *                 (see {@link #aliasOperation}
   * @return         the final graph
   */
  public static IDP parseAndOptimize(String file, boolean heun, double stepsize, Operator ...alias) {
    IDP graph = new IDP();
    
    // parse JSON
    graph.parse(file);
    
    // optimize
    for (Operator op : alias) graph.aliasOperation(op);
    graph.commonSubexpressionElimination(false);
    graph.constantPropagation();
    graph.deadCodeElimination();
  
    // use AMIDAR-style flow control
    graph.prepareMuxDecision();
    graph.muxToPredication();
  
    // integrator expansion
    graph.setStepsize(stepsize);
    if (heun) graph.expandHeun(); else graph.expandEuler();
    
    // final optimizations
    graph.bitwidthAnalysis(); 
    
    return graph;
  }

  /**
   * Parse CAMeL-View IR from JSON file.
   * <p>
   * This method exploits (but does not require) the topological sorting of the {@link Node}s in the JSON input file. 
   * Only back edges are resolved in a second pass through the {@link Node}s. 
   * <p>
   * If the {@link #time} {@link Node} is specified in the JSON source, the {@link #stepsize} also has to be provided.
   * In this case, the missing {@link Node}s for the system time calculation (counter and multiply) are auto-generated.
   * 
   * TODO: assert mandatory JSON properties
   * 
   * @param file path to JSON representation of CAMeL-View IR
   * @return true, if parsing succeeded
   */
  @SuppressWarnings("unchecked")
  public boolean parse(String file) {
    if (file == null) return false;
    
    // read DSC in JSON format
    JSONObject json;
    try {
      json = (JSONObject) new JSONParser().parse(new FileReader(file));
    } catch (IOException | ParseException e) {
      e.printStackTrace();
      return false;
    }
    
    // remember all nodes and missing dependencies for (nearly) single pass parsing
    LinkedHashMap<Integer, ANode>    nodeList    = new LinkedHashMap<Integer, ANode>();
    LinkedHashMap<ANode, List<Long>> missingEdge = new LinkedHashMap<ANode, List<Long>>();
    
    // system time => handled as MUL(cycle_counter, stepsize)
    if (json.containsKey("time")) {
      JSONObject conf = (JSONObject) json.get("time");
      time = new ANode.Operation(((Long)   conf.get("id")).intValue(), 
                                  (String) conf.get("tag"), 
                                           OP.MUL);
      nodeList.put(time.getID(), time);
    }
    
    // special stepsize constant
    if (json.containsKey("time")) {
      JSONObject conf = (JSONObject) json.get("stepsize");
      stepsize = getConstant(((Number) conf.get("val")).doubleValue(),
                              (String) conf.get("tag"),
                       () -> ((Long)   conf.get("id")).intValue());
      nodeList.put(stepsize.getID(), stepsize);
    }
    
    // all other top-level entries in optimal/natural order (!do not trust json.foreach!)
    for (String key : Arrays.asList(
        "toplevel_inputs", 
        "toplevel_parameters", 
        "constants", 
        "states", 
        "predecessors",
        "operators",
        "toplevel_outputs")) {
      
      for (JSONObject conf : (List<JSONObject>) json.get(key)) {
        ANode node;
        
        // every node has a unique ID, and may have a tag
        int    id  = ((Long)   conf.get("id")).intValue();
        String tag =  (String) conf.get("tag");
        
        // sensor or host input
        if (key.equals("toplevel_inputs")) {
          if (conf.containsKey("saddr")) {
            node = new ANode.SensorInput(id, tag, ((Long) conf.get("saddr")).intValue());
          } else {
            node = new ANode.HostInput(id, tag);
          }
          
        // sporadic update
        } else if (key.equals("toplevel_parameters")) {
          node = new ANode.Parameter(id, tag, (Number) conf.get("init"));
          
        // fixed at runtime (!can not fold constants here as the later nodes reference to a specific constant ID!)
        } else if (key.equals("constants")) {
          ANode.Constant c = new ANode.Constant(id, tag, (Number) conf.get("val"));
          constants.put(c.getDoubleValue(), c);
          node = c;
        
        //  (integrated) value from last cycle
        } else if (key.equals("states") || key.equals("predecessors")) {
          Predecessor pred = new ANode.Predecessor(id, tag, conf.containsKey("init") ? (Number) conf.get("init") : 0);
          if (key.equals("predecessors")) pred.setIntegratorResolved();
          node = pred;
          
          Long driv = (Long) conf.get("driv");
          ANode d = nodeList.get(driv.intValue());
          if (d == null) {
            missingEdge.put(node, Arrays.asList(driv));
          } else {
            addBackedge(d, pred);
          }
          
        // real calculation
        } else if (key.equals("operators")) {
          String opName   = (String)     conf.get("op");
          List<Long> args = (List<Long>) conf.get("args");
          
          if (opName.equals("IF")) opName = "MUX"; // to better match semantic of that operator
          Operator op = UltraSynth.Instance.getOperatorByName(opName);
          if (op == null)     throw new IllegalArgumentException("unkown op in node " + id);
          node = new ANode.Operation(id, tag, UltraSynth.Instance.getOperatorByName(opName));
          
          for (int i=0; i<args.size(); i++) {
            ANode d = nodeList.get(args.get(i).intValue());
            if (d != null) {
              args.set(i, null);
              if (op == OP.MUX) { // first arg of MUX is its control signal modeled as control dependency
                if (i == 0) {
                  setControlDependency(d, node); 
                  node.setController(d, null);
                } else {
                  setDataDependency(d, node, i-1);
                }
              } else {
                setDataDependency(d, node, i);
              }
            } else {
              missingEdge.put(node, args);
            }
          }
        
        // actuator, host and/or log output 
        } else if (key.equals("toplevel_outputs")) {
          ANode.Output out = new ANode.Output(id, tag);
          node = out;
          if (conf.containsKey("raddr")) out.setResultAddress  (((Long) conf.get("raddr")).intValue());
          if (conf.containsKey("aaddr")) out.setActuatorAddress(((Long) conf.get("aaddr")).intValue());
          if (conf.containsKey("laddr")) out.setLogAddress     (((Long) conf.get("laddr")).intValue());
          
          Long driv = (Long) conf.get("driv");
          ANode d = nodeList.get(driv.intValue());
          if (d == null) {
            missingEdge.put(node, Arrays.asList(driv));
          } else {
            setDataDependency(d, node, 0);
          }
          
          
        // invalid JSON
        } else throw new IllegalArgumentException("Unsupported node class: " + key);
        
        
        // optional Precision specification
        JSONObject pconf = (JSONObject) conf.get("prec");
        if (pconf != null) {
          // TODO: get string instead of Number to better control rounding
          Double min = pconf.containsKey("min") ? ((Number) pconf.get("min")).doubleValue() : null;
          Double max = pconf.containsKey("max") ? ((Number) pconf.get("max")).doubleValue() : null;
          node.setRange(Range.generate(min == null ? null : BigDecimal.valueOf(min), 
                                       max == null ? null : BigDecimal.valueOf(max)));
          if (pconf.containsKey("res")); // TODO: handle precision
        }
        
        // optional array ID
        Long aid = ((Long) conf.get("arrayindex"));
        if (aid != null) node.setArrayID(aid.intValue());
        
        // remember node id for later reference
        nodeList.put(node.getID(), node);
      }
    }
    
    
    // add missing edges (for topologically sorted JSON, only states and predecessors should cause missing edges)
    missingEdge.forEach((node, dependencies) -> {
      for (int i=0; i<dependencies.size(); i++) {
        Long id = dependencies.get(i);
        if (id == null) continue;    // edge was already added in first pass
        ANode d = nodeList.get(id.intValue());
        if (d == null) throw new IllegalArgumentException("missing node "+id+" = input "+i+" of node "+node.getID());
        
        // predecessors are connected via back edge
        if (node instanceof ANode.Predecessor) {
          addBackedge(d, (ANode.Predecessor) node);
        
        // operators are connected via data edge
        } else {
          if (node.getOperation() == OP.MUX) {// first arg of MUX is its control signal modeled as control dependency
            if (i == 0) {
              setControlDependency(d, node); 
              node.setController(d, null);
            } else {
              setDataDependency(d, node, i-1);
            }
          } else {
            setDataDependency(d, node, i);
          }
        }
      }
    });
    
    // add nodes and dependencies for incrementing time steps
    if (time != null) {
      if (stepsize == null) throw new IllegalArgumentException("time specified without stepsize");
      
      JSONObject conf = (JSONObject) json.get("time");
      double startTime = conf.containsKey("starttime") ? ((Number) conf.get("starttime")).doubleValue() : 0;
      
      ANode.Constant    one = getConstant          (1,        "time.counter");
      ANode.Operation   add = new ANode.Operation  (autoID--, "time.counter", OP.ADD);
      ANode.Predecessor cnt = new ANode.Predecessor(autoID--, "time.counter", startTime / stepsize.getDoubleValue());
      cnt.setIntegratorResolved();
      
      addBackedge(      add,      cnt    );
      setDataDependency(cnt,      add,  0);
      setDataDependency(one,      add,  1);
      setDataDependency(cnt,      time, 0);
      setDataDependency(stepsize, time, 1);
    }
    
    return true;
  }
  
  @Override
  public IDP clone() {
    IDP copy;
    
    // create new empty instance of appropriate class
    try {
      copy = getClass().getConstructor().newInstance();
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
    
    // generate new nodes
    LinkedHashMap<ANode, ANode> map = new LinkedHashMap<ANode, ANode>();
    for (ANode n : iterate()) map.put(n, n.clone());
    
    // generate new data and control edges
    for (ANode n : iterate()) {
      ANode      c = map.get(n);
      Node[] dpred = n.getPredecessors();
      for (int i=0; i<dpred.length; i++) copy.setDataDependency(map.get(dpred[i]), c, i);

      LinkedHashSet<Node> cpred = controlPredecessors.get(n);
      if (cpred != null) for (Node p : cpred) copy.setControlDependency(map.get(p), c);
      
      if (n.getController() != null) c.setController(map.get(n.getController()), n.getDecision());
    }
    
    // copy lists and maps
    for (ANode  n : backedges.keySet()) copy.backedges.put((ANode.Predecessor) map.get(n), map.get(backedges.get(n)));
    for (Double d : constants.keySet()) copy.constants.put(d,             (ANode.Constant) map.get(constants.get(d)));
    for (ANode  n : schedule)           copy.schedule .add(map.get(n));
    
    // copy additional properties
    copy.autoID   = autoID;
    if (time     != null) copy.time     = (ANode.Operation) map.get(time);
    if (stepsize != null) copy.stepsize = (ANode.Constant)  map.get(stepsize);

    return copy;
  }
  
/*
 * Basic Graph manipulation
 **********************************************************************************************************************/
  
  @Override
  public void setDataDependency(Node from, Node to, int pos) {
    super.setDataDependency(from, to, pos);
    setTransitiveDependency(from, to);
  }
  
  @Override
  public void setControlDependency(Node from, Node to) {
    super.setControlDependency(from, to);
    setTransitiveDependency(from, to);
  }
  
  /**
   * Remove a data edge from this graph.
   * @param from the starting point of the edge
   * @param to   the end point {@link Node} of the edge
   * @param pos  the end point port of the edge
   */
  public void removeDataDependency(Node from, Node to, int pos) {
    // sanity check: is this a legal modification
    if (to.getPredecessor(pos) != from) {
      throw new IllegalArgumentException("can not remove data dependency from " + from + " to " + to + "[" + pos + "]");
    }
    // clear specific data predecessor
    to.setPredecessor(pos, null);

    // we are done, if there is another parallel from => to data connection at another port
    for (Node p : to.getPredecessors()) if (p == from) return;
    
    // so we have disconnected the data connection => tell this to from
    remove(dataSuccessors, from, to);
    
    // if there is no from => to control connection left, the nodes are disconnected completely
    LinkedHashSet<Node> s = controlSuccessors.get(from); 
    if (s == null || !s.contains(to)) removeDependency(from, to);
  }
  
  /**
   * Remove a control edge from this graph.
   * @param from the starting point of the edge
   * @param to   the end point node of the edge
   */
  public void removeControlDependency(Node from, Node to) {
   // sanity check: is this a legal modification
    LinkedHashSet<Node> s = controlPredecessors.get(to);
    if (s == null || !s.contains(from)) {
      throw new IllegalArgumentException("can not remove control dependency from " + from + " to " + to);
    }
    
    // clear control connections
    remove(controlSuccessors, from, to);
    remove(controlPredecessors, to, from);
    
    // if there is no from => to data connection left, the nodes are disconnected completely
    s = dataSuccessors.get(from); 
    if (s == null || !s.contains(to)) removeDependency(from, to);
  }
  /**
   * Remove a {@code Node} from this graph.
   * All edges of the {@link Node} are removed first to update the dependencies properly.
   * TODO: Implement more efficient by updating all dependencies at once
   * @param n the {@link Node} to be removed
   */
  public void removeNode(Node n) {
    LinkedHashSet<Node> t;
    
    // remove incoming data edges
    Node[] pred = n.getPredecessors();
    for (int i=0; i<pred.length; i++) removeDataDependency(pred[i], n, i);
    
    // remove outgoing data edges
    t = dataSuccessors.get(n);
    if (t != null) for (Node s : new LinkedHashSet<Node>(t)) {
      pred = s.getPredecessors();
      for (int i=0; i<pred.length; i++) if (pred[i] == n) removeDataDependency(n, s, i);
    }
    
    // remove incoming control edges
    t = controlPredecessors.get(n);
    if (t != null) for (Node p : new LinkedHashSet<Node>(t)) removeControlDependency(p, n);
    
    // remove outgoing control edges
    t = controlSuccessors.get(n);
    if (t != null) for (Node s : new LinkedHashSet<Node>(t)) removeControlDependency(n, s);
    
    nodes.remove(n);
    
    // IDP specific update of specific node reference  
    if (n == time) time = null;
    if (n == stepsize) {
      constants.remove(stepsize.getDoubleValue());
      stepsize = null;
    }
    backedges.remove(n); // LOAD of backedge removed. TODO: is there any valid situation were only STORE is removed?
  }
  
  /**
   * Replace a {@code Node} by another {@code Node}.
   * Relinks successors of old {@link Node} to new {@link Node} and removes the old one from this graph.
   * @param a to be replaced
   * @param b replacement
   */
  public void replaceNode(Node a, Node b) {

    // IDP specific update of specific node reference
    if (a == time)     time     = (ANode.Operation) b;
    if (a == stepsize) stepsize = (ANode.Constant)  b;
    
    // copy successors from a to b
    LinkedHashSet<Node> s = dataSuccessors.get(a);
    if (s != null) for (Node n : new LinkedList<Node>(s)) {
      Node[] pred = n.getPredecessors();
      for (int i=0; i<pred.length; i++) if (pred[i] == a) {
        removeDataDependency(a, n, i);
           setDataDependency(b, n, i);
      }
    }
    s = controlSuccessors.get(a);
    if (s != null) for (Node n : new LinkedList<Node>(s)) {
      removeControlDependency(a, n);
         setControlDependency(b, n);
      if (n.getController() == a) n.setController(b, n.getDecision());
    }
    
    removeNode(a);
  }
  
  /**
   * Update transitive dependencies after connecting two {@code Node}s
   * @param from starting point of new edge
   * @param to   end      point of new edge
   */
  protected void setTransitiveDependency(Node from, Node to) {
    allTransitivePredecessors = null;
    allTransitiveSuccessors   = null;
  }
 
  
  /**
   * Update data and control dependencies after disconnecting two {@code Node}s.
   * This method modifies {@link #allSuccessors} and {@link #allPredecessors}. The data and control specific sets are 
   * not modified.
   * 
   * @param from the starting point of the edge
   * @param to   the end      point of the edge
   */
  protected void removeDependency(Node from, Node to) {
    allTransitivePredecessors = null;
    allTransitiveSuccessors   = null;
    remove(allSuccessors, from, to);
    remove(allPredecessors, to, from);
  }
  
  /**
   * Remove {@code Node} from mapped set of another {@code Node}.
   * If the removed {@link Node} was the last of the mapped set, the set is also removed from the map. 
   * @param map 
   * @param key
   * @param val
   * @return    true, if the mapped set was modified
   */
  protected static boolean remove(LinkedHashMap<Node,LinkedHashSet<Node>> map, Node key, Node val) {
    LinkedHashSet<Node> s = map.get(key);
    if (s == null) return false;
    s.remove(val);
    if (s.isEmpty()) map.remove(key);
    return true;
  }
  
  /**
   * Add a {@link OP.STORE} between an operator producing a value at the end of an application cycle to be reloaded at
   * the beginning of the next application cycle. 
   * @param value  the value producing {@link Node}
   * @param load   the value reloading {@link Node}
   */
  protected void addBackedge(ANode value, ANode.Predecessor load) {
    ANode store = new ANode.Operation(autoID--, "backedge", OP.STORE);
    store.connect(load);
    setDataDependency(value, store, 0);
    backedges.put(load, store);
  }
  
  /**
   * Ensure that all controllers of multiplexers generate a status signal.
   * Simple comparisons are replaced with their status producing equivalents. 
   * All other conditions are complemented by a comparison with zero.
   * @return true, if the graph was modified.
   */
  public boolean prepareMuxDecision() {
    boolean modified = false;
    for (ANode mux : getNodes(OP.MUX)) {
      ANode cond = ((ANode) mux.getController());
      switch ((OP) cond.getOperation()) {
        case EQ  : cond.setOperator(OP.IFEQ); break;
        case NE  : cond.setOperator(OP.IFNE); break;
        case LT  : cond.setOperator(OP.IFLT); break;
        case GT  : cond.setOperator(OP.IFGT); break;
        case LE  : cond.setOperator(OP.IFLE); break;
        case GE  : cond.setOperator(OP.IFGE); break;
        default : 
          // replace condition by comparison with zero
          ANode zero = getConstant(0, null);
          ANode ne   = new ANode.Operation(autoID--, "decision", OP.IFNE);
          removeControlDependency(cond, mux);
          setControlDependency(ne, mux);
          mux.setController(ne, null);
          setDataDependency(cond, ne, 0);
          setDataDependency(zero, ne, 1);
          break;
      }
      modified = true;
    }
    return modified;
  }
  
  
  /**
   * Replace multiplexer by predicated stores.
   * <p>
   * For each multiplexer {@code MUX(C,A,B)}, two conditional STORE {@link ANode}s ({@code if (C) t=A; else t=B;}) are
   * inserted. The multiplexer is transformed into a LOAD {@link Node} reading the stored variable {@code t}.
   * <p>
   * TODO: use short circuit evaluation, where applicable
   * @return true, if graph was modified
   */
  public boolean muxToPredication() {
    boolean modified = false;
    for (ANode mux : getNodes(OP.MUX)) {
      ANode select = (ANode) mux.getController();
      
      for (int i=0; i<2; i++) {
        ANode store = new ANode.Operation(autoID--, Boolean.toString(i==0), OP.STORE);
        store.connect(mux);
        setDataDependency(mux.getPredecessor(i), store, 0);
        setControlDependency(select, store);
        setControlDependency(store,  mux);
        store.setController(select, i==0);
        removeDataDependency(mux.getPredecessor(i), mux, i);
      }
      
      mux.predecessors = new ANode[0];
      mux.setController(null, null);
      removeControlDependency(select, mux);
      mux.setOperator(OP.LOAD);
      modified = true;
    }
    return modified;
  }
  
/*
 * Graph analysis
 **********************************************************************************************************************/

  /**
   * Iterate through this graphs {@code Node}s.
   * This method helps to avoid casting from {@link Node} to {@link ANode} in many loops without copying all 
   * {@link #nodes} to a new data structure.
   * @return {@link ANode} iterator
   */
  protected Iterable<ANode> iterate() {
    return new Iterable<ANode>() {
      public Iterator<ANode> iterator() {
        Iterator<Node> i = nodes.iterator();
        return new Iterator<ANode>() {
          public boolean hasNext() {return         i.hasNext();}
          public ANode next()      {return (ANode) i.next();}
        };
      }
    };
  }
  
  /**
   * Select all output {@code Node}s.
   * @return list of all {@link ANode.Output}s from this graph
   */
  @SuppressWarnings("unchecked")
  protected <T extends ANode> LinkedList<T> getNodes(Class<T> clazz) {
    LinkedList<T> res = new LinkedList<T>();
    for (ANode n : iterate()) if (clazz.isInstance(n)) res.add((T) n);
    return res;
  }
  
  /**
   * Select {@code Node}s by operation.
   * @param op the operation to be selected
   * @return   list of all {@link ANode}s from this graph executing the specified operation
   */
  protected LinkedList<ANode> getNodes(Operator op) {
    LinkedList<ANode> res = new LinkedList<ANode>();
    for (ANode n : iterate()) if (n.getOperation() == op) res.add(n);
    return res;
  }
  
  /**
   * Select {@code Node} by ID.
   * @param id the ID to be selected
   * @return   the requested {@link Node} of null
   */
  protected ANode getNode(int id) {
    for (ANode n : iterate()) if (n.getID() == id) return n;
    return null;
  }
  
  /**
   * Select existing {@code Constant} by its value or generate a new one.
   * @param val the value of the constant to be selected or generated
   * @param tag the debug symbol to be attached to a new {@link Node}
   * @param id  generator for unique identifier
   * @return    {@link ANode.Input.Constant}
   */
  protected ANode.Constant getConstant(double val, String tag, Supplier<Integer> id) {
    ANode.Constant n = constants.get(val);
    if (n == null) {
      n = new ANode.Constant(id.get(), tag, val);
      n.setRange(Range.generate(val));
      constants.put(val, n);
    } else {
      n.setNametag("folded");
    }
    return n;
  }

  /**
   * Select existing {@link ANode.Input.Constant} by its value or generate a new one.
   * @param val the value of the {@link Type#constant} to be selected or generated
   * @param tag the debug symbol to be attached to a new {@link Node}
   * @return    {@link Type#constant} {@link Node}
   */
  protected ANode.Constant getConstant(double val, String tag) {
    return getConstant(val, tag, () -> autoID--);
  }
  
  /**
   * Determine transitive (data and control) predecessors.
   */
  protected void findAllTransitivePredecessors() {
    if (allTransitivePredecessors != null) return;
    allTransitivePredecessors = new LinkedHashMap<Node, LinkedHashSet<Node>>();
    for (Node n : getNodes()) findTransitive(n, allTransitivePredecessors, allPredecessors);
  }
  
  /**
   * Determine transitive (data and control) successors.
   */
  protected void findAllTransitiveSuccessors() {
    if (allTransitiveSuccessors != null) return;
    allTransitiveSuccessors = new LinkedHashMap<Node, LinkedHashSet<Node>>();
    for (Node n : getNodes()) findTransitive(n, allTransitiveSuccessors, allSuccessors);
  }
  
  /**
   * Find transitive closure of dependencies between {@code Node}s
   * @param n      the {@link Node} to find the transitive neighbors for
   * @param map    the already found mapping
   * @param direct the single-hop dependency to be expanded transitively
   * @return       the transitive neighbors of {@code n}
   */
  protected static LinkedHashSet<Node> findTransitive(Node n, LinkedHashMap<Node, LinkedHashSet<Node>> map, LinkedHashMap<Node, LinkedHashSet<Node>> direct) {
    if (map.containsKey(n)) return map.get(n);
    LinkedHashSet<Node> t = direct.get(n);
    if (t == null) return null; // avoid empty Sets in the map => testing (s==null) should be faster then (set.isEmpty())
    
    LinkedHashSet<Node> s = new LinkedHashSet<Node>();
    map.put(n, s);
    
    for (Node p : t) {
      s.add(p);
      LinkedHashSet<Node> a = findTransitive(p, map, direct);
      if (a != null) s.addAll(map.get(p));
    }
    return s;
  }
    
  /**
   * Find all {@code Node}s, whose values influence at least one of the outputs.
   * <b>Attention:</b> there seems to be a bug in transitive dependencies causing this method to produce wrong results
   * @return list of observable {@link ANode}s
   */
  protected LinkedHashSet<Node> getObservableNodes() {
    findAllTransitivePredecessors();
    LinkedHashSet<Node> list  = new LinkedHashSet<Node>();
    LinkedList<ANode> outputs = new LinkedList<ANode>(getNodes(ANode.Output.class));
    while (!outputs.isEmpty()) {
      Node n = outputs.pop();
      list.add(n);
      LinkedHashSet<Node> tpred = allTransitivePredecessors.get(n);
      if (tpred != null) for (Node p : tpred) {
        if (list.contains(p)) continue;
        list.add(p);
        
        // continue search at STORE node for reached back edge
        ANode be = backedges.get(p);
        if (be != null) outputs.push(be);
      }
    }
    return list;
  }
  
  
  /**
   * Partition graph according to loop characteristics.
   * The following partitions are returned, even if they are empty:
   * <ol>
   *   <li> pre loop:    between any input/parameter and any loop
   *   <li> inter loop:  between two loops
   *   <li> post loop:   between any loop and any output
   *   <li> feedthrough: between any input/parameter and any output without intermediate loops
   *   <li> independent loops (each loop in its own category, i.e. getLoopPartitioning().size() - 4 = number of loops)
   * </ol>
   * @return
   */
  protected List<LinkedHashSet<Node>> getLoopPartitioning() {
    findAllTransitivePredecessors();
    findAllTransitiveSuccessors();
    
    LinkedList<LinkedHashSet<Node>>   res         = new LinkedList<LinkedHashSet<Node>>();
    LinkedHashSet<Node>               preLoop     = new LinkedHashSet<Node>(); 
    LinkedHashSet<Node>               interLoop   = new LinkedHashSet<Node>(); 
    LinkedHashSet<Node>               postLoop    = new LinkedHashSet<Node>(); 
    LinkedHashSet<Node>               feedthrough = new LinkedHashSet<Node>(); 
    LinkedHashSet<Node>               insideLoop  = new LinkedHashSet<Node>();

    // every backedge STORE that depends on its LOAD is a direct loop
    for (ANode load : backedges.keySet()) {
      LinkedHashSet<Node> succ = allTransitiveSuccessors.get(load);
      if (succ == null) continue;
      
      ANode store = backedges.get(load);
      if (!succ.contains(store)) continue;
      
      LinkedHashSet<Node> pred = allTransitivePredecessors.get(store);
      if (pred == null) throw new RuntimeException("transitive successors and predecessors are not mirrored"); 
 
      // each node reachable from LOAD and STORE is on the loop
      LinkedHashSet<Node> loop = new LinkedHashSet<Node>();
      loop.add(load);
      loop.add(store);
      for (Node s : succ) if (pred.contains(s)) loop.add(s);
      res.add(loop);
      insideLoop.addAll(loop);
    }
    
    // no loop found => all nodes are feedthrough
    if (insideLoop.isEmpty()) {
      for (Node n : iterate()) feedthrough.add(n);
    
    } else {
      
      // merge overlapping loops
      if (res.size() > 1) {
        LinkedList<LinkedHashSet<Node>> mergedLoops = new LinkedList<LinkedHashSet<Node>>();
        for (LinkedHashSet<Node> loop : res) {
          boolean merged = false;
          for (LinkedHashSet<Node> mLoop : mergedLoops) if (!Collections.disjoint(loop, mLoop)) {
            merged = true;
            mLoop.addAll(loop);
            break;
          }
          if (!merged) mergedLoops.add(loop);
        }
        res = mergedLoops;
      }
      
      // classify nodes outside loops
      for (Node n : iterate()) {
        if (insideLoop.contains(n)) continue;
        
        LinkedHashSet<Node> pred = allTransitivePredecessors.get(n);
        LinkedHashSet<Node> succ = allTransitiveSuccessors  .get(n);
        
        boolean loopBefore = pred != null && !Collections.disjoint(pred, insideLoop);
        boolean loopAfter  = succ != null && !Collections.disjoint(succ,  insideLoop);
        
        if (loopBefore) {
          if (loopAfter) interLoop  .add(n);
          else           postLoop   .add(n);
        } else {
          if (loopAfter) preLoop    .add(n);
          else           feedthrough.add(n);
        }
      }
    }
    
//    print("preLoop",     preLoop);
//    print("interLoop",   interLoop);
//    print("postLoop",    postLoop);
//    print("feedthrough", feedthrough);
    
    res.add(0, preLoop);
    res.add(1, interLoop);
    res.add(2, postLoop);
    res.add(3, feedthrough);
    
    return res;
  }
  
  /**
   * Perform some sanity checks and report errors.
   * <ul>
   *   <li>duplicated IDs
   *   <li>duplicated peripheral addresses
   *   <li>unconnected input ports
   *   <li>unused {@link Node}s (i.e., result not observable at any output)
   * </ul>
   * @return number of failed sanity checks
   */
  protected int checkSanity() {
    int errors = 0;
    LinkedHashSet<Node> observable = getObservableNodes();
    for (ANode n : iterate()) {
      Node[] pred = n.getPredecessors();
      for (int i=0; i<pred.length; i++) {
        if (pred[i] == null) {
          System.out.println("  Input " + i + " of " + n + " not connected");
          errors++;
        }
      }
      for (ANode o : iterate()) if (o != n) {
        if (o.getID() == n.getID()) {
          System.out.println("  Duplicated ID " + n.getID());
          errors++;
        }
//        if (o.getType() == n.getType() && o.getAddress() == n.getAddress() && 
//           (n.getType() == Type.input || n.getType() == Type.parameter || n.getType() == Type.output)) {
//          System.out.println("  Duplicated peripheral address at " + n + " and " + o);
//          errors++;
//        }
      }
      if (!observable.contains(n)) {
        System.out.println("  Output of " + n + " is not observable by any output");
        errors++;
      }
      
    }
    if (errors > 0) System.out.println("  " + errors + " sanity checks failed");
    return errors;
  }
  
  

/*
 * Graph optimization
 **********************************************************************************************************************/

  /**
   * Reduce number of operations that have to be implemented explicitly. 
   * <p>
   * The following substitutions are supported:
   * <ul>
   *   <li> SQR(A)   = MUL(A,A)
   *   <li> SGN(A)   = CMP(A,0)
   *   <li> ABS(A)   = MUX(LT(A,0),NEG(A),A)
   *   <li> SQRT(A)  = POW(A,0.5)
   *   <li> EXP(A)   = POW(2.71,A)
   *   <li> MIN(A,B) = MUX(LT(A,B),A,B)     
   *   <li> MAX(A,B) = MUX(LT(A,B),B,A)     
   * <ul>
   * <b>Attention:</b> check, whether all replacement operations are already part of the graph.
   * @param op operation to be eliminated
   * @return true, if graph was modified
   */
  public boolean aliasOperation(Operator op) {
    boolean modified = false;
    for (ANode n : getNodes(op)) {
      Node a = n.getPredecessor(0);
      switch ((OP) op) {
        case SQR  : 
          n.setOperator(OP.MUL);
          n.predecessors = new Node[] {a,a};
          break;
          
        case SGN  : 
          n.setOperator(OP.CMP);
          n.predecessors = new Node[] {a,null};
          ANode c = getConstant(0, n.getNametag());
          setDataDependency(c, n, 1);
          break;
          
        case ABS  :
          n.setOperator(OP.MUX);
          n.predecessors = new Node[] {null, null};
          ANode lt  = new ANode.Operation(autoID--, n.getNametag(), OP.LT);
                c   = getConstant(0,     n.getNametag());
          ANode neg = new ANode.Operation(autoID--, n.getNametag(), OP.NEG);
          setDataDependency(a,   neg, 0);
          setDataDependency(neg, n,   0);
          setDataDependency(a,   n,   1);
          setDataDependency(a,   lt,  0);
          setDataDependency(c,   lt,  1);
          setControlDependency(lt, n);
          n.setController(lt, null);
          break;
          
        case SQRT :
          n.setOperator(OP.POW);
          n.predecessors = new Node[] {a,null};
          c = getConstant(0.5, n.getNametag());
          setDataDependency(c, n, 1);
          break;
          
        case EXP  :
          n.setOperator(OP.POW);
          n.predecessors = new Node[] {null,a};
          c = getConstant(Math.E, n.getNametag());
          setDataDependency(c, n, 0);
          break;

        case MIN  :
        case MAX  : 
          Node b = n.getPredecessor(1);
          n.setOperator(OP.MUX);
          n.predecessors = new Node[] {null, null};
          lt = new ANode.Operation(autoID--, n.getNametag(), OP.LT);
          setDataDependency(a, n,  op == OP.MAX ? 1 : 0);
          setDataDependency(b, n,  op == OP.MAX ? 0 : 1);
          setDataDependency(a, lt, 0);
          setDataDependency(b, lt, 1);
          setControlDependency(lt, n);
          n.setController(lt, null);
          break;
          
        default : return false;
      }
      modified = true;
    }
    return modified;
  }
  
  public static List<Operator> getSupportedAliasOperators() {
    return Arrays.asList(OP.SQR, OP.SGN, OP.ABS, OP.SQRT, OP.EXP, OP.MIN, OP.MAX);
  }
  
  
  /**
   * Common subexpression elimination.
   * <p>
   * Finds and folds {@link Node}s with equal values. Commutative equalities are also detected. The debug symbols of
   * folded {@link Node}s are discarded. The tree is visited recursively from the outputs along all predecessors. 
   * Disconnected parts (dead code) will be removed completely. However, this method does not necessarily find all dead
   * code. An additional {@link #deadCodeElimination} is recommended.
   * <p>
   * <b>Attention:</b>Multilevel cascades of associative equalities (e.g., ADD[ADD[A,B],C] = ADD[A,ADD[B,C]) are not 
   * detected yet.
   * 
   * @return true, if graph was modified
   */
  public boolean commonSubexpressionElimination(boolean keepOutputs) {
    int numNodes = nodes.size();
    
    // prepare map to store already visited nodes and provide a fast access to equivalent operators
    LinkedHashMap<Operator,LinkedHashSet<ANode>> visited = new LinkedHashMap<Operator,LinkedHashSet<ANode>>();
    
    // start search bottom up
    for (ANode.Output n : getNodes(ANode.Output.class)) cseVisit(n, visited);
    
    // also visit time calculation, to avoid its removal as dead code
    // TODO: is this required? if time is not part of any computation, it should be removed
    //cseVisit(time, visited);

    // now dead code (all not visited nodes) can be removed
    LinkedList<Node> dead = new LinkedList<Node>();
    for (Node n : iterate()) {
      LinkedHashSet<ANode> s = visited.get(n.getOperation());
      if (s == null || !s.contains(n)) dead.add(n);
    }
    for (Node n : dead) removeNode(n); // delete in second pass to avoid concurrent modification exception
    
    return numNodes != nodes.size();
  }
  
  /**
   * Recursive part of {@link #commonSubexpressionElimination}.
   * Bottom-Up search for {@link Node}s to be folded with other {@link Node}s representing the same expression.
   * @param  n           the {@link Node} to visit
   * @param  visited     the {@link Node}s already visited
   */
  protected void cseVisit(ANode n, LinkedHashMap<Operator,LinkedHashSet<ANode>> visited) {
    
    // noting to do, if already visited
    OP op = (OP) n.getOperation();
    LinkedHashSet<ANode> opNodes = visited.get(op);
    if (opNodes == null) {
      opNodes = new LinkedHashSet<ANode>();
      visited.put(op, opNodes);
    } else if (opNodes.contains(n)) return;
    
    // mark node as visited to avoid endless loops
    opNodes.add(n);
    
    // visit all predecessors and backedges
    LinkedHashSet<Node> pred = new LinkedHashSet<Node>(getAllPredecessors(n));
    Node be = backedges.get(n);
    if (be != null) pred.add(be);
    for (Node p : pred) cseVisit((ANode) p, visited);
    
    // if there is another already visited node representing the same value => fold nodes 
    for (ANode v : opNodes) {
      if (v != n && n.equalValue(v)) {
//        System.out.println("cse for " + n + " => " + v);
        replaceNode(n, v);
        v.setNametag("folded");
        opNodes.remove(n);
        return;
      }
    }
  }
  
  /**
   * Evaluate arithmetic expressions not depending on runtime variables.
   * Besides evaluating all operations only depending on constants, the following arithmetic simplifications are 
   * applied:
   * <ul>
   *   <li> {@code  ADD(A,0)   =     A}
   *   <li> {@code  ADD(0,B)   =     B}
   *   <li> {@code   OR(A,0)   =     A}
   *   <li> {@code   OR(0,B)   =     B}
   *   <li> {@code  XOR(A,0)   =     A}
   *   <li> {@code  XOR(0,B)   =     B}
   *   <li> {@code  AND(A,0)   =     0}
   *   <li> {@code  AND(0,B)   =     0}
   *   <li> {@code  MUL(A,0)   =     0}
   *   <li> {@code  MUL(0,B)   =     0}
   *   <li> {@code  MUL(A,1)   =     A}
   *   <li> {@code  MUL(1,B)   =     B}
   *   <li> {@code  MUL(A,-1)  = NEG(A)}
   *   <li> {@code  MUL(-1,B)  = NEG(B)}
   *   <li> {@code  DIV(A,1)   =     A}
   *   <li> {@code  DIV(A,-1)  = NEG(A)}
   *   <li> {@code  MOD(A,0)   =     0}
   *   <li> {@code  REM(A,0)   =     0}
   *   <li> {@code  POW(A,0)   =     1}
   *   <li> {@code  SUB(A,A)   =     0}
   *   <li> {@code  SUB(A,0)   =     A}
   *   <li> {@code  SUB(0,B)   = NEG(B)}
   *   <li> {@code  SHL(A,0)   =     A}
   *   <li> {@code  SHR(A,0)   =     A}
   *   <li> {@code  ROL(A,0)   =     A}
   *   <li> {@code  ROR(A,0)   =     A}
   *   <li> {@code  SHL(0,B)   =     0}
   *   <li> {@code  SHR(0,B)   =     0}
   *   <li> {@code  ROL(0,B)   =     0}
   *   <li> {@code  ROR(0,B)   =     0}
   *   <li> {@code  MUX(0,A,B) =     B}
   *   <li> {@code  MUX(1,A,B) =     A}
   * </ul>
   * TODO: how about DIV(0,B), REM(0,B), MOD(0,B) ?
   * @return true, if the graph was modified
   */
  public boolean constantPropagation() {
    boolean modified = false;
    
    // list of all constant nodes not yet analyzed
    LinkedList<ANode> constNodes = new LinkedList<ANode>(constants.values());
    
    // analyze all operands below any constant
    while (!constNodes.isEmpty()) {
      ANode c = constNodes.pop();
      LinkedHashSet<Node> succ = allSuccessors.get(c);
      if (succ != null) for (Node op : new LinkedList<Node>(succ)) if (op instanceof ANode.Operation) {
        Implementation imp = ((ANode.Operation) op).getImplementation();
        Node[]        pred = op.getPredecessors();
        ANode            r = null;
        Double        rVal = null;
        
        switch (pred.length) {
          case 1 :
            // unary operator with constant input => evaluate
            ANode a = (ANode) pred[0];
            if (a instanceof ANode.Constant && imp instanceof Unary) {
              rVal = ((Unary) imp).apply(((ANode.Constant) a).getDoubleValue()).doubleValue();
            }
            break;
          
          case 2 :
                      a = (ANode) pred[0];
            ANode     b = (ANode) pred[1];
            Double aVal = (a instanceof ANode.Constant) ? ((ANode.Constant) a).getDoubleValue() : Double.NaN;
            Double bVal = (b instanceof ANode.Constant) ? ((ANode.Constant) b).getDoubleValue() : Double.NaN;
            
            
            // binary operator with constant inputs => evaluate
            if (!aVal.isNaN() && !bVal.isNaN() && imp instanceof Binary) {
              rVal = ((Binary) imp).apply(aVal, bVal).doubleValue();
              break;
            }
            
            // arithmetic simplifications for specific inputs
            switch ((OP) op.getOperation()) {
              case ADD :
              case OR  : 
              case XOR : r = (aVal == 0.0) ? b : (bVal == 0.0) ? a : null;
                         break;
              case MUL : if (aVal == 1.0) r = b;
                    else if (bVal == 1.0) r = a;
                    else if (aVal == -1.0 || bVal == -1.0) {
                      r = new ANode.Operation(autoID--, "cprop", OP.NEG);
                      setDataDependency(aVal == -1.0 ? b : a, r, 0);
                   }
                   // no break here as the next statement also holds for MUL
              case AND : if  (aVal == 0.0 || bVal == 0.0) rVal = 0.0;
                         break;
              case DIV : if (bVal ==  1.0) r = a;
                    else if (bVal == -1.0) {
                      r = new ANode.Operation(autoID--, "cprop", OP.NEG);
                      setDataDependency(a, r, 0);
                    }
                         break;
              case MOD :
              case REM : if  (bVal == 1.0)  rVal = 0.0;
                         break;
              case POW : if  (bVal == 0.0)  rVal = 1.0;
                         break;
              case SHL :
              case SHR :
              case ROL :
              case ROR :      if (aVal == 0.0) rVal = 0.0;
                         else if (bVal == 0.0) r    = a;
                         break;
              case SUB :      if (a == b)      rVal = 0.0;
                         else if (bVal == 0.0) r    = a;
                         else if (aVal == 0.0) {
                           r = new ANode.Operation(autoID--, "cprop", OP.NEG);
                           setDataDependency(b, r, 0);
                         }
                         break;
              case MUX : ANode d = (ANode) op.getController();
                         if (d instanceof ANode.Constant) r = ((ANode.Constant) d).getDoubleValue() == 0 ? b : a;
                         break;
              default:
            }
          
          default:
        }
        
        if (rVal != null) r = getConstant(rVal, "cprop");
        
        if (r != null) {
//          System.out.println("  replace " + op + " by " + r);
          replaceNode(op, r);
          if (r instanceof ANode.Constant) constNodes.push(r); // replacement may have new constant predecessors
          modified = true;
        }
      }
    }
    return modified;
  }
  
  /**
   * Dead code elimination.
   * Remove all {@link Node}s not observable at any {@link Type#output}.
   * @return true, if graph was modified
   */
  public boolean deadCodeElimination() {
    LinkedHashSet<Node> deadNodes  = new LinkedHashSet<Node>();
    LinkedHashSet<Node> observable = getObservableNodes();
    for (Node n : iterate()) if (!observable.contains(n)) deadNodes.add(n);
    for (Node n : deadNodes) removeNode(n); // delete in second pass to avoid concurrent modification exception
    return !deadNodes.isEmpty();
  }
  
  
  
/*
 * Integrator expansion
 **********************************************************************************************************************/

   
  /**
   * Change the value of the {@link #stepsize} {@code Node} or create a new one.
   * @param val the new stepsize
   * @return true, if graph was modified
   */
  public boolean setStepsize(double val) {
    if (stepsize == null) {
      // build new stepsize node
      stepsize = getConstant(val, "h");
      nodes.add(stepsize);
    } else {
      if (stepsize.getDoubleValue() == val) return false;
      if (time != null) {
        // rescale initial counter value
        ANode.Predecessor cnt = (ANode.Predecessor) time.getPredecessor(0);
        cnt.setInitValue(Math.round((cnt.getInitValue() * stepsize.getDoubleValue()) / val));
      }
      stepsize.setValue(val);
    }
    return true;
  }
  
  /**
   * First order integrator expansion.
   * Update all internal states x as: 
   * <pre>
   *   x(t+h) = x(t) + h * x'(t, x(t))
   * </pre>
   * where {@code h} = stepsize, {@code x(t)) = current state, and {@code x'(t, x(t))} = first temporal derivative of 
   * current state. The latter is expected to be the data predecessor of backedge (i.e., STORE {@link Node}) associated
   * with the {@link Type.state} {@link Node}.
   * <p> 
   * The {@link Type#state}s become {@link Type#predecessor}s after the integrator expansion.
   */
  public void expandEuler() {
    if (stepsize == null) throw new RuntimeException("set stepsize before expanding integrator");
    
    // update all states
    for (ANode.Predecessor state : getNodes(ANode.Predecessor.class)) {
      if (state.isIntegratorResolved()) continue; 
      
      // cut connection from derivative to store
      Node store      = backedges.get(state);
      Node derivative = store.getPredecessor(0);
      removeDataDependency(derivative, store, 0);
      
      // generate two new nodes for state_new = state_old + derivative * stepsize
      Node mul = new ANode.Operation(autoID--, "euler " + state.getID(), OP.MUL);
      Node add = new ANode.Operation(autoID--, "euler " + state.getID(), OP.ADD);
      
      // connect new nodes with state, store, derivative, and stepsize
      setDataDependency(derivative, mul, 0); 
      setDataDependency(stepsize,   mul, 1);
      setDataDependency(mul,        add, 0);
      setDataDependency(state,      add, 1);
      setDataDependency(add,      store, 0);
      
      // now state is just the predecessor loading the stored value of the adder
      state.setIntegratorResolved();
    }
  }
  
  /**
   * Second order integrator expansion.
   * Update all internal states x as: 
   * <pre>
   *   xe     = x(t) + h   *  x'(t, x(t))
   *   x(t+h) = x(t) + h/2 * (x'(t, x(t)) + x'(t+h, xe)}.
   * </pre> 
   * where {@code h} = stepsize, {@code x(t)) = current state, {@code x'(t, x(t))} = first temporal derivative of 
   * current state, {@code xe} = new state that euler would have taken, and {@code x'(t+h, xe)} the sub step computation
   * of the first temporal derivative at the hypothetical euler point. {@code x'(t, x(t))} is expected to be the data 
   * predecessor of the backedge (i.e., STORE {@link Node}) associated with the {@link Type.state} {@link Node}.
   * <p>
   * The iXtronics software implementation of the Heun integrator first calculates {@code xe} for all states and than
   * recomputes the first temporal derivates from all states. The substeps can thus not be searched isolated for each 
   * state. Furthermore, the value of the predecessors for the sub steps must be taken from the end of the main step 
   * (i.e., their drivers).
   * <p>
   * The {@link Type#state}s become {@link Type#predecessor}s after the integrator expansion.
   */
  public void expandHeun() {
    if (stepsize == null) throw new RuntimeException("set stepsize before expanding integrator");
    
    // constant for h/2
    Node half_stepsize = getConstant(stepsize.getDoubleValue()/2, "h/2");
    
    // mapping of original nodes to their sub step equivalents (copies or identities)
    LinkedHashMap<Node, Node> subStep = new LinkedHashMap<Node, Node>();
    
    // find sub steps, i.e., copy any operator depending on any state or time and relevant for any derivative
    LinkedHashSet<ANode.Predecessor> states = new LinkedHashSet<ANode.Predecessor>();
    for (ANode.Predecessor state : getNodes(ANode.Predecessor.class)) {
      if (state.isIntegratorResolved()) continue; 
      states.add(state);
      substepHeun((ANode) backedges.get(state).getPredecessor(0), subStep);
    }
    
    // connect derivatives of main and sub steps to derive new system states
    for (ANode.Predecessor state : states) {
      
      // cut connection from derivative to store
      Node store      = backedges.get(state);
      Node derivative = store.getPredecessor(0);
      removeDataDependency(derivative, store, 0);
      
      // generate new nodes state_new = state_old + (mainstep_derivative + substep_derivative) * half_stepsize
      Node add  = new ANode.Operation(autoID--, "heun " + state.getID(), OP.ADD);
      Node mul  = new ANode.Operation(autoID--, "heun " + state.getID(), OP.MUL);
      Node xnew = new ANode.Operation(autoID--, "heun " + state.getID(), OP.ADD);
      
      // connect new nodes with state, store, derivative, and half_stepsize
      setDataDependency(derivative,              add, 0);
      setDataDependency(subStep.get(derivative), add, 1);
      setDataDependency(add,                     mul, 0);
      setDataDependency(half_stepsize,           mul, 1);
      setDataDependency(mul,                    xnew, 0);
      setDataDependency(state,                  xnew, 1);
      setDataDependency(xnew,                  store, 0);
      
      // now state is just the predecessor loading the stored value of the xnew adder
      state.setIntegratorResolved();;
    }
  }

  /**
   * Find the sub step copies of a {@code Node} and all its transitive predecessors.
   * After calling this function, {@code n} and all its (transitive) predecessors are ensured to be mapped to 
   * themselves (if its sub step value is the same as its main step value) or to a new sub step {@link Node} already 
   * linked to the rest of the graph.
   *  
   * @param n      the root {@link ANode} for the transitive predecessor search
   * @param copies the list of already mapped sub step {@link Node}s
   */
  protected void substepHeun(ANode n, LinkedHashMap<Node, Node> copies) {
    
    // nothing to be done, if node was already visited
    if (copies.containsKey(n)) return;
    
    // just copy a link to the main step for nodes expected to be equal for main and sub step
    if (n instanceof ANode.HostInput || n instanceof ANode.SensorInput || n instanceof ANode.Parameter || n instanceof ANode.Constant) {
      copies.put(n, n); 
      
    } else if (n instanceof ANode.Predecessor) {
      // value of predecessors in sub step equals its value in next main step = end of current main step
      if (((ANode.Predecessor)n).isIntegratorResolved()) {
        copies.put(n, (ANode) backedges.get(n).getPredecessor(0));
        
      // link initial state of sub step to its hypothetical euler point
      } else {
        Node derivative = backedges.get(n).getPredecessor(0);
        
        // generate new nodes for xe = state_old +  derivative * stepsize
        Node mul = new ANode.Operation(autoID--, "heuler " + n.getID(), OP.MUL); // heuler = hypothetical euler (as part of heun)
        Node add = new ANode.Operation(autoID--, "heuler " + n.getID(), OP.ADD);
        
        // connect new nodes with state, derivative, and stepsize
        setDataDependency(derivative, mul, 0); 
        setDataDependency(stepsize,   mul, 1);
        setDataDependency(mul,        add, 0);
        setDataDependency(n,          add, 1);
        
        // remember sub state as entry point for sub step
        copies.put(n, add);
        
      }
    } else if (n instanceof ANode.Operation) {
      // TODO: special case for time operator possible: use t+h (as in software) instead of (tcnt+1)*h
      // the latter version is automatically generated, as tcnt is the predecessor of (tcnt+1) and h is a constant
      
      // copy of operator for main step needed, if any of its data or control predecessors differs between main and 
      // sub step
      boolean copyNeeded = false;
      for (Node p : allPredecessors.get(n)) {
        substepHeun((ANode) p, copies);
        if (p != copies.get(p)) copyNeeded = true; 
      }
      ANode ctrl = (ANode) n.getController(); // just in case someone configured a controller without data dependency
      if (ctrl != null) {                     // TODO: can be removed?
        substepHeun(ctrl, copies);
        if (ctrl != copies.get(ctrl)) copyNeeded = true;
      }

      // copy and link node
      if (copyNeeded) {
        Node[] dpred = n.getPredecessors();
        ANode cop = n.clone();
        cop.setID(autoID--); 
        cop.setNametag("sub " + n.getID());
        for (int i=0; i<dpred.length; i++) setDataDependency(copies.get((ANode) dpred[i]), cop, i);
        if (ctrl != null) cop.setController(copies.get(ctrl), n.getDecision());
        LinkedHashSet<Node> cpred = controlPredecessors.get(n);
        if (cpred != null) for (Node p : cpred) setControlDependency(copies.get(p), cop);
        copies.put(n, cop);
        
        // ensure that LOADs keep connected to STOREs
        if (cop.getOperation() == OP.LOAD && cpred != null) {
          cop.connect(cop.getID()); // cloned regfile value has to be updated 
          for (Node p : cpred) ((ANode) copies.get(p)).connect(cop);
        }
      
      // just copy reference
      } else {
        copies.put(n, n);
      }
      
      // outputs should not be reached by sub step search
    } else if (n instanceof ANode.Output) {
      throw new RuntimeException("no derivative should depend on output " + n.getID());
    }
  }
  
/*
 * Bitwidth optimization
 **********************************************************************************************************************/

  
  public void bitwidthAnalysis() {
    List<ANode.Output> outputs =                       getNodes(ANode.Output.class);
    List<ANode>        inputs  = new LinkedList<ANode>(getNodes(ANode.HostInput.class));
    inputs.addAll(                                     getNodes(ANode.Parameter.class));
    
    // Static Analysis of range and precision: worst-case propagation from inputs to outputs and vica-versa 
    for (ANode n : inputs)  staticRangeForwardAnalysis(n);
    for (ANode n : outputs) staticRangeBackwardAnalysis(n);
    for (ANode n : inputs)  staticPrecisionForwardAnalysis(n);
    for (ANode n : outputs) staticPrecisionBackwardAnalysis(n);
  }
  
  protected void staticRangeForwardAnalysis(ANode n) {
    
  }
  
  protected void staticRangeBackwardAnalysis(ANode n) {
    
  }
  
  protected void staticPrecisionForwardAnalysis(ANode n) {
    
  }
  
  protected void staticPrecisionBackwardAnalysis(ANode n) {
    
  }

/*
 * Simulated graph execution (not tested yet)
 **********************************************************************************************************************/
  
  public void scheduleSimulation() {
    schedule.clear();
    for (ANode.Output n : getNodes(ANode.Output.class)) schedule(n);
  }
  
  protected void schedule(ANode n) {
    if (n instanceof ANode.Constant    || 
        n instanceof ANode.Parameter   || 
        n instanceof ANode.HostInput   || 
        n instanceof ANode.SensorInput || // TODO: is this correct?
        schedule.contains(n)) return;

    if (getAllPredecessors(n) != null) for (Node p : getAllPredecessors(n)) schedule((ANode) p);
    schedule.add(n);
  }
  
  public void resetSimulation() {
    for (ANode n : iterate()) n.resetSimulationValue();
  }
  
  /**
   * TODO: add flag to activate quantization by reduced bitwidth
   */
  public void stepSimulation() {
    for (ANode n : schedule) {
      switch ((OP) n.getOperation()) {
        case STORE : 
          n.setSimulationValue(((ANode) n.getPredecessor(0)).getSimulationValue()); 
          break;
        
        case LOAD :
          // search suitable and activated STORE
          for (Node p : controlPredecessors.get(n)) {
            if (p.getValue() != n.getValue()) continue;
            ANode c = (ANode) p.getController();
            if (c == null || (c.getSimulationValue() != 0.0) == p.getDecision()) {
              n.setSimulationValue(((ANode) p).getSimulationValue());
              break;
            }
          }
          break;
        
        case MUX : 
          int sel = ((ANode) n.getController()).getSimulationValue() == 0 ? 1 : 0;
          n.setSimulationValue(((ANode) n.getPredecessor(sel)).getSimulationValue());
          break;
          
        default:
          if (n instanceof ANode.Operation) {
            Implementation imp = ((ANode.Operation) n).getImplementation();
            if (imp instanceof Unary) {
              double a = ((ANode) n.getPredecessor(0)).getSimulationValue();
              n.setSimulationValue(((Unary) imp).apply(a).doubleValue());
            } else {
              double a = ((ANode) n.getPredecessor(0)).getSimulationValue();
              double b = ((ANode) n.getPredecessor(1)).getSimulationValue();
              n.setSimulationValue(((Binary) imp).apply(a,b).doubleValue());
            }
          }
          break;
      }
    }
  }
  
/*
 * Dumps
 **********************************************************************************************************************/

  /**
   * Generate a description of relevant statistical measures of this graph
   * @return
   */
  public String getStatistics() {
    
    int dataEdges = 0;
    int controlEdges = 0;
    LinkedHashMap<String,Integer>    nodeTypes  = new LinkedHashMap<String, Integer>();
    LinkedHashMap<Operator, Integer> operations = new LinkedHashMap<Operator,  Integer>();
    List<LinkedHashSet<Node>>        partitions = getLoopPartitioning();
    
    for (ANode n : iterate()) {
      String   t = n.getClass().getSimpleName();
      Operator o = n.getOperation();
      
      nodeTypes .put(t, nodeTypes .containsKey(t) ? nodeTypes .get(t)+1 : 1);
      operations.put(o, operations.containsKey(o) ? operations.get(o)+1 : 1);
      dataEdges += n.getPredecessors().length;
      if (controlPredecessors.containsKey(n)) controlEdges += controlPredecessors.get(n).size();
    }
    
    StringBuilder res = new StringBuilder();
    int c = 15;
    res.append(String.format("%-"+c+"s : %d\n", "nodes", nodes.size()));
    nodeTypes.forEach((type, count) -> res.append(String.format("  %-"+(c-2)+"s : %d\n", type, count)));
    LinkedList<Operator> ops = new LinkedList<Operator>(operations.keySet());
    Collections.sort(ops, Operator.COMPARATOR);
    res.append(String.format("%-"+c+"s : %d\n", "operator types", ops.size()));
    for (Operator o : ops) {
      res.append(String.format("  %-"+(c-2)+"s : %d\n", o.toString(), operations.get(o)));
    }
    res.append(String.format("%-"+c+"s : %d\n", "edges", controlEdges + dataEdges));
    res.append(String.format("  %-"+(c-2)+"s : %d\n", "data",    dataEdges));
    res.append(String.format("  %-"+(c-2)+"s : %d\n", "control", controlEdges));
    res.append(String.format("%-"+c+"s : %d\n", "loops", partitions.size()-4));
    for (int i=0; i<partitions.size(); i++) {
      String label;
      switch (i) {
        case 0 : label = "pre";           break;
        case 1 : label = "inter";         break;
        case 2 : label = "post";          break;
        case 3 : label = "feedthrough";   break;
        default: label = "loop " + (i-3); break;
      }
      res.append(String.format("  %-"+(c-2)+"s : %d\n", label, partitions.get(i).size()));
    }
    
    return res.toString();
  }
  
  /**
   * {@link #ranking} handler used to place all sources set at the beginning of an application cycle on top, and all 
   * data sinks (i.e., outputs and backedges) computed at the end of an application cycle on the bottom of the graph.
   */
  public final Function<ANode, String> IO_RANKING = n -> {
    if (n instanceof ANode.HostInput   ||
        n instanceof ANode.SensorInput ||
        n instanceof ANode.Parameter   ||
        n instanceof ANode.Predecessor) return "min";
    if (n instanceof ANode.Output      ||
        backedges.containsValue(n))     return "max";
    return null;
  };
  
  /**
   * Export the graph in dot-format.
   * 
   * @param nodes   subset of nodes to be exported 
   * @param ranking Handler assigning a ranking category to every {@code Node}
   * The ranking is used to group {@link Node}s at certain levels in the dot format. All {@link Node}s within the same
   * ranking category are placed on the same level (i.e., vertical position). The dot builtin categories 
   * <ul>
   *   <li> min/source - top of graph
   *   <li> max/sink   - bottom of graph
   * </ul>
   * should be used to determine the absolute position. The {@link Node}s of all other rank categories are placed within
   * one line, but dot is free to choose the appropriate absolute position. {@link Node}s assigned to null are not 
   * constrained for placement. See {@link #IO_RANKING}.
   */
  public String toDot(LinkedHashSet<Node> nodes, Function<ANode, String> ranking){
    StringBuilder res = new StringBuilder();
    res.append("digraph " + this.getClass().getSimpleName() + " {\n");
    
    LinkedHashMap<String,LinkedHashSet<Node>> ranks = new LinkedHashMap<String,LinkedHashSet<Node>>();
    
    // plot nodes and group ranks
    for (Node n : nodes) {
      res.append("\"" + n + "\" " + ((ANode) n).toDot() + "\n");
      
      String rank = ranking == null ? null : ranking.apply((ANode) n);
      if (rank != null) {
        LinkedHashSet<Node> s = ranks.get(rank);
        if (s == null) {
          s = new LinkedHashSet<Node>();
          ranks.put(rank, s);
        }
        s.add(n);
      }
    }
    
    // plot edges
    for (Node n : nodes) {
      
      // data edges
      Node[] preds = n.getPredecessors();
      for(int i=0; i<preds.length; i++){
          res.append("\"" + preds[i]+ "\" -> \"" + n + "\"");
          if (preds.length > 1) res.append(" [headlabel="+i+", labeldistance=1.8]");
          res.append("\n");
      }
      
      // control edges and short circuit evaluation edges
      LinkedHashSet<Node> s = controlPredecessors.get(n);
      if (s != null) for(Node p : s) {
        Node    c = n.getController();
        Boolean d = n.getDecisionTristate();
        if (p != c) {
          res.append("\"" + p + "\" -> \"" + n + "\" [color=lightgray];\n");
        } else {
          String color = d == null ? "blue" : d ? "green" : "red"; 
          res.append("\"" + c + "\" -> \"" + n + "\" [color=" + color + ",style=dashed];\n");
          if (d == null) continue;
          Node   from = d ? c.getShortCircuitEvaluationTrueBranchController()
                          : c.getShortCircuitEvaluationFalseBranchController();
          if (from == null) continue;
          boolean sce = d ? c.getShortCircuitEvaluationTrueBranchControllerDecision() 
                          : c.getShortCircuitEvaluationFalseBranchControllerDecision();
          res.append("\"" + from + "\" -> \"" + n + "\" [color=" + (d == sce ? "green" : "red") + ",style=dashed];\n");
        }
      }
    }
    
    // realize ranking
    for (String k : ranks.keySet()) {
      String r = (k.matches("^(min|max|source|sink)$")) ? k : "same";
      res.append("{rank=" + r + ";");
      for (Node n : ranks.get(k)) res.append(" \"" + n + "\""); 
      res.append("}\n");
    }
    
    res.append("}");
    return res.toString();
  }
  
  /**
   * Export the subgraph surrounding a specific {@code Node} in dot-format.
   * @param n the {@link Node} in the center of the subgraph
   * @return
   */
  public String toDot(Node n) {
    LinkedHashSet<Node> t, s = new LinkedHashSet<Node>();
    
    findAllTransitivePredecessors(); t = allTransitivePredecessors.get(n); if (t != null) s.addAll(t);
    findAllTransitiveSuccessors();   t = allTransitiveSuccessors  .get(n); if (t != null) s.addAll(t);
    s.add(n);
    return toDot(s, null);
  }
  
  /**
   * Export the whole graph in dot format ranking all {@link Type#input}s, {@link Type#parameter}s, {@link Type#state}s
   * and {@link Type#predecessor}s at the top and all {@link Type#output}s and backedges at the bottom.
   */
  @Override
  public String toString() {
    return toDot(nodes, IO_RANKING);
  }
  
  /**
   * Dump dot representation and operator list into files.
   * @param file         the file to write to.
   * @throws IOException
   */
  protected void dump(String file) throws IOException {
    file += ".dot";
    System.out.println("dump " + nodes.size() + " nodes to "+ file);
    FileWriter fw = new FileWriter(file);
    fw.write(toString());
    fw.close();
  }
  
  /**
   * Plot a list {@code Node}s
   * @param label a descriptive prefix
   * @param set   the {@link Node}s to plot
   */
  protected static void print(String label, LinkedHashSet<Node> set) {
    System.out.print(label + ":"); 
    if (set != null) for (Node n : set) System.out.print(" " + n);
    System.out.println();
  }
  
  
/*
 * Performance Improvement by extensive dynamic programming
 **********************************************************************************************************************/

  /**
   * Extension of {@link IDP} trying to speed up graph analysis by updating transitive successor and predecessor
   * dependencies during generation and modification of the graph. This should be faster than rebuilding those sets 
   * from scratch with {@link IDP#findAllTransitivePredecessors()} and {@link IDP#findAllTransitiveSuccessors()} each 
   * time they are required.
   * <p>
   * <b>Attention:</b> There seems to be a bug somewhere in the update procedures. At least, resulting 
   *                   LinearDriveControl graphs differ from the {@link IDP} results.
   * <br/>
   * <b>Attention:</b> The extra effort during construction overcompensates the time savings during analysis.
   */
  public static class Transitive extends IDP {
    
    /**
     * Default constructor initializes transitive dependency maps
     */
    public Transitive() {
      allTransitivePredecessors = new LinkedHashMap<Node,LinkedHashSet<Node>>();
      allTransitiveSuccessors   = new LinkedHashMap<Node,LinkedHashSet<Node>>();
    }

    /**
     * Update (transitive) data and control dependencies after disconnecting two {@code Node}s.
     * This method modifies {@link #allSuccessors}, {@link #allPredecessors}, {@link #allTransitiveSuccessors} and 
     * {@link #allTransitivePredecessors}. The data and control specific sets are not modified.
     * 
     * @param from the starting point of the edge
     * @param to   the end      point of the edge
     */
    @Override
    protected void removeDependency(Node from, Node to) {
      remove(allSuccessors, from, to);
      remove(allPredecessors, to, from);
      
      // rebuild transitive predecessors of to by accumulating remaining connections
      LinkedHashSet<Node> old, s,t,a;
      old = allTransitivePredecessors.get(to);
      if (old != null) {
        s = new LinkedHashSet<Node>();
        t = allPredecessors.get(to);
        if (t != null) for (Node n : t) {
          s.add(n);
          a = allTransitivePredecessors.get(n);
          if (a != null) s.addAll(a); 
        }
        if (s.isEmpty()) {
          allTransitivePredecessors.remove(to);
        } else {
          allTransitivePredecessors.put(to, s);
          old.removeAll(s);
        }
        // all nodes in old are no longer predecessors of to => they have to be informed
        for (Node n : old) {
          s = allTransitiveSuccessors.get(n);
          if (s != null) s.remove(to);
        }
      }
      
      // rebuild transitive successors of from by accumulating remaining connections
      old = allTransitiveSuccessors.get(from);
      if (old != null) {
        s = new LinkedHashSet<Node>();
        t = allSuccessors.get(from);
        if (t != null) for (Node n : t) {
          s.add(n);
          a = allTransitiveSuccessors.get(n);
          if (a != null) s.addAll(a); 
        }
        if (s.isEmpty()) {
          allTransitiveSuccessors.remove(from);
        } else {
          allTransitiveSuccessors.put(from, s);
          old.removeAll(s);
        }
        // all nodes in old are no longer successors of from => they have to be informed
        for (Node n : old) {
          s = allTransitivePredecessors.get(n);
          if (s != null) s.remove(from);
        }
      }
    }
    
    @Override
    protected void setTransitiveDependency(Node from, Node to) {
      LinkedHashSet<Node> s, t;
      
      // update transitive successors of from
      s = allTransitiveSuccessors.get(from);
      if (s == null){
        s = new LinkedHashSet<Node>();
        allTransitiveSuccessors.put(from, s);
      }
      s.add(to);
      t = allTransitiveSuccessors.get(to);
      if (t != null) s.addAll(t); 
      
      // update transitive predecessors of to
      s = allTransitivePredecessors.get(to);
      if (s == null){
        s = new LinkedHashSet<Node>();
        allTransitivePredecessors.put(to, s);
      }
      s.add(from);
      t = allTransitivePredecessors.get(from);
      if (t != null) s.addAll(t);
      
      // update transitive successors of all transitive predecessors of from
      s = allTransitiveSuccessors.get(from);
      t = allTransitivePredecessors.get(from);
      if (t != null) for (Node n : t) allTransitiveSuccessors.get(n).addAll(s);
      
      // update transitive predecessors of all transitive successors of to
      s = allTransitivePredecessors.get(to);
      t = allTransitiveSuccessors.get(to);
      if (t != null) for (Node n : t) allTransitivePredecessors.get(n).addAll(s);
    }
    
    @Override
    protected void findAllTransitivePredecessors() {}
    
    @Override
    protected void findAllTransitiveSuccessors() {}
    
  }

}

/*
 * Copyright (c) 2016,
 * Embedded Systems and Applications Group,
 * Department of Computer Science,
 * TU Darmstadt,
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 *    disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the institute nor the names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE INSTITUTE AND CONTRIBUTORS ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE INSTITUTE OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 **********************************************************************************************************************/
