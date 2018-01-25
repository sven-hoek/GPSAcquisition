package generator;

import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import operator.Memory;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroupFile;

import com.sun.org.apache.bcel.internal.generic.GETFIELD;

import cgramodel.CBoxModel;
import cgramodel.CBoxModel.BranchSelectionConnection;
import cgramodel.CgraModel;
import cgramodel.ContextMaskCBoxEvaluationBlock;
import cgramodel.ContextMaskPE;
import cgramodel.PEModel;
import generator.Module.Port;
import operator.Implementation;
import operator.Operator;
import target.Processor;

/**
 * Root class for all generators. It constructs the data path of a CGRA, which is independent
 * of the the host processor. For each integration with a different host processor a new VerilogGenerator class
 * is to be created that is an heritage of this class. <br> The naming convention is VerilogGenerator<b>Hostprocessor</b>.
 * @author Dennis Wolf
 * @param <Technology>
 *
 */
public abstract class VerilogGenerator  {


	/**
	 * Model to be exported/converted into an Verilog description
	 */
	protected CgraModel model = null;

	protected String destinationFolder = "";

	/**
	 * Set of all generated files.
	 */
	protected Set<String> genFiles;
	
	/**
   * Generated {@code Module}s.
   * Map name to interface.
   */
  protected Map<String, Module> modules;
  

	/**
	 * Field to to store whether single Operator testbenches are to dumped as well.
	 */
	boolean dumpOperatorTestbench;

	/**
	 * Field that holds the date
	 */
	java.util.Date date;


	/**
	 * Set of all top level ports.
	 */
	Set<VerilogPort> toplevelports	= new HashSet<VerilogPort>();
	
	protected String initializationPath = null;


	/**
	 * Constructor to create an object
	 * @param model Composition to be exported
	 */
	public VerilogGenerator(CgraModel model){
		this();
		this.model = model;
	}

	/**
	 * Constructor to create an object
	 * @param model Composition to be exported
	 */
	public VerilogGenerator(){
		genFiles = new HashSet<String>();
	}


	/**
	 * Setter method for a composition {@link model}
	 * @param model
	 */
	public void setModel(CgraModel model){
		this.model = model;
	}

	/**
	 * Getter method for a composition {@link model} that is to be exported
	 * @param model
	 */
	public CgraModel getModel(){
		return model;
	}

	/**
	 * Container method to export a composition. Core method is {@link printVerilogDescription}.<br>
	 * 
	 * <b> Important : </b>
	 * Do not use this method unless you've used the method <i> finalize() </i> in CGRAModel.
	 * @param folder Destination folder where all Verilog files are to be stored
	 * @param model Model to exported
	 */
	public void printVerilogDescription(String folder, CgraModel model){
		this.model = model;
		destinationFolder = folder;
		printVerilogDescription();
	}

	/**
	 * Container method to export a composition. Core method is {@link printVerilogDescription}.<br>
	 * 
	 * <b> Important : </b>
	 * Do not use this method unless you've used the method <i> finalize() </i> in CGRAModel. 
	 * @param folder Destination folder where all Verilog files are to be stored
	 */
	public void printVerilogDescription(String folder){
		destinationFolder = folder;
		printVerilogDescription();
	}

	/**
	 * This method uses ancient String Template magic to generate Verilog Code that represents the given model.<br>
	 * 
	 * <b> Important : </b>
	 * Do not use this method unless you've used the method <i> finalize() </i> in CGRAModel.
	 *  
	 * @param model  Model of the CGRA
	 * @param fold  folder for export
	 */
	private void printVerilogDescription(){
		
		if(destinationFolder.isEmpty()){
			destinationFolder= target.Processor.Instance.getHardwareDestinationPath();
		}

		if(model == null){
			throw new IllegalArgumentException("The Cgra is null and therefore not ready for an export. Check"
					+ " all encodings and contextmasks");
		}
		if(!model.isFinalized()){
			throw new IllegalArgumentException("The Cgra is not finalized and therefore not ready for an export. Check"
					+ " all encodings and contextmasks");
		}
		File folder;		
		//		 checks and deletes for existing folder
		folder = new File(destinationFolder);
		if (folder.exists()) {
			String[] entries = folder.list();
			for (String s : entries) {
				File currentFile = new File(folder.getPath(), s);
				currentFile.delete();
			}
			folder.delete();
		}
		folder.mkdirs();
		date = Calendar.getInstance().getTime();

		prepare();
		addStaticFiles();
		
		modules = new HashMap<String, Module>();

		for (PEModel pe : model.getPEs()) {
			printOperators(    pe );
			printAluVerilogI(  pe );
			printRegisterFile( pe );
			printPE(           pe );
		}

		printHostProcessorRelatedModules();
		printCbox();
		printCCU();
		printROM();
		printTopLevel();

		printInformation();
		printDummy(model);
	}



	private void printCCU() {
		STGroupFile group = new STGroupFile(target.Processor.Instance.getHardwareTemplatePathDataPath() + "/Context_Control_Unit.stg", '§', '§');
		ST template = group.getInstanceOf("CCU");
		template.add("stallcapability", model.isStallable());
		template.add("branchselection", model.isConditionalJumps());
		if(initializationPath != null){
			template.add("initPath", initializationPath);
		}
		dump("Context_Control_Unit.v", template);		
		genFiles.add("Context_Control_Unit.v");
	}

	/**
	 * This method can be used for preparation steps if needed
	 */
	protected abstract void prepare();

	/**
	 * Method to print the top level module. This varies with the host processor and has to be generated dynamically.
	 */
	protected abstract void printTopLevel();


	/**
	 * There are some static Verilog files that don't need to be generated dynamically and can be copied. Currently
	 * the following files are static:
	 * 
	 * <p><ul>
	 * <li> ContextControlUnit
	 * </ul><p>
	 */
	protected void addStaticFiles(){
		
		Path source = Paths.get(target.Processor.Instance.getHardwareTemplatePathDataPath() + "/constraints.xdc");
		Path destination = Paths.get(destinationFolder + "/timing_constraints.xdc");
		try {
			Files.copy(source, destination);
		} catch (IOException e) {
			System.err.println("Error "+ e);
			System.err.println("IO Exception in addStaticFiles() with File " + source);
		}
	}

