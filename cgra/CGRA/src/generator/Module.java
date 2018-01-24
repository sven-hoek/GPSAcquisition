package generator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import generator.Module.Port.Type;

/**
 * Representation of a Verilog module with a name and a list of {@code Port}s.
 * 
 * @author Andreas Engel [engel@esa.cs.tu-darmstadt.de]
 */
public interface Module {
  
  /*
   * Sequential ports
   ********************************************************************************************************************/
  
  /** Clock for sequential operators */
  public static final Port CLOCK  = new Port(Type.IN,  "CLK",              "synchronous clock");
  
  /** Synchronous reset (high active) */
  public static final Port RESET  = new Port(Type.IN,  "RST",              "synchronous reset (high active)");
  
  /** Clock enable (high active) */
  public static final Port ENABLE = new Port(Type.IN,  "EN",               "clock enable (high active)");
  
  /** Inputs valid (high active) */
  public static final Port START  = new Port(Type.IN,  "VALID",            "inputs valid (high active)");
  
  /** ID of selected operation */
  public static final Port OPCODE = new Port(Type.IN,  "OPCODE",           "id of selected operation");
  
  
  /*
   * Arithmetic ports
   ********************************************************************************************************************/
  
  /** Arithmetic operand */
  public static final Port OPA          = new Port(Type.IN,  "OP_A",         "arithmetic operand");
  
  /** Arithmetic operand */
  public static final Port OPB          = new Port(Type.IN,  "OP_B",         "arithmetic operand");
  
  /** Arithmetic operand */
  public static final Port OPC          = new Port(Type.IN,  "OP_C",         "arithmetic operand");
  
  /** Result valid (high active) */
  public static final Port RESULT_VALID = new Port(Type.OUT, "RESULT_VALID", "result valid (high active)");

  /** Arithmetic result */
  public static final Port RESULT       = new Port(Type.REG, "RESULT",       "arithmetic result");
  
  
  /*
   * Controlflow ports
   ********************************************************************************************************************/
  
  /** High active status valid (high active) */
  public static final Port STATUS_VALID = new Port(Type.OUT, "STATUS_VALID", "status valid (high active)");
  
  /** Control flow decision to CBOX */
  public static final Port STATUS       = new Port(Type.OUT, "STATUS",       "control flow decision to CBOX");

  /** Cache access is conditional (high active) */
  public static final Port COND         = new Port(Type.IN,  "CONDITIONAL",  "cache access is conditional (high active)");
  
  /** Cache access condition (high active) */
  public static final Port CBOX         = new Port(Type.IN,  "CBOX",         "cache access condition (high active)");
  
  
  /*
   * Memory ports (ROM and Cache)
   ********************************************************************************************************************/
  
  
  /** Memory signals valid (high active) */
  public static final Port MEM_VALID      = new Port(Type.OUT, "MEM_VALID",          "memory signals valid (high active)"); 
  
  /** Indexed memory access (high active) */
  public static final Port MEM_ARRAY      = new Port(Type.OUT, "MEM_ARRAY_ACCESS",   "indexed memory access (high active)");
  
  /** Additional memory access words */
  public static final Port MEM_WIDE       = new Port(Type.OUT, "MEM_WIDE_ACCESS",    "additional memory access words");
  
  /** Cache read succeeded (high active) */
  public static final Port CACHE_STATUS   = new Port(Type.IN,  "CACHE_STATUS",       "cache read succeeded (high active)"); 
  
  /** Cache read data */
  public static final Port CACHE_RD_DATA  = new Port(Type.IN,  "CACHE_DATA",         "cache read data"); 
  
  /** Cache write data */
  public static final Port CACHE_WR_DATA  = new Port(Type.OUT, "CACHE_DATA",         "cache write data"); 
  
  /** Cache address */
  public static final Port CACHE_ADDR     = new Port(Type.OUT, "CACHE_ADDR",         "`CACHE_ADDR_WIDTH", "cache address"); 

  /** Cache offset */
  public static final Port CACHE_OFFSET   = new Port(Type.OUT, "CACHE_OFFSET",       "`CACHE_ADDR_WIDTH", "cache offset"); 
  
  /** Cache signals valid (high active) */
  public static final Port CACHE_VALID    = new Port(Type.OUT, "CACHE_VALID",        "cache signals valid (high active)"); 
  
