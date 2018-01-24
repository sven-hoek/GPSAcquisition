package javasim.synth.model.datum;

import javasim.synth.model.instruction.Instruction;

/**
 * This Datum represents a data pipe.
 * @author Michael Raitza
 * @version – 07.07.2011
 */
public class PipeDatum extends Datum {

	private PipeDatum() {
		super(0, null);
	}

	private PipeDatum(Number value, Instruction creator) {
		super(value, creator);
	}

	public PipeDatum(Datum reference, Number value, Instruction creator) {
		super(value, creator);
		reference(reference);
	}

	public AccessType accessType(){
		return AccessType.WRITE;
	}

	public Type type() {
		return Type.PIPE;
	}
	
}