  /**
   * Method to generate the Verilog description of a single PE.
   * @param pe
   */
  private void printPE(PEModel pe) {
    
    // PE string template
    STGroupFile group = new STGroupFile(target.Processor.Instance.getHardwareTemplatePathDataPath() + "/PE_template.stg", '§', '§');
    ST template = group.getInstanceOf("PE");
    
    // some signal width settings
    int contextSize      = model.getContextMemorySize();
    int contextAddrWidth = (int) Math.ceil((Math.log(contextSize) / Math.log(2)));
    int contextWidth     = pe.getContextWidth();
    int outputWidth      = pe.getMaxWidthResult();
    int dataPathWidth    = target.Processor.Instance.getDataPathWidth();
    int predicationWidth = (int) Math.ceil((Math.log(model.getcBoxModel().getCBoxPredicationOutputs()/ Math.log(2))));
    
    // instantiated modules
    int id = pe.getID();
    Module alu     = modules.get("Alu"          + id);
    Module regfile = modules.get("Registerfile" + id);
    
    
    // some ALU ports have to be forwarded to PE ports directly
    List<Port> aluPorts = alu.getPorts();
    List<Port> regfilePorts = regfile.getPorts();
    List<Port> pePorts  = new LinkedList<Port>();
    
    // sequential ports
    pePorts.add(Module.CLOCK);
    for (Port p : Arrays.asList(Module.RESET, Module.ENABLE)) {
      if (aluPorts.contains(p) || regfilePorts.contains(p)) pePorts.add(p);
    }
    
    // controlflow ports
    if (aluPorts.contains(Module.STATUS)) pePorts.add(Module.STATUS);
    
    // context ports
    pePorts.add(new Port(Module.CCNT,             contextAddrWidth));
    pePorts.add(         Module.LV_CONTEXT_WR_EN);
    pePorts.add(new Port(Module.LV_DATA,          contextWidth));
    pePorts.add(new Port(Module.CONTEXT_WR_ADDR,  contextAddrWidth));
    pePorts.add(new Port(Module.CONTEXT_DATA,     contextWidth));
    pePorts.add(         Module.CONTEXT_WR_EN);
    
    // inputs from other PEs
    for (PEModel source : pe.getInputs()) {
      if (pe.getID() != source.getID()) {
        template.add("interconnect_inputs", true);
        pePorts.add(new Port(
            Port.Type.IN, 
            "INPUT_" + source.getID(), 
            source.getMaxWidthResult(),
            "input from PE" + source.getID()
        ));
      }
    }
    
    // cache ports
    boolean cache = aluPorts.contains(Module.CACHE_VALID);
    if (cache) {
      pePorts.add(new Port(Module.CACHE_RD_DATA, dataPathWidth)); // TODO: not required if only CACHE STORE in ALU
      pePorts.add(new Port(Module.CACHE_WR_DATA, outputWidth));
      pePorts.add(         Module.CACHE_ADDR);
      pePorts.add(         Module.CACHE_OFFSET);
      pePorts.add(         Module.CACHE_VALID);
      for (Port p : Arrays.asList(Module.CACHE_WRITE, Module.CACHE_ARRAY, Module.CACHE_PREFETCH, Module.CACHE_WIDE, Module.CACHE_STATUS)) {
        for (Port ap : aluPorts) if (ap.equals(p)) pePorts.add(ap);
      }
    }
    
    // rom ports
    boolean rom = aluPorts.contains(Module.ROM_VALID);
    if (rom) {
      pePorts.add(new Port(Module.ROM_DATA, dataPathWidth));
      pePorts.add(new Port(Module.ROM_ADDR, pe.getRomAddrWidth()));
      pePorts.add(new Port(Module.ROM_OFFSET, pe.getRomAddrWidth()));
      pePorts.add(         Module.ROM_VALID);
      for (Port p : Arrays.asList(Module.ROM_ARRAY, Module.ROM_WIDE)) {
        for (Port ap : aluPorts) if (ap.equals(p)) pePorts.add(ap);
      }
    }
    
    // PE specific ports
    if (pe.getLiveout()) pePorts.add(         Module.LIVE_OUT);
    if (pe.getLivein())  pePorts.add(new Port(Module.LIVE_IN,     dataPathWidth));
                         pePorts.add(new Port(Module.DIRECT_OUT,  outputWidth));
                         pePorts.add(new Port(Module.PREDICATION, predicationWidth));
    
    
    Module module = new Module() {
      @Override public String     getName()  {return "PE" + pe.getID();}
      @Override public List<Port> getPorts() {return pePorts;}
    };

    template.add("contextsize", contextSize);
    template.add("declaration", module.getDeclaration());
    template.add("cache",       cache);
    template.add("rom",         rom);
    template.add("pipelined",   model.isPipelined());
    template.add("stallcapable", model.isStallable());

    for(int i = 0; i < model.getcBoxModel().getCBoxPredicationOutputs(); i++) {
      ST caseTemplate = group.getInstanceOf("Case");
      caseTemplate.add("case", i);
      template.add("cases", caseTemplate.render());
    } 

    template.add("outputwidth", pe.getMaxWidthResult());
    if(pe.getMaxWidthInputA()>0){
      template.add("inputwidtha", pe.getMaxWidthInputA());
    }
    if(pe.getMaxWidthInputB()>0){
      template.add("inputwidthb", pe.getMaxWidthInputB());
    }
    template.add("liveout", pe.getLiveout());
    template.add("livein",  pe.getLivein());

    String aluA = "";
    String aluB = "";
    int counter = 0;
    for (PEModel source : pe.getInputs()) {
      if (pe.getID() != source.getID()) {
        aluA = aluA + counter + "  : w_alu_in_A ";
        if(model.isPipelined()){
          aluA = aluA + "<";
        }
        aluA = aluA + "= INPUT_" + source.getID() + "_I;\n";

        aluB = aluB + counter + "  : w_alu_in_B ";
        if(model.isPipelined()){
          aluB = aluB + "<";
        }
        aluB = aluB +"= INPUT_" + source.getID() + "_I;\n";
        counter++;
      }
    }
    aluA = aluA + counter + "  : w_alu_in_A ";
    if(model.isPipelined()){
      aluA = aluA + "<";
    }
    aluA = aluA +"= w_reg_to_operand_mux;\n";


    aluB = aluB + counter + "  : w_alu_in_B ";
    if(model.isPipelined()){
      aluB = aluB + "<";
    }

    aluB = aluB + "= w_reg_to_operand_mux;\n";
    counter++;
    if(model.isPipelined() && model.isRFBypass()){
      aluA = aluA + counter + "  : w_alu_in_A <= w_alu_Out;\n";
      aluB = aluB + counter + "  : w_alu_in_B <= w_alu_Out;\n";
      counter++;
    }
    if(model.isSecondRFOutput2ALU()){
      aluA = aluA + counter + "  : w_alu_in_A ";
      aluB = aluB + counter + "  : w_alu_in_B ";
      if(model.isPipelined()){
        aluB = aluB + "<";
        aluA = aluA + "<";
      }
      aluA = aluA + "= w_direct_o;\n";
      aluB = aluB + "= w_direct_o;\n";
    }

    template.add("muxA", aluA);
    template.add("muxB", aluB);
    
    // instantiate alu
    HashMap<String, String> wires = new HashMap<String, String>();
    for (Port p : alu.getPorts()) {
      String w = p.toString();
           if (p.equals(Module.OPA))    w = "w_alu_in_A";
      else if (p.equals(Module.OPB))    w = "w_alu_in_B";
      else if (p.equals(Module.OPCODE)) w = "w_opcode";
      else if (p.equals(Module.CBOX))   w = "predication_w";
      else if (p.equals(Module.COND))   w = "w_conditional_dma";
      else if (p.equals(Module.RESULT)) w = "w_alu_Out";
      wires.put(p.toString(), w);
    }
    template.add("aluInst", alu.getInstance("i_alu", wires));
    
    // instantiate regfile
    wires = new HashMap<String, String>();
    for (Port p : regfile.getPorts()) {
      String w = p.toString();
           if (p.equals(Module.RD_DIRECT_ADDR)) w = "w_directout_addr";
      else if (p.equals(Module.RD_DIRECT))      w = "w_direct_o";
      else if (p.equals(Module.RD_MUX_ADDR))    w = "w_rf_addr_operand_mux";
      else if (p.equals(Module.RD_MUX))         w = "w_reg_to_operand_mux";
      else if (p.equals(Module.WR_EN))          w = "w_write_enable_regfile";
      else if (p.equals(Module.WR_ADDR))        w = "w_write_addr";
      else if (p.equals(Module.WR_DATA))        w = "w_reg_in";
      else if (p.equals(Module.RD_MEM_ADDR))    w = "w_mem_addr";
      else if (p.equals(Module.RD_MEM))         w = "w_mem";
                
      wires.put(p.toString(), w);
    }
    template.add("regfileInst", regfile.getInstance("i_regfile", wires));
    
    printMask(template, pe);
    template.add("date", date);
    
    if(initializationPath != null){
      template.add("initPath", initializationPath+"/pe"+pe.getID()+".dat");
    }
    
    dump(module, template);
  }

