package operator;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import accuracy.BigNumber;
import accuracy.Format;
import accuracy.Range;

/**
 * Common logic for binary addition and subtraction.
 * 
 * @author Andreas Engel [engel@esa.cs.tu-darmstadt.de]
 */
public abstract class AddSub extends Binary {

  /** Unique ID for (de)marshalling */
  private static final long serialVersionUID = 4329616496845746776L;

  protected AddSub(Format a, Format b, Format r, String symbol) {
    super(a, b, r, symbol);
  }
  
  @Override 
  protected Number apply(BigNumber a, BigNumber b) {
    
    // make sure the operands can be represented using their format
    a = BigNumber.quantize(getOperandFormat(0), a);
    b = BigNumber.quantize(getOperandFormat(1), b);
    
    // sign adjustment for sub
    boolean as  = a.getSign();
    boolean bs  = b.getSign() ^ (symbol.equals("-"));
    
    // handle special symbols:
    //     +  | NaN +Inf -Inf NonInf         -  | NaN +Inf -Inf NonInf
    // -------+----------------------    -------+----------------------
    //    NaN | NaN  NaN  NaN  Nan          NaN | NaN  NaN  NaN  Nan  
    //   +Inf | NaN +Inf  NaN +Inf         +Inf | NaN  NaN +Inf +Inf  
    //   -Inf | NaN  NaN -Inf -Inf         -Inf | NaN -Inf NaN  -Inf  
    // NonInf | NaN +Inf -Inf NonInf     NonInf | NaN +Inf -Inf NonInf
    if (a.isNaN()      || b.isNaN())      return BigNumber.NaN;
    if (a.isInfinite() || b.isInfinite()) return as == bs ? a : BigNumber.cast(Double.NaN);
    
    // adjust larger to smaller exponent by least significant zeros to lsb
    boolean[] am, bm;
    int exp, dif;
    if (a.getExponent() > b.getExponent()) {
      exp = b.getExponent();
      dif = a.getExponent() - exp;
      bm  = b.getMantissa();
      am  = new boolean[a.getMantissa().length + dif];
      System.arraycopy(a.getMantissa(), 0, am, dif, a.getMantissa().length);
    } else {
      exp = a.getExponent();
      dif = b.getExponent() - exp;
      am  = a.getMantissa();
      bm  = new boolean[b.getMantissa().length + dif];
      System.arraycopy(b.getMantissa(), 0, bm, dif, b.getMantissa().length);
    }
    
    // avoid explicit inverting and sign extension by swapping operands
    boolean rs  = false;
    boolean add = true;
    boolean[] tmp;
    if (as == bs) {   //  a +  b = +(a + b)  and  -a + -b = -(a + b)
      rs  = as; 
    } else if (as) {  // -a +  b = +(b - a)
      add = false;
      tmp = bm;
      bm  = am;
      am  = tmp;
    } else {
      add = false;     //  a + -b = +(a - b)
    }
    
    // full adder with carry chain
    boolean[] rm = new boolean[Math.max(am.length, bm.length)+1];
    boolean cb = false;
    for (int i=0; i<rm.length; i++) {
      boolean ab = i < am.length && am[i];
      boolean bb = i < bm.length && bm[i];
      rm[i] = ab ^ bb ^ cb;
      cb    = add ? (ab && bb) || ( cb && (ab ^ bb))
                  : (cb && bb) || (!ab && (cb ^ bb));
    }
    
    // negative after subtract => invert mantissa
    if (!add && rm[rm.length-1]) {
      rs = true;
      cb = true;
      for (int i=0; i< rm.length; i++) {
        rm[i] ^= !cb;
        if (rm[i]) cb = false;
      }
    }
    return BigNumber.quantize(getResultFormat(), new BigNumber(rs, exp, rm));
  }
  
  /**
   * {@inheritDoc}
   * <p>
   * Multi cycle IO overlaps with actual computation 
   */
  @Override
  public List<Integer> getSupportedIntegerLatency() {
    return Arrays.asList(getOutputLatency());
  }
  
