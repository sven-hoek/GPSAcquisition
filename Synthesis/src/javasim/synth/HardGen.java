package javasim.synth;

import graph.CDFG;
import graph.LG;
import graph.Loop;
import graph.Node;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeMap;

import com.cedarsoftware.util.io.JsonWriter;
import com.sun.org.apache.bcel.internal.generic.IINC;

import cgramodel.CgraModel;
import cgramodel.LocationInformation;
import target.Amidar;
import dataContainer.ByteCode;
import dataContainer.Invokation;
import dataContainer.MethodDescriptor;
import dataContainer.SynthesizedKernelDescriptor;
import javasim.synth.model.CGRAIntrinsics;
import javasim.synth.model.DataGraph;
import javasim.synth.model.I;
import javasim.synth.model.LoopGraph;
import javasim.synth.model.VStack;
import javasim.synth.model.datum.ConstDatum;
import javasim.synth.model.datum.Datum;
import javasim.synth.model.datum.MergerDatum;
import javasim.synth.model.datum.SWriteDatum;
import javasim.synth.model.instruction.AbstractIF;
import javasim.synth.model.instruction.Instruction;
import javasim.synth.model.instruction.StartInstr;
import javasim.synth.model.instruction.StopInstr;
import scheduler.LPW;
import scheduler.MissingOperationException;
import scheduler.NotEnoughHardwareException;
import scheduler.NotSchedulableException;
import scheduler.RCListSched;
import scheduler.RCListSched.AliasingSpeculation;
import scheduler.Schedule;

/**
 * A new implementation of the synthesis algorithm.
 * 
 * @author Michael Raitza  + Lukas Jung
 * @version – 08.09.2016
 */
public class HardGen {

	/**
	 * The code of the method which contains the kernel to be synthesized
	 */
	private Short[] code;
	
	/**
	 * Stores the offset of the local variable index. Needed for inlined codesequences: In those sequences for example the 
	 * local variable index 0 points to another local variable than in another part of the code
	 */
	private Short[] lVarOffset;

	
	private boolean[] isPrefetch;
	private boolean[] isPrefetchFill;
	
	/**
	 * The start index of the kernel to be synthesized (relative to the method code)
	 */
	private Integer start;
	
	/**
	 * The stop index of the kernel to be synthesized (relative to the method code)
	 */
	private Integer stop;
	
	/**
	 * The address in the method to which the TokenMachine should jump after the kernel was executed on the CGRA
	 * This information is patched into the bytecode
	 */
	private Integer backJumpAddress;
	
	/**
	 * The offset of the method code in the code section of the AXT-file
	 */
	private int codeOffset;
	
	/**
	 * The complete code section of the AXT-file. Needed for Inlining.
	 */
	private byte[] allCode;

	/**
	 * The Instruction graph of the kernel
	 */
	private InstrGraph igraph;
	
	/**
	 * The scheduler used to schedule the kernel
	 */
	scheduler.RCListSched listSched;
	
	/**
	 * Stores information about all previously synthesized kernels
	 */
	private SynthesizedKernelDescriptor[] kernelTable;
	
	/**
	 * Stores information about all methods needed for inlining
	 */
	private MethodDescriptor[] methodTable;

	/**
	 * Holds information about the current synthesis process (e.g. information about local variable accesses)
	 */
	private SynthData synthData;
	
	/**
	 * A model of the CGRA on which the kernel will be executed
	 */
	private CgraModel model;
	
	/**
	 * Whether constant folding should be executed
	 */
	private boolean CONSTANT_FOLDING;
	
	/**
	 * Whether common subexpression elimination should be executed
	 */
	public boolean CSE;
	
	/**
	 * Whether methods should be inlined
	 */
	public boolean INLINE;
	
	/**
	 * The maximal length of a method that is allowed to be inlined
	 */
	public int MAX_INLINE_LENGTH = 10000;
	
	/**
	 * The maximal bytecode length the unrolled inner loop may have after inlining
	 */
	public int MAX_UNROLL_LENGTH;
	
	/**
	 * Decides how to handle the Aliasing problem
	 */
	public AliasingSpeculation ALIASING_SPECULATION;
	
	/**
	 * The name of the method containing the kernel
	 */
	private String methodName;
	
	/**
	 * A FIFO storing the previous invocations of object methods. For those invocations the method
	 * that is actually called is dependent on the object itself. For speculative method inlining
	 * we assume that the same method will be used again the next time this instruction is called.
	 * Thus we record the AMTI for each invocation.
	 */
	private ArrayList<Invokation> invokationHistory;
	
	/**
	 * Stores the number of local variables of the method containing the kernel.
	 * This information is needed by the scheduler for the tokenset generation:
	 * Only the local variables of this method have to be transferred form the CGRA 
	 * back to AMIDAR. Local variables of inlined methods have a index higher than 
	 * nrOfLocalVariables (see lVarOffset) and need not to be transferred.
	 */
	private int nrOfLocalVariables;
	
	/**
	 * This variable points to the highest local variable index + 1.
	 * If a method is inlined, the lVarOffset of this method is set to localVarPointer.
	 * Afterward the localVarPointer is increased
	 */
	private short localVarPointer; // needed for method inlining - lv of methods need new index
	
	/**
	 * Maps the lVarOffset to the corresponding method
	 */
	private LinkedHashMap<Short, MethodID> inlinedMethod; // identify the inlined method with   - maps localVar offset to MethodID
	
	/**
	 * Graph that represents all dependencies in a clean way
	 */
	CDFG cdfg;
	
	/**
	 * The loop Graph
	 */
	LG loopGraph;
	
	/**
	 * Holds all PE contexts 
	 */
	long [][] contextsPE;
	
	/**
	 * Holds all C-Box contexts
	 */
	long[][] contextsCBox;
	
	/**
	 * Holds all Control Unit contexts
	 */
	long[] contextsControlUnit;
	
	/**
	 * HOlds all contexts of the handle compare unit
	 */
	long[] contextsHandleCompare;
	
	/**
	 * Holds all information for the token generation
	 * It stores value information (local variable ID, contant, contant pool pointer)
	 * plus the location information on the CGRA (PE + Regfile address)
	 */
	ArrayList<Integer> constantMemory;
	ArrayList<Integer> constantMemoryIndirect;
	ArrayList<LocationInformation> locationInformation;
	ArrayList<Integer> localVariables;
	
	/**
	 * Number of local variables that have to be sent back from CGRA to AMIDAR
	 */
	int nrLocalVarSend;
	
	/**
	 * Number of constants that have to be loaded from the contant pool and sent to the CGRA
	 */
	int nrIndirectConst;
	
	/**
	 * Number of constants that have to be sent to the CGRA directly
	 */
	int nrDirectConst;
	
	/**
	 * Number of local variables that the CGRA receives
	 */
	int nrLocalVarReceive;
	
	
	CGRAIntrinsics intrinsics;
	
	boolean unrollBasedPrefetch = false;

	
	/**
	 * A unique Identifier of a Method
	 * @author jung
	 *
	 */
	class MethodID{
		int amti;
		int expectedCi = -1;	// for invoke virtual
		int byteCodeAddrOffset; // address offset after inlining
		int codeTableIndex;
		
		MethodID(int amti, int offset, int codeTableIndex){
			this.amti = amti;
			this.byteCodeAddrOffset = offset;
			this.codeTableIndex = codeTableIndex;
		}
		
		public String toString(){
			return "MID: "+amti+" " +byteCodeAddrOffset;
		}
	}
	
	/**
	 * Returns the instruction graph of this kernel
	 * @return the instruction graph
	 */
	public InstrGraph getigraph() {
		return igraph;
	}


	/**
	 *  Initialises the object holding synthesis context 
	 */
	private SynthData init_synthesis(Integer start, Integer stop) {
		return new SynthData(code, lVarOffset, (StartInstr) I.SYNTH_START.create(start),(StopInstr) I.SYNTH_STOP.create(stop), localVarPointer, inlinedMethod, methodTable, CSE, ALIASING_SPECULATION, CONSTANT_FOLDING, intrinsics);
	}

	/**
	 *  Creates the control Instruction graph and the loop hierarchy graph 
	 */
	private InstrGraph create_graph(SynthData syn) {
		Instruction next_i = syn.start();
		while (next_i != null) {
			next_i.insert(syn);
			next_i = syn.update(); 
		}
		syn.ig().restructure_graph(syn);

		return syn.ig();
	}

	/**
	 *  Creates the data flow graph and scheduling dependence graph
	 */
	private DataGraph data_graph(SynthData syn, LoopGraph.Loop lp) {
		syn.dg_init();
		Instruction next_i = syn.ig().get(lp.start_addr);
		next_i.vstack(new VStack());
		Instruction stop_instr = lp.ifinstr.phi_node();
		boolean run = false;
		syn.pos(next_i.addr());

		while (next_i != null) {
			next_i.addr();
			next_i.eval(syn);
			if (next_i == stop_instr)
				if (run)
					break;
				else
					run = true;
			next_i = syn.update();
		}
		syn.dg().foldLVMemInstructions(syn);
		syn.dg().cleanup();
		syn.dg().reorderChains();
		return syn.dg();
	}


