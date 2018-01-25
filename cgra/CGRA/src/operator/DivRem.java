package operator;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;

import accuracy.Format;
import accuracy.Format.FloatingPoint;

/**
 * Binary division and remainder: N = Q * D + R. The actual result is selected
 * by subclass.
 *
 * <h4>Nomenclature</h4>
 * <ul>
 * <li>N: numerator
 * <li>D: denominator
 * <li>Q: quotient = N/D = max q in Z : q*D < N
 * <li>R: remainder = N%D < D
 * </ul>
 *
 * <h4>Modulus versus Remainder</h4> The difference arises from two different
 * truncation definitions of integer divisions:
 * <ul>
 * <li>N divZ D => integer division with truncation torwards zero
 * <li>N divN D => integer division with truncation torwards -inf
 * </ul>
 * Modulus and remainder are the counter parts to these operations, i.e.
 * <ul>
 * <li>N = (N divZ D) * D + (N rem D)
 * <li>N = (N divN D) * D + (N mod D)
 * </ul>
 *
 * As long as signum(N) == signum(D) (i.e. Q >= 0), both interpretations behave
 * the same: <table border>
 * <tr>
 * <th>N</th>
 * <th>D</th>
 * <th>N divZ D</th>
 * <th>N rem D</th>
 * <th>N divN D</th>
 * <th>N mod D</th>
 * </tr>
 * <tr>
 * <td>5</td>
 * <td>3</td>
 * <td>1</td>
 * <td>2</td>
 * <td>1</td>
 * <td>2</td>
 * </tr>
 * <tr>
 * <td>5</td>
 * <td>-3</td>
 * <td>-1</td>
 * <td>2</td>
 * <td>-2</td>
 * <td>-1</td>
 * </tr>
 * <tr>
 * <td>-5</td>
 * <td>3</td>
 * <td>-1</td>
 * <td>-2</td>
 * <td>-2</td>
 * <td>1</td>
 * </tr>
 * <tr>
 * <td>-5</td>
 * <td>-3</td>
 * <td>1</td>
 * <td>-2</td>
 * <td>1</td>
 * <td>-2</td>
 * </tr>
 * <tr>
 * <td>-5</td>
 * <td>0</td>
 * <td>Error</td>
 * <td>Error</td>
 * <td>Error</td>
 * <td>5</td>
 * </tr>
 * <tr>
 * <td>-5</td>
 * <td>0</td>
 * <td>Error</td>
 * <td>Error</td>
 * <td>Error</td>
 * <td>-5</td>
 * </tr>
 * </table>
 *
 * In Java
 * <ul>
 * <li>long / long performs the divZ operation
 * <li>long % long performs the rem operation (also it is often called "modulo")
 * </ul>
 *
 * <h4>Selection of Algorithm</h4> The division algorithm can be selected to
 * trade off latency against the required resources.
 *
 * All algorithms perform an initial sign analysis and convert the inputs such
 * that N>=0 and D>0. Afterwards, the sign of Q and R are restored
 * appropriately.
 *
 * <table border>
 * <tr>
 * <th>Algorithm</th>
 * <th>Idea</th>
 * <th>PRO</th>
 * <th>CON</th>
 * </tr>
 * <tr>
 * <td>Euclidean division</td>
 * <td>
 * <ul>
 * <li>Q=0, R=N
 * <li>while (R>=D) {Q++; R-=D;}
 * </ul>
 * </td>
 * <td>
 * <ul>
 * <li>Simple (no multiplication)</td>
 * <td>
 * <ul>
 * <li>input sensitive runtime
 * <li>no speedup by higher radix
 * </ul>
 * </td>
 * </tr>
 * <tr>
 * <td>Restoring division = Long division</td>
 * <td>
 * <ul>
 * <li>try to set quotient bits q (MSB to LSB)
 * <li>subtract q*D from partial remainder
 * <li>restore partial remainder, if it became negative
 * </ul>
 * </td>
 * <td>
 * <ul>
 * <li>simple for radix 2 (paper and pen method)
 * <li>no concluding normalization step required, as for non-restoring division
 * </td>
 * <td>
 * <ul>
 * <li>higher radix can not be simplified by "guessing" the correct bits as for
 * Non-restoring division
 * <li>multiple parallel multiplications + subtraction to find correct quotient
 * bits
 * <li>unbalanced cycles (for restore/non restore), but can be implemented with
 * static latency ("non-performing")
 * </ul>
 * </td>
 * </tr>
 * <tr>
 * <td>Non-restoring division</td>
 * <td>
 * <ul>
 * <li>guess quotient bits q (MSB to LSB)
 * <li>subtract q*D from partial remainder
 * <li>mark bits a negative, if partial remainder became negative
 * </ul>
 * </td>
 * <td>
 * <ul>
 * <li>balanced cycles (input independent)
 * <li>guessing by LUT (SRT division)
 * <li>exactly one MAC has to be performed per cycle
 * <li>radix 4 => only shift and add/sub</td>
 * <td>
 * <ul>
 * <li>concluding normalization step required to get rid of negative digits
 * </ul>
 * </td>
 * </tr>
 * <tr>
 * <td>Newton-Raphson division</td>
 * <td>
 * <ul>
 * <li>approximate 1/D by X0=(48-32*D)/17; Xi+1=Xi(2-DXi)
 * <li>Q = N * Xk
 * </ul>
 * </td>
 * <td>
 * <ul>
 * <li>quadratic convergence (k ~= 4 cycles for 64 bit result)
 * <li>fixed latency possible
 * <li>based on simple MAC operations</td>
 * <td>
 * <ul>
 * <li>no speed up by higher radix
 * <li>no automatic remainder calculation
 * <li>fractional precision (MAC) required even for integer division
 * <li>initial scaling to 0.5 < D < 1 required
 * </ul>
 * </td>
 * </tr>
 * <tr>
 * <td>Goldschmidt division</td>
 * <td>
 * <ul>
 * <li>sequentially multiply N and D by common factors Fi, until D*F0*...*Fk ~ 1
 * <li>Binomial (IBM) method: Fi = 1+(1-D)^2^i for 0.5<D<1
 * </ul>
 * </td>
 * <td>
 * <ul>
 * <li>fast convergence (k ~= 6 cycles for 64 bit result)
 * <li>fixed latency possible
 * <li>based on simple MUL operations</td>
 * <td>
 * <ul>
 * <li>no speed up by higher radix
 * <li>no automatic remainder calculation
 * <li>fractional precision (MAC) required even for integer division
 * <li>initial scaling to 0.5 < D < 1 required
 * </ul>
 * </td>
 * </tr>
 * </table>
 *
 * Division algorithms considerable for a CGRA operation have to provide a
 * static latency (i.e., not depending on the actual numeric inputs) for proper
 * scheduling. Supporting a speed vs. resource trade-off by radix selection (as
 * for the digit recurrence methods) are be beneficial, but not essential. In
 * fact, the optimal algorithm must be selected based on the configured output
 * precision.
 *
 *
 * @author Andreas Engel [engel@esa.cs.tu-darmstadt.de]
 * @see <a href='http://stackoverflow.com/questions/5891140/difference-between-mod-and-rem-in-haskell'>MOD vs REM</a>
 * @see <a href='http://research.microsoft.com/pubs/70645/tr-2008-141.pdf'>Unsigned Integer Newton-Raphson</a>
 */
