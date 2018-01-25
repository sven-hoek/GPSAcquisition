
package operator;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import accuracy.BigNumber;
import accuracy.Format;
import accuracy.Range;

/**
 * Binary multiplication: R = A * B
 * In general, the multiplication can not start before the actual sign of the smaller input is available.
 * For unsigned operands, the result can be computed sequentially (starting to output the result at the first cycle), 
 * but all input words also have to be buffered as for the signed operation.
 * The current implementation just reads the whole inputs before calculating and shifting out the result.
 * TODO fully buffer inputs and outputs to just put multiplication on critical path?
 * TODO reduce latency for unsigned or unbalanced operands.
 * TODO reduce maximum width of combinatorial multiplier to achieve target frequency
 * @author Andreas Engel [engel@esa.cs.tu-darmstadt.de]
 */
public class MUL extends Binary {

  /** Unique ID for (de)marshalling */
  private static final long serialVersionUID = -6326783557895658502L;

  public MUL(Format a, Format b, Format r) {
    super(a, b, r, "*");
  }
  
  @Override 
  protected Number apply(BigNumber a, BigNumber b) {
    
    // make sure the operands can be represented using their format
    a = BigNumber.quantize(getOperandFormat(0), a);
    b = BigNumber.quantize(getOperandFormat(1), b);
    
    // sign of result
    boolean rs  = a.getSign() ^ b.getSign();
    
    // handle special symbols (after sign extraction):
    //      /  | NaN   Inf  Zero Other
    //   ------+----------------------
    //     NaN | NaN   NaN   NaN   Nan
    //     Inf | NaN   Inf   Nan   Inf
    //    Zero | NaN   NaN  Zero  Zero
    //   Other | NaN   Inf  Zero
    if  (a.isNaN() 
     ||  b.isNaN()
     || (a.isInfinite() && b.isZero())
     || (b.isInfinite() && a.isZero()))    return      BigNumber.NaN;
    if  (a.isInfinite() || b.isInfinite()) return rs ? BigNumber.NEGATIVE_INFINITY : BigNumber.POSITIVE_INFINITY;
    if  (b.isZero()     || b.isZero())     return rs ? BigNumber.NEGATIVE_ZERO     : BigNumber.POSITIVE_ZERO;
    
    // MUL as sum or partial products
    boolean[] am = a.getMantissa();
    boolean[] bm = b.getMantissa();
    boolean[] rm = new boolean[am.length + bm.length];
    for (int i = 0; i < bm.length; i++) if (bm[i]) {
      boolean cb = false;
      for (int k = i; k < rm.length; k++) {
        boolean rb = rm[k];
        boolean ab = k-i < am.length && am[k-i];
        rm[k] = rb ^ ab ^ cb;
        cb    = (ab && rb) || ( cb && (ab ^ rb));
      }
    }
    
    // no need to adjust exponent, will be done during quantization
    
    return BigNumber.quantize(getResultFormat(), new BigNumber(rs, a.getExponent() + b.getExponent(), rm));
  }
  
  /**
   * The last input cycle overlaps with the first output cycle and the actual computation (including sign conversions).
   * For unbounded I/O, this results in a single cycle combinatorial solution.
   */
  private int getMinIntegerLatency() {
    return getInputLatency() + getOutputLatency() - 1;
  }
  
  /**
   * The last input cycle overlaps with the first output cycle and the actual computation (including sign conversions).
   * For unbounded I/O, this results in a single cycle combinatorial solution.
   */
  @Override
  public List<Integer> getSupportedIntegerLatency() {
    int min = getMinIntegerLatency();
    List<Integer> latencies = new LinkedList<Integer>(Arrays.asList(min));
    //if (min == 1) latencies.add(2); // option to stretch combinatorial implementation across two cycles
    return latencies;
  }
  
