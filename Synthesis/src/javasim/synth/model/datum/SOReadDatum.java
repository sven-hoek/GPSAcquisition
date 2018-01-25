package javasim.synth.model.datum;

import javasim.synth.model.instruction.Instruction;

/**
 * A single node of the data flow graph representing
 * the read from a static object reference.
 *
 * @author Michael Raitza
 * @version 14.04.2011
 */
public class SOReadDatum extends Datum {

	private SOReadDatum() {
		super(0, null);
	}

	/**
	 * Constructs a new static object reference read node.
	 * @param value the "value" associated with this datum. The meaning of value highly
	 * depends on the type of node
	 * @param creator the instruction which created this node
	 */
	public SOReadDatum(Number value, Instruction creator) {
		super(value, creator);
	}

	public AccessType accessType(){
		return Datum.AccessType.READ;
	}

	public Type type() {
		return Datum.Type.STATIC_OBJECT;
	}

}
