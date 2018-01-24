package generator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiFunction;
import java.util.function.Function;

import io.GeneratedSourceFile;
import io.SourceFile;
import io.SourceFileLib;
import io.TemplateSourceFile;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroupFile;

import cgramodel.AugmentedPE;
import cgramodel.AxiTransactionModel;
import cgramodel.AxiTransactionModel.SingleRegTag;
import cgramodel.CgraModelUltrasynth;
import cgramodel.ContextMask;
import cgramodel.ContextMaskContextControlUnit;
import cgramodel.ContextMaskPE;
import cgramodel.InterfaceContext;
import cgramodel.LogGlobalContext;
import cgramodel.OCMInterface;
import cgramodel.PEModel;
import target.Processor;
import target.UltraSynth;
import util.SimpleMath;

public class TestbenchGeneratorUltrasynth extends TestbenchGenerator {

	private final int runUntil = 42;
	private final String destinationFolder;
	private CgraModelUltrasynth model;

	public TestbenchGeneratorUltrasynth(CgraModelUltrasynth model) {
		super(null);
		this.model = model;
		this.destinationFolder = UltraSynth.Instance.getHardwareDestinationPath() + "/" + model.getName();
	}

	void generate(CgraModelUltrasynth model, SourceFileLib sourceFileLib,
				  int sourceIndex, DriverUltrasynth.Config config)
	{
		this.model = model;

		TemplateSourceFile templateSource = sourceFileLib.getSourceFile(
				sourceIndex, "tb", TemplateSourceFile.class
		);

		if (templateSource == null)
			return;

		{	// test bench top level
			STGroupFile group = templateSource.getStGroupFile();
			ST template = templateSource.getTargetTemplate();

			// common information
			template.add("contextAddrWidth", SimpleMath.checkedLog(model.getContextMemorySize()));
			template.add("maxContextAddrWidth", model.getOffsetAddrWidth());
			template.add("idTabAddrWidth", model.getComUnit().getIDCAddrWidth());
			template.add("maxRFAddrWidth", model.getMaxRegfileAddrWidth());
			template.add("maxContextWidth", model.getMaxContextWidth());
			template.add("peIDWidth", model.getPeIDWidth());

			// this switches on/off everything related to test sequence writing and checking
			template.add("random", config.generateRandomContexts);

			template.add("seqFileName", generator.getTemplateFileName("SingleTestSeq"));
			template.add("localVarsFile", generator.getTemplateFileName("TestLocals"));
			template.add("runUntil", runUntil);
			template.add("contextMemSize", model.getContextMemorySize());
			template.add("expParamCount", model.getParameterBuffer().getMaxExpectedParamerters());
			template.add("ocmInc", (runUntil/2 + 4) * 8);
			template.add("logInc", model.getLogInterface().getGlobalContextTestSize() * 8);
			template.add("ocmBound", (runUntil/2 + 4) * 2 * 8);
			template.add("logBound", (model.getLogInterface().getGlobalContextTestSize()
					* 2 + (runUntil/2 + 4) * 2) * 8);

			// master tests
			for (int i = 0; i < model.getNrOfPEs(); ++i)
				template.add("masterTestData",
						"$readmemb(\"log_" + i + "_data.dat\", cgra.log.logPE" + i + ".log1);\n");

			template.add("ocmFifoSize", model.getOcmInterface().getBufferSize());

			// context checks and writes
			String renderedTemplate;
			for (AugmentedPE augmentedPE : model.getPeComponents()) {
				int peID = augmentedPE.id;
				PEModel pe = model.getPEs().get(peID);

				ST rfCheckTemplate = group.getInstanceOf("rfCheck");
				rfCheckTemplate.add("peID", peID);

				InterfaceContext logContext = augmentedPE.getLogContext();
				renderedTemplate = rfCheckTemplate.render();
				template.add("rfChecks", renderedTemplate);

				renderedTemplate = renderTestBenchContextWriteTemplate(group, augmentedPE.getAxiModel());
				template.add("peWrites", renderedTemplate);

				renderedTemplate = renderTestBenchContextCheckTemplate(group, augmentedPE.getAxiModel(),
						"pe_" + peID + ".contextmemory");
				template.add("peContextChecks", renderedTemplate);

				renderedTemplate = renderTestBenchContextWriteTemplate(group, augmentedPE.getLogAxiModel());
				template.add("peWrites", renderedTemplate);

				renderedTemplate = renderTestBenchContextCheckTemplate(group, augmentedPE.getLogAxiModel(),
						"log.logPE" + peID + ".logContext");
				template.add("peContextChecks", renderedTemplate);
			}

			// render the other write templates
			for (AxiTransactionModel axiModel : model.getOtherTransactions()) {
				if (axiModel.name.equals("CBoxContext") && !model.isConditionalJumps() &&
						!(model.getcBoxModel().getBranchSelectionSources().size()  > 1))
					continue; // We skip the generation to avoid hierarchical path errors during sim

				renderedTemplate = renderTestBenchContextWriteTemplate(group, axiModel);
				template.add("otherWrites", renderedTemplate);
			}

			{
				// Render the CBox evaluation block checkers
				int i = 0;
				for (AxiTransactionModel axiModel : model.getcBoxComp().getEvalBlockContextTransactions()) {
					renderedTemplate = renderTestBenchContextCheckTemplate(group, axiModel,
							"cBoxWrapper.generatedEvaluationBlocks[" + i + "].cBox.contextmemory");
					template.add("otherContextChecks", renderedTemplate);
					++i;
				}

				// Render the CBox checker only if it was generated
				if (model.isConditionalJumps() && model.getcBoxModel().getBranchSelectionSources().size()  > 1) {
					renderedTemplate = renderTestBenchContextCheckTemplate(group, model.getcBoxComp().getAxiModel(),
							"cBoxWrapper.contextmemory");
					template.add("otherContextChecks", renderedTemplate);
				}
			}

			// render the remaining checkers
			renderedTemplate = renderTestBenchContextCheckTemplate(group, model.getCcuComp().getAxiModel(),
					"controlunit.contextmemory");
			template.add("otherContextChecks", renderedTemplate);

			renderedTemplate = renderTestBenchContextCheckTemplate(group, model.getLogInterface().getAxiModel(),
					"log.logGlobalContext");
			template.add("otherContextChecks", renderedTemplate);

			renderedTemplate = renderTestBenchContextCheckTemplate(group, model.getActorInterface().getAxiModel(),
					"actor_interface.context_memory");
			template.add("otherContextChecks", renderedTemplate);

			renderedTemplate = renderTestBenchContextCheckTemplate(group, model.getSensorInterface().getAxiModel(),
					"sensor_interface.context_memory");
			template.add("otherContextChecks", renderedTemplate);

			renderedTemplate = renderTestBenchContextWriteTemplate(group, model.getOcmInterface().getAxiModel());
			template.add("otherWrites", renderedTemplate);

			renderedTemplate = renderTestBenchContextCheckTemplate(group, model.getOcmInterface().getAxiModel(),
					"log.ocm_context");
			template.add("otherContextChecks", renderedTemplate);

			renderedTemplate = renderTestBenchContextCheckTemplate(group, model.getConstBuffer().getAxiModel(),
					"constBuf.mem0.mem");
			template.add("otherContextChecks", renderedTemplate);
		}

		// other modules and test data
		if (config.generateRandomContexts) {
			generateContexts();
			generateTestLocalVars(model, sourceFileLib, sourceIndex, config);
			generateTestSequence();
		}

		generateOtherTestModules(sourceFileLib, sourceIndex);
		createScripts();
	}

