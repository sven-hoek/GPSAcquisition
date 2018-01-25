package javasim.synth.model.instruction;

import javasim.synth.*;
import javasim.synth.model.I;

/**
 * This represents the special stop instruction
 * used by the hardware synthesis.
 * The graphical representation of this
 * node is shaped as a circle carrying the
 * label 'E' for end.
 */
public class StopInstr extends Instruction {

	/**
	 * Constructs a new stop instruction.
	 * @param instr the instruction type
	 * @param pos the address of the instruction
	 */
	public StopInstr(I instr, Integer pos) {
		super(instr, pos);
		attr("shape", "circle");
		attr("label", "F");
	}

	public void relabel() { }

	public void eval(SynthData data) { }

	public void insert(SynthData data) { }
}
