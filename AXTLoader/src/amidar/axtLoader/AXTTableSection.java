package amidar.axtLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeMap;
//import amidar.tools.*;


public class AXTTableSection {
	
	private byte[] cti2Name;
	private byte[] classTable;
	private byte[] methodTable;
	private byte[] staticMethodTable;
	private byte[] exceptionTable;
	private byte[] implementedInterfaces;
	private byte[] interfaceTable;
	
	private int numberOfBundles;
	private AXTHeader header;
	
	/****** ByteCode Profiling *******/
//	ArrayList<TreeMap<Integer, JumpStat>> jumps;
//	ArrayList<LinkedList<int[]>> lockedSequences;
//	float[] bestPercentageClass;
//	float[] bestPercentageMethod;
//	JumpStat[] bestStat;
//	int[] bestMethod;
	/******* Object Profiling ********/
	boolean[] invoke;
	/****** Object-Method Call Profiling *****/
	ArrayList<HashMap<Integer, Integer>> expectedClassIndex;  // maps addr of invokevirtual to expected class
	
	int classTableLength;
	int methodTableLength;
	int staticMethodTableLength;

	AXTTableSection(AXTHeader header, int numberOfBundles){
		this.header = header;
		this.numberOfBundles = numberOfBundles;
	}
	
	void init(){
		long sizeOfCti2Name = 0, sizeOfClassTable = 0, sizeOfMethodTable = 0, sizeOfStaticMethodTable = 0, sizeOfExceptionTable = 0,
				sizeOfImplementedInterfaces = 0, sizeOfInterfaceTable = 0/*, sizeOfStaticFieldTable = 0*/;
		
		for(int bundleNumber = 0; bundleNumber < numberOfBundles; bundleNumber++){
			sizeOfCti2Name += header.getClassTableOffset(bundleNumber) - header.getCti2NameOffset(bundleNumber);
			sizeOfClassTable += header.getMethodTableOffset(bundleNumber) - header.getClassTableOffset(bundleNumber);
			sizeOfMethodTable += header.getStaticMethodTableOffset(bundleNumber) - header.getMethodTableOffset(bundleNumber);
			sizeOfStaticMethodTable += header.getExceptionTableOffset(bundleNumber) - header.getStaticMethodTableOffset(bundleNumber);
			sizeOfExceptionTable += header.getImplementedInterfacesOffset(bundleNumber) - header.getExceptionTableOffset(bundleNumber);
			sizeOfImplementedInterfaces += header.getInterfaceTableOffset(bundleNumber) - header.getImplementedInterfacesOffset(bundleNumber);
			sizeOfInterfaceTable += header.getConstantPoolOffset(bundleNumber) - header.getInterfaceTableOffset(bundleNumber);
		}
		
		classTableLength = (int) sizeOfClassTable;
		methodTableLength = (int) sizeOfMethodTable;
		staticMethodTableLength = (int) sizeOfStaticMethodTable;
		
		cti2Name = new byte[(int) sizeOfCti2Name];
		classTable = new byte[(int) sizeOfClassTable];
		methodTable = new byte[(int) sizeOfMethodTable];
		staticMethodTable = new byte[(int) sizeOfStaticMethodTable];
		exceptionTable = new byte[(int) sizeOfExceptionTable];
		implementedInterfaces = new byte[(int) sizeOfImplementedInterfaces];
		interfaceTable = new byte[(int) sizeOfInterfaceTable];

		int methodsSize = (int) (sizeOfMethodTable / AXTFile_const.METHODTABLEENTRYSIZE + sizeOfStaticMethodTable / AXTFile_const.STATICMETHODTABLEENTRYSIZE);
//		jumps = new ArrayList<TreeMap<Integer, JumpStat>>();
//		lockedSequences = new ArrayList<LinkedList<int[]>>();
//		bestMethod = new int[(int) sizeOfClassTable / AXTFile_const.CLASSTABLEENTRYSIZE + 1];
//		expectedClassIndex = new ArrayList<HashMap<Integer, Integer>>();
//		bestPercentageClass = new float[(int) sizeOfClassTable / AXTFile_const.CLASSTABLEENTRYSIZE + 1]; //+1 because of static methods
//		bestPercentageMethod = new float[methodsSize];
//		bestStat = new JumpStat[methodsSize];
//		invoke = new boolean[methodsSize];
//		for(int i = 0; i < methodsSize; i++){
//			jumps.add(new TreeMap<Integer, JumpStat>());
//			lockedSequences.add(new LinkedList<int[]>());
//			expectedClassIndex.add(new HashMap<Integer, Integer>());
//			invoke[i] = false;
//		}
	}

