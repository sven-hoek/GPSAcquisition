package amidar;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import functionalunit.cache.PrefetchStrategy;
import scheduler.RCListSched.AliasingSpeculation;
import tracer.CheckWriter;
import tracer.Trace;
import tracer.TraceManager;


/**
 * Gives access to all configuration options of the AMIDAR Simulator.
 * 
 * @author Michael Raitza
 * @version - 1.3.2011
 */
public class ConfMan implements Serializable {


	
	String applicationPath;
	boolean synthesis;
	
	private boolean checkOutput = false;
	private CheckWriter checkWriter = null;
	
	
	private LinkedHashMap<String, String> fuConfigFiles;
	
	private LinkedHashMap<String, Object> synthesisConfig;
	
	private LinkedHashMap<String, Boolean> traceActivation;
	
	private static final int FORMAT_LENGTH = 25;
	
	/**
	 * Constructs a map of configuration item names and their respective item
	 * objects loaded with default values.
	 */
	public ConfMan(String configFile, String applicationPath, boolean synthesis) {
		
		this.applicationPath = applicationPath;
		this.synthesis = synthesis;
		
		synthesisConfig = new LinkedHashMap<String, Object>();
		fuConfigFiles = new LinkedHashMap<String, String>();
		traceActivation = new LinkedHashMap<String,Boolean>();
		
		
		JSONParser parser = new JSONParser();
		JSONObject amidarConfig = null;
		JSONObject traceConfig = null;
		JSONObject synthesisConfigFile = null;
		
		try {
			amidarConfig = (JSONObject) parser.parse(new FileReader(configFile));
			traceConfig = (JSONObject) parser.parse(new FileReader((String) amidarConfig.get("trace_activation")));
			synthesisConfigFile = (JSONObject) parser.parse(new FileReader((String) amidarConfig.get("synthesis")));
		} catch (FileNotFoundException e) {
			System.err.println("No config file found");
			e.printStackTrace(System.err);
		} catch (IOException e) {
			System.err.println("Error while reading config file" );
			e.printStackTrace(System.err);
		} catch (ParseException e) {
			System.err.println("Error while parsing config file");
			e.printStackTrace(System.err);
		}		
		
		updateTraceActivation(traceConfig);
		updateSynthesisConfig(synthesisConfigFile);
		updateFUs(amidarConfig);
	}
	
	public ConfMan(){
		
	}

	/**
	 * Updates all Synthesis parameters according to the given JSON-configfile
	 * All parameters have to be declared
	 * @param synthesisConfig the config file
	 */
	public void updateSynthesisConfig(JSONObject synthesisConfigFile) {
		synthesisConfig.put("UNROLL", ((Long)synthesisConfigFile.get("UNROLL")));
		synthesisConfig.put("SYNTHLOG", (Boolean)synthesisConfigFile.get("SYNTHLOG"));
		synthesisConfig.put("MAX_UNROLL_LENGTH", ((Long)synthesisConfigFile.get("MAX_UNROLL_LENGTH")).intValue());
		synthesisConfig.put("CSE", (Boolean)synthesisConfigFile.get("CSE"));
		synthesisConfig.put("CONSTANT_FOLDING", (Boolean)synthesisConfigFile.get("CONSTANT_FOLDING"));
		synthesisConfig.put("INLINE", (Boolean)synthesisConfigFile.get("INLINE"));
		synthesisConfig.put("COHERENCE_PROTOCOL", (String)synthesisConfigFile.get("COHERENCE_PROTOCOL"));
		
		
		PrefetchStrategy prefetch = PrefetchStrategy.NONE;
		String prefetchString = (String)synthesisConfigFile.get("PREFETCHING");
		prefetch = convertPrefetchStrategy(prefetchString);
		synthesisConfig.put("PREFETCHING", prefetch);
		
		
		AliasingSpeculation aliasing = AliasingSpeculation.OFF;
		String aliasingSpeculation = (String)synthesisConfigFile.get("ALIASING_SPECULATION");
		aliasing = convertAliasingSpeculation(aliasingSpeculation);
		synthesisConfig.put("ALIASING_SPECULATION", aliasing);
		

		JSONArray blacklist = (JSONArray)synthesisConfigFile.get("BLACKLIST");
		LinkedHashMap<String,LinkedHashSet<Integer>> methodBlacklist = new LinkedHashMap<>();
		for(Object methodO: blacklist){
			
			
			String method = (String)((JSONObject)methodO).keySet().iterator().next();
			Integer line = ((Long)((JSONObject)methodO).get(method)).intValue();
			LinkedHashSet<Integer> lines = methodBlacklist.get(method);
			if(lines == null){
				lines = new LinkedHashSet<>();
				methodBlacklist.put(method, lines);
			}
			lines.add(line);
		}
		
		synthesisConfig.put("BLACKLIST", methodBlacklist);
	}
	
