package amidar.axtLoader;

public class AXTHeader {

	private long[] magicNumber;
	private long[] cti2NameOffset;
	private long[] classTableOffset;
	private long[] methodTableOffset;
	private long[] staticMethodTableOffset;
	private long[] exceptionTableOffset;
	private long[] implementedInterfacesOffset;
	private long[] interfaceTableOffset;
	private long[] constantPoolOffset;
	private long[] bytecodeOffset;
	private long[] objectHeapOffset;
	private long[] handleTableOffset;
	private long[] size;
	private long[] mainMethod; //Index der Main-Methode, NICHT die Bytes
	private int[] interfaceOffset; //gibt an, wo in Klassentabelle die InterfaceEinträge beginnen (den Index, NICHT die Bytes)
	private int[] arrayTypeOffset; //gibt an, wo in Klassentabelle die ArrayEinträge beginnen (den Index, NICHT die Bytes)
	private long[] threadAMTI;
	
	private int implementedInterfacesEntrySize; //gibt die Anzahl an Byte jedes Eintrages in "implementedInterfaces" an
	private long classTableInterfaceOffset; //gibt den Index-Offset (den Index, NICHT die Bytes) der Interfaces in "classTable" an (für alle Bündel zusammen)
	private long classTableArrayTypeOffset; //gibt den Index-Offset (den Index, NICHT die Bytes) der Arrays in "classTable" an (für alle Bündel zusammen)
	
	int numberOfBundles;
	long numberOfClasses;
	long numberOfMethods;
	long numberOfStaticMethods;
	
	AXTHeader(int numberOfBundles){
		this.numberOfBundles = numberOfBundles;
		
		magicNumber = new long[numberOfBundles];
		cti2NameOffset = new long[numberOfBundles];
		classTableOffset = new long[numberOfBundles];
		methodTableOffset = new long[numberOfBundles];
		staticMethodTableOffset = new long[numberOfBundles];
		exceptionTableOffset = new long[numberOfBundles];
		implementedInterfacesOffset = new long[numberOfBundles];
		interfaceTableOffset = new long[numberOfBundles];
		constantPoolOffset = new long[numberOfBundles];
		bytecodeOffset = new long[numberOfBundles];
		objectHeapOffset = new long[numberOfBundles];
		handleTableOffset = new long[numberOfBundles];
		size = new long[numberOfBundles];
		mainMethod = new long[numberOfBundles];
		interfaceOffset = new int[numberOfBundles];
		arrayTypeOffset = new int[numberOfBundles];
		threadAMTI = new long[numberOfBundles];
	}
	
	void init(){
		implementedInterfacesEntrySize = calculateImplementedInterfacesEntrySize();
	}
	
	void setMagicNumber(int bundleNumber, long value){
		magicNumber[bundleNumber] = value;
	}
	
	void setCti2NameOffset(int bundleNumber, long value){
		cti2NameOffset[bundleNumber] = value;
	}
	
	void setClassTableOffset(int bundleNumber, long value){
		classTableOffset[bundleNumber] = value;
	}
	
	void setMethodTableOffset(int bundleNumber, long value){
		methodTableOffset[bundleNumber] = value;
	}
	
	void setStaticMethodTableOffset(int bundleNumber, long value){
		staticMethodTableOffset[bundleNumber] = value;
	}
	
	void setExceptionTableOffset(int bundleNumber, long value){
		exceptionTableOffset[bundleNumber] = value;
	}
	
	void setImplementedInterfacesOffset(int bundleNumber, long value){
		implementedInterfacesOffset[bundleNumber] = value;
	}
	
	void setInterfaceTableOffset(int bundleNumber, long value){
		interfaceTableOffset[bundleNumber] = value;
	}
	
	void setConstantPoolOffset(int bundleNumber, long value){
		constantPoolOffset[bundleNumber] = value;
	}
	
	void setBytecodeOffset(int bundleNumber, long value){
		bytecodeOffset[bundleNumber] = value;
	}
	
	void setHandleTableOffset(int bundleNumber, long value){
		handleTableOffset[bundleNumber] = value;
	}
	
	void setMainMethod(int bundleNumber, long value){
		mainMethod[bundleNumber] = value;
	}
	
