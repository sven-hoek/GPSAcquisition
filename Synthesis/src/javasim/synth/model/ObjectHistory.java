 package javasim.synth.model;

import graph.Node;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

import scheduler.RCListSched.AliasingSpeculation;

import com.sun.org.apache.bcel.internal.generic.AALOAD;
import com.sun.org.apache.bcel.internal.generic.IADD;

import javasim.synth.DEdge;
import javasim.synth.SynthData;
import javasim.synth.model.LoopGraph.Loop;
import javasim.synth.model.datum.AAReadDatum;
import javasim.synth.model.datum.AAWriteDatum;
import javasim.synth.model.datum.ConstDatum;
import javasim.synth.model.datum.Datum;
import javasim.synth.model.datum.Indexed;
import javasim.synth.model.datum.LReadDatum;
import javasim.synth.model.datum.LWriteDatum;
import javasim.synth.model.datum.OVReadDatum;
import javasim.synth.model.datum.OVWriteDatum;
import javasim.synth.model.datum.PipeDatum;
import javasim.synth.model.datum.SWriteDatum;
import javasim.synth.model.instruction.ArrayLdInstr;
import javasim.synth.model.instruction.ArrayLengthInstr;
import javasim.synth.model.instruction.ArrayStInstr;
import javasim.synth.model.instruction.IINCInstr;
import javasim.synth.model.instruction.LVLdInstr;
import javasim.synth.model.instruction.LVStInstr;
import javasim.synth.model.instruction.OVLdInstr;
import javasim.synth.model.instruction.PHIInstr;
import javasim.synth.model.instruction.SVLdInstr;
import javasim.synth.model.instruction.SVStInstr;

/**
 * This class stores past accesses to a specific object. This class also manages the dependencies between those past accesses and a new one
 * @author jung
 *
 */
public class ObjectHistory {
	
	/**
	 * Gives the maximum history length. If there are more accesses, the new accesses are automatically assumed to be dependant on the old ones
	 */
	int historyLength = 1000;
	
	/**
	 * The oldest object in the access history
	 */
	Datum oldestObject;
	
	/**
	 * The newest object in the history
	 */
	Datum latestObject;
	
	/**
	 * The history itself
	 */
	LinkedList<Set<Datum>> history;
	
	enum ReferenceOrigin{
		CONST,
		LV,
		OV,
		SV,
		ARRAY
	}
	
	/**
	 * Creates a new History
	 */
	public ObjectHistory(){
		this.history = new LinkedList<Set<Datum>>();
	}
	
	/**
	 * Creates a new History
	 */
	public ObjectHistory(int historyLength){
		this.historyLength = historyLength;
		this.history = new LinkedList<Set<Datum>>();
	}
	
	/**
	 * Creates a new History
	 */
	public ObjectHistory(ObjectHistory orig){
		this.historyLength = orig.historyLength;
		this.history = new LinkedList<Set<Datum>>(orig.history);
		this.oldestObject = orig.oldestObject;
		this.latestObject = orig.latestObject;
	}
	
	
	public static long dep = 0, indep = 0;
	
	/**
	 * Adds a datum to the history and sets dependencies between the new Datum and the ones in the history
	 * @param syn
	 * @param d
	 * @return
	 */
	public Datum add(SynthData syn, Datum d){
		latestObject = d;
		DataGraph datagraph = syn.dg();
		Datum ret = oldestObject;
		for(Set<Datum> s: history){
			for(Datum oldD: s){
				if(dependent(d,oldD, syn)){
					dep++;
					datagraph.add_sedge(oldD,d);	// Go through the history and check dependencies. Add edge to graph if necessary
				} else {
					indep++;
				}
			} 
		}
		
		// accesses too old have to be assumed dependent
		if(history.size()>=historyLength){
			PipeDatum nop = new PipeDatum(d.reference(), d.value(), I.NOP.create(d.creator().addr()));
			datagraph.add_op(nop);
			datagraph.add_sedge(oldestObject, nop);
			for(Datum last: history.removeLast()){
				datagraph.add_sedge(last, nop);
			}
			oldestObject = nop;
		}
		LinkedHashSet<Datum> newSet = new LinkedHashSet<Datum>();
		newSet.add(d);
		history.addFirst(newSet);
		datagraph.add_sedge(oldestObject, d); // TODO Perhaps i can shift this before the if - might be better
		
//		String indexInfo = getIndexInfo(d, syn);
//		System.out.println(d.creator().addr()+":"+d.creator().i() + "\t\t\t INDEX INFO: " + indexInfo);
		return ret;
	}
	
