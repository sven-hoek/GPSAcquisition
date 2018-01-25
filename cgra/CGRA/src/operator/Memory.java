package operator;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import accuracy.Format;

/**
 * Abstraction for all cached or read only {@code Memory} access operations.
 * <p>
 * Inside the ALU, the memory operations only provide some status signals (e.g., {@link STORE} is conditional}). 
 * The actual memory access (address fetch and calculation from register file, data read/write from/to register file)
 * is handled by PE-external logic (i.e., a memory controller and context status bits.).
 * However, the address (base and offset) and data (input or output) ports are modeled by this implementation, 
 * as this information is required by the graph scheduler.
 *
 * @author Andreas Engel [engel@esa.cs.tu-darmstadt.de]
 */
public abstract class Memory extends Implementation {
  
  /** Unique ID for (de)marshalling */
  private static final long serialVersionUID = 1701633066420366828L;
  
  /**
   * Default constructor required for demarshalling.
   */
  private Memory() {}
  
  
  /** Bitwidth of the memory access */
  protected int dataWidth;
  
  /** 
   * Flag showing a indexed memory access.
   * If set, the absolute memory access address is to be calculated as {@code base+offset*dataWidth/8}. 
   */
  protected boolean indexed;
  
  /**
   * Build a generic {@code Memory} access.
   *
   * @param base      the base address
   * @param offset    the address offset or array index
   * @param dataWidth the bitwidth of the accessed data 
   * @param indexed   if set, {@code offset} is an index
   */
  protected Memory(Format base, Format offset, int dataWidth, boolean indexed) {
    super(Arrays.asList(base, offset), Collections.emptyList());
    this.dataWidth = dataWidth;
    this.indexed   = indexed;
  }
  
  /**
   * Get access modification {@code Port}s signaling array of wide access
   * 
   * @return list of {@link Port}s
   * @see #MEM_ARRAY
   * @see #MEM_WIDE
   */
  protected List<Port> getAccessPorts() {
    LinkedList<Port> res = new LinkedList<Port>();
    if (isIndexedMemAccess()) res.add(MEM_ARRAY);
    int w = getWideMemAccessPortWidth();
    if (w > 0) res.add(new Port(MEM_WIDE, w));
    return res;
  }
  
  /**
   * Add {@code MREADY} instead of {@code RREADY} to handshake ports
   */
  @Override
  protected List<Port> getHandshakePorts() {
    LinkedList<Port> res = new LinkedList<Port>();
    res.add(START);
    res.add(MEM_VALID);
    return res;
  }
  
  /**
   * Cache ports are completely different from arithmetic ports.
   * 
   * @see #ENABLE
   * @see #START
   */
  @Override
  public List<Port> getPorts() {
    LinkedList<Port> res = new LinkedList<Port>();
    if (isMultiCycle()) res.addAll(getSequentialPorts());
                        res.add(CBOX);
                        res.add(COND);
                        res.addAll(getHandshakePorts());
                        res.addAll(getAccessPorts());
    return res;
  }
  
  /**
   * Reflect {#link #indexed} and {@link #dataWidth} in module name.
   */
  @Override
  public String getName() {
    return "CACHE_" + (isIndexedMemAccess() ? "ARRAY_" : "") + getClass().getSimpleName() + dataWidth;
  }

  @Override
  public boolean isCacheAccess() {
    return true;
  }
  
  @Override
  public boolean isIndexedMemAccess() {
    return indexed;
  }
  
  @Override
  public int getMemAccessWords() {
    int dpw = target.Processor.Instance.getDataPathWidth();
    return (dataWidth+dpw-1) / dpw;
  }
  
