package amidar.sweep;

import java.rmi.Remote;
import java.rmi.RemoteException;

import tracer.TraceManager;
import amidar.AmidarSimulationResult;
import amidar.ConfMan;
import amidar.axtLoader.AXTLoader;

/**
 * RMI interface for AmidarRemote Simulation
 * @author jung
 *
 */
public interface RemoteSimulator extends Remote {
	
	/**
	 * Runs a simulation on the remote simulator
	 * @param configManager stores the configuration of the simulation
	 * @param name the name of the logfile of this simulation
	 * @return
	 * @throws RemoteException
	 */
	AmidarSimulationResult run(ConfMan configManager, String name) throws RemoteException;

}
