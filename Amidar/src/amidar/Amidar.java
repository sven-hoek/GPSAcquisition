package amidar;


import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.NavigableMap;
import java.util.TreeMap;

import cgramodel.CgraModelAmidar;
import cgramodel.ContextMaskCBoxEvaluationBlock;
import cgramodel.ContextMaskContextControlUnit;
import cgramodel.ContextMaskPE;
import cgramodel.LocationInformation;
import dataContainer.ByteCode;
import dataContainer.Invokation;
import dataContainer.MethodDescriptor;
import dataContainer.SynthesizedKernelDescriptor;
import exceptions.AmidarSimulatorException;
import javasim.synth.HardGen;
import javasim.synth.SequenceNotSynthesizeableException;
import javasim.synth.model.CGRAIntrinsics;
import bus.Arbiter;
import scheduler.RCListSched.AliasingSpeculation;
import tracer.Trace;
import tracer.TraceManager;
import functionalunit.*;
import functionalunit.cache.Cache;
import functionalunit.cache.HandleTableCache;
import functionalunit.cache.L2Cache;
import functionalunit.cache.L2CachePhysical;
import functionalunit.cache.L2CacheVirtual;
import functionalunit.cache.Memory;
import functionalunit.cache.PrefetchStrategy;
import functionalunit.cache.coherency.CoherenceController;
import functionalunit.cache.coherency.DragonController;
import functionalunit.cache.coherency.FireflyController;
import functionalunit.cache.coherency.MOESIController;
import functionalunit.cgra.KernelTableEntryCGRA;
import functionalunit.cgra.PE;
import functionalunit.tables.ConstantPoolEntry;
import functionalunit.tables.LoopProfileTableEntry;
import functionalunit.tokenmachine.KernelProfiler;
import functionalunit.tokenmachine.Profiler;
import amidar.axtLoader.AXTLoader;

/**
 * The AMIDAR Model
 * @author jung
 *
 */
public class Amidar {
	
	private ConfMan configManager;
	private TraceManager traceManager;
	private boolean synthesis;
	private int numberOfFus;

	Memory memory;
	
	private String[] methodNames; // Only for Debugging and Synthesis report
	
	// All Functional Units
	@SuppressWarnings("rawtypes")
	FunctionalUnit [] functionalUnits;
	TokenMachine tokenMachine;
	ObjectHeap heap;
	KernelProfiler kernelProfiler;
	CoherenceController coherenceController;
	
	Arbiter arbiter;
	
	private long synthTime = 0;

	/**
	 * Creates a new Amidar processor for simulation
	 * @param configManager Config Manager which contains all parameters
	 * @param traceManager Trace Manager which handles all Traces
	 * @param synthesis decides whether synthesis is on or off
	 */
	public Amidar(ConfMan configManager, TraceManager traceManager){
		this.configManager = configManager;
		this.traceManager = traceManager;
		this.synthesis = configManager.getSynthesis();
		
		HashMap<String,String> fuConfigFiles = configManager.getFuConfigFiles();
		
		IALU ialu = new IALU(fuConfigFiles.get("IALU"), traceManager);
		IMUL imul = new IMUL(fuConfigFiles.get("IALU"), traceManager);
		IDIV idiv = new IDIV(fuConfigFiles.get("IALU"), traceManager);
		LALU lalu = new LALU(fuConfigFiles.get("IALU"), traceManager);
		FPU fpu = new FPU(fuConfigFiles.get("FALU"), traceManager);
		FDIV fdiv = new FDIV(fuConfigFiles.get("FALU"), traceManager);
		TokenMachine tokenMachine = new TokenMachine(fuConfigFiles.get("TOKENMACHINE"), traceManager, configManager.getBenchmarkScale());
		memory = new Memory(traceManager);
		
		L2Cache l2Cache = new L2CachePhysical(memory, fuConfigFiles.get("HEAP"), traceManager);
		HandleTableCache htCache = new HandleTableCache(memory, fuConfigFiles.get("HEAP"));
		
		
		
		String coherenceName = (String)(configManager.getSynthesisConfig().get("COHERENCE_PROTOCOL"));
		switch (coherenceName) {
		case "DRAGON":
			coherenceController = new DragonController();
			break;
		case "FIREFLY":
			coherenceController = new FireflyController();
			break;
		case "MOESI":
			coherenceController = new MOESIController();
			break;
		default:
			throw new AmidarSimulatorException("Coherence Protocol \"" +coherenceName+ "\" unknown");
		}
		
		PrefetchStrategy prefetch = (PrefetchStrategy) configManager.getSynthesisConfig().get("PREFETCHING");
		
		coherenceController.setPrefetchStrategy(prefetch);
		coherenceController.setL2Cache(l2Cache);
		coherenceController.setHandleTableCache(htCache);
		
		
		ObjectHeap heap = new ObjectHeap(memory, fuConfigFiles.get("HEAP"), traceManager, synthesis, coherenceController);
		FrameStack frameStack = new FrameStack(fuConfigFiles.get("FRAMESTACK"), traceManager);
		
		kernelProfiler = tokenMachine.getKernelProfiler();
		
		if(synthesis){
			numberOfFus = 10;
			CGRA  cgra = new CGRA(fuConfigFiles.get("CGRA"), false, traceManager, memory, kernelProfiler, coherenceController);
//			heap.setMOESICaches(cgra.getCaches());
			
			Cache[] cgraCaches = cgra.getCaches();
			
			Cache[] caches = new Cache[cgraCaches.length+1];
			
			for(int i = 1; i < caches.length; i++){
				caches[i] = cgraCaches[i-1];
			}
			
			caches[0] = heap.getCaches()[0];
			coherenceController.setCaches(caches);
			
			functionalUnits = new FunctionalUnit[numberOfFus];
			functionalUnits[9] = cgra;
			tokenMachine.setFUs(ialu, imul, idiv, lalu, fpu, fdiv, heap, frameStack, tokenMachine, cgra, null);
			
		} else {
			
			
			Cache[] caches = new Cache[1];
			caches[0] = heap.getCaches()[0];
			coherenceController.setCaches(caches);
			numberOfFus = 9;
			functionalUnits = new FunctionalUnit[numberOfFus];
			tokenMachine.setFUs(ialu, imul, idiv, lalu, fpu, fdiv, heap, frameStack, tokenMachine, null, null);
		}
		
		// TODO Order? Maybe relevant for the Bus
		this.tokenMachine = tokenMachine;
		this.heap = heap;
		functionalUnits[0] = ialu;
		functionalUnits[1] = imul;
		functionalUnits[2] = idiv;
		functionalUnits[3] = lalu;
		functionalUnits[4] = tokenMachine;
		functionalUnits[5] = heap;
		functionalUnits[6] = frameStack;
		functionalUnits[7] = fpu;
		functionalUnits[8] = fdiv;
		
		arbiter = new Arbiter(functionalUnits);
		
	}
	