  @Override
  public String getIntegerImplementation() {
    StringBuilder res = new StringBuilder();
    int rw = getResultFormat().getBitWidth();
    int pw = getResultPortWidth();

    // single cycle
    if (getLatency() == 1) {
      for (int i = 0; i < getNumberOfOperands(); i++) {
        if (i != 0) {
          res.append(" " + symbol + " ");
        }
        res.append(getOperandPort(i));
        Format f = getOperandFormat(i);

        if (f.getBitWidth() > rw) {
          res.append(bitRange(rw));
        }
      }
      res = new StringBuilder(getResultPort().getAssignment(res.toString()));
      res.append(RESULT_VALID.getAssignment(START));

    // multi cycle
    } else {
      res.append("wire " + bitRange(pw + 1) + " tmp;\n");
      res.append("reg  overflow;\n\n");

      res.append(getClockedProcess("overflow <= tmp[" + pw + "];"));
      res.append("assign tmp = " + getOperandPort(0) + " " + symbol + " " + getOperandPort(1) + " " + symbol
          + " (" + START + " ? 0 : overflow);\n");
      res.append(getResultPort().getAssignment("tmp" + bitRange(pw)));
    }

    return res.toString();
  }

  /**
   * According to Stolfi2003 (SelfValidated Numerical Methods and
   * Applications):
   * <code>A +- B = a0 +- b0 + sum_{i>0} (ai +- bi) uncertainty_i</code>
   */
  @Override
  public Range getResultRange(Range.AA a, Range.AA b) {

    Range.AA res = new Range.AA(apply(a.getBase(), b.getBase()).doubleValue());
    for (Object tag : Range.AA.combineTags(a, b)) {
      res.setUncertainty(tag, apply(a.getUncertainty(tag), b.getUncertainty(tag)).doubleValue());
    }
    return res;
  }
 
//TODO Lars
  
  @Override
  protected int getTestRuns(){
	  return 1;
  }
  
@Override
protected Number[] getTestVector(int run) {
    if (!(getCommonFormat() instanceof Format.FloatingPoint)) return super.getTestVector(run);
	  Format.FloatingPoint op0 = (Format.FloatingPoint)getOperandFormat(0);
	  Format.FloatingPoint op1 = (Format.FloatingPoint)getOperandFormat(1);
	  Format.FloatingPoint res = (Format.FloatingPoint)getResultFormat(0);
	
	  BigNumber Nan = op0.getNaN();
	  BigNumber PosZero = op0.getZero(true);
	  BigNumber NegZero = op0.getZero(false);
	  BigNumber PosInf = op0.getInfinity(true);
	  BigNumber NegInf = op0.getInfinity(false);
	  BigNumber x = op0.getRandomValue();
	  BigNumber y = op1.getRandomValue();
	  
	  
	  Number[] ret;
	  if(run>10){run = 11;}
	  
	  switch(run) {
		//  0: NaN + NaN -> NaN
	  case 0:	ret = new Number[]{op0.getNaN(), 			op1.getNaN(), 					res.getNaN()};
	  			break;		
		//  1: NaN + x -> NaN
	  case 1:	ret = new Number[]{op0.getNaN(), 			op1.getRandomValue(), 			res.getNaN()};
	  			break;
		//  2: x + NaN -> NaN
	  case 2:	ret = new Number[]{op0.getRandomValue(), 	op1.getNaN(), 					res.getNaN()};
				break;
	    //  3: (+Inf) + (-Inf) -> NaN
	  case 3:	ret = new Number[]{op0.getInfinity(true), 	op1.getInfinity(false),			res.getNaN()};
				break;
	    //  4: (-Inf) + (+Inf) -> NaN
	  case 4:	ret = new Number[]{op0.getInfinity(false),	op1.getInfinity(true), 			res.getNaN()};
				break;
	    //  5: (+Inf) + (+Inf) -> NaN
	  case 5:	ret = new Number[]{op0.getInfinity(true), 	op1.getInfinity(true),	 		res.getNaN()};
				break;
		//  6: (-Inf) + (-Inf) -> NaN
	  case 6:	ret = new Number[]{op0.getInfinity(false), 	op1.getInfinity(false), 		res.getNaN()};
	  			break;
			  
	    //  7: (+Inf) + (x != (-Inf)) -> (+Inf)
	  case 7:	ret = new Number[]{op0.getInfinity(true),	op1.getRandomValue(),			res.getInfinity(true)};
	  			break;
		//  8: (x != (-Inf)) + (+Inf) -> (+Inf)
	  case 8:	ret = new Number[]{op0.getRandomValue(),	op1.getInfinity(true),			res.getInfinity(true)};
	  			break;
		//  9: (-Inf) + (x != (+Inf)) -> (-Inf)
	  case 9:	ret = new Number[]{op0.getInfinity(false),	op1.getRandomValue(),			res.getInfinity(false)};
	  			break;
		// 10: (x != (+Inf)) + (-Inf) -> (-Inf)
	  case 10:	ret = new Number[]{op0.getRandomValue(), 	op1.getInfinity(false),			res.getInfinity(false)};
	  			break;
	  case 11:  ret = new Number[]{x,y, apply(x,y)};
	  			break;
	  default: ret = new Number[]{op0.getNaN(), op1.getNaN(), res.getNaN()};
	  			break;
		
	  }
	  return ret;
	}
  
