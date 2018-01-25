package functionalunit.tokenmachine;

/**
 * Very simple implementation of Instruction cache. Cache always hits
 * @author jung
 *
 */
public class SimpleInstructionCache implements InstructionCache {
	
	
	
	byte[] memory;
	
	byte data;
	
	public void initMemory(byte[] memory){
		this.memory = memory;
	}
	

	public int requestData(int addr) {
		data = memory[addr];
		return 0;
	}

	public byte getData() {
		return data;
	}

	public boolean isReady() {
		return true;
	}

	public void invalidate() {
		// Nothing to do as this cache always hits
	}
	
	public byte[] getMemory(){
		return memory;
	}

}