	/**
	 * Sets the application that shall be executed on AMIDAR
	 * @param application the path to the application
	 */
	public void setApplication(AXTLoader axtLoader){
		methodNames = axtLoader.getMethodNames();
		tokenMachine.initTables(axtLoader);
		tokenMachine.setMethodNames(methodNames);
		heap.initHeap(axtLoader);
	}
	
	boolean started =  false;
	
	/**
	 * Simulates execution on AMIDAR
	 * @param saveCore determines whether the core should be saved in the simulation results.
	 * @return The simulation results
	 */
	public AmidarSimulationResult simulate(boolean saveCore){
		int SYNTHESIS_INTERVAL = 1;//TODO
		
//		int[] time = new int[functionalUnits.length + 1];
		
		boolean ready = false;
		long ticks = 0;
		
		Trace ticksTracer = traceManager.getf("ticks");
		
		long  startTime = System.nanoTime();
		
		Cache [] caches = heap.getCaches();
		
		BufferedWriter [] cacheStateOutput = new  BufferedWriter[caches.length];
		
//		try{
//		for(int i = 0; i< caches.length; i++){
//			FileWriter fw = new FileWriter("log/cache"+i+".csv");
//			BufferedWriter bw = new BufferedWriter(fw);
//			cacheStateOutput[i] = bw;
//		}
//		} catch(IOException e){
//			
//		}
		
	
		
		
//		long avTime = 0;
		while(!ready){
//			long start = System.nanoTime();
			if(tokenMachine.startedActualApplication){
				if(started == false){
					if(traceManager.getf("heap").active()){
						heap.getOHTrace().appendTrace(ObjectHeap.RESET, 0, 0, 0, 0, 0, 0, 0);
					}
					for(FunctionalUnit fu : functionalUnits){
						fu.resetExecutionCounter();
					}
					
//					for(Cache ca: heap.getCaches()){
//						ca.resetStatistics();
//					}
					started = true;
				}
//				traceManager.getf("methods").activate();
//				traceManager.getf("ticks").activate();
				ticks++;
			}
			ready = true;
			if(ticksTracer.active()){
				ticksTracer.println("------------------------------------------- Cycle "+ticks+ " --------------------------------------------");
				ticksTracer.println();
			}
			long fuTime = System.nanoTime();
			for(int i = 0; i < numberOfFus; i++){
				ready &= functionalUnits[i].tick();
//				long fuTime2 = System.nanoTime();
//				time[i] += fuTime2-fuTime;
//				fuTime = System.nanoTime();
			}
			tokenMachine.setTicks(ticks);
			arbiter.tick();
			coherenceController.tick();
//			time[functionalUnits.length]+= System.nanoTime()-fuTime;
			
			if(synthesis && (ticks % SYNTHESIS_INTERVAL) == 0 && tokenMachine.startedActualApplication){
//				System.err.println("STARTSYNTH");
				// Call synthesis algorithm here - normaly this is done by a sw thread invoked by the thread scheduler
				synthesize();
			}
			
//			if(ticks % 10000== 1){
//				for(Cache cach: caches){
//					double used = cach.usage();
//					System.out.print((int)(used*100)+" ");
//				}System.out.println();
//				System.out.println( "   " + memory.l2cache.usage()*100);
//			}
			
//			for(int i = 0; i < caches.length; i++){
//				int[] cacheState = caches[i].getState();
//				try {
//					cacheStateOutput[i].write(cacheState[0]);
//					for(int index = 1; index < cacheState.length; index++){
//
//						cacheStateOutput[i].write(", "+cacheState[index]);
//
//					}
//					cacheStateOutput[i].write("\n");
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
			
			
//			long stop = System.nanoTime();
//			long tt = stop-start;
//			avTime = (long)(0.1*tt+0.9*avTime);
//			System.out.println("TT " + (avTime));
			
		}
		
//		for(Cache cach: caches){
//			double used = cach.usage();
//			System.out.print((int)(used*100)+" ");
//		}System.out.println();
//		System.out.println( "   " + memory.l2cache.usage()*100);
//		
//		
//		if(functionalUnits.length == 10){
//			CGRA cgra = (CGRA)functionalUnits[9];
//			
//			
//			System.out.println(cgra.getStateCNT());
//		}
		
//		try{
//			for(int i = 0; i< caches.length; i++){
//				cacheStateOutput[i].close();
//			}
//			} catch(IOException e){
//				
//			}
		long stoptime = System.nanoTime();
		
//		String a = "CACHE USAGE: ";
//		for(Cache cach: caches){
//			double used = cach.usage();
//			a = a + (double)((int)(used*10000))/100.0+" ";
////			System.err.print((int)(used*100)+" ");
//		}
//		
//		a = a + memory.getL2Cache().usage();
//		System.err.println(a);
		
		long executionTime = stoptime - startTime;
		int executionTimeMillis = (int)(executionTime/1000000L);
		
//		for(int i = 0; i<time.length; i++){
//			System.out.println(i + " Time " + time[i]/1000000 + "us");
//		}
		
		double energy = 0;
		
		
		for(FunctionalUnit fu: functionalUnits){
//			System.out.println("Energyconsumption "+ fu+": "+ fu.getDynamicEnergy());
			 
			energy += fu.getDynamicEnergy() + fu.getStaticEnergy()*ticks;
		}

		Trace cacheTrace = traceManager.getf("caches");
		if(cacheTrace.active()){
			cacheTrace.setPrefix("caches");
			if(synthesis) cacheTrace.printTableHeader("USED CACHES:   Heap Cache  +  "+(coherenceController.getNrOfCaches()-1)+" CGRA Caches");
			else cacheTrace.printTableHeader("USED CACHES:   Heap Cache");
			coherenceController.printMissrates();
//			System.out.println("Cohrerenccontroller util: " + coherenceController.getUtilization());
//			System.out.println("Cohrerenccontroller prefratio: " + coherenceController.getPrefetchRatio());
//			heap.cacheTrace();
		}
		
		LinkedHashMap<String, Integer> cgraStateCount = null;
		
		if(synthesis){
			cgraStateCount = getCGRA().getStateCount();
		}
		
		
		AmidarSimulationResult res;
		if(saveCore){
			res = new AmidarSimulationResult(ticks, executionTimeMillis, energy, tokenMachine.getProfiler(), this, kernelProfiler, cgraStateCount);
		} else{
			res = new AmidarSimulationResult(ticks, executionTimeMillis, energy, tokenMachine.getProfiler(), null, kernelProfiler, cgraStateCount);
		}
		
		double l1Utilisation = 0;
		int cnt = 0;
		
		int nrOfPrefetchesL1 = 0;
		int nrOfUsedPrefetchesL1 = 0;
		int nrOfPrefetchesL2 = 0;
		int nrOfUsedPrefetchesL2 = 0;
		
		
		for(Cache cache: coherenceController.getCaches()){
			if(cnt==0){
				cnt++;
				continue; // omit heap Cache
			}
			l1Utilisation += cache.usage();
			cnt++;
			nrOfPrefetchesL1 += cache.getNrPrefetchedFromL1() + cache.getNrPrefetchedFromL2();
//			nrOfPrefetchesL2 += cache.getNrPrefetchedFromL2();
			nrOfUsedPrefetchesL1 += cache.getNrUsedPrefetchFromL1()+ cache.getNrUsedPrefetchFromL2();
//			nrOfUsedPrefetchesL2 += cache.getNrUsedPrefetchL2();
		}
		
		res.addL1Utilization(l1Utilisation/(cnt-1));
		res.addL2Utilization(coherenceController.getL2Cache().usage());
		if(synthesis){
			res.setMemoryAccessTime(getCGRA().getMemoryAccessTime());
			res.setMemoryAccesses(getCGRA().getMemoryAccesses());
			res.setCoherenceControllerTaskSimulator(coherenceController.getTaskSimulator());
			res.setNrOfContexts(contextPointer);
			res.setNrOfPrefetchesL1(nrOfPrefetchesL1);
//			res.setNrOfPrefetchesL2(nrOfPrefetchesL2);
			res.setNrOfUsedPrefetchesL1(nrOfUsedPrefetchesL1);
//			res.setNrOfUsedPrefetchesL2(nrOfUsedPrefetchesL2);
			res.setNrOfHandledPrefetchRequests(coherenceController.prefAlreadyAvailable+coherenceController.prefetched);
			res.setNrOfHandledPrefetchRequestsAlreadyAvailable(coherenceController.prefAlreadyAvailable);
			res.setCachelineFills(coherenceController.loadedData + coherenceController.prefetched);
			res.setSynthTime(synthTime);
		}
		
//		System.err.println("PREF: " + coherenceController.prefetched);
//		System.err.println("PREFAVAIL: " + coherenceController.prefAlreadyAvailable);
		
//		System.err.println("apsdfasd CACHELINERELOADS: " + (coherenceController.prefetched + coherenceController.loadedData));
		
		
		return res;
	}
	
	
	int contextPointer = 0;
	byte kernelPointer = 0;
	int lVarPointer = 0;
	int locationInformationPointer = 0;
	int constantPointer = 0;
	
