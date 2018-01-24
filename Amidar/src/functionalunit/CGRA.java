package functionalunit;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;

import cgramodel.CBoxModel;
import cgramodel.CgraModel;
import cgramodel.CgraModelAmidar;
import cgramodel.ContextMask;
import cgramodel.ContextMaskPE;
import cgramodel.LocationInformation;
import tracer.TraceManager;
import exceptions.AmidarSimulatorException;
import functionalunit.cache.Cache;
import functionalunit.cache.Memory;
import functionalunit.cache.coherency.CoherenceController;
import functionalunit.cgra.CBoxWrapper;
import functionalunit.cgra.Cbox;
import functionalunit.cgra.ContextMem;
import functionalunit.cgra.ControlUnit;
import functionalunit.cgra.KernelTableEntryCGRA;
import functionalunit.cgra.PE;
import functionalunit.opcodes.CgraOpcodes;
import functionalunit.tokenmachine.KernelProfiler;
import generator.CgraInstruction;
import generator.StimulusAmidar;
import io.AttributeParser;
import io.CGRASerializer;
import target.Processor;

/**
 * This class is the frontend module for the CGRA. It emulates the toplevel module on a fined grained level. All
 * submodule emulation are triggered during simulation automatically. *  In order to trigger * the cycle based 
 * processing use the method operate().   
 * @author Dennis Wolf
 *
 */
public class CGRA extends FunctionalUnit<CgraOpcodes>  {

	/**
	 * The maximal number of PEs whose register file can be initialized via Token (see Token generation)
	 */
//	public static final int MAX_PES = 25;
//	public static final int MAX_REGFILE_ADDR_WIDTH = 32 - MAX_PES;
//	public static final int MUX_WIDTH = 5;
//	public static final int VIA_WIDTH = 4;
	

	LinkedHashMap<String,Integer> stateCNT = new LinkedHashMap();
	
	/** 
	 * New model of the cgra, which is to be inserted.
	 */
	CgraModel model;
	
	public int[][] stallCnt;
	public int[] visitCnt;

	
	public CgraModel getModel() {
		return model;
	}

	
	public void setCgraModel (CgraModel model) {
		this.model = model;
	}

	
	/**
	 * Array of all PEs in the CGRA
	 */
	public PE[] PEs;

	
	/**
	 * List of all PEs, which live out Connections 
	 */
	private int[] PEsliveOut;


	/**
	 * Array of all context-memories. Each PE is connected and and controlled to one context-memory.
	 */
	public ContextMem[] contextspes;


	/**
	 * Same information as Interconnect, but saved as a Set for the Scheduler   [in] -> LinkedList of all sources
	 */	
	public LinkedList<Integer>[] interconnectSet;

	/**
	 *  A magical box called "C"
	 */
	CBoxWrapper cboxWrapper;

	
	/**
	 * Context for the magical Boxes called "C"
	 */
	public ContextMem[] contextscboxevaluationboxes;
	
	/**
	 * Context magical wrapper of the cbox
	 */
	public ContextMem contextscboxwrapper;


	/**
	 * Control Unit manages the programm counter and stalls during a busy phase, when data is processed. 
	 */
	public ControlUnit controlunit;


	/**
	 * Valid inputs from the caches
	 */
	public boolean[] InputCacheValid;


	/**
	 * Valid output to the caches
	 */
	public boolean[] OutputCacheValid;

	
	/**
	 * Valid output to the caches
	 */
	public boolean[] OutputCacheWrite;
	
	/**
	 * Valid output to the caches
	 */
	public boolean[] wideDMAAccess;
	
	
	public int[] wideState;
	public int[] wideBuffer;
	public int[] lastContext;

	
	public boolean[] isPrefetch;
	
	
	/**
	 * Outputs holding the base and offset addresses. One each per PE that has cache access 
	 */
	public int[] cacheBaseAddress, cacheOffset;


	/**
	 * Data input/output from caches. One each per PE that has cache access 
	 */
	public int[] InputCacheData, OutputCacheData;


	/**
	 * Control signal to enable PEs
	 */
	private boolean enableSubmodules;

	
	/**
	 * Control signal to enable the control unit which generates the pc.
	 */
	public boolean CtrlEnable;

	
	/**
	 * Controlsignal to initial a write to all contexts. needs to be set in combination with 
	 */
	public boolean contextWrite;

	
	/**
	 * signal of joint cache enables
	 */
	public boolean BundledCacheEnable;

	
	/**
	 * helper variables or information
	 */
	public int pc,contextwidth,Cacheaddr,CacheOffSet, stateSizeControlUnit, contextsize,pboxcontextwidth,regsizeMax,ccntwidth;

	
	
	/**
	 * Coherence Controller
	 */
	CoherenceController coherenceController;
	
	/**
	 * Current state of the global FSM
	 */
	private CGRAState state = CGRAState.IDLE;
	
	
	/**
	 * Next state of the global FSM
	 */
	private CGRAState nextState = CGRAState.IDLE;
	
	
	private KernelProfiler kernelProfiler;

	ArrayList<CgraOpcodes> trackerOpcode =  new ArrayList<CgraOpcodes>();
	ArrayList<Integer> trackerOperandAddr =  new ArrayList<Integer>();
	ArrayList<Integer> trackerOperandData =  new ArrayList<Integer>();

	FileWriter fwRegDebug;
	FileWriter fwSystemDebug;
	
	ArrayList<StimulusAmidar> stimulus = new ArrayList<>();
//========== CACHE DEV ==========
	private Cache[] caches;
	private Memory mem;
	private int[] cacheTicks;
	private int[] cacheTicksBackground;
	
	public void createCaches(String configFile, TraceManager traceManager, CoherenceController coherenceController){
		for(int i = 0; i<caches.length; i++){
			caches[i] = new Cache(configFile, i+1, true, traceManager, coherenceController);
		}
		for(int i = 0; i<caches.length; i++){
			caches[i].setMOESICaches(caches);
		}
	}
	
	public Cache[] getCaches(){
		return caches;
	}
	//========== /CACHE DEV ==========
	

	int locationInformationTablePointer = 0;
	int contextPointer = 0;
	int constantPointer = 0;
	int nrOfConstants = 0;
	int constantCounter = 0;
	
	int[] constMemory;
	LocationInformation[] locationInformationTable;
	KernelTableEntryCGRA[] kernelTableCGRA;
	
