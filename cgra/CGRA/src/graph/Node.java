package graph;



import java.util.ArrayList;
import java.util.List;

import operator.Operator;


/**
 * Represents a node in a DCFG
 * @author jung
 *
 */
public class Node  implements Comparable<Node> {

	/**
	 * Only needed to use JSONIO to import DCFGs
	 */
	@SuppressWarnings("unused")
	private Node(){}
	
	/**
	 * The address of the corresponding bytecode - needed to determine loop membership
	 */
	protected int address;
	
	/**
	 * The operation of the node
	 */
	protected Operator operation;

	/**
	 * Value of the node - needed for local variable identification + constants
	 */
	protected Number value = null;
	
	/**
	 * Stores all predecessors (data dependencies). All of them produce an operand for this node
	 */
	protected Node[] predecessors;
	
	/**
	 * The controlling node of this node
	 */
	private Node controller;
	
	/**
	 * The whether this node belongs to the true or false path of the controller
	 */
	private Boolean decision;
	
	/**
	 * Stores whether this Node describes an access to a static field - can only be true for load/store
	 */
	private boolean isIndirectConst = false; 
	
	/**
	 * Stores whether the true-branch of this if-Node is a part of short circuit evaluation
	 */
	private boolean shortCircuitEvaluationTrueBranch = false;
	
	/**
	 * Stores whether the false-branch of this if-Node is a part of short circuit evaluation
	 */
	private boolean shortCircuitEvaluationFalseBranch = false;
	

	private Node shortCircuitEvaluationTrueBranchController;
	
	private Node shortCircuitEvaluationFalseBranchController;
	
	private boolean shortCircuitEvaluationTrueBranchControllerDecision;
	
	private boolean shortCircuitEvaluationFalseBranchControllerDecision;
	
	/**
	 * Nametag for increased readability in dotfile
	 */
	protected String nametag = ""; // relevant only for dotfile. simplifies debugging
	
	/**
	 * from TaimoorCode - stores the id of the basic block, from which the creator instruction of this node belongs
	 * needed for loopcounter evaluation
	 */
	private int basicBlockId;
	
	/**
	 * from TaimoorCode - loop id to which the node belongs
	 * needed for loopcounter evaluation
	 */
	private Integer loopId;
	
	/**
	 * from TaimoorCode
	 * needed for loopcoutner evaluation
	 */
	private List<Node> loopCarriedDependency;
	
	/**
	 * Creates a new Node.
	 * @param address The address of the corresponding bytecode
	 * @param enum1 The operation of the Node
	 * @param value The value (for Local variable identification or constants)
	 * @param controller The controlling node
	 * @param decision Defines whether the node belongs to true or false path
	 */
	public Node(int address, Operator operation,  int value, Node controller, Boolean decision){
		this(address, operation, controller, decision);
		this.value = value;
	}
	
	/**
	 * Creates a new Node with long value .
	 * @param address The address of the corresponding bytecode
	 * @param operation The operation of the Node
	 * @param value The long value (only for 64 bit constants
	 * @param controller The controlling node
	 * @param decision Defines whether the node belongs to true or false path
	 */
	public Node(int address, Operator operation,  long value, Node controller, Boolean decision){
		this(address, operation,controller, decision);
		this.value = value;
	}
	
	/**
	 * Creates a new Node.
	 * @param address The address of the corresponding bytecode
	 * @param operation The operation of the Node
	 * @param controller The controlling node
	 * @param decision Defines whether the node belongs to true or false path
	 */
	public Node(int address, Operator operation,  Node controller, Boolean decision){
		this.address = address;
		this.operation = operation;
		predecessors = new Node[operation.getNumberOfOperands()];
		this.controller = controller;
		this.decision = decision;
		loopCarriedDependency = new ArrayList<Node>();
		loopId = null;
	}
	
	/**
	 * Return the address of the corresponding bytecode
	 * @return the address
	 */
	public int getAddress(){
		return address;
	}
	
	/**
	 * Returns the value of the node. Needed for local variable identification and constants
	 * @return
	 */
	public int getValue(){
		if (value == null) return -1;
		return value.intValue();
	}
	
	
	public void setValue(int value){
		this.value = value;
	}
	
	/**
	 * Returns the long value of the node. Needed only for 64 bit constants
	 * @return
	 */
	public long getValueLong(){
		if (value == null) return -1;
		return value.longValue();
	}
	