	/**
	 * Constructs a new hardware generator.
	 * 
	 * @param c
	 *            the simulator core this hardware should be generate for
	 * @param methodTable
	 *            the method where the sequence should be synthesised from
	 * @param classIndex
	 *            the class index of this method
	 */
	public HardGen(MethodDescriptor[] methodTable, SynthesizedKernelDescriptor[] kernelTable, ArrayList<Invokation> invokationHistory, Integer methodIndex, int start, int stop, boolean cse, boolean inline, int maxUnrollLength, int unroll, byte[] code, CgraModel model, AliasingSpeculation aliasing, boolean constantFolding, boolean unrollBasedPrefetch) {
		this.kernelTable = kernelTable;
		this.invokationHistory = invokationHistory;
		
		this.methodName = methodTable[methodIndex].getMethodName();
		
		this.localVarPointer = (short)methodTable[methodIndex].getMaxLocals();
		this.nrOfLocalVariables = this.localVarPointer;
		
		this.methodTable = methodTable;
		
		this.allCode = code;
		this.model = model;
		
		this.code = new Short[methodTable[methodIndex].getCodeLength()];
		codeOffset = methodTable[methodIndex].getCodeRef();
		backJumpAddress = stop -codeOffset;
		for (int i = 0; i < this.code.length; i++) {
			
			short aaf  = ( (short) (0xFF&code[codeOffset + i]));
			
			this.code[i] = aaf; //TODO 
		}
		
		
		intrinsics = new CGRAIntrinsics();
		intrinsics.registerFunctions(methodTable);
		
		
		this.start = start-codeOffset;
		this.stop = stop -codeOffset;
		
		this.unrollBasedPrefetch = unrollBasedPrefetch;
		
		CONSTANT_FOLDING = constantFolding;
		CSE = cse;
		MAX_UNROLL_LENGTH = maxUnrollLength;
		INLINE = inline;
		ALIASING_SPECULATION = aliasing;
		
		codePreparation();
		
		
		
		
		lVarOffset = new Short[this.code.length];
		isPrefetch = new boolean[this.code.length];
		isPrefetchFill = new boolean[this.code.length];
		for(int i = 0; i< lVarOffset.length; i++){
			lVarOffset[i] = 0;
			isPrefetch[i] = false;
			isPrefetchFill[i] = false;
		}
		
		
		
		inlinedMethod = new LinkedHashMap<Short,MethodID>();
		inlinedMethod.put((short)0, new MethodID(methodIndex, 0, codeOffset));
		
		
	
		inlineMethodCalls();
		
		// Unroll the bytecode 
			unrollInnerLoops(unroll);
		igraph = null;
		I.init();
	}
	
	
	/**
	 * Prepares the method code for the synthesis:
	 * 		- adapt start/stop in case the profiler did not properly record the loop borders (see While loop bug).
	 * 		- if there was e inner loop that was mapped to the CGRA, the bytecode patch has to be revoked 
	 */
	private void codePreparation(){
		// check if we covered the whole loop  (WHILE LOOP BUG case 1)
		// The first jump, that jumps over the current stop, is assumed to be the loop exit point.
		// Thus the target address - 3 is the end of the loop. ( minus 3 bc the last instruction
		// in a loop is always a GOTO with two parameter bytes)
		int potentialEndOfLoop = 0;
		for(int i = start; i< stop; i++){
			if(((code[i]&0xFF) >= 0x99) && ((code[i]&0xFF) <= 0xA6 )){
				if(stop+3 <= ((code[i+1].byteValue()<< 8 | code[i+2].byteValue()&0xFF) +i )){
					potentialEndOfLoop = ((code[i+1].byteValue()<< 8 | code[i+2].byteValue()&0xFF) +i );
					break;
				}
			}
			
			i+=ByteCode.getParamCount(code[i].byteValue());
		}
		
		
		if(potentialEndOfLoop != 0){
			if(code[potentialEndOfLoop-3] == 0xA7){

				stop = potentialEndOfLoop-3;
				backJumpAddress = stop;
			}
		}
		
		// insert goto if necessary (WHILE LOOP BUG case 2)
		
		TreeMap<Integer, Integer> forwardJumps = new TreeMap<Integer, Integer>(); // Storing all forward jump - needed in order to adjust the jump targets if they jump over  a inserted goto
		//  <Target addr, jumpLength>
		TreeMap<Integer, Integer> backwardJumps = new TreeMap<Integer, Integer>();
		
		
		for(int i = start; i <= stop; i++){
			
			
			// first Check if it's already synthesized and undo the code patching
			if((code[i]&0xFF) == (0xFD&0xFF)){	/// SYNTH
				int kernelID = code[i+1];

				SynthesizedKernelDescriptor kernel = kernelTable[kernelID];

				byte[] oldcode = kernel.getReplacedBytes();
				for(int j = 0;j<oldcode.length;j++){
					code[i+j] = (short)(0x00ff & oldcode[j]);
				}
			}
			
			if(code[i] >= 0xAC && code[i] <= 0xB1){//RETURN
				throw new SequenceNotSynthesizeableException("Unable to synthesize given sequence because it contains a return statement");
			}
			
			
			int functionID = new Short((short) (code[i + 2] | code[i+1] << 8)).intValue(); 
			if(!INLINE && (((code[i]&0xFF) == 0xd6)|| ((code[i]&0xFF) == 0xd8)||((code[i]&0xFF) == 0xd7 && !intrinsics.isKnown(functionID)))){
				throw new SequenceNotSynthesizeableException("Unable to synthesize given sequence: Method Inlining is switched off ");
			} else if (!INLINE && ((code[i]&0xFF) == 0xd7 && intrinsics.isKnown(functionID)) && !model.supportsOperation(mapInstruction(intrinsics.getInstruction(functionID)))){
				throw new SequenceNotSynthesizeableException("Unable to synthesize given sequence: Method Inlining is switched off and CGRA does not support the intrinsic function " + mapInstruction(intrinsics.getInstruction(functionID)));
			}

			if(code[i] >= 0x99 && code[i] <= 0xA7){ 				//track all jumps
				int gotoVal = (short)((short)((code[i+1]& 0xFF)<<8) | (short)(code[i+2]& 0xFF));
				if (gotoVal < 0){
					backwardJumps.put(i, gotoVal);
				} else {
					forwardJumps.put(i, gotoVal);
				}
			}else if (code[i] == 0xFB || code[i] == 0xDB){
				throw new SequenceNotSynthesizeableException(code[i], i);
			}
			i+=ByteCode.getParamCount(code[i].byteValue());
		}
		
		if(forwardJumps.size() == 0){
			throw new SequenceNotSynthesizeableException("Shitty while loop that i cant handle... (case 2) TODO");
		}
		
		while(backwardJumps.size() != 0){
			Integer i = backwardJumps.firstKey();
			Integer gotoVal = backwardJumps.remove(i);
			int head = i + gotoVal;
			int lastIncrease = 0;

			if((code[i]&0xFF) != 0xA7){ // GOTO

				int target = i + (short)((short)((code[i+1]& 0xFF)<<8) | (short)(code[i+2]& 0xFF));
				
				Integer correspondingForwardJump = null;
				Integer correspondingEnd = null;
				
				// find a forward jump that starts whithin this loop (defined by the current backward jump)
				// and jumps out of it
				for(Integer fwdJmp: forwardJumps.keySet()){
					if( fwdJmp > target && fwdJmp < i && (forwardJumps.get(fwdJmp)+fwdJmp)> i){
						correspondingForwardJump = fwdJmp;
						correspondingEnd = forwardJumps.get(fwdJmp) + fwdJmp;
						break;
					}
					
					
				}

				if(((short)((code[correspondingEnd-2]& 0xFF)<<8) | (short)(code[correspondingEnd-1]& 0xFF))+correspondingEnd-3 != target  ){ //no goto in the end (case 2)
					// There is no goto in the end thus we create one and let the backward jump first jump to this one and than back to  the beginning
					// this is necessary so htat the instuction graph can be generated properly
					
					
					/// Redirect backward jump
					int newTarget = correspondingEnd - i;
					
					code[i+1] = (short)(((newTarget) & 0xff00)>>8);
					code[i+2] = (short)((newTarget) & 0xff);

					Short [] newCode = new Short[code.length+3];
									
					// insert GOTO (backward jump)
					lastIncrease = 3; 
					stop+=3;
					
					for(int j = 0; j< newCode.length; j++){
						if(j<correspondingEnd){
							newCode[j] = code[j];
						} else if(j == correspondingEnd){
							newCode[j] = 0xA7; // GOTO
							int newBackjump = target - correspondingEnd;
							newCode[j+1] = (short)(((newBackjump) & 0xff00)>>8);
							newCode[j+2] = (short)((newBackjump) & 0xff);
							j+=2;
						} else {
							newCode[j] = code[j-3];
						}
					}
					
					
					code = newCode;
					
					// Update all jump values of jumps that jump over the address where we inserted the GOTO
					TreeMap<Integer,Integer> newforwardJumps = new TreeMap<Integer, Integer>();
					for(Integer jump: forwardJumps.keySet()){
						Integer jumpTarget = forwardJumps.get(jump);
						if((jumpTarget+jump>=correspondingEnd-3) && (jump < correspondingEnd-3)){ //this jump jumps over the inserted GOTO
							jumpTarget = jumpTarget+lastIncrease;
							code[jump+1] = (short)(((jumpTarget) & 0xff00)>>8);
							code[jump+2] = (short)((jumpTarget) & 0xff);
							newforwardJumps.put(jump, jumpTarget);
						} else if(jump > correspondingEnd-3){
							newforwardJumps.put(jump+lastIncrease, jumpTarget);
						} else {
							newforwardJumps.put(jump, jumpTarget);
						}
					}
					forwardJumps = newforwardJumps;
					TreeMap<Integer,Integer> newBackwardJumps = new TreeMap<Integer, Integer>();
					for(Integer backJump: backwardJumps.keySet()){
						if(backJump<=correspondingEnd-3){
							newBackwardJumps.put(backJump, backwardJumps.get(backJump));
							continue;
						}
						Integer jumpTarget = backwardJumps.get(backJump);
						if((backJump + jumpTarget < correspondingEnd-3)){	// this jump jumps over the inner loop
							jumpTarget = jumpTarget - lastIncrease;
							code[backJump+lastIncrease+1] = (short)(((jumpTarget) & 0xff00)>>8);
							code[backJump+lastIncrease+2] = (short)((jumpTarget) & 0xff);
						}
						newBackwardJumps.put(backJump+lastIncrease, jumpTarget);
					}
					backwardJumps = newBackwardJumps;
				}


			}else if((code[i+3]&0xFF) == 0xA7){
				
				
				Short [] newCode = new Short[code.length+1];
				
				// insert GOTO (backward jump)
				lastIncrease = 1; 
				stop+=1;
				
				for(int j = 0; j< newCode.length; j++){
					if(j<i+3){
						newCode[j] = code[j];
					} else if(j == i+3){
						newCode[j] = 0x00; // NOP
					} else {
						newCode[j] = code[j-1];
					}
				}
				
				
				code = newCode;
				
				// Update all jump values of jumps that jump over the address where we inserted the GOTO
				TreeMap<Integer,Integer> newforwardJumps = new TreeMap<Integer, Integer>();
				for(Integer jump: forwardJumps.keySet()){
					Integer jumpTarget = forwardJumps.get(jump);
					if((jumpTarget+jump>i+3) && (jump < i+3)){ //this jump jumps over the inserted GOTO
						jumpTarget = jumpTarget+lastIncrease;
						code[jump+1] = (short)(((jumpTarget) & 0xff00)>>8);
						code[jump+2] = (short)((jumpTarget) & 0xff);
						newforwardJumps.put(jump, jumpTarget);
					} else if(jump > i+3){
						newforwardJumps.put(jump+lastIncrease, jumpTarget);
					} else {
						newforwardJumps.put(jump, jumpTarget);
					}
				}
				forwardJumps = newforwardJumps;
				TreeMap<Integer,Integer> newBackwardJumps = new TreeMap<Integer, Integer>();
				for(Integer backJump: backwardJumps.keySet()){
					if(backJump<i+3){
						newBackwardJumps.put(backJump, backwardJumps.get(backJump));
						continue;
					}
					Integer jumpTarget = backwardJumps.get(backJump);
					if((backJump + jumpTarget < i+3)){	// this jump jumps over the inner loop
						jumpTarget = jumpTarget - lastIncrease;
						code[backJump+lastIncrease+1] = (short)(((jumpTarget) & 0xff00)>>8);
						code[backJump+lastIncrease+2] = (short)((jumpTarget) & 0xff);
					}
					newBackwardJumps.put(backJump+lastIncrease, jumpTarget);
				}
				backwardJumps = newBackwardJumps;
			}

		}

	}