	void setObjectHeapOffset(int bundleNumber, long value){
		objectHeapOffset[bundleNumber] = value;
	}
	
	void setInterfaceOffset(int bundleNumber, int value){
		interfaceOffset[bundleNumber] = value;
	}
	
	void setArrayTypeOffset(int bundleNumber, int value){
		arrayTypeOffset[bundleNumber] = value;
	}
	
	void setThreadAMTI(int bundleNumber, long value){
		threadAMTI[bundleNumber] = value;
	}
	
	void setSize(int bundleNumber, long value){
		size[bundleNumber] = value;
	}
	
	public long getMagicNumber(int bundleNumber){
		return magicNumber[bundleNumber];
	}
	
	public long getCti2NameOffset(int bundleNumber){
		return cti2NameOffset[bundleNumber];
	}
	
	public long getClassTableOffset(int bundleNumber){
		return classTableOffset[bundleNumber];
	}
	
	public long getMethodTableOffset(int bundleNumber){
		return methodTableOffset[bundleNumber];
	}
	
	public long getStaticMethodTableOffset(int bundleNumber){
		return staticMethodTableOffset[bundleNumber];
	}
	
	public long getExceptionTableOffset(int bundleNumber){
		return exceptionTableOffset[bundleNumber];
	}
	
	public long getImplementedInterfacesOffset(int bundleNumber){
		return implementedInterfacesOffset[bundleNumber];
	}
	
	public long getInterfaceTableOffset(int bundleNumber){
		return interfaceTableOffset[bundleNumber];
	}
	
	public long getConstantPoolOffset(int bundleNumber){
		return constantPoolOffset[bundleNumber];
	}
	
	public long getBytecodeOffset(int bundleNumber){
		return bytecodeOffset[bundleNumber];
	}
	
	public long getHandleTableOffset(int bundleNumber){
		return handleTableOffset[bundleNumber];
	}
	
	public long getObjectHeapOffset(int bundleNumber){
		return objectHeapOffset[bundleNumber];
	}
	
	public long getMainMethod(int bundleNumber){
		return mainMethod[bundleNumber];
	}
	
	public int getInterfaceOffset(int bundleNumber){
		return interfaceOffset[bundleNumber];
	}
	
	public int getArrayTypeOffset(int bundleNumber){
		return arrayTypeOffset[bundleNumber];
	}
	
	public long getThreadAMTI(int bundleNumber){
		return threadAMTI[bundleNumber];
	}
	
	public long getSize(int bundleNumber){
		return size[bundleNumber];
	}
	
	public long getClassTableInterfaceOffset(){
		return classTableInterfaceOffset;
	}
	
	void setClassTableInterfaceOffset(int value){
		classTableInterfaceOffset = value;
	}
	
	public long getClassTableArrayTypeOffset(){
		return classTableArrayTypeOffset;
	}
	
	void setClassTableArrayTypeOffset(int value){
		classTableArrayTypeOffset = value;
	}
	
	int calculateImplementedInterfacesEntrySize(){
		int numberOfInterfaces = 0; 
		for(int bundleNumber = 0; bundleNumber < numberOfBundles; bundleNumber++){
			numberOfInterfaces += getArrayTypeOffset(bundleNumber) - getInterfaceOffset(bundleNumber);
		}
		
//		return (int)Math.ceil(Math.log(numberOfInterfaces)/Math.log(2));
		return (numberOfInterfaces + 31)/32 * 4; //wandelt das Ergebnis von Bit in Word (= 4 Byte) um, wobei immer aufgerundet wird
	}
	
	public int getImplementedInterfacesEntrySize(){
		return implementedInterfacesEntrySize;
	}
	
	void setNumberOfClasses(long number){
		numberOfClasses = number;
	}
	
	public long getNumberOfClasses(){
		return numberOfClasses;
	}
	
	void setNumberOfMethods(long number){
		numberOfMethods = number;
	}
	
	public long getNumberOfMethods(){
		return numberOfMethods;
	}
	
	void setNumberOfStaticMethods(long number){
		numberOfStaticMethods = number;
	}
	
	public long getNumberOfStaticMethods(){
		return numberOfStaticMethods;
	}
}