  /**
   * TODO for signed * unsigned see http://www.uccs.edu/~gtumbush/published_papers/Tumbush%20DVCon%2005.pdf
   */
  @Override
  public String getIntegerImplementation() {

    // sanity check
    if (!getCommonFormat().isSigned()) throw new NotImplementedException("unsigned"); // not tested yet
    
    StringBuilder res = new StringBuilder();
    int minLatency    = getMinIntegerLatency();
    int generateCycle = getInputLatency()-1;
    boolean[]   inBuf = new boolean[getNumberOfOperands()];
    
    // result must be buffered, if it requires an output shift register or must be hold before output
    boolean    outBuf = getOutputLatency() > 1 || generateCycle < getLatency()-1;
    
    // check, if input buffer is required
    for (int i = 0; i < getNumberOfOperands(); i++) {
      int latency = ioLatency(getOperandFormat(i).getBitWidth());
      inBuf[i] = latency   > 1                 // requires input shift register 
              || latency-1 < generateCycle;    // requires buffer until read access
      if (inBuf[i]) res.append(getOperandBuffer(i, generateCycle, generateCycle));
    }
    if (outBuf) res.append(getResultBuffer(0, generateCycle));
    
    // generate product (directly into output for single cycle or into temp var)
    StringBuilder gen = new StringBuilder();
    for (int i=0; i<getNumberOfOperands(); i++) {
      Format f = getOperandFormat(i);
      if (i != 0) gen.append(" " + symbol + " ");
      if (f.isSigned()) gen.append("$signed(");
      gen.append(getOperandPort(i));
      if (inBuf[i]) gen.append("_buffered");
      if (f.isSigned()) gen.append(")");
    }
    
    if (outBuf) res.append("assign " + getResultPort() + "_generated = " + gen + ";\n");
    else        res.append(getResultPort().getAssignment(gen.toString()));
    
    if (outBuf) res.append(RESULT_VALID.getAssignment("cycle > " + (getLatency()-getOutputLatency()) + " ? 1 : 0"));
    else        res.append(RESULT_VALID.getAssignment(START));
    
    return res.toString();
  }

  @Override
  public Number apply(long a, long b) {
    if (getResultFormat().getBitWidth() <= 64) return a*b;
    return apply(new BigDecimal(a).toBigInteger(), new BigDecimal(b).toBigInteger());
  }

  @Override
  public Number apply(double a, double b) {
    if (getResultFormat().getBitWidth() <= 64) return a*b;
    return apply(new BigDecimal(a), new BigDecimal(b));
  }


  @Override
  public Number apply(BigInteger a, BigInteger b) {
    return a.multiply(b);
  }

  @Override
  public Number apply(BigDecimal a, BigDecimal b) {
    return a.multiply(b);
  }
  
  /**
   * According to Stolfi2003 (SelfValidated Numerical Methods and Applications):
   * <code>
   *   A*B = a0*b0 + sum_{i>0} (a0*bi + ai*b0) uncertainty_i + (sum_{i>0} |ai|)*(sum_{i>0} |bi|) uncertainty_{i+1}
   * </code>
   */
  @Override
  public Range getResultRange(Range.AA a, Range.AA b) {
    
    Range.AA res = new Range.AA(a.getBase() * b.getBase());
    for (Object tag : Range.AA.combineTags(a, b)) {
      res.setUncertainty(tag, a.getUncertainty(tag) * b.getBase() + a.getBase() * b.getUncertainty(tag));
    }
    res.setUncertainty(null, a.getMaxUncertainty() * b.getMaxUncertainty());
    return res;
  }
  
