package javasim.synth.model.instruction;

import javasim.synth.SynthData;
import javasim.synth.model.I;
import javasim.synth.model.VStack;

/**
 * This represents the special start instruction
 * used by the hardware synthesis.
 */
public class StartInstr extends Instruction {

	/**
	 * Constructs a new start instruction.
	 * The graphical representation of this
	 * node is shaped as a circle carrying the
	 * label 'S' for start.
	 * @param instr the instruction type
	 * @param pos the address of the instruction
	 */
	public StartInstr(I instr, Integer pos) {
		super(instr, pos);
		attr("label", "S");
		attr("shape", "circle");
		vstack(new VStack());
	}

	/**
	 * Inserts this instruction into the graph.
	 * @param data holds the synthesis context as a SynthData object
	 */
	public void insert(SynthData data) {
		Instruction i = I.get_new(data, i().size() + addr());
		i.branchpoint(branchpoint());
		i.decision(decision());
		data.push(i);
		data.ig().insert(this, i);
	}
}