	String patch = "";
	
	/**
	 * Invokes the Synthesis algorithm - THIS NO PART OF THE HW - this will be a SW thread running on Amidar itself
	 * the best kernel found by the profliler will be synthesized
	 */
	public void synthesize(){
		int REPLACED = 10;

		
		///////////////////// PSEUDO PERIPHERY ACCESSS ///////////////////// 
		
		
		Profiler profiler = tokenMachine.getProfiler();
		
		
		LoopProfileTableEntry loop = profiler.getBestCandidate();
		if(loop == null){
			return;
		}
		
		int methodIndex = loop.get(LoopProfileTableEntry.AMTI);
		int start = loop.get(LoopProfileTableEntry.START);
		int stop = loop.get(LoopProfileTableEntry.END) -3 ;
		
		if(stop <= 0 || (start <= tokenMachine.getCurrentAddress() && start+REPLACED>= tokenMachine.getCurrentAddress() )){
			return;
		}
		
		int state = synthesize(methodIndex, start, stop, true);
		
//		System.out.println("synthed: " + state);
		
//		System.out.println("INVOKECNT:  " + loop.get(LoopProfileTableEntry.INVOKE_COUNTER));
		
		loop.setData(LoopProfileTableEntry.SYNTHESIZED, state);
		
	}
	
