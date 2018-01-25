package operator;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;

import accuracy.Format;
import accuracy.Range;

/**
 * Square root: R = sqrt(A)
 * 
 * <h4>Selection of Algorithm</h4>
 * The following iterative algorithms approximate a square root of the value {@code A} with {@code N} bit precision.
 * All algorithm with quadratic convergence would highly benefit from more precise initial estimations of 
 * {code sqrt(a)} and {@code 1/sqrt(a)}. Worst case analysis over the whole input range have to be carried out to 
 * achieve constant runtime.
 * 
 * <table border>
 *   <tr>
 *     <th>Algorithm</th>
 *     <th>Idea</th>
 *     <th>PRO</th>
 *     <th>CON</th>
 *   </tr>
 *   <tr>
 *     <td>Pen and Paper</td>
 *     <td>Set bits in result register (MSB to LSB) and test, whether square of result is smaller than {@code A}</td>
 *     <td>Simple (no division)</td>
 *     <td>Slow (linerar convergence)</td>
 *   </tr>
 *   <tr>
 *     <td>{@link #newton} Raphson</td>
 *       <td>Find root of <code>f(x)=x^2 - A => sqrt(A)</code>
 *<pre>
 *  e=max(A,1);
 *  do 
 *    o = e;
 *    e = (o + A/o)/2;
 *  while (e < o)
 *  sqrt(A) = e;
 *</pre>
 *     <td>Fast (quadratic convergence)</td>
 *     <td>Requires Divider</td>
 *   </tr>
 *   <tr>
 *     <td>Division-free Newton Raphson ({@link #newtonDF})</td>
 *     <td>Find root of <code>f(x)=1/x^2 - A => 1/sqrt(A)</code>, Multiply by {@code A}
 *<pre>
 *  e = 1/max(A,1)
 *  do 
 *    o = e;
 *    e = o/2 (3 - A o²);
 *  while (e > o)
 *  sqrt(A) = A*e;
 *</pre>
 *     </td>
 *     <td>Fast (quadratic convergence), no divider</td>
 *     <td>large multipliers</td>
 *   </tr>
 *   <tr>
 *     <td>{@link #goldschmidt}</td>
 *     <td>if {@code A*y0²*...yk² = 1} then {@code A*y0*...yk = SQRT(A)}
 *<pre>
 *  y  = 1/max(A,1)
 *  b  = A;
 *  g  = A * y;
 *  do
 *    b = b * y²;
 *    y = (3-b)/2;
 *    g = g * y;
 *  while b != 1
 *  sqrt(A) = g;
 *</pre>
 *     </td>
 *     <td>Good potential for parallelization, quadratic convergence</td>
 *     <td>no self correction (roundoff errors may explode)</td>
 *   </tr>
 *   <tr>
 *     <td>CORDIC</td>
 *     <td></td>
 *     <td>only shift/add operations</td>
 *     <td></td>
 *   </tr>
 * </table>
 * See 
 * <ul>
 *   <li>https://mathlesstraveled.com/2009/06/11/square-roots-with-pencil-and-paper-method-2/
 *   <li>Karp1993
 *   <li>Markstein2004
 * </ul>
 * 
 * @author Andreas Engel [engel@esa.cs.tu-darmstadt.de]
 */
public class SQRT extends Unary {
  
  /**
   * Constant sqrt(2) = 1.41421356237309504880168872420969807 with 34 accurate decimal digits.
   */
  public static final BigDecimal TWO = approximate(BigDecimal.valueOf(2), MathContext.DECIMAL128);

  /** Unique ID for (de)marshalling */
  private static final long serialVersionUID = 1610160606389390290L;

  /**
   * Generate {@code Implementation} of a square root {@code Operator}.
   * @param a input precision
   * @param r output precision
   */
  public SQRT(Format a, Format r) {
    super(a, r, "sqrt");
  }