	/**
	 * This class holds the option to print modules that are needed for the individual processors.
	 */
	abstract void printHostProcessorRelatedModules();

	/**
	 * The main purpose of this class is to determine the encoding of a Context for a PE. It is based on the 
	 * ContextMaskPE class. 
	 * @param template template to be filled
	 * @param pe The PE for which the encoding should be determined
	 */
	public void printMask(ST template, PEModel pe) {

		ContextMaskPE peMask = pe.getContext();
		template.add("opL", peMask.getOpL());
		template.add("opH", peMask.getOpH());
		template.add("muxRegL", peMask.getMuxRegL());
		template.add("muxRegH", peMask.getMuxRegH());
		template.add("muxRegwidth", peMask.getRegistermuxwidth());
		template.add("muxwidth", peMask.getMuxwidth());
		if(pe.getMaxWidthInputB()>0){
			template.add("muxBL", peMask.getMuxBL());
			template.add("muxBH", peMask.getMuxBH());
		}
		if(pe.getMaxWidthInputA()>0){
			template.add("muxAL", peMask.getMuxAL());
			template.add("muxAH", peMask.getMuxAH());
		}
		int regaddrwidth = peMask.getRegAddrWidthWrite();
		if(regaddrwidth >0){
			template.add("regaddrwidthWrite", regaddrwidth);
			template.add("regaddrwidthRead", peMask.getRegAddrWidthRead());


			template.add("wrL", peMask.getWrL());
			template.add("wrH", peMask.getWrH());

			if (pe.getMemAccess()) {
				template.add("rdCacheL", peMask.getRdCacheL());
				template.add("rdCacheH", peMask.getRdCacheH());
			}

			template.add("rddoL", peMask.getRddoL());
			template.add("rddoH", peMask.getRddoH());

			template.add("rdmuxL", peMask.getRdmuxL());
			template.add("rdmuxH", peMask.getRdmuxH());
		}
		template.add("wr_en", peMask.getWr_en());
		template.add("cond_wr", peMask.getCond_wr());
		if (pe.getMemAccess()) {
			template.add("cond_dma", peMask.getCond_dma());
		}
		if(peMask.getOpcodewidth() > 0){
			template.add("opcodewidth", peMask.getOpcodewidth());
		}
		if(peMask.getCBoxSelWidth()>0){
			template.add("cBoxSelWidth", peMask.getCBoxSelWidth());
			template.add("cBoxSelL", peMask.getCBoxSelL());
			template.add("cBoxSelH", peMask.getCBoxSelH());
		}
		template.add("contextwidth", peMask.getContextWidth());
	}
	
  
	
   
  /**
   * Generate or-aggregation statement for a list of drivers.
   * 
   * @param port    the target to write the aggregated value to
   * @param modules list of driver names
   * @param wire    the common infix of the driver signals
   * @return        aggregation statement
   */
  private static String getOr(Port port, String wire, LinkedList<String> modules) {
    if (modules.isEmpty())   return "";
    if (modules.size() == 1) return port.getAssignment("w_" + wire + "_" + modules.getFirst());
    
    StringBuilder res = new StringBuilder();
    String out = port.toString();
    if (port.getType() == Port.Type.REG) {
      out = "w_" + port.getName().toLowerCase();
      res.append("wire " + port.getWidthDeclaration() + " " + out + ";\n");
    }
    res.append("or(" + out); 
    for (String op : modules) res.append(", w_" + wire + "_" + op);
    res.append(");\n"); 
    if (port.getType() == Port.Type.REG) res.append(port.getAssignment(out));
    
    return res.toString();
  }

