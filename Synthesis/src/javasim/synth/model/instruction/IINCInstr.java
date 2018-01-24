package javasim.synth.model.instruction;


import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import javasim.synth.DEdge;
import javasim.synth.HardGen;
import javasim.synth.SynthData;
import javasim.synth.model.I;
import javasim.synth.model.LoopGraph;
import javasim.synth.model.LoopGraph.Loop;
import javasim.synth.model.datum.ConstDatum;
import javasim.synth.model.datum.Datum;
import javasim.synth.model.datum.LReadDatum;
import javasim.synth.model.datum.LWriteDatum;
import javasim.synth.model.datum.Datum.Type;
import javasim.synth.model.datum.SWriteDatum;

/**
 * The IINC Instruction.
 *
 * @author Michael Raitza
 * @version 14.04.2011
 */
public class IINCInstr extends Instruction {

	/**
	 * Constructs a new IINC Instruction.
	 * @param instr the instruction type
	 * @param pos the address of the instruction
	 */
	public IINCInstr(I instr, Integer pos) {
		super(instr, pos);
	}

	/**
	 * Evaluates the Instruction constructing
	 * an LReadDatum and an LWriteDatum object.
	 * @param data holds the synthesis context as a SynthData object
	 */
	public void eval(SynthData data) {
		Integer v = new Integer(data.code(addr()+1)  + data.getLVarOffset(addr()));
		data.lvar_read(v);
		data.lvar_write(v);
		data.lv_read(v, this.addr());
		data.lv_write(v, this.addr());
		Datum d = new LReadDatum(v, this);
		Integer value = data.code(addr()+2);
		if ((value >> 7) == 1) //signextension
			value|=0xFFFFFF00;
		Datum r = new ConstDatum(value, this);
		r = data.dg().getConstant(r);
		LinkedHashSet<Datum> realPreds = vstack().getRealPredecessorsLV(d);
		Datum df = vstack().local_add(d);
		
		Datum bypassedDatum = null;
		
		if (df != null) {
			if(df.type().equals(Datum.Type.MERGER) || df.type().equals(Datum.Type.PIPE)){

				data.dg().add_node(d);

				data.dg().add_simple_sedge(df, d);

			} else {
				vstack().local_add(df,realPreds);			//this is basically folding - no need to store and load again. only possible if the previous access was not in another loop (code above)
				d = df;
				if(df instanceof LWriteDatum){
					LWriteDatum lwr = (LWriteDatum)df;
					Datum lwrSource = lwr.getSource();
					boolean fold = false;
					PHIInstr iiinst = branchpoint();
					Boolean decision = decision();
					
					if(data.lg().getLoop(this.addr()).contains(lwr.creator().addr()) && !((lwrSource instanceof LWriteDatum || lwrSource instanceof LReadDatum )&& !lwrSource.value().equals(lwr.value()))){
						do { // wirklich nur folden, wenn im gleichen branch - könnte man noch verbessern TODO (ist aber eher selten der fall)
							if(iiinst.equals(lwrSource.creator().branchpoint()) && decision == lwrSource.creator().decision() || lwrSource.creator().branchpoint() == null){
								fold = true;
//								System.err.println("FOLD " + lwrSource);
//								System.err.println("\tlwr: " + lwr );
//								System.err.println("\t" + this);
								break;
							}else{
								decision = iiinst.decision();
								iiinst = iiinst.ifinstr().branchpoint();

							}
						} while (iiinst != null);


						if(fold){
							d = lwrSource;
						}else{
							lwr.defineAsNecessary();
						}
					}
				}
			}
		} else{
			data.dg().add_node(d);
		}

		
		vstack().push(d);
		///// ALLES OBERHALB IST LOAD ////////////
		
		Datum op1 = vstack().pop();
		Datum res = new SWriteDatum(v, this);
		// constant folding
		
		
		if(data.CONSTANT_FOLDING() && (op1.creator().i().equals(I.IADD) || (op1.creator().i().equals(I.IINC)&& op1 instanceof SWriteDatum) || op1.creator().i().equals(I.ISUB))){
			boolean alsoConst = false;
			Datum otherConst = null;
			int attr = -1;
			int newValue = 0;
			for(DEdge op1PredE : data.dg().preds(op1)){
				Datum op1Pred = op1PredE.sink;
				if(op1Pred instanceof ConstDatum){
					alsoConst = true;
					otherConst = op1Pred;
					attr = op1PredE.attr;
					
					if(op1.creator().i().equals(I.ISUB) && attr == 2){
						newValue =  op1Pred.value().intValue()- r.value().intValue();
					} else{
						newValue = r.value().intValue() + op1Pred.value().intValue();
					}
					
					
					break;
				}
			}
			if(alsoConst){
				
				Instruction newInstruction;
				
				if(op1.creator().i().equals(I.ISUB)){
					newInstruction  = I.ISUB.create(addr());
				} else {
					newInstruction = I.IADD.create(addr());
				}
				
				
//				System.out.println("RESTUCTURE: " + res);
//				System.out.println("     " + op1);
//				System.out.println("     " + r);
//				System.out.println(" new al " + newValue);
				
				bypassedDatum = op1;
				
				newInstruction.branchpoint(this.branchpoint());
				newInstruction.decision(this.decision());
				res = new SWriteDatum(otherConst.value(), newInstruction);
				for(DEdge op1PredE : data.dg().preds(op1)){
//					System.out.println(" --- " + op1PredE.sink);
					Datum newOp; 
					if(op1PredE.attr == attr){
						Datum newConst =new ConstDatum(newValue, otherConst.creator());
						newConst = data.dg().getConstant(newConst);
						data.dg().add_node(newConst);
						newOp = newConst;
					} else {
						newOp = op1PredE.sink;
					}
					if(op1PredE.attr == 1){
						op1 = newOp;
					} else {
						r = newOp;
					}
					
				}
//				System.out.println(" nw " + op1);
//				System.out.println(" r  " + r + " " + r.value());
//				System.out.println("foldedWrits " + foldedWrite);
			}
		}
		
		
		/// instruction Folding
		HashSet<Datum> ops = data.dg().getOps();
		boolean folded = false;
		if(ops != null && data.CSE()){
			for(Datum oldOp: ops){
				Loop oldLoop = data.lg().getLoop(oldOp.creator().addr());
				
				if((oldOp.creator().i().equals(I.IINC) || oldOp.creator().i().equals(I.IADD)) && oldLoop.contains(this.addr()) ){
					Iterator<DEdge> it = data.dg().preds(oldOp).iterator();
					DEdge de1 = it.next(); 
					Datum old1 = de1.sink;
					Datum old2 = it.next().sink;
					if(old1.equals(r) && old2.equals(op1)&& de1.attr == 2 || old1.equals(op1) && old2.equals(r) && de1.attr == 1){//TODO commutative ops?
						bypassedDatum = null;// i dont use the new datum bc of folding so no dependencies have to be set 
						res  = oldOp;
						folded = true;
						break;
					}
				}
			}
		}
		
		if(!folded){
			data.dg().add_op(res);
			data.dg().add_edge(op1, res, 1);
			data.dg().add_node(r);
			data.dg().add_edge(r, res, 2);
		}
		///// ALLES UNTERHALB IST STORE
		
		Instruction store = I.IINCISTORE.create(addr()); 
		store.branchpoint(this.branchpoint());
		store.decision(this.decision());
		
		Datum iincstore = new LWriteDatum(v, store, res);
		
		data.dg().add_op(iincstore);
		data.dg().add_edge(res, iincstore, 1);
		
		if (branchpoint() != null)
			data.dg().add_sedge(branchpoint().ifdatum(), iincstore);
		
		Datum prev = vstack().local_add(iincstore);
		

		data.dg().add_sedge(prev, iincstore);
		
		data.addLVStore((LWriteDatum)iincstore);
		
		
		/*
		 * An IINC stores the value directly back to the local variable memory. Thus, if there
		 * is a value of that variable on the stack, the old value has to be used. Thus, the 
		 * dependencies consumer of the value on the stack and the IINCISTORE invert. 
		 */
		for(Datum onVstack: vstack()){
			if(onVstack.isLType() && onVstack.value().equals(v)){
				data.dg().addPostIncrementCouple(iincstore, onVstack);
			}
		}
		
		// the consumer of previous versions of this local variable have to be executed first (otherwise a RAW error would occure)
		if(realPreds!=null){
			for(Datum pred: realPreds){
				if(data.dg().succs(pred)!=null)
				for(DEdge pp: data.dg().succs(pred)){
					Datum ppp = pp.sink;
					
					LinkedHashMap<Instruction, Boolean> iincInstructionHist = new LinkedHashMap<>();
					
					PHIInstr ifInstr = this.branchpoint();
					boolean dec = this.decision();
					
					boolean sameBranch = true;
					
					while(ifInstr != null){ // TODO: creation of the history is loop invariant and can be moved outside
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
					

					
					if(sameBranch && !ppp.equals(iincstore)){
						data.dg().add_sedge(ppp, iincstore);
					}
				}
			}
		} 
		
		if(bypassedDatum != null && (op1 instanceof LReadDatum  || op1 instanceof LWriteDatum)){
			if(data.dg().succs(bypassedDatum) != null){
				for(DEdge de: data.dg().succs(bypassedDatum)){
					Datum succ = de.sink;
					if( succ instanceof LWriteDatum && op1.value().equals(succ.value())){
						data.dg().add_sedge(res, succ);
					}
				}
			}
			if(data.dg().succs_s(bypassedDatum) != null){
				for(Datum succ : data.dg().succs_s(bypassedDatum)){
					if( succ instanceof LWriteDatum && op1.value().equals(succ.value())){
						data.dg().add_sedge(res, succ);
					}
				}
			}

			
			// if the foldedwrite writes to a variable we read here, it has to be executed later
		}
		
		
		super.eval(data);
	}
	
	public void insert(SynthData data) {
		Integer v = new Integer(data.code(addr()+1)  + data.getLVarOffset(addr()));
		int addr = addr();
		data.regLVSTore(v);
		
		super.insert(data);
	}
}
