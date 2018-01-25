package generator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroupFile;

import cgramodel.PEModel;
import target.Processor;

public class TestbenchGeneratorAmidar extends TestbenchGenerator{


	TestbenchContextGenerator tbcongen;

	StimulusAmidar[] stimuli;

	public TestbenchGeneratorAmidar(VerilogGeneratorAmidar generator) {
		super(generator);
	}


	public void importAppAndPrintTestbench(String application){

		File dir = null;
		dir = new File(target.Processor.Instance.getHardwareDestinationPath());
		if(!dir.exists()){
			dir.mkdirs();
		}

		tbcongen = new TestbenchContextGenerator(generator.getModel());
		tbcongen.importApp(application);
		tbcongen.exportContext();
		printTestbench();
	}

	public void exportAppAndPrintTestbench(StimulusAmidar[] stimuli){
		this.stimuli= stimuli;
		// some magic
		printTestbench();
	}

	public void printTestbench(){


		Path source = Paths.get(target.Processor.Instance.getHardwareTemplatePathProcessorRelated() + "/wave.do");
		Path destination = Paths.get(target.Processor.Instance.getTestbenchDestinationPath() + "/wave.do");
		try{
			Files.copy(source, destination);
		} catch (IOException e) {
			System.err.println("IO Exception in printTestbench() with File " + source);
		}

		STGroupFile group = new STGroupFile(target.Processor.Instance.getHardwareTemplatePathProcessorRelated() + "/Testbench_template.stg", '§', '§');
		ST template = group.getInstanceOf("testbench_topmodule");

		template.add("date", generator.getDate());
		template.add("debugPath", target.Processor.Instance.getDebuggingPathFromHDLSimulationPath());
		template.add("app", "de.amidar.SimpleTest.main([Ljava.lang.String;)V-30-94");
		//		 setting up signals
		for(VerilogPort port: generator.getToplevelports()){
			String declaration = "";
			if(port.direction == portdirectionality.input){
				declaration += "reg ";
			}
			else{
				declaration += "wire ";
			}

			if(port.getPortwidth() > 1){
				declaration += "[" + port.getPortwidth() + "-1:0] ";
			}
			declaration +=port.getPortDeclaration() + "; \n";
			template.add("connectors", declaration);
		}

		String dut = "\n " + generator.getToplevelName() + " dut " + "( \n";
		int portcounter = generator.getToplevelports().size();
		for(VerilogPort port: generator.getToplevelports()){

			if(port.direction == portdirectionality.input){
				if(port.getName().equals("CACHE_VALID") || port.getName().equals("RST")){
					template.add("initiallizeSignals", port.getPortDeclaration()+" = 1'b1; \n");
				}
				else{
					template.add("initiallizeSignals", port.getPortDeclaration()+" = 0; \n");
				}
			}

			portcounter--;
			dut+="."+port.getPortDeclaration()+"("+ port.getPortDeclaration() + ")";
			if(portcounter > 0){
				dut+=",";
			}
			dut += "\n";
		}
		dut +=");";

		template.add("dutInstanciation", dut );
		template.add("dataBusWidth", Processor.Instance.getDataPathWidth());
		template.add("modelname", generator.getModel().getName());

		int maxContextWidth = 0;
		for(PEModel pe : generator.getModel().getPEs()){
			if(pe.getContextWidth() > maxContextWidth){
				maxContextWidth = pe.getContextWidth();
			}
		}
		if(generator.getModel().getcBoxModel().getContextmaskEvaLuationBlocks().getContextWidth() > maxContextWidth){
			maxContextWidth = generator.getModel().getcBoxModel().getContextmaskEvaLuationBlocks().getContextWidth();
		}
		
		

		template.add("maxContextWidth", maxContextWidth);
		template.add("nrOfPEs", generator.getModel().getPEs().size());
		template.add("cboxslots", generator.getModel().getcBoxModel().getMemorySlots());
		template.add("nrContexts", generator.getModel().getContextMemorySize());

		int variable = 0;
		for(PEModel pe : generator.getModel().getPEs()){
			ST contextinittemplate = group.getInstanceOf("init_context");
			ST regidebug = group.getInstanceOf("regDump");
			ST aludebug = group.getInstanceOf("aluDump");

			contextinittemplate.add("pathToContext",/*System.getProperty("user.dir")
				+ "/"+generator.getPathhelper().getTestbenchDestinationPath()+"/*/"Context_PE"+pe.getID()+".dat");
			contextinittemplate.add("name","pe_"+pe.getID());
			template.add("intialContexts","writeContextSet("+pe.getID()+");\n");//////////////////////////////////////////
			
			
			regidebug.add("id", pe.getID());
			regidebug.add("regfilesize",pe.getRegfilesize());
			regidebug.add("regfilevariable","var"+variable);
			
			aludebug.add("id", pe.getID());
			aludebug.add("controlflow", pe.getControlFlow());
			
			template.add("regfiledumps",regidebug.render());
			template.add("aluDump", aludebug.render());
			template.add("regfilevariables", ","+ "var"+variable++);
			
		}
		
		
		template.add("maxCycles", 12000);
		ST contextinittemplate = group.getInstanceOf("init_context");

		contextinittemplate.add("pathToContext",/*System.getProperty("user.dir")
				+ "/"+generator.getPathhelper().getTestbenchDestinationPath()+"/*/"Context_CCU.dat");
		contextinittemplate.add("name","controlunit");
		template.add("intialContexts","writeContextSet("+generator.model.getNrOfPEs()+");\n");///////////////////////////////7

		contextinittemplate = group.getInstanceOf("init_context");

		contextinittemplate.add("pathToContext",/*System.getProperty("user.dir")
				+ "/"+generator.getPathhelper().getTestbenchDestinationPath()+"/*/"Context_CBOX.dat");
		contextinittemplate.add("name","magical");
		template.add("intialContexts","writeContextSet("+(generator.model.getNrOfPEs()+1)+");\n");/////////////////////////////////

		for(StimulusAmidar littlestimi : stimuli){
			template.add("stimuli", littlestimi.taskCall());
		}
		generator.dump("Testbench.sv", template);

		createScripts();
	}
	
	
	private void createScripts(){
		
		try {
			FileWriter TCLAutomated = new FileWriter(new File(target.Processor.Instance.getHardwareDestinationPath()+"/simscriptmodelsim.tcl"));
			BufferedWriter bwTCLa = new BufferedWriter(TCLAutomated);
			File simtrigger =new File(target.Processor.Instance.getHardwareDestinationPath()+"/"+"simtrigger.sh");
			FileWriter fwSimTrigger = new FileWriter(simtrigger);
			BufferedWriter bwSimTrigger = new BufferedWriter(fwSimTrigger);
			File TCLmanual =new File(target.Processor.Instance.getHardwareDestinationPath()+"/"+"simtriggerManual.sh");
			FileWriter fwTCLManual = new FileWriter(TCLmanual);
			BufferedWriter bw3 = new BufferedWriter(fwTCLManual);
			bwSimTrigger.write("#!/bin/bash \n");
			//				bw2.write(". $ISE_ROOT/default/ISE_DS/settings64.sh &> /dev/null "
			//						+ "export MODELSIM=$XILINX/verilog/xst/modelsim.ini"
			//						+ "export LC_NUMERIC=en_US.UTF-8\n");
			
			// /opt/tools/modelsim/10.3c/modeltech/bin
			
			bwSimTrigger.write("export LM_LICENSE_FILE=1717@idefix.ies.e-technik.tu-darmstadt.de \n");
			String modelsimFolder = ""; // 
			if(new File(Processor.Instance.getConfigurationPath()+"/vsimDir").exists()){
				System.out.println("file detected");
				BufferedReader reader = new BufferedReader(new FileReader(Processor.Instance.getConfigurationPath()+"/vsimDir"));
				modelsimFolder = reader.readLine();
				System.out.println("modelsim folder : " + modelsimFolder);
			}
			else{
				System.out.println("else pfad");
				File dir = new File("/opt/tools/modelsim");
				File[] files = dir.listFiles();
				for(File folder : files){
					if(folder.toString().contains(".")){
						dir = folder.getAbsoluteFile();
						break;
					}
				}
				modelsimFolder = dir.toString() + "/modeltech/bin"; 
				FileWriter fw = new FileWriter(new File (Processor.Instance.getConfigurationPath()+"/vsimDir"));
				fw.write(modelsimFolder);
				fw.close();
			}			
			bwSimTrigger.write(modelsimFolder + "/vsim -c -do "+ target.Processor.Instance.getHardwareDestinationPath() +"/simscriptmodelsim.tcl");
			bwSimTrigger.close();
			bwTCLa.write("cd "+target.Processor.Instance.getTestbenchDestinationPath()+"\n");
			bwTCLa.write("vlib cgra_work\n"
					+ "vmap work cgra_work\n"
					//						+ "	vlog Testbench.sv \n"
					);
			fwTCLManual.write("vlib cgra_work\n"
					+ "vmap work cgra_work\n"
					//						+ "	vlog Testbench.sv \n"
					);

			for(String module: generator.getModules()){
				bwTCLa.write("vlog "+ module +"\n");
				fwTCLManual.write("vlog "+ module +"\n");
			}
			
			bwTCLa.write( "vsim testbench_cgra -novopt\n"
					+ "run -all\n"
					+"quit -f\n");
			fwTCLManual.write( "vsim testbench_cgra -novopt\n"
					+"do wave.do\n"
					+ "run -all\n");
			
			bwTCLa.close();
			fwTCLManual.close();
			TCLAutomated.close();
			
		} catch (IOException e) { // TODO
			e.printStackTrace();
		}
	}


	public String getBitString(int context){
		String val = Integer.toBinaryString(context);
		int l = val.length();

		int diff = 32 - l;

		if(diff < 0){
			System.err.println("getBitString Error in TestbenchGeneratorAmidar");
			System.err.println(val);
		}else{

			char[] padding = new char[diff];

			for(int i = 0; i<diff; i++){
				padding[i] = '0';
			}

			val = new String(padding) + val;
		}

		return val;
	}

}
