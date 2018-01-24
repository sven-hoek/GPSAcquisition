package de.amidar;

public class FrameStack implements AmidarPeripheral {
	// Reg 0 (R): Number of thread slots
	// Reg 1 (R): Max words per thread
	// Reg 2 (R): Number of local variables reserved for initializer code

	// Reg 4 (R/W): Currently selected thread id to read/write information on
	// Reg 5 (R/W): Current locals pointer of thread selected in reg 4
	// Reg 6 (R/W): Current stack pointer of thread selected in reg 4
	// Reg 7 (R/W): Current callercontext pointer of thread selected in reg 4
	// Reg 8 (R/W): Current max pointer of thread selected in reg 4
	// Reg 9 (R/W): Thread selected in reg 4 in overflow state

	// Reg 12 (R/W): Currently selected memory address (relative to the currently selected thread id) to read/write data to
	// Reg 13 (R/W): Current memory data at address selected by {reg4, reg 12}
	// Reg 14 (R/W): Current memory entry type data at address selected by {reg4, reg 12}

	public int threadSlots;
	public int wordsPerThread;
	public int localsForInitializer;
	
	private int unused3;
	
	public int threadSelect;
	public int localsPointer;
	public int stackPointer;
	public int callercontextPointer;
	public int maxPointer;
	public int overflow;

	private int unused10;
	private int unused11;

	public int stackAddressSelect;
	public int stackData;
	public int stackMeta;

	public static final int CALLERCONTEXT_WORDS = 4;
	public static final int ENTRYTYPE_EMPTY = 0;
	public static final int ENTRYTYPE_META = 1;
	public static final int ENTRYTYPE_REF = 2;
	public static final int ENTRYTYPE_VALUE = 3;
	
	private FrameStack () {}
}
