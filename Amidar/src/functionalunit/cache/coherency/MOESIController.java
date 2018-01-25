package functionalunit.cache.coherency;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import functionalunit.cache.Cache;
import functionalunit.cache.CacheLine;
import functionalunit.cache.HandleTableCache;
import functionalunit.cache.L2CachePhysical;
import functionalunit.cache.Cache.MOESIState;
import functionalunit.cache.coherency.CoherenceControllerTask.RequestType;
import functionalunit.cache.coherency.CoherenceControllerTask.TaskType;

public class MOESIController extends CoherenceController {
	
//	int nrOfMOESITagPorts = 4;
	
	

//	public MOESIController(Cache[] caches, L2CachePhysical l2Cache, HandleTableCache handleTableCache) {
//		super(caches, l2Cache, handleTableCache);
//	}
	
	public MOESIController(){
		super();
	}
	
	boolean USE_SECOND = false;
	
	@Override
	public int requestData(int handle, int offset, Cache self) {
		int wordsPerLineL1 = self.getWordsPerLine();
		loadedData++;
//		accessPerTicks++;
//		readAccPerTicks++;
//		System.out.println("REQDATA " + handle + " " + offset + " " + self.getCacheID());
		int delay = busyTime;

		int sharedCounter = 0;
		int sharedCache = 0;
		int sharedCacheSet = 0;
		int sharedIndex = 0;
		
		int getFromCache = -1;
		int getFromSet = -1;
		int getFromIndex = -1;
		
		fromL2Cache = false;
		isDataExclusive = true;
		
		for(int i = 0; i<caches.length; i++){
			if(caches[i] == self){
				continue; // Do not check the cache itself
			}
			MOESIState cacheResult = caches[i].checkState(handle, offset); //
			if(cacheResult != MOESIState.INVALID && cacheResult != MOESIState.SHARED){
				getFromCache = i;
				getFromSet = caches[i].getReturnSet();
				getFromIndex = caches[i].getReturnIndex();
				break;
			}
			if(cacheResult == MOESIState.SHARED){
				sharedCounter++;
				sharedCache = i;
				sharedCacheSet = caches[i].getReturnSet();
				sharedIndex = caches[i].getReturnIndex();
			}
		}
		if(getFromCache == -1 && sharedCounter > 0){
			getFromCache = sharedCache;
			getFromSet = sharedCacheSet;
			getFromIndex = sharedIndex;
//			return MOESIState.SHARED;
		}
//		return MOESIState.INVALID;
		
		if(getFromCache != -1){
			requestedData = new CacheLine(wordsPerLineL1);
			
			for(int i = 0; i < wordsPerLineL1; i++){
				requestedData.setData(i, caches[getFromCache].getCLData(getFromIndex, getFromSet, i));
			}
			requestedData.setMaxOffset(caches[getFromCache].getMaxOffset(getFromIndex, getFromSet));
			isDataExclusive = false;
			
			int additionalTime = WORDSPERLINE_L1 / BUSWIDTH_L1 + L1_READ_FROM_L1_OVERHEAD;
			
			delay += additionalTime;

			busyTime = delay;
			taskSimulator.addCoherenceControllerTask(new CoherenceControllerTask(TaskType.L1DataFetch, RequestType.Regular, additionalTime, self.getCacheID()));
			return delay ; 
		}

		/// get from L2 ///////////////////////////////
		//
		fromL2Cache = true;
		
		
		
		if(l2Cache.physicallyAddressed()){
			// WAIT FOR HT
			if(delay < busyTimeHT){
				delay = busyTimeHT;
			}
			
			delay += handleTableCache.requestData(handle);
			int baseAddress = handleTableCache.getAddr();
			int address = baseAddress + (((offset)>>>3)<<3);

			busyTimeHT = delay;
			
			requestedData = new CacheLine(WORDSPERLINE_L1);



			

//			int delayCopy = WORD
			
			// WAIT FOR L2
			if(delay < busyTimeL2){
				delay = busyTimeL2;
			}
			
			
			delay += WORDSPERLINE_L1 / BUSWIDTH_L2 + L1_READ_FROM_L2_OVERHEAD;
			
			delay += l2Cache.getLoadOverHead();
			
//			System.err.println("L1111111111: "  + (delay-busyTime));
			int writeBackTime = 0;

			for(int i = 0; i < WORDSPERLINE_L1; i++){
				if( !l2Cache.holdsData(handle, offset, address + i)){
					delay += l2Cache.getRAMaccessTime();
				}
				
				
				writeBackTime += l2Cache.requestData(handle, offset, address + i);
				requestedData.setData(i, l2Cache.getData(handle, offset, address + i));
			}
//			System.err.println("L11111111112: "  + (delay-busyTime));
			
			busyTimeL2 = delay + writeBackTime;



			//		for(int i = 0; i < WORDS_PER_LINE; i++){
			//			
			//		}
			requestedData.setMaxOffset(0);


			taskSimulator.addCoherenceControllerTask(new CoherenceControllerTask(TaskType.L2DataFetch, RequestType.Regular, delay-busyTime, self.getCacheID()));

			busyTime = delay;
			return delay;
		} else {
			// WAIT FOR L2
			if(delay < busyTimeL2){
				delay = busyTimeL2;
			}

			int writeBackTime = 0;
			
			if(!l2Cache.holdsData(handle, offset, 999)){
				
				// WAIT FOR HT
				if(delay < busyTimeHT){
					delay = busyTimeHT;
				}
				
				delay += handleTableCache.requestData(handle);
				int baseAddress = handleTableCache.getAddr();
				int address = baseAddress + (((offset)>>>3)<<3);

				busyTimeHT = delay;
				
				requestedData = new CacheLine(WORDSPERLINE_L1);
				
				
				
				delay += l2Cache.getRAMaccessTime(); // NACHLADEN
				
				for(int i = 0; i < WORDSPERLINE_L1; i++){
					writeBackTime += l2Cache.requestData(handle, offset, address + i);
					requestedData.setData(i, l2Cache.getData(handle, offset, address + i));
				}
				
				
			} else {
				
				int baseAddress = handleTableCache.addressFAKE(handle); // bissl doof programmiert TODO
				int address = baseAddress + (((offset)>>>3)<<3);
				
				for(int i = 0; i < WORDSPERLINE_L1; i++){
					writeBackTime += l2Cache.requestData(handle, offset, address + i);
					requestedData.setData(i, l2Cache.getData(handle, offset, address + i));
				}
				
			}
			
			
			
			delay += WORDSPERLINE_L1 / BUSWIDTH_L2 + L1_READ_FROM_L2_OVERHEAD;

			delay += l2Cache.getLoadOverHead();
			
			taskSimulator.addCoherenceControllerTask(new CoherenceControllerTask(TaskType.L2DataFetch, RequestType.Regular, delay-busyTime, self.getCacheID()));
			
			busyTime = delay;
			busyTimeL2 = delay + writeBackTime;
			
			return delay;
		}
	}
	

	
	
