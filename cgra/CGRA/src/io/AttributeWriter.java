package io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import cgramodel.CBoxModel.BranchSelectionConnection;
import cgramodel.CgraModel;
import cgramodel.PEModel;
import operator.Operator;

public abstract class AttributeWriter {

	ArrayList<String> files;

	public AttributeWriter(){

	}

	public void writeJSON(CgraModel model, String name, String folder){
		
		files = new ArrayList<String>();
		model.finalizeCgra();
		folder.trim();
		if(name.endsWith(".json")) {
			name = name.substring(0,name.lastIndexOf("."));
		}
		if(!folder.endsWith("/")) {
			folder += "/";
		}
		File dir = new File (folder);
		if(!dir.exists()){
			dir.mkdirs();
		}
		writeJSONComposition(model,folder,name);
		writeToplevel(model,folder,name);
		formatFiles(files);
	}

	public void formatFiles(ArrayList<String> files) {
		ArrayList<String> filestoiterate = new ArrayList<String>(files);
		for(String filename : filestoiterate){
			try {
				File file = new File(filename);
				BufferedReader br = new BufferedReader(new FileReader(file));
				BufferedWriter bw = new BufferedWriter(new FileWriter(new File(filename+".json")));
				int c;
				int bracketcount = 0;
				while((c = br.read()) != -1) {
		            String character = String.valueOf((char) c);
		            if(character.equals("[")){
		            	bracketcount++;
		            }
		            if(character.equals("]")){
		            	bracketcount--;
		            }
		            if((character.equals("{") || character.equals("}") || character.equals(",")) && bracketcount == 0 ){
		            	character +="\n";
		            }
		            bw.write(character);
		        }
				br.close();
				bw.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		for(String filename : filestoiterate){
			File file = new File(filename);
			file.delete();
		}
	}

	public abstract void writeToplevel(CgraModel model,String folder, String name);

	public void writeJSONComposition(CgraModel model,String folder, String filename){
		JSONObject obj = new JSONObject();
		String name = model.getName();	
		if(name.isEmpty()){
			name = "CGRA_" + model.getPEs().size() + "_" + model.getContextMemorySize() + "c_" + model.getcBoxModel().getMemorySlots()+"s" + "_comp";
		}		
		obj.put("Pipelined", model.isPipelined());
		JSONObject objpes = new JSONObject();
		for(PEModel pe : model.getPEs()){
			String dma;
			if(pe.getMemAccess()){
				dma = "_dma";
			}
			else{
				dma = "_no_dma";
			}
			String cf;
			if(pe.getControlFlow()){
				cf = "_cf";
			}
			else{
				cf = "_no_cf";
			}
			String namepe = "PE_" + pe.getID() +"_" + pe.getAvailableNonNativeOperators().size() + "_ops" + dma + cf;
			objpes.put(pe.getID(), namepe+".json");
			writeJSONPE(pe,folder,namepe); 
		}
		obj.put("PEs", objpes);
		obj.put("Context_memory_size", model.getContextMemorySize());
		String interconnectname = "Interconnect_" + name;
		obj.put("Interconnect", interconnectname + ".json");
		obj.put("CBox_slots", model.getcBoxModel().getMemorySlots());
		obj.put("CBox_evaluation_blocks", model.getcBoxModel().getNrOfEvaluationBlocks());
		obj.put("CBOX_output_ports_per_evaluation_blocks", model.getcBoxModel().getCBoxPredicationOutputsPerBox());
		obj.put("SecondRFOutput2ALU", model.isSecondRFOutput2ALU());
		if(model.getcBoxModel().getBranchSelectionSources().isEmpty()){
			obj.put("Branch_selection_mode", "None");
		}
		else if(model.getcBoxModel().getBranchSelectionSources().size() == 1 
				&& model.getcBoxModel().getBranchSelectionSources().contains(BranchSelectionConnection.regInNegative) ){
			obj.put("Branch_selection_mode", "Default");
		}
		else{
			obj.put("Branch_selection_mode", "Custom");
			JSONArray branchselsignals = new JSONArray();
			for(BranchSelectionConnection bsc : model.getcBoxModel().getBranchSelectionSources()){
				branchselsignals.add(bsc.toString());
			}
			obj.put("Branch_selection_sources",branchselsignals);
		}
		String finalfile = folder+"/"+filename+"composition";
		try (FileWriter file = new FileWriter(finalfile)) {
			file.write(obj.toJSONString());
			file.flush();
			file.close();
			files.add(finalfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// ------- interconnect
		
		obj = new JSONObject();
		
		obj.put("name", interconnectname);
		JSONObject interconnect = new JSONObject();
		JSONObject indivliveout = new JSONObject();
		for(PEModel pe: model.getPEs()){
			JSONArray inputs = new JSONArray();
			for(PEModel inputpe : pe.getInputs()){
				inputs.add(inputpe.getID());
			}
			interconnect.put(pe.getID(), inputs);
			indivliveout.put(pe.getID(), pe.getLiveout());
		}
		obj.put("live_out", indivliveout);
		obj.put("Interconnection", interconnect);
		
		String interconnectfinalfile = folder+"/"+interconnectname;
		try (FileWriter file = new FileWriter(interconnectfinalfile)) {
			file.write(obj.toJSONString());
			file.flush();
			file.close();
			files.add(interconnectfinalfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	public void writeJSONPE(PEModel pe, String folder, String name){
		JSONObject obj = new JSONObject();
		obj.put("name", name);
		for(Operator op : pe.getAvailableNonNativeOperators().keySet()){
			JSONObject objop = new JSONObject();
			objop.put("energy", pe.getAvailableNonNativeOperators().get(op).getEnergyconsumption());
			objop.put("duration", pe.getAvailableNonNativeOperators().get(op).getLatency());
			obj.put(op.toString(), objop);
		}
		obj.put("Regfile_size", pe.getRegfilesize());
		String finalfile = folder + "/" + name;
		try (FileWriter file = new FileWriter(finalfile)) {
			file.write(obj.toJSONString());
			file.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		files.add(finalfile);
	}
}