	/**
	 * Inlines  methods if Possible
	 * @return
	 */
	private boolean inlineMethodCalls(){

		boolean changed = false;
		TreeMap<Integer, Integer> forwardJumps = new TreeMap<Integer, Integer>(); // Storing all forward jump - needed in order to adjust the jump targets if they jump over the inlined method
		//  <Target addr, jumpLength>
		TreeMap<Integer, Integer> backwardJumps = new TreeMap<Integer, Integer>();

		for(int i = start; i <= stop; i++){

			if(code[i] >= 0x99 && code[i] <= 0xA7){ 				//track all jumps
				int gotoVal = (short)((short)((code[i+1]& 0xFF)<<8) | (short)(code[i+2]& 0xFF));
				if (gotoVal < 0){
					backwardJumps.put(i, gotoVal);
				} else {
					forwardJumps.put(i, gotoVal);
				}
			}else if (code[i] == 0xFB|| code[i] == 0xDE){
				throw new SequenceNotSynthesizeableException(code[i], i);
			}
			i+=ByteCode.getParamCount(code[i].byteValue());
		}
		

		
		

		for(int i = start; i<=stop; i++){
			
			
			int functionID = new Short((short) (code[i + 2] | code[i+1] << 8)).intValue();
			boolean inlineIntrinsic = false;
			Short ccode = code[i];
			if(!intrinsics.isKnown(functionID) || intrinsics.isKnown(functionID) && !model.supportsOperation(mapInstruction(intrinsics.getInstruction(functionID)))){
				inlineIntrinsic = true;
				intrinsics.deleteIntrinsic(functionID);
			}
			
			
			// look for invokes
			if( ((code[i]&0xFF) == 0xda)|| ((code[i]&0xFF) == 0xd6)|| ((code[i]&0xFF) == 0xd8)|| ((code[i]&0xFF) == 0xd7 && inlineIntrinsic)){ // INVOKESTATIC_QUICK - but not
				changed = true;
				
				// get The method from which the new method is called
				MethodID mid = inlinedMethod.get(lVarOffset[i]);
				
				
				//////// Determine AMTI //////////////////////////////////////////////////////////////
				int methodAMTI;
				MethodID newMid;
				short oldLVarPointer;
				
				// we don't know the AMTI from  the bytecode so we look in the invokation history which AMTI was used before
				if((code[i]&0xFF) == 0xd6 || (code[i]&0xFF) == 0xDA ){ // invokevirtual + invokeinterface
					
					
					int addr = i+ mid.codeTableIndex - mid.byteCodeAddrOffset; // absolute address of the invokation in the code section of AXT
					int cti = -1;
					methodAMTI = -1;
					
					for(int j = invokationHistory.size()-1; j >= 0; j--){
						Invokation inv = invokationHistory.get(j);
						if(inv.address == addr){ 	//This method was called before and we assume the class will be the same and thus the same AMTI will be used again
							methodAMTI = inv.amti;
							cti = inv.cti;
							break;
						}
					}
					
					if(methodAMTI == -1){
						throw new SequenceNotSynthesizeableException("Found no Method for invokevirtual at " + addr);
					}
					
					// Patch the asumed CTI into the bytecode so that during instruction graph generation the assumed CTI is known
					// as this assumption has to be checked during execution (-> speculative method inlining)
					code[i+1] = (short)((code[i+1]&0xFC) + (cti>>8));
					code[i+2] = (short)(cti & 0xFF);
					if((code[i]&0xFF) == 0xd6){
						newMid = new MethodID(methodAMTI,i+3, methodTable[methodAMTI].getCodeRef());
					} else {
						newMid = new MethodID(methodAMTI,i+5, methodTable[methodAMTI].getCodeRef());
					}
				}else{ // for Private methods or static methods the AMTI is coded in the bytecode (non speculative inlining)
					methodAMTI = (code[i+1]<<8) | (0xFF&code[i+2]);
					newMid = new MethodID(methodAMTI,i+3, methodTable[methodAMTI].getCodeRef());
				}
				
				/////////////// Inline the Method ///////////////////////////////////////////
				oldLVarPointer = localVarPointer;
				int lastIncrease = inlineMethod(methodAMTI, code, i); // inlines 
				
				/////////////// Update offsets etc //////////////////////////////////////////
				for(MethodID midd: inlinedMethod.values()){
					midd.byteCodeAddrOffset=midd.byteCodeAddrOffset+lastIncrease;
					// all following invocations have another address in the bc the method was inlined. This has to be recorded
					// in order to calculate the absolute addres of an invoke in the code SEction of AXT. This is necessary to find 
					// the invoke in the invoke history
				}
				
				inlinedMethod.put(oldLVarPointer, newMid); // Add the inlined method
				
				// Update all jumps that jump over the inlined method				
				TreeMap<Integer,Integer> newforwardJumps = new TreeMap<Integer, Integer>();
				for(Integer jump: forwardJumps.keySet()){
					Integer jumpTarget = forwardJumps.get(jump);
					if((jumpTarget+jump>i) && (jump < i)){ //this jump jumps over invokation
						jumpTarget = jumpTarget+lastIncrease;
						if(jumpTarget > 32768){
							throw new SequenceNotSynthesizeableException("The code sequence is getting too big - no more methods can be inlined");
						}
						code[jump+1] = (short)(((jumpTarget) & 0xff00)>>8);
						code[jump+2] = (short)((jumpTarget) & 0xff);
						newforwardJumps.put(jump, jumpTarget);
					} else if(jump > i){
						newforwardJumps.put(jump+lastIncrease, jumpTarget);
					}
				}
				forwardJumps = newforwardJumps;
				TreeMap<Integer,Integer> newBackwardJumps = new TreeMap<Integer, Integer>();
				for(Integer backJump: backwardJumps.keySet()){
					Integer jumpTarget = backwardJumps.get(backJump);
					if((backJump > i) && (backJump + jumpTarget < i )){	// this jump jumps invokation
						jumpTarget = jumpTarget - lastIncrease;
						code[backJump+lastIncrease+1] = (short)(((jumpTarget) & 0xff00)>>8);
						code[backJump+lastIncrease+2] = (short)((jumpTarget) & 0xff);
						newBackwardJumps.put(backJump+lastIncrease, jumpTarget);
					} else if(backJump > i) {
						newBackwardJumps.put(backJump+lastIncrease,jumpTarget);
					}
					
				}
				backwardJumps = newBackwardJumps;
				
				//////////////////////// "prepare" the inlined code:///////////////////////////////////////////
				//  - record jumps
				//  - check for recoursion
				//  (undo of the previously patched bytecode is done in the inlineMethod(..) function)
				for(int j = i + ByteCode.getParamCount(code[i].byteValue()) + 1; j <= i+ByteCode.getParamCount(code[i].byteValue()) + lastIncrease; j++){

					if(code[j] >= 0x99 && code[j] <= 0xA7){ 				//track all new jumps
						int gotoVal = (short)((short)((code[j+1]& 0xFF)<<8) | (short)(code[j+2]& 0xFF));
						if (gotoVal < 0){
							backwardJumps.put(j, gotoVal);
						} else {
							forwardJumps.put(j, gotoVal);
						}
					}
					if((code[i]&0xFF) == (code[j]&0xFF) && (code[i+1]&0xFF) == (code[j+1]&0xFF) && (code[i+2]&0xFF) == (code[j+2]&0xFF)){ /// Recoursion
						// TODO might be broken for invokevirtual bc cti is patched into the parameters
						throw new SequenceNotSynthesizeableException("Unable to synthesize given sequence: Inlining of recoursive functions not possible");
					}
					
					
					j+=ByteCode.getParamCount(code[j].byteValue());
				}

			}
			i+=ByteCode.getParamCount(code[i].byteValue());
		}
		
		return changed;
	}