public abstract class DivRem extends Binary {

  /** Unique ID for (de)marshalling */
  private static final long serialVersionUID = -3815550440643443410L;

  protected DivRem(Format a, Format b, Format r) {
    super(a, b, r, "divrem");
    fitAlgorithm();
  }

/*
 * Algorithm configuration
 **********************************************************************************************************************/

  /**
   * Type of division algorithm
   *
   * @author Andreas Engel [engel@esa.cs.tu-darmstadt.de]
   */
  public static enum Algorithm {
    Combinatorial, /// < builtin single cycle division
    Restoring,     /// < radix 2 (non performing) restoring digit recurrence (|N| iterations)
    NonRestoring4, /// < radix 4 non restoring digit recurrence              (|N|/2 iterations)
    NewtonRaphson, /// < Newton-Raphson functional recurrence
    Goldschmidt;
    
    public int getLatency(Implementation g) {
      int rw = g.getOperandFormat(0).getBitWidth(); // quotient size determined by nominator
      switch (this) {
        case Combinatorial: return 1;
        case NewtonRaphson: return bitwidth(rw) + 2;
        case NonRestoring4: return (rw + 1) / 2;
        case Goldschmidt:   return 38;	// muss berechnet werden!
      default:              return rw + 2;
      }
      // +2 = additional cycles for register preparation and final sign selection
    }
  }

  protected Algorithm algorithm; /// < the selected division algorithm

  /**
   * Get the configured division algorithm If no algorithm was selected yet,
   * the optimal algorithm for the current I/O configuration is returned
   *
   * @return
   */
  public Algorithm getAlgorithm() {
    return algorithm == null ? getOptimalAlgorithm() : algorithm;
  }