  /** Write to memory (high active) */
  public static final Port CACHE_WRITE    = new Port(Type.OUT, "CACHE_WRITE",        "write to memory (high active)"); 
  
  /** Cache prefetch (high active) */
  public static final Port CACHE_PREFETCH = new Port(Type.OUT, "CACHE_PREFETCH",     "cache prefetch (high active)");
  
  /** Additional cache access words */
  public static final Port CACHE_WIDE     = new Port(Type.OUT, "CACHE_WIDE_ACCESS",  "additional cache access words");
  
  /** Indexed cache access (high active) */
  public static final Port CACHE_ARRAY    = new Port(Type.OUT, "CACHE_ARRAY_ACCESS", "indexed cache access (high active)");

  /** ROM read data */
  public static final Port ROM_DATA       = new Port(Type.IN,  "ROM_DATA",           "rom read data"); 
  
  /** ROM address */
  public static final Port ROM_ADDR       = new Port(Type.OUT, "ROM_ADDR",           "rom address"); 

  /** ROM offset */
  public static final Port ROM_OFFSET     = new Port(Type.OUT, "ROM_OFFSET",         "rom offset"); 

  /** ROM signals valid (high active) */
  public static final Port ROM_VALID      = new Port(Type.OUT, "ROM_VALID",          "rom signals valid (high active)"); 
  
  /** Additional ROM access words */
  public static final Port ROM_WIDE       = new Port(Type.OUT, "ROM_WIDE_ACCESS",    "additional rom access words");
  
  /** Indexed ROM access (high active) */
  public static final Port ROM_ARRAY      = new Port(Type.OUT, "ROM_ARRAY_ACCESS",   "indexed rom access (high active)");
  

  /*
   * Regfile ports
   ********************************************************************************************************************/
  
  public static final Port RD_DIRECT_ADDR = new Port(Type.IN,  "RD_PORT_DIRECT_ADDR");
  public static final Port RD_DIRECT      = new Port(Type.OUT, "RD_PORT_DIRECT");
  public static final Port RD_MEM_ADDR    = new Port(Type.IN,  "RD_PORT_MEM_ADDR");
  public static final Port RD_MEM         = new Port(Type.OUT, "RD_PORT_MEM");
  public static final Port RD_MUX_ADDR    = new Port(Type.IN,  "RD_PORT_MUX_ADDR");
  public static final Port RD_MUX         = new Port(Type.OUT, "RD_PORT_MUX");
  public static final Port WR_EN          = new Port(Type.IN,  "WR_PORT_EN");
  public static final Port WR_ADDR        = new Port(Type.IN,  "WR_PORT_ADDR");
  public static final Port WR_DATA        = new Port(Type.IN,  "WR_PORT_DATA");

  /*
   * Context ports
   ********************************************************************************************************************/
  
  /** local variable write address and context counter */
  public static final Port CCNT             = new Port(Type.IN, "CCNT",             "local variable write address and context counter");
  
  /** local variable write enable (high active) */
  public static final Port LV_CONTEXT_WR_EN = new Port(Type.IN, "LV_CONTEXT_WR_EN", "local variable write enable (high active)");
  
  /** local variable write data */
  public static final Port LV_DATA          = new Port(Type.IN, "LV_DATA",          "local variable write data");
  
  /** context write address */
  public static final Port CONTEXT_WR_ADDR  = new Port(Type.IN, "CONTEXT_WR_ADDR",  "context write address");
  
  /** context write enable (high active) */
  public static final Port CONTEXT_WR_EN    = new Port(Type.IN, "CONTEXT_WR_EN",    "context write enable (high active)");
  
  /** context write data */
  public static final Port CONTEXT_DATA     = new Port(Type.IN, "CONTEXT_DATA",     "context write data");
  
  /*
   * PE ports
   ********************************************************************************************************************/
  
  /** decision from cbox */
  public static final Port PREDICATION      = new Port(Type.IN,  "PREDICATION",         "decision from cbox");

  /** additional input to regfile */
  public static final Port LIVE_IN          = new Port(Type.IN,  "LIVE",                "additional input to regfile");

  /** additional output from alu_a */
  public static final Port LIVE_OUT         = new Port(Type.OUT, "LIVE", "`DATA_WIDTH", "additional output from alu_a");

