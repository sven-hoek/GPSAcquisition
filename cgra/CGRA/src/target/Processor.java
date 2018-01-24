package target;

import java.io.File;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;

import cgramodel.CgraModel;
import cgramodel.PEModel;
import generator.VerilogGenerator;
import io.AttributeParser;
import io.AttributeWriter;
import operator.Implementation;
import operator.Operator;

/**
 * Common interface for all target processors.
 * <p>
 * To select the {@link Processor} to be addressed, just set
 * <ul>
 *   <li> {@code Processor.Instance = Amidar.Instance}     to work with the {@link Amidar} settings
 *   <li> {@code Processor.Instance = UltraSynth.Instance} to work with the {@link UltraSynth} settings
 * </ul>
 * Currently, the {@link Processor}-specific aspects comprise
 * <ul>
 *   <li>the supported {@link Operator}s
 *   <li>the supported data path width 
 * </ul>
 * Other options may follow such as
 * <ul>
 *   <li> details of the top level module
 * </ul>
 * 
 * @param T the enumeration implementing the {@link Operator} interface 
 * @author Andreas Engel [engel@esa.cs.tu-darmstadt.de] <p> Dennis Wolf [wolf@rs.tu-darmstadt.de]
 */
public abstract class Processor<T extends Enum<T> & Operator> {
  
  /**
   * The {@code Processor} targeted by the CGRA library.
   * <p>
   * Defaults to {@link Amidar}.
   */
  public static Processor<?> Instance = Amidar.Instance;
  
  
  /**
   * Runtime type of the generic Enumeration.
   */
  protected Class<T> operatorEnum;
  
  /**
   * Hidden constructor.
   * 
   * @param operatorEnum the runtime type of the generic Enumeration
   */
  protected Processor(Class<T> operatorEnum) {
    this.operatorEnum = operatorEnum;
    
    String currentDir = new File(System.getProperty("user.dir")).getName();
    if (currentDir.matches("CGRA|Metaheuristics|Teacher|ConstructiveHeuristic|NeuralNetwork")) {
      activeProject = ProjectType.GENERIC;
    } else if (currentDir.matches(this.getClass().getSimpleName())) {
      activeProject = ProjectType.PROCESSOR;
    } else if (currentDir.matches("cgra")) {
      activeProject = ProjectType.GENERIC_BASE;
    }
  }
  
/*
 * Processor specific operators
 **********************************************************************************************************************/

  
  /**
   * Get the {@code Operator} enumeration this {@code Processor} is based on.
   * 
   * @return class of {@link Operator} enumeration
   */
  public Class<T> getOperationType (){
	  return operatorEnum;
  }
  
  /**
   * Get the list of all {@code Operator}s supported by this {@code Processor}.
   * 
   * @return the list of all supported {@link Operator}s
   */
  public T[] getOperators() {
    return operatorEnum.getEnumConstants();
  }
  
  public List<T> getNonNativeOperators() {
    LinkedList<T> res = new LinkedList<T>();
    for (T op : getOperators()) if (!op.createDefaultImplementation().isNative()) res.add(op);
    return res;
  }
  
  public List<T> getNativeOperators() {
    LinkedList<T> res = new LinkedList<T>();
    for (T op : getOperators()) if (op.createDefaultImplementation().isNative()) res.add(op);
    return res;
  }
  
  public List<T> getImplementedOperators() {
    LinkedList<T> res = new LinkedList<T>();
    for(T op : getOperators()) if(op.hasImplementation()) res.add(op);
    return res;
  }
  
  /**
   * Get a specific {@code Operator} of the this {@code Processor}.
   * @param name the name or the requested {@link Operator}.
   * @return the requested {@link Operator} or null, if {@code name} does not specify a valid {@link Operator}
   */
  public T getOperatorByName(String name) {
    for (T operator : getOperators()) {
      if (operator.name().equals(name)) return operator;
    }
    return null;
  }
  
  /**
   * Get the NOP {@code Operator} of this {@code Processor}.
   * @return the NOP {@link Operator}, or null, if this {@link Processor} does not provide a NOP operator
   */
  public T getNOP() {
    return getOperatorByName("NOP");
  }
  
