package operator;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.json.simple.JSONObject;

import accuracy.BigNumber;
import accuracy.Format;
import cgramodel.CgraModel;
import cgramodel.PEModel;
import generator.Module;
import generator.Module.Port.Type;
import target.Processor;

/**
 * Module used to configure the concrete implementation of an {@code Operator} and generate the corresponding Verilog
 * code.
 * <p>
 * Use {@link Operator#createDefaultImplementation()} to generate an {@link Implementation} of an {@link Operator} and 
 * adjust the configuration as required (e.g., by {@link #configure(JSONObject)}). Besides some 
 * {@link Operator}-specific features, the typical parameters configurable for an {@link Implementation} comprise
 * <ul>
 *   <li> the numerical {@link Format} of the arithmetic operands and results.
 *   <li> the execution latency
 *   <li> the opcode (used at ALU level to enable active {@link Operator}s)
 *   <li> the estimated energy consumption (used only for simulation)
 * </ul>
 * In addition to these configurable properties, the {@link Implementation} subclasses can define the {@link Operator} 
 * to be
 * <ul>
 *   <li> native, if it is not required as Verilog implementation
 *   <li> memAccess, if it controls the access to the register file
 *   <li> controlFlow, if it influences the configuration counter by a branch decision
 * </ul> 
 * 
 * <h4>Arithmetic arity and precision</h4> 
 * Although the arity supported by the {@link CgraModel} is (yet) limited (i.e., 2->1 and 1->1), the 
 * {@link Implementation} does not restrict the {@link Operator} arity in general. However, all non-abstract subclasses
 * define their specific number of operands and results by appropriate constructors. If the I/O precision of a subclass
 * can not be configured during creation of the {@link Implementation}, it is assumed to not support dynamic I/O 
 * accuracy at all (as for all {@link target.Amidar.OP}s). Otherwise, the selected I/O precision is reflected by the 
 * module name.
 * 
 * <h4>Execution latency</h4>
 * Each configuration has a minimum latency, i.e., the minimum number of clock cycles required to load the operands, 
 * execute the {@link Operation} and output the results. The I/O transfer between the {@link Operator} and the register
 * file takes multiple cycles, if bit width of the I/O is larger than the processors data path width. This module thus 
 * consequently distinguishes between the bit width of an arithmetic I/O and the corresponding module port. The 
 * configured latency may differ from the minimum latency for simulation based design space exploration. However, an 
 * exception will be thrown when trying to generate the Verilog code for an unsupported latency configuration.
 * 
 * <h4>Native Operators</h4>
 * Native {@link Operator}s are only used for scheduling and simulation. They can not generate Verilog code and are 
 * neglected when assigning opcodes to all {@link Operator}s available within a {@link PEModel}.
 * 
 * <h4>Design space exploration</h4>
 * If the {@link Operator} supports dynamic accuracy, the {@link Format} of each operand is a DOF for the design space 
 * exploration. There are 6243 different {@link Format}s with up to 64 bit overall width. This results in 39e6 potential
 * configurations just for the I/O precision. Subclasses may add additional DOF such as the division algorithm of the 
 * {@link DivRem} module. Generating all configurations at once in a big list may exceed the systems memory capacity. 
 * Instead, an iterator concept is implemented to step through the design space. The {@code Iterator<Implementation>} 
 * interface would require to generate a new {@link Implementation} instance for each configuration and is also avoided.
 * Currently, only configurations with equal {@link Format}s for all operands and results are generated (i.e., only one
 * DOF for the I/O precision).<br>
 * TODO: generate all input combinations and derive suitable result precision with {@link #fitResultFormat}<br>
 * TODO: is the result {@code Format} a relevant DOF (more efficient operators instead of converting the format 
 * afterwards)?
 * 
 * <h4>Automatic testbench generation</h4>
 * To simplify the functional verification of the generated Verilog modules, testbenches can be generated automatically.
 * Therefore, a number of test runs is executed. In each run, operand values are generated randomly from within their
 * configured range. The {@link Implementation} subclasses just need to implement {@link #getTestOutput} to provide the
 * expected values to compare the results against. Besides the testbench module, appropriate Modelsim scripts can be
 * generated with {@link #getSimTCL} and {@link #getWaveTCL}.
 * <p>
 * @author Andreas Engel
 */
public abstract class Implementation implements Module, Cloneable, Serializable {

  /*
   * General global constants
   **********************************************************************************************************************/

  /**
   * Unique ID for (de)marshalling
   */
  private static final long serialVersionUID = -7169026658439132642L;

  /** Clock period in ns used for simulation. */
  protected static final double CLOCK_PERIOD = 10;
  
  /** Name of working library for Modelsim */
  protected static final String WORK_LIB = "work"; 

/*
 * global aux functions
 **********************************************************************************************************************/

  /**
   * Binary logarithm.
   * 
   * @param bits
   * @return {@code log_2(bits)}
   */
  public static int bitwidth(int bits) {
    return (int) Math.ceil((Math.log(bits) / Math.log(2)));
  }
  
  /**
   * Generate Verilog code for a bit range.
   * 
   * @param high the index of the most significant bit
   * @param low  the index of the least significant bit
   * @return     the bit slice expression for declaring a or accessing a bit vector
   */
  public static String bitRange(int high, int low) {
    return "[" + high + ":" + low + "]";
  }
  
  /**
   * Generate Verilog code for a bit range of a certain width
   * 
   * @param width the number of bits in the range
   * @return      the bit slice expression for declaring a or accessing a bit vector
   */
  public static String bitRange(int width) {
    return (width == 1) ? "" : bitRange(width-1, 0);
  }
  
  /**
   * Get number of cycles required to shift a bit vector through the data path of the {@code target.Processor}.
   * 
   * @param bits the width of the bit vector
   * @return     {@code ceil(bits/dataPathWidth)}
   */
  public static int ioLatency(int bits) {
    int dataPathWidth = target.Processor.Instance.getDataPathWidth();
    return (dataPathWidth <= 0) ? 1 : (bits+dataPathWidth-1)/dataPathWidth;
  }
  
  /**
   * Get number of cycles required to shift a bit vector through the data path of the {@code target.Processor}.
   * 
   * @param format the {@link Format} of the bit vector
   * @return       {@code ceil(bits/dataPathWidth)}
   */
  public static int ioLatency(Format format) {
    return format == null ? 0 : ioLatency(format.getBitWidth());
  }
  
  /**
   * Indent each line of a String by another string
   * @param pre the prefix for each line
   * @param s   the list of lines to be indented
   * @return    the indented lines
   */
  protected static String indent(String pre, String s) {
    String[] lines = s.split("\n", -1);
    for (int i=0; i<lines.length; i++) lines[i] = (lines[i].isEmpty() ? "" : pre) + lines[i];
    return String.join("\n", lines);
  }
  
/*
 * Instance Fields
 **********************************************************************************************************************/
  
  /** Arithmetic input {@code Format}s  (no control signals) */
  protected ArrayList<Format> operandFormat = new ArrayList<>();

  /** Arithmetic output {@code Format}s (no control signals) */
  protected ArrayList<Format> resultFormat = new ArrayList<>();
  
  /** Unique (within {@code PE}) {@code Operator} identifier */
  protected int opcode = 0; 

  /** Number of clock cycles per execution (including I/O latency) */
  protected int latency = 1; 

