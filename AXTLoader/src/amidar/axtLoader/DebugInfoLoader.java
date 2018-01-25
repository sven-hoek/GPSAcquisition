package amidar.axtLoader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class DebugInfoLoader {
	
	public static String[] loadDebugInfo(String application){
		String[] result = null;
		
		String[] path = application.split("/");
		
		String fileName = application.substring(0, application.length()-path[path.length-1].length()) + "Methods.json";
		
		JSONParser parser;
		JSONObject json = null;
		parser = new JSONParser();
		FileReader fileReader;
		try {
			fileReader = new FileReader(fileName);
			json = (JSONObject) parser.parse(fileReader);
		} catch (FileNotFoundException e) {
			System.err.println("No debug file \""+fileName+"\" found\n");
			e.printStackTrace(System.err);
		} catch (IOException e) {
			System.err.println("Error while reading debug file \""+fileName+"\"\n");
			e.printStackTrace(System.err);
		} catch (ParseException e) {
			System.err.println("Error while parsing debug file \""+fileName+"\"\n");
			e.printStackTrace(System.err);
		}
		
		ArrayList<String> methods = new ArrayList<String>();
		
		int cnt = 0;
		
		while(json.get(Integer.toString(cnt)) != null){
			
			methods.add((String)json.get(Integer.toString(cnt)));
			cnt++;
		}
		
		result = new String[methods.size()];
		
		for(int i = 0; i<result.length; i++){
			result[i] = methods.get(i);
		}
		
		
		return result;
	}

}