	public int handleDataPrefetchRequest(int handle, int offset, Cache self) {
		if(self.holdsValue(handle, offset)){
			busyTime++; //todo really inc?
			taskSimulator.addCoherenceControllerTask(new CoherenceControllerTask(TaskType.L1DataFetch, RequestType.Prefetch, 1, self.getCacheID()));
			prefAlreadyAvailable++;
			return -4;
		}
		
//		if(busyTime > 20){ // TODO find suitable threshold
//			busyTime++; //todo really inc?
//			return 0;
//		}
//		System.out.println("PREFETCHING " + self.getCacheID() + " handle: " + handle+ " offset: "+ offset);
		prefetched++;
		
		int delay = busyTime;

		int sharedCounter = 0;
		int sharedCache = 0;
		int sharedCacheSet = 0;
		int sharedIndex = 0;
		
		int getFromCache = -1;
		int getFromSet = -1;
		int getFromIndex = -1;
		
		fromL2Cache = false;
		isDataExclusive = true;
		
		for(int i = 0; i<caches.length; i++){
			if(caches[i] == self){
				continue; // Do not check the cache itself
			}
			MOESIState cacheResult = caches[i].checkState(handle, offset); //
			if(cacheResult != MOESIState.INVALID && cacheResult != MOESIState.SHARED){
				getFromCache = i;
				getFromSet = caches[i].getReturnSet();
				getFromIndex = caches[i].getReturnIndex();
				break;
			}
			if(cacheResult == MOESIState.SHARED){
				sharedCounter++;
				sharedCache = i;
				sharedCacheSet = caches[i].getReturnSet();
				sharedIndex = caches[i].getReturnIndex();
			}
		}
		if(getFromCache == -1 && sharedCounter > 0){
			getFromCache = sharedCache;
			getFromSet = sharedCacheSet;
			getFromIndex = sharedIndex;
//			return MOESIState.SHARED;
		}
//		return MOESIState.INVALID;
		CacheLine prefetchedData  = new CacheLine(WORDSPERLINE_L1);;
		
		
		if(getFromCache != -1){
//			prefetchedData = new CacheLine(BUSWIDTH_L1);
			
			for(int i = 0; i < WORDSPERLINE_L1; i++){
				prefetchedData.setData(i, caches[getFromCache].getCLData(getFromIndex, getFromSet, i));
			}
			prefetchedData.setMaxOffset(caches[getFromCache].getMaxOffset(getFromIndex, getFromSet));
			isDataExclusive = false;
			
			int additionalTime = WORDSPERLINE_L1 / BUSWIDTH_L1 + L1_READ_FROM_L1_OVERHEAD;
			
			delay += additionalTime;
			
//			delay+=L1_READ_FROM_L1;
			taskSimulator.addCoherenceControllerTask(new CoherenceControllerTask(TaskType.L1DataFetch, RequestType.Prefetch, additionalTime, self.getCacheID()));
			busyTime = delay;
			self.updateValue(handle, offset, prefetchedData);
			return 1 ; 
		}

		
		fromL2Cache = true;
		
		if(l2Cache.physicallyAddressed()){
			if(delay < busyTimeHT){
				delay = busyTimeHT;
			}

			delay += handleTableCache.requestData(handle);
			int baseAddress = handleTableCache.getAddr();
			int address = baseAddress + (((offset)>>>3)<<3);
			busyTimeHT = delay;
			


			delay+=2;
			if(l2Cache.holdsData(handle, offset, address) && l2Cache.holdsData(handle, offset, address+7)){
				if(delay < busyTimeL2){
					delay = busyTimeL2;
				}


				delay += WORDSPERLINE_L1 / BUSWIDTH_L2 + L1_READ_FROM_L2_OVERHEAD;

				delay += l2Cache.getLoadOverHead();


				int writeBackTime = 0;
				for(int i = 0; i < WORDSPERLINE_L1; i++){
					writeBackTime += l2Cache.requestData(handle, offset, address + i);
					prefetchedData.setData(i, l2Cache.getData(handle, offset, address + i));
				}
				taskSimulator.addCoherenceControllerTask(new CoherenceControllerTask(TaskType.L2DataFetch, RequestType.Prefetch, delay-busyTime, self.getCacheID()));
				busyTime = delay;
				busyTimeL2 = delay + writeBackTime;
				prefetchedData.setMaxOffset(0);
				self.updateValue(handle, offset, prefetchedData);
			} else {
				busyTimeL2 = delay +l2Cache.prefetchData(handle, offset, address);
				busyTimeL2 += l2Cache.prefetchData(handle, offset, address+7);
				taskSimulator.addCoherenceControllerTask(new CoherenceControllerTask(TaskType.L2DataFetch, RequestType.Prefetch, delay-busyTime, self.getCacheID()));
				prefetched--;
				busyTime = delay;
				
			}



			//		for(int i = 0; i < WORDS_PER_LINE; i++){
			//			
			//		}




			return 1;
		} else {
			// WAIT FOR L2
			if(delay < busyTimeL2){
				delay = busyTimeL2;
			}

			delay+=1;
			if(l2Cache.holdsData(handle, offset, 999)){
				int baseAddress = handleTableCache.addressFAKE(handle); // bissl doof programmiert TODO
				int address = baseAddress + (((offset)>>>3)<<3);
				
				for(int i = 0; i < WORDSPERLINE_L1; i++){
					l2Cache.requestData(handle, offset, address + i);
					prefetchedData.setData(i, l2Cache.getData(handle, offset, address + i));
				}
				
				delay += WORDSPERLINE_L1 / BUSWIDTH_L2 + L1_READ_FROM_L2_OVERHEAD;

				delay += l2Cache.getLoadOverHead();
				taskSimulator.addCoherenceControllerTask(new CoherenceControllerTask(TaskType.L2DataFetch, RequestType.Prefetch, delay-busyTime, self.getCacheID()));
				busyTime = delay;
				busyTimeL2 = delay;
				prefetchedData.setMaxOffset(0);
				self.updateValue(handle, offset, prefetchedData);
			} else {
				// WAIT FOR HT
				if(delay < busyTimeHT){
					delay = busyTimeHT;
				}
				
				delay += handleTableCache.requestData(handle);
				int baseAddress = handleTableCache.getAddr();
				int address = baseAddress + (((offset)>>>3)<<3);

				busyTimeHT = delay;
				
				busyTimeL2 = delay +l2Cache.prefetchData(handle, offset, address);
				taskSimulator.addCoherenceControllerTask(new CoherenceControllerTask(TaskType.L2DataFetch, RequestType.Prefetch, delay-busyTime, self.getCacheID()));
				busyTime = delay;
			}
			return 0;
		}
	}

