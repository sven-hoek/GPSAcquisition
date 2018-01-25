package javasim.synth.model.datum;

import javasim.synth.model.instruction.Instruction;

/**
 * A single node of the data flow graph representing
 * the write of a local variable.
 *
 * @author Michael Raitza
 * @version 14.04.2011
 */
public class LWriteDatum extends Datum {
	
	/**
	 * Stores the Datum which produces the value that'll be stored
	 */
	private Datum source = null;
	
	/**
	 * Denotes whether the corresponding store instruction is necessary.
	 * Necessary stores are the ones before the start and end of a branch/loop.
	 */
	private boolean necessary = false;
	
	private LWriteDatum() {
		super(0, null);
	}

	/**
	 * Constructs a new local write node.
	 * @param value the "value" associated with this datum. The meaning of value highly
	 * depends on the type of node
	 * @param creator the instruction which created this node
	 */
	public LWriteDatum(Number value, Instruction creator) {
		super(value, creator);
	}
	
	
	public LWriteDatum(Number value, Instruction creator, Datum source) {
		super(value, creator);
		this.source = source;
	}

	public AccessType accessType(){
		return Datum.AccessType.WRITE;
	}

	public Type type() {
		return Datum.Type.LOCAL_VARIABLE;
	}

	/**
	 * Returns the Datum that produces the value to be stored
	 * This is needed for folding
	 * @return
	 */
	public Datum getSource(){
		return source;
	}
	
	/**
	 * Defines this store as necessary
	 */
	public void defineAsNecessary(){
		necessary = true;
	}
	
	/**
	 * Defines this store as unnecessary
	 */
	public void defineAsUnNecessary(){
		necessary = false;
	}
	
	/**
	 * Returns whether this store is necessary
	 * @return
	 */
	public boolean isNecessary(){
		return necessary;
	}

}
