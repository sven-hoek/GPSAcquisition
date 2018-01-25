package graph;

import accuracy.Range;
import operator.Implementation;
import operator.Operator;
import target.UltraSynth.OP;

/**
 * Accuracy extension of {@link Node}.
 * <p>
 * Besides the {@link #range} and precision of possible {@link Node} values, {@link ANode}s are {@link type}d to 
 * reflect the different categories of the CAMeL-View IR. 
 * <p>
 * Reuse of protected fields from superclass:
 * <ul>
 *   <li>{@link Node#operation} => memory access, arithmetic, control decisions, constant
 *   <li>{@link Node#nametag}   => debug symbol (e.g., name of CAMeL-View module, this node initiated from)
 *   <li>{@link Node#address}   => address of peripheral modules (sensor/actuator) and host I/O or parameters 
 *   <li>{@link Node#value}     => value of {@link Type#constant}s or ID of variable to group load/store nodes
 * </ul>
 *
 * @author Andreas Engel [engel@esa.cs.tu-darmstadt.de]
 */
public abstract class ANode extends Node implements Cloneable {
  
  /**
   * Unique identifier.
   * Positive values for {@link ANode}s parsed from from JSON. 
   * Negative values for auto-generated {@link ANode}s inserted afterwards.
   */
  private int id = 0;
  
  /**
   * Unique identifier within a node class.
   * This group id specifies the location of this {@link ANode} within the its JSON group:
   * <ul>
   *   <li> toplevel_inputs
   *   <li> toplevel_parameters
   * </ul> 
   * For all other node types, the {@code #arrayID} is negative. 
   * 
   * This ID is used by the C-API to identify a certain node.
   */
  private int arrayID = -1;
  
  /**
   * Intermediate results during simulated execution of an {@code IDP}.
   */
  protected double simulationValue = Double.NaN;
  
  /**
   * Range of possible {@link Node#operation} results required for bit width optimization.
   */
  private Range range = null;
  
  /**
   * Generate an {@code ANode}.
   * @param id    unique identifier
   * @param tag   debug symbol
   */
  public ANode(int id, String tag, Operator op) {
	super(0, op, null, null);
	setNametag(tag);
    this.value           = id; // values are used as variable identifier to couple LOADs and STOREs
    this.id              = id;  
    this.range           = null;
    this.simulationValue = Double.NaN;
  }
  
  /**
   * Clone an {@code ANode}.
   * @param n the {@code ANode} to be cloned 
   */
  protected ANode(ANode n) {
    super(n.address, n.operation, null, null);
    this.nametag         = n.nametag;
    this.value           = n.value;
    this.value           = n.value; // values are used as variable identifier to couple LOADs and STOREs
    this.id              = n.id;
    this.arrayID         = n.arrayID;
    this.range           = n.range == null ? null : n.range.clone();
    this.simulationValue = n.simulationValue;
  }
    
  public int getID() {
    return id;
  }
  
  public void setID(int id) {
    this.id = id;
  }
  
  public int getArrayID() {
    return arrayID;
  }
  
  public void setArrayID(int id) {
    this.arrayID = id;
  }
  
  /**
   * Initial value to be loaded into the Regfile before the first execution.
   * To be overriden by initializable Node types.
   */
  public double getInitValue() {
    return Double.NaN;
  }
  
  public void connect(ANode n) {
    connect(n.value);
  }
  
  public void connect(Number val) {
    this.value = val;
  }
 
  public double getSimulationValue() {
    return simulationValue;
  }
  
  public void setSimulationValue(double val) {
    simulationValue = val;
  }
  
  public void resetSimulationValue() {
    simulationValue = getInitValue();
  }
  
  public Range getRange() {
    return range;
  }
  
  public void setRange(Range range) {
    this.range = range;
    range.tag(this);
  }
  
  public void setOperator(Operator op) {
    this.operation = op;
  }
  
  @Override
  public String toString() {
//    return getClass().getSimpleName() + "(" + id + ")"; 
    return address + ":"+operation + "(" + id + ")";
  }
  
