package amidar.axtLoader;

public class AXTFile {
	
	
	
	//methodTableIndex = the index of the methodTableEntry in the class it belongs to
	//methodTableNumber = the index of the methodTableEntry in methodTable
	
	//32 Bit Werte werden als long gespeichert um Vorzeichenfehlern vorzubeugen
	//16 Bit Werte werden als int gespeichert um Vorzeichenfehlern vorzubeugen

	
	private int numberOfBundles;
	
	AXTHeader header;
	AXTTableSection tabSec;
	AXTDataSection dataSec;
	
	public AXTFile(int numberOfBundles){
		this.numberOfBundles = numberOfBundles;
		
		header = new AXTHeader(numberOfBundles);
		tabSec = new AXTTableSection(header, numberOfBundles);
		dataSec = new AXTDataSection(header, tabSec, numberOfBundles);
	}
	
	public void init(){
		header.init();
		tabSec.init();
		dataSec.init();
	}
	
	//invokes a new instance of AXTFile, if it does not already exists //TODO shitty - why is this like that??
//	public static void invokeInstance(int numberOfBundles){
//		try{
//			if (AXTFile.instance != null){
//				throw new NoInstanceOfAXTFileExistingException();
//			}
//			AXTFile.instance = new AXTFile(numberOfBundles);
//		}catch(Exception e){
//			e.printStackTrace();
//		}
//	}
//	
//	public static AXTFile getInstance(){
//		try{
//			if (AXTFile.instance == null){
//				throw new NoInstanceOfAXTFileExistingException();
//			}
//			return AXTFile.instance;
//		}catch(Exception e){
//			e.printStackTrace();
//			return null;
//		}
//	}
	
	public AXTHeader getHeader(){
		return header;
	}
	
	public AXTTableSection getTabSec(){
		return tabSec;
	}
	
	public AXTDataSection getDataSec(){
		return dataSec;
	}
	
	public int getNumberOfBundles(){
		return numberOfBundles;
	}
	
	//transforms a part of the size 'size' of the array beginning from startIndex (included) into an long
	public static long byteArrayToLong(byte[] array, int startIndex, int size){
		byte[] temp = new byte[size];
		for(int i = 0; i < size; i++){
			temp[i] = array[startIndex + i];
		}
		return byteArrayToLong(temp);
	}
	
	//transforms the complete array into an long
	public static long byteArrayToLong(byte[] array){
		long result = 0;
		for(int i = 0; i < array.length; i++){
			result = (long) (result << 8) + (array[i] & 0xFF);
		}
		return result;
	}
	
	//transforms given long into an byte[]
	public static byte[] longToByteArray(long value){
		byte[] result = new byte[8];
		for(int i = 0;  i < 8; i++){
			result[i] = (byte) (value >> (8 * (7 - i)) & 0xFF);
		}
		return result;
	}
	

}
