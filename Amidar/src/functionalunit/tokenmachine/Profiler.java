package functionalunit.tokenmachine;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;

import tracer.Trace;
import functionalunit.tables.LoopProfileTableEntry;

/**
 * NOT YET CORRESPONDING TO HW - WHICH SIZES? CACHING? TIMING MODEL? DUNNO...
 * This version slightly differs from the one described in the Paper "Hardware Based Online Profiling in AMIDAR Processors"
 * differences are:	- one profile memory for all methods (because of axtformat and inlining)
 * 					- end address is the jump target of the loop controlling jump bytecode - thus we solve the while loop bug and the inlining problem
 * 					- detection of loop end address is done when reached the first jump within the loop
 * 
 * @author jung
 *
 */
public class Profiler implements Serializable{
	
	private long globalBytecodeCount = 0;
	
	private LinkedHashMap<Integer, Integer> CAM;
	private boolean enteredLoop = false;
	private int memPointer = 0;
	private int cnt = 0;
	
	private String [] methodNames; // Only for Debugging and Synthesis report
	
	
	private LoopProfileTableEntry [] profileMemory = new LoopProfileTableEntry[512]; //So we can use Tablecache later on???
	
	private LoopProfileTableEntry currentLoop;
	private int previousIndex = -1;
	
	/**
	 * Creates a new Profiler
	 */
	public Profiler(){
		CAM = new LinkedHashMap<Integer,Integer>();
	}
	
	/**
	 * This method is used by the token machine decoder to report jumps to the profiler in order to register loops correctly
	 * @param currentAddress The address of the jump bytecode
	 * @param jumpValue the jump distance
	 */
	public void jump(int currentAddress, int jumpValue, int AMTI){
		
		if(enteredLoop){ // This means the current jump is the loop controller and jumps to the exit addr
			updateEndAddress(currentAddress + jumpValue);
			enteredLoop = false;
		}
		
		if(jumpValue<0){
			if(!CAM.containsKey(currentAddress+jumpValue)){
				registerLoop(currentAddress,jumpValue, AMTI);
			}
			
			if(currentLoop != null){
				currentLoop.setData(LoopProfileTableEntry.INVOKE_COUNTER, currentLoop.get(LoopProfileTableEntry.INVOKE_COUNTER)+1);
			}
//			enteredLoop = true;
		} 
	}
	
	/**
	 * Register a new backward jump (which is always a loop)
	 * @param currentAddress The address of the jump bytecode
	 * @param jumpValue the jump distance
	 */
	private void registerLoop(int currentAddress, int jumpValue, int AMTI){
		CAM.put(currentAddress + jumpValue, memPointer);
		
		
		int[] data = new int[8];
		
		data[LoopProfileTableEntry.START] = currentAddress + jumpValue;
		data[LoopProfileTableEntry.END] = currentAddress + 3;
		data[LoopProfileTableEntry.GLOBAL_COUNTER] = 0;
		data[LoopProfileTableEntry.LOCAL_COUNTER] = 0;
		data[LoopProfileTableEntry.PREDECESSOR_INDEX] = 0;
		data[LoopProfileTableEntry.SYNTHESIZED] = 0; 
		data[LoopProfileTableEntry.INVOKE_COUNTER] = 0;
		data[LoopProfileTableEntry.AMTI] = AMTI;	//NEEDED ONLY FOR DEBUGGING 
		
		profileMemory[memPointer] = new LoopProfileTableEntry(data);
		memPointer++;
	}
	
	/**
	 * We reached the first jump after we entered a loop - this jump leads us to the exit address of the loop
	 * @param endAddress
	 */
	private void updateEndAddress(int endAddress){
		if(endAddress > currentLoop.get(LoopProfileTableEntry.END)){
			
			currentLoop.setData(LoopProfileTableEntry.END, endAddress);
			
		}
	}

	/**
	 * This method is used by the token machine to report a new Bytecode
	 * @param addr the instruction memory address
	 */
	public void newByteCode(int addr){
//		System.err.println("PROFADDR: " + addr);
		globalBytecodeCount++;
		if(currentLoop != null && addr == currentLoop.get(LoopProfileTableEntry.END)) {
			leftLoop();
			enteredLoop = false;
		}
		if(CAM.containsKey(addr) && previousIndex != CAM.get(addr)){
			enteredLoop(CAM.get(addr));
			enteredLoop = true;
		}
		cnt++;
		
	}
	
	/**
	 * To be called when a new loop is entered
	 * @param loopIndex the index of the new loop
	 */
	private void enteredLoop(int loopIndex){
		if(currentLoop != null){
			currentLoop.setData(LoopProfileTableEntry.LOCAL_COUNTER, cnt);
		}
		currentLoop = profileMemory[loopIndex];
		currentLoop.setData(LoopProfileTableEntry.PREDECESSOR_INDEX, previousIndex);
		previousIndex = loopIndex;
		cnt = 0;
	}
	
	/**
	 * To be called when a loop is left
	 */
	private void leftLoop(){
		currentLoop.setData(LoopProfileTableEntry.GLOBAL_COUNTER, currentLoop.get(LoopProfileTableEntry.GLOBAL_COUNTER) + cnt);
		previousIndex = currentLoop.get(LoopProfileTableEntry.PREDECESSOR_INDEX);
		if(previousIndex != -1){
			currentLoop = profileMemory[previousIndex];
			cnt = cnt + currentLoop.get(LoopProfileTableEntry.LOCAL_COUNTER);
		} else {
			currentLoop = null;
		}
		
	}
	
	
	public void reportProfile(Trace tracer){
		for(int i = 0; i < memPointer; i++){
			tracer.println(methodNames[profileMemory[i].get(LoopProfileTableEntry.AMTI)]);
			int synthesized = profileMemory[i].get(LoopProfileTableEntry.SYNTHESIZED);
			String synth =  synthesized < 0?"NOT POSSIBLE":(synthesized==0?"NO":"YES ("+synthesized+" ms)");
			tracer.println("\tMethod AMTI: "+profileMemory[i].get(LoopProfileTableEntry.AMTI)+"\tBytecode: " +profileMemory[i].get(LoopProfileTableEntry.START)+"-"+profileMemory[i].get(LoopProfileTableEntry.END) + "\tGlobalCounter: " + profileMemory[i].get(LoopProfileTableEntry.GLOBAL_COUNTER) + "\tSynthesized: " + synth);
		}
	}
	
	public long getGlobalBytecodeCount(){
		return globalBytecodeCount;
	}

	public LoopProfileTableEntry getBestCandidate() {
		int max = -1;
		LoopProfileTableEntry loop = null;
		
		for(int i = 0; i < memPointer; i++){
			
			LoopProfileTableEntry current = profileMemory[i];
			if(current.get(LoopProfileTableEntry.GLOBAL_COUNTER) > max && current.get(LoopProfileTableEntry.SYNTHESIZED) == 0){
				loop = current;
				max = current.get(LoopProfileTableEntry.GLOBAL_COUNTER);
			}
		}
		return loop;
	}

	public void setMethodNames(String[] methodNames) {
		this.methodNames = methodNames;
	}
	
	/**
	 * Delete Method names in order to save heap space - in sweeps i don't need those names but i need space
	 */
	public void deleteMethodNames(){
		methodNames = null;
	}

}