	private String getIndexInfo(Datum d, SynthData syn) {
		String info = "NONE!";
		
		if(d instanceof Indexed){
			Indexed dInd = (Indexed)d;
			Datum index = dInd.index();
			
			if(index instanceof ConstDatum){
				info = "CONST " + index.value().intValue();
			} else if (index.creator().i().equals(I.IADD) || index.creator() instanceof IINCInstr) {
//				System.out.println("index: " + index);
				int constCnt = 0;
				int lvCnt = 0;
				int constValue = -123123123;
				for(DEdge predEdge: syn.dg().preds(index)){
					Datum pred = predEdge.sink;
					if(pred.creator() instanceof LVStInstr || pred.creator() instanceof LVLdInstr){
						lvCnt++;
					} else if(pred instanceof ConstDatum){
						constCnt++;
						constValue = pred.value().intValue();
					}
				}
				if(constCnt == 1 && lvCnt == 1){
					info = "LV OFFSET " + constValue;
				}
			} else if(index.creator() instanceof LVStInstr || index.creator() instanceof LVLdInstr){
				info = "LV OFFSET 0";
			}
			
			
		}
		
		return info;
	}

	/** 
	 * Returns the lates access to this object
	 * @return
	 */
	public Datum latestObject(){
		return latestObject;
	}
	
