package functionalunit.cache;

import tracer.TraceManager;

public class L2CacheVirtual extends L2Cache {

	public L2CacheVirtual(Memory memory, String configFile,	TraceManager traceManager) {
		super(memory, configFile, traceManager);
	}
	
	private int generateCacheAddr(int handle, int offset){
		int boMask = (int)Math.pow(2, boBits)-1;
		blockOffset = offset & boMask;
		
		int offsetBits = 4;
		
		index = ((offset>>boBits) ) ^ (handle<<offsetBits);
		
		index = index%CACHELINES;
		
		tag = ((long)handle << 32) + (offset&((~0)<<boBits));
		
		
		return 0;
	}
	
	public int requestData(int handle, int offset){
		int waitTime = 0;
		readAccesses++;
		generateCacheAddr(handle, offset);
		int set = findSet();
		if( set == -1){ // MISS
			readMisses++;
//			waitTime += RAM_ACCESS_TIME + globalWaitTime; // TODO
			
			set = decisionPLRU(index);
			if(dirty[set][index]){
				waitTime = RAM_ACCESS_TIME;
			} else {
				waitTime = 0;
			}
			tagMemory[set][index] = tag;
			valid[set][index] = true;
			dirty[set][index] = false;
		} 
		setPLRU(index, set);
		return waitTime;
	}
	
	public boolean holdsData(int handle, int offset){
		generateCacheAddr(handle, offset);
//		globalWaitTime++;
		return findSet() != -1;
	}
	
	public int prefetchData(int handle, int offset){
		int waitTime = SINGLE_VALUE_TIME;
//		readAccesses++;
		generateCacheAddr(handle, offset);
		int set = findSet();
		if( set == -1){ // MISS
//			readMisses++;
			waitTime = RAM_ACCESS_TIME + waitTime; // TODO
			
			set = decisionPLRU(index);
			if(dirty[set][index]){
				waitTime += RAM_ACCESS_TIME;
			} else {
				waitTime += 0;
			}
			tagMemory[set][index] = tag;
			valid[set][index] = true;
			dirty[set][index] = false;
		} 
		setPLRU(index, set);
		return waitTime;
	}
	

	@Override
	public int prefetchData(int handle, int offset, int physicalAddress) {
		return prefetchData(handle, offset);
	}

	@Override
	public boolean holdsData(int handle, int offset, int physicalAddress) {
		return holdsData(handle, offset);
	}

	@Override
	public int getData(int handle, int offset, int physicalAddress) {
		return memory[physicalAddress];
	}

	@Override
	public int writeData(int handle, int offset, int physicalAddress, int data) {
		int waitTime = 0;
		writeAccesses++;
		generateCacheAddr(handle, offset);
		int set = findSet();
		if(set == -1){
			writeMisses++;
			
//			globalWaitTime += RAM_ACCESS_TIME;
			
			set = decisionPLRU(index);
			if(dirty[set][index]){
				waitTime = RAM_ACCESS_TIME;
			} else {
				waitTime = 0;
			}
			tagMemory[set][index] = tag;
			valid[set][index] = true;
			
		}
		memory[physicalAddress] = data;
		dirty[set][index] = true;
		setPLRU(index, set);
		return waitTime;
	}

	@Override
	public int requestData(int handle, int offset, int physicalAddress) {
		return requestData(handle, offset);
	}

	@Override
	public void writeDataPhysical(int addr, int data) {
		memory[addr] = data;

	}

	@Override
	public int readDataPhysical(int addr) {
		return memory[addr];
	}

	@Override
	public boolean physicallyAddressed() {
		return false;
	}

}
