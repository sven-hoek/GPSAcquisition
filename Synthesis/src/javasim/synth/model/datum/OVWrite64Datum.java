package javasim.synth.model.datum;

import javasim.synth.model.instruction.Instruction;

/**
 * A single node of the data flow graph representing
 * the write to a dynamic object reference.
 *
 * @author Michael Raitza
 * @version 14.04.2011
 */
public class OVWrite64Datum extends OVWriteDatum {

	private OVWrite64Datum() {
		super(null, 0,null, null);
	}

	/**
	 * Constructs a new dynamic object reference write node.
	 * @param reference the reference this dynamic object lives at
	 * @param datum the datum this write consumes to write it to the referenced address
	 * @param value the "value" associated with this datum. The meaning of value highly
	 * depends on the type of node
	 * @param creator the instruction which created this node
	 */
	public OVWrite64Datum(Datum reference, Number value, Datum storedData, Instruction creator) {
		super(reference, value, storedData,creator);
	}

}
