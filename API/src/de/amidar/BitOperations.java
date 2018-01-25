package de.amidar;

public class BitOperations {
	private BitOperations () {}
	
	public static int Uint8ToInt32 (byte uint8) {
		return ((int)uint8) & 0x000000ff;
	}

	public static int Uint16ToInt32 (short uint16) { 
		return ((int)uint16) & 0x0000ffff;
	}

	public static long Uint32ToInt64 (int uint32) {
		return ((long)uint32) & 0xffffffffL;
	}
	
	public static long CombineUint32ToLong (int uint32_high, int uint32_low) {
		return (Uint32ToInt64 (uint32_high) << 32) | Uint32ToInt64 (uint32_low);
	}
}