	/**
	 * Generates the slaves taking data from the log/ocm master interfaces.
	 */
	private void generateOtherTestModules(SourceFileLib sourceFileLib, int sourceIndex) {
		TemplateSourceFile templateSource = sourceFileLib.getSourceFile(
				sourceIndex, "slavesim", TemplateSourceFile.class
		);

		if (templateSource == null)
			return;

		ST template = templateSource.getTargetTemplate();

		for (AugmentedPE augmentedPE : model.getPeComponents()) {
			int id = augmentedPE.id;

			template.add("fileDecl", "int fileOut" + id + ";\n");
			template.add("fileClose", "$fclose(fileOut" + id + ");\n");
			template.add("fileOpen", "fileOut" + id + " = $fopen(\"logOut" + id + "\", \"w\");\n");

			// for simpler testing: get the ONE big log BRAM by its ID, write the "out file" only once (during the first transaction)
			if (id == model.getLogInterface().getIdOfBiggestLog())
				template.add("fileWrites", id + ": if (0 == testCoutner) $fwrite(fileOut" + id + " ,\"%b\\n\", M_AXI_WDATA_I);\n");
			else
				template.add("fileWrites", id + ": $fwrite(fileOut" + id + " ,\"%b\\n\", M_AXI_WDATA_I);\n");
		}
	}