	int currentConstant;
	LocationInformation currentLocationInfo;
	
	
	boolean[] PEhasDMA;
	
	
	/**
	 * Constructor of this class.  
	 */
	public CGRA(String configFile, boolean print, TraceManager traceManager, Memory mem, CoherenceController coherenceController){
		// TODO - OPcodes wechseln !
		super(CgraOpcodes.class, configFile, traceManager);
		this.mem = mem;
		createCaches(configFile, traceManager, coherenceController);
		input = new int[getNrOfInputports()];
		output =new int[1];
		
		
		
		constMemory = new int[((CgraModelAmidar)model).getConstantMemorySize()];
		locationInformationTable = new LocationInformation[((CgraModelAmidar)model).getLocationInformationMemorySize()];
		kernelTableCGRA = new KernelTableEntryCGRA[((CgraModelAmidar)model).getKernelTableSize()];
		this.coherenceController = coherenceController;
		
		stateCNT.put("WAIT", 0);
		stateCNT.put("REC", 0);
		stateCNT.put("SEND", 0);
		stateCNT.put("RUN", 0);
		stateCNT.put("DMA", 0);
	}
	
	public CGRA(String configFile, boolean print, TraceManager traceManager, Memory mem, KernelProfiler kernelProfiler, CoherenceController coherenceController){
		this(configFile, print, traceManager, mem, coherenceController);
		this.kernelProfiler = kernelProfiler;
	}

	
	/**
	 * States of class CGRA 
	 */
	public static enum CGRAState{
		IDLE,
		WRITECONTEXT_RECEIVE,
		RECEIVELOCALVAR,
		//		WRITE,
		WRITECONTEXT_SEND,
		SENDLOCALVAR,
		INIT,
		LOAD_CONST,
		LOAD_AND_WRITE_CONST,
		WRITE_CONST,
		SETADDRESS,
		RUN;
		//		ACK,
		//		SENDING;

		public static final int length = values().length;
	}

	ContextMask mask;

	protected void configureFU(String configFile){
		
		model = target.Processor.Instance.getAttributeParser().loadCgra(configFile);
		model.finalizeCgra();
		mask = new ContextMask();
		mask.setContextWidth(32);

		//Actual Hardware components
		cboxWrapper = new CBoxWrapper(model.getcBoxModel().getNrOfEvaluationBlocks(), model.getcBoxModel().getCBoxPredicationOutputsPerBox());
		int nrOfPEs = model.getNrOfPEs();
		PEs = new PE[nrOfPEs];
		contextspes = new ContextMem[nrOfPEs]; 

		PEhasDMA = new boolean[nrOfPEs];
		
		

		//HashMap<Integer,String> elements = (HashMap<Integer,String>) json.get("PEs");
		for(int i = 0; i<nrOfPEs ; i++){
			PEs[i]=new PE();
			contextspes[i] = new ContextMem();	
			contextspes[i].configureContext(model.getContextMemorySize(), i);
			PEs[i].configure(model.getPEs().get(i),this);
			PEs[i].createPorts();
			PEhasDMA[i] = PEs[i].memoryAccess();
		}

		contextscboxevaluationboxes = new ContextMem[model.getcBoxModel().getNrOfEvaluationBlocks()];
		for(int i = 0; i< contextscboxevaluationboxes.length; i++){
			contextscboxevaluationboxes[i] = new ContextMem();
			contextscboxevaluationboxes[i].configureContext(model.getContextMemorySize(), model.getNrOfPEs()+i);
		}
		contextscboxwrapper = new ContextMem();
		contextscboxwrapper.configureContext(model.getContextMemorySize(),model.getNrOfPEs()+contextscboxevaluationboxes.length);
		int cachecounter = model.getNrOfMemoryAccessPEs();
		OutputCacheData = new int [cachecounter];
		InputCacheData = new int [cachecounter];
		InputCacheValid = new boolean [cachecounter];
		OutputCacheValid = new boolean [cachecounter];
		OutputCacheWrite = new boolean [cachecounter];
		cacheBaseAddress = new int [cachecounter];
		cacheOffset = new int [cachecounter];
		wideDMAAccess = new boolean [cachecounter];
		wideState = new int [cachecounter];
		wideBuffer = new int [cachecounter];
		isPrefetch = new boolean[cachecounter];
		lastContext = new int [cachecounter];
		contextsize = model.getContextMemorySize();
		cacheTicks = new int[cachecounter];
		cacheTicksBackground = new int[cachecounter];
		
		//========== CACHE DEV ==========
		caches = new Cache[cachecounter];
		stallCnt = new int [model.getContextMemorySize()][cachecounter];
		visitCnt = new int [model.getContextMemorySize()];
		//========= /CACHE DEV ==========

		int max=0;
		for(PE i : PEs){
			if(max<i.getNrOfInputports()){
				max = i.getNrOfInputports();
			}
		}
			
		cboxWrapper.configure(model);
		controlunit = new ControlUnit();
		stateSizeControlUnit = contextsize;
		ccntwidth = controlunit.configure(contextsize,model.getContextmaskccu());
		int liveouts = 0;
		for(int i = 0; i <PEs.length;i++){
			if(PEs[i].getModel().getLiveout()){
				liveouts++;
			}
		}
		PEsliveOut = new int[liveouts];
		
		liveouts = 0;
		int cboxmappingcounter = 0;
		for(int i = 0; i <PEs.length;i++){
			if(PEs[i].getModel().getLiveout()){
				PEsliveOut[liveouts] = i;
				liveouts ++;
			}
			if(PEs[i].getModel().getControlFlow()){
				cboxWrapper.setInputMapping(cboxmappingcounter,i);
				cboxmappingcounter++;
			}
		}
		
		resetCGRA();
		
		
		
		super.configureFU(configFile);

		try {
			File folder = new File(Processor.Instance.getDebuggingPath());
			if(!folder.exists()){
				folder.mkdir();
			}
		File debugregistesfile = new File(Processor.Instance.getDebuggingPath()+"/debug_registerfiles_emulation");
		File debugsystemfile = new File(Processor.Instance.getDebuggingPath()+"/debug_ALU_emulation");
		
			fwRegDebug = new FileWriter(debugregistesfile);
			fwSystemDebug = new FileWriter(debugsystemfile);
		} catch (IOException e) {
			System.err.println("opening filewriter in cgra didn't work");
		}
	}

	/**
	 * Returns Number of Inputports
	 */
	public int getNrOfInputports() {
		return 4;
	}

	/**
	 * Returns an Array of all PEs available in the CGRA  
	 */
	public PE[] getPEs(){
		return PEs;
	}

	/**
	 * Returns the ID of the PE 
	 */
	public PE getPeViaId(int id){
		return PEs[id];
	}

	/**
	 * Returns the Number of PEs
	 */
	public int getNumberOfPes(){
		return PEs.length;	
	}


