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
 * Binary division: R = A / B For integer arithmetic, this is the devision with
 * truncation towards zero.
 *
 * @author Andreas Engel [engel@esa.cs.tu-darmstadt.de]
 */
public class DIV extends DivRem {

  /** Unique ID for (de)marshalling */
  private static final long serialVersionUID = -3424307883530140861L;

  public DIV(Format a, Format b, Format r) {
    super(a, b, r);
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
    //     Inf | NaN   NaN   Inf   Inf
    //    Zero | NaN  Zero   NaN  Zero
    //   Other | NaN  Zero   Inf
    if  (a.isNaN() 
     ||  b.isNaN()
     || (a.isInfinite() && b.isInfinite())
     || (a.isZero()     && b.isZero()))   return      BigNumber.NaN;
    if  (a.isInfinite() || b.isZero())    return rs ? BigNumber.NEGATIVE_INFINITY : BigNumber.POSITIVE_INFINITY;
    if  (b.isInfinite() || a.isZero())    return rs ? BigNumber.NEGATIVE_ZERO     : BigNumber.POSITIVE_ZERO;
    
    // Analyze required result accuracy
    int     re = a.getExponent() - b.getExponent();     // result exponent without mantissa adjustment
    Format  rf = getResultFormat();
    int     rb = rf instanceof Format.Integer ? 0 : 20; // additional bits for proper rounding (why is 3 not sufficient?)
    Integer mb = null;                                  // required mantissa bits for floating point result
    int     lsb;                                        // weight of least significant mantissa bit
    if (rf instanceof Format.FloatingPoint) {
      Format.FloatingPoint rrf = (Format.FloatingPoint) rf;
      mb  =  rrf.getMantissaBits()+1 + rb;
      lsb = 1-rrf.getBias()-mb;
    } else {
      lsb = -((Format.FixedPoint) rf).getFractionBits() - rb;
    }
    
    // operand and result mantissa as modifiable list to support shifting
    LinkedList<Boolean> am = new LinkedList<Boolean>(); for (boolean bit : a.getMantissa()) am.add(bit);
    LinkedList<Boolean> bm = new LinkedList<Boolean>(); for (boolean bit : b.getMantissa()) bm.add(bit);
    LinkedList<Boolean> rm = new LinkedList<Boolean>();
    
    // left shift bm to meet weight of most significant result bit
    re += am.size();
    for (int i=0; i<am.size()-1; i++) bm.add(0, false);
    
    // non-performing sequential division
    while (re > lsb) {
      // diff = am - bm
      LinkedList<Boolean> diff = new LinkedList<Boolean>();
      boolean cb = false;
      for (int i=0; i<Math.max(am.size(), bm.size()); i++) {
        boolean ab = i < am.size() && am.get(i);
        boolean bb = i < bm.size() && bm.get(i);
        diff.add(ab ^ bb ^ cb);
        cb = (cb && bb) || (!ab && (cb ^ bb));
      }
      
      // diff >= 0 => set result bit and keep diff as remainder 
      re--;
      if (!cb) {
        rm.add(0,true);
        am = diff;
        // remove leading zeros
        while (!am.isEmpty() && !am.getLast()) am.removeLast();
        
        // remainder is zero => division done
        if (am.isEmpty()) break;
      
      // diff < 0 => prepend zero, if leading one already set
      } else if (!rm.isEmpty()) {
        rm.add(0, false);
      }
      
      // enough bits collected?
      if (mb != null && rm.size() == mb) break;
      
      // prepare next cycle
      am.add(0, false);
    }
    
    // extract result mantissa
    boolean[] res = new boolean[rm.size()];
    for (int i=0; i<res.length; i++) res[i] = rm.removeFirst();
    
    return BigNumber.quantize(rf, new BigNumber(rs, re, res));
  }
  
  /**
   * Select the quotient as result from the division operation
   */
  @Override
  protected String getResultSelection(boolean outBuf, String denominator) {
    Format af = getOperandFormat(0);
    Format bf = getOperandFormat(1);
    boolean inputBuffered = getInputLatency() > 1;
    StringBuilder res = new StringBuilder();
    
    // NewtonRaphson needs some extra correction before restoring the sign
    if (getAlgorithm() == Algorithm.NewtonRaphson) {
      res.append("wire " + bitRange(getResultFormat().getBitWidth()) + " quotient = ");
      res.append("rem >= 2*" + denominator + " ? quot+2 : ");
      res.append("rem >= "   + denominator + " ? quot+1 : quot;\n");
    }
    
    StringBuilder gen = new StringBuilder();
    switch (getAlgorithm()) {
      case Combinatorial : 
        for (int i=0; i<getNumberOfOperands(); i++) {
          Format f = getOperandFormat(i);
          if (i != 0) gen.append(" / ");
          if (f.isSigned()) gen.append("$signed(");
          gen.append(getOperandPort(i));
          if (inputBuffered) gen.append("_buffered");
          if (f.isSigned()) gen.append(")");
        }
        break;
        
      case Restoring:
      case NewtonRaphson:
        if (af.isSigned())                  gen.append(getOperandPort(0) + "_sign");
        if (af.isSigned() && bf.isSigned()) gen.append(" ^ ");
        if (bf.isSigned())                  gen.append(getOperandPort(1) + "_sign");
        if (af.isSigned() || bf.isSigned()) gen.append(" ? -quotient : ");
        gen.append("quotient");
        break;
      
      default: throw new NotImplementedException("algorithm="+getAlgorithm()); 
    }
    
    if (outBuf) res.append("assign " + getResultPort() + "_generated = " + gen + ";\n");
    else        res.append(getResultPort().getAssignment(gen.toString()));
    
    return res.toString();
  }
  
  @Override
  public Number apply(long a, long b) {
    return a / b;
  }

  @Override
  public Number apply(double a, double b) {
    return a / b;
  }

  @Override
  public Number apply(BigInteger a, BigInteger b) {
    return a.divide(b);
  }

  @Override
  public Number apply(BigDecimal a, BigDecimal b) {
    return a.divide(b);
  }
  
  /**
   * According to Stolfi2003 (SelfValidated Numerical Methods and Applications):
   */
  @Override
  public Range getResultRange(Range.AA a, Range.AA b) {
    
    // fall back to interval arithmetic, if b covers zero
    if (b.contains(0)) return getResultRange(a.toIA(), b.toIA());
    
    // TODO
    return Range.UNBOUNDED;
  }
  
  @Override
  protected Range getResultRange(Range.IA a, Range.IA b) {
    // see Stolfi1997 page 28
    
    if (b.isConstant(0) || a.isEmpty() || b.isEmpty()) return Range.EMPTY;
    if (a.isConstant(0))                               return Range.generate(0);
    
    if (b.isNonNegative()) {          // b.hi >= b.lo >= 0
      boolean blz = b.lo().equals(BigDecimal.ZERO);
      if (a.isNonNegative()) {        // a.hi >= a.lo >= 0
        return Range.generate(b.hi() == null        ? BigDecimal.ZERO : a.lo().divide(b.hi(), Range.FLOOR),
                              a.hi() == null || blz ? null            : a.hi().divide(b.lo(), Range.CEILING));
      } else if (a.isNonPositive()) { // a.lo <= a.hi <= 0
        return Range.generate(a.lo() == null || blz ? null            : a.lo().divide(b.lo(), Range.FLOOR),
                              b.hi() == null        ? BigDecimal.ZERO : a.hi().divide(b.hi(), Range.CEILING));
      } else {                        // a.lo < 0 && a.hi > 0
        return Range.generate(a.lo() == null || blz ? null            : a.lo().divide(b.lo(), Range.FLOOR),
                              a.hi() == null || blz ? null            : a.hi().divide(b.lo(), Range.CEILING));
      }
    } else if (b.isNonPositive()) {   // b.lo <= b.hi <= 0
      boolean bhz = b.hi().equals(BigDecimal.ZERO);
      if (a.isNonNegative()) {        // a.hi >= a.lo >= 0
        return Range.generate(a.hi() == null || bhz ? null            : a.hi().divide(b.hi(), Range.FLOOR),
                              b.lo() == null        ? BigDecimal.ZERO : a.lo().divide(b.lo(), Range.CEILING));
      } else if (a.isNonPositive()) { // a.lo <= a.hi <= 0
        return Range.generate(b.lo() == null        ? BigDecimal.ZERO : a.hi().divide(b.lo(), Range.FLOOR),
                              a.lo() == null || bhz ? null            : a.lo().divide(b.hi(), Range.CEILING));
      } else {                        // a.lo < 0 && a.hi > 0
        return Range.generate(a.hi() == null || bhz ? null            : a.hi().divide(b.hi(), Range.FLOOR),
                              a.lo() == null || bhz ? null            : a.lo().divide(b.hi(), Range.CEILING));
      }
    } else {
      return Range.UNBOUNDED;
    }
  }
  
  
 //TODO Lars