	/**
	 * checks whether two accesses to a Object are dependant
	 * @param newD
	 * @param oldD
	 * @param syn
	 * @return
	 */
	private boolean dependent(Datum newD, Datum oldD, SynthData syn){
		
		
//		VStack vs = new VStack();
		

		
		if(newD.accessType().equals(Datum.AccessType.READ) && oldD.accessType().equals(Datum.AccessType.READ)){
			return false;
		}
		
		DMA_TYPE oldDMAType = getDMAType(oldD);
		DMA_TYPE newDMAType = getDMAType(newD);
		
		if(!newDMAType.equals(oldDMAType)){
			return false;
		}
		
		if(!newDMAType.equals(DMA_TYPE.ARRAY) && !oldDMAType.equals(DMA_TYPE.ARRAY)){
			if(!newD.value().equals(oldD.value())){
				return false; 
			}
		}
		
		
		
		
		
		if(newD instanceof Indexed && oldD instanceof Indexed){

			Indexed newWrite = (Indexed)newD;
			Indexed oldWrite = (Indexed)oldD;
			Datum newIndex = newWrite.index();
			Datum oldIndex = oldWrite.index();
			
			if(newIndex.creator().i().equals(I.IADD) && oldIndex.creator().i().equals(I.IADD)){
				LinkedHashSet<Datum> predsNew = new LinkedHashSet<>();
				for(DEdge de: syn.dg().preds(newIndex)){
					predsNew.add(de.sink);
				}
				LinkedHashSet<Datum> predsOld = new LinkedHashSet<>();
				for(DEdge de: syn.dg().preds(oldIndex)){
					predsOld.add(de.sink);
				}
				LinkedHashSet<Datum> inBoth = new LinkedHashSet<>(predsNew);
				inBoth.retainAll(predsOld);
				if(inBoth.size()==1){
					Datum inbD = inBoth.iterator().next();
					if(inbD.isLType()){
//						System.err.println("FFFFFFFFFFFFFFFF yeazh " + inBoth.iterator().next());
						predsNew.remove(inbD);
						predsOld.remove(inbD);
						newIndex = predsNew.iterator().next();
						oldIndex = predsOld.iterator().next();
//						System.err.println("new " + newIndex);
//						System.err.println("old " + oldIndex);
					}
				}
			}
			
			
			Set<DEdge> oldIndexSuccs = syn.dg().succs(oldIndex);
			Set<DEdge> newIndexPreds = syn.dg().preds(newIndex);
			
			Set<DEdge> newIndexSuccs = syn.dg().succs(newIndex);
			Set<DEdge> oldIndexPreds = syn.dg().preds(oldIndex);
			
			
			if(newIndex instanceof ConstDatum && oldIndex instanceof ConstDatum && !newIndex.value().equals(oldIndex.value())){
				return false;
			}
			
			
			
			if(getValueType(oldD) != getValueType(newD)){ // This won't work in C   - you can cast pointer there
				return false;
			}
			
			
			if(oldIndexSuccs != null && newIndexPreds != null){
				for(DEdge suc : oldIndexSuccs){
					Datum successor = suc.sink;

					if(successor.equals(newIndex) && successor.creator() instanceof IINCInstr){
						return false;
					}

					if(successor.equals(newIndex) && successor.creator().i().equals(I.IADD)){
						Datum op1 = null;
						Datum op2 = null;
						for(DEdge de : syn.dg().preds(newIndex)){
							if(de.attr == 1){
								op1 = de.sink;
							} else {
								op2 = de.sink;
							}
						}

						if(op1.equals(oldIndex) && op2 instanceof ConstDatum && op2.value().intValue() != 0){
							return false;
						}
						if(op2.equals(oldIndex) && op1 instanceof ConstDatum && op1.value().intValue() != 0){
							return false;
						}

					}

					if((oldIndex.creator() instanceof IINCInstr || oldIndex.creator().i().equals(I.IADD) ) && (newIndex.creator() instanceof IINCInstr  || oldIndex.creator().i().equals(I.IADD))){
						boolean isIndependent = true;
						for(DEdge deNew: syn.dg().preds(newIndex)){
							if(syn.dg().preds(oldIndex) != null)
								for(DEdge deOld: syn.dg().preds(oldIndex)){		// Both are independent when:
									if(deNew.attr == 1 && deOld.attr == 1){  
										if(!deNew.sink.equals(deOld.sink)){		// - they load the same local variable
											isIndependent = false;
										}
									}
									if(deNew.attr == 2 && deOld.attr == 2){		// - but add different constants

										if(deNew.sink.equals(deOld.sink) || !(deNew.sink instanceof ConstDatum)  || !(deOld.sink instanceof ConstDatum)){
											isIndependent = false;
										}
									}

								}
						}
						if(isIndependent){
							return false;
						}


					}

				}
			}
			
			if(newIndexSuccs != null && oldIndexPreds != null){
				for(DEdge suc : newIndexSuccs){
					Datum successor = suc.sink;

					if(successor.equals(oldIndex) && successor.creator() instanceof IINCInstr){
						return false;
					}

					if(successor.equals(oldIndex) && successor.creator().i().equals(I.IADD)){
						Datum op1 = null;
						Datum op2 = null;
						for(DEdge de : syn.dg().preds(oldIndex)){
							if(de.attr == 1){
								op1 = de.sink;
							} else {
								op2 = de.sink;
							}
						}

						if(op1.equals(newIndex) && op2 instanceof ConstDatum && op2.value().intValue() != 0){
							return false;
						}
						if(op2.equals(newIndex) && op1 instanceof ConstDatum && op1.value().intValue() != 0){
							return false;
						}

					}

					if((newIndex.creator() instanceof IINCInstr || newIndex.creator().i().equals(I.IADD) ) && (oldIndex.creator() instanceof IINCInstr  || newIndex.creator().i().equals(I.IADD))){
						boolean isIndependent = true;
						for(DEdge deold: syn.dg().preds(oldIndex)){
							if(syn.dg().preds(newIndex) != null)
								for(DEdge denew: syn.dg().preds(newIndex)){		// Both are independent when:
									if(deold.attr == 1 && denew.attr == 1){  
										if(!deold.sink.equals(denew.sink)){		// - they load the same local variable
											isIndependent = false;
										}
									}
									if(deold.attr == 2 && denew.attr == 2){		// - but add different constants

										if(deold.sink.equals(denew.sink) || !(deold.sink instanceof ConstDatum)  || !(denew.sink instanceof ConstDatum)){
											isIndependent = false;
										}
									}

								}
						}
						if(isIndependent){
							return false;
						}


					}

				}
			}
			
			
			
		}
		
		
		
		boolean sameReference = false;
		
			if ((oldD.reference().value().equals(newD.reference().value())) && (oldD.value().equals(newD.value()))  && getReferenceOrigin(oldD) == getReferenceOrigin(newD)){
				sameReference = true;
			}
		
		
			if(syn.getAliasSpeculation() != AliasingSpeculation.OFF){
				if(!sameReference){
					//			if(syn.dg().areDependent(oldD, newD.reference())){
					//				return false;
					//			}
//					if(syn.getAliasSpeculation() != AliasingSpeculation.NO_CHECK){
						syn.dg().add_sedge(oldD.reference(), newD);
//					}
//					if(oldD instanceof Indexed){
//						Indexed oldI = (Indexed)oldD;
//						Datum index = oldI.index();
//						syn.dg().add_sedge(index, newD);
//					}

					syn.addPotentialAliases(oldD, newD);


					return false;
				}
			}
		return true;
	}
	
	
	private VALUE_TYPE getValueType(Datum d){
		if(d.creator().i().equals(I.IALOAD) || d.creator().i().equals(I.IASTORE) ){
			return VALUE_TYPE.INTEGER;
		}
		if(d.creator().i().equals(I.LALOAD) || d.creator().i().equals(I.LASTORE) ){
			return VALUE_TYPE.LONG;
		}
		if(d.creator().i().equals(I.FALOAD) || d.creator().i().equals(I.FASTORE) ){
			return VALUE_TYPE.FLOAT;
		}
		if(d.creator().i().equals(I.DALOAD) || d.creator().i().equals(I.DASTORE) ){
			return VALUE_TYPE.DOUBLE;
		}
		if(d.creator().i().equals(I.BALOAD) || d.creator().i().equals(I.BASTORE) ){
			return VALUE_TYPE.BYTE;
		}
		if(d.creator().i().equals(I.CALOAD) || d.creator().i().equals(I.CASTORE) ){
			return VALUE_TYPE.CHAR;
		}
		if(d.creator().i().equals(I.SALOAD) || d.creator().i().equals(I.SASTORE) ){
			return VALUE_TYPE.SHORT;
		}
		if(d.creator().i() == I.ALOAD || d.creator().i() == I.ALOAD_0 || d.creator().i() == I.ALOAD_1 || d.creator().i() == I.ALOAD_2 || d.creator().i() == I.ALOAD_3 || d.creator().i() == I.AALOAD || d.creator().i() == I.GETSTATIC_A_QUICK){
			return VALUE_TYPE.REFERENCE; 
		}
		return null;
	}
	
	
	private enum VALUE_TYPE{
		CHAR,
		BYTE,
		SHORT,
		INTEGER,
		LONG,
		FLOAT,
		DOUBLE,
		BOOLEAN,
		REFERENCE
	}
	