	/**
	 * Invokes the Synthesis algorithm - THIS NO PART OF THE HW - this is only for the simulator
	 * @param methodName
	 * @param schedule
	 */
	public void synthesize(String methodName, boolean schedule){
		
		
		Trace synthTrace = traceManager.getf("synthesis");
		
		MethodDescriptor[] methodTable = tokenMachine.getMethodTable();
		
		int methodIndex = -1, start = 0, stop = 0;
		
		
		for(int i = 0; i< methodTable.length; i++){
			if(methodTable[i].getMethodName().equals(methodName)){
				methodIndex = i;
				start = methodTable[i].getCodeRef();
				stop = start + methodTable[i].getCodeLength();
				break;
			}
		}
		if(methodIndex == -1){
			synthTrace.println("Method " + methodName + " not found");
			String className = methodName.split("\\.")[0];
			synthTrace.println("Available methods of this class are:");
			for(int i = 0; i< methodTable.length; i++){
				if(methodTable[i].getMethodName().contains(className)){
					synthTrace.println("\t- " + methodTable[i].getMethodName());
				}
			}
		}
			
		
		
		
		
		//1 Collect backward jumps
		TreeMap<Integer,Integer> backwardJumps = new TreeMap<>();
		
		byte[] code = tokenMachine.getCode();
		
		for(int i = start; i < stop; i++){
			if(code[i] >= (byte)0x99 && code[i] <= (byte)0xA7){ 				//track all jumps
				int gotoVal = (short)((short)((code[i+1]& 0xFF)<<8) | (short)(code[i+2]& 0xFF));
				if (gotoVal < 0){
					backwardJumps.put(i, gotoVal);
				}
			}
//			System.out.println(ByteCode.debug(code[i]));
			i+= ByteCode.getParamCount(code[i]);
		}
		
//		backwardJumps.
		
//		System.out.println(start + " peter " + stop);
		
		NavigableMap<Integer, Integer> bJumps = backwardJumps.descendingMap();
		
		int alreadyCovered = stop;
		 
		for(Integer i : backwardJumps.descendingKeySet()){
			if( i < alreadyCovered){
				alreadyCovered = i + backwardJumps.get(i);
				synthesize(methodIndex, alreadyCovered, i, schedule);
				synthTrace.println("\t"+methodName+"-"+(alreadyCovered-start)+"-"+(i-start));
			}
			
			
			
			
		}
		
		//2 Synthesize all outer loops
		
		
		
		
		
		
	}
	
