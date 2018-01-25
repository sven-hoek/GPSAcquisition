package javasim.synth.model.instruction;

import javasim.synth.SynthData;
import javasim.synth.model.I;
import javasim.synth.model.datum.Datum;

public class Dup2X1Instr extends Instruction {

	public Dup2X1Instr(I instr, Integer pos) {
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
		if (d.creator().i().wdata()) {
			vstack().push(d);
			vstack().push(d2);
			vstack().push(d);
		} else  {
			Datum d3 = vstack().pop();
			vstack().push(d2);
			vstack().push(d);
			vstack().push(d3);
			vstack().push(d2);
			vstack().push(d);
		}

		super.eval(data);
	}
}
