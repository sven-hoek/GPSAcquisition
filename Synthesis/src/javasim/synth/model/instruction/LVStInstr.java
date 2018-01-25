package javasim.synth.model.instruction;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import javasim.synth.DEdge;
import javasim.synth.SynthData;
import javasim.synth.model.I;
import javasim.synth.model.datum.Datum;
import javasim.synth.model.datum.LWrite64Datum;
import javasim.synth.model.datum.LWriteDatum;

/**
 * Local variable store instruction.
 *
 * @author Michael Raitza
 * @version 15.04.2011
 */
public class LVStInstr extends Instruction {

	public LVStInstr(I instr, Integer pos) {
		super(instr, pos);
	}

	/**
	 * Evaluates this instruction and creates a new local write datum.
	 * @param data holds the synthesis context as a SynthData object
	 * @see javasim.synth.model.I#eval
	 */
	public void eval(SynthData data) {
		Datum o = vstack().pop();
		Integer v = (Integer)i().getByteCodeParameter(data) + data.getLVarOffset(addr()) ;

		data.lvar_write(v);
		data.lv_write(v, this.addr());
		
		Datum d;
		if (i().wdata())
			d = new LWrite64Datum(v, this, o);
		else
			d = new LWriteDatum(v, this, o);
		data.dg().add_op(d);
		data.dg().add_edge(o, d);
		if (branchpoint() != null)
			data.dg().add_sedge(branchpoint().ifdatum(), d);
		
		
		data.addLVStore((LWriteDatum)d);

		
		LinkedHashSet<Datum> realPreds = vstack().getRealPredecessorsLV(d);
		Datum prev = vstack().local_add(d);
		
		
		
		data.dg().add_sedge(prev, d);
		
		// the consumer of previous versions of this local variable have to be executed first (other wise a RAW error would occur)
		if(realPreds!=null){ 
			for(Datum pred: realPreds){
				if(data.dg().succs(pred)!=null)
				for(DEdge pp: data.dg().succs(pred)){
					Datum ppp = pp.sink;
					
					LinkedHashMap<Instruction, Boolean> iincInstructionHist = new LinkedHashMap<>();
					
					PHIInstr ifInstr = this.branchpoint();
					boolean dec = this.decision();
					
					boolean sameBranch = true;
					
					while(ifInstr != null){// TODO: creation of the history is loop invariant and can be moved outside
						iincInstructionHist.put(ifInstr, dec);
						dec = ifInstr.decision();
						ifInstr = ifInstr.ifinstr().branchpoint();
					}
					
					ifInstr = ppp.creator().branchpoint();
					dec = ppp.creator().decision();
					
					while(ifInstr != null){
						if(iincInstructionHist.containsKey(ifInstr) && dec != iincInstructionHist.get(ifInstr)){
							sameBranch = false;
							break;
						}

						dec = ifInstr.decision();
						ifInstr = ifInstr.ifinstr().branchpoint();
					}
					

					
					if(sameBranch && !ppp.equals(d)){
						data.dg().add_sedge(ppp, d);
					}
				}
			}
		} 
		super.eval(data);
	}
	
	public void insert(SynthData data) {
		Integer v = (Integer)i().getByteCodeParameter(data) + data.getLVarOffset(addr());
		int addr = addr();
		data.regLVSTore(v);
		
		super.insert(data);
	}
}
