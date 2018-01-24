package amidar.axtLoader;


public class AXTDataSection {

	private byte[] constantPool;
	private byte[] bytecode;
	private byte[] handleTable;
	private byte[] objectHeap;
	
	private AXTHeader header;
	private AXTTableSection tabSec;
	private int numberOfBundles;
	
	AXTDataSection(AXTHeader header, AXTTableSection tabSec, int numberOfBundles){
		this.header = header;
		this.tabSec = tabSec;
		this.numberOfBundles = numberOfBundles;
	}
	
	void init(){
		long sizeOfConstantPool = 0, sizeOfBytecode = 0, sizeOfHandleTable = 0, sizeOfObjectHeap = 0;
		
		for(int bundleNumber = 0; bundleNumber < numberOfBundles; bundleNumber++){
			sizeOfConstantPool += header.getBytecodeOffset(bundleNumber) - header.getConstantPoolOffset(bundleNumber);
			sizeOfBytecode += header.getObjectHeapOffset(bundleNumber) - header.getBytecodeOffset(bundleNumber);
			sizeOfObjectHeap += header.getHandleTableOffset(bundleNumber) - header.getObjectHeapOffset(bundleNumber);
			sizeOfHandleTable += header.getSize(bundleNumber) - header.getHandleTableOffset(bundleNumber);
		}
		
		constantPool = new byte[(int) sizeOfConstantPool];
		bytecode = new byte[(int) sizeOfBytecode];
		handleTable = new byte[(int) sizeOfHandleTable];
		objectHeap = new byte[(int) sizeOfObjectHeap];
	}
	
	void setConstantPool(int number, byte value){
		constantPool[number] = value;
	}
	
	void setBytecode(int number, byte value){
		bytecode[number] = value;
	}
	
	void setBytecodeComplete(byte[] completeBytecode){
		bytecode = completeBytecode;
	}
	
	void setHandleTable(int number, byte value){
		handleTable[number] = value;
	}
	
	void setObjectHeap(int number, byte value){
		objectHeap[number] = value;
	}
	
	public byte[] getConstantPool(){
		return constantPool;
	}
	
	public byte[] getBytecode(){
		return bytecode;
	}
	
	public byte[] getHandleTable(){
		return handleTable;
	}
	
	public byte[] getObjectHeap(){
		return objectHeap;
	}
	
	public int getConstantPoolEntry(int index){
		return (int) AXTFile.byteArrayToLong(constantPool, index * AXTFile_const.CONSTANTPOOLENTRYSIZE, AXTFile_const.CONSTANTPOOLENTRYSIZE);
	}
	
	public long getConstantPoolEntry2(int index){
		return AXTFile.byteArrayToLong(constantPool, index * AXTFile_const.CONSTANTPOOLENTRYSIZE, AXTFile_const.CONSTANTPOOLENTRYSIZE * 2);
	}
	
	public int getConstantPoolSize(){
		return constantPool.length/AXTFile_const.CONSTANTPOOLENTRYSIZE;
	}
	
	//returns the complete bytecode of method
	public byte[] getBytecode(int methodNumber){
		byte[] methodTab = tabSec.getMethodTableEntry(methodNumber);
		return getBytecode(methodTab);
	}
	
//	public void setBytecode(int methodNumber, byte[] code){
//		int codeRef = (int) AXTFile.getInstance().getTabSec().getCodeRef(methodNumber);
//		long codeLength = AXTFile.getInstance().getTabSec().getCodeLength(methodNumber);
//		for(int i = 0; i< codeLength; i++){
//			bytecode[i + codeRef] = code[i];
//		}
//	}
	
	//returns the complete bytecode of method
	private byte[] getBytecode(byte[] methodTableEntry){
		int codeRef = (int) AXTFile.byteArrayToLong(methodTableEntry, AXTFile_const.CODEREFOFFSET, AXTFile_const.CODEREFSIZE);
		int codeLength = (int) AXTFile.byteArrayToLong(methodTableEntry, AXTFile_const.CODELENGTHOFFSET, AXTFile_const.CODELENGTHSIZE);
		byte[] result = new byte[codeLength];
		for(int i = 0; i < codeLength; i++){
			result[i] = bytecode[codeRef + i];
		}
		return result;
	}
	
	//returns maximum the next four bytes (transformed to int) from the bytecode, beginning by addr
	public int getByteCode(int methodNumber, int addr){
		byte[] codeMeth = getBytecode(methodNumber);
		int size = 4;
		if(codeMeth.length < addr + size){
			size = codeMeth.length - addr;
		}
		byte[] code = new byte[4];
		for(int i = 0; i < 4; i++){
			if(i < size){
				code[i] = codeMeth[addr + i];
			} else{
				code[i] = 0;
			}
		}
		int reducedCode = ((code[0] << 24) & 0xff000000) | ((code[1] << 16) & 0xff0000) | ((code[2] << 8) & 0xff00) | (code[3] & 0xff);
		return reducedCode;
	}
	
	//index defines the index of the handleTableEntry, NOT the index of the byte
	//returns null, if index does not exist, else: the handleTableEntry of the given index
	private byte[] getHandleTableEntry(int handleIndex){
		byte[] result = new byte[AXTFile_const.HANDLETABLEENTRYSIZE];
		if(handleIndex < 0 || handleIndex >= handleTable.length / AXTFile_const.HANDLETABLEENTRYSIZE){
			System.err.println("Wrong Handle Index");
//			TraceManager.stderr().println("Wrong Handle Index!!!!");
			return null;
		}
		else{
			for(int i = 0; i < AXTFile_const.HANDLETABLEENTRYSIZE; i++){
				result[i] = handleTable[handleIndex * AXTFile_const.HANDLETABLEENTRYSIZE + i];
			}
			return result;
		}
	}
	
	public int getHandleFlags(int handleTableIndex){
		byte[] handleTableEntry = getHandleTableEntry(handleTableIndex);
		return (int) AXTFile.byteArrayToLong(handleTableEntry, AXTFile_const.HANDLEFLAGSOFFSET, AXTFile_const.HANDLEFLAGSSIZE);
	}
	
	public int getClassTableIndex(int handleTableIndex){
		byte[] handleTableEntry = getHandleTableEntry(handleTableIndex);
		return (int) AXTFile.byteArrayToLong(handleTableEntry, AXTFile_const.CLASSTABLEINDEXOFFSET, AXTFile_const.CLASSTABLEINDEXSIZE);
	}
	
	public int getMID(int handleTableIndex){
		byte[] handleTableEntry = getHandleTableEntry(handleTableIndex);
		return (int) AXTFile.byteArrayToLong(handleTableEntry, AXTFile_const.MONITORIDOFFSET, AXTFile_const.MONITORIDSIZE);
	}
	
	public long getRefObjectSize(int handleTableIndex){
		byte[] handleTableEntry = getHandleTableEntry(handleTableIndex);
		return AXTFile.byteArrayToLong(handleTableEntry, AXTFile_const.REFOBJECTSIZEOFFSET, AXTFile_const.REFOBJECTSIZESIZE);
	}
	
	public long getAbsoluteReference(int handleTableIndex){
		byte[] handleTableEntry = getHandleTableEntry(handleTableIndex);
		return AXTFile.byteArrayToLong(handleTableEntry, AXTFile_const.ABSOLUTEREFERENCEOFFSET, AXTFile_const.ABSOLUTEREFERENCESIZE);
	}
	
}
