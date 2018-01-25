package generator;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroupFile;

import cgramodel.ActorInterface;
import cgramodel.AugmentedPE;
import cgramodel.AxiTransactionModel;
import cgramodel.CgraModel;
import cgramodel.CgraModelUltrasynth;
import cgramodel.ContextMaskPE;
import cgramodel.InterfaceContext;
import cgramodel.PEModel;
import generator.Module.Port;
import target.Processor;
import util.SimpleMath;

public class VerilogGeneratorUltrasynth extends VerilogGenerator {

	private JSONObject templateFilesJSON;
	private CgraModelUltrasynth ultrasynthModel;

	@Override
	public CgraModelUltrasynth getModel() {
		return ultrasynthModel;
	}

	public void setUltrasynthModel(CgraModelUltrasynth model) {
		super.setModel(model);
		ultrasynthModel = model;
	}

	public VerilogGeneratorUltrasynth(CgraModelUltrasynth model) {
		super(model);
		ultrasynthModel = model;
		parseTemplateFileNames();
	}

	public VerilogGeneratorUltrasynth() {
		super();
		parseTemplateFileNames();
	}

	private void parseTemplateFileNames() {
		templateFilesJSON = null;
		JSONParser parser = new JSONParser();
		FileReader fileReader;
		String templateFilesJsonPath = Processor.Instance.getHardwareTemplatePathProcessorRelated() + "/template_files.json";

		try {
			fileReader = new FileReader(templateFilesJsonPath);
			templateFilesJSON = (JSONObject) parser.parse(fileReader);
		} catch (FileNotFoundException e) {
			System.err.println("File not found - VerilogGeneratorUltrasynth : \"" + templateFilesJsonPath + "\"");
			e.printStackTrace(System.err);
		} catch (IOException e) {
			System.err.println("Error while reading template files description - VerilogGeneratorUltrasynth");
			e.printStackTrace(System.err);
		} catch (ParseException e) {
			System.err.println("Error while reading template files description - VerilogGeneratorUltrasynth ");
			e.printStackTrace(System.err);
		}
	}

