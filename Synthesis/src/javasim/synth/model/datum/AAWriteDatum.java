package javasim.synth.model.datum;

import javasim.synth.SynthData;
import javasim.synth.model.instruction.Instruction;

/**
 * Datum that writes to an array
 * @author jung
 *
 */
public class AAWriteDatum extends Datum implements Indexed{

	private Datum index;
	private Datum datum;
	protected boolean wide;

	private AAWriteDatum() {
		super(0, null);
	}

	public AAWriteDatum(Datum reference, Datum index, Datum datum, Number value, Instruction creator) {
		super(value, creator);
		this.index = index;
		this.datum = datum;
		reference(reference);
		wide = false;
	}

	public AccessType accessType(){
		return Datum.AccessType.WRITE;
	}

	public Type type() {
		return Datum.Type.DYNAMIC_OBJECT;
	}
	
	public Datum index(){
		return index;
	}
	
	public Datum datum(){
		return datum;
	}

}
