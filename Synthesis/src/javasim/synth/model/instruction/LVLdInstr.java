package javasim.synth.model.instruction;


import java.util.LinkedHashSet;

import javasim.synth.SynthData;
import javasim.synth.model.I;
import javasim.synth.model.LoopGraph.Loop;
import javasim.synth.model.datum.ConstDatum;
import javasim.synth.model.datum.Datum;
import javasim.synth.model.datum.LRead64Datum;
import javasim.synth.model.datum.LReadDatum;
import javasim.synth.model.datum.LWriteDatum;

/**
 * Local variable load instruction.
 *
 * @author Michael Raitza
 * @version 15.04.2011
 */
public class LVLdInstr extends Instruction {

	public LVLdInstr(I instr, Integer pos) {
		super(instr, pos);
	}

	/**
	 * Evaluates this instruction and creates a new local read datum.
	 * @param data holds the synthesis context as a SynthData object
	 * @see javasim.synth.model.I#eval
	 */
	public void eval(SynthData data) {
		Integer v = (Integer)i().getByteCodeParameter(data) + data.getLVarOffset(addr());
		data.lvar_read(v);
		data.lv_read(v, this.addr());
		Datum d;
		if (i().wdata())
			d = new LRead64Datum(v, this);
		else
			d = new LReadDatum(v, this);
		
		LinkedHashSet<Datum> realPreds = vstack().getRealPredecessorsLV(d);
		Datum df = vstack().local_add(d);
		
		if( df != null && !df.value().equals(d.value()) ){ // this is the case when a method was inlined with a reference as parameter -> call by reference
			vstack().push(df);
			vstack().local_add(df, (Integer)d.value());
			super.eval(data);
			return;
		}
		
		
		
		if (df != null) {
			if(df.type().equals(Datum.Type.MERGER) || df.type().equals(Datum.Type.PIPE)){
				data.dg().add_node(d);
				data.dg().add_simple_sedge(df, d);
				
			} else {

					vstack().local_add(df,realPreds);				//this is basically folding - no need to store and load again. only possible if the previous access was not in another loop (code above)
//					d = df;
					if(df instanceof LWriteDatum){
						LWriteDatum ldf = (LWriteDatum)df;
						Datum src = ldf.getSource();
						
						
						boolean fold = false;
						PHIInstr iiinst = branchpoint();
						Boolean decision = decision();
						do { // wirklich nur folden, wenn im gleichen branch - könnte man noch verbessern TODO (ist aber eher selten der fall)
							if(iiinst.equals(df.creator().branchpoint()) && decision == df.creator().decision() || df.creator().branchpoint() == null){
								fold = true;
								break;
							}else{
								decision = iiinst.decision();
								iiinst = iiinst.ifinstr().branchpoint();

							}
						} while (iiinst != null);
						
						Loop thisloop = data.lg().getLoop(addr());
						Loop ldfloop = data.lg().getLoop(ldf.creator().addr());
//						if(thisloop != ldfloop){
//							fold = false;
//						}
						
						if(!data.isSingleLVStore(df.value().intValue()) && thisloop !=ldfloop){
							fold = false;
						}
						
						
						if(fold && src instanceof ConstDatum){
//							System.err.println("FOLLIDING " + d);
//							System.err.println("          " + df);
//							System.err.println("          " + src);
							d = src;
						} else {
							d = df;
						}
						
						
					} else {
						d = df;
					}
			}
		} else{
			data.dg().add_node(d);
		}

		vstack().push(d);

		super.eval(data);
	}
	
}