	protected void printTopLevel() {
		String templateFileName = getTemplateFileName("T_CGRA");
		if (templateFileName == null)
			return;

		// Setup the ultrasynth module now (should be done elsewhere)
		final UltrasynthModule module = UltrasynthModule.createFromModel(ultrasynthModel);
		module.connectModules(ultrasynthModel, modules);

		final int maxContextWidth = ultrasynthModel.getMaxContextWidth();
		final int maxDataOffset = (maxContextWidth - 1) / 32 * 32;

		STGroupFile group = new STGroupFile(Processor.Instance.getHardwareTemplatePathProcessorRelated() +
											"/" + templateFileName, '§', '§');
		ST template = group.getInstanceOf("toplevel");

		int contextAddrWidth = ultrasynthModel.getCCNTWidth();
		template.add("date", date.toString());
		template.add("ccntwidth", contextAddrWidth);
		template.add("contextAddrWidth", contextAddrWidth);
		template.add("maxContextAddrWidth", ultrasynthModel.getOffsetAddrWidth());
		template.add("gLogContextAddrWidth", ultrasynthModel.getLogInterface().getGlobalContextAddrWidth());
		template.add("maxRFaddrWidth", ultrasynthModel.getMaxRegfileAddrWidth());
		template.add("contextDataWireWidth", 32 + ((maxContextWidth - 1) / 32 * 32));
		template.add("maxOCMContextAddrWidth", ultrasynthModel.getOcmInterface().getMaxContextAddrWidth());

		// CBox related stuff which was added later during a restructuring of the core CGRA
		template.add("nrOfCBoxOutputs", model.getcBoxModel().getCBoxPredicationOutputsPerBox() *
				model.getcBoxModel().getNrOfEvaluationBlocks());
		template.add("evaluationblocks", model.getcBoxModel().getNrOfEvaluationBlocks());
		template.add("branchselection", model.isConditionalJumps());

		for (PEModel pe : ultrasynthModel.getPEs()) {
		  
      int id            = pe.getID();
      int contextWidth  = pe.getContextWidth();
      int contextOffset = maxDataOffset - (contextWidth - 1) / 32 * 32;
      
      ST templatePE = group.getInstanceOf("peModule");
      templatePE.add("ID",           id);
      templatePE.add("contextwidth", contextWidth);
      
      generator.Module peModule = modules.get("PE" + id);
      if (peModule == null) throw new RuntimeException("Interface module for " + pe + " missing");
      
      // instantiate pe
      HashMap<String, String> wires = new HashMap<String, String>();
      List<Port> pePorts = peModule.getPorts();
      for (Port p : peModule.getPorts()) {
        String w = "";
             if (p.equals(Module.CLOCK))            w = "CGRA_CLK_I";
        else if (p.equals(Module.RESET))            w = "alu_reset";
        else if (p.equals(Module.CCNT))             w = "w_ccnt";
        else if (p.getName().startsWith("INPUT_"))  w = "w_direct_out_" + p.getName().substring(6);
        else if (p.equals(Module.LV_CONTEXT_WR_EN)) w = "parameter_context_write_enable_pe" + id;
        else if (p.equals(Module.LV_DATA))          w = "preped_context_pe" + id;
        else if (p.equals(Module.CONTEXT_DATA))     w = "w_context_data[" + (contextOffset + contextWidth - 1) + ":" + contextOffset + "]";
        else if (p.equals(Module.CONTEXT_WR_EN))    w = "context_write_enable_pe" + id;
        else if (p.equals(Module.CONTEXT_WR_ADDR))  w = "w_offset_addr[CONTEXT_ADDR_WIDTH-1:0]";
        else if (p.equals(Module.ROM_DATA))         w = "w_rom_data_pe_"   + id;
        else if (p.equals(Module.ROM_ADDR))         w = "w_rom_addr_pe_"          + id;
        else if (p.equals(Module.ROM_OFFSET))       w = "w_rom_offset_pe_"         + id;
        else if (p.equals(Module.ROM_VALID))        w = "w_rom_en_pe_" + id;
        else if (p.equals(Module.ROM_WIDE))         w = "w_rom_wide_pe_"   + id;
        else if (p.equals(Module.ROM_ARRAY))        w = "w_rom_array_pe_"   + id;
        else if (p.equals(Module.LIVE_IN))          w = "data_live_in";
        else if (p.equals(Module.PREDICATION))      w = "predication";
        else if (p.equals(Module.DIRECT_OUT))       w = "w_direct_out_" + id;
        else if (p.equals(Module.STATUS))         { w = "w_status_"     + id;
          template.add("statusIns", "\n\t.STATUS_" + id + "_I(" + w + "),");
          template.add("statusWireDeclaration", "wire " + w + ";\n");
          template.add("wires_status", "wire  " + w + ";\n");
        }
        
        wires.put(p.toString(), w);
      }
      
			// add all needed top level PE control wires
			template.add("wrCtrlPeContextEnWires",   "wire ctrl_contextEn_pe"   + id + ";\n");
			template.add("wrCtrlPeParameterEnWires", "wire ctrl_parameterEn_pe" + id + ";\n");

			// add ports to write the control module
			template.add("wrCtrlPeEnPorts", ".CONTEXT_WREN_PE"           + id + "_O(ctrl_contextEn_pe"   + id + "),\n");
			template.add("wrCtrlPeEnPorts", ".PARAMETER_CONTEXT_WREN_PE" + id + "_O(ctrl_parameterEn_pe" + id + "),\n");

			{// context mask for preparing a parameter write START
				ContextMaskPE con = pe.getContext();
				templatePE.add("maskZeroPadding1Len", 1 + con.getOpH()     - con.getOpL());
				templatePE.add("maskRFMuxLen",        1 + con.getMuxRegH() - con.getMuxRegL());
				templatePE.add("maskZeroPadding2Len", 2 + con.getMuxBH()   - con.getMuxBL() + con.getMuxAH() - con.getMuxAL());
				templatePE.add("regFileAddrWidth", SimpleMath.checkedLog(pe.getRegfilesize()));

				int paddingLen;
				if (pe.getMemAccess())
					paddingLen = 3 + con.getRdCacheH() - con.getRdCacheL() + con.getRddoH() - con.getRddoL() + con.getRdmuxH() - con.getRdmuxL();
				else
					paddingLen = 2 + con.getRddoH() - con.getRddoL() + con.getRdmuxH() - con.getRdmuxL();
				templatePE.add("maskZeroPadding3Len", paddingLen);

				if (pe.getMemAccess())
					paddingLen = con.getCond_dma() - con.getWr_en();
				else
					paddingLen = con.getCond_wr()  - con.getWr_en();
				templatePE.add("maskZeroPadding4Len", paddingLen);
			}// context mask for preparing a parameter write END

			templatePE.add("constAccess", pe.getMemAccess());
			template.add("pes", templatePE.render());
			template.add("pes", peModule.getInstance("i_pe"+id, wires));

			template.add("wires_direct_out_Pe", "wire ["+ pe.getMaxWidthResult() +"-1:0] w_direct_out_"+ id +";\n");
			if (pePorts.contains(generator.Module.LIVE_OUT) || pePorts.contains(generator.Module.ROM_VALID)) {
				template.add("wires_out_Pe", "wire ["+ pe.getMaxWidthResult() +"-1:0] w_pe_out_"+ id +";\n");
			}
		}

		{// additional context wire printing BEGIN
			int count = 1;
			int remainingContextWidth = maxContextWidth - 32;
			while (remainingContextWidth > 0) {
				remainingContextWidth -= 32;

				String port = ".DATA_" + count + "_O(w_incoming_context_data_" + count + "),\n";
				template.add("comUnitContextPorts", port);
				// add a context wire
				template.add("context_wire_decl", "wire [`SLAVE_DATA_WIDTH-1:0] w_incoming_context_data_" + count +";\n");
				// add the wire to the context_data aggregation
				template.add("context_wire_aggregation", ", w_incoming_context_data_" + count);
				++count;
			}
		}// additional context wire printing END

		{// SyncUnit START
			template.add("runCounterWidth", ultrasynthModel.getSyncUnit().getRunCounterWidth());
			template.add("syncUnitDataWidth", ultrasynthModel.getSyncUnit().getInputDataWidth());
			template.add("cycleCounterWidth", ultrasynthModel.getSyncUnit().getCycleCounterWidth());
		}// SyncUnit END

		{// Sensor START
			InterfaceContext sensorContext = ultrasynthModel.getSensorInterface().getContext();
			int sensorContextWidth = sensorContext.getContextWidth();
			int contextOffset = maxDataOffset - (sensorContextWidth - 1) / 32 * 32;
			template.add("sensorContextOffset", (contextOffset + sensorContextWidth - 1) + ":" + contextOffset);
		}// Sensor END
		
		{// Actor START
			ActorInterface.Context actorContext = ultrasynthModel.getActorInterface().getContext();
			int actorContextWidth = actorContext.getContextWidth();
			int contextOffset = maxDataOffset - (actorContextWidth - 1) / 32 * 32;
			template.add("actorContextOffset", (contextOffset + actorContextWidth - 1) + ":" + contextOffset);
			for (PEModel pe : model.getPEs())
				template.add("actorDataSelection", pe.getID() + ": actor_data = w_direct_out_" + pe.getID() + ";\n");
		}// Actor END

		{// Log/OCMBuffer START
			int globalLogWidth = ultrasynthModel.getLogInterface().getGlobalContext().getContextWidth();
			template.add("maxLogContextWidth", globalLogWidth); // this is always max because it grows with the PE log context widths

			int maxAddrWidth = Math.max(ultrasynthModel.getLogInterface().getMaxPELogAddrWidth(),
										ultrasynthModel.getLogInterface().getGlobalContextAddrWidth());
			template.add("maxLogContextAddrWidth", maxAddrWidth);

			for (PEModel pe : ultrasynthModel.getPEs()) {
				String port = ".CONTEXT_PE" + pe.getID() + "_WREN_I(ctrl_contextEn_pe" + pe.getID() + "),\n";
				template.add("logContextEnablePorts", port);
				port = ".DIRECT_OUT_PE" + pe.getID() + "_I(w_direct_out_" + pe.getID() + "),\n";
				template.add("PEDirectOutPorts", port);
			}
		}// Log/OCMBuffer END

		{// ROM instances START
			for (PEModel pe : ultrasynthModel.getPEs()) {
				if (pe.getRomAccess()) {
					RomModule romModule = (RomModule) modules.get(RomModule.getName(pe.getID()));
					template.add("rom_modules", romModule.getInstance("i_rom_" + pe.getID()));
					template.add("constBufOuts", romModule.getWireDeclarations());
					template.add("peRomOuts", module.getPeModules().get(pe.getID()).getWireDeclarations());
				}
			}
		}// ROM instances END

		Function<Integer, Integer> calcOffset = (width) -> maxDataOffset - (width - 1) / 32 * 32;

		int contextOffset = calcOffset.apply(ultrasynthModel.getContextmaskccu().getContextWidth());
		template.add("ccuContextOffset", (contextOffset + ultrasynthModel.getContextmaskccu().getContextWidth() - 1) + ":" + contextOffset);

		contextOffset = calcOffset.apply(ultrasynthModel.getcBoxComp().getMaxInputDataWidth());
		template.add("cboxContextOffset", (contextOffset + ultrasynthModel.getcBoxComp().getMaxInputDataWidth() - 1) + ":" + contextOffset);

		template.add("maxContextWidth", maxContextWidth);
		template.add("peIDWidth", ultrasynthModel.getPeIDWidth());
		template.add("otherIDWidth", ultrasynthModel.getOtherIDWidth());
		template.add("maxIDWidth", ultrasynthModel.getComUnit().getIDCAddrWidth());
		template.add("contextmemorysize", ultrasynthModel.getContextMemorySize());
		template.add("parameterBufferSize", ultrasynthModel.getParameterBuffer().getSize());
		template.add("parameterBufferCntrWidth", ultrasynthModel.getParameterBuffer().getFifoCntrWidth());

		dump(ultrasynthModel.getName() + ".v", template);
	}