	void setCti2Name(int number, byte value){
		cti2Name[number] = value;
	}
	
	void setClassTable(int number, byte value){
		classTable[number] = value;
	}
	
	void setMethodTable(int number, byte value){
		methodTable[number] = value;
	}
	
	void setStaticMethodTable(int number, byte value){
		staticMethodTable[number] = value;
	}
	
	void setExceptionTable(int number, byte value){
		exceptionTable[number] = value;
	}
	
	void setImplementedInterface(int number, byte value){
		implementedInterfaces[number] = value;
	}
	
	void setInterfaceTable(int number, byte value){
		interfaceTable[number] = value;
	}
	
	public byte[] getCti2Name(){
		return cti2Name;
	}
	
	public byte[] getClassTable(){
		return classTable;
	}
	
	public byte[] getMethodTable(){
		return methodTable;
	}
	
	public byte[] getStaticMethodTable(){
		return staticMethodTable;
	}
	
	public byte[] getExceptionTable(){
		return exceptionTable;
	}
	
	public byte[] getImplementedInterface(){
		return implementedInterfaces;
	}
	
	public byte[] getInterfaceTable(){
		return interfaceTable;
	}
	
	/*********************/
	/**** Class Table ****/
	/*********************/
	
	
	public int getClassTableSize(){
		return classTableLength / AXTFile_const.CLASSTABLEENTRYSIZE;
	}
	
	
	//index defines the index of the classTableEntry, NOT the index of the byte
	//returns null, if index does not exist, else: the classTableEntry of the given index
	private byte[] getClassTableEntry(int classIndex){
		byte[] result = new byte[AXTFile_const.CLASSTABLEENTRYSIZE];
		if(classIndex < 0 || classIndex >= classTableLength / AXTFile_const.CLASSTABLEENTRYSIZE){
			//TODO update trace
//			IOManager.get().getf("errio").println("Wrong Class Index!" + classIndex + ", " + header.getNumberOfClasses());
			return null;
		}
		else{
			for(int i = 0; i < AXTFile_const.CLASSTABLEENTRYSIZE; i++){
				result[i] = classTable[classIndex * AXTFile_const.CLASSTABLEENTRYSIZE + i];
			}
			return result;
		}
	}
	
	//returns null if methodTableEntry does not exist in methodTable, else: the classTableEntry, the methodTableEntry belongs to
	private byte[] getClassTableEntry(byte[] methodTableEntry){		
		byte[] result = new byte[AXTFile_const.CLASSTABLEENTRYSIZE];
		int methodTableNumber = getMethodNumber(methodTableEntry);
		int prevMethodTableRef = 0, currMethodTableRef;
		int index = -1;
		for(int i = 1; i < classTableLength / AXTFile_const.CLASSTABLEENTRYSIZE; i++){
			currMethodTableRef = classTableGetMethodTableRef(i);
			if(methodTableNumber >= prevMethodTableRef && methodTableNumber < currMethodTableRef){
				index = i - 1;
				break;
			}
			prevMethodTableRef = currMethodTableRef;
		}
		if(index >= 0){
			for(int i = 0; i < AXTFile_const.CLASSTABLEENTRYSIZE; i++){
				result[i] = classTable[index * AXTFile_const.CLASSTABLEENTRYSIZE + i];
			}
		}
		else{
			return null;
		}
		return result;
	}
	
