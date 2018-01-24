package javasim.synth.model.instruction;

import javasim.synth.SynthData;
import javasim.synth.model.I;
import javasim.synth.model.datum.Datum;

public class DupX1Instr extends Instruction {

	public DupX1Instr(I instr, Integer pos) {
		super(instr, pos);
	}

	/**
	 * Evaluates this instruction and creates a new local read datum.
	 * @param data holds the synthesis context as a SynthData object
	 * @see javasim.synth.model.I#eval
	 */
	public void eval(SynthData data) {
		Datum d = vstack().pop();
		Datum d2 = vstack().pop();
		vstack().push(d);
		vstack().push(d2);
		vstack().push(d);

		super.eval(data);
	}
}
