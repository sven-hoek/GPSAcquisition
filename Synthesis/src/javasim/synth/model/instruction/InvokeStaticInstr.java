package javasim.synth.model.instruction;

import javasim.synth.SynthData;
import javasim.synth.model.CGRAIntrinsics;
import javasim.synth.model.I;
import javasim.synth.model.datum.Datum;
import javasim.synth.model.datum.LWrite64Datum;
import javasim.synth.model.datum.LWriteDatum;
import javasim.synth.model.datum.SWriteDatum;

/**
 * Invokes a static method
 * @author jung
 *
 */
public class InvokeStaticInstr extends Instruction {
	

	public InvokeStaticInstr(I instr, Integer pos) {
		super(instr, pos);
	}
	
	public void eval(SynthData data) {
		Integer functionID = (Integer)i().getByteCodeParameter(data); 	// get the other bytes -> defined by the eval function in the enum I - identifies the intrinsic
		if(data.kownIntrinsic(functionID)){ // Check whether this function is a known Intrinsic of the CGRA
		

			//When function is known  this means this is an intrinsic
			int nrOfOps = data.numberOfOperandsOfIntrinsic(functionID);

			if(nrOfOps == 1){ ///unary operation
				Datum op1 = vstack().pop();
				Datum res = new SWriteDatum(op1.value(), this);
				data.dg().add_op(res);
				data.dg().add_edge(op1, res);
				if (branchpoint() != null)
					data.dg().add_sedge(branchpoint().ifdatum(), res);
				vstack().push(res);
				super.eval(data);
			} else if( nrOfOps == 2){ ///binary operation
				Datum op1 = vstack().pop();
				Datum res = new SWriteDatum(op1.value(), this);
				data.dg().add_op(res);
				data.dg().add_edge(op1, res, 2);
				if (branchpoint() != null)
					data.dg().add_sedge(branchpoint().ifdatum(), res);
				Datum op2 = vstack().pop();
				data.dg().add_edge(op2, res, 1);
				vstack().push(res);
				super.eval(data);
			} else if( nrOfOps == 3){ ///trinary operation
				Datum op1 = vstack().pop();
				Datum res = new SWriteDatum(op1.value(), this);
				data.dg().add_op(res);
				data.dg().add_edge(op1, res, 3);
				if (branchpoint() != null)
					data.dg().add_sedge(branchpoint().ifdatum(), res);
				Datum op2 = vstack().pop();
				data.dg().add_edge(op2, res, 2);
				Datum op3 = vstack().pop();
				data.dg().add_edge(op3, res, 1);
				vstack().push(res);
				super.eval(data);
			}

		} else{	
			int paramCount = data.getMethodTable()[functionID].getNumberOfArgs();
			
			for(int i = 0; i< paramCount; i++){ // Pop all parameters
				Datum src = vstack().pop();
				LWriteDatum dest = null;
				
				I instr = I.ISTORE;
				
				
				Integer value = paramCount -i-1 + data.getLVarOffset(addr()+3);
				if(src.creator().i().createsReference()){// The parameter is a reference - we use the exact same reference datum, to be able to resolve the dependencies correctly 
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

			super.eval(data);
		}
	}
	
	

}