	public LinkedList<Integer>[] getPeConnections(){
		return interconnectSet;
	}
	
	public ArrayList<StimulusAmidar> getStimulus(){
		int i = 0;
		for(i = 0; i < PEs.length; i++){
			stimulus.add(0, new StimulusAmidar(CgraInstruction.LOADPROGRAM, contextspes[i].memory, i));
		}
		
		stimulus.add(0, new StimulusAmidar(CgraInstruction.LOADPROGRAM, controlunit.memory, i++));
		stimulus.add(0, new StimulusAmidar(CgraInstruction.LOADPROGRAM, contextscboxevaluationboxes[0].memory, i));
		
		
		return stimulus;
	}
	
	public long[][] getContextCopyPEs(){
		long[][] contexts = new long[model.getNrOfPEs()][];
		for(int i = 0; i < PEs.length; i++){
			contexts[i] = contextspes[i].memory;
		}
		return contexts;
	}
	
	public long[] getContextCopyCCU(){
		return controlunit.memory;
	}

	public long[] getContextCopyCBOX(){
		return contextscboxevaluationboxes[0].memory;
	}
	
	/**
	 *	Copies a new Synthesis into the Context memory for all PEs with a magical Hand.
	 */
	public boolean newSynthesisContext(long[][] synthesis, int startingSlots ){
		for(int i = 0; i < PEs.length; i++){
			contextspes[i].setContext(synthesis[i], startingSlots);
		}
		return true;
	}

	/**
	 * method to set the next state. emulates the behaviour of a real hardware
	 */
	private void nextState(CgraOpcodes op){

		switch(getState()){

		case IDLE:
			//			if(tokenValid && validInputs(InputOpcode)){
			switch(op){
			case RUN:
				nextState = CGRAState.SETADDRESS;
				break;
			case RECEIVELOCALVAR:
				nextState = CGRAState.WRITECONTEXT_RECEIVE;
				break;
			case SENDLOCALVAR:
				nextState = CGRAState.WRITECONTEXT_SEND;
				break;
			case INIT:
				nextState = CGRAState.INIT;
				break;
			default : throw new AmidarSimulatorException("Unkown Opcode found in CGRA");
			}
			//			}
			break;

		case WRITECONTEXT_RECEIVE:
			stateCNT.put("REC", stateCNT.get("REC") + 3);
			nextState = CGRAState.RECEIVELOCALVAR;
			break;
		case RECEIVELOCALVAR:
			nextState = CGRAState.IDLE;
			break;
			//		case WRITE:
			//			nextState = CGRAState.IDLE;
			//			break;
		case WRITECONTEXT_SEND:
			stateCNT.put("SEND", stateCNT.get("SEND") + 3);
			nextState = CGRAState.SENDLOCALVAR;
			break;
		case SENDLOCALVAR:
			nextState = CGRAState.IDLE;
			break;
		case SETADDRESS:
			nextState = CGRAState.RUN;
			cycle = 0;
			runCounter++;
//						for(PE pe :getPEs()){
			//				for(Operator ops: pe.getModel().getAvailableNonNativeOperators().keySet()){
			//					System.out.println(pe.PeID + "  " + pe.getModel().getAvailableNonNativeOperators().get(ops).getName() + "  " + pe.getModel().getAvailableNonNativeOperators().get(ops).getLatency());
			//					
			//				}
//							for(int i = 0; i< pe.regfile.registers.length;i++){
//								System.out.println(pe.PeID + " "+i +"  " + pe.regfile.registers[i]);
//							}
//							System.out.println("\n");
//						}

			break;

		case RUN:
			stateCNT.put("RUN", stateCNT.get("RUN") + 1);
			if(pc == contextsize-1){
				kernelProfiler.stopKernel(cycle);
				nextState = CGRAState.IDLE;
				debugregisterfiles = false;
			}
			break;
		case INIT:
			if(constantCounter < nrOfConstants){
				nextState = CGRAState.LOAD_CONST;
			} else {
				nextState = CGRAState.IDLE;
			}
			break;
		case LOAD_CONST:
		case LOAD_AND_WRITE_CONST:
//			if(constantsCounter == nrOfConstants){
//				nextState = CGRAState.WRITE_CONST;
//			} else {
//				nextState = CGRAState.LOAD_AND_WRITE_CONST;
//			}
			stateCNT.put("REC", stateCNT.get("REC") + 2);
			nextState = CGRAState.WRITE_CONST;
			
			break;
		case WRITE_CONST:
			if(constantCounter < nrOfConstants){
				nextState = CGRAState.LOAD_CONST;
			} else {
				nextState = CGRAState.IDLE;
			}
			break;
		default: throw new AmidarSimulatorException("Not existing state found in CGRA : " + getState());
		}		
		if(nextState != null)
			setState(nextState);	
	}

	/**
	 * Resets the CGRA
	 */
	public void resetCGRA(){
		//		OutputAck = false;
		setResultAck(false);
		controlunit.setLoadEnable(false);
		enableSubmodules = false;
		contextWrite = false;
		state = CGRAState.IDLE;


		for(int i = 0; i<PEs.length ;i++){
			PEs[i].fetchContext(contextspes[i].combinatorial());
//			PEs[i].regfile.reset();
		}
		for(int i = 0; i< contextscboxevaluationboxes.length; i++){
			cboxWrapper.fetchContextEvaluationBox(contextscboxevaluationboxes[i].combinatorial(),i);
		}
	}


	public int cycle = 0;
	private int startCycles = 0;
	
	
	public int runCounter = 0;

	/**
	 * Main function to be triggered every cycle. Is ideally split into 3 steps. 
	 * 	1) clocked signal  
	 *  2) combinatorial signals 
	 *  3) combinatorial signals due to a feedback on module level 
	 */

    int lastVAL;

