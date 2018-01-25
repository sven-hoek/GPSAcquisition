package javasim.synth.model.instruction;

import javasim.synth.SynthData;
import javasim.synth.model.I;
import javasim.synth.model.datum.AAWrite64Datum;
import javasim.synth.model.datum.AAWriteDatum;
import javasim.synth.model.datum.ConstDatum;
import javasim.synth.model.datum.Datum;
import javasim.synth.model.datum.SOWrite64Datum;
import javasim.synth.model.datum.SOWriteDatum;

/**
 * Static object reference store instruction.
 *
 * @author Michael Raitza
 * @version 15.04.2011
 */
public class SVStInstr extends Instruction {

	public SVStInstr(I instr, Integer pos) {
		super(instr, pos);
	}

	/**
	 * Evaluates this instruction and creates a new static object write datum.
	 * @param data holds the synthesis context as a SynthData object
	 * @see javasim.synth.model.I#eval
	 */
	public void eval(SynthData data) {
		Datum v = vstack().pop();
		Datum ref = new ConstDatum(1, I.BIPUSH.create(addr()));
		Datum index = new ConstDatum(i().getByteCodeParameter(data), I.SIPUSH.create(addr()));
		
		ref = data.dg().getConstant(ref);
		index = data.dg().getConstant(index);
		
		Datum res;
		data.a_write(ref.did(), index.isLType(),this.addr(), index.value());
		if (i().wdata())
			res = new AAWrite64Datum(ref, index, v, ref.value(), this);
		else
			res = new AAWriteDatum(ref, index, v, ref.value(), this);
		data.dg().add_op(res);
		data.dg().add_node(ref);
		data.dg().add_node(index);
		if (branchpoint() != null)
			data.dg().add_sedge(branchpoint().ifdatum(), res);
		data.dg().add_edge(v, res, 3);
		data.dg().add_edge(index, res, 2);
		data.dg().add_edge(ref, res, 1);
		vstack().object_add(data, res);
		
		super.eval(data);
		
		
		
		/*
		Datum v = vstack().pop();
		Datum index = vstack().pop();
		Datum ref = vstack().pop();
		
		Datum res;
		data.a_write(ref.did(), index.isLType(),this.addr(), index.value());
		if (i().wdata())
			res = new AAWrite64Datum(ref, index, v, ref.value(), this);
		else
			res = new AAWriteDatum(ref, index, v, ref.value(), this);
		data.dg().add_op(res);
		if (branchpoint() != null)
			data.dg().add_sedge(branchpoint().ifdatum(), res);
		data.dg().add_edge(v, res, 3);
		data.dg().add_edge(index, res, 2);
		data.dg().add_edge(ref, res, 1);
		vstack().object_add(data, res);*/
	}
}