  /**
   * 
   */
  private int getMinFloatingPointLatency() {
	  return getInputLatency() + getOutputLatency() + 6;
  }
  
  /**
   * 
   */
  @Override
  public List<Integer> getSupportedFloatingPointLatency() {
	  int min = getMinFloatingPointLatency();
	  List<Integer> latencies = new LinkedList<Integer>(Arrays.asList(min));
	  return latencies;
  }
  
  /**
   * 
   */
  @Override
  public String getFloatingPointImplementation() {

	  StringBuilder res = new StringBuilder();
	  
	  // Hole Operandengröße & Datenpfadbreite
	  Format.FloatingPoint tmp = (Format.FloatingPoint)getOperandFormat(0);
	  int dpWidth = target.Processor.Instance.getDataPathWidth();
	  
	  // Ist Addition oder Subtraktion
	  boolean add = symbol == "+";
	  
	  // arithmetic inputs ports
	  Port opa = getOperandPort(0);
	  Port opb = getOperandPort(1);
	  
	  // Definiere Parameter und setze Exponenten-, Mantissen- und Datenpfadbreite
	  res.append("localparam	OP_EXPONENT = " + tmp.getExponentBits() + ",\n"
			+ "						OP_MANTISSA = " + tmp.getMantissaBits() + ",\n"
			+ "						OP_WIDTH = 1 + OP_EXPONENT + OP_MANTISSA,\n"
	  		+ "						DP_WIDTH = " + dpWidth + ",\n"
			+ "						CYCLE = OP_WIDTH / DP_WIDTH,\n"
			+ "						ODD = OP_WIDTH % DP_WIDTH,\n"
			+ "						POS_INF = {1'b0, {(OP_EXPONENT){1'b1}}, {(OP_MANTISSA){1'b0}}},\n"
 			+ "						NEG_INF = {1'b1, {(OP_EXPONENT){1'b1}}, {(OP_MANTISSA){1'b0}}},\n"
 			+ "						NAN = {1'b0, {(OP_EXPONENT){1'b1}}, 1'b1, {(OP_MANTISSA-1){1'b1}}};\n \n");
 			  
	  // unpack exponent for switch
	  res.append("\n"
			+ "// unpack exponent for switch \n"
			+ "reg [OP_WIDTH-1:0] full_op_a_i, full_op_b_i, next_op_a_i, next_op_b_i;\n"
			+ "wire [OP_EXPONENT-1:0] exp_a_i, exp_b_i;\n"
			+ "wire change;\n"
			+ "assign exp_a_i = full_op_a_i[OP_WIDTH-2:OP_WIDTH-OP_EXPONENT-1];\n"
			+ "assign exp_b_i = full_op_b_i[OP_WIDTH-2:OP_WIDTH-OP_EXPONENT-1];\n"
			+ "assign change = (exp_b_i >= exp_a_i) ? 0 : 1;\n");
	  
	  // switch operands & unpack operands
	  res.append("\n"
	  		+ "// switch operands & unpack operands \n"
			+ "reg [OP_WIDTH-1:0] op_a, op_b, next_op_a, next_op_b;\n"
			+ "wire sign_a, sign_b;\n"
			+ "wire [OP_EXPONENT-1:0] exp_a, exp_b;\n"
			+ "wire [OP_MANTISSA-1:0] man_a, man_b;\n"
			+ "assign sign_a = op_a[OP_WIDTH-1];\n"
			+ "assign sign_b = op_b[OP_WIDTH-1];\n"
			+ "assign exp_a = op_a[OP_WIDTH-2:OP_MANTISSA];\n"
			+ "assign exp_b = op_b[OP_WIDTH-2:OP_MANTISSA];\n"
			+ "assign man_a = op_a[OP_MANTISSA-1:0];\n"
			+ "assign man_b = op_b[OP_MANTISSA-1:0];\n");
	  
	  // extend mantissa, [sign, 2*lhs comma, mantissa, 3*digit for rounding]
	  // correct exponent
	  res.append("\n"
	  		+ "// extend mantissa, [sign, 2*lhs comma, mantissa, 3*digit for rounding] \n"
	  		+ "// correct exponent \n"
	  		+ "wire [OP_MANTISSA+5:0] ext_man_a, ext_man_b;\n"
	  		+ "wire [OP_EXPONENT:0] correct_exp_a, correct_exp_b;\n"
		  	+ "assign ext_man_a[OP_MANTISSA+2:0] = {man_a, 3'b000};\n"
		  	+ "assign ext_man_a[OP_MANTISSA+3] = (exp_a != 0) ? 1 : 0;\n"
		  	+ "assign ext_man_a[OP_MANTISSA+5:OP_MANTISSA+4] = {2'b00};\n"
		  	+ "assign ext_man_b[OP_MANTISSA+2:0] = {man_b, 3'b000};\n"
		  	+ "assign ext_man_b[OP_MANTISSA+3] = (exp_b != 0) ? 1 : 0;\n"
		  	+ "assign ext_man_b[OP_MANTISSA+5:OP_MANTISSA+4] = {2'b00};\n"
			+ "assign correct_exp_a = (exp_a == 0 && man_a != 0) ? {{(OP_EXPONENT){1'b0}}, 1'b1} : {1'b0, exp_a};\n"
			+ "assign correct_exp_b = (exp_b == 0 && man_b != 0) ? {{(OP_EXPONENT){1'b0}}, 1'b1} : {1'b0, exp_b};\n");

	  // shift a
	  res.append("\n"
	  		+ "reg [OP_MANTISSA+5:0] add_a_i, add_b_i, next_add_a_i, next_add_b_i;\n"
	  		+ "// shift operand a \n"
	  		+ "reg [OP_EXPONENT:0] k;\n"
	  		+ "reg [2*OP_MANTISSA+12-1:0] a_shift;\n"
	  		+ "always @(*) begin \n"
	  		+ "	k = correct_exp_b - correct_exp_a;\n"
	  		+ "	a_shift = {ext_man_a, {(OP_MANTISSA+6){1'b0}}} >> k;\n"
	  		+ "end \n");
	  
	  // calculate sticky-bit of operand a
	  res.append("\n"		
	  		+ "// calculate sticky-bit of operand a \n"
	  		+ "wire possible_s;\n"
	  		+ "wire s;\n"
	  		+ "wire [OP_MANTISSA+5:0] shift_man_a;\n"
	  		+ "assign possible_s = (a_shift[OP_MANTISSA+6:0] != 0);\n"
	  		+ "assign s = (k > OP_MANTISSA+4 && ext_man_a != 0) ? 1 : (k >= 3 && k <= OP_MANTISSA+4) ? possible_s : 0;\n"
	  		+ "assign shift_man_a [OP_MANTISSA+5:1] = ext_man_a[OP_MANTISSA+5:1] >> k;\n"
	  		+ "assign shift_man_a [0] = s;\n");
	  		
	  // neg mantissa
	  res.append("\n"
	  		+ "// neg mantissa \n"
	  		+ "wire [OP_MANTISSA+5:0] neg_man_a, neg_man_b;\n"
	  		+ "assign neg_man_a = -shift_man_a;\n"
	  		+ "assign neg_man_b = -ext_man_b;\n");
	  
	  // 
	  res.append("\n"
	  		+ "//  \n"
	  		+ "reg [OP_MANTISSA+5:0] add_result, next_add_result;\n"
	  		+ "reg signed [OP_EXPONENT:0] norm_exp, next_norm_exp;\n"
	  		+ "wire [OP_MANTISSA+5:0] ab_result;\n"
	  		+ "assign ab_result = add_a_i + add_b_i;\n");
	  
	  // norm
	  res.append("\n"
	  		+ "// norm \n"
	  		+ "reg sign_r, next_sign_r;\n"
	  		+ "reg [OP_MANTISSA+4:0] norm_man, next_norm_man;\n"
	  		+ "wire [OP_MANTISSA+5:0] pos_add_result; // kann eins kleiner\n"
	  		+ "assign pos_add_result = (add_result[OP_MANTISSA+5]) ? -add_result : add_result;\n"
	  		
	  		+ "// # r- & l-shifts \n"
	  		+ "wire r_shift;\n"
	  		+ "reg [10:0] l_shift;						// ToDO größe \n"
	  		+ "assign r_shift = (pos_add_result[OP_MANTISSA+4]) ? 1 : 0;\n"
	  		+ "integer j;\n"
	  		+ "always@(*) begin \n"
	  		+ "	j = 0;\n"
	  		+ "	while(j <= OP_MANTISSA+3 && pos_add_result[OP_MANTISSA+3-j] == 0) begin \n"
	  		+ "		j = j + 1;\n"
	  		+ "	end \n"
	  		+ "	l_shift = j;\n"
	  		+ "end \n"
	  		
	  		+ "// shift mantissa & calculate sticky-bit	\n"
	  		+ "wire [OP_MANTISSA+4:0] r_shift_add_result;\n"
	  		+ "wire new_s;\n"
	  		+ "wire [OP_MANTISSA+4:0] l_shift_result;\n"
	  		+ "wire signed [OP_EXPONENT:0] r_shift_exp, l_shift_exp;\n"
			+ "assign new_s = pos_add_result[1] | pos_add_result[0];\n"
	  		+ "assign r_shift_add_result[OP_MANTISSA+4:1] = pos_add_result[OP_MANTISSA+4:1] >> 1;\n"
	  		+ "assign  r_shift_add_result[0] = new_s;\n"
	  		+ "assign l_shift_result = pos_add_result[OP_MANTISSA+4:0] << l_shift;\n"
	  		+ "assign r_shift_exp = norm_exp +1;\n"
	  		+ "assign l_shift_exp = norm_exp - l_shift;\n");
	  
	  // round 			state 5
	  res.append("\n"
	  		+ "// round \n"
	  		+ "reg [OP_MANTISSA+1:0] round_man, next_round_man; \n"
	  		+ "reg signed [OP_EXPONENT:0] round_exp, next_round_exp;\n"
	  		+ "wire round;\n"
	  		+ "wire [OP_MANTISSA+1:0] round_up;\n"
	  		+ "wire need_new_norm;\n"
	  		+ "wire [OP_MANTISSA+1:0] new_norm_man;\n"
	  		+ "wire signed [OP_EXPONENT:0] new_norm_exp;\n"
	  		+ "assign round = (norm_man[2] && ((norm_man[1:0] == 2'b00 && norm_man[3]) || (norm_man[1] || norm_man[0]) )) ? 1 : 0;\n"
	  		+ "assign round_up = norm_man[OP_MANTISSA+4:3] + 1;\n"
	  		+ "assign need_new_norm = round_up[OP_MANTISSA+1];\n"
	  		+ "assign new_norm_man = round_up >> 1;\n"
	  		+ "assign new_norm_exp = norm_exp + 1;\n");
	  
	  // exceptions 		state 6
	  res.append("\n"
	  		+ "// State 6:  	// Exceptions // TODO Größen korrigierne \n"
	  		+ "reg [OP_MANTISSA+1:0] result_man, next_result_man;\n"
	  		+ "reg [OP_EXPONENT:0] result_exp, next_result_exp;\n"
	  		+ "reg [OP_EXPONENT:0] exp_max = {(OP_EXPONENT){1'b1}};\n"
	  		+ "reg [OP_MANTISSA+1:0] man_null = {(OP_MANTISSA+2){1'b0}};\n"
	  		+ "wire [OP_MANTISSA+1:0] denorm_man;\n"
	  		+ "assign denorm_man = round_man >> -round_exp + 1;\n"
	  		+ "\n"
	  		+ "reg [OP_WIDTH-1:0] result_finish, next_result_finish;\n"
	  		+ getResultPort().getAssignment("result_finish[DP_WIDTH-1:0]")
	  		+ "reg valid_o, next_valid_o;\n"
	  		+ RESULT_VALID.getAssignment("valid_o"));
	  
	  // state machine
	  res.append("\n\n\n"
	  		+ "// state machine \n"
	  		+ "reg [$clog2(CYCLE+1)+1:0] input_cycle, next_input_cycle;\n"
	  		+ "reg [3:0] state, next_state; \n"
	  		+ "localparam	READY				= 4'd0,\n"
	  		+ "						LOAD_OP			= 4'd1,\n"
	  		+ "						CHANGE 			= 4'd2,\n"
	  		+ "						SHIFT_A 		= 4'd3,\n"
	  		+ "						ADD 				= 4'd4,\n"
	  		+ "						NORM 				= 4'd5,\n"
	  		+ "						ROUND				= 4'd6,\n"
	  		+ "						EXCEPTIONS	= 4'd7,\n"
	  		+ "						FINISH 			= 4'd8;\n");
	  		
	  // synchronous
	  res.append("\n");
	  res.append(getClockedProcess(
	      // reset
	        "state <= READY;\n"
	      + "full_op_a_i <= 0;\n"
	      + "full_op_b_i <= 0;",
	     
	      // start (TODO: this can not be correct, originally, this block was guarded by EN_I)
	        "state <= next_state;\n"
        + "full_op_a_i <= next_op_a_i;\n"
        + "full_op_b_i <= next_op_b_i;\n"
        + "op_a <= next_op_a;\n"
        + "op_b <= next_op_b;\n"
        + "add_a_i <= next_add_a_i;\n"
        + "add_b_i <= next_add_b_i;\n"
        + "add_result <= next_add_result;\n"
        + "sign_r <= next_sign_r;\n"
        + "norm_exp <= next_norm_exp;\n"
        + "norm_man <= next_norm_man;\n"
        + "round_exp <= next_round_exp;\n"
        + "round_man <= next_round_man;\n"
        + "result_exp <= next_result_exp;\n"
        + "result_man <= next_result_man;\n"
        + "input_cycle <= next_input_cycle;\n"
        + "result_finish <= next_result_finish;\n"
        + "valid_o <= next_valid_o;",
        
        // remaining
        null
	  ));
	  
	  // compute next_state & outputs
	  res.append("\n"
	  		+ "always@(*) begin	\n"
	  		+ "	next_state = READY;\n"
	  		+ "	next_op_a_i = full_op_a_i;\n"
	  		+ "	next_op_b_i = full_op_b_i;\n"
	  		+ "	next_op_a = op_a;\n"
	  		+ "	next_op_b = op_b;\n"
	  		+ "	next_add_a_i = add_a_i;\n"
	  		+ "	next_add_b_i = add_b_i;\n"
	  		+ "	next_add_result = add_result;\n"
	  		+ "	next_sign_r = sign_r;\n"
	  		+ "	next_norm_exp = norm_exp;\n"
	  		+ "	next_norm_man = norm_man;\n"
	  		+ "	next_round_exp = round_exp;\n"
	  		+ "	next_round_man = round_man;\n"
	  		+ "	next_result_exp = result_exp;\n"
	  		+ "	next_result_man = result_man;\n"
	  		+ "	next_input_cycle = 0;\n"
	  		+ "	next_result_finish = 0;\n"
	  		+ "	next_valid_o = 0;\n"
	  		+ "	case(state) \n"
	  		+ "		// 0: standby, load first op-part \n"
	  		+ "		READY: begin \n"
	  		+ "			next_op_a_i = 0;\n"
	  		+ "			next_op_b_i = 0;\n"
	  		+ "			next_state = READY;\n"
	  		+ "			if(VALID_I) begin \n"
	  		+ "				next_op_a_i = " + opa + ";\n"
	  		+ "				next_op_b_i = " + opb + ";\n"
	  		+ "				next_input_cycle = input_cycle + 1;\n"
	  		+ "				if((next_input_cycle == CYCLE && ODD != 0) || next_input_cycle < CYCLE) begin \n"
	  		+ "					next_state = LOAD_OP;\n"
	  		+ "				end else begin \n"
	  		+ "					next_state = CHANGE;\n"
	  		+ "				end \n"
	  		+ "			end \n"
	  		+ "		end	\n"
	  		+ "		// 1: load upper op-parts \n"
	  		+ "		LOAD_OP: begin \n"
	  		+ "			next_op_a_i = next_op_a_i | (" + opa + " << input_cycle * DP_WIDTH);\n"
	  		+ "			next_op_b_i = next_op_b_i | (" + opb + " << input_cycle * DP_WIDTH);\n"
	  		+ "			next_input_cycle = input_cycle + 1;\n"
	  		+ "			if((next_input_cycle == CYCLE && ODD != 0) || next_input_cycle < CYCLE) begin \n"
	  		+ "				next_state = LOAD_OP;\n"
	  		+ "			end else begin \n"
	  		+ "				next_state = CHANGE;\n"
	  		+ "			end \n"
	  		+ "		end	\n"
	  		+ "		// 2: load smaller op into register for operand a \n"
	  		+ "		CHANGE: begin \n"
	  		+ "			if(change) begin \n"
	  		+ "				next_op_a = full_op_b_i;\n"
	  		+ "				next_op_b = full_op_a_i;\n"
	  		+ "			end else begin \n"
	  		+ "				next_op_a = full_op_a_i;\n"
	  		+ "				next_op_b = full_op_b_i;\n"
	  		+ "			end \n"
	  		+ "			next_state = SHIFT_A;\n"
	  		+ "		end \n"
	  		+ "		// 3: adjust a \n"
	  		+ "		SHIFT_A: begin \n"
	  		+ "			if(k <= OP_MANTISSA+2) begin \n"
	  		+ "				if(sign_a) begin \n"
	  		+ "					next_add_a_i = neg_man_a;\n"
	  		+ "				end else begin \n"
	  		+ "					next_add_a_i = shift_man_a;\n"
	  		+ "				end \n"
	  		+ "			end else begin \n"
	  		+ "				next_add_a_i[OP_MANTISSA+5:1] = {(OP_MANTISSA+5){1'd0}};\n"
	  		+ "				next_add_a_i[0] = s;\n"
	  		+ "			end	\n");
	  		
	  		// sign switch
	  		if(add) {
	  			res.append("			if(sign_b) begin \n");
	  		} else {
	  			res.append("			if(~sign_b) begin \n");
	  		}
	  		
	  		res.append(
	  		  "				next_add_b_i = neg_man_b;\n"
	  		+ "			end else begin \n"
	  		+ "				next_add_b_i = ext_man_b;\n"
	  		+ "			end \n"
	  		+ "			next_state = ADD;\n"
	  		+ "		end \n"
	  		+ "		// 4: add \n"
	  		+ "		ADD: begin \n"
	  		+ "			next_add_result = ab_result;\n"
	  		+ "			next_norm_exp = {1'b0, correct_exp_b[OP_EXPONENT-1:0]};\n"
	  		+ "			next_state = NORM;\n"
	  		+ "		end \n"
	  		+ "		// 5: norm \n"
	  		+ "		NORM: begin \n");
	  		
	  		// sign switch
	  		if(add) {res.append(
	  		  "			if(add_result[OP_MANTISSA+5]) begin \n"
			+ "				next_sign_r = 1;\n"
			+ "			end else begin \n"
			+ "				next_sign_r = 0;\n"
			+ "			end \n");
	  		// in case of sub
	  		} else {res.append(
	  		  "			if(change) begin \n"
	  		+ "				next_sign_r = ~add_result[OP_MANTISSA+5];\n"
	  		+ "			end else begin \n"
	  		+ "				next_sign_r = add_result[OP_MANTISSA+5];\n"
	  		+ "			end \n");
	  		}
	  		
	  		res.append(
	  		  "			if(r_shift) begin \n"
	  		+ "				next_norm_man = r_shift_add_result;\n"
	  		+ "				next_norm_exp = r_shift_exp;\n"
	  		+ "			end else begin \n"
	  		+ "				next_norm_man = l_shift_result;\n"
	  		+ "				next_norm_exp = l_shift_exp;\n"
	  		+ "			end	\n"
	  		+ "			next_state = ROUND;\n"
	  		+ "		end \n"
	  		+ "		// 6: round \n"
	  		+ "		ROUND: begin \n"
	  		+ "			if(round) begin \n"
	  		+ "				if(need_new_norm) begin \n"
	  		+ "					// nach runden neu normiert	\n"
	  		+ "					next_round_man = new_norm_man;\n"
	  		+ "					next_round_exp = new_norm_exp;\n"
	  		+ "				end else begin \n"
	  		+ "					// nur gerundet	\n"
	  		+ "					next_round_man = round_up;\n"
	  		+ "					next_round_exp = norm_exp;\n"
	  		+ "				end	\n"
	  		+ "			end else begin \n"
	  		+ "				// kein runden \n"
	  		+ "				next_round_man = norm_man[OP_MANTISSA+4:3];\n"
	  		+ "				next_round_exp = norm_exp;\n"
	  		+ "			end \n"
	  		+ "			next_state = EXCEPTIONS;\n"
	  		+ "		end \n"
	  		+ "		// 7: exceptions \n"
	  		+ "		EXCEPTIONS: begin \n"
	  		+ "			if(	(op_a == NAN || op_b == NAN) || \n"
	  		+ "				(	(op_a == POS_INF || op_a == NEG_INF) && \n"
	  		+ "					(op_b == POS_INF || op_b == NEG_INF))) begin \n"
	  		+ "				next_result_finish = NAN;\n"
	  		+ "			end else if (op_a == POS_INF ||op_b == POS_INF) begin \n"
	  		+ "				next_result_finish = POS_INF;\n"
	  		+ "			end else if (op_b == NEG_INF || op_b == NEG_INF) begin \n"
	  		+ "				next_result_finish = NEG_INF;\n"
	  		+ "			end else \n"
	  		+ "			if(round_exp >= 2**OP_EXPONENT - 1) begin \n"
	  		+ "				next_result_finish = sign_r ? NEG_INF : POS_INF;\n"
	  		+ "			end else if(round_exp <= 0) begin \n"
	  		+ "					next_result_man = denorm_man;\n"
	  		+ "					next_result_exp = {(OP_EXPONENT){1'd0}};\n"
	  		+ "					next_result_finish = {sign_r, {(OP_EXPONENT){1'b0}}, denorm_man[OP_MANTISSA-1:0]};\n"
	  		+ "				end else begin \n"
	  		+ "					next_result_exp = round_exp;\n"
	  		+ "					next_result_man = round_man;\n"
	  		+ "					next_result_finish = {sign_r, round_exp[OP_EXPONENT-1:0], round_man[OP_MANTISSA-1:0]};\n"
	  		+ "				end \n"
	  		+ "			next_input_cycle = 1;\n"
	  		+ "			next_state = FINISH;\n"
	  		+ "			next_valid_o = 1;\n"
	  		+ "		end \n"
	  		+ "		// 8: finish \n"
	  		+ "		FINISH: begin \n"
	  		+ "			next_result_finish = result_finish >> DP_WIDTH;\n"
	  		+ "			next_input_cycle = input_cycle + 1;\n"
	  		+ "			next_valid_o = 0;\n"
	  		+ "			if((next_input_cycle == CYCLE && ODD != 0) || next_input_cycle < CYCLE) begin \n"
	  		+ "				next_state = FINISH;\n"
	  		+ "			end else begin \n"
	  		+ "				next_state = READY;\n"
	  		+ "			end	\n"
	  		+ "		end \n"
	  		+ "	endcase \n"
	  		+ "end \n");
	  
	  return res.toString();
  }
  
}

/*
 * Copyright (c) 2016, Embedded Systems and Applications Group, Department of
 * Computer Science, TU Darmstadt, All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of the
 * institute nor the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE INSTITUTE AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE INSTITUTE OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 **********************************************************************************************************************/