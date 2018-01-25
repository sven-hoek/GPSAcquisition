package amidar.sweep;

import java.io.File;
import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import amidar.AmidarSimulator;

/**
 * Class to manage the Simulation on a remote Server. Runs on the remote machine.
 * @author jung
 *
 */
public class AmidarRemoteManager implements RemoteManager {
	
	/**
	 * Stub name of the Amidar Remote Manager in RMI Registry
	 */
	public static final String REMOTE_MANAGER_STUB_NAME = "Amidar_manager";
	
	/**
	 * Stub namebase for the Amidar Remote Simulators in RMI Registry
	 */
	public static final String REMOTE_SERVER_STUB_NAME = "AmidarL_";
	
	
	/**
	 * The name of the Host on which the RMI Registry is running
	 */
	String registryHost;
	
	/**
	 * The port on which the RMI Registry is reachable on the host. Default value is 1099
	 */
	int registryPort;
	
	/**
	 * Stores all AmidarSimulator processes running on that machine 
	 */
	Process[] servers;
	
	/**
	 * The process of the RMI registry
	 */
	Process regProcess;
	
	/**
	 * A watchdog to terminate the AmidarRemoteManager when not used for a certain time.
	 * (As AMIDAR is in development the simulator is modified frequently. Thus is likely
	 * that RemoteSimulators that ran for a longer time on a Remote Host are outdated.)
	 */
	WatchDog watchdog;
	
	/**
	 * Creates a new RemoteManager
	 * @param registryHost
	 * @param registryPort
	 */
	private AmidarRemoteManager(String registryHost, int registryPort){
		this.registryHost = registryHost;
		this.registryPort = registryPort;
		watchdog = new WatchDog();
//		watchdog.start();
	}
	

	/**
	 * Either starts or stops a remote server depending on the first element of args:
	 * <ul>
	 * <li><b>createRegistry</b>: Creates a new RMI registry on localhost (the machine on which this code is executed) using port 1099 and starts a RemoteManager and registers it</li>
	 * <li><b>close</b>: Gets the RemoteManager via RMI registry on the given host & port (args[1] & args[2]). Then the method close() is called on the manager in order to stop both the manager and the RMI registry</li>
	 * <li><b>if non from above</b>: A manager is created and registered on the RMI manager running on the given host & port (args[1] & args[2]).
	 * </ul>
	 * @param args
	 */
	public static void main(String[] args) {
		
		try {
			if(args[0].equals("close")){
				Registry reg = LocateRegistry.getRegistry(args[1], Integer.parseInt(args[2]));
				
				RemoteManager manager;
				try {
					manager = (RemoteManager) reg.lookup(REMOTE_MANAGER_STUB_NAME);
					manager.close();
				} catch (NotBoundException e) {
					System.out.println("No Remote manager found on " + args[1]+":"+args[2]+".");
					e.printStackTrace();
				}
				
				
			
				
				
				return;
			}
			
			
			System.out.print("Starting AmidarRemoteManager...");
			Process regP = null;
			String registryHost;
			int registryPort;
			
			if(args[0].equals("createRegistry")){
				File logFolder = new File("log/remoteSimLog");
				logFolder.mkdirs();
				ProcessBuilder regProcessBuilder = new ProcessBuilder("rmiregistry", "-J-Djava.rmi.server.codebase="
						+ "file://" + System.getProperty("user.dir") + "/../AmidarTools/bin/ "
						+ "file://" + System.getProperty("user.dir") + "/../Synthesis/bin/ "
						+ "file://" + System.getProperty("user.dir") + "/../cgra/CGRA/bin/ "
						+ "file://" + System.getProperty("user.dir") + "/bin/ "
						+ "file://" + System.getProperty("user.dir") + "/../AXTLoader/bin/ "
						+ "file://" + System.getProperty("user.dir") + "/../AmidarTools/lib/axtConverter.jar "
						+ "file://" + System.getProperty("user.dir") + "/../AmidarTools/lib/bcel-5.2.jar "
						+ "file://" + System.getProperty("user.dir") + "/../AmidarTools/lib/commons-lang-2.6.jar "
						+ "file://" + System.getProperty("user.dir") + "/../AmidarTools/lib/j-text-utils-0.3.3.jar "
						+ "file://" + System.getProperty("user.dir") + "/../AmidarTools/lib/json-simple-1.1.1.jar "
						+ "file://" + System.getProperty("user.dir") + "/../AmidarTools/lib/lombok.jar");

				File regLog = new File("log/remoteSimLog/registry.log");
				regProcessBuilder.redirectError(regLog);
				regProcessBuilder.redirectOutput(regLog);
				regP = regProcessBuilder.start();
				
				registryHost = "127.0.0.1";
				
				System.out.print(" using port 1099...");
				registryPort = 1099;
				Thread.sleep(1000);
			}else{
				registryHost = args[0];
				registryPort = Integer.parseInt(args[1]);
			}
			AmidarRemoteManager server = new AmidarRemoteManager(registryHost, registryPort);
			server.setRegProcess(regP);
			
			RemoteManager stub = (RemoteManager) UnicastRemoteObject.exportObject(server, 0);
			
			Registry reg = LocateRegistry.getRegistry(registryHost, registryPort);
			
			reg.bind(REMOTE_MANAGER_STUB_NAME, stub);
			System.out.println(" DONE");
		} catch (InterruptedException | AlreadyBoundException | IOException e) {
			e.printStackTrace(System.out);
			System.out.println(" FAILED");
		}

	}


