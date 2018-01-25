package amidar.axtLoader;

public class AXTFile_const {
	
	// AXTHeader
	public static final long MAGICNUMBER = 0xAC5DF5CC;
	public static final int HEADERSIZE = 64;
	
	/*
	 * ****************************************************************************************************
	 * 			TableSection
	 * ****************************************************************************************************
	 */
	
	//Cti2Name
	public static final int CTISIZE = 4;
	
	//ClassTable
	public static final int CLASSTABLEENTRYSIZE = 12; // size of each ClassTableEntry in byte
	public static final int CLASSFLAGSOFFSET = 0, CLASSFLAGSSIZE = 2;
	public static final int OBJECTSIZEOFFSET = 2, OBJECTSIZESIZE = 2; //= DataType for Arrays; = 'empty' for Interfaces
	public static final int SUPERINDEXOFFSET = 4, SUPERINDEXSIZE = 2; //SuperIndex of Array/Interface is 0 (= ClassIndex of Object)
	public static final int IMPLEMENTEDINTERFACESREFOFFSET = 6, IMPLEMENTEDINTERFACESREFSIZE = 2; //for Array -> clonable; 
	public static final int INTERFACETABLEREFOFFSET = 8, INTERFACETABLEREFSIZE = 2; // = dimensions for Arrays
	public static final int METHODTABLEREFOFFSET = 10, METHODTABLEREFSIZE = 2; //for Arrays: ClassTableRef of the ObjectType, the Array is filled with

	//ClassFlags
	public static final long ARRAY = 0x0001; // declares the object as Array-Type
	public static final long PRIMITIVE = 0x0010; // declares, if elements of an array are primitives or references
	public static final long PRIMITIVETYPE1 = 0x0020; // coding of primitive type (only when "PRIMITV"-flag is set)
	public static final long PRIMITIVETYPE2 = 0x0040; // 000 -> 111: boolean, char, float, double, byte, short, int, long
	public static final long PRIMITIVETYPE3 = 0x0080;
	
	
	// MethodTable
	public static final int METHODTABLEENTRYSIZE = 16; // size of each MethodTableEntry in byte
	public static final int METHODFLAGSOFFSET = 0, METHODFLAGSSIZE = 1;
	public static final int NUMARGSOFFSET = 1, NUMARGSSIZE = 1;
	public static final int MAXSTACKOFFSET = 2, MAXSTACKSIZE = 2;
	public static final int MAXLOCALSOFFSET = 4, MAXLOCALSSIZE = 2;
	public static final int EXCEPTIONTABLELENGTHOFFSET = 6, EXCEPTIONTABLELENGTHSIZE = 2;
	public static final int EXCEPTIONTABLEREFOFFSET = 8, EXCEPTIONTABLEREFSIZE = 2;
	public static final int CODELENGTHOFFSET = 10, CODELENGTHSIZE = 2;
	public static final int CODEREFOFFSET = 12, CODEREFSIZE = 4;
	
	//MethodFlags
	public static final long SYNCHRONIZED = 0x01; // Deklariert die Methode als synchronized
	
	//StaticMethodTable
	public static final int STATICMETHODTABLEENTRYSIZE = 16; // size of each StaticMethodTableEntry in byte
	public static final int STATICMETHODFLAGSOFFSET = 0, STATICMETHODFLAGSSIZE = 1;
	public static final int STATICNUMARGSOFFSET = 1, STATICNUMARGSSIZE = 1;
	public static final int STATICMAXSTACKOFFSET = 2, STATICMAXSTACKSIZE = 2;
	public static final int STATICMAXLOCALSOFFSET = 4, STATICMAXLOCALSSIZE = 2;
	public static final int STATICEXCEPTIONTABLELENGTHOFFSET = 6, STATICEXCEPTIONTABLELENGTHSIZE = 2;
	public static final int STATICEXCEPTIONTABLEREFOFFSET = 8, STATICEXCEPTIONTABLEREFSIZE = 2;
	public static final int STATICCODELENGTHOFFSET = 10, STATICCODELENGTHSIZE = 2;
	public static final int STATICCODEREFOFFSET = 12, STATICCODEREFSIZE = 4;

	//ExceptionTable
	public static final int EXCEPTIONTABLEENTRYSIZE = 8; // size of each ExceptionTableEntry in byte
	public static final int STARTPCOFFSET = 0, STARTPCSIZE = 2;
	public static final int ENDPCOFFSET = 2, ENDPCSIZE = 2;
	public static final int HANDLERPCOFFSET = 4, HANDLERPCSIZE = 2;
	public static final int CATCHTYPEOFFSET = 6, CATCHTYPESIZE = 2;

	//ImplementedInterfaces
	//size of each ImplementedInterfaceEntry is given by "implementedInterfacesEntrySize" in "AXTHeader"
	
	//InterfaceTable
	public static final int INTERFACETABLEENTRYSIZE = 2; // size of each InterfaceTableEntry in byte
	public static final int INTERFACESTARTOFFSET = 0, INTERFACESTARTSIZE = 2;

	/*
	 * ****************************************************************************************************
	 * 			DataSection
	 * ****************************************************************************************************
	 */
	
	public static final int CONSTANTPOOLENTRYSIZE = 4; // size of each constantPoolEntry in byte
	
	/*
	 * ****************************************************************************************************
	 * 			Heap
	 * ****************************************************************************************************
	 */
	
	//HandleTable
	public static final int HANDLETABLEENTRYSIZE = 16;
	public static final int MONITORIDOFFSET = 0, MONITORIDSIZE = 4;
	public static final int CLASSTABLEINDEXOFFSET = 4, CLASSTABLEINDEXSIZE = 2;
	public static final int HANDLEFLAGSOFFSET = 6, HANDLEFLAGSSIZE = 2;
	public static final int REFOBJECTSIZEOFFSET = 8, REFOBJECTSIZESIZE = 4;
	public static final int ABSOLUTEREFERENCEOFFSET = 12, ABSOLUTEREFERENCESIZE = 4;
	
	//HandleFlags
	public static final long HANDLEARRAY = 0x0001; // declares the object as Array-Type
	public static final long HANDLEALIVE = 0x0002; // declares, if the object is still reachable
	public static final long HANDLEPRIMITIVE = 0x0010; // declares, if elements of an array are primitives or references
	public static final long HANDLEPRIMITIVETYPE1 = 0x0020; // coding of primitive type (only when "PRIMITV"-flag is set)
	public static final long HANDLEPRIMITIVETYPE2 = 0x0040; // 000 -> 111: boolean, char, float, double, byte, short, int, long
	public static final long HANDLEPRIMITIVETYPE3 = 0x0080;
	public static final long STATICFIELD = 0x8000; // declares the object as static field section
	
	//ObjectHeap
	public static final int OBJECTHEAPENTRYSIZE = 4; // size of each objectHeapEntry in byte (only for output)
	
	/*
	 * ****************************************************************************************************
	 * 			Miscellaneous
	 * ****************************************************************************************************
	 */
	
	public static final int BOOLEAN_TYPE = 0b000;
	public static final int CHAR_TYPE = 0b001;
	public static final int FLOAT_TYPE = 0b010;
	public static final int DOUBLE_TYPE = 0b011;
	public static final int BYTE_TYPE = 0b100;
	public static final int SHORT_TYPE = 0b101;
	public static final int INT_TYPE = 0b110;
	public static final int LONG_TYPE = 0b111;
}