  /**
   * Generate multiplexer statements for a list of drivers.
   * 
   * @param port    the target to write the multiplexed value to
   * @param modules list of driver names
   * @param wire    the common infix of the driver signals
   * @param valid   the common infix of the one-hot selector signals
   * @return        multiplexer statement
   */
  private static String getMux(Port port, String wire, String valid, LinkedList<String> modules) {
    if (modules.isEmpty())   return "";
    if (modules.size() == 1) return port.getAssignment("w_" + wire + "_" + modules.getFirst());
    StringBuilder res = new StringBuilder();
    String out = port.toString();
    if (port.getType() == Port.Type.OUT) {
      out = "r_" + port.getName().toLowerCase();
      res.append("reg " + port.getWidthDeclaration() + " " + out + ";\n");
    }
    res.append("always@* begin\n"); 
    res.append("(* full_case *)\n"); 
    res.append("  casez({");
    for (String op : modules) res.append("w_" + valid + "_" + op + ",");
    res.setLength(res.length()-1);
    res.append("})\n"); 
    for (String op : modules) {
      res.append("    " + modules.size() + "'b");
      for (String m : modules) res.append(op == m ? "1" : "0");
      res.append(" : " + out + " = w_" + wire + "_" + op + ";\n");
    }
    res.append("  endcase\n");
    res.append("end\n");
    if (port.getType() == Port.Type.OUT) res.append(port.getAssignment(out));
    
    return res.toString();
  }

  private void printAluVerilogI(PEModel pe) {
    
    STGroupFile group = new STGroupFile(target.Processor.Instance.getHardwareTemplatePathDataPath() + "/Alu_template _interleaved.stg", '§', '§');
    ST template = group.getInstanceOf("Alu");
    
    // statistics accumulated over all actually implemented operators
    boolean isMultiCycle            = false;
    int     maxCacheWideAccessWidth = 0;
    int     maxRomWideAccessWidth   = 0;
    int     maxOpAWidth             = 0;
    int     maxOpBWidth             = 0;
    int     maxResultWidth          = 0;
    
    // collection of modules driving a certain toplevel output port
    LinkedList<String> cacheValidDriver       = new LinkedList<String>();
    LinkedList<String> cacheWriteDriver       = new LinkedList<String>();
    LinkedList<String> cachePrefetchDriver    = new LinkedList<String>();
    LinkedList<String> cacheArrayAccessDriver = new LinkedList<String>();
    LinkedList<String> cacheWideAccessDriver  = new LinkedList<String>();
    LinkedList<String> romValidDriver         = new LinkedList<String>();
    LinkedList<String> romArrayAccessDriver   = new LinkedList<String>();
    LinkedList<String> romWideAccessDriver    = new LinkedList<String>();
    LinkedList<String> resultDriver           = new LinkedList<String>();
    LinkedList<String> statusDriver           = new LinkedList<String>();

    
    
    // check and instantiate all actually implemented operators
    for (Operator op : pe.getAvailableNonNativeOperators().keySet()) {
      Implementation imp = pe.getAvailableOperators().get(op);
      String module = imp.getName();
      
      
      if (imp.isMultiCycle()) isMultiCycle = true;
      
      // Cache operation
      if (imp.isCacheAccess()) {
        cacheValidDriver.add(module);
        if (imp.isStore())             cacheWriteDriver      .add(module);
        if (imp.isCachePrefetch())     cachePrefetchDriver   .add(module);
        if (imp.isIndexedMemAccess())  cacheArrayAccessDriver.add(module);
        if (imp.isWideMemAccess()) {
          cacheWideAccessDriver.add(module);
          maxCacheWideAccessWidth = Math.max(maxCacheWideAccessWidth, imp.getWideMemAccessPortWidth());
        }
      }
      
      // ROM operation
      if (imp.isRomAccess()) {
        romValidDriver.add(module);
        if (imp.isIndexedMemAccess()) romArrayAccessDriver.add(module);
        if (imp.isWideMemAccess()) {
          romWideAccessDriver.add(module);
          maxRomWideAccessWidth = Math.max(maxRomWideAccessWidth, imp.getWideMemAccessPortWidth());
        }
      }
      
      // Controlflow operation (may also be cache prefetch)
      if (imp.isControlFlow()) statusDriver.add(module);
      
      // Arithmetic operation
      if (!imp.isMemAccess() && !imp.isControlFlow()) {
        resultDriver.add(module);
      }
      
      // Operand In
      if(!imp.isMemAccess()){
        switch (imp.getNumberOfOperands()) {
          case 2 : maxOpAWidth = Math.max(maxOpAWidth, imp.getOperandPortWidth(0));
                   maxOpBWidth = Math.max(maxOpBWidth, imp.getOperandPortWidth(1));
                   break;
          case 1:  maxOpAWidth = Math.max(maxOpAWidth, imp.getOperandPortWidth(0));
                   break;
          default: throw new IllegalArgumentException("unsupported operator input parity");
        }
        maxResultWidth = Math.max(maxResultWidth, imp.getResultPortWidth(0));
      }
      
      // collect all operator instantiations in ALU template variable bib
      template.add("bib", "// Instantiation and wiring of operation " + module + "\n");
      HashMap<String, String> wires = new HashMap<String, String>();  // map ports to wires
      LinkedList<String[]>    lines = new LinkedList<String[]>();     // lines for local wire declarations
      int mw = 0;                                                     // max width declaration
      for (Port p : imp.getPorts()) {
        // all outputs are connected to local wires
        if (p.getType() != Port.Type.IN) {
          String w = "w_" + p.getName().toLowerCase() + "_" + module;
          String d = p.getWidthDeclaration();
          if (d.length() > mw) mw = d.length();
          lines.add(new String[] {d, w});
          wires.put(p.toString(), w);
          
        // inputs are typically driven by alu inputs
        } else {
          String wire = p.toString();
          
          // sequential start signal derived from OPCODE
          if (p == Module.START) {
            wire = "w_valid_in_" + module;
            if(pe.getAvailableNonNativeOperators().size() > 1){
            	lines.add(new String[] {"", wire + " = (" + Module.OPCODE + " == " + imp.getOpcode() + ") ? 1 : 0"});
            }
            else{
            	if(pe.getAvailableNonNativeOperators().size() == 1){
            		lines.add(new String[] {"", wire + " =  1 "});
            	}
            }
          
          // restrict arithmetic operands to actually required width
          } else if (p instanceof Implementation.ArithmeticPort) {
            wire += p.getWidthDeclaration();
          }
          wires.put(p.toString(), wire);
        }
      }
      for (String[] line : lines) {
        template.add("bib", "wire");
        if (mw > 0) {
          template.add("bib", String.format(" %" + mw + "s", line[0]));
        }
        template.add("bib", " " + line[1] + ";\n");
      }
      template.add("bib", imp.getInstance("i_" + module, wires) + "\n");
    }
    
    // build alu module
    int opcodeWidth = pe.getContextMaskPE().getOpcodewidth();
    LinkedList<Port> ports = new LinkedList<Port>();
    if (isMultiCycle)                       {ports.add(         Module.CLOCK);
                                             ports.add(         Module.RESET);}
    if (isMultiCycle && model.isStallable()) ports.add(         Module.ENABLE);
    if (opcodeWidth > 0)                     ports.add(new Port(Module.OPCODE,          opcodeWidth));
    if (maxOpAWidth > 0)                     ports.add(new Port(Module.OPA,             maxOpAWidth));
    if (maxOpBWidth > 0)                     ports.add(new Port(Module.OPB,             maxOpBWidth));
    if (!resultDriver          .isEmpty())   ports.add(new Port(Module.RESULT,          maxResultWidth));
    if (!statusDriver          .isEmpty())   ports.add(         Module.STATUS);
    if (!cacheValidDriver      .isEmpty())  {ports.add(         Module.CACHE_VALID); 
                                             ports.add(         Module.CBOX);
                                             ports.add(         Module.COND);}
    if (!cacheWriteDriver      .isEmpty())   ports.add(         Module.CACHE_WRITE);
    if (!cachePrefetchDriver   .isEmpty())   {ports.add(         Module.CACHE_PREFETCH); ports.add(Module.CACHE_STATUS);}
    if (!cacheArrayAccessDriver.isEmpty())   ports.add(         Module.CACHE_ARRAY);
    if (!cacheWideAccessDriver .isEmpty())   ports.add(new Port(Module.CACHE_WIDE,      maxCacheWideAccessWidth));
    if (!romValidDriver        .isEmpty())   ports.add(         Module.ROM_VALID);
    if (!romArrayAccessDriver  .isEmpty())   ports.add(         Module.ROM_ARRAY);
    if (!romWideAccessDriver   .isEmpty())   ports.add(new Port(Module.ROM_WIDE,        maxRomWideAccessWidth));

    Module module = new Module() {
      @Override public String     getName()  {return "Alu" + pe.getID();}
      @Override public List<Port> getPorts() {return ports;}
    };

    // pass derived information to Alu template to aggregate drivers
    template.add("date",         date);
    template.add("declaration",  module.getDeclaration());
    template.add("cacheDriver",  getOr (Module.CACHE_VALID,    "mem_valid",                        cacheValidDriver));
    template.add("cacheDriver",  getOr (Module.CACHE_WRITE,    "cache_write",                      cacheWriteDriver));
    template.add("cacheDriver",  getOr (Module.CACHE_PREFETCH, "cache_prefetch",                   cachePrefetchDriver));
    template.add("cacheDriver",  getOr (Module.CACHE_ARRAY,    "mem_array_access",                 cacheArrayAccessDriver));
    template.add("cacheDriver",  getMux(Module.CACHE_WIDE,     "mem_wide_access",  "mem_valid",    cacheWideAccessDriver));
    template.add("romDriver",    getOr (Module.ROM_VALID,      "mem_valid",                        romValidDriver));
    template.add("romDriver",    getOr (Module.ROM_ARRAY,      "mem_array_access",                 romArrayAccessDriver));
    template.add("romDriver",    getMux(Module.ROM_WIDE,       "mem_wide_access",  "mem_valid",    romWideAccessDriver));
    template.add("resultDriver", getMux(Module.RESULT,         "result",           "result_valid", resultDriver));
    template.add("statusDriver", getMux(Module.STATUS,         "status",           "status_valid", statusDriver));
    dump(module, template);
  }