  /** direct output from regfile */
  public static final Port DIRECT_OUT       = new Port(Type.OUT, "DIRECT",              "direct output from regfile");
  

  class Net {

    /** The value of the {@link #id} field when unused */
    final public static int NO_ID = -1;

    /** {@code Net} name without direction suffix */
    protected String name;

    /** {@code Net} bit width as absolute integer or parameter string */
    protected Object width;

    /** optional {@code Net} usage description */
    protected String comment = "";

    /** optional ID of a {@code Net} */
    private int id = NO_ID;

    public Net(String name, Object width) {
      this.name = name;
      this.width = width;
    }

    public Net(String name, String comment) {
      this.name = name;
      this.width = 1;
      this.comment = comment;
    }

    public Net(String name, Object width, String comment) {
      this(name, width);
      this.comment = comment;
    }

    public Net(String name, Object width, String comment, int id) {
      this(name, width, comment);
      this.id = id;
    }

    /**
     * @return {@link Port} name without direction suffix
     */
    public String getName() {
      return name;
    }

    /**
     * @return Verilog code for bit width declaration
     */
    public String getWidthDeclaration() {
      if (width instanceof Integer) {
        int w = (Integer) width;
        if (w == 1) return "";
        if (w >  1) return "[" + (w-1) + ":0]";
        throw new IllegalArgumentException("port width <= 0");

      } else {
        if (width != null) return "[" + width + "-1:0]";
        throw new IllegalArgumentException("port width <= 0");
      }
    }

    /**
     * @return The integer {@link #width} of this {@link Net} or -1 the {@link #width} is not
     *         represented by an integer.
     */
    public int getWidth() {
      return width instanceof Integer ? (int) width : -1;
    }

    /**
     * @return Modelsim format best suitable to display the {@link Port}
     */
    public String getWaveRadix() {
      return (width instanceof Integer) && ((Integer) width) == 1 ? "binary" : "hexadecimal";
    }

    /**
     * @return the {@link Port} usage description
     */
    public String getComment() {
      return comment;
    }

    /**
     * @return weather this Net has an associated ID
     */
    public boolean hasID() {
      return id != NO_ID && id >= 0;
    }

    /**
     * @return the ID of this net
     */
    public int getId() {
      return id;
    }
  }

  /*
   * Actual Port implementation
   ********************************************************************************************************************/
  
  /**
   * Representation of a Verilog module port.
   */
  public static class Port extends Net {
    
    /**
     * {@code Port} direction and buffering.
     */
    public static enum Type {
      /** Unregistered input.  */ IN ("input  wire", "_I"), 
      /** Unregistered output. */ OUT("output wire", "_O"),
      /**   Registered output. */ REG("output reg ", "_O");
      
      /** Verilog code for {@code Port} declaration */
      public final String declare;
      
      /** Common {@code Port} name suffix */
      public final String suffix;
      
      /**
       * Initialize a {@code Port} type.
       * 
       * @param declare Verilog code for {@code Port} declaration
       * @param suffix  Common {@code Port} name suffix
       */
      Type(String declare, String suffix) {
        this.declare = declare;
        this.suffix  = suffix;
      }
    }
    
    /** {@code Port} direction and buffering */
    protected Type type;

    /**
     * Initialize a {@code Port}.
     *
     * @param type    {@link Port} direction and buffering
     * @param name    {@link Port} name without direction suffix
     * @param width   {@link Port} bit width
     * @param comment {@link Port} usage description
     */
    public Port(Type type, String name, Object width, String comment) {
      super(name, width, comment, NO_ID);
      this.type = type;
    }
    
    /**
     * Initialize a {@code Port}.
     * 
     * @param type    {@link Port} direction and buffering
     * @param name    {@link Port} name without direction suffix
     * @param width   {@link Port} bit width
     * @param comment {@link Port} usage description
     */
    public Port(Type type, String name, Object width, String comment, int id) {
      super(name, width, comment, id);
      this.type = type;
    }
    
    /**
     * Initialize {@code Port} with empty comment.
     * 
     * @param type  {@link Port} direction and buffering
     * @param name  {@link Port} name without direction suffix
     * @param width {@link Port} bit width
     */
    public Port(Type type, String name, Object width) {
      this(type, name, width, null, NO_ID);
    }
    