	/**
	 * Inlines the method denoted by AMTI into the code at the given position
	 * @param amti
	 * @param code
	 * @param position
	 * @return
	 */
	private int inlineMethod(int amti, Short [] code, int position){
		int methodCodeLength = methodTable[amti].getCodeLength();
		// TODO will we do this?
		if(methodCodeLength > MAX_INLINE_LENGTH)
			throw new SequenceNotSynthesizeableException("Unable to synthesize given sequence: Method to be inlined is too long. Length is "+methodCodeLength+". Supported Length is "+MAX_INLINE_LENGTH);
		
		// code of the inlined Method
		byte [] methodCode = new byte[methodCodeLength];
		
				
		System.arraycopy(allCode, methodTable[amti].getCodeRef(), methodCode, 0, methodCode.length);
		
//		System.out.println("INLINING " + methodTable[amti].getMethodName());
		/////////////////// Undo previous bytecode patches ///////////////////////////////////////
		for(int i = 0; i< methodCode.length; i++){ //if inlined code was Synthesized before, redo patch
//			System.out.println(methodCode[i] + " " + (methodCode[i]&0xff) + " " + ByteCode.mnemonic(new byte[] {methodCode[i],0,0,0,0}));
			if((methodCode[i]&0xFF) == (0xFD&0xFF)){	
//				System.out.println("-------------- > oi");
				int kernelID = methodCode[i+1];
				
				SynthesizedKernelDescriptor kernel = kernelTable[kernelID];

				byte[] oldcode = kernel.getReplacedBytes();
				for(int j = 0;j<oldcode.length;j++){
					methodCode[i+j] = (byte)(0x00ff & oldcode[j]);
				}
			}else if ((methodCode[i]&0xFF) == 0xFB || (methodCode[i]&0xFF) == 0xDD){
				throw new SequenceNotSynthesizeableException(methodCode[i],i);
			}
			
			i+=ByteCode.getParamCount(methodCode[i]);
			
		}
		
		
		
		//offset from invoking bytecode to the position where the method code should be pasted 
		int offset = ByteCode.getParamCount(code[position].byteValue());

		int newLength = code.length+methodCodeLength;

		Short [] newCode = new Short[newLength];
		Short [] newLVarOffset = new Short[newLength];
		boolean [] newIsPrefetch = new boolean[newLength];
		boolean [] newIsPrefetchFill = new boolean[newLength];

		////////////// INLINE ////////////////////////////////
		for(int i = 0; i < newLength; i++){
			if(i <= position+offset){
				
				newCode[i]=code[i];
				newLVarOffset[i] = lVarOffset[i];
			} else if (i <= position+offset + methodCodeLength ){
				newCode[i] = new Short((short)(0xFF&methodCode[i-position-offset-1]));
				newLVarOffset[i] = this.localVarPointer;
			} else {
				newCode[i] = code[i-methodCode.length];
				newLVarOffset[i] = lVarOffset[i-methodCode.length];
			}
		}


		this.code = newCode;
		this.lVarOffset = newLVarOffset;
		this.isPrefetch = newIsPrefetch;
		this.isPrefetchFill = newIsPrefetchFill;
		stop = stop + methodCodeLength;
		localVarPointer += (short) methodTable[amti].getMaxLocals();

		return methodCodeLength;
	}
	
	int bytecodecnt = 0;
	int dmaaccnt = 0; 

	/**
	 * This method iterates over the loopbody code and finds inner Loops only the inner Loops are unrolled
	 * @param unroll the number of iterations that will be unrolled
	 */
	private void unrollInnerLoops(int unroll){
		//		System.out.println(method.toString());
		int lastGoTo = -1;
		int lastIncrease = 0;
		TreeMap<Integer, Integer> forwardJumps = new TreeMap<Integer, Integer>(); // Storing all forward jump - needed in order to adjust the jump targets if they jump over the unnrolled loop
		//  <Target addr, jumpLength>
		TreeMap<Integer, Integer> backwardJumps = new TreeMap<Integer, Integer>();
		LinkedHashMap<Integer, LinkedHashSet<Integer>> headsOfBackwardJumps = new LinkedHashMap<Integer, LinkedHashSet<Integer>>(); // inverse of backwardJumps


		for(int i = start; i <= stop; i++){
			bytecodecnt++;

			if((code[i]&0xFF) >= 0x99 && (code[i]&0xFF) <= 0xA7){ 				//track all jumps
				int gotoVal = (short)((short)((code[i+1]& 0xFF)<<8) | (short)(code[i+2]& 0xFF));
				if (gotoVal < 0){
					backwardJumps.put(i, gotoVal);
					if(headsOfBackwardJumps.containsKey(gotoVal+i)){ //while loop bug handling
						for(Integer orig : headsOfBackwardJumps.get(gotoVal+i)){
							int newJump = i-orig;
							code[orig+1] =(short)( (newJump >> 8)&0xFF);
							code[orig+2] =(short)( (newJump)&0xFF);
							backwardJumps.remove(orig);
							forwardJumps.put(orig, newJump); // transform multiple backward jumps in one backward (last) and several forward jumps to the one backward jump
						}
					} else {
						LinkedHashSet<Integer> jmps = new LinkedHashSet<Integer>();
						headsOfBackwardJumps.put(gotoVal+i, jmps);
					}
					
					headsOfBackwardJumps.get(gotoVal+i).add(i);
				} else {
					forwardJumps.put(i, gotoVal);
				}
			}
			if(((code[i]&0xFF) >= 0x2E && (code[i]&0xFF) <= 0x35) || ((code[i]&0xFF) >= 0x4f && (code[i]&0xFF) <= 0x56))
				dmaaccnt++;
			
			i+=ByteCode.getParamCount(code[i].byteValue());
		}

		
		//////////////////// Look for inner loops //////////////////////////
		while(backwardJumps.size() != 0){
			Integer i = backwardJumps.firstKey(); // Map is sorted
			Integer gotoVal = backwardJumps.remove(i);
			int head = i + gotoVal;
			lastIncrease = 0;

			if((code[i]&0xFF) == 0xA7){ // GOTO
				
				if( head > lastGoTo){ 	//found inner loop 

					int ll = i -head;
					int unrolln = MAX_UNROLL_LENGTH / ll;
					if (unrolln > unroll)
						unrolln = unroll;
					else if( unrolln < 1)
						unrolln = 1;
					
//					System.err.println("unrolln: "+unrolln);
					lastIncrease = unrollCode(unroll, head, i); 

					// update the jumps, that jump over the unrolled loop
					TreeMap<Integer,Integer> newforwardJumps = new TreeMap<Integer, Integer>();
					for(Integer jump: forwardJumps.keySet()){
						Integer jumpTarget = forwardJumps.get(jump);
						if((jumpTarget+jump>i) && (jump < head)){ //this jump jumps over the inner loop
							jumpTarget = jumpTarget+lastIncrease;
							code[jump+1] = (short)(((jumpTarget) & 0xff00)>>8);
							code[jump+2] = (short)((jumpTarget) & 0xff);
							newforwardJumps.put(jump, jumpTarget);
						} else if(jump > i){
							newforwardJumps.put(jump+lastIncrease, jumpTarget);
						}
					}
					forwardJumps = newforwardJumps;
					TreeMap<Integer,Integer> newBackwardJumps = new TreeMap<Integer, Integer>();
					for(Integer backJump: backwardJumps.keySet()){
						Integer jumpTarget = backwardJumps.get(backJump);
						if((backJump + jumpTarget < head)){	// this jump jumps over the inner loop
							jumpTarget = jumpTarget - lastIncrease;
							code[backJump+lastIncrease+1] = (short)(((jumpTarget) & 0xff00)>>8);
							code[backJump+lastIncrease+2] = (short)((jumpTarget) & 0xff);
						}
						newBackwardJumps.put(backJump+lastIncrease, jumpTarget);
					}
					backwardJumps = newBackwardJumps;

				} 
				lastGoTo = i+lastIncrease;

			}

		}


	}