  @Override
  protected Range getResultRange(Range.IA a, Range.IA b) {
    // see Stolfi1997 page 26
    
    if (a.isConstant(0) || b.isConstant(0)) return Range.generate(0);
    
    if (a.isNonNegative()) {        // a.hi >= a.lo >= 0
      if (b.isNonNegative()) {      // b.hi >= b.lo >= 0
        return Range.generate(                                          a.lo().multiply(b.lo(), Range.FLOOR), 
                              a.hi() == null || b.hi() == null ? null : a.hi().multiply(b.hi(), Range.CEILING));
      } if (b.isNonPositive()) {    // b.lo <= b.hi <= 0
        return Range.generate(a.hi() == null || b.lo() == null ? null : a.hi().multiply(b.lo(), Range.FLOOR),
                                                                        a.lo().multiply(b.hi(), Range.CEILING));
      } else {                      // b.lo < 0 && b.hi > 0  (!! swapped min<->max compared to Stolfi1997 !!)
        return Range.generate(a.hi() == null || b.lo() == null ? null : a.hi().multiply(b.lo(), Range.FLOOR),
                              a.hi() == null || b.hi() == null ? null : a.hi().multiply(b.hi(), Range.CEILING));
      }
    } else if (a.isNonPositive()) { // a.lo <= a.hi <= 0
      if (b.isNonNegative()) {      // b.hi >= b.lo >= 0
        return Range.generate(a.lo() == null || b.hi() == null ? null : a.lo().multiply(b.hi(), Range.FLOOR), 
                                                                        a.hi().multiply(b.lo(), Range.CEILING));
      } if (b.isNonPositive()) {    // b.lo <= b.hi <= 0
        return Range.generate(                                          a.hi().multiply(b.hi(), Range.FLOOR),
                              a.lo() == null || b.lo() == null ? null : a.lo().multiply(b.lo(), Range.CEILING));
      } else {                      // b.lo < 0 && b.hi > 0
        return Range.generate(a.lo() == null || b.hi() == null ? null : a.lo().multiply(b.hi(), Range.FLOOR),
                              a.lo() == null || b.lo() == null ? null : a.lo().multiply(b.lo(), Range.CEILING));
      }
    } else {                        // a.lo < 0 && a.hi > 0
      if (b.isNonNegative()) {      // b.hi >= b.lo >= 0
        return Range.generate(a.lo() == null || b.hi() == null ? null : a.lo().multiply(b.hi(), Range.FLOOR), 
                              a.hi() == null || b.hi() == null ? null : a.hi().multiply(b.hi(), Range.CEILING));
      } if (b.isNonPositive()) {    // b.lo <= b.hi <= 0
        return Range.generate(a.hi() == null || b.lo() == null ? null : a.hi().multiply(b.lo(), Range.FLOOR),
                              a.lo() == null                   ? null : a.lo().multiply(b.hi(), Range.CEILING));
      } else {                      // b.lo < 0 && b.hi > 0
        if (a.lo() == null || a.hi() == null || b.lo() == null || b.hi() == null) return Range.UNBOUNDED;
        return Range.generate(a.lo().multiply(b.hi(), Range.FLOOR)  .min(a.hi().multiply(b.lo(), Range.FLOOR)),
                              a.lo().multiply(b.lo(), Range.CEILING).max(a.hi().multiply(b.hi(), Range.CEILING)));
      }
    }
  }

  @Override
  protected int getTestRuns() {
      return 5000;
    }
  
