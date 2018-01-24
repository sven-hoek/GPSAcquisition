package exceptions;

/**
 * Exception that concerns the Simulator (not to be used in simulated Code)
 * @author jung
 *
 */
public class AmidarSimulatorException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6150159905356301564L;

	public AmidarSimulatorException(String string) {
		super(string);
	}

}
