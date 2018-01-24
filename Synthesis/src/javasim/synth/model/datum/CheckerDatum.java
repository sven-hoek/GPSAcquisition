package javasim.synth.model.datum;

import javasim.synth.model.instruction.Instruction;

/**
 * Datum that compares two Class indexes if they aren't the same, an exception is thrown
 * This is needed to check whether speculative method inlining was correct
 * @author jung
 *
 */
public class CheckerDatum extends SWriteDatum {

	public CheckerDatum(Number value, Instruction creator) {
		super(value, creator);
	}
	
}