	/**
	 * Returns the operation of the node
	 * @return the operation
	 */
	public Operator getOperation(){
		return operation;
	}
	
	/**
	 * Adds a new predecessor (data dependency). Predecessors are the producers of operands
	 * @param pos Identification of the operand the predecessor produces
	 * @param predecessor the predecessor
	 */
	public void setPredecessor(int pos, Node predecessor){
		predecessors[pos] = predecessor;
	}
	
	/**
	 * Returns the predecessor that produces the operand identified by pos
	 * @param pos the operand identifier
	 * @return the predecessor node
	 */
	public Node getPredecessor(int pos){
		return predecessors[pos];
	}
	
	/**
	 * Returns all predecessors (data dependencies) of the node
	 * @return all predecessors
	 */
	public Node[] getPredecessors(){
		return predecessors;
	}
	
	/**
	 * Sets the controller and the path membership of the node
	 * @param controller The controlling node
	 * @param decision The path membership
	 */
	public void setController(Node controller, Boolean decision){
		this.controller = controller;
		this.decision = decision;
	}
	
	/**
	 * Adds a name tag to the node. This nametag is relevant for the generated dotfile and
	 * should increase its readability
	 * @param nametag the name tag
	 */
	public void setNametag(String nametag){
		this.nametag = nametag;
	}
	
	/**
	 * Returns the nametag of this Node.
	 * @return the Nametag
	 */
	public String getNametag(){
		return nametag;
	}
	
	/**
	 * Returns a string representation of the Node
	 */
	public String toString(){
		if(value == null)
			return (address) +":"+ operation+nametag;
		else
			return (address) + ":"+ operation + ":"+(value.intValue())+nametag;
	}

	/**
	 * Returns the controller node of this node
	 * @return The controller
	 */
	public Node getController() {
		return controller;
	}
	

	/**
	 * Returns the path membership of this node
	 * @return True or false path
	 */
	public boolean getDecision() {
		return decision != null && decision;
	}
	
	/**
	 * Returns the path membership of this node
	 * For the null path, the result of the node should be stored for all results of the {@link #controller}. 
	 * <p>
	 * TODO: This method was added to not modify the previous {@link Node} interface. As soon as all referrer of 
	 * {@link #getDecision} know about a possible null result, this method could be removed. 
	 * 
	 * @return true | false | null
	 */
	public Boolean getDecisionTristate() {
	  return decision;
	}
	
	/**
	 * Returns whether the Node describes access to a static field (STORE /LOAD only) 
	 * Only relevant for token generation as static fields are stored in Token Machine and not in LVM
	 * @return true if node describes access to a static field
	 */
	public boolean isIndirectConstant(){
		return isIndirectConst;
	}
	
	/**
	 * Defines whether the node describes an access to a static field
	 * @param isIndirect true if access to static field
	 */
	public void isIndirectConstant(boolean isIndirect){
		this.isIndirectConst = isIndirect;
	}

	/**
	 * Returns whether the true-branch of this if-Node which is part of short circuit evaluation
	 * @return true if is part of short circuit evaluation
	 */
	public boolean getShortCircuitEvaluationTrueBranch() {
		return shortCircuitEvaluationTrueBranch;
	}
	
	/**
	 * Returns whether the false-branch of this if-Node which is part of short circuit evaluation
	 * @return true if is part of short circuit evaluation
	 */
	public boolean getShortCircuitEvaluationFalseBranch() {
		return shortCircuitEvaluationFalseBranch;
	}

	/**
	 * Defines that the true-branch of this if-Node is part of short circuit evaluation
	 * @param sceController the opposite Node of SCE
	 * @param sceController the decision of the opposite Node
	 */
	public void setShortCircuitEvaluationTrueBranch(Node sceController, boolean sceControllerDecision) {
		shortCircuitEvaluationTrueBranch = true;
		shortCircuitEvaluationTrueBranchController = sceController;
		shortCircuitEvaluationTrueBranchControllerDecision = sceControllerDecision;
	}
	
	/**
	 * Defines that the false-branch of this if-Node is part of short circuit evaluation
	 * @param sceController the opposite Node of SCE
	 * @param sceController the decision of the opposite Node
	 */
	public void setShortCircuitEvaluationFalseBranch(Node sceController, boolean sceControllerDecision) {
		shortCircuitEvaluationFalseBranch = true;
		shortCircuitEvaluationFalseBranchController = sceController;
		shortCircuitEvaluationFalseBranchControllerDecision = sceControllerDecision;
	}

