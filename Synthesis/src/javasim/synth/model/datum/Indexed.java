package javasim.synth.model.datum;

/**
 * Interface for object accesses with index
 * @author jung
 *
 */
public interface Indexed {
	
	/**
	 * Returns the index of via which the access on the object/array takes place
	 * @return
	 */
	public Datum index();

}
