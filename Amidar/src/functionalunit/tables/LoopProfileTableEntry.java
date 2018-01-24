package functionalunit.tables;

import java.io.Serializable;

public class LoopProfileTableEntry extends TableEntry implements Serializable {
	
	public static final int PREDECESSOR_INDEX = 0;
	public static final int START = 1;
	public static final int END = 2;
	public static final int LOCAL_COUNTER = 3;
	public static final int GLOBAL_COUNTER = 4;
	public static final int SYNTHESIZED = 5;
	public static final int INVOKE_COUNTER = 6;
	public static final int AMTI = 7;	// NEEDED ONLY FOR DEBUGGING
	
	public LoopProfileTableEntry(){
		super();
	}

	public LoopProfileTableEntry(int[] data) {
		super(data);
	}
	
	public void setData(int index, int data){
		this.data[index] = data;
	}
	
	public String getProfile(){
		StringBuilder erg = new StringBuilder();
		
		return "Method "+data[AMTI]+ ":";
		
	}

}
