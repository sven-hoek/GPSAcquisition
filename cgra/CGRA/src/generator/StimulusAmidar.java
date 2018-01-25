package generator;

import cgramodel.ContextMask;

/**
 * Stimulus class, heretage of {@link Stimulus} These are tracked during simulation
 * and can be used as a trigger in HDL simulations in Modelsim. 
 * @author wolf
 *
 */
public class StimulusAmidar extends Stimulus{

	CgraInstruction instruction = null;
	long[] contexts = null;
	Integer contextID = null;
	Integer operandAddress = null; // operand a_low
	Integer operandData = null; // operand b_low
	Integer result = null;
	
	
	protected StimulusAmidar(){
		
	}
	
	public StimulusAmidar(CgraInstruction instruction, int adr, int data){
		this.instruction = instruction;
		operandAddress = adr;
		operandData = data;
	}
	
	public StimulusAmidar(CgraInstruction instruction, int adr, int data, int result){
		this.instruction = instruction;
		operandAddress = adr;
		operandData = data;
		this.result = result;
	}
	
	public StimulusAmidar(CgraInstruction instruction, long[] contexts, int contextID){
		this.contexts = contexts;
		this.instruction = instruction;
		this.contextID = contextID;
	}
	
	public String taskCall(){
		
		String callStatement ="";
		
		switch(instruction){
		case LOADPROGRAM:
			callStatement+= ("//writeContextSet("+contextID+","+contexts.length+");\n");
			ContextMask trick = new ContextMask();
			break;
		case RECEIVELOCALVAR: 
			callStatement+= ("writeLocalVariable("+ operandAddress +","+operandData+");\n");
			break;
		case SENDLOCALVAR:
			callStatement+= ("retrieveLocalVariable("+operandAddress+","+result+");\n");
			break;
		case RUN:
			callStatement+= ("triggerRun("+operandAddress+");\n");
			break;
		}
		return callStatement;
	}
	

	public CgraInstruction getInstruction() {
		return instruction;
	}

	public void setInstruction(CgraInstruction instruction) {
		this.instruction = instruction;
	}

	public long[] getContexts() {
		return contexts;
	}

	public void setContexts(long[] contexts) {
		this.contexts = contexts;
	}

	public Integer getOperandAddress() {
		return operandAddress;
	}

	public void setOperandAddress(Integer operandAddress) {
		this.operandAddress = operandAddress;
	}

	public Integer getOperandData() {
		return operandData;
	}

	public void setOperandData(Integer operandData) {
		this.operandData = operandData;
	}
	
	public String toString(){
		String ret = "Stimulus: " + instruction;
		return ret;
	}
}
