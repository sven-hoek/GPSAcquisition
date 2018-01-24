package generator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import target.Processor;

@Deprecated
public class RedundancyChecker {

	
	public RedundancyChecker(){
	}
	
	
	public void findRegfileMissmatch(){
		
		FileReader regshdlfile = null;
		FileReader regsemulationfile = null;
		try {
			regshdlfile = new FileReader(new File(Processor.Instance.getDebuggingPath()+"/debug_registerfiles_hdl"));
			 regsemulationfile = new FileReader(new File(Processor.Instance.getDebuggingPath()+"/debug_registerfiles_emulation"));
		} catch (FileNotFoundException e) {
			System.err.println("Fileproblem in FindRegfileMissmatch");
		}
		BufferedReader brhdlregs = new BufferedReader(regshdlfile);
		BufferedReader bremulationregs = new BufferedReader(regsemulationfile);
		String hdlline;
		String emulationline;
		int cycle = -1;
		int run = -1;
		try {
			while( (hdlline=brhdlregs.readLine())!= null && (emulationline=bremulationregs.readLine()) != null){
				if(hdlline.contains("--------- cycle:")){
					String helper = hdlline;
					helper = helper.substring(helper.indexOf(":")+2,helper.indexOf("("));
					helper.trim();
					cycle = Integer.parseInt(helper);
					
					helper = hdlline;
					helper = helper.substring(helper.indexOf("(")+5,helper.indexOf(")"));
					helper.trim();
					run = Integer.parseInt(helper);
				}
				
				if(!hdlline.equals(emulationline)){
					System.out.println("The first missmatch occurs in Run " + run + " tick " + cycle );
					break;
				}
			}
			
			
		} catch (IOException e) {
			System.err.println("Problem while iteration through lines for finding the missmatch in findRegfileMissmatch");
		}
	}
	
	
}
