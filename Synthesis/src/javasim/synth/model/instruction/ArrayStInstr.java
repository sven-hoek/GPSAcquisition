package javasim.synth.model.instruction;

import javasim.synth.SynthData;
import javasim.synth.model.I;
import javasim.synth.model.datum.AAWrite64Datum;
import javasim.synth.model.datum.AAWriteDatum;
import javasim.synth.model.datum.Datum;
import javasim.synth.model.datum.LReadDatum;
import javasim.synth.model.datum.LWriteDatum;

public class ArrayStInstr extends Instruction {

	public ArrayStInstr(I instr, Integer pos) {
		super(instr, pos);
	}

	public void eval(SynthData data) {
		Datum v = vstack().pop();
		Datum index = vstack().pop();
		
		if(index instanceof LWriteDatum){
			LWriteDatum lwr = (LWriteDatum)index;
			Datum lwrSource = lwr.getSource();
			boolean fold = false;
			PHIInstr iiinst = branchpoint();
			Boolean decision = decision();
			if(data.lg().getLoop(this.addr()).contains(lwr.creator().addr()) && !((lwrSource instanceof LWriteDatum || lwrSource instanceof LReadDatum )&& !lwrSource.value().equals(lwr.value()))){
				do { // wirklich nur folden, wenn im gleichen branch - könnte man noch verbessern TODO (ist aber eher selten der fall)
					if(iiinst.equals(lwrSource.creator().branchpoint()) && decision == lwrSource.creator().decision() || lwrSource.creator().branchpoint() == null){
						fold = true;
						break;
					}else{
						decision = iiinst.decision();
						iiinst = iiinst.ifinstr().branchpoint();

					}
				} while (iiinst != null);


				if(fold){
					index = lwrSource;
				} else{
					lwr.defineAsNecessary();
				}

			}
		}
		
		
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
		vstack().object_add(data, res);
		
		
		super.eval(data);
	}
}
