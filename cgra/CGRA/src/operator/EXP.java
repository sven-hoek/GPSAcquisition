package operator;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;

import accuracy.Format;
import accuracy.Range;

/**
 * Exponential function: R = exp(A)
 *
 * <h4>Selection of Algorithm</h4>
 * The following iterative algorithms approximate {@code exp(A)} with {@code N} bit precision. The taylor series
 * expansions however require lots of iterations for large absolute inputs. {@code exp(-a) = 1 / exp(a)} and
 * {@code exp(A) = 2^k 3^l 5^m exp(A - k ln(2) - l ln(3) - m ln(5))} should thus be applied to transform the input 
 * domain to to a small interval around zero.
 * <table border>
 *   <tr>
 *     <th>Algorithm</th>
 *     <th>Idea</th>
 *     <th>PRO</th>
 *     <th>CON</th>
 *   </tr>
 *   <tr>
 *     <td>{@link #taylor} series</td>
 *     <td><pre>exp(A) = sum_{k=0}^m A^k / k!</pre></td>
 *     <td>fast convergence near zero</td>
 *     <td>requires division and complex pre/post scaling, {@code m} upto 18 required</td>
 *   </tr>
 *   <tr>
 *     <td>Limit</td>
 *     <td><pre>exp(A) = (1+A/m)^m for m->+inf</pre></td>
 *     <td>parallelizable, as no accumulation or partial sums</td>
 *     <td>base of power series changes, slow convergence:<br> {@code (1-0.1/1e10)^1e10-exp(0.1) = 9e-9}</td>
 *   </tr>
 *   <tr>
 *     <td>CORDIC</td>
 *     <td></td>
 *     <td>only shift/add operations</td>
 *     <td></td>
 *   </tr>
 *   <tr>
 *     <td>BKM</td>
 *     <td></td>
 *     <td>only shift/add operations</td>
 *     <td></td>
 *   </tr>
 * </table>
 * <p>
 * @author Andreas Engel [engel@esa.cs.tu-darmstadt.de]
 */
public class EXP extends Unary {

  /** Unique ID for (de)marshalling */
  private static final long serialVersionUID = -768208385598721981L;

  /**
   * Generate {@code Implementation} of a exponentiation {@code Operator}.
   * @param a input precision
   * @param r output precision
   */
  public EXP(Format a, Format r) {
    super(a, r, "exp");
  }

  @Override
  public Number apply(long a)       {return apply((double) a);}
  @Override
  public Number apply(double a)     {return Math.exp(a);}
  @Override
  public Number apply(BigInteger a) {return apply(new BigDecimal(a));}
  @Override
  public Number apply(BigDecimal a) {return approximate(a, MathContext.DECIMAL128);}
  
  /**
   * Approximate the exponential function.
   * Based on the {@link #taylor} series expansion.
   * @param a   the arithmetic input
   * @param mc  the decimal precision and {@link RoundingMode} to apply. 
   * @return    {@code sqrt(a)}
   */
  public static BigDecimal approximate(BigDecimal a, MathContext mc) {
    switch (mc.getRoundingMode()) {
      case FLOOR   : // use more precision in iterative approximation to leave rounding control to last step 
      case CEILING : return approximate(a, MathContext.DECIMAL128).round(mc);
      default      : return taylor(a, mc); 
    }
  }
  
/*
 * Range propagation
 *********************************************************************************************************************/

  
  @Override
  public Range getResultRange(Range.IA a) {
    // see Stolfi1997 page 30
    // TODO: exp(-inf) => Double.MIN_NORMAL (0 is actually never reached)
    // TODO: exp(>709.8) => +inf (overflow for for double precision)
    return Range.generate(a.lo() == null ? BigDecimal.ZERO : approximate(a.lo(), Range.FLOOR),
                          a.hi() == null ? null            : approximate(a.hi(), Range.CEILING));
  }
  
/*
 * Iterative approximation
 *********************************************************************************************************************/
  
