package io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;

import cgramodel.CgraModel;

public class CGRASerializer{

	public boolean serialize(CgraModel cgra, String folder, String name) {
		try {	
			
			if(!folder.endsWith("/")){
				folder ="/";
			}
			if(!name.endsWith(".ser")){
				name +=".ser";
			}
			File file = new File(folder + name);
			if(file.exists()){
				file.delete();
				System.out.println("Overwriting existing Serialization " + folder + name);
			}
			File dir = new File(folder);
			dir.mkdirs();
			FileOutputStream stream = new FileOutputStream(folder + name);
			ObjectOutputStream outputStream = new ObjectOutputStream(stream);
			outputStream.writeObject(cgra);
			outputStream.close();
			stream.close();
			return true;
		} catch (Exception e) {
			System.err.println("Error while serializing the Cgra - " + e.toString());
			return false;
		}
	}


	public boolean serialize(CgraModel cgra, String folder) {

		String filename = "";
		if (cgra.getName() == null || cgra.getName().isEmpty()) {
			filename = "cgra_" + new Date().toString();
		} else {
			filename = cgra.getName();
		}
		return serialize(cgra, folder, filename);
	}

	public CgraModel loadCGRA(String folder, String name) {

		if(!name.endsWith(".ser")){
			name += ".ser";
		}
		return loadCGRA(folder + name + ".ser");
	}	


	public CgraModel loadCGRA(String fullPath) {
		CgraModel cgra = null;
		try {
			FileInputStream fileIn = new FileInputStream(fullPath);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			cgra = (CgraModel) in.readObject();
			in.close();
			fileIn.close();
			return cgra;
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Something went wrong with: " + fullPath);
			return null;
		} catch (ClassNotFoundException e) {
			System.err.println("CNFEX");
			return null;
		}
	}

}