  @Override
  protected Number[] getTestVector(int run) {
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
    if(run>18){run = 19;}
    // Ergebnis in Java berechnen, apply muss erweitert werden, hier Operanden gebenfalls schon testen
      switch (run) {
              //  0: NaN * NaN -> NaN
          case 0:    ret = new Number[]{op0.getNaN(),       op1.getNaN(),         res.getNaN()};
                break;
                //  1: NaN * x -> NaN
          case 1:  ret = new Number[]{op0.getNaN(),       op1.getRandomValue(),     res.getNaN()};
                break;
                //  2: x * NaN -> NaN
          case 2:  ret = new Number[]{op0.getRandomValue(),   op1.getNaN(),         res.getNaN()};
                break;
            //  3: (+0) * (+Inf) -> NaN
          case 3:  ret = new Number[]{op0.getZero(true),     op1.getInfinity(true),       res.getNaN()};
                break;
            //  4: (-0) * (+Inf) -> NaN
          case 4:  ret = new Number[]{op0.getZero(false),     op1.getInfinity(true),       res.getNaN()};
          break;
            //  5: (+0) * (-Inf) -> NaN
          case 5:  ret = new Number[]{op0.getZero(true),     op1.getInfinity(false),     res.getNaN()};
          break;
          //  6: (-0) * (-Inf) -> NaN
          case 6:  ret = new Number[]{op0.getZero(false),     op1.getInfinity(false),     res.getNaN()};
          break;
            //  7: (+Inf) * (+0) -> NaN
          case 7:  ret = new Number[]{op0.getInfinity(true),  op1.getZero(true),        res.getNaN()};
          break;
          //  8: (+Inf) * (-0) -> NaN
          case 8:  ret = new Number[]{op0.getInfinity(true),  op1.getZero(false),        res.getNaN()};
          break;
          //  9: (-Inf) * (+0) -> NaN
          case 9:  ret = new Number[]{op0.getInfinity(false),  op1.getZero(true),        res.getNaN()};
          break;
          // 10: (-Inf) * (-0) -> NaN
          case 10:  ret = new Number[]{op0.getInfinity(false),   op1.getZero(false),        res.getNaN()};
          break;
          // 11: (+Inf) * x(!=0) -> (+Inf)
          case 11:  ret = new Number[]{op0.getInfinity(true),  op1.getRandomValue(),      res.getInfinity(true)};
          break;
          // 12: (-Inf) * x(!=0) -> (-Inf)
          case 12:  ret = new Number[]{op0.getInfinity(false),  op1.getRandomValue(),      res.getInfinity(false)};
          break;
          // 13: x(!=0) * (+Inf) -> (+Inf)
          case 13:  ret = new Number[]{op0.getRandomValue(),  op1.getInfinity(true),      res.getInfinity(true)};
          break;
          // 14: x(!=0) * (-Inf) -> (-Inf)
          case 14:  ret = new Number[]{op0.getRandomValue(),  op1.getInfinity(false),      res.getInfinity(false)};
          break;
          // 15: (+Inf) * (+Inf) -> (+Inf)
          case 15:  ret = new Number[]{op0.getInfinity(true),   op1.getInfinity(true),      res.getInfinity(true)};
          break;
          // 16: (+Inf) * (-Inf) -> (-Inf)
          case 16:  ret = new Number[]{op0.getInfinity(true),  op1.getInfinity(false),      res.getInfinity(false)};
          break;
          // 17: (-Inf) * (+Inf) -> (-Inf)
          case 17:  ret = new Number[]{op0.getInfinity(false),  op1.getInfinity(true),      res.getInfinity(false)};
          break;
          // 18: (-Inf) * (-Inf) -> (+Inf)
          case 18:  ret = new Number[]{op0.getInfinity(false),  op1.getInfinity(false),      res.getInfinity(true)};
          break;
          // weitere
          case 19:  ret = new Number[]{x,   y,      apply(x,y)};
                break;
          // default
          default:  ret = new Number[]{op0.getNaN(),       op1.getNaN(),           res.getNaN()};
                break;
      }
      
    return ret;
  }
  