  /** Measure of runtime and area requirements. TODO: use power*latency instead */
  protected double energyConsumption = Double.NaN;


/*
 * Constructors
 **********************************************************************************************************************/
  
  protected Implementation() {
  }
  
  /**
   * Generate {@code Implementation} for multi result {@code Operator}.
   * 
   * @param operandFormat the arithmetic inputs
   * @param resultFormat  the arithmetic outputs
   */
  protected Implementation(List<Format> operandFormat, List<Format> resultFormat) {
    this();
    this.operandFormat.addAll(operandFormat);
    this.resultFormat.addAll(resultFormat);
  }
  
  /**
   * Generate {@code Implementation} for single result {@code Operator}.
   * 
   * @param operandFormat the arithmetic inputs
   * @param resultFormat  the arithmetic outputs
   */
  protected Implementation(List<Format> operandFormat, Format resultFormat) {
    this(operandFormat, Arrays.asList(resultFormat));
  }
  
  
/*
 * Runtime configuration
 **********************************************************************************************************************/
  
  /**
   * Configure unique identifier of {@code Operator} within a {@code PE}.
   * 
   * @param  opcode identifier
   * @throws IllegalArgumentException if {@code opcode < 0}
   */
  public void setOpcode(int opcode) {
    if (opcode < 0) {
    throw new IllegalArgumentException("invalid opcode: " + opcode);
  }
    this.opcode = opcode;
  }
  
  /**
   * Configure execution delay of {@code Operator}.
   * <p>
   * The latency can be configured freely without considering the underlying {@link Operator} {@link Implementation} to
   * allow for a full simulation-based design space exploration. 
   *  
   * @param  latency the execution delay
   * @throws IllegalArgumentException if {@code latency < 1}
   * @see    #configure(JSONObject)
   */
  public void setLatency(int latency) {
    if (latency < 1) {
    throw new IllegalArgumentException("invalid latency: " + latency);
  }
    this.latency = latency;
  }
  
  /**
   * Configure estimated energy consumption.
   * 
   * @param energyConsumption the new estimate
   * @see   #configure(JSONObject)
   */
  public void setEnergyConsumption(double energyConsumption) {
    this.energyConsumption = energyConsumption;
  }

  /**
   * Change arithmetic precision of a certain operand.
   * 
   * @param  index  the operand to be modified
   * @param  format the new input precision of the operand
   * @throws IndexOutOfBoundsException if no valid operand is specified
   * @see    #configure(JSONObject)
   */
  public void setOperandFormat(int index, Format format) {
    if (index < 0 || index >= getNumberOfOperands()) {
      throw new IndexOutOfBoundsException("invalid index: " + index);
    }
    operandFormat.set(index, format);
  }
  
  /**
   * Change arithmetic precision of a certain result.
   * 
   * @param  index  the result to be modified
   * @param  format the new output precision of the result
   * @throws IndexOutOfBoundsException if no valid result is specified
   * @see    #configure(JSONObject)
   */
  public void setResultFormat(int index, Format format) {
    if (index < 0 || index >= getNumberOfResults()) {
      throw new IndexOutOfBoundsException("invalid index: " + index);
    }
    resultFormat.set(index, format);
  }
  
  /**
   * Change arithmetic precision of the (first) result.
   * 
   * @param  format the new output precision of the result
   * @throws UnsupportedOperationException if this {@link Operator} does not support dynamic accuracy
   * @throws IndexOutOfBoundsException if this {@link Operator} does not provide any results
   */
  public void setResultFormat(Format format) {
    setResultFormat(0, format);
  }
  
  /**
   * Set all operands and results to a common {@code Format}
   * @param f the common {@link Format}
   */
  public void setCommonFormat(Format f) {
    for (int i=0; i<getNumberOfOperands(); i++) setOperandFormat(i, f);
    for (int i=0; i<getNumberOfResults();  i++) setResultFormat (i, f);
  }
  
  /**
   * Set the result {@code Format}(s) to the minimum number of bits required, such that no information gets lost.
   * <p>
   * This {@code Operator}-specific forward propagation of arithmetic accuracy has to be implemented by the 
   * {@link Implementation} subclasses.
   * 
   * @throws NotImplementedException if this underlying {@link Implementation} does not yet support this method
   */
  public void fitResultFormat() {
    throw new NotImplementedException("result format fitting");
  }
  
  /**
   * Adjust latency to the minimum supported latency.
   */
  public boolean fitLatency() {
    int ml = getMinLatency();
    if (ml <= 0) return false;
    setLatency(ml);
    return true;
  }
  
  /**
   * Derive {@code Implementation} details from JSON configuration.
   * <p>
   * A simple String is interpreted as canonical name of the accuracy {@link Format} to be used by all operands and 
   * results. See {@link Format#parse} for a description of valid canonical {@link Format}s. Key/Value maps are 
   * interpreted by {@link #configure(JSONObject)}.
   * 
   * @param o 
   */
  public void configure(Object o) {
    if (o instanceof String) {
      Format f = Format.parse(o.toString());
      if (f == null) throw new IllegalArgumentException("invalid format: " + o);
      for (int i=0; i<getNumberOfOperands(); i++) setOperandFormat(i, f);
      for (int i=0; i<getNumberOfResults();  i++) setResultFormat( i, f);
    } else {
      configure((JSONObject) o);
    }
  }
  
  /**
   * Derive {@code Implementation} details from JSON configuration.
   * <p>
   * Key/Value maps are interpreted as
   * <ul>
   *  <li> OP_x               : canonical {@link Format} of operand x (counted as A, B, ...)
   *  <li> RESULT             : canonical {@link Format} of the (first) result
   *  <li> RESULTx            : canonical {@link Format} of further results (x>=1)
   *  <li> format             . canonical {@link Format} of all operands and results
   *  <li> energy             : numeric value of energy consumption
   *  <li> latency | duration : number of cycles per {link Operator} execution
   * </ul>
   * See {@link Format#parse} for a description of valid canonical {@link Format}s. 
   * 
   * Subclasses derive additional settings (e.g., radix of division)
   * @param o
   */
  public void configure(JSONObject o) {
    for (int i=0; i<getNumberOfOperands(); i++) {
      String key = getOperandPort(i).getName();
      if (o.containsKey(key)) setOperandFormat(i, Format.parse(o.get(key).toString()));
    }
    for (int i=0; i<getNumberOfResults(); i++) {
      String key = getResultPort(i).getName();
      if (o.containsKey(key)) setResultFormat(i,  Format.parse(o.get(key).toString()));
    }
    
    Object val = o.get("format");
    if (val != null) {
      Format f = Format.parse(val.toString());
      for (int i=0; i<getNumberOfOperands(); i++) setOperandFormat(i, f);
      for (int i=0; i<getNumberOfResults();  i++) setResultFormat( i, f);
    }
        
    val = o.get("energy");
    if (val != null) setEnergyConsumption(Double.parseDouble(val.toString()));
    
    val = o.get("latency");
    if (val != null) setLatency(Integer.parseInt(val.toString()));
    
    val = o.get("duration");
    if (val != null) setLatency(Integer.parseInt(val.toString()));
  }
  
