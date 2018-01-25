package de.amidar;

import java.util.ArrayList;

public abstract class AmidarSystem {
	private AmidarSystem () {
	}

	private static HashMapIdentity peripherals;

	// Not needed for Simulation
//	static {
//		if ((AmidarSystem.ReadAddress (0x90030000) & 0x01) > 0) { // Only when Bootloader UART bit is set (i.e. don't clear in simulation)
//			AmidarSystem.ClearMem (2*1024*1024);
//		}
//		
//		SetupPeripherals ();
//	}

	public static final int AXT_THREAD_WRAPPER_AMTI_ADDRESS = 0x3c;

	private static final int HW_PERI_ROM_ADDRESS = 0x80000000;

	private static final int HW_HEAP_ADDRESS_BASE = 0x80020000;
	private static final int HEAP_HANDLETABLE_OFFSET_REGISTER = 2;
	private static final int HEAP_PERITABLE_OFFSET_REGISTER = 3;
	private static final int HEAP_HEAP_BASE_REGISTER = 5;
	private static final int HEAP_FREE_HEAP_OFFSET_REGISTER = 6;
	private static final int HEAP_NEXT_HEAP_OFFSET_REGISTER = 16;

	private static final int HW_HANDLETABLE_ENTRY_WIDTH = 16;

	private static void SetupPeripherals () {
		if (peripherals == null) {
			HashMapInt hwNamesTable = new HashMapInt (45);
			peripherals = new HashMapIdentity (45);

			int periTableBase = AmidarSystem.ReadAddress (HW_HEAP_ADDRESS_BASE
					+ HEAP_PERITABLE_OFFSET_REGISTER);

			int curPeriRomAddress = HW_PERI_ROM_ADDRESS;

			int periEntryCount = ReadAddress (curPeriRomAddress++);

			while (periEntryCount-- > 0) {
				int handleCount = ReadAddress (curPeriRomAddress++);
				int handleStart = ReadAddress (curPeriRomAddress++);
				int periClassnameOffset = ReadAddress (curPeriRomAddress++);

				if (handleCount > 0) {
					String periClassname = readPeriName (periClassnameOffset,
							hwNamesTable);
					Class periClass = null;//Class.forName (periClassname);

					if (periClass != null) {
						if (!peripherals.containsKey (periClass)) {
							peripherals.put (periClass, new ArrayList (
									handleCount * 2));
						}
						ArrayList peris = (ArrayList) peripherals
								.get (periClass);

						for (int i = 0; i < handleCount; i++) {
							int periHandle = handleStart + i;
							writePeriTableEntry (periTableBase, periHandle, 0);
								//	periClass.getCti ());
							peris.add (IntToRef (periHandle));
						}
					}
				}
			}
		}
	}

	private static String readPeriName (int periClassnameOffset,
			HashMapInt hwNamesTable) {
		int absAddress = HW_PERI_ROM_ADDRESS + periClassnameOffset;
		if (hwNamesTable.containsKey (absAddress)) {
			return (String) hwNamesTable.get (absAddress);
		} else {
			String name = readString (absAddress);
			hwNamesTable.put (absAddress, name);
			return name;
		}
	}

	/**
	 * Get a list of all peripherals of a peripheral class
	 * 
	 * @param classObj
	 *            Class representing the peripheral
	 * @return List of all peripherals with the given class if there are any,
	 *         null otherwise
	 */
	public static ArrayList GetPeripherals (Class classObj) {
		// TODO: remove when static initializer is used
		if (peripherals == null) {
			SetupPeripherals ();
		}
		return (ArrayList) peripherals.get (classObj);
	}

	/**
	 * Get a peripheral handle based on the peripheral class and an index
	 * 
	 * @param classObj
	 *            Class representing the peripheral
	 * @param index
	 *            Index of all peripherals of the given class
	 * @return A peripheral handle if there are at least index+1 instances of
	 *         the queried peripheral, null otherwise
	 */
	public static AmidarPeripheral GetPeripheral (Class classObj, int index) {
		ArrayList peris = GetPeripherals (classObj);
		if (peris == null)
			return null;
		if (index >= peris.size ())
			return null;
		return (AmidarPeripheral) peris.get (index);
	}

	/**
	 * Write an entry in the peripherals table
	 * 
	 * @param periTableBase
	 *            Base address of the peripherals table
	 * @param handle
	 *            Number of peripheral handle
	 * @param cti
	 */
	private static void writePeriTableEntry (int periTableBase, int handle,
			int cti) {
		int physAddress = periTableBase + (handle & ~0x80000000)
				* HW_HANDLETABLE_ENTRY_WIDTH;
		WriteAddress (physAddress + 0, 0); // MID
		WriteAddress (physAddress + 4, cti << 16); // {CTI, Flags}
		WriteAddress (physAddress + 8, 0); // Size
		WriteAddress (physAddress + 12, 0); // Address
	}
	
	public static int[] readHandle (int handle) {
		int handleTableBase = AmidarSystem.ReadAddress (HW_HEAP_ADDRESS_BASE
				+ HEAP_HANDLETABLE_OFFSET_REGISTER);
		int physAddress = handleTableBase + handle * HW_HANDLETABLE_ENTRY_WIDTH;
		
		int[] result = new int[HW_HANDLETABLE_ENTRY_WIDTH / 4];
		for (int i = 0; i < HW_HANDLETABLE_ENTRY_WIDTH / 4; i++) {
			int data = ReadAddress (physAddress + i * 4);
			result[i] = data;
		}
		
		return result;
	}

	public static int[] readHandle (Object obj) {
		return readHandle (AmidarSystem.RefToInt (obj));
	}

