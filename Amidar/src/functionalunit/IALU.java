package functionalunit;

import tracer.TraceManager;
import exceptions.AmidarSimulatorException;
import functionalunit.FunctionalUnit.State;
import functionalunit.opcodes.IaluOpcodes;


public class IALU extends FunctionalUnit<IaluOpcodes> {
	
	final static int OPERAND_A=0;
	final static int OPERAND_B=2;
	
	boolean waitingAckState=true;
	
	public IALU(String configFile, TraceManager traceManager){
		super(IaluOpcodes.class, configFile, traceManager);
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
	
	public boolean executeOp(IaluOpcodes op){
		long in1,in2;
		switch (op) {
			case IADD:
				output[RESULT_LOW] = input[OPERAND_A_LOW] + input[OPERAND_B_LOW];
//				System.out.println("IADD");
//				System.out.println("\tInputA: "+input[OPERAND_A_LOW]);
//				System.out.println("\tInputB: "+input[OPERAND_B_LOW]);
//				System.out.println("\tOutput: "+output[RESULT_LOW]);
				setOutputValid(RESULT_LOW);
				break;
			case IMUL:
				output[RESULT_LOW] = input[OPERAND_A_LOW] * input[OPERAND_B_LOW];
				setOutputValid(RESULT_LOW);
				break;
			case ISUB:
				output[RESULT_LOW] = input[OPERAND_A_LOW] - input[OPERAND_B_LOW];
				setOutputValid(RESULT_LOW);
				break;
			case IDIV:
				output[RESULT_LOW] = input[OPERAND_A_LOW] / input[OPERAND_B_LOW];
				setOutputValid(RESULT_LOW);
				break;
			case IREM:
				output[RESULT_LOW] = input[OPERAND_A_LOW] % input[OPERAND_B_LOW];
				setOutputValid(RESULT_LOW);
				System.out.println(" IREM : " + input[OPERAND_A_LOW] +  "%" + input[OPERAND_B_LOW] +" = " + output[RESULT_LOW]);
				break;
				
			//Integer Casts	
			case I2B:
				output[RESULT_LOW] = (byte) input[OPERAND_A_LOW];
				setOutputValid(RESULT_LOW);
//				System.out.println("I2B");
//				System.out.println("\tInputA: "+input[OPERAND_A_LOW]);
//				System.out.println("\tOutput: "+output[RESULT_LOW]);
				break;
			case I2C:
				output[RESULT_LOW] = (char) input[OPERAND_A_LOW];
				setOutputValid(RESULT_LOW);
				break;
			case I2S:
				output[RESULT_LOW] = (short) input[OPERAND_A_LOW];
				setOutputValid(RESULT_LOW);
				break;
			case I2L:
				outputLong(input[OPERAND_A_LOW]);
				break;
			//Integer Compare	
			case ICMP:
				if(input[OPERAND_A_LOW] == input[OPERAND_B_LOW]) { 
					output[RESULT_LOW] = 0;
				}
				if(input[OPERAND_A_LOW] > input[OPERAND_B_LOW]) {
					output[RESULT_LOW] = 1;
				}
				if(input[OPERAND_A_LOW] < input[OPERAND_B_LOW]) {
					output[RESULT_LOW] = -1;
				}
//				System.out.println("IADD");
//				System.out.println("\tInputA: "+input[OPERAND_A_LOW]);
//				System.out.println("\tInputB: "+input[OPERAND_B_LOW]);
//				System.out.println("\tOutput: "+output[RESULT_LOW]);
				setOutputValid(RESULT_LOW);
				break;
			case ICMP_ZERO:
				if(input[OPERAND_A_LOW] == 0) { 
					output[RESULT_LOW] = 0;
				}
				if(input[OPERAND_A_LOW] > 0) {
					output[RESULT_LOW] = 1;
				}
				if(input[OPERAND_A_LOW] < 0) {
					output[RESULT_LOW] = -1;
				}
//				System.out.println("ICMP_zerO");
//				System.out.println("\tInputA: "+input[OPERAND_A_LOW]);
//				System.out.println("\tOutput: "+output[RESULT_LOW]);
				setOutputValid(RESULT_LOW);
				break;
			//Integer Binary	
			case ISHL:
				output[RESULT_LOW] = input[OPERAND_A_LOW] << input[OPERAND_B_LOW];
				setOutputValid(RESULT_LOW);
				break;
			case IUSHR:
				output[RESULT_LOW] = input[OPERAND_A_LOW] >>> input[OPERAND_B_LOW];
				setOutputValid(RESULT_LOW);
				break;
			case ISHR:
				output[RESULT_LOW] = input[OPERAND_A_LOW] >> input[OPERAND_B_LOW];
				setOutputValid(RESULT_LOW);
//				System.out.println("ISHR");
//				System.out.println("\tInputA: "+input[OPERAND_A_LOW]);
//				System.out.println("\tInputB: "+input[OPERAND_B_LOW]);
//				System.out.println("\tOutput: "+output[RESULT_LOW]);
				break;
			case IAND:
				output[RESULT_LOW] = input[OPERAND_A_LOW] & input[OPERAND_B_LOW];
				setOutputValid(RESULT_LOW);
				break;
			case IOR:
				output[RESULT_LOW] = input[OPERAND_A_LOW] | input[OPERAND_B_LOW];
				setOutputValid(RESULT_LOW);
				break;
			case IXOR:
				output[RESULT_LOW] = input[OPERAND_A_LOW] ^ input[OPERAND_B_LOW];
				setOutputValid(RESULT_LOW);
				break;
			case INEG:
				output[RESULT_LOW] = -input[OPERAND_A_LOW];
				setOutputValid(RESULT_LOW);
				break;
			//Long
			case LADD:
				outputLong(input2Long(OPERAND_A) + input2Long(OPERAND_B));
				break;
			case LSUB:
				outputLong(input2Long(OPERAND_A) - input2Long(OPERAND_B));
				break;
			case LMUL:
				outputLong(input2Long(OPERAND_A) * input2Long(OPERAND_B));
				break;
			case LDIV:
				outputLong(input2Long(OPERAND_A) / input2Long(OPERAND_B));
				break;
			case LREM:
				outputLong(input2Long(OPERAND_A) % input2Long(OPERAND_B));
				break;
			//Long Casts
			case L2I:
				output[RESULT_LOW] = input[OPERAND_A_LOW];
				setOutputValid(RESULT_LOW);
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

	public boolean validInputs(IaluOpcodes op) {
		switch(op){
		case IADD:
		case ISUB:
		case IMUL:
		case IDIV:
		case IREM:
		case ICMP:
		case ISHL:
		case ISHR:
		case IUSHR:
		case IAND:
		case IOR:
		case IXOR:
			return inputValid[OPERAND_A_LOW] && inputValid[OPERAND_B_LOW];
		case I2B:
		case I2C:
		case I2S:
		case I2L:
		case INEG:
		case ICMP_ZERO:
			return inputValid[OPERAND_A_LOW];
		case LADD:
		case LSUB:
		case LMUL:
		case LAND:
		case LOR:
		case LXOR:
		case LCMP:
		case LDIV:
		case LREM:
			return inputValid[OPERAND_A_HIGH] && inputValid[OPERAND_A_LOW] && inputValid[OPERAND_B_HIGH] && inputValid[OPERAND_B_LOW];
		case LNEG:
		case L2I:
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
