package de.amidar;

public class Heap implements AmidarPeripheral {
	// Addr 0  (R)   : Status
	// Addr 1  (R/W) : Command
	//		               Bit 0 --> Reset Cache Stats ( High Active )
	//		               Bit 1 --> Set Object Cache into Arbit-LRU Mode
	// Addr 2  (R/W) : Handle Table Offset (Physical Start Adress) 
	// Addr 3  (R/W) : Peri Table Offset (Physical Start Adress)
	
	// Addr 4  (R/W) : First Object handle to be alloced (Offset to existing Objects)
	// Addr 5  (R/W) : Object Memory (Physical Start Adress with offset to fixed Objects)
	// Addr 6  (R/W) : First non-static heap address
	// Addr 7  (R)   : Read Hit Rate of Data Cache
	
	// Addr 8  (R)   : Reas Miss Rate of Data Cache
	// Addr 9  (R)   : Write Hit Rate of Data Cache
	// Addr 10 (R)   : Write Miss Rate of Data Cache
	// Addr 11 (R)   : WriteBack on Read of Data Cache
	
	// Addr 12 (R)   : WriteBack on Write of Data Cache
	// Addr 13 (R)   : Read Hit Rate of HandleTable Cache
	// Addr 14 (R)   : Reas Miss Rate of HandleTable Cache
	// Addr 15 (R)   : Next free Handle Number

	public int status;
	public int command;
	public int handleTableOffset;
	public int periTableOffset;
	
	public int firstDynamicHandle;
	public int heapStartAddress;
	public int firstDynamicHeapOffset;
	public int dataCacheReadHitRate;
	
	public int dataCacheReadMissRate;
	public int dataCacheWriteHitRate;
	public int dataCacheWriteMissRate;
	public int dataCacheReadWriteBack;

	public int dataCacheWriteWriteBack;
	public int handleCacheReadHitRate;
	public int handleCacheReadMissRate;
	public int nextFreeHandle;
	
	public int nextFreeHeapOffset;
	

	private Heap () {}
}
