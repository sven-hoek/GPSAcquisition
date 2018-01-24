package functionalunit.cache.coherency;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;

import functionalunit.cache.Cache;
import functionalunit.cache.CacheLine;
import functionalunit.cache.HandleTableCache;
import functionalunit.cache.L2Cache;
import functionalunit.cache.L2CachePhysical;
import functionalunit.cache.PrefetchStrategy;
import functionalunit.cache.coherency.CoherenceControllerTask.RequestType;
import functionalunit.cache.coherency.CoherenceControllerTask.TaskType;
import tracer.Trace;

public abstract class CoherenceController {
	
	protected int WORDSPERLINE_L1 = 8;
	protected int WORDSPERLINE_L2 = 8;

	protected final int BUSWIDTH_L1 = 8;
//	
	protected final int BUSWIDTH_L2 = 8;

	protected final int L1_READ_FROM_L1_OVERHEAD = 3;
	protected final int L1_READ_FROM_L2_OVERHEAD = 6;
//	protected final int L1_READ_FROM_L2 = BUSWIDTH_L1 + 2;
//
//	protected final int L1_WRITEBACK_TO_L2 = BUSWIDTH_L1 + 2;
//
//	protected final int L1_WRITENOTIFICATION_TO_L1 = 1;
//
//	protected final int L2_READ_FROM_DRAM = 40;
//
	protected final int L2_WRITEBACK_TO_DRAM = 40;
//
//	protected final int HT_LOOKUP_OVERHEAD = 2;

	//	int accessPerTicks = 0;
	//	int ticksWithAccesses = 0;
	//	int readAccPerTicks = 0;
	//	int writeNotAccPerTicks = 0;
	//	int readAccessPerTicksAccumulated = 0;
	//	int writeNotAccessPerTicksAccumulated = 0;
	//	int accessPerTicksAccumulated = 0;
	//	int totalTicks = 0;
	//	
	//	
	//	
	//	
	//	int kkount = 0;
	//	double valddd = 0;


	protected int busyTime = 0;
	protected int busyTimeHT = 0;
	protected int busyTimeL2 = 0;

	Cache[] caches;

	CacheLine requestedData;
	boolean fromL2Cache;
	boolean isDataExclusive;

	PrefetchStrategy prefetchStrategy;

	L2Cache l2Cache;

	HandleTableCache handleTableCache;

	CoherenceControllerTaskSimulator taskSimulator = new CoherenceControllerTaskSimulator();

	public  int prefAlreadyAvailable = 0;
	public int prefetched = 0;
	static int overWrittenPref = 0;
	public int loadedData= 0;
	
	
	boolean alreadyWriteNotificationInTimeStep = false;


	int ringBufferSize = 8;

	LinkedHashMap<Cache, PrefetchRequest[]> prefetchRequestRingBuffers;
	LinkedHashMap<Cache, Integer> readPointer,writePointer;


	public CoherenceController() {

	}

	public void setCaches(Cache[] caches){
		this.caches = caches;

		prefetchRequestRingBuffers = new LinkedHashMap<>();
		readPointer = new LinkedHashMap<>();
		writePointer = new LinkedHashMap<>();

		for(Cache c: caches){
			prefetchRequestRingBuffers.put(c, new PrefetchRequest[ringBufferSize]);
			readPointer.put(c, 0);
			writePointer.put(c, 0);
		}
	}

	public void setL2Cache(L2Cache l2Cache){
		this.l2Cache = l2Cache;
		WORDSPERLINE_L2 = l2Cache.getWordsPerLine();
	}

	public void setHandleTableCache(HandleTableCache handleTableCache){
		this.handleTableCache = handleTableCache;
	}


	public abstract int requestData(int handle, int offset, Cache self);

	public abstract int requestDataPrefetch(int handle, int offset, Cache self);

	protected abstract int handleDataPrefetchRequest();

	public CacheLine getData(){
		return requestedData; // returns a clone, so that
	}

	public boolean fromL2Cache(){
		return fromL2Cache;
	}

	public boolean isDataExclusive(){
		return isDataExclusive;
	}

	public abstract boolean updatingL2Cache();

	public abstract int writeBack(int handle, int offset, CacheLine cacheLine, boolean isPrefetch, Cache self);

	public abstract int writeNotification(int handle, int offset, CacheLine cacheLine, Cache self);


	//	static FileWriter fw;
	//	static BufferedWriter bw;


	static long ticks = 0;
	static long busyTicks = 0;
	static long prefetchTicks = 0;
	boolean kernelRunningOnCGRA = false;
	boolean cgraBlocked = false;
	int contextCounter = 0;


	int HISTORY = 1000;
	char[][] contextUsedCohrenceController = new char[HISTORY][4096];
	long[] stepcnt = new long[4096];

	//	double val, last, secondToLast;

	//	boolean wasJustBlocked = false;

	public void tick(){

//		l2Cache.tick();

		alreadyWriteNotificationInTimeStep = false;

		RequestType rt = taskSimulator.tick(busyTime, kernelRunningOnCGRA, cgraBlocked);

		int t;

		if(!cgraBlocked){
			t = (int)(stepcnt[contextCounter]%HISTORY);
			stepcnt[contextCounter] = (stepcnt[contextCounter]+1);
		} else {
			t = (int)((stepcnt[contextCounter]-1)%HISTORY);
			if(t < 0){
				t = 0;
			}
		}


		if(rt == RequestType.Regular){
			contextUsedCohrenceController[t][contextCounter] = '+';
		} else if(rt == RequestType.Prefetch){
			char rr;

			if(busyTime>15){
				rr = '*';
			} else {
				String bt = Integer.toHexString(busyTime);
				rr = bt.charAt(bt.length()-1);
			}
			contextUsedCohrenceController[t][contextCounter] = rr;
		} else {
			contextUsedCohrenceController[t][contextCounter] = '-';
		}


		if(kernelRunningOnCGRA){
			ticks++;
		}
		if(busyTime > 0){
			busyTime--;
			if(kernelRunningOnCGRA){
				busyTicks++;
			}

		} else {
			boolean prefetch = true;
			int futureContext = 10;
			int past = 4;

			for(int i = 0; i < futureContext && contextCounter + i < 4096; i++){
				for(int tt = 0; tt <  past && t-tt >= 0; tt++){
					prefetch &= contextUsedCohrenceController[t-tt][contextCounter+i]!='+';
				}
			}
//			if(prefetch)
				handleDataPrefetchRequest(); // Not busy -> handle prefetch requests

		}
		if(busyTimeHT > 0){
			busyTimeHT--;
		}
		if(busyTimeL2 > 0){
			busyTimeL2--;
		}
	}


	public HandleTableCache getHTCache() {
		return handleTableCache;
	}


	public void printMissrates() {
		for(Cache c: caches){
			c.printStatistics();
		}
		l2Cache.printStatistics();

		//		if(caches.length != 1){
		//			kkount++;
		//			valddd += accessPerTicksAccumulated/(double)totalTicks;
		//			System.out.println("ASDF " + valddd/kkount);
		//			
		//		}


		//		taskSimulator.report();

		//		for(int i = 0; i< 635; i++){
		//			System.out.print(i+":\t" + stepcnt[i]+"\t");
		//			for(int t = 0; t < stepcnt[i] && t < HISTORY; t++){
		//				System.out.print(contextUsedCohrenceController[t][i]);
		//			}System.out.println();
		//		}

		//		System.err.println("pref: " + prefetched);
		//		System.err.println("prefalreadayavali:  " + prefAlreadyAvailable);
		//		System.err.println("OVEWRW: " + overWrittenPref);

	}

	public int getNrOfCaches(){
		return caches.length;
	}

	public int requestHandleTable(int handle, boolean isPrefetch, Cache self){
		int time = busyTime;
		time += handleTableCache.requestData(handle);
		taskSimulator.addCoherenceControllerTask(new CoherenceControllerTask(TaskType.HTRequest, isPrefetch?RequestType.Prefetch:RequestType.Regular, time-busyTime+1, self.getCacheID()));
		busyTime = time+1;
		busyTimeHT = time+1;

		return time;
	}

	public int getCTI(){
		return handleTableCache.getCTI();
	}

	public int getArrayLength(){
		return handleTableCache.getSize();
	}


	public double getUtilization(){
		return busyTicks/(double)ticks;
	}

	public double getPrefetchRatio(){
		return prefetchTicks/(double)ticks;
	}

	public class PrefetchRequest{

		int handle;
		int offset;

		public PrefetchRequest(int handle, int offset){
			this.handle = handle;
			this.offset = offset;
		}

		public int getHandle(){
			return handle;
		}

		public int getOffset(){
			return offset;
		}

	}

	public void count(boolean kernelRunningOnCGRA, boolean cgraBlocked, int contextCounter) {
		this.kernelRunningOnCGRA = kernelRunningOnCGRA;
		this.cgraBlocked = cgraBlocked;
		this.contextCounter = contextCounter;
	}


	public int getBusyTime() {
		return busyTime;
	}

	public void resetBusyTime(){
		System.err.println("Reseting cache Coherence controller - I hope you know what you are doing. ");
		// Used in speedup measurement when invalidating the cache
		busyTime = 0;
		busyTimeHT = 0;
		busyTimeL2 = 0;
	}


	public void invalidateFlushAllCaches() {

		for(Cache cache: caches){
			cache.invalidateFlush();
		}
		l2Cache.invalidateFlush();
		handleTableCache.invalidateFlush();
		taskSimulator.resetCounter();
	}


	public Cache[] getCaches() {
		return caches;
	}


	public L2Cache getL2Cache() {
		return l2Cache;
	}


	public boolean isLooongArray(int handle){

		int size = handleTableCache.arrayLengthFAKE(handle);

		//		System.err.println("SIZE: " + size + " Handle: " + handle);

		boolean isLong = false;
		//		int mod = size %32;
		//		
		//		if(mod  == 0){
		//			isLong = true;
		//		}
		//		
		//		if((32.0-mod)/size < 1.0/32){
		//			isLong = true;
		//		}
		//		
		//		
		//		return size >= 1024 && handle == 1226;


		//		return size == 1024;
		return isLong;
		//		

	}


	public CoherenceControllerTaskSimulator getTaskSimulator() {
		return taskSimulator;
	}


	public void setPrefetchStrategy(PrefetchStrategy prefetch) {
		this.prefetchStrategy = prefetch;

	}

	public PrefetchStrategy getPrefetchStrategy(){
		return prefetchStrategy;
	}


	//	public int writeBack(int handle, int offset, CacheLine cacheLine, boolean isPrefetchm, Cache self) {
	//		// TODO Auto-generated method stub
	//		return 0;
	//	}

}
