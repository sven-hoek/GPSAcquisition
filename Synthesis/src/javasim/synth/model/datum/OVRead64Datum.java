package javasim.synth.model.datum;

import javasim.synth.model.instruction.Instruction;

/**
 * A single node of the data flow graph representing
 * the read from a dynamic object reference.
 *
 * @author Michael Raitza
 * @version 14.04.2011
 */
public class OVRead64Datum extends OVReadDatum {

	private OVRead64Datum() {
		super(null, 0, null);
	}

	/**
	 * Constructs a new dynamic object reference read node.
	 * @param reference the reference this dynamic object lives at
	 * @param value the "value" associated with this datum. The meaning of value highly
	 * depends on the type of node
	 * @param creator the instruction which created this node
	 */
	public OVRead64Datum(Datum reference, Number value, Instruction creator) {
		super(reference ,value, creator);
	}

}