    /**
     * Initialize single bit {@code Port}.
     * 
     * @param type    {@link Port} direction and buffering
     * @param name    {@link Port} name without direction suffix
     * @param comment {@link Port} usage description
     */
    public Port(Type type, String name, String comment) {
      this(type, name, 1, comment, NO_ID);
    }
    
    /**
     * Initialize single bit {@code Port} with empty comment.
     * 
     * @param type {@link Port} direction and buffering
     * @param name {@link Port} name without direction suffix
     */
    public Port(Type type, String name) {
      this(type, name, 1);
    }
    
    /**
     * Clone a {@code Port} with a specific width.
     * 
     * @param base  {@link Port} to clone
     * @param width {@link Port} bit width
     */
    public Port(Port base, Object width) {
      this(base.type, base.name, width, base.comment, NO_ID);
    }

    /**
     * Clone a {@code Port} with a specific width and id.
     *
     * @param base  {@link Port} to clone
     * @param width {@link Port} bit width
     */
    public Port(Port base, Object width, int id) {
      this(base.type, base.name, width, base.comment, id);
    }
    
    /**
     * Test equality by type and name (but not size and comment).
     */
    @Override
    public boolean equals(Object o) {
      if (!(o instanceof Port)) return false;
      Port p = (Port) o;
      return type == p.type && name.equals(p.name);
    }
    
    @Override
    public String toString() {
      return toString("");
    }
    
    /**
     * Append _id to {@code Port} name
     * @param id suffix
     */
    public String toString(int id) {
      return toString("_" + id);
    }
    
    /**
     * Append suffix to {@code Port} name
     * @param suffix
     */
    public String toString(String suffix) {
      return name + suffix + type.suffix;
    }
    
    /**
     * @return {@link Port} direction and buffering
     */
    public Type getType() {
      return type;
    }

    /**
     * Generate assignment depending on output {@code Port type}
     * @param expression the value to be assigned
     * @return
     */
    public String getAssignment(String expression) {
      if      (type == Type.IN) throw new IllegalArgumentException("can not assign to input " + this);
      return ((type == Type.OUT) ? "assign " : "always @* ") + this + " = " + expression + ";\n";
    }
    
    /**
     * Generate assignment depending on output {@code Port type}
     * @param p the {@link Port} to be assigned
     * @return
     */
    public String getAssignment(Port p) {
      return getAssignment(p.toString());
    }

    /**
     * Get a Verilog string representing this port when connected to the given wire.
     *
     * @param wire the wire to connect to
     * @param isLastInInstancePortList determines if this port is last in the printed module instance port list
     * @return the Verilog string representing the instance port
     */
    public String getInstanceString(Wire wire, boolean isLastInInstancePortList) {
      String instance =
              "."
              + (hasID() ? toString(getId()) : toString())
              + "(" + wire.toString(getWidth())
              + ")";

      if (isLastInInstancePortList)
        instance += "\n";
      else
        instance += ",\n";

      return instance;
    }
      
  }

  /**
   * Base class for {@link Port} and {@link Wire}
   */
  class Wire extends Net {
    public enum Type {
      /**
       * Default reg type
       */
      Reg("", "reg"),

      /**
       * Default wire type
       */
      Wire("w_", "wire"),

      /**
       * Represents a wired input port of the surrounding {@link Module}
       */
      InputPortWire("", "");

      public final String prefix;
      public final String name;

      Type(String prefix, String typeName) {
        this.prefix = prefix;
        this.name = typeName;
      }
    }

    private Type type;

    public Wire(Type type, String name, String comment) {
      super(name, comment);
      this.type = type;
    }

    public Wire(Type type, String name, int width, String comment) {
      super(name, width, comment);
      this.type = type;
    }

    public Wire(Wire other, Object width) {
      super(other.name, width, other.comment);
      this.type = other.type;
    }

    public Wire(Wire other, int width, int id) {
      super(other.name, width, other.comment, id);
      this.type = other.type;
    }

    public Wire(Wire other, int id) {
      super(other.name, other.width, other.comment, id);
      this.type = other.type;
    }

    public Wire(Port port) {
      super(port.getName(), port.getWidth(), port.getComment(), port.getId());
      this.type = Type.InputPortWire;
    }

    @Override
    public String toString() {
      return getFullName();
    }