  /**
   * Create an empty map from the {@code Operation}s of this {@code Processor} to their {@code Implementation}s.
   * <p>
   * The map is not filled, i.e., <code>createEmptyOperatorMap().size() == 0</code>.
   * 
   * @return an empty {@link EnumMap}
   */
  public EnumMap<T, Implementation> createEmptyOperatorMap() {
    return new EnumMap<T, Implementation>(operatorEnum);
  }
  
  /**
   * Create a map from the {@code Operation}s of this {@code Processor} to their default {@code Implementation}s.
   * <p>
   * The map is completely populated, i.e. <code>createDefaultOperatorMap().size() == getOperators().length</code>
   * 
   * @return a full {@link EnumMap}
   */
  public EnumMap<T, Implementation> createDefaultOperatorMap() {
    EnumMap<T, Implementation> map = createEmptyOperatorMap();
    for (T operator : getOperators()) {
      Implementation implementation = operator.createDefaultImplementation();
      if (implementation != null) map.put(operator, implementation);
    }
    return map;
  }
  
  /**
   * Insert a link from an {@code Operator} to its {@code Implementation} into an appropriate {@code EnumMap}.
   * <p>
   * This method is required, as the required casts and type checks are hard to realize without access to {@link T} and
   * {@link operatorEnum}. If the {@link Operator} is not supported by this {@link Processor}, the map remains 
   * unchanged.
   * 
   * @param map the map to add the link to
   * @param op  the {@link Operator} to add to {@code map}
   * @param imp the {@link Implementation} to be associated with {@code op}
   * @return    true, is the {@link Operator} was added to {@code map}
   */
  @SuppressWarnings("unchecked")
  public boolean add(EnumMap<? extends Operator, Implementation> map, Operator op, Implementation imp) {
    if (!op.getClass().equals(operatorEnum)) return false;
    ((EnumMap<T, Implementation>) map).put((T) op, imp);
    return true;
  }
  
/*
 * Interface for processor-specific features besides the operators
 **********************************************************************************************************************/

  
  /**
   * Get the maximum number of data path bits.
   * <p>
   * This is the maximum width of a register file access per cycle. For a value of 0, the data path width is adopted to 
   * the largest input or output of any {@link Operator} within a {@link PEModel}.
   *   
   * @return the maximum data path width
   */
  public abstract int getDataPathWidth();
  
  /**
   * Determine, whether this {@code Processor} may stall the {@code CGRA}.
   *   
   * @return the stallable flag
   */
  public abstract boolean isStallable();
  
  /**
   * Determine the number of clock cycles between the cache address valid cycle (exclusive) 
   * and the cache status known cycle (exclusive). The requested data is assumed to be available immediatly afterwards.
   *   
   * @return cache delay cycles
   */
  public int getCacheAccessDelay() {
    return 0;
  }
 
  
/*
 * Interface for processor-specific (de)serialization and verilog generation
 **********************************************************************************************************************/

//  /**
//  * The Technology in which the CGRA is supposed to be realized. 
//  */
//  public static Technology technology = Technology.FPGA; 
//  
//  public Technology getTechnology(){
//   return technology;
//  }
//  
//  public void setTechnology(Technology tech){
//   technology = tech;
//  }
  
  /**
   * Get the {@code VerilogGenerator} of this {@code Processor}.
   * 
   * @return RTL backend
   */
  abstract public VerilogGenerator getGenerator();
  
  /**
   * Get the AttributeParser of this {@code Processor}: Parser to read in an attribute description in JSON.
   * 
   * @return Parser to read in a JSON description 
   */
  abstract public AttributeParser getAttributeParser();
  
  /**
   * Get the AttributeWriter of this {@code Processor}: Parser to export a CgraModel in a JSON Format.
   * 
   * @return Writer to export a JSON description 
   */
  abstract public AttributeWriter getAttributeWriter();
  
  /**
   * Get a new Model for the processor.
   * @return CgraModel
   */
  abstract public CgraModel getEmptyCgraModel();
  
  
/*
 * Path helper interface (provide relative paths to certain directories depending on base path or current project)
 **********************************************************************************************************************/

  /**
   * Type of (eclipse) project defining the working directory for all relative path specifications.
   */
  private static enum ProjectType {
    /** Top level project for a specific {@link Processor} */
    PROCESSOR("../cgra/", "../"),

    /** Generic project for {@link Processor} building blocks and further analysis */
    GENERIC("../", "../../"),

    /** Collection of all generic sub projects */
    GENERIC_BASE("./", "../");