	/**
	 * Enable/Disable operator testbench generation
	 * If enabled, for each operator module op, the following additional files will be generated:
	 * <ul>
	 *   <li> tb_op.v    - the testbench
	 *   <li> tb_op.sim  - the Modelsim script to start the simulation
	 *   <li> tb_op.wave - the Modelsim script to display relevant signals
	 * </ul>
	 * @param flag
	 */
	public void setDumpOperatorTestbench(boolean flag) {
		dumpOperatorTestbench = flag;
	}

	/**
	 * Dump Verilog modules of operations used in a certain PE.
	 * Already existing modules (either old ones ore from other PEs) will be overwritten, as all implementation-relevant 
	 * operator settings are reflected by the module name. 
	 * @param pe
	 */
	private void printOperators(PEModel pe) {
		for (Implementation imp : pe.getAvailableOperatorImplementations()) {
			if (imp.isNative()) {
				continue;
			}
			try {
				dump(imp.getName() + ".v", imp.getModule());
				modules.put(imp.getName(), imp);
				if (dumpOperatorTestbench) {
					String tb = imp.getTestbench();
					if (tb == null) {
						continue;
					}
					dump(imp.getTestbenchName() + ".v",    tb);
					dump(imp.getTestbenchName() + ".sim",  imp.getSimTCL());
					dump(imp.getTestbenchName() + ".wave", imp.getWaveTCL());
				}
			} catch (Implementation.NotImplementedException e) {
				System.err.println(e.getMessage());
			}
		}
	}

