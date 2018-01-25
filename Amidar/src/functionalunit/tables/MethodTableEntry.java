package functionalunit.tables;

public class MethodTableEntry extends TableEntry {
	
	public MethodTableEntry(int[] data) {
		super(data);
	}
	public static final int FLAGS = 0;
	public static final int NUMBER_ARGS = 1;
	public static final int MAX_STACK = 2;
	public static final int MAX_LOCALS = 3;
	public static final int EXCEPTION_TABLE_LENGTH = 4;
	public static final int EXCEPTION_TABLE_REF = 5;
	public static final int CODE_LENGTH = 6;
	public static final int CODE_REF = 7;
	

}