	/**
	 * Unrolls the given loop by unroll times
	 * @param unroll the number of iterations that will be unrolled
	 */
	@SuppressWarnings("unused")
	private int unrollCode(int unrollRegular, int start, int stop){
		// Zu beachten:
		// - while loop bug - (das mit denn dummys als quick and dirty fix)
		// - weitere jump befehle zum finden von exitVal (short circuit evaluation)
		// - goto_w
		
		int localdmaccnt = 0;
		int bcCount = 0;
		
		///////////////// HEURISTIC TO LIMIT UNROLL in order to keep the synthesis time small /////////////////////////////////
		for(int i = start; i<= stop; i++){ 												// Parse Bytecode to find loop controlling if instructions
			if(((code[i]&0xFF) >= 0x2E && (code[i]&0xFF) <= 0x35) || ((code[i]&0xFF) >= 0x4f && (code[i]&0xFF) <= 0x56)){
				localdmaccnt++;
			}
			bcCount++;
			
			i += ByteCode.getParamCount(code[i].byteValue());
		}
		
		int MAX_DEPS = 125;
		
//		if(localdmaccnt!=0){
//			int newUnroll =  (MAX_DEPS - dmaaccnt)/localdmaccnt +1;
//			if(newUnroll< unroll){
//				if(newUnroll < 1)
//					unroll = 1;
//				else
//					unroll = newUnroll;
//			}
//			
//		}
		
		
		int unrollPrefetch = 0;
		int unrollFill = 0;
		
		if(unrollBasedPrefetch){
//		unrollPrefetch = unrollRegular;
				unrollPrefetch = 1;
				unrollFill = 0;
				if((localdmaccnt*100.0/((double)bcCount-7)) >= 40){
					unrollPrefetch = 0;
				} else if((localdmaccnt*100.0/((double)bcCount-7)) < 7){
					unrollFill = 4;
				}
			
//			unrollPrefetch= 1;
			
//			unrollFill = 5 - (int)(100*localdmaccnt/(double)bcCount/2);
//			if(unrollFill < 0){
//				unrollFill = 0;
//			}
//			System.out.println("************************************* " + (localdmaccnt*100.0/((double)bcCount-7)) + "\t" + bcCount);
//			System.out.println("urnrolffli: " + unrollFill);
		}
		int unroll = unrollPrefetch +unrollFill + unrollRegular;
		
		

//		System.err.println("Unrollnnn " + unroll);
		dmaaccnt+=(unroll-1)*localdmaccnt;
		
		
		//////////////// END of HEURISTIC /////////////////////////////////////////////////////////////////////////////////////////

		int synthLength = stop-start+3;	 		//+2 for the goto bytes
		int unrollLength = stop-start;
		Short[] newCode = new Short[code.length+(unroll-1)*(unrollLength+1)];
		Short[] newLVarOffset = new Short[newCode.length];
		boolean [] newIsPrefetch = new boolean[newCode.length];
		boolean [] newIsPrefetchFill = new boolean[newCode.length];
		short gotoVal = (short)((short)(code[stop+1]<<8) | (short)(code[stop+2]& 0xFF));
		short newGotoVal = (short)(gotoVal - (unroll-1)*(unrollLength+1));
		if(newGotoVal > 0){
			throw new SequenceNotSynthesizeableException("Too much unrolling...");
		}


		LinkedHashMap<Integer,Integer> exits = new LinkedHashMap<Integer,Integer>();

		short exitVal = -1;																// Jump value of the loop controlling if
		short newExitVal = -1;
		int exitIndex = -1;																// Index of the loop controlling if
		for(int i = start; i<= stop; i++){ 												// Parse Bytecode to find loop controlling if instructions
			if(code[i] >= 0x99 && code[i] <= 0xA6){	
				exitVal = (short)((short)(code[i+1]<<8) | (short)(code[i+2]& 0xFF));
				exitIndex = i;
				newExitVal = (short)(exitVal + (unroll-1)*(unrollLength+1));
				if(exitIndex+exitVal-3 == stop) // check whether the jump exits the loop
					exits.put(exitIndex, (int)newExitVal);
			}
			i += ByteCode.getParamCount(code[i].byteValue());
		}
		
		short loopLVarOffset = lVarOffset[start];

		int newStop = stop  +(unroll-1)*(unrollLength+1);
		this.stop = this.stop + (unroll-1)*(unrollLength+1);		// adapt the end of the whole loopbody 

		for(int i = 0; i < newCode.length; i++){					//create the new code
			if(i < start){
				newCode[i]=code[i];									// leave untouched
				newLVarOffset[i] = lVarOffset[i];
				newIsPrefetch[i] = isPrefetch[i];
				newIsPrefetchFill[i] = isPrefetchFill[i];
			} else if (i < start + unrollLength*unroll){			
				newCode[i]=code[start+((i-start)%unrollLength)];	// copy the loop body
				short currentLVarOffset = lVarOffset[start+((i-start)%unrollLength)];
				if(currentLVarOffset != loopLVarOffset){
					currentLVarOffset += localVarPointer*((i-start)/unrollLength);
//					System.out.println(localVarPointer);
				}
				
				if( (i-start)/unrollLength >= unrollRegular){
					newIsPrefetchFill[i] = true;
				} else {
					newIsPrefetchFill[i] = false;
				}
				
				if( (i-start)/unrollLength >= unrollRegular+unrollFill){
					newIsPrefetch[i] = true;
					newIsPrefetchFill[i] = false;
				} else {
					newIsPrefetch[i] = false;
				}
					
				newLVarOffset[i] = currentLVarOffset;//lVarOffset[start+((i-start)%unrollLength)] + ;
				if(exits.containsKey(start+((i-start)%unrollLength))){	// set the correct exitvalue
					int iteration = (i-start)/unrollLength;
					newExitVal = exits.get(start+((i-start)%unrollLength)).shortValue();
					newCode[i+1] = (short)(((newExitVal) & 0xff00)>>8);
					newCode[i+2] = (short)((newExitVal) & 0xff);
					if(iteration == 0){		// the 0th iteration is loop controller and thus  exits the loop - other iteration jump to the end of the code to the nop before the goto
						newExitVal -= 3;	//skip the goto
					}
					newExitVal -=unrollLength;
					newExitVal--;			//new Exitval points to the correct nop now
					exits.put(start+((i-start)%unrollLength), (int)newExitVal);
					i+=2;
				}
				

				
			} else if(i < newStop){
				newCode[i]=0;		//insert nops for merging
				newIsPrefetch[i] = false;
				newIsPrefetchFill[i] = false;
			} else if(i == newStop){
				newCode[i]=code[stop];
				newCode[i+1] = (short)((newGotoVal & 0xff00)>>8);	// set the backwards jump at the end of the loop correctly
				newCode[i+2] = (short)(newGotoVal & 0xff);
				newIsPrefetch[i] = false;
				newIsPrefetch[i+1] = false;
				newIsPrefetch[i+2] = false;
				newIsPrefetchFill[i] = false;
				newIsPrefetchFill[i+1] = false;
				newIsPrefetchFill[i+2] = false;
				i+=2;
				
			} else{
				newCode[i]=code[stop+(i-newStop)];					//leave untouched
				newLVarOffset[i] = lVarOffset[stop+(i-newStop)];
				newIsPrefetch[i] = isPrefetch[stop+(i-newStop)];
				newIsPrefetchFill[i] = isPrefetchFill[stop+(i-newStop)];
			}

		}

		code = newCode;
		lVarOffset = newLVarOffset;
		isPrefetch = newIsPrefetch;
		isPrefetchFill = newIsPrefetchFill;
		return (unroll-1)*(unrollLength+1);
	}

	
	/**
	 * Creates a DCFG from the original DataGraph
	 * @param dgraph the original DataGraph
	 * @return the DCFG
	 */
	private LinkedHashMap<Datum, Node> createCDFG(DataGraph dgraph, LoopGraph lgraph){
		LinkedHashSet<Datum> nodes = dgraph.nodes();
		LinkedHashMap<Datum, Node> mapping = new LinkedHashMap<Datum, Node>();
		
		LinkedHashMap<Integer, Node> constMapping = new LinkedHashMap<Integer, Node>();
		LinkedHashMap<Long, Node> longConstMapping = new LinkedHashMap<Long, Node>();
		
		
		CDFG graph = new CDFG();
		
		/////////////////////////////////////////////////////////////////////////////////////
		// Create Nodes from Datums														   //
		/////////////////////////////////////////////////////////////////////////////////////
		for(Datum d: nodes){
			
			// These node should not be in the graph anymore
			if( d instanceof MergerDatum){
				System.err.println("MUX:");
				for(DEdge de: dgraph.preds(d) ){
					System.err.println("\t"+de.sink.getClass().getName() + " " + de.sink.creator().i());
				}
				if(dgraph.preds_s(d) != null)
				for(Datum dd: dgraph.preds_s(d)){
					System.err.println("\t\t"+dd.getClass().getName()+" "+dd.creator().i());
				}
			}
			
			// Translate the Bytecodes into the CGRA Operations
			Amidar.OP op = getPEOP(d);
			Integer value = null;
			//////////// LOCAL VARIABLES //////////////////////////////////
			if(op == Amidar.OP.LOAD || op == Amidar.OP.STORE || op == Amidar.OP.LOAD64 || op == Amidar.OP.STORE64 || op == Amidar.OP.MUX ){
				value = d.value().intValue();
				mapping.put(d, new Node(d.creator().addr(), op,value, null, false));
			} else if( op == Amidar.OP.CONST){
				////////// CONSTANTS 32 //////////////////////////////////
				value = d.value().intValue();
				Node c = constMapping.get(value);
				if(c == null){
					c = new Node(d.creator().addr(), op,value, null, false);
					constMapping.put(value, c);
				}
				mapping.put(d, c);
				if(d.creator().i() == I.LDC_W_QUICK ){
					c.isIndirectConstant(true);
					c.setNametag("(indirect)");
				}
			} else if( op == Amidar.OP.CONST64){
		        ////////// CONSTANTS 64 //////////////////////////////////
				Long lvalue = d.value().longValue();
				Node c = longConstMapping.get(lvalue);
				if(c == null){
					c = new Node(d.creator().addr(), op,lvalue, null, false);
					longConstMapping.put(lvalue, c);
				}
				mapping.put(d, c);
				if(d.creator().i() == I.LDC2_W_QUICK ){
					c.isIndirectConstant(true);
					c.setNametag("(indirect)");
				}
			}else {
				////////// OTHER CASES //////////////////////////////////
				mapping.put(d, new Node(d.creator().addr(), op, null, false));
			}
			
			
			I origOP = d.creator().i();
//			if( origOP == I.GETSTATIC2_QUICK || origOP == I.GETSTATIC_QUICK || origOP == I.GETSTATIC_A_QUICK || origOP == I.PUTSTATIC2_QUICK || origOP == I.PUTSTATIC_QUICK || origOP == I.PUTSTATIC_A_QUICK){
////				mapping.get(d).isIndirectConstant(true);TODO
//				mapping.get(d).setNametag("(static)");
//			}
			
			if(d.creator() instanceof AbstractIF){
				AbstractIF iif = (AbstractIF)d.creator();
				if(iif.isShortcircuitevaluationTrueBranch()){
					mapping.get(d).setShortCircuitEvaluationTrueBranch(mapping.get(iif.getSceControllerTrueBranch().phi_node().ifdatum()), iif.getSceControllerTrueBranchDecision());
					mapping.get(d).setNametag("(sce)");
					graph.setControlDependency(mapping.get(iif.getSceControllerTrueBranch().phi_node().ifdatum()), mapping.get(d));
				}
				if(iif.isShortcircuitevaluationFalseBranch()){
					mapping.get(d).setShortCircuitEvaluationFalseBranch(mapping.get(iif.getSceControllerFalseBranch().phi_node().ifdatum()), iif.getSceControllerFalseBranchDecision());
					mapping.get(d).setNametag("(sce)");
					graph.setControlDependency(mapping.get(iif.getSceControllerFalseBranch().phi_node().ifdatum()), mapping.get(d));
				}
			}
			
			if(d.creator().i().equals(I.IALOAD) || d.creator().i().equals(I.IASTORE) ){
				mapping.get(d).setNametag("(I)");
			}
			if(d.creator().i().equals(I.LALOAD) || d.creator().i().equals(I.LASTORE) ){
				mapping.get(d).setNametag("(L)");
			}
			if(d.creator().i().equals(I.FALOAD) || d.creator().i().equals(I.FASTORE) ){
				mapping.get(d).setNametag("(F)");
			}
			if(d.creator().i().equals(I.DALOAD) || d.creator().i().equals(I.DASTORE) ){
				mapping.get(d).setNametag("(D)");
			}
			if(d.creator().i().equals(I.BALOAD) || d.creator().i().equals(I.BASTORE) ){
				mapping.get(d).setNametag("(B)");
			}
			if(d.creator().i().equals(I.CALOAD) || d.creator().i().equals(I.CASTORE) ){
				mapping.get(d).setNametag("(C)");
			}
			if(d.creator().i().equals(I.SALOAD) || d.creator().i().equals(I.SASTORE) ){
				mapping.get(d).setNametag("(S)");
			}
			if(d.creator().i() == I.ALOAD || d.creator().i() == I.ALOAD_0 || d.creator().i() == I.ALOAD_1 || d.creator().i() == I.ALOAD_2 || d.creator().i() == I.ALOAD_3 || d.creator().i() == I.AALOAD || d.creator().i() == I.GETSTATIC_A_QUICK){
				mapping.get(d).setNametag("(ref)"); 
			}
		}
		
		
		
		/////////////////////////////////////////////////////////////////////////////////////
		// Set all controllers   														   //
		/////////////////////////////////////////////////////////////////////////////////////
		LinkedHashMap<Instruction, Node> ifMapping = new LinkedHashMap<Instruction, Node>();
		for(Datum d: nodes){
				Instruction controllerI = d.creator().branchpoint();
				if(controllerI != null)
					controllerI = d.creator().branchpoint().ifinstr();
				Node controller = null;
				boolean decision = d.creator().decision();
				if(controllerI != null){
					for(Datum dd: nodes){
						if(controllerI == dd.creator()){
							controller = mapping.get(dd);
							break;
						}
					}
					ifMapping.put(controllerI, controller);
					mapping.get(d).setController(controller, decision);
				}
		}
		
		
		/////////////////////////////////////////////////////////////////////////////////////
		// Manage all Loops																   //
		/////////////////////////////////////////////////////////////////////////////////////
		Iterator<LoopGraph.Loop> it = lgraph.outermost();
		
		loopGraph = new LG();
		LinkedHashMap<LoopGraph.Loop, Loop> loopMapping = new LinkedHashMap<LoopGraph.Loop, Loop>();
		
		
		while(it.hasNext()){
			LoopGraph.Loop loo = it.next();
			Loop l = new Loop(loo.start_addr, loo.stop_addr);
			l.addController(ifMapping.get(loo.ifinstr));
			loopMapping.put(loo, l);
			
		}
		
		it = lgraph.outermost();
		
		while(it.hasNext()){
			LoopGraph.Loop loo = it.next();
			Loop l = loopMapping.get(loo);
			Loop parent = loopMapping.get(loo.father());
			LinkedHashSet<Loop> children = new LinkedHashSet<Loop>();
			for(LoopGraph.Loop chl: loo.children()){
				children.add(loopMapping.get(chl));
			}
			loopGraph.addLoop(l, children, parent);
		}
		
		/////////////////////////////////////////////////////////////////////////////////////
		// Manage all Dependencies + Implicit Constants									   //
		/////////////////////////////////////////////////////////////////////////////////////
		for(Datum d: nodes){
			
			LinkedHashSet<Datum> ops = dgraph.getOps();
			
			
			
			
			
			if(dgraph.succs_s(d) != null)
			for(Datum csucc: dgraph.succs_s(d)){
				if(nodes.contains(csucc))
					graph.setControlDependency(mapping.get(d), mapping.get(csucc));
			}
			if(dgraph.succs(d) != null)
			for(DEdge dsucce: dgraph.succs(d)){
				Datum dsucc = dsucce.sink;
				if(nodes.contains(dsucc)){
					if(dsucc.creator().i() != I.PUTFIELD_QUICK && dsucc.creator().i() != I.PUTFIELD2_QUICK ){
						graph.setDataDependency(mapping.get(d), mapping.get(dsucc), dsucce.attr-1);
					} else {
						if(dsucce.attr == 1){
							graph.setDataDependency(mapping.get(d), mapping.get(dsucc), 2);
						} else {
							graph.setDataDependency(mapping.get(d), mapping.get(dsucc), 0);
						}
						Node cnst;
//						if(d.creator().i().wdata()){
//							Long value = d.value().longValue();
//							cnst = longConstMapping.get(value);
//							if(cnst == null){
//								cnst = new Node(d.creator().addr(), Amidar.OP.CONST64, value, null, false);
//								longConstMapping.put(value, cnst);
//							}
//						} else {
							Integer value = dsucc.value().intValue();
							cnst = constMapping.get(value);
							if(cnst == null){
								cnst = new Node(dsucc.creator().addr(), Amidar.OP.CONST, value, null, false);
								constMapping.put(value, cnst);
							}
//						}
						graph.setDataDependency(cnst, mapping.get(dsucc), 1);
					}
				} 
				
			}
			
			if(d.creator().i() == I.GETFIELD_QUICK || d.creator().i() == I.GETFIELD_QUICK_ARRAY || d.creator().i() == I.GETFIELD2_QUICK){
				Node cnst;
				if(d.creator().i().wdata()){
					Long value = d.value().longValue();
					cnst = longConstMapping.get(value);
					if(cnst == null){
						cnst = new Node(d.creator().addr(), Amidar.OP.CONST64, value, null, false);
						longConstMapping.put(value, cnst);
					}
				} else {
					Integer value = d.value().intValue();
					cnst = constMapping.get(value);
					if(cnst == null){
						cnst = new Node(d.creator().addr(), Amidar.OP.CONST, value, null, false);
						constMapping.put(value, cnst);
					}
				}
				graph.setDataDependency(cnst, mapping.get(d), 1);
				if(d.creator().i() == I.GETFIELD_QUICK_ARRAY){
					mapping.get(d).setNametag("(ref)"); // just for debugging with dot graph
				}
			}
			
			
			
			if(!(d instanceof MergerDatum)&&( d.creator().i() == I.IFEQ || d.creator().i() == I.IFGE || d.creator().i() == I.IFGT || d.creator().i() == I.IFLE || d.creator().i() == I.IFLT || d.creator().i() == I.IFNE)){
				Node cnst = constMapping.get(0);
				if(cnst == null){
					cnst = new Node(d.creator().addr(), Amidar.OP.CONST, 0, null, false);
					constMapping.put(0, cnst);
				}
				graph.setDataDependency(cnst, mapping.get(d), 1);
			}
			
			
		}
		
		cdfg =  graph;
		
		return mapping;
	}
	