  @Override
  public String getMixedFormatImplementation() {
    StringBuilder res = new StringBuilder();
    
    // static assignments (will be multiplexed at ALU level)
    int words = getMemAccessWords();
    if (words > 1) res.append("assign " + MEM_WIDE + " = " + (words-1) + ";\n");
    
    // 
    
    // start signal is conditional for cache access
    String valid_in = START.toString();
    if (isCacheAccess()) {
      valid_in = "valid_in";
      res.append("wire " + valid_in + " = " + START + " & (!" + COND + " | " + CBOX + ");\n");
    }

    // start signal must be registered for multi-cycle operations
    if (isMultiCycle()) {
      res.append("reg valid_reg;\n");
      res.append(getClockedProcess(
          "valid_reg <= 0;", 
          "if (cycle == 0) valid_reg <= " + valid_in + ";\n" +
          "if (cycle == " + (getLatency()-1) + ") valid_reg <= 0;"
      ));
    }
      
    // mready is used for operator internal multiplexing => manage local copy
    res.append("wire mready = " + valid_in);
    if (isMultiCycle()) res.append(" | valid_reg");
    res.append(";\n");

    // internally multiplexed assignments
    res.append("assign " + MEM_VALID + " = mready;\n");
    if (isIndexedMemAccess()) res.append("assign " + MEM_ARRAY + " = mready;\n");
    
    return res.toString();
  }

  /**
   * Cache read access.
   * The whole {@link Processor} is stalled on a cache miss to ensure a fixed latency.
   */
  public static class LOAD extends Memory {

    /** Unique ID for (de)marshalling */
    private static final long serialVersionUID = 2481030716520137286L;

    /**
     * Default constructor required for demarshalling
     */
    @SuppressWarnings("unused")
    private LOAD() {}

    /**
     * Generate a cache read access.
     *
     * @param base      the base address
     * @param offset    the address offset
     * @param dataWidth the bitwidth of the accessed data 
     * @param indexed   if set, {@code offset} is an index
     */
    public LOAD(Format base, Format offset, int dataWidth, boolean indexed) {
      super(base, offset, dataWidth, indexed);
      resultFormat.add(new Format.Integer(dataWidth));
    }
    
    /**
     * The address is applied to the memory for every word to read.
     */
    @Override 
    public int getInputLatency() {
      return getOutputLatency();
    }
    
    /**
     * Cycles until result can be accessed.
     */
    protected int getSetupLatency() {
      return 1 +                                               // provide address
             target.Processor.Instance.getCacheAccessDelay();  // cache lookup
    }
    
    /**
     * Address operands do not influence the actual {@code Implementation}.
     */
    @Override
    public Format getCommonFormat() {
      return getResultFormat(0);
    }
    
    /**
     * Take cache access delay into account.
     */
    @Override 
    protected List<Integer> getSupportedMixedFormatLatency() {
      return new LinkedList<Integer>(Arrays.asList(getSetupLatency() + getOutputLatency()));
    }
  }
  
  /**
   * Non-blocking cache read access.
   * This prefetch handles caches misses by the control logic (signaling an invalid read) instead of 
   * stalling the whole {@link Processor}. The effective Operator latency is the same as for a {@code LOAD} operation.
   */
  public static class FETCH extends LOAD {

    
    /** Unique ID for (de)marshalling */
    private static final long serialVersionUID = -4016642843747427098L;
    
    /**
     * Default constructor required for demarshalling
     */
    @SuppressWarnings("unused")
    private FETCH() {}

    /**
     * Generate a cache prefetch.
     *
     * @param base      the base address
     * @param offset    the address offset
     * @param dataWidth the bitwidth of the accessed data 
     * @param indexed   if set, {@code offset} is an index
     */
    public FETCH(Format base, Format offset, int dataWidth, boolean indexed) {
      super(base, offset, dataWidth, indexed);
    }
    
    @Override
    public boolean isCachePrefetch() {
      return true;
    }
    
    @Override
    public boolean isControlFlow() {
      return true;
    }
    
    /**
     * Add prefetch control signals
     */
    @Override
    public List<Port> getPorts() {
      List<Port> res = super.getPorts();
      res.add(CACHE_PREFETCH);
      res.add(CACHE_STATUS);
      res.add(STATUS_VALID);
      res.add(STATUS);
      return res;
    }
    
