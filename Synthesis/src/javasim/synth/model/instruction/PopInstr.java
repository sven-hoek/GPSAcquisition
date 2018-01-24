package javasim.synth.model.instruction;

import javasim.synth.SynthData;
import javasim.synth.model.I;
import javasim.synth.model.datum.Datum;

public class PopInstr extends Instruction {

	public PopInstr(I instr, Integer pos) {
		super(instr, pos);
	}

	public void eval(SynthData data) {
		Datum d = vstack().pop();
		if (i().wdata()) {
			try {
				d.value();
				vstack().pop();
			} catch(ClassCastException e) { }
		}

		super.eval(data);
	}
}
