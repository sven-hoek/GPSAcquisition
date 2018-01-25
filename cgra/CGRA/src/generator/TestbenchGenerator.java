package generator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import target.Processor;

public abstract class TestbenchGenerator {

	
	VerilogGenerator generator;
	
	TestbenchGenerator(VerilogGenerator generator){
		this.generator = generator;
	}

	public String getModelSimPath() {
		String modelsimFolder = "";
		if(new File(Processor.Instance.getConfigurationPath()+"/vsimDir").exists()){
			System.out.println("Modelsim path file detected, reading ...");
			
			try {
				BufferedReader reader = new BufferedReader(new FileReader(Processor.Instance.getConfigurationPath() + "/vsimDir"));
				modelsimFolder = reader.readLine();
				reader.close();
				System.out.println("Modelsim folder location: " + modelsimFolder);
			} catch(IOException e) {
				System.err.println("Could not read path file!");
				e.printStackTrace();
			}
		}
		else {
			System.out.println("Did not find Modelsim path file, searching ...");
			File dir = new File("/opt/tools/modelsim");

			if (!dir.exists())
				dir = new File("/opt/remote/rsstudent/tools/modelsim/default");
			else {
				File[] files = dir.listFiles();
				for(File folder : files) {
					if(folder.toString().contains(".")) {
						dir = folder.getAbsoluteFile();
						break;
					}
				}
			}
			
			modelsimFolder = dir.toString() + "/modeltech/bin";
			System.out.println("Found Modelsim path: " + modelsimFolder);
			System.out.println("Writing path to \"vsimDir\" ...");
			try {
				FileWriter fw = new FileWriter(new File (Processor.Instance.getConfigurationPath() + "/vsimDir"));
				fw.write(modelsimFolder);
				fw.close();
			} catch(IOException e) {
				System.err.println("Could not write path file!");
				e.printStackTrace();
			}
		}
		return modelsimFolder;
	}
}