	public void updateSynthesisConfig(String key, Object value){
		synthesisConfig.put(key, value);
	}

	/**
	 * Updates which trace unit is activated according to the given JSON config file
	 * @param traceConfig the config file
	 */
	public void updateTraceActivation(JSONObject traceConfig) {
		Collection<?> io_keys = traceConfig.keySet();
		Iterator<?> io_iterator = io_keys.iterator();
		while (io_iterator.hasNext()) {
			String io_key = (String) io_iterator.next();
			//activates an IOFacility
			//if the wanted IOFacility does not already exist, it gets created
			traceActivation.put(io_key,(Boolean) traceConfig.get(io_key));
		}
	}
	
	public void updateFUs(JSONObject amidarConfig){
		
		fuConfigFiles.put("HEAP", (String)amidarConfig.get("HEAP"));
		fuConfigFiles.put("CGRA", (String)amidarConfig.get("CGRA"));
		fuConfigFiles.put("IALU", (String)amidarConfig.get("IALU"));
		fuConfigFiles.put("FALU", (String)amidarConfig.get("FALU"));
		fuConfigFiles.put("TOKENMACHINE", (String)amidarConfig.get("TOKENMACHINE"));
		fuConfigFiles.put("FRAMESTACK",(String)amidarConfig.get("FRAMESTACK"));
	}
	
	public void printConfig(Trace configTrace){
		if(!configTrace.active())
			return;
		StringBuffer config;
		configTrace.printTableHeader("Current Configuraton");
		configTrace.println("Amidar Config:");
		for(String fu: fuConfigFiles.keySet()){
			config =  new StringBuffer("    "+fu);
			for(int i = 0; i < FORMAT_LENGTH - fu.length(); i++){
				config.append(" ");
			}
			config.append(fuConfigFiles.get(fu));
			configTrace.println(config);
		}
		configTrace.println();
		configTrace.println("Trace Activation:");
		for(String trace: traceActivation.keySet()){
			config =  new StringBuffer("    "+trace);
			for(int i = 0; i < FORMAT_LENGTH - trace.length(); i++){
				config.append(" ");
			}
			config.append(traceActivation.get(trace));
			configTrace.println(config);
		}
		configTrace.println();
		configTrace.println("Synthesis Configuration:");
		for(String param : synthesisConfig.keySet()){
			config =  new StringBuffer("    "+param);
			for(int i = 0; i < FORMAT_LENGTH - param.length(); i++){
				config.append(" ");
			}
			config.append(synthesisConfig.get(param));
			configTrace.println(config);
		}
		configTrace.println();
		configTrace.println("Application:                 "+applicationPath);
		configTrace.println("Synthesis:                   "+synthesis);
		configTrace.println();
	}
	