	/**
	 * This methods exports a register file for a PE
	 * @param pe Pe that should hold the RF
	 */
	private void printRegisterFile(PEModel pe) {
	  
	  int size           = pe.getRegfilesize();
	  int wordWidth      = pe.getMaxWidthResult();
	  int readAddrWidth  = size > 1 ? pe.getContextMaskPE().getRegAddrWidthRead()  : 0;
	  int writeAddrWidth = size > 1 ? pe.getContextMaskPE().getRegAddrWidthWrite() : 0;
	  boolean mem        = pe.getMemAccess(); //! this must also hold true for ROM access
    
	  LinkedList<Port> ports = new LinkedList<Port>();
	                                ports.add(         Module.CLOCK);
	  if (model.isStallable())      ports.add(         Module.ENABLE);
	  if (readAddrWidth > 0)        ports.add(new Port(Module.RD_DIRECT_ADDR, readAddrWidth));
	                                ports.add(new Port(Module.RD_DIRECT,      wordWidth));
	  if (readAddrWidth > 0)        ports.add(new Port(Module.RD_MUX_ADDR,    readAddrWidth));
                                  ports.add(new Port(Module.RD_MUX,         wordWidth));
                                  ports.add(         Module.WR_EN);
    if (writeAddrWidth > 0)       ports.add(new Port(Module.WR_ADDR,        writeAddrWidth));
                                  ports.add(new Port(Module.WR_DATA,        wordWidth));
    if (mem && readAddrWidth > 0) ports.add(new Port(Module.RD_MEM_ADDR,    readAddrWidth));
    if (mem)                      ports.add(new Port(Module.RD_MEM,         wordWidth));
	  
	  Module module = new Module() {
      @Override public String     getName()  {return "Registerfile" + pe.getID();}
      @Override public List<Port> getPorts() {return ports;}
    };

		STGroupFile group = new STGroupFile(target.Processor.Instance.getHardwareTemplatePathDataPath() + "/Registerfile_template.stg", '§', '§');
		ST template = group.getInstanceOf("Registerfile");
		              template.add("declaration",                module.getDeclaration());
		              template.add("mem",                        mem);
		              template.add("codeConstantsInReadAddress", pe.codeConstantsInReadAddress());
		              template.add("stallcapability",            model.isStallable());

		if (size > 1) template.add("regfile_size",               size);
		if (size > 1) template.add("regfile_addr_width_read",    readAddrWidth);
		              template.add("regfile_word_width",         wordWidth);

		dump(module, template);
	}

	/**
	 * Method to export the CBox
	 */
	private void printCbox() {

		printCBoxWrapper();

		STGroupFile group = new STGroupFile(target.Processor.Instance.getHardwareTemplatePathDataPath() + "/C_Box_template_bypassvariations.stg", '§', '§');
		ST template = group.getInstanceOf("CBox");
		CBoxModel cboxmodel = model.getcBoxModel();



		template.add("date", date.toString());
		template.add("memoryslots", cboxmodel.getMemorySlots());

		//		int counter = 0;
		//		for(BranchSelectionConnection bss : cboxmodel.getBranchSelectionSources()){
		//			template.add("branchSelection", "  " + counter + ": BRANCH_SELECTION_O = " + bss.getVerilogDeclaration() + ";\n");
		//			counter ++;
		//			if(cboxmodel.getBranchSelectionSources().size() == 1){
		//				template.add("branchSelectionBypass","  BRANCH_SELECTION_O = " + bss.getVerilogDeclaration() + ";\n");
		//			}
		//		}
		//
		//		template.add("branchSelectionBypassMemory", cboxmodel.getBranchSelectionBypass());
		int counter = 0;
		for (int i = 0; i < model.getNrOfPEs(); i++) {
			if (model.getPEs().get(i).getControlFlow()) {
				template.add("status_inputs", "input wire 			STATUS_" + model.getPEs().get(i).getID() + "_I, \n");
				template.add("mux_status", counter + ": w_status = STATUS_" + model.getPEs().get(i).getID() + "_I; \n");
				template.add("bypass_inputmux", "STATUS_" + model.getPEs().get(i).getID() + "_I; \n");
				counter++;
			}
		}

		ContextMaskCBoxEvaluationBlock contextmaskcbox = model.getcBoxModel().getContextmaskEvaLuationBlocks();
		template.add("ccntwidth", model.getCCNTWidth());
		template.add("contextmemorywidth", contextmaskcbox.getContextWidth());
		template.add("contextsize", model.getContextMemorySize());
		template.add("bypassAndNegative", contextmaskcbox.getBypassAndNegative());
		template.add("bypassAndPositive", contextmaskcbox.getBypassAndPositive());
		template.add("bypassOrNegative", contextmaskcbox.getBypassOrNegative());
		template.add("bypassOrPositive", contextmaskcbox.getBypassOrPositive());
		template.add("raddrOrPositiveH", contextmaskcbox.getRAddrOrPositiveH());
		template.add("raddrOrPositiveL", contextmaskcbox.getRAddrOrPositiveL());
		template.add("raddrOrNegativeH", contextmaskcbox.getRAddrOrNegativeH());
		template.add("raddrOrNegativeL", contextmaskcbox.getRAddrOrNegativeL());

		template.add("waddrNegativeH", contextmaskcbox.getWAddrNegativeH());
		template.add("waddrNegativeL", contextmaskcbox.getWAddrNegativeL());
		template.add("waddrPositiveH", contextmaskcbox.getWAddrPositiveH());
		template.add("waddrPositiveL", contextmaskcbox.getWAddrPositiveL());
		if(contextmaskcbox.getInputMuxWidth()>0){
			template.add("muxH", contextmaskcbox.getInputMuxH());
			template.add("muxL", contextmaskcbox.getInputMuxL());
		}
		template.add("write_enable", contextmaskcbox.getWriteEnable());


		int outputs = model.getcBoxModel().getCBoxPredicationOutputsPerBox();
		if(outputs>1){
			template.add("nrOfOutputs", outputs);
			for(int i = 1; i < outputs; i++){
				ST addOutputAddr = group.getInstanceOf("CBoxReadPortAddr");
				addOutputAddr.add("i", i);
				addOutputAddr.add("raddrPredicationL", contextmaskcbox.getRAddrPredicationL()+i*contextmaskcbox.getAddrWidth());
				addOutputAddr.add("raddrPredicationH", contextmaskcbox.getRAddrPredicationL()+(i+1)*contextmaskcbox.getAddrWidth()-1);
				template.add("additionalOutputs", addOutputAddr.render());

				ST addOutputAssign = group.getInstanceOf("CBoxReadPortAssign");
				addOutputAssign.add("i", i);
				addOutputAssign.add("raddrPredicationL", contextmaskcbox.getRAddrPredicationL()+i*contextmaskcbox.getAddrWidth());
				addOutputAssign.add("raddrPredicationH", contextmaskcbox.getRAddrPredicationL()+(i+1)*contextmaskcbox.getAddrWidth()-1);
				template.add("additionalOutputAssign", addOutputAssign.render());
			}
			template.add("raddrPredicationH", contextmaskcbox.getRAddrPredicationL()+contextmaskcbox.getAddrWidth()-1);
			template.add("raddrPredicationL", contextmaskcbox.getRAddrPredicationL());
		} else {
			template.add("raddrPredicationH", contextmaskcbox.getRAddrPredicationH());
			template.add("raddrPredicationL", contextmaskcbox.getRAddrPredicationL());
		}
		
		if(initializationPath != null){
			template.add("initPath", initializationPath);
		}


		dump("CBox.v", template);
	}

