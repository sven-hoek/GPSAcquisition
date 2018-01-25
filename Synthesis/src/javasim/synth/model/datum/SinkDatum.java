package javasim.synth.model.datum;

import javasim.synth.model.instruction.Instruction;

/**
 * This Datum represents a data sink and should have no dependences.
 * @author Michael Raitza
 * @version – 07.07.2011
 */
public class SinkDatum extends Datum {

	private SinkDatum() {
		super(0, null);
	}

	public SinkDatum(Number value, Instruction creator) {
		super(value, creator);
	}

	public SinkDatum(Datum reference, Number value, Instruction creator) {
		super(value, creator);
		reference(reference);
	}

	public AccessType accessType(){
		return Datum.AccessType.WRITE;
	}

	public Type type() {
		return Datum.Type.SINK;
	}
}
