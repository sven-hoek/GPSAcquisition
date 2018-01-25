package generator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroupFile;

import cgramodel.CgraModel;
import cgramodel.CgraModelAmidar;
import cgramodel.ContextMaskPE;
import cgramodel.PEModel;
import generator.Module.Port;
import target.Processor;

/**
 * This is dedicated Verilog generator for the host processor Amidar. 
 * @author wolf
 *
 */
public class VerilogGeneratorAmidar extends VerilogGenerator{


	/**
	 * Amidar works with Caches of 32 bit words. 
	 */
	final static int CACHEDATAWIDTH = 32;

	/**
	 * The model that is to be exported.
	 */
	public CgraModel getModel(){
		return model;
	}

	/**
	 * constructor
	 * @param model Model to be exported
	 */
	public VerilogGeneratorAmidar(CgraModel model){
		super(model);
	}

	/**
	 * constructor
	 */
	public VerilogGeneratorAmidar(){
		super();
	}

	/**
	 * This methods prints the top level Verilog description of the CGRA. It instantiates all modules and connects them.
	 * Furthermore An FSM is contained as well as context converters to map incoming context to the indiv. context masks. 
	 */
	protected void printTopLevel() {
		STGroupFile group = new STGroupFile(target.Processor.Instance.getHardwareTemplatePathProcessorRelated() + "/Cgra_new_gen_template.stg", '§', '§');
		ST template = group.getInstanceOf("toplevel");

		template.add("date", date.toString());
		template.add("ccntwidth", model.getContextmaskccu().getCCNTWidth());
		template.add("contextWidthCBox", model.getcBoxModel().getContextmaskEvaLuationBlocks().getContextWidth());
		template.add("viaWidth",model.getViaWidth());

		template.add("nrOfCBoxOutputs", model.getcBoxModel().getCBoxPredicationOutputsPerBox()*model.getcBoxModel().getNrOfEvaluationBlocks());
		template.add("evaluationblocks", model.getcBoxModel().getNrOfEvaluationBlocks());
		template.add("branchselection", model.isConditionalJumps());
		template.add("maxMemCols", (int)Math.ceil(((CgraModelAmidar)model).getMaxMemoryWidth()/32.0));

		// constPointerWidth,locationInformationPointerWidth, kernelTableAddrWidth, maxMuxWidth, rfAddrWidth, locInfoMemWidth
		template.add("constPointerWidth", ((CgraModelAmidar)model).getConstantMemoryAddrWidth());
		template.add("locationInformationPointerWidth", ((CgraModelAmidar)model).getLocationInformationMemoryAddrWidth());
		template.add("kernelTableAddrWidth", ((CgraModelAmidar)model).getKernelTableAddrWidth());
		template.add("maxMuxWidth", model.getMaxMuxAddrWidth());
		template.add("rfAddrWidth", model.getMaxRegfileAddrWidth());
		template.add("ccuContextWidth", model.getContextmaskccu().getContextWidth());

		int a = model.getViaWidth() + model.getMaxMuxAddrWidth();
		if(a < model.getNrOfPEs()){
			a =  model.getNrOfPEs();
		}

		template.add("locInfoMemWidth", model.getMaxRegfileAddrWidth() + a);

		LinkedList<Port> ports = new LinkedList<Port>();

		int resultconnectioncounter = 0;

		// port decleration
		fillPortList();

		int portcounter = toplevelports.size();
		for(VerilogPort port: toplevelports){
			portcounter --;
			String verilogport = "(* dont_touch = \"true\" *) ";
			verilogport += port.getDirection() + " ";
			verilogport += port.getTypeDeclaration() + " ";
			if(port.getPortwidth() > 1){
				verilogport += "[" + port.getPortwidth() + "-1:0] ";
			}
			verilogport += port.getPortDeclaration().toLowerCase() + " ";

			if(portcounter > 0){
				verilogport += ", \n";
			}
			template.add("portlist", verilogport);
		}

		int cnt = -1;
		for (PEModel pe : model.getPEs()) {
			int id = pe.getID();
			ST templatePE = group.getInstanceOf("peModule");
			templatePE.add("ID",           id);
			templatePE.add("contextwidth", pe.getContextWidth());
			templatePE.add("muxInput", pe.getContextMaskPE().getMuxBH()>0);
			if(pe.getContextMaskPE().getRegAddrWidthRead() > 0){
				templatePE.add("regFileAddrWidth", pe.getContextMaskPE().getRegAddrWidthRead());
			}
			
			templatePE.add("muxWidth", pe.getContextMaskPE().getMuxwidth());//(int)Math.ceil(Math.log(pe.getInputs().size()+1)/Math.log(2)));
			
			if(pe.getCacheAccess()){
				templatePE.add("cache", pe.getCacheAccess());
				templatePE.add("NR", ++cnt);
			}
			
			template.add("pes",            templatePE.render());


			Module peModule = modules.get("PE" + id);
			if (peModule == null) throw new RuntimeException("Interface module for " + pe + " missing");

			// instantiate pe
			HashMap<String, String> wires = new HashMap<String, String>();
			List<Port> pePorts = peModule.getPorts();
			for (Port p : peModule.getPorts()) {
				String w = "";
				if (p.equals(Module.CLOCK))            w = p.toString().toLowerCase();
				else if (p.equals(Module.RESET))            w = "w_reset_alu";
				else if (p.equals(Module.ENABLE))           w = "joint_cache_valids";
				else if (p.equals(Module.CCNT))             w = "w_ccnt";
				else if (p.getName().startsWith("INPUT_"))  w = "w_direct_out_" + p.getName().substring(6);
				else if (p.equals(Module.LV_CONTEXT_WR_EN)) w = "w_write_context_for_lv_" + id;
				else if (p.equals(Module.LV_DATA))          w = "w_context_data_lv_" + id;
				else if (p.equals(Module.CONTEXT_DATA))     w = "memoryLine["+pe.getContextWidth()+"-1:0]";
				else if (p.equals(Module.CONTEXT_WR_EN))    w = "w_context_write_instance_ID_reg == (PE+"+id+") && writeMemoryLine";
				else if (p.equals(Module.CONTEXT_WR_ADDR))  w = "w_context_write_address_reg["+model.getCCNTWidth()+"-1:0]";
				else if (p.equals(Module.CACHE_RD_DATA))    w = p.toString(cnt).toLowerCase();
				else if (p.equals(Module.CACHE_WR_DATA))    w = p.toString(cnt).toLowerCase();
				else if (p.equals(Module.CACHE_ADDR))       w = p.toString(cnt).toLowerCase();
				else if (p.equals(Module.CACHE_OFFSET))     w = p.toString(cnt).toLowerCase();
				else if (p.equals(Module.CACHE_VALID))    { w = p.toString(cnt).toLowerCase(); 
				template.add("jointCacheValid", "," + w.replaceAll("_o", "_i"));
				if(w.endsWith("_o")){
					w = w.substring(0, w.length()-2);
				}
				}
				else if (p.equals(Module.CACHE_WRITE))      w = ("CACHE_WR_" + cnt + "_O").toLowerCase(); // TODO: resolve inconsistent naming
				else if (p.equals(Module.CACHE_ARRAY)) w = p.toString(cnt).toLowerCase();
				else if (p.equals(Module.CACHE_PREFETCH)) w = p.toString(cnt).toLowerCase();
				else if (p.equals(Module.CACHE_STATUS)) w = p.toString(cnt).toLowerCase();
				else if (p.equals(Module.CACHE_WIDE)) w = p.toString(cnt).toLowerCase();	           

				else if (p.equals(Module.LIVE_IN))          w = "liveIn";
				else if (p.equals(Module.LIVE_OUT))       { w = "w_pe_out_" + id;
				// TODO - check width !!!
				template.add("resultConnection", " \n" + resultconnectioncounter + ": result_low = " + w + "["+ pe.getMaxWidthResult() + "-1:0];");
				resultconnectioncounter++;
				}
				else if (p.equals(Module.PREDICATION))      w = "w_predication";
				else if (p.equals(Module.DIRECT_OUT))       w = "w_direct_out_" + id;
				else if (p.equals(Module.STATUS))         { w = "w_status_" + id;
				template.add("statusIns",             "\n\t.STATUS_" + id + "_I(" + w + "),");
				template.add("statusWireDeclaration",   w + ";\n");
				template.add("wires_status", "wire  " + w + ";\n");
				}

				wires.put(p.toString(), w);
			}
			template.add("pes", peModule.getInstance("i_pe"+id, wires));

			template.add("wires_direct_out_Pe", "wire ["+ pe.getMaxWidthResult() +"-1:0] w_direct_out_"+ id +";\n");
			if (pePorts.contains(Module.LIVE_OUT) || pePorts.contains(Module.CACHE_VALID)) {
				template.add("wires_out_Pe", "wire ["+ pe.getMaxWidthResult() +"-1:0] w_pe_out_"+ id +";\n");
			}

		}

		int maxContextWidth = 0;
		for(PEModel pe : model.getPEs()){
			if(pe.getContextWidth() > maxContextWidth){
				maxContextWidth = pe.getContextWidth();
			}
		}
		if(model.getcBoxModel().getContextmaskEvaLuationBlocks().getContextWidth() > maxContextWidth){
			maxContextWidth = model.getcBoxModel().getContextmaskEvaLuationBlocks().getContextWidth();
		}

		template.add("maxContextWidth", maxContextWidth);
		template.add("peAddrWidth", (int) Math.ceil((Math.log(model.getNrOfPEs()) / Math.log(2) )));
		template.add("nrOfPes", model.getNrOfPEs());
		template.add("contextmemorysize", model.getContextMemorySize());
		
		int maxAddrWidth = model.getCCNTWidth();
		if(maxAddrWidth < ((CgraModelAmidar)model).getConstantMemoryAddrWidth()){
			maxAddrWidth = ((CgraModelAmidar)model).getConstantMemoryAddrWidth();
		}
		if(maxAddrWidth < ((CgraModelAmidar)model).getLocationInformationMemoryAddrWidth()){
			maxAddrWidth = ((CgraModelAmidar)model).getLocationInformationMemoryAddrWidth();
		}
		if(maxAddrWidth < ((CgraModelAmidar)model).getKernelTableAddrWidth()){
			maxAddrWidth = ((CgraModelAmidar)model).getKernelTableAddrWidth();
		}
		
		template.add("maxAddrWidth", maxAddrWidth);

		if(initializationPath != null){
			template.add("initPath", initializationPath);
		}

		dump(model.getName() +".v", template);
	}

