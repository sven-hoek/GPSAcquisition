package functionalunit;

import tracer.TraceManager;
import functionalunit.opcodes.FpuOpcodes;

public class FPU extends FunctionalUnit<FpuOpcodes> {
	
	final static int OPERAND_A=0;
	final static int OPERAND_B=2;
	
	public FPU(String configFile, TraceManager traceManager){
		super(FpuOpcodes.class, configFile, traceManager);
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
	
	public boolean executeOp(FpuOpcodes op){
		float in1F, in2F, outF;
		double in1D, in2D, outD;
		long outL;
		switch (op) {
		
		//Float arithmetic
		case FADD:
			in1F=Float.intBitsToFloat(input[OPERAND_A_LOW]);
			in2F=Float.intBitsToFloat(input[OPERAND_B_LOW]);
			outF=in1F+in2F;
			output[RESULT_LOW]=Float.floatToIntBits(outF);
			setOutputValid(RESULT_LOW);
			break;
		case FSUB:
			in1F=Float.intBitsToFloat(input[OPERAND_A_LOW]);
			in2F=Float.intBitsToFloat(input[OPERAND_B_LOW]);
			outF=in1F-in2F;
			output[RESULT_LOW]=Float.floatToIntBits(outF);
			setOutputValid(RESULT_LOW);
			break;
		case FMUL:
			in1F=Float.intBitsToFloat(input[OPERAND_A_LOW]);
			in2F=Float.intBitsToFloat(input[OPERAND_B_LOW]);
			outF=in1F*in2F;
//			System.out.println(outF);
			output[RESULT_LOW]=Float.floatToIntBits(outF);
			setOutputValid(RESULT_LOW);
			break;
		case FTRUNC:
			int bits=Float.floatToIntBits(Math.round(Float.intBitsToFloat(input[OPERAND_A_LOW])));
			int exp=((bits & 0x7f800000) >> 23) - 127;
			if(exp<0) output[RESULT_LOW]=bits & 0x800000;
			else output[RESULT_LOW]=bits & (0xff800000 | (~(0x7FFFFF>>exp)));
			output[RESULT_LOW] = bits;
			setOutputValid(RESULT_LOW);
			break;
		case FNEG: //aus alt übernommen
			output[RESULT_LOW] = input[OPERAND_A_LOW] ^ 0x80000000;
			setOutputValid(RESULT_LOW);
			break;
		//Float Compare
		case FCMPG:
			in1F=Float.intBitsToFloat(input[OPERAND_A_LOW]);
			in2F=Float.intBitsToFloat(input[OPERAND_B_LOW]);
			if(in1F<in2F)  output[RESULT_LOW]=-1;
			else if(in1F==in2F) output[RESULT_LOW]=0;
			else output[RESULT_LOW]=1; //greater or either value is NaN
			setOutputValid(RESULT_LOW);
			break;
		case FCMPL:
			in1F=Float.intBitsToFloat(input[OPERAND_A_LOW]);
			in2F=Float.intBitsToFloat(input[OPERAND_B_LOW]);
			if (in1F>in2F) output[RESULT_LOW]=1;
			else if (in1F==in2F) output[RESULT_LOW]=0;
			else output[RESULT_LOW]=-1; //in1 lower than in2 or either value is NaN
			setOutputValid(RESULT_LOW);
			break;
			
		//Double arithmetic
		case DADD:
			in1D = Double.longBitsToDouble(input2Long(OPERAND_A));
			in2D = Double.longBitsToDouble(input2Long(OPERAND_B));
			outD = in1D + in2D;
			outputDouble(outD);
			break;
		case DSUB:
			in1D = Double.longBitsToDouble(input2Long(OPERAND_A));
			in2D = Double.longBitsToDouble(input2Long(OPERAND_B));
			outD = in1D - in2D;
			outputDouble(outD);
			
//			System.out.println("DSUB:");
//			System.out.println("\t"+in1D);
//			System.out.println("\t"+in2D);
//			System.out.println("\t"+outD);
			break;
		
		case DMUL:
			in1D = Double.longBitsToDouble(input2Long(OPERAND_A));
			in2D = Double.longBitsToDouble(input2Long(OPERAND_B));
			outD = in1D * in2D;
			outputDouble(outD);
//			System.out.println("DMUL:");
//			System.out.println("\t"+in1D);
//			System.out.println("\t"+in2D);
//			System.out.println("\t"+outD);
			break;
//		case DOUBLE_REM:
//			in1D = Double.longBitsToDouble(input2Long(OPERAND_A));
//			in2D = Double.longBitsToDouble(input2Long(OPERAND_B));
//			outD = in1D % in2D;
//			outputDouble(outD);
//			break;
		case DTRUNC:
			long bits1=input2Long(OPERAND_A);
			
//			System.out.println("TRUNC: " + Double.longBitsToDouble(bits1));
			
			int exp1=(int) ((bits1 & 0x7ff0000000000000L) >> 52) - 1023;
			if(exp1<0) {
				outL=0;
			}
			else {
				outL=bits1 & (~(0xFFFFFFFFFFFFFL>>exp1));
			}
			
//			System.out.println( " RES: "  + Double.longBitsToDouble(outL));
			output[RESULT_LOW] = (int) (outL & 0xFFFFFFFF);
			output[RESULT_HIGH] = (int) (outL>>32);
			setOutputValid(RESULT_LOW);
			setOutputValid(RESULT_HIGH);
			break;
		case DNEG:
			output[RESULT_HIGH] = input[OPERAND_A_HIGH] ^ 0x80000000;
			output[RESULT_LOW] = input[OPERAND_A_LOW];
			setOutputValid(RESULT_LOW);
			setOutputValid(RESULT_HIGH);
			break;
			
		//Double Compare
		case DCMPG:
			in1D = Double.longBitsToDouble(input2Long(OPERAND_A));
			in2D = Double.longBitsToDouble(input2Long(OPERAND_B));
			if(in1D<in2D)  output[RESULT_LOW]=-1;
			else if(in1D==in2D) output[RESULT_LOW]=0;
			else output[RESULT_LOW]=1; //greater or either value is NaN
			setOutputValid(RESULT_LOW);
			break;
		case DCMPL:
			in1D = Double.longBitsToDouble(input2Long(OPERAND_A));
			in2D = Double.longBitsToDouble(input2Long(OPERAND_B));
			if (in1D>in2D) output[RESULT_LOW]=1;
			else if (in1D==in2D) output[RESULT_LOW]=0;
			else output[RESULT_LOW]=-1; //in1 lower than in2 or either value is NaN
			setOutputValid(RESULT_LOW);
			break;
		//Casts
		case I2F:
			output[RESULT_LOW] = Float.floatToIntBits(input[OPERAND_A_LOW]);
			setOutputValid(RESULT_LOW);
			break;
		case F2I:
			in1F=Float.intBitsToFloat(input[OPERAND_A_LOW]);
			output[RESULT_LOW]=(int) in1F;
			setOutputValid(RESULT_LOW);
			break;
		case F2L:
			outL=(long) Float.intBitsToFloat(input[OPERAND_A_LOW]);
			output[RESULT_LOW] = (int) (outL & 0xFFFFFFFF);
			output[RESULT_HIGH] = (int) (outL>>32);
			setOutputValid(RESULT_LOW);
			setOutputValid(RESULT_HIGH);
			break;
		case F2D:
			in1F=Float.intBitsToFloat(input[OPERAND_A_LOW]);
			outputDouble(in1F);
			break;	
		case D2F:
			in1D = Double.longBitsToDouble(input2Long(OPERAND_A));
			output[RESULT_LOW] = Float.floatToIntBits((float) in1D);
			setOutputValid(RESULT_LOW);
			break;
		case D2I:
			in1D = Double.longBitsToDouble(input2Long(OPERAND_A));
			output[RESULT_LOW]=(int) in1D;
			setOutputValid(RESULT_LOW);
			break;
		case D2L:
			outL =(long) Double.longBitsToDouble(input2Long(OPERAND_A));
			output[RESULT_LOW] = (int) (outL & 0xFFFFFFFF);
			output[RESULT_HIGH] = (int) (outL>>32);
			setOutputValid(RESULT_LOW);
			setOutputValid(RESULT_HIGH);
			break;
		case L2D:
			outputDouble(input2Long(OPERAND_A));
			break;
		case L2F:
			outL=input2Long(OPERAND_A);
			output[RESULT_LOW] = Float.floatToIntBits(outL);
			setOutputValid(RESULT_LOW);
			break;	
		case I2D:
			outL=input[OPERAND_A_LOW];
			outputDouble(outL);
			break;
			
		//Constants
//		case FLOAT_NEGATIV_INFINITY:
//			output[RESULT_LOW] = Float.floatToIntBits(Float.NEGATIVE_INFINITY);
//			setOutputValid(RESULT_LOW);
//			break;
//		case FLOAT_NAN:
//			output[RESULT_LOW] = Float.floatToIntBits(Float.NaN);
//			setOutputValid(RESULT_LOW);
//			break;
//		case FLOAT_NEGATIV_ZERO:
//			output[RESULT_LOW] = Float.floatToIntBits(0) ^ 0x80000000;
//			setOutputValid(RESULT_LOW);
//			break;
//		case FLOAT_POSITIV_INFINITY:
//			output[RESULT_LOW] = Float.floatToIntBits(Float.NEGATIVE_INFINITY);
//			setOutputValid(RESULT_LOW);
//			break;
//		case FLOAT_POSITIV_ONE:
//			output[RESULT_LOW] = Float.floatToIntBits(1);
//			setOutputValid(RESULT_LOW);
//			break;
//		case FLOAT_POSITIV_TWO:
//			output[RESULT_LOW] = Float.floatToIntBits(2);
//			setOutputValid(RESULT_LOW);
//			break;
//		case FLOAT_POSITIV_ZERO:
//			output[RESULT_LOW] = Float.floatToIntBits(0);
//			setOutputValid(RESULT_LOW);
//			break;	
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
//		default:
			//GRTthrow new AmidarSimulatorException("Operation "+op.toString()+ " not defined in "+this.getClass());
		}
		return true;
	}
	
	public int getNrOfInputports() {
		return 4;
	}

	@Override
	public boolean validInputs(FpuOpcodes op) {
		switch(op) {
		case FADD:
		case FSUB:
		case FMUL:
		case FCMPG:
		case FCMPL:
			return inputValid[OPERAND_A_LOW] && inputValid[OPERAND_B_LOW];
		case FTRUNC:
		case FNEG:
		case I2F:
		case F2I:
		case F2L:
		case F2D:
		case I2D:
			return inputValid[OPERAND_A_LOW];
		case DTRUNC:
		case DNEG:
		case D2F:
		case D2I:
		case D2L:
		case L2D:
		case L2F:
			return inputValid[OPERAND_A_HIGH] && inputValid[OPERAND_A_LOW];
		case DADD:
		case DSUB:
		case DMUL:
		case DCMPG:
		case DCMPL:
			return inputValid[OPERAND_A_HIGH] && inputValid[OPERAND_A_LOW] && inputValid[OPERAND_B_HIGH] && inputValid[OPERAND_B_LOW];
		default: //(Constants)
			return true;
		}
	}

}

