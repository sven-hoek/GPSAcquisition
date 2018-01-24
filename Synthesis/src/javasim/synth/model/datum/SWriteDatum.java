package javasim.synth.model.datum;

import javasim.synth.model.instruction.Instruction;


/**
 * A single node of the data flow graph representing
 * the write onto the stack from an operation.
 *
 * @author Michael Raitza
 * @version 14.04.2011
 */
public class SWriteDatum extends Datum {

	private SWriteDatum() {
		super(0, null);
	}

	/**
	 * Constructs a new stack write node.
	 * @param value the "value" associated with this datum. The meaning of value highly
	 * depends on the type of node
	 * @param creator the instruction which created this node
	 */
	public SWriteDatum(Number value, Instruction creator) {
		super(value, creator);
	}

	public AccessType accessType(){
		return Datum.AccessType.STACK;
	}

	public Type type() {
		return Datum.Type.STACK;
	}
}