	/**
	 * Maps the bytecode instructions (javasim.synth.model.I) used for DataGraph generation to PE Operations
	 * @param d A Datum which represents a Node in the original DataGraph. This Datum holds the information about the instruction
	 * @return The corresponding PE Operation
	 */
	private Amidar.OP getPEOP(Datum d){
		if(d instanceof MergerDatum){
			return Amidar.OP.MUX;
		}
		//		switch(d.creator().i()){

		I i = d.creator().i();

		if(i.equals(I.IINC)){

			if(d instanceof SWriteDatum) 
				return Amidar.OP.IADD;
			if(d instanceof ConstDatum){
				if(d.creator().i().wdata())
					return Amidar.OP.CONST64;
				else
					return Amidar.OP.CONST;
			} else {
				return Amidar.OP.LOAD;
			}
		} else {
			return mapInstruction(i);
		}
	}
	
	
	
	private Amidar.OP mapInstruction(I i){
		switch(i){
		case IADD: return Amidar.OP.IADD;
		case ISUB: return Amidar.OP.ISUB;
		case IMUL: return Amidar.OP.IMUL;
		case IDIV: return Amidar.OP.IDIV;
		case IREM: return Amidar.OP.IREM;
		case IOR: return Amidar.OP.IOR;
		case IAND: return Amidar.OP.IAND;
		case IXOR: return Amidar.OP.IXOR;
		case ISHL: return Amidar.OP.ISHL;
		case ISHR: return Amidar.OP.ISHR;
		case IUSHR: return Amidar.OP.IUSHR;
		case LOR: return Amidar.OP.LOR;
		case LAND: return Amidar.OP.LAND;
		case LXOR: return Amidar.OP.LXOR;
		case LSHL: return Amidar.OP.LSHL;
		case LSHR: return Amidar.OP.LSHR;
		case LUSHR: return Amidar.OP.LUSHR;
		case LADD: return Amidar.OP.LADD;
		case LSUB: return Amidar.OP.LSUB;
		case LMUL: return Amidar.OP.LMUL;
		case LDIV: return Amidar.OP.LDIV;
		case LREM: return Amidar.OP.LREM;
		case FADD: return Amidar.OP.FADD;
		case FSUB: return Amidar.OP.FSUB;
		case FMUL: return Amidar.OP.FMUL;
		case FDIV: return Amidar.OP.FDIV;
		case DADD: return Amidar.OP.DADD;
//		case FREM: return Amidar.OP.FREM;
		case DSUB: return Amidar.OP.DSUB;
		case DMUL: return Amidar.OP.DMUL;
		case DDIV: return Amidar.OP.DDIV;
		case ILOAD:
		case ILOAD_0:
		case ILOAD_1:
		case ILOAD_2:
		case ILOAD_3:
		case ALOAD:
		case ALOAD_0:
		case ALOAD_1:
		case ALOAD_2:
		case ALOAD_3:
		case FLOAD:
		case FLOAD_0:
		case FLOAD_1:
		case FLOAD_2:
		case FLOAD_3:
			return Amidar.OP.LOAD;
		case IINCISTORE:
		case ISTORE:
		case ISTORE_0:
		case ISTORE_1:
		case ISTORE_2:
		case ISTORE_3:
		case FSTORE:
		case FSTORE_0:
		case FSTORE_1:
		case FSTORE_2:
		case FSTORE_3:
		case ASTORE:
		case ASTORE_0:
		case ASTORE_1:
		case ASTORE_2:
		case ASTORE_3:
			return Amidar.OP.STORE;
		case LLOAD:
		case LLOAD_0:
		case LLOAD_1:
		case LLOAD_2:
		case LLOAD_3:
		case DLOAD:
		case DLOAD_0:
		case DLOAD_1:
		case DLOAD_2:
		case DLOAD_3:
			return Amidar.OP.LOAD64;
		case LSTORE:
		case LSTORE_0:
		case LSTORE_1:
		case LSTORE_2:
		case LSTORE_3:
		case DSTORE:
		case DSTORE_0:
		case DSTORE_1:
		case DSTORE_2:
		case DSTORE_3:
			return Amidar.OP.STORE64;
		case GETFIELD_QUICK:
		case GETFIELD_QUICK_ARRAY:
		case IALOAD:
		case CALOAD:
		case BALOAD:
		case FALOAD:
		case SALOAD:
		case AALOAD:
		case GETSTATIC_QUICK:
		case GETSTATIC_A_QUICK:
		case ARRAYLENGTH:
			return Amidar.OP.DMA_LOAD;
		case PUTFIELD_QUICK:
		case IASTORE:
		case CASTORE:
		case BASTORE:
		case SASTORE:
		case FASTORE:
		case AASTORE:
		case PUTSTATIC_A_QUICK:
		case PUTSTATIC_QUICK: 
			return Amidar.OP.DMA_STORE;
		case LALOAD:
		case DALOAD:
		case GETFIELD2_QUICK:
		case GETSTATIC2_QUICK:
			return Amidar.OP.DMA_LOAD64;
		case LASTORE:
		case DASTORE:		
		case PUTFIELD2_QUICK:
		case PUTSTATIC2_QUICK:
			return Amidar.OP.DMA_STORE64;
		case IF_ACMPEQ:
		case IF_ICMPEQ:
		case IFEQ: return Amidar.OP.IFEQ;
		case IF_ICMPGE:
		case IFGE: return Amidar.OP.IFGE;
		case IF_ICMPGT:
		case IFGT: return Amidar.OP.IFGT;
		case IF_ICMPLE:
		case IFLE: return Amidar.OP.IFLE;
		case IF_ICMPLT:
		case IFLT: return Amidar.OP.IFLT;
		case IF_ICMPNE:
		case IF_ACMPNE:
		case IFNE: return Amidar.OP.IFNE;
		case FCMPG: return Amidar.OP.FCMPG;
		case FCMPL: return Amidar.OP.FCMPL;
		case DCMPG: return Amidar.OP.DCMPG;
		case DCMPL: return Amidar.OP.DCMPL;
		case LCMP: return Amidar.OP.LCMP;
		case BIPUSH:
		case SIPUSH:
		case LDC_W_QUICK:
		case ICONST_0:
		case ICONST_1:
		case ICONST_2:
		case ICONST_3:
		case ICONST_4:
		case ICONST_5:
		case ICONST_M1:
		case FCONST_0: 
		case FCONST_1:
		case FCONST_2:return Amidar.OP.CONST;
		case LDC2_W_QUICK:
		case DCONST_0:
		case DCONST_1:	
		case LCONST_0:
		case LCONST_1:return Amidar.OP.CONST64;
		case I2B: return Amidar.OP.I2B;
		case I2C: return Amidar.OP.I2C;
		case I2D: return Amidar.OP.I2D;
		case I2F: return Amidar.OP.I2F;
		case I2L: return Amidar.OP.I2L;
		case I2S: return Amidar.OP.I2S;
		case L2D: return Amidar.OP.L2D;
		case L2F: return Amidar.OP.L2F;
		case L2I: return Amidar.OP.L2I;
		case F2D: return Amidar.OP.F2D;
		case F2I: return Amidar.OP.F2I;
		case F2L: return Amidar.OP.F2L;
		case D2F: return Amidar.OP.D2F;
		case D2I: return Amidar.OP.D2I;
		case D2L: return Amidar.OP.D2L;
		case INEG: return Amidar.OP.INEG;
		case LNEG: return Amidar.OP.LNEG;
		case FNEG: return Amidar.OP.FNEG;
		case DNEG: return Amidar.OP.DNEG;
		case NOP: return Amidar.OP.NOP;
		case FSIN: return Amidar.OP.FSIN;
		case FCOS: return Amidar.OP.FCOS;
		case CI_CMP: return Amidar.OP.CI_CMP;
		
		default: System.err.println("WHAAT?" + i); return null;
		}
		
	}
	