  /**
   * Dump configurable fields of this {@code Implementation} to JSON.
   * Opposite of {@link #configure(JSONObject)}.
   * @return {@link JSONObject} describing the format, latency and energy consumption
   */
  @SuppressWarnings("unchecked")
  public JSONObject getJSONDescription() {
    JSONObject res = new JSONObject();
    Format f = getCommonFormat();
    if (f != null) {
      res.put("format", f.getCanonicalName());
    } else {
      for (int i=0; i<getNumberOfOperands(); i++) res.put(getOperandPort(i), getOperandFormat(i).getCanonicalName());
      for (int i=0; i<getNumberOfResults(); i++)  res.put(getResultPort(i),  getResultFormat(i) .getCanonicalName());
    }
    if (Double.isFinite(energyConsumption)) res.put("energy", energyConsumption);
    res.put("latency", latency);
    return res;
  }
  

/*
 * Derived properties  
 **********************************************************************************************************************/
  
  /**
   * Number of arithmetic inputs (without control signals).
   * 
   * @return input arity. 
   */
  public int getNumberOfOperands() {
    return operandFormat.size();
  }
  
  /**
   * Number of arithmetic outputs (without control signals).
   * 
   * @return output arity.
   */
  public int getNumberOfResults() {
    return resultFormat.size();
  }
  
  /**
   * Get {@code Module} {@code Port} for arithmetic operand.
   * 
   * @param index the requested operand
   * @return      the corresponding {@link Port}
   * @throws IndexOutOfBoundsException if no valid operand is specified
   */
  protected Port getOperandPort(int index) {
    switch (index) {
      case 0:  return OPA;
      case 1:  return OPB;
      case 2:  return OPC;
      default: throw new IndexOutOfBoundsException("invalid index: "     + index);
    }
  }
  
  /**
   * Get {@code Module} {@code Port} for arithmetic results.
   * 
   * @param index the requested result
   * @return      the corresponding {@link Port}
   * @throws IndexOutOfBoundsException if no valid result is specified
   */
  protected Port getResultPort(int index) {
    switch (index) {
      case 0:  return RESULT;
      default: throw new IndexOutOfBoundsException("invalid index: "     + index);
    }
  }
  
  /**
   * Get {@code Module} {@code Port} for the (first) arithmetic result.
   * 
   * @return the corresponding {@link Port}
   * @throws IndexOutOfBoundsException if this {@link Operator} does not provide any result
   */
  protected Port getResultPort() {
    return getResultPort(0);
  }
  
  /**
   * Get accuracy of certain arithmetic input.
   * 
   * @param index the requested operand
   * @return      the configured operand {@link Format} or null, if requested output does not exist 
   */
  public Format getOperandFormat(int index) {
    return (index < 0 || index >= operandFormat.size()) ? null : operandFormat.get(index);
  }
  
  /**
   * Get accuracy of certain arithmetic output.
   * 
   * @param index the requested result
   * @return      the configured result {@link Format} or null, if requested output does not exist
   */
  public Format getResultFormat(int index) {
    return (index < 0 || index >= resultFormat.size()) ? null : resultFormat.get(index);
  }
  
  /**
   * Get accuracy of the (first) arithmetic output.
   * 
   * @return the configured result {@link Format}
   */
  public Format getResultFormat() {
    return getResultFormat(0);
  }
  
  /**
   * Check, whether all operands and results share a common {@code Format}.
   * 
   * @return the common {@link Format} or {@code null}
   */
  public Format getCommonFormat() {
    Format f = getResultFormat(0);
    for (int i=0; i<getNumberOfOperands(); i++) if (!getOperandFormat(i).equals(f)) return null;
    for (int i=1; i<getNumberOfResults();  i++) if (!getResultFormat(i) .equals(f)) return null;
    return f;
  }
  
  /**
   * Get bit width of arithmetic input port.
   * <p>
   * The port width may be smaller than the operand width, if the {@link Processor} limits the data path width.
   * Native {@link Operator}s report a port width of 0, as they are not actually implemented.
   * 
   * @param index the requested operand
   * @return      the effective operand port width
   */
  public int getOperandPortWidth(int index) {
    if (isNative() || getNumberOfOperands() <= index) return 0;
    int bw = getOperandFormat(index).getBitWidth();
    return target.Processor.Instance.getDataPathWidth() > 0 ? Math.min(bw, Processor.Instance.getDataPathWidth())
                                                            : bw;
  }
  
  /**
   * Get bit width of arithmetic output port.
   * <p>
   * The port width may be smaller than the result width, if the {@link Processor} limits the data path width.
   * Native {@link Operator}s report a port width of 0, as they are not actually implemented.
   * 
   * @param index the requested result
   * @return      the effective result port width
   */
  public int getResultPortWidth(int index) {
    if (isNative() || getNumberOfResults() <= index) return 0;
    int bw = getResultFormat(index).getBitWidth();
    return target.Processor.Instance.getDataPathWidth() > 0 ? Math.min(bw, Processor.Instance.getDataPathWidth())
                                                            : bw;
  }
  
  /**
   * Get bit width of the (first) arithmetic output port.
   * 
   * @return the effective result port width
   * @see #getResultPortWidth(int)
   */
  public int getResultPortWidth() {
    return getResultPortWidth(0);
  }
  
  
  /**
   * Check, if all operands and results are configured to be integers.
   * 
   * @return true, if all operands and results are integers.
   */
  public boolean isIntegerOperation() {
    return getCommonFormat() instanceof Format.Integer;
  }
  
  /**
   * Check, if this {@code Implementation} corresponds to a native {@code Operator}.
   * <p>
   * Those {@code Operator}s do not require/support Verilog code generation.
   * 
   * @return true, if this {@link Implementation} is native
   */
  public boolean isNative() {
    return false;
  }
  
  
  /**
   * Get canonical name of {@code Operator} reflecting the class name, I/O accuracy, and latency.
   *  
   * @return name of {@code Operator}.
   */
  public String getName() {
    StringBuilder res = new StringBuilder();
    res.append(getClass().getSimpleName());
    
    // shorten name, if all I/O uses the same format
    Format cf = getCommonFormat();
    if (cf != null) {
      res.append("_" + cf.getCanonicalName());
      
    // show format for all I/O for mixed format configurations
    } else {
      for (Format f : operandFormat) res.append("_" + f.getCanonicalName());
      for (Format f : resultFormat)  res.append("_" + f.getCanonicalName());
    }
    res.append("_" + getLatency() + "C");
    return res.toString();
  }
  
  @Override
  public String toString() {
    return getName();
  }
  
  /**
   * Derive the latency required to shift in a certain operand.
   * @param index the requested operand
   * @return operand input cycles
   */
  public int getInputLatency(int index) {
    return ioLatency(getOperandFormat(index));
  }
  
  /**
   * Derive the minimum latency required to shift in all operands.
   * 
   * @return minimum input latency
   */
  public int getInputLatency() {
    int max = 0;
    for (Format f : operandFormat) max = Math.max(max, f.getBitWidth());
    return ioLatency(max);
  }
  
  /**
   * Derive the latency required to shift out a certain result.
   * @param index the requested result
   * @return result output cycles
   */
  public int getOutputLatency(int index) {
    return ioLatency(getResultFormat(index));
  }
  
  /**
   * Derive the minimum latency required to shift out all results.
   * @return result output cycles
   */
  public int getOutputLatency() {
    int max = 0;
    for (Format f : resultFormat) max = Math.max(max, f.getBitWidth());
    return ioLatency(max);
  }
  
