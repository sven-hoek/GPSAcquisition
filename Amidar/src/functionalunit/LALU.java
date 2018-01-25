package functionalunit;

import tracer.TraceManager;
import exceptions.AmidarSimulatorException;
import functionalunit.FunctionalUnit.State;
import functionalunit.opcodes.IaluOpcodes;
import functionalunit.opcodes.LaluOpcodes;

public class LALU extends FunctionalUnit<LaluOpcodes> {
	
	final static int OPERAND_A=0;
	final static int OPERAND_B=2;
	
	boolean waitingAckState=true;
	
	public LALU(String configFile, TraceManager traceManager){
		super(LaluOpcodes.class, configFile, traceManager);
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
	
	public boolean executeOp(LaluOpcodes op){
		long in1,in2;
		switch (op) {
			//Long
			case LADD:
				outputLong(input2Long(OPERAND_A) + input2Long(OPERAND_B));
				break;
			case LSUB:
				outputLong(input2Long(OPERAND_A) - input2Long(OPERAND_B));
				break;
			//Long Compare
			case LCMP:
				in1 = input2Long(OPERAND_A);
				in2 = input2Long(OPERAND_B);
				if(in1 == in2) output[RESULT_LOW]=0;
				if(in1 > in2) output[RESULT_LOW]=1;
				if(in1 < in2) output[RESULT_LOW]=-1;
				setOutputValid(RESULT_LOW);
				break;
			//Long Binary Operators
			case LSHL:
				outputLong(input2Long(OPERAND_A) << input[OPERAND_B_LOW]);
				break;
			case LUSHR:
				outputLong(input2Long(OPERAND_A) >>> input[OPERAND_B_LOW]);
				break;
			case LSHR:
				outputLong(input2Long(OPERAND_A) >> input[OPERAND_B_LOW]);
				break;
			case LAND:
				outputLong(input2Long(OPERAND_A) & input2Long(OPERAND_B));
				break;
			case LOR:
				outputLong(input2Long(OPERAND_A) | input2Long(OPERAND_B));
				break;
			case LXOR:
				outputLong(input2Long(OPERAND_A) ^ input2Long(OPERAND_B));
				break;
			case LNEG:
				outputLong(-input2Long(OPERAND_A));
				break;
			default:
				throw new AmidarSimulatorException("Operation "+op.toString()+ " not defined in "+this.getClass());
			}
			return true;
	}

	public int getNrOfInputports() {
		return 4;
	}

	public boolean validInputs(LaluOpcodes op) {
		switch(op){
		case LADD:
		case LSUB:
		case LAND:
		case LOR:
		case LXOR:
		case LCMP:
			return inputValid[OPERAND_A_HIGH] && inputValid[OPERAND_A_LOW] && inputValid[OPERAND_B_HIGH] && inputValid[OPERAND_B_LOW];
		case LNEG:
			return inputValid[OPERAND_A_HIGH] && inputValid[OPERAND_A_LOW];
		case LSHL:
		case LSHR:
		case LUSHR:
			return inputValid[OPERAND_A_HIGH] && inputValid[OPERAND_A_LOW] && inputValid[OPERAND_B_LOW];
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
