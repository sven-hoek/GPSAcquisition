package javasim.synth.model.instruction;

import javasim.synth.SynthData;
import javasim.synth.model.I;
import javasim.synth.model.datum.Datum;
import javasim.synth.model.datum.OVWrite64Datum;
import javasim.synth.model.datum.OVWriteDatum;

/**
 * Dynamic object reference store instruction.
 *
 * @author Michael Raitza
 * @version 15.04.2011
 */
public class OVStInstr extends Instruction {

	public OVStInstr(I instr, Integer pos) {
		super(instr, pos);
	}

	/**
	 * Evaluates this instruction and creates a new object write datum.
	 * @param data holds the synthesis context as a SynthData object
	 * @see javasim.synth.model.I#eval
	 */
	public void eval(SynthData data) {
		Datum op1 = vstack().pop(); /* value */
		Datum op2 = vstack().pop(); /* ref */
		Datum res;
		if (i().wdata())
			res = new OVWrite64Datum(op2, i().getByteCodeParameter(data), op1, this);
		else
			res = new OVWriteDatum(op2, i().getByteCodeParameter(data),op1, this);
		data.dg().add_op(res);
		data.dg().add_simple_edge(op2, res, 2);
		data.dg().add_edge(op1, res, 1);
		if (branchpoint() != null)
			data.dg().add_sedge(branchpoint().ifdatum(), res);
		vstack().object_add(data, res);
		

		super.eval(data);
	}
	
	
	public void insert(SynthData data) {
		Integer v = (Integer)i().getByteCodeParameter(data);
		int addr = addr();
		data.regPutField(v);
		
		super.insert(data);
	}
}