  @Override
  public Number apply(long a)       {return apply((double) a);}
  @Override
  public Number apply(double a)     {return Math.sqrt(a);}
  @Override
  public Number apply(BigInteger a) {return apply(new BigDecimal(a));}
  @Override
  public Number apply(BigDecimal a) {return approximate(a, MathContext.DECIMAL128);}
  
  /**
   * Approximate a square root.
   * Based on iterative {@link #goldschmidt} algorithm.
   * @param a   the arithmetic input
   * @param mc  the decimal precision and {@link RoundingMode} to apply. 
   * @return    {@code sqrt(a)}
   */
  public static BigDecimal approximate(BigDecimal a, MathContext mc) {
    switch (mc.getRoundingMode()) {
      case FLOOR   : // use more precision in iterative approximation to leave rounding control to last step 
      case CEILING : return approximate(a, MathContext.DECIMAL128).round(mc);
      default      : return goldschmidt(a, mc); 
    }
  }
  
/*
 * Range propagation
 *********************************************************************************************************************/
  
  @Override
  protected Range getResultRange(Range.IA a) {
    // see Stolfi1997 page 29
    if (a.isNegative()) return Range.EMPTY;
    
    return Range.generate(
        a.lo()==null || a.lo().compareTo(BigDecimal.ZERO)<=0 ? BigDecimal.ZERO : approximate(a.lo(), Range.FLOOR),
        a.hi()==null                                         ? null            : approximate(a.hi(), Range.CEILING));
  }
  
/*
 * Iterative approximation
 *********************************************************************************************************************/

  /**
   * Approximate a square root by newton raphson algorithm.
   * <p>
   * Finds {@code x} such that {@code f(x)=x²-a = 0}, i.e., {@code x = sqrt(a)}.
   *<pre>
   *  e=max(a,1);     // initial estimate always larger than sqrt(a)
   *  do 
   *    o = e;
   *    e = (o + a/o)/2;
   *  while (e < o)
   *  sqrt(a) = e;
   *</pre>
   * Tends to overestimates the perfect result.
   * <p>
   * TODO: speed up by better initial estimate
   *
   * @param a  the arithmetic input
   * @param mc the decimal precision and {@link RoundingMode} to apply
   * @return   {@code sqrt(a)} 
   */
  private static BigDecimal newton(BigDecimal a, MathContext mc) {
    if (a.compareTo(BigDecimal.ZERO) == 0) return a;
    
    // additional accuracy for intermediate results
    mc = new MathContext(mc.getPrecision()+2, mc.getRoundingMode());
    
    BigDecimal est = a.max(BigDecimal.ONE); 
    BigDecimal two = BigDecimal.valueOf(2L);
    BigDecimal old;
    
    do {
      Test.iterations++;
      old = est;
      est = a.divide(old, mc)
             .add   (old, mc)
             .divide(two, mc);
    
    // always move from over estimation towards result 
    } while (est.compareTo(old) < 0);
    return est;
  }
  
  /**
   * Approximate a square root by division free newton raphson algorithm.
   * <p>
   * Finds {@code x} such that {@code f(x)=1/x²-a = 0}, i.e., {@code x = 1/sqrt(a)} and multiplies {@code x} by 
   * {@code a}.
   * <pre>
   *  e = 1/max(a,1)       // initial estimate always smaller than 1/sqrt(a)
   *  do 
   *    o = e;
   *    e = o/2 (3 - a o²);
   *  while (e > o)
   *  sqrt(a) = a*e;
   *</pre>
   * Tends to underestimates the perfect result.
   * <p>
   * TODO: speed up by better initial estimate
   *
   * @param a  the arithmetic input
   * @param mc the decimal precision and {@link RoundingMode} to apply
   * @return   {@code sqrt(a)} 
   */
  private static BigDecimal newtonDF(BigDecimal a, MathContext mc) {
    if (a.compareTo(BigDecimal.ZERO) == 0) return a;
    
    // additional accuracy for intermediate results
    mc = new MathContext(mc.getPrecision()+2, mc.getRoundingMode());
    
    // under estimate => always smaller than perfect result
    BigDecimal est = BigDecimal.ONE.divide(a.max(BigDecimal.ONE), mc); 
    BigDecimal tri = BigDecimal.valueOf(3L);
    BigDecimal two = BigDecimal.valueOf(2L);
    BigDecimal old;
    
    do {
      Test.iterations++;
      old = est;
      est = a.multiply(old, mc)
             .multiply(old, mc)
             .negate  (     mc)
             .add     (tri, mc)
             .multiply(old, mc)
             .divide  (two, mc);

    // always move from under estimation towards result 
    } while (est.compareTo(old) > 0);

    return est.multiply(a, mc);
  }
  