  /**
   * Select the division algorithm
   *
   * @param algorithm
   */
  public void setAlgorithm(Algorithm algorithm) {
    this.algorithm = algorithm;
  }

  /**
   * Select the best algorithm for the current I/O precision
   */
  public Algorithm getOptimalAlgorithm() {
    return getCommonFormat() instanceof FloatingPoint ? Algorithm.Goldschmidt : Algorithm.NewtonRaphson;
  }

  /**
   * Configure the best algorithm for the current I/O precision
   */
  public void fitAlgorithm() {
    setAlgorithm(getOptimalAlgorithm());
  }

  /**
   * Also parse the division algorithm from JSON configuration: algo[rithm] :
   * Combinatorial | Restoring | NonRestoring4 | NewtonRaphson
   */
  @Override
  public void configure(JSONObject o) {
    super.configure(o);

    Object a = o.get("algorithm");
    if (a == null) {
      a = o.get("algo");
    }
    if (a != null) {
      setAlgorithm(Algorithm.valueOf(a.toString()));
    }
  }
  
  @Override
  @SuppressWarnings("unchecked")
  public JSONObject getJSONDescription() {
    JSONObject res = super.getJSONDescription();
    res.put("algo", getAlgorithm().toString());
    return res;
  }

  /**
   * If the selected algorithm is not the optimal algorithm, this must be
   * reflected by the module name
   */
  @Override
  public String getName() {
    String res = super.getName();
    if (getAlgorithm() != getOptimalAlgorithm()) {
      res += "_" + getAlgorithm();
    }
    return res;
  }

  /**
   * Show algorithm also in module header comment
   */
  @Override
  protected Map<String, Object> getModuleHeaderInfo() {
    Map<String, Object> info = super.getModuleHeaderInfo();
    info.put("algorithm", getAlgorithm());
    return info;
  }

//  /**
//   * Add algorithm as DOF for design space exploration
//   */
//  @Override
//  public void setFirstConfiguration() {
//    setAlgorithm(Algorithm.values()[0]);
//    super.setFirstConfiguration();
//  }

//  /**
//   * Add algorithm as DOF for design space exploration
//   */
//  @Override
//  public boolean setNextConfiguration() {
//    int i = getAlgorithm().ordinal();
//    Algorithm[] vals = Algorithm.values();
//    if (i + 1 < vals.length) {
//      setAlgorithm(vals[i + 1]);
//      fitLatency();
//      return true;
//    } else {
//      setAlgorithm(vals[0]);
//      return super.setNextConfiguration();
//    }
//  }

  /*
   * Concrete implementation
   **********************************************************************************************************************/

  /**
   * Reading inputs, performing division and writing outputs have to be
   * performed sequentially
   */
  @Override
  public int getMinLatency() {
    return getInputLatency() - 1 + getAlgorithm().getLatency(this) - 1 + getOutputLatency();
  }
  
  @Override 
  public List<Integer> getSupportedIntegerLatency() {
    return new LinkedList<Integer>(Arrays.asList(getMinLatency()));
  }