	public void configureTraceManager(TraceManager traceManager){
		
		if(checkOutput){
			traceManager.newf("system", new Trace(checkWriter, null , "" , ""));
		}
		

		for(String io_key: traceActivation.keySet()){
			boolean activated = traceActivation.get(io_key);



			if(activated){
				if(traceManager.getf(io_key) != null){
					traceManager.getf(io_key).activate();
				}
				else{
					traceManager.newf(io_key, "stdout", "stdint");
					traceManager.getf(io_key).activate();
				}
				traceManager.getf(io_key).setPrefix(io_key);
			}
			else { 
				if(traceManager.getf(io_key) != null){
					traceManager.getf(io_key).deactivate();
				}else{
					traceManager.newf(io_key, "stdout", "stdint");
					traceManager.getf(io_key).deactivate();
				}
			}
		}
		
//		configTrace = traceManager.getf("config");
//		if(configTrace.active()){
//			printConfig();
//		}
		
		
	}
	
	
	public LinkedHashMap<String,String> getFuConfigFiles(){
		return fuConfigFiles;
	}

	public LinkedHashMap<String, Object> getSynthesisConfig() {
		return synthesisConfig;
	}

	public String getApplicationPath() {
		return applicationPath;
	}
	
	public void setApplication(String application){
		applicationPath = application;
	}

	public boolean getSynthesis() {
		return synthesis;
	}
	
	public void setSynthesis(boolean synthesis){
		this.synthesis = synthesis;
	}
	
	public void setTraceActivation(String traceName, boolean active){
		traceActivation.put(traceName, active);
	}
	
	public boolean getTraceActivation(String traceName){
		return traceActivation.get(traceName);
	}

	public ConfMan getDeepCopy() {
		ConfMan result = new ConfMan();
		result.applicationPath = this.applicationPath;
		result.synthesis = this.synthesis;
		
		result.fuConfigFiles = new LinkedHashMap<>();
		result.fuConfigFiles.putAll(this.fuConfigFiles);
		
		result.synthesisConfig = new LinkedHashMap<>();
		result.synthesisConfig.putAll(this.synthesisConfig);
		
		result.traceActivation = new LinkedHashMap<>();
		result.traceActivation.putAll(this.traceActivation);
		
		return result;
	}
	
	public CheckWriter activateOutputCheck(){
		checkOutput = true;
		checkWriter = new CheckWriter();
		return checkWriter;
	}
	
	int benchmarkScale = 6;

	public int getBenchmarkScale() {
		return benchmarkScale;
	}
	
	public void setBenchmarkScale(int benchmarkScale){
		this.benchmarkScale = benchmarkScale;
	}
	
	public static PrefetchStrategy convertPrefetchStrategy(String strategy){
		PrefetchStrategy prefetch;
		
		switch(strategy){
		case "NONE":
			prefetch = PrefetchStrategy.NONE;
			break;
		case "LINEAR":
			prefetch = PrefetchStrategy.LINEAR;
			break;
		case "UNROLL":
			prefetch = PrefetchStrategy.UNROLL;
			break;
		default:
			throw new RuntimeException("Unkown prefetching strategy: "+ strategy);
		}
		return prefetch;
	}
	
	public static AliasingSpeculation convertAliasingSpeculation(String speculation){
		AliasingSpeculation aliasing;
		switch(speculation){
		case "OFF":
			aliasing = AliasingSpeculation.OFF;
			break;
		case "EXACT_CHECK":
			aliasing = AliasingSpeculation.EXACT_CHECK;
			break;
		case "INDEX_CHECK":
			aliasing = AliasingSpeculation.INDEX_CHECK;
			break;
		case "PREDICATION_CHECK":
			aliasing = AliasingSpeculation.PREDICATION_CHECK;
			break;
		case "PESSIMISTIC_CHECK":
			aliasing = AliasingSpeculation.PESSIMISTIC_CHECK;
			break;
		case "NO_CHECK":
			aliasing = AliasingSpeculation.NO_CHECK;
			break;
		default:
			throw new RuntimeException("Unknown aliasing speculation strategy: " + speculation);
				
		}
		
		return aliasing;
	}
	
	
}