	/**
	 * Internal method to read a null-terminated ASCII string from a memory.
	 * 
	 * @param address
	 *            Start address of string
	 * @return The String object
	 */
	private static String readString (int address) {
		char[] chars = new char[40];
		int length = 0;

		boolean done = false;
		do {
			int data = ReadAddress (address);
			address++;
			int i = 4;
			while (i > 0) {
				char c = (char) (data & 0xff);
				if (c > 0) {
					if (length == chars.length) {
						char[] newChars = new char[length + 40];
						System.arraycopy (chars, 0, newChars, 0, length);
						chars = newChars;
					}
					data = (data >>> 8);
					chars[length++] = c;
					i--;
				} else {
					done = true;
					break;
				}
			}
		} while (!done);

		return new String (chars, 0, length);
	}

	/**
	 * Initialize heap memory with zeros without overwriting data from AXT or
	 * already created objects.
	 * 
	 * @param bytes
	 *            Number of bytes to zero. Counting from memory address 0 so
	 *            make sure this number is bigger than the size of the loaded
	 *            AXT file
	 */
	public static void ClearMem (int bytes) {
		int freeHeapOffset = AmidarSystem.ReadAddress (HW_HEAP_ADDRESS_BASE
				+ HEAP_NEXT_HEAP_OFFSET_REGISTER) * 4;
		int heapBase = AmidarSystem.ReadAddress (HW_HEAP_ADDRESS_BASE
				+ HEAP_HEAP_BASE_REGISTER);
		int freeHeapStart = heapBase + freeHeapOffset;
		int endAddress = freeHeapStart + bytes;

		for (int i = freeHeapStart; i < endAddress; i += 4) {
//			AmidarSystem.WriteAddress (i, 0);
		}
	}

	/**
	 * Internally used by the TokenMachine to handle unchecked exceptions from
	 * FUs. Processes the exception info from TM and generates the appropriate
	 * Exception object.
	 * 
	 * @throws RuntimeException
	 *             The generated exception
	 */
	protected final static void UncheckedExceptionHandler ()
			throws RuntimeException {
		// TODO: Check exception source, build exception object, throw it
		if (System.err != null) {
			System.err.println ("Unchecked exception");

			System.err.println ("FU:   " + AmidarSystem.ReadAddress (0x80010028));
			System.err.println ("ID:   " + AmidarSystem.ReadAddress (0x80010029));
			System.err.println ("AMTI: " + AmidarSystem.ReadAddress (0x8001002a));
			System.err.println ("PC:   " + AmidarSystem.ReadAddress (0x8001002b));
		}

		throw new RuntimeException ("Unchecked exception");
	}


	// ------------------------------------------------------------------------------
	// Patched methods: AXT Converter replaces INVOKESTATIC by new individual
	// bytecodes

	/**
	 * Read a 32 bit word from a physical address
	 * 
	 * @param address
	 *            Address to read from
	 * @return 32 bit word
	 */
	public static native int ReadAddress (int address);

	/**
	 * Write a 32 bit word to a physical address
	 * 
	 * @param address
	 *            Address to write to
	 * @param value
	 *            32 bit word to write
	 */
	public static native void WriteAddress (int address, int value);

	/**
	 * Retrieve the value from the MonitorID column in the handle table for the
	 * given object
	 * 
	 * @param obj
	 *            Object to retrieve MonitorID from
	 * @return Monitor ID
	 */
	public static native int GetMonitorId (Object obj);

	/**
	 * Disable scheduler, implemented as bytecode so there is no delay between
	 * the Java command and the scheduler receiving it
	 */
	public static native void DisableScheduling ();

	/**
	 * Forces a scheduler run right now and also (re)enables the scheduler
	 */
	public static native void ForceScheduling ();

	/**
	 * Enable scheduler, implemented as bytecode for symmetry with
	 * DisableScheduling
	 */
	public static native void EnableScheduling ();

	/**
	 * Convert an integer to a reference (sets REFERENCE flag in Stack)
	 * 
	 * @param value
	 *            Value to convert to a handle
	 * @return The converted handle
	 */
	public static native Object IntToRef (int value);

	/**
	 * Convert an reference to integer (sets the VALUE flag in Stack)
	 * 
	 * @param ref
	 *            Value to convert to an integer
	 * @return The converted integer
	 */
	public static native int RefToInt (Object ref);
	
	
	public static void invalidateFlushAllCaches(){
		
	}

	public static void arrayCopy(byte[] src, int srcOffset, byte[] dst, int dstOffset, int length){
		for(int i = 0; i < length; i++){
			dst[dstOffset + i ] = src[srcOffset + i];
		}
		
		
	}
	
	public static void arrayCopy(int[] src, int srcOffset, int[] dst, int dstOffset, int length){
		for(int i = 0; i < length; i++){
			dst[dstOffset + i ] = src[srcOffset + i];
		}
		
		
	}

	private static int bmpWidth;
	private static int bmpHeight;
	
	private static int getBMPWidth(){
		return 8;
	}
	
	private static int getBMPHeight(){
		return 8;
	}
	
	private static void loadPixels(int [] pixels){
		
	}
	
	
	public static int[] readBMP(int scale, int factor) {
		bmpWidth = getBMPWidth();
		bmpHeight = getBMPHeight();
	
		
		
		
		int length = bmpWidth * bmpHeight * factor;
		
		int h = 1<<(scale/2);
		int w = factor/h;
		
		bmpWidth *= w;
		bmpHeight *= h;
		
		
		int [] ret = new int[length];
		
		loadPixels(ret);
		
		return ret;
	}

	public static int getBMPwidth() {
		
		return bmpWidth;
	}

	public static int getBMPheight() {
		return bmpHeight;
	}
	
}