    /**
     * Prints all required ROM modules to one file
     */
    private void printROM() {
        final Processor processor = Processor.Instance;
        final STGroupFile group = new STGroupFile(processor.getHardwareTemplatePathDataPath()
                + "/rom.stg", '§', '§');

        // The ST to add all ROM modules to
        ST template = group.getInstanceOf("rom");

        int romModuleCount = 0;
        for (PEModel pe : model.getPEs()) {
            if (pe.getRomAccess()) {
                RomModule romMod = RomModule.createDefault(
                    pe.getID(), pe.getRomAddrWidth(), processor.getDataPathWidth()
                );

                // The ST for a single ROM module
                ST romTemplate = group.getInstanceOf("rom_module");

                // At this point it would be nice to have the PE Module implemented by deriving from GenericModule
                // The same goes for the Alu

                // Check if there are any special requirements for this module
                List<Port> aluPorts = modules.get("Alu" + pe.getID()).getPorts();
                for (Port ap : aluPorts) {
                    if (ap.equals(Module.ROM_ARRAY)) {
                        romMod.addPort(RomModule.Index.RdArray, RomModule.RD_ARRAY);
                        romTemplate.add("uses_array", true);
                    }

                    if (ap.equals(Module.ROM_ARRAY)) {
                        int width = ap.getWidth();
                        Port rdWide = new Port(RomModule.RD_WIDE, width);
                        romMod.addPort(RomModule.Index.RdArray, rdWide);
                        romTemplate.add("uses_width", true);
                        romTemplate.add("wide_width", width);
                    }
                }

                // Fill out the rest of the template
                romTemplate.add("declaration", romMod.getDeclaration());
                romTemplate.add("data_width", processor.getDataPathWidth());
                romTemplate.add("rom_size", pe.getRomSize());
                romTemplate.add("rom_addr_width", pe.getRomAddrWidth());

                // Housekeeping
                template.add("rom_modules", romTemplate.render());
                modules.put(romMod.getName(), romMod);
                ++romModuleCount;
            }
        }

        if (romModuleCount > 0)
            dump("rom.v", template);
    }

	/**
	 * By using this method a tcl script is stored that triggers a synthesis&implementation run once with the exported constraints.
	 * @param folder destination folder for the script
	 */
	private void makeTCLSynthesis(String folder) {

		File file = new File(folder+"/synthesisvivado.tcl");
		try {
			FileWriter fw = new FileWriter(file);
			BufferedWriter bw;
			bw = new BufferedWriter(fw);

			bw.write(" # to run this script, open a terminal first. Navigate to this folder, then type in \n" +
					" # vivado –modebatch –source <go.tcl> \n" +
					"create_project -force utilization_test_" + model.getName() + " \n" +
					"add_file -scan_for_includes Cgra.v \n" +
					"add_file Controlunit.v \n" +
					"add_file CBox.v \n" +
					"add_file ContextPE.v \n" +
					"add_file block_multiplier.v \n" +
					"add_file Context_cbox.v \n \n");

			for(int i = 0; i < model.getNrOfPEs() ;i++){
				bw.write("add_file PE" + i + ".v \n" );
				bw.write("add_file ALU" + i + ".v \n \n" );
			}
			bw.write("add_file Registerfile.v \n" +
					"add_file Registerfile_mem_access.v \n" +
					"add_file timing_constraints.xdc \n" +
					"set_property board_part xilinx.com:vc709:1.1 [current_project] \n" + 
					" #synth_design -s_1 \n" +
					"create_run -flow {Vivado Synthesis 2015} s_1 \n" +
					"launch_run s_1 \n" +
					"wait_on_run s_1\n" +
					" #report_timing_summary \n" +
					"launch_runs impl_1 \n" +
					"wait_on_run impl_1 \n" +
					"open_run impl_1 ");

			bw.close();
			fw.close();
		}
		catch(IOException e){
			System.err.println("IOException in print Verilog -> TCL Script Synthesis");
		}
	}