  /**
   * Derive all supported latencies for the current IO {@code Format} settings.
   * <p>
   * The restricted data path width may cause sequential shift in and out of operands and results. This multi cycle IO
   * may overlap with the actual computation of the result. Subclasses should add more knowledge about the precision 
   * dependent latency by overriding 
   * <ul>
   *   <li>{@link #getSupportedIntegerLatency()}
   *   <li>{@link #getSupportedFixedPointLatency()}
   *   <li>{@link #getSupportedFloatingPointLatency()}
   *   <li>{@link #getSupportedMixedFormatLatency()}
   * </ul>
   * ATTENTION: the supported latency is effected by the configured operand and result {@link Format}s AND the datapath
   * width of the target {@link Processor}.
   * 
   * @return list of supported latencies
   */
  public List<Integer> getSupportedLatency() {
    return getSupportedLatency(getCommonFormat());
  }
  
  /**
   * Derive all supported latencies.
   * 
   * @param  common the common IO format or null for mixed format configurations
   * @return list of supported latencies
   */
  protected List<Integer> getSupportedLatency(Format common) {
    List<Integer> res = null;
    if (res == null && common instanceof Format.Integer)       res = getSupportedIntegerLatency();
    if (res == null && common instanceof Format.FixedPoint)    res = getSupportedFixedPointLatency();
    if (res == null && common instanceof Format.FloatingPoint) res = getSupportedFloatingPointLatency();
    if (res == null)                                           res = getSupportedMixedFormatLatency();
    return res;
  }
  
  /**
   * Derive all supported latencies for an {@code Format.Raw} implementation.
   * @return list of supported latencies
   */
  protected List<Integer> getSupportedRawLatency() {
    return null;
  }
  /**
   * Derive all supported latencies for an {@code Format.Integer} implementation.
   * @return list of supported latencies
   */
  protected List<Integer> getSupportedIntegerLatency() {
    return null;
  }
  
  /**
   * Derive all supported latencies for an {@code Format.FixedPoint} implementation.
   * @return list of supported latencies
   */
  protected List<Integer> getSupportedFixedPointLatency() {
    return null;
  }
  
  /**
   * Derive all supported latencies for an {@code Format.FloatingPoint} implementation.
   * @return list of supported latencies
   */
  protected List<Integer> getSupportedFloatingPointLatency() {
    return null;
  }
  
  /**
   * Derive all supported latencies for an {@code Format.FloatingPoint} implementation.
   * @return list of supported latencies
   */
  protected List<Integer> getSupportedMixedFormatLatency() {
    return null;
  }
  
  /**
   * Derive the minimum latency required to handle the currently configured I/O precision.
   * @return the smallest supported latency or 0, if implementation is not supported
   */
  public int getMinLatency() {
    List<Integer> list = getSupportedLatency();
    if (list == null) return 0;
    Collections.sort(list);
    return list.get(0);
  }
  
  /**
   * Get number of cycles per operation.
   * <p>
   * The configured latency may deviate from the minimum (natural) latency of this {@link Implementation}. In this case,
   * the {@link Implementation} may be used for simulation, but not for Verilog generation.
   * 
   * @return the configured latency.
   */
  public int getLatency() {
    return latency;
  }
  
  /**
   * Check for sequential operation.
   * 
   * @return true, if this {@link Implementation} requires multiple execution cycles
   */
  public boolean isMultiCycle() {
    return getLatency() > 1;
  }
  
  /**
   * Check for control flow {@code Operator}.
   * 
   * @return true, if this {@link Implementation} generates a status flag
   */
  public boolean isControlFlow() {
    return false;
  }
  
  /**
   * Check for memory (cache or ROM) access {@code Operator}.
   * 
   * @return true, if this {@link Implementation} represents a memory access
   */
  public boolean isMemAccess() {
    return isCacheAccess() || isRomAccess();
  }
  
  /**
   * Number of words to read/write from/to external memory
   * @return
   */
  public int getMemAccessWords() {
    return 0;
  }
  
  /**
   * Check for wide memory access {@code Operator}.
   * 
   * @return true, if this {@link Implementation} represents an wide memory access
   */
  public boolean isWideMemAccess() {
    return getMemAccessWords() > 1;
  }
  
  /**
   * Get port width required to specifiy the number of additional words in wide memory accesses
   * @return
   */
  public int getWideMemAccessPortWidth() {
    int words = getMemAccessWords();
    if (words <= 1) return 0;                               // no wide mem access
    if (words == 2) return 1;                               // simple wide mem access
    return (int) Math.ceil(Math.log(words-1)/Math.log(2));  // more than one additional word
  }
  
  /**
   * Check for memory store {@code Operator}.
   * 
   * @return true, if this {@link Implementation} writes to a memory
   */
  public boolean isStore() {
    return false;
  }
  
  /**
   * Check for regfile access {@code Operator}.
   * 
   * @return true, if this {@link Implementation} represents a register access
   */
  public boolean isRegfileAccess() {
    return false;
  }
  
  /**
   * Check for cache access {@code Operator}.
   * 
   * @return true, if this {@link Implementation} represents a cache access
   */
  public boolean isCacheAccess() {
    return false;
  }
  
  /**
   * Check for ROM access {@code Operator}.
   * 
   * @return true, if this {@link Implementation} represents a ROM access
   */
  public boolean isRomAccess() {
    return false;
  }
  
  /**
   * Check for indexed memory access {@code Operator}.
   * 
   * @return true, if this {@link Implementation} represents an array memory access
   */
  public boolean isIndexedMemAccess() {
    return false;
  }
  
  /**
   * Check for non-blocking cache prefetch {@code Operator}.
   * 
   * @return true, if this {@link Implementation} represents a cache prefetch
   */
  public boolean isCachePrefetch() {
    return false;
  }
  
  
  /**
   * Get configured opcode.
   * <p>
   * The opcode is set by {@link PEModel#organizeOpcode} and not actually used for the {@link Operator}-specific Verilog code
   * generation. Instead, it is used at ALU-level to enable active {@link Operator}s. 
   * 
   * @return the configured opcode
   */
  public int getOpcode() {
    return opcode;
  }
  
  /**
   * Get estimated energy consumption for one {@code Operator} execution.
   * 
   * @return the configured energy consumption.
   */
  public double getEnergyconsumption() {
    return energyConsumption;
  }

  
/*
 * Design space exploration
 **********************************************************************************************************************/

  /**
   * Get supported I/O accuracy, this {code Implementation} can generate Verilog code for.
   *  
   * If the resulting list contains {@code null}, the {@code Operator} supports mixed format {@code Implementation}s, 
   * i.e., operand and result-specific accuracy.
   * 
   * @return list of supported I/O {@link Format}s, which may contain null.
   */
  public List<Class<? extends Format>> getSupportedFormats() {
    LinkedList<Class<? extends Format>> list = new LinkedList<Class<? extends Format>>();
    for (Method m : getClass().getMethods()) {
      if (m.getName().equals("getRawImplementation"))           list.add(Format.Raw.class);
      if (m.getName().equals("getIntegerImplementation"))       list.add(Format.Integer.class);
      if (m.getName().equals("getFixedPointImplementation"))    list.add(Format.FixedPoint.class);
      if (m.getName().equals("getFloatingPointImplementation")) list.add(Format.FloatingPoint.class);
      if (m.getName().equals("getMixedFormatImplementation"))   list.add(null);
    }
    return list;
  }
  
  /**
   * Check, whether the current configuration can be implemented.
   * <p>
   * TODO: speedup by grouping all sanity checks in a single method (without actually producing the implementation).
   * 
   * @return true, if Verilog code can be generated for this {@link Implementation}.
   */
  public boolean isValidConfiguration() {
    try {
      return getImplementation() != null; 
    } catch (Exception e) {
      return false;
    }
  }
  
