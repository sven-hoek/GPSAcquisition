package javasim.synth.model;

import java.util.*;

import functions.HashCalculator;
import javasim.synth.SynthData;
import javasim.synth.model.ObjectHistory.DMA_TYPE;
import javasim.synth.model.datum.ConstDatum;
import javasim.synth.model.datum.Datum;
import javasim.synth.model.datum.LReadDatum;
import javasim.synth.model.datum.LWriteDatum;
import javasim.synth.model.datum.OVReadDatum;
import javasim.synth.model.datum.OVWriteDatum;
import javasim.synth.model.instruction.ArrayLdInstr;
import javasim.synth.model.instruction.ArrayLengthInstr;
import javasim.synth.model.instruction.ArrayStInstr;
import javasim.synth.model.instruction.SVLdInstr;
import javasim.synth.model.instruction.SVStInstr;

/**
 * The virtual stack helps to manage the references to all the different data
 * when building the data flow graph.
 * 
 * @author Michael Raitza
 * @version – 25.05.2011
 */
public class VStack extends LinkedList<Datum> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6768181195042687005L;
	
	/**
	 * Tracks all accesses to local variables
	 */
	private LinkedHashMap<Integer, LocalVariableHistory> locals;
	
	/**
	 * Tracks all accesses to objects and arrays
	 */
	private EnumMap<DMA_TYPE, ObjectHistory> objects;
	
	/**
	 * Tracks all accesses to static fields (TODO)
	 */
	private LinkedHashMap<Integer, Datum> statics;

	/**
	 * Denotes whether this vstack belongs to the true for false block
	 */
	private boolean decision;

	/**
	 * Constructs a new virtual stack.
	 */
	public VStack() {
		locals = new LinkedHashMap<Integer, LocalVariableHistory>();
		objects = new EnumMap<DMA_TYPE, ObjectHistory>(DMA_TYPE.class);
		statics = new LinkedHashMap<Integer, Datum>();
	}

	/**
	 * Splits the virtual stack. This operation clones the local variables the
	 * objects and the statics.
	 * 
	 * @return The split stack.
	 */
	public VStack split(boolean d) {
		VStack vs = (VStack) clone();
		vs.locals(locals());
		vs.objects(objects());
		vs.statics(new LinkedHashMap<Integer, Datum>(statics()));
		vs.decision(d);
		return vs;
	}

	/**
	 * Sets whether this vstack belongs to the true or false block
	 * @param d
	 */
	public void decision(boolean d) {
		decision = d;
	}

	/**
	 * Returns whether this vstack belongs to the true or false block
	 * @return
	 */
	public boolean decision() {
		return decision;
	}
	
	/**
	 * Adds a local variable.
	 * 
	 * @param d
	 *            the datum representing the access to a local variable
	 * @return the old list of accesses to this local variable
	 */
	public Datum local_add(Datum d) {
		LocalVariableHistory hist = locals.get(new Integer(d.value().intValue()));
		if(hist == null){
			hist = new LocalVariableHistory();
			locals.put(new Integer(d.value().intValue()), hist);
		}
		
		Datum res = hist.add(d);
		return res;
	}
	
	/**
	 * Adds a local variable access to the history of the local variable with id value.
	 * This is needed for method inlining -> contexts change
	 * @param d
	 * @param value
	 */
	public void local_add(Datum d, Integer value){
		LocalVariableHistory hist = new LocalVariableHistory();
		hist.add(d);
		locals.put(value, hist);
	}
	
	/**
	 * Adds merged local variable acces to history
	 * @param merger
	 * @param mergerPredecessors
	 * @return
	 */
	public Datum local_add(Datum merger, LinkedHashSet<Datum> mergerPredecessors){
		return local_add(new Integer(merger.value().intValue()), merger, mergerPredecessors);
	}
	
	/**
	 * Adds merged local variable acces to history
	 * @param value
	 * @param merger
	 * @param mergerPredecessors
	 * @return
	 */
	public Datum local_add(Integer value, Datum merger, LinkedHashSet<Datum> mergerPredecessors){
		LocalVariableHistory hist = locals.get(value);
		Datum ret = null; 
		if(hist != null){
			ret = hist.getLastDatum();
		}
		hist = new LocalVariableHistory(merger, mergerPredecessors);
		locals.put(value, hist);
		return ret;
	}
	
	/**
	 * Get the merged predecessors of a local variable access
	 * @param d
	 * @return
	 */
	public LinkedHashSet<Datum> getRealPredecessorsLV(Datum d){
		LocalVariableHistory hist = locals.get(new Integer(d.value().intValue()));
		if(hist != null)
			return hist.getRealPredecessors();
		else
			return null;
	}

	/**
	 * Sets the map of accesses to the local variables.
	 * 
	 * @param locs
	 *            the map local variables
	 */
	public void locals(LinkedHashMap<Integer, LocalVariableHistory> locs) {
		locals = new LinkedHashMap<Integer, LocalVariableHistory>();
		for(Integer d: locs.keySet()){
			locals.put(d, new LocalVariableHistory(locs.get(d)));
		}
	}

	/**
	 * Returns the map of all local variables and their recent accessing datum
	 * objects.
	 * 
	 * @return a map of local variables and their accessors
	 */
	public LinkedHashMap<Integer, LocalVariableHistory> locals() {
		return locals;
	}
	
	/**
	 * Returns true if this datum is the last one to acess the local variable
	 * @param d
	 * @return
	 */
	public boolean isLastAccesToLV(Datum d){
		LocalVariableHistory hist = locals.get(d.value().intValue());
		if(hist == null ){
			return true;
		}
		return hist.isLast(d);
	}

	/**
	 * Adds an object variable.
	 * 
	 * @param d
	 *            the datum representing the access to an object variable
	 * @return the old list of accesses to this object variable
	 */
	public Datum object_add(SynthData syn, Datum d) {
		DMA_TYPE dmaType = ObjectHistory.getDMAType(d);
		
		ObjectHistory hist = objects.get(dmaType);
		if(hist == null){
			hist = new ObjectHistory();
			objects.put(dmaType, hist);
		}
		return hist.add(syn,d);
	}
	
	/**
	 * Returns the history of object accesses
	 * @param d
	 * @return
	 */
	public LinkedList<Set<Datum>> getHistory(Datum d){
		DMA_TYPE marker =ObjectHistory.getDMAType(d);
		ObjectHistory hist = objects.get(marker);
		if(hist != null){
			return hist.getHistory();
		}
		else return null;
	}
	
	/**
	 * Sets the map of accesses to the object variables.
	 * 
	 * @param objs
	 *            the map object variables
	 */
	public void objects(EnumMap<DMA_TYPE, ObjectHistory> objs) {
		objects = new EnumMap<DMA_TYPE, ObjectHistory>(DMA_TYPE.class);
		for(DMA_TYPE dmaType: objs.keySet()){
			objects.put(dmaType, new ObjectHistory(objs.get(dmaType))); // create an own objHistory for each obj - using the same would be easier to handle but is probably suboptimal
		}
	}

	/**
	 * Returns the map of all object variables and their recent accessing datum
	 * objects.
	 * 
	 * @return a map of object variables and their accessors
	 */
	public EnumMap<DMA_TYPE, ObjectHistory> objects() {
		return objects;
	}

	/**
	 * Adds a static variable.
	 * 
	 * @param d
	 *            the datum representing the access to a static variable
	 * @return the old list of accesses to this static variable
	 */
	public Datum static_add(Datum d) {
		return statics.put((Integer) d.value(), d);
	}

	/**
	 * Sets the map of accesses to the static object variables.
	 * 
	 * @param objs
	 *            the map static object variables
	 */
	public void statics(LinkedHashMap<Integer, Datum> objs) {
		statics = objs;
	}

	/**
	 * Returns the map of all object variables and their recent accessing datum
	 * objects.
	 * 
	 * @return a map of object variables and their accessors
	 */
	public LinkedHashMap<Integer, Datum> statics() {
		return statics;
	}

	public String print_stack() {
		Formatter f = new Formatter();
		try {
			for (Datum s : this) {
				f.format("%s\n", s.creator().attr("label"));
			}
			return f.toString();
		} finally {
			f.close();
		}
	}
	
}