	//returns -1 if classTableEntry does not exist in classTable, else: the index of classTableEntry in classTable
	private int getClassTableIndex(byte[] classTableEntry){
		boolean classTableEntryInClassTable = false;
		if(classTableEntry != null){
			for(int i = 0; i < classTableLength; i += AXTFile_const.CLASSTABLEENTRYSIZE){
				if(classTable[i] == classTableEntry[0]){
					classTableEntryInClassTable = true;
					for(int j = 1; j < AXTFile_const.CLASSTABLEENTRYSIZE; j++){
						if(classTableEntry[i + j] == classTableEntry[j]){
							classTableEntryInClassTable &= true;
						}
						else{
							classTableEntryInClassTable = false;
						}
					}
					if(classTableEntryInClassTable){
						return i;
					}
				}
			}
		}
		return -1;
	}
	
	//methodNumber is the number of the method in methodTable
	public int getClassTableIndex(int methodNumber){
		byte[] methodTableEntry = getMethodTableEntry(methodNumber);
		byte[] classTableEntry = getClassTableEntry(methodTableEntry);
		return getClassTableIndex(classTableEntry);
	}
	
	public short classTableGetObjectSize(int classTableIndex){
		byte[] classTableEntry = getClassTableEntry(classTableIndex);
		return (short) AXTFile.byteArrayToLong(classTableEntry, AXTFile_const.OBJECTSIZEOFFSET, AXTFile_const.OBJECTSIZESIZE);
	}
	
//	//identisch mit "getObjectSize", bezieht sich auf Array-Typen
//	public short getDataType(int classTableIndex){
//		byte[] classTableEntry = getClassTableEntry(classTableIndex);
//		return (short) AXTFile.byteArrayToLong(classTableEntry, AXTFile_const.OBJECTSIZEOFFSET, AXTFile_const.OBJECTSIZESIZE);
//	}
	
	public short classTableGetInterfaceTableRefOffset(int classTableIndex){
		byte[] classTableEntry = getClassTableEntry(classTableIndex);
		return (short) AXTFile.byteArrayToLong(classTableEntry, AXTFile_const.INTERFACETABLEREFOFFSET, AXTFile_const.INTERFACETABLEREFSIZE);
	}
	
	public short classTableGetImplInterfaceTableRefOffset(int classTableIndex){
		byte[] classTableEntry = getClassTableEntry(classTableIndex);
		return (short) AXTFile.byteArrayToLong(classTableEntry, AXTFile_const.IMPLEMENTEDINTERFACESREFOFFSET, AXTFile_const.IMPLEMENTEDINTERFACESREFSIZE);
	}
	
//	//identisch mit "getInterfaceTableRefOffset", bezieht sich auf Array-Typen
//	public int getDimension(int classIndex){
//		byte[] classTableEntry = getClassTableEntry(classIndex);
//		return (int) AXTFile.byteArrayToLong(classTableEntry, AXTFile_const.INTERFACETABLEREFOFFSET, AXTFile_const.INTERFACETABLEREFSIZE);
//	}
	
	public short classTableGetMethodTableRef(int classTableIndex){
		byte[] classTableEntry = getClassTableEntry(classTableIndex);
		return (short) AXTFile.byteArrayToLong(classTableEntry, AXTFile_const.METHODTABLEREFOFFSET, AXTFile_const.METHODTABLEREFSIZE);
	}
	
	public int classTableGetClassFlags(int classTableIndex){
		byte[] classTableEntry = getClassTableEntry(classTableIndex);
		return (int) AXTFile.byteArrayToLong(classTableEntry, AXTFile_const.CLASSFLAGSOFFSET, AXTFile_const.CLASSFLAGSSIZE);
	}
	
