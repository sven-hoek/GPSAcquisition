package javasim.synth.model.instruction;

import javasim.synth.SynthData;
import javasim.synth.model.I;
import javasim.synth.model.datum.Datum;
import javasim.synth.model.datum.LWriteDatum;
import javasim.synth.model.datum.SinkDatum;

/**
 * The IF* instruction.
 *
 * @author Michael Raitza
 * @version 15.04.2011
 */
public class IFInstr extends AbstractIF {

	/**
	 * Construct a new IF* instruction
	 * @param instr the instruction type
	 * @param pos the address of the instruction
	 */
	public IFInstr(I instr, Integer pos) {
		super(instr, pos);
	}

	/**
	 * Evaluates this instruction popping the
	 * last value off the vstack and connecting
	 * it to a newly constructed SinkDatum.
	 * @param data holds the synthesis context as a SynthData object
	 */
	public void eval(SynthData data) {
		Datum op1 = vstack().pop();
		if(loopcontroller() && op1 instanceof LWriteDatum){
			((LWriteDatum)op1).defineAsNecessary();
		}
		Datum res = new SinkDatum(op1.value(), this);
		data.dg().add_node(res);
		phi_node().ifdatum(res);
		data.dg().add_op(res);
		data.dg().add_edge(op1, res);
		if (branchpoint() != null)
			data.dg().add_sedge(branchpoint().ifdatum(), res);

		super.eval(data);
	}
}
