package functionalunit.cache;

/**
 * ObjectCacheLine
 * @author Patrick Appenheimer
 *
 */
public class ObjCacheLine{	
	
	private long tag;
	private int validBit;
	private int modBit;
	private int maxOffset;
	private int selMask;
	
	private int[] data;
	
	
	public ObjCacheLine(int datawordsPerLine){
		data = new int[datawordsPerLine];
		tag = 0;
		validBit = 0;
		modBit = 0;
		maxOffset = 0;
		selMask = 0;
	}
	
	public void setOverhead(long tag, int validBit, int modBit, int maxOffset, int selMask){
		this.tag = tag;
		this.validBit = validBit;
		this.modBit = modBit;
		this.maxOffset = maxOffset;
		this.selMask = selMask;
	}
	
	public int getData(int offset){
		return data[offset];
	}
	
	public void setData(int offset, int data){
		this.data[offset] = data;
	}
	
	public long getTag(){
		return this.tag;
	}
	
	public int getValidBit(){
		return this.validBit;
	}
	
	public int getModBit(){
		return this.modBit;
	}
	
	public int getMaxOffset(){
		return this.maxOffset;
	}
	
	public int getSelMask(){
		return this.selMask;
	}
	

}