	/**
	 * Helper method to finally dump a file/module
	 * @param file Filename of the module 
	 * @param content content of the file
	 */
	protected void dump(String file, String content) {
		try {
			if(!file.contains("dummy")){
				genFiles.add(file);
			}
			file = destinationFolder + "/" + file;
			FileWriter fw = new FileWriter(file);
			fw.write(content);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Container method for {@link #dump(String, String)}
	 * @param file Filename of the module 
	 * @param template template to be exported
	 */
	protected void dump(String file, ST template) {
		dump(file, template.render() + "\n");
	}
	
	/**
   * Container method for {@link #dump(String, ST)}
   * @param mod module to be dumped 
   * @param template template to be exported
   */
	protected void dump(Module mod, ST template) {
	  modules.put(mod.getName(), mod);
	  dump(mod.getName() + ".v", template);
	}


	/**
	 * This methods prints an overview of the CGRA that is exported. It contains several information about the
	 * CGRA.
	 */
	protected void printInformation(){

		String processor = Processor.Instance.getClass().getName();
		int index2cut = processor.lastIndexOf(".");
		if(processor.contains(".")){
			processor = processor.substring(index2cut+1);
		}

		try {
			FileWriter fw = new FileWriter(destinationFolder + "/Data Sheet");
			BufferedWriter bw;
			bw = new BufferedWriter(fw);
			bw.write("Host processor : " + processor + "\n"); 

			bw.write("Data path width : " + Processor.Instance.getDataPathWidth()+ "\n");

			bw.write("ALU pipelined: " + model.isPipelined() + "\n");
			
			bw.write("2ndRFOut2ALU :" + model.isSecondRFOutput2ALU() + "\n");
			
			bw.write("Conditional Jumps : " + model.isConditionalJumps() + "\n");
			
			bw.write("CBox Evaluation Blocks : " + model.getcBoxModel().getNrOfEvaluationBlocks() + "\n");
			
			bw.write("CBox Predication per Evaluation Blocks : " + model.getcBoxModel().getCBoxPredicationOutputsPerBox() + "\n");
			
			
			for(BranchSelectionConnection cons : model.getcBoxModel().getBranchSelectionSources()){
				bw.write(cons.toString() + "  ");
			}				
			bw.write("\n");
			bw.write( "Context memorysize : " + model.getContextMemorySize() + "\n");
			bw.write( "" + "\n");
			bw.write( "Number of PEs : " + model.getNrOfPEs() + "\n");
			bw.write( "Number of PEs with memory access : " + model.getNrOfMemoryAccessPEs() + "\n");
			bw.write( "Number of PEs with control flow : " + model.getNrOfControlFlowPEs() + "\n");
			bw.write( "\n ---- Overview Processing Elements ----\n");

			for(PEModel pe : model.getPEs()){
				bw.write(" -- PE " + pe.getID() + "  -- " + pe.getRegfilesize() + " Regentrie(s) \n");
				for(Operator op : pe.getAvailableNonNativeOperators().keySet()){
					bw.write(op.toString() + " \t ( dur: " + pe.getAvailableNonNativeOperators().get(op).getLatency()  
							+ ", en: " + pe.getAvailableNonNativeOperators().get(op).getLatency()  +") \n");
				}
				bw.newLine();
			}

			bw.close();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	/**
	 * The toplevel ports can be generated by iteration over the Set {@link toplevelports}. Previously the
	 * set has to be filled. This method shouldb e to be used and extented for this purpose.
	 */
	protected abstract void fillPortList();

	public java.util.Date getDate() {
		return date;
	}

	public  Set<VerilogPort> getToplevelports() {
		return toplevelports;
	}
	public String getToplevelName() {
		return model.getName();
	}

	public Set<String> getModules() {
		return genFiles;
	}


	/**
	 * Since it is often of interest to synthesis the CGRA as a stand alone design, the fan out often exceeds the io-limits
	 * of an FPGA. Therefore a top level dummy can be used to reduce the fan out. 
	 * @param cgra
	 */

	protected abstract void printDummy(CgraModel cgra);

	public String getTemplateFileName(String templateName) {
		return null;
	}

	protected void addAllFilesFrom(String sourceFolder, String folderName) {
		File dir = new File(sourceFolder + "/" + folderName);

		if (!dir.exists()) {
			System.err.printf("Specified folder %s does not exist.\n", sourceFolder + "/" + folderName);
			return;
		}

		for (File file : dir.listFiles()) {
		  String name = file.getName();
			int dot = name.lastIndexOf('.');
			if (dot != 0) {
				String ext = name.substring(dot + 1);
				if (ext.equals("stg"))
					continue; // don't add ST files, they are added in different ways
				if (ext.equals("v") || ext.equals("sv"))
					genFiles.add(name);
			}

			Path source = Paths.get(sourceFolder + "/" + folderName + "/" + file.getName());
			Path destination = Paths.get(this.destinationFolder + "/" + file.getName());
			try {
				Files.copy(source, destination);
			} catch (IOException e) {
				System.err.println("IO Exception while adding a static file with file " + source);
			}
		}
	}


	private void printCBoxWrapper(){

		STGroupFile group = new STGroupFile(target.Processor.Instance.getHardwareTemplatePathDataPath() + "/C_Box_wrapper_template.stg", '§', '§');
		ST template = group.getInstanceOf("CBoxWrapper");
		CBoxModel cboxmodel = model.getcBoxModel();
		

		template.add("date", date.toString());
		template.add("memoryslots", cboxmodel.getMemorySlots());
		template.add("contextmemorysize", model.getContextMemorySize());
		
		template.add("stallcapability", model.isStallable());
		template.add("branchselection", model.isConditionalJumps());
		ContextMaskCBoxEvaluationBlock contextmaskcbox = model.getcBoxModel().getContextmaskEvaLuationBlocks();
		template.add("ccntwidth", model.getCCNTWidth());
		template.add("contextmemorywidth", contextmaskcbox.getContextWidth());
		template.add("addrWidth", contextmaskcbox.getAddrWidth());

		template.add("nrOfOutputs", model.getcBoxModel().getNrOfEvaluationBlocks()*model.getcBoxModel().getCBoxPredicationOutputsPerBox());
		template.add("evaluationblocks", model.getcBoxModel().getNrOfEvaluationBlocks());

		int counter = 0;
		for(BranchSelectionConnection bss : cboxmodel.getBranchSelectionSources()){
			template.add("branchSelection", "  " + counter + ": BRANCH_SELECTION_O = " + bss.getVerilogDeclaration() + ";\n");
			counter ++;
			template.add("branchSelectionBypass","  BRANCH_SELECTION_O = " + bss.getVerilogDeclaration() + ";\n");
		}
		if(cboxmodel.getBranchSelectionSources().size() != 1){
			template.add("branchselectionwidth", cboxmodel.getContextmaskWrapper().getBranchSelectionMuxWidth());
		}


		int outputs = model.getcBoxModel().getCBoxPredicationOutputsPerBox();
		if(outputs>1){
			for(int i = 1; i < outputs; i++){
				ST addOutputAssign = group.getInstanceOf("CBoxReadPortAssign");
				addOutputAssign.add("i", i);
				addOutputAssign.add("nrOfOutputsPerCBox", model.getcBoxModel().getCBoxPredicationOutputsPerBox());
				template.add("additionalOutputAssignments", addOutputAssign.render());

				ST addOutput = group.getInstanceOf("CBoxReadPort");
				addOutput.add("i", i);
				addOutput.add("evaluationblocks", model.getcBoxModel().getNrOfEvaluationBlocks());
				addOutput.add("addrWidth", contextmaskcbox.getAddrWidth());
				template.add("additionalOutputs", addOutput.render());

				template.add("additionalOutputConnections", ".w_rd_addr_predication"+i+"(w_rd_addr_predication"+i+"[n]),\n");
			}
		}

		//			template.add("branchSelectionBypassMemory", cboxmodel.getBranchSelectionBypass());
		counter = 0;
		for (int i = 0; i < model.getNrOfPEs(); i++) {
			if (model.getPEs().get(i).getControlFlow()) {
				template.add("status_inputs", "input wire 			STATUS_" + model.getPEs().get(i).getID() + "_I, \n");
				template.add("statusIns", "\n  .STATUS_"+i+"_I(STATUS_"+i+"_I),");
				//				template.add("mux_status", counter + ": w_status = STATUS_" + model.getPEs().get(i).getID() + "_I; \n");
				//				template.add("bypass_inputmux", "STATUS_" + model.getPEs().get(i).getID() + "_I; \n");
				counter++;
			}
		}



		dump("CBoxWrapper.v", template);
	}
	
	public void setInitializationPath(String path){
		initializationPath = path;
	}
}