  /**
   * Returns latency of implementation inclusive all cycles to load the OP´s and shift the result through the output
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
    
    // Definiere Parameter und setze Exponenten-, Mantissen- und Datenpfadbreite
    res.append("localparam  OP_EXPONENT = " + tmp.getExponentBits() + ",\n"
             + "            OP_MANTISSA = " + tmp.getMantissaBits() + ",\n"
             + "            OP_WIDTH = 1 + OP_EXPONENT + OP_MANTISSA,\n"
             + "            DP_WIDTH = " + dpWidth  + ",\n"
             + "            CYCLE = OP_WIDTH / DP_WIDTH,\n"
             + "            ODD = OP_WIDTH % DP_WIDTH,\n"
             + "            EXP_MAX = 2**OP_EXPONENT - 1,\n"
             + "            POS_ZERO = {1'b0, {(OP_WIDTH-1){1'b0}}},\n"
             + "            NEG_ZERO = {1'b1, {(OP_WIDTH-1){1'b0}}},\n"
             + "            POS_INF = {1'b0, {(OP_EXPONENT){1'b1}}, {(OP_MANTISSA){1'b0}}},\n"
             + "            NEG_INF = {1'b1, {(OP_EXPONENT){1'b1}}, {(OP_MANTISSA){1'b0}}},\n"
             + "            NAN = {1'b0, {(OP_EXPONENT){1'b1}}, 1'b1, {(OP_MANTISSA-1){1'b1}}};\n \n"
           
        + "localparam [OP_EXPONENT:0] EXP_BIAS = (2**OP_EXPONENT) / 2 - 1;\n");
   
    // unpack operands
    res.append("\n"
        + "// unpack operands \n"
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
    
    // extend mantissa, compute sign
    res.append("\n"
        + "// extend mantissa, compute sign \n"
      + "wire sign_r;\n"
        + "wire [OP_MANTISSA:0] ext_man_a, ext_man_b;\n"
        + "assign sign_r = sign_a ^ sign_b;\n"
        + "assign ext_man_a[OP_MANTISSA-1:0] = man_a;\n"
        + "assign ext_man_a[OP_MANTISSA] = (exp_a != 0) ? 1 : 0;\n"
        + "assign ext_man_b[OP_MANTISSA-1:0] = man_b;\n"
        + "assign ext_man_b[OP_MANTISSA] = (exp_b != 0) ? 1 : 0;\n");
    
    // multiply mantissa
    res.append("\n"
        + "// multiply mantissa \n"
        + "reg [OP_MANTISSA:0] mult_a_i, mult_b_i, next_mult_a_i, next_mult_b_i;\n"
        + "reg [2*OP_MANTISSA+1:0] mult_man, next_mult_man;\n"
        + "wire [2*OP_MANTISSA+1:0] mult_man_o;\n"
        + "assign mult_man_o = mult_a_i * mult_b_i;\n");
    
    // correct exponents, compute exponent
    res.append("\n"
        + "// correct exponents \n"
        + "wire [OP_EXPONENT-1:0] correct_exp_a, correct_exp_b;\n"
        + "assign correct_exp_a = (exp_a == 0 && man_a != 0) ? {{(OP_EXPONENT-1){1'b0}}, 1'b1} : exp_a;\n"
        + "assign correct_exp_b = (exp_b == 0 && man_b != 0) ? {{(OP_EXPONENT-1){1'b0}}, 1'b1} : exp_b;\n"
        + "// compute exponent \n"
        + "reg signed [OP_EXPONENT-1:0] add_a_i, add_b_i, next_add_a_i, next_add_b_i;\n"
        + "reg signed [OP_EXPONENT+1:0] add_exp, next_add_exp;\n"
        + "wire signed [OP_EXPONENT+1:0] add_exp_o;\n"
        + "assign add_exp_o = add_a_i + add_b_i - EXP_BIAS;\n");
//+ "//ADD #(.OP_EXPONENT(OP_EXPONENT)) addExp (CLK_I, add_a_i, add_b_i, add_exp_o);\n"
    
    // norm, compute r- & i*l-shift, for mantissa & exponent
    res.append("\n"
        + "// norm, compute r- & i*l-shift, for mantissa & exponent \n"
        + "wire r_shift; \n"
        + "wire [2*OP_MANTISSA+1:0] r_shift_man, l_shift_man;\n"
        + "wire signed [OP_EXPONENT+1:0] r_shift_exp, l_shift_exp;\n"
        + "assign r_shift = (mult_man[2*OP_MANTISSA+1]) ? 1 : 0;\n"
        + "assign r_shift_man = mult_man >> 1;\n"
        + "assign l_shift_man = mult_man << l_shift;\n"
        + "assign r_shift_exp = add_exp + 1;\n"
        + "assign l_shift_exp = add_exp - l_shift;\n"
        + "reg [$clog2(2*OP_MANTISSA)-1:0] l_shift;\n"
        + "integer i;\n"
        + "always@(*) begin\n"
        + "  i = 0;\n"
        + "  while (i <= 2*OP_MANTISSA && mult_man[2*OP_MANTISSA-i] == 0) begin\n"
        + "    i = i + 1;\n"
        + "  end\n"
        + "  l_shift = i;\n"
        + "end\n");        
    
    // handle underflow -> denorm
    res.append("\n"
        + "// handle underflow -> denorm \n"
        + "wire underflow;\n"
        + "wire [2*OP_MANTISSA+1:0] underflow_shift_man;\n"
        + "wire signed [OP_EXPONENT+1:0] underflow_exp;\n"
        + "assign underflow = (add_exp <= 0) ? 1 : 0;\n"
        + "assign underflow_shift_man = mult_man >> -add_exp + 1;\n"
        + "assign underflow_exp = {(OP_EXPONENT+2){1'd0}};\n");
    
    // round
    res.append("\n"
        + "// round \n"
        + "wire round;\n"
        + "wire s;\n"
        + "wire [OP_MANTISSA+1:0] man_plus_1;\n"
        + "assign round = ((mult_man[OP_MANTISSA-1] && s) || (mult_man[OP_MANTISSA] && mult_man[OP_MANTISSA-1])) ? 1 : 0;\n"
        + "assign s = (mult_man[OP_MANTISSA-2:0] > 0) ? 1 : 0;\n"
        + "assign man_plus_1 = mult_man[2*OP_MANTISSA+1:OP_MANTISSA] + 1;\n");
    

    // handle overflow & hold result
    // use r_shift wires from above
    res.append("\n"
        + "// handle overflow & hold result \n"
        + "// use r_shift wires from above \n"
        + "wire signed [OP_EXPONENT+1:0] exp_max = EXP_MAX;\n"
        + "wire signed [OP_MANTISSA-1:0] man_null = {(OP_MANTISSA){1'd0}};\n"
        + "wire signed shift_overflow = (r_shift_exp >= EXP_MAX) ? 1 : 0;\n"
        + "wire overflow = (add_exp >= EXP_MAX) ? 1 : 0;\n"
        + "\n"
        + "reg [OP_WIDTH-1:0] result_finish, next_result_finish;\n"
        + getResultPort().getAssignment("result_finish[DP_WIDTH-1:0]")
        + "reg valid_o, next_valid_o;\n"
        + RESULT_VALID.getAssignment("valid_o"));
    
    // state machine
    res.append("\n\n\n"
        + "// state machine \n"
        + "reg [$clog2(CYCLE+1)+1:0] input_cycle, next_input_cycle;\n"
        + "reg [3:0] state, next_state;\n"
        + "localparam  READY        = 4'd0,\n"
        + "            LOAD_OP      = 4'd1,\n"
        + "            UNPACK      = 4'd2,\n"
        + "            MUL_ADD      = 4'd3,\n"
        + "            NORM_DENORM  = 4'd4,\n"
        + "            UNDERFLOW    = 4'd5,\n"
        + "            ROUND        = 4'd6,\n"
        + "            OVERFLOW    = 4'd7,\n"
        + "            FINISH      = 4'd8;\n");
        
      // synchronous  
      res.append("\n" + getClockedProcess(
          // reset
          "state <= READY;\n"
        + "op_a <= 0;\n"
        + "op_b <= 0;",
        
          // start (TODO: this can not be correct, originally, this block was guarded by ENABLE_I)
          "state <= next_state;\n"
        + "op_a <= next_op_a;\n"
        + "op_b <= next_op_b;\n"
        + "mult_a_i <= next_mult_a_i;\n"
        + "mult_b_i <= next_mult_b_i;\n"
        + "add_a_i <= next_add_a_i;\n"
        + "add_b_i <= next_add_b_i;\n"
        + "mult_man <= next_mult_man;\n"
        + "add_exp <= next_add_exp;\n"
        + "input_cycle <= next_input_cycle;\n"
        + "result_finish <= next_result_finish;\n"
        + "valid_o <= next_valid_o;",
        
          // remaining
          null
    ));
    
    // compute next_state & outputs
    res.append("\n"
        + "always @(*) begin \n"
        + "  next_state = READY;\n"
        + "  next_op_a = op_a;\n"
        + "  next_op_b = op_b;\n"
        + "  next_mult_a_i = mult_a_i;\n"
        + "  next_mult_b_i = mult_b_i;\n"
        + "  next_mult_man = mult_man;\n"
        + "  next_add_a_i = add_a_i;\n"
        + "  next_add_b_i = add_b_i;\n"
        + "  next_add_exp = add_exp;\n"
        + "  next_input_cycle = 0;\n"
        + "  next_result_finish = 0;\n"
        + "  next_valid_o = 0;\n"
        + "  case(state) \n"
        + "    // 0: standby, load first op-part \n"
        + "    READY: begin \n"
        + "      next_op_a = 0;\n"
        + "      next_op_b = 0;\n"
        + "      next_state = READY;\n"
        + "      if(VALID_I) begin \n"
        + "        next_op_a = " + getOperandPort(0) + ";\n"
        + "        next_op_b = " + getOperandPort(1) + ";\n"
        + "        next_input_cycle = input_cycle + 1;\n"
        + "        if(next_input_cycle == CYCLE && ODD != 0 || next_input_cycle < CYCLE) begin \n"
        + "          next_state = LOAD_OP;\n"
        + "        end else begin \n"
        + "          next_state = UNPACK;\n"
        + "        end \n"
        + "      end \n"
        + "    end \n"
        + "    // 1: load upper op-parts \n"
        + "    LOAD_OP: begin \n"
        + "      next_op_a = next_op_a | (" + getOperandPort(0) + " << input_cycle * DP_WIDTH);\n"
        + "      next_op_b = next_op_b | (" + getOperandPort(1) + " << input_cycle * DP_WIDTH);\n"
        + "      next_input_cycle = input_cycle + 1;\n"
        + "      if(next_input_cycle == CYCLE && ODD != 0 || next_input_cycle < CYCLE) begin \n"
        + "        next_state = LOAD_OP;\n"
        + "      end else begin \n"
        + "        next_state = UNPACK;\n"
        + "      end \n"
        + "    end \n"
        + "    // 2: unpack & correct \n"
        + "    UNPACK: begin \n"
        + "      next_mult_a_i = ext_man_a;\n"
        + "      next_mult_b_i = ext_man_b;\n"
        + "      next_add_a_i = correct_exp_a;\n"
        + "      next_add_b_i = correct_exp_b;\n"
        + "      next_state = MUL_ADD;\n"
        + "    end \n"
        + "    // 3: multiply \n"
        + "    MUL_ADD: begin \n"
        + "      next_mult_man = mult_man_o;\n"
        + "      next_add_exp = add_exp_o;\n"
        + "      next_state = NORM_DENORM;\n"
        + "    end \n"
        + "    // 4: norm / denorm \n"
        + "    NORM_DENORM: begin \n"
        + "      if(r_shift) begin \n"
        + "        next_mult_man = r_shift_man;\n"
        + "        next_add_exp = r_shift_exp;\n"
        + "      end else begin \n"
        + "        next_mult_man = l_shift_man;\n"
        + "        next_add_exp = l_shift_exp;\n"
        + "      end \n"
        + "      next_state = UNDERFLOW;\n"
        + "    end \n"
        + "    // 5: handle underflow \n"
        + "    UNDERFLOW: begin \n"
        + "      if(underflow) begin \n"
        + "        next_mult_man = underflow_shift_man;\n"
        + "        next_add_exp = underflow_exp;\n"
        + "      end else begin \n"
        + "        next_mult_man = mult_man;\n"
        + "        next_add_exp = add_exp;\n"
        + "      end \n"
        + "      next_state = ROUND;\n"
        + "    end \n"
        + "    // 6: round \n"
        + "    ROUND: begin \n"
        + "      if(round) begin \n"
        + "        next_mult_man[2*OP_MANTISSA+1:OP_MANTISSA] = man_plus_1;\n"
        + "      end \n"
        + "      next_state = OVERFLOW;\n"
        + "    end \n"
        + "    // 7: handle overflow \n"
        + "    OVERFLOW: begin \n"
        + "      if( (op_a == NAN || op_b == NAN) || \n"
        + "          (op_a == POS_ZERO || op_a == NEG_ZERO) && (op_b == POS_INF || op_b == NEG_INF) || \n"
        + "          (op_a == POS_INF || op_a == NEG_INF) && (op_b == POS_ZERO || op_b == NEG_ZERO)) begin \n"
        + "        // Ergebnis ist NaN \n"
        + "        next_result_finish = NAN;\n"
        + "      end else if ( (op_a == POS_INF || op_a == NEG_INF) && \n"
        + "          (op_b == POS_INF || op_b == NEG_INF)) begin \n"
        + "        next_result_finish = sign_r ? NEG_INF : POS_INF;\n"
        + "      end else if ( (op_a == NEG_INF && op_b[OP_WIDTH-2:0] != 0) || \n"
        + "          (op_a[OP_WIDTH-2:0] != 0 && op_b == NEG_INF)) begin \n"
        + "        // Ergebnis ist NEG_INF \n"
        + "        next_result_finish = NEG_INF;\n"
        + "      end else if ( (op_a == POS_INF && op_b[OP_WIDTH-2:0] != 0) || \n"
        + "          (op_a[OP_WIDTH-2:0] != 0 && op_b == POS_INF)) begin \n "
        + "        // Ergebnis ist POS_INF \n"
        + "        next_result_finish = POS_INF;\n"
        + "      end else if(r_shift) begin \n"
        + "        // overflow after r-shift \n"
        + "        if(shift_overflow) begin \n"
        + "          next_result_finish = sign_r ? NEG_INF : POS_INF;\n"
        + "        // just r-shift \n"
        + "        end else begin \n"
        + "          next_add_exp = r_shift_exp;\n"
        + "          next_mult_man = r_shift_man;\n"
        + "          next_result_finish = {sign_r, r_shift_exp[OP_EXPONENT-1:0], r_shift_man[2*OP_MANTISSA-1:OP_MANTISSA]};\n"
        + "        end \n"
        + "      end  else begin \n"
        + "        // no shift, handle overflow \n"
        + "        if(overflow) begin \n"
        + "          next_result_finish = sign_r ? NEG_INF : POS_INF;\n"
        + "        end else begin \n"
        + "          // no shift, no overflow \n"
        + "          next_result_finish = {sign_r, add_exp[OP_EXPONENT-1:0], mult_man[2*OP_MANTISSA-1:OP_MANTISSA]};\n"
        + "        end \n"
        + "      end \n"
        + "      if(op_a == NAN || op_b == NAN) begin \n"
        + "        next_result_finish = NAN;\n"
        + "      end \n"
        + "      next_input_cycle = 1;\n"
        + "      next_state = FINISH;\n"
        + "      next_valid_o = 1;\n"
        + "    end \n"
        + "    // 8: shift result throug output \n"
        + "    FINISH: begin \n"
        + "      next_result_finish = result_finish >> DP_WIDTH;\n"
        + "      next_input_cycle = input_cycle + 1;\n"
        + "      next_valid_o = 0;\n"
        + "      if(next_input_cycle == CYCLE && ODD != 0 || next_input_cycle < CYCLE) begin \n"
        + "        next_state = FINISH;\n"
        + "      end else begin \n"
        + "        next_state = READY;\n"
        + "      end \n"
        + "    end \n"
        + "  endcase \n"
        + "end \n");
    
    return res.toString();
  }
  

  public static void main(String[] args) {
    double[] list = new double[] {Double.NaN,Double.POSITIVE_INFINITY,Double.NEGATIVE_INFINITY,+0.0,-0.0,1.0,-1.0};
    for (double a : list) for (double b : list) System.out.println(a + " * " + b + " = " + (a*b));
  }
  
}


/*
 * Copyright (c) 2016,
 * Embedded Systems and Applications Group,
 * Department of Computer Science,
 * TU Darmstadt,
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 *    disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the institute nor the names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE INSTITUTE AND CONTRIBUTORS ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE INSTITUTE OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 **********************************************************************************************************************/