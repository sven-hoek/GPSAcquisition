package javasim.synth.model.instruction;

import java.util.HashSet;

import javasim.synth.HardGen;
import javasim.synth.SynthData;
import javasim.synth.model.I;
import javasim.synth.model.datum.Datum;
import javasim.synth.model.datum.SWriteDatum;

/**
 * This Instruction represents any unary ALU operation.
 *
 * @author Michael Raitza
 * @version 15.04.2011
 */
public class ALUunopInstr extends Instruction {

	/**
	 * Construct a new instruction.
	 * @param instr the instruction type
	 * @param pos the address of the instruction
	 */
	public ALUunopInstr(I instr, Integer pos) {
		super(instr, pos);
	}

	/**
	 * Evaluates this instruction and constructs
	 * a new SWriteDatum.
	 * @param data holds the synthesis context as a SynthData object
	 */
	public void eval(SynthData data) {
		Datum op1 = vstack().pop();
		Datum res = new SWriteDatum(op1.value(), this);
		HashSet<Datum> ops = data.dg().getOps();
		if(ops != null && data.CSE()){
			for(Datum oldOp: ops){
				if(oldOp.creator().i() == i()){
					
				
					Datum old1 = data.dg().preds(oldOp).iterator().next().sink;
					if(old1.equals(op1)){
						vstack().push(oldOp);
						super.eval(data);
						return;
					}
				}
			}
		}
		
		
		data.dg().add_op(res);
		data.dg().add_edge(op1, res);
		vstack().push(res);
		super.eval(data);
	}
}
