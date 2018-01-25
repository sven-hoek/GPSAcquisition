package javasim.synth.model.instruction;

import javasim.synth.SynthData;
import javasim.synth.model.I;
import javasim.synth.model.datum.Datum;

public class SwapInstr extends Instruction {

	public SwapInstr(I instr, Integer pos) {
		super(instr, pos);
	}

	public void eval(SynthData data) {
		Datum d = vstack().pop();
		Datum v = vstack().pop();
		vstack().push(d);
		vstack().push(v);

		super.eval(data);
	}
}