  @Override
  @SuppressWarnings("unchecked")
  public Implementation clone() {
    try {
      Implementation g = (Implementation) super.clone();
      g.operandFormat = (ArrayList<Format>) operandFormat.clone(); // arrays must be cloned explicitly
      g.resultFormat  = (ArrayList<Format>) resultFormat.clone();
      return g;
    } catch (CloneNotSupportedException e) {
      return null;
    }
  }
  
  
/*
 * Test vector genration for automated functional testing
 **********************************************************************************************************************/
  
  /**
   * Define number of validated I/O combinations.
   * 
   * @return test vector length > 0
   */
  protected int getTestRuns() {
    return 1;
  }
  
  /**
   * Generate test input data.
   * 
   * @param operandIndex the operand to generate a value for
   * @param run          the index of the current test run
   * @return             (random) value from the configured operand range
   */
  protected Number[] getTestVector(int run) {
    Number[] input = new Number[getNumberOfOperands()];
    for (int i=0; i<input.length; i++) input[i] = getOperandFormat(i).getRandomValue();
    Number[] result = apply(input);
    
    return Stream.concat(Arrays.stream(input), Arrays.stream(result)).toArray(Number[]::new); 
  }
  
  /**
   * Apply operator to concrete operands.
   * 
   * @param  input the operands
   * @return       the computed results
   */
  public Number[] apply(Number... input) {
    throw new NotImplementedException("arbitrary arithmetic");
  }
  
/*
 * Verilog generation 
 **********************************************************************************************************************/
  
  /**
   * Generate body of the Verilog testbench implementing the automated functional verification.
   * 
   * @return testbench body
   */
  protected String getTest() {
    int dataPathWidth = target.Processor.Instance.getDataPathWidth();
    StringBuilder res = new StringBuilder();
    res.append("initial begin\n");
    
    // wait for release of reset
    res.append("  @(negedge " + RESET + ");\n");
    res.append("  @(posedge " + CLOCK + ");\n\n");
    
    
    // generate test I/O
    for (int r = 0; r<getTestRuns(); r++) {
      Number[] io  = getTestVector(r);
      
      // print inputs as hex literals, to avoid 32bit restriction of decimal literals
      // use sequential I/O if port width is restricted data path width
      res.append("  // run " + r +"\n");
      for (int c=0; c<getLatency(); c++) {
        res.append("  // cycle " + c +"\n");
        if (c == 0) res.append("  " + START + " <= 1;\n");
        
        for (int i=0; i<getNumberOfOperands(); i++) {
          Format       f = getOperandFormat(i);
          BigNumber    n = BigNumber.cast(io[i]);
          boolean[]  raw = f.getRawBinary(n);
          int         ow = f.getBitWidth();
          int         pw = getOperandPortWidth(i);
          String literal = getOperandPortWidth(i) + "'bX";
          String comment = "sequential input finished";

          // input completely passed in first cycle => generate decimal literal, and X for subsequent cycles 
          if (ow == pw) {
            if (c > 0) {
              literal = ow + "'bX";
              comment = "sequential input finished";
            } else {
              literal = BigNumber.getHexLiteral(raw); 
              comment = n.toString(f);
            }
          
          // input passed in multiple cycles => generate XX also in last input cycle for the invalid high bits
          } else {
            if (c >= ioLatency(ow)) {
              literal = pw + "'bX";
              comment = "sequential input finished";
            } else {
              int lb = c * dataPathWidth;
              int hb = Math.min(ow, lb+dataPathWidth)-1; 
              literal = BigNumber.getHexLiteral(raw, hb, lb); // use hex literal for sliced values
              if (hb == ow-1 && hb+1-lb < dataPathWidth) literal = "{"+(dataPathWidth-hb-1+lb) + "'bX," + literal+"}";
              comment = n.toString(f) + bitRange(hb, lb);
            }
          }
          res.append("  " + getOperandPort(i) + " <= " + literal + "; // " + comment +"\n");
        }
        
        // wait for next cycle
        res.append("  @(posedge " + CLOCK + ");\n");
        if (c == 0) {
      res.append("  " + START + " <= 0;\n");
    }
        
        // check results in latest possible cycles
        // compare results with expected values
        for (int i=0; i<getNumberOfResults(); i++) {
          String   name = getResultPort(i).toString();
          Format      f = getResultFormat(i);
          BigNumber   n = BigNumber.cast(io[getNumberOfOperands()+i]);
          boolean[] raw = f.getRawBinary(n);
          String literal;
          String slice;
          if (dataPathWidth <= 0 || f.getBitWidth() == dataPathWidth) {
            if (c < getLatency()-1) continue;
            literal = BigNumber.getHexLiteral(raw);
            slice   = "";
          } else {
            int outCycles = (f.getBitWidth() + dataPathWidth-1) / dataPathWidth;
            if (c < getLatency() - outCycles) continue;
            int lb = (c-getLatency()+outCycles) * dataPathWidth;
            int hb = Math.min(f.getBitWidth(), lb+dataPathWidth)-1;
            literal = BigNumber.getHexLiteral(raw, hb, lb);
            slice   = bitRange(hb,lb);
            if (c == getLatency()-1 && (hb+1-lb != dataPathWidth)) name += bitRange(hb+1-lb);
          }
          res.append("  if (!(" + name + " === " + literal + ")) "); // "name != literal" does not capture XXX
          res.append("$error(\"run="+ r + ", cycle=" + c + ": ");
          res.append("expected " + name + " = " + literal + " (" + n.toString(f)+slice+")\");\n");
        }
      }
      res.append("\n");
    }
    res.append("  $display(\"FINISHED test\");\n");
    res.append("  $finish();\n");
    res.append("end");
    return res.toString();
  }

  /**
   * Exception thrown, if generation of the Verilog module was not yet implemented for a specific configuration of this
   * {@code Operator}.
   * 
   * @author Andreas Engel [engel@esa.cs.tu-darmstadt.de]
   */
  @SuppressWarnings("serial")
  public class NotImplementedException extends RuntimeException {
    protected NotImplementedException(String detail, String msg) {
      super(detail + " " + msg + " for " + Implementation.this.getName() + 
           (target.Processor.Instance.getDataPathWidth() <= 0 ? "" : 
               " with " + target.Processor.Instance.getDataPathWidth() + " bit datapath"));
    }
    public NotImplementedException(String feature) {
       this(feature, "not yet implemented");
    }
  }
  
  /**
   * Exception thrown, if the generation of the Verilog module is not possible for the specific configuration of this
   * {@code Operator}.
   * 
   * @author Andreas Engel [engel@esa.cs.tu-darmstadt.de]
   */
  @SuppressWarnings("serial")
  public class InvalidConfigException extends NotImplementedException {
    public InvalidConfigException(String config) {
      super(config, "not possible");
    }
  }
  
  /**
   * Get input {@code Port}s required for sequential {@code Operator}s.
   * 
   * @return list of {@link Port}s
   * @see #CLOCK
   * @see #RESET
   * @see #ENABLE
   */
  protected List<Port> getSequentialPorts() {
    LinkedList<Port> res = new LinkedList<Port>();
    res.add(CLOCK);
    res.add(RESET);
    if (target.Processor.Instance.isStallable()) res.add(ENABLE);
    return res;
  }
  
