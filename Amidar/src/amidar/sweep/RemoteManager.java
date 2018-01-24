package amidar.sweep;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * RMI Interface for AmidarRemoteManager
 */
public interface RemoteManager extends Remote {
	
	/**
	 * Starts new AmidarRemoteSimulator instances and registers them on the RMI registry
	 * @param numberOfServers the number of Simulator Instances to be started
	 */
	public boolean createServers(int numberOfServers) throws RemoteException;
	
	/**
	 * Closes all AmidarRemoteSimulator instances created by this manager
	 */
	public boolean closeServers() throws RemoteException;
	
	/**
	 * Converts a Java application in to AXT format on the remote host, so that RemoteSimulators can simulate it.
	 */
	public String convertApplication(String applicationPath, String[] args) throws RemoteException;
	
	/**
	 * Close the remote Manager and the corresponding RMI registry
	 * @throws RemoteException
	 */
	public void close() throws RemoteException;

}
