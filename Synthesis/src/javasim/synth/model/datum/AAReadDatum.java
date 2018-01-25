package javasim.synth.model.datum;

import javasim.synth.model.instruction.Instruction;

/**
 * Datum that reads from an array
 * @author jung
 *
 */
public class AAReadDatum extends Datum implements Indexed{

	private Datum index;
	protected boolean wide;

	private AAReadDatum() {
		super(0, null);
	}

	public AAReadDatum(Datum reference, Datum index, Number value, Instruction creator) {
		super(value, creator);
		this.index = index;
		reference(reference);
		wide = false;
	}

	public AccessType accessType() {
		return Datum.AccessType.READ;
	}

	public Type type() {
		return Datum.Type.DYNAMIC_OBJECT;
	}
	
	public Datum index(){
		return index;
	}

}
