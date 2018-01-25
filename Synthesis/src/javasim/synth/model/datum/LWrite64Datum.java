package javasim.synth.model.datum;

import javasim.synth.SynthData;
import javasim.synth.model.instruction.Instruction;

/**
 * A single node of the data flow graph representing
 * the write of a local variable.
 *
 * @author Michael Raitza
 * @version 14.04.2011
 */
public class LWrite64Datum extends LWriteDatum {

	private LWrite64Datum() {
		super(0, null);
	}

	public LWrite64Datum(Number value, Instruction creator) {
		super(value, creator);
	}
	
	public LWrite64Datum(Number value, Instruction creator, Datum source) {
		super(value, creator, source);
	}
	
}
