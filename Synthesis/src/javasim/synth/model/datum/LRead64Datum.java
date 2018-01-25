package javasim.synth.model.datum;

import javasim.synth.model.instruction.Instruction;

/**
 * A single node of the data flow graph representing
 * the read of a local 64-bit variable.
 *
 * @author Michael Raitza
 * @version 14.04.2011
 */
public class LRead64Datum extends LReadDatum {

	private LRead64Datum() {
		super(0, null);
	}

	public LRead64Datum(Number value, Instruction creator) {
		super(value, creator);
	}

}
