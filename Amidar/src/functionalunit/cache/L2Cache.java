package functionalunit.cache;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import tracer.Trace;
import tracer.TraceManager;

public abstract class L2Cache {
	
	protected int SINGLE_VALUE_TIME = 1;
	protected int RAM_ACCESS_TIME  = 40;
	
	protected int CACHESIZE;
	protected int SETS;
	protected int WORDSPERLINE;
	protected int BYTESPERWORD;
	protected int CACHELINES;
	
	protected final int boBits;
	protected final int indexBits;
	
	protected long readAccesses;
	protected long readMisses;
	
	protected long writeAccesses;
	protected long writeMisses;
	
	protected int [] memory;
	protected long [][] tagMemory;
	protected boolean [][] valid;
	protected boolean [][] dirty;
	protected int[] plru;
	
	protected int index, blockOffset;
	protected long tag;
	
//	protected int globalWaitTime = 0;
	
	TraceManager traceManager;
	
	public L2Cache(Memory memory, String configFile, TraceManager traceManager){
		
		
		configureCache(configFile);
		
		int bytesPerSet = CACHESIZE / SETS;
		int bytesPerLine = WORDSPERLINE * BYTESPERWORD;
		
		
		
		this.CACHELINES = bytesPerSet/bytesPerLine;
		
		boBits = (int)Math.ceil(Math.log(WORDSPERLINE)/Math.log(2));
		indexBits = (int)Math.ceil(Math.log(CACHELINES)/Math.log(2));
		
		this.memory = memory.memory;
		tagMemory = new long [SETS][CACHELINES];
		valid = new boolean [SETS][CACHELINES];
		dirty = new boolean [SETS][CACHELINES];
		plru = new int[CACHELINES];
		
		this.traceManager = traceManager;
		

	}
	
	
	private void configureCache(String configFile){
		if(configFile == null) System.err.println("No Config File");
		JSONParser parser = new JSONParser();
		FileReader fileReader;
		JSONObject json = null;
		try {
			fileReader = new FileReader(configFile);
			json = (JSONObject) parser.parse(fileReader);
			String cacheConfig = (String) json.get("L2CacheConfig");
			if(cacheConfig == null) System.err.println("No Cache Config File");
			fileReader = new FileReader(cacheConfig);
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
		
//		System.out.println("WORDSP.LINE: " + WORDSPERLINE);
//		System.out.println("SETS: " + SETS);
//		System.out.println("CACHELINES: " + CACHELINES);
//		System.out.println("CACHESIZE: " + CACHESIZE);
//		System.out.println("----------------------------------");
		
		long extMem = (long) json.get("extMemoryAccTicks");
		RAM_ACCESS_TIME = (int) extMem;
	}
	
	public void initMemory(int[] memory){
		this.memory = memory;
	}
	
	protected int findSet(){
		for(int i = 0; i < SETS; i++){
			if(tagMemory[i][index] == tag && valid[i][index]){
				// HIT
				return i;
			}
		}
		return -1;
	}
	
	protected int decisionPLRU(int index){
		switch(SETS){
		case 1:
			return 0;
		case 2:
			return decisionLRU2(index);
		case 4:
			return decisionPLRU4(index);
		case 8:
			return decisionPLRU8(index);
		default:
			return 99;
		}
	}
	
	
	protected int decisionPLRU8(int index){
		if(!valid[0][index]) return 0;
		if(!valid[1][index]) return 1;
		if(!valid[2][index]) return 2;
		if(!valid[3][index]) return 3;
		if(!valid[4][index]) return 4;
		if(!valid[5][index]) return 5;
		if(!valid[6][index]) return 6;
		if(!valid[7][index]) return 7;
		
		switch(plru[index]){
		case 0:
		case 1:
		case 4:
		case 5:
			return 0;
		case 2:
		case 3:
		case 6:
		case 7:
			return 1;
		case 8:
		case 10:
		case 12:
		case 14:
			return 2;
		case 9:
		case 11:
		case 13:
		case 15:
			return 3;
		case 16:
		case 17:
		case 24:
		case 25:
			return 4;
		case 18:
		case 19:
		case 26:
		case 27:
			return 5;
		case 20:
		case 22:
		case 28:
		case 30:
			return 6;
		case 21:
		case 23:
		case 29:
		case 31:
			return 7;
		default:
			return 99;
		}
	
	}
	
	protected int decisionPLRU4(int index){
		if(!valid[0][index]) return 0;
		if(!valid[1][index]) return 1;
		if(!valid[2][index]) return 2;
		if(!valid[3][index]) return 3;
		
		
		
		switch(plru[index]){
		case 0:
		case 1:
			return 0;
		case 2:
		case 3:
			return 1;
		case 4:
		case 6:
			return 2;
		case 5:
		case 7:
			return 3;
		default:
			return 99;
		}
	}

	
	protected int decisionLRU2(int index){
		if(!valid[0][index]) return 0;
		if(!valid[1][index]) return 1;
		
		return plru[index];
		
	
	}
	
	protected void setPLRU(int index, int set){
		switch(SETS){
		case 1:
			setPLRU1(index, set);
			break;
		case 2:
			setLRU2(index, set);
			break;
		case 4:
			setPLRU4(index, set);
			break;
		case 8:
			setPLRU8(index, set);
			break;
		default:
		}
	}
	
	protected void setPLRU8(int index, int set){
		switch(set){
		case 0:
			
			plru[index] = plru[index] | 26;
			break;
		case 1:
			plru[index] = plru[index] | 24;
			plru[index] = plru[index] & 30;
			break;
		case 2:
			plru[index] = plru[index] | 17;
			plru[index] = plru[index] & 23;
			break;
		case 3:
			plru[index] = plru[index] | 16;
			plru[index] = plru[index] & 22;
		case 4:
			plru[index] = plru[index] | 6;
			plru[index] = plru[index] & 15;
			break;
		case 5:
			plru[index] = plru[index] | 4;
			plru[index] = plru[index] & 13;
			break;
		case 6:
			plru[index] = plru[index] | 1;
			plru[index] = plru[index] & 11;
			break;
		case 7:
			plru[index] = plru[index] & 10;
			break;
		default:		
		}
	}
	
	
	protected void setPLRU4(int index, int set){
		switch(set){
		case 0:
			
			plru[index] = plru[index] | 0x6;
			break;
		case 1:
			plru[index] = plru[index] | 0x4;
			plru[index] = plru[index] & 0x5;
			break;
		case 2:
			plru[index] = plru[index] | 0x1;
			plru[index] = plru[index] & 0x3;
			break;
		case 3:
			plru[index] = plru[index] & 0x2;
			break;
		default:		
		}
	}
	protected void setLRU2(int index, int set){
		switch(set){
		case 0:
			plru[index] = 1;
			break;
		case 1:
			plru[index] = 0;
			break;
		default:		
		}
	}
	
	protected void setPLRU1(int index, int set){
		//NOTHING TODO HERE
	}
	
	public void tick(){
//		if(globalWaitTime > 0){
//			globalWaitTime--;
//		}
	}
	
	public double getReadMissRate(){
		return readMisses/(double)readAccesses;
	}
	
	public double getWriteMissRate(){
		return writeMisses/(double)writeAccesses;
	}
	
	
	public double usage(){
		double invalidNr=0, validNr=0;
		
		for(int set = 0; set < SETS; set++){
			for(int index = 0; index < CACHELINES; index++){
				boolean val = valid[set][index];
				if(!val){
					invalidNr++;
				} else {
					validNr++;
				}
			}
		}
		
		
		return (validNr/(invalidNr+validNr));
	}
	
	
	public void printStatistics(){
		Trace cacheTrace = traceManager.getf("caches");
		cacheTrace.setPrefix(" L2 cache ");
		cacheTrace.printTableHeader("L2 Cache:");
		
		cacheTrace.println("Read Missrate:\t\t\t     "+getReadMissRate()*100+"%");
		cacheTrace.println("Write Missrate:\t\t\t     "+getWriteMissRate()*100+"%");
	}
	
	public void invalidateFlush(){
		
		for(int line = 0; line < CACHELINES; line++){
			for(int set = 0; set < SETS; set++){
				
				valid[set][line] = false;
			}
		}
	}
	
	public int getRAMaccessTime() {
		return RAM_ACCESS_TIME;
	}
	
	public int getWordsPerLine(){
		return WORDSPERLINE;
	}
	
	public int getLoadOverHead() {
		return 0; // Only in L2Physically htis is different from 0: Cacheline Alignment
	}
	
	public abstract int prefetchData(int handle, int offset, int physicalAddress);
	
	public abstract boolean holdsData(int handle, int offset, int physicalAddress);
	
	public abstract int getData(int handle, int offset, int physicalAddress);
	
	public abstract int writeData(int handle, int offset, int physicalAddress, int data);
	
	public abstract int requestData(int handle, int offset, int physicalAddress);

	public abstract void writeDataPhysical(int addr, int data);
	
	public abstract int readDataPhysical(int addr);
	
	public abstract boolean physicallyAddressed();




}
