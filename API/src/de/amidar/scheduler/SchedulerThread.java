package de.amidar.scheduler;

import de.amidar.AmidarPeripheral;
import de.amidar.BitOperations;

public class SchedulerThread implements AmidarPeripheral {
	public boolean isReady;
	public int priority;
	public int amti;
	public int pc;
	
	/**
	 * Reference to the object handle that this thread is waiting on.
	 * 
	 * Only stores 15 bits of the reference (bits 14 - 0) internally as it is
	 * only used for peripheral interrupts in hardware. Peripheral objects
	 * always have handles between 0x8000_0000 and 0x8000_7FFF.
	 */
	public Object monitorObject;
	
	public boolean hasWaitTimeout;
	public int waitTimeout_low;
	public int waitTimeout_high;
	public boolean isStarted;			// only for debugger
	
	public long GetTimeout () {
		return BitOperations.CombineUint32ToLong (waitTimeout_high, waitTimeout_low);
	}

	private SchedulerThread () {}
}
