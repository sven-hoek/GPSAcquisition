package javasim.synth.model.instruction;

import javasim.synth.SynthData;
import javasim.synth.model.I;
import javasim.synth.model.datum.ConstDatum;
import javasim.synth.model.datum.Datum;

public class ConstLdInstr extends Instruction {

	public ConstLdInstr(I instr, Integer pos) {
		super(instr, pos);
	}

	public void eval(SynthData data) {
		Datum d = new ConstDatum(i().getByteCodeParameter(data), this);
		d = data.dg().getConstant(d);
		data.dg().add_node(d);
		vstack().push(d);
		super.eval(data);
	}
}
