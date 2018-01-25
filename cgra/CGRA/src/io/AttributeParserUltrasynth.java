package io;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import cgramodel.AugmentedPE;
import cgramodel.CgraModel;
import cgramodel.CgraModelUltrasynth;

public class AttributeParserUltrasynth extends AttributeParser{

	public AttributeParserUltrasynth(){
		super();
	}

	public CgraModel loadCgra(String configFile){
		return loadCgra(configFile, new CgraModelUltrasynth());
	}
	
	/**
	 * Loads an Ultrasynth model from a JSON file.
	 * Do NOT call this method directly. ALWAYS call it through loadCGRA.
	 * It is public only because the abstract method is defined in this way.
	 *
	 * @param configFile
	 * 			The JSON file's path relative to the current PWD
	 * @return
	 * 			The model or null if there was an error
	 */
	public CgraModel loadHostSpecificAttributes(String configFile, CgraModel dataPath) {
		JSONParser parser = new JSONParser();
		JSONObject config = null;
		FileReader fileReader;

		try {
			fileReader = new FileReader(configFile);
			config = (JSONObject) parser.parse(fileReader);
		} catch (FileNotFoundException e) {
			System.err.println("File not found - AttributeParser: \"" + configFile + "\"");
			e.printStackTrace(System.err);
		} catch (IOException e) {
			System.err.println("IO error while reading Ultrasynth description - AttributeParser");
			e.printStackTrace(System.err);
		} catch (ParseException e) {
			System.err.println("Parser error while parsing Ultrasynth description - AttributeParser ");
			e.printStackTrace(System.err);
		}

		// Convenience cast, works every time as we are only calling
		// this method via the loadCgra method of this derived class.
		CgraModelUltrasynth model = (CgraModelUltrasynth) dataPath;

		// Getting the name from the config file and setting it for the loaded CGRA
		// should be part of the core CGRA load.
		String name = (String) config.get("name");

		if (name == null) {
			name = model.getClass().getName();
			System.err
					.println("WARNING: No name defined in \"" + configFile + "\". Name is set to \"" + name + "\"");
			System.err.println("         Simulator will run nevertheless.");
		}

		model.setName(name);

		// Start the Ultrasynth specific attributes by finalizing the base class
		// and instantiating all the required fields in the derived class.
		model.init();
		
		long configNumber;
		configNumber = (long) config.get("runCounterWidth");
		model.getSyncUnit().setRunCounterWidth((int) configNumber);

		configNumber = (long) config.get("cycleCounterWidth");
		model.getSyncUnit().setCycleCounterWidth((int) configNumber);

		configNumber = (long) config.get("parameterBufferSize");
		model.getParameterBuffer().setSize((int) configNumber);

		configNumber = (long) config.get("parameterBufferExpectedParameterCount");
		model.getParameterBuffer().setMaxExpectedParamerters((int) configNumber);

		configNumber = (long) config.get("IDTableSize");
		model.getComUnit().setIDCSize((int) configNumber);

		configNumber = (long) config.get("actorCount");
		model.getActorInterface().setActorCount((int) configNumber);

		configNumber = (long) config.get("sensorCount");
		model.getSensorInterface().setSensorCount((int) configNumber);

		configNumber = (long) config.get("ocmDataWidth");
		model.getOcmInterface().setBufferWidth((int) configNumber);

		configNumber = (long) config.get("ocmBufferSize");
		model.getOcmInterface().setBufferSize((int) configNumber);

		configNumber = (long) config.get("ocmOutputContextSize");
		model.getOcmInterface().setOutputContextSize((int) configNumber);
		
		configNumber = (long) config.get("globalLogContextSize");
		model.getLogInterface().setGlobalContextSize((int) configNumber);

		JSONObject logSizes = (JSONObject) config.get("logs");
		if (logSizes.size() != model.getNrOfPEs()) {
			System.err.println("Number of entries in Ultrasynth config \"logs\" was not equal to the PE count.");
			System.err.printf("Found %d entries but expected %d\n", logSizes.size(), model.getNrOfPEs());
			return null;
		}

		for (AugmentedPE augmentedPE : model.getPeComponents()) {
			configNumber = (long) logSizes.get("logSize" + augmentedPE.id);
			augmentedPE.setLogSize((int) configNumber);
		}

		return model;
	}
	
}
