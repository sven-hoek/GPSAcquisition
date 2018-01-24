package functionalunit.cache;

import functionalunit.cache.Cache.MOESIState;

/**
 * ObjectCacheLine
 * @author Patrick Appenheimer
 *
 */
public class CacheLine{	
	
	private long tag;
	private int validBit;
	private int modBit;
	private int maxOffset;
	private int selMask;
	private MOESIState moesiState;
	
	private int[] data;
	
	public CacheLine(int datawordsPerLine){
		data = new int[datawordsPerLine];
		tag = 0;
		validBit = 0;
		modBit = 0;
		maxOffset = 0;
		selMask = 0;
		moesiState = MOESIState.INVALID;
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
	
	public void setMaxOffset(int maxOff){
		this.maxOffset = maxOff;
	}
	
	public int getSelMask(){
		return this.selMask;
	}
	
	public MOESIState getMoesiState(){
		return moesiState;
	}
	
	public void setMoesiState(MOESIState moesi){
		moesiState = moesi;
	}
	
	public void printData(){
		System.out.print("Data in "+this+": ===> ");
		for(int i = 0; i<data.length; i++){
			System.out.print("["+data[i]+"]");
		}
		System.out.println(" <=== Tag="+tag);
	}
	
	public CacheLine getClone(){
		CacheLine clone = new CacheLine(data.length);
		
		clone.tag = this.tag;
		clone.validBit = this.validBit;
		clone.modBit = this.modBit;
		clone.maxOffset = this.maxOffset;
		clone.selMask = this.selMask;
		clone.moesiState = this.moesiState;
		for(int i = 0; i < data.length; i++){
			clone.setData(i, this.getData(i));
		}
		
		return clone;
	}

}