	/**
	 * Starts new AmidarRemoteSimulator instances and registers them on the RMI registry
	 * @param numberOfServers the number of Simulator Instances to be started
	 */
	public boolean createServers(int numberOfServers) throws RemoteException {

		try{
			servers = new Process[numberOfServers];

			File f = new File("log/remoteSimLog");
			f.mkdirs();

			for(int i = 0; i < numberOfServers; i++){
				String serverName = REMOTE_SERVER_STUB_NAME + i ;

				ProcessBuilder pb = new ProcessBuilder("java", "-cp", "../Amidar/bin:../AmidarTools/bin:../AmidarTools/lib/axtConverter.jar:../AmidarTools/lib/bcel-5.2.jar:../AmidarTools/lib/commons-lang-2.6.jar:../AmidarTools/lib/j-text-utils-0.3.3.jar:../AmidarTools/lib/json-simple-1.1.1.jar:../AmidarTools/lib/lombok.jar:../AXTLoader/bin:../Synthesis/bin:../cgra/CGRA/bin", "amidar.sweep.AmidarRemoteSimulator", serverName);
				File log = new  File("log/remoteSimLog/"+serverName+".log");
				pb.redirectError(log);
				pb.redirectOutput(log);

				servers[i] = pb.start();

			}
			return true;
		} catch(IOException e){
			e.printStackTrace(System.out);
			return false;
		}
	}

	/**
	 * Closes all AmidarRemoteSimulator instances created by this manager
	 */
	public boolean closeServers() throws RemoteException {
		Registry reg = LocateRegistry.getRegistry(registryHost, registryPort);
		for(int i = 0; i< servers.length; i++){
			if(servers[i] != null){
				try {
					reg.unbind(REMOTE_SERVER_STUB_NAME+i);
				} catch (NotBoundException e) {
					e.printStackTrace();
				}
				servers[i].destroy();
			}
		}
		servers = null;
		watchdog.reset();
		return false;
	}


	/**
	 * Converts a Java application in to AXT format on the remote host, so that RemoteSimulators can simulate it.
	 */
	public String convertApplication(String applicationPath, String[] args) throws RemoteException {
		System.out.println("Converting application " + applicationPath);
		return AmidarSimulator.convertApplication(applicationPath, args);
	}
	
	/**
	 * Stores the process of the RMI registry in order to be able to close it lateron
	 * @param regProcess
	 */
	private void setRegProcess(Process regProcess){
		this.regProcess = regProcess;
	}
	

	/**
	 * A thread that stops the remote manager and RḾI registry after 1 second.
	 * @author jung
	 *
	 */
	class Closer extends Thread{
		
		/**
		 * Closes the application and the RMI registry after 1 second.
		 */
		public void run(){
			try {
				System.out.println("Stopping AmidarRemoteManager... ");
				sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			regProcess.destroy();
			System.exit(0);
		}
		
	}
	
	/**
	 * A watchdog to terminate the RMI registry and the remote manager after a certain time of absence.
	 * (As AMIDAR is in development the simulator is modified frequently. Thus is likely
	 * that RemoteSimulators that ran for a longer time on a Remote Host are outdated.)
	 * @author jung
	 *
	 */
	class WatchDog extends Thread{
		
		/**
		 * The timeout
		 */
		public static final int TIMEOUT_MIN = 3*60;
		
		/**
		 * The evaluation interval
		 */
		public static final int WAIT_MS = 1000;
		
		/**
		 * The timer
		 */
		int cnt = TIMEOUT_MIN;
		
		/**
		 * Counts down when the remote manager is idle and terminates it when the timer is zero.S
		 */
		public void run(){
			while(true){
				try {
					Thread.sleep(WAIT_MS);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if(servers == null){
					if(cnt-- == 0){
						regProcess.destroy();
						System.exit(0);
					}
				}
			}
		}
		
		/**
		 * Resets the timer to the maximum. Used to keep the manager alive.
		 */
		private void reset(){
			cnt = TIMEOUT_MIN;
		}
		
	}


	/**
	 * Starts a thread that closes the remote manager and the corresponding RMI registry in one second.
	 * This is not done in this method directly because this method will be called remotely. Thus it has
	 * to return so that no exceptions are thrown on the calling machine.
	 */
	public void close() throws RemoteException {
		if(servers != null){
			closeServers();
		}
		new Closer().start();
		
	}
}
