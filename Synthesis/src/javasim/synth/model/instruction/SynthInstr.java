package javasim.synth.model.instruction;

import javasim.synth.model.I;

/**
 * This instruction just represents a graph node. It is of no
 * functional meaning.
 *
 * @author Michael Raitza
 * @version 15.04.2011
 */
public class SynthInstr extends Instruction {

	public SynthInstr(I instr, Integer pos) {
		super(instr, pos);
	}
}