	protected void prepare() {
		ultrasynthModel = (CgraModelUltrasynth) model;
	}

	@Override
	void printHostProcessorRelatedModules() {
		printComUnit();
		printInterfaceDefinitions();
		printWriteControl();
		printOCMBuffer();
		printLogBuffer();
		printActorIFDefs();
		printSensorIFDefs();
		printCgraDefs();
		printParameterBufferDefs();
	}

	private void printComUnit() {
		String templateFileName = getTemplateFileName("T_ComUnit");
		String fileName = getTemplateFileName("ComUnit");

		if (templateFileName == null || fileName == null)
			return;

		genFiles.add(fileName);

		STGroupFile group = new STGroupFile(Processor.Instance.getHardwareTemplatePathProcessorRelated() + "/" + templateFileName, '§', '§');
		ST template = group.getInstanceOf("comUnit");
		template.add("idcSize", ultrasynthModel.getComUnit().getIDCSize());
		template.add("idcAddrWidth", ultrasynthModel.getComUnit().getIDCAddrWidth());

		{ /* data path expansion START */
			int maxContextWidth = ultrasynthModel.getMaxContextWidth();

			if (maxContextWidth > 32) {
				template.add("bigContexts", true);

				int count = 1;
				int remainingContextWidth = maxContextWidth - 32;
				while (remainingContextWidth > 0) {
					// add port an additional port
					template.add("moreDataPorts", "output wire [`SLAVE_DATA_WIDTH-1:0] DATA_" + count + "_O,\n");
					// add another data reg declaration
					template.add("contextDataRegDecl", "reg [`SLAVE_DATA_WIDTH-1:0] data_" + count +";\n");
					// add the reg to the assignments section
					template.add("moreDataPortAssignments", "assign DATA_" + count + "_O = data_" + count + ";\n");
					// add it to the cascade
					if (count > 1) template.add("contextDataCascade", "data_" + count + " <= data_" + (count - 1) + ";\n");
					// update counters
					remainingContextWidth -= 32;
					++count;
				}

				template.add("validDataCntrWidth", SimpleMath.checkedLog(count));
			} else {
				template.add("bigContexts", false);
			}
		} /* data path expansion END */

		{ /* Big parameter handling START */
			AxiTransactionModel paramModel = ultrasynthModel.getParameterBuffer().getParameterAxiModel();

			if (paramModel.valueTransferCount > 0) {
				// We have big parameters to send and we assume that
				// all parameters will have the maximum width (aka the data path width)
				template.add("bigParameters", true);
				template.add("singleParamTransferCount", paramModel.valueTransferCount);
				template.add("paramValidCounterWidth", SimpleMath.checkedLog(paramModel.valueTransferCount));
			}
		} /* Big parameter handling END */
		
		{ /* Big context transaction handling START */
			List<AugmentedPE> peComps = ultrasynthModel.getPeComponents();
			List<AxiTransactionModel> transactions = ultrasynthModel.getOtherTransactions();

			// Temporarily save the edited templates by mapping their ID to the ST they are
			// using to check for the hold condition.
			HashMap<Integer, ST> stMap = new HashMap<>();
			String noMatch = "1'b1;";

			// As this is the first run we will have to generate new a ST every time
			for (AugmentedPE pe : peComps) {
				AxiTransactionModel axiLog = pe.getLogAxiModel();
				AxiTransactionModel axiPe = pe.getAxiModel();

				if (axiLog.valueTransferCount > 0) {
					ST validGenLine = group.getInstanceOf("validContextGenLine");
					validGenLine.add("otherCntrMatch", noMatch);
					validGenLine.add("peCntrMatch", renderCounterComparison(group, axiLog.valueTransferCount));
					validGenLine.add("id", axiLog.id);
					stMap.put(axiLog.id, validGenLine);
				}

				if (axiPe.valueTransferCount > 0) {
					ST validGenLine = group.getInstanceOf("validContextGenLine");
					validGenLine.add("otherCntrMatch", noMatch);
					validGenLine.add("peCntrMatch", renderCounterComparison(group, axiPe.valueTransferCount));
					validGenLine.add("id", axiPe.id);
					stMap.put(axiPe.id, validGenLine);
				}
			}

			// Now we have to check if we have seen an ID already
			for (AxiTransactionModel axiModel : transactions) {
				if (axiModel.valueTransferCount > 0) {
					ST validGenLine;

					if (stMap.containsKey(axiModel.id)) {
						validGenLine = stMap.get(axiModel.id);
						validGenLine.remove("otherCntrMatch");
					} else {
						validGenLine = group.getInstanceOf("validContextGenLine");
						validGenLine.add("peCntrMatch", noMatch);
						validGenLine.add("id", axiModel.id);
					}

					validGenLine.add("otherCntrMatch", renderCounterComparison(group, axiModel.valueTransferCount));
					stMap.put(axiModel.id, validGenLine);
				}
			}

			// Render all required ID checks in one go
			for (Map.Entry<Integer, ST> pair : stMap.entrySet()) {
				ST validGenLine = pair.getValue();
				template.add("validContextGen", validGenLine.render());
			}
		} /* Big context transaction handling END */

		dump(fileName, template);
	}