  /**
   * Estimate the exponential function by the taylor series {@code exp(a) = sum_{k=0}^m a^k / k!}.
   * <p>
   * Transforms argument to ]-ln(2),ln(2)[ interval by {@code exp(a) = 2^pre exp(a - pre ln(2))} and then expands the
   * taylor series for {@code m} appropriate to reach the required precision. The input transformation is finally 
   * compensated by scaling with {@code 2^pre}.
   * <p>
   * For 16 digit precision (i.e. IEEE 754 double), the following number of iterations are required:
   * <table border=1>
   *   <tr><th> a </th><td>-1.0</td><td>-0.9</td><td>-0.8</td><td>-0.7</td><td>-0.6</td><td>-0.5</td><td>-0.4</td>
   *                   <td>-0.3</td><td>-0.2</td><td>-0.1</td><td> 0.0</td><td> 0.1</td><td> 0.2</td><td> 0.3</td>
   *                   <td> 0.4</td><td> 0.5</td><td> 0.6</td><td> 0.7</td><td> 0.8</td><td> 0.9</td><td> 1.0</td></tr>
   *   <tr><th>pre</th><td>  -1</td><td>  -1</td><td>  -1</td><td>  -1</td><td>   0</td><td>   0</td><td>   0</td>
   *                   <td>   0</td><td>   0</td><td>   0</td><td>   0</td><td>   0</td><td>   0</td><td>   0</td>
   *                   <td>   0</td><td>   0</td><td>   0</td><td>   1</td><td>   1</td><td>   1</td><td>   1</td></tr>
   *   <tr><th> m </th><td>  14</td><td>  13</td><td>  11</td><td>   8</td><td>  17</td><td>  16</td><td>  15</td>
   *                   <td>  14</td><td>  13</td><td>  11</td><td>   0</td><td>  11</td><td>  13</td><td>  14</td>
   *                   <td>  15</td><td>  16</td><td>  17</td><td>   8</td><td>  11</td><td>  13</td><td>  14</td></tr>
   * </table>
   * <p>
   * @param a   the arithmetic input
   * @param mc  the decimal precision and {@link RoundingMode} to apply. 
   * @return    {@code exp(a)} 
   */
  private static BigDecimal taylor(BigDecimal a, MathContext mc) {
    if (a.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ONE;

    // additional accuracy for intermediate results
    int precision = mc.getPrecision();
    mc = new MathContext(precision+2, mc.getRoundingMode());
    
    // transform input domain to ]-ln(2),ln(2)[
    BigDecimal[] divrem = a.divideAndRemainder(Logarithm.LN.TWO, mc);
    int pre = divrem[0].intValue();
    a = divrem[1];
    
    BigDecimal thr = BigDecimal.ONE.scaleByPowerOfTen(-precision);             // accuracy threshold for q
    BigDecimal k   = BigDecimal.ONE;                                           // running index
    BigDecimal fak = BigDecimal.ONE;                                           // accumulator for k!
    BigDecimal num = BigDecimal.ONE;                                           // accumulator for a^k
    BigDecimal q   = num;                                                      // holds a^k/k!
    BigDecimal est = q;                                                        // accumulator for partial sums
    
    while (!approximationConverged(est, q, thr, mc)) {
      num = num.multiply(a, mc);
      fak = fak.multiply(k, mc);
      q   = num.divide(fak, mc);
      k   = k.add(BigDecimal.ONE, mc);
      est = est.add(q, mc);
    };
    
    // compensate initial scaling
    if (pre < 0) return est.divide  (BigDecimal.valueOf(2).pow(-pre,mc), mc);
    if (pre > 0) return est.multiply(BigDecimal.valueOf(2).pow( pre,mc), mc);
    return est;
  }
  
  
/*
 * Debugging
 *********************************************************************************************************************/
  
  /**
   * Test iterative approximation.
   * @param args ignored
   */
  public static void main(String[] args) {
    testApproximations(-1000000, 1000000, -4, Verbosity.STATISTICS, null, new Test[] {
                                 // description,                       calculation, compare,            math context
      new Test(a->"Math.exp   ("+a+")         ", (a,mc)->Math.exp(a.doubleValue()),    null,                   null),
      new Test(a->"approximate("+a+", DECI128)", (a,mc)->approximate(a, mc),              0, MathContext.DECIMAL128),
      new Test(a->"approximate("+a+", CEILING)", (a,mc)->approximate(a, mc),              1,          Range.CEILING),
      new Test(a->"approximate("+a+", FLOOR)  ", (a,mc)->approximate(a, mc),              1,          Range.FLOOR  ),
      new Test(a->"taylor     ("+a+", DECI64) ", (a,mc)->taylor     (a, mc),              1,  MathContext.DECIMAL64)
    });
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