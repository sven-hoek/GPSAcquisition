package io;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import cgramodel.CgraModel;
import cgramodel.CgraModelAmidar;

public class AttributeParserAmidar extends AttributeParser{
	
	
	public AttributeParserAmidar(){
		super();
	}

	public CgraModel loadCgra(String configFile){
		return loadCgra(configFile, new CgraModelAmidar());
	}

	@Override
	public CgraModel loadHostSpecificAttributes(String configFile, CgraModel cgra) {
		
		JSONParser parser = new JSONParser();
		JSONObject config = null;
		FileReader fileReader;
		try {
			fileReader = new FileReader(configFile);
			config = (JSONObject) parser.parse(fileReader);
		} catch (FileNotFoundException e) {
			System.err.println("File not found - AttributeParserAmidar: \"" + configFile + "\"");
			e.printStackTrace(System.err);
		} catch (IOException e) {
			System.err.println("IO error while reading Amidar description - AttributeParser");
			e.printStackTrace(System.err);
		} catch (ParseException e) {
			System.err.println("Parser error while parsing Amidar description - AttributeParser ");
			e.printStackTrace(System.err);
		}
		cgra.setName((String)config.get("name"));
		String cacheconfig = (String)config.get("CacheConfig");
		((CgraModelAmidar) cgra).setCacheConfiguration(cacheconfig);
		cacheconfig = (String)config.get("htCacheConfig");
		((CgraModelAmidar) cgra).setHTCacheConfiguration((String)config.get(cacheconfig));
		((CgraModelAmidar) cgra).setEnergyStatic((double)config.get("staticEnergy"));
		JSONObject johowo = (JSONObject) config.get("RUN");
		((CgraModelAmidar) cgra).setEnergyRun((double)johowo.get("energy"));
		((CgraModelAmidar) cgra).setDurationRun((int)(long)johowo.get("duration"));
		
		johowo = (JSONObject) config.get("SENDLOCALVAR");
		((CgraModelAmidar) cgra).setEnergySendLocalVar((double)johowo.get("energy"));
		((CgraModelAmidar) cgra).setDurationSendLocalVar((int)(long)johowo.get("duration"));
		
		johowo = (JSONObject) config.get("WRITECONTEXT");
		((CgraModelAmidar) cgra).setEnergyWriteContext((double)johowo.get("energy"));
		((CgraModelAmidar) cgra).setDurationWriteContext((int)(long)johowo.get("duration"));
		
		johowo = (JSONObject) config.get("RECEIVELOCALVAR");
		((CgraModelAmidar) cgra).setEnergyReceiveLocalVar((double)johowo.get("energy"));
		((CgraModelAmidar) cgra).setDurationReceiveLocalVar((int)(long)johowo.get("duration"));
		return cgra;
	}
	
	
}
