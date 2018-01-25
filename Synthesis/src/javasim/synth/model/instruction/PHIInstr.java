package javasim.synth.model.instruction;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;

import javasim.synth.SynthData;
import javasim.synth.model.I;
import javasim.synth.model.LocalVariableHistory;
import javasim.synth.model.ObjectHistory;
import javasim.synth.model.VStack;
import javasim.synth.model.ObjectHistory.DMA_TYPE;
import javasim.synth.model.datum.Datum;
import javasim.synth.model.datum.LRead64Datum;
import javasim.synth.model.datum.LReadDatum;
import javasim.synth.model.datum.LWrite64Datum;
import javasim.synth.model.datum.LWriteDatum;
import javasim.synth.model.datum.MergerDatum;
import javasim.synth.model.datum.PipeDatum;

/**
 * The PHIInstr represents a Φ node merge control flow
 * in the instruction graph.
 *
 * @author Michael Raitza
 * @version 14.04.2011
 */
public class PHIInstr extends Instruction {

	/**
	 * The if Datum that created the branches
	 */
	private Datum ifdatum;
	
	/**
	 * The if-instruction  that created the branches
	 */
	private AbstractIF ifinstr;
	
	
	/**
	 * Holds the vstack version before the merge
	 */
	private VStack oldstack;

	/**
	 * Constructs a new Φ node.
	 * @param instr the instruction type (I.SYNTH_PHI)
	 * @param pos the address of the instruction
	 */
	public PHIInstr(I instr, Integer pos) {
		super(instr, pos);
		attr("shape", "polygon");
		attr("sides", "4");
		attr("distortion", "\"0.05\"");
		attr("width","\"0.1\"");
		oldstack = null;
	}

	/**
	 * Evaluates this instruction. Merges the two virtual stacks and adds
	 * access barriers to the data graph.
	 * @param data holds the synthesis context as a SynthData object
	 * @see javasim.synth.model.I#eval
	 */
	public void eval(SynthData data) {
		if (oldstack == null) {
			oldstack = vstack();
			return;
		}
		VStack newstack = vstack().split(vstack().decision());
		if (vstack().decision())
			merge_stacks(data, newstack, oldstack);
		else
			merge_stacks(data, oldstack, newstack);

		oldstack = null; /* sanitise */
		vstack().decision(decision());
		super.eval(data);
	}

	/**
	 * Adds a NOP in the datagraph
	 * @param d
	 * @param data
	 * @return
	 */
	private Datum add_nop(Datum d, SynthData data) {
		if (d.type() == Datum.Type.MERGER) {
			Datum nop = new PipeDatum(d.reference(), d.value(), I.NOP.create(d.creator().addr()));
			data.dg().add_op(nop);
			data.dg().add_edge(d, nop);
			return nop;
		}
		return d;
	}