	private String renderCounterComparison(STGroupFile group, int size) {
		String str;

		if (size > 0) {
			ST counterComp = group.getInstanceOf("validContextMatchCounter");
			counterComp.add("width", (int) Math.ceil((Math.log(size) / Math.log(2))) + 1);
			counterComp.add("size", size);
			str = counterComp.render();
		} else {
			str = "1'b1;";
		}

		return str;
	}
	
	private void printInterfaceDefinitions() {
		String templateFileName = getTemplateFileName("T_InterfaceDefs");
		String fileName = getTemplateFileName("InterfaceDefs");
		if (templateFileName == null || fileName == null)
			return;

		STGroupFile group = new STGroupFile(Processor.Instance.getHardwareTemplatePathProcessorRelated() + "/" + templateFileName, '§', '§');
		ST template = group.getInstanceOf("definitions");

		template.add("singleRegIdWidth", ultrasynthModel.getSingleRegIDWidth());
		template.add("targetIdWidth", ultrasynthModel.getTargetIDWidth());
		template.add("peLogSelectionBit", ultrasynthModel.getPeLogSelectionBitPos());
		template.add("otherIdWidth", ultrasynthModel.getOtherIDWidth());
		template.add("offsetWidth", ultrasynthModel.getOffsetAddrWidth());

		for (AugmentedPE augmentedPE : ultrasynthModel.getPeComponents()) {
			template.add("peIDs", "`define   " + "ID_" + augmentedPE.getAxiModel().name + " " + augmentedPE.id + "\n");
			template.add("peIDs", "`define   " + "ID_" + augmentedPE.getLogAxiModel().name + " " + augmentedPE.getLogId() + "\n");
		}
		
		List<AxiTransactionModel> transactionTargets = ultrasynthModel.getOtherTransactions();
		for (AxiTransactionModel transaction : transactionTargets)
			template.add("otherIDs", "`define   " + "ID_" + transaction.name + " " + transaction.id + "\n");

		transactionTargets = ultrasynthModel.getSingleRegTransactions();
		for (AxiTransactionModel transaction : transactionTargets)
			template.add("singleRegIDs", "`define   " + "ID_" + transaction.name + " " + transaction.id + "\n");

		dump(fileName, template);
	}

