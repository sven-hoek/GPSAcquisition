package io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;

/**
 * The ObejctSerializer can be used to store/load any Objects on the on the harddrive independant of 
 * the current project. This can also be seen an as import or exprt of existing object. 
 * @author wolf
 *
 */
public class ObjectSerializer {

	public ObjectSerializer(){
	}
	
	/**
	 * This method can be used to store/export/serialize any object.
	 * @param obj The object to be stored
	 * @param filename the name of the file
	 * @return true if successful, otherwise false 
	 */
	public boolean serialize(Object obj, String filename){

		try {
			String name =  filename +".json";
			File file = new File(name);
			file.delete();
			JsonWriter jwrite = new JsonWriter(new FileOutputStream(name));
			jwrite.write(obj);
			jwrite.close();
			return true;			
		}
		catch(Exception e){
			System.err.println("Error while serializing an object - " + Object.class.getName() + "  -  "+ e.toString());
			return false;
		}
	}
	
	/**
	 * This method can be used to load/import/deserialize any given Object.
	 * @param filename name including the path to the desired object
	 * @return return the object if successful, otherwise null
	 */
	public Object deserialize(String filename){
		JsonReader jreader;
		try {
			jreader = new JsonReader(new FileInputStream(filename));
			Object obj =jreader.readObject(); 
			jreader.close();
			return obj;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.err.println("File not found Exception in ObjectSerializer: " + filename);
			return null;
		}
	}
	
}