	private LinkedHashMap<Node, LinkedHashSet<Node>> mapPotentialAliases(LinkedHashMap<Datum, LinkedHashSet<Datum>> potentialAliases, LinkedHashMap<Datum,Node> mapping){
		LinkedHashMap<Node, LinkedHashSet<Node>> returnValues = new LinkedHashMap<>();
		for(Datum firstDatum : potentialAliases.keySet()){
			Node firstNode = mapping.get(firstDatum);
			LinkedHashSet<Node> secondNodes = new LinkedHashSet<>();
			for(Datum secondDatum: potentialAliases.get(firstDatum)){
				secondNodes.add(mapping.get(secondDatum));
			}
			returnValues.put(firstNode, secondNodes);
		}
		
		
		
		
		return returnValues;
	}
	
	/**
	 * Returns the Token Set
	 * @return
	 */
	public ArrayList<LocationInformation> getLocationInformation() {
		return locationInformation;
	}

	public ArrayList<Integer> getLocalVariables() {
		return localVariables;
	}

	public ArrayList<Integer> getConstantMemory() {
		return constantMemory;
	}
	
	public ArrayList<Integer> getConstantMemoryIndirect() {
		return constantMemoryIndirect;
	}

	/**
	 * Return the number of local variables that have to be sent back from CGRA to AMIDAR
	 * @return the number
	 */
	public int getNrLocalVarSend() {
		return nrLocalVarSend;
	}

