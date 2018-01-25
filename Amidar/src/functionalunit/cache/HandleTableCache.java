package functionalunit.cache;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import exceptions.AmidarSimulatorException;
import functionalunit.ObjectHeap;
import functionalunit.cache.Cache.MOESIState;

/**
 * Handle table cache
 * @author Patrick Appenheimer
 *
 */
public class HandleTableCache{	

	private int CACHESIZE;
	private int SETS;
	private int WORDSPERLINE;
	private int BYTESPERWORD;
	private int CACHELINES;
	
	private boolean wrAlloc;	//TODO
	private boolean wrBack;		//TODO
	
	private int extMemAcc;
	
	private Memory memory;
	private HTEntry[][] cache;
	private int[] data;
	private int[] plru;
	
//	private Cache dataCache;
		
	public HandleTableCache( Memory memory, String configFile){
		this.configureCache(configFile);
//		this.dataCache = dataCache;
		this.memory = memory;
		cache = new HTEntry[CACHELINES][SETS];
		data = new int[5];
		plru = new int[CACHELINES];
		this.createCacheLines();
//		System.out.println(this + " CREATED");
	}
	
	private void configureCache(String configFile){
		if(configFile == null) System.err.println("No Config File");
		JSONParser parser = new JSONParser();
		FileReader fileReader;
		JSONObject json = null;
		try {
			fileReader = new FileReader(configFile);
			json = (JSONObject) parser.parse(fileReader);
			String htCacheConfig = (String) json.get("htCacheConfig");
			if(htCacheConfig == null) System.err.println("No Handle Table Cache Config File");
			fileReader = new FileReader(htCacheConfig);
			json = (JSONObject) parser.parse(fileReader);
		} catch (FileNotFoundException e) {
			System.err.println("No Config File found");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Error while reading config file");
			e.printStackTrace();
		} catch (ParseException e) {
			System.err.println("Error while reading config file");
			e.printStackTrace();
		}
		
		long size = (long) json.get("size");
		CACHESIZE = ((int) size) * 1024;
		long sets = (long) json.get("sets");
		SETS = (int) sets;
		long wordsperline = (long) json.get("wordsperline");
		WORDSPERLINE = (int) wordsperline;
		BYTESPERWORD = 4;
		CACHELINES = (CACHESIZE/SETS)/(BYTESPERWORD*WORDSPERLINE);
		wrAlloc = (boolean) json.get("wrAlloc");
		wrBack = (boolean) json.get("wrBack");
		long extMem = (long) json.get("extMemoryAccTicks");
		extMemAcc = (int) extMem; 
	}
	
	private void createCacheLines(){
		for(int i = 0; i < SETS; i++){
			for(int j = 0; j < CACHELINES; j++){
				cache[j][i] = new HTEntry();
			}
		}
	}
	
	int lllhandle = -1;
	
	
	public boolean holdsValue(int handle){
		int index = handle & 0x1FF;
		int tag = handle >>> 9;
		for(int i=0; i<SETS; i++){
			if(cache[index][i].getTag() == tag && cache[index][i].getValidBit() == 1){
				return true;
			}
		}
		return false;
		
	}

	public int requestData(int handle) {
		handle = handle & 0xFFFFF;
		lllhandle = handle;
		if(memory.getHandles() <= handle) throw new AmidarSimulatorException("Handle doesn't exist. Registered handles: " + memory.getHandles() + " Requested handle: " + handle);
		int index = handle & 0x1FF;
		int tag = handle >>> 9;
		for(int i=0; i<SETS; i++){
			if(cache[index][i].getTag() == tag && cache[index][i].getValidBit() == 1){
				data[0] = cache[index][i].getAddr();
				data[1] = cache[index][i].getSize();
				data[2] = cache[index][i].getCTI();
				data[3] = cache[index][i].getFlags();
				data[4] = cache[index][i].getMID();
				setPLRU(index, i);
				return 0;
			}
		}
		int replaceInSet = decisionPLRU(index);
		cache[index][replaceInSet].setTag(tag);
		
//		if(cache[index][replaceInSet].getValidBit()==1){
//			System.out.println("REEEEEEEEEEEEEEEEEEEEEEePLACE");
//		}
		cache[index][replaceInSet].setValidBit(1);
		cache[index][replaceInSet].setAddr(memory.getAddrHT(handle));
		cache[index][replaceInSet].setSize(memory.getSizeHT(handle));
		cache[index][replaceInSet].setCTI(memory.getCTIandFlagsHT(handle) >>> 16);
		cache[index][replaceInSet].setFlags(memory.getCTIandFlagsHT(handle) & 0x0000FFFF);
		cache[index][replaceInSet].setMID(memory.getMidHT(handle));
		setPLRU(index, replaceInSet);
		data[0] = cache[index][replaceInSet].getAddr();
		data[1] = cache[index][replaceInSet].getSize();
		data[2] = cache[index][replaceInSet].getCTI();
		data[3] = cache[index][replaceInSet].getFlags();
		data[4] = cache[index][replaceInSet].getMID();
		return (extMemAcc+4+4)-1;
	}
	