	public short classTableGetSuperCTI(int classTableIndex){
		byte[] classTableEntry = getClassTableEntry(classTableIndex);
		return (short) AXTFile.byteArrayToLong(classTableEntry, AXTFile_const.SUPERINDEXOFFSET, AXTFile_const.SUPERINDEXSIZE);
	}
	
	static TreeMap<Integer,Integer> methodTableRef;
	
	//returns the number of methods of the class of the classTableEntry, NOT the number of bytes
//	public int getNumberOfMethodsOfClass(int classTableIndex){
//		int numberOfClasses = (int) AXTFile.getInstance().getHeader().getNumberOfClasses();
//		if(methodTableRef == null){
//			methodTableRef = new TreeMap<Integer,Integer>();
//			for(int i = 0; i < numberOfClasses; i++){ //TODO: muss nur einmal berechnet werden!! UMSCHREIBEN!!!
//				methodTableRef.put((int) getMethodTableRef(i), i);
//			}
//		}
//		Object[] keys = methodTableRef.keySet().toArray();
//		Object[] values = methodTableRef.values().toArray();
//		int result = 0;
//		for(int i = 0; i < numberOfClasses; i++){
//			if((int) values[i] == classTableIndex){
//				if(i + 1 < numberOfClasses){
//					result = (int) keys[i + 1] -  (int) keys[i];
//				} else{
//					result = ((int) header.getNumberOfMethods() - 1) -  (int) keys[i]; //-1 wegen "METHOD-TABLE-ClassNameNotSet"
//				}
//			}
//		}
//		return result;
//	}
	
	/********************************************/
	/**** Method Table & Static Method Table ****/
	/********************************************/
		
	//methodNumber defines the index of the method in methodTable, NOT the index of the byte
	//methodNumber is the number of the methodTableEntry in the complete methodTable-array 
	//returns null, if methodNumber does not exist, else: the methodTableEntry of the given methodNumber
	protected byte[] getMethodTableEntry(int methodNumber){
		byte[] result = new byte[AXTFile_const.METHODTABLEENTRYSIZE];
		if(methodNumber < 0 || methodNumber >= header.getNumberOfMethods() + (int) header.getNumberOfStaticMethods()){ 
//			TODO update trace
//			IOManager.get().getf("errio").println("Wrong Method Index!");
			return null;
		}
		//static methods:
		if(methodNumber * AXTFile_const.METHODTABLEENTRYSIZE >= methodTableLength){
			for(int i = 0; i < AXTFile_const.STATICMETHODTABLEENTRYSIZE; i++){
				result[i] = staticMethodTable[(methodNumber - (int) header.getNumberOfMethods()) * AXTFile_const.STATICMETHODTABLEENTRYSIZE + i];
			}
		}
		//dynamic methods:
		else{
			for(int i = 0; i < AXTFile_const.METHODTABLEENTRYSIZE; i++){
				result[i] = methodTable[methodNumber * AXTFile_const.METHODTABLEENTRYSIZE + i];
			}
		}
		return result;
	}
	
	//returns -1 if methodTableEntry does not exist in methodTable, else: the index of methodTableEntry in methodTable
	private int getMethodNumber(byte[] methodTableEntry){
		boolean methodTableEntryExists = false;
		for(int i = 0; i < methodTableLength; i += AXTFile_const.METHODTABLEENTRYSIZE){
			if(methodTable[i] == methodTableEntry[0]){
				methodTableEntryExists = true;
				for(int j = 1; j < AXTFile_const.METHODTABLEENTRYSIZE; j++){
					if(methodTable[i + j] != methodTableEntry[j]){
						methodTableEntryExists = false;
						break;
					}
				}
				if(methodTableEntryExists){
					return i / AXTFile_const.METHODTABLEENTRYSIZE;
				}
			}
		}
		//bei statischen Methoden
		for(int i = 0; i < staticMethodTableLength; i += AXTFile_const.STATICMETHODTABLEENTRYSIZE){
			if(staticMethodTable[i] == methodTableEntry[0]){
				methodTableEntryExists = true;
				for(int j = 1; j < AXTFile_const.STATICMETHODTABLEENTRYSIZE; j++){
					if(staticMethodTable[i + j] != methodTableEntry[j]){
						methodTableEntryExists = false;
						break;
					}
				}
				if(methodTableEntryExists){
					return i / AXTFile_const.STATICMETHODTABLEENTRYSIZE + (int) header.getNumberOfMethods();
				}
			}
		}
		return -1;
	}
	
