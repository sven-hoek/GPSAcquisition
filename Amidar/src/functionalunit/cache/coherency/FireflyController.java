package functionalunit.cache.coherency;

import functionalunit.cache.Cache;
import functionalunit.cache.CacheLine;
import functionalunit.cache.coherency.CoherenceControllerTask.RequestType;
import functionalunit.cache.coherency.CoherenceControllerTask.TaskType;

public class FireflyController extends MOESIController {
	
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
//				System.out.println("updated cache " + i + " from cache " + self.getCacheID() + " handle: " + handle +  " offset: "+offset);
			}
			
			
		}
		busyTime += WORDSPERLINE_L1 / BUSWIDTH_L1;
		taskSimulator.addCoherenceControllerTask(new CoherenceControllerTask(TaskType.L1WriteNotification, RequestType.Regular, 1, self.getCacheID()));
//		boolean dummy = isDataExclusive;
		
		
		if(l2Cache.physicallyAddressed()){

			int l2UpdateStartTime = busyTime;
			
			// WAIT FOR HT
			if(busyTime < busyTimeHT){
				busyTime = busyTimeHT;
			}

			busyTime += handleTableCache.requestData(handle);
			int baseAddress = handleTableCache.getAddr();
			
			busyTimeHT = busyTime;

			// WAIT FOR L2
			if(busyTime < busyTimeL2){
				busyTime = busyTimeL2;
			}
			
			busyTime += WORDSPERLINE_L1 / BUSWIDTH_L2 + L1_READ_FROM_L2_OVERHEAD;
			
			busyTime += l2Cache.getLoadOverHead();
			
			busyTimeL2 = busyTime;
			
			if( !l2Cache.holdsData(handle, offset, baseAddress + offset)){
				busyTimeL2 += l2Cache.getRAMaccessTime();
			}
			
			busyTimeL2 += l2Cache.writeData(handle, offset, baseAddress + offset, cacheLine.getData(offset& 0x7)); //TODO

			taskSimulator.addCoherenceControllerTask(new CoherenceControllerTask(TaskType.L2DataWriteBack, RequestType.Regular, busyTime-l2UpdateStartTime, self.getCacheID()));
//			busyTime+= l2UpdateTime;

			isDataExclusive = !updated;
			//		isDataExclusive = true;
			return delay;
		}else {
			
			// WAIT FOR L2
			if(busyTime < busyTimeL2){
				busyTime = busyTimeL2;
			}
			
			if(!l2Cache.holdsData(handle, offset, 999)){
				// WAIT FOR HT
				if(busyTime < busyTimeHT){
					busyTime = busyTimeHT;
				}
				
				busyTime += handleTableCache.requestData(handle);
				int baseAddress = handleTableCache.getAddr();
				
				busyTimeHT = busyTime;
				
//				busyTime += WORDSPERLINE_L1 / BUSWIDTH_L2 + L1_READ_FROM_L2_OVERHEAD;
//
//				busyTime += l2Cache.getLoadOverHead();

				busyTimeL2 = busyTime;
				
				busyTimeL2 += l2Cache.getRAMaccessTime();
				
				busyTimeL2 += l2Cache.writeData(handle, offset, baseAddress + offset, cacheLine.getData(offset& 0x7)); 
			} else {
				int baseAddress = handleTableCache.addressFAKE(handle);
				l2Cache.writeData(handle, offset, baseAddress + offset, cacheLine.getData(offset& 0x7)); 
			}

			
			
			
			
			return delay;
		}
	}
	
	public boolean updatingL2Cache() {
		return true;
	}

}
