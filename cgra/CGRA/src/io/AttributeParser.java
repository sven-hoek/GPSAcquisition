package io;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import cgramodel.CBoxModel;
import cgramodel.CBoxModel.BranchSelectionConnection;
import cgramodel.CgraModel;
import cgramodel.PEModel;

/**
 * This class can be used to load a Cgra composition from an attribute base JSON
 * description.
 *
 * @author wolf
 *
 */
public abstract class AttributeParser {

	/**
	 * Constructor
	 */
	public AttributeParser() {
	}
	
	public abstract CgraModel loadCgra(String configFile);

	protected CgraModel loadCgra(String configFile, CgraModel cgra) {
		JSONParser parser = new JSONParser();
		JSONObject config = null;
		FileReader fileReader;
		configFile.trim();
		if(!configFile.endsWith(".json")){
			configFile+=".json";
		}
		String file = configFile;
		String folder = configFile;
		int dash = configFile.lastIndexOf("/");
		folder = file.substring(0, dash+1);
		file = file.substring(dash+1);
		try {
			String toload = folder+file;
			fileReader = new FileReader(toload);
			config = (JSONObject) parser.parse(fileReader);
		} catch (FileNotFoundException e) {
			System.err.println("File not found - AttributeParser: \"" + folder+file + "\"");
			e.printStackTrace(System.err);
		} catch (IOException e) {
			System.err.println("IO error while reading description - AttributeParser");
			e.printStackTrace(System.err);
		} catch (ParseException e) {
			System.err.println("Parser error while reading description - AttributeParser ");
			e.printStackTrace(System.err);
		}
		String cgraConfigFileName = (String)config.get("composition");
		if (cgraConfigFileName == null) {
			System.err.println("Could not read the CGRA composition from the config file, aborting");
			return null;
		}
		cgra = loadCgraDataPath(folder+cgraConfigFileName, cgra);
		loadHostSpecificAttributes(configFile, cgra);
		cgra.finalizeCgra();
		return cgra;
		
	}
	
	protected abstract CgraModel loadHostSpecificAttributes(String configFile, CgraModel cgra);

	/**
	 * Method to load a composition from a Json description. This is basically the datapath <b> Note that the
	 * description should follow the rules, as shown by an example for the JSON
	 * template in the folder "examples".
	 * 
	 * @param Folder
	 *            where the composition can be found
	 * @param Name
	 *            of the composition
	 * @return a valid composition - null otherwise
	 */
	private CgraModel loadCgraDataPath(String configFile, CgraModel cgra) {

		JSONParser parser;
		JSONObject json = null;
		parser = new JSONParser();
		FileReader fileReader;
		
		configFile.trim();
		String file = configFile;
		String folder = configFile;
		int dash = configFile.lastIndexOf("/");
		folder = file.substring(0, dash+1);
		file = file.substring(dash+1);
		try {
			fileReader = new FileReader(folder + "/" + file);
			json = (JSONObject) parser.parse(fileReader);
			int nrboxes = (int) (long) json.get("CBox_evaluation_blocks");
			cgra.getcBoxModel().setNrOfEvaluationBlocks(nrboxes);
			cgra.getcBoxModel().setCBoxOutputsPerBox((int) (long) json.get("CBOX_output_ports_per_evaluation_blocks"));
			
			// Actual Hardware components
			int registerfileSizeMax = 0;
			
			cgra.setPipelined((boolean) json.get("Pipelined"));
			cgra.setSecondRFOutput2ALU((boolean) json.get("SecondRFOutput2ALU"));
			CBoxModel cbox = cgra.getcBoxModel();
			String branchSelectionMode = (String) json.get("Branch_selection_mode");
			switch(branchSelectionMode.trim().toLowerCase()){
			case "default" : 
			cbox.addbranchSelectionSource(BranchSelectionConnection.regInNegative); 
			break;
			
			case "custom" :  
			JSONArray cboxelements = (JSONArray)json.get("Branch_selection_sources");
			for(Object element : cboxelements){
				String key = (String) element;
				cbox.addbranchSelectionSourceByName((String)element);
			}
			break;
			
			case "none" :  
				break;
			
			default : 
				System.err.println("Branch selection mode is illegal in " + folder + "/" + file + "\n Options: none / default / custom");	System.exit(0); 
			break;	
			}
			
			JSONObject elements = (JSONObject) json.get("PEs");
			for (int i = 0; i < elements.size(); i++) {
				PEModel pe = new PEModel();
				if(registerfileSizeMax < pe.configure(folder + (String)elements.get(Integer.toString(i)), i)){
					registerfileSizeMax = pe.getRegfilesize();
				}
				pe.setID(i);
				cgra.addPE(pe);
			}
			cgra.getcBoxModel().setMemorySlots((int) (long) json.get("CBox_slots"));
			cgra.setContextMemorySize((int)(long)json.get("Context_memory_size"));
			
			setConnectionsfinal(folder + (String)json.get("Interconnect"), cgra);	 // TODO - change !!!!

			int max = 0;
			for (PEModel pe : cgra.getPEs()) {
				if (max < pe.getInputs().size()) {
					max = pe.getInputs().size();
				}
			}
			// ContextPE.createMask(max,cgra.getOps().getEnumConstants().length,
			// registerfileSizeMax);
			return cgra;

		} catch (FileNotFoundException e) {
			System.err.println("No config file found- AttributeParser : \"" + configFile + "\"");
			e.printStackTrace(System.err);
			return null;
		} catch (IOException e) {
			System.err.println("Error while reading config file - AttributeParser");
			e.printStackTrace(System.err);
			return null;
		} catch (ParseException e) {
			System.err.println("Error while reading config file - AttributeParser ");
			e.printStackTrace(System.err);
			return null;
		}

	}

	private static void setConnectionsfinal(String configFile, CgraModel cgra) {

		JSONParser parser;
		JSONObject json = null;
		parser = new JSONParser();
		FileReader fileReader;
		try {
			fileReader = new FileReader(configFile);
			json = (JSONObject) parser.parse(fileReader);
		} catch (FileNotFoundException e) {
			System.err.println("No config file found- AttributeParser : \"" + configFile + "\"");
			e.printStackTrace(System.err);
		} catch (IOException e) {
			System.err.println("Error while reading config file - AttributeParser");
			e.printStackTrace(System.err);
		} catch (ParseException e) {
			System.err.println("Error while reading config file - AttributeParser ");
			e.printStackTrace(System.err);
		}

		JSONObject object = (JSONObject) json.get("Interconnection");
		JSONObject object2 = (JSONObject) json.get("live_out");
		JSONObject object3 = (JSONObject) json.get("live_in");
// TODO - change to id not iterate til nrofpes !!
		
		
		for (int i = 0; i < cgra.getNrOfPEs(); i++) {
			for (Long k : (List<Long>) object.get(Integer.toString(i))) {
				cgra.getPEs().get(i).addPE2inputs(cgra.getPEs().get(k.intValue()));
			}
			cgra.getPEs().get(i).setLiveout((boolean) object2.get(Integer.toString(i)));
			
			if(object3 != null){ // this is optional, as normally all PEs have live ins
				Boolean li = (Boolean)object3.get(Integer.toString(i));
				if(li != null){
					cgra.getPEs().get(i).setLivein(li);
				}
			}
		}
	}

}