	public boolean executeOp(CgraOpcodes op){
		
		BundledCacheEnable = true;
		
		//========== CACHE DEV ==========
        for(int i = 0; i<caches.length; i++){
                if(!InputCacheValid[i]){
                        if(cacheTicks[i] == 0) InputCacheValid[i] = true;
                        else cacheTicks[i]--;
                        if(cacheTicks[i] == 1)         		stallCnt[controlunit.getProgramCounter()][i]++;
                }
        }
        //===============================

		

//		for(boolean b: InputCacheValid){
//			if(!b){
//				BundledCacheEnable = false;
//			}
//		}	
        for(int i = 0; i < caches.length; i++){
        	if(!InputCacheValid[i]){
        		BundledCacheEnable = false;
        		stateCNT.put("DMA", stateCNT.get("DMA")+1);
        	}
        }
        if(BundledCacheEnable){
        	visitCnt[controlunit.getProgramCounter()]++;
//        	if(controlunit.getProgramCounter()>=581 && controlunit.getProgramCounter()<868  )
//        	System.err.print("RUN " );
        	for(int i = 0; i<caches.length; i++){
//        		if(controlunit.getProgramCounter()>=581 && controlunit.getProgramCounter()<868  )
//        		System.err.print(cacheTicksBackground[i] + " ");
        		if(cacheTicksBackground[i]> 0){
        			cacheTicksBackground[i]--;
        		}
        	}
//        	if(controlunit.getProgramCounter()>=581 && controlunit.getProgramCounter()<868  )
//        	System.err.println();
        	
        } else {
//        	System.err.print("WAIT ");
        	cacheWaitTime++;
        	for(int i = 0; i<caches.length; i++){
//        		System.err.print(cacheTicks[i] + " ");
        	}
//        	System.err.println();
        }
        
//        System.err.println("cohreencecontr: " + coherenceController.getBusyTime());
        
		// only for debugging purposes
		cycle ++;
		
		
		
//		System.out.println("CGRA TTp : " + (controlunit.getProgramCounter()));
		

		// prints register content at the beginning of a run 
		if(state == CGRAState.SETADDRESS){
			debugregisterfiles = true;
		}

		/*
		 * Basically the Enable block of a Verilog description 
		 */
		if(BundledCacheEnable || (state != CGRAState.RUN )) {
			state = nextState;
			operateSubmodulesClocked();
		}

		switch(getState()){		
		case IDLE:
			controlunit.setLoadEnable(false);
			enableSubmodules = false;
			CtrlEnable=false;
			contextWrite = false;
			operateSubmodulesComb();
			// otherwise nothing should happen here
			break;
		case WRITECONTEXT_RECEIVE:
//			stimulus.add(new StimulusAmidar(CgraInstruction.RECEIVELOCALVAR,input[OPERAND_A_LOW], input[OPERAND_B_LOW]));
			// this input operand_data is actually connected to all context inputs
			//			if(input[OPERAND_ADDRESS] > PEs.length-1 )
			//				throw new AmidarSimulatorException("Attempt to write an non existing PE with ID : " + input[OPERAND_ADDRESS]);
			currentLocationInfo = locationInformationTable[locationInformationTablePointer++];
			int address = currentLocationInfo.getRegisterFileAddress(); 
        
//        System.out.println("REC: " + input[OPERAND_B_LOW]);

        
//        System.out.println("--------------------------   " + input[OPERAND_B_LOW]);
//        System.out.println("getting: " + Double.longBitsToDouble(((long)input[OPERAND_B_LOW]<<32)+(0xFFFFFFFF&lastVAL)));
//        System.out.println("getting: " + Float.intBitsToFloat(input[OPERAND_B_LOW]));
        lastVAL = input[OPERAND_B_LOW];
        
        for(int i = 0; i <PEs.length;i++){
        	PEs[i].setInputAmidar((int) input[OPERAND_B_LOW]);
        	if(currentLocationInfo.getPESelect()[i]){
        		ContextMaskPE mask = PEs[i].getModel().getContextMaskPE();
        		long converted = 0;
        		converted = mask.setAddrWr( converted, address);
        		converted = mask.setWriteEnable(converted, true);
        		converted = mask.setMuxReg(converted, PE.IN);
        		contextspes[i].setInputData(converted);
        	}
        	else{
        		contextspes[i].setInputData((long) 0);
        	}
        }			
        //			OutputResult_low_valid = false;
        controlunit.setLoadEnable(false);
        enableSubmodules = false;
        contextWrite = true;
        CtrlEnable=false;
        operateSubmodulesComb();
        break;
        case RECEIVELOCALVAR:
        	setResultAck(true);
        	//			OutputResult_low_valid = false;
        	controlunit.setLoadEnable(false);
        	enableSubmodules = true;
        	contextWrite = false;
			CtrlEnable=false;
			operateSubmodulesComb();
			break;
		case WRITECONTEXT_SEND:
			// this input operand_data is actually connected to all context inputs
			//			if(input[OPERAND_ADDRESS] > PEs.length-1 )
			//				throw new AmidarSimulatorException("Attempt to write an non existing PE with ID : " + input[OPERAND_ADDRESS]);
			currentLocationInfo = locationInformationTable[locationInformationTablePointer++];
			address = currentLocationInfo.getRegisterFileAddress(); 
//			address = input[OPERAND_A_LOW] >> (model.getMaxMuxAddrWidth() + model.getViaWidth());
		int mux = currentLocationInfo.getMux();
		for(int i = 0; i <PEs.length;i++){
            int muxTmp = mux;
            ContextMaskPE mask = PEs[i].getModel().getContextMaskPE();
            long converted = 0;
            converted =    mask.setAddrMux(converted, address);
            converted = mask.setAddrDo(converted,address);
            if(PEs[i].getModel().getInputs().size()<mux){     // the value of mux is only relevant for the PE with the liveout conection providing the desired value
                muxTmp = 0; // for all others it is irrelevant. For some this might lead to a array index out of bound exception
            } // Thus we limit mux for those PEs. (In HW this isn't necessary)
            converted = mask.setMuxB(converted, muxTmp);
            contextspes[i].setInputData(converted); 
		}			
		//			OutputResult_low_valid = false;
		controlunit.setLoadEnable(false);
		enableSubmodules = false;
		contextWrite = true;
		CtrlEnable=false;
		operateSubmodulesComb();
		break;
		case SENDLOCALVAR:
//			System.out.println("EXECUTED " + cycle + " ticks");
			int liveOut = currentLocationInfo.getLiveOut(); 
			//			OutputResult_low_valid = false;
			controlunit.setLoadEnable(false);
			enableSubmodules = true;
			contextWrite = false;
			CtrlEnable=false;

			operateSubmodulesComb();
			output[RESULT_LOW] = PEs[PEsliveOut[liveOut]].getOffsetCache();
//			System.out.println("SEND: " + output[RESULT_LOW]);
			setOutputValid(RESULT_LOW);
			stimulus.add(new StimulusAmidar(CgraInstruction.SENDLOCALVAR, input[OPERAND_A_LOW], input[OPERAND_B_LOW], output[RESULT_LOW]));
			break;
		case SETADDRESS:
			stimulus.add(new StimulusAmidar(CgraInstruction.RUN, input[OPERAND_A_LOW], 99));
			
			cacheTicks = new int[cacheTicks.length];
			
			setResultAck(true);
			//			OutputResult_low_valid = false;
			controlunit.setLoadEnable(true);
			enableSubmodules = false;
			controlunit.setInputData(contextPointer);
			CtrlEnable=true;
			contextWrite = true;
			for(int i = 0; i <PEs.length;i++){
				contextspes[i].setInputData((long) 0);
			}
			for(int cache = 0; cache < wideState.length; cache++){
				wideState[cache] = 0;
			}
			operateSubmodulesComb();
			break;
		case RUN:
			setResultAck(true);
			//			OutputResult_low_valid = false;
			controlunit.setLoadEnable(false);
			enableSubmodules = true;
			CtrlEnable=true;
			contextWrite = false;
			operateSubmodulesComb();
			break;
		case INIT:
//			System.out.println("STARTING KERNEL " + input[OPERAND_A_LOW]);
			KernelTableEntryCGRA currentKernel = kernelTableCGRA[input[OPERAND_A_LOW]];
			contextPointer = currentKernel.getContextPointer();
			locationInformationTablePointer = currentKernel.getLocationInformationMemoryPointer();
			constantPointer = currentKernel.getConstantPointer();
			nrOfConstants = currentKernel.getNrOfConstants();
			constantCounter = 0;
			setResultAck(true);
			break;
		case LOAD_CONST:
			currentLocationInfo = locationInformationTable[locationInformationTablePointer++];
			address = currentLocationInfo.getRegisterFileAddress(); 
			
			currentConstant = constMemory[constantPointer++];
	        constantCounter++;
//	        System.out.println("REC CONST: " + currentConstant);

	        
//	        System.out.println("--------------------------   " + input[OPERAND_B_LOW]);
//	        System.out.println("getting: " + Double.longBitsToDouble(((long)input[OPERAND_B_LOW]<<32)+(0xFFFFFFFF&lastVAL)));
//	        System.out.println("getting: " + Float.intBitsToFloat(input[OPERAND_B_LOW]));
	        lastVAL = currentConstant;
	        
	        for(int i = 0; i <PEs.length;i++){
	        	PEs[i].setInputAmidar((int) currentConstant);
	        	if( currentLocationInfo.getPESelect()[i]){
	        		ContextMaskPE mask = PEs[i].getModel().getContextMaskPE();
	        		long converted = 0;
	        		converted = mask.setAddrWr( converted, address);
	        		converted = mask.setWriteEnable(converted, true);
	        		converted = mask.setMuxReg(converted, PE.IN);
	        		contextspes[i].setInputData(converted);
	        	}
	        	else{
	        		contextspes[i].setInputData((long) 0);
	        	}
	        }			
	        //			OutputResult_low_valid = false;
	        controlunit.setLoadEnable(false);
	        enableSubmodules = false;
	        contextWrite = true;
	        CtrlEnable=false;
	        operateSubmodulesComb();
	        break;
	        case WRITE_CONST:
	        	setResultAck(true);
	        	//			OutputResult_low_valid = false;
	        	controlunit.setLoadEnable(false);
	        	enableSubmodules = true;
	        	contextWrite = false;
				CtrlEnable=false;
				operateSubmodulesComb();
				break;
		default: throw new AmidarSimulatorException("Not existing state found in CGRA : " + getState());
		}

		
		nextState(op);
//		printStatusDebug();
		return nextState == CGRAState.IDLE;
	}

