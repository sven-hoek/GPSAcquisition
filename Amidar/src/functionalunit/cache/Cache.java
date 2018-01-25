package functionalunit.cache;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.TreeMap;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.sun.javafx.css.CalculatedValue;
import com.sun.xml.internal.fastinfoset.algorithm.BuiltInEncodingAlgorithm.WordListener;

import exceptions.AmidarSimulatorException;
import functionalunit.CGRA;
import functionalunit.ObjectHeap;
import functionalunit.cache.coherency.CoherenceController;
import functionalunit.cache.coherency.CoherenceController.PrefetchRequest;
import tracer.Trace;
import tracer.TraceManager;

/**
 * Datacache, currently used by Heap and CGRA
 * @author Patrick Appenheimer
 *
 */
public class Cache{	
	
	//For debugging purposes only
	//true:		tag = {handle,offset[31:3],000}
	//false:	old tag generation
	private static final boolean NEW_CACHE_ADDR = true;	
	
	private int CACHESIZE;
	private int SETS;
	private int WORDSPERLINE;
	private int BYTESPERWORD;
	private int CACHELINES;
	
	private boolean wrAlloc;	//TODO
	private boolean wrBack;		//TODO
	
	private int extMemAcc;
	
	private int[] plru;
	private CacheLine[][] cache; 
	
	private long tag;
	private int index;
	private int blockoffset;
	private boolean isLoong = false;
	
	private int handle;
	private int offset;
	
	private int data;
	
//	private ObjectHeap heap;
//	public Memory memory;
//	private HandleTableCache htCache;
	
	CoherenceController coherenceController;
	
	private int cacheID;
	public int getCacheID(){
		return cacheID;
	}
	private boolean isHeapCache;
			
	private boolean synthesis;
	
	private Cache[] moesiCaches;
//	private int getFromCache;
//	private int extCacheSet;
	private int returnSet;
	private int returnIndex;
	
	private int [] replaced;
	
	//======================================== TRACE =========================================
	//==== 0=totalRead / 1=totalWrite / 2=readMiss / 3=readHit / 4=writeMiss / 5=writeHit ====
	//====   6=fromCache[read] / 7=fromCache[write] / 8=fromMem[read] / 9=fromMem[write]  ====
	//====   10=tagButInvalid[read] / 11=tagButInvalid[write] / 12=cachelineUpdates       ====
	
	public static final int TOTAL_READ = 0;
	public static final int TOTAL_WRITE = 1;
	public static final int READ_MISS = 2;
	public static final int READ_HIT = 3;
	public static final int WRITE_MISS = 4;
	public static final int WRITE_HIT = 5;
	public static final int FROM_L1_READ = 6;
	public static final int FROM_L1_WRITE = 7;
	public static final int FROM_L2_READ = 8;
	public static final int FROM_L2_WRITE = 9;
	public static final int TAG_BUT_INVALID_READ  = 10;
	public static final int TAG_BUT_INVALID_WRITE = 11;
	public static final int CACHELINE_UPDATES = 12;
	
	private int[] statistics;
	
	private TraceManager traceManager;
	
	private PrefetchStrategy prefetchStrategy;

	//======= MOESI States =======
	public static enum MOESIState{
		INVALID,
		SHARED,
		EXCLUSIVE,
		OWNED,
		MODIFIED;
	}
	
	public Cache(String configFile, int cacheID, boolean synthesis, TraceManager traceManager, CoherenceController coherenceController){
		this.configureCache(configFile);
		this.plru = new int[CACHELINES];
		this.cache = new CacheLine[CACHELINES][SETS];
		this.replaced = new int[CACHELINES];
//		this.memory = memory;
//		htCache = new HandleTableCache(this, memory, configFile);
		this.createCacheLines();
		this.synthesis = synthesis;
		this.cacheID = cacheID;
		
		this.coherenceController = coherenceController;
		this.prefetchStrategy = coherenceController.getPrefetchStrategy();
		
		statistics = new int[13];
		this.traceManager = traceManager;
		if(cacheID==99){
			isHeapCache=true;
			this.cacheID=0;
		}
	}
	
	private void createCacheLines(){
		for(int i = 0; i < CACHELINES; i++){
			for(int j = 0; j < SETS; j++){
				cache[i][j] = new CacheLine(WORDSPERLINE);
			}
		}
	}
	
