package javasim.synth.model.instruction;

import javasim.synth.SynthData;
import javasim.synth.model.I;
import javasim.synth.model.datum.CheckerDatum;
import javasim.synth.model.datum.ConstDatum;
import javasim.synth.model.datum.Datum;
import javasim.synth.model.datum.LWrite64Datum;
import javasim.synth.model.datum.LWriteDatum;
import javasim.synth.model.datum.OVReadDatum;

public class InvokeVirtualInstr extends Instruction {

	public InvokeVirtualInstr(I instr, Integer pos) {
		super(instr, pos);
	}
	
	public void eval(SynthData data) {
		
		int byteCodeParam = ((Integer)i().getByteCodeParameter(data));
		int paramCount = byteCodeParam>>10;
		int estimatedCTI = byteCodeParam & 0x3FF;
		
		Datum src =  null;
		
		for(int i = 0; i< paramCount; i++){ //Pop all parameters
			 
			src = vstack().pop();
			
			
			
			LWriteDatum dest = null;
			
			I instr = I.ISTORE;
			
			
			Integer value = paramCount -i-1 + data.getLVarOffset(addr()+3);
			if(src.creator().i().createsReference()){ // The parameter is a reference - we use the exact same reference datum, to be able to resolve the dependencies correctly 
				vstack().local_add(src, value);
				continue;
			}
			
			if(src.creator().i().wdata()){
				i++; // bc wide data counts as two params
				Instruction creator = I.LSTORE.create(addr()); 
				creator.branchpoint(branchpoint());
				creator.decision(decision());
				dest = new LWrite64Datum(value-1, creator, src); 
				data.regLVSTore(value-1);
			}
			else{
				Instruction creator = instr.create(addr());
				creator.branchpoint(branchpoint());
				creator.decision(decision());
				dest = new LWriteDatum(value, creator, src); 
				data.regLVSTore(value);
			}


			if (branchpoint() != null)
				data.dg().add_sedge(branchpoint().ifdatum(), dest);
			vstack().local_add(dest);
			data.addLVStore(dest);
			data.dg().add_op(dest);
			data.dg().add_edge(src, dest);
		}
		
		
		Integer previousCTIAssumption = data.dg().getCTIAssumption(src);
		
		if(previousCTIAssumption == null){

			// src contains the object reference now!
			// now we have to implement the check of the CTI
			Datum ciLoader = new OVReadDatum(src, Integer.MAX_VALUE-1, I.GETFIELD_QUICK.create(addr()));// Integer.MAX_VALUE-1 -> cti

			Datum ciAssumption = new ConstDatum(estimatedCTI, I.SIPUSH.create(addr()));
			ciAssumption = data.dg().getConstant(ciAssumption);

			Datum ciCmp = new CheckerDatum(0, I.CI_CMP.create(addr()));

			ciCmp.creator().branchpoint(branchpoint());
			ciCmp.creator().decision(decision());

			if(branchpoint() != null){
				data.dg().add_sedge(branchpoint().ifdatum(), ciCmp);
			}

			data.dg().add_op(ciLoader);
			data.dg().add_op(ciAssumption);
			data.dg().add_op(ciCmp);

			data.dg().add_edge(src, ciLoader);
			data.dg().add_edge(ciLoader, ciCmp, 1);
			data.dg().add_edge(ciAssumption, ciCmp, 2);
			
			data.dg().addCTIAssumption(src, estimatedCTI);
			//TODO make sure we can call a roll back!
		} else if(previousCTIAssumption != estimatedCTI){
			throw new RuntimeException("Wow, something's really wrong - two different assumptions for one CTI?");
		} else {
			
		}
		
		super.eval(data);
	}

}