	private void printWriteControl() {
		String templateFileName = getTemplateFileName("T_WrCtrl");
		String fileName = getTemplateFileName("WrCtrl");
		if (templateFileName == null || fileName == null)
			return;

		genFiles.add(fileName);
		STGroupFile group = new STGroupFile(Processor.Instance.getHardwareTemplatePathProcessorRelated() + "/" + templateFileName, '§', '§');
		ST template = group.getInstanceOf("wrCtrl");

		for (PEModel pe : ultrasynthModel.getPEs()) {
			int peID = pe.getID();

			template.add("peWrEnPorts", "output wire CONTEXT_WREN_PE" + peID + "_O,\n");
			template.add("peWrEnPorts", "output wire PARAMETER_CONTEXT_WREN_PE" + peID + "_O,\n");
			template.add("parameterWires", "reg parameterContextWrEn_pe" + peID + ";\n");
			template.add("parameterWires", "reg parameterContextWrEn_pe" + peID + "_sync;\n");
			template.add("parameterWires", "assign PARAMETER_CONTEXT_WREN_PE" + peID + "_O = parameterContextWrEn_pe" + peID + "_sync;\n");
			template.add("parameterWiresNull", "parameterContextWrEn_pe" + peID + " = 1'b0;\n");
			template.add("parameterWiresNotNull", "`ID_PE" + peID + ": parameterContextWrEn_pe" + peID + " = 1'b1;\n");
			template.add("parameterWiresNullSync", "parameterContextWrEn_pe" + peID + "_sync <= 1'b0;\n");
			template.add("parameterWiresNotNullSync", "parameterContextWrEn_pe" + peID + "_sync <= parameterContextWrEn_pe" + peID + ";\n");
			template.add("contextEnPE", "reg contextWrEn_pe" + peID + ";\n");
			template.add("contextEnPE", "reg contextWrEn_pe" + peID + "_sync;\n");
			template.add("contextEnPE", "assign CONTEXT_WREN_PE" + peID + "_O = contextWrEn_pe" + peID + "_sync;\n");
			template.add("contextEnPEnull", "contextWrEn_pe" + peID + " = 1'b0;\n");
			template.add("contextEnPEnotNull", "`ID_PE" + peID + ": contextWrEn_pe" + peID + " = VALID_CONTEXT_I;\n");
			template.add("contextEnPEnullSync", "contextWrEn_pe" + peID + "_sync <= 1'b0;\n");
			template.add("contextEnPEnotNullSync", "contextWrEn_pe" + peID + "_sync <= contextWrEn_pe" + peID + ";\n");
		}

		int cBoxEvalBlockIndex = 0;
		for (AxiTransactionModel transaction : ultrasynthModel.getcBoxComp().getEvalBlockContextTransactions()) {
			template.add("cBoxEvalContextNotNull", "`ID_CBoxEvalContext" + cBoxEvalBlockIndex +
					": cbox_eval_block_context_wren[" + cBoxEvalBlockIndex + "] = VALID_CONTEXT_I;\n");
			++cBoxEvalBlockIndex;
		}

		dump(fileName, template);
	}