    /**
     * Signal prefetch and cache status.
     */
    @Override
    public String getMixedFormatImplementation() {
      return super.getMixedFormatImplementation() 
           + CACHE_PREFETCH.getAssignment("mready")                                      // internally multiplexed
           + STATUS        .getAssignment(CACHE_STATUS)                                  // multiplexed at ALU level
           + STATUS_VALID  .getAssignment("cycle == " + getSetupLatency() + " ? 1 : 0"); // status mux control signal
    }
  }
  
  /**
   * Uncached read (only) memory access.
   * The effective Operator latency is the same as for a {@code LOAD} operation.
   */
  public static class ROM extends LOAD {
    
    /** Unique ID for (de)marshalling */
    private static final long serialVersionUID = -5045602837682462763L;
    
    /**
     * Default constructor required for demarshalling
     */
    @SuppressWarnings("unused")
    private ROM() {}

    /**
     * Generate a ROM read.
     *
     * @param base      the base address
     * @param offset    the address offset
     * @param dataWidth the bitwidth of the accessed data 
     * @param indexed   if set, {@code offset} is an index
     */
    public ROM(Format base, Format offset, int dataWidth, boolean indexed) {
      super(base, offset, dataWidth, indexed);
    }
    
    /**
     * Force LOAD in ROM module name.
     */
    @Override
    public String getName() {
      return "ROM_" + (isIndexedMemAccess() ? "ARRAY_" : "") + "LOAD" + dataWidth;
    }
    
    @Override
    public boolean isCacheAccess() {
      return false;
    }
    
    @Override
    public boolean isRomAccess() {
      return true;
    }
    
    /**
     * ROM ports are completely different from cache ports.
     * 
     * @see #ENABLE
     * @see #START
     */
    @Override
    public List<Port> getPorts() {
      LinkedList<Port> res = new LinkedList<Port>();
      if (isMultiCycle()) res.addAll(getSequentialPorts());
                          res.addAll(getHandshakePorts());
                          res.addAll(getAccessPorts());
      return res;
    }
    
    /**
     * Output latency determines overall latency, but initial cycle is required to apply the first address.
     */
    @Override 
    protected List<Integer> getSupportedMixedFormatLatency() {
      return new LinkedList<Integer>(Arrays.asList(1+getOutputLatency()));
    }
  }

  /**
   * (Conditional) cache write access.
   */
  public static class STORE extends Memory {

    /** Unique ID for (de)marshalling */
    private static final long serialVersionUID = -3281783860031849218L;

    /**
     * Default constructor required for demarshalling
     */
    @SuppressWarnings("unused")
    private STORE() {}

    /**
     * Generate a cache store access.
     *
     * @param base      the base address
     * @param offset    the address offset
     * @param dataWidth the bitwidth of the accessed data 
     * @param indexed   if set, {@code offset} is an index
     */
    public STORE(Format base, Format offset, int dataWidth, boolean indexed) {
      super(base, offset, dataWidth, indexed);
      operandFormat.add(new Format.Integer(dataWidth));
    }
    
    @Override
    public boolean isStore() {
      return true;
    }
    
    /**
     * Add cache write signals
     */
    @Override
    public List<Port> getPorts() {
      List<Port> res = super.getPorts();
      res.add(CACHE_WRITE);
      return res;
    }
    
    /**
     * Input latency determines overall latency.
     */
    @Override 
    protected List<Integer> getSupportedMixedFormatLatency() {
      return new LinkedList<Integer>(Arrays.asList(getInputLatency()));
    }
    
    /**
     * Address operands do not influence the actua l{@code implementation}.
     */
    @Override
    public Format getCommonFormat() {
      return getOperandFormat(2);
    }
    
    /**
     * Signal write access.
     */
    @Override
    public String getMixedFormatImplementation() {
      return super.getMixedFormatImplementation() + "assign " + CACHE_WRITE + " = mready;\n";
    }
  }

}

/*
 * Copyright (c) 2016, Embedded Systems and Applications Group, Department of
 * Computer Science, TU Darmstadt, All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of the
 * institute nor the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE INSTITUTE AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE INSTITUTE OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 **********************************************************************************************************************/