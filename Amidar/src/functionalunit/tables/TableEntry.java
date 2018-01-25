package functionalunit.tables;

/**
 * This class describes a generic table entry for the tables described by the AXT format
 * @author jung
 *
 */
public class TableEntry {
	
	/**
	 * The columns of the entry
	 */
	int [] data = null;
	
	public TableEntry(){
		
	}
	
	
	/**
	 * creates a new entry
	 * @param data
	 */
	public TableEntry(int [] data){
		this.data = data;
	}
	
	/**
	 * Gets the value of the given colum
	 * @param column the column
	 * @return the value of the column
	 */
	public int get(int column){
		return data[column];
	}

	/**
	 * Convenience method for table entries with just one column
	 * @return
	 */
	public int value(){
		return data[0];
	}

}
