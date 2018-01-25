package io;

import java.io.FileWriter;
import java.io.IOException;

import org.json.simple.JSONObject;

import cgramodel.CgraModel;
import cgramodel.CgraModelAmidar;

/**
 * This class can be used to export a CgraModel for the {@link target.Amidar} processor as a JSON description.
 * The {@link AttributeWriter} is used as a stub to export all generic information of the CGRA.
 * This class extends the {@link AttributeWriter} and adds host specific attributes of the {@link CgraModelAmidar}.
 * @author Dennis Wolf
 */
public class AttributeWriterAmidar extends AttributeWriter{

	/**
	 * Constructor
	 */
	public AttributeWriterAmidar(){
		
	}

	/**
	 * Export of a all {@link target.Amidar} specific attributes of the {@link CgraModelAmidar}.
	 */
	
	@Override
	public void writeToplevel(CgraModel model, String path2file, String name) {
		
		JSONObject obj = new JSONObject();
		obj.put("name", model.getName());
		obj.put("CacheConfig", ((CgraModelAmidar) model).getCacheConfiguration());
		obj.put("htCacheConfig", ((CgraModelAmidar) model).getHTCacheConfiguration());
		obj.put("staticEnergy",((CgraModelAmidar) model).getEnergyStatic());
		
		JSONObject objduren = new JSONObject();
		objduren.put("duration", ((CgraModelAmidar) model).getDurationSendLocalVar());
		objduren.put("energy", ((CgraModelAmidar) model).getEnergySendLocalVar());
		obj.put("SENDLOCALVAR",objduren);
		objduren = new JSONObject();
		objduren.put("duration", ((CgraModelAmidar) model).getDurationRun());
		objduren.put("energy", ((CgraModelAmidar) model).getEnergyRun());		
		obj.put("RUN",objduren);
		objduren = new JSONObject();
		objduren.put("duration", ((CgraModelAmidar) model).getDurationWriteContext());
		objduren.put("energy", ((CgraModelAmidar) model).getEnergyWriteContext());
		obj.put("WRITECONTEXT",objduren);
		objduren = new JSONObject();
		objduren.put("duration", ((CgraModelAmidar) model).getDurationReceiveLocalVar());
		objduren.put("energy", ((CgraModelAmidar) model).getEnergyReceiveLocalVar());
		obj.put("RECEIVELOCALVAR",objduren);
		
		obj.put("composition", name+"composition.json");
		String finalfile = path2file+name;
		try (FileWriter file = new FileWriter(finalfile)) {
			file.write(obj.toJSONString());
			file.flush();
			file.close();
			files.add(finalfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}