	private void configureCache(String configFile){
		if(configFile == null) System.err.println("No Config File");
		JSONParser parser = new JSONParser();
		FileReader fileReader;
		JSONObject json = null;
		try {
			fileReader = new FileReader(configFile);
			json = (JSONObject) parser.parse(fileReader);
			String cacheConfig = (String) json.get("CacheConfig");
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


		wrAlloc = (boolean) json.get("wrAlloc");
		wrBack = (boolean) json.get("wrBack");
		long extMem = (long) json.get("extMemoryAccTicks");
		extMemAcc = (int) extMem;
	}
	
	public int requestData(int handle, int offset) {
		int selMask = generateCacheAddr(handle, offset);
//		System.out.println("RDC " +  offset +"  ID: " + cacheID);
		statistics[TOTAL_READ]++;
		if(!isHeapCache){
			if(offset == Integer.MAX_VALUE){
//				htCache.requestData(handle);
//				data = htCache.getSize();
				int time = coherenceController.requestHandleTable(handle, false, this);	
				data = coherenceController.getArrayLength();
				return time;
			}
			if(offset == Integer.MAX_VALUE-1){
				int time = coherenceController.requestHandleTable(handle, false, this);	
				data = coherenceController.getCTI();
				return time;
			}
		}
//		int selMask = generateCacheAddr(handle, offset);
		if(selMask == 99) throw new AmidarSimulatorException("ObjCache.requestData() says: \"No valid selMask!\"");
		boolean foundTag = false;
		for(int i = 0; i<SETS; i++){
			if(cache[index][i].getTag() == tag && cache[index][i].getSelMask() == selMask){
				if(cache[index][i].getMoesiState() != MOESIState.INVALID){
					statistics[READ_HIT]++;
					data = cache[index][i].getData(blockoffset);
					setPLRU(index, i);
					if(!isHeapCache && prefetchStrategy == PrefetchStrategy.LINEAR && !holdsValue(handle, offset+WORDSPERLINE)){
						coherenceController.requestDataPrefetch(handle, offset+WORDSPERLINE, this); /// PREFETCHING
						resetPrefetchRepeatCounter();
					}
					
					Long taggg = ((long)handle)<<32;
					taggg += offset&0xFFFFFFF1;
					if(prefetchedFromL1.remove(taggg)){
						nrUsedPrefetchFromL1++;
					}
					if(prefetchedFromL2.remove(taggg)){
						nrUsedPrefetchFromL2++;
					}
//					System.out.println("HIT");
//					System.out.println("RDC " + offset + ": " + data);
//					System.out.println("RDC " +handle + " " + offset + ": " + data);
					return 0;
				}
				foundTag=true;
			}
		}
		if(foundTag) statistics[TAG_BUT_INVALID_READ]++;
		statistics[READ_MISS]++;
//		System.out.println("MISS");
		
		int replaceInSet = decisionPLRU(index);
		MOESIState tempMoesi = cache[index][replaceInSet].getMoesiState();
		int ticks = 0;
		
		CacheLine toWriteBack = null;
		
		
		if(tempMoesi == MOESIState.OWNED || tempMoesi == MOESIState.MODIFIED){
			toWriteBack = cache[index][replaceInSet].getClone();
		}
		
		
		
		ticks += coherenceController.requestData(handle, offset, this);
		CacheLine newCacheLine = coherenceController.getData();
		
		for(int i=0; i<WORDSPERLINE; i++){
			cache[index][replaceInSet].setData(i, newCacheLine.getData(i));
		}
		
		cache[index][replaceInSet].setOverhead(tag, 1, 0, newCacheLine.getMaxOffset(), selMask);
		
		MOESIState newState;
		
		if(coherenceController.fromL2Cache()){
			newState = MOESIState.EXCLUSIVE;
			statistics[FROM_L2_READ]++;
		} else {
			newState = MOESIState.SHARED;
			statistics[FROM_L1_READ]++;
		}
		
		
		cache[index][replaceInSet].setMoesiState(coherenceController.fromL2Cache()? MOESIState.EXCLUSIVE : MOESIState.SHARED);
		
		if(toWriteBack != null){
//			System.out.println("WRITEBACK");
			generateHandleOffset(toWriteBack.getTag(), index, 0, toWriteBack.getSelMask());
			coherenceController.writeBack(this.handle, this.offset, toWriteBack, false, this);
			Long taggg = ((long)this.handle)<<32;
			taggg += this.offset&0xFFFFFFF1;
			prefetchedFromL1.remove(taggg);
			prefetchedFromL2.remove(taggg);
		}
		
		data = cache[index][replaceInSet].getData(blockoffset);
		setPLRU(index, replaceInSet);
//		System.out.println("RDC " +handle + " " + offset + ": " + data);
		if(!isHeapCache && prefetchStrategy == PrefetchStrategy.LINEAR && !holdsValue(handle, offset+WORDSPERLINE)){
			coherenceController.requestDataPrefetch(handle, offset+WORDSPERLINE, this); /// PREFETCHING
			resetPrefetchRepeatCounter();
		}
		return ticks;
	}

	public int writeData(int handle, int offset, int data){
//		System.out.println("WRC " + handle + " " +  offset + ": " + data +" id: " + cacheID);
		statistics[TOTAL_WRITE]++;
		int selMask = generateCacheAddr(handle, offset);
		if(selMask == 99) throw new AmidarSimulatorException("ObjCache.writeData() says: \"No valid selMask!\"");
		boolean foundTag = false;
		for(int i = 0; i<SETS; i++){
			if(cache[index][i].getTag() == tag && cache[index][i].getSelMask() == selMask){
				MOESIState currMOESI = cache[index][i].getMoesiState();
				if(currMOESI != MOESIState.INVALID){
					statistics[WRITE_HIT]++;
					cache[index][i].setData(blockoffset, data);
					int oldMaxOffset = cache[index][i].getMaxOffset();
					if(oldMaxOffset > blockoffset){
						cache[index][i].setOverhead(tag, 1, 1, oldMaxOffset, selMask);
					} else {
						cache[index][i].setOverhead(tag, 1, 1, blockoffset, selMask);
					}
					setPLRU(index, i);							
					int tt = 0;
					if(coherenceController.updatingL2Cache()){
						tt += coherenceController.writeNotification(handle, offset, cache[index][i], this);
						if(coherenceController.isDataExclusive()){ 
							cache[index][i].setMoesiState(MOESIState.EXCLUSIVE);
						} else {
							cache[index][i].setMoesiState(MOESIState.SHARED);
						}
					} else {
						if(currMOESI == MOESIState.EXCLUSIVE){
							cache[index][i].setMoesiState(MOESIState.MODIFIED);
						}

						//					System.out.println("HIT");
						if(currMOESI == MOESIState.OWNED || currMOESI == MOESIState.SHARED){
							tt += coherenceController.writeNotification(handle, offset, cache[index][i], this);
							//						System.out.println("WRITENOTIFICATION");
//							if(coherenceController.isDataExclusive()){ 
//								cache[index][i].setMoesiState(MOESIState.MODIFIED);
//							} else {
								cache[index][i].setMoesiState(MOESIState.OWNED);
//							}

						}
					}
					if(!isHeapCache && prefetchStrategy == PrefetchStrategy.LINEAR && !holdsValue(handle, offset+WORDSPERLINE)){
						coherenceController.requestDataPrefetch(handle, offset+WORDSPERLINE, this); /// PREFETCHING
						resetPrefetchRepeatCounter();
					}
					
					Long taggg = ((long)handle)<<32;
					taggg += offset&0xFFFFFFF1;
					if(prefetchedFromL1.remove(taggg)){
						nrUsedPrefetchFromL1++;
					}
					if(prefetchedFromL2.remove(taggg)){
						nrUsedPrefetchFromL2++;
					}
					
					return tt;
				}
				foundTag=true;
			}
		}
		if(foundTag) statistics[TAG_BUT_INVALID_WRITE]++;
		statistics[WRITE_MISS]++;
		CGRA.writeMusage++;
		int ticks = 1;
		int replaceInSet = decisionPLRU(index);
//		if(replaceInSet == 99) throw new AmidarSimulatorException("Something went wrong with the PLRU decision");
//		System.out.println("MISS");
		
		MOESIState tempMoesi = cache[index][replaceInSet].getMoesiState();
		CacheLine toWriteBack = null;
		
		
		if(tempMoesi == MOESIState.OWNED || tempMoesi == MOESIState.MODIFIED){
			toWriteBack = cache[index][replaceInSet].getClone();
		}
		//MOESI:
		
		ticks += coherenceController.requestData(handle, offset, this);
		CacheLine newCacheLine = coherenceController.getData();
		
		for(int i=0; i<WORDSPERLINE; i++){
			cache[index][replaceInSet].setData(i, newCacheLine.getData(i));
		}
		
		
		int newMax = newCacheLine.getMaxOffset() > blockoffset? newCacheLine.getMaxOffset(): blockoffset;
		cache[index][replaceInSet].setOverhead(tag, 1, 1, newMax, selMask);
		
		
		cache[index][replaceInSet].setData(blockoffset, data);
		setPLRU(index, replaceInSet);
		
		MOESIState newState;
		if(coherenceController.updatingL2Cache()){
			if(!coherenceController.fromL2Cache()){
				statistics[FROM_L1_WRITE]++;
			} else {
				statistics[FROM_L2_WRITE]++;
			}
			
			coherenceController.writeNotification(handle, offset, cache[index][replaceInSet], this);
			
			if(coherenceController.isDataExclusive()){ 
				newState = MOESIState.EXCLUSIVE;
			} else {
				newState = MOESIState.SHARED;
			}
			
			
		}else{


			if(!coherenceController.fromL2Cache()){
				coherenceController.writeNotification(handle, offset, cache[index][replaceInSet], this);
				statistics[FROM_L1_WRITE]++;
			} else {
				statistics[FROM_L2_WRITE]++;
			}




			
			if(coherenceController.isDataExclusive()){ 
				newState = MOESIState.MODIFIED;
			} else {
				newState = MOESIState.OWNED;
			}
		}

		
		
		cache[index][replaceInSet].setMoesiState(newState);
		
		
		setPLRU(index, replaceInSet);
		
		
		if(toWriteBack != null){
//			System.out.println("WRITEBACK");
			generateHandleOffset(toWriteBack.getTag(), index, 0, toWriteBack.getSelMask());
			coherenceController.writeBack(this.handle, this.offset, toWriteBack, false, this);
			Long taggg = ((long)this.handle)<<32;
			taggg += this.offset&0xFFFFFFF1;
			prefetchedFromL1.remove(taggg);
			prefetchedFromL2.remove(taggg);
		}
		if(!isHeapCache && prefetchStrategy == PrefetchStrategy.LINEAR && !holdsValue(handle, offset+WORDSPERLINE)){
			
			coherenceController.requestDataPrefetch(handle, offset+WORDSPERLINE, this); /// PREFETCHING
			resetPrefetchRepeatCounter();
		}
		return ticks;
	}
	
	private int generateCacheAddr(int handle, int offset){
		
		
		int selMask = 99;
//		if(NEW_CACHE_ADDR) selMask = generateCacheAddrNew2(handle, offset);
//		if(!NEW_CACHE_ADDR) selMask = generateCacheAddrOld(handle, offset);
//		if(offset >=32){
//		if(coherenceController.isLooongArray(handle)){
//			selMask = 22; generateCacheAddrStatic2(handle, offset);
//		} else {
			selMask = 11; generateCacheAddrStatic(handle, offset);
//		}
		
//		selMask = generateCacheAddrOld(handle, offset);
		
		return selMask;
	}	
	
	private int generateCacheAddrStatic(int handle, int offset){
		int boBits = 3;
		if(WORDSPERLINE == 16){
			boBits = 4;
		} else if(WORDSPERLINE == 32){
			boBits = 5;
		} else if(WORDSPERLINE == 64){
			boBits = 6;
		} else if(WORDSPERLINE == 4){
			boBits = 2;
		}
		int boMask = (int)Math.pow(2, boBits)-1;
		blockoffset = offset & boMask;
		int indexBits = (int)(Math.log(CACHELINES)/Math.log(2));
		
		int offsetBits = 4;
		
//		index = ((offset>>boBits) & ~((-1)<<offsetBits)) + (handle<<offsetBits);
		index = ((offset>>boBits) ) ^ (handle<<offsetBits);
		
		index = index%CACHELINES;
		
		tag = ((long)handle << 32) + (offset&((~0)<<boBits));
		
//		System.out.println("HANDLE:" + handle + "\toff: " + offset + "\ttag: " +tag + "\tindex: " + index + "\tblockoff: " +blockoffset);
		
		return 0;
	}
	
	private int generateCacheAddrStatic2(int handle, int offset){
		
		
//		System.out.println("handle " + handle + "\toffset " + offset);
		
		int lower = offset& 0x3;
		int middle = offset & 0x1C;
		int upper = offset & 0xFFFFFFE0;
		
		offset = upper | (lower<<3) | (middle >> 2);
		
		
		int boBits = 3;
		if(WORDSPERLINE == 16){
			boBits = 4;
		} else if(WORDSPERLINE == 32){
			boBits = 5;
		} else if(WORDSPERLINE == 64){
			boBits = 6;
		} else if(WORDSPERLINE == 4){
			boBits = 2;
		}
		int boMask = (int)Math.pow(2, boBits)-1;
		blockoffset = offset & boMask;
		int indexBits = (int)(Math.log(CACHELINES)/Math.log(2));
		
		int offsetBits = 4;
		
//		index = ((offset>>boBits) & ~((-1)<<offsetBits)) + (handle<<offsetBits);
		index = ((offset>>boBits) ) ^ (handle<<offsetBits);
		
		index = index%CACHELINES;
		
		tag = ((long)handle << 32) + (offset&((-1)<<boBits));
		
//		System.out.println("\tTag " + tag + "\tindex " + index + "\tbo " + blockoffset);
		
		
		return 0;
	}
	
	private int generateCacheAddrOld(int handle, int offset){
		int selMask = 99;
		if(offset<=7){
			blockoffset = offset & 0x7;			
			int handle8to0 = handle & 0xFF;
			index = handle8to0;	
			long offset31to3 = offset & 0xFFFFFFF8;
			long handle31to9 = handle & 0xFFFFFE00;
			tag = (offset31to3 << 20) + (handle31to9 >>> 9);
			selMask = 0;
		}		
		if(8<=offset && offset<=15){
			blockoffset = offset & 0x7;			
			int handle7to0 = handle & 0x7F;
			int offset3 = offset & 0x8;
			index = (handle7to0 << 1) + (offset3 >>> 3);
			long offset31to4 = offset & 0xFFFFFFF0;
			long handle31to8 = handle & 0xFFFFFF00;
			tag = (offset31to4 << 20) + (handle31to8 >>> 8);
			selMask = 1;
		}		
		if(16<=offset && offset<=31){
			blockoffset = offset & 0x7;			
			int handle6to0 = handle & 0x3F;
			int offset4to3 = offset & 0x18;
			index = (handle6to0 << 2) + (offset4to3 >>> 3);	
			long offset31to5 = offset & 0xFFFFFFE0;
			long handle31to7 = handle & 0xFFFFFF80;
			tag = (offset31to5 << 20) + (handle31to7 >>> 7);
			selMask = 2;
		}		
		if(32<=offset){
			blockoffset = offset & 0x7;			
			int handle5to0 = handle & 0x1F;
			int offset5to3 = offset & 0x38;
			index = (handle5to0 << 3) + (offset5to3 >>> 3);		
			long offset31to6 = offset & 0xFFFFFFC0;
			long handle31to6 = handle & 0xFFFFFFC0;
			tag = (offset31to6 << 20) + (handle31to6 >>> 6);
//			System.out.println(" index_: " + index+ " tag: " + tag);
			selMask = 3;
		}
		tag = ((long)handle << 32) + (offset&((-1)<<3));
		return selMask;
	}
	
	private int generateCacheAddrNew(int handle, int offset){
		int selMask = 99;
		if(offset<=7){
			blockoffset = offset & 0x7;			
			int handle8to0 = handle & 0x1FF;
			index = handle8to0;	
			long longHandle = handle;
			int tmpOffset = offset & 0xFFFFFFF8;
			tag = (longHandle << 32) + tmpOffset;
			selMask = 0;
		}		
		if(8<=offset && offset<=15){
			blockoffset = offset & 0x7;			
			int handle7to0 = handle & 0xFF;
			int offset3 = offset & 0x8;
			index = (handle7to0 << 1) + (offset3 >>> 3);
			long longHandle = handle;
			int tmpOffset = offset & 0xFFFFFFF8;
			tag = (longHandle << 32) + tmpOffset;
			selMask = 1;
		}		
		if(16<=offset && offset<=31){
			blockoffset = offset & 0x7;			
			int handle6to0 = handle & 0x7F;
			int offset4to3 = offset & 0x18;
			index = (handle6to0 << 2) + (offset4to3 >>> 3);	
			long longHandle = handle;
			int tmpOffset = offset & 0xFFFFFFF8;
			tag = (longHandle << 32) + tmpOffset;
			selMask = 2;
		}		
		if(32<=offset){
			blockoffset = offset & 0x7;			
			int handle5to0 = handle & 0x3F;
			int offset5to3 = offset & 0x38;
			index = (handle5to0 << 3) + (offset5to3 >>> 3);		
			long longHandle = handle;
			int tmpOffset = offset & 0xFFFFFFF8;
			tag = (longHandle << 32) + tmpOffset;
			selMask = 3;
		}
		return selMask;
	}
	
	private int generateCacheAddrNew2(int handle, int offset){
		int selMask = 99;
		if(offset<=7){
			blockoffset = offset & 0xF;			
			int handle8to0 = handle & 0xFF;
			index = handle8to0;	
			long longHandle = handle;
			int tmpOffset = offset & 0xFFFFFFF0;
			tag = (longHandle << 32) + tmpOffset;
			selMask = 0;
		}		
		if(8<=offset && offset<=15){
			blockoffset = offset & 0xF;			
			int handle7to0 = handle & 0x7F;
			int offset4 = offset & 0x10;
			index = (handle7to0 << 1) + (offset4 >>> 4);
			long longHandle = handle;
			int tmpOffset = offset & 0xFFFFFFF0;
			tag = (longHandle << 32) + tmpOffset;
			selMask = 1;
		}		
		if(16<=offset && offset<=31){
			blockoffset = offset & 0xF;			
			int handle6to0 = handle & 0x3F;
			int offset5to4 = offset & 0x30;
			index = (handle6to0 << 2) + (offset5to4 >>> 4);	
			long longHandle = handle;
			int tmpOffset = offset & 0xFFFFFFF0;
			tag = (longHandle << 32) + tmpOffset;
			selMask = 2;
		}		
		if(32<=offset){
			blockoffset = offset & 0xF;			
			int handle5to0 = handle & 0x1F;
			int offset6to4 = offset & 0x70;
			index = (handle5to0 << 3) + (offset6to4 >>> 4);		
			long longHandle = handle;
			int tmpOffset = offset & 0xFFFFFFF0;
			tag = (longHandle << 32) + tmpOffset;
			selMask = 3;
		}
		return selMask;
	}
	
	boolean USE_SECOND = false;
	
	private void generateHandleOffset(long tag, int index, int boff, int selMask){
//		if(NEW_CACHE_ADDR) generateHandleOffsetNew(tag, boff);
//		if(!NEW_CACHE_ADDR) generateHandleOffsetOld(tag, index, boff, selMask);
		
//		generateHandleOffsetOld(tag, index, boff, selMask);
		
		
		if(selMask == 22){
			generateHandleOffsetStatic2(tag, index, boff, selMask);
		} else {
			if (selMask != 11 ){
				throw new RuntimeException("Shittyfuck");
			}
			generateHandleOffsetStatic(tag, index, boff, selMask);
		}
		
//		System.out.println("DEGEN " + handle + " " + offset+ ": " + tag + " " + index + " " + blockoffset + " " + selMask);
	}
	
	private void generateHandleOffsetStatic(long tag, int index, int blockoffset, int selMask){
		handle = (int) (tag >>> 32);
		offset = (int) (tag+blockoffset);
	}
	
	private void generateHandleOffsetStatic2(long tag, int index, int blockoffset, int selMask){
		handle = (int) (tag >>> 32);
		offset = (int) (tag+blockoffset);
		
		int lower = offset& 0x7;
		int middle = offset & 0x18;
		int upper = offset & 0xFFFFFFE0;
		
		offset = upper | (lower<<2) | (middle >> 3);
		
		
	}

	private void generateHandleOffsetOld(long tag, int index, int blockoffset, int selMask){
		switch(selMask){
		case 0:
			int handle31to9 = (int)(tag & 0x7FFFFF);
			int offset31to3 = (int)(tag >>> 23);
			int handle8to0 = index;
			handle = (handle31to9 << 9) + handle8to0;
			offset = (offset31to3 << 3) + blockoffset;
			break;
		case 1:
			int handle31to8 = (int)(tag & 0xFFFFFF);
			int offset31to4 = (int)(tag >>> 24);
			int offset3 = index & 0x1;
			int handle7to0 = index >>> 1;
			handle = (handle31to8 << 8) + handle7to0;
			offset = (offset31to4 << 4) + (offset3 << 3) + blockoffset;
			break;
		case 2:
			int handle31to7 = (int)(tag & 0x1FFFFFF);
			int offset31to5 = (int)(tag >>> 25);
			int offset4to3 = index & 0x3;
			int handle6to0 = index >>> 2;
			handle = (handle31to7 << 7) + handle6to0;
			offset = (offset31to5 << 5) + (offset4to3 << 3) + blockoffset;
			break;
		case 3:
			int handle31to6 = (int)(tag & 0x3FFFFFF);
			int offset31to6 = (int)(tag >>> 26);
			int offset5to3 = index & 0x7;
			int handle5to0 = index >>> 3;
			handle = (handle31to6 << 6) + handle5to0;
			offset = (offset31to6 << 6) + (offset5to3 << 3) + blockoffset;
			break;
		default: throw new AmidarSimulatorException("generateHandleOffset() says: \"No valid selMask!\"");
		}
	}
	
	private void generateHandleOffsetNew(long tag, int boff){
		handle = (int) (tag >>> 32);
		offset = (int) (tag+boff);
	}
	
	private void generateHandleOffsetNew2(long tag, int boff){
		handle = (int) (tag >>> 32);
		offset = (int) (tag+boff);
	}
	
	private void setPLRU(int index, int set){
		switch(SETS){
		case 1:
			setPLRU1(index, set);
			break;
		case 2:
			setPLRU2(index, set);
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
	
	private void setPLRU8(int index, int set){
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
	
	private void setPLRU4(int index, int set){
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
	private void setPLRU2(int index, int set){
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
	private void setPLRU1(int index, int set){
		//NOTHING TODO HERE
	}
	
	private int decisionPLRU(int index){
		
		switch(SETS){
		case 1:
			return 0;
		case 2:
			return plru[index];
		case 4:
			return decisionPLRU4(index);
		case 8:
			return decisionPLRU8(index);
		default:
			return 99;
		}
	}
	private int decisionPLRU8(int index){
		if(cache[index][0].getValidBit() == 0) return 0;
		if(cache[index][1].getValidBit() == 0) return 1;
		if(cache[index][2].getValidBit() == 0) return 2;
		if(cache[index][3].getValidBit() == 0) return 3;
		if(cache[index][4].getValidBit() == 0) return 4;
		if(cache[index][5].getValidBit() == 0) return 5;
		if(cache[index][6].getValidBit() == 0) return 6;
		if(cache[index][7].getValidBit() == 0) return 7;
		
		
//		Random r = new Random();
//		
//		return r.nextInt(8);
		
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
	private int decisionPLRU4(int index){
		if(cache[index][0].getValidBit() == 0) return 0;
		if(cache[index][1].getValidBit() == 0) return 1;
		if(cache[index][2].getValidBit() == 0) return 2;
		if(cache[index][3].getValidBit() == 0) return 3;
		
		
		replaced[index] = replaced[index] +4;
		
//		return plru[index];
		
		switch(plru[index]){
		case 0:
		case 1:
//			System.err.println(0);
			return 0;
		case 2:
		case 3:
//			System.err.println(1);
			return 1;
		case 4:
		case 6:
//			System.err.println(2);
			return 2;
		case 5:
		case 7:
//			System.err.println(3);
			return 3;
		default:
			return 99;
		}
	}
	
	
	private int decisionPLRU4Alt(int index){
		if(cache[index][0].getValidBit() == 0) return 0;
		if(cache[index][1].getValidBit() == 0) return 1;
		if(cache[index][2].getValidBit() == 0) return 2;
		if(cache[index][3].getValidBit() == 0) return 3;
		
		
		replaced[index] = replaced[index] +4;
		
//		return plru[index];
		
		switch(plru[index]){
		case 4:
		case 5:
//			System.err.println(0);
			return 0;
		case 6:
		case 7:
//			System.err.println(1);
			return 1;
		case 0:
		case 2:
//			System.err.println(2);
			return 2;
		case 1:
		case 3:
//			System.err.println(3);
			return 3;
		default:
			return 99;
		}
	}
	
	public int getData(int handle, int offset){
		int index = this.index;
		long tag = this.tag;
		int bo = this.blockoffset;
		
		generateCacheAddr(handle, offset);
		
		if(index != this.index || tag != this.tag || bo != this.blockoffset){
//			throw new RuntimeException("FUCK");
//			System.err.println("FUCK... "  + cacheID);
		}
		
		return data;
	}

	public boolean isReady(){
		return true;
	}

	
	public int getMemoryAccessTime(){
		return extMemAcc;
	}
	
	public int getCLData(int index, int set, int boff){
		return cache[index][set].getData(boff);
	}
	
	public void setCLData(int index, int set, int boff, int data){
		cache[index][set].setData(boff, data);
	}
	
	public int getMaxOffset(int index, int set){
		return cache[index][set].getMaxOffset();
	}
	
	public int getReturnSet(){
		return returnSet;
	}
	
	public int getReturnIndex(){
		return returnIndex;
	}
	
	public void setMOESICaches(Cache[] moesiCaches){
		int cachecount = moesiCaches.length + 1;
		this.moesiCaches = new Cache[cachecount];
		System.arraycopy(moesiCaches, 0, this.moesiCaches, 0, moesiCaches.length);
		if(isHeapCache){
			for(int i = 0; i<cachecount-1; i++){
				moesiCaches[i].setHeapCache(this);			
			}
			this.setHeapCache(this);
			cacheID = cachecount-1;			
		}
	}
	
	public void setHeapCache(Cache objCache){
		int position = this.moesiCaches.length-1;
		this.moesiCaches[position] = objCache;
	}
	
	
	public MOESIState checkState(int handle, int offset){
		int selMask = generateCacheAddr(handle, offset);
		for(int i = 0; i<SETS; i++){
			MOESIState moesiState = cache[index][i].getMoesiState();
			if((cache[index][i].getTag() == tag) && (moesiState != MOESIState.INVALID) && cache[index][i].getSelMask() == selMask){
				returnSet = i;
				returnIndex = index;
				if(moesiState == MOESIState.EXCLUSIVE) cache[index][i].setMoesiState(MOESIState.SHARED);
				if(moesiState == MOESIState.MODIFIED) cache[index][i].setMoesiState(MOESIState.OWNED);
				return moesiState;				
			}
		}
		return MOESIState.INVALID;
	}
	
	
	public void writeNotification(int handle, int offset){
		int selMask = generateCacheAddr(handle, offset);
		
		for(int i = 0; i<SETS; i++){
			if(cache[index][i].getTag() == tag && cache[index][i].getMoesiState() != MOESIState.INVALID && cache[index][i].getSelMask() == selMask){
//				System.out.prisntln(" CACHE "+ cacheID + " INVALIDATEING INDEX " + index);
				cache[index][i].setMoesiState(MOESIState.INVALID);	
			}
		}
	}
	
	
	public boolean updateNotification(int handle, int offset, CacheLine cacheLine){
		int selMask = generateCacheAddr(handle, offset);
		boolean updated = false;
		
		for(int i = 0; i<SETS; i++){
			if(cache[index][i].getTag() == tag && cache[index][i].getMoesiState() != MOESIState.INVALID && cache[index][i].getSelMask() == selMask){
//				System.out.prisntln(" CACHE "+ cacheID + " INVALIDATEING INDEX " + index);
				updated = true;
				for(int word = 0; word < WORDSPERLINE; word++){
					cache[index][i].setData(word, cacheLine.getData(word));
				}
				
				cache[index][i].setOverhead(tag, 1, 1, cacheLine.getMaxOffset(), selMask);
				
				cache[index][i].setMoesiState(MOESIState.SHARED);	
				statistics[CACHELINE_UPDATES]++;
			}
		}
		return updated;
	}
	
	
	public void printStatistics(){
		Trace cacheTrace = traceManager.getf("caches");
		double temp = 0;
		if(isHeapCache){
			cacheTrace.setPrefix(" heap cache ");
			cacheTrace.printTableHeader("Heap Cache:");
		}
		else{
			cacheTrace.setPrefix("cgra cache "+cacheID);
			cacheTrace.printTableHeader("Cgra Cache "+cacheID+":");
		}
		int read = statistics[0];
		cacheTrace.println("Read Requests:                      "+read);
		int write = statistics[1];
		cacheTrace.println("Write Requests:                     "+write);
		cacheTrace.println();
		cacheTrace.println("Read Miss abs.:                     "+statistics[2]);
		cacheTrace.println("    thereof Tag Match but INVALID:  "+statistics[10]);
		if(read!=0) temp = ((statistics[2]*10000d)/read);
		else temp = 0;
		cacheTrace.println("Read Missrate:                      "+((int)temp)/100d+"%");
		cacheTrace.println();
		cacheTrace.println("Write Miss abs.:                    "+statistics[4]);
		cacheTrace.println("    thereof Tag Match but INVALID:  "+statistics[11]);
		if(write!=0) temp = ((statistics[4]*10000d)/write);
		else temp = 0;
		cacheTrace.println("Write Missrate:                     "+((int)temp)/100d+"%");
		cacheTrace.println();
		temp = statistics[8]+statistics[9];
		cacheTrace.println("Lines Reloaded from L2 Cache:         "+(int)temp);
		temp = statistics[6]+statistics[7];
		cacheTrace.println("Lines Reloaded from ext. L1 Cache:     "+(int)temp);
		cacheTrace.println("    thereof while Read Op.:         "+statistics[6]);
		cacheTrace.println("    thereof while Write Op.:        "+statistics[7]);
		cacheTrace.println("Cacheline Updates:                  "+statistics[12]);
		cacheTrace.println();
		cacheTrace.println("Prefetches from L1:                 "+nrPrefetchedFromL1);
		cacheTrace.println("              used:                 "+100*nrUsedPrefetchFromL1/(double)nrPrefetchedFromL1+"%");
		cacheTrace.println("Prefetches from L2:                 "+nrPrefetchedFromL2);
		cacheTrace.println("              used:                 "+100*nrUsedPrefetchFromL2/(double)nrPrefetchedFromL2+"%");
		
//		nrPrefetchedFromL1S += nrPrefetchedFromL1;
//		nrUsedPrefetchL1S += nrUsedPrefetchL1;
//		nrPrefetchedFromL2S += nrPrefetchedFromL2;
//		nrUsedPrefetchL2S += nrUsedPrefetchL2;
//		System.err.println("Prefetches from L1:                 "+nrPrefetchedFromL1S);
//		System.err.println("              used:                 "+100*nrUsedPrefetchL1S/(double)nrPrefetchedFromL1S+"%");
//		System.err.println("Prefetches from L2:                 "+nrPrefetchedFromL2S);
//		System.err.println("              used:                 "+100*nrUsedPrefetchL2S/(double)nrPrefetchedFromL2S+"%");
	}
	
	public void traceAll(){
		this.printStatistics();
		if(synthesis){
			for(int i = 0; i<moesiCaches.length-1; i++){
				moesiCaches[i].printStatistics();
			}
		}
		Trace cacheTrace = traceManager.getf("caches");
		cacheTrace.setPrefix("L2 Cache");
		
		L2Cache l2 = coherenceController.getL2Cache();
		cacheTrace.println("Read Missrate:\t" + l2.getReadMissRate());
		cacheTrace.println("Write Missrate:\t" + l2.getWriteMissRate());
	}
	
	public void resetStatistics(){
		statistics = new int[13];
	}
	
	public Cache[] getCaches(){
		return moesiCaches;
	}
	
	
	public double usage(){
		double invalid=0, valid=0;
		
		for(int set = 0; set < SETS; set++){
			for(int index = 0; index < CACHELINES; index++){
				MOESIState state = cache[index][set].getMoesiState();
				if(state == MOESIState.INVALID){
					invalid++;
				} else {
					valid++;
				}
			}
		}
		
		
		return(valid/(invalid+valid));
	}
	
	public int[] getState(){
		int[] ret = new int[CACHELINES];
		
		for(int index = 0; index < CACHELINES; index++){
			ret[index]  = replaced[index];
			replaced[index] = 0;
//			for(int set = 0; set < SETS; set++){
//				if(cache[index][set].getMoesiState()!= MOESIState.INVALID){
//					ret[index]++;
//				}
//			}
		}
		
		
		return ret;
		
	}
	
	/**
	 * MOESI Backdoor
	 * @param handle
	 * @param offset
	 * @return
	 */
	public boolean holdsValue(int handle, int offset){
		if(offset < 0 || handle < 0 || offset == Integer.MAX_VALUE  || offset == Integer.MAX_VALUE-1 || offset > 65535 || handle > 65535){
			return false;
		}
		generateCacheAddr(handle, offset);
		for(int i = 0; i<SETS; i++){
			MOESIState moesiState = cache[index][i].getMoesiState();
			if((cache[index][i].getTag() == tag) && (moesiState != MOESIState.INVALID)){
				return true;				
			}
		}
		return false;
	}
	
	
	
	LinkedHashSet<Long> prefetchedFromL1 = new LinkedHashSet<>();
	LinkedHashSet<Long> prefetchedFromL2 = new LinkedHashSet<>();
	int nrPrefetchedFromL1 = 0;
	int nrPrefetchedFromL2 = 0;
	int nrUsedPrefetchFromL1 = 0;
	int nrUsedPrefetchFromL2 = 0;
	static int nrPrefetchedFromL1S = 0;
	static int nrPrefetchedFromL2S = 0;
	static int nrUsedPrefetchL1S = 0;
	static int nrUsedPrefetchL2S = 0;
	
	
	/** MOESI BACKDOOR
	 * 
	 * @param handle2
	 * @param offset
	 * @param prefetchedData
	 */
	public int updateValue(int handle, int offset, CacheLine prefetchedData) {
		
		int selMask = generateCacheAddr(handle, offset);
		int replaceInSet = decisionPLRU4Alt(index);
		MOESIState currentState = cache[index][replaceInSet].getMoesiState();
		
		int delay = 0;
		CacheLine toWriteBack = null;
		if(currentState == MOESIState.OWNED || currentState == MOESIState.MODIFIED){
			toWriteBack = cache[index][replaceInSet].getClone();
		}
		
		for(int i=0; i<WORDSPERLINE; i++){
			cache[index][replaceInSet].setData(i, prefetchedData.getData(i));
		}
		
		cache[index][replaceInSet].setOverhead(tag, 1, 0, prefetchedData.getMaxOffset(), selMask);
		
		MOESIState newState;
		
		Long tagg = ((long)handle)<<32;
		tagg += offset&0xFFFFFFF1; 
		
		if(coherenceController.fromL2Cache()){
			newState = MOESIState.EXCLUSIVE;
//			statistics[FROM_L2_READ]++;
			prefetchedFromL2.add(tagg);
			nrPrefetchedFromL2++;
		} else {
			newState = MOESIState.SHARED;
//			statistics[FROM_L1_READ]++;
			prefetchedFromL1.add(tagg);
			nrPrefetchedFromL1++;
		}
		
		
		cache[index][replaceInSet].setMoesiState(newState);
//		System.out.println(cache[index][replaceInSet].getMoesiState());
		
		if(toWriteBack != null){
//			System.out.println("WRITEBACK");
			generateHandleOffset(toWriteBack.getTag(), index, 0, toWriteBack.getSelMask());
			coherenceController.writeBack(this.handle, this.offset, toWriteBack, true, this);
			Long taggg = ((long)this.handle)<<32;
			taggg += this.offset&0xFFFFFFF1;
			prefetchedFromL1.remove(taggg);
			prefetchedFromL2.remove(taggg);
		}
		
		setPLRU(index, replaceInSet);
		
		return delay;
	}
	
	int prefetchRepeatCounter = 0;
	int prefetchRepeatLimit = 0;

	public void prefetchDone(PrefetchRequest currentreq) {
		if(isHeapCache){
			return;
		}
		
		
		prefetchRepeatCounter++;
		if(prefetchRepeatCounter >= prefetchRepeatLimit){
			return;
		}
		if(prefetchStrategy == PrefetchStrategy.LINEAR){
			coherenceController.requestDataPrefetch(currentreq.getHandle(), currentreq.getOffset()+ WORDSPERLINE *(int)(Math.pow(2, prefetchRepeatCounter+1)-1), this);
		}
		
	}
	
	private void resetPrefetchRepeatCounter(){
		prefetchRepeatCounter = 0;
	}
	
	
	
	public void invalidateFlush(){
		
		for(int line = 0; line < CACHELINES; line++){
			for(int set = 0; set < SETS; set++){
				CacheLine cacheLine = cache[line][set];
				
				if(cacheLine.getMoesiState() == MOESIState.MODIFIED || cacheLine.getMoesiState() == MOESIState.OWNED){
					//TODO WRITE BACK
					generateHandleOffset(cacheLine.getTag(), line, 0, cacheLine.getSelMask());
					coherenceController.writeBack(handle, offset, cacheLine, false, this);
					Long taggg = ((long)this.handle)<<32;
					taggg += this.offset&0xFFFFFFF1;
					prefetchedFromL1.remove(taggg);
					prefetchedFromL2.remove(taggg);
				}
				
				cacheLine.setMoesiState(MOESIState.INVALID);
			}
		}
		
		lastAccess = new LinkedHashMap<>();
		histograms = new TreeMap<>();
		
		
	}
	
	
	LinkedHashMap<Integer, Integer> lastAccess = new LinkedHashMap<>();
	TreeMap<Integer, TreeMap<Integer,Integer>> histograms = new TreeMap<>();
	
	private void trackReadPattern(int handle, int offset){
		Integer lastOffset = lastAccess.get(handle);
		lastAccess.put(handle, offset);
		if(lastOffset == null){
			return;
		}
		
		int diff = lastOffset - offset;
		
		TreeMap<Integer, Integer> histogram = histograms.get(handle);
		if(histogram == null){
			histogram = new TreeMap<>();
			histograms.put(handle, histogram);
			
		}
		if(histogram.containsKey(diff)){
			histogram.put(diff, histogram.get(diff) +1);
		} else {
			histogram.put(diff, 1);
		}
	}
	
	
	public void printHistogram(Trace trace){
		
		trace.println("HISTOGRAM CACHE " + cacheID);
		for(Integer handle: histograms.keySet()){
			System.out.println("\tHandle: " + handle);
			TreeMap<Integer, Integer> hist = histograms.get(handle);
			for(Integer diff: hist.keySet()){
				System.out.println("\t\t"+diff+":\t" + hist.get(diff) );
			}
		}
		
	}
	
	public void requestPrefetch(int handle, int offset){
		if(offset < 0 || handle < 0 || offset == Integer.MAX_VALUE  || offset == Integer.MAX_VALUE-1 || offset > 65535 || handle > 65535){
			return;
		}
//		System.out.println("REQ: " + handle + "  + " + offset);
		
		coherenceController.requestDataPrefetch(handle, offset, this);
	}
	
	
	public int getWordsPerLine(){
		return WORDSPERLINE;
	}

	public  int getNrPrefetchedFromL1() {
		return nrPrefetchedFromL1;
	}

	public  int getNrPrefetchedFromL2() {
		return nrPrefetchedFromL2;
	}

	public  int getNrUsedPrefetchFromL1() {
		return nrUsedPrefetchFromL1;
	}

	public  int getNrUsedPrefetchFromL2() {
		return nrUsedPrefetchFromL2;
	}
	
	

}