	private void printLogBuffer() {
		final String templateFile = Processor.Instance.getHardwareTemplatePathProcessorRelated() + "/logbuffer.stg" ;
		STGroupFile group = new STGroupFile(templateFile, '§', '§');
		ST template = group.getInstanceOf("logBuffer");

		// Parameters
		template.add("globalLogSize", ultrasynthModel.getLogInterface().getGlobalContextSize());
		template.add("globalLogContextWidth", ultrasynthModel.getLogInterface().getGlobalContext().getContextWidth());
		template.add("globalLogContextAddrWidth", ultrasynthModel.getLogInterface().getGlobalContextAddrWidth());
		template.add("logIDWidth", ultrasynthModel.getPeIDWidth());
		template.add("logMaxAddrWidth", ultrasynthModel.getLogInterface().getMaxPELogAddrWidth());

		template.add("runCounterWidth", ultrasynthModel.getSyncUnit().getRunCounterWidth());
		template.add("ccntWidth", ultrasynthModel.getCCNTWidth());

		// Print logging facilities for each PE
		for (AugmentedPE augmentedPE : ultrasynthModel.getPeComponents()) {
			InterfaceContext logPEContext = augmentedPE.getLogContext();

			String enableWire = "enable_logContext_pe" + augmentedPE.id;
			String incomingEnable = "CONTEXT_PE" + augmentedPE.id + "_WREN_I";
			String incomingDirectOutPort = "DIRECT_OUT_PE" + augmentedPE.id + "_I";

			template.add("contextEnablePorts", "input wire " + incomingEnable + ",\n");
			template.add("logDataOutWires", "wire [`DATA_WIDTH-1:0] log_out" + augmentedPE.id + ";\n");
			template.add("dataToSendAssignment", augmentedPE.id + ": log_data = log_out" + augmentedPE.id + ";\n");
			template.add("wiresLogPEenable", "wire " + enableWire + ";\n");
			template.add("wiresLogPEenable", "assign " + enableWire + " = " + incomingEnable + " && IS_LOG_CONTEXT_I;\n");
			template.add("peDirectOut", "input wire [`DATA_WIDTH-1:0] " + incomingDirectOutPort + ",\n");

			ST logPE = group.getInstanceOf("logPE");
			logPE.add("id", augmentedPE.id);
			logPE.add("contextSize", ultrasynthModel.getContextMemorySize());
			logPE.add("contextAddrWidth", ultrasynthModel.getCCNTWidth());
			logPE.add("contextWidth", logPEContext.getContextWidth());
			logPE.add("logSize", augmentedPE.getLogSize());
			logPE.add("logAddrWidth", SimpleMath.checkedLog(augmentedPE.getLogSize()));

			template.add("logs", logPE.render());
		}

		// Find the max address with needed to write to any memory local to
		// the log buffer.
		int maxAddrWidth = Math.max(ultrasynthModel.getLogInterface().getMaxPELogAddrWidth(),
									ultrasynthModel.getLogInterface().getGlobalContextAddrWidth());
		template.add("maxAddrWidth", maxAddrWidth);

		final String outputFile = "logbuffer.v";
		genFiles.add(outputFile);
		dump(outputFile, template);
	}

