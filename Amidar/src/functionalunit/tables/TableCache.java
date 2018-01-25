package functionalunit.tables;

/**
 * Defines a Cache for the Tables which are part of the AXT format
 * @author jung
 *
 * @param <Entry> A sub class of TableEntry
 */
public interface TableCache<Entry extends TableEntry> {
	
	/**
	 * Request an TableEntry from the Table cache from the given address
	 * @param addr The address of the TableEntry
	 * @return whether the data is available
	 */
	public boolean requestData(int addr);
	
	
	/**
	 * Gets the requested data from the cache. The data is only valid iff the return value of the request was true 
	 * @return the requested data
	 */
	public Entry getData();
	
	
	/**
	 * Initializes the Cache. The memory is initialized with the instructions from the AXT-file
	 * This method does not reflect the bootloading process in the HW implementation at all.
	 * 
	 * @param memory the actual instructions
	 */
	public void initMemory(Entry[] memory);
	
}
