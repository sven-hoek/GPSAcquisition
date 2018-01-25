package javasim.synth.model;

import java.util.LinkedHashSet;

import javasim.synth.model.datum.Datum;
import javasim.synth.model.datum.LWriteDatum;

/**
 * Stores the history of local variable accesses
 * @author jung
 *
 */
public class LocalVariableHistory {
	
	/**
	 * The datum that accessed this local variable the last time
	 */
	private Datum lastDatum;					
	
	/**
	 * When two or more branches are merged, this set holds all the lastDatums of each branch
	 */
	private LinkedHashSet<Datum>	realPredecessors;
	
	/**
	 * Creates a new local variable history
	 */
	public LocalVariableHistory(){
		lastDatum = null;
		realPredecessors = null;
	}
	
	/**
	 * Creates a new local variable history
	 * @param merger
	 * @param mergerPredecessors
	 */
	public LocalVariableHistory(Datum merger, LinkedHashSet<Datum> mergerPredecessors){
		lastDatum = merger;
		realPredecessors = mergerPredecessors;
	}
	
	/**
	 * Creates a new local variable history
	 * @param hist
	 */
	public LocalVariableHistory(LocalVariableHistory hist){
		this.lastDatum = hist.lastDatum;
		if(this.lastDatum instanceof LWriteDatum){
			((LWriteDatum)this.lastDatum).defineAsNecessary();
		}
		this.realPredecessors = new LinkedHashSet<Datum>(hist.getRealPredecessors());
	}
	
	
	/**
	 * Adds a new acces to the local variable to the history
	 * @param newDatum the accessing datum
	 * @return the datum that accessed the local variable the previous time. 
	 */
	public Datum add(Datum newDatum){
		Datum old = lastDatum;
		lastDatum = newDatum;
		realPredecessors = new LinkedHashSet<Datum>();
		realPredecessors.add(newDatum);
		return old;
	}
	
	/**
	 * Gets all predecessors
	 * @return
	 */
	public LinkedHashSet<Datum> getRealPredecessors() {
		return realPredecessors;
	}
	
	/**
	 * Get the datum that accessed this local variable last
	 * @return
	 */
	public Datum getLastDatum(){
		return lastDatum;
	}
	
	/**
	 * Returns true if the given Datum is the last one that accessed this local variable
	 * @param d
	 * @return
	 */
	public boolean isLast(Datum d){
		return d.equals(lastDatum);
	}

}
