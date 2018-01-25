package javasim.synth.model.instruction;

import javasim.synth.SequenceNotSynthesizeableException;
import javasim.synth.model.I;

/**
 * An InvalidInstr instruction is used to mark an instruction
 * type as unusable or not implemented. This class serves
 * as a trigger to find faulty code.
 *
 * @author Michael Raitza
 */
public class InvalidInstr extends Instruction {

	/**
	 * Constructs an instruction but throws an
	 * <code>IllegalArgumentException</code>
	 * exception upon invokation.<p>
	 * Instructions of this class type are not usable but
	 * the error message contains the instruction type and
	 * code address to locate the error.
	 * @param instr the instruction type
	 * @param pos the address of the instruction
	 */
	public InvalidInstr(I instr, Integer pos) throws IllegalArgumentException {
		super(instr, pos);
		throw new SequenceNotSynthesizeableException(instr.c().intValue(), pos);
	}
}
