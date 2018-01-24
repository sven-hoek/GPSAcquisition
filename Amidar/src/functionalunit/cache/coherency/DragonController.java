package functionalunit.cache.coherency;

import java.util.LinkedHashMap;

import functionalunit.cache.Cache;
import functionalunit.cache.CacheLine;
import functionalunit.cache.HandleTableCache;
import functionalunit.cache.L2CachePhysical;
import functionalunit.cache.coherency.CoherenceController.PrefetchRequest;
import functionalunit.cache.coherency.CoherenceControllerTask.RequestType;
import functionalunit.cache.coherency.CoherenceControllerTask.TaskType;

public class DragonController extends MOESIController {
	
//	public DragonController(Cache[] caches, L2Cache l2Cache, HandleTableCache handleTableCache) {
//		super(caches, l2Cache, handleTableCache);
//		
//		prefetchRequestRingBuffers = new LinkedHashMap<>();
//		readPointer = new LinkedHashMap<>();
//		writePointer = new LinkedHashMap<>();
//		
//		for(Cache c: caches){
//			prefetchRequestRingBuffers.put(c, new PrefetchRequest[ringBufferSize]);
//			readPointer.put(c, 0);
//			writePointer.put(c, 0);
//		}
//		
//	}
	
	public int writeNotification(int handle, int offset, CacheLine cacheLine, Cache self) {

		int delay = busyTime;
		
		boolean updated = false;
		
		for(int i = 0; i<caches.length; i++){
			if(caches[i] == self){
				continue; // Do not notify the cache itself
			}
			boolean locupdated = caches[i].updateNotification(handle, offset, cacheLine); 
			updated |= locupdated;
			
			if(locupdated){
				loadedData++;
//				System.out.println("updated cache " + i + " from cache " + self.getCacheID() + " handle: " + handle +  " offset: "+offset);
			}
			
			
		}
		
		busyTime += WORDSPERLINE_L1 / BUSWIDTH_L1;// + L1_READ_FROM_L1_OVERHEAD;
		taskSimulator.addCoherenceControllerTask(new CoherenceControllerTask(TaskType.L1WriteNotification, RequestType.Regular, busyTime-delay, self.getCacheID()));
//		boolean dummy = isDataExclusive;
		isDataExclusive = !updated;
//		isDataExclusive = true;
		return delay;
	}

}