    /**
     * String representation of the wire with indexed selection of {@code requiredWidth}
     * bits from the beginning of the wire.
     *
     * @param requiredWidth the with to select
     * @return the String representing the selected bits
     */
    public String toString(int requiredWidth) {
      if (requiredWidth <= getWidth()) {
        if (requiredWidth == getWidth())
          return getFullName();
        else
          return getFullName() + "[" + requiredWidth + "-1:0]";
      } else {
        throw new IllegalArgumentException("Wire is not wide enough");
      }
    }

    /**
     * @return the full name of the Wire, meaning all its components in one string
     */
    private String getFullName() {
      return type.prefix + name + (hasID() ? "_" + getId() : "") + (type == Type.InputPortWire ? "_I" : "");
    }

    /**
     * @return the complete declaration of the wire
     */
    public String getDeclaration() {
      return type.name + " " + getWidthDeclaration() + " " + getFullName();
    }
  }
  
  /*
   * Actual Module implementation
   ********************************************************************************************************************/
  
  
  /**
   * @return the name of the {@link Module}
   */
  public String getName();
  
  /**
   * @return the {@link Port}s of the {@link Module}.
   */
  public List<Port> getPorts();
  
  /**
   * Generate {@code Module} header
   * @return Verilog code.
   */
  default String getDeclaration() {
    
    // collect information
    LinkedList<String[]> lines = new LinkedList<String[]>();
    for (Port p : getPorts()) {
      String comment = p.getComment();
      lines.add(new String[] {
        p.getType().declare, p.getWidthDeclaration(), p.toString(), comment == null ? "" : "// " + comment
      });
    }
    
    // right align bit width and name
    for (int i : Arrays.asList(1,2)) {
      int max = 0;
      for (String[] line : lines) if (line[i] != null && line[i].length() > max) max = line[i].length();
      if (max == 0) continue;
      for (String[] line : lines) line[i] = String.format("%"+max+"s", line[i] == null ? "" : line[i]);
    }
    
    // concat all information
    StringBuilder res = new StringBuilder();
    res.append("module " + getName());
    if (lines.size() > 0) {
      res.append(" (\n");
      for (String[] line : lines) {
        line[line.length-2] += line == lines.getLast() ? " " : ",";
        res.append(" ");
        for (String s : line) {
          if (s == null) continue;
          res.append(" ");
          res.append(s);
        }
        res.append("\n");
      }
      res.append(");");
    }
    res.append("\n");
    
    return res.toString();
  }
  
  /**
   * Generate instantiation of this {@code module}
   * @param name  the instance identifier
   * @param wires the actually connected wires for each {@link Port} 
   * @return Verilog code
   */
  default String getInstance(String name, HashMap<String, String> wires) {
    
    // collect information
    LinkedList<String[]> lines = new LinkedList<String[]>();
    for (Port p : getPorts()) {
      String w = wires.get(p.toString());
      if (w == null) throw new IllegalArgumentException("Port " + p + " not assigned");
      lines.add(new String[] {"." + p.toString(), "(" + w + ")"});
    }
    
    // left align name
    for (int i : Arrays.asList(0)) {
      int max = 0;
      for (String[] line : lines) if (line[i] != null && line[i].length() > max) max = line[i].length();
      for (String[] line : lines) line[i] = String.format("%-"+max+"s", line[i] == null ? "" : line[i]);
    }
    
    // concat all information
    StringBuilder res = new StringBuilder();
    res.append(getName() + " " + name);
    if (lines.size() > 0) {
      res.append(" (\n");
      for (String[] line : lines) {
        res.append("  ");
        res.append(line[0]);
        res.append(" ");
        res.append(line[1]);
        if (line != lines.getLast()) res.append(",");
        res.append("\n");
      }
      res.append(");");
    }
    res.append("\n");
    
    return res.toString();
  }

  /**
   * Search the list of all ports for a port which is equal to the given port.
   * {@see {@link Port#equals(Object)}} on how equality is determined!
   *
   * @param port the port to search for
   * @return the found port or null if no equal port was found
   */
  default Port getPort(Port port) {
    for (Port p : getPorts()) {
      if (p.equals(port))
        return p;
    }
    return null;
  }
}

/*
 * Copyright (c) 2017, Embedded Systems and Applications Group, Department of
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