package javasim.synth.model.datum;

import javasim.synth.model.I;
import javasim.synth.model.instruction.StartInstr;


/**
 * A virtual node of the data flow graph representing
 * the virtual start of dependencies
 *
 * @author Andreas Dixius
 * @version 18.08.2012
 */
public class StartDatum extends Datum {

	/**
	 * Creates a new StartDatum for DepList's virtual start-node
	 */
	public StartDatum() {
		super(0, new StartInstr(I.NOP, 0));
	}

	public AccessType accessType(){
		return AccessType.READ;
	}

	public Type type() {
		return Type.CONSTANT;
	}
}
