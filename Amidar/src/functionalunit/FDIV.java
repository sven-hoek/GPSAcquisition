package functionalunit;

import tracer.TraceManager;
import functionalunit.opcodes.FaluOpcodes;
import functionalunit.opcodes.FdivOpcodes;

public class FDIV extends FunctionalUnit<FdivOpcodes> {
	
	final static int OPERAND_A=0;
	final static int OPERAND_B=2;
	
	public FDIV(String configFile, TraceManager traceManager){
		super(FdivOpcodes.class, configFile, traceManager);
	}
	
	private long input2Long(int port) {
		return (((long) input[port+1]) <<32) | (((long) input[port])&0xFFFFFFFFL);
	}
	
	private void outputDouble(double out) {
		long outL=Double.doubleToLongBits(out);
		output[RESULT_LOW] = (int) (outL & 0xFFFFFFFF);
		output[RESULT_HIGH] = (int) (outL>>32);
		setOutputValid(RESULT_LOW);
		setOutputValid(RESULT_HIGH);
	}
	
	public boolean executeOp(FdivOpcodes op){
		float in1F, in2F, outF;
		double in1D, in2D, outD;
		long outL;
		switch (op) {
		
		//Float arithmetic

		case FDIV:
			in1F=Float.intBitsToFloat(input[OPERAND_A_LOW]);
			in2F=Float.intBitsToFloat(input[OPERAND_B_LOW]);
			outF=in1F/in2F;
			output[RESULT_LOW]=Float.floatToIntBits(outF);
			setOutputValid(RESULT_LOW);
			break;
		case DDIV:
			in1D = Double.longBitsToDouble(input2Long(OPERAND_A));
			in2D = Double.longBitsToDouble(input2Long(OPERAND_B));
			outD = in1D / in2D;
			outputDouble(outD);
//			System.out.println("DDIV:");
//			System.out.println("\t"+in1D);
//			System.out.println("\t"+in2D);
//			System.out.println("\t"+outD);
			break;
//		case INT_NEGATIV_ONE:
//			break;
//		case INT_POSITIV_ONE:
//			break;
//		case INT_ZERO:
//			break;
		
//		case LONG_NEGATIV_ONE:
//			break;
//		case LONG_POSITIV_ONE:
//			break;
//		case LONG_ZERO:
//			break;
//			case DOUBLE_NAN:
//			break;
//		
//		case DOUBLE_NEGATIV_INFINITY:
//			break;
//		case DOUBLE_NEGATIV_ZERO:
//			break;
//		case DOUBLE_POSITIV_INFINITY:
//			break;
//		case DOUBLE_POSITIV_ONE:
//			break;
//		case DOUBLE_POSITIV_TWO:
//			break;
//		case DOUBLE_POSITIV_ZERO:
//			break;
		default:
			//GRTthrow new AmidarSimulatorException("Operation "+op.toString()+ " not defined in "+this.getClass());
		}
		return true;
	}
	
	public int getNrOfInputports() {
		return 4;
	}

	@Override
	public boolean validInputs(FdivOpcodes op) {
		switch(op) {
		case FDIV:
			return inputValid[OPERAND_A_LOW] && inputValid[OPERAND_B_LOW];
		case DDIV:
			return inputValid[OPERAND_A_HIGH] && inputValid[OPERAND_A_LOW] && inputValid[OPERAND_B_HIGH] && inputValid[OPERAND_B_LOW];
		default: //(Constants)
			return true;
		}
	}

}