	private void printOCMBuffer() {
		final String templateFile = Processor.Instance.getHardwareTemplatePathProcessorRelated() + "/ocmbuffer.stg" ;
		STGroupFile group = new STGroupFile(templateFile, '§', '§');
		ST template = group.getInstanceOf("ocmBuffer");

		// Parameters
		template.add("cgraContextSize", ultrasynthModel.getContextMemorySize());
		template.add("cgraContextAddrWidth", ultrasynthModel.getCCNTWidth());
		template.add("bufferContextWidth", ultrasynthModel.getOcmInterface().getGatherContext().getContextWidth());
		template.add("outputContextSize", ultrasynthModel.getOcmInterface().getOutputContextSize());
		template.add("outputContextWidth", ultrasynthModel.getOcmInterface().getOutputContext().getContextWidth());
		template.add("outputContextAddr_width", ultrasynthModel.getOcmInterface().getOutputContextAddrWidth());
		template.add("resultBufferSize", ultrasynthModel.getOcmInterface().getBufferSize());
		template.add("resultNufferWidth", ultrasynthModel.getOcmInterface().getBufferWidth());
		template.add("resultBufferAddrWidth", ultrasynthModel.getOcmInterface().getBufferAddrWidth());

		template.add("maxContextAddrWidth", ultrasynthModel.getOcmInterface().getMaxContextAddrWidth());

		// Print gathering facilities for each PE
		for (AugmentedPE augmentedPE : ultrasynthModel.getPeComponents()) {
			String incomingDirectOutPort = "DIRECT_OUT_PE" + augmentedPE.id + "_I";

			template.add("peDirectOut", "input wire [`DATA_WIDTH-1:0] " + incomingDirectOutPort + ",\n");
			template.add("resultBufferWrites", augmentedPE.id +
					": ocm_data[ocm_data_write_counter] <= DIRECT_OUT_PE" + augmentedPE.id + "_I;\n");
		}

		final String outputFile = "ocmbuffer.v";
		genFiles.add(outputFile);
		dump(outputFile, template);
	}

