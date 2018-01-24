package javasim.synth.model.instruction;

import java.util.LinkedList;
import java.util.Set;

import javasim.synth.HardGen;
import javasim.synth.SynthData;
import javasim.synth.model.I;
import javasim.synth.model.LoopGraph.Loop;
import javasim.synth.model.datum.AAWriteDatum;
import javasim.synth.model.datum.Datum;
import javasim.synth.model.datum.Indexed;
import javasim.synth.model.datum.OVRead64Datum;
import javasim.synth.model.datum.OVReadDatum;
import javasim.synth.model.datum.OVWriteDatum;

/**
 * Dynamic object reference load instruction.
 *
 * @author Michael Raitza
 * @version 15.04.2011
 */
public class OVLdInstr extends Instruction {

	public OVLdInstr(I instr, Integer pos) {
		super(instr, pos);
	}

	/**
	 * Evaluates this instruction and creates a new object read datum.
	 * @param data holds the synthesis context as a SynthData object
	 * @see javasim.synth.model.I#eval
	 */
	public void eval(SynthData data) {
		Datum op1 = vstack().pop();
		Datum res;
		if (i().wdata())
			res = new OVRead64Datum(op1, i().getByteCodeParameter(data), this);
		else
			res = new OVReadDatum(op1, i().getByteCodeParameter(data), this);
		
		/* folding !! */
		LinkedList<Set<Datum>> hist = vstack().getHistory(res);
		if(hist != null && data.CSE()){
			for(Set<Datum> set: hist){
				if(set.size()==1){
					Datum d = set.iterator().next();
					if(d.reference().equals(res.reference())){
						if( (d instanceof OVReadDatum || d instanceof OVWriteDatum) && d.value().equals(res.value())){

							boolean fold = false;
							PHIInstr iiinst = res.creator().branchpoint();
							Boolean decision = res.creator().decision();
							do { // wirklich nur folden, wenn im gleichen branch - könnte man noch verbessern TODO (ist aber eher selten der fall)
								if(iiinst.equals(d.creator().branchpoint()) && decision == d.creator().decision() || d.creator().branchpoint() == null){
									fold = true;
									break;
								}else{
									decision = iiinst.decision();
									iiinst = iiinst.ifinstr().branchpoint();
								}
							} while (iiinst != null);
							
							Loop thisloop = data.lg().getLoop(addr());
							Loop dloop = data.lg().getLoop(d.creator().addr());
//							if(thisloop != ldfloop){
//								fold = false;
//							}
							
							if(d instanceof OVWriteDatum && !data.isSinglePutField(d.value().intValue()) && thisloop !=dloop){
								fold = false;
//								System.err.println("AAAAAAA");
							}

							if(fold){
								
								if(d instanceof OVWriteDatum){
									d = ((OVWriteDatum)d).getStoredData();
								}
								vstack().push(d);	
								super.eval(data);
//								System.err.println("ASDF " + res + " " + d);
								return;
							}else{
								if(d instanceof OVReadDatum && data.lg().getLoop(d.creator().addr()).contains(this.addr())){
//									System.err.println("BBBB");
									d.creator().branchpoint(null);
									vstack().push(d);
									super.eval(data);
//									System.err.println("ASDF2 " + res + " " + d);
									return;
								}
									
							}
						}
							
					}else{
						if((d instanceof OVReadDatum || d instanceof OVWriteDatum)){
							break;
						}
					}
				} else{
					break;
				}

			}

		} 


			
		
		
		
		/// end folding
		
		vstack().object_add(data, res);
		data.dg().add_op(res);
		data.dg().add_simple_edge(op1, res);
		if (branchpoint() != null){
			data.dg().add_sedge(branchpoint().ifdatum(), res);

		}
		vstack().push(res);

		super.eval(data);
	}
}