  @Override
  protected int getTestRuns() {
      return 1000;
    }
  @Override 
  protected List<Integer> getSupportedFloatingPointLatency() {
    return new LinkedList<Integer>(Arrays.asList(getInputLatency()+36+getOutputLatency()));
  }
  @Override
  public String getFloatingPointImplementation() {
    
    StringBuilder res = new StringBuilder();
   
    Format.FloatingPoint tmp = (Format.FloatingPoint)getOperandFormat(0);
    int dpWidth = target.Processor.Instance.getDataPathWidth();
   
    //Module
    //MAIN
    res.append("localparam OP_EXPONENT = " + tmp.getExponentBits() + ",\n"
             + "          OP_MANTISSA = " + tmp.getMantissaBits() + ",\n"
             + "          DP_WIDTH = " + dpWidth + ",\n"
             + "          IT=11, \n"
             + "          OP_WIDTH   = 1 + OP_EXPONENT + OP_MANTISSA,\n"
             + "          CYCLE     = OP_WIDTH / DP_WIDTH,\n"
             + "          ODD      = OP_WIDTH % DP_WIDTH,\n"
             + "          EXP_MAX    = {(OP_EXPONENT){1'b1}},\n"
             + "          MUL_WIDTH   = OP_MANTISSA + 20,\n"
             + "          TAKTE     = 36,\n"
             + "          POS_ZERO  = {1'b0, {(OP_WIDTH-1){1'b0}}},\n"
             + "          NEG_ZERO  = {1'b1, {(OP_WIDTH-1){1'b0}}},\n"
             + "          POS_INF    = {1'b0, {(OP_EXPONENT){1'b1}}, {(OP_MANTISSA){1'b0}}},\n"
             + "          NEG_INF    = {1'b1, {(OP_EXPONENT){1'b1}}, {(OP_MANTISSA){1'b0}}},\n"
             + "          NAN     = {1'b0, {(OP_EXPONENT+OP_MANTISSA){1'b1}}};\n"
        + "localparam  [OP_EXPONENT-1:0] EXP_BIAS = (2**OP_EXPONENT) / 2 - 1;\n\n"
        + "// Operanden load and unpack \n"
        + "reg [OP_WIDTH-1:0] op_a_n, op_b_n, next_op_a_n, next_op_b_n;\n"
        + "wire op_a_vz = op_a_n[OP_WIDTH-1];\n"
        + "wire op_b_vz = op_b_n[OP_WIDTH-1];\n"
        + "wire [OP_EXPONENT-1:0] op_a_exp = op_a_n[OP_WIDTH-2:OP_MANTISSA];\n"
        + "wire [OP_EXPONENT-1:0] op_b_exp = op_b_n[OP_WIDTH-2:OP_MANTISSA];\n"
        + "wire [OP_MANTISSA-1:0] op_a_man = op_a_n[OP_MANTISSA-1:0];\n"
        + "wire [OP_MANTISSA-1:0] op_b_man = op_b_n[OP_MANTISSA-1:0];\n"
        + "// Signale und Init \n"
        + "reg isFloat;\n"
        + "reg res_is_denorm, next_res_is_denorm;\n"
        + "reg [OP_EXPONENT-1:0] res_denorm_shift, next_res_denorm_shift;\n"
        + "reg [OP_MANTISSA:0] n_man, next_n_man;\n"
        + "reg [OP_MANTISSA:0] d_man, next_d_man;\n"
        + "reg [OP_EXPONENT-1:0] q_exp, next_q_exp;\n"
        + "reg vz_r, next_vz_r;\n reg [MUL_WIDTH-1:0] q, next_q;  // q_n \n"
        + "reg [MUL_WIDTH-1:0] denorm_res, next_denorm_res;\n"
        + "wire [MUL_WIDTH-1:0] x_0;  // erster wert für x \n"
        + "wire [MUL_WIDTH-1:0] x_plus_1_0;  // und für x + 1 \n"
        + "assign x_0 = {1'b1, {(MUL_WIDTH-1){1'b0}}} - {1'b0, d_man, {(MUL_WIDTH-1-OP_MANTISSA-1){1'b0}}};\n"
        + "assign x_plus_1_0 = ({1'b1, {(MUL_WIDTH-1){1'b0}}} - {2'b00, d_man, {(MUL_WIDTH-2-OP_MANTISSA-1){1'b0}}}) << 1;\n"
        + "// Verschiedene Rundungen \n"
        + "wire [OP_MANTISSA-1:0] result_rounded_1;  // Case n_man > d_man \n"
        + "wire [OP_MANTISSA-1:0] result_rounded_2;  // Case n_man < d_man \n"
        + "assign result_rounded_1 = result[MUL_WIDTH-2:MUL_WIDTH-2-OP_MANTISSA+1] + 1;\n"
        + "assign result_rounded_2 = result[MUL_WIDTH-3:MUL_WIDTH-3-OP_MANTISSA+1] + 1;\n"
        + "// Result \n"
        + "reg valid_o, next_valid_o;\n"
        + RESULT_VALID.getAssignment("valid_o")
        + "reg [OP_WIDTH-1:0] finished_result, next_finished_result;\n"
        + getResultPort().getAssignment("finished_result[DP_WIDTH-1:0]")
        + "//  #### Module #### \n"
      // Square X
        + "//  Square_X \n"
        + "reg sqrX_enable_i, next_sqrX_enable_i;\n"
        + "reg [MUL_WIDTH-1:0] sqrX_x_i, next_sqrX_x_i;\n"
        + "wire [MUL_WIDTH-1:0] sqrX_x_o;\n"
        + "wire [MUL_WIDTH-1:0] sqrX_plus_eins_o;\n"
        + "wire sqrX_valid_o;\n"
        + "square_x_" + tmp.getCanonicalName() + " #(\n"
        + "  .MUL_WIDTH(MUL_WIDTH)\n"
        + ") sqrX (\n"
        + "  .clk_i(" + CLOCK + "),\n"
        + "  .enable_i(sqrX_enable_i),\n"
        + "  .reset_i(" + RESET + "),\n"
        + "  .x_i(sqrX_x_i),\n"
        + "  .x_sqr_o(sqrX_x_o),\n"
        + "  .eins_plus_x_sqr_o(sqrX_plus_eins_o),\n"
        + "  .r_valid_o(sqrX_valid_o) );\n\n"
        + " //  Mul_Q \n"
      // Mul_Q
        + "reg mulQ_enable_i, next_mulQ_enable_i;\n"
        + "reg [MUL_WIDTH-1:0] mulQ_x_i, next_mulQ_x_i;\n"
        + "reg [MUL_WIDTH-1:0] mulQ_q_i, next_mulQ_q_i;\n"
        + "wire [MUL_WIDTH-1:0] mulQ_prod_o;\n"
        + "wire mulQ_valid_o;\n"
        + "mul_q_" + tmp.getCanonicalName() + " #(\n"
        + "  .MUL_WIDTH(MUL_WIDTH)\n"
        + ") mulQ (\n"
        + "  .clk_i(" + CLOCK + "),\n"
        + "  .enable_i(mulQ_enable_i),\n"
        + "  .reset_i(" + RESET + "),\n"
        + "  .x_plus_eins_i(mulQ_x_i),\n"
        + "  .q_i(mulQ_q_i),\n"
        + "  .prod_o(mulQ_prod_o),\n"
        + "  .r_valid_o(mulQ_valid_o));\n\n"
        + "//  ExceptionCheckerReg \n"
      // ExceptionChecker
        + "wire[OP_WIDTH-1:0] exc_res;\n"
        + "wire exc_res_valid;\n"
        + "wire exc_op_denorm;\n"
        + "wire exc_res_denorm;\n"
        + "ExceptionCheckerReg_" + tmp.getCanonicalName() + " #(\n"
        + "  .OP_EXPONENT(OP_EXPONENT),\n"
        + "  .OP_MANTISSA(OP_MANTISSA)\n"
        + ") excCheck (\n"
        + "  .clk_i(" + CLOCK + "),\n"
        + "  .operand_a_i(op_a_n),\n"
        + "  .operand_b_i(op_b_n),\n"
        + "  .result_o(exc_res),\n"
        + "  .result_valid_o(exc_res_valid),\n"
        + "  .op_denorm_o(exc_op_denorm),\n"
        + "  .res_denorm_o(exc_res_denorm));\n\n"
        + "//  DenormHandler \n"
      // DenormHandler
        + "reg norm_enable;\n"
        + "wire [OP_MANTISSA+1:0] norm_man_a_o;\n"
        + "wire [OP_MANTISSA+1:0] norm_man_b_o;\n"
        + "wire [OP_EXPONENT-1:0] norm_exp_o;\n"
        + "wire norm_res_denorm;\n"
        + "wire norm_overflow;\n"
        + "wire norm_underflow;\n"
        + "wire norm_finish_o;\n"
        + "reg next_norm_enable;\n"
        + "DenormHandler_" + tmp.getCanonicalName() + " #(\n"
        + "  .OP_EXPONENT(OP_EXPONENT),\n"
        + "  .OP_MANTISSA(OP_MANTISSA)\n"
        + ") norm (\n"
        + "  .clk_i(" + CLOCK + "),\n"
        + "  .rst_i(" + RESET + "),\n"
        + "  .enable_i(next_norm_enable),\n"
        + "  .op_a_i(op_a_n), .op_b_i(op_b_n),\n"
        + "  .op_a_man_o(norm_man_a_o),\n"
        + "  .op_b_man_o(norm_man_b_o),\n"
        + "  .exp_r_o(norm_exp_o),\n"
        + "  .res_denorm_o(norm_res_denorm),\n"
        + "  .overflow_o(norm_overflow),\n"
        + "  .underflow_o(norm_underflow),\n"
        + "  .finish_o(norm_finish_o));\n\n "
        + "//  Result_Mul \n"
      // Result_Mul
        + "reg result_checked, next_result_checked;\n"
        + "reg rmul_enable_i, next_rmul_enable_i;\n"
        + "reg [MUL_WIDTH-1:0] rmul_r_i, next_rmul_r_i;\n"
        + "wire [MUL_WIDTH-1:0] rmul_r_o;\n"
        + "wire rmul_valid_o;\n"
        + "reg [MUL_WIDTH-1:0] result;\n"
        + "result_mul_" + tmp.getCanonicalName() + " #(\n"
        + "  .MUL_WIDTH(MUL_WIDTH),\n"
        + "  .OP_EXPONENT(OP_EXPONENT),\n"
        + ".OP_MANTISSA(OP_MANTISSA)\n"
        + ") rmul (\n"
        + "  .clk_i(" + CLOCK + "),\n"
        + "  .enable_i(rmul_enable_i),\n"
        + "  .reset_i(" + RESET + "),\n"
        + "  .isFloat(isFloat),\n"
        + "  .d_i({d_man, {(MUL_WIDTH-OP_MANTISSA-1){1'b0}}}),\n"
        + "  .result_i(rmul_r_i),\n"
        + "  .n_i({n_man, {(MUL_WIDTH-OP_MANTISSA-1){1'b0}}}),\n"
        + "  .result_o(rmul_r_o), .r_valid_o(rmul_valid_o));\n\n "
        + "//  always@ \n"
        + "reg result_needs_check;\n"
        + "always @(*) begin \n"
        + "  if(res_is_denorm) begin \n"
        + "    result_needs_check = 0;\n"
        + "  end else begin \n"
        + "    if((result[MUL_WIDTH-1] && (&result[MUL_WIDTH-1-OP_MANTISSA-3:0])) || ((~result[MUL_WIDTH-1]) && (&result[MUL_WIDTH-1-OP_MANTISSA-4:0]))) begin \n"
        + "      result_needs_check = 1;\n "
        + "    end else begin \n"
        + "      result_needs_check = 0;\n"
        + "    end \n"
        + "  end \n"
        + "end \n\n"
        + "always @(*)begin \n"
        + "// Result Auswahl \n"
        + "  if(res_is_denorm) begin \n"
        + "    result = denorm_res;\n"
        + "  end else if (result_checked) begin \n"
        + "    result = rmul_r_o;\n"
        + "  end else begin \n"
        + "    result = mulQ_prod_o;\n"
        + "  end \n"
        + "end \n\n"
        + "//  #### Automat #### \n"
        + "reg [3:0] state, next_state;\n"
        + "localparam    READY         = 4'd0,\n"
        + "          LOAD_OP      = 4'd1,\n"
        + "          PREPARE     = 4'd2,\n"
        + "          CALC_X           = 4'd3,\n"
        + "          NORM             = 4'd4,\n"
        + "          NORM_CALC_X     = 4'd5,\n"
        + "          ITERATION       = 4'd6,\n"
        + "          SET_RESULT       = 4'd7,\n"
        + "          SET_DENORM_RES   = 4'd8,\n"
        + "          FINISHED         = 4'd9;\n\n"
        + " reg [3:0] loop;\n"
        + "reg iteration_set;\n"
        + "reg incr_loop;\n"
        + "reg next_iteration_set;\n"
        + "reg rst_loop;\n "
        + "// Einlesen und Gesamttakte \n"
        + "reg [$clog2(CYCLE+1):0] input_cycle, next_input_cycle;\n"
        + "reg [8:0] counter, next_counter;\n  // todo größe \n"
        + getClockedProcess(
              // reset
              "state <= READY;\n"
            + "valid_o <= 0;",
            
              // start (TODO: this can not be correct, originally, this block was guarded by EN_I)
              "state <= next_state;\n"
            + "n_man <= next_n_man;\n"
            + "d_man <= next_d_man;\n "
            + "q <= next_q;\n"
            + "// \n"
            + "vz_r <= next_vz_r;\n"
            + "q_exp <= next_q_exp;\n"
            + "res_is_denorm <= next_res_is_denorm;\n"
            + "res_denorm_shift <= next_res_denorm_shift;\n"
            + "sqrX_x_i <= next_sqrX_x_i;\n"
            + "mulQ_x_i <= next_mulQ_x_i;\n"
            + "mulQ_q_i <= next_mulQ_q_i;\n"
            + "iteration_set <= next_iteration_set;\n"
            + "// \n"
            + "norm_enable <= next_norm_enable;\n"
            + "denorm_res <= next_denorm_res;\n"
            + "mulQ_enable_i <= next_mulQ_enable_i;\n"
            + "sqrX_enable_i <= next_sqrX_enable_i;\n"
            + "// \n"
            + "rmul_enable_i <= next_rmul_enable_i;\n"
            + "rmul_r_i <= next_rmul_r_i;\n"
            + "result_checked <= next_result_checked;\n"
            + "// \n"
            + "input_cycle <= next_input_cycle;\n"
            + "op_a_n <= next_op_a_n;\n"
            + "op_b_n <= next_op_b_n;\n"
            + "finished_result <= next_finished_result;\n"
            + "counter <= next_counter;\n"
            + "isFloat <= 1;\n"
            + "valid_o <= next_valid_o;",
            
              // remaining (TODO: compare to original, this might not work)
              "if(incr_loop) begin \n"
            + "  loop <= loop+1;\n"
            + "end else begin \n"
            + "  if(rst_loop) \n"
            + "    loop <= 0;\n"
            + "end"
          )
        + " //  #### Zustandsübergänge / Ausgaben #### \n"
        + "always @(*) begin \n"
        + "  next_state = READY;\n"
        + "  next_n_man = n_man;\n"
        + "  next_d_man = d_man;\n"
        + "  next_q = q;\n"
        + "  next_vz_r = vz_r;\n"
        + "  next_q_exp = q_exp;\n"
        + "  next_res_is_denorm = res_is_denorm;\n"
        + "  next_res_denorm_shift = res_denorm_shift;\n"
        + "  next_sqrX_x_i = sqrX_x_i;\n"
        + "  next_mulQ_x_i = mulQ_x_i;\n"
        + "  next_mulQ_q_i = mulQ_q_i;\n"
        + "  incr_loop = 1'b0;\n"
        + "  rst_loop = 1'b1;\n"
        + "  next_iteration_set = iteration_set;\n"
        + "  next_norm_enable = 0;\n"
        + "  next_denorm_res = denorm_res;\n"
        + "  next_sqrX_enable_i = 0;\n"
        + "  next_rmul_r_i = rmul_r_i;\n"
        + "  next_mulQ_enable_i = 0;\n"
        + "  next_rmul_enable_i = 0;\n"
        + "  next_result_checked = 0;\n"
        + "  next_valid_o = 0;\n"
        + "  next_finished_result = finished_result;\n"
        + "  next_input_cycle = 0;\n"
        + "  next_op_a_n = op_a_n;\n"
        + "  next_op_b_n = op_b_n;\n"
        + "  next_counter = 1337;\n"
        + "  if(" + START + " && (state != READY)) begin \n"
        + "    next_counter = counter + 1;\n"
        + "  end \n\n"
        + "// #### \n"
        + "  case(state) \n"
        + "    READY:  begin   // Ersten Teil Operanden holen \n"
        + "      next_counter = 0;\n"
        + "      next_op_a_n = 0;\n"
        + "      next_op_b_n = 0;\n"
        + "      next_state = READY;\n"
        + "      if(VALID_I) begin \n"
        + "        next_op_a_n = " + getOperandPort(0) + ";\n"
        + "        next_op_b_n = " + getOperandPort(1) + ";\n"
        + "        next_input_cycle = input_cycle + 1;\n"
        + "        if(next_input_cycle == CYCLE && ODD != 0 || next_input_cycle < CYCLE) begin \n"
        + "          next_state = LOAD_OP;\n"
        + "        end else begin \n"
        + "          next_counter = 1;\n\n"
        + "          next_state = PREPARE;\n"
        + "        end \n"
        + "      end \n"
        + "    end \n"
        + "    LOAD_OP:  begin   // Weitere Teile Operand holen \n"
        + "      next_op_a_n = next_op_a_n | (" + getOperandPort(0) + " << input_cycle * DP_WIDTH);\n"
        + "      next_op_b_n = next_op_b_n | (" + getOperandPort(1) + " << input_cycle * DP_WIDTH);\n"
        + "      next_input_cycle = input_cycle + 1;\n"
        + "      if(next_input_cycle == CYCLE && ODD != 0 || next_input_cycle < CYCLE) begin \n"
        + "        next_state = LOAD_OP;\n"
        + "      end else begin \n"
        + "        next_counter = 1;\n"
        + "        next_state = PREPARE;\n"
        + "      end \n"
        + "    end \n"
        + "    PREPARE:  begin \n"
        + "      // n, d und q laden \n"
        + "      // Hier unterschiedliche Genaunigkeiten laden \n"
        + "      next_n_man = {1'b1, op_a_man};\n"
        + "      next_d_man = {1'b1, op_b_man};\n"
        + "      next_q = {2'b01, op_a_man, {(MUL_WIDTH-2-OP_MANTISSA){1'b0}}};\n"
        + "      next_state = CALC_X;\n"
        + "      next_input_cycle = 1;\n"
        + "    end \n"
        + "    CALC_X:  begin \n"
        + "      next_state = ITERATION;\n"
        + "      next_vz_r = op_a_vz ^ op_b_vz;\n"
        + "      next_iteration_set = 0;\n "
        + "      // Check special cases 0, NaN, infinity, Over- and Underflow \n"
        + "      if(exc_res_valid) begin \n"
        + "        // Ergebnis steht schon fest weil ein Exception Result \n"
        + "        next_finished_result = exc_res;\n"
        + "        next_state = FINISHED;\n"
        + "      end else if(exc_op_denorm) begin \n"
        + "        // Ein oder beider Operanden sind denormiert \n"
        + "        next_state = NORM;\n"
        + "      end else if(~exc_op_denorm && ~exc_res_valid) begin \n"
        + "        // Normale Division //Exponent berechnen: \n"
        + "        if (~exc_res_denorm) begin \n"
        + "          // Ergebnis nicht denormalisiert, exponenten subtrahieren \n"
        + "          next_q_exp = op_a_exp - op_b_exp + EXP_BIAS;\n"
        + "          next_res_is_denorm = 0;\n"
        + "        end else begin \n"
        + "          // Ergebnis denormalisiert, Exponent muss am Ende berechnet werden \n"
        + "          next_res_is_denorm = 1;\n"
        + "          next_q_exp = 0;\n"
        + "          if({1'b1, op_a_man} > {1'b1, op_b_man}) begin \n"
        + "            next_res_denorm_shift = op_b_exp - op_a_exp - EXP_BIAS + 1;\n"
        + "          end else begin \n"
        + "            next_res_denorm_shift = op_b_exp - op_a_exp - EXP_BIAS + 2;\n"
        + "          end \n"
        + "        end \n"
        + "      // Iteration vorbereiten: \n"
        + "      next_sqrX_x_i = x_0 << 1;\n"
        + "      next_mulQ_q_i = q;\n"
        + "      next_mulQ_x_i = x_plus_1_0;\n"
        + "      end \n"
        + "    end \n"
        + "    NORM: begin \n"
        + "      next_state = NORM;\n"
        + "      next_norm_enable = 1;\n"
        + "      if(norm_finish_o) begin \n"
        + "        next_norm_enable = 0;\n"
        + "        if(norm_overflow) begin \n"
        + "          // Normieren ergab Overflow \n"
        + "          next_finished_result = vz_r ? NEG_INF : POS_INF;\n"
        + "          next_state = FINISHED;\n"
        + "        end else if (norm_underflow) begin \n"
        + "          // Normieren ergab Underflow \n"
        + "          next_finished_result = vz_r ? NEG_ZERO : POS_ZERO;\n"
        + "          next_state = FINISHED;\n"
        + "        end else if (norm_man_a_o == norm_man_b_o) begin \n"
        + "          // Beide Operanden gleich \n"
        + "          next_finished_result = {vz_r, norm_exp_o, {(OP_MANTISSA){1'b0}}};\n"
        + "          next_state = FINISHED;\n"
        + "        end else begin \n"
        + "          next_n_man = norm_man_a_o[OP_MANTISSA+1:1];\n"
        + "          next_d_man = norm_man_b_o[OP_MANTISSA+1:1];\n"
        + "          next_q = {1'b0, norm_man_a_o, {(MUL_WIDTH-1-OP_MANTISSA-2){1'b0}}};\n"
        + "          if(norm_res_denorm) begin \n"
        + "            // Ergebnis denormalisiert, Exponent muss am Ende berechnet werden \n"
        + "            next_res_is_denorm = 1;\n"
        + "            $display(\"denorm\");\n"
        + "            next_res_denorm_shift = norm_exp_o;\n"
        + "            next_q_exp = 0;\n"
        + "          end else begin \n"
        + "            // Ergebnis normalisiert, Exponent wurde in norm berechnet \n"
        + "            next_q_exp = norm_exp_o + 1;\n"
        + "            next_res_is_denorm  = 0;\n"
        + "          end \n"
        + "          next_state = NORM_CALC_X;\n"
        + "        end \n"
        + "      end \n"
        + "    end \n"
        + "    NORM_CALC_X: begin \n"
        + "      // Iteration vorbereiten: \n"
        + "      next_sqrX_x_i = x_0 << 1;\n"
        + "      next_mulQ_q_i = q;\n"
        + "      next_mulQ_x_i = x_plus_1_0;\n"
        + "      next_state = ITERATION;\n"
        + "      next_iteration_set = 0;\n"
        + "    end \n"
        + "    ITERATION:   begin \n"
        + "      next_state = ITERATION;\n"
        + "      // x^n -> x^n+1 "
        + "      // q -> q * 1+x^n \n"
        + "      next_mulQ_enable_i = 1;\n"
        + "      next_sqrX_enable_i = 1;\n"
        + "      rst_loop = 1'b0;\n"
        + "      if(mulQ_valid_o && sqrX_valid_o && ~iteration_set) begin \n"
        + "        next_iteration_set = 1;\n"
        + "        next_sqrX_x_i = sqrX_x_o;\n"
        + "        next_mulQ_q_i = mulQ_prod_o;\n"
        + "        next_mulQ_x_i = sqrX_plus_eins_o;\n"
        + "        incr_loop = 1'b1;\n"
        + "        if(loop >= IT) begin //before 10/12 \n"
        + "          next_state = res_is_denorm ? SET_DENORM_RES : SET_RESULT;\n"
        + "          next_mulQ_enable_i = 0;\n"
        + "          next_sqrX_enable_i = 0;\n"
        + "          next_rmul_r_i = mulQ_prod_o;\n"
        + "        end else begin \n"
        + "          next_state = ITERATION;\n"
        + "        end \n"
        + "      end else begin \n"
        + "        next_iteration_set = 0;\n"
        + "      end \n"
        + "    end \n"
        + "    SET_DENORM_RES: begin \n"
        + "      next_denorm_res = mulQ_prod_o >> (mulQ_prod_o[MUL_WIDTH-1] ? res_denorm_shift : (res_denorm_shift - 1));\n //TODO: rausziehen/immer machen \n"
        + "      next_state = SET_RESULT;\n \n"
        + "    end \n"
        + "    SET_RESULT: begin \n"
        + "      next_state = SET_RESULT;\n"
        + "      next_result_checked = result_checked;\n"
        + "      next_rmul_enable_i = rmul_enable_i;\n"
        + "      if(result_needs_check && ~result_checked) begin \n"
        + "        next_rmul_enable_i = 1;\n"
        + "        next_result_checked = 1;\n"
        + "        next_state = SET_RESULT;\n"
        + "      end else if(~result_checked || (result_checked && rmul_valid_o)) begin \n"
        + "        next_rmul_enable_i = 0;\n"
        + "        next_state = FINISHED;\n"
        + "        if(result[MUL_WIDTH-1] || res_is_denorm) begin \n"
        + "          if(result[MUL_WIDTH-2-OP_MANTISSA])begin \n"
        + "            next_finished_result = {vz_r, q_exp[OP_EXPONENT-1:0], result_rounded_1};\n"
        + "          end else begin \n"
        + "            next_finished_result = {vz_r, q_exp[OP_EXPONENT-1:0], result[MUL_WIDTH-2:MUL_WIDTH-2-OP_MANTISSA+1]};\n"
        + "          end \n"
        + "        end else begin \n"
        + "          if(result[MUL_WIDTH-2-OP_MANTISSA-1]) begin \n"
        + "            next_finished_result = {vz_r, (q_exp[OP_EXPONENT-1:0] - 1'b1), result_rounded_2};\n"
        + "          end else begin \n"
        + "            next_finished_result = {vz_r, (q_exp[OP_EXPONENT-1:0] - 1'b1), result[MUL_WIDTH-3:MUL_WIDTH-3-OP_MANTISSA+1]};\n"
        + "          end \n"
        + "        end \n"
        + "      end \n"
        + "    end \n"
        + "    FINISHED:  begin \n"
        + "      if(counter < TAKTE) begin \n"
        + "        next_state = FINISHED;\n"
        + "      end else if(counter == TAKTE) begin \n"
        + "        next_valid_o = 1;\n"
        + "        if(next_input_cycle == CYCLE && ODD != 0 || next_input_cycle < CYCLE) begin \n"
        + "          next_state = FINISHED;\n"
        + "        end else begin \n"
        + "          next_input_cycle = 0;\n"
        + "          next_state = READY;\n"
        + "        end \n"
        + "      end else begin \n"
        + "        next_finished_result = finished_result >> DP_WIDTH;\n"
        + "        next_input_cycle = input_cycle + 1;\n"
        + "        if(next_input_cycle == CYCLE && ODD != 0 || next_input_cycle < CYCLE) begin \n"
        + "          next_state = FINISHED;\n"
        + "        end else begin \n"
        + "          next_input_cycle = 0;\n"
        + "          next_state = READY;\n"
        + "        end \n"
        + "      next_valid_o = 0;\n"
        + "    end \n"
        + "    end \n"
        + "  endcase \n"
        + "end \n"
        + "endmodule\n\n");
    
    
    
    //SquareX
    res.append("module square_x_" + tmp.getCanonicalName() + " #(\n"
        + "parameter MUL_WIDTH = 68 \n"
        + ")(\n"
        + "input wire clk_i,\n"
        + "input wire enable_i,\n"
        + "input wire reset_i,\n"
        + "input wire [MUL_WIDTH-1:0] x_i,\n"
        + "output reg [MUL_WIDTH-1:0] x_sqr_o,\n"
        + "output reg [MUL_WIDTH-1:0] eins_plus_x_sqr_o,\n"
        + "output reg r_valid_o);\n\n"
        + "wire[2*MUL_WIDTH-1:0] res;\n"
        + "assign res = x_i * x_i;\n"
        + "reg next_r_valid_o;\n"
        + "reg[MUL_WIDTH-1:0] next_x_sqr_o;\n"
        + "reg[MUL_WIDTH-1:0] next_eins_plus_x_sqr_o;\n"
        + "// Automat //\n"
        + "reg state, next_state;\n"
        + "localparam   READY    = 1'd0,\n"
        + "        FINISH  = 1'd1;\n\n"
        + "always @(posedge clk_i) begin \n"
        + "  if(reset_i || ~enable_i) begin \n"
        + "    state <= READY;\n"
        + "    r_valid_o <= 0;\n"
        + "  end  else begin \n"
        + "    state <= next_state;\n "
        + "  end \n"
        + "  x_sqr_o <= next_x_sqr_o;\n"
        + "  eins_plus_x_sqr_o <= next_eins_plus_x_sqr_o;\n"
        + "  r_valid_o <= next_r_valid_o;\n"
        + "end \n\n"
        + "always @(*) begin \n"
        + "  next_r_valid_o = 0;\n"
        + "  next_x_sqr_o = 0;\n"
        + "  next_eins_plus_x_sqr_o = 0;\n"
        + "  case(state) \n"
        + "    READY:  begin \n"
        + "      if(enable_i) begin \n"
        + "        next_r_valid_o = 0;\n"
        + "        next_state = FINISH;\n"
        + "      end else begin \n"
        + "        next_state = READY;\n"
        + "      end \n"
        + "    end \n"
        + "    FINISH:  begin \n"
        + "      next_x_sqr_o = res[2*MUL_WIDTH-1:MUL_WIDTH];\n"
        + "      next_eins_plus_x_sqr_o = {1'b1, res[2*MUL_WIDTH-1:MUL_WIDTH+1]};\n"
        + "      next_r_valid_o = 1;\n"
        + "      next_state = READY;\n "
        + "    end \n"
        + "  endcase \n"
        + "end \n"
        + "endmodule\n\n");
    
    //MulQ
    res.append("module mul_q_" + tmp.getCanonicalName() + " #(\n"
        + "parameter MUL_WIDTH = 68 \n"
        + ")(\n"
        + "input wire clk_i,\n"
        + "input wire enable_i,\n"
        + "input wire reset_i,\n"
        + "input wire [MUL_WIDTH-1:0] x_plus_eins_i,\n"
        + "input wire [MUL_WIDTH-1:0] q_i,\n"
        + "output reg [MUL_WIDTH-1:0] prod_o,\n"
        + "output reg r_valid_o);\n\n"
        + "wire[2*MUL_WIDTH-1:0] res;\n"
        + "assign res = x_plus_eins_i * q_i;\n"
        + "reg next_r_valid_o;\n"
        + "reg [MUL_WIDTH-1:0] next_prod_o;\n"
        + "// Automat //\n"
        + "reg state, next_state;\n "
        + "localparam   READY  = 1'd0,\n"
        + "        FINISH = 1'd1;\n\n"
        + "always @(posedge clk_i) begin \n"
        + "  if (reset_i) begin \n"
        + "    r_valid_o <= 0;\n"
        + "    state <= READY;\n"
        + "  end else begin \n"
        + "    case(state) \n"
        + "      READY: begin \n"
        + "        r_valid_o <= 0;\n "
        + "        if(enable_i) \n"
        + "          state <= FINISH;\n "
        + "      end \n"
        + "      FINISH: begin \n"
        + "        prod_o <= res[2*MUL_WIDTH-2:MUL_WIDTH-1];\n"
        + "        r_valid_o <= 1;\n"
        + "        state<= READY;\n"
        + "      end \n"
        + "    endcase \n"
        + "  end \n"
        + "end \n"
        + "endmodule\n\n");
    
    //DenormHandler
    res.append("module DenormHandler_" + tmp.getCanonicalName() + " #(\n"
        + "parameter OP_EXPONENT = 8,\n"
        + "parameter OP_MANTISSA = 23 \n"
        + ")(\n"
        + "input wire clk_i,\n"
        + "input wire rst_i,\n"
        + "input wire enable_i,\n"
        + "input wire [OP_EXPONENT + OP_MANTISSA:0] op_a_i,\n"
        + "input wire [OP_EXPONENT + OP_MANTISSA:0] op_b_i,\n"
        + "output reg [OP_MANTISSA+1:0] op_a_man_o,\n"
        + "output reg [OP_MANTISSA+1:0] op_b_man_o,\n"
        + "output reg [OP_EXPONENT-1:0] exp_r_o,\n"
        + "output reg res_denorm_o,\n"
        + "output reg overflow_o,\n"
        + "output reg underflow_o,\n"
        + "output reg finish_o);\n\n"
        + "localparam  OP_WIDTH = 1 + OP_EXPONENT + OP_MANTISSA;\n"
        + "localparam  [OP_EXPONENT:0] EXP_BIAS = (2**OP_EXPONENT) / 2 - 1;\n\n"
        + "reg[OP_EXPONENT-1:0] op_a_i_exp, op_b_i_exp;\n"
        + "reg[OP_MANTISSA-1:0] op_a_i_man, op_b_i_man;\n"
        + "reg [$clog2(OP_MANTISSA)-1:0] shamt_a, shamt_a_reg;\n"
        + "reg [$clog2(OP_MANTISSA)-1:0] shamt_b, shamt_b_reg;\n"
        + "reg [OP_EXPONENT-1:0] new_shamt_b_reg_min_shamt_a_reg_BIAS_minus_1;\n"
        + "reg new_man_a_gr_man_b, new_man_b_gr_man_a;\n"
        + "reg [OP_EXPONENT:0] new_ex_a_minus_ex_b, new_ex_a_plus_sh_b, new_ex_b_minus_ex_a, new_ex_b_plus_sh_a;\n\n"
        + "reg[1:0] state, next_state;\n "
        + "localparam      READY   = 2'd0,\n"
        + "            PRE_CMP = 2'd1,\n"
        + "            SHIFT   = 2'd2,\n"
        + "            FINISH   = 2'd3;\n\n"
        + "reg res_denorm, overflow, underflow, finish, take_shamt;\n"
        + "reg [OP_MANTISSA+1:0] op_a_man, op_b_man;\n"
        + "reg [OP_EXPONENT-1:0] exp_r;\n"
        + "reg [OP_EXPONENT-1:0] ls_tmp13, ls_tmp_BIAS_minus_2, ls_tmp_BIAS_minus_1, ls_tmp_BIAS, ls_tmp13_reg, ls_tmp_BIAS_minus_2_reg, ls_tmp_BIAS_minus_1_reg, ls_tmp_BIAS_reg;\n"
        + "reg op_a_exp_0, op_b_exp_0;\n\n"
        + "always@(posedge clk_i) begin \n"
        + "  if(rst_i) begin \n"
        + "    state <= READY;\n"
        + "  end else begin \n"
        + "    state <= next_state;\n"
        + "  end \n"
        + "  if(take_shamt) begin \n"
        + "    shamt_a_reg <= shamt_a;\n "
        + "    shamt_b_reg <= shamt_b;\n"
        + "  end \n"
        + "  op_a_man_o <= op_a_man;\n"
        + "  op_b_man_o <= op_b_man;\n"
        + "  exp_r_o <= exp_r;\n"
        + "  overflow_o <= overflow;\n"
        + "  underflow_o <= underflow;\n"
        + "  res_denorm_o <= res_denorm;\n"
        + "  finish_o <= finish;\n"
        + "  op_a_exp_0 <= op_a_i[OP_WIDTH-2:OP_MANTISSA] == 0;\n"
        + "  op_b_exp_0 <= op_b_i[OP_WIDTH-2:OP_MANTISSA] == 0;\n"
        + "  op_a_i_exp <= op_a_i[OP_WIDTH-2:OP_MANTISSA];\n"
        + "  op_a_i_man <= op_a_i[OP_MANTISSA-1:0];\n"
        + "  op_b_i_exp <= op_b_i[OP_WIDTH-2:OP_MANTISSA];\n"
        + "  op_b_i_man <= op_b_i[OP_WIDTH-1:0];\n"
        + "  //PRE_CMP signals //valid nach PRE_CMP state \n"
        + "  new_man_a_gr_man_b <= {1'b1,op_a_i_man<<shamt_a_reg} > {1'b1,op_b_i_man<<shamt_b_reg};\n"
        + "  new_man_b_gr_man_a <= ~({1'b1,op_a_i_man<<shamt_a_reg} > {1'b1,op_b_i_man<<shamt_b_reg});\n"
        + "  new_ex_a_minus_ex_b <= op_a_i_exp - shamt_a_reg - op_b_i_exp + shamt_b_reg;\n "
        + "  new_ex_a_plus_sh_b <= op_a_i_exp + shamt_b_reg;\n "
        + "  new_ex_b_plus_sh_a <= op_b_i_exp + shamt_a_reg;\n "
        + "  new_ex_b_minus_ex_a <= op_b_i_exp - shamt_b_reg - op_a_i_exp + shamt_a_reg;\n "
        + "  new_shamt_b_reg_min_shamt_a_reg_BIAS_minus_1 <= shamt_b_reg - shamt_a_reg + EXP_BIAS - 1;\n"
        + "  ls_tmp_BIAS_reg <= ls_tmp_BIAS;\n"
        + "  ls_tmp_BIAS_minus_1_reg <= ls_tmp_BIAS_minus_1;\n"
        + "  ls_tmp_BIAS_minus_2_reg <= ls_tmp_BIAS_minus_2;\n"
        + "  ls_tmp13_reg <= ls_tmp13;\n "
        + "end \n\n"
        + "always@(*) begin \n"
        + "  //vorberechnungen for Shift State: \n"
        + "  //valid nach PRE_CMP state \n"
        + "  ls_tmp13 = op_b_i_exp - shamt_b_reg - op_a_i_exp + shamt_a_reg;\n"
        + "  ls_tmp_BIAS_minus_2 = op_a_i_exp - shamt_a_reg - op_b_i_exp + shamt_b_reg + EXP_BIAS - 2;\n"
        + "  ls_tmp_BIAS_minus_1 = op_a_i_exp - shamt_a_reg - op_b_i_exp + shamt_b_reg + EXP_BIAS - 1;\n"
        + "  ls_tmp_BIAS = op_a_i_exp - shamt_a_reg - op_b_i_exp + shamt_b_reg + EXP_BIAS;\n"
        + "  take_shamt = 0;\n"
        + "  finish = 0;\n"
        + "  res_denorm = 0;\n"
        + "  overflow = 0;\n"
        + "  underflow = 0;\n"
        + "  shamt_b = 0;\n"
        + "  shamt_a = 0;\n"
        + "  op_a_man = 0;\n"
        + "  op_b_man = 0;\n"
        + "  exp_r = 0;\n"
        + "  next_state = READY;\n"
        + "  op_a_man = {1'b1, op_a_i_man << shamt_a_reg, 1'b0};\n"
        + "  op_b_man = {1'b1, op_b_i_man << shamt_b_reg, 1'b0};\n"
        + "  case(state) \n"
        + "    READY: begin \n"
        + "      if(enable_i) begin \n"
        + "        take_shamt = 1;\n"
        + "        if(op_a_i[OP_WIDTH-2:OP_MANTISSA] == 0) begin \n"
        + "          // a normalisieren \n"
        + "          shamt_a = shamt(op_a_i_man);\n"
        + "        end else begin \n"
        + "          shamt_a = 0;\n"
        + "        end if(op_b_i[OP_WIDTH-2:OP_MANTISSA] == 0) begin \n"
        + "          // b normalisieren \n"
        + "          shamt_b = shamt(op_b_i_man);\n "
        + "        end else begin \n"
        + "          shamt_b = 0;\n "
        + "        end \n"
        + "        next_state = PRE_CMP;\n"
        + "      end \n"
        + "    end \n"
        + "    PRE_CMP: begin \n"
        + "      next_state = SHIFT;\n"
        + "    end \n"
        + "    SHIFT: begin \n"
        + "      next_state = FINISH;\n "
        + "      finish = 1;\n "
        + "      //Exponent berechnen und Norm/Denorm für ergebniss prüfen \n"
        + "      if(((new_ex_a_plus_sh_b) < (new_ex_b_plus_sh_a)) && ((new_ex_b_minus_ex_a) > (new_man_a_gr_man_b + OP_MANTISSA + EXP_BIAS))) begin \n"
        + "        // Unterlauf \n"
        + "        underflow = 1;\n"
        + "      end else if (((new_ex_a_plus_sh_b) < (new_ex_b_plus_sh_a)) && (ls_tmp13_reg) <= (new_man_a_gr_man_b+ OP_MANTISSA + EXP_BIAS) && (ls_tmp13_reg) > (new_man_a_gr_man_b + EXP_BIAS -1)) begin \n"
        + "        // Ergebnis Denormalisiert \n"
        + "        res_denorm = 1;\n"
        + "        // Shift für Exponent zurückgeben: \n"
        + "        exp_r = {(ls_tmp13_reg - (EXP_BIAS - {{(OP_EXPONENT-1){1'b0}}, {1'b1}}) - new_man_a_gr_man_b)};\n"
        + "      end else if (((new_ex_a_plus_sh_b) > (new_ex_b_plus_sh_a)) && new_ex_a_minus_ex_b > (EXP_BIAS + 1 + new_man_b_gr_man_a)) begin \n"
        + "        // Überlauf \n"
        + "        overflow = 1;\n"
        + "      end else begin \n"
        + "        // Ergebnis normalisiert \n"
        + "        if(op_a_exp_0 && op_b_exp_0) begin \n"
        + "          // beide operanden denormalisiert \n"
        + "          exp_r = new_shamt_b_reg_min_shamt_a_reg_BIAS_minus_1;\n"
        + "        end else if ((op_a_exp_0) && ~(op_b_exp_0)) begin \n"
        + "          // a denormalisiert \n"
        + "          exp_r = ls_tmp_BIAS;\n"
        + "        end else if (~(op_a_exp_0) && (op_b_exp_0)) begin \n"
        + "          // b denormalisiert \n"
        + "          exp_r = ((op_a_i_man << shamt_a_reg) == (op_b_i_man << shamt_b_reg)) ? ls_tmp_BIAS_minus_1 : ls_tmp_BIAS_minus_2;\n "
        + "        end else begin \n"
        + "          // kein operand denormalisiert \n"
        + "          exp_r = ls_tmp_BIAS;\n "
        + "        end \n"
        + "      end \n"
        + "    end \n"
        + "    FINISH: begin \n"
        + "      next_state = READY;\n"
        + "    end \n"
        + "  endcase \n"
        + "end \n\n"
        + "function [$clog2(OP_MANTISSA)-1:0] shamt;\n "
        + "  input [OP_MANTISSA-1:0] a;\n"
        + "  integer shift;\n"
        + "begin \n"
        + "  shift = 1;\n "
        + "  while (shift <= OP_MANTISSA && a[OP_MANTISSA-shift] == 0) begin \n"
        + "    shift = shift + 1;\n "
        + "  end \n"
        + "  shamt = shift;\n"
        + "end \n"
        + "endfunction \n"
        + "endmodule\n\n");
    
    // ExceptionCheckerReg
    res.append("module ExceptionCheckerReg_" + tmp.getCanonicalName() + " #(\n"
        + "parameter OP_EXPONENT = 8,\n"
        + "parameter OP_MANTISSA = 23\n"
        + ")(\n"
        + "input wire clk_i,\n"
        + "input wire [OP_EXPONENT + OP_MANTISSA:0] operand_a_i,\n"
        + "input wire [OP_EXPONENT + OP_MANTISSA:0] operand_b_i,\n"
        + "output reg [OP_EXPONENT + OP_MANTISSA:0] result_o,\n"
        + "output reg result_valid_o,\n"
        + "output reg op_denorm_o,\n"
        + "output reg res_denorm_o\n"
        + ");\n"
        + "localparam  OP_WIDTH   = 1 + OP_EXPONENT + OP_MANTISSA,\n"
        + "        POS_ZERO = {1'b0, {(OP_WIDTH-1){1'b0}}},\n"
        + "        NEG_ZERO = {1'b1, {(OP_WIDTH-1){1'b0}}},\n"
        + "        POS_INF = {1'b0, {(OP_EXPONENT){1'b1}}, {(OP_MANTISSA){1'b0}}},\n"
        + "        NEG_INF = {1'b1, {(OP_EXPONENT){1'b1}}, {(OP_MANTISSA){1'b0}}},\n"
        + "        NAN     = {1'b0, {(OP_EXPONENT+OP_MANTISSA){1'b1}}},\n"
        + "        EXP_MAX = {(OP_EXPONENT){1'b1}};\n"
        + "localparam  [OP_EXPONENT-1:0] EXP_BIAS = (2**OP_EXPONENT) / 2 - 1;\n\n"
        + "reg [OP_WIDTH-1:0] result;\n"
        + "reg result_valid;\n"
        + "reg op_denorm;\n"
        + "reg res_denorm;\n"
        + "wire op_a_vz = operand_a_i[OP_WIDTH-1];\n"
        + "wire [OP_EXPONENT-1:0] op_a_exp = operand_a_i[OP_WIDTH-2:OP_MANTISSA];\n"
        + "wire [OP_MANTISSA-1:0] op_a_man = operand_a_i[OP_MANTISSA-1:0];\n"
        + "wire op_b_vz = operand_b_i[OP_WIDTH-1];\n"
        + "wire [OP_EXPONENT-1:0] op_b_exp = operand_b_i[OP_WIDTH-2:OP_MANTISSA];\n"
        + "wire [OP_MANTISSA-1:0] op_b_man = operand_b_i[OP_MANTISSA-1:0];\n"
        + "always @(posedge clk_i) begin \n"
        + "  result_o <= result;\n"
        + "  result_valid_o <= result_valid;\n"
        + "  op_denorm_o <= op_denorm;\n"
        + "  res_denorm_o <= res_denorm;\n"
        + "end \n"
        + "always@(*) begin \n"
        + "  result = 0;\n"
        + "  result_valid = 0;\n"
        + "  op_denorm = 0;\n"
        + "  res_denorm = 0;\n"
        + "  begin \n"
        + "    if((op_a_exp == {(OP_EXPONENT){1'b1}} && op_a_man != 0) ||(op_b_exp == {(OP_EXPONENT){1'b1}} && op_b_man != 0)) begin \n"
        + "      // Double und min. einer der Operanden NaN -> Ergebnis = NaN \n"
        + "      result = NAN; result_valid = 1;\n"
        + "      op_denorm = 0; res_denorm = 0;\n"
        + "    end else if((operand_a_i == POS_INF || operand_a_i == NEG_INF) && (operand_b_i == POS_INF || operand_b_i == NEG_INF)) begin \n"
        + "      // Double, beide Operanden Infinity -> Ergebnis NaN \n"
        + "      result = NAN;\n"
        + "      result_valid = 1;\n"
        + "      op_denorm = 0;\n"
        + "      res_denorm = 0;\n"
        + "    end else if((operand_a_i == POS_ZERO || operand_a_i == NEG_ZERO) && (operand_b_i == POS_ZERO || operand_b_i == NEG_ZERO)) begin \n"
        + "      // Double, beide Operanden 0 -> Ergebnis NaN \n"
        + "      result = NAN;\n"
        + "      result_valid = 1;\n"
        + "      op_denorm = 0;\n"
        + "      res_denorm = 0;\n"
        + "    end else if(operand_a_i == POS_INF || operand_a_i == NEG_INF) begin \n"
        + "      // double, Dividend = Inf -> Ergbenis Inf \n"
        + "      result = op_a_vz ^ op_b_vz ? NEG_INF : POS_INF;\n"
        + "      result_valid = 1;\n"
        + "      op_denorm = 0; res_denorm = 0;\n"
        + "    end else if(operand_a_i == POS_ZERO || operand_a_i == NEG_ZERO) begin \n"
        + "      // double, Dividend = 0 -> Ergbenis 0 \n"
        + "      result = op_a_vz ^ op_b_vz ? NEG_ZERO : POS_ZERO;\n"
        + "      result_valid = 1;\n"
        + "      op_denorm = 0;\n"
        + "      res_denorm = 0;\n"
        + "    end else if(operand_b_i == POS_INF || operand_b_i == NEG_INF) begin \n"
        + "      // double, Divisor = Inf -> Ergbenis 0 \n"
        + "      result = op_a_vz ^ op_b_vz ? NEG_ZERO : POS_ZERO;\n"
        + "      result_valid = 1;\n"
        + "      op_denorm = 0;\n"
        + "      res_denorm = 0;\n"
        + "    end else if(operand_b_i == POS_ZERO || operand_b_i == NEG_ZERO) begin \n"
        + "      // double, Divisor = 0 -> Ergbenis Inf \n"
        + "      result = op_a_vz ^ op_b_vz ? NEG_INF : POS_INF;\n"
        + "      result_valid = 1;\n"
        + "      op_denorm = 0;\n"
        + "      res_denorm = 0;\n"
        + "    end else if(op_a_exp == 0 || op_b_exp == 0) begin \n"
        + "      // Einer der Eingänge ist Denormalisiert \n"
        + "      result_valid = 0;\n"
        + "      op_denorm = 1;\n"
        + "      res_denorm = 0;\n"
        + "     end else if((op_a_exp < op_b_exp) && ((({1'b1,op_a_man} > {1'b1,op_b_man}) && (op_b_exp - op_a_exp > EXP_BIAS + OP_MANTISSA)) //TODO  \n"
        + "          || (({1'b1,op_a_man} < {1'b1,op_b_man}) && (op_b_exp - op_a_exp > EXP_BIAS + OP_MANTISSA - 1)))) begin \n"
        + "      // double Unterlauf -> ergebnis 0 \n"
        + "      result = op_a_vz ^ op_b_vz ? NEG_ZERO : POS_ZERO;\n"
        + "      result_valid = 1;\n"
        + "      op_denorm = 0;\n"
        + "      res_denorm = 0;\n"
        + "    end else if((op_a_exp < op_b_exp) && (op_b_exp - op_a_exp <= EXP_BIAS + OP_MANTISSA)  // TODO \n "
        + "          && ((op_b_exp - op_a_exp > EXP_BIAS - 1) || (({1'b1,op_a_man} < {1'b1,op_b_man}) && (op_b_exp - op_a_exp == EXP_BIAS - 1)))) begin \n"
        + "      // double Denorm \n"
        + "      result_valid = 0;\n"
        + "      op_denorm = 0;\n"
        + "      res_denorm = 1;\n"
        + "     end else if((op_a_exp > op_b_exp) &&   // TODO\n"
        + "          ((({1'b1,op_a_man} > {1'b1,op_b_man}) && (op_a_exp - op_b_exp > EXP_BIAS)) || (({1'b1,op_a_man} < {1'b1,op_b_man}) && (op_a_exp - op_b_exp > EXP_BIAS + 1)))) begin \n"
        + "      // double Überlauf -> Ergebnis INF \n"
        + "      result = op_a_vz ^ op_b_vz ? NEG_INF : POS_INF;\n"
        + "      result_valid = 1;\n"
        + "      op_denorm = 0;\n"
        + "      res_denorm = 0;\n"
        + "    end else if(op_b_man == 0) begin \n"
        + "      // Division durch vielfaches von 2 \n"
        + "      result = {(op_a_vz ^ op_b_vz), (op_a_exp + EXP_BIAS - op_b_exp), op_a_man};\n"
        + "      result_valid = 1;\n"
        + "      op_denorm = 0;\n"
        + "      res_denorm = 0;\n"
        + "    end else if(op_a_man == op_b_man) begin \n"
        + "      //mantissen gleich, nur exponent berechnen \n"
        + "      result = {(op_a_vz ^ op_b_vz), (op_a_exp + EXP_BIAS - op_b_exp), {(OP_MANTISSA){1'b0}}};\n"
        + "      result_valid = 1;\n"
        + "      op_denorm = 0;\n"
        + "      res_denorm = 0;\n"
        + "    end else begin \n"
        + "      result_valid = 0;\n"
        + "      op_denorm = 0;\n"
        + "      res_denorm = 0;\n"
        + "    end \n"
        + "  end \n"
        + "end \n"
        + "endmodule \n");
    
    // ResultMul
    // TODO: check functionality after bitrange fixes due to synthesis errors
    res.append("module result_mul_" + tmp.getCanonicalName() + " #(\n"
        + "parameter MUL_WIDTH = 68,\n"
        + "parameter OP_EXPONENT = 8,\n"
        + "parameter OP_MANTISSA = 23 \n"
        + ")(\n"
        + "input wire clk_i,\n"
        + "input wire enable_i,\n"
        + "input wire reset_i,\n"
        + "input wire isFloat,\n"
        + "input wire [MUL_WIDTH-1:0] d_i,\n"
        + "input wire [MUL_WIDTH-1:0] result_i,\n"
        + "input wire [MUL_WIDTH-1:0] n_i,\n"
        + "output reg [MUL_WIDTH-1:0] result_o,\n"
        + "output reg r_valid_o );\n\n "
        + "localparam  float = OP_EXPONENT + OP_MANTISSA;\n"
        + "wire [2*MUL_WIDTH-1:0] big_result;\n"
        + "assign big_result = d_i * result_i;\n"
        // [Synth 8-524] part-select [135:66] out of range of prefix 'big_result' ["/home/wimi/ae/RS/cgra/CGRA/out/amidar/fp/CGRA_4/DIV_float8x23_38C.v":932]
        //+ "wire [69:0] test = (float == 31) ? big_result[135:66] : (float == 63) ? big_result[102:34] : 70'b0;\n"
        + "wire [69:0] test = (float == 31) ? big_result[85:15] : (float == 63) ? big_result[102:34] : 70'b0;\n"
        + "// ### \n"
        + "wire [35:0]  s_low_new = test[34:0];\n"
        + "reg [34:0] s_high;\n"
        + "reg [34:0] s_low_reg_new;\n"
        + "reg [34:0] s_high_new;\n"
        + "wire [69:0] s_new = {s_high_new, s_low_reg_new};\n"
        + "reg[67:0] r_plus_1;\n"
        + "reg keep_result_new;\n"
        + "reg [16:0] epsilon;\n"
        + "reg [69:0] epsilon_mal_d;\n"
        + "reg [27:0] n_minus_ed;\n"
        + "always @(*) begin \n"
        + "  if(float == 31) begin \n"
        //[Synth 8-524] part-select [46:45] out of range of prefix 'n_i' ["/home/wimi/ae/RS/cgra/CGRA/out/amidar/fp/CGRA_4/DIV_float8x23_38C.v":946]
        //+ "    keep_result_new = (((result_i[MUL_WIDTH-1] ? s_new[45:20] : s_new[45:20]) > n_minus_ed[27:2]) || (s_new[47:46] == n_i[46:45] && |s_new[45:35] == 0));\n"
        + "    keep_result_new = (((result_i[MUL_WIDTH-1] ? s_new[45:20] : s_new[45:20]) > n_minus_ed[27:2]) || (s_new[47:46] == n_i[42:41] && |s_new[45:35] == 0));\n"
        + "  end else if (float == 63) begin \n"
        + "    keep_result_new = result_i[MUL_WIDTH-1] ? (s_new[50:25] >= n_minus_ed[25:0]) : (s_new[50:27] >= n_minus_ed[23:0]);\n"
        + "  end else begin\n"
        + "    keep_result_new = 1;\n"
        + "  end\n"
        + "end \n\n"
        + "reg [1:0] state;\n"
        + "localparam READY  = 2'b00,\n"
        + "        MUL    = 2'b01,\n"
        + "        DIF    = 2'b10,\n"
        + "        FINISH = 2'b11;\n\n "
        + "always @(posedge clk_i) begin \n"
        + "  if (reset_i || ~enable_i) begin \n"
        + "    r_valid_o <= 0;\n"
        + "    state <= READY;\n"
        + "  end else if (state == READY && enable_i) begin \n"
        + "    r_valid_o <= 0;\n"
        + "    if (float == 31) begin \n"
        + "      epsilon <= {1'b1, 25'b0} - result_i[42:19];\n"
        + "    end else if (float == 63)begin \n"
        + "      epsilon <= result_i[MUL_WIDTH-1] ? {({1'b1, 14'b0} - result_i[13:0]), 3'b0} : {({1'b1, 13'b0} - result_i[13:0]), 1'b0};\n"
        + "    end else begin \n"
        + "      epsilon <= 0;\n"
        + "    end \n"
        + "    r_plus_1 <= result_i + ((float == 31) ? {1'b1, 40'b0} : (float == 63) ? {1'b1, 10'b0} : 0);\n"
        + "    state <= MUL;\n"
        + "  end else if(state == MUL && enable_i) begin \n"
        + "    epsilon_mal_d <= epsilon * d_i[MUL_WIDTH-1:MUL_WIDTH-1-OP_MANTISSA];\n"
        + "    state <= DIF;\n"
        + "  end else if(state == DIF && enable_i) begin \n"
        + "    s_low_reg_new <= s_low_new[34:0];\n"
        + "    s_high_new <= test[69:35] + s_low_new[35];\n"
        + "    if(float == 31) begin \n"
        // [Synth 8-524] part-select [47:17] out of range of prefix 'n_i' ["/home/wimi/ae/RS/cgra/CGRA/out/amidar/fp/CGRA_4/DIV_float8x23_38C.v":982]
        //+ "      n_minus_ed <= (result_i[MUL_WIDTH-1] ? n_i[47:17] : n_i[47:17]) - epsilon_mal_d[69:50];\n"
        + "      n_minus_ed <= (result_i[MUL_WIDTH-1] ? n_i[42:12] : n_i[42:12]) - epsilon_mal_d[69:50];\n"
        + "    end else if (float == 63) begin \n"
        + "      n_minus_ed <= (result_i[MUL_WIDTH-1] ? {n_i[17:0], 8'b0} : {n_i[17:0], 6'b0})- epsilon_mal_d[69:47];\n"
        + "    end else begin \n"
        + "      n_minus_ed <= 0;\n"
        + "    end \n"
        + "    state <= FINISH;\n"
        + "  end else if (state == FINISH && enable_i) begin \n"
        + "    if(keep_result_new) begin \n"
        + "      result_o <= result_i;\n"
        + "    end else begin \n"
        + "      result_o <= r_plus_1;\n"
        + "    end \n"
        + "    r_valid_o <= 1;\n"
        + "    state <= READY;\n"
        + "  end \n"
        + "end \n");
        //+ "endmodule");
    
    return res.toString();
  }
  
  
  public static void main(String[] args) {
    double[] list = new double[] {Double.NaN,Double.POSITIVE_INFINITY,Double.NEGATIVE_INFINITY,+0.0,-0.0,1.0,-1.0};
    for (double a : list) for (double b : list) System.out.println(a + " / " + b + " = " + (a/b));
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