	//methodNumber is the number of the method in methodTable
	//methodIndex is the index of the method in the class it belongs to
	public int getMethodNumber(int classIndex, int methodIndex){
		return classTableGetMethodTableRef(classIndex) + methodIndex;
	}
	
	public short methodTableGetNumArgs(int methodNumber){
		byte[] methTabEntry = getMethodTableEntry(methodNumber);
		return (short) AXTFile.byteArrayToLong(methTabEntry, AXTFile_const.NUMARGSOFFSET, AXTFile_const.NUMARGSSIZE);
	}
	
	public short methodTableGetMaxLocals(int methodNumber){
		byte[] methTabEntry = getMethodTableEntry(methodNumber);
		return (short) AXTFile.byteArrayToLong(methTabEntry, AXTFile_const.MAXLOCALSOFFSET, AXTFile_const.MAXLOCALSSIZE);
	}
	
	public short methodTableGetCodeLength(int methodNumber){
		byte[] methTabEntry = getMethodTableEntry(methodNumber);
		return (short) AXTFile.byteArrayToLong(methTabEntry, AXTFile_const.CODELENGTHOFFSET, AXTFile_const.CODELENGTHSIZE);
	}
	
	public long methodTableGetCodeRef(int methodNumber){
		byte[] methTabEntry = getMethodTableEntry(methodNumber);
		return AXTFile.byteArrayToLong(methTabEntry, AXTFile_const.CODEREFOFFSET, AXTFile_const.CODEREFSIZE);
	}
	
	public short methodTableGetExceptionTableLength(int methodNumber){
		byte[] methTabEntry = getMethodTableEntry(methodNumber);
		return (short) AXTFile.byteArrayToLong(methTabEntry, AXTFile_const.EXCEPTIONTABLELENGTHOFFSET, AXTFile_const.EXCEPTIONTABLELENGTHSIZE);
	}
	
	public short methodTableGetExceptionTableRef(int methodNumber){
		byte[] methTabEntry = getMethodTableEntry(methodNumber);
		return (short) AXTFile.byteArrayToLong(methTabEntry, AXTFile_const.EXCEPTIONTABLEREFOFFSET, AXTFile_const.EXCEPTIONTABLEREFSIZE);
	}
	
	public int methodTableGetMethodFlags(int methodNumber){
		byte[] methTabEntry = getMethodTableEntry(methodNumber);
		return  (int) AXTFile.byteArrayToLong(methTabEntry, AXTFile_const.METHODFLAGSOFFSET, AXTFile_const.METHODFLAGSSIZE);
	}
	
	public int methodTableGetMaxStack(int methodNumber){
		byte[] methTabEntry = getMethodTableEntry(methodNumber);
		return  (int) AXTFile.byteArrayToLong(methTabEntry, AXTFile_const.MAXSTACKOFFSET, AXTFile_const.MAXSTACKSIZE);
	}

	/*************************/
	/**** Exception Table ****/
	/*************************/
	
//	//index defines the index of the exception table, NOT the index of the byte
//	private byte[] getExceptionTable(int index){
//		byte[] result = new byte[AXTFile_const.EXCEPTIONTABLEENTRYSIZE];
//		for(int i = 0; i < AXTFile_const.EXCEPTIONTABLEENTRYSIZE; i++){
//			result[i] = exceptionTable[index * AXTFile_const.EXCEPTIONTABLEENTRYSIZE + i];
//		}
//		return result;
//	}

