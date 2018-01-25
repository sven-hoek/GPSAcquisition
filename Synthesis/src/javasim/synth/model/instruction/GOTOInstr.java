package javasim.synth.model.instruction;

import javasim.synth.SequenceNotSynthesizeableException;
import javasim.synth.SynthData;
import javasim.synth.model.I;

/**
 * The GOTO instruction.
 * @author Michael Raitza
 * @version 15.04.2011
 */
public class GOTOInstr extends Instruction {

	private boolean loop;

	/**
	 * Constructs a GOTO instruction.
	 * @param instr the instruction type (I.GOTO)
	 * @param pos the address of the instruction
	 */
	public GOTOInstr(I instr, Integer pos) {
		super(instr, pos);
		loop = false;
	}

	/**
	 * Inserts this GOTO instruction into the graph.
	 * Depending on the fact, that this GOTO targets a point
	 * backward (THE loop controlling GOTO), forward inside the loop
	 * or forward outside the loop (THE loop cancelling GOTO) the
	 * instruction graph is modified accordingly and the synthesis
	 * context updated.
	 * @param data holds the synthesis context as a SynthData object
	 */
	public void insert(SynthData data) {
		data.ig().reg_goto(this);
		Integer target = data.code_w(addr()+1) + addr();
		if (target <= data.stop_addr()) {
			if (target > addr()) {
				/* normal forward jump */
				Instruction i = I.get_new(data, target);
				i.branchpoint(this.branchpoint());
				i.decision(this.decision());
				data.push(i);
				data.ig().insert(this, i);
			} else {
				/* backward jump, loop controlling */
				loop = true;
				Integer next = addr()+i().size();
				if (next < data.stop_addr()) {
					Instruction i = I.get_new(data, addr() + i().size());
					i.branchpoint(this.branchpoint().branchpoint());
					i.decision(this.branchpoint().decision());
					data.push(i);
					data.ig().insert(this, i);
				} else
					data.ig().insert(this, data.stop());
			}
		} else {
			throw new SequenceNotSynthesizeableException("Unable to synthesize given sequence: Break statements are currently not supported");
		}
	}

	/**
	 * Registers a loop profile iff this GOTO instruction belongs to
	 * a loop structure. The profiling must be done after analysing
	 * the control flow and is a separate step in the control flow graph
	 * creation.
	 * @param data holds the synthesis context as a SynthData object
	 */
	public void profile(SynthData data) {
		if (loop){
			int start = data.code_w(addr()+1) + addr();
			int stop = addr();

			data.loop_profile(branchpoint().ifinstr(), start, stop);
			for(Instruction i : data.ig().phi_nodes().keySet()){
				PHIInstr phi = (PHIInstr) i;
				int phiaddr = phi.ifinstr().addr();
				if(phiaddr > start && phiaddr < stop && (phiaddr + data.code_w(phiaddr+1)-3 == addr())){
					phi.ifinstr().set_loopcontroller();
				}
			}
		}
	}
}
