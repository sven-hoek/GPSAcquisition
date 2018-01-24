package functionalunit.cache;

/**
 * HandleTableEntry
 * Used in HandleTableCache and Memory
 * @author Patrick Appenheimer
 *
 */
public class HTEntry{	
	
	private int tag;	//Cache only
	private long addr;
	private long size;
	private int cti;
	private int flags;
	private int mid;
	private int validBit;	//Cache only
		
	
	public HTEntry(){
		tag = 0;
		addr = 0;
		size = 0;
		cti = 0;
		flags = 0;
		mid = 0;
		validBit = 0;
	}
	
	public int getTag(){
		return this.tag;
	}
	
	public void setTag(int tag){
		this.tag = tag;
	}
	
	public int getAddr(){
		return (int)this.addr;
	}
	
	public void setAddr(long addr){
		this.addr = addr;
	}
	
	public int getSize(){
		return (int)this.size;
	}
	
	public void setSize(long size){
		this.size = size;
	}
	
	public int getCTI(){
		return this.cti;
	}
	
	public void setCTI(int cti){
		this.cti = cti;
	}
	
	public int getFlags(){
		return this.flags;
	}
	
	public void setFlags(int flags){
		this.flags = flags;
	}
	
	public int getMID(){
		return this.mid;
	}
	
	public void setMID(int mid){
		this.mid = mid;
	}
	
	public int getValidBit(){
		return this.validBit;
	}
	
	public void setValidBit(int vb){
		this.validBit = vb;
	}
}