	private byte[] getExceptionTableEntry(int index){
		byte[] result = new byte[AXTFile_const.EXCEPTIONTABLEENTRYSIZE];
		if(index < 0 || index >= exceptionTable.length / AXTFile_const.EXCEPTIONTABLEENTRYSIZE){
			//TODO update trace
//			IOManager.get().getf("errio").println("Wrong Class Index!" + classIndex + ", " + header.getNumberOfClasses());
			return null;
		}
		else{
			for(int i = 0; i < AXTFile_const.EXCEPTIONTABLEENTRYSIZE; i++){
				result[i] = exceptionTable[index * AXTFile_const.EXCEPTIONTABLEENTRYSIZE + i];
			}
			return result;
		}
	}
	
	
	public short exceptionTableGetStartPC(int index){
		byte[] exceptionTableEntry = getExceptionTableEntry(index);
		return (short) AXTFile.byteArrayToLong(exceptionTableEntry, AXTFile_const.STARTPCOFFSET, AXTFile_const.STARTPCSIZE);
	}
	
	public short exceptionTableGetEndPC(int index){
		byte[] exceptionTableEntry = getExceptionTableEntry(index);
		return (short) AXTFile.byteArrayToLong(exceptionTableEntry, AXTFile_const.ENDPCOFFSET, AXTFile_const.ENDPCSIZE);
	}
	
	public short exceptionTableGetHandlerPC(int index){
		byte[] exceptionTableEntry = getExceptionTableEntry(index);
		return (short) AXTFile.byteArrayToLong(exceptionTableEntry, AXTFile_const.HANDLERPCOFFSET, AXTFile_const.HANDLERPCSIZE);
	}
	
	public short exceptionTableGetCatchType(int index){
		byte[] exceptionTableEntry = getExceptionTableEntry(index);
		return (short) AXTFile.byteArrayToLong(exceptionTableEntry, AXTFile_const.CATCHTYPEOFFSET, AXTFile_const.CATCHTYPESIZE);
	}
	
	public int getExceptionTableSize(){
		return exceptionTable.length / AXTFile_const.EXCEPTIONTABLEENTRYSIZE;
	}
	
	/********************************/
	/**** Implemented Interfaces ****/
	/********************************/
	
	// index defines the index of the implementedInterfacesEntry, NOT the index of the byte
	public int getImplementedInterfaces(int index) {
		byte[] result = new byte[header.getImplementedInterfacesEntrySize()];
		for (int i = 0; i < header.getImplementedInterfacesEntrySize(); i++) {
			result[i] = implementedInterfaces[index * header.getImplementedInterfacesEntrySize() + i];
		}
		return (int)AXTFile.byteArrayToLong(result);
	}
	
	public int getImplementedInterfacesSize(){
		return implementedInterfaces.length / header.getImplementedInterfacesEntrySize();
	}
	
	/*************************/
	/**** Interface Table ****/
	/*************************/

	// index defines the index of the interfaceTableEntry, NOT the index of the byte
	private byte[] getInterfaceTableEntry(int index) {
		byte[] result = new byte[AXTFile_const.INTERFACETABLEENTRYSIZE];
		for (int i = 0; i < AXTFile_const.INTERFACETABLEENTRYSIZE; i++) {
			result[i] = interfaceTable[index * AXTFile_const.INTERFACETABLEENTRYSIZE + i];
		}
		return result;
	}
	
	public int interfaceTableGetMethodOffset(int index){
		byte[] intTabEntry = getInterfaceTableEntry(index);
		return (int) AXTFile.byteArrayToLong(intTabEntry, AXTFile_const.INTERFACESTARTOFFSET, AXTFile_const.INTERFACESTARTSIZE);
	}
	
//	public short getInterfaceStart(int classIndex, int interfaceIndex){
//		int interfaceTableRefOffset = classTableGetInterfaceTableRefOffset(classIndex);
//		byte[] intTabEntry = getInterfaceTableEntry(interfaceTableRefOffset + interfaceIndex);
//		return (short) AXTFile.byteArrayToLong(intTabEntry, AXTFile_const.INTERFACESTARTOFFSET, AXTFile_const.INTERFACESTARTSIZE);
//	}
	