	/**
	 * Return the number of constants that have to be loaded from the contant pool and sent to the CGRA
	 * @return
	 */
	public int getNrIndirectConst() {
		return nrIndirectConst;
	}

	/**
	 * Return the number of constants that have to be sent to the CGRA directly
	 * @return
	 */
	public int getNrDirectConst() {
		return nrDirectConst;
	}

	/**
	 * Get the number of local variables that the CGRA receives
	 * @return
	 */
	public int getNrLocalVarReceive() {
		return nrLocalVarReceive;
	}

	/**
	 * Get all PE contexts
	 * @return
	 */
	public long[][] getContextsPE() {
		return contextsPE;
	}

	/**
	 * Get all C-Box contexts
	 * @return
	 */
	public long[][] getContextsCBox() {
		return contextsCBox;
	}

	/**
	 * Get all control unit contetxs
	 * @return
	 */
	public long[] getContextsControlUnit() {
		return contextsControlUnit;
	}
	
	/**
	 * Get all contexts of the handle compare unit
	 * @return
	 */
	public long[] getContextsHandleCompare() {
		return contextsHandleCompare;
	}
	
	LinkedHashMap<Node, LinkedHashSet<Node>> potentialAliases;

	/**
	 * Generates a CDFG of the given kernel
	 * 
	 * @throws SequenceNotSynthesizeableException
	 *             if there are problems to map the algorithm to hardware
	 */
	public void generateCDFG() throws SequenceNotSynthesizeableException {
		synthData = init_synthesis(start, stop);
		DataGraph dgraph;

		igraph = create_graph(synthData);										// create instruction graph and loop graph
		dgraph = data_graph(synthData, synthData.lg().outermost().next());		// create data graph

		LinkedHashMap<Datum, Node> mapping = createCDFG(dgraph, synthData.lg());
		
		potentialAliases = mapPotentialAliases(synthData.getPotentialAliases(), mapping);
		
	}
	
	
	private void schedule(LinkedHashMap<Object,LinkedHashMap<Integer,LinkedHashSet<Integer>>> handleToPeMap ) throws MissingOperationException, NotSchedulableException{

		boolean [] pref = new boolean[isPrefetch.length];
		for(int i = 0; i< pref.length; i++){
			pref[i] = isPrefetch[i] || isPrefetchFill[i];
		}
		
		LPW lpw = new LPW(cdfg, pref);

		// Schedule
		listSched = new RCListSched(cdfg, loopGraph, nrOfLocalVariables, model, handleToPeMap, pref );
		listSched.setPriorityCritereon(lpw);
		Schedule sched = listSched.schedule();
		
		
//		listSched.printPEutilization();
		 
		listSched.aliasCheck(potentialAliases,ALIASING_SPECULATION);
		listSched.registerAllocation();
		listSched.cBoxAllocation();
		
		// Generate tokens
		listSched.initDataGeneration();
		nrLocalVarReceive = listSched.getNrLocalVarReceive();
		nrLocalVarSend = listSched.getNrLocalVarSend();
		nrIndirectConst = listSched.getNrIndirectConst();
		nrDirectConst = listSched.getNrDirectConst();
		
		constantMemory = listSched.getConstantMemory();
		constantMemoryIndirect = listSched.getConstantMemoryIndirect();
		localVariables = listSched.getLocalVariables();
		locationInformation = listSched.getLocationInformation();
		
//		System.err.println("REC: " + nrLocalVarReceive);
//		System.err.println("SEND: " + nrLocalVarSend);
//		System.err.println("CONST: " + nrIndirectConst);
//		System.err.println("CONSTDIR: " + nrDirectConst);
		
		
		
		// Generate contexts
		listSched.ctxtGeneration();
		
		
		
		contextsPE = listSched.getContextsPE();
		contextsCBox = listSched.getContextsCBox();
		contextsControlUnit = listSched.getContextsControlUnit();
		contextsHandleCompare = listSched.getContextsHandleCompare();
	}
	
	/**
	 * Generates the CDFG for the kernel and schedules it
	 * @throws SequenceNotSynthesizeableException
	 */
	public void generate(LinkedHashMap<Object,LinkedHashMap<Integer,LinkedHashSet<Integer>>> handleToPeMap ) throws SequenceNotSynthesizeableException {
		generateCDFG();
		
		if(unrollBasedPrefetch){
			
			cdfg.cleanFromPrefetch(isPrefetch, isPrefetchFill);
		}
//		printGraphs();
		try {
			schedule( handleToPeMap );
		} catch (MissingOperationException | NotEnoughHardwareException | NotSchedulableException e) {
			throw new SequenceNotSynthesizeableException(e.getMessage());
		}
	}
	
	/**
	 * Prints all graphs and the schedule to dot files
	 */
	public void printGraphs(){
		boolean [] pref = new boolean[isPrefetch.length];
		for(int i = 0; i< pref.length; i++){
			pref[i] = isPrefetch[i] || isPrefetchFill[i];
		}
		
		String modName = "log/"+ methodName.replace('/', '.')+"-"+start+"-"+stop;
		try {
			
			FileWriter fw = new FileWriter(modName+"-instructions.dot");
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(igraph.print_graph(pref));
			bw.flush();
			bw.close();
			fw = new FileWriter(modName+"-CDFG.dot");
			bw = new BufferedWriter(fw);
			bw.write(cdfg.toString());
			bw.flush();
			bw.close();
			fw = new FileWriter(modName+"-LoopGraph.dot");
			bw = new BufferedWriter(fw);
			bw.write(loopGraph.toString());
			bw.flush();
			bw.close();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
		listSched.draw(modName+"-schedule.tex");
	}
	
	/**
	 * Serializes the CDFG and exports it in JSON format
	 */
	public void exportCDFG(){
		String modNamedcfg = "log/"+ methodName.replace('/', '.')+"-"+start+"-"+stop+"DCFG.json";
		String modNamelg = "log/"+ methodName.replace('/', '.')+"-"+start+"-"+stop+"LG.json";
		String modNamevariables = "log/"+ methodName.replace('/', '.')+"-"+start+"-"+stop+"localVariables.json";
		
		try {
			JsonWriter jwriterdcfg = new JsonWriter(new FileOutputStream(modNamedcfg));
			JsonWriter jwriterlog = new JsonWriter(new FileOutputStream(modNamelg));
			JsonWriter jwritervar = new JsonWriter(new FileOutputStream(modNamevariables));
			jwriterdcfg.write(cdfg);
			jwriterlog.write(loopGraph);
			jwritervar.write(nrOfLocalVariables);
			
//			JsonReader jreadyer = new JsonReader(new FileInputStream(modName));
//			DCFG dcfg =  (DCFG) jreadyer.readObject();
			
			jwriterdcfg.close();
			jwriterlog.close();
			jwritervar.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Prints the bytecode of the kernel
	 * @return
	 */
	public String dumpSynthesizedByteCode() {

		if (synthData == null) {
			throw new IllegalStateException("Cannot dump the byte code since synthesis has not run yet.");
		}

		StringBuilder sb = new StringBuilder();

		int addr = synthData.start_addr();
		while (addr < synthData.stop_addr()) {
			Instruction instruction = I.get_new(synthData, addr);
			I i = instruction.i();

			// show address and additional bytes
			sb.append(addr);
			if (i.size() > 1) {
				sb.append("[+" + (i.size() - 1) + "]");
			} else {
				sb.append("    "); // four spaces
			}

			// show opcode + instruction name
			if (i.c() < 0) {
				sb.append(String.format(" (____) ", i.c()));
			} else {
				sb.append(String.format(" (0x%02x) ", i.c()));
			}
			sb.append(i.toString());

			// show additional byte content
			if (i.size() > 1) {
				sb.append("[");
				for (int j = addr + 1; j < addr + i.size(); j++) {
					if (j > addr + 1) {
						sb.append(", ");
					}
					sb.append(String.format("0x%02x", synthData.code(j)));
				}
				sb.append("]");
			}

			sb.append("\n");
			addr += i.size();
		}

		return sb.toString();
	}

	/**
	 * Get the absolute address where the Tokenmachine should jump after the kernel was executed on the CGRA
	 * @return
	 */
	public int getEndOfSequence() {
		return backJumpAddress + codeOffset;
	}
}