    /**
     * Relative path to root of CGRA git repository (as submodule inside toplevel processor repository)
     */
    public  String subBase;
    
    /**
     * Relative path to root of toplevel processor repository
     */
    public  String topBase;
    
    /**
     * Define the location of this {@cod ProjectType} within the git repositories 
     * @param subBase relative path to root of CGRA git repository
     * @param topBase relative path to root of toplevel processor repository
     */
    private ProjectType(String subBase, String topBase) {
      this.subBase = subBase;
      this.topBase = topBase;
    }
  }
  
  /**
   * Executed project (defines working directory)
   */
  protected ProjectType activeProject = null;
  
  /**
   * Check, whether this {@Processor} is used within a valid project.
   * @throws RuntimeException if {@link Processor#activeProject} is not valid for this {@link Processor}.
   */
  private void validateActiveProject() {
    if (activeProject == null) throw new RuntimeException(
        System.getProperty("user.dir") + " is no valid project path for " + this.getClass().getName()
    );
  }
  
  /**
   * Derive path relative to CGRA submodule
   * @param dir relative path starting at CGRA submodule
   * @return normalized relative path
   * @throws RuntimeException if {@link Processor#activeProject} is not valid for this {@link Processor}.
   */
  protected String getRelativeSubPath(String dir) {
    validateActiveProject();
    return util.Path.getRelativePath(".", activeProject.subBase + dir);
  }

  /**
   * Derive relative path to Verilog String templates for the data path.
   * <pre>{processor}/cgra/Verilog_templates_Datapath</pre>
   * @return normalized relative path
   * @throws RuntimeException if {@link Processor#activeProject} is not valid for this {@link Processor}.
   */
  public String getHardwareTemplatePathDataPath() {
    return getRelativeSubPath("Verilog_templates_Datapath");
  }

  /**
   * Derive relative path to Verilog String templates for the {@code Processor}.
   * <pre>{processor}/cgra/Verilog_templates_{processor}</pre>
   * @return normalized relative path
   * @throws RuntimeException if {@link Processor#activeProject} is not valid for this {@link Processor}.
   */
  public String getHardwareTemplatePathProcessorRelated(){
    return getRelativeSubPath("Verilog_templates_" + Instance.getClass().getSimpleName());
  }
  
  /**
   * Derive relative path to Verilog module output.
   * <pre>{processor}/cgra/CGRA/out</pre>
   * @return normalized relative path
   * @throws RuntimeException if {@link Processor#activeProject} is not valid for this {@link Processor}.
   */
  public String getHardwareDestinationPath() {
    return getRelativeSubPath("CGRA/out");
  }
  
  /**
   * Derive relative path to Verilog testbench output.
   * <pre>{processor}/cgra/CGRA/out</pre>
   * @return normalized relative path
   * @throws RuntimeException if {@link Processor#activeProject} is not valid for this {@link Processor}.
   */
  public String getTestbenchDestinationPath() {
    return getHardwareDestinationPath();
  }

  /**
   * Derive relative path to testbench driving source applications.
   * <pre>{processor}/../ExportedApps</pre>
   * @return normalized relative path
   * @throws RuntimeException if {@link Processor#activeProject} is not valid for this {@link Processor}.
   */
  public String getApplicationPath() {
    validateActiveProject();
    return util.Path.getRelativePath(".", activeProject.topBase + "ExportedApps");
  }
  
  /**
   * Derive relative path to temp outputs.
   * <pre>{processor}/cgra/CGRA/debugging</pre>
   * @return normalized relative path
   * @throws RuntimeException if {@link Processor#activeProject} is not valid for this {@link Processor}.
   */
  public String getDebuggingPath() {
    return getRelativeSubPath("CGRA/debugging");
  }
  
  /**
   * Derive relative path to CGRA configuration files.
   * <pre>{processor}/cgra/CGRA/config</pre>
   * @return normalized relative path
   * @throws RuntimeException if {@link Processor#activeProject} is not valid for this {@link Processor}.
   */
  public String getConfigurationPath() {
    return getRelativeSubPath("CGRA/config");
  }

  /**
   * Derive relative path to temp outputs from the working directory of the simulator
   * @return normalized relative path
   * TODO verify
   */
  public String getDebuggingPathFromHDLSimulationPath() {
    return "debugging"; 
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