	public int getInterfaceTableSize(){
		return interfaceTable.length / AXTFile_const.INTERFACETABLEENTRYSIZE;
	}
	
	/***********************************/
	/**** ByteCode Profiling Classes****/
	/***********************************/
	
//	public void dumpStats(int classIndex) {
//		int index = getMethodTableRef(classIndex);
//		for(int i = 0; i < getNumberOfMethodsOfClass(classIndex); i++){
//			dumpJumpRecords(index + i);
//		}
//	}
	
//	public float getBestPercentageClass(int classIndex) {
//		return bestPercentageClass[classIndex];
//	}
//	
//	public int getBestMethodNumber(int classIndex) {
//		return bestMethod[classIndex];
//	}
	
//	public void calculateMostEfficientJumpStatMethod(long bcNum, int classIndex) {
//		bestPercentageClass[classIndex] = 0;
////		System.out.println("classIndex = " + classIndex);
//		//dynamic methods
//		if(classIndex < header.getNumberOfClasses()){
//			int index = getMethodTableRef(classIndex);
//			for (int i = 0; i < getNumberOfMethodsOfClass(classIndex); i++) {
//				calculateMostEfficientJumpStat(bcNum, index + i);
//				float currper = getBestPercentageMethod(index + i);
//	//			System.out.println("currper = " + currper);
//				if (currper > bestPercentageClass[classIndex]) {
//					bestPercentageClass[classIndex] = currper;
//					bestMethod[classIndex] = index + i;
//				}
//			}
//		}
//		//static methods
//		else if(classIndex == header.getNumberOfClasses()){
//			int numberOfMethods = (int) header.getNumberOfMethods();
//			for (int i = 0; i < (int) header.getNumberOfStaticMethods(); i++) {
//				calculateMostEfficientJumpStat(bcNum, numberOfMethods + i);
//				float currper = getBestPercentageMethod(numberOfMethods + i);
//	//			System.out.println("currper = " + currper);
//				if (currper > bestPercentageClass[classIndex]) {
//					bestPercentageClass[classIndex] = currper;
//					bestMethod[classIndex] = numberOfMethods + i;
//				}
//			}
//		}
//	}
	
	/***********************************/
	/**** ByteCode Profiling Methods****/
	/***********************************/
	
	//methodNumber is the number of the method in an array of all methods
//	public void recordJump(int t, int end, int accu, int methodNumber) {
////		System.out.println("methNum = " + methodNumber);
////		int classIndex = getClassTableIndex(methodNumber);
////		System.out.println("jumps.size() = "+ jumps.size());
//	    JumpStat j = jumps.get(methodNumber).get(t);
//	    if (j==null) {
//			j = new JumpStat(t,end, methodNumber);
//	    	jumps.get(methodNumber).put(t, j);
//	    	
//			Integer i = jumps.get(methodNumber).lowerKey(t);
//			if (i != null) {
//				JumpStat f = jumps.get(methodNumber).get(i);
//				if (f.getEnd() > t)
//					j.father(f);
//			}
//
//			i = jumps.get(methodNumber).higherKey(t);
//			if (i != null) {
//				JumpStat c = jumps.get(methodNumber).get(i);
//				if ((c.father() == null) && (i < end)) {
//					c.father(j);
//				}
//			}
//		} else {
//	    	j.inc();
//	    	j.accumulate(accu);
//	    }
//	    
//	    Iterator<Integer> it = jumps.get(methodNumber).keySet().iterator();
//	    while (it.hasNext()) {
//	    	JumpStat ju = jumps.get(methodNumber).get(it.next());
//	    	if (ju.remove()) {
//			    System.out.println("removing jump");
//		    	it.remove();
//			}
//	    }
//	    
//	}

