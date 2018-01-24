package amidar.sweep;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import tracer.Trace;
import tracer.TraceManager;
import amidar.AmidarSimulationResult;
import amidar.AmidarSimulator;
import amidar.ConfMan;
import amidar.axtLoader.AXTLoader;

/**
 * Class that simulates Amidar on a remote host. This is executed on the remote host
 * @author jung
 *
 */
public class AmidarRemoteSimulator implements RemoteSimulator {
	
	/**
	 * The name under which the remote simulator is registered in the RMI registry 
	 */
	private String registeredName;
	
	/**
	 * Starts a new AmidarRemoteSimulator and registers it on the RMI registry on the localhost
	 * @param args
	 */
	public static void main(String[] args){
		try {
			System.out.print("Starting AmidarRemoteSimulator... " + args[0]);
			AmidarRemoteSimulator server = new AmidarRemoteSimulator();
			RemoteSimulator stub = (RemoteSimulator) UnicastRemoteObject.exportObject(server, 0);
			
			Registry reg = LocateRegistry.getRegistry();
			reg.bind(args[0], stub);
			server.setRegName(args[0]);
			System.out.println(" DONE");
		} catch (RemoteException | AlreadyBoundException e) {
			e.printStackTrace(System.out);
			System.out.println(" FAILED");
		}
		
	}

	
	public AmidarSimulationResult run(ConfMan configManager, String name) throws RemoteException {
		FileOutputStream fos;
		AmidarSimulationResult result = null;
		try {
			fos = new FileOutputStream(new File(name));
			Trace simpleTrace = new Trace(fos, System.in, "","");
			simpleTrace.setPrefix("config");			
			configManager.printConfig(simpleTrace);
			
			/////SIM
			result = AmidarSimulator.run(configManager, fos,false);
			/////SIM END
			
			if(configManager.getTraceActivation("results")){
				simpleTrace.setPrefix("results");
				simpleTrace.printTableHeader("Simulated "+configManager.getApplicationPath()+" - Synthesis "+(configManager.getSynthesis()?"ON":"OFF"));
				simpleTrace.println("Ticks:               "+result.getTicks());
				simpleTrace.println("Bytecodes:           "+result.getByteCodes());
				simpleTrace.println("Execution Time:      "+result.getExecutionDuration()+" ms");
				simpleTrace.printTableHeader("Loop Profiling");
				result.getProfiler().reportProfile(simpleTrace);
			}
			
			
			
		} catch (FileNotFoundException e) {
			e.printStackTrace(System.out);
			throw new RemoteException("Error While trying to create log File on AmidarRemoteSimulator. See "+System.getProperty("user.dir")+"/remoteSimLog/"+registeredName+".log for more information");
		}
		
		
		return result;
	}
	
	/**
	 * Sets the name under which the remote simulator is registered in the RMI registry
	 * @param name the name
	 */
	private void setRegName(String name){
		this.registeredName = name;
	}

}