	/**
	 * The prepare method is currently not needed
	 */
	protected void prepare(){
	}

	/**
	 * The context converters are Amidar related and need to be generated 
	 */
	void printHostProcessorRelatedModules(){
		for(PEModel pe : model.getPEs()){
			printConverter(pe);
		}

		String sourceFolder = target.Processor.Instance.getHardwareTemplatePathProcessorRelated();
		String fileName = "/kernelTableCGRA.v";

		File source = new File(sourceFolder + fileName);
		File destination = new File(destinationFolder + fileName);

		try {
			Files.copy(source.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace(System.err);
		}

		fileName = "/locationInformationMemory.v";

		source = new File(sourceFolder + fileName);
		destination = new File(destinationFolder + fileName);

		try {
			Files.copy(source.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace(System.err);
		}

		fileName = "/constantMemory.v";

		source = new File(sourceFolder + fileName);
		destination = new File(destinationFolder + fileName);

		try {
			Files.copy(source.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace(System.err);
		}
	}


	/**
	 * Method to print an individual Converter for a PE. They are need to map incoming context  
	 * @param pe
	 */
	private void printConverter(PEModel pe) {

		STGroupFile group = new STGroupFile(target.Processor.Instance.getHardwareTemplatePathProcessorRelated() + "/Context_Converter.stg", '§', '§');
		ST template = group.getInstanceOf("Context_Converter");
		template.add("date", date.toString());
		template.add("ID",pe.getID());
		template.add("ccntwidth",model.getCCNTWidth());
		template.add("contextWidth",pe.getContext().getContextWidth());
		template.add("nrPes",model.getNrOfPEs());
		template.add("memaccess", pe.getMemAccess());
		template.add("multipleInputs", 1);
		ContextMaskPE peMask = pe.getContext();
		if(peMask.getOpcodewidth() > 0){
			template.add("opL", peMask.getOpL());
			template.add("opH", peMask.getOpH());
		}
		template.add("muxRegL", peMask.getMuxRegL());
		template.add("muxRegH", peMask.getMuxRegH());
		if(pe.getMaxWidthInputB()>0){
			template.add("muxBL", peMask.getMuxBL());
			template.add("muxBH", peMask.getMuxBH());
		}
		if(pe.getMaxWidthInputB()>0){
			template.add("muxAL", peMask.getMuxAL());
			template.add("muxAH", peMask.getMuxAH());
		}
		template.add("maxMuxWidth", model.getMaxMuxAddrWidth());

		boolean print = (peMask.getRegAddrWidthWrite()>0) ? true : false;
		if(print){
			template.add("regAddrWidthRead", peMask.getRegAddrWidthRead());
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

		dump("Context_Converter" + pe.getID() + ".v", template);
	}


	/**
	 * Method to add static files. Currently used:
	 * 	 <p><ul>
	 * <li> Definitions (CGRA specific parameters)
	 * <li> Definitions_Amidar (Amidar specific parameters)
	 * <li> Timing constraints (default: 10.0 ns)
	 * </ul><p>
	 */
	protected void addStaticFiles(){
		super.addStaticFiles();

		Path source = Paths.get(target.Processor.Instance.getHardwareTemplatePathProcessorRelated() + "/cgra_definitions_template.vh");
		Path destination = Paths.get(destinationFolder + "/cgra.vh");


		try {
			Files.copy(source, destination);
			source = Paths.get(target.Processor.Instance.getHardwareTemplatePathProcessorRelated() + "/AMIDAR_definitions.vh");
			destination = Paths.get(destinationFolder + "/AMIDAR_definitions.vh");
			genFiles.add("definitions.vh");
			genFiles.add("AMIDAR_definitions.vh");
			Files.copy(source, destination);
		} catch (IOException e) {
			System.err.println("Error "+ e);
			System.err.println("IO Exception in addStaticFiles() with File " + source);
		}
	}

	protected static final String TOKENVALID = "TOKEN_VALID";
	protected static final String OPCODE = "OPCODE";
	protected static final String DESTINATIONTAG = "DEST_TAG";
	protected static final String DESTINATIONFU = "DEST_FU";
	protected static final String DESTINATIONPORT = "DEST_PORT";
	protected static final String RESULTTAG = "RESULT_TAG";
	protected static final String RESULTFU = "RESULT_FU";
	protected static final String RESULTPORT = "RESULT_PORT";
	protected static final String OPERANDBLOW = "OPERAND_B_LOW";
	protected static final String OPERANDBLOW_VALID = "OPERAND_B_LOW_VALID";
	protected static final String OPERANDALOW = "OPERAND_A_LOW";
	protected static final String OPERANDALOW_VALID = "OPERAND_A_LOW_VALID";
	protected static final String OPERANDBHIGH = "OPERAND_B_HIGH";
	protected static final String OPERANDBHIGH_VALID = "OPERAND_B_HIGH_VALID";
	protected static final String OPERANDAHIGH = "OPERAND_A_HIGH";
	protected static final String OPERANDAHIGH_VALID = "OPERAND_A_HIGH_VALID";
	protected static final String RESULTLOW = "RESULT_LOW";
	protected static final String RESULTLOWVALID = "RESULT_LOW_VALID";
	protected static final String RESULTACK = "RESULT_ACK";
	protected static final String RESULTHIGH = "RESULT_HIGH";
	protected static final String RESULTHIGHVALID = "RESULT_HIGH_VALID";

	protected static final String CACHEDATA = "CACHE_DATA";
	protected static final String CACHEVALID = "CACHE_VALID";
	protected static final String CACHEADDR = "CACHE_ADDR";
	protected static final String CACHEOFFSET = "CACHE_OFFSET";
	protected static final String CACHEWR = "CACHE_WR";
	protected static final String CACHE_PREFETCH = "CACHE_PREFETCH";
	protected static final String CACHE_WIDE_ACCESS = "CACHE_WIDE_ACCESS";
	protected static final String CACHE_ARRAY_ACCESS = "CACHE_ARRAY_ACCESS";
	protected static final String CACHE_STATUS = "CACHE_STATUS";

	protected static final String OPERANDACKNOWLEDGE = "OPERAND_ACK";

	protected static final String CLOCK  = "CLK";
	protected static final String RESET  = "RST";


	int maxContextWidth;

	protected void fillPortList() {
		int busdatawidth = Processor.Instance.getDataPathWidth();
		toplevelports.clear();
		toplevelports.add(new VerilogPort(CLOCK, 1, 0, portdirectionality.input,porttype.wire, false));
		toplevelports.add(new VerilogPort(RESET, 1, 0, portdirectionality.input,porttype.wire, false));
		// TODO - all ports that exceed the width of 1 should be checked whether they actually are of width 32 .. or databuswidth
		toplevelports.add(new VerilogPort(TOKENVALID, 1, 0, portdirectionality.input,porttype.wire, false));	
		toplevelports.add(new VerilogPort(OPCODE, 7, 0, portdirectionality.input,porttype.wire, false));
		toplevelports.add(new VerilogPort(DESTINATIONTAG, 7, 0, portdirectionality.input,porttype.wire, false));
		toplevelports.add(new VerilogPort(DESTINATIONFU, 4, 0, portdirectionality.input,porttype.wire, false));
		toplevelports.add(new VerilogPort(DESTINATIONPORT, 2, 0, portdirectionality.input,porttype.wire, false));

		toplevelports.add(new VerilogPort(RESULTTAG, 7, 0, portdirectionality.output,porttype.wire, false));
		toplevelports.add(new VerilogPort(RESULTFU, 4, 0, portdirectionality.output,porttype.wire, false));
		toplevelports.add(new VerilogPort(RESULTPORT, 2, 0, portdirectionality.output,porttype.wire, false));

		toplevelports.add(new VerilogPort(OPERANDBLOW, busdatawidth, 0, portdirectionality.input,porttype.wire, false));
		toplevelports.add(new VerilogPort(OPERANDBLOW_VALID, 1, 0, portdirectionality.input,porttype.wire, false));
		toplevelports.add(new VerilogPort(OPERANDALOW, busdatawidth, 0, portdirectionality.input,porttype.wire, false));
		toplevelports.add(new VerilogPort(OPERANDALOW_VALID, 1, 0, portdirectionality.input,porttype.wire, false));

		toplevelports.add(new VerilogPort(OPERANDBHIGH, busdatawidth, 0, portdirectionality.input,porttype.wire, false));
		toplevelports.add(new VerilogPort(OPERANDBHIGH_VALID, 1, 0, portdirectionality.input,porttype.wire, false));
		toplevelports.add(new VerilogPort(OPERANDAHIGH, busdatawidth, 0, portdirectionality.input,porttype.wire, false));
		toplevelports.add(new VerilogPort(OPERANDAHIGH_VALID, 1, 0, portdirectionality.input,porttype.wire, false));

		maxContextWidth = 0;
		int cnt = -1;
		for(PEModel pe : model.getPEs()){
			
			if(pe.getCacheAccess()){
				cnt++;
				toplevelports.add(new VerilogPort(CACHEDATA, CACHEDATAWIDTH, cnt, portdirectionality.input,porttype.wire, true));
				toplevelports.add(new VerilogPort(CACHEDATA, CACHEDATAWIDTH, cnt, portdirectionality.output,porttype.wire, true));
				toplevelports.add(new VerilogPort(CACHEVALID, 1, cnt, portdirectionality.input,porttype.wire, true));
				toplevelports.add(new VerilogPort(CACHEADDR, busdatawidth, cnt, portdirectionality.output,porttype.wire, true));
				toplevelports.add(new VerilogPort(CACHEOFFSET, busdatawidth, cnt, portdirectionality.output,porttype.wire, true));
				toplevelports.add(new VerilogPort(CACHEWR, 1, cnt, portdirectionality.output,porttype.wire, true));
				toplevelports.add(new VerilogPort(CACHEVALID, 1, cnt, portdirectionality.output,porttype.wire, true));

				if(pe.getArrayMemAccess()){
					toplevelports.add(new VerilogPort(CACHE_ARRAY_ACCESS, 1, cnt, portdirectionality.output,porttype.wire, true));
				}
				if(pe.getPrefetchCacheAccess()){
					toplevelports.add(new VerilogPort(CACHE_PREFETCH, 1, cnt, portdirectionality.output,porttype.wire, true));
					toplevelports.add(new VerilogPort(CACHE_STATUS, 1, cnt, portdirectionality.input,porttype.wire, true));
				}
				// TODO - determine width correctly !
				if(pe.getWideMemAccess()){
					toplevelports.add(new VerilogPort(CACHE_WIDE_ACCESS, 1, cnt, portdirectionality.output,porttype.wire, true));
				}
			}

			if(pe.getContextWidth() > maxContextWidth){
				maxContextWidth = pe.getContextWidth();
			}
		}
		if(model.getcBoxModel().getContextmaskEvaLuationBlocks().getContextWidth() > maxContextWidth){
			maxContextWidth = model.getcBoxModel().getContextmaskEvaLuationBlocks().getContextWidth();
		}

		toplevelports.add(new VerilogPort(RESULTLOW, busdatawidth, 0, portdirectionality.output,porttype.register, false));
		toplevelports.add(new VerilogPort(RESULTLOWVALID, 1, 0, portdirectionality.output,porttype.wire, false));
		toplevelports.add(new VerilogPort(RESULTHIGH, busdatawidth, 0, portdirectionality.output,porttype.register, false));
		toplevelports.add(new VerilogPort(RESULTHIGHVALID, 1, 0, portdirectionality.output,porttype.wire, false));
		toplevelports.add(new VerilogPort(RESULTACK, 1, 0, portdirectionality.input,porttype.wire, false));
		toplevelports.add(new VerilogPort(OPERANDACKNOWLEDGE, 1, 0, portdirectionality.output,porttype.wire, false));
	}

	protected void printDummy(CgraModel cgra) {
		STGroupFile group = new STGroupFile(target.Processor.Instance.getHardwareTemplatePathProcessorRelated() + "/top_level_dummy_template.stg", '§',
				'§');
		ST template = group.getInstanceOf("topLevelDummy");
		int maxContextWidth = 0;
		for (PEModel pe : cgra.getPEs()) {
			if (pe.getMemAccess()) {
				ST cacheIO = group.getInstanceOf("cacheIO");
				cacheIO.add("index", pe.getID());
				template.add("cacheIO", cacheIO.render() + "\n");
				ST cacheOutputs = group.getInstanceOf("cacheOutputs");
				cacheOutputs.add("index", pe.getID());
				template.add("cacheOutputs", cacheOutputs);
				ST cacheConjunction = group.getInstanceOf("cacheConjunction");
				cacheConjunction.add("index", pe.getID());
				template.add("cacheConjunction", cacheConjunction);
				if (maxContextWidth < pe.getContextWidth()) {
					maxContextWidth = pe.getContextWidth();
				}
			}
		}
		if (maxContextWidth < cgra.getcBoxModel().getContextmaskEvaLuationBlocks().getContextWidth()) {
			maxContextWidth = cgra.getcBoxModel().getContextmaskEvaLuationBlocks().getContextWidth();
		}
		template.add("maxContextWidth", maxContextWidth);
		template.add("ccntwidth", model.getCCNTWidth());

		for(VerilogPort port : toplevelports){
			if(port.getDirection().equals(portdirectionality.input)){
				if(port.getName().equals("CLK")){
					template.add("port", "."+ port.getPortDeclaration().toLowerCase() +"(CLK_I)\n");
				}
				else{
					template.add("port", "."+ port.getPortDeclaration().toLowerCase() +"(in["+ port.portwidth +":1])\n");
				}
			}
			else{
				template.add("port", "."+ port.getPortDeclaration().toLowerCase() +"(w_"+ port.getPortDeclaration() +")\n");
				template.add("outputwires", "wire["+ port.getPortwidth() +"-1:0] w_" + port.getPortDeclaration() +";\n");
				template.add("outputcollection","w_"+port.getPortDeclaration()+"\n");
			}

		}

		dump("top_level_dummy.v", template);
	}

	public static void main(String [] args){
		//		String currentDir = System.getProperty("user.dir");
		//	    System.out.println("Current dir using System:"	 +currentDir);

		String cgraPath = args[0];
		String outputPath = args[1];

		CgraModelAmidar cgra = (CgraModelAmidar) target.Processor.Instance.getAttributeParser().loadCgra(cgraPath);

		cgra.finalizeCgra();

		VerilogGenerator gen = target.Processor.Instance.getGenerator();


		String path = System.getProperty("user.dir");


		path = path + "/../../Amidar/gen";
		System.out.println(path);
		gen.setInitializationPath(path);

		gen.printVerilogDescription(outputPath,cgra);



	}

}