  @Override
  public String getIntegerImplementation() {

    // frequently used properties
    Algorithm a = getAlgorithm();
    String[] op = new String[] {getOperandPort(0).toString(), getOperandPort(1).toString()};
    int nw = getOperandFormat(0).getBitWidth();
    int dw = getOperandFormat(1).getBitWidth();
    int rw = getResultFormat().getBitWidth();
    int startCycle = getInputLatency() - 1;
    int divCycles = a.getLatency(this);
    boolean outBuf = getOutputLatency() > 1 || getLatency() > getMinLatency();
    boolean inAbs = (getOperandFormat(0).isSigned() || getOperandFormat(1).isSigned())
        && a != Algorithm.Combinatorial;

    // sanity checks
    if (getLatency() < getMinLatency()) {
      throw new InvalidConfigException("latency=" + getLatency());
    }
    if (nw != dw) {
      throw new NotImplementedException("unbalanced inputs"); // not really tested yet
    }

    StringBuilder res = new StringBuilder();

    // I/O buffers
    for (int i = 0; i < getNumberOfOperands(); i++) {
      int firstAccess = startCycle;
      int lastAccess = startCycle;
      int lastDivCycle = startCycle + divCycles - 1;
      switch (a) {
      case Restoring:
        if (i == 1) {
          lastAccess = lastDivCycle;
        }
        break;
      case NewtonRaphson:
        lastAccess = lastDivCycle;
        break;
      default:
        break;
      }
      // numerator is buffered in partialRemainder, but denominator is
      // required until end of division
      if (firstAccess > 0 || lastAccess > firstAccess) {
        res.append(getOperandBuffer(i, firstAccess, lastAccess));
        op[i] += "_buffered";
      }
    }
    if (outBuf) {
      res.append(getResultBuffer(0, startCycle + divCycles - 1));
    }

    // sign analysis and conversion to unsigned required for sequential
    // algorithms
    if (inAbs) {
      for (int i = 0; i < getNumberOfOperands(); i++) {
        Format f = getOperandFormat(i);
        if (f.isSigned()) {
          int w = f.getBitWidth();
          if (keepSign(i)) {
            res.append("reg  " + getOperandPort(i) + "_sign;\n");
            res.append("always @(posedge " + CLOCK + ") begin\n");
            res.append("  if (cycle == " + startCycle + ") " + getOperandPort(i) + "_sign <= " + op[i] + "["
                + (w - 1) + "];\n");
            res.append("end\n");
          }
          res.append("wire " + bitRange(w) + " " + op[i] + "_abs = " + op[i] + "[" + (w - 1) + "] ? -" + op[i]
              + " : " + op[i] + ";\n\n");
          op[i] += "_abs";
        }
      }
    }

    // algorithm specific result preparation (not actually writing to the
    // output)
    switch (a) {
    case Combinatorial:
      // all arithmetic will be generated by subclasses
      break;

    case Restoring:
      // sequential shift and subtract
      res.append("wire " + bitRange(dw + nw) + " denominator = {" + op[1] + "," + nw + "'b0};\n");
      res.append("reg  " + bitRange(nw + nw) + " partialRemainder;\n");
      res.append("wire " + bitRange(nw + nw) + " partialRemainderX2 = {partialRemainder" + bitRange(nw + nw - 1)
          + ",1'b0};\n");
      if (keepQuotient()) {
        res.append("reg  " + bitRange(rw) + " quotient;\n");
        res.append("wire " + bitRange(rw) + " quotientX2 = {quotient" + bitRange(rw - 1) + ",1'b0};\n");
      }
      res.append("always @(posedge " + CLOCK + ") begin\n");
      res.append("  if (cycle == " + startCycle + ") begin\n");
      res.append("    partialRemainder <= " + op[0] + ";\n");
      if (keepQuotient()) {
        res.append("    quotient         <= " + 0 + ";\n");
      }
      res.append("  end else if (partialRemainderX2 >= denominator) begin\n");
      res.append("    partialRemainder <= partialRemainderX2 - denominator;\n");
      if (keepQuotient()) {
        res.append("    quotient         <= quotientX2 | 1;\n");
      }
      res.append("  end else begin\n");
      res.append("    partialRemainder <= partialRemainderX2;\n");
      if (keepQuotient()) {
        res.append("    quotient         <= quotientX2;\n");
      }
      res.append("  end\n");
      res.append("end\n\n");
      break;

    case NewtonRaphson:
      // start recurrence at 1<<leading zeros(D)
      res.append("wire " + bitRange(dw + 1) + " z0;\n");
      res.append(getName() + "_shiftByLeadingZero #(.width(" + dw + ")) init_z (.a(" + op[1] + "), .r(z0));\n\n");

      // sequential recurrence: z += highWord(z * lowWord(-D*z))
      // /z converges against inverse of D, i.e., largest number, such
      // that z*D < 2^dw
      res.append("reg  " + bitRange(dw) + " z;\n");
      res.append("wire " + bitRange(dw) + " mDxZ = -" + op[1] + " * z;\n");
      res.append("wire " + bitRange(2 * dw) + " step = mDxZ * z;\n");
      res.append("always @(posedge " + CLOCK + ") begin\n");
      res.append("  if (cycle == " + startCycle + ") begin\n");
      res.append("    z <= z0;\n"); // MSB of z0 only set for D=0 => can
                      // be ignored
      res.append("  end else begin\n");
      res.append("    z <= z + step" + bitRange(2 * dw - 1, dw) + ";\n"); // MSB
                                        // of
                                        // z0
                                        // only
                                        // set
                                        // for
                                        // D=0
                                        // =>
                                        // can
                                        // be
                                        // ignored
                                        // +
                                        // ;\n");
      res.append("  end\n");
      res.append("end\n");

      // derive quotient and remainder from z
      res.append("wire " + bitRange(nw + dw) + " Nxz  = " + op[0] + " * z;\n");
      res.append("wire " + bitRange(nw) + " quot = Nxz" + bitRange(nw + dw - 1, dw) + ";\n");
      res.append("wire " + bitRange(nw) + " prod = " + op[1] + " * quot;\n");
      res.append("wire " + bitRange(nw) + " rem  = " + op[0] + " - prod;\n");
      break;

    default:
      throw new NotImplementedException("algorithm=" + a);
    }

    // subclass specific result selection (quotient or remainder)
    res.append(getResultSelection(outBuf, op[1]));
    res.append(RESULT_VALID.getAssignment("cycle == " + (startCycle+divCycles) + " ? 1 : 0"));
    
    return res.toString();
  }

