package generator;

/**
 * A stimulus is an incoming instruction including related data to the CGRA. For instance, for Amidar these can be tracked during simulation
 * and can be used as a trigger in HDL simulations in Modelsim. 
 * @author wolf
 *
 */
public abstract class Stimulus {

	public abstract String taskCall();

}