  /**
   * Get handshake {@code Port}s signaling valid operands and results.
   * 
   * @return list of {@link Port}s
   * @see #START
   * @see #READY
   */
  protected List<Port> getHandshakePorts() {
    LinkedList<Port> res = new LinkedList<Port>();
                                  res.add(START);
    if (isControlFlow())          res.add(STATUS_VALID);
    if (getNumberOfResults() > 0) res.add(RESULT_VALID);
    return res;
  }
  
  public static class ArithmeticPort extends Port {
    private String radix;
    public ArithmeticPort(Port base, Format format) {
      super(
          base.getType(), 
          base.getName(), 
          Math.min(Processor.Instance.getDataPathWidth(), format.getBitWidth()), 
          base.getComment() + " (" + format.getCanonicalName() + ")"
      );
      radix = format.getBitWidth() <= Processor.Instance.getDataPathWidth() ? format.getWaveRadix() 
                                                                            : super.getWaveRadix();
    }
    
    @Override
    public String getWaveRadix() {
      return radix;
    }
  }
  
  /**
   * Get input and output {@code Port}s required for arithmetic operands and results.
   * 
   * @return list of {@link Port}s
   * @see #getOperandPort(int)
   * @see #getResultPort(int)
   */
  protected List<Port> getArithmeticPorts() {
    LinkedList<Port> res = new LinkedList<Port>();
    for (int i=0; i<getNumberOfOperands(); i++) res.add(new ArithmeticPort(getOperandPort(i), getOperandFormat(i)));
    for (int i=0; i<getNumberOfResults();  i++) res.add(new ArithmeticPort(getResultPort(i),  getResultFormat(i)));
    if (isControlFlow()) res.add(STATUS);
    return res;
  }
  
  /**
   * Derive the whole interface of this {@code Operator} within the ALU of a {@code PE}.
   * <p>
   * Besides the arithmetic I/O, some control signals are added for sequential and memory access operations.
   * 
   * @return list of {@link Port}s
   */
  public List<Port> getPorts() {
    LinkedList<Port> res = new LinkedList<Port>();
    if (isMultiCycle())  res.addAll(getSequentialPorts());
                         res.addAll(getHandshakePorts());
                         res.addAll(getArithmeticPorts());
    return res;
  }
  
  /**
   * Generate comment describing the configuration details.
   * 
   * @return module header comment
   */
  protected String getModuleHeaderComment() {
    Map<String, Object> info = getModuleHeaderInfo();
    int max = 0;
    for (String s : info.keySet()) max = Math.max(max, s.length());
    String f = " * %-" + max + "s : %s\n";
    StringBuilder res = new StringBuilder();
    res.append("/*\n");
    for (String s : info.keySet()) res.append(String.format(f, s, info.get(s).toString()));
    res.append(" */\n");
    return res.toString();
  }
  
  
  /**
   * Generate detailed information about this {@code Implementation} and the generation time.
   * 
   * @return key/value map for {@link #getModuleHeaderComment}
   */
  protected Map<String, Object> getModuleHeaderInfo() {
    LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
    map.put("generated on",              new Date());
    map.put("generated by",              getClass().getCanonicalName());
    map.put("opcode",                    getOpcode());
    map.put("latency",                   getLatency());
    map.put("stallable",                 target.Processor.Instance.isStallable());
    map.put("is control flow",           isControlFlow());
    map.put("is cache access",           isCacheAccess());
    map.put("is prefetch",               isCachePrefetch());
    map.put("is store",                  isStore());
    map.put("is rom access",             isRomAccess());
    map.put("is array access",           isIndexedMemAccess());
    map.put("is wide memory access",     isWideMemAccess());
    map.put("energy consumption",        getEnergyconsumption());
    return map;
  }
  
  /**
   * Generate a Verilog module implementing the configured operator.
   * <p>
   * This method concatenates {@link #getModuleHeaderComment()}, the module interface declaration and
   * {@link #getImplementation}.
   * 
   * @return Verilog code ready to be dumped to a text file.
   */
  public String getModule() {
    StringBuilder res = new StringBuilder();
    
    // the header
    res.append(getModuleHeaderComment() + "\n");
    res.append("`default_nettype none\n\n");
    
    // the interface
    res.append(getDeclaration());
        
    // generic logic
    if (isMultiCycle()) res.append(indent("  ", getCycleCounter()));
    
    // specific logic
    res.append(indent("  ", getImplementation()));
    res.append("\nendmodule");
    
    return res.toString();
  }

  /**
   * Drive {@link #RESULT_VALID} high, as long as valid results are generated
   */
  protected String getReadyDriver() {
    StringBuilder res = new StringBuilder("assign " + RESULT_VALID + " = ");

    // never generate output?
    if (getOutputLatency() == 0) {
      res.append("1'b0");
      
    // combinatorial operator?
    } else if (getLatency() == 1) {
      res.append(START);
    
    // multicycle op => last cycles used for output
    } else if (getLatency() > getOutputLatency()) {
      res.append("cycle >= " + (getLatency()-getOutputLatency()) + " ? 1'b1 : 1'b0");
    
    // special case: all cycles produce outputs, but first cycle only if START is set
    } else {
      res.append("cycle == 0 ? " + START + " : 1'b1");
    }
    res.append(";\n\n");
    return res.toString();
  }
  
  /**
   * Generate the body of the Verilog module implementing the configured operator.
   * 
   * @return Verilog code
   * @throws NotImplementedException, if the underlying {@code Implementation} is native or not yet available.
   */
  protected String getImplementation() {
    Format f = getCommonFormat();
    
    // sanity checks
    List<Integer> supportedLatency = getSupportedLatency(f);
    if (supportedLatency == null)                 throw new NotImplementedException("code generation");
    if (!supportedLatency.contains(getLatency())) throw new NotImplementedException("latency=" + getLatency());
    
    // distinguish targeted operator format (try next complex, if prev. format is actually not supported)
    String res = null;
    if (res == null && f instanceof Format.Raw)           res = getRawImplementation();
    if (res == null && f instanceof Format.Integer)       res = getIntegerImplementation();
    if (res == null && f instanceof Format.FixedPoint)    res = getFixedPointImplementation();
    if (res == null && f instanceof Format.FloatingPoint) res = getFloatingPointImplementation();
    if (res == null)                                      res = getMixedFormatImplementation();
    if (res == null) throw new NotImplementedException(f.getClass().getSimpleName() + " code generation");
    return res;
  }
  
  /**
   * Generate the body of the Verilog module implementing the configured {@code Format.Raw} operator.
   * @return Verilog code
   */
  protected String getRawImplementation() {
    return null;
  }
  
  /**
   * Generate the body of the Verilog module implementing the configured {@code Format.Integer} operator.
   * @return Verilog code
   */
  protected String getIntegerImplementation() {
    return null;
  }
  
  /**
   * Generate the body of the Verilog module implementing the configured {@code Format.Fixedpoint} operator.
   * @return Verilog code
   */
  protected String getFixedPointImplementation() {
    return null;
  }
  
  /**
   * Generate the body of the Verilog module implementing the configured {@code Format.Fixedpoint} operator.
   * @return Verilog code
   */
  protected String getFloatingPointImplementation() {
    return null;
  }
  
  /**
   * Generate the body of the Verilog module implementing the configured mixed {@code Format} operator.
   * @return Verilog code
   */
  protected String getMixedFormatImplementation() {
    return "";
  }
  