	/**
	 * Merges two stacks and sets the result as own vstack
	 * @param data
	 * @param t_stack
	 * @param f_stack
	 */
	private void merge_stacks(SynthData data, VStack t_stack, VStack f_stack) {
		LinkedHashMap<Integer, LocalVariableHistory> oldlocals = f_stack.locals();
		LinkedHashMap<Integer, LocalVariableHistory> newlocals = t_stack.locals();

		for (Integer d : oldlocals.keySet()) {


			Datum nd = null, od = null;

			/* merge local variables */
			LocalVariableHistory newHistory = newlocals.get(d);
			LocalVariableHistory oldHistory = oldlocals.get(d);
			if(newHistory != null)
				nd = newlocals.get(d).getLastDatum();
			if(oldHistory != null)
				od = oldlocals.get(d).getLastDatum();


			if ((nd == null)) {

				vstack().local_add(d,od, oldHistory.getRealPredecessors());
				if(od instanceof LWriteDatum){
					((LWriteDatum)od).defineAsNecessary();
				}
				continue;
			}else if( nd.equals(od)){
				oldHistory.getRealPredecessors().addAll(newHistory.getRealPredecessors());
				vstack().local_add(d,od, oldHistory.getRealPredecessors());
				if(od instanceof LWriteDatum)
					((LWriteDatum)od).defineAsUnNecessary();
				continue;
			}

			LinkedHashSet<Datum> mergerPredecessors = new LinkedHashSet<Datum>();
			mergerPredecessors.addAll(newlocals.get(d).getRealPredecessors());
			mergerPredecessors.addAll(oldlocals.get(d).getRealPredecessors());
			
			if(od instanceof LWriteDatum)
                                        ((LWriteDatum)od).defineAsNecessary();
			if(nd instanceof LWriteDatum)
                                        ((LWriteDatum)nd).defineAsNecessary();

			nd = add_nop(nd, data);
			od = add_nop(od, data);
	
			Datum md = new MergerDatum(od, nd, d, ifdatum.creator(), ifdatum);
			if (branchpoint() != null)
				data.dg().add_sedge(branchpoint().ifdatum(), md);
			data.dg().add_op(md);
			data.dg().add_edge(nd, md, 1);
			data.dg().add_edge(od, md, 2);
			mergerPredecessors.add(md);
			vstack().local_add(d,md, mergerPredecessors);
		}
		for (Integer d : newlocals.keySet()) {
			Datum nd = newlocals.get(d).getLastDatum();
			if(vstack().locals().containsKey(nd.value()))
				continue;
			else{
				vstack().local_add(d,nd, newlocals.get(d).getRealPredecessors());
				if(nd instanceof LWriteDatum){
						((LWriteDatum)nd).defineAsNecessary();
				}
			}
		}
		
		
		
		
		
		LinkedHashMap<Integer, Datum> oldstatics = f_stack.statics();
		LinkedHashMap<Integer, Datum> newstatics = t_stack.statics();
		
		/* Merge static variables */
		for (Integer d : oldstatics.keySet()) {
			Datum nd = newstatics.get(d);
			Datum od = oldstatics.get(d);

			if ((nd == null) || (nd.equals(od))) {
				vstack().static_add(od);
				continue;
			}

			nd = add_nop(nd, data);
			od = add_nop(od, data);

			Datum md = new MergerDatum(od, nd, od.value(), ifdatum.creator(), ifdatum);
			if (branchpoint() != null)
				data.dg().add_sedge(branchpoint().ifdatum(), md);
			data.dg().add_op(md);
			data.dg().add_edge(nd, md, 1);
			data.dg().add_edge(od, md, 2);
			vstack().static_add(md);
		}
		for (Integer d : newstatics.keySet()) {
			Datum nd = newstatics.get(d);
			if(vstack().statics().containsKey(nd.value()))
				continue;
			else
				vstack().static_add(nd);
		}
		EnumMap<DMA_TYPE, ObjectHistory> oldobjects = f_stack.objects();
		EnumMap<DMA_TYPE, ObjectHistory> newobjects = t_stack.objects();
		for (DMA_TYPE d : oldobjects.keySet()) {
			oldobjects.get(d).merge(data, newobjects.get(d));
			vstack().objects().put(d,oldobjects.get(d));
		}
		
		for (DMA_TYPE d : newobjects.keySet()) {
			if(vstack().objects().containsKey(d))
				continue;
			else{
			vstack().objects().put(d,newobjects.get(d));
			}
		}

		/* Merge elements on stack */
		LinkedList<Datum> mergerstack = new LinkedList<Datum>();
		
		while(t_stack.size() !=  f_stack.size()){ // this should only be the case for multiple return statements in method inlining - result is pushed only once
			if(t_stack.size() > f_stack.size()){
				mergerstack.push(t_stack.pop());
			}else {
				mergerstack.push(f_stack.pop());
			}
		}
		
		
		while(t_stack.size() != 0 && f_stack.size() != 0) {
			Datum nd = t_stack.pop();
			Datum od = f_stack.pop();

			if (nd.equals(od)) {
				mergerstack.push(nd);
				continue;
			}

			Datum s1;
			Datum s2;
//			Instruction cl;
			Datum l;
			int lvID = data.getFreeLVID();
			Instruction c1;
			Instruction c2;
			if(nd.creator().i().wdata()){

				 c1 =  I.LSTORE.create(ifinstr().addr());
				c1.branchpoint(this);
				c1.decision(true);
				 c2 =  I.LSTORE.create(ifinstr().addr());
				c2.branchpoint(this);
				c2.decision(false);



				s1 = new LWrite64Datum(lvID, c1, nd);
				s2 = new LWrite64Datum(lvID, c2, od);


				Instruction cl =   I.LLOAD.create(ifinstr().addr());
				l = new LRead64Datum(lvID, cl);
			} else{


				 c1 =  I.ISTORE.create(ifinstr().addr());
				c1.branchpoint(this);
				c1.decision(true);
				 c2 =  I.ISTORE.create(ifinstr().addr());
				c2.branchpoint(this);
				c2.decision(false);



				s1 = new LWriteDatum(lvID, c1, nd);
				s2 = new LWriteDatum(lvID, c2, od);


				Instruction cl =   I.ILOAD.create(ifinstr().addr());
				l = new LReadDatum(lvID, cl);

			}

			lvID++;
			data.dg().add_op(s1);
			data.dg().add_op(s2);
			data.dg().add_node(l);
			data.dg().add_edge(nd, s1);
			data.dg().add_edge(od, s2);
			data.dg().add_sedge(ifdatum(), s1);
			data.dg().add_sedge(ifdatum(), s2);
			
			if(c1.addr()<c2.addr())
				data.dg().add_sedge(s1, s2);
			else if(c1.addr()>c2.addr())
				data.dg().add_sedge(s2, s1);
			data.dg().add_sedge(s1, l);
			data.dg().add_sedge(s2, l);
			
			mergerstack.push(l);
		}
		while (t_stack.size() != 0)
			mergerstack.push(t_stack.pop());
		while (f_stack.size() != 0)					
			mergerstack.push(f_stack.pop());
		vstack().clear();
		while (mergerstack.size() != 0)
			vstack().push(mergerstack.pop());
	}
	
//	static int lvID = 257; // TODO: anpassen an return id dingsi + mergen nochmal überprüfen (sind wirklich alle schritte nötig)

