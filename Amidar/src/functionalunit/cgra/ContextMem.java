package functionalunit.cgra;

/**
 * This class emulates a memory containing contexts.
 * @author Dennis Wolf
 *
 */
public class ContextMem {
	
	public long[] memory;
	
	public int memory_length;
	
	long output;
	
	int inputCCNT;
	
	/**
	 * Write enable input to write contextentries
	 */
	private boolean InputWriteEnable = false;

	/**
	 * Input form a bus
	 */
	long InputData;

	/**
	 * Every context has its own context-ID, which should be equal to its corresponding PE.  
	 */
	int ID;

	
	/**
	 * Method to configure the context memory. 
	 */
	public void configureContext(int memory_length, int id){
		this.memory_length = memory_length;
		memory = new long[memory_length];
//		for(int i = 0; i <memory_length;i++){
//			memory[i] = 0;
//		}
		ID = id;
	}
	

	/**
	 * Operates memory writes since they are clocked 
	 */
	public void clocked(){
		if(InputWriteEnable){
			memory[memory_length-1] = InputData;
		}
		if(inputCCNT > memory.length || inputCCNT <0)
			throw new RuntimeException("ERROR Pointer in Context " + ID + " out of Bound");// TODO
		output = memory[inputCCNT];
	}

	
	/**
	 * Triggers the emulation of the combinatorial circuit
	 */
	public long combinatorial(){
		if(output!=-1){
			return output;
		}
		else{
			return memory[memory_length-1];
		}
	}

	/**
	 * Getter method for the output 
	 */
	public long getOutput() {
		return output;
	}

	/**
	 * Getter method for input CCNT
	 */
	public int getInputCCNT() {
		return inputCCNT;
	}

	/**
	 * Setter method for input CCNT
	 */
	public void setInputCCNT(int inputCCNT) {
		this.inputCCNT = inputCCNT;
	}

	
	/**
	 * Method to write a complete configuration to the Context. This is only a helper function and not synthesizable in hardware
	 */
	public void setContext(long[] data, int slot){
		for(int i = slot; i < data.length; i ++)
			memory[i] = data[i];
	}
	
	
	/**
	 * Method to set a single entry
	 */
	public void setContext(long data, int slot){
		memory[slot] = data;
	}

	
	/**
	 * Getter method of the memory size 
	 */
	public int getMemorySize(){
		return memory.length;
	}

	
	/**
	 * Getter method of the write enable input
	 */
	public boolean getInputWriteEnable() {
		return InputWriteEnable;
	}

	
	/**
	 * Setter method of the write enable input
	 */
	public void setInputWriteEnable(boolean inputWriteEnable) {
		InputWriteEnable = inputWriteEnable;
	}

	/**
	 * Setter method for the input of the context memory 
	 */
	public void setInputData(long data){
		InputData = data;
	}

}