	/**
	 * Generates contexts for testing purposes. Has the added side effect of building input 
	 * and expected output data files for Log master testing. 
	 */
	private void generateContexts() {
		System.out.println("Creating additional test contexts ...");
		String fileName;

		// === random contexts === 
		fileName = "Context_" + model.getActorInterface().getAxiModel().name + ".dat";
		writeRandomContext(model.getActorInterface().getContext(), model.getContextMemorySize(), fileName);
		fileName = "Context_" + model.getSensorInterface().getAxiModel().name + ".dat";
		writeRandomContext(model.getSensorInterface().getContext(), model.getContextMemorySize(), fileName);
		fileName = "Context_" + model.getOcmInterface().getAxiModel().name + ".dat";
		writeRandomContext(model.getOcmInterface().getGatherContext(), model.getContextMemorySize(), fileName);

		if (model.getcBoxModel().getContextmaskWrapper().getContextWidth() != 0){
			// A context file for the cbox wrapper is only required if there will be a context
			// in the hardware description.
			fileName = "Context_" + model.getcBoxComp().getAxiModel().name + ".dat";
			writeRandomContext(model.getcBoxModel().getContextmaskWrapper(), model.getContextMemorySize(), fileName);
		}

		for (AxiTransactionModel axiModel : model.getcBoxComp().getEvalBlockContextTransactions()) {
			fileName = "Context_" + axiModel.name + ".dat";
			writeRandomContext(model.getcBoxModel().getContextmaskEvaLuationBlocks(), model.getContextMemorySize(), fileName);
		}

//		fileName = "Context_" + model.getConstBuffer().getAxiModel().name + ".dat";;
//		ContextMask dummy = new ContextMask();
//		dummy.setContextWidth(Processor.Instance.getDataPathWidth());
//		writeRandomContext(dummy, model.getConstBuffer().getSize(), fileName);

		// === PE-/log random contexts ===
		for (AugmentedPE augmentedPE : model.getPeComponents()) {
			fileName = "Context_" + augmentedPE.getLogAxiModel().name + ".dat";
			InterfaceContext mask = augmentedPE.getLogContext();
			writeRandomContext(mask, model.getContextMemorySize(), fileName);

			fileName = "Context_" + augmentedPE.getAxiModel().name + ".dat";
			ContextMaskPE peMask = model.getPEs().get(augmentedPE.id).getContextMaskPE();
			writeRandomContext(peMask, model.getContextMemorySize(), fileName);
		}
		
		// lambda factory
		BiFunction<BufferedWriter, String, Function<Object,Integer>> writerFactory =
		(fileWriter, usedfileName) ->
			(toWrite) -> {
				try {
					fileWriter.write(toWrite.toString());
					fileWriter.newLine();
					return 1;
				} catch (IOException e) {
					System.err.printf("Could not write file \"%s\" - TestbenchGeneratorUltrasynth\n", usedfileName);
					e.printStackTrace();
				}
				return 0;
			};

		Function<Integer,Long> randomBits =
		(randomBitCount) -> {
			long rand = ThreadLocalRandom.current().nextLong(0, Long.MAX_VALUE);
			return ~(Long.MAX_VALUE << randomBitCount) & rand;
		};

		// === log master test ===
		{
			String file = "Context_GlblLogContext.dat";

			// preparations
			int biggestLogSize = model.getLogInterface().getBiggestLogSize();
			int idOfBiggestLog = model.getLogInterface().getIdOfBiggestLog();

			Function<Integer,String> randBinString = (bits) -> {
				long rand = randomBits.apply(bits);
				StringBuilder sb = new StringBuilder(bits);
				for (int i = 0; i < bits; ++i) {
					long trunc = rand >> (bits - i - 1);
					if (trunc % 2 == 0)
						sb.append('0');
					else
						sb.append('1');
				}
				return sb.toString();
			};
		
			BufferedWriter contextFileWriter = null;
			try {
				contextFileWriter = new BufferedWriter(new FileWriter(new File(destinationFolder + "/" + file)));
			} catch (IOException e) {
				System.err.printf("Could not open file \"%s\" - TestbenchGeneratorUltrasynth", file);
				e.printStackTrace();
			}
			Function<Object, Integer> contextWriter = writerFactory.apply(contextFileWriter, file);

			BufferedWriter expectedOutputFileWriter = null;
			try {
				expectedOutputFileWriter = new BufferedWriter(new FileWriter(new File(destinationFolder + "/" + "logMasterOutExpected.dat")));
			} catch (IOException e) {
				System.err.printf("Could not open file \"%s\" - TestbenchGeneratorUltrasynth", "logMasterOutExpected.dat");
				e.printStackTrace();
			}
			Function<Object, Integer> expectedOutput = writerFactory.apply(expectedOutputFileWriter, "logMasterOutExpected.dat");

			ArrayList<Function<Object, Integer>> dataWriters = new ArrayList<>();
			ArrayList<BufferedWriter> fileWriters = new ArrayList<>();

			for (AugmentedPE augmentedPE : model.getPeComponents()) {
				String dataFileName = "log_" + augmentedPE.id + "_data.dat";
				BufferedWriter writer;
				try {
					writer = new BufferedWriter(new FileWriter(new File(destinationFolder + "/" + dataFileName)));
					fileWriters.add(writer);
					dataWriters.add(writerFactory.apply(writer, dataFileName));
				} catch (IOException e) {
					System.err.printf("Could not open file \"%s\" - TestbenchGeneratorUltrasynth", dataFileName);
					e.printStackTrace();
				}
			}

			LogGlobalContext globalContext = model.getLogInterface().getGlobalContext();
			int line = 0;

			// the first context of a burst
			long contextStart = 0;
			contextStart = globalContext.setAWvalid(contextStart, 1);
			// transfer contexts
			long contextTransfer = 0;
			contextTransfer = globalContext.setLogID(contextTransfer, idOfBiggestLog);

			// send the biggest log BRAM, tests sequential reads
			ArrayList<String> bigLogData = new ArrayList<>();
			int remaining = biggestLogSize + 1; // + 1 for the tag info send during the first transaction of a set
			boolean firstTransaction = true;

			while (remaining > 0) {
				Function<Object, Integer> dataWriter = dataWriters.get(idOfBiggestLog);
				int length = remaining <= 256 ? remaining : 256;
				contextStart = globalContext.setBurstLength(contextStart, length-1);
				line += contextWriter.apply(globalContext.getBitString(contextStart));

				if (firstTransaction) {
					// the first transfer is the tag info
					--remaining; // -> decrement remaining, no context entry needed for this transfer
					--length; // -> needed to run the next for loop correctly
					firstTransaction = false;
				}

				for (int i = 0; i < length; ++i) {
					contextTransfer = globalContext.setReadAddr(contextTransfer, biggestLogSize - remaining--);
					line += contextWriter.apply(globalContext.getBitString(contextTransfer));
					String dataString = randBinString.apply(Processor.Instance.getDataPathWidth());
					dataWriter.apply(dataString);
					expectedOutput.apply(dataString);
					bigLogData.add(dataString);
				}
			}

			// send mixed log entries, tests random access reads
			for (int readIndex = 0; readIndex < biggestLogSize; ++readIndex) {
				boolean started = false;
				contextTransfer = globalContext.setReadAddr(contextTransfer, readIndex);

				remaining = 0;

				for (AugmentedPE augmentedPE : model.getPeComponents()) {
					int size = augmentedPE.getLogSize();
					remaining += readIndex < size ? 1 : 0;
				}

				// the transfers of a transaction, one for each PE log instance
				for (AugmentedPE augmentedPE : model.getPeComponents()) {
					int logID = augmentedPE.id;

					if (augmentedPE.getLogSize() <= readIndex)
						continue;

					if (!started) {
						int length = remaining <= 256 ? remaining : 256;
						contextStart = globalContext.setBurstLength(contextStart, length-1);
						line += contextWriter.apply(globalContext.getBitString(contextStart));
						started = true;
					}

					// do the actual context write
					contextTransfer = globalContext.setLogID(contextTransfer, logID);
					line += contextWriter.apply(globalContext.getBitString(contextTransfer));

					// expand input and expected output data files corresponding to the current logID
					if (logID == idOfBiggestLog) {
						expectedOutput.apply(bigLogData.get(readIndex)); // use previously generated data
					} else {
						Function<Object, Integer> dataWriter = dataWriters.get(logID);
						String data = randBinString.apply(Processor.Instance.getDataPathWidth());
						dataWriter.apply(data);
						expectedOutput.apply(data);
					}

					if (0 == --remaining) 
						started = false;
				}
			}

			long contextDone = 0;
			contextDone = globalContext.setDone(contextDone, 1);
			long contextCCNT = 0;
			contextCCNT = globalContext.setCcntStart(contextCCNT, runUntil);

			line += contextWriter.apply(globalContext.getBitString(contextDone));

			// write the CCNT value to start the log transactions in all remaining lines
			while (line < model.getLogInterface().getGlobalContextTestSize())
				line += contextWriter.apply(globalContext.getBitString(contextCCNT));

			// close all files
			try {
				for (BufferedWriter writer : fileWriters)
					writer.close();
				expectedOutputFileWriter.close();
				contextFileWriter.close();
			} catch (IOException e) {
				System.err.println("Could not close a file related to global log context and data generation - TestbenchGeneratorUltrasynth");
				e.printStackTrace();
			}
		}

		// === ocm master test ===
		{
			String file = "Context_OCMContext.dat";
			BufferedWriter contextFileWriter = null;
			try {
				contextFileWriter = new BufferedWriter(new FileWriter(new File(destinationFolder + "/" + file)));
			} catch (IOException e) {
				System.err.printf("Could not open file \"%s\" - TestbenchGeneratorUltrasynth\n", file);
				e.printStackTrace();
			}

			OCMInterface.Context ocmContext = model.getOcmInterface().getGatherContext();
			Function<Object, Integer> contextWriter = writerFactory.apply(contextFileWriter, file);
			int line = 0;

			long bufferWrite = 0; // take data from PE0 (test bench will provide data on this wire)
			bufferWrite = ocmContext.setEnable(bufferWrite, 1);
			while (line < runUntil/2)
				line += contextWriter.apply(ocmContext.getBitString(bufferWrite));

			long contextCompete = 0;
			contextCompete = ocmContext.setComplete(contextCompete, 1);
			while (line < model.getContextMemorySize())
				line += contextWriter.apply(ocmContext.getBitString(contextCompete));

			try {
				contextFileWriter.close();
			} catch (IOException e) {
				System.err.printf("Could not close file \"%s\" - TestbenchGeneratorUltrasynth\n", file);
				e.printStackTrace();
			}
		}

		// === CCU ===
        {
			fileName = "Context_" + model.getCcuComp().getAxiModel().name + ".dat";

			try {
				ContextMaskContextControlUnit mask = model.getContextmaskccu();
				BufferedWriter fileWriter = new BufferedWriter(new FileWriter(new File(destinationFolder + "/" + fileName)));
				int line;

				for (line = 0; line < runUntil; ++line) {
					fileWriter.write(mask.getBitString(0));
					fileWriter.newLine();
				}

				long jump = ~(Long.MAX_VALUE << (mask.getContextWidth())) & Long.MAX_VALUE;
				jump = mask.setConditional(jump, false);
				jump = mask.setRelative(jump, false);
				fileWriter.write(mask.getBitString(jump));
				fileWriter.newLine();
				++line;

				for (; line < model.getContextMemorySize() - 1; ++line) {
					fileWriter.write(mask.getBitString(0));
					fileWriter.newLine();
				}

				fileWriter.write(mask.getBitString(jump));
				fileWriter.newLine();

				fileWriter.close();
			} catch (IOException e) {
				System.err.printf("Could not write file \"%s\" - TestbenchGeneratorUltrasynth\n", fileName);
				e.printStackTrace();
			}
		}
		System.out.println("Done: Creating additional test contexts");
	}