	boolean debugregisterfiles = false;

	/**
	 * Debug method that prints the status of the current CGRA 
	 */
	public void printStatusDebug(){

//
//		if(debugregisterfiles){
//			try {
//				fwRegDebug.write("--------- cycle: "+ cycle + "(Run "+runCounter+")" +"----------\n");
//				
//				for(int i = 0; i <cbox.getCBoxModel().getMemorySlots();i++){
//					int entry = (cbox.regfile[i]) ? 1 : 0;
//						fwRegDebug.write(entry+"\n");
//				}
//				fwRegDebug.write("---\n");				
//				
//				for(int pe = 0;pe < getNumberOfPes();pe++){
//					for(int reg = 0;reg < PEs[pe].regfile.registers.length;reg++){
//						if(!PEs[pe].regfile.registerusage[reg]){
//							fwRegDebug.write("x\n");
//						}
//						else{
//							fwRegDebug.write(PEs[pe].regfile.registers[reg]+"\n");
//						}
//					}
//					fwRegDebug.write("---\n");
//				}
//				
//				fwRegDebug.write("\n \n \n");		
//
//				fwSystemDebug.write("--------- cycle:"+ cycle + "(Run "+runCounter+")" +"----------\n");
//				fwSystemDebug.write("ccu : "+controlunit.getProgramCounter() +"\n");
//				for(PE pe:PEs){
//					fwSystemDebug.write(" ------- PE " + pe.getModel().getID()  + "\n");
//					fwSystemDebug.write("Context : " +pe.context + "\n");
//					fwSystemDebug.write("loading entry : " + pe.contextmask.addrMux(pe.context) + "\n");
//					if(pe.inputALUAdefined){
//						fwSystemDebug.write("A : " + pe.inputALUA + "\n"); 
//					}
//					else{
//						fwSystemDebug.write("A : x\n");
//					}
//						
////					if(pe.contextmask.muxA(pe.context) >= pe.inputs.length){
////						fwSystemDebug.write("(reg) \n");
////					}
////					else{
////						fwSystemDebug.write("(PE "+ pe.inputs[pe.contextmask.muxA(pe.context)] +") \n");
////					}
//					if(pe.inputALUBdefined){
//						fwSystemDebug.write("B : " + pe.inputALUB+ "\n");
//					}
//					else{
//						fwSystemDebug.write("B : x\n");
//					}
////					if(pe.contextmask.muxB(pe.context) >= pe.inputs.length){
////						fwSystemDebug.write("(reg) \n");
////					}
////					else{
////						fwSystemDebug.write("(PE "+ pe.inputs[pe.contextmask.muxB(pe.context)] +") \n");
////					}
//					fwSystemDebug.write("op - " + pe.contextmask.operation(pe.context) + "\n");
//					fwSystemDebug.write("R : " + pe.outputALU);
//					if(pe.regfile.getWriteEnable()){
//						fwSystemDebug.write(" ( -> "+ pe.regfile.getWriteAddress() +")");
//					}
//					fwSystemDebug.write("\n");
//					if(pe.controlFlow()){
//						fwSystemDebug.write("S : " + pe.getStatus() + "\n");
//					}
//					fwSystemDebug.write("\n");		
//				}
//				
//				fwSystemDebug.write("------- CBOX \n");
//				fwSystemDebug.write("Context : "+cbox.context+" \n");
//				int pred = 0;
//				if(cbox.getPredicationOutput(0)){
//					pred = 1;
//				}
//				fwSystemDebug.write("Predication out: "+pred+" \n");
//				int sel = 0;
//				if(cbox.getBranchSelectionSignal()){
//					sel = 1;
//				}
//				fwSystemDebug.write("Branchselection out: "+sel +" \n");
//				fwSystemDebug.write("\n");
//				
//				fwSystemDebug.write("\n \n");
//			} catch (IOException e) {
//				System.err.println("probleme while writing debugged in cgra");
//			}	
//		}
	}
	
