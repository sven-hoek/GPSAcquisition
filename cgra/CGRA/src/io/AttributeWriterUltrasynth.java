package io;

import java.io.FileWriter;
import java.io.IOException;
import org.json.simple.JSONObject;

import cgramodel.AugmentedPE;
import cgramodel.CgraModel;
import cgramodel.CgraModelAmidar;
import cgramodel.CgraModelUltrasynth;

/**
 * This class can be used to export a CgraModel for the {@link target.UltraSynth} processor as a JSON description.
 * The {@link AttributeWriter} is used as a stub to export all generic information of the CGRA.
 * This class extends the {@link AttributeWriter} and adds host specific attributes of the {@link CgraModelUltraSynth}.
 * @author Dennis Wolf
 */
public class AttributeWriterUltrasynth extends AttributeWriter{

	/**
	 * Export of a all {@link target.UltraSynth} specific attributes of the {@link CgraModelUltraSynth}.
	 */
	@Override
	public void writeToplevel(CgraModel model, String path2File, String name) {

		JSONObject obj = new JSONObject();
		obj.put("name", model.getName());
		obj.put("composition", name+"composition.json");
		
		obj.put("runCounterWidth", ((CgraModelUltrasynth) model).getSyncUnit().getRunCounterWidth());
		obj.put("cycleCounterWidth", ((CgraModelUltrasynth) model).getSyncUnit().getCycleCounterWidth());
		obj.put("parameterBufferSize", ((CgraModelUltrasynth) model).getParameterBuffer().getSize());
		obj.put("parameterBufferExpectedParameterCount", ((CgraModelUltrasynth) model).getParameterBuffer().getMaxExpectedParamerters());
		obj.put("IDTableSize", ((CgraModelUltrasynth) model).getComUnit().getIDCSize());
		obj.put("actorCount", ((CgraModelUltrasynth) model).getActorInterface().getActorCount());
		obj.put("sensorCount", ((CgraModelUltrasynth) model).getSensorInterface().getSensorCount());
		obj.put("ocmDataWidth", ((CgraModelUltrasynth) model).getOcmInterface().getBufferWidth());
		obj.put("ocmBufferSize", ((CgraModelUltrasynth) model).getOcmInterface().getBufferSize());
		obj.put("ocmOutputContextSize", ((CgraModelUltrasynth) model).getOcmInterface().getOutputContextSize());
		obj.put("globalLogContextSize", ((CgraModelUltrasynth) model).getLogInterface().getGlobalContextSize());
		obj.put("cycleCounterWidth", ((CgraModelUltrasynth) model).getSyncUnit().getCycleCounterWidth());
		obj.put("cycleCounterWidth", ((CgraModelUltrasynth) model).getSyncUnit().getCycleCounterWidth());
		JSONObject logsizes = new JSONObject();
		
		for(AugmentedPE augpe : ((CgraModelUltrasynth) model).getPeComponents()){
			logsizes.put("logSize"+augpe.id, augpe.getLogSize());
		}
		obj.put("logs", logsizes);
		String finalfile = path2File+name;
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