	private void writeRandomContext(ContextMask mask, int contextSize, String fileName) {
		try {
			BufferedWriter fileWriter = new BufferedWriter(new FileWriter(new File(destinationFolder + "/" + fileName)));

			for (int line = 0; line < contextSize - 1; ++line) {
				long rand = ThreadLocalRandom.current().nextLong(0, Long.MAX_VALUE);
				rand = ~(Long.MAX_VALUE << (mask.getContextWidth())) & rand;
				fileWriter.write(mask.getBitString(rand));
				fileWriter.newLine();
			}

			fileWriter.write(mask.getBitString(0));
			fileWriter.newLine();

			fileWriter.close();
		}
		catch (IOException e) {
			System.err.printf("Could not write file \"%s\" - TestbenchGeneratorUltrasynth", fileName);
			e.printStackTrace();
		}
	}

	/**
	 * Generates all (shell) scripts needed for running the test bench.
	 */
	private void createScripts() {
		final String compileTclName = generator.getTemplateFileName("CompileScript");
		final String simTclName = generator.getTemplateFileName("SimScript");
		final String triggerComName = generator.getTemplateFileName("CompileTrigger");
		final String triggerSimName = generator.getTemplateFileName("SimTrigger");

		System.out.println("Creating Scripts ...");
		try {
			if (compileTclName == null) {
				System.out.println("Could not find template file name. Aborting compilation TCL script creation!");
				return;
			}
			BufferedWriter compileTclWriter = new BufferedWriter(new FileWriter(new File(destinationFolder + "/" + compileTclName)));
			
			// prepare run and add the test bench files to the used modules
			compileTclWriter.write("cd "+ destinationFolder);
			compileTclWriter.newLine();
			compileTclWriter.write("vlib cgra_work");
			compileTclWriter.newLine();
			compileTclWriter.write("vmap work cgra_work");
			compileTclWriter.newLine();

			// add all modules which are part of the model
			for (String module : generator.getModules()) {
				compileTclWriter.write("vlog "+ module);
				compileTclWriter.newLine();
			}

			compileTclWriter.write("quit -f");
			compileTclWriter.newLine();
			
			compileTclWriter.close();
		} catch (IOException e) {
			System.err.printf("Could not write file \"%s\" - TestbenchGeneratorUltrasynth", compileTclName);
			e.printStackTrace();
		}

		try {
			if (simTclName == null) {
				System.out.println("Aborting simulation TCL script creation!");
				return;
			}
			BufferedWriter simTclWriter = new BufferedWriter(new FileWriter(new File(destinationFolder + "/" + simTclName)));

			// prepare run
			simTclWriter.write("cd "+ destinationFolder);
			simTclWriter.newLine();
			simTclWriter.write("vmap work cgra_work");
			simTclWriter.newLine();

			// print actual simulation commands
			simTclWriter.write("vsim tb bind_SVA");
			simTclWriter.newLine();
			simTclWriter.write("run -all");
			simTclWriter.newLine();
			simTclWriter.write("quit -f");
			simTclWriter.newLine();

			simTclWriter.close();
		} catch (IOException e) {
			System.err.printf("Could not write file \"%s\" - TestbenchGeneratorUltrasynth", simTclName);
			e.printStackTrace();
		}

		try {
			if (triggerComName == null) {
				System.out.println("Aborting compilation trigger creation!");
				return;
			}
			BufferedWriter trigCompileWriter = new BufferedWriter(new FileWriter(new File(destinationFolder + "/" + triggerComName)));
			
			trigCompileWriter.write("export LM_LICENSE_FILE=1717@idefix.ies.e-technik.tu-darmstadt.de");
			trigCompileWriter.newLine();
			trigCompileWriter.write(getModelSimPath() + "/vsim -c -do " 
								+ destinationFolder + "/"
								+ compileTclName);
			trigCompileWriter.newLine();

			trigCompileWriter.close();
		}
		catch (IOException e) {
			System.err.printf("Could not write file \"%s\" - TestbenchGeneratorUltrasynth", triggerComName);
			e.printStackTrace();
		}

		try {
			if (triggerSimName == null) {
				System.out.println("Aborting simulation trigger creation!");
				return;
			}
			BufferedWriter trigSimWriter = new BufferedWriter(new FileWriter(new File(destinationFolder + "/" + triggerSimName)));
			
			trigSimWriter.write(getModelSimPath() + "/vsim -c -do " 
								+ destinationFolder + "/"
								+ simTclName);
			trigSimWriter.newLine();
			trigSimWriter.close();
		}
		catch (IOException e) {
			System.err.printf("Could not write file \"%s\" - TestbenchGeneratorUltrasynth", triggerSimName);
			e.printStackTrace();
		}	
	}