	/**
	 * First step of operate(). Triggers all clocked gates
	 */
	private void operateSubmodulesClocked(){

		controlunit.operateClocked();
		for(int i = 0; i<PEs.length ;i++){
			contextspes[i].setInputCCNT(controlunit.getProgramCounter());
//			System.out.println("\tPPPE: "+i);
			PEs[i].regClocked();
			contextspes[i].clocked();
		}
		for(int i = 0; i< contextscboxevaluationboxes.length; i++){
			contextscboxevaluationboxes[i].setInputCCNT(controlunit.getProgramCounter());
			contextscboxevaluationboxes[i].clocked();
		}
		
		contextscboxwrapper.setInputCCNT(controlunit.getProgramCounter());
		contextscboxwrapper.clocked();
		cboxWrapper.operateClocked();
	}
	
	public static int musage= 0;
	public static int writeMusage = 0;
	public static int specialMusage = 0;

	/**
	 * Second step of operate(). Triggers all combinatorial processes. The order is highly important!
	 */
	private void operateSubmodulesComb() {

		// store pc to propagate it
		controlunit.operateComb();	
		pc = controlunit.getProgramCounter();
		//		System.out.println("\n PC - " + pc );

		for(int i = 0; i<PEs.length ;i++){
			contextspes[i].setInputWriteEnable(contextWrite);
			PEs[i].fetchContext(contextspes[i].combinatorial());
			PEs[i].setInputEnable(enableSubmodules);
			PEs[i].regComb();
		}

		int cachecnt = 0;
		for(int i = 0; i<PEs.length ;i++){
			if(BundledCacheEnable || state != CGRAState.RUN ){//stall the ALUs...
				PEs[i].operate();
			}
			if(model.getPEs().get(i).getControlFlow()){
				cboxWrapper.setInputMapped(i, PEs[i].getStatus());
			}
			///HERE DMA CACHE
			if(PEhasDMA[i]){
//				PEs[i].setInputCacheData(InputCacheData[cachecnt]);
				cachecnt++;
			}
		}
		cboxWrapper.setEnable(enableSubmodules);
		for(int i = 0; i < contextscboxevaluationboxes.length; i++){
			
			long contexxxt = contextscboxevaluationboxes[i].combinatorial();
			cboxWrapper.fetchContextEvaluationBox(contexxxt,i);
		}
		cboxWrapper.fetchContextWrapper(contextscboxwrapper.combinatorial());
		cboxWrapper.operateComb();

		// propagate outputof cbox to controlunit
		controlunit.setInputCbox(cboxWrapper.getBranchSelectionSignal());
		controlunit.setInputEnable(CtrlEnable);
		cachecnt = 0;
		
		boolean usedCache = false;
		String cacheUsage = "";
		musage = 0;
		writeMusage = 0;
		specialMusage = 0;
		
		
		for(int i = 0; i<PEs.length; i++){
			boolean predication = cboxWrapper.getPredicationOutput(PEs[i].getCBoxSelect());
			
			PEs[i].setInputCBox(predication);
			PEs[i].checkException();
			// additional combinatorial signals - mainly preparation for next cycle 
			
			if(PEhasDMA[i]){
				OutputCacheData[cachecnt] = PEs[i].getOutputCache();
				OutputCacheValid[cachecnt] = PEs[i].getOutputCacheValid();
				OutputCacheWrite[cachecnt] = PEs[i].getOutputCacheWrite();
				cacheOffset[cachecnt] = PEs[i].getOffsetCache();
				cacheBaseAddress[cachecnt]	= PEs[i].getBaseAddrCache();
				wideDMAAccess[cachecnt] = PEs[i].isWideDMAAcsess();
				isPrefetch[cachecnt] = PEs[i].isPrefetch();
				
				//========== CACHE DEV ==========
				if(BundledCacheEnable && controlunit.getProgramCounter() != model.getContextMemorySize()-1){
					if(cacheTicks[cachecnt] == 0){
						if(OutputCacheValid[cachecnt]){
							cacheUsage += (cachecnt+ " " + (OutputCacheWrite[cachecnt]?"W: ": "R: ") + cacheBaseAddress[cachecnt]+ " + " +cacheOffset[cachecnt] + "\t");
							usedCache = true;
							
							if(lastContext[cachecnt] != controlunit.getProgramCounter()-1){
								wideState[cachecnt] = 0; // means we start a new access
							}
							if(isPrefetch[cachecnt]){
								if(caches[cachecnt].holdsValue(cacheBaseAddress[cachecnt], cacheOffset[cachecnt])){
									caches[cachecnt].requestData(cacheBaseAddress[cachecnt], cacheOffset[cachecnt]);
									InputCacheData[cachecnt] = caches[cachecnt].getData(cacheBaseAddress[cachecnt], cacheOffset[cachecnt]);
//									caches[cachecnt].requestPrefetch(cacheBaseAddress[cachecnt], cacheOffset[cachecnt]);
								} else {
									caches[cachecnt].requestPrefetch(cacheBaseAddress[cachecnt], cacheOffset[cachecnt]);
									InputCacheData[cachecnt] = Integer.MIN_VALUE;
								}
								
								
							} else 
							if(OutputCacheWrite[cachecnt]){
								LinkedHashSet<Integer> pes = dmawritesHandleTOPEmap.get(cacheBaseAddress[cachecnt]);
								if(pes == null){
									pes = new LinkedHashSet<>();
									dmawritesHandleTOPEmap.put(cacheBaseAddress[cachecnt], pes);
								}
								
								pes.add(cachecnt);
								
//								if(pes.size()>1){
//									System.err.println("Ahoiiiiiiiii ihr landratten");
//								}
								
								
								
								
//								System.out.println("CACHEWRI"+cachecnt +" " + wideDMAAccess[cachecnt] + "("+controlunit.getProgramCounter()+") ");
//								System.out.println("\t" + cacheBaseAddress[cachecnt] + " + " + cacheOffset[cachecnt] + " : " + OutputCacheData[cachecnt]);
								if(!wideDMAAccess[cachecnt]){
//									cacheTicks[cachecnt] = cacheTicksBackground[cachecnt];
									cacheTicks[cachecnt] = caches[cachecnt].writeData(cacheBaseAddress[cachecnt], cacheOffset[cachecnt], OutputCacheData[cachecnt]);
//									time += cacheTicks[cachecnt];
									nrOfCacheAccesses++;
									if(cacheTicks[cachecnt] != 0){
										cacheTicks[cachecnt] += cacheTicksBackground[cachecnt];
										
										cacheTicksBackground[cachecnt] = 0;
									}  else if( cacheOffset[cachecnt] < 100000 && cacheTicksBackground[cachecnt] == 0){
//										cacheTicksBackground[cachecnt] = caches[cachecnt].requestData(cacheBaseAddress[cachecnt], cacheOffset[cachecnt]+ 8);
//										if(cacheTicksBackground[cachecnt] == 0)
//										cacheTicksBackground[cachecnt] = 1 + caches[cachecnt].requestData(cacheBaseAddress[cachecnt], cacheOffset[cachecnt]+ 16);
//										if(cacheTicksBackground[cachecnt] == 1)
//											cacheTicksBackground[cachecnt] += 1+ caches[cachecnt].requestData(cacheBaseAddress[cachecnt], cacheOffset[cachecnt]+ 24);
//										if(cacheTicksBackground[cachecnt] == 2)
//											cacheTicksBackground[cachecnt] += 1 + caches[cachecnt].requestData(cacheBaseAddress[cachecnt], cacheOffset[cachecnt]+ 32);
									}

								} else {
									lastContext[cachecnt] = controlunit.getProgramCounter();
									if(wideState[cachecnt] == 0){
										wideState[cachecnt] = 1;
										cacheTicks[cachecnt] = caches[cachecnt].writeData(cacheBaseAddress[cachecnt], cacheOffset[cachecnt]*2, OutputCacheData[cachecnt]);
										wideBuffer[cachecnt] = OutputCacheData[cachecnt];
									} else {
										wideState[cachecnt]=0;
										cacheTicks[cachecnt] = caches[cachecnt].writeData(cacheBaseAddress[cachecnt], cacheOffset[cachecnt]*2+1, OutputCacheData[cachecnt]);
//										System.out.println("CWR: " + wideBuffer[cachecnt] + ":" + OutputCacheData[cachecnt]);
									}
									
								}
							}
							else{
//								if(controlunit.getProgramCounter() == 208 || controlunit.getProgramCounter() == 207){
//									System.out.println("CACHEREA"+cachecnt+" " + wideDMAAccess[cachecnt]+"---------------------------");
//									System.out.print("\t" + cacheBaseAddress[cachecnt] + " + " + cacheOffset[cachecnt] + " : ");
//								}
								if(!wideDMAAccess[cachecnt]){
//									if(controlunit.getProgramCounter()>441 && controlunit.getProgramCounter()<680  )
//									System.out.println("CACHEREA"+cachecnt+" " + wideDMAAccess[cachecnt]+"--------------------------- " + controlunit.getProgramCounter() +" " +model.getContextMemorySize());
//									if(controlunit.getProgramCounter()>441 && controlunit.getProgramCounter()<680  )
//									System.err.print(cachecnt + "\t" + cacheBaseAddress[cachecnt] + " + " + cacheOffset[cachecnt] + " : ");
//									cacheTicks[cachecnt] = cacheTicksBackground[cachecnt];
									cacheTicks[cachecnt] =  caches[cachecnt].requestData(cacheBaseAddress[cachecnt], cacheOffset[cachecnt]);
//									time += cacheTicks[cachecnt];
									nrOfCacheAccesses++;
//									System.out.println(InputCacheData[cachecnt]);
									InputCacheData[cachecnt] = caches[cachecnt].getData(cacheBaseAddress[cachecnt], cacheOffset[cachecnt]);
//									System.out.println(InputCacheData[cachecnt]);
									if(cacheTicks[cachecnt] != 0){
//										musage++;
										cacheTicks[cachecnt] += cacheTicksBackground[cachecnt];
										cacheTicksBackground[cachecnt] = 0;
									} else if( cacheOffset[cachecnt] < 100000 && cacheTicksBackground[cachecnt] == 0){
//										cacheTicksBackground[cachecnt] = caches[cachecnt].requestData(cacheBaseAddress[cachecnt], cacheOffset[cachecnt]+ 8);
//										if(cacheTicksBackground[cachecnt] == 0)
//											cacheTicksBackground[cachecnt] = 1 + caches[cachecnt].requestData(cacheBaseAddress[cachecnt], cacheOffset[cachecnt]+ 16);
//										if(cacheTicksBackground[cachecnt] == 1)
//											cacheTicksBackground[cachecnt] += 1+ caches[cachecnt].requestData(cacheBaseAddress[cachecnt], cacheOffset[cachecnt]+ 24);
//										if(cacheTicksBackground[cachecnt] == 2)
//											cacheTicksBackground[cachecnt] += 1 + caches[cachecnt].requestData(cacheBaseAddress[cachecnt], cacheOffset[cachecnt]+ 32);
									} else {
//										cacheTicksBackground[cachecnt] = 0;
									}
									
									
//									if(controlunit.getProgramCounter()>441 && controlunit.getProgramCounter()<680  )
//									System.err.println(InputCacheData[cachecnt] + "  ("+(InputCacheData[cachecnt]));
									
									
								} else {
									lastContext[cachecnt] = controlunit.getProgramCounter();
									if(wideState[cachecnt]==0){
										wideState[cachecnt]=1;
										cacheTicks[cachecnt] = caches[cachecnt].requestData(cacheBaseAddress[cachecnt], cacheOffset[cachecnt]*2);
										wideBuffer[cachecnt]= caches[cachecnt].getData(cacheBaseAddress[cachecnt], cacheOffset[cachecnt]*2);
										InputCacheData[cachecnt] = wideBuffer[cachecnt];
									} else if(wideState[cachecnt]==1){
										cacheTicks[cachecnt] = caches[cachecnt].requestData(cacheBaseAddress[cachecnt], cacheOffset[cachecnt]*2 + 1);
										wideState[cachecnt]=2;
									} else if(wideState[cachecnt]==2){
										InputCacheData[cachecnt] = caches[cachecnt].getData(cacheBaseAddress[cachecnt], cacheOffset[cachecnt]*2 + 1);
										wideState[cachecnt]=3;
									} else {
//										System.out.println("CACHEREA"+cachecnt+" " + wideDMAAccess[cachecnt]+"---------------------------");
										
										
										wideState[cachecnt] = 0;
//										System.out.println("     " + InputCacheData[cachecnt]+":"+wideBuffer[cachecnt]+ "          -- " +Double.longBitsToDouble((((long)InputCacheData[cachecnt])<<32)+wideBuffer[cachecnt]));
									}
								}
								
//								System.out.println(InputCacheData[cachecnt]);
							}
//							System.err.println(cacheTicks[cachecnt]);
							if(cacheTicks[cachecnt] == 0) InputCacheValid[cachecnt] = true;
							else InputCacheValid[cachecnt] = false;
						} else {
							cacheUsage += (cachecnt+"\t\t");
						}
						
					}
				}
				//========== /CACHE DEV ==========
				PEs[i].setInputCacheData(InputCacheData[cachecnt]);
				cachecnt++;

			}
			PEs[i].combinatorialLateArrival();
		}
		if(usedCache && (musage + writeMusage + specialMusage ) >1) {
//			System.err.println("MOESIUSAGE: " + (musage+writeMusage+specialMusage) + " (writes: " + writeMusage + ", speical: "+ specialMusage+") ");
			moesiConflictcnt++;
//			System.err.println("            "  + moesiConflictcnt);
		}
		controlunit.setInputCbox(cboxWrapper.getBranchSelectionSignal());
		controlunit.operateLateArrival(); // (Update PC)
	}
	
	
	static int moesiConflictcnt = 0;
	
