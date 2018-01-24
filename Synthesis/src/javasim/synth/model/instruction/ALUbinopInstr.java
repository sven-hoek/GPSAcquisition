package javasim.synth.model.instruction;


import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import javasim.synth.DEdge;
import javasim.synth.HardGen;
import javasim.synth.SynthData;
import javasim.synth.model.I;
import javasim.synth.model.LoopGraph.Loop;
import javasim.synth.model.datum.ConstDatum;
import javasim.synth.model.datum.Datum;
import javasim.synth.model.datum.LReadDatum;
import javasim.synth.model.datum.LWriteDatum;
import javasim.synth.model.datum.SWriteDatum;

/**
 * This Instruction represents any binary ALU operation.
 *
 * @author Michael Raitza
 * @version 15.04.2011
 */
public class ALUbinopInstr extends Instruction {

	/**
	 * Constructs a new instruction.
	 * @param instr the instruction type
	 * @param pos the address of the instruction
	 */
	public ALUbinopInstr(I instr, Integer pos) {
		super(instr, pos);
	}

	/**
	 * Evaluates this instruction and constructs
	 * a new SWriteDatum.
	 * @param data holds the synthesis context as a SynthData object
	 */
	public void eval(SynthData data) {
		Datum op1 = vstack().pop();
		Datum op2 = vstack().pop();
		Datum res = new SWriteDatum(op1.value(), this);
		
		
		/// FOLD LVSTORE ///////////////////////////////////////////
		
		Datum bypassedDatum = null;
		
		
		if(op1 instanceof LWriteDatum){
			LWriteDatum lwr = (LWriteDatum)op1;
			Datum lwrSource = lwr.getSource();
			boolean fold = false;
			PHIInstr iiinst = branchpoint();
			Boolean decision = decision();
			
			int addffr = lwr.creator().addr();
			Loop lp = data.lg().getLoop(this.addr());
			
			
			if(data.lg().getLoop(this.addr()).contains(lwr.creator().addr()) && !((lwrSource instanceof LWriteDatum || lwrSource instanceof LReadDatum )&& !lwrSource.value().equals(lwr.value()))){
				do { // wirklich nur folden, wenn im gleichen branch - könnte man noch verbessern TODO (ist aber eher selten der fall)
					if(iiinst.equals(lwrSource.creator().branchpoint()) && decision == lwrSource.creator().decision() || lwrSource.creator().branchpoint() == null){
						fold = true;
//						System.err.println("FOLD2 " + lwrSource);
//						System.err.println("\tlwr: " + lwr );
//						System.err.println("\t" + this);
//						System.err.println("-------- " + data.lg().getLoop(this.addr()));
						break;
					}else{
						decision = iiinst.decision();
						iiinst = iiinst.ifinstr().branchpoint();

					}
				} while (iiinst != null);


				if(fold){
					op1 = lwrSource;
				} else{
					lwr.defineAsNecessary();
				}

			}
		}
		if(op2 instanceof LWriteDatum){
			LWriteDatum lwr = (LWriteDatum)op2;
			Datum lwrSource = lwr.getSource();
			boolean fold = false;
			PHIInstr iiinst = branchpoint();
			Boolean decision = decision();
			if(data.lg().getLoop(this.addr()).contains(lwr.creator().addr()) && !((lwrSource instanceof LWriteDatum || lwrSource instanceof LReadDatum )&& !lwrSource.value().equals(lwr.value()))){
				do { // wirklich nur folden, wenn im gleichen branch - könnte man noch verbessern TODO (ist aber eher selten der fall)
					if(iiinst.equals(lwrSource.creator().branchpoint()) && decision == lwrSource.creator().decision() || lwrSource.creator().branchpoint() == null){
						fold = true;
//						System.err.println("FOLD2 " + lwrSource);
//						System.err.println("\tlwr: " + lwr );
//						System.err.println("\t" + this);
//						System.err.println("-------- " + data.lg().getLoop(this.addr()));
						break;
					}else{
						decision = iiinst.decision();
						iiinst = iiinst.ifinstr().branchpoint();

					}
				} while (iiinst != null);


				if(fold){
					op2 = lwrSource;
				} else{
					lwr.defineAsNecessary();
				}

			}
		}
		
		
		
		/// FOLD LVSTORE END ///////////////////////////////////////////
				
		if(i().equals(I.ISHL) && data.CONSTANT_FOLDING()){
			if(op1 instanceof ConstDatum && op2 instanceof ConstDatum){
				Instruction newInst = I.SIPUSH.create(addr());
				res = new ConstDatum(op2.value().intValue()<<op1.value().intValue(), newInst);
				res = data.dg().getConstant(res);
				data.dg().add_op(res);
				vstack().push(res);
				super.eval(data);
				return;
			}
		}
		if(i().equals(I.ISHR) && data.CONSTANT_FOLDING()){
			if(op1 instanceof ConstDatum && op2 instanceof ConstDatum){
				Instruction newInst = I.SIPUSH.create(addr());
				res = new ConstDatum(op2.value().intValue()>>op1.value().intValue(), newInst);
				res = data.dg().getConstant(res);
				data.dg().add_op(res);
				vstack().push(res);
				super.eval(data);
				return;
			}
		}
		if(i().equals(I.IUSHR) && data.CONSTANT_FOLDING()){
			if(op1 instanceof ConstDatum && op2 instanceof ConstDatum){
				Instruction newInst = I.SIPUSH.create(addr());
				res = new ConstDatum(op2.value().intValue()>>>op1.value().intValue(), newInst);
				res = data.dg().getConstant(res);
				data.dg().add_op(res);
				vstack().push(res);
				super.eval(data);
				return;
			}
		}
		
		if(i().equals(I.ISUB) && data.CONSTANT_FOLDING()){
			if(op1 instanceof ConstDatum && op2 instanceof ConstDatum){
				Instruction newInst = I.SIPUSH.create(addr());
				res = new ConstDatum(op2.value().intValue()-op1.value().intValue(), newInst);
				res = data.dg().getConstant(res);
				data.dg().add_op(res);
				vstack().push(res);
//				System.out.println("SSSSSSSSSSUUB");
				super.eval(data);
				return;
			}
		}
		
		if(i().equals(I.IADD) && data.CONSTANT_FOLDING()){
			if(op1 instanceof ConstDatum && op2 instanceof ConstDatum){
				Instruction newInst = I.SIPUSH.create(addr());
				res = new ConstDatum(op2.value().intValue()+op1.value().intValue(), newInst);
				res = data.dg().getConstant(res);
				data.dg().add_op(res);
				vstack().push(res);
//				System.out.println("SSSSSSSSSSUUB");
				super.eval(data);
				return;
			}
		}
		
		if(data.CONSTANT_FOLDING()){
			if(op1 instanceof ConstDatum && op2 instanceof ConstDatum){
				System.out.println("OOOOIIII my friend " + i() + " oop1: " + op1 + " op2: " + op2);

			}
		}
		
		if( i().equals(I.IDIV)){
			if(op1 instanceof ConstDatum){
				int value = op1.value().intValue();
				if(value == 2 || value == 4 || value == 8 || value == 16 || value == 32){
					op1 = new ConstDatum((int)(Math.log(value)/Math.log(2)), op1.creator());
					op1 = data.dg().getConstant(op1);
					data.dg().add_node(op1);
					Instruction creator = I.ISHR.create(addr());
					creator.branchpoint(this.branchpoint());
					creator.decision(this.decision());
					res = new SWriteDatum(op1.value(), creator);
					
					
					
				} else if(value == 1){
					res = op2;
					vstack().push(res);
					super.eval(data);
					return;
				}
			}
		}if( i().equals(I.IMUL)){
			if(op1 instanceof ConstDatum){
				int value = op1.value().intValue();
				if(value == 2 || value == 4 || value == 8 || value == 16 || value == 32){
					op1 = new ConstDatum((int)(Math.log(value)/Math.log(2)), op1.creator());
					op1 = data.dg().getConstant(op1);
					data.dg().add_node(op1);
					Instruction creator = I.ISHL.create(addr());
					creator.branchpoint(this.branchpoint());
					creator.decision(this.decision());
					res = new SWriteDatum(op1.value(), creator);
					
					
					
				} else if(value == 1){
					res = op2;
					vstack().push(res);
					super.eval(data);
					return;
				}
			}
		} else if(i().equals(I.IADD) && data.CONSTANT_FOLDING()){
			if(op1 instanceof ConstDatum && (op2.creator().i().equals(I.IADD) || op2.creator().i().equals(I.IINC) || op2.creator().i().equals(I.ISUB))){
				boolean alsoConst = false;
				Datum otherConst = null;
				int attr = -1;
				int newValue = 0;
				for(DEdge op2PredE : data.dg().preds(op2)){
					Datum op2Pred = op2PredE.sink;
					if(op2Pred instanceof ConstDatum && !(op2Pred.creator().i().equals(I.LDC_W_QUICK) || op2Pred.creator().i().equals(I.LDC2_W_QUICK))){
						alsoConst = true;
						otherConst = op2Pred;
						attr = op2PredE.attr;
						
						if(op2.creator().i().equals(I.ISUB) && attr == 2){
							newValue =  op2Pred.value().intValue()- op1.value().intValue();
						} else{
							newValue = op1.value().intValue() + op2Pred.value().intValue();
						}
						
						
						break;
					}
				}
				if(alsoConst){
					Instruction newInstruction;
					
					if(op2.creator().i().equals(I.ISUB)){
						newInstruction  = I.ISUB.create(addr());
					} else {
						newInstruction = I.IADD.create(addr());
					}
					newInstruction.branchpoint(this.branchpoint());
					newInstruction.decision(this.decision());
					res = new SWriteDatum(otherConst.value(), newInstruction);
					
					bypassedDatum = op2;
//					System.out.println("T3Is " + this);
//					System.out.println("    -> " + res);
//					System.out.println("      newval: " + newValue);
					for(DEdge op2PredE : data.dg().preds(op2)){
						Datum newOp;
						if(op2PredE.attr == attr){
							Datum newConst =new ConstDatum(newValue, otherConst.creator());
							newConst = data.dg().getConstant(newConst);
							data.dg().add_node(newConst);
							newOp = newConst;
						} else {
							newOp = op2PredE.sink;
						}
						if(op2PredE.attr == 1){
							op2 = newOp;
						} else {
							op1 = newOp;
						}
						
					}
				}
			} else if(op2 instanceof ConstDatum && (op1.creator().i().equals(I.IADD) || op1.creator().i().equals(I.IINC) || op1.creator().i().equals(I.ISUB))){
				boolean alsoConst = false;
				Datum otherConst = null;
				int attr = -1;
				int newValue = 0;
				for(DEdge op1PredE : data.dg().preds(op1)){
					Datum op1Pred = op1PredE.sink;
					if(op1Pred instanceof ConstDatum && !(op1Pred.creator().i().equals(I.LDC_W_QUICK) || op1Pred.creator().i().equals(I.LDC2_W_QUICK))){
						alsoConst = true;
						otherConst = op1Pred;
						attr = op1PredE.attr;
						
						if(op1.creator().i().equals(I.ISUB) && attr == 2){
							newValue =  op1Pred.value().intValue()- op2.value().intValue();
						} else{
							newValue = op2.value().intValue() + op1Pred.value().intValue();
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
					
					
					bypassedDatum = op1;
					newInstruction.branchpoint(this.branchpoint());
					newInstruction.decision(this.decision());
					res = new SWriteDatum(otherConst.value(), newInstruction);
//					System.out.println("TIs " + this);
//					System.out.println("    -> " + res);
//					System.out.println("      newval: " + newValue);
					for(DEdge op1PredE : data.dg().preds(op1)){
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
							op2 = newOp;
						} else {
							op1 = newOp;
						}
						
					}
				}
			}
			
		} else if(i().equals(I.ISUB) && data.CONSTANT_FOLDING()){
			if(op1 instanceof ConstDatum && (op2.creator().i().equals(I.IADD) || op2.creator().i().equals(I.IINC) || op2.creator().i().equals(I.ISUB))){
				boolean alsoConst = false;
				Datum otherConst = null;
				int attr = -1;
				int newValue = 0;
//				System.err.println("FOOLDING " + this);
//				System.err.println("    op1 " + op1);
//				System.err.println("    op2 " + op2);
				for(DEdge op2PredE : data.dg().preds(op2)){
					Datum op2Pred = op2PredE.sink;
//					System.err.println("               op2pred " + op2PredE.attr + ": " + op2Pred);
					if(op2Pred instanceof ConstDatum && !(op2Pred.creator().i().equals(I.LDC_W_QUICK) || op2Pred.creator().i().equals(I.LDC2_W_QUICK))){
						alsoConst = true;
						otherConst = op2Pred;
						attr = op2PredE.attr;
						
						if(op2.creator().i().equals(I.ISUB) && attr == 2){
							newValue = op1.value().intValue() + op2Pred.value().intValue();
						} else{
							newValue = op2Pred.value().intValue()- op1.value().intValue();
						}
						
						
						break;
					}
				}
				if(alsoConst){
					Instruction newInstruction;
					
					if(op2.creator().i().equals(I.ISUB)){
						newInstruction  = I.ISUB.create(addr());
					} else {
						newInstruction = I.IADD.create(addr());
					}
					newInstruction.branchpoint(this.branchpoint());
					newInstruction.decision(this.decision());
					res = new SWriteDatum(otherConst.value(), newInstruction);
					
					
					bypassedDatum = op2;
//					System.out.println("TI1s " + this);
//					System.out.println("     " + op2  + " "  + op2.value().intValue());
//					System.out.println("     " + op1  + " "  + op1.value().intValue());
					
					for(DEdge op2PredE : data.dg().preds(op2)){
//						System.out.println("   OP2Pred " + op2PredE.attr + ": " + op2PredE.sink + " " + op2PredE.sink.value().intValue());
						Datum newOp;
						if(op2PredE.attr == attr){
							Datum newConst =new ConstDatum(newValue, otherConst.creator());
							newConst = data.dg().getConstant(newConst);
							data.dg().add_node(newConst);
							newOp = newConst;
						} else {
							newOp = op2PredE.sink;
//							for(DEdge pp : data.dg().succs(newOp)){
////								System.out.println("******* " + pp.sink);
//							}
//							for(Datum ppp: data.dg().succs_s(newOp)){
////								System.out.println("''''''' " + ppp);
//							}
						}
						if(op2PredE.attr == 1){
							op2 = newOp;
						} else {
							op1 = newOp;
						}
						
					}
//					System.err.println("    -> " + res);
//					System.err.println("\t" + op2  + " "  + op2.value().intValue());
//					System.err.println("\t" + op1  + " "  + op1.value().intValue());
//					System.err.println("__________________________________________");
					
				}
			} else if(op2 instanceof ConstDatum && (op1.creator().i().equals(I.IADD) || op1.creator().i().equals(I.IINC) || op1.creator().i().equals(I.ISUB))){
				boolean alsoConst = false;
				Datum otherConst = null;
				int attr = -1;
				int newValue = 0;
				if(data.dg().preds(op1) != null)
				for(DEdge op1PredE : data.dg().preds(op1)){
					Datum op1Pred = op1PredE.sink;
					if(op1Pred instanceof ConstDatum && !(op1Pred.creator().i().equals(I.LDC_W_QUICK) || op1Pred.creator().i().equals(I.LDC2_W_QUICK))){
						alsoConst = true;
						otherConst = op1Pred;
						attr = op1PredE.attr;
						
						if(op1.creator().i().equals(I.ISUB) && attr == 2){
							newValue =  op1Pred.value().intValue() + op2.value().intValue();
						} else{
							newValue = op2.value().intValue() - op1Pred.value().intValue();
						}
						
						break;
					}
				}
				if(alsoConst){
					Instruction newInstruction;
					
					if(op1.creator().i().equals(I.ISUB) && attr == 1){
						newInstruction  = I.IADD.create(addr());
					} else {
						newInstruction = I.ISUB.create(addr());
					}
					newInstruction.branchpoint(this.branchpoint());
					newInstruction.decision(this.decision());
					res = new SWriteDatum(otherConst.value(), newInstruction);
					
					bypassedDatum = op1;
//					System.out.println("TI2s " + this);
//					System.out.println("    -> " + res);
//					System.out.println("      newval: " + newValue);
//					
					Datum newConst =new ConstDatum(newValue, otherConst.creator());
					newConst = data.dg().getConstant(newConst);
					data.dg().add_node(newConst);
					op2 = newConst;
					
					
					for(DEdge op1PredE : data.dg().preds(op1)){
						if(op1PredE.attr != attr){
							op1 = op1PredE.sink;
						}
						
					}
				}
			}
			
		}
		
		
		HashSet<Datum> ops = data.dg().getOps();
		if(ops != null && data.CSE()){
			for(Datum oldOp: ops){
				
				if(oldOp.creator().i() == res.creator().i() || (res.creator().i().equals(I.IADD) && oldOp.creator().i().equals(I.IINC)) || (res.creator().i().equals(I.IADD) && oldOp.creator().i().equals(I.IINC))){
					
					if(!data.lg().getLoop(oldOp.creator().addr()).contains(res.creator().addr())){
						continue; // cant fold if the first instruction may not be executed 
					}
					
					Iterator<DEdge> it = data.dg().preds(oldOp).iterator();
					DEdge de1 = it.next(); 
					Datum old1 = de1.sink;
					Datum old2 = it.next().sink;
					
//					Loop loopOld = data.lg().getLoop(oldOp.creator().addr());
//					Loop loopRes = data.lg().getLoop(res.creator().addr());
					
					
					if(old1.equals(op1) && old2.equals(op2)&& de1.attr == 2 || old1.equals(op2) && old2.equals(op1) && de1.attr == 1){//TODO commutative ops?
//						System.out.println("FOLDING " + res);
//						System.out.println("        " + oldOp);
//						System.out.println(loopOld);
//						System.out.println(loopRes);
						bypassedDatum = null;
						vstack().push(oldOp);
						super.eval(data);
						return;
					}
				}
			}
		}
		
		if(res.creator().i() == I.IADD || res.creator().i() == I.ISUB){
			if(op1 instanceof ConstDatum && op1.value().intValue() == 0){
				if(bypassedDatum != null && (op2 instanceof LReadDatum  || op2 instanceof LWriteDatum) ){
					if(data.dg().succs(bypassedDatum) != null){
						for(DEdge de: data.dg().succs(bypassedDatum)){
							Datum succ = de.sink;
							if( succ instanceof LWriteDatum && op2.value().equals(succ.value())){
								data.dg().addPostIncrementCouple(succ, op2);
							}
						}
					}
					if(data.dg().succs_s(bypassedDatum) != null){
						for(Datum succ : data.dg().succs_s(bypassedDatum)){
							if( succ instanceof LWriteDatum && op2.value().equals(succ.value())){
								data.dg().addPostIncrementCouple(succ, op2);
							}
						}
					}
				}

				vstack().push(op2);
				super.eval(data);
				return;
			}
		}
		if(res.creator().i() == I.IADD){
			if(op2 instanceof ConstDatum && op2.value().intValue() == 0){
				if(bypassedDatum != null && (op1 instanceof LReadDatum  || op1 instanceof LWriteDatum)){
					if(data.dg().succs(bypassedDatum) != null){
						for(DEdge de: data.dg().succs(bypassedDatum)){
							Datum succ = de.sink;
							if( succ instanceof LWriteDatum && op1.value().equals(succ.value())){
								data.dg().addPostIncrementCouple(succ, op1);
							}
						}
					}
					if(data.dg().succs_s(bypassedDatum) != null){
						for(Datum succ : data.dg().succs_s(bypassedDatum)){
							if( succ instanceof LWriteDatum && op1.value().equals(succ.value())){
								data.dg().addPostIncrementCouple(succ, op1);
							}
						}
					}
				}
				vstack().push(op1);
				super.eval(data);
				return;
			}
		}
		
		
		if(bypassedDatum != null){
			if((op1 instanceof LReadDatum  || op1 instanceof LWriteDatum)){
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
			}
			if((op2 instanceof LReadDatum  || op2 instanceof LWriteDatum) ){
				if(data.dg().succs(bypassedDatum) != null){
					for(DEdge de: data.dg().succs(bypassedDatum)){
						Datum succ = de.sink;
						if( succ instanceof LWriteDatum && op2.value().equals(succ.value())){
							data.dg().add_sedge(res, succ);
						}
					}
				}
				if(data.dg().succs_s(bypassedDatum) != null){
					for(Datum succ : data.dg().succs_s(bypassedDatum)){
						if( succ instanceof LWriteDatum && op2.value().equals(succ.value())){
							data.dg().add_sedge(res, succ);
						}
					}
				}
			}
		}
		
		

		
		data.dg().add_op(res);
		data.dg().add_edge(op1, res, 2);
		data.dg().add_edge(op2, res, 1);
		vstack().push(res);
		
		super.eval(data);
	}
}