	/**
	 * Generates all sequences used to test the current composition
	 */
	private void generateTestSequence() {
		final String sequenceFile = generator.getTemplateFileName("SetupSequence");
		if (sequenceFile == null) {
			System.out.println("Could not find template file name. Aborting test sequence creation!");
			return;
		}

		// filter out PEs with different sizes
		ArrayList<Integer> testPEs = new ArrayList<>(8);
		for (int i = 0; i < model.getMaxContextWidth(); i += 32) {
			for (PEModel pe : model.getPEs()) {
				if (pe.getContextWidth() <= i + 32 && pe.getContextWidth() > i) {
					testPEs.add(pe.getID());
					break;
				}
			}
		}

		// get other modules (CCU, CBOX, IDC, Global Log Context, Actor Context, Sensor Context, ...)
		//ArrayList<SimpleEntry<Integer, String>> testOtherModules = model.getOtherSizeNameMap();
		List<AxiTransactionModel> otherTransactions = model.getOtherTransactions();
		
		try {
			BufferedWriter fileWriter = new BufferedWriter(new FileWriter(new File(destinationFolder + "/" + sequenceFile)));
			TestSequence sequence = new TestSequence();

			int idc_id = model.getComUnit().getAxiModel().id;
			String idc_name = model.getComUnit().getAxiModel().name;

			int ccu_id = model.getCcuComp().getAxiModel().id;
			String ccu_name = model.getCcuComp().getAxiModel().name;

			// iterate "other" modules
			for (AxiTransactionModel other : model.getOtherTransactions()) {

				if (model.getcBoxComp().getAxiModel().id == other.id) {
					if(model.getcBoxModel().getContextmaskWrapper().getContextWidth() == 0)
						continue; // don't test the wrapper if there is no branch selection (i.e. no context to test)
				}

				for (int j = 0; j < testPEs.size(); ++j) {
					int peID = testPEs.get(j);

					// other -> PE
					sequence.clear();
					sequence.addOtherWrite(other.name, other.id);
					sequence.addPEWrite(peID);
					sequence.addWait();
					sequence.addCheck();
					fileWriter.write(sequence.getSequenceString());

					// PE -> other
					sequence.clear();
					sequence.addPEWrite(peID);
					sequence.addOtherWrite(other.name, other.id);
					sequence.addWait();
					sequence.addCheck();
					fileWriter.write(sequence.getSequenceString());

					// other -> PE_Log
					sequence.clear();
					sequence.addOtherWrite(other.name, other.id);
					sequence.addPEWrite(peID + model.getPeLogIDoffset());
					sequence.addWait();
					sequence.addCheck();
					fileWriter.write(sequence.getSequenceString());

					// PE_log -> other
					sequence.clear();
					sequence.addPEWrite(peID + model.getPeLogIDoffset());
					sequence.addOtherWrite(other.name, other.id);
					sequence.addWait();
					sequence.addCheck();
					fileWriter.write(sequence.getSequenceString());
				}

				// loop the single reg writes
				for (AxiTransactionModel single : model.getSingleRegTransactions()) {
					// other -> single reg
					sequence.clear();
					sequence.addOtherWrite(other.name, other.id);
					sequence.addSingleWrite(single.name, single.id);
					sequence.addWait();
					sequence.addCheck();
					fileWriter.write(sequence.getSequenceString());

					// single -> other
					sequence.clear();
					sequence.addSingleWrite(single.name, single.id);
					sequence.addOtherWrite(other.name, other.id);
					sequence.addWait();
					sequence.addCheck();
					fileWriter.write(sequence.getSequenceString());
				}

				// other -> parameter
				sequence.clear();
				sequence.addOtherWrite(idc_name, idc_id);
				sequence.addOtherWrite(other.name, other.id);
				sequence.addParameterWrite();
				sequence.addWait();
				sequence.addCheck();
				fileWriter.write(sequence.getSequenceString());

				// parameter -> other
				sequence.clear();
				sequence.addOtherWrite(idc_name, idc_id);
				sequence.addParameterWrite();
				sequence.addOtherWrite(other.name, other.id);
				sequence.addWait();
				sequence.addCheck();
				fileWriter.write(sequence.getSequenceString());

				// other -> state change
				sequence.clear();
				sequence.addOtherWrite(ccu_name, ccu_id);
				sequence.addOtherWrite(other.name, other.id);
				sequence.addStateChange(1, 0); // start, use address 0
				sequence.addWait();
				sequence.addCheck();
				fileWriter.write(sequence.getSequenceString());

				// state change -> other
				sequence.clear();
				sequence.addOtherWrite(ccu_name, ccu_id);
				sequence.addStateChange(1, 0); // start, use address 0
				sequence.addOtherWrite(other.name, other.id);
				sequence.addWait();
				sequence.addCheck();
				fileWriter.write(sequence.getSequenceString());
			}

			// iterate PEs
			for (int j = 0; j < testPEs.size(); ++j) {
				int peID = testPEs.get(j);

				// loop the single reg writes
				for (AxiTransactionModel single : model.getSingleRegTransactions()) {
					// pe -> single reg
					sequence.clear();
					sequence.addPEWrite(peID);
					sequence.addSingleWrite(single.name, single.id);
					sequence.addWait();
					sequence.addCheck();
					fileWriter.write(sequence.getSequenceString());

					// pe log -> single reg
					sequence.clear();
					sequence.addPEWrite(peID + model.getPeLogIDoffset());
					sequence.addSingleWrite(single.name, single.id);
					sequence.addWait();
					sequence.addCheck();
					fileWriter.write(sequence.getSequenceString());

					// pe -> single reg
					sequence.clear();
					sequence.addSingleWrite(single.name, single.id);
					sequence.addPEWrite(peID);
					sequence.addWait();
					sequence.addCheck();
					fileWriter.write(sequence.getSequenceString());

					// pe log -> single reg
					sequence.clear();
					sequence.addSingleWrite(single.name, single.id);
					sequence.addPEWrite(peID + model.getPeLogIDoffset());
					sequence.addWait();
					sequence.addCheck();
					fileWriter.write(sequence.getSequenceString());
				}

				// PE -> PE_Log
				sequence.clear();
				sequence.addPEWrite(peID);
				sequence.addPEWrite(peID + model.getPeLogIDoffset());
				sequence.addWait();
				sequence.addCheck();
				fileWriter.write(sequence.getSequenceString());

				// PE_Log -> PE
				sequence.clear();
				sequence.addPEWrite(peID + model.getPeLogIDoffset());
				sequence.addPEWrite(peID);
				sequence.addWait();
				sequence.addCheck();
				fileWriter.write(sequence.getSequenceString());

				// PE -> parameter
				sequence.clear();
				sequence.addOtherWrite(idc_name, idc_id);
				sequence.addPEWrite(peID);
				sequence.addParameterWrite();
				sequence.addWait();
				sequence.addCheck();
				fileWriter.write(sequence.getSequenceString());

				// parameter -> PE
				sequence.clear();
				sequence.addOtherWrite(idc_name, idc_id);
				sequence.addParameterWrite();
				sequence.addPEWrite(peID);
				sequence.addWait();
				sequence.addCheck();
				fileWriter.write(sequence.getSequenceString());

				// PE_Log -> parameter
				sequence.clear();
				sequence.addOtherWrite(idc_name, idc_id);
				sequence.addPEWrite(peID + model.getPeLogIDoffset());
				sequence.addParameterWrite();
				sequence.addWait();
				sequence.addCheck();
				fileWriter.write(sequence.getSequenceString());

				// parameter -> PE_Log
				sequence.clear();
				sequence.addOtherWrite(idc_name, idc_id);
				sequence.addParameterWrite();
				sequence.addPEWrite(peID + model.getPeLogIDoffset());
				sequence.addWait();
				sequence.addCheck();
				fileWriter.write(sequence.getSequenceString());

				// PE -> state change
				sequence.clear();
				sequence.addOtherWrite(ccu_name, ccu_id);
				sequence.addPEWrite(peID);
				sequence.addStateChange(1, 0); // start, use address 0
				sequence.addWait();
				sequence.addCheck();
				fileWriter.write(sequence.getSequenceString());

				// state change -> PE
				sequence.clear();
				sequence.addOtherWrite(ccu_name, ccu_id);
				sequence.addStateChange(1, 0); // start, use address 0
				sequence.addPEWrite(peID);
				sequence.addWait();
				sequence.addCheck();
				fileWriter.write(sequence.getSequenceString());

				// PE_Log -> state change
				sequence.clear();
				sequence.addOtherWrite(ccu_name, ccu_id);
				sequence.addPEWrite(peID + model.getPeLogIDoffset());
				sequence.addStateChange(1, 0); // start, use address 0
				sequence.addWait();
				sequence.addCheck();
				fileWriter.write(sequence.getSequenceString());

				// state change -> PE_Log
				sequence.clear();
				sequence.addOtherWrite(ccu_name, ccu_id);
				sequence.addStateChange(1, 0); // start, use address 0
				sequence.addPEWrite(peID + model.getPeLogIDoffset());
				sequence.addWait();
				sequence.addCheck();
				fileWriter.write(sequence.getSequenceString());
			}

			/* --- master tests --- */
			sequence.clear();
			sequence.addOtherWrite(ccu_name, ccu_id);
			sequence.addOtherWrite(model.getLogInterface().getAxiModel().name, model.getLogInterface().getAxiModel().id);
			sequence.addSingleWrite(SingleRegTag.LogDest.name(), SingleRegTag.LogDest.ordinal());
			sequence.addSingleWrite(SingleRegTag.LogDestBound.name(), SingleRegTag.LogDestBound.ordinal());
			sequence.addSingleWrite(SingleRegTag.LogDestInc.name(), SingleRegTag.LogDestInc.ordinal());
			sequence.addSingleWrite(SingleRegTag.IntervalLength.name(), SingleRegTag.IntervalLength.ordinal());
			sequence.addStateChange(1, 0); // start, use address 0
			sequence.addMasterTest("log");
			sequence.addWait(model.getLogInterface().getGlobalContextTestSize() + 100);
			sequence.addCheck();
			fileWriter.write(sequence.getSequenceString());

			sequence.clear();
			sequence.addOtherWrite(ccu_name, ccu_id);
			sequence.addOtherWrite(model.getOcmInterface().getAxiModel().name, model.getOcmInterface().getAxiModel().id);
			sequence.addSingleWrite(SingleRegTag.OCMDest.name(), SingleRegTag.OCMDest.ordinal());
			sequence.addSingleWrite(SingleRegTag.OCMDestBound.name(), SingleRegTag.OCMDestBound.ordinal());
			sequence.addSingleWrite(SingleRegTag.OCMDestInc.name(), SingleRegTag.OCMDestInc.ordinal());
			sequence.addSingleWrite(SingleRegTag.IntervalLength.name(), SingleRegTag.IntervalLength.ordinal());
			sequence.addStateChange(1, 0); // start, use address 0
			sequence.addMasterTest("ocm");
			sequence.addWait(runUntil/2 + 60);
			sequence.addCheck();
			fileWriter.write(sequence.getSequenceString());

			/* --- specials --- */
			// state change and parameter write
			sequence.clear();
			sequence.addOtherWrite(idc_name, idc_id);
			sequence.addOtherWrite(ccu_name, ccu_id);
			sequence.addParameterWrite();
			sequence.addStateChange(1, 0);
			sequence.addWait();
			sequence.addCheck();
			fileWriter.write(sequence.getSequenceString());

			sequence.clear();
			sequence.addOtherWrite(idc_name, idc_id);
			sequence.addOtherWrite(ccu_name, ccu_id);
			sequence.addStateChange(1, 0);
			sequence.addParameterWrite();
			sequence.addWait();
			sequence.addCheck();
			fileWriter.write(sequence.getSequenceString());

			// state change (hybrid) and parameter write
			sequence.clear();
			sequence.addOtherWrite(idc_name, idc_id);
			sequence.addOtherWrite(ccu_name, ccu_id);
			sequence.addParameterWrite();
			sequence.addStateChange(1, 0, true);
			sequence.addWait();
			sequence.addCheck();
			fileWriter.write(sequence.getSequenceString());

			sequence.clear();
			sequence.addOtherWrite(idc_name, idc_id);
			sequence.addOtherWrite(ccu_name, ccu_id);
			sequence.addStateChange(1, 0, true);
			sequence.addParameterWrite();
			sequence.addWait();
			sequence.addCheck();
			fileWriter.write(sequence.getSequenceString());

			// state change, address change
			sequence.clear();
			sequence.addOtherWrite(ccu_name, ccu_id);
			sequence.addStateChange(1, 0);
			sequence.addWait();
			sequence.addCheck();
			sequence.addStateChange(0, 0);
			sequence.addWait();
			sequence.addCheck();
			sequence.addStateChange(1, 1);
			sequence.addWait();
			sequence.addCheck();
			fileWriter.write(sequence.getSequenceString());

			fileWriter.close();
		}
		catch (IOException e) {
			System.err.printf("Could not write file \"%s\" - TestbenchGeneratorUltrasynth", sequenceFile);
			e.printStackTrace();
		}
	}
	
