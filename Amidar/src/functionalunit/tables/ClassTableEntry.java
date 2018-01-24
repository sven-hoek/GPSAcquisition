package functionalunit.tables;

public class ClassTableEntry extends TableEntry {

	public ClassTableEntry(int[] data) {
			super(data);
	}
	public static final int FLAGS = 0;
	public static final int CLASSSIZE = 1;
	public static final int SUPER_CTI = 2;
	public static final int IMPL_INTERFACE_TABLE_REF = 3;
	public static final int INTERFACE_TABLE_REF = 4;
	public static final int METHOD_TABLE_REF = 5;
	
	

}
