package javasim.synth.model.datum;

import javasim.synth.model.instruction.Instruction;

/**
 * A single node of the data flow graph representing
 * the read of a constant.
 *
 * @author Michael Raitza
 * @version 14.04.2011
 */
public class ConstDatum extends Datum {

	private ConstDatum() {
		super(0, null);
	}

	/**
	 * Constructs a new constant read datum.
	 * @param value the "value" associated with this datum. The meaning of value highly
	 * depends on the type of node and can be an address or a constant value.
	 * @param creator the instruction which created this node
	 */
	public ConstDatum(Number value, Instruction creator) {
		super(value, creator);
	}

	public AccessType accessType(){
		return Datum.AccessType.WRITE;
	}

	public Type type() {
		return Datum.Type.CONSTANT;
	}

}
