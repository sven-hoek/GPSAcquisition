package javasim.synth.model.instruction;

import javasim.synth.SynthData;
import javasim.synth.model.I;
import javasim.synth.model.datum.Datum;

public class Dup2Instr extends Instruction {

	public Dup2Instr(I instr, Integer pos) {
		super(instr, pos);
	}

	/**
	 * Evaluates this instruction and creates a new local read datum.
	 * @param data holds the synthesis context as a SynthData object
	 * @see javasim.synth.model.I#eval
	 */
	public void eval(SynthData data) {
		Datum d = vstack().pop();
		if (d.creator().i().wdata()) {
			vstack().push(d);
			vstack().push(d);
		} else {
			Datum f = vstack().pop();
			vstack().push(f);
			vstack().push(d);
			vstack().push(f);
			vstack().push(d);
		}

		super.eval(data);
	}
}