  /**
   * Estimate a square root by the Goldschmidt algorithm.
   * <p>
   * Finds {@code y0, ..., ym} such that {@code a*y0²*...ym² = 1}, i.e., {@code a*y0*...yk = sqrt(a)}.
   *<pre>
   *  y  = 1/max(a,1)     // initial estimate
   *  b  = a;
   *  g  = a * y;
   *  do
   *    b = b * y²;
   *    y = (3-b)/2;
   *    g = g * y;
   *  while b != 1
   *  sqrt(a) = g;
   *</pre>
   * 
   * @param a  the arithmetic input
   * @param mc the decimal precision and {@link RoundingMode} to apply
   * @return   {@code sqrt(a)} 
   */
  private static BigDecimal goldschmidt(BigDecimal a, MathContext mc) {
    if (a.compareTo(BigDecimal.ZERO) == 0) return a;
    
    // additional accuracy for intermediate results
    int precision = mc.getPrecision();
    mc = new MathContext(precision+2, mc.getRoundingMode());
    
    BigDecimal thr = BigDecimal.ONE.scaleByPowerOfTen(-precision);       // accuracy threshold for b
    BigDecimal b   = a;                                                  // approximates 1
    BigDecimal y   = BigDecimal.ONE.divide(a.max(BigDecimal.ONE), mc);   // estimate for 1/sqrt(a)
    BigDecimal g   = a.multiply(y, mc);                                  // approximates sqrt(a)
    BigDecimal tri = BigDecimal.valueOf(3L);
    BigDecimal two = BigDecimal.valueOf(2L);
    BigDecimal old;
    
    do {
      old = g;
      b = b.multiply(y, mc).multiply(y, mc);
      y = tri.subtract(b, mc).divide(two, mc);                           // just shifts and bit toggles
      g = g.multiply(y, mc);                                             // may be executed in parallel with next b
    } while (!approximationConverged(g, g.subtract(old, mc), thr, mc));
    
    return g;
  }
  
/*
 * Debugging
 *********************************************************************************************************************/
  
  /**
   * Test iterative approximation.
   * @param args ignored
   */
  public static void main(String[] args) {
    testApproximations(1, 1000000, -4, Verbosity.STATISTICS, null, new Test[] {
                                 // description,                        calculation, compare,           math context
      new Test(a->"Math.sqrt  ("+a+")         ", (a,mc)->Math.sqrt(a.doubleValue()),    null,                   null),
      new Test(a->"approximate("+a+", DECI128)", (a,mc)->approximate(a, mc),               0, MathContext.DECIMAL128),
      new Test(a->"approximate("+a+", CEILING)", (a,mc)->approximate(a, mc),               1,          Range.CEILING),
      new Test(a->"approximate("+a+", FLOOR)  ", (a,mc)->approximate(a, mc),               1,          Range.FLOOR  ),
      new Test(a->"newton     ("+a+", DECI64) ", (a,mc)->newton     (a, mc),               1,  MathContext.DECIMAL64),
      new Test(a->"newtonDF   ("+a+", DECI64) ", (a,mc)->newtonDF   (a, mc),               1,  MathContext.DECIMAL64),
      new Test(a->"goldschmidt("+a+", DECI64) ", (a,mc)->goldschmidt(a, mc),               1,  MathContext.DECIMAL64)
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