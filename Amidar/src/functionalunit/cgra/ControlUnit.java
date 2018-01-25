package functionalunit.cgra;

import cgramodel.ContextMaskContextControlUnit;


/**
 * The Control Unit is a generic class that generates the program counter. The main core is an FSM that is sensible 
 * to branch selection signal from the CBox.  
 * @author Dennis Wolf
 *
 */
public class ControlUnit {
	
	/**
	 * debugging purpuses
	 */
	
	
	public String state = "";

	/**
	 * Memory of the ControlUnit
	 */
	public long[] memory;
	
	
	public ContextMaskContextControlUnit contextmaskccu;

	
	/**
	 * Information from the magical Box called "C"
	 */
	public boolean InputbranchSelectionCBox;

	
	/**
	 * Determines whether the address is processed (State : RUN) or loaded (State : SETADDRESS or else). * Realised by multiplexor
	 */
	boolean InputLoadEnable;
	
	
	boolean conditional, jump, relative;

	/**
	 * Input to load an starting address. is connected to the bus. could probably be merged with input to load instruction, which
	 * are to be stored in the memory- 
	 */
	int InputData;

	
	/**
	 * Enable input
	 */
	private boolean InputEnable;

	
	/**
	 * Program counter or Context Pointer. 
	 */
	int CCNT;

	int oldCCNT;
	
	int alternative_CCNT;
	
	
	/**
	 * Variable to save address convertion
	 */
	int address;

	
	/**
	 * Register is split for coding reasons
	 */
	int stateregister;

	int msb;

	private long context;

	int nrofstates;

	public int memwidth;

	
	/**
	 * Constructor
	 */
	public ControlUnit(){

	}

	
	/**
	 * Method to configure the ControlUnit.  
	 */
	public int configure(int contextsize, ContextMaskContextControlUnit mask){
		memory = new long[contextsize];
		contextmaskccu = mask;
		int counterwidth = (int)Math.ceil((Math.log(contextsize)/Math.log(2)));
		memwidth = counterwidth-1;
		CCNT = contextsize-1;
		for(int i = 0; i < contextsize; i++){
//			memory[i] = 2<<(0xffff);
			memory[i] = 0;
			memory[i] = mask.setJump(memory[i], true);
			memory[i] = mask.setConditional(memory[i], false);
			memory[i] = mask.setRelative(memory[i], true);
		}
		context  = memory[CCNT-1];
		return counterwidth;
	}

	
	/**
	 * Stores a complete configuration in the memory. only for debugging purposes. This is not synthesisable 
	 */
	public boolean newSynthesis(long[] instructions, int slot){
		for(int i = slot, k = 0; i<slot+instructions.length;i++, k++){
			memory[i] = instructions[k];
		}
		return true;
	}
	

	/**
	 * Triggers all writes to the memory, since they are clocked.
	 */
	public void operateClocked(){
		if(InputEnable){
			context = memory[CCNT];
			oldCCNT = CCNT;
		}
	}

	
	/**
	 * Triggers all combinatorial emulation. Mainly reading the memory
	 */
	public void operateComb(){
		if(context >= 0){
			alternative_CCNT = contextmaskccu.getCounter(context);
			conditional = contextmaskccu.getConditional(context);
			jump = contextmaskccu.getJump(context);
			relative = contextmaskccu.getRelative(context);
		}
	}

	
	/**
	 * Triggers all combinatorial emulation that are dependent on later arrived signals. Mainly the next state logic.  
	 */
	public void operateLateArrival(){
//		System.out.println("OLD CCNT: "  +(CCNT-185)); 
		if(InputLoadEnable){
			CCNT = InputData;
			state = "external pc load";
		} 
		else if(InputEnable){

			if(jump){
				
				if((conditional && !InputbranchSelectionCBox) || !conditional){
					if(relative){
						CCNT = oldCCNT + alternative_CCNT;
						state ="R JMP";
					}
					else{
						CCNT = alternative_CCNT;
						state ="A JMP";
					}
				}
				else{
					CCNT = oldCCNT + 1;
					state ="Inc";
				}
			}
			else {
				CCNT = oldCCNT + 1;
				state ="Inc";
			}
			
		}
//		System.out.println("CCU STATE: " + state + " CC " + (CCNT%memory.length-185) + "cond: " + conditional);
		CCNT = CCNT%memory.length;
	}
	
	public int getProgramCounter(){
		return CCNT;		
	}

	
	protected boolean getInputPbox() {
		return InputbranchSelectionCBox;
	}

	
	public void setInputCbox(boolean inputPbox) {
		InputbranchSelectionCBox = inputPbox;
	}

	
	protected boolean getLoadEnable() {
		return InputLoadEnable;
	}

	
	public void setLoadEnable(boolean input) {
		InputLoadEnable = input;
	}

	
	protected int getInputData() {
		return InputData;
	}

	
	public void setInputData(int inputData) {
		InputData = inputData;
	}

	
	public boolean getInputEnable() {
		return InputEnable;
	}

	
	public void setInputEnable(boolean inputEnable) {
		InputEnable = inputEnable;
	}
}