	public int writeData(int handle, long addr, long size, int cti, int flags, int mid){
		handle = handle & 0xFFFFF;
		int index = handle & 0x1FF;
		int tag = handle >>> 9;
//		dataCache.notifyHTCaches(index, tag);
		for(int i=0; i<SETS; i++){
			if(getTag(index, i) == tag && getVB(index, i) == 1){
				cache[index][i].setAddr(addr);
				cache[index][i].setSize(size);
				cache[index][i].setCTI(cti);
				cache[index][i].setFlags(flags);
				cache[index][i].setMID(mid);
				setPLRU(index, i);
				memory.registerHandle(handle, mid, flags, cti, size, addr);
				
				return 0;//(extMemAcc+6+4)-1;
			}
		}
		memory.registerHandle(handle, mid, flags, cti, size, addr);
		return (extMemAcc+6+4)-1;
	}

	public int setFlags(int handle, int flags){
		int index = handle & 0x1FF;
		int tag = handle >>> 9;
		for(int i=0; i<SETS; i++){
			if(getTag(index, i) == tag && getVB(index, i) == 1){
				cache[index][i].setFlags(flags);
				setPLRU(index, i);
				memory.setFlags(handle, flags);
				return (extMemAcc+6+1)-1;
			}
		}
		memory.setFlags(handle, flags);
		return (extMemAcc+6+1)-1;
	}
	
	public int setMID(int handle, int mid){
		int index = handle & 0x1FF;
		int tag = handle >>> 9;
		for(int i=0; i<SETS; i++){
			if(getTag(index, i) == tag && getVB(index, i) == 1){
				cache[index][i].setMID(mid);
				setPLRU(index, i);
				memory.setMID(handle, mid);
				return (extMemAcc+6+1)-1;
			}
		}
		memory.setMID(handle, mid);
		return (extMemAcc+6+1)-1;
	}
	
	public int[] getData() {
		return data;
	}
	
	public int getAddr(){
		return data[0];
	}
	
	public int getSize(){
		return data[1];
	}
	
	public int getCTI(){
		return data[2];
	}
	
	public int getFlags(){
		return data[3];
	}
	
	public int getMID(){
		return data[4];
	}
	
	private int getTag(int index, int set){
		return cache[index][set].getTag();
	}
	
	private int getVB(int index, int set){
		return cache[index][set].getValidBit();
	}

	public boolean isReady() {
		return true;
	}

	public void invalidate() {

	}
	
	private void setPLRU(int index, int set){
		switch(set){
		case 0:
			plru[index] = 0;
			break;
		case 1:
			plru[index] = 1;
			break;
		default:		
		}
	}
	
	private int decisionPLRU(int index){
		switch(plru[index]){
		case 0:
			return 1;
		case 1:
			return 0;
		default:
			return 99;
		}
	}
	
	public void writeNotification(int index, int tag){
		for(int i = 0; i<SETS; i++){
			if(cache[index][i].getTag() == tag && cache[index][i].getValidBit() == 1){
				cache[index][i].setValidBit(0);	
			}
		}
	}

	public void invalidateFlush() {
		for(int line = 0; line < CACHELINES; line++){
			for(int set = 0; set < SETS; set++){
				HTEntry entry = cache[line][set];
				entry.setValidBit(0);
			}
		}
		
	}
	
	
	public int arrayLengthFAKE(int handle){
		return memory.getSizeHT(handle);
	}
	
	public int addressFAKE(int handle){
		return memory.getAddrHT(handle);
	}

}
