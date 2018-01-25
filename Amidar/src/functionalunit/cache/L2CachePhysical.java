package functionalunit.cache;

import java.util.Random;

import tracer.Trace;
import tracer.TraceManager;
import functionalunit.cache.Cache.MOESIState;

public class L2CachePhysical extends L2Cache {
	

	
	public L2CachePhysical(Memory memory, String configFile, TraceManager traceManager){
		super(memory, configFile, traceManager);
	}
	
	private void generateCacheAddr(int physicalAddress){
		
		int boMask = (int)Math.pow(2, boBits)-1;
		blockOffset = physicalAddress & boMask;
		index = physicalAddress >> boBits;
		
		int indexMask = (int)Math.pow(2, indexBits)-1;
		index = index & indexMask;
		
		tag = physicalAddress>>(indexBits + boBits);
		
	}
	
	
	public int requestData(int physicalAddress){
		int waitTime = 0;
		readAccesses++;
		generateCacheAddr(physicalAddress);
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
	
	public int writeData(int physicalAddress, int data){
		int waitTime = 0;
		writeAccesses++;
		generateCacheAddr(physicalAddress);
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
	
	public int getData(int physicalAddress){
		return memory[physicalAddress];
	}
	
	public boolean holdsData(int physicalAddress){
		generateCacheAddr(physicalAddress);
//		globalWaitTime++;
		return findSet() != -1;
	}
	
	public int prefetchData(int physicalAddress){
		int waitTime = SINGLE_VALUE_TIME;
//		readAccesses++;
		generateCacheAddr(physicalAddress);
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
		return prefetchData(physicalAddress);
	}

	@Override
	public boolean holdsData(int handle, int offset, int physicalAddress) {
		return holdsData(physicalAddress);
	}

	@Override
	public int getData(int handle, int offset, int physicalAddress) {
		return getData(physicalAddress);
	}

	@Override
	public int writeData(int handle, int offset, int physicalAddress, int data) {
		return writeData(physicalAddress, data);
	}

	@Override
	public int requestData(int handle, int offset, int physicalAddress) {
		return requestData(physicalAddress);
	}

	@Override
	public void writeDataPhysical(int addr, int data) {
		writeData(addr, data);
		
	}

	@Override
	public int readDataPhysical(int addr) {
		return getData(addr);
	}

	@Override
	public boolean physicallyAddressed() {
		return true;
	}

	@Override
	public int getLoadOverHead() {
		return 2; // For Cacheline Alignment
	}
	
	
	

}
