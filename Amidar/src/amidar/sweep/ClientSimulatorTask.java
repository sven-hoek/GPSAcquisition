package amidar.sweep;

import java.io.File;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import amidar.AmidarSimulationResult;
import amidar.ConfMan;
import amidar.axtLoader.AXTLoader;
import tracer.Trace;
import tracer.TraceManager;

/**
 * Thread that connects to a remote Simulator and starts simulions on it
 * @author jung
 *
 */
public class ClientSimulatorTask extends Thread {
	
	/**
	 * Pointer to the next simulation configuration to be simulated.
	 * This Integer is the monitor for all clientSimulatior tasks
	 */
	static Integer III = 0;
	
	/**
	 * Name of the folder where to store the results
	 */
	static String logFolder;
	
	/**
	 * Holds the configurations for all simulations
	 */
	static ConfMan[] simulations;
	
	/**
	 * Holds the names of all simulations
	 */
	static String[] simulationNames;
	
	/**
	 * Holds the results of all simulations
	 */
	static AmidarSimulationResult[] results;

	/**
	 * The number of simulations
	 */
	static int nrOfSimulations;
	
	/**
	 * The name of the remote simulator stub of this client thread
	 */
	String stubName;
	
	/**
	 * The result of the latest simulation
	 */
	AmidarSimulationResult result;
	

	/**
	 * The name of the host running the RMI registry
	 */
	String registryHost;
	
	/**
	 * The port to acces the RMI registry on the remote host
	 */
	int registryPort;
	
	/**
	 * Pointer to the current simulation configuration of this thread
	 */
	int currentI = 0;
	
	/**
	 * Current simulation configuration of this thread
	 */
	ConfMan configManager;
	
	/**
	 * Trace to output status messages on the client machine
	 */
	Trace sweepTrace;
	
	/**
	 * Creates a new ClientSimulatorTask to start simulations on a remote simulator
	 * (handled by RMI).
	 * @param stubName the RMI registry name of the corresponding remote simulator
	 * @param sweepTrace the output trace on the client machine
	 * @param registryHost name of the host running the RMI registry
	 * @param registryPort the port to access the RMI registry on the remote host
	 */
	public ClientSimulatorTask(String stubName, Trace sweepTrace, String registryHost, int registryPort) {
		this.stubName = stubName;
		this.sweepTrace = sweepTrace;
		this.registryHost = registryHost;
		this.registryPort = registryPort;
	}
	
	/**
	 * Initializes the ClientSimulatorTask Class with a sweep configuration. This creates a static list of simulations 
	 * that can be executed by different ClientSimulatorTask instances.
	 * @param sweep
	 * @param log
	 */
	public static void init(SweepConfig sweep, String log){
		synchronized (III) {
			III  = 0;
		}
		simulations = sweep.getConfManager();
		simulationNames = sweep.getSweepConfigurationFileNames();
		nrOfSimulations = simulations.length;
		results = new AmidarSimulationResult[nrOfSimulations];
		logFolder = log;
		File f = new File(logFolder);
		f.mkdirs();
	}


	/**
	 * Starts this client thread. The treads starts the next simulation using the pointer III (synchronized with other client threads).
	 * The thread finishes when there is no simulation left to execute
	 */
	public void run(){
		Registry reg;
		while(true){
			synchronized(III){
				currentI = III;
				III++;
			}
			if(currentI >= nrOfSimulations){
				break;
			}
			configManager = simulations[currentI];
			String instanceName = simulationNames[currentI].replace("/", ".").replaceFirst("application:...axt.",	"");
			sweepTrace.println(stubName + " simulating " + instanceName + " host: " + registryHost+"::"+registryPort);
			try {
				reg = LocateRegistry.getRegistry(registryHost, registryPort);
				RemoteSimulator stub = (RemoteSimulator) reg.lookup(stubName);
				
				result = stub.run(configManager, logFolder+"/"+instanceName+".log");
			} catch (RemoteException | NotBoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace(System.err);
			}
			sweepTrace.println(stubName + " finished");
			results[currentI] = result;
			
			
		}
	}

	/**
	 * Returns the results of all simulations
	 * @return the results
	 */
	public static AmidarSimulationResult[] getResults() {
		return results;
	}

}