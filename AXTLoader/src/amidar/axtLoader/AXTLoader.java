package amidar.axtLoader;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class AXTLoader implements Serializable {
	
	private ArrayList<String> axtFilePaths;
	
	private ArrayList<byte[]> axtFileArray;
	private ArrayList<ByteBuffer> axtFileBuffer;
	private String name;
	private AXTFile axtFile;
	private AXTHeader header;
	
	private String[] methodNames;
	
	
	public AXTLoader(String cname){
//		axtFilePaths = defineLoadingPath(cname, filePaths);
		axtFilePaths = new ArrayList<String>();
		axtFilePaths.add(cname);
		axtFileArray = new ArrayList<byte[]>();
		axtFileBuffer = new ArrayList<ByteBuffer>();
		name = nameByPath();
		axtFile = new AXTFile(axtFilePaths.size());
		header = axtFile.getHeader();
		for(int bundleNumber = 0; bundleNumber < axtFile.getNumberOfBundles(); bundleNumber++){
			axtFileArray.add(loadFile(axtFilePaths.get(bundleNumber)));
			axtFileBuffer.add(ByteBuffer.wrap(axtFileArray.get(bundleNumber)));
			interpretHeader(bundleNumber);
			try{
				checkMagicNumber(bundleNumber);
				checkSize(bundleNumber);
				
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		axtFile.init();
		this.loadApplication();
		
		
		methodNames = DebugInfoLoader.loadDebugInfo(cname);
		
	}
	
	public void loadApplication(){
		loadCti2Name();
		loadClassTable();
		loadMethodTable();
		loadStaticMethodTable();
		loadExceptionTable();
		loadImplementedInterfaces();
		loadInterfaceTable();
		loadConstantPool();
		loadBytecode();
		loadHandleTable();
		loadObjectHeap();
//		Printer printer = new Printer(); //entkommentieren um eingelesene Daten auszugeben
//		printer.printAXTFile(Start.getAXTFilePath().get(0) + "/prettyPrintReadIn");
	}
	
	public byte[] loadFile(String path){
		FileInputStream fileInput = null;
		ByteArrayOutputStream fileOutput = null;
		byte[] data = null;
		byte[] file= null;
		try{
			fileInput = new FileInputStream(path);
			fileOutput = new ByteArrayOutputStream();
			data = new byte[(int) Math.pow(2, 16)];
			int i = fileInput.read(data, 0, data.length);
			while (i != -1){
				fileOutput.write(data, 0, i);
				i = fileInput.read(data, 0, data.length);
			}
			fileOutput.flush();
			file = fileOutput.toByteArray();
			fileInput.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		return file;
	}
	
	public void interpretHeader(int bundleNumber){
		try{
			axtFileBuffer.get(bundleNumber).position(0);
			header.setMagicNumber(bundleNumber, axtFileBuffer.get(bundleNumber).getInt() & 0xFFFFFFFFL);
			header.setCti2NameOffset(bundleNumber, axtFileBuffer.get(bundleNumber).getInt() & 0xFFFFFFFFL);
			header.setClassTableOffset(bundleNumber, axtFileBuffer.get(bundleNumber).getInt() & 0xFFFFFFFFL);
			header.setMethodTableOffset(bundleNumber, axtFileBuffer.get(bundleNumber).getInt() & 0xFFFFFFFFL);
			header.setStaticMethodTableOffset(bundleNumber, axtFileBuffer.get(bundleNumber).getInt() & 0xFFFFFFFFL);
			header.setExceptionTableOffset(bundleNumber, axtFileBuffer.get(bundleNumber).getInt() & 0xFFFFFFFFL);
			header.setImplementedInterfacesOffset(bundleNumber, axtFileBuffer.get(bundleNumber).getInt() & 0xFFFFFFFFL);
			header.setInterfaceTableOffset(bundleNumber, axtFileBuffer.get(bundleNumber).getInt() & 0xFFFFFFFFL);
			header.setConstantPoolOffset(bundleNumber, axtFileBuffer.get(bundleNumber).getInt() & 0xFFFFFFFFL);
			header.setBytecodeOffset(bundleNumber, axtFileBuffer.get(bundleNumber).getInt() & 0xFFFFFFFFL);
			header.setObjectHeapOffset(bundleNumber, axtFileBuffer.get(bundleNumber).getInt() & 0xFFFFFFFFL);
			header.setHandleTableOffset(bundleNumber, axtFileBuffer.get(bundleNumber).getInt() & 0xFFFFFFFFL);
			header.setSize(bundleNumber, axtFileBuffer.get(bundleNumber).getInt() & 0xFFFFFFFFL);
			header.setMainMethod(bundleNumber, axtFileBuffer.get(bundleNumber).getInt() & 0xFFFFFFFFL);
			header.setInterfaceOffset(bundleNumber, axtFileBuffer.get(bundleNumber).getShort() & 0xFFFF);
			header.setArrayTypeOffset(bundleNumber, axtFileBuffer.get(bundleNumber).getShort() & 0xFFFF);
			header.setThreadAMTI(bundleNumber, axtFileBuffer.get(bundleNumber).getInt() & 0xFFFFFFFFL);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	//checks if loaded file has the correct magic number
	public boolean checkMagicNumber(int bundleNumber) throws WrongMagicNumberException{
		if(header.getMagicNumber(bundleNumber) != (AXTFile_const.MAGICNUMBER  & 0xFFFFFFFFL)){
			throw new WrongMagicNumberException(name, header.getMagicNumber(bundleNumber));
		}
		else{
//			if(axtTrace.active())
//				axtTrace.println("Correct magic number (" + Long.toHexString((AXTFile_const.MAGICNUMBER  & 0xFFFFFFFFL)).toUpperCase() + ")!");
			return true;
		}
	}
	
	public boolean checkSize(int bundleNumber) throws WrongSizeInHeaderException{
		if(header.getSize(bundleNumber) != axtFileArray.get(bundleNumber).length){
			throw new WrongSizeInHeaderException(name, header.getSize(bundleNumber), axtFileArray.get(bundleNumber).length);
		}
		else{
//			if(axtTrace.active())
//				axtTrace.println("Correct Size (" + header.getSize(bundleNumber) + " byte)!");
			return true;
		}
	}
	
	public void loadCti2Name(){
		try{
			int lastIndex = 0;
			long cti2NameSize;
			for(int bundleNumber = 0; bundleNumber < axtFile.getNumberOfBundles(); bundleNumber++){
				byteBufferToPosition(bundleNumber, header.getCti2NameOffset(bundleNumber));
				cti2NameSize = header.getClassTableOffset(bundleNumber) - header.getCti2NameOffset(bundleNumber);
				for(int i = 0; i < cti2NameSize; i++){
					axtFile.getTabSec().setCti2Name(lastIndex + i, axtFileBuffer.get(bundleNumber).get());
					if(i == cti2NameSize - 1){
						lastIndex += i + 1;
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void loadClassTable(){
		try{
			int index = 0;
			long classTableClassesSize = 0, classTableInterfacesSize = 0, classTableArraysSize = 0;
			for(int bundleNumber = 0; bundleNumber < axtFile.getNumberOfBundles(); bundleNumber++){
				byteBufferToPosition(bundleNumber, header.getClassTableOffset(bundleNumber));
				classTableClassesSize = (header.getInterfaceOffset(bundleNumber)) * AXTFile_const.CLASSTABLEENTRYSIZE;
				while(index < classTableClassesSize){
					axtFile.getTabSec().setClassTable(index, axtFileBuffer.get(bundleNumber).get());
					index++;
				}
			}
			axtFile.getHeader().setNumberOfClasses(classTableClassesSize / AXTFile_const.CLASSTABLEENTRYSIZE);
			header.setClassTableInterfaceOffset(index / AXTFile_const.CLASSTABLEENTRYSIZE);
			for(int bundleNumber = 0; bundleNumber < axtFile.getNumberOfBundles(); bundleNumber++){
				byteBufferToPosition(bundleNumber, header.getClassTableOffset(bundleNumber) + (header.getInterfaceOffset(bundleNumber) * AXTFile_const.CLASSTABLEENTRYSIZE));
				classTableInterfacesSize = (header.getArrayTypeOffset(bundleNumber) - header.getInterfaceOffset(bundleNumber)) * AXTFile_const.CLASSTABLEENTRYSIZE;
				while(index < classTableInterfacesSize + classTableClassesSize){
					axtFile.getTabSec().setClassTable(index, axtFileBuffer.get(bundleNumber).get());
					index++;
				}
			}
			header.setClassTableArrayTypeOffset(index / AXTFile_const.CLASSTABLEENTRYSIZE);
			for(int bundleNumber = 0; bundleNumber < axtFile.getNumberOfBundles(); bundleNumber++){
				byteBufferToPosition(bundleNumber, header.getClassTableOffset(bundleNumber) + (header.getArrayTypeOffset(bundleNumber) * AXTFile_const.CLASSTABLEENTRYSIZE));
				classTableArraysSize = header.getMethodTableOffset(bundleNumber)  - header.getClassTableOffset(bundleNumber) - (header.getArrayTypeOffset(bundleNumber) * AXTFile_const.CLASSTABLEENTRYSIZE);
				while(index < classTableArraysSize + classTableClassesSize + classTableInterfacesSize){
					axtFile.getTabSec().setClassTable(index, axtFileBuffer.get(bundleNumber).get());
					index++;
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void loadMethodTable(){
		try{
			int lastIndex = 0;
			long methodTableSize = 0;
			for(int bundleNumber = 0; bundleNumber < axtFile.getNumberOfBundles(); bundleNumber++){
				byteBufferToPosition(bundleNumber, header.getMethodTableOffset(bundleNumber));
				methodTableSize = header.getStaticMethodTableOffset(bundleNumber) - header.getMethodTableOffset(bundleNumber);
				for(int i = 0; i < methodTableSize; i++){
					axtFile.getTabSec().setMethodTable(lastIndex + i, axtFileBuffer.get(bundleNumber).get());
					if(i == methodTableSize - 1){
						lastIndex += i + 1;
					}
				}
			}
			axtFile.getHeader().setNumberOfMethods(methodTableSize / AXTFile_const.METHODTABLEENTRYSIZE);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void loadStaticMethodTable(){
		try{
			int lastIndex = 0;
			long staticMethodTableSize = 0;
			for(int bundleNumber = 0; bundleNumber < axtFile.getNumberOfBundles(); bundleNumber++){
				byteBufferToPosition(bundleNumber, header.getStaticMethodTableOffset(bundleNumber));
				staticMethodTableSize = header.getExceptionTableOffset(bundleNumber) - header.getStaticMethodTableOffset(bundleNumber);
				for(int i = 0; i < staticMethodTableSize; i++){
					axtFile.getTabSec().setStaticMethodTable(lastIndex + i, axtFileBuffer.get(bundleNumber).get());
					if(i == staticMethodTableSize - 1){
						lastIndex += i + 1;
					}
				}
			}
			axtFile.getHeader().setNumberOfStaticMethods(staticMethodTableSize / AXTFile_const.STATICMETHODTABLEENTRYSIZE);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void loadExceptionTable(){
		try{
			int lastIndex = 0;
			long exceptionTableSize;
			for(int bundleNumber = 0; bundleNumber < axtFile.getNumberOfBundles(); bundleNumber++){
				byteBufferToPosition(bundleNumber, header.getExceptionTableOffset(bundleNumber));
				exceptionTableSize = header.getImplementedInterfacesOffset(bundleNumber) - header.getExceptionTableOffset(bundleNumber);
				for(int i = 0; i < exceptionTableSize; i++){
					axtFile.getTabSec().setExceptionTable(lastIndex + i, axtFileBuffer.get(bundleNumber).get());
					if(i == exceptionTableSize - 1){
						lastIndex += i + 1;
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void loadImplementedInterfaces(){
		try{
			int lastIndex = 0;
			long implementedInterfacesSize;
			for(int bundleNumber = 0; bundleNumber < axtFile.getNumberOfBundles(); bundleNumber++){
				byteBufferToPosition(bundleNumber, header.getImplementedInterfacesOffset(bundleNumber));
				implementedInterfacesSize = header.getInterfaceTableOffset(bundleNumber) - header.getImplementedInterfacesOffset(bundleNumber);
				for(int i = 0; i < implementedInterfacesSize; i++){
					axtFile.getTabSec().setImplementedInterface(lastIndex + i, axtFileBuffer.get(bundleNumber).get());
					if(i == implementedInterfacesSize - 1){
						lastIndex += i + 1;
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void loadInterfaceTable(){
		try{
			int lastIndex = 0;
			long interfaceTableSize;
			for(int bundleNumber = 0; bundleNumber < axtFile.getNumberOfBundles(); bundleNumber++){
				byteBufferToPosition(bundleNumber, header.getInterfaceTableOffset(bundleNumber));
				interfaceTableSize = header.getConstantPoolOffset(bundleNumber) - header.getInterfaceTableOffset(bundleNumber);
				for(int i = 0; i < interfaceTableSize; i++){
					axtFile.getTabSec().setInterfaceTable(lastIndex + i, axtFileBuffer.get(bundleNumber).get());
					if(i == interfaceTableSize - 1){
						lastIndex += i + 1;
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void loadConstantPool(){
		try{
			int lastIndex = 0;
			long constantPoolSize;
			for(int bundleNumber = 0; bundleNumber < axtFile.getNumberOfBundles(); bundleNumber++){
				byteBufferToPosition(bundleNumber, header.getConstantPoolOffset(bundleNumber));
				constantPoolSize = header.getBytecodeOffset(bundleNumber) - header.getConstantPoolOffset(bundleNumber);
				for(int i = 0; i < constantPoolSize; i++){
					axtFile.getDataSec().setConstantPool(lastIndex + i, axtFileBuffer.get(bundleNumber).get());
					if(i == constantPoolSize - 1){
						lastIndex += i + 1;
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void loadBytecode(){
		try{
			int lastIndex = 0;
			long bytecodeSize;
			byte[] temp = axtFile.getDataSec().getBytecode();
			for(int bundleNumber = 0; bundleNumber < axtFile.getNumberOfBundles(); bundleNumber++){
				byteBufferToPosition(bundleNumber, header.getBytecodeOffset(bundleNumber));
				bytecodeSize = header.getObjectHeapOffset(bundleNumber) - header.getBytecodeOffset(bundleNumber);
				for(int i = 0; i < bytecodeSize; i++){
					temp[lastIndex + i] = axtFileBuffer.get(bundleNumber).get();
					if(i == bytecodeSize - 1){
						lastIndex += i + 1;
					}
				}
			}
			axtFile.getDataSec().setBytecodeComplete(WordSwapper.swapWords(temp, 0, temp.length));
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void loadObjectHeap(){
		try{
			int lastIndex = 0;
			long objectHeapSize;
			for(int bundleNumber = 0; bundleNumber < axtFile.getNumberOfBundles(); bundleNumber++){
				byteBufferToPosition(bundleNumber, header.getObjectHeapOffset(bundleNumber));
				objectHeapSize = header.getHandleTableOffset(bundleNumber) - header.getObjectHeapOffset(bundleNumber);
				for(int i = 0; i < objectHeapSize; i++){
					axtFile.getDataSec().setObjectHeap(lastIndex + i, axtFileBuffer.get(bundleNumber).get());
					if(i == objectHeapSize - 1){
						lastIndex += i + 1;
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void loadHandleTable(){
		try{
			int lastIndex = 0;
			long handleTableSize;
			for(int bundleNumber = 0; bundleNumber < axtFile.getNumberOfBundles(); bundleNumber++){
				byteBufferToPosition(bundleNumber, header.getHandleTableOffset(bundleNumber));
				handleTableSize = axtFileArray.get(bundleNumber).length - header.getHandleTableOffset(bundleNumber);
				for(int i = 0; i < handleTableSize; i++){
					axtFile.getDataSec().setHandleTable(lastIndex + i, axtFileBuffer.get(bundleNumber).get());
					if(i == handleTableSize - 1){
						lastIndex += i + 1;
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
//	public void fillHeap(ObjectHeap heap){
//		//ObjectHeap Data
//		byte[] objHeapByte = axtFile.getDataSec().getObjectHeap();
//		int objHeapSize = objHeapByte.length;
//		int[] objHeapInt = new int[(objHeapSize + 3) / 4];
//		for(int i = 0; i < objHeapSize / 4; i++){
//			objHeapInt[i] = (int) AXTFile.byteArrayToLong(objHeapByte, i * 4, 4);
//		}
//		heap.fillHeap(objHeapInt);
//		//ObjectHeap Handles
//		byte[] handleTable = axtFile.getDataSec().getHandleTable();
////		boolean isArray = false;
////		boolean isPrimitive = false;
//		for(int i = 0; i < handleTable.length / AXTFile_const.HANDLETABLEENTRYSIZE; i++){
//			int flags = axtFile.getDataSec().getHandleFlags(i);
////			isArray = (flags & AXTFile_const.HANDLEARRAY) != 0;
////			isPrimitive = (flags & AXTFile_const.HANDLEPRIMITIVE) != 0;
////			if(i == 1 && (flags & AXTFile_const.STATICFIELD) == 0){
////				axtTrace.println("Wrong Flag set!");
////				return;
////			}
//			heap.registerHandle(i, axtFile.getDataSec().getMID(i), flags, axtFile.getDataSec().getClassTableIndex(i), axtFile.getDataSec().getRefObjectSize(i), axtFile.getDataSec().getAbsoluteReference(i));
//		}
////		heap.printHandles();
//	}
	
	private void byteBufferToPosition(int bundleNumber, long position){
		ByteBuffer buffer = axtFileBuffer.get(bundleNumber);
		if(position > 2147483647){
			long rest = position - 2147483647;
			buffer.position(2147483647);
			while(rest != 0){
				buffer.get();
				rest--;
			}
		}
		else{
			buffer.position((int) position);
		}
	}
	
	public ArrayList<String> defineLoadingPath(String name, ArrayList<String> list){
		ArrayList<String> result = new ArrayList<String>();
		result.add(list.get(0) + "/" + name + ".axt");
		for(int i = 1; i < list.size(); i++){ //muss noch entsprechend verändert werden, wenn es mehrere Bündel gibt
			result.add(list.get(i) + ".axt");
		}
		return result;
	}
	
	public String nameByPath(){
		String result = "";
		for(int i = axtFilePaths.get(0).length() - 1; i > 0; i--){
			if(axtFilePaths.get(0).charAt(i) == '/'){
				result = axtFilePaths.get(0).substring(i + 1, axtFilePaths.get(0).length() - 4);
				break;
			}
		}
		return result;
	}
	
	public AXTFile getAxtFile(){
		return axtFile;
	}
	
	public String [] getMethodNames() {
		return methodNames;
	}
	
}