  /**
   * Append submodule for deriving (1<<leading zeros) as required by
   * NewtonRaphson
   *
   * @return
   */
  @Override
  public String getModule() {
    StringBuilder res = new StringBuilder(super.getModule());
    if (getAlgorithm() == Algorithm.NewtonRaphson) {
      String mod = getName() + "_shiftByLeadingZero";
      res.append("\n\nmodule " + mod + "\n");
      res.append(" #(parameter width = 1)\n");
      res.append("  (input  wire [width-1:0] a,\n");
      res.append("   output wire [width  :0] r);\n");
      res.append("  generate\n");
      res.append("    if (width == 1) begin\n");
      res.append("      assign r = a[0] ? 1 : 2;\n");
      res.append("    end else begin\n");
      res.append("      localparam wHigh = width/2;\n");
      res.append("      localparam wLow  = width-wHigh;\n");
      res.append("      wire [wHigh:0] high;\n");
      res.append("      wire [wLow :0] low;\n");
      res.append("      " + mod + " #(.width(wHigh)) mHigh (.a(a[width-1:wLow]), .r(high));\n");
      res.append("      " + mod + " #(.width(wLow))  mLow  (.a(a[wLow -1:0   ]), .r(low));\n");
      res.append("      assign r = high[wHigh] ? {low,{wHigh{1'b0}}} : {{wLow{1'b0}},high};\n");
      res.append("    end\n");
      res.append("  endgenerate\n");
      res.append("endmodule\n");
    }
    return res.toString();
  }

  /**
   * Map intermediate signals to result (quotient or remainder).
   *
   * @param outBuf
   * @param denominator
   * @return
   */
  protected abstract String getResultSelection(boolean outBuf, String denominator);

  /**
   * Select, which input sign is required to derive the output sign
   *
   * @param operandIndex
   * @return
   */
  protected boolean keepSign(int operandIndex) {
    return true;
  }

  /**
   * Select, if quotient is required to derive output
   *
   * @return
   */
  protected boolean keepQuotient() {
    return true;
  }

//  /**
//   * Ensure non zero denominator for testing and smaller denominators for
//   * later test runs (to get larger quotients)
//   */
//  @Override
//  protected Number getTestInput(int operandIndex, int run) {
//    while (true) {
//      Number res = super.getTestInput(operandIndex, run);
//      if (operandIndex != 1) {
//        return res;
//      }
//      if (res instanceof BigInteger) {
//        res = ((BigInteger) res).divide(new BigDecimal(run + 1).toBigInteger());
//      } else if (res instanceof BigDecimal) {
//        res = ((BigDecimal) res).divide(new BigDecimal(run + 1));
//      } else if (res instanceof Double || res instanceof Float) {
//        res = res.doubleValue() / (run + 1);
//      } else {
//        res = res.longValue() / (run + 1);
//      }
//      if (res.longValue() != 0) {
//        return res;
//      }
//    }
//  }
  
 
  

  /**
   * Testing difference between REM and MOD
   *
   * @param args
   */
  public static void main(String[] args) {
    System.out.println(String.format("%3s%3s%5s%5s%5s%5s", "N", "D", "divZ", "rem", "mod", "divN"));
    for (int[] p : new int[][] { new int[] { 5, 3 }, new int[] { 5, -3 }, new int[] { -5, 3 }, new int[] { -5, -3 },
        new int[] { 5, 0 }, new int[] { -5, 0 }, }) {
      String res = String.format("%3d%3d", p[0], p[1]);
      int rem = 0, mod = 0;
      try {
        res += String.format("%5d", p[0] / p[1]);
      } catch (Exception e) {
        res += "    E";
      }
      try {
        res += String.format("%5d", rem = p[0] % p[1]);
      } catch (Exception e) {
        res += "    E";
      }
      // http://stackoverflow.com/a/18935194,
      // http://www.mathworks.com/matlabcentral/newsreader/view_thread/16027
      try {
        res += String.format("%5d", mod = (p[1] == 0 ? p[0] : (rem + p[1]) % p[1]));
      } catch (Exception e) {
        res += "    E";
      }
      try {
        res += String.format("%5d", (p[0] - mod) / p[1]);
      } catch (Exception e) {
        res += "    E";
      }
      System.out.println(res);
    }
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