	@Override
	public int writeBack(int handle, int offset, CacheLine cacheLine, boolean isPrefetch, Cache self) { //asdfjalösdfjaödsfj LUKAS:  MONTAG HIER WEITER MACHEN
		//		System.out.println("WRITEBACKKK");
		int delay = busyTime;
		

		if(l2Cache.physicallyAddressed()){
			// WAIT FOR HT
			if(busyTime < busyTimeHT){
				busyTime = busyTimeHT;
			}

			busyTime += handleTableCache.requestData(handle);
			busyTimeHT = busyTime;

			int baseAddress = handleTableCache.getAddr();
			int address = baseAddress + (((offset)>>>3)<<3);

			// WAIT FOR L2
			if(busyTime < busyTimeL2){
				busyTime = busyTimeL2;
			}

			busyTime += WORDSPERLINE_L1 / BUSWIDTH_L2 + L1_READ_FROM_L2_OVERHEAD;
			
			busyTime += l2Cache.getLoadOverHead();
			
			busyTimeL2 = busyTime;
			
			
			for(int i = 0; i <= cacheLine.getMaxOffset(); i++){
				if( !l2Cache.holdsData(handle, offset, address + i)){
					busyTimeL2 += l2Cache.getRAMaccessTime();
				}
				busyTimeL2 += l2Cache.writeData(handle, offset, address + i, cacheLine.getData(i));
			}

			taskSimulator.addCoherenceControllerTask(new CoherenceControllerTask(TaskType.L2DataWriteBack, isPrefetch?RequestType.Prefetch:RequestType.Regular, busyTime-delay, self.getCacheID()));

			return 321;
		} else {
			// WAIT FOR L2
			if(busyTime < busyTimeL2){
				busyTime = busyTimeL2;
			}
			
			if(l2Cache.holdsData(handle, offset, 999)){
//				busyTime++;
				int baseAddress = handleTableCache.addressFAKE(handle);
				int address = baseAddress + (((offset)>>>3)<<3);
				
				for(int i = 0; i <= cacheLine.getMaxOffset(); i++){
					busyTimeL2 += l2Cache.writeData(handle, offset, address + i, cacheLine.getData(i));
				}
				
				busyTime += WORDSPERLINE_L1 / BUSWIDTH_L2 + L1_READ_FROM_L2_OVERHEAD;
				
				busyTime += l2Cache.getLoadOverHead();
				
				busyTimeL2 = busyTime;
				
			} else {
				
				if(busyTime < busyTimeHT){
					busyTime = busyTimeHT;
				}

				busyTime += handleTableCache.requestData(handle);
				busyTimeHT = busyTime;
				
				busyTime += WORDSPERLINE_L1 / BUSWIDTH_L2 + L1_READ_FROM_L2_OVERHEAD;
				
				busyTime += l2Cache.getLoadOverHead();
				
				busyTimeL2 = busyTime;

				int baseAddress = handleTableCache.getAddr();
				int address = baseAddress + (((offset)>>>3)<<3);
				
				for(int i = 0; i <= cacheLine.getMaxOffset(); i++){
					if( !l2Cache.holdsData(handle, offset, address + i)){
						busyTimeL2 += l2Cache.getRAMaccessTime();
					}
					busyTimeL2 += l2Cache.writeData(handle, offset, address + i, cacheLine.getData(i));
				}
				
				
			}
			
			taskSimulator.addCoherenceControllerTask(new CoherenceControllerTask(TaskType.L2DataWriteBack, isPrefetch?RequestType.Prefetch:RequestType.Regular, busyTime-delay, self.getCacheID()));
			
			return 123; // TODO
		}
	}

	@Override
	public int writeNotification(int handle, int offset, CacheLine cacheLine, Cache self) {
//		accessPerTicks++;
//		writeNotAccPerTicks++;
		int delay = busyTime;
		if(alreadyWriteNotificationInTimeStep){
			delay--;
			System.out.println("Yoooooooooooooooo watt willst du???");
		}
		
		for(int i = 0; i<caches.length; i++){
			if(caches[i] == self){
				continue; // Do not notify the cache itself
			}
			
			caches[i].writeNotification(handle, offset);
			
			
		}
		if(!alreadyWriteNotificationInTimeStep){
			busyTime++;
			taskSimulator.addCoherenceControllerTask(new CoherenceControllerTask(TaskType.L1WriteNotification, RequestType.Regular, 1, self.getCacheID()));
		}
		
		isDataExclusive = true;
		alreadyWriteNotificationInTimeStep = true;
		return delay;
	}
	
	
//	LinkedHashMap<Cache,PrefetchRequest> prefetchRequests = new LinkedHashMap<>();
	
	

	@Override
	public int requestDataPrefetch(int handle, int offset, Cache self) {
		if(offset < 0){
			offset  =0;
		}
		PrefetchRequest[] ringBuffer = prefetchRequestRingBuffers.get(self);
		int ptr = writePointer.get(self);
		if(ringBuffer[ptr] != null){
			overWrittenPref++;
		}

		ringBuffer[ptr] = new PrefetchRequest(handle, offset);
		writePointer.put(self, (ptr+1)%ringBufferSize);
		readPointer.put(self, ptr);
				
		
		return 0;
	}

	int currentCache = 0;
	
	@Override
	protected int handleDataPrefetchRequest() {
		int startcache = currentCache;
		Cache actualCache = caches[currentCache];
		
		PrefetchRequest[] ringBuffer = prefetchRequestRingBuffers.get(actualCache);
		int ptr = readPointer.get(actualCache);
		
		
		PrefetchRequest currentreq = ringBuffer[ptr];
		currentCache++;
		currentCache = currentCache % caches.length;
		while(currentreq == null && currentCache != startcache){ // Simple round robin?
			actualCache = caches[currentCache];
			ringBuffer = prefetchRequestRingBuffers.get(actualCache);
			ptr = readPointer.get(actualCache);
			
			
			currentreq = ringBuffer[ptr];
			currentCache++;
			currentCache = currentCache % caches.length;
		}
		
		readPointer.put(actualCache, (ptr+ ringBufferSize -1)%ringBufferSize);
		
		if(currentreq != null){
			int retVal = handleDataPrefetchRequest(currentreq.getHandle(), currentreq.getOffset(), actualCache);
//			System.err.println("PREFETCHED FOR cache " + actualCache.getCacheID() + " start: " + startcache + " " + prefetchRequests);
//			prefetchRequests.remove(actualCache);
			ringBuffer[ptr] = null;
			if(retVal == -4){
				actualCache.prefetchDone(currentreq);
			}
		} else {
			currentCache++;
			currentCache = currentCache % caches.length;
		}
		if(kernelRunningOnCGRA){
			prefetchTicks+=busyTime;
		}
		return 0;
	}

	@Override
	public boolean updatingL2Cache() {
		return false;
	}
	
	
	

}