  /**
   * Generate clocked process.
   * <p>
   * Handles 
   * <ul>
   *   <li> {@link Module#CLOCK}, 
   *   <li> {@link Module#RESET}, 
   *   <li> {@link Module#START} and 
   *   <li> {@link Module#ENABLE} signals.
   * </ul>
   * 
   * @param  reset  to be executed on reset
   * @param  remaining to be executed after reset, if start is active
   * @param  remaining to be executed after reset, if start is not active
   * @return Verilog code
   */
  protected String getClockedProcess(String reset, String start, String remaining) {
    String p = "";
    StringBuilder res = new StringBuilder();
    res.append("always @(posedge " + CLOCK + ") begin\n");
    if (target.Processor.Instance.isStallable()) {
      res.append("  if (" + ENABLE + ") begin\n");
      p += "  ";
    }
    if (reset != null || start != null) p += "  ";
    
    if (reset != null) {
      res.append(p + "if (" + RESET + ") begin\n");
      res.append(indent(p + "  ", reset) + "\n");
    }
    if (start != null) {
      res.append(p);
      if (reset != null) res.append("end else ");
      res.append("if (" + START + ") begin\n");
      res.append(indent(p + "  ", start) + "\n");
    }
    if (remaining != null) {
      if (reset != null || start != null) res.append(p + "end else begin\n");
      res.append(indent(p + "  ", remaining) + "\n");
    }
    if (reset != null || start != null) res.append(p + "end\n");
    if (target.Processor.Instance.isStallable()) {
      res.append("  end\n");
    }
    res.append("end\n\n");
    return res.toString();
  }
  
  /**
   * Generate clocked process.
   * <p>
   * Handles 
   * <ul>
   *   <li> {@link Module#CLOCK}, 
   *   <li> {@link Module#RESET}, 
   *   <li> {@link Module#ENABLE} signals.
   * </ul>
   * 
   * @param  reset  to be executed on reset
   * @param  active to be executed after reset independent from {@link Module#START}.
   * @return Verilog code
   */
  protected String getClockedProcess(String reset, String active) {
    return getClockedProcess(reset, null, active);
  }
  
  /**
   * Generate clocked process.
   * <p>
   * Handles 
   * <ul>
   *   <li> {@link Module#CLOCK},  
   *   <li> {@link Module#ENABLE} signals.
   * </ul>
   * 
   * @param  active to be executed independent from {@link Module#RESET} and {@link Module#START}.
   * @return Verilog code
   */
  protected String getClockedProcess(String active) {
    return getClockedProcess(null, null, active);
  }
  
  /**
   * Generate implementation of cycle counter.
   * <p>
   * Declares a {@code cycle} signal and a counter starting on {@link #START} and counting from 0 to latency-1.
   * 
   * @return Verilog code
   */
  protected String getCycleCounter() {
    return "reg [" + (bitwidth(getLatency())-1) + ":0] cycle;\n" +
            getClockedProcess(
                "cycle <= 0;",
                "if (cycle == " + (getLatency()-1) + ") begin\n"    +
                "  cycle <= 0;\n"                                   +
                "end else if (" + START + " || cycle > 0) begin\n" +
                "  cycle <= cycle + 1;\n"                           +
                "end");
  }
  
  
  /**
   * Generate shiftRegister for operand input.
   * <p>
   * The buffered operand can be accessed via wire {@code OP_X_I_buffered}.
   * 
   * @param index       the operand to be buffered
   * @param firstAccess the cycle, in which the (buffered) operand is read first
   * @param lastAccess  the cycle, in which the (buffered) operand is read last 
   * @return
   */
  protected String getOperandBuffer(int index, int firstAccess, int lastAccess) {
    StringBuilder res  = new StringBuilder();
    
    Format f          = getOperandFormat(index);
    int    bw         = f.getBitWidth();
    int    readyCycle = ioLatency(bw) - 1;

    // sanity checks
    if (firstAccess > lastAccess) throw new IllegalArgumentException("firstAccess > lastAccess");
    if (firstAccess < readyCycle) throw new IllegalArgumentException("firstAccess < input latency");
    
    boolean keep        = lastAccess  >  readyCycle;  // even the most significant bits must be buffered
    boolean early       = firstAccess == readyCycle;  // extra multiplexer to assemble operand from port and buffer
                                                      // strict correlations: !keep => early and !early => keep
                                                      
    int     pw          = getOperandPortWidth(index);
    int     highBits    = bw % pw;
    int     bufferWidth = keep ? bw : pw * readyCycle;
    String  opName      = getOperandPort(index).toString();
    String  accessName  = opName + "_buffered";
    String  bufferName  = early ? (opName + "_buffer") : accessName;

    if (early) {
    res.append("wire " + bitRange(bw) + " " + accessName + ";\n");
  }
    
    // just in case ... (this method should not be called for single cycle input that is not required after first cycle)
    if (readyCycle == 0 && !keep) {
      res.append("assign " + accessName + " = " + opName + ";\n\n");
      return res.toString();
    }
    
    // the buffer
    res.append("reg  " + bitRange(bufferWidth) + " " + bufferName + ";\n");
    res.append("always @(posedge " + CLOCK + ") begin\n");
    
    // for single cycle input, that is accessed after first cycle
    if (readyCycle == 0 && keep) {
      res.append("  if (cycle == 0) begin\n");
      res.append("    " + bufferName + " <= " + opName + ";\n");
      res.append("  end\n");
      res.append("end\n");
      if (early) res.append("assign " + accessName + " = cycle > 0 ? " + bufferName + " : " + opName + ";\n\n");
      return res.toString();
    }
    
    // for multi cycle input
    if (keep) {
      if (highBits > 0) {
        res.append("  if (cycle == " + readyCycle + ") begin\n");
        res.append("    " + bufferName + " <= {" + opName     + bitRange(highBits) +
                                             "," + bufferName + bitRange(bufferWidth-1, highBits) + "};\n");
        res.append("  end else");
      } else {
        res.append("  ");
      }
      res.append("if (cycle <");
      if (highBits == 0) res.append("=");
      res.append(" " + readyCycle + ") begin\n");
      res.append("  ");
    }
    res.append("  " + bufferName + " <= ");
    if (pw < bufferWidth) res.append("{");
    res.append(opName);
    if (pw < bufferWidth) res.append("," + bufferName + bitRange(bufferWidth-1, pw) + "}");
    res.append(";\n");
    if (keep) res.append("  end\n");
    res.append("end\n");
    
    // the access signal (required also for keep=true to ensure access in last input cycle)
    if (early) {
      res.append("assign " + accessName + " = ");
      if (keep) res.append("(cycle > " + readyCycle + ") ? " + bufferName + " : ");
      res.append("{" + opName);
      if (highBits > 0) res.append(bitRange(highBits));
      res.append("," + bufferName);
      if (keep) res.append(bitRange(bufferWidth-1, highBits > 0 ? highBits : pw));
      res.append("};\n");
    }
    
    res.append("\n");
    return res.toString();
  }
  
