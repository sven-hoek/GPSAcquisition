package javasim.synth.model.instruction;

import javasim.synth.SynthData;
import javasim.synth.model.I;
import javasim.synth.model.datum.Datum;
import javasim.synth.model.datum.SOWrite64Datum;
import javasim.synth.model.datum.SOWriteDatum;

/**
 * Static object reference store instruction.
 *
 * @author Michael Raitza
 * @version 15.04.2011
 */
public class SVSt32Instr extends Instruction {

	public SVSt32Instr(I instr, Integer pos) {
		super(instr, pos);
	}

	/**
	 * Evaluates this instruction and creates a new static object write datum.
	 * @param data holds the synthesis context as a SynthData object
	 * @see javasim.synth.model.I#eval
	 */
	public void eval(SynthData data) {
		Datum v = vstack().pop();
		Datum d;
		if (i().wdata())
			d = new SOWrite64Datum(i().getByteCodeParameter(data), this);
		else
			d = new SOWriteDatum(i().getByteCodeParameter(data), this);
		data.dg().add_op(d);
		data.dg().add_edge(v, d);
		if (branchpoint() != null)
			data.dg().add_sedge(branchpoint().ifdatum(), d);

		data.dg().add_sedge(vstack().static_add(d), d);
		super.eval(data);
	}
}
