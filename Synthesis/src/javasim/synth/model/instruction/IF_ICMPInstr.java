package javasim.synth.model.instruction;

import javasim.synth.SynthData;
import javasim.synth.model.I;
import javasim.synth.model.datum.Datum;
import javasim.synth.model.datum.LWriteDatum;
import javasim.synth.model.datum.SinkDatum;

/**
 * The IF_ICMP* instruction.
 *
 * @author Michael Raitza
 * @version 15.04.2011
 */
public class IF_ICMPInstr extends AbstractIF {

	/**
	 * Constructs a new IF_ICMP* instruction
	 * @param instr the instruction type
	 * @param pos the address of the instruction
	 */
	public IF_ICMPInstr(I instr, Integer pos) {
		super(instr, pos);
	}

	/**
	 * Evaluates this instruction popping the
	 * last two values off the vstack and connecting
	 * them to a newly constructed SinkDatum.
	 * @param data holds the synthesis context as a SynthData object
	 */
	public void eval(SynthData data) {
		Datum op2 = vstack().pop();
		Datum op1 = vstack().pop();
		if(loopcontroller()){
			if(op1 instanceof LWriteDatum){
				((LWriteDatum)op1).defineAsNecessary();
			}
			if(op2 instanceof LWriteDatum){
				((LWriteDatum)op2).defineAsNecessary();
			}
		}
		Datum res = new SinkDatum(op2.value(), this);
		data.dg().add_node(res);
		phi_node().ifdatum(res);
		data.dg().add_op(res);
		data.dg().add_edge(op1, res, 1);
		data.dg().add_edge(op2, res, 2);
		if (branchpoint() != null)
			data.dg().add_sedge(branchpoint().ifdatum(), res);

		super.eval(data);
	}
}
