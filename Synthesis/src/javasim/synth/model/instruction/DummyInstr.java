package javasim.synth.model.instruction;

import javasim.synth.model.I;

public class DummyInstr extends Instruction {
	public DummyInstr(I instr, Integer pos) {
		super(instr, pos);
		attr("shape", "ellipse");
	}
}
