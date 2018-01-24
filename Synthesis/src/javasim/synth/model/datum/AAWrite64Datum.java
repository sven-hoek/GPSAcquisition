package javasim.synth.model.datum;

import javasim.synth.model.instruction.Instruction;

/**
 * Datum that writes to a 64-bit array
 * @author jung
 *
 */
public class AAWrite64Datum extends AAWriteDatum {

	private AAWrite64Datum() {
		super(null, null, null, 0, null);
	}

	public AAWrite64Datum(Datum reference, Datum index, Datum datum, Number value, Instruction creator) {
		super(reference, index, datum, value, creator);
		wide = true;
	}

}
