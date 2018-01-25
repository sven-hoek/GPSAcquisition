package javasim.synth.model.datum;

import javasim.synth.model.instruction.Instruction;

/**
 * A single node of the data flow graph representing
 * the write to a dynamic object reference.
 *
 * @author Michael Raitza
 * @version 14.04.2011
 */
public class OVWriteDatum extends Datum {
	
	Datum storedData;

	/**
	 * Constructs a new dynamic object reference write node.
	 * @param reference the reference this dynamic object lives at
	 * @param datum the datum this write consumes to write it to the referenced address
	 * @param value the "value" associated with this datum. The meaning of value highly
	 * depends on the type of node
	 * @param creator the instruction which created this node
	 */
	public OVWriteDatum(Datum reference, Number value, Datum storedData, Instruction creator) {
		super(value, creator);
		reference(reference);
		this.storedData = storedData; 
	}

	public AccessType accessType() {
		return Datum.AccessType.WRITE;
	}

	public Type type() {
		return Datum.Type.DYNAMIC_OBJECT;
	}
	
	public Datum getStoredData(){
		return storedData;
	}

}
