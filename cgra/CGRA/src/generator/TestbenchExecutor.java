package generator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import target.Processor;

public class TestbenchExecutor {

	static Process process;
	
	public TestbenchExecutor(){
	}
	
	public boolean runTestbench(){
		
		try {
			Process chmod = Runtime.getRuntime().exec("chmod +x+r ./"+ Processor.Instance.getHardwareDestinationPath() +"/simtrigger.sh\n");
			BufferedReader chmoderror = new BufferedReader(new InputStreamReader(chmod.getErrorStream()));
			String line = "";
			while ((line = chmoderror.readLine())!= null) {
				System.err.println("(e - chmod) " + line);
				return false;
			}
			
			ProcessBuilder pb = new ProcessBuilder(new String[]{"sh","-c","./"+ Processor.Instance.getHardwareDestinationPath()+"/simtrigger.sh"});
			pb.redirectError(new File(Processor.Instance.getDebuggingPath()+"/verilogSimulationErrorLog.txt"));
			pb.redirectOutput(new File(Processor.Instance.getDebuggingPath()+"/verilogSimulationOutputLog.txt"));
			process = pb.start();
			
			process.waitFor();
			BufferedReader reader = new BufferedReader(new FileReader(Processor.Instance.getDebuggingPath()+"/verilogSimulationOutputLog.txt"));
			BufferedReader error = new BufferedReader(new FileReader(Processor.Instance.getDebuggingPath()+"/verilogSimulationErrorLog.txt"));			
			line = "";

			while ((line = reader.readLine())!= null) {
//				System.out.println("(vsim) " + line);
				if(line.contains("Errors: ")){
					String errorstring = line.substring(line.indexOf(":")+1,line.indexOf(",")).trim();
					int errors = Integer.parseInt(errorstring);
					if(errors >0){
						System.err.println("Errors during Simulation (This is a Verilog based problem)");
						return false;
					}
					String substringwarnings = line.substring(line.lastIndexOf(":")+2);
					int warnings = Integer.parseInt(substringwarnings);
					if(warnings > 0){
						System.out.println(warnings + " warnings during Modelsim Simulation");
					}
				}
				if(line.contains("Cosimulation has failed")){
					System.err.println(" Failbehaviour during Simulation (This is a Verilog based problem). sending wrong local variable");
					return false;
				}
			}
			
			while ((line = error.readLine())!= null) {
				System.err.println("(vsim error) " + line);
				return false;
			}
			reader.close();
			error.close();
		} catch (IOException e) {
			System.err.println("IO Exception while executing bash vsim -c -do ...");
			e.printStackTrace();
			return false;
		} catch (InterruptedException e) {
			System.err.println("InterruptedException while waiting for feedback of bash vsim -c -do ...");
			return false;
		}
		return true;
	}

	/**
	 * Run all test sequences of a previously generated Ultrasynth test composition.
	 * @return
	 * 		True if the run was successful, false otherwise.
	 */
	public static boolean runUltrasynth() {
		final String testSequencePath = Processor.Instance.getHardwareDestinationPath() + "/"
										+ Processor.Instance.getGenerator().getTemplateFileName("SetupSequence");
		final String singleTestPath = Processor.Instance.getHardwareDestinationPath() + "/"
										+ Processor.Instance.getGenerator().getTemplateFileName("SingleTestSeq");
		final String triggerCompilePath = Processor.Instance.getHardwareDestinationPath() + "/"
											+ Processor.Instance.getGenerator().getTemplateFileName("CompileTrigger");
		final String triggerPath = Processor.Instance.getHardwareDestinationPath() + "/"
									+ Processor.Instance.getGenerator().getTemplateFileName("SimTrigger");
		final String errorLogPath = Processor.Instance.getDebuggingPath() + "/"
									+ Processor.Instance.getGenerator().getTemplateFileName("SimError");
		final String simLogPath = Processor.Instance.getDebuggingPath() + "/"
									+ Processor.Instance.getGenerator().getTemplateFileName("SimLog");

		// setup the debug directory for the new test run
		File debugDir = new File(Processor.Instance.getDebuggingPath());
		if (!debugDir.exists()) {
			if (!debugDir.mkdir()) {
				System.err.println("Could not create debug folder, aborting test run.");
				return false;
			}
		} else {
			File[] files = debugDir.listFiles();
			if (null == files) {
				System.err.println("Could not get a list of all files in the debug folder, aborting test run.");
				return false;
			}
			for (File file : files) {
				if (!file.delete()) {
					System.err.printf("Could not delete %s, aborting test run.\n", file.getName());
					return false;
				}
			}
		}

		if (!makeExe(triggerPath) || !makeExe(triggerCompilePath))
			return false;
		try {
			int counter = 0;
			String simErrThisRun = errorLogPath + counter;
			String simLogThisRun = simLogPath + counter;
			
			// compilation
			if (!exec(counter, triggerCompilePath, simErrThisRun, simLogThisRun)) 
				return false;

			// simulation
			BufferedReader testSequence = new BufferedReader(new FileReader(testSequencePath));
			
			String line;
			while ( (line = testSequence.readLine()) != null )  {
				++counter;
				simErrThisRun = errorLogPath + counter;
				simLogThisRun = simLogPath + counter;

				File singleTest = new File(singleTestPath);
				if (singleTest.exists())
					singleTest.delete();

				FileWriter testWriter = new FileWriter(singleTest);
				testWriter.write(line);
				testWriter.close();

				if (!exec(counter, triggerPath, simErrThisRun, simLogThisRun)) return false;
				if (!checkErrorLog(simErrThisRun)) return false;
				if (!checkSimLog(simLogThisRun)) return false;
			}
			testSequence.close();
		} catch (IOException e) {
			System.err.printf("IO Exception while preparing the next simulation run. Abort.\n");
			e.printStackTrace();
			return false;
		} 
		return true;				
	}