	/**
	 * Sets the current branch point for this Φ node
	 * to <code>bp</code>.
	 * @param bp the Instruction object holding the branching instruction.
	 */
	public void branchpoint(PHIInstr bp) {
		super.branchpoint(bp);
	}

	/**
	 * Sets the Datum node to the corresponding IF* instruction of this Φ node.
	 * @param d the datum object of the IF* instruction
	 */
	public void ifdatum(Datum d) {
		ifdatum = d;
	}

	/**
	 * Gets the Datum node to the corresponding IF* instruction of this Φ node.
	 * @return
	 */
	public Datum ifdatum() {
		return ifdatum;
	}

	/**
	 * Sets the IF* instruction of this Φ node.
	 * @return
	 */
	
	public void ifinstr(AbstractIF is) {
		ifinstr = is;
	}

	/**
	 * Gets the IF* instruction of this Φ node.
	 * @return
	 */
	public AbstractIF ifinstr() {
		return ifinstr;
	}

	/**
	 * Sets the branchpoint and decision of the later one.
	 * Is used only in Short circuit evaluation
	 * @param branchpoint
	 * @param decision
	 */
	public void branchpoint2(PHIInstr branchpoint, boolean decision) {
		if(branchpoint != null && branchpoint().ifinstr().addr()>branchpoint.ifinstr().addr()){
			branchpoint(branchpoint);
			decision(decision);
		}
	}
}