	private void printSensorIFDefs() {
		String templateFileName = getTemplateFileName("T_SensorIF_defs");
		String fileName = getTemplateFileName("SensorIF_defs");
		if (templateFileName == null || fileName == null)
			return;

		STGroupFile group = new STGroupFile(Processor.Instance.getHardwareTemplatePathProcessorRelated()
				+ "/" + templateFileName, '{', '}');
		ST template = group.getInstanceOf("sensorif_defs");

		template.add("sensorContextWidth", ultrasynthModel.getSensorInterface().getContext().getContextWidth());
		template.add("sensorIDWidth", ultrasynthModel.getSensorInterface().getContext().getAddrWidth());

		dump(fileName, template);
	}

	private void printActorIFDefs() {
		String templateFileName = getTemplateFileName("T_ActorIF_defs");
		String fileName = getTemplateFileName("ActorIF_defs");
		if (templateFileName == null || fileName == null)
			return;

		STGroupFile group = new STGroupFile(Processor.Instance.getHardwareTemplatePathProcessorRelated()
				+ "/" + templateFileName, '{', '}');
		ST template = group.getInstanceOf("actorif_defs");

		template.add("actorContextWidth", ultrasynthModel.getActorInterface().getContext().getContextWidth());
		template.add("actorIDWidth", ultrasynthModel.getActorInterface().getContext().getAddrWidth());
		
		dump(fileName, template);
	}

	private void printCgraDefs() {
		String templateFileName = getTemplateFileName("T_CGRA_defs");
		String fileName = getTemplateFileName("CGRA_defs");
		if (templateFileName == null || fileName == null)
			return;

		STGroupFile group = new STGroupFile(Processor.Instance.getHardwareTemplatePathProcessorRelated()
				+ "/" + templateFileName, '{', '}');
		ST template = group.getInstanceOf("cgra_defs");

		template.add("contextAddrWidth", ultrasynthModel.getCCNTWidth());
		template.add("contextSize", ultrasynthModel.getContextMemorySize());
		template.add("peCount", ultrasynthModel.getNrOfPEs());
		template.add("peIDWidth", ultrasynthModel.getPeIDWidth());
		template.add("rfAddrWidth", ultrasynthModel.getMaxRegfileAddrWidth());
		template.add("dataPathWidth", Processor.Instance.getDataPathWidth());
		template.add("cBoxEvalBlockCount", ultrasynthModel.getcBoxModel().getNrOfEvaluationBlocks());

		dump(fileName, template);
	}

	private void printParameterBufferDefs() {
		String templateFileName = getTemplateFileName("T_ParameterBuffer_defs");
		String fileName = getTemplateFileName("ParameterBuffer_defs");
		if (templateFileName == null || fileName == null)
			return;

		STGroupFile group = new STGroupFile(Processor.Instance.getHardwareTemplatePathProcessorRelated() + "/" + templateFileName, '{', '}');
		ST template = group.getInstanceOf("ParameterBuffer");

		template.add("bufSize", ultrasynthModel.getParameterBuffer().getSize());
		template.add("addrWidth", ultrasynthModel.getParameterBuffer().getFifoCntrWidth());
		template.add("parameterExpectedCounterWidth", ultrasynthModel.getParameterBuffer().getExpectedCntrWidth());

		dump(fileName, template);
	}

	@Override
	protected void printDummy(CgraModel cgra) {

	}

	@Override
	protected void addStaticFiles() {
		super.addStaticFiles(); // adds CCU
	}

	@Override
	protected void fillPortList() {

	}
	
	@Override
	public String getTemplateFileName(String templateName) {
		String templateFileName = (String) templateFilesJSON.get(templateName);

		if (templateFileName == null)
			System.out.println("WARNING: No " + templateName + " template file specified, skipping generation of " + templateName + ".");

		return templateFileName;
	}
}
