package functionalunit;

import tracer.TraceManager;
import exceptions.AmidarSimulatorException;
import functionalunit.opcodes.FUOpcodes;
import functionalunit.opcodes.FaluOpcodes;


public class FALU extends FunctionalUnit<FaluOpcodes>  {
	
	final static int OPERAND_A=0;
	final static int OPERAND_B=2;
	
	public FALU(String configFile, TraceManager traceManager){
		super(FaluOpcodes.class, configFile, traceManager);
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
	
	public boolean executeOp(FaluOpcodes op){
		float in1F, in2F, outF;
		double in1D, in2D, outD;
		long outL;
		switch (op) {
		
		//Float arithmetic
		case FLOAT_ADD:
			in1F=Float.intBitsToFloat(input[OPERAND_A_LOW]);
			in2F=Float.intBitsToFloat(input[OPERAND_B_LOW]);
			outF=in1F+in2F;
			output[RESULT_LOW]=Float.floatToIntBits(outF);
			setOutputValid(RESULT_LOW);
			break;
		case FLOAT_SUB:
			in1F=Float.intBitsToFloat(input[OPERAND_A_LOW]);
			in2F=Float.intBitsToFloat(input[OPERAND_B_LOW]);
			outF=in1F-in2F;
			output[RESULT_LOW]=Float.floatToIntBits(outF);
			setOutputValid(RESULT_LOW);
			break;
		case FLOAT_MUL:
			in1F=Float.intBitsToFloat(input[OPERAND_A_LOW]);
			in2F=Float.intBitsToFloat(input[OPERAND_B_LOW]);
			outF=in1F*in2F;
//			System.out.println(outF);
			output[RESULT_LOW]=Float.floatToIntBits(outF);
			setOutputValid(RESULT_LOW);
			break;
		case FLOAT_DIV:
			in1F=Float.intBitsToFloat(input[OPERAND_A_LOW]);
			in2F=Float.intBitsToFloat(input[OPERAND_B_LOW]);
			outF=in1F/in2F;
			output[RESULT_LOW]=Float.floatToIntBits(outF);
			setOutputValid(RESULT_LOW);
			break;
//		case FLOAT_REM: // HW- Unterschied!
//			in1F=Float.intBitsToFloat(input[OPERAND_A_LOW]);
//			in2F=Float.intBitsToFloat(input[OPERAND_B_LOW]);
//			outF=in1F % in2F;
//			output[RESULT_LOW] = Float.floatToIntBits(outF);
//			setOutputValid(RESULT_LOW);
//			break;
		case FLOAT_TRUNC:
			int bits=Float.floatToIntBits(Math.round(Float.intBitsToFloat(input[OPERAND_A_LOW])));
			int exp=((bits & 0x7f800000) >> 23) - 127;
			if(exp<0) output[RESULT_LOW]=bits & 0x800000;
			else output[RESULT_LOW]=bits & (0xff800000 | (~(0x7FFFFF>>exp)));
			output[RESULT_LOW] = bits;
			setOutputValid(RESULT_LOW);
			break;
		case FLOAT_NEG: //aus alt übernommen
			output[RESULT_LOW] = input[OPERAND_A_LOW] ^ 0x80000000;
			setOutputValid(RESULT_LOW);
			break;
		//Float Compare
		case FLOAT_GRT:
			in1F=Float.intBitsToFloat(input[OPERAND_A_LOW]);
			in2F=Float.intBitsToFloat(input[OPERAND_B_LOW]);
			if(in1F<in2F)  output[RESULT_LOW]=-1;
			else if(in1F==in2F) output[RESULT_LOW]=0;
			else output[RESULT_LOW]=1; //greater or either value is NaN
			setOutputValid(RESULT_LOW);
//			System.out.println("FLUAT_GRT: "+ in1F + " " + in2F + " " + output[RESULT_LOW] );
			break;
		case FLOAT_LST:
			in1F=Float.intBitsToFloat(input[OPERAND_A_LOW]);
			in2F=Float.intBitsToFloat(input[OPERAND_B_LOW]);
			if (in1F>in2F) output[RESULT_LOW]=1;
			else if (in1F==in2F) output[RESULT_LOW]=0;
			else output[RESULT_LOW]=-1; //in1 lower than in2 or either value is NaN
			
//			System.out.println("FLUAT_LST: "+ in1F + " " + in2F + " " + output[RESULT_LOW] );
			setOutputValid(RESULT_LOW);
			break;
			
		//Double arithmetic
		case DOUBLE_ADD:
			in1D = Double.longBitsToDouble(input2Long(OPERAND_A));
			in2D = Double.longBitsToDouble(input2Long(OPERAND_B));
			outD = in1D + in2D;
			outputDouble(outD);
			break;
		case DOUBLE_SUB:
			in1D = Double.longBitsToDouble(input2Long(OPERAND_A));
			in2D = Double.longBitsToDouble(input2Long(OPERAND_B));
			outD = in1D - in2D;
			outputDouble(outD);
			
//			System.out.println("DSUB:");
//			System.out.println("\t"+in1D);
//			System.out.println("\t"+in2D);
//			System.out.println("\t"+outD);
			break;
		case DOUBLE_DIV:
			in1D = Double.longBitsToDouble(input2Long(OPERAND_A));
			in2D = Double.longBitsToDouble(input2Long(OPERAND_B));
			outD = in1D / in2D;
			outputDouble(outD);
//			System.out.println("DDIV:");
//			System.out.println("\t"+in1D);
//			System.out.println("\t"+in2D);
//			System.out.println("\t"+outD);
			break;
		case DOUBLE_MUL:
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
		case DOUBLE_TRUNC:
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
		case DOUBLE_NEG:
			output[RESULT_HIGH] = input[OPERAND_A_HIGH] ^ 0x80000000;
			output[RESULT_LOW] = input[OPERAND_A_LOW];
			setOutputValid(RESULT_LOW);
			setOutputValid(RESULT_HIGH);
			break;
			
		//Double Compare
		case DOUBLE_GRT:
			in1D = Double.longBitsToDouble(input2Long(OPERAND_A));
			in2D = Double.longBitsToDouble(input2Long(OPERAND_B));
			if(in1D<in2D)  output[RESULT_LOW]=-1;
			else if(in1D==in2D) output[RESULT_LOW]=0;
			else output[RESULT_LOW]=1; //greater or either value is NaN
			setOutputValid(RESULT_LOW);
			break;
		case DOUBLE_LST:
			in1D = Double.longBitsToDouble(input2Long(OPERAND_A));
			in2D = Double.longBitsToDouble(input2Long(OPERAND_B));
			if (in1D>in2D) output[RESULT_LOW]=1;
			else if (in1D==in2D) output[RESULT_LOW]=0;
			else output[RESULT_LOW]=-1; //in1 lower than in2 or either value is NaN
			setOutputValid(RESULT_LOW);
//			System.out.println("FLUAT_LST: "+ in1D + " " + in2D + " " + output[RESULT_LOW] );
			break;
		//Casts
		case INT_2_FLOAT:
			output[RESULT_LOW] = Float.floatToIntBits(input[OPERAND_A_LOW]);
			setOutputValid(RESULT_LOW);
			break;
		case FLOAT_2_INT:
			in1F=Float.intBitsToFloat(input[OPERAND_A_LOW]);
			output[RESULT_LOW]=(int) in1F;
			setOutputValid(RESULT_LOW);
			break;
		case FLOAT_2_LONG:
			outL=(long) Float.intBitsToFloat(input[OPERAND_A_LOW]);
			output[RESULT_LOW] = (int) (outL & 0xFFFFFFFF);
			output[RESULT_HIGH] = (int) (outL>>32);
			setOutputValid(RESULT_LOW);
			setOutputValid(RESULT_HIGH);
			break;
		case FLOAT_2_DOUBLE:
			in1F=Float.intBitsToFloat(input[OPERAND_A_LOW]);
			outputDouble(in1F);
			break;	
		case DOUBLE_2_FLOAT:
			in1D = Double.longBitsToDouble(input2Long(OPERAND_A));
			output[RESULT_LOW] = Float.floatToIntBits((float) in1D);
			setOutputValid(RESULT_LOW);
			break;
		case DOUBLE_2_INT:
			in1D = Double.longBitsToDouble(input2Long(OPERAND_A));
			output[RESULT_LOW]=(int) in1D;
			setOutputValid(RESULT_LOW);
			break;
		case DOUBLE_2_LONG:
			outL =(long) Double.longBitsToDouble(input2Long(OPERAND_A));
			output[RESULT_LOW] = (int) (outL & 0xFFFFFFFF);
			output[RESULT_HIGH] = (int) (outL>>32);
			setOutputValid(RESULT_LOW);
			setOutputValid(RESULT_HIGH);
			break;
		case LONG_2_DOUBLE:
			outputDouble(input2Long(OPERAND_A));
			break;
		case LONG_2_FLOAT:
			outL=input2Long(OPERAND_A);
			output[RESULT_LOW] = Float.floatToIntBits(outL);
			setOutputValid(RESULT_LOW);
			break;	
		case INT_2_DOUBLE:
			outL=input[OPERAND_A_LOW];
			outputDouble(outL);
			break;
			
		//Constants
		case FLOAT_NEGATIV_INFINITY:
			output[RESULT_LOW] = Float.floatToIntBits(Float.NEGATIVE_INFINITY);
			setOutputValid(RESULT_LOW);
			break;
		case FLOAT_NAN:
			output[RESULT_LOW] = Float.floatToIntBits(Float.NaN);
			setOutputValid(RESULT_LOW);
			break;
		case FLOAT_NEGATIV_ZERO:
			output[RESULT_LOW] = Float.floatToIntBits(0) ^ 0x80000000;
			setOutputValid(RESULT_LOW);
			break;
		case FLOAT_POSITIV_INFINITY:
			output[RESULT_LOW] = Float.floatToIntBits(Float.NEGATIVE_INFINITY);
			setOutputValid(RESULT_LOW);
			break;
		case FLOAT_POSITIV_ONE:
			output[RESULT_LOW] = Float.floatToIntBits(1);
			setOutputValid(RESULT_LOW);
			break;
		case FLOAT_POSITIV_TWO:
			output[RESULT_LOW] = Float.floatToIntBits(2);
			setOutputValid(RESULT_LOW);
			break;
		case FLOAT_POSITIV_ZERO:
			output[RESULT_LOW] = Float.floatToIntBits(0);
			setOutputValid(RESULT_LOW);
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
	public boolean validInputs(FaluOpcodes op) {
		switch(op) {
		case FLOAT_ADD:
		case FLOAT_SUB:
		case FLOAT_MUL:
		case FLOAT_DIV:
		case FLOAT_REM: //nicht in HW
		case FLOAT_GRT:
		case FLOAT_LST:
			return inputValid[OPERAND_A_LOW] && inputValid[OPERAND_B_LOW];
		case FLOAT_TRUNC:
		case FLOAT_NEG:
		case INT_2_FLOAT:
		case FLOAT_2_INT:
		case FLOAT_2_LONG:
		case FLOAT_2_DOUBLE:
		case INT_2_DOUBLE:
			return inputValid[OPERAND_A_LOW];
		case DOUBLE_TRUNC:
		case DOUBLE_NEG:
		case DOUBLE_2_FLOAT:
		case DOUBLE_2_INT:
		case DOUBLE_2_LONG:
		case LONG_2_DOUBLE:
		case LONG_2_FLOAT:
			return inputValid[OPERAND_A_HIGH] && inputValid[OPERAND_A_LOW];
		case DOUBLE_ADD:
		case DOUBLE_SUB:
		case DOUBLE_DIV:
		case DOUBLE_MUL:
		case DOUBLE_REM:
		case DOUBLE_GRT:
		case DOUBLE_LST:
			return inputValid[OPERAND_A_HIGH] && inputValid[OPERAND_A_LOW] && inputValid[OPERAND_B_HIGH] && inputValid[OPERAND_B_LOW];
		default: //(Constants)
			return true;
		}
	}

}