	public Node getShortCircuitEvaluationTrueBranchController() {
		return shortCircuitEvaluationTrueBranchController;
	}

	public Node getShortCircuitEvaluationFalseBranchController() {
		return shortCircuitEvaluationFalseBranchController;
	}

	public boolean getShortCircuitEvaluationTrueBranchControllerDecision() {
		return shortCircuitEvaluationTrueBranchControllerDecision;
	}

	public boolean getShortCircuitEvaluationFalseBranchControllerDecision() {
		return shortCircuitEvaluationFalseBranchControllerDecision;
	}
	
	public int compareTo(Node o) {
		
		return Integer.compare(address, o.address);
	}
	
	public void setBasicBlockId(int id)
	{
		basicBlockId = id;
	}
	
	public int getBasicBlockId()
	{
		return basicBlockId;
	}
	
	/**
	 * Compares two nodes
	 * @param n1	node 1
	 * @param n2    node 2
	 * @return		returns true if operation and preds are the same
	 */
	public static boolean compareNodes(Node n1, Node n2)
	{
		if(n1.getOperation() != n2.getOperation())
			return false;
		else
		{
			int count = 0;
			for(int i = 0; i < n1.predecessors.length; i++)
			{
				if(n1.predecessors[i] == n2.predecessors[i])
					count++;
			}
			if(count == n1.predecessors.length)
				return true;
			else
				return false;
		}
	}
	
	public void setLoopCarriedDependency(Node node)
	{
		if(!loopCarriedDependency.contains(node))
			loopCarriedDependency.add(node);
	}
	
	public void overwriteLoopCarriedDependency(List<Node> nodes){
		loopCarriedDependency=nodes;
	}
	
	public List<Node> getLoopCarriedDependency()
	{
		return loopCarriedDependency;
	}
	
	public void setLoopId(Integer loopId)
	{
		this.loopId = loopId;
	}
	
	public Integer getLoopId()
	{
		return loopId;
	}
	
	public void setOperation(Operator op){
		operation = op;
	}

	public void setPredecessors(Node[] predecessors) {
		this.predecessors = predecessors;
	}

	/**
	 * TODO
	 * @param nd
	 * @return
	 */
//	public boolean equals(Node nd){
//		return nd.address == this.address && nd.operation.equals(this.operation);
//	}


/*
 * Peripherals extension (required by UltraSynth, but integrated here as dummies to keep scheduler consistent)
 *  - Sensor   Input  (read    via BRAM,        once per cycle)
 *  - Actuator Output (written via BRAM,        once per cycle)
 *  - Result   Output (written via AXI to OCM,  once per cycle)
 *  - Log      Output (written via AXI to DDR,  once per PE per cycle)
 * A node may represent multiple outputs at once, but they have to be scheduled in the same cycle.
 **********************************************************************************************************************/

  /**
   * Get the access address of a sensor input {@code Node}.
   * @return the sensor BRAM address
   */
  public int getSensorAddress() {
    return -1;
  }
  
  /**
   * @return true, if this {@code Node} is a sensor input.
   */
  public boolean isSensorInput() {
    return getSensorAddress() >= 0;
  }
  
  /**
   * Get the access address of an actuator output {@code Node}.
   * @return the actuator BRAM address
   */
  public int getActuatorAddress() {
    return -1;
  }
  
  /**
   * @return true, if this {@code Node} is an actuator output.
   */
  public boolean isActuatorOutput() {
    return getActuatorAddress() >= 0;
  }
  
  /**
   * Get the access address of a result output {@code Node}.
   * @return the result AXI address
   */
  public int getResultAddress() {
    return -1;
  }
  
  /**
   * @return true, if this {@code Node} is a result output.
   */
  public boolean isResultOutput() {
    return getResultAddress() >= 0;
  }
  
  /**
   * Get the access address of a result output {@code Node}.
   * @return the log AXI address
   */
  public int getLogAddress() {
    return -1;
  }
  
  /**
   * @return true, if this {@code Node} is a log output.
   */
  public boolean isLogOutput() {
    return getLogAddress() >= 0;
  }
	
}
