package functionalunit.tables;


/**
 * A very simple Table cache. Every access is a hit
 * @author jung
 *
 */
public class SimpleTableCache<Entry extends TableEntry> implements TableCache<Entry> {
	
	Entry [] memory ;
	Entry data;
	
	public void initMemory(Entry[] memory){
		this.memory = memory;
	}



	public boolean requestData(int addr) {
		data = memory[addr];
		return true;
	}

	public Entry getData() {
		return data;
	}
	
	public int getSize(){
		return memory.length;
	}
	
	public Entry[] getMemory(){
		return memory;
	}

}