	LinkedHashMap<Integer, LinkedHashSet<Integer>> dmawritesHandleTOPEmap = new LinkedHashMap(); 
	

	/*
	 *  Information methods for scheduling
	 */

//	public int getCBoxConditionMemorySlots(){
//		return cbox.getCBoxModel().getMemorySlots();
//	}

	
	public int getMemorySizeOfControlUnit(){
		return stateSizeControlUnit;
	}

	
	public int getMemorySizehOfContext(){
		return contextsize;
	}

	
//	public Cbox getPBox() {
//		return cbox;
//	}

	
	public CGRAState getState() {
		return state;
	}

	
	public void setState(CGRAState state) {
		this.state = state;
	}

	
	public int[] getLiveOuts(){
		return PEsliveOut;
	}

	double cacheWaitTime = 0;
	double nrOfCacheAccesses = 0;
	
	public boolean tick(){
//				System.out.println("Ruuuuuuuuuuuuuuuuuuuning "+ controlunit.getProgramCounter() + " " + currentState);
		
		boolean adsf = getState() == CGRAState.RUN;
		
		coherenceController.count(adsf, !BundledCacheEnable, controlunit.getProgramCounter());
		
		
		boolean isReady = (currentState == State.IDLE) && !tokenValid; 
		State nextState = currentState;
		if(currentState == State.IDLE){
			if(tokenValid && validInputs(opcode)){
				nextState = State.BUSY;
				trackerOpcode.add(opcode);
				trackerOperandAddr.add(input[OPERAND_A_LOW]);
				trackerOperandData.add(input[OPERAND_B_LOW]);
//				System.out.println(opcode + " " + input[OPERAND_A_LOW] + "  " + input[OPERAND_B_LOW] + "-> " + mask.getBitString(input[OPERAND_A_LOW]));
				//				count = getDuration(opcode);
				if(executeTrace.active()){
					executeTrace.println(this.toString()+ " starting "+ opcode + " ("+tag+")"); //TODO
				}
			} else if(!tokenValid){
				tokenAdapter.nextToken();
			} else if(tokenValid) {
//				System.out.println("WAIT : " + stateCNT.get("WAIT") + " OP: "  + opcode);
				stateCNT.put("WAIT", stateCNT.get("WAIT") + 1);
			}
		} else if(currentState == State.BUSY){
			//			count--;
			//			if(count <= 0){
			if(executeOp(opcode)){
				if(executeTrace.active()){
					executeTrace.println(this.toString()+ " executed "+ opcode); //TODO
					executeTrace.println("\toutput low: "+ output[RESULT_LOW]);
				}
				if(getResultAck()){
					nextState = State.IDLE;
					setResultAck(false);
				}
				else{
					nextState = State.SENDING;
				}

				for(int i = 0; i < inputValid.length; i++){
					inputValid[i] = false;
				}
				tokenAdapter.nextToken();
			}
			//			}
		} else if(currentState == State.SENDING){
			if(getResultAck()){
				nextState = State.IDLE;
				//				for(int i = 0; i < inputValid.length; i++){
				//					inputValid[i] = false;
				//				}
				for(int i = 0; i < outputValid.length; i++){
					outputValid[i] = false;
				}
				//				tokenAdapter.nextToken();
				setResultAck(false);
			}

		}
		currentState = nextState;
		return isReady;
	}

	
		public double getAdditionalEnergy() {
		double energy = 0;
		
		for(PE pe: PEs){
			energy += pe.getDynamicEnergy();
		}

		return energy;
	}
	
		
	public double getStaticEnergy() {
		double energy = super.getStaticEnergy();
		
		for(PE pe: PEs){
			energy += pe.getStaticEnergy();
		}
		
		energy += contextspes[0].getMemorySize()/5000;
		energy *= 0.7;
		return energy;
	}

	
	@Override
	public boolean validInputs(CgraOpcodes op) {
		switch (op) {
		case RECEIVELOCALVAR:
			return inputValid[OPERAND_B_LOW];
		case SENDLOCALVAR:
			return true;
		case INIT:
			return inputValid[OPERAND_A_LOW];
		case RUN:
			return true;
		default:
			break;
		}
		return false;
	}
	
	public String getStateCNT(){
		int run = stateCNT.get("RUN");
		
		int overhead = stateCNT.get("SEND") + stateCNT.get("REC") +stateCNT.get("WAIT"); 
		
		int percent = 0;
		if(run != 0)
			percent = 100*overhead / run;
		
		
		return "Overhead in percent ----------------- "+ percent +" " + stateCNT.toString(); 
	}
	
	public double getTransmissionOverHeadInPercent(){
		double overhead = stateCNT.get("SEND") + stateCNT.get("REC") +stateCNT.get("WAIT"); 
		
		overhead = overhead/(overhead + stateCNT.get("RUN"));
		return overhead;
	}
	
	public LinkedHashMap<String,Integer> getStateCount(){
		return stateCNT;
	}


	public int[] getConstMemory() {
		return constMemory;
	}


	public LocationInformation[] getLocationInformationTable() {
		return locationInformationTable;
	}


	public KernelTableEntryCGRA[] getKernelTableCGRA() {
		return kernelTableCGRA;
	}
	
	public double getMemoryAccessTime(){
		return cacheWaitTime;
	}
	
	public double getMemoryAccesses(){
		return nrOfCacheAccesses;
	}
	
}
