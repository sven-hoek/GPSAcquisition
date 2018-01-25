package functionalunit.tokenmachine;

/**
 * This interface models an instruction cache for the AMIDAR token machine
 * @author jung
 *
 */
public interface InstructionCache {
	
	/**
	 * Request an instruction from the Instruction cache from the given address
	 * @param addr The address of the instruction
	 * @return whether the data is available
	 */
	public int requestData(int addr);
	
	
	/**
	 * Gets the requested from the cache. The data is only valid iff the return value of the request was true 
	 * @return the requested data
	 */
	public byte getData();
	
	
	/**
	 * Denotes whether the Cache can process a request
	 * @return true if Cache can process a data request
	 */
	public boolean isReady();
	
	/**
	 * Invalidates the cache
	 */
	public void invalidate();
	
	/**
	 * Initializes the Cache. The memory is initialized with the instructions from the AXT-file
	 * This method does not reflect the bootloading process in the HW implementation at all.
	 * 
	 * @param memory the actual instructions
	 */
	public void initMemory(byte[] memory);
	
	public byte[] getMemory();

}
