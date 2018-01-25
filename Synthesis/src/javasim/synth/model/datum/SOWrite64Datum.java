package javasim.synth.model.datum;

import javasim.synth.model.instruction.Instruction;

/**
 * A single node of the data flow graph representing
 * the write to a static object reference.
 *
 * @author Michael Raitza
 * @version 14.04.2011
 */
public class SOWrite64Datum extends SOWriteDatum {

	private SOWrite64Datum() {
		super(0, null);
	}

	/**
	 * Constructs a new static object reference write node.
	 * @param value the "value" associated with this datum. The meaning of value highly
	 * depends on the type of node
	 * @param creator the instruction which created this node
	 */
	public SOWrite64Datum(Number value, Instruction creator) {
		super(value, creator);
	}

}
