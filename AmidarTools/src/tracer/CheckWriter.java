package tracer;

import java.io.PrintWriter;

public class CheckWriter extends PrintWriter {
	
	StringBuilder value;
	
	String expected;
	String output;
	
	
	
	public CheckWriter(){
		super(System.out);
		value = new StringBuilder();
	}
	
	
	public void print(String s) {
		value.append(s);
	}
	
	public void println(String s){
		value.append(s+"\n");
	}
	
	
	public void flush(){
		
	}
	
	public boolean check(String reference){
		expected = reference.trim();
		output = value.toString().trim();
		value = new StringBuilder();
//		System.out.println("\tEXPECTED: " + expected);
//		System.out.println("\tFOUND:    " + output);
		return expected.equals(output);
		
	}


	public String getExpected() {
		return expected;
	}


	public String getOutput() {
		return output;
	}
	
	
	

}