  /**
   * Generate shiftRegister for result output.
   * <p>
   * The output must be written via signal {@code RESULT(X)_O_generated}. The most significant bits are presented at the
   * result port in the last {@code Operator} cycle.
   *  
   * @param index          the result to be buffered
   * @param generateCycle  the cycle, in which the output is generated
   * @param wire           true, if {@code RESULT(X)_O_generated} should be a wire (otherwise it will be a reg) 
   * @return Verilog code
   */
  protected String getResultBuffer(int index, int generateCycle, boolean wire) {
    StringBuilder res        = new StringBuilder();
    Format f                = getResultFormat(index);
    int    bw               = f.getBitWidth();
    int    firstOutputCycle = getLatency() - ioLatency(bw);
    
    // sanity checks
    if (firstOutputCycle < generateCycle) {
      throw new IllegalArgumentException("result produced to late (in cycle " + generateCycle + ")");
    }
    
    int    portWidth   = getResultPortWidth(index);
    int    bufferWidth = bw - (firstOutputCycle == generateCycle ? portWidth : 0);
    String resName     = getResultPort(index).toString();
    String bufferName  = resName + "_buffer";
    String accessName  = resName + "_generated";

    res.append((wire ? "wire " : "reg  ") + bitRange(bw) + " " + accessName + ";\n");
    
    // just in case ... (this method should not be called for single cycle output)
    if (generateCycle == getLatency()-1) {
      res.append("assign " + resName + " = " + accessName + ";\n\n");
      return res.toString();
    }
    
    // the buffer
    res.append("reg  " + bitRange(bufferWidth) + " " + bufferName + ";\n");
    res.append("always @(posedge " + CLOCK + ") begin\n");
      res.append("  if (cycle == " + generateCycle + ") begin\n");
      res.append("    " + bufferName + " <= " + accessName);
      if (generateCycle == firstOutputCycle) res.append(bitRange(bw-1,portWidth));
      res.append(";\n");
      if (bufferWidth > portWidth) {
        res.append("  end else if (cycle >= " + firstOutputCycle + ") begin\n");
        res.append("    "+bufferName+" <= {"+portWidth+"'d0,"+bufferName + bitRange(bufferWidth-1,portWidth) +"};\n");
      }
      res.append("  end\n");
      res.append("end\n");
      
    // sequential output (with multiplexer, if least significant word has to be presented at port immediately)
    StringBuilder gen = new StringBuilder();
    if (generateCycle == firstOutputCycle) {
      gen.append("cycle == " + generateCycle + " ? " + accessName + bitRange(portWidth) + " : ");
    }
    gen.append(bufferName);
    if (bufferWidth > portWidth) {
    gen.append(bitRange(portWidth));
  }
    res.append(getResultPort(index).getAssignment(gen.toString()) + "\n");
    
    return res.toString();
  }
  
  protected String getResultBuffer(int index, int generateCycle) {
    return getResultBuffer(index, generateCycle, true);
  }
    
  
  /**
   * Name of Verilog testbench module.
   * 
   * @return name of testbench
   */
  public String getTestbenchName() {
    return "tb_" + getName();
  }
  
  /**
   * Generate Verilog module implementing a testbench for the {{@code operator}.
   * <p>
   * This method instantiates the unit under test and drives the signals required for sequential execution. The actual
   * test is generated by {@link #getTest}.
   * 
   * @return Verilog code ready to be dumped to a text file. 
   */
  public String getTestbench() {
    StringBuilder res = new StringBuilder();
    res.append("`default_nettype none\n");
    res.append("`timescale 1 ns / 10 ps\n\n");
    res.append("module " + getTestbenchName() +";\n");
    
    // declare local signals
    List<Port> ports        = getPorts();
    List<Port> localSignals = ports;
    if (!isMultiCycle()) {
      localSignals = getSequentialPorts();
      localSignals.addAll(ports);
    }
    for (Port p : localSignals) {
      res.append("  ");
      res.append(p.getType() == Type.IN ? "reg " : "wire");
      res.append(" ");
      res.append(p.getWidthDeclaration());
      res.append(" ");
      res.append(p);
      res.append(";\n");
    }
    res.append("  integer i;\n");
    res.append("\n");
    
    // drive clock, reset, enable and opcode
    res.append("  initial         " + CLOCK + " = 1;\n");
    res.append("  always  #" + (CLOCK_PERIOD/2) + "    " + CLOCK + " = ~" + CLOCK + ";\n");
    res.append("  initial       " + RESET + " = 1;\n");
    res.append("  initial #" + (2*CLOCK_PERIOD) + " " + RESET + " = 0;\n");
    if (target.Processor.Instance.isStallable()) res.append("  initial      " + ENABLE + " = 1;\n");
    res.append("  initial       " + START + " = 0;\n\n");
    
    // instantiate unit under test
    HashMap<String, String> wires = new HashMap<String, String>();
    for (Port p : ports)  {
      System.out.println(p);
      wires.put(p.toString(), p.toString());
    }
    res.append(indent("  ", getInstance("uut", wires)));
    res.append("\n");
    
    // sequential testing
    res.append(indent("  ", getTest()));
    res.append("\nendmodule");
    return res.toString();
  }
  
  /**
   * Get required testbench execution time.
   * 
   * @return testbench execution time in ns
   */
  protected double getSimulationDuration() {
    return CLOCK_PERIOD * (getLatency() * getTestRuns() + 5);
  }
  
  /**
   * Generate TCL script for Modelsim waveform definition.
   * <p>
   * Used to visualize testbensh simulation. 
   * 
   * @return TCL code ready to be dumped into text file
   */
  public String getWaveTCL() {
    StringBuilder res = new StringBuilder();
    res.append("onerror {resume}\n");
    res.append("quietly WaveActivateNextPane {} 0\n");
    
    for (Port p : getPorts()) res.append(addWaveTCL(p));
    
    res.append("TreeUpdate [SetDefaultTree]\n");
    res.append("configure wave -namecolwidth 120\n");
    res.append("configure wave -valuecolwidth 90\n");
    res.append("configure wave -justifyvalue left\n");
    res.append("configure wave -signalnamewidth 1\n");
    res.append("configure wave -snapdistance 10\n");
    res.append("configure wave -datasetprefix 0\n");
    res.append("configure wave -rowmargin 4\n");
    res.append("configure wave -childrowmargin 2\n");
    res.append("configure wave -gridoffset 0\n");
    res.append("configure wave -gridperiod 1\n");
    res.append("configure wave -griddelta 40\n");
    res.append("configure wave -timeline 0\n");
    res.append("configure wave -timelineunits ns\n");
    res.append("update\n");
    res.append("WaveRestoreZoom {0 ns} {" + getSimulationDuration() + " ns}\n");
    return res.toString();
  }
  
  /**
   * Generate a TCL command used to display a {@code Port}.
   *  
   * @param p the {@link Port} to display
   * @return TCL code
   */
  protected String addWaveTCL(Port p) {
    return "add wave -noupdate -radix " + p.getWaveRadix() + " /" + getTestbenchName() + "/" + p + "\n";
  }
  
  /**
   * Get TCL script to initialize the Modelsim simulation.
   * 
   * @return TCL code ready to be dumped into text file
   */
  public String getSimTCL() {
    StringBuilder res = new StringBuilder();
    res.append("if {![file exists " + WORK_LIB + "/_info]} {vlib " + WORK_LIB + "}\n");
    res.append("vmap work " + WORK_LIB + "\n");
    res.append("vlog -O0 -novopt " + getName() + ".v\n");
    res.append("vlog -O0 -novopt " + getTestbenchName() + ".v\n");
    res.append("vsim -msgmode both -displaymsgmode both " + getTestbenchName() + "\n");
    res.append("do " + getTestbenchName() + ".wave\n");
    res.append("add log -r /*\n");
    res.append("run " + getSimulationDuration() + "ns\n");
    return res.toString();
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