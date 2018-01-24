package scheduler.representation;


import graph.Node;

public class ScheduleRepresentationPE {
	
	public static final int MAX_OPERATION_STRING_LENGTH = 15;
	public static final int MAX_INPUT_STRING_LENGTH = 25;
	
	int in1PE,in2PE,in0PE;
	int in1RFAddr,in2RFAddr,in0RFAddr;
	Node in1Node = null, in2Node = null, in0Node = null;
	
	int cboxSelect;
	
	Node operation = null;
	int operationRFaddr = -1;
	boolean operationConditional = false;
	int duration = 1;
	
	Node outNode;
	int outAddr;
	
	
	
	
	boolean copying = false;
	
	
	private enum COLOR{
		RED,
		GREEN,
		BLUE,
		GRAY,
		WHITE
	}
	
	
	
	public ScheduleRepresentationPE(){
		
	}
	

	public int getIn1PE() {
		return in1PE;
	}

	public void setIn1PE(int in1pe) {
		in1PE = in1pe;
	}

	public int getIn2PE() {
		return in2PE;
	}

	public void setIn2PE(int in2pe) {
		in2PE = in2pe;
	}

	public int getIn3PE() {
		return in0PE;
	}

	public void setIn0PE(int in0pe) {
		in0PE = in0pe;
	}

	public int getIn1RFAddr() {
		return in1RFAddr;
	}

	public void setIn1RFAddr(int in1rfAddr) {
		in1RFAddr = in1rfAddr;
	}

	public int getIn2RFAddr() {
		return in2RFAddr;
	}

	public void setIn2RFAddr(int in2rfAddr) {
		in2RFAddr = in2rfAddr;
	}

	public int getIn0RFAddr() {
		return in0RFAddr;
	}

	public void setIn0RFAddr(int in0rfAddr) {
		in0RFAddr = in0rfAddr;
	}

	public Node getIn1Node() {
		return in1Node;
	}

	public void setIn1Node(Node in1Node) {
		this.in1Node = in1Node;
	}

	public Node getIn2Node() {
		return in2Node;
	}

	public void setIn2Node(Node in2Node) {
		this.in2Node = in2Node;
	}

	public Node getIn0Node() {
		return in0Node;
	}

	public void setIn0Node(Node in0Node) {
		this.in0Node = in0Node;
	}

	public Node getOperation() {
		return operation;
	}

	public void setOperation(Node operation) {
		this.operation = operation;
	}

	public int getOperationRFaddr() {
		return operationRFaddr;
	}

	public void setOperationRFaddr(int operationRFaddr) {
		this.operationRFaddr = operationRFaddr;
	}
	
	

	public Node getOutNode() {
		return outNode;
	}


	public void setOutNode(Node outNode) {
		this.outNode = outNode;
	}


	public int getOutAddr() {
		return outAddr;
	}


	public void setOutAddr(int outAddr) {
		this.outAddr = outAddr;
	}


	public boolean isCopying() {
		return copying;
	}


	public void setCopying(boolean copying) {
		this.copying = copying;
	}


	public int getIn0PE() {
		return in0PE;
	}
	
	


	public boolean isOperationConditional() {
		return operationConditional;
	}


	public void setOperationConditional(boolean operationConditional) {
		this.operationConditional = operationConditional;
	}
	
	public void setCBoxSelect(int cboxSelect){
		this.cboxSelect = cboxSelect;
	}
	public void setDuration(Integer length) {
		this.duration = length;
		
	}
	
	public int getDuration(){
		return this.duration;
	}


	public COLOR getColor(){
		if(operation == null){
			return COLOR.WHITE;
		} else if(copying){
			return COLOR.GRAY;
		} else if(!operationConditional){//|| !(operation.getOperation() == Amidar.OP.STORE || operation.getOperation() == Amidar.OP.DMA_STORE)){
			return COLOR.BLUE;
		} else if(operation.getDecision()){
			return COLOR.GREEN;
		} else {
			return COLOR.RED;
		}
	}
	
	
	public String getOperationAsString(){
		if(operation == null){
			return "";
		}
		String retVal = operation.toString(); 
		if(copying){
			retVal = "C"+retVal;
		}
		if(retVal.length() > MAX_OPERATION_STRING_LENGTH){
			retVal = retVal.substring(0, MAX_OPERATION_STRING_LENGTH);
		}
		retVal = retVal.replace('_', '-');
		return retVal;
	}
	
	public String getInput1AsString(){
		if(in1Node == null){
			return "";
		}
		String retVal = "In1 : PE" +in1PE+"."+in1RFAddr+ " " + in1Node;
		
		if(retVal.length() > MAX_INPUT_STRING_LENGTH){
			retVal = retVal.substring(0, MAX_INPUT_STRING_LENGTH);
		}
		retVal = retVal.replace('_', '-');
		return retVal;
	}
	public String getInput2AsString(){
		if(in2Node == null){
			return "";
		}
		String retVal = "In2 : PE" +in2PE+"."+in2RFAddr+ " " + in2Node;
		
		if(retVal.length() > MAX_INPUT_STRING_LENGTH){
			retVal = retVal.substring(0, MAX_INPUT_STRING_LENGTH);
		}
		retVal = retVal.replace('_', '-');
		return retVal;
	}
	public String getInput0AsString(){
		if(in0Node == null){
			return "";
		}
		String retVal = "In0 : PE" +in0PE+"."+in0RFAddr+ " " + in0Node;
		
		if(retVal.length() > MAX_INPUT_STRING_LENGTH){
			retVal = retVal.substring(0, MAX_INPUT_STRING_LENGTH);
		}
		retVal = retVal.replace('_', '-');
		return retVal;
	}
	
	public String getOutputAsString(){
		String retVal = "Out : " + outAddr+ " " + outNode;
		
		if(retVal.length() > MAX_INPUT_STRING_LENGTH){
			retVal = retVal.substring(0, MAX_INPUT_STRING_LENGTH);
		}
		retVal = retVal.replace('_', '-');
		return retVal;
	}
	
	public String getPStricksColor(){
		switch(getColor()){
		case GRAY:
			return "lightgray";
		case BLUE:
			return "lightblue";
		case GREEN:
			return "lightgreen";
		case RED:
			return "lightred";
		case WHITE:
			return "white";
		default: 
			return "black";
		}
	}
	
	public String getPStricksColorOutput(){
		if(outNode != null){
			return "lightgray";
		} else {
			return "white";
		}
	}
	
	public String getPEdescriptionPSTricks(double x, double y, double width, double height , boolean printFull, boolean [] isPrefetch){
		String fillstyle="solid";
		if(isPrefetch != null && operation != null && isPrefetch[operation.getAddress()]){
			fillstyle = "vlines, hatchcolor=lightblue";
		}
		
		StringBuilder retVal = new StringBuilder("\\psframe[linewidth = 1.1pt,  fillstyle="+fillstyle+", fillcolor="+getPStricksColor()+"]("+x+","+y+")("+(x+width)+","+(y+height)+")\n");
		if(printFull){
			retVal.append("\\rput[lb]("+(x+0.1)+","+(y+height*5/6.0)+"){"+getInput0AsString()+"}\n");
			retVal.append("\\rput[lb]("+(x+0.1)+","+(y+height*4/6.0)+"){"+getInput1AsString()+"}\n");
			retVal.append("\\rput[lb]("+(x+0.1)+","+(y+height*3/6.0)+"){"+getInput2AsString()+"}\n");
		}
		
		if(operation!=null){
			retVal.append("\\rput("+(x+width/2.0)+","+(y+height/4.0)+"){\\large "+getOperationAsString() + "\\normalsize");
			if(operationRFaddr != -1){
				retVal.append("$\\rightarrow$ " + operationRFaddr+"}\n");
			} else {
				retVal.append("}\n");
			}
		}
		if(operationConditional){
			retVal.append("\\rput("+(x+width*0.95)+","+(y+width*0.05)+"){\\large "+cboxSelect + "\\normalsize}\n");
		}
		
		return retVal.toString();
	}
	
	public String getPEOutputPSTricks(double x, double y, double width, double height ){
		StringBuilder retVal;
//		retVal.append("\\rput[lb]("+(x+0.1)+","+(y+height*5/6.0)+"){"+getInput0AsString()+"}\n");
//		retVal.append("\\rput[lb]("+(x+0.1)+","+(y+height*4/6.0)+"){"+getInput1AsString()+"}\n");
//		retVal.append("\\rput[lb]("+(x+0.1)+","+(y+height*3/6.0)+"){"+getInput2AsString()+"}\n");
		
		if(outNode!=null){
			retVal  = new StringBuilder("\\psframe[linewidth = 1.1pt,  fillstyle=solid, fillcolor="+getPStricksColorOutput()+"]("+x+","+y+")("+(x+width)+","+(y+height)+")\n");
			retVal.append("\\rput("+(x+width/2.0)+","+(y+height/2.0)+"){\\large "+getOutputAsString() + "\\normalsize");
			if(operationRFaddr != -1){
				retVal.append("$\\rightarrow$ " + operationRFaddr+"}\n");
			} else {
				retVal.append("}\n");
			}
		} else {
			retVal  = new StringBuilder("\\psframe[linewidth = 1.1pt]("+x+","+y+")("+(x+width)+","+(y+height)+")\n");
		}
		
		
		return retVal.toString();
	}




}