	/**
	 * @param a The address that is looked up in the Jump Table
	 * @return The entry of the Jump Table if existing
	 *         null, otherwise.
	 */
//	public JumpStat matchAddress(int a, int methodNumber) {
//	    return jumps.get(methodNumber).get(a);
//	}
//
//
//	public String dumpJumpRecords(int methodNumber) {
//		StringBuilder res = new StringBuilder();
//		if(!jumps.get(methodNumber).isEmpty()) res.append("Jump-Stats for method: "+ methodNumber);
//
//		for(int t : jumps.get(methodNumber).keySet()) {
//	    	res.append("\t"+jumps.get(methodNumber).get(t).dump());
//		}
//		
//		return res.toString();
//	}
//	
//	public JumpStat getBestJumpStat(int methodNumber) {
//		return bestStat[methodNumber];
//	}
//	
//	public float getBestPercentageMethod(int methodNumber) {
//		return bestPercentageMethod[methodNumber];
//	}
//	
//	public void calculateMostEfficientJumpStat(long bcNum, int methodNumber) {
//		bestPercentageMethod[methodNumber] = 0;
//		bestStat[methodNumber] = null;
//		calculate_jump(bcNum, methodNumber);
////		System.out.println("values = " + jumps.get(methodNumber).values());
//		for(JumpStat current : jumps.get(methodNumber).values()) {
//		    if (current.isBlocked()) continue;
//			long accu = current.getByteCodeAccu();
// 			float currper = (float)accu/(float)bcNum;
//// 			System.out.println("currper = " + currper + ", accu = " + accu + ", bcNum = " + bcNum);
//			if (currper > bestPercentageMethod[methodNumber]) {
//				bestPercentageMethod[methodNumber] = currper;
//				bestStat[methodNumber] = current;
//			}
//		}
//	}
//
//	/**
//	 * @param bcNum total bytecodes executed so far.
//	 */
//	public void calculate_jump(long bcNum, int methodNumber) {
//		for (Integer j : jumps.get(methodNumber).descendingKeySet()) {
//			JumpStat lp = jumps.get(methodNumber).get(j);
//			JumpStat father = lp.father();
//			if (father != null)
//				father.accumulate(father.count()*lp.getByteCodeAccu());
//		}
//	}
//
//	public void blockBestJump(int methodNumber) {
//	    bestStat[methodNumber].block();
//	}
//    
//	public void markBestJump(int methodNumber) {
//		bestStat[methodNumber].markToRemove();
//	}
//	
//	public void deleteBestJump(int methodNumber) {
//		jumps.get(methodNumber).remove(bestStat[methodNumber].getStart());
//	}
//	
//	public String toString(int methodNumber) {
//		return String.valueOf(methodNumber);
//	}
//	
//	/***********************************/
//	/***** Object Profiling Methods*****/
//	/***********************************/
//	
//	public void setInvoke(int methodNumber) {
//		invoke[methodNumber] = true;
//	}
//	
//	public boolean getInvoke(int methodNumber) {
//		return invoke[methodNumber];
//	}
//
//	public void addLockedSequence(int start, int end, int methodNumber){
//		int[] lockedSequence = {start, end};
//		lockedSequences.get(methodNumber).add(lockedSequence);
//	}
//	
//	public boolean isSequenceLocked(int start, int end, int methodNumber){
//		Iterator<int[]> it = lockedSequences.get(methodNumber).iterator();
//		while (it.hasNext()){
//			int [] actSequence = it.next();
//			if (start>=actSequence[0] & end<=actSequence[1]) return true;
//		}
//		return false;
//	}
//	
//	/***************************************/
//	/***** Object-Method Call Profiling*****/
//	/***************************************/
//	
//	public void addExpectedClass(Integer addr, Integer ci, int methodNumber){
//		expectedClassIndex.get(methodNumber).put(addr, ci);
//	}
//	
//	public Integer getExpectedClass(Integer addr, int methodNumber){
//		return expectedClassIndex.get(methodNumber).get(addr);
//	}
}
