package scheduler.representation;

import graph.Node;

public class ScheduleRepresentationCBox {
	public static final int MAX_OPERATION_STRING_LENGTH = 15;
	public static final int MAX_INPUT_STRING_LENGTH = 15;
	
	Node input;
	int inPE;
	int inAddr;
	
	Node [] outputToPEs = new Node[8];
	boolean [] outputDecision = new boolean[8];
	int [] outAddr = new int [8];
	
	
	public Node getInput() {
		return input;
	}
	public void setInput(Node input) {
		this.input = input;
	}
	public Node getOutputToPEs(int port) {
		return outputToPEs[port];
	}
	public void setOutputToPEs(Node outputToPEs, int port) {
		this.outputToPEs[port] = outputToPEs;
	}
	public boolean getOutputDecision(int port) {
		return outputDecision[port];
	}
	public void setOutputDecision(boolean outputDecision, int port) {
		this.outputDecision[port] = outputDecision;
	}
	public int getInPE() {
		return inPE;
	}
	public void setInPE(int inPE) {
		this.inPE = inPE;
	}
	public int getInAddr() {
		return inAddr;
	}
	public void setInAddr(int inAddr) {
		this.inAddr = inAddr;
	}
	public int getOutAddr(int port) {
		return outAddr[port];
	}
	public void setOutAddr(int outAddr, int port) {
		this.outAddr[port] = outAddr;
	}
	
	public String getInputAsString(){
		if(input == null){
			return "";
		}
		String retVal = "In1 : PE" +inPE+ " " + input;
		
		if(retVal.length() > MAX_INPUT_STRING_LENGTH){
			retVal = retVal.substring(0, MAX_INPUT_STRING_LENGTH);
		}
		retVal = retVal.replace('_', '-');
		return retVal;
	}
	
	public String getOutputAsString(){
		if(input == null){
			return "";
		}
		String retVal = "In1 : PE" +inPE+ " " + input;
		
		if(retVal.length() > MAX_INPUT_STRING_LENGTH){
			retVal = retVal.substring(0, MAX_INPUT_STRING_LENGTH);
		}
		retVal = retVal.replace('_', '-');
		return retVal;
	}
	
	public String getOperationAsString(int port){
		String retVal = outputToPEs[port].toString(); 

		if(retVal.length() > MAX_OPERATION_STRING_LENGTH){
			retVal = retVal.substring(0, MAX_OPERATION_STRING_LENGTH);
		}
		retVal = retVal.replace('_', '-');
		return retVal;
	}
	
	String getPStricksColor(int port){
		if(outputDecision[port]){
			return "lightgreen";
		} else {
			return "lightred";
		}
	}
	
	public String getCBoxDescriptionPSTricks(double d, double e, double f, double g ){
		StringBuilder retVal;
		if(input != null){
			retVal = new StringBuilder("\\psframe[linewidth = 1.1pt,  fillstyle=solid, fillcolor=lightblue]("+d+","+e+")("+(d+f)+","+(e+g)+")\n");
			retVal.append("\\rput[lb]("+d+","+(e+g*4/8.0)+"){"+getInputAsString()+" $\\rightarrow$ " + inAddr + "}\n");
		} else {
			retVal = new StringBuilder("\\psframe[linewidth = 1.1pt,  fillstyle=solid, fillcolor=white]("+d+","+e+")("+(d+f)+","+(e+g)+")\n");
		}
//		retVal.append("\\rput("+(x+width/2.0)+","+(y+height/4.0)+"){\\large"+getOperationAsString() + "\\normalsize("+outAddr+")}\n");
		
		
		return retVal.toString();
	}

	public String getCBoxOutputPSTricks(double x, double y, double width, double height, int port ){
	
		StringBuilder retVal;
//		retVal.append("\\rput[lb]("+x+","+(y+height*4/6.0)+"){"+getInputAsString()+" $\\rightarrow$ " + inAddr + "}\n");
		if(outputToPEs[port] != null){
			retVal = new StringBuilder("\\psframe[linewidth = 1.1pt,  fillstyle=solid, fillcolor="+getPStricksColor(port)+"]("+x+","+y+")("+(x+width)+","+(y+height)+")\n");
			retVal.append("\\rput("+(x+width/2.0)+","+(y+height/2.0)+"){\\large"+port+": "+getOperationAsString(port) + "\\normalsize("+outAddr[port]+")}\n");
		} else {
			retVal = new StringBuilder("\\psframe[linewidth = 1.1pt,  fillstyle=solid, fillcolor=white]("+x+","+y+")("+(x+width)+","+(y+height)+")\n");
		}
		
		
		return retVal.toString();
	}

}