	/**
	 * Generates variable values and locations for local variables to be used for testing.
	 */
	private void generateTestLocalVars(CgraModelUltrasynth model, SourceFileLib sourceFileLib,
									   int sourceIndex, DriverUltrasynth.Config config) {
		StringBuilder file = new StringBuilder(1024);

		final int data = 1;
		final int sizeDivisor = 8;

		BiFunction<Boolean, Integer, String> paramWriter = (Boolean hybrid, Integer offset) -> {
			int usedData = data;
			StringBuilder sb = new StringBuilder();

			for (final PEModel pe : model.getPEs()) {
				for (int i = 0; i < model.getMinRegFileSize() / sizeDivisor && i + offset < pe.getRegfilesize(); ++i) {
					sb.append("{ pe: ");
					sb.append(pe.getID());
					sb.append(", offset: ");
					sb.append(i + offset);
					sb.append(", value: ");
					sb.append(usedData++);
					sb.append(", hybrid: ");
					sb.append(hybrid ? 1 : 0);
					sb.append(" };\n");
				}
			}

			return sb.toString();
		};

		file.append(paramWriter.apply(false, 0));
		file.append(paramWriter.apply(true, model.getMinRegFileSize() / sizeDivisor));

		GeneratedSourceFile sourceFile = new GeneratedSourceFile("testVars", null,
				"", SourceFile.Extension.ExtTF);
		sourceFile.setSourceString(file.toString());
		sourceFileLib.addSourceFile(sourceIndex, sourceFile);
	}

