package javasim.synth.model.datum;

import javasim.synth.model.instruction.Instruction;

/**
 * Datum that reads from an 64 Bit array
 * @author jung
 *
 */
public class AARead64Datum extends AAReadDatum {

	private AARead64Datum() {
		super(null, null, 0, null);
	}

	public AARead64Datum(Datum reference, Datum index, Number value, Instruction creator) {
		super(reference, index, value, creator);
		wide = true;
	}

}
