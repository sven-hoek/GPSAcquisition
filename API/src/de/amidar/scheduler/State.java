package de.amidar.scheduler;

import de.amidar.AmidarPeripheral;
import de.amidar.BitOperations;

public class State implements AmidarPeripheral {
	public boolean enableScheduling;
	public int currentThreadId;
	private int tickCount_low;
	private int tickCount_high;
	public int frequency;
	
	public long GetTickCount () {
		// Read low first, high gets buffered in hardware for following read access to simulate an atomic 64 bit
		int low = tickCount_low;
		return BitOperations.CombineUint32ToLong (tickCount_high, low);
	}
	
	public long GetSystemUptimeMillis () {
		return 1000 * GetTickCount () / frequency;
	}
	
	private State () {}
}