	/**
	 *
	 * @param group
	 * 			The File to draw the String Template instance from.
	 * @return The rendered context write string.
	 */
	private String renderTestBenchContextWriteTemplate(STGroupFile group, AxiTransactionModel axiModel) {
		int transfersPerEntry = axiModel.valueTransferCount + 1;
		int transferCount = transfersPerEntry * axiModel.maxValueCount;
		ST contextWriteTemplate = group.getInstanceOf("contextWrite");

		contextWriteTemplate.add("idDef", axiModel.name);
		contextWriteTemplate.add("op", axiModel.generalTarget.name());
		contextWriteTemplate.add("transfersPerEntry", transfersPerEntry);
		contextWriteTemplate.add("transferCount", transferCount);
		contextWriteTemplate.add("contextAddrOffset", 0);

		return contextWriteTemplate.render();
	}

	/**
	 *
	 * @param group
	 * 			The File to draw the String Template instance from.
	 * @param instancePath
	 * 			The hierarchical path to the actual instance in the design
	 * @return The rendered context check string.
	 */
	private String renderTestBenchContextCheckTemplate(STGroupFile group, AxiTransactionModel axiModel, String instancePath) {
		ST contextCheckTemplate = group.getInstanceOf("contextCheck");

		contextCheckTemplate.add("idDef", axiModel.name);
		contextCheckTemplate.add("instancePath", instancePath);
		contextCheckTemplate.add("contextWidth", axiModel.valueWidth);

		return contextCheckTemplate.render();
	}