	public static LinkedHashMap<Integer, Integer> kernelLengthHistogram = new LinkedHashMap<>();
	
	LinkedHashMap<Object,LinkedHashMap<Integer,LinkedHashSet<Integer>>> handleToPeMap = new LinkedHashMap<>();
	
	/**
	 * Invokes the Synthesis algorithm - THIS NO PART OF THE HW - this will be a SW thread running on Amidar itself
	 * 
	 */
	public int synthesize(int methodIndex, int start, int stop, boolean schedule){
		
		int REPLACED = 8;
		int returnValue;

		Trace synthTracer = traceManager.getf("synthesis");
		
		///////////////////// PSEUDO PERIPHERY ACCESSS ///////////////////// 
		long time = System.nanoTime();
		
		
		
		HashMap<String, Object> synthesisConfig = configManager.getSynthesisConfig();
		

		MethodDescriptor[] methodTable = tokenMachine.getMethodTable();
		SynthesizedKernelDescriptor[] kernelTable = tokenMachine.getKernelTable();
		ArrayList<Invokation> invokationHistory = tokenMachine.getInvocationHistory();
		
		
		boolean unrollBasedPrefetch =  PrefetchStrategy.UNROLL ==  (PrefetchStrategy)synthesisConfig.get("PREFETCHING");
		boolean constantFolding = (Boolean)synthesisConfig.get("CONSTANT_FOLDING");
		boolean cse = (Boolean)synthesisConfig.get("CSE");
		boolean inline = (Boolean)synthesisConfig.get("INLINE");
		int maxUnrollLength = (Integer)synthesisConfig.get("MAX_UNROLL_LENGTH");
		int unroll = ((Long)synthesisConfig.get("UNROLL")).intValue();
		AliasingSpeculation aliasing = ((AliasingSpeculation)synthesisConfig.get("ALIASING_SPECULATION"));
		byte[] code = tokenMachine.getCode();
		boolean writeContexts = false; // TODO: include in config???
		
		String cgraModel = configManager.getFuConfigFiles().get("CGRA");
		
		
		///////////////// END PSEUDO PERIPHERY ACCESSS /////////////////////
		LinkedHashMap<String,LinkedHashSet<Integer>> methodBlacklist = (LinkedHashMap<String,LinkedHashSet<Integer>>)synthesisConfig.get("BLACKLIST");
		
		
		String kernelName = methodTable[methodIndex].getMethodName() + ": "  + (start - methodTable[methodIndex].getCodeRef()) + "-" + (stop - methodTable[methodIndex].getCodeRef());
		
		if(synthTracer.active()){
			synthTracer.println("Synthesizing " + kernelName);
		}
		





		if(stop - 3 - start < REPLACED){
			System.err.println("NOWAY");
			return -999;
		}
		

		HardGen hardwareGenerator;
		CGRA cgra = (CGRA)functionalUnits[9];
		int [] localVariableLiveInOut = null;
		try{
			
			if(methodBlacklist.containsKey(methodTable[methodIndex].getMethodName()) ){
				
				if(methodBlacklist.get(methodTable[methodIndex].getMethodName()).contains(start-methodTable[methodIndex].getCodeRef()))
				
				throw new SequenceNotSynthesizeableException("Method is on Blacklist");
			}
			
			if(kernelPointer >= kernelTable.length){
				throw new SequenceNotSynthesizeableException("Not enough entries in the KernelTable (Currently 32 are available)");
			}
			
			
			hardwareGenerator = new HardGen(methodTable, kernelTable, invokationHistory, methodIndex, start, stop, cse, inline, maxUnrollLength, unroll, code, cgra.getModel(), aliasing, constantFolding, unrollBasedPrefetch);
			if(schedule){
				
//				if(handleToPeMap == null)
				handleToPeMap = new LinkedHashMap<>();
				
				long startTime = System.nanoTime();	
					
				hardwareGenerator.generate(handleToPeMap );
				
				long stopTime = System.nanoTime();
				
				synthTime += stopTime-startTime;
				
				if(synthTracer.active()){
					hardwareGenerator.printGraphs();
				}
				
				
				long[][] contextsPE = hardwareGenerator.getContextsPE();
				long[][] contextsCBox = hardwareGenerator.getContextsCBox();
				long[] contextsControlUnit = hardwareGenerator.getContextsControlUnit();
				
				stop = hardwareGenerator.getEndOfSequence();
				
				
				
			
				
				if(contextPointer + contextsCBox[0].length >= cgra.contextscboxevaluationboxes[0].memory_length){
					throw new SequenceNotSynthesizeableException("Context memory is too small. Needed " + (contextPointer + contextsCBox[0].length) + " contexts. Only " + cgra.contextscboxevaluationboxes[0].memory_length + " available");
				}
				if(synthTracer.active()){
					synthTracer.println("Synthesized " + contextsCBox[0].length + " kernelID: " + kernelPointer + " Contextpointer: " +contextPointer);
				}
				
				Integer nr = kernelLengthHistogram.get(contextsCBox[0].length);
				if(nr == null){
					nr = 0;
				}
				kernelLengthHistogram.put(contextsCBox[0].length, nr + 1);
				
				///////////////////// PSEUDO PERIPHERY ACCESSS /////////////////////
				
				try{

					// PE Contexts
					for(int i = 0; i < contextsPE.length; i++){
						for( int j = 0; j < contextsPE[0].length; j++){
							cgra.contextspes[i].memory[j+contextPointer] = contextsPE[i][j];
						}
					}
					// CBox Contexts
					for(int box = 0; box < contextsCBox.length; box++){
						for(int i = 0; i < contextsCBox[0].length; i++){
							cgra.contextscboxevaluationboxes[box].memory[i+contextPointer] = contextsCBox[box][i];
						}
					}
					// Control Unit contexts
					for(int i = 0; i < contextsControlUnit.length; i ++){
						cgra.controlunit.memory[i+contextPointer] = contextsControlUnit[i];
					}
					// Handlecompare contexts
//					for(int i = 0; i < contextsHandleCompare.length; i ++){
//						cgra.handleCompare.contexts[i+contextPointer] = contextsHandleCompare[i];
//					}
				} catch( ArrayIndexOutOfBoundsException e){
					System.err.println("Contexts to small: " + e.getMessage());
				}
				
				// Patch Bytecode
				byte[] replacedBytes = new byte [REPLACED];
				
				for(int i = 0; i<REPLACED; i++){
					replacedBytes[i] = code[i+start]; 
				}

				code[start] = ByteCode.CGRA_START; 
				code[start+1] = kernelPointer;
				code[start+2] = (byte)hardwareGenerator.getNrLocalVarReceive();
//				code[start+3] = (byte)hardwareGenerator.getNrIndirectConst();
//				code[start+4] = (byte)hardwareGenerator.getNrDirectConst();
				code[start+3] = ByteCode.CGRA_STOP;
				code[start+4] = (byte)hardwareGenerator.getNrLocalVarSend();
				code[start+5] = (byte)0;
				int jump = stop-start;
				code[start+6] = (byte)((jump>>8)&0xFF);
				code[start+7] = (byte)((jump)&0xFF);
				
				
				if(writeContexts){
				try {
					FileWriter fw;
					fw = new FileWriter("gen/axt.patch");
					BufferedWriter bw = new BufferedWriter(fw);
					for(int i = 0; i<10; i++){
						patch = patch+code[start+i]+" ";
//						bw.write(code[start+i]+" ");
					}
//					bw.write(methodTable[methodIndex].getMethodName()+" "+((start - methodTable[methodIndex].getCodeRef())));
					patch = patch + methodTable[methodIndex].getMethodName()+" "+((start - methodTable[methodIndex].getCodeRef())) + "\n";
					bw.write(patch);
					bw.flush();
					bw.close();
				
				} catch (IOException e) {
				}
				}
				
				
//				System.out.println("METHOD OFF " +  (start - methodTable[methodIndex].getCodeRef()));
//				System.out.println("start " + start);
//				for(int i = 0; i<10; i++){
//					System.out.println("replaced: "+code[start+i]);
//				}
				
				
				// Store TokenSet // localVariables		
				ArrayList<Integer> localVariables = hardwareGenerator.getLocalVariables();
				
				localVariableLiveInOut = tokenMachine.getliveInOutMemory();
				
				for(int i = 0; i < localVariables.size(); i++){
					localVariableLiveInOut[i+lVarPointer] = localVariables.get(i);
				}
				
				LocationInformation[] locationInformationTable = cgra.getLocationInformationTable();
				ArrayList<LocationInformation> locInfo = hardwareGenerator.getLocationInformation();
				
				for(int i = 0; i < locInfo.size(); i++){
					locationInformationTable[i+locationInformationPointer] = locInfo.get(i);
				}
				
				int[] constantMemory = cgra.getConstMemory();
				ArrayList<Integer> constants = hardwareGenerator.getConstantMemory();
				
				for(int i = 0; i < constants.size(); i++){
					constantMemory[i + constantPointer] = constants.get(i);
				}
				
				ArrayList<Integer> constantsIndirect = hardwareGenerator.getConstantMemoryIndirect();
				ConstantPoolEntry[] constantPool = tokenMachine.getConstantPool();
				
				int constantPointer2 = constantPointer + constants.size();
				
				for(int i = 0; i < constantsIndirect.size(); i++){
					constantMemory[i + constantPointer2] = constantPool[constantsIndirect.get(i)].get(0);
				}
				
				// Write Kernel Descriptor
				SynthesizedKernelDescriptor kernel = new SynthesizedKernelDescriptor();
				KernelTableEntryCGRA kernelCGRA = new KernelTableEntryCGRA();
				
				kernel.setReplacedBytes(replacedBytes);
				kernel.setContextPointer(contextPointer);
				kernel.setSynthConstPointer(lVarPointer);
				
				kernelCGRA.setConstantPointer(constantPointer);
				kernelCGRA.setContextPointer(contextPointer);
				kernelCGRA.setLocationInformationMemoryPointer(locationInformationPointer);
				kernelCGRA.setNrOfConstants(constants.size() + constantsIndirect.size());
				
				kernelTable[kernelPointer] = kernel;
				
				cgra.getKernelTableCGRA()[kernelPointer] = kernelCGRA;
				
				kernelProfiler.registerKernel(kernelPointer, kernelName);
				
				kernelPointer += 1;
				lVarPointer += localVariables.size();
				constantPointer += constantsIndirect.size() + constants.size();
				locationInformationPointer += locInfo.size();
				contextPointer += contextsCBox[0].length;
				for(int i = 0; i<cgra.getModel().getNrOfMemoryAccessPEs() ; i++){
					cgra.InputCacheValid[i] = true;
				}
			} else {
				hardwareGenerator.generateCDFG();
				hardwareGenerator.exportCDFG();
//				hardwareGenerator.getCDFG();//TODO
			}
			
			
		} catch(SequenceNotSynthesizeableException e){
			if(synthTracer.active()){
				synthTracer.println("Not able to synthesize: " + e.getMessage());
			}
//			loop.setData(LoopProfileTableEntry.SYNTHESIZED, -1);
			return -1;
		}

		
		
		time = System.nanoTime() - time;
		
//		loop.setData(LoopProfileTableEntry.SYNTHESIZED, (int)(time/1000000));
		
		returnValue = (int)(time/1000000);
		if(returnValue == 0){
			returnValue = 1;
		}
		
		
		
		
		/// WRITE INIT FOR HW IMPLEMENTATION
		if(writeContexts){
		try {
			FileWriter fw;
			BufferedWriter bw;
			for(int cb = 0; cb < cgra.getModel().getcBoxModel().getNrOfEvaluationBlocks(); cb++){
				fw = new FileWriter("gen/cbox"+cb+".dat");
				bw = new BufferedWriter(fw);
				ContextMaskCBoxEvaluationBlock cboxMask = cgra.getModel().getcBoxModel().getContextmaskEvaLuationBlocks();

				for(int addr = 0; addr < contextPointer; addr++){
					String ret = cboxMask.getBitString(cgra.contextscboxevaluationboxes[cb].memory[addr]);
					bw.write(ret);
					bw.write("\n");
				}
				bw.close();
			}
			
			fw = new FileWriter("gen/ctrlunit.dat");
			bw = new BufferedWriter(fw);
			ContextMaskContextControlUnit ccuMask = cgra.getModel().getContextmaskccu();
			
			for(int addr = 0; addr < cgra.getMemorySizehOfContext(); addr++){
				String ret = ccuMask.getBitString(cgra.controlunit.memory[addr]);
				bw.write(ret);
				bw.write("\n");
			}
			bw.close();
			
			
			for( int pe = 0; pe < cgra.contextspes.length; pe++){
				fw = new FileWriter("gen/pe"+pe+".dat");
				bw = new BufferedWriter(fw);
				PE currentPE = cgra.getPEs()[pe];
				ContextMaskPE peMask = currentPE.getContextmask();
				
				for(int addr = 0; addr < cgra.getMemorySizehOfContext(); addr++){
					String ret = peMask.getBitString(cgra.contextspes[pe].memory[addr]);
					bw.write(ret);
					bw.write("\n");
				}
				bw.close();
				
			}
			
			fw = new FileWriter("gen/kernel.dat");
			bw = new BufferedWriter(fw);
			
			for(int i = 0; i < kernelPointer; i++){
				String ret = "";
				String buff = "0000000000";
				String b;
				byte[] bytes = kernelTable[i].getReplacedBytes();
//				if(bytes == null){
					bytes = new byte[10];
//				}
				for(int j = 0; j<10; j++){
					b = Integer.toBinaryString(bytes[j]&0xFF);
					int l = b.length();
					int expL = 8;
					if(l<expL){
						b = buff.substring(0, expL-l) +b;
					}
					ret = ret+b;
				}

				b = Integer.toBinaryString(kernelTable[i].getSynthConstPointer());
							
				int l = b.length();
				int expL = 10;
				if(l<expL){
					b = buff.substring(0, expL-l) +b;
				}
				
				ret = ret+b;
				
				b = Integer.toBinaryString(kernelTable[i].getContextPointer());
				
				l = b.length();
				expL = 10;
				if(l<expL){
					b = buff.substring(0, expL-l) +b;
				}
				ret = ret+b;
				
				
				bw.write(ret);
				bw.write("\n");				
			}
			bw.close();
			
			fw = new FileWriter("gen/liveinout.dat");
			bw = new BufferedWriter(fw);
			
			for(int i = 0; i<lVarPointer; i++){
				String buff = "00000000000000000000000000000000";
				String ret = Integer.toBinaryString(localVariableLiveInOut[i]);
				int l = ret.length();
				int expL = 32;
				if(l<expL){
					ret = buff.substring(0, expL-l) +ret;
				}
				
				
				bw.write(ret);
				bw.write("\n");
			}

			bw.close();
			
			fw = new FileWriter("gen/constMemory.dat");
			bw = new BufferedWriter(fw);
			
			for(int i = 0; i<constantPointer; i++){
				String buff = "00000000000000000000000000000000";
				String ret = Integer.toBinaryString(cgra.getConstMemory()[i]);
				int l = ret.length();
				int expL = 32;
				if(l<expL){
					ret = buff.substring(0, expL-l) +ret;
				}
				
				
				bw.write(ret);
				bw.write("\n");
			}
			
			
			bw.close();
			
			fw = new FileWriter("gen/locationInformation.dat");
			bw = new BufferedWriter(fw);
			
			for(int i = 0; i<locationInformationPointer; i++){
				LocationInformation locInfo = cgra.getLocationInformationTable()[i];
				
				String buff = "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
				String ret = Integer.toBinaryString(locInfo.getRegisterFileAddress());
				
				int l = ret.length();
				
				int expL = cgra.getModel().getMaxRegfileAddrWidth();
				if(l<expL){
					ret = buff.substring(0, expL-l) +ret;
				}
				
				if(locInfo.isSendInfo()){
					ret = Integer.toBinaryString(locInfo.getMux()) + ret;
					
					
					expL += cgra.getModel().getMaxMuxAddrWidth();
					
					l = ret.length();
					if(l<expL){
						ret = buff.substring(0, expL-l) +ret;
					}
					
					
					ret = Integer.toBinaryString(locInfo.getLiveOut()) + ret;
					
					
				} else {
//					String peSel = "";
					
					for(int pe = 0; pe < cgra.getModel().getNrOfPEs(); pe++){
						ret = (locInfo.getPESelect()[pe]?"1":"0") + ret;
					}
					
				}
				
				
				
				expL = cgra.getModel().getNrOfPEs();// TODO
				
				int expL2 = cgra.getModel().getViaWidth() + cgra.getModel().getMaxMuxAddrWidth();
				if(expL2 > expL){
					expL = expL2;
				}
				
				expL += cgra.getModel().getMaxRegfileAddrWidth();
				
				
				l = ret.length();
				if(l<expL){
					ret = buff.substring(0, expL-l) +ret;
				}
				
				
				bw.write(ret);
				bw.write("\n");
			}

			bw.close();
			
			
			fw = new FileWriter("gen/cgraKernel.dat");
			bw = new BufferedWriter(fw);
			
			for(int i = 0; i<kernelPointer; i++){
				String buff = "00000000000000000000000000000000";
				
				KernelTableEntryCGRA kernel = cgra.getKernelTableCGRA()[i];
				
				
				
				String ret = Integer.toBinaryString(kernel.getContextPointer());
				int l = ret.length();
				int expL = cgra.getModel().getCCNTWidth();
				if(l<expL){
					ret = buff.substring(0, expL-l) +ret;
				}
				
				
				
				ret = Integer.toBinaryString(kernel.getLocationInformationMemoryPointer()) + ret;
				
				expL += ((CgraModelAmidar)cgra.getModel()).getLocationInformationMemoryAddrWidth();
				
				l = ret.length();
				if(l<expL){
					ret = buff.substring(0, expL-l) +ret;
				}
				
				ret = Integer.toBinaryString(kernel.getConstantPointer()) + ret;
				expL += ((CgraModelAmidar)cgra.getModel()).getConstantMemoryAddrWidth();
				
				l = ret.length();
				if(l<expL){
					ret = buff.substring(0, expL-l) +ret;
				}
				
				
				ret = Integer.toBinaryString(kernel.getNrOfConstants()) + ret;
				expL += ((CgraModelAmidar)cgra.getModel()).getConstantMemoryAddrWidth();
				
				l = ret.length();
				if(l<expL){
					ret = buff.substring(0, expL-l) +ret;
				}
				
				
				
				bw.write(ret);
				bw.write("\n");
			}
			
			
			bw.close();
			
			
		
		} catch (IOException e) {
		}
		}
		
		///////////////// END PSEUDO PERIPHERY ACCESSS /////////////////////
		return returnValue;
	}
	

	public CGRA getCGRA(){
		if(functionalUnits.length == 10){
			CGRA cgra = (CGRA)functionalUnits[9];
			return cgra;
		} else {
			return null;
		}
	}

}
