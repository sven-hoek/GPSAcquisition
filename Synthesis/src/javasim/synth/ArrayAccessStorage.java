package javasim.synth;

/**
 * Stores array accesses for loop classification
 * @author jung
 *
 */
public class ArrayAccessStorage implements Comparable<ArrayAccessStorage> {

	// Direct means array index is load from memory - may be LoopIndex or other variable (See SynthData.checkIndexAccess())
	private boolean direct = false;
	private int addr = -1;
	private int indexVar;
	
	public boolean isDirect() {
		return direct;
	}
	public int getAddr() {
		return addr;
	}
	public int getIndexVar() {
		return indexVar;
	}
	
	public ArrayAccessStorage(boolean direct, int addr, int indexVar){
		this.direct = direct;
		this.addr = addr;
		this.indexVar = indexVar;
	}
	public ArrayAccessStorage(boolean direct, int addr){
		this(direct, addr, 0);
	}

	public int compareTo(ArrayAccessStorage o) {
		if (this.addr < o.getAddr()) return -1;
		if (this.addr > o.getAddr()) return 1;
		return 0;
	}
}
