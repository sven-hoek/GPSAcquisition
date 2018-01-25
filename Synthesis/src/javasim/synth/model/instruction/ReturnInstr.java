package javasim.synth.model.instruction;

import javasim.synth.SequenceNotSynthesizeableException;
import javasim.synth.SynthData;
import javasim.synth.model.I;
import javasim.synth.model.datum.Datum;
import javasim.synth.model.datum.LReadDatum;
import javasim.synth.model.datum.LWriteDatum;

/**
 * Instruction that returns a value for a method call
 * @author jung
 *
 */
public class ReturnInstr extends Instruction {
	
	boolean finalReturn = false;

	public ReturnInstr(I instr, Integer pos) {
		super(instr, pos);
	}
	
	
	public void eval(SynthData data) {
		
		int returnID = data.getFreeLVID();
		
		if(i() != I.RETURN && data.getReturns(this).size()>1){
			// Every possible return value is stored in a pseudo local variable
			Datum op = vstack().pop();
			Instruction sinst = I.ISTORE.create(addr());
			sinst.branchpoint(branchpoint());
			sinst.decision(decision());
			LWriteDatum store = new LWriteDatum(returnID, sinst);

			data.dg().add_op(store);
			data.dg().add_edge(op, store);

			data.addLVStore(store);

			Datum pred = vstack().local_add(store);


			data.dg().add_sedge(pred, store);
			data.dg().add_sedge(branchpoint().ifdatum(), store);

			// In the end the pseudo local variable is loaded and pushed on the stack
			if(finalReturn){
				Instruction linst = I.ILOAD.create(addr());
				linst.branchpoint(branchpoint().branchpoint());
				linst.decision(branchpoint().decision());

				LReadDatum load = new LReadDatum(returnID, linst);
				data.dg().add_node(load);
				vstack().local_add(load);
				data.dg().add_sedge(store, load);
				vstack().push(load);
			}
		}
		
		
		super.eval(data);
	}
	
	
	
	
	/**
	 * Inserts this instruction into the control flow graph.
	 * 
	 * @param data
	 *            holds the synthesis context as a SynthData object
	 */
	public void insert(SynthData data) {
		
		int CurrOff = data.getLVarOffset(addr());
		Short nextOff = data.getLVarOffset(addr()+ i().size());
		
		int adler = this.addr();
		
		if(nextOff == null){
			throw new SequenceNotSynthesizeableException("Sequence contains return statement"); // TODO
		}
		
		
		if(nextOff<CurrOff){ // This means the following instruction belongs to the calling method
			Instruction i = I.get_new(data, i().size() + addr());
			i.branchpoint(branchpoint());
			i.decision(decision());
			data.push(i);
			data.ig().insert(this, i);

			data.addReturn(this, i);

			
			finalReturn = true;
			
			
		}else {
			data.addReturn(this);
		}
		
		
	}

}
