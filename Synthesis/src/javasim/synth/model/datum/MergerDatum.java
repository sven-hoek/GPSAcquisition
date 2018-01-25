package javasim.synth.model.datum;

import javasim.synth.model.instruction.Instruction;

/**
 * This Datum represents a merger node that is a special node to calculate scheduling
 * dependences across Φ node boundaries.
 *
 * @author Michael Raitza
 * @version – 07.07.2011
 */
public class MergerDatum extends Datum {

	private final Datum ifnode;

	private MergerDatum() {
		super(0, null);
		ifnode = null;
		attr("shape","octagon");
		attr("style","filled");
		attr("fillcolor","\"#ffbcbc\"");
	}

	/**
	 * Constructs a new MergerDatum.
	 */
	public MergerDatum(Datum reference, Datum ref2, Number value, Instruction creator, Datum ifnode) {
		super(value, creator);
		reference(reference);
		this.ifnode = ifnode;
		attr("shape","octagon");
		attr("style","filled");
		attr("fillcolor","\"#ffbcbc\"");
	}

	public AccessType accessType(){
		return Datum.AccessType.WRITE;
	}

	public Type type() {
		return Datum.Type.MERGER;
	}

	public Datum toSchedule() {
		return ifnode;
	}

}