	/**
	 * Encapsulates the generation of test sequences, use (and extend if needed) this to generate
	 * all kinds of sequences.
	 * The syntax of these sequences follows the definition of the parser in TestFileParser.sv
	 */
	private class TestSequence {

		StringBuilder m_sequence;

		TestSequence() {
			m_sequence = new StringBuilder();
			m_sequence.append("{ ");
		}

		void addPEWrite(int id) {
			m_sequence.append("{t_pe, id: ");
			m_sequence.append(id);
			appendType();
		}

		void addSingleWrite(String target, int id) {
			m_sequence.append("{t_single, target: ");
			m_sequence.append(target);
			m_sequence.append(", id: ");
			m_sequence.append(id);
			appendType();
		}

		void addOtherWrite(String target, int id) {
			m_sequence.append("{t_other, target: ");
			m_sequence.append(target);
			m_sequence.append(", id: ");
			m_sequence.append(id);
			appendType();
		}

		void addParameterWrite() {
			m_sequence.append("{t_param");
			appendType();
		}

		void addStateChange(int newState, int addr) {
			addStateChange(newState, addr, false);
		}


		void addStateChange(int newState, int addr, boolean hybrid) {
			m_sequence.append("{t_state, newState: ");
			m_sequence.append(newState);
			m_sequence.append(", addr: ");
			m_sequence.append(addr);
			m_sequence.append(", hybrid: ");
			m_sequence.append(hybrid ? 1 : 0);
			appendType();
		}

		void addMasterTest(String target) {
			m_sequence.append("{");
			m_sequence.append("t_master, target: ");
			m_sequence.append(target);
			m_sequence.append("}; ");
		}
		
		void addCheck() {
			m_sequence.append("{");
			m_sequence.append("check");
			m_sequence.append("}; ");
		}

		void addWait() {
			addWait(100);
		}

		void addWait(int cycles) {
			m_sequence.append("{");
			m_sequence.append("wait, ");
			m_sequence.append(cycles);
			m_sequence.append("}; ");
		}

		String getSequenceString() {
			m_sequence.append("};\n");
			return m_sequence.toString();
		}

		void clear() {
			// We set this to two as we want to preserve "{ " at the start of the sequence!
			m_sequence.setLength(2);
		}

		private void appendType() {
			m_sequence.append(", type: ");
			m_sequence.append(ThreadLocalRandom.current().nextInt(0, 3));
			m_sequence.append("}; "); // because "type" is always expected last
		}
	}
}