	private static boolean checkSimLog(final String simLogPath) {
		
		try {
			BufferedReader simLog = new BufferedReader(new FileReader(simLogPath));			

			String line;
			int lineNumber = 1;
			while ( (line = simLog.readLine()) != null ) {
				if(line.contains("Errors: ")) {
					String errorstring = line.substring(line.indexOf(":") + 1, line.indexOf(",")).trim();
					int errors = Integer.parseInt(errorstring);
					if(errors > 0) {
						System.err.printf("Abort: Error in log file \"%s\", line %d\n", simLogPath, lineNumber);
						simLog.close();
						return false;
					}

					String substringwarnings = line.substring(line.lastIndexOf(":") + 2);
					int warnings = Integer.parseInt(substringwarnings);
					if(warnings > 0) {
						System.out.println(warnings + " warnings during Modelsim Simulation");
					}
				}
				++lineNumber;
			}
			simLog.close();
		} catch (IOException e) {
			System.err.printf("IO Exception while reading the simulation log file %s. Abort.\n", simLogPath);
			e.printStackTrace();
			return false;
		} 
		return true;
	}

	private static boolean checkErrorLog(String errLogPath) {
		try {
			BufferedReader errorLog = new BufferedReader(new FileReader(errLogPath));

			String line;
			int lineNumber = 1;
			while ((line = errorLog.readLine()) != null) {
				System.err.printf("Abort: Error in log file \"%s\", line %d\n", errLogPath, lineNumber);
				System.out.printf("Error was: \"%s\"\n", line);
				errorLog.close();
				return false;
			}
			errorLog.close();
		} catch (IOException e) {
			System.err.printf("IO Exception while reading the error log file \"%s\". Abort.\n", errLogPath);
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private static boolean exec(int runNo, String triggerPath, String errLogPath, String simLogPath) {
		try {
			System.out.printf("Starting test run %d using trigger \"%s\" ...\n", runNo, triggerPath);
			ProcessBuilder pb = new ProcessBuilder("sh","-c","./" + triggerPath);
			pb.redirectError(new File(errLogPath));
			pb.redirectOutput(new File(simLogPath));
			//pb.inheritIO();
			
			process = pb.start();
			System.out.println("Waiting for Modelsim ...");
			process.waitFor();
			System.out.println("Modelsim done!");

		} catch (InterruptedException e) {
			System.err.println("InterruptedException while waiting for feedback of bash vsim -c -do ...");
			return false;
		} catch (IOException e) {
			System.err.println("IO Exception while waiting for feedback of bash vsim -c -do. Abort.");
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private static boolean makeExe(String triggerPath) {
		try {
			Process chmod = Runtime.getRuntime().exec("chmod +x+r " + triggerPath + "\n");
			BufferedReader chmoderror = new BufferedReader(new InputStreamReader(chmod.getErrorStream()));
			String line;
			while ((line = chmoderror.readLine()) != null) {
				System.err.println("(e - chmod) " + line);
				return false;
			}
		} catch (IOException e) {
			System.err.printf("IO Exception while making \"%s\" executable. Abort.\n", triggerPath);
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
