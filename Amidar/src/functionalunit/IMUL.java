package functionalunit;

import tracer.TraceManager;
import exceptions.AmidarSimulatorException;
import functionalunit.FunctionalUnit.State;
import functionalunit.opcodes.IaluOpcodes;
import functionalunit.opcodes.ImulOpcodes;

public class IMUL extends FunctionalUnit<ImulOpcodes> {
	
	final static int OPERAND_A=0;
	final static int OPERAND_B=2;
	
	boolean waitingAckState=true;
	
	public IMUL(String configFile, TraceManager traceManager){
		super(ImulOpcodes.class, configFile, traceManager);
	}
	
	
//	public enum OPCODE implements FUOpcodes{
//		IADD(2), 
//		IMUL(2);
//		
//		private OPCODE(int nrOfOps){
//			this.nrOfOps = nrOfOps;
//		}
//		int nrOfOps;
//		
//		public int getNumberOfOperands() {
//			return nrOfOps;
//		}
//	};
	
	private long input2Long(int port) {
		return (((long) input[port+1]) <<32) | (((long) input[port]) & 0xFFFFFFFFL);
	}
	
	private void outputLong(long out) {
		output[RESULT_HIGH] = (int) (out>>>32);
		output[RESULT_LOW] = (int) (out&0xFFFFFFFF);
		setOutputValid(RESULT_LOW);
		setOutputValid(RESULT_HIGH);
	}
	
	public boolean executeOp(ImulOpcodes op){
		long in1,in2;
		switch (op) {
			case IMUL:
				output[RESULT_LOW] = input[OPERAND_A_LOW] * input[OPERAND_B_LOW];
//				System.out.println("IMUL");
//				System.out.println("\tInputA: "+input[OPERAND_A_LOW]);
//				System.out.println("\tInputB: "+input[OPERAND_B_LOW]);
//				System.out.println("\tOutput: "+output[RESULT_LOW]);
				setOutputValid(RESULT_LOW);
				break;
			case LMUL:
				outputLong(input2Long(OPERAND_A) * input2Long(OPERAND_B));
				break;
			default:
				throw new AmidarSimulatorException("Operation "+op.toString()+ " not defined in "+this.getClass());
			}
			return true;
	}

	public int getNrOfInputports() {
		return 4;
	}

	public boolean validInputs(ImulOpcodes op) {
		switch(op){
		case IMUL:
			return inputValid[OPERAND_A_LOW] && inputValid[OPERAND_B_LOW];
		case LMUL:
			return inputValid[OPERAND_A_HIGH] && inputValid[OPERAND_A_LOW] && inputValid[OPERAND_B_HIGH] && inputValid[OPERAND_B_LOW];
		default:
			return false;
		}
		
	}
	
	public boolean tick() {
		if(currentState == State.SENDING){
			if(waitingAckState) {
				if(getResultAck()){
					count=-1;
					waitingAckState=false;
					for(int i = 0; i < outputValid.length; i++){
						outputValid[i] = false;
					}
					setResultAck(false);
				}
			}
			else {
				currentState= State.IDLE;
				waitingAckState=true;
			}
			return false;
		}
		return super.tick();
	}

}