	public static DMA_TYPE getDMAType(Datum d){
		if(d instanceof OVWriteDatum || d instanceof OVReadDatum){
			return DMA_TYPE.OBJECT_FIELD;
		} 
		if(d.creator() instanceof SVLdInstr || d.creator() instanceof SVStInstr){
			return DMA_TYPE.STATIC_FIELD;
		}
		if(d.creator() instanceof ArrayLdInstr || d.creator() instanceof ArrayStInstr || d.creator() instanceof ArrayLengthInstr){
			return DMA_TYPE.ARRAY;
		}
		
		
		return null;
	}
	
	public enum DMA_TYPE{
		ARRAY,
		STATIC_FIELD,
		OBJECT_FIELD
	}
	
	public ReferenceOrigin getReferenceOrigin(Datum d) {
		ReferenceOrigin refOrigin;

		if(d.reference() instanceof LWriteDatum || d.reference() instanceof LReadDatum){
			refOrigin = ReferenceOrigin.LV;
		} else if( d.reference() instanceof OVReadDatum || d.reference() instanceof OVWriteDatum){
			refOrigin = ReferenceOrigin.OV;
		} else if( d.reference() instanceof ConstDatum){
			refOrigin = ReferenceOrigin.CONST;
		} else if(d.reference().creator() instanceof SVLdInstr){
			refOrigin = ReferenceOrigin.SV; 
		} else {
			refOrigin = ReferenceOrigin.ARRAY;
		}
		return refOrigin;
	}

	
	
	
	/**
	 * Returns the history
	 * @return
	 */
	public LinkedList<Set<Datum>> getHistory(){
		return history;
	}
	
	/**
	 * Returns the oldest access recorded in the history
	 * @return
	 */
	public Datum getLast(){
		return oldestObject;
	}
	
	
	/**
	 * Merges two histories from two branches
	 * @param syn
	 * @param hist
	 */
	public void merge(SynthData syn, ObjectHistory hist){
		DataGraph datagraph = syn.dg();
		LinkedList<Set<Datum>> longer, shorter;
		if(hist == null || hist.history == null)
			return;
		if(this.history.size() > hist.history.size()){
			longer = this.history;
			shorter = hist.history;
		} else {
			longer = hist.history;
			shorter = this.history;
		}
		int i = 0;
		for(Set<Datum> s: shorter){
			longer.get(i).addAll(s);
			i++;
		}
		if(this.oldestObject == null){
			oldestObject = hist.oldestObject;
		} else if( hist.oldestObject != null){
			PipeDatum nop = new PipeDatum(oldestObject.reference(), oldestObject.value(), I.NOP.create(oldestObject.creator().addr()));
			datagraph.add_op(nop);
			datagraph.add_sedge(oldestObject, nop);
			datagraph.add_sedge(hist.oldestObject, nop);
			oldestObject = nop;
		}
		this.history = longer;
		
	}
	

}