  @Override
  public ANode clone() {
    try {
      return getClass().getConstructor(ANode.class).newInstance(this);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
  
  public boolean equalValue(ANode other) {
    return other == this; 
  }
  
  public String getDotShape() {
    return "box";
  }
  
  public abstract String getDotColor();
  
  public String getDotContent() {
    StringBuilder res = new StringBuilder();
    if (getRange() != null)                    res.append("<BR/>" + getRange());
    if (!Double.isNaN(getInitValue()))         res.append("<BR/>I: " + getInitValue());
    if (Double.isFinite(getSimulationValue())) res.append("<BR/>(" + getSimulationValue() + ")");
    return res.toString();
  }

  public String toDot() {
    StringBuilder res = new StringBuilder();
    
    res.append("[label=<<FONT POINT-SIZE=\"10\" FACE=\"verdena\">" + getArrayID() + "<BR/>");
    res.append(        "<B>" + getOperation() + "</B>");
    if (getOperation().isRegfileAccess()) res.append(" " + value);
    if (getNametag() != null) res.append("<BR/>" + getNametag());
    res.append(getDotContent());
    res.append("</FONT>>, shape="+getDotShape()+",style=filled,fillcolor="+getDotColor()+"]");
    
    return res.toString().replaceAll("Infinity", "&infin;");
  }
  
  
  public static class HostInput extends ANode {
    
    /**
     * Input from Host
     * @param id
     * @param tag
     */
    public HostInput(int id, String tag) {
      super(id, tag, OP.CONST);
      this.address = -1;
    }
    
    public HostInput(ANode n) {
      super(n);
    }

    @Override
    public int getSensorAddress() {
      return address;
    }
    
    @Override
    public String getDotColor() {
      return "red";
    }
  }
  
public static class SensorInput extends ANode {
    
    /**
     * Input from Sensor
     * @param id
     * @param tag
     * @param address
     */
    public SensorInput(int id, String tag, int address) {
      super(id, tag, OP.NOP);
      this.address = address;
    }
    
    public SensorInput(ANode n) {
      super(n);
    }

    @Override
    public int getSensorAddress() {
      return address;
    }
    
    @Override
    public String getDotColor() {
      return "pink";
    }
    
    @Override
    public String getDotContent() {
      StringBuilder res = new StringBuilder(super.getDotContent());
      if (isSensorInput()) res.append("<BR/>S: " + getSensorAddress());
      return res.toString();
    }
    
    @Override
    public boolean equalValue(ANode other) {
      if (!(other instanceof SensorInput)) return false;
      return getSensorAddress() == other.getSensorAddress();
    }
  }
  
  public static class Constant extends ANode {
    public Constant(int id, String tag, Number value) {
      super(id, tag, OP.CONST);
      setValue(value);
    }
    
    public Constant(ANode n) {
      super(n);
    }
    
    public double getDoubleValue() {
      return value.doubleValue();
    }
    
    public void setValue(Number value) {
      this.value = value;
      setRange(Range.generate(getDoubleValue()));
    }
    
    @Override
    public double getInitValue() {
      return getDoubleValue();
    }
    
    @Override
    public String getDotColor() {
      return "yellow";
    }
    
    @Override
    public boolean equalValue(ANode other) {
      if (!(other instanceof Constant)) return false;
      return value.equals(other.value);
    }
  }
  
  public static class Parameter extends ANode {
    public Parameter(int id, String tag, Number init) {
      super(id, tag, OP.CONST);
      this.value = init;
    }
    
    public Parameter(ANode n) {
      super(n);
    }
    
    @Override
    public double getInitValue() {
      return value.doubleValue();
    }
    
    @Override
    public String getDotColor() {
      return "orange";
    }
    
  }
  
  public static class Predecessor extends ANode {
    
    private Number init;
    
    public Predecessor(int id, String tag, Number init) {
      super(id, tag, OP.LOAD);
      setInitValue(init);
      this.address = 0;
    }
    
    public Predecessor(ANode n) {
      super(n);
      if (n instanceof Predecessor) this.init = ((Predecessor) n).init;
    }
    
    public boolean isIntegratorResolved() {
      return address == 1;
    }
    
    public void setIntegratorResolved() {
      address = 1;
    }
    
    public double getInitValue() {
      return init.doubleValue();
    }
    
    public void setInitValue(Number init) {
      this.init = init;
    }
    
    @Override
    public String getDotColor() {
      return isIntegratorResolved() ? "violet" : "blue";
    }
    
    /**
     * internal states and predecessors are supposed to be unique for now.
     * TODO: use init and driver to assert uniqueness
     */
    @Override
    public boolean equalValue(ANode other) {
      return super.equalValue(other); 
    }
  }
  
  public static class Operation extends ANode {
    private Implementation implementation;
    
    public Operation(int id, String tag, Operator op) {
      super(id, tag, op);
      this.implementation = op.createDefaultImplementation();
    }
    
    public Operation(ANode n) {
      super(n);
      if (n instanceof Operation) implementation = ((Operation) n).implementation.clone();
    }
    
    public Implementation getImplementation() {
      return implementation;
    }
    
    public void setOperator(Operator op) {
      super.setOperator(op);
      implementation = op.createDefaultImplementation();
    }
    
    public String getDotShape() {
      return "ellipse";
    }
    
    @Override
    public String getDotColor() {
      return "lightgray";
    }
    
    @Override
    public boolean equalValue(ANode other) {
      if (!(other instanceof Operator)) return false;
      
      // extra control input
      if (operation == OP.MUX) {
        if (getController()   == other.getController()    && 
            getPredecessor(0) == other.getPredecessor(0)  &&
            getPredecessor(1) == other.getPredecessor(1)) return true;
    
      // commutative operations
      } else if (operation == OP.ADD || 
                 operation == OP.MUL || 
                 operation == OP.EQ  || 
                 operation == OP.NE  || 
                 operation == OP.OR  || 
                 operation == OP.AND || 
                 operation == OP.XOR) {
        if ((getPredecessor(0) == other.getPredecessor(0)  &&
             getPredecessor(1) == other.getPredecessor(1)) ||
            (getPredecessor(0) == other.getPredecessor(1)  &&
             getPredecessor(1) == other.getPredecessor(0))) return true;
        
      // all other unary/binary operations (//TODO: what about RAND, maybe model RAND as input)
      } else if (getController()             == other.getController()    &&
                 getDecision()               == other.getDecision()      &&
                 getPredecessor(0)           == other.getPredecessor(0)  &&
                (getPredecessors().length     < 2                    ||
                 getPredecessor(1)           == other.getPredecessor(1))) return false;
      
      return super.equalValue(other); 
    }
  }
  
  public static class Output extends ANode {
    
    public Output(int id, String tag) {
      super(id, tag, OP.NOP);
    }
    
    public Output(ANode n) {
      super(n);
      setActuatorAddress(n.getActuatorAddress());
      setResultAddress  (n.getResultAddress());
      setLogAddress     (n.getLogAddress());
    }
    
    /**
     * Flag marking {@link #address} as actuator output address. 
     */
    private boolean isActuatorOutput = false;
    
    /**
     * Make this {@code Node} an actuator output.
     * @param addr the actuator BRAM address
     */
    public void setActuatorAddress(int addr) {
      address = addr;
      isActuatorOutput = addr >= 0;
    }
    
    @Override
    public int getActuatorAddress() {
      return isActuatorOutput ? address : -1;
    }
    
    /**
     * AXI address for a result output {@code Node}. 
     */
    private int resultOutputAddress = -1;
    
    /**
     * Make this {@code Node} a result output.
     * @param addr the result AXI address
     */
    public void setResultAddress(int addr) {
      resultOutputAddress = addr;
    }
    
    @Override
    public int getResultAddress() {
      return resultOutputAddress;
    }
    
    /**
     * AXI address for a log output {@code Node}. 
     */
    private int logOutputAddress = -1;
    
    /**
     * Make this {@code Node} a log output.
     * @param addr the log AXI address
     */
    public void setLogAddress(int addr) {
      logOutputAddress = addr;
    }
    
    @Override
    public int getLogAddress() {
      return logOutputAddress;
    }
    
    @Override
    public String getDotColor() {
      return "green";
    }
    
    @Override
    public String getDotContent() {
      StringBuilder res = new StringBuilder(super.getDotContent());
      if (isActuatorOutput()) res.append("<BR/>A: " + getActuatorAddress());
      if (isResultOutput())   res.append("<BR/>R: " + getResultAddress());
      if (isLogOutput())      res.append("<BR/>L: " + getLogAddress());
      return res.toString();
    }
    
    
    @Override
    public boolean equalValue(ANode other) {
      if (!(other instanceof Output))                                             return false;
      if (getPredecessor(0) != other.getPredecessor(0))                           return false;
      // enable these lines to keep multiple output nodes to writing the same value to different targets
      // (should all be folded into the same at the beginning)
      // TODO: keep these checks and only merge those outputs writing to different target types (not only addresses) 
//      if (isActuatorOutput && getActuatorAddress() != other.getActuatorAddress()) return false;
//      if (isResultOutput() && getResultAddress()   != other.getResultAddress())   return false;
//      if (isLogOutput()    && getLogAddress()      != other.getLogAddress())      return false;
      return true; 
    }
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
