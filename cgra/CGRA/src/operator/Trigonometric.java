package operator;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.function.BiFunction;
import java.util.function.Function;

import accuracy.Format;
import accuracy.Range;
import operator.Logarithm.LN;

/**
 * Circular and hyperbolic functions.
 * 
 * <h4>Iterative approximation</h4>
 * The circular {@link SIN}, {@link COS}, {@link TAN}, {@link COT} and hyperbolic {@link SINH}, {@link COSH}, 
 * {@link TANH}, {@link COTH} functions as well as their inverse {@link ASIN}, {@link ACOS}, {@link ATAN}, 
 * {@link ACOT}, {@link ASINH}, {@link ACOSH}, {@link ATANH}, and {@link ACOTH} functions are transcendental and have 
 * to be approximated by iterative algorithms (e.g, taylor, euler or product series, CORDIC) or reduced to other 
 * transcendental functions (e.g, {@link EXP}, {@link LN}).
 * <p>
 * The iterative approximation starts at an initial estimate (typically 0, 1, or a tabulated seed) and converges 
 * against the perfect result with each iteration. The number of iterations required to reach a certain accuracy 
 * depends on the input argument. Typically, the convergence is fast near the expansion point of the series and slows
 * down in direction of the limits of the convergence interval. For input arguments outside the convergence interval, 
 * another algorithm has to be applied or the symmetry of the trigonometric functions have to be exploited map the
 * whole function domain to the convergence interval. If the convergence intervals of two algorithms are not 
 * overlapping, the required iterations will explode near the transition point. Instead of switching to a third
 * algorithm for a safety margin around the transition point, a loss of accuracy caused by an upper iteration bound is
 * accepted.
 * 
 * <h4>Selection of Algorithm</h4>
 * The following iterative algorithms approximate trigonometric functions. To reduce the actual number of different
 * implementations, the following equalities can be used:
 * <ul>
 *   <li>{@code sin(A) = cos(pi/2 - A)}
 *   <li>{@code sin(-A) = -sin(A)}
 *   <li>{@code cos(-A) =  cos(A)}
 *   <li>{@code sin(pi/2 + A) =  sin(pi/2 - A)}
 *   <li>{@code cos(pi/2 + A) = -cos(pi/2 - A)}
 *   <li>{@code sin(2 pi + A) =  sin(A)}
 *   <li>{@code cos(2 pi + A) =  cos(A)}
 *   <li>{@code tan(A) = sin(A) / cos(A)}
 *   <li>{@code cot(A) = cos(A) / sin(A)}
 *   <li>{@code acos(A) = pi/2 - asin(A)}
 * </ul>
 * <table border>
 *   <tr>
 *     <th>Algorithm</th>
 *     <th>Idea</th>
 *     <th>PRO</th>
 *     <th>CON</th>
 *   </tr>
 *   <tr>
 *     <td>taylor series</td>
 *     <td><pre> sin(A) = sum_{k=0}^m (-1)^n A^(2k+1)/(2k+1)!</pre></td>
 *     <td></td>
 *     <td></td>
 *   </tr>
 *   <tr>
 *     <td>product series</td>
 *     <td><pre> sin(A) = A mul_{k=1}^m (1- (A/(k pi))²)</pre></td>
 *     <td></td>
 *     <td></td>
 *   </tr>
 *   <tr>
 *     <td>exponential</td>
 *     <td><pre> sin(A) = 1/2i (exp(ix) - exp(-ix))</pre></td>
 *     <td></td>
 *     <td></td>
 *   </tr>
 *   <tr>
 *     <td>taylor series</td>
 *     <td><pre> cos(A) = sum_{k=0}^m (-1)^n A^(2k)/(2k)!</pre></td>
 *     <td></td>
 *     <td></td>
 *   </tr>
 *   <tr>
 *     <td>product series</td>
 *     <td><pre> cos(A) = mul_{k=1}^m (1- (2A/((2-1) pi))²)</pre></td>
 *     <td></td>
 *     <td></td>
 *   </tr>
 *   <tr>
 *     <td>exponential</td>
 *     <td><pre> cos(A) = 1/2 (exp(ix) + exp(-ix))</pre></td>
 *     <td></td>
 *     <td></td>
 *   </tr>
 *   <tr>
 *     <td>taylor series</td>
 *     <td><pre> tan(A) = sum_{k=1}^m B(2k) (-4)^k (1-4^k) A^(2k-1)/(2k)! for -pi/2 < A < pi/2</pre></td>
 *     <td></td>
 *     <td>requires Bernoulli number {@code B(0) = 1},<br> 
 *         {@code B(n) = -1/(n+1) sum_{k=0}^{n-1} (n+1)!/(k!(n+1-k)!)B(k)}</td>
 *   </tr>
 *   <tr>
 *     <td>taylor series</td>
 *     <td><pre>asin(A) = sum_{k=0}^m (2k)! A^(2k+1) / (4^k k!² (2k+1))   for -1 < A < 1</pre></td>
 *     <td></td>
 *     <td></td>
 *   </tr>
 *   <tr>
 *     <td>taylor series</td>
 *     <td><pre>atan(A) = sum_{k=0}^m (-1)^k A^(2k+1) / (2k+1)            for -1 < A < 1</pre></td>
 *     <td></td>
 *     <td></td>
 *   </tr>
 *   <tr>
 *     <td>CORDIC</td>
 *     <td></td>
 *     <td></td>
 *     <td></td>
 *   </tr>
 * </table>
 * <p>
 * Due to the fixed-latency scheduling concept, a worst case analysis is required to select the best (combination of)
 * approximation algorithm(s) for the required precision and dynamic range. To automate this optimization step (not yet
 * implemented), many iterative approximation algorithms supporting arbitrary output precision have been implemented 
 * for all trigonometric functions. Set {@link Unary#maxApproximationIterations} to prevent non-terminating 
 * approximations near or outside the convergence intervals.
 * 
 * <h4>Important references</h4> 
 * <ul>
 *   <li><a href='http://www.mclimatiano.com/faster-sine-approximation-using-quadratic-curve'>
 *       Faster Sine Approximation Using Quadratic Curve</a>
 *   <li><a href='http://math.stackexchange.com/q/157372'>Infinite product of sine function</a>
 *   <li><a href='http://scipp.ucsc.edu/~haber/ph116A/taylor11.pdf'>Taylor Series Expansions, Bernoulli numbers</a>
 * </ul>
 * 
 * @author Andreas Engel [engel@esa.cs.tu-darmstadt.de]
 */
public abstract class Trigonometric extends Unary {

  /** Unique ID for (de)marshalling */
  private static final long serialVersionUID = -1000098868954269878L;
 
  /**
   * Constant 2.0
   */
  protected static final BigDecimal ZWO = BigDecimal.valueOf(2);
  
  /**
   * Constant 2
   */
  protected static final BigInteger TWO = BigInteger.valueOf(2);
  
  
  /**
   * Constant 4
   */
  protected static final BigInteger FOUR = BigInteger.valueOf(4);
  
  
  /**
   * Constant PI/4 = 0.7853981633974483096156608458198757 with 34 accurate decimal digits.
   * High precision approximation as asin(1/sqrt(2))
   */
  public static final BigDecimal PIQ = 
      ASIN.approximate(BigDecimal.ONE.divide(SQRT.TWO, MathContext.DECIMAL128), MathContext.DECIMAL128);

  /**
   * Constant PI/2 = 1.5707963267948966192313216916397514 with 34 accurate decimal digits.
   */
  public static final BigDecimal PIH = PIQ.add(PIQ);
  
  /**
   * Constant PI = 3.1415926535897932384626433832795028 with 34 accurate decimal digits.
   */
  public static final BigDecimal PI  = PIH.add(PIH);
  
  /**
   * Generate {@code Implementation} of a square root {@code Operator}.
   * @param a  input precision
   * @param r  output precision
   */
  public Trigonometric(Format a, Format r) {
    super(a, r, "");
    symbol = getClass().getSimpleName();
  }
  
  @Override
  public Number apply(long a)       {return apply((double) a);}
  @Override
  public Number apply(BigInteger a) {return apply(new BigDecimal(a));}
  @Override
  public Number apply(BigDecimal a) {
    try {
      return (Number) getClass().getMethod("approximate", BigDecimal.class, MathContext.class)
                                .invoke(a, MathContext.DECIMAL128);
    } catch (Exception e) {
      throw new NotImplementedException("BitDecimal arithmetic");
    }
  }
  
/*
 * Common methods and configurations for iterative approximations
 *********************************************************************************************************************/

  /**
   * Lookup table used to speedup recomputation of {@link #bernoulli} numbers.
   */
  private static HashMap<Integer,BigDecimal> bernoulliLUT = new HashMap<Integer, BigDecimal>();
  
  /**
   * Get bernoulli number.
   * <p>
   * Implementation follows http://scipp.ucsc.edu/~haber/ph116A/taylor11.pdf
   * <pre>B_n = (n+1)! sum_{k=1}^{n-1} (-1)^(k-1) / k / (n+1-k)! / k! sum_{j=0}^{k-1} j^n</pre>
   * with LUT for already computed values.
   * 
   * @param n index of requested bernoulli number
   * @return  {@code B_k}
   */
  protected static BigDecimal bernoulli(int n) {
    
    // handle special cases
    if (n < 0)      return null;
    if (n == 1)     return new BigDecimal("-0.5");
    if (n % 2 == 1) return BigDecimal.ZERO;
    
    // Some examples precalculated with http://www.numberempire.com/bernoullinumbers.php
    //                              and http://www.ttmath.org/online_calculator
    switch (n) {
case  0: return new BigDecimal(                        "1");
case  2: return new BigDecimal(                        "0.1666666666666666666666666666666666666666666666666666666667");
case  4: return new BigDecimal(                       "-0.0333333333333333333333333333333333333333333333333333333333");
case  6: return new BigDecimal(                        "0.0238095238095238095238095238095238095238095238095238095238");
case  8: return new BigDecimal(                       "-0.0333333333333333333333333333333333333333333333333333333333");
case 10: return new BigDecimal(                        "0.0757575757575757575757575757575757575757575757575757575758");
case 12: return new BigDecimal(                       "-0.2531135531135531135531135531135531135531135531135531135531");
case 14: return new BigDecimal(                        "1.1666666666666666666666666666666666666666666666666666666667");
case 16: return new BigDecimal(                       "-7.0921568627450980392156862745098039215686274509803921568627");
case 18: return new BigDecimal(                       "54.9711779448621553884711779448621553884711779448621553884712");
case 20: return new BigDecimal(                     "-529.1242424242424242424242424242424242424242424242424242424242");
case 22: return new BigDecimal(                     "6192.1231884057971014492753623188405797101449275362318840579710");
case 24: return new BigDecimal(                   "-86580.2531135531135531135531135531135531135531135531135531135531");
case 26: return new BigDecimal(                  "1425517.1666666666666666666666666666666666666666666666666666666667");
case 28: return new BigDecimal(                "-27298231.0678160919540229885057471264367816091954022988505747126437");
case 30: return new BigDecimal(                "601580873.9006423683843038681748359167714006423683843038681748359168");
case 32: return new BigDecimal(             "-15116315767.0921568627450980392156862745098039215686274509803921568627");
case 34: return new BigDecimal(             "429614643061.1666666666666666666666666666666666666666666666666666666667");
case 36: return new BigDecimal(          "-13711655205088.3327721590879485616327721590879485616327721590879485616327");
case 38: return new BigDecimal(          "488332318973593.1666666666666666666666666666666666666666666666666666666667");
case 40: return new BigDecimal(       "-19296579341940068.1486326681448632668144863266814486326681448632668144863267");
case 42: return new BigDecimal(       "841693047573682615.0005537098560354374307862679955703211517165005537098560354");
case 44: return new BigDecimal(    "-40338071854059455413.0768115942028985507246376811594202898550724637681159420290");
case 46: return new BigDecimal(   "2115074863808199160560.1453900709219858156028368794326241134751773049645390070922");
case 48: return new BigDecimal("-120866265222965259346027.3119370825253178194354664942900237017884076707606119370825");
    }    
    
    // try a lookup
    BigDecimal res = bernoulliLUT.get(n); 
    if (res != null) return res;
    
    // so we can not get around computing B_n with high accuracy
    MathContext mc = new MathContext(200);
    
    // (n+1)!
    BigInteger fakNp1 = BigInteger.ONE;
    for (int k=2; k<=n+1; k++) fakNp1 = fakNp1.multiply(BigInteger.valueOf(k));
    
    // accumulator for sum over j => just add (k-1)^n in each main loop
    BigInteger sumj = BigInteger.ZERO;
    
    // main sum
    BigDecimal sum = BigDecimal.ZERO;
    for (int k=1; k<= n+1; k++) {
      
      // (n+1-k)!
      BigInteger fakNp1mK = BigInteger.ONE;
      for (int j=2; j<=n+1-k; j++) fakNp1mK = fakNp1mK.multiply(BigInteger.valueOf(j));
      
      // k!
      BigInteger fakK = BigInteger.ONE;
      for (int j=2; j<=k; j++) fakK = fakK.multiply(BigInteger.valueOf(j));
      
      // (k-1)^n
      BigInteger km1 = BigInteger.valueOf(k-1);
      BigInteger exp = BigInteger.ONE;
      for (int j=0; j<n; j++) exp = exp.multiply(km1);
      sumj = sumj.add(exp);
      
      // prepare large division
      BigInteger num = sumj.multiply(fakNp1);
      BigInteger den = fakK.multiply(fakNp1mK).multiply(BigInteger.valueOf(k));
      
      // accumulate partial sum (negative for even k)
      BigDecimal q = new BigDecimal(num).divide(new BigDecimal(den), mc);
      if (k % 2 == 0) q = q.negate();
      sum = sum.add(q, mc);
    }
    
    // remember the result
    bernoulliLUT.put(n, sum);
    
    return sum;
  }
  
  /**
   * Approximate a (hyperbolic) (co)sine by the taylor series {@code sum_{k=0}^m (-1)^k^c a^(2k+s)/(2k+s)!}
   * @param a   the arithmetic input ({@code -pi/4 < a < pi/4})
   * @param mc  the decimal precision and {@link RoundingMode} to apply.
   * @param c   circular flag
   * @param s   sine flag
   * @return    <pre> sin (a) for  c &&  s</pre>
   *            <pre> sinh(a) for !c &&  s</pre>
   *            <pre> cos (a) for  c && !s</pre>
   *            <pre> cosh(a) for !c && !s</pre>
   */
  protected static BigDecimal taylorZero_cosinh(BigDecimal a, MathContext mc, boolean c, boolean s) {

    // additional accuracy for intermediate results
    int precision = mc.getPrecision();
    mc = new MathContext(precision+2, mc.getRoundingMode());
    
    BigDecimal thr = BigDecimal.ONE.scaleByPowerOfTen(-precision);     // accuracy threshold
    BigInteger p   = s ? BigInteger.ONE : BigInteger.ZERO;             // accumulator for 2k+s
    BigInteger fak = BigInteger.ONE;                                   // accumulator for p!
    BigDecimal aa  = a.multiply(a, mc);                                // a²
    BigDecimal num = s ? a : BigDecimal.ONE;                           // accumulator for a^p
    BigDecimal q   = num;                                              // intermediate for partial sum
    BigDecimal est = q;                                                // accumulator for partial sums
    
    while (!approximationConverged(est, q, thr, mc)) {
      num = num.multiply(aa, mc);                            // num = a^(2k+s)*a^2 = a^(2(k+1)+s)
      if (c) num = num.negate();                             // num oscillates to represent (-1)^k
      p   = p.add(BigInteger.ONE);                           // p   = (2k+s)+1
      fak = fak.multiply(p);                                 // fak = (2k+s)! * ((2k+s)+1)
      p   = p.add(BigInteger.ONE);                           // p   = (2k+s)+2 = 2(k+1)+s
      fak = fak.multiply(p);                                 // fak = (2k+s)! * ((2k+s)+1) * ((2k+s)+2) = (2(k+1)+s)!
      q   = num.divide(new BigDecimal(fak), mc);             // update (partial) sum 
      est = est.add(q, mc);
    } 
    return est;
  }
  
  /**
   * Approximate a (hyperbolic) (co)tangent by the taylor series
   * {@code sum_{k=s}^m (-1)^(k-s)^t 2^(2k) (2^(2k)-1)^s B_2k a^(2k-1) / (2k)!}
   * 
   * The convergence is quite slow.
   * TODO: tan(0.5) in 18 iterations with 1e-17 precision has already been observed 
   * (git 9af3a88090b7772a596a06689baa4a7ccf7cee93). What caused slow down?
   * 
   * @param a   the arithmetic input ({@code 0 < |a| < pi/4})
   * @param mc  the decimal precision and {@link RoundingMode} to apply.
   * @param t   toggle flag (true for circular, false for hyperbolic)
   * @param s   selection flag (true for tangent,  false for cotangent)
   * @return    <pre>tan (a) for  t &&  s</pre>
   *            <pre>tanh(a) for !t &&  s</pre>
   *            <pre>cot (a) for  t && !s</pre>
   *            <pre>coth(a) for !t && !s</pre>
   */
  protected static BigDecimal taylorZero_cotanh(BigDecimal a, MathContext mc, boolean t, boolean s) {
    if (a.abs().compareTo(PIQ) >= 0) return null;
    
    // additional accuracy for intermediate results
    int precision = mc.getPrecision();
    mc = new MathContext(precision+2, mc.getRoundingMode());
    
    BigDecimal thr = BigDecimal.ONE.scaleByPowerOfTen(-precision);     // accuracy threshold
    BigInteger p   = s ? TWO : BigInteger.ZERO;                        // accumulator for 2k
    BigInteger fak = s ? TWO : BigInteger.ONE;                         // accumulator for p!
    BigInteger exp = s ? FOUR : BigInteger.ONE;                        // accumulator for 2^p
    BigDecimal aa  = a.multiply(a, mc);                                // a²
    BigDecimal num = s ? a : BigDecimal.ONE.divide(a, mc);             // accumulator for a^(p-1)
    BigDecimal q   = num;                                              // intermediate for partial sum
    BigDecimal est = q;                                                // accumulator for partial sums
    BigInteger mul;
    
    while (!approximationConverged(est, q, thr, mc)) {
      num = num.multiply(aa, mc);                                      // num = a^(2k-1)*a^2 = a^(2(k+1)-1)
      if (t) num = num.negate();                                       // num oscillates to represent (-1)^(k-s)
      p   = p.add(BigInteger.ONE);                                     // p   =  2k+1
      fak = fak.multiply(p);                                           // fak = (2k)! * (2k+1)
      p   = p.add(BigInteger.ONE);                                     // p   =  2k+2 = 2(k+1)
      fak = fak.multiply(p);                                           // fak = (2k)! * (2k+1) * (2k+2) = (2(k+1))!
      exp = exp.multiply(FOUR);                                        // exp = 2^(2k)*2^2 = 2^(2(k+1))
      mul = exp;                                                       // mul = 2^p
      if (s) mul = mul.multiply(exp.subtract(BigInteger.ONE));         // mul = 2^p * (2^p-1)
      q   = num.divide  (new BigDecimal(fak),     mc)                  // partial sum for next iteration
               .multiply(new BigDecimal(mul),     mc)
               .multiply(bernoulli(p.intValue()), mc);                 // invoke bernoulli number B_2k
      est = est.add(q, mc);
    }
    return est;
  }
  
  /**
   * Approximate a inverse (hyperbolic) sin/tan/cot by the taylor series 
   * {@code sum_{k=0}^m (-1)^k^t a^(-1^r)^(2k+1) ((2k!) / 2^(2k) / k!²)^s / (2k+1) }
   * 
   * @param a   the arithmetic input ({@code -1 < a < 1})
   * @param mc  the decimal precision and {@link RoundingMode} to apply.
   * @param t   toggle flag
   * @param s   sine flag (true for sine,  false for tangent)
   * @return    <pre>asinh(a) for  t &&  s &&  !r</pre>
   *            <pre>asin (a) for !t &&  s &&  !r</pre>
   *            <pre>atan (a) for  t && !s &&  !r</pre>
   *            <pre>atanh(a) for !t && !s &&  !r</pre>
   */
  protected static BigDecimal taylorZero_asintanh(BigDecimal a, MathContext mc, boolean t, boolean s) {
    
    // additional accuracy for intermediate results
    int precision = mc.getPrecision();
    mc = new MathContext(precision+2, mc.getRoundingMode());
    
    BigDecimal thr  = BigDecimal.ONE.scaleByPowerOfTen(-precision);     // accuracy threshold
    BigInteger k    = BigInteger.ZERO;                                  // accumulator for k
    BigInteger p    = BigInteger.ZERO;                                  // accumulator for 2k
    BigInteger fak  = BigInteger.ONE;                                   // accumulator for p!
    BigInteger sqr  = BigInteger.ONE;                                   // accumulator for k!²
    BigInteger exp  = BigInteger.ONE;                                   // accumulator for 2^p
    BigDecimal aa   = a.multiply(a, mc);                                // a²
    BigDecimal num  = a;                                                // accumulator for b^(p+1)
    BigDecimal q    = a;                                                // intermediate for partial sum
    BigDecimal est  = q;                                                // accumulator for partial sums
    
    while (!approximationConverged(est, q, thr, mc)) {
      num = num.multiply(aa, mc);                                       // num = a^(2k+1)*a^2 = a^(2(k+1)+1)
      if (t) num = num.negate();                                        // num oscillates to represent (-1)^k
      q   = num;
      if (s) {
        p   = p.add(BigInteger.ONE);                                    // p   =  2k+1
        fak = fak.multiply(p);                                          // fak = (2k)! * (2k+1)
        p   = p.add(BigInteger.ONE);                                    // p   =  2k+2 = 2(k+1)
        fak = fak.multiply(p);                                          // fak = (2k)! * (2k+1) * (2k+2) = (2(k+1))!
        k   = k.add(BigInteger.ONE);                                    // k   = k+1
        sqr = sqr.multiply(k).multiply(k);                              // sqr = k!² * (k+1)² = (k+1)!²
        exp = exp.multiply(FOUR);                                       // exp = 2^(2k)*2^2 = 2^(2(k+1))
        q   = q.multiply(new BigDecimal(fak), mc)                       // q  *= optional faktor
               .divide  (new BigDecimal(exp), mc)
               .divide  (new BigDecimal(sqr), mc);
      } else {
        p   = p.add(TWO);
      }
      q = q.divide(new BigDecimal(p.add(BigInteger.ONE)), mc);          // q   /= (2k+1) 
      est = est.add(q, mc);
    }
    return est;
  }
  
  /**
   * Approximate a inverse hyperbolic (co)sine at large inputs by the taylor series 
   * {@code ln(2a) - sum_{k=1}^m (-1)^k^t (2k)! / (2a)^(2k) / k!² / (2k)}
   * 
   * @param a   the arithmetic input ({@code a > 1})
   * @param mc  the decimal precision and {@link RoundingMode} to apply.
   * @param t   toggle flag
   * @return    <pre>asinh(a) for  t</pre>
   *            <pre>acosh(a) for !t</pre>
   */
  protected static BigDecimal taylorInf_acosinh(BigDecimal a, MathContext mc, boolean t) {
    
    // additional accuracy for intermediate results
    int precision = mc.getPrecision();
    mc = new MathContext(precision+2, mc.getRoundingMode());
    
    // start at k = 0 to avoid division for initializers => compensate by finally adding 1
    // TODO: why is this correct (for k = 0, the partial sum is affected by DIVZERO)
    
    BigDecimal thr  = BigDecimal.ONE.scaleByPowerOfTen(-precision);               // accuracy threshold
    BigInteger k    = BigInteger.ZERO;                                            // accumulator for k
    BigInteger p    = BigInteger.ZERO;                                            // accumulator for 2k
    BigInteger fak  = BigInteger.ONE;                                             // accumulator for (2k)!
    BigInteger sqr  = BigInteger.ONE;                                             // accumulator for k!²
    BigDecimal a2   = a.add(a, mc);                                               // 2a
    BigDecimal aa4  = a2.multiply(a2, mc);                                        // (2a)²
    BigDecimal exp  = BigDecimal.ONE;                                             // accumulator for (2a)^(2k)
    BigDecimal q    = BigDecimal.ONE;                                             // intermediate for partial sum
    BigDecimal est  = LN.approximate(a2, mc).add(BigDecimal.ONE).subtract(q, mc); // accumulator for partial sums
    
    while (!approximationConverged(est, q, thr, mc)) {
      exp = exp.multiply(aa4, mc);                                      // exp = (2a)^(2k)*(2a)^2 = (2a)^(2(k+1))
      if (t) exp = exp.negate();                                        // exp oscillates to represent (-1)^k
      p   = p.add(BigInteger.ONE);                                      // p   =  2k+1
      fak = fak.multiply(p);                                            // fak = (2k)! * (2k+1)
      p   = p.add(BigInteger.ONE);                                      // p   =  2k+2 = 2(k+1)
      fak = fak.multiply(p);                                            // fak = (2k)! * (2k+1) * (2k+2) = (2(k+1))!
      k   = k.add(BigInteger.ONE);                                      // k   = k+1
      sqr = sqr.multiply(k).multiply(k);                                // sqr = k!² * (k+1)² = (k+1)!²
      q   = new BigDecimal(fak).divide(exp, mc)                         // update parital sum
                               .divide(new BigDecimal(sqr.multiply(p)),mc);
      est = est.subtract(q, mc);
    }
    return est;
  }
  
  /**
   * Approximate a (hyperbolic) (co)sine by the product series 
   * <pre>a^s mul_{k=1}^m (1 + (-1)^c (a/(k+(s-1)/2) pi)²)</pre>

   * <b>Attention:</b> very slow and does not reach requested accuracy!
   * 
   * @param a   the arithmetic input ({@code -pi/4 < a < pi/4}
   * @param mc  the decimal precision and {@link RoundingMode} to apply. 
   * @param c   the circular flag
   * @param s   the sine flag
   * @return    <pre> sin (a) for  c &&  s</pre>
   *            <pre> sinh(a) for !c &&  s</pre>
   *            <pre> cos (a) for  c && !s</pre>
   *            <pre> cosh(a) for !c && !s</pre>
   */
  protected static BigDecimal product_cosinh(BigDecimal a, MathContext mc, boolean c, boolean s) {

    // additional accuracy for intermediate results
    int precision = mc.getPrecision();
    mc = new MathContext(precision+2, mc.getRoundingMode());
    
    BigDecimal thr = BigDecimal.ONE.scaleByPowerOfTen(-precision);   // accuracy threshold
    BigDecimal p   = s ? PI : PIH;                                   // accumulator for (k+(s-1)/2) pi
    BigDecimal est = s ? a  : BigDecimal.ONE;                        // accumulator for partial products
    BigDecimal sqr, q, old;                                          // intermediates for partial product
    
    do {
      sqr = a.divide(p, mc);
      sqr = sqr.multiply(sqr, mc);
      p   = p.add(PI, mc);
      q   = c ? BigDecimal.ONE.subtract(sqr, mc) : BigDecimal.ONE.add(sqr, mc);
      old = est;
      est = est.multiply(q, mc);
    } while (!approximationConverged(est, est.subtract(old, mc), thr, mc));
    return est;
  }
  
  /**
   * Approximate an arc tangent by the euler series 
   * <pre> 1/a + sum_{k=0}^m 2^(2k) k!² / (2k+1)! / (1+1/a²)^(k+1)</pre>
   * 
   * @param a   the arithmetic input ({@code a != 0})
   * @param mc  the decimal precision and {@link RoundingMode} to apply. 
   * @return    {@code atan(a)}
   */
  protected static BigDecimal euler_atan(BigDecimal a, MathContext mc) {
      
      // additional accuracy for intermediate results
      int precision = mc.getPrecision();
      mc = new MathContext(precision+2, mc.getRoundingMode());
      
      BigDecimal thr  = BigDecimal.ONE.scaleByPowerOfTen(-precision);   // accuracy threshold
      BigInteger k    = BigInteger.ZERO;                                // accumulator for k
      BigInteger p    = BigInteger.ONE;                                 // accumulator for 2k+1
      BigInteger fak  = BigInteger.ONE;                                 // accumulator for (2k+1)!
      BigInteger sqr  = BigInteger.ONE;                                 // accumulator for k!²
      BigInteger exp  = BigInteger.ONE;                                 // accumulator for 2^(2k)
      BigDecimal aa   = BigDecimal.ONE.add(BigDecimal.ONE.divide(a.multiply(a, mc), mc), mc); // 1+1/a²
      BigDecimal den  = aa;                                             // accumulator for (1+1/a²)^(k+1)
      BigDecimal q    = BigDecimal.ONE.divide(den, mc);                 // intermediate for partial sum
      BigDecimal est  = q;                                              // accumulator for partial sums
      
      while (!approximationConverged(est, q, thr, mc)) {
        den = den.multiply(aa, mc);                                     // den = aa^(k+1)*aa = a^((k+1)+1)
        p   = p.add(BigInteger.ONE);                                    // p   =  2k+2
        fak = fak.multiply(p);                                          // fak = (2k+1)!*(2k+2)
        p   = p.add(BigInteger.ONE);                                    // p   =  2k+3 = 2(k+1)+1
        fak = fak.multiply(p);                                          // fak = (2k+1)!*(2k+2)*(2k+3) = (2(k+1)+1)!
        k   = k.add(BigInteger.ONE);                                    // k   = k+1
        sqr = sqr.multiply(k).multiply(k);                              // sqr = k!² * (k+1)² = (k+1)!²
        exp = exp.multiply(FOUR);                                       // exp = 2^(2k)*2^2 = 2^(2(k+1))
        q   = new BigDecimal(exp.multiply(sqr)).divide(den, mc)         // update (partial) sum
                                               .divide(new BigDecimal(fak), mc);
        est = est.add(q, mc);
      }
      return est.divide(a, mc);
  }
  
  /**
   * Derive a (co)sine as <pre>(s ? 2 tan(a/2) : 1 - tan(a/2)²) / (1 + tan(a/2)²)</pre>
   * 
   * @param a   the arithmetic input
   * @param mc  the decimal precision and {@link RoundingMode} to apply. 
   * @param s   sine flag
   * @return    <pre>sin(a) for  s</pre>
   *            <pre>cos(a) for !s</pre>
   */
  protected static BigDecimal geometric_cosin(BigDecimal a, MathContext mc, boolean s) {
    BigDecimal t   = TAN.approximate(a.divide(ZWO, mc), mc);
    BigDecimal tt  = t.multiply(t, mc);
    BigDecimal num = s ? t.add(t, mc) : BigDecimal.ONE.subtract(tt, mc);
    return num.divide(tt.add(BigDecimal.ONE, mc), mc);
  }

  
/*
 * Implementations of dedicated trigonometric functions
 *********************************************************************************************************************/

  /**
   * Sine {@code Implementation}.
   * <pre>sin : R -> [-1,1]</pre>
   * 
   * <h4>Symmetry and periodicity</h4>
   * <ul>
   *   <li>{@code sin(a) = -sin(-a)}
   *   <li>{@code sin(a) = sin(a + 2 pi)}
   *   <li>{@code sin(pi/2 + a) = sin(pi/2 - a)}
   * </ul>
   * 
   * <h4>Relation to other functions</h4>
   * <ul>
   *   <li>{@code sin(a) = cos(pi/2 - a)}
   *   <li>{@code sin(a) = (exp(ia) - exp(-ia)) / 2i}
   *   <li>{@code sin(a) = 2 tan(a/2) / (1 + tan(a/2)²)}
   * </ul>
   * 
   * <h4>Iterative Approximation</h4>
   * To achieve the fastest convergence, the input should be transformed to ]-pi/4,pi/4[ using symmetry, periodicity 
   * and the cosine relation.
   * <p>
   * <table border>
   *   <tr>
   *     <th>Algorithm</th>
   *     <th>Idea</th>
   *     <th>PRO</th>
   *     <th>CON</th>
   *   </tr>
   *   <tr>
   *     <td>{@link #taylor}</td>
   *     <td><pre>sin(a) = sum_{k=0}^m (-1)^k a^(2k+1)/(2k+1)!</pre></td>
   *     <td>fast convergence ({@code 5 <= m <= 11} for 1e-16 precision)</td>
   *     <td>requires large division in each iteration</td>
   *   </tr>
   *   <tr>
   *     <td>{@link #product}</td>
   *     <td><pre>sin(a) = a mul_{k=1}^m 1-(a/(k pi))²</pre></td>
   *     <td></td>
   *     <td>based on PI, much to slow convergence ({@code m >> 3e6} for 1e-10 precision)</td>
   *   </tr>
   * </table>
   * <p>
   * See <a href='https://de.wikipedia.org/wiki/Sinus_und_Kosinus'>Wikipedia</a>
   * and <a href='http://mathworld.wolfram.com/Sine.html'>Mathematica</a>.
   */
  public static class SIN extends Trigonometric {
    
    /** Unique ID for (de)marshalling */
    private static final long serialVersionUID = 3075717726070590717L;
  
    /**
     * Generate a sine {@code Implementation}.
     * @param a input  {@link Format}
     * @param r result {@link Format}
     */
    public SIN(Format a, Format r) {
      super(a,r);
    }
    
    @Override
    public Number apply(double a) {
      return Math.sin(a);
    }
    
    /**
     * Approximate a sine.
     * @param a   the arithmetic input
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code sin(a)}
     */
    public static BigDecimal approximate(BigDecimal a, MathContext mc) {
      switch (mc.getRoundingMode()) {
        case FLOOR   : // use more precision in iterative approximation to leave rounding control to last step 
        case CEILING : return approximate(a, MathContext.DECIMAL128).round(mc);
        default      : return taylor(a, mc); 
      }
    }
    
    /**
     * Scale input domain to improve convergence. Also handles extremes and roots.
     * 
     * @param a      the arithmetic input
     * @param mc     the decimal precision and {@link RoundingMode} to apply. 
     * @param approx the approximation to be applied to the transformed input (second argument selects sine/cosine)
     * @return       sin(a)
     */
    private static BigDecimal prepare(BigDecimal a, MathContext mc, BiFunction<BigDecimal,Boolean,BigDecimal> approx) {
   
      // transform input domain to [-pi/4,pi/4]
      boolean neg = a.compareTo(BigDecimal.ZERO) < 0;
      BigDecimal[] divrem = a.abs().divideAndRemainder(PIQ, mc);
      int section = divrem[0].toBigInteger().remainder(BigInteger.valueOf(8)).intValue();
      a = divrem[1];
      
      // short cut for extremes and roots
      if (a.compareTo(BigDecimal.ZERO) == 0) {
        switch (section) {
          case 0:
          case 4: return BigDecimal.ZERO;
          case 2: return BigDecimal.ONE;
          case 6: return BigDecimal.ONE.negate();
        }
      }
      
      // section specific mirroring
      switch (section) {
        case  0: a = approx.apply(a,              true);            break; // sin([0pi/4, 1pi/4]) =  sin([    0, pi/4]) 
        case  1: a = approx.apply(a.subtract(PIQ),false);           break; // sin([1pi/4, 2pi/4]) =  cos([-pi/4,    0]) 
        case  2: a = approx.apply(a,              false);           break; // sin([2pi/4, 3pi/4]) =  cos([    0, pi/4])
        case  3: a = approx.apply(PIQ.subtract(a),true);            break; // sin([3pi/4, 4pi/4]) =  sin([ pi/4,    0])
        case  4: a = approx.apply(a.negate()     ,true);            break; // sin([4pi/4, 5pi/4]) =  sin([    0,-pi/4])
        case  5: a = approx.apply(a.subtract(PIQ),false); neg=!neg; break; // sin([5pi/4, 6pi/4]) = -cos([-pi/4,    0])
        case  6: a = approx.apply(a,              false); neg=!neg; break; // sin([6pi/4, 7pi/4]) = -cos([    0, pi/4])
        default: a = approx.apply(a.subtract(PIQ),true);            break; // sin([7pi/4, 8pi/4]) =  sin([-pi/4,    0])
      }
      
      // compensate initial abs()
      return neg ? a.negate() : a;
    }
    
    /**
     * Approximate a sine by the taylor series {@code sin(a) = sum_{k=0}^m (-1)^k a^(2k+1)/(2k+1)!} and
     *                                         {@code cos(a) = sum_{k=0}^m (-1)^k a^(2k)  /(2k)!}
     * @param a   the arithmetic input
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code sin(a)}
     */
    private static BigDecimal taylor(BigDecimal a, MathContext mc) {
      return prepare(a, mc, (b, sine) -> taylorZero_cosinh(b, mc, true, sine));
    }
    
    /**
     * Approximate a sine by the product series {@code sin(a) = a mul_{k=1}^m 1-(a/( k      pi))²} and
     *                                          {@code cos(a) =   mul_{k=1}^m 1-(a/((k-1/2) pi))²}
     * <p>
     * <b>Attention:</b> very slow and does not reach requested accuracy!
     * 
     * @param a   the arithmetic input
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code sin(a)}
     */
    private static BigDecimal product(BigDecimal a, MathContext mc) {
      return prepare(a, mc, (b, sine) -> product_cosinh(b, mc, true, sine));
    }
    
    /**
     * Approximate a sine as <pre>2 tan(a/2)² / (1 + tan(a/2)²)</pre>
     * 
     * @param a   the arithmetic input
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code sin(a)}
     */
    private static BigDecimal geometric(BigDecimal a, MathContext mc) {
      return prepare(a, mc, (b, sine) -> geometric_cosin(b, mc, sine));
    }
    
    /**
     * Approximate a sine by the {@link CORDIC} algorithm.
     * 
     * @param a   the arithmetic input
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code sin(a)}
     */
    private static BigDecimal cordic(BigDecimal a, MathContext mc) {
      return prepare(a, mc, (b, sine) -> sine ? CORDIC.sin(b, mc) : CORDIC.cos(b, mc));
    }
    
    /**
     * Test accuracy and convergence speed of sine approximations.
     * @param args ignored
     */
    public static void main(String[] args) {
      System.out.println("Testing SIN approximations");
      SIN d = new SIN(null, null);
      testApproximations(-10000, 10000, -3, Verbosity.STATISTICS, "out/stat/sin.dat", new Test[] {
        new Test(a -> ".sin        ("+a+")        ", (a,mc) -> d.apply(a.doubleValue()), null,                   null),
        new Test(a -> " approximate("+a+",DECI128)", (a,mc) -> approximate(a, mc),          0, MathContext.DECIMAL128),
        new Test(a -> ".approximate("+a+",CEILING)", (a,mc) -> approximate(a, mc),          1,          Range.CEILING),
        new Test(a -> ".approximate("+a+",FLOOR)  ", (a,mc) -> approximate(a, mc),          1,          Range.FLOOR  ),
        new Test(a -> " taylor     ("+a+",DECI64) ", (a,mc) -> taylor     (a, mc),          1,  MathContext.DECIMAL64),
        new Test(a -> " product    ("+a+",DECI64) ", (a,mc) -> product    (a, mc),          1,  MathContext.DECIMAL64),
        new Test(a -> " geometric  ("+a+",DECI64) ", (a,mc) -> geometric  (a, mc),          1,  MathContext.DECIMAL64),
        new Test(a -> " cordic     ("+a+",DECI64) ", (a,mc) -> cordic     (a, mc),          1,  MathContext.DECIMAL64),
      });
    }
  }
  
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  
  /**
   * Cosine {@code Implementation}.
   * <pre>cos : R -> [-1,1]</pre>
   * 
   * <h4>Symmetry and periodicity</h4>
   * <ul>
   *   <li>{@code cos(a) = cos(-a)}
   *   <li>{@code cos(a) = cos(a + 2 pi)}
   *   <li>{@code cos(pi/2 + a) = -cos(pi/2 - a)}
   * </ul>
   * 
   * <h4>Relation to other functions</h4>
   * <ul>
   *   <li>{@code cos(a) = sin(pi/2 - a)}
   *   <li>{@code cos(a) = (exp(ia) + exp(-ia)) / 2}
   *   <li>{@code cos(a) = (1 - tan(a/2)²) / (1 + tan(a/2)²)}
   * </ul>
   * 
   * <h4>Iterative Approximation</h4>
   * To achieve the fastest convergence, the input should be transformed to ]-pi/4,pi/4[ using symmetry, periodicity 
   * and the sine relation.
   * <p>
   * <table border>
   *   <tr>
   *     <th>Algorithm</th>
   *     <th>Idea</th>
   *     <th>PRO</th>
   *     <th>CON</th>
   *   </tr>
   *   <tr>
   *     <td>{@link #taylor}</td>
   *     <td><pre>cos(a) = sum_{k=0}^m (-1)^k a^(2k)/(2k)!</pre></td>
   *     <td>fast convergence ({@code 5 <= m <= 11} for 1e-16 precision)</td>
   *     <td>requires large division in each iteration</td>
   *   </tr>
   *   <tr>
   *     <td>{@link #product}</td>
   *     <td><pre>cos(a) = mul_{k=1}^m 1-(a/((k-1/2) pi))²</pre></td>
   *     <td></td>
   *     <td>based on PI, much to slow convergence ({@code m >> 3e6} for 1e-10 precision)</td>
   *   </tr>
   * </table>
   * <p>
   * See <a href='https://de.wikipedia.org/wiki/Sinus_und_Kosinus'>Wikipedia</a>
   * and <a href='http://mathworld.wolfram.com/Cosine.html'>Mathematica</a>.
   */
  public static class COS extends Trigonometric {
    
    /** Unique ID for (de)marshalling */
    private static final long serialVersionUID = -1573863489695733758L;
  
    /**
     * Generate a cosine {@code Implementation}.
     * @param a input  {@link Format}
     * @param r result {@link Format}
     */
    public COS(Format a, Format r) {
      super(a,r);
    }
    
    @Override
    public Number apply(double a) {
      return Math.cos(a);
    }
    
    /**
     * Approximate a cosine.
     * @param a   the arithmetic input
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code cos(a)}
     */
    public static BigDecimal approximate(BigDecimal a, MathContext mc) {
      switch (mc.getRoundingMode()) {
        case FLOOR   : // use more precision in iterative approximation to leave rounding control to last step 
        case CEILING : return approximate(a, MathContext.DECIMAL128).round(mc);
        default      : return taylor(a, mc); 
      }
    }
    
    /**
     * Scale input domain to improve convergence. Also handles extremes and roots.
     * 
     * @param a      the arithmetic input
     * @param mc     the decimal precision and {@link RoundingMode} to apply. 
     * @param approx the approximation to be applied to the transformed input (second argument selects sine/cosine)
     * @return       cos(a)
     */
    private static BigDecimal prepare(BigDecimal a, MathContext mc, BiFunction<BigDecimal,Boolean,BigDecimal> approx) {
   
      // transform input domain to [-pi/4,pi/4]
      BigDecimal[] divrem = a.abs().divideAndRemainder(PIQ, mc);
      int section = divrem[0].toBigInteger().remainder(BigInteger.valueOf(8)).intValue();
      a = divrem[1];
      
      // short cut for extremes and roots
      if (a.compareTo(BigDecimal.ZERO) == 0) {
        switch (section) {
          case 2:
          case 6: return BigDecimal.ZERO;
          case 0: return BigDecimal.ONE;
          case 4: return BigDecimal.ONE.negate();
        }
      }
      
      // section specific mirroring
      switch (section) {
        case  0: return approx.apply(a              ,false);             // cos([0pi/4, 1pi/4]) =  cos([    0, pi/4]) 
        case  1: return approx.apply(PIQ.subtract(a),true);              // cos([1pi/4, 2pi/4]) =  sin([ pi/4,    0]) 
        case  2: return approx.apply(a.negate()     ,true);              // cos([2pi/4, 3pi/4]) =  sin([    0,-pi/4])
        case  3: return approx.apply(a.subtract(PIQ),false).negate();    // cos([3pi/4, 4pi/4]) = -cos([-pi/4,    0])
        case  4: return approx.apply(a              ,false).negate();    // cos([4pi/4, 5pi/4]) = -cos([    0, pi/4])
        case  5: return approx.apply(a.subtract(PIQ),true);              // cos([5pi/4, 6pi/4]) =  sin([-pi/4,    0])
        case  6: return approx.apply(a              ,true);              // cos([6pi/4, 7pi/4]) =  sin([    0, pi/4])
        default: return approx.apply(a.subtract(PIQ),false);             // cos([7pi/4, 8pi/4]) =  cos([-pi/4,    0])
      }
      
    }
    
    /**
     * Approximate a cosine by the taylor series {@code sin(a) = sum_{k=0}^m (-1)^k a^(2k+1)/(2k+1)!} and
     *                                           {@code cos(a) = sum_{k=0}^m (-1)^k a^(2k)  /(2k)!}
     * @param a   the arithmetic input
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code cos(a)}
     */
    private static BigDecimal taylor(BigDecimal a, MathContext mc) {
      return prepare(a, mc, (b, sine) -> taylorZero_cosinh(b, mc, true, sine));
    }
    
    /**
     * Approximate a cosine by the product series {@code sin(a) = a mul_{k=1}^m 1-(a/( k      pi))²} and
     *                                            {@code cos(a) =   mul_{k=1}^m 1-(a/((k-1/2) pi))²}
     * <p>
     * <b>Attention:</b> very slow and does not reach requested accuracy!
     * 
     * @param a   the arithmetic input
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code cos(a)}
     */
    private static BigDecimal product(BigDecimal a, MathContext mc) {
      return prepare(a, mc, (b, sine) -> product_cosinh(b, mc, true, sine));
    }
    
    /**
     * Approximate a cosine as <pre>(1 - tan(a/2)²) / (1 + tan(a/2)²)</pre>
     * 
     * @param a   the arithmetic input
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code cos(a)}
     */
    private static BigDecimal geometric(BigDecimal a, MathContext mc) {
      return prepare(a, mc, (b, sine) -> geometric_cosin(b, mc, sine));
    }
    
    /**
     * Approximate a cosine by the {@link CORDIC} algorithm.
     * 
     * @param a   the arithmetic input
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code cos(a)}
     */
    private static BigDecimal cordic(BigDecimal a, MathContext mc) {
      return prepare(a, mc, (b, sine) -> sine ? CORDIC.sin(b, mc) : CORDIC.cos(b, mc));
    }
    
    /**
     * Test accuracy and convergence speed of cosine approximations.
     * @param args ignored
     */
    public static void main(String[] args) {
      System.out.println("Testing COS approximations");
      COS d = new COS(null, null);
      testApproximations(-10000, 10000, -3, Verbosity.STATISTICS, "out/stat/cos.dat", new Test[] {
        new Test(a -> ".cos        ("+a+")        ", (a,mc) -> d.apply(a.doubleValue()), null,                   null),
        new Test(a -> " approximate("+a+",DECI128)", (a,mc) -> approximate(a, mc),          0, MathContext.DECIMAL128),
        new Test(a -> ".approximate("+a+",CEILING)", (a,mc) -> approximate(a, mc),          1,          Range.CEILING),
        new Test(a -> ".approximate("+a+",FLOOR)  ", (a,mc) -> approximate(a, mc),          1,          Range.FLOOR  ),
        new Test(a -> " taylor     ("+a+",DECI64) ", (a,mc) -> taylor     (a, mc),          1,  MathContext.DECIMAL64),
        new Test(a -> " product    ("+a+",DECI64) ", (a,mc) -> product    (a, mc),          1,  MathContext.DECIMAL64),
        new Test(a -> " geometric  ("+a+",DECI64) ", (a,mc) -> geometric  (a, mc),          1,  MathContext.DECIMAL64),
        new Test(a -> " cordic     ("+a+",DECI64) ", (a,mc) -> cordic     (a, mc),          1,  MathContext.DECIMAL64),
      });
    }
  }
  
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Tangent {@code Implementation}.
   * <pre>tan : {a in R : a != (2k+1) pi/2, k in Z} -> R</pre>
   * 
   * <h4>Symmetry and periodicity</h4>
   * <ul>
   *   <li>{@code tan(a) = -tan(-a)}
   *   <li>{@code tan(a) = tan(a + pi)}
   * </ul>
   * 
   * <h4>Relation to other functions</h4>
   * <ul>
   *   <li>{@code tan(a) = 1 / cot(a)}
   *   <li>{@code tan(a) = cot(pi/2 - a)}
   *   <li>{@code tan(a) = sin(a) / cos(a)}
   * </ul>
   * 
   * <h4>Iterative Approximation</h4>
   * To achieve the fastest convergence, the input should be transformed to ]-pi/4,pi/4[ using symmetry, periodicity 
   * and the cotangent relation.
   * <p>
   * <table border>
   *   <tr>
   *     <th>Algorithm</th>
   *     <th>Idea</th>
   *     <th>PRO</th>
   *     <th>CON</th>
   *   </tr>
   *   <tr>
   *     <td>{@link #taylor}</td>
   *     <td><pre>tan(a) = sum_{k=1}^m (-1)^(k-1) 2^(2k)(2^(2k)-1) B_2k a^(2k-1)/(2k)!  for |a| < pi/2</pre></td>
   *     <td>fast convergence ({@code m <= 18} for 1e-17 precision) for {@code a < 0.5)}</td>
   *     <td>requires large and accurate Bernoulli numbers, very slow convergence near pi/2</td>
   *   </tr>
   * </table>
   * <p>
   * See <a href='https://de.wikipedia.org/wiki/Tangens_und_Kotangens'>Wikipedia</a>
   * and <a href='http://mathworld.wolfram.com/Tangent.html'>Mathematica</a>.
   */
  public static class TAN extends Trigonometric {
    
    /** Unique ID for (de)marshalling */
    private static final long serialVersionUID = 4117587212753543439L;
  
    /**
     * Generate a tangent {@code Implementation}.
     * @param a input  {@link Format}
     * @param r result {@link Format}
     */
    public TAN(Format a, Format r) {
      super(a,r);
    }
    
    @Override
    public Number apply(double a) {
      return Math.tan(a);
    }
    
    /**
     * Approximate a tangent.
     * @param a   the arithmetic input
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code tan(a)}
     */
    public static BigDecimal approximate(BigDecimal a, MathContext mc) {
      switch (mc.getRoundingMode()) {
        case FLOOR   : // use more precision in iterative approximation to leave rounding control to last step  
        case CEILING : a = approximate(a, MathContext.DECIMAL128);
                       return a == null ? null : a.round(mc);
        default      : return taylor(a, mc);
      }
    }
    
    /**
     * Scale input domain to improve convergence. Also handles poles and roots.
     * 
     * @param a      the arithmetic input
     * @param mc     the decimal precision and {@link RoundingMode} to apply. 
     * @param approx the approximation to be applied to the transformed input (second argument selects tan/cot)
     * @return       tan(a)
     */
    private static BigDecimal prepare(BigDecimal a, MathContext mc, BiFunction<BigDecimal,Boolean,BigDecimal> approx) {
   
      // transform input domain to [-pi/4,pi/4]
      boolean neg = a.compareTo(BigDecimal.ZERO) < 0;
      BigDecimal[] divrem = a.abs().divideAndRemainder(PIQ, mc);
      int section = divrem[0].toBigInteger().remainder(BigInteger.valueOf(4)).intValue();
      a = divrem[1];
      
      // short cut for poles and roots
      if (a.compareTo(BigDecimal.ZERO) == 0) {
        switch (section) {
          case 2 : return null;
          case 0 : return BigDecimal.ZERO; 
        }
      }
      
      // section specific mirroring
      switch (section) {
        case  0: a = approx.apply(a,              true);  break;          // tan([0pi/4, 1pi/4]) =  tan([    0, pi/4]) 
        case  1: a = approx.apply(PIQ.subtract(a),false); break;          // tan([1pi/4, 2pi/4]) =  cot([ pi/4,    0]) 
        case  2: a = approx.apply(a.negate(),     false); break;          // tan([2pi/4, 3pi/4]) =  cot([    0,-pi/4])
        default: a = approx.apply(a.subtract(PIQ),true);  break;          // tan([3pi/4, 4pi/4]) =  tan([-pi/4,    0])
      }
      
      // compensate initial abs()
      return neg ? a.negate() : a;
    }
    
    /**
     * Approximate a tangent by the taylor series 
     * {@code tan(a) = sum_{k=1}^m (-1)^(k-1) a^(2k-1) 2^(2k) (2^(2k)-1) B_2k / (2k)!} and
     * {@code cot(a) = sum_{k=0}^m (-1)^k     a^(2k-1) 2^(2k)            B_2k / (2k)!}
     * 
     * @param a   the arithmetic input
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code tan(a)}
     */
    private static BigDecimal taylor(BigDecimal a, MathContext mc) {
      return prepare(a, mc, (b, tan) -> taylorZero_cotanh(b, mc, true, tan));
    }
    
    /**
     * Approximate a tangent based on approximating sine and cosine.
     * As sine and cosine can be approximated in parallel, this is faster than using the slow taylor series for tangent
     * with large bernoulli numbers.
     * 
     * @param a   the arithmetic input
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code tan(a)}
     */
    private static BigDecimal geometric(BigDecimal a, MathContext mc) {
      BigDecimal num = SIN.approximate(a, mc);
      if (num.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
      BigDecimal den = COS.approximate(a, mc);
      if (den.compareTo(BigDecimal.ZERO) == 0) return null;
      return num.divide(den, mc);
    }
    
    /**
     * Approximate a tangent by the {@link CORDIC} algorithm.
     * 
     * @param a   the arithmetic input
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code tan(a)}
     */
    private static BigDecimal cordic(BigDecimal a, MathContext mc) {
      return prepare(a, mc, (b, tan) -> tan ? CORDIC.tan(b, mc) : CORDIC.cot(b, mc));
    }
    
    
    /**
     * Test accuracy and convergence speed of tangent approximations.
     * @param args ignored
     */
    public static void main(String[] args) {
      System.out.println("Testing TAN approximations");
      TAN d = new TAN(null, null);
      testApproximations(-10000, 10000, -3, Verbosity.STATISTICS, "out/stat/tan.dat", new Test[] {
        new Test(a -> ".tan        ("+a+")        ", (a,mc) -> d.apply(a.doubleValue()), null,                   null),
        new Test(a -> " approximate("+a+",DECI128)", (a,mc) -> approximate(a, mc),          0, MathContext.DECIMAL128),
        new Test(a -> ".approximate("+a+",CEILING)", (a,mc) -> approximate(a, mc),          1,          Range.CEILING),
        new Test(a -> ".approximate("+a+",FLOOR)  ", (a,mc) -> approximate(a, mc),          1,          Range.FLOOR  ),
        new Test(a -> " taylor     ("+a+",DECI64) ", (a,mc) -> taylor     (a, mc),          1,  MathContext.DECIMAL64),
        new Test(a -> " geometric  ("+a+",DECI64) ", (a,mc) -> geometric  (a, mc),          1,  MathContext.DECIMAL64),
        new Test(a -> " cordic     ("+a+",DECI64) ", (a,mc) -> cordic     (a, mc),          1,  MathContext.DECIMAL64),
      });
    }
  }
  
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Cotangent {@code Implementation}.
   * <pre>cot : {a in R : a != k pi, k in Z} -> R</pre>
   * 
   * <h4>Symmetry and periodicity</h4>
   * <ul>
   *   <li>{@code cot(a) = -cot(-a)}
   *   <li>{@code cot(a) = cot(a + pi)}
   * </ul>
   * 
   * <h4>Relation to other functions</h4>
   * <ul>
   *   <li>{@code cot(a) = 1 / tan(a)}
   *   <li>{@code cot(a) = tan(pi/2 - a)}
   *   <li>{@code cot(a) = cos(a) / sin(a)}
   * </ul>
   * 
   * <h4>Iterative Approximation</h4>
   * To achieve the fastest convergence, the input should be transformed to ]-pi/4,pi/4[ using symmetry, periodicity 
   * and the tangent relation.
   * <p>
   * <table border>
   *   <tr>
   *     <th>Algorithm</th>
   *     <th>Idea</th>
   *     <th>PRO</th>
   *     <th>CON</th>
   *   </tr>
   *   <tr>
   *     <td>{@link #taylor}</td>
   *     <td><pre> cot (a) = sum_{k=0}^m (-1)^k 2^(2k) B_2k a^(2k-1) / (2k)!      for 0 < |a| < pi</pre></td>
   *     <td>converges faster than {@link TAN#taylor} as discontinuity near zero</td>
   *     <td>requires large and accurate Bernoulli numbers, final scaling by division</td>
   *   </tr>
   * </table>
   * <p>
   * See <a href='https://de.wikipedia.org/wiki/Tangens_und_Kotangens'>Wikipedia</a>
   * and <a href='http://mathworld.wolfram.com/Cotangent.html'>Mathematica</a>.
   */
  public static class COT extends Trigonometric {
    
    /** Unique ID for (de)marshalling */
    private static final long serialVersionUID = -4865111057987807908L;
  
    /**
     * Generate a cotangent {@code Implementation}.
     * @param a input  {@link Format}
     * @param r result {@link Format}
     */
    public COT(Format a, Format r) {
      super(a,r);
    }
    
    @Override
    public Number apply(double a) {
      return 1 / Math.tan(a);
    }
    
    /**
     * Approximate a cotangent.
     * @param a   the arithmetic input
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code cot(a)}
     */
    public static BigDecimal approximate(BigDecimal a, MathContext mc) {
      switch (mc.getRoundingMode()) {
        case FLOOR   : // use more precision in iterative approximation to leave rounding control to last step
        case CEILING : a = approximate(a, MathContext.DECIMAL128);
                       return a == null ? null : a.round(mc);
        default      : return geometric(a, mc); 
      }
    }
    
    /**
     * Scale input domain to improve convergence. Also handles poles and roots;
     * 
     * @param a      the arithmetic input
     * @param mc     the decimal precision and {@link RoundingMode} to apply. 
     * @param approx the approximation to be applied to the transformed input (second argument selects tan/cot)
     * @return       cot(a)
     */
    private static BigDecimal prepare(BigDecimal a, MathContext mc, BiFunction<BigDecimal,Boolean,BigDecimal> approx) {
   
      // transform input domain to [-pi/4,pi/4]
      boolean neg = a.compareTo(BigDecimal.ZERO) < 0;
      BigDecimal[] divrem = a.abs().divideAndRemainder(PIQ, mc);
      int section = divrem[0].toBigInteger().remainder(BigInteger.valueOf(4)).intValue();
      a = divrem[1];
      
      // short cut for poles and roots
      if (a.compareTo(BigDecimal.ZERO) == 0) {
        switch (section) {
          case 0 : return null;
          case 2 : return BigDecimal.ZERO; 
        }
      }
        
      
      // section specific mirroring
      switch (section) {
        case  0: a = approx.apply(a,              false); break;           // cot([0pi/4, 1pi/4]) =  cot([    0, pi/4]) 
        case  1: a = approx.apply(PIQ.subtract(a),true);  break;           // cot([1pi/4, 2pi/4]) =  tan([ pi/4,    0]) 
        case  2: a = approx.apply(a.negate(),     true);  break;           // cot([2pi/4, 3pi/4]) =  tan([    0,-pi/4])
        default: a = approx.apply(a.subtract(PIQ),false); break;           // cot([3pi/4, 4pi/4]) =  cot([-pi/4,    0])
      }
      
      // compensate initial abs()
      return neg ? a.negate() : a;
    }
    
    /**
     * Approximate a cotangent by the taylor series 
     * {@code tan(a) = sum_{k=1}^m (-1)^(k-1) a^(2k-1) 2^(2k) (2^(2k)-1) B_2k / (2k)!} and
     * {@code cot(a) = sum_{k=0}^m (-1)^k     a^(2k-1) 2^(2k)            B_2k / (2k)!}
     * 
     * @param a   the arithmetic input
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code cot(a)}
     */
    private static BigDecimal taylor(BigDecimal a, MathContext mc) {
      return prepare(a, mc, (b, tan) -> taylorZero_cotanh(b, mc, true, tan));
    }
    
    /**
     * Approximate a cotangent based on approximating sine and cosine.
     * As sine and cosine can be approximated in parallel, this is faster than using the slow taylor series for tangent
     * with large bernoulli numbers.
     * 
     * @param a   the arithmetic input
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code cot(a)}
     */
    private static BigDecimal geometric(BigDecimal a, MathContext mc) {
      BigDecimal num = COS.approximate(a, mc);
      if (num.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
      BigDecimal den = SIN.approximate(a, mc);
      if (den.compareTo(BigDecimal.ZERO) == 0) return null;
      return num.divide(den, mc);
    }
    
    /**
     * Approximate a cotangent by the {@link CORDIC} algorithm.
     * 
     * @param a   the arithmetic input
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code cot(a)}
     */
    private static BigDecimal cordic(BigDecimal a, MathContext mc) {
      return prepare(a, mc, (b, tan) -> tan ? CORDIC.tan(b, mc) : CORDIC.cot(b, mc));
    }
    
    /**
     * Test accuracy and convergence speed of cotangent approximations.
     * @param args ignored
     */
    public static void main(String[] args) {
      System.out.println("Testing COT approximations");
      COT d = new COT(null, null);
      testApproximations(-10000, 10000, -3, Verbosity.STATISTICS, "out/stat/cot.dat", new Test[] {
        new Test(a -> ".cot        ("+a+")        ", (a,mc) -> d.apply(a.doubleValue()), null,                   null),
        new Test(a -> " approximate("+a+",DECI128)", (a,mc) -> approximate(a, mc),          0, MathContext.DECIMAL128),
        new Test(a -> ".approximate("+a+",CEILING)", (a,mc) -> approximate(a, mc),          1,          Range.CEILING),
        new Test(a -> ".approximate("+a+",FLOOR)  ", (a,mc) -> approximate(a, mc),          1,          Range.FLOOR  ),
        new Test(a -> " taylor     ("+a+",DECI64) ", (a,mc) -> taylor     (a, mc),          1,  MathContext.DECIMAL64),
        new Test(a -> " geometric  ("+a+",DECI64) ", (a,mc) -> geometric  (a, mc),          1,  MathContext.DECIMAL64),
        new Test(a -> " cordic     ("+a+",DECI64) ", (a,mc) -> cordic     (a, mc),          1,  MathContext.DECIMAL64),
      });
    }
  }
  
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Arc sine {@code Implementation}.
   * <pre>asin : [-1,1] -> [-p/2,pi/2]</pre>
   * 
   * <h4>Symmetry</h4>
   * <ul>
   *   <li>{@code asin(a)  = -asin(-a)}
   * </ul>
   * 
   * <h4>Relation to other functions</h4>
   * <ul>
   *   <li>{@code asin(a) = pi/2 - acos(a)}
   *   <li>{@code asin(a) = -i ln(ia + sqrt(1-a²))}
   *   <li>{@code asin(a) = sgn(a) arctan(sqrt(a²/(1-a²)))}
   *   <li>{@code asin(a) = 2 arctan(a / (1+sqrt(1-a²)))}
   * </ul>
   * 
   * <h4>Iterative Approximation</h4>
   * <table border>
   *   <tr>
   *     <th>Algorithm</th>
   *     <th>Idea</th>
   *     <th>PRO</th>
   *     <th>CON</th>
   *   </tr>
   *   <tr>
   *     <td>{@link #taylor}</td>
   *     <td><pre>asin(a) = sum_{k=0}^m (2k)! a^(2k+1) / 2^(2k) / k!² / (2k+1)             for -1 < a < 1</pre></td>
   *     <td></td>
   *     <td></td>
   *   </tr>
   * </table>
   * <p>
   * See <a href='https://de.wikipedia.org/wiki/Arkussinus_und_Arkuskosinus'>Wikipedia</a>
   * and <a href='http://mathworld.wolfram.com/InverseSine.html'>Mathematica</a>.
   */
  public static class ASIN extends Trigonometric {
    
    /** Unique ID for (de)marshalling */
    private static final long serialVersionUID = -8834659265179564907L;
   
    /**
     * Generate a arc sine {@code Implementation}.
     * @param a input  {@link Format}
     * @param r result {@link Format}
     */
    public ASIN(Format a, Format r) {
      super(a,r);
    }
    
    @Override
    public Number apply(double a) {
      return Math.asin(a);
    }
    
    /**
     * Approximate an arc sine.
     * @param a   the arithmetic input ({@code -1 <= a <= 1})
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code asin(a)}
     */
    public static BigDecimal approximate(BigDecimal a, MathContext mc) {
      switch (mc.getRoundingMode()) {
        case FLOOR   : // use more precision in iterative approximation to leave rounding control to last step 
        case CEILING : a = approximate(a, MathContext.DECIMAL128);
                       return a == null ? null : a.round(mc);
        default      : return taylor(a, mc); 
      }
    }
    
    /**
     * Scale input domain to improve convergence. Also handles root and boundaries.
     * 
     * @param a      the arithmetic input
     * @param mc     the decimal precision and {@link RoundingMode} to apply. 
     * @param approx the approximation to be applied to the transformed input
     * @return       asin(a)
     */
    private static BigDecimal prepare(BigDecimal a, MathContext mc, Function<BigDecimal, BigDecimal> approx) {
      // try to reduce |a|
      
      // short cut for root and boundaries
      int c = a.compareTo(BigDecimal.ONE.negate());
      if (c < 0)                             return null;
      if (c == 0)                            return PIH.negate();
      if (a.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
      c = a.compareTo(BigDecimal.ONE);
      if (c > 0)                             return null;
      if (c == 0)                            return PIH;
      
      // invoke asin approximation
      return approx.apply(a);
    }
    
    /**
     * Approximate an arc sine by the taylor series {@code sum_{k=0}^m (2k)! a^(2k+1) / 2^(2k) / k!² / (2k+1)}
     * @param a   the arithmetic input ({@code -1 <= a <= 1})
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code asin(a)}
     */
    private static BigDecimal taylor(BigDecimal a, MathContext mc) {
      return prepare(a, mc, b -> taylorZero_asintanh(a, mc, false, true));
    }
    
    /**
     * Approximate an arc sine by its relation to the arcus tangens: {@code asin(a) = 2 arctan(a / (1+sqrt(1-a²)))}.
     * @param a   the arithmetic input ({@code -1 <= a <= 1})
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code asin(a)}
     */
    private static BigDecimal geometric(BigDecimal a, MathContext mc) {
      return prepare(a, mc, b ->  ZWO.multiply(
                                 ATAN.approximate(
                                    b.divide(BigDecimal.ONE.add(
                                                   SQRT.approximate(BigDecimal.ONE.subtract(
                                                      b.multiply(b, mc), mc), mc), mc), mc), mc), mc));
    }
    
    /**
     * Test accuracy and convergence speed of arc sine approximations.
     * @param args ignored
     */
    public static void main(String[] args) {
      System.out.println("Testing ASIN approximations");
      ASIN d = new ASIN(null, null);
      testApproximations(-1100, 1100, -3, Verbosity.STATISTICS, "out/stat/asin.dat", new Test[] {
        new Test(a -> ".asin       ("+a+")        ", (a,mc) -> d.apply(a.doubleValue()), null,                   null),
        new Test(a -> " approximate("+a+",DECI128)", (a,mc) -> approximate(a, mc),          0, MathContext.DECIMAL128),
        new Test(a -> ".approximate("+a+",CEILING)", (a,mc) -> approximate(a, mc),          1,          Range.CEILING),
        new Test(a -> ".approximate("+a+",FLOOR)  ", (a,mc) -> approximate(a, mc),          1,          Range.FLOOR  ),
        new Test(a -> " taylor     ("+a+",DECI64) ", (a,mc) -> taylor     (a, mc),          1,  MathContext.DECIMAL64),
        new Test(a -> " geometric  ("+a+",DECI64) ", (a,mc) -> geometric  (a, mc),          1,  MathContext.DECIMAL64),
      });
    }
  }
  
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Arc cosine {@code Implementation}.
   * <pre>acos : [-1,1] -> [0,pi]</pre>
   * 
   * <h4>Symmetry</h4>
   * <ul>
   *   <li>{@code acos(a)  = pi - acos(-a)}
   * </ul>
   * 
   * <h4>Relation to other functions</h4>
   * <ul>
   *   <li>{@code acos(a) = pi/2 - asin(-a)}
   *   <li>{@code acos(a) = -i ln(a + i sqrt(1-a²))}
   *   <li>{@code acos(a) = pi/2 - sgn(a) arctan(sqrt(a²/(1-a²)))}
   *   <li>{@code acos(a) = pi/2 - 2 arctan(a / (1+sqrt(1-a²)))}
   *   <li>{@code acos(a) = 2 arctan(sqrt((1-a)/(1+a))) for -1 < a <= 1}
   * </ul>
   * <p>
   * See <a href='https://de.wikipedia.org/wiki/Arkussinus_und_Arkuskosinus'>Wikipedia</a>
   * and <a href='http://mathworld.wolfram.com/InverseCosine.html'>Mathematica</a>.
   */
  public static class ACOS extends Trigonometric {
    
    /** Unique ID for (de)marshalling */
    private static final long serialVersionUID = 5446412457833581909L;
    
    /**
     * Generate a arc cosine {@code Implementation}.
     * @param a input  {@link Format}
     * @param r result {@link Format}
     */
    public ACOS(Format a, Format r) {
      super(a,r);
    }
    
    @Override
    public Number apply(double a) {
      return Math.acos(a);
    }
    
    /**
     * Approximate an arc cosine.
     * @param a   the arithmetic input ({@code -1 <= a <= 1})
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code asin(a)}
     */
    public static BigDecimal approximate(BigDecimal a, MathContext mc) {
      switch (mc.getRoundingMode()) {
        case FLOOR   : // use more precision in iterative approximation to leave rounding control to last step 
        case CEILING : a = approximate(a, MathContext.DECIMAL128);
                       return a == null ? null : a.round(mc);
        default      : a = ASIN.approximate(a, mc);
                       return a == null ? null : PIH.subtract(a, mc); 
      }
    }
    
    /**
     * Test accuracy and convergence speed of arc cosine approximations.
     * @param args ignored
     */
    public static void main(String[] args) {
      System.out.println("Testing ACOS approximations");
      ACOS d = new ACOS(null, null);
      testApproximations(-1100, 1100, -3, Verbosity.STATISTICS, "out/stat/acos.dat", new Test[] {
        new Test(a -> ".acos       ("+a+")        ", (a,mc) -> d.apply(a.doubleValue()), null,                   null),
        new Test(a -> " approximate("+a+",DECI128)", (a,mc) -> approximate(a, mc),          0, MathContext.DECIMAL128),
        new Test(a -> ".approximate("+a+",CEILING)", (a,mc) -> approximate(a, mc),          1,          Range.CEILING),
        new Test(a -> ".approximate("+a+",FLOOR)  ", (a,mc) -> approximate(a, mc),          1,          Range.FLOOR  ),
      });
    }
  }
  
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Arc tangent {@code Implementation}.
   * <pre>atan : R -> [-pi/2,pi/2]</pre>
   * 
   * <h4>Symmetry</h4>
   * <ul>
   *   <li>{@code atan(a)  = -atan(-a)}
   *   <li>{@code atan(a)  = sgn(a)*pi/2 - atan(1/a)}
   * </ul>
   * 
   * <h4>Relation to other functions</h4>
   * <ul>
   *   <li>{@code atan(a)  = acot(1/a) - pi  for a < 0}
   *   <li>{@code atan(a)  = acot(1/a)       for a > 0}
   * </ul>
   * 
   * <h4>Iterative Approximation</h4>
   * The symmetry of the arc tangent can be used to map the whole domain to the convergence radius.
   * <table border>
   *   <tr>
   *     <th>Algorithm</th>
   *     <th>Idea</th>
   *     <th>PRO</th>
   *     <th>CON</th>
   *   </tr>
   *   <tr>
   *     <td>{@link #taylor}</td>
   *     <td><pre>atan(a) = sum_{k=0}^m (-1)^k a^(2k+1) / (2k+1)                            for -1 < a < 1</pre></td>
   *     <td></td>
   *     <td>convergence radius smaller than input domain</td>
   *   </tr>
   *   <tr>
   *     <td>{@link #euler}</td>
   *     <td><pre>atan(a) = 1/a sum_{k=0}^m 2^(2k) k!² / (2k+1)! / (1+1/a²)^(k+1)           for -1 < a < 1</pre></td>
   *     <td>much faster than {@link #euler}, also converges outside [-1,1]</td>
   *     <td></td>
   *   </tr>
   * </table>
   * <p>
   * See <a href='https://de.wikipedia.org/wiki/Arkustangens_und_Arkuskotangens'>Wikipedia</a>
   * and <a href='http://mathworld.wolfram.com/InverseTangent.html'>Mathematica</a>.
   */
  public static class ATAN extends Trigonometric {
    
    /** Unique ID for (de)marshalling */
    private static final long serialVersionUID = 3293663203858310215L;
  
    /**
     * Generate a arc tangent {@code Implementation}.
     * @param a input  {@link Format}
     * @param r result {@link Format}
     */
    public ATAN(Format a, Format r) {
      super(a,r);
    }
    
    @Override
    public Number apply(double a) {
      return Math.atan(a);
    }
    
    /**
     * Approximate an arc tangent.
     * @param a   the arithmetic input
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code atan(a)}
     */
    public static BigDecimal approximate(BigDecimal a, MathContext mc) {
      switch (mc.getRoundingMode()) {
        case FLOOR   : // use more precision in iterative approximation to leave rounding control to last step 
        case CEILING : return approximate(a, MathContext.DECIMAL128).round(mc);
        default      : return taylor(a, mc); //!! do not use cordic here, as CORDIC requires ATANH to init its LUT
      }
    }
    
    /**
     * Map the input domain to the convergence interval [-1,1] and directly handle the root.
     * 
     * @param a      the arithmetic input
     * @param mc     the decimal precision and {@link RoundingMode} to apply. 
     * @param approx the approximation to be applied to the transformed input
     * @return       atan(a)
     */
    private static BigDecimal prepare(BigDecimal a, MathContext mc, Function<BigDecimal, BigDecimal> approx) {
      
      // short cut for root and boundaries
      int c = a.compareTo(BigDecimal.ONE.negate());
      if (c < 0)                             return PIH.add(approx.apply(BigDecimal.ONE.divide(a, mc)), mc).negate();
      if (c == 0)                            return PIQ.negate();
      if (a.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
      c = a.compareTo(BigDecimal.ONE);
      if (c == 0)                            return PIQ;
      if (c > 0)                             return PIH.subtract(approx.apply(BigDecimal.ONE.divide(a, mc)), mc);
      
      // invoke atan approximation
      return approx.apply(a);
    }
    
    /**
     * Approximate an arc tangent by the taylor series {@code sum_{k=0}^m (-1)^k a^(2k+1) / (2k+1)}
     * @param a   the arithmetic input
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code atan(a)}
     */
    private static BigDecimal taylor(BigDecimal a, MathContext mc) {
      return prepare(a, mc, b -> taylorZero_asintanh(b, mc, true, false));
    }
    
    /**
     * Approximate an arc tangent by the euler series 
     * <pre>1/a sum_{k=0}^m 2^(2k) k!² / (2k+1)! / (1+1/a²)^(k+1)}</pre>
     * 
     * @param a   the arithmetic input
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code atan(a)}
     */
    private static BigDecimal euler(BigDecimal a, MathContext mc) {
      return prepare(a, mc, b -> euler_atan(b, mc));
    }
    
    /**
     * Approximate a arc tangent by the {@link CORDIC} algorithm.
     * 
     * @param a   the arithmetic input
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code atan(a)}
     */
    private static BigDecimal cordic(BigDecimal a, MathContext mc) {
      return prepare(a, mc, b -> CORDIC.atan(b, mc));
    }
    
    /**
     * Test accuracy and convergence speed of arc tangent approximations.
     * @param args ignored
     */
    public static void main(String[] args) {
      System.out.println("Testing ATAN approximations");
      ATAN d = new ATAN(null, null);
      testApproximations(-10000, 10000, -3, Verbosity.STATISTICS, "out/stat/atan.dat", new Test[] {
        new Test(a -> ".atan       ("+a+")        ", (a,mc) -> d.apply(a.doubleValue()), null,                   null),
        new Test(a -> " approximate("+a+",DECI128)", (a,mc) -> approximate(a, mc),          0, MathContext.DECIMAL128),
        new Test(a -> ".approximate("+a+",CEILING)", (a,mc) -> approximate(a, mc),          1,          Range.CEILING),
        new Test(a -> ".approximate("+a+",FLOOR)  ", (a,mc) -> approximate(a, mc),          1,          Range.FLOOR  ),
        new Test(a -> " taylor     ("+a+",DECI64) ", (a,mc) -> taylor     (a, mc),          1,  MathContext.DECIMAL64),
        new Test(a -> " euler      ("+a+",DECI64) ", (a,mc) -> euler      (a, mc),          1,  MathContext.DECIMAL64),
        new Test(a -> " cordic     ("+a+",DECI64) ", (a,mc) -> cordic     (a, mc),          1,  MathContext.DECIMAL64),
      });
    }
  }
  
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Arc cotangent {@code Implementation}.
   * <pre>acot : R -> ]0,pi[</pre>
   * 
   * <h4>Symmetry</h4>
   * <ul>
   *   <li>{@code acot(a) = pi - acot(-a)}
   *   <li>{@code acot(a) = (2-sgn(a)) pi/2 - acot(1/a)}
   * </ul>
   * 
   * <h4>Relation to other functions</h4>
   * <ul>
   *   <li>{@code acot(a) = atan(1/a) + pi  for a < 0}
   *   <li>{@code acot(a) = atan(1/a)       for a > 0}
   * </ul>
   * 
   * <h4>Iterative Approximation</h4>
   * The symmetry of the arc tangent can be used to map the whole domain to the convergence radius.
   * <table border>
   *   <tr>
   *     <th>Algorithm</th>
   *     <th>Idea</th>
   *     <th>PRO</th>
   *     <th>CON</th>
   *   </tr>
   *   <tr>
   *     <td>{@link #taylor} at +inf</td>
   *     <td><pre>acot(a) = sum_{k=0}^m (-1)^k (1/a)^(2k+1) / (2k+1)                       for a >= 1</pre></td>
   *     <td>can be implemented as {@link ATAN#taylor}(1/a)</td>
   *     <td>convergence radius smaller than input domain</td>
   *   </tr>
   *   <tr>
   *     <td>euler</td>
   *     <td><pre> cot (a) = a sum_{k=0}^m 2^(2k) k!² / (2k+1)! / (1+a²)^k                  for a >= 1</pre></td>
   *     <td>can be implemented as {@link ATAN#euler}(1/a)</td>
   *     <td></td>
   *   </tr>
   * </table>
   * 
   * <h4>Attention</h4> 
   * Some mathlibs (like wolfram.com) define {@code acot : R -> ]-pi/2,pi/0[ \ {0}}, which is point
   * symmetric to the origin and satisfies {@code acot(a) = atan(1/a)}.
   * TODO: which version uses CAMeL-View?
   * <p>
   * See <a href='https://de.wikipedia.org/wiki/Arkustangens_und_Arkuskotangens'>Wikipedia</a>
   * and <a href='http://mathworld.wolfram.com/InverseCotangent.html'>Mathematica</a>.
   */
  public static class ACOT extends Trigonometric {
    
    /** Unique ID for (de)marshalling */
    private static final long serialVersionUID = 3667542804484744802L;
 
    /**
     * Generate a arc cotangent {@code Implementation}.
     * @param a input  {@link Format}
     * @param r result {@link Format}
     */
    public ACOT(Format a, Format r) {
      super(a,r);
    }
    
    @Override
    public Number apply(double a) {
      return (a < 0) ? Math.atan(1/a) + Math.PI :
             (a > 0) ? Math.atan(1/a)           :
                       Math.PI/2;
    }
    
    /**
     * Approximate an arc cotangent.
     * @param a   the arithmetic input
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code atan(a)}
     */
    public static BigDecimal approximate(BigDecimal a, MathContext mc) {
      switch (mc.getRoundingMode()) {
        case FLOOR   : // use more precision in iterative approximation to leave rounding control to last step 
        case CEILING : return approximate(a, MathContext.DECIMAL128).round(mc);
        default      : return taylor(a,mc);
      }
    }
    
    /**
     * Map the input domain to the convergence interval {@code R \ [-1,1]} and directly handle the root.
     * 
     * @param a      the arithmetic input
     * @param mc     the decimal precision and {@link RoundingMode} 
     * @param approx the approximation to be applied to applied to {@code 1/a}.
     * @return       acot(a)
     */
    private static BigDecimal prepare(BigDecimal a, MathContext mc, Function<BigDecimal, BigDecimal> approx) {
      
      // a == 0 => pi/2
      int sgn = a.signum();
      if (sgn == 0) return PIH;
      
      switch (a.abs().compareTo(BigDecimal.ONE)) {
        //              a == -1 => 3pi/4,             a == 1 => pi/4
        case 0 : return sgn < 0 ?  PI.subtract(PIQ) :           PIQ;
        
        // |a| < 1 => cot(a) = (3)pi/2 - cot(1/a) = (3)pi/2 - (tan(a) (+pi)) = pi/2 - tan a   | values in braces for a<0
        case -1 : return PIH.subtract(approx.apply(a));
        
        // |a| > 1 => 1/a can be passed to atan
        default : a = approx.apply(BigDecimal.ONE.divide(a, mc));
                  return sgn < 0 ? a.add(PI) : a;
      }
    }
    
    /**
     * Approximate an arc cotangent by taylor series {@code sum_{k=0}^m (-1)^k (1/a)^(2k+1) / (2k+1)}
     * @param a   the arithmetic input
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code acot(a)}
     */
    private static BigDecimal taylor(BigDecimal a, MathContext mc) {
      // pass 1/a to taylor series of atan
      return prepare(a, mc, b -> taylorZero_asintanh(b, mc, true, false));
    }
    
    /**
     * Approximate an arc cotangent by euler series {@code a sum_{k=0}^m 2^(2k-1) (k-1)!² k / (1+a²)^k / (2k)!}.
     * @param a   the arithmetic input
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code acot(a)}
     */
    private static BigDecimal euler(BigDecimal a, MathContext mc) {
      // pass 1/a to euler series of atan
      return prepare(a, mc, b -> euler_atan(b, mc)); 
    }
    
    /**
     * Approximate an arc cotangent by {@code atan(1/a) + a<0 ? pi : 0}.
     * @param a   the arithmetic input
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code acot(a)}
     */
    private static BigDecimal geometric(BigDecimal a, MathContext mc) {
      // pass 1/a to general atan (this will unnecessarily cause reanalyzing the input domain)
      return prepare(a, mc, b -> ATAN.approximate(b, mc)); 
    }
    
    /**
     * Approximate a arc cotangent by the {@link CORDIC} algorithm.
     * 
     * @param a   the arithmetic input
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code acot(a)}
     */
    private static BigDecimal cordic(BigDecimal a, MathContext mc) {
      return prepare(a, mc, b -> CORDIC.acot(b, mc));
    }
    
    /**
     * Test accuracy and convergence speed of arc cotangent approximations.
     * @param args ignored
     */
    public static void main(String[] args) {
      System.out.println("Testing ACOT approximations");
      ACOT d = new ACOT(null, null);
      testApproximations(-10000, 10000, -3, Verbosity.STATISTICS, "out/stat/acot.dat", new Test[] {
        new Test(a -> ".acot       ("+a+")        ", (a,mc) -> d.apply(a.doubleValue()), null,                   null),
        new Test(a -> " approximate("+a+",DECI128)", (a,mc) -> approximate(a, mc),          0, MathContext.DECIMAL128),
        new Test(a -> ".approximate("+a+",CEILING)", (a,mc) -> approximate(a, mc),          1,          Range.CEILING),
        new Test(a -> ".approximate("+a+",FLOOR)  ", (a,mc) -> approximate(a, mc),          1,          Range.FLOOR  ),
        new Test(a -> " taylor     ("+a+",DECI64) ", (a,mc) -> taylor     (a, mc),          1,  MathContext.DECIMAL64),
        new Test(a -> " euler      ("+a+",DECI64) ", (a,mc) -> euler      (a, mc),          1,  MathContext.DECIMAL64),
        new Test(a -> " geometric  ("+a+",DECI64) ", (a,mc) -> geometric  (a, mc),          1,  MathContext.DECIMAL64),
        new Test(a -> " cordic     ("+a+",DECI64) ", (a,mc) -> cordic     (a, mc),          1,  MathContext.DECIMAL64),
      });
    }
  }
  
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Hyperbolic sine {@code Implementation}.
   * <pre> sinh : R -> R</pre>
   * 
   * <h4>Symmetry</h4>
   * <ul>
   *   <li>{@code sinh(a)  = -sinh(-a)}
   * </ul>
   * 
   * <h4>Relation to other functions</h4>
   * <ul>
   *   <li>{@code sinh(a)    = (exp(a) - 1/exp(a)) / 2}
   *   <li>{@code sinh(a)    = exp(a) - cosh(a)}
   *   <li>{@code sinh(a)    = sqrt(cosh(a)² - 1)}
   *   <li>{@code sinh(2a)   = 2 sinh(a) cosh(a)}
   *   <li>{@code sinh(a+-b) = sinh(a) cosh(b) +- cosh(a) sinh(b)}
   * </ul>
   * 
   * <h4>Iterative Approximation</h4>
   * The convergence radius is unlimited, but fastest convergence is reached near zero. Splitting the approximation in 
   * two (or more) approximations of smaller arguments could be worthwhile (not tested jet)
   * <p>
   * <table border>
   *   <tr>
   *     <th>Algorithm</th>
   *     <th>Idea</th>
   *     <th>PRO</th>
   *     <th>CON</th>
   *   </tr>
   *   <tr>
   *     <td>{@link #taylor}</td>
   *     <td><pre>sinh(a) = sum_{k=0}^m a^(2k+1)/(2k+1)!</pre></td>
   *     <td>fast convergence</td>
   *     <td>requires large division in each iteration</td>
   *   </tr>
   *   <tr>
   *     <td>{@link #product}</td>
   *     <td><pre>sinh(a) = a mul_{k=1}^m 1+(a/(k pi))²</pre></td>
   *     <td></td>
   *     <td>based on PI, much to slow convergence</td>
   *   </tr>
   * </table>
   * <p>
   * See <a href='https://de.wikipedia.org/wiki/Sinus_Hyperbolicus_und_Kosinus_Hyperbolicus'>Wikipedia</a>
   * and <a href='http://mathworld.wolfram.com/HyperbolicSine.html'>Mathematica</a>.
   */
  public static class SINH extends Trigonometric {
    
    /** Unique ID for (de)marshalling */
    private static final long serialVersionUID = -6028356322848510546L;
  
    /**
     * Generate a hyperbolic sine {@code Implementation}.
     * @param a input  {@link Format}
     * @param r result {@link Format}
     */
    public SINH(Format a, Format r) {
      super(a,r);
    }
    
    @Override
    public Number apply(double a) {
      return Math.sinh(a);
    }
    
    /**
     * Approximate a hyperbolic sine.
     * @param a   the arithmetic input
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code sinh(a)}
     */
    public static BigDecimal approximate(BigDecimal a, MathContext mc) {
      switch (mc.getRoundingMode()) {
        case FLOOR   : // use more precision in iterative approximation to leave rounding control to last step 
        case CEILING : return approximate(a, MathContext.DECIMAL128).round(mc);
        default      : return taylor(a, mc); 
      }
    }
    
    /**
     * Scale input domain to improve convergence. Also handles root.
     * 
     * @param a      the arithmetic input
     * @param mc     the decimal precision and {@link RoundingMode} to apply. 
     * @param approx the approximation to be applied to the transformed input
     * @return       sinh(a)
     */
    private static BigDecimal prepare(BigDecimal a, MathContext mc, Function<BigDecimal, BigDecimal> approx) {
      // try to reduce |a| by addition theorems
      
      // short cut for root
      if (a.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
      
      // invoke sinh approximation
      return approx.apply(a);
    }
    
    /**
     * Approximate a hyperbolic sine by the taylor series {@code sinh(a) = sum_{k=0}^m a^(2k+1)/(2k+1)!} and
     *                                                    {@code cosh(a) = sum_{k=0}^m a^(2k)  /(2k)!}
     * @param a   the arithmetic input
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code sinh(a)}
     */
    private static BigDecimal taylor(BigDecimal a, MathContext mc) {
      return prepare(a, mc, b -> taylorZero_cosinh(b, mc, false, true));
    }
    
    /**
     * Approximate a hyperbolic sine by the product series {@code sinh(a) = a mul_{k=1}^m 1+(a/( k      pi))²} and
     *                                                     {@code cosh(a) =   mul_{k=1}^m 1+(a/((k-1/2) pi))²}
     * <p>
     * <b>Attention:</b> very slow and does not reach requested accuracy!
     * 
     * @param a   the arithmetic input
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code sinh(a)}
     */
    private static BigDecimal product(BigDecimal a, MathContext mc) {
      return prepare(a, mc, b -> product_cosinh(b, mc, false, true));
    }
    
    /**
     * Approximate a hyperbolic sine by its relation to the exponential function.
     * For large values, the taylor series of the exponential function converges faster than the hyperbolic sine.
     * 
     * @param a   the arithmetic input
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code sinh(a)}
     */
    private static BigDecimal exponential(BigDecimal a, MathContext mc) {
      // additional accuracy for intermediate results
      mc = new MathContext(mc.getPrecision()+2, mc.getRoundingMode());
      
      BigDecimal e = EXP.approximate(a, mc);
      return e.subtract(BigDecimal.ONE.divide(e, mc)).divide(ZWO, mc);
    }
    
    /**
     * Approximate a hyperbolic sine by the {@link CORDIC} algorithm.
     * 
     * @param a   the arithmetic input
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code sinh(a)}
     */
    private static BigDecimal cordic(BigDecimal a, MathContext mc) {
      return prepare(a, mc, b -> CORDIC.sinh(b, mc));
    }
    
    /**
     * Test accuracy and convergence speed of hyperbolic sine approximations.
     * @param args ignored
     */
    public static void main(String[] args) {
      System.out.println("Testing SINH approximations");
      SINH d = new SINH(null, null);
      testApproximations(-10000, 10000, -3, Verbosity.STATISTICS, "out/stat/sinh.dat", new Test[] {
        new Test(a -> ".sinh       ("+a+")        ", (a,mc) -> d.apply(a.doubleValue()), null,                   null),
        new Test(a -> " approximate("+a+",DECI128)", (a,mc) -> approximate(a, mc),          0, MathContext.DECIMAL128),
        new Test(a -> ".approximate("+a+",CEILING)", (a,mc) -> approximate(a, mc),          1,          Range.CEILING),
        new Test(a -> ".approximate("+a+",FLOOR)  ", (a,mc) -> approximate(a, mc),          1,          Range.FLOOR  ),
        new Test(a -> " taylor     ("+a+",DECI64) ", (a,mc) -> taylor     (a, mc),          1,  MathContext.DECIMAL64),
        new Test(a -> " product    ("+a+",DECI64) ", (a,mc) -> product    (a, mc),          1,  MathContext.DECIMAL64),
        new Test(a -> " exponential("+a+",DECI64) ", (a,mc) -> exponential(a, mc),          1,  MathContext.DECIMAL64),
        new Test(a -> " cordic     ("+a+",DECI64) ", (a,mc) -> cordic     (a, mc),          1,  MathContext.DECIMAL64),
      });
    }
  }
  
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Hyperbolic cosine {@code Implementation}.
   * <pre> cosh : R -> [1,+inf[</pre>
   * 
   * <h4>Symmetry</h4>
   * <ul>
   *   <li>{@code cosh(a) = cosh(-a)}
   * </ul>
   * 
   * <h4>Relation to other functions</h4>
   * <ul>
   *   <li>{@code cosh(a)    = (exp(a) + 1/exp(a) / 2}
   *   <li>{@code cosh(a)    = exp(a) - sinh(a)}
   *   <li>{@code cosh(a)    = sqrt(sinh(a)² + 1)}
   *   <li>{@code cosh(2a)   = 1 + 2 sinh(a)²}
   *   <li>{@code cosh(a+-b) = cosh(a) cosh(b) +- sinh(a) sinh(b)} 
   * </ul>
   * 
   * <h4>Iterative Approximation</h4>
   * The convergence radius is unlimited, but fastest convergence is reached near zero. Splitting the approximation in 
   * two (or more) approximations of smaller arguments could be worthwhile (not tested jet).
   * <p>
   * <table border>
   *   <tr>
   *     <th>Algorithm</th>
   *     <th>Idea</th>
   *     <th>PRO</th>
   *     <th>CON</th>
   *   </tr>
   *   <tr>
   *     <td>{@link #taylor}</td>
   *     <td><pre>cosh(a) = sum_{k=0}^m a^(2k)/(2k)!</pre></td>
   *     <td>fast convergence</td>
   *     <td>requires large division in each iteration</td>
   *   </tr>
   *   <tr>
   *     <td>{@link #product}</td>
   *     <td><pre>sinh(a) = mul_{k=1}^m 1+(a/((k-1/2) pi))²</pre></td>
   *     <td></td>
   *     <td>based on PI, much to slow convergence</td>
   *   </tr>
   * </table>
   * <p>
   * See <a href='https://de.wikipedia.org/wiki/Sinus_Hyperbolicus_und_Kosinus_Hyperbolicus'>Wikipedia</a>
   * and <a href='http://mathworld.wolfram.com/HyperbolicCosine.html'>Mathematica</a>.
   */
  public static class COSH extends Trigonometric {
    
    /** Unique ID for (de)marshalling */
    private static final long serialVersionUID = -5460353184375290914L;
  
    /**
     * Generate a hyperbolic cosine {@code Implementation}.
     * @param a input  {@link Format}
     * @param r result {@link Format}
     */
    public COSH(Format a, Format r) {
      super(a,r);
    }
    
    @Override
    public Number apply(double a) {
      return Math.cosh(a);
    }
    
    /**
     * Approximate a hyperbolic cosine.
     * @param a   the arithmetic input
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code cosh(a)}
     */
    public static BigDecimal approximate(BigDecimal a, MathContext mc) {
      switch (mc.getRoundingMode()) {
        case FLOOR   : // use more precision in iterative approximation to leave rounding control to last step 
        case CEILING : return approximate(a, MathContext.DECIMAL128).round(mc);
        default      : return taylor(a, mc); 
      }
    }
    
    /**
     * Scale input domain to improve convergence. 
     * 
     * @param a      the arithmetic input
     * @param mc     the decimal precision and {@link RoundingMode} to apply. 
     * @param approx the approximation to be applied to the transformed input
     * @return       cosh(a)
     */
    private static BigDecimal prepare(BigDecimal a, MathContext mc, Function<BigDecimal, BigDecimal> approx) {
      // try to reduce |a| by addition theorems
      
      // invoke cosh approximation
      return approx.apply(a);
    }
    
    /**
     * Approximate a hyperbolic cosine by the taylor series {@code sinh(a) = sum_{k=0}^m a^(2k+1)/(2k+1)!} and
     *                                                      {@code cosh(a) = sum_{k=0}^m a^(2k)  /(2k)!}
     * @param a   the arithmetic input
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code cosh(a)}
     */
    private static BigDecimal taylor(BigDecimal a, MathContext mc) {
      return prepare(a, mc, b -> taylorZero_cosinh(b, mc, false, false));
    }
    
    /**
     * Approximate a hyperbolic cossine by the product series {@code sinh(a) = a mul_{k=1}^m 1+(a/( k      pi))²} and
     *                                                        {@code cosh(a) =   mul_{k=1}^m 1+(a/((k-1/2) pi))²}
     * <p>
     * <b>Attention:</b> very slow and does not reach requested accuracy!
     * 
     * @param a   the arithmetic input
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code cosh(a)}
     */
    private static BigDecimal product(BigDecimal a, MathContext mc) {
      return prepare(a, mc, b -> product_cosinh(b, mc, false, false));
    }
    
    /**
     * Approximate a hyperbolic cosine by its relation to the exponential function.
     * For large values, the taylor series of the exponential function converges faster than the hyperbolic cosine.
     * 
     * @param a   the arithmetic input
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code cosh(a)}
     */
    private static BigDecimal exponential(BigDecimal a, MathContext mc) {
      // additional accuracy for intermediate results
      mc = new MathContext(mc.getPrecision()+2, mc.getRoundingMode());
      
      BigDecimal e = EXP.approximate(a, mc);
      return e.add(BigDecimal.ONE.divide(e, mc)).divide(ZWO, mc);
    }
    
    /**
     * Approximate a hyperbolic cosine by the {@link CORDIC} algorithm.
     * 
     * @param a   the arithmetic input
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code cosh(a)}
     */
    private static BigDecimal cordic(BigDecimal a, MathContext mc) {
      return prepare(a, mc, b -> CORDIC.cosh(b, mc));
    }
    
    /**
     * Test accuracy and convergence speed of hyperbolic cosine approximations.
     * @param args ignored
     */
    public static void main(String[] args) {
      System.out.println("Testing COSH approximations");
      COSH d = new COSH(null, null);
      testApproximations(-10000, 10000, -3, Verbosity.STATISTICS, "out/stat/cosh.dat", new Test[] {
        new Test(a -> ".cosh       ("+a+")        ", (a,mc) -> d.apply(a.doubleValue()), null,                   null),
        new Test(a -> " approximate("+a+",DECI128)", (a,mc) -> approximate(a, mc),          0, MathContext.DECIMAL128),
        new Test(a -> ".approximate("+a+",CEILING)", (a,mc) -> approximate(a, mc),          1,          Range.CEILING),
        new Test(a -> ".approximate("+a+",FLOOR)  ", (a,mc) -> approximate(a, mc),          1,          Range.FLOOR  ),
        new Test(a -> " taylor     ("+a+",DECI64) ", (a,mc) -> taylor     (a, mc),          1,  MathContext.DECIMAL64),
        new Test(a -> " product    ("+a+",DECI64) ", (a,mc) -> product    (a, mc),          1,  MathContext.DECIMAL64),
        new Test(a -> " exponential("+a+",DECI64) ", (a,mc) -> exponential(a, mc),          1,  MathContext.DECIMAL64),
        new Test(a -> " cordic     ("+a+",DECI64) ", (a,mc) -> cordic     (a, mc),          1,  MathContext.DECIMAL64),
      });
    }
  }
  
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Hyperbolic tangent {@code Implementation}.
   * <pre> tanh : R -> [-1,1]</pre>
   * 
   * <h4>Symmetry</h4>
   * <ul>
   *   <li>{@code tanh(a) = -tanh(-a)}
   * </ul>
   * 
   * <h4>Relation to other functions</h4>
   * <ul>
   *   <li>{@code tanh(a) = 1 / coth(a)}
   *   <li>{@code tanh(a) = sinh(a) / cosh(a)}
   *   <li>{@code tanh(a) = 1 - 2 / (exp(2a)+1)}
   *   <li>{@code tanh(a) = sinh(a) / sqrt(1 + sinh(a)²)}
   * </ul>
   * 
   * <h4>Iterative Approximation</h4>
   * There is no good symmetry to map large inputs to the convergence radius. A fallback to another approximation is
   * thus required. 
   * <table border>
   *   <tr>
   *     <th>Algorithm</th>
   *     <th>Idea</th>
   *     <th>PRO</th>
   *     <th>CON</th>
   *   </tr>
   *   <tr>
   *     <td>{@link #taylor}</td>
   *     <td><pre> tanh(a) = sum_{k=1}^m 2^(2k) (2^(2k)-1) B_2k a^(2k-1) / (2k)!        for -pi/2 < a < pi/2</pre></td>
   *     <td></td>
   *     <td>convergence radius smaller than input domain</td>
   *   </tr>
   * </table>
   * <p>
   * See <a href='https://de.wikipedia.org/wiki/Tangens_Hyperbolicus_und_Kotangens_Hyperbolicus'>Wikipedia</a>
   * and <a href='http://mathworld.wolfram.com/HyperbolicTangent.html'>Mathematica</a>.
   */
  public static class TANH extends Trigonometric {
   
    /** Unique ID for (de)marshalling */
    private static final long serialVersionUID = -5839248888764761867L;
  
    /**
     * Generate a hyperbolic tangent {@code Implementation}.
     * @param a input  {@link Format}
     * @param r result {@link Format}
     */
    public TANH(Format a, Format r) {
      super(a,r);
    }
    
    @Override
    public Number apply(double a) {
      return Math.tanh(a);
    }
    
    /**
     * Approximate a hyperbolic tangent.
     * @param a   the arithmetic input
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code tanh(a)}
     */
    public static BigDecimal approximate(BigDecimal a, MathContext mc) {
      switch (mc.getRoundingMode()) {
      case FLOOR   : // use more precision in iterative approximation to leave rounding control to last step  
      case CEILING : a = approximate(a, MathContext.DECIMAL128);
                     return a == null ? null : a.round(mc);
      default      : return taylor(a, mc);
        
      }
    }
    
    /**
     * Scale input domain to improve convergence. Also handles root.
     * 
     * @param a      the arithmetic input
     * @param mc     the decimal precision and {@link RoundingMode} to apply. 
     * @param approx the approximation to be applied to the transformed input
     * @return       tanh(a)
     */
    private static BigDecimal prepare(BigDecimal a, MathContext mc, Function<BigDecimal,BigDecimal> approx) {
      // try to reduce |a|
      
      // short cut for root
      if (a.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
      
      // invoke tanh approximation
      return approx.apply(a);
    }
    
    /**
     * Approximate a hyperbolic tangent by the taylor series {@code sum_{k=1}^m 2^(2k) (2^(2k)-1) B_2k a^(2k-1)/(2k)!}
     * 
     * @param a   the arithmetic input
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code tanh(a)}
     */
    private static BigDecimal taylor(BigDecimal a, MathContext mc) {
      return prepare(a, mc, b -> taylorZero_cotanh(b, mc, false, true));
    }
    
    /**
     * Approximate a hyperbolic tangent based on approximating hyperbolic sine and cosine.
     * As sine and cosine can be approximated in parallel, this is faster than using the slow taylor series for tangent
     * with large bernoulli numbers.
     * 
     * @param a   the arithmetic input
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code tanh(a)}
     */
    private static BigDecimal geometric(BigDecimal a, MathContext mc) {
      // cosh(a) >= 1 for all a in R => no div zero check required
      return prepare(a, mc, b -> SINH.approximate(b, mc).divide(COSH.approximate(b, mc), mc)); 
    }
    
    /**
     * Approximate a hyperbolic tangent by its relation to the exponential function.
     * For large values, the taylor series of the exponential function converges faster than the hyperbolic tangent.
     * 
     * @param a   the arithmetic input
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code tanh(a)}
     */
    private static BigDecimal exponential(BigDecimal a, MathContext mc) {
      // additional accuracy for intermediate results
      final MathContext mc2 = new MathContext(mc.getPrecision()+2, mc.getRoundingMode());
      
      return prepare(a, mc2, b -> BigDecimal.ONE.subtract(ZWO.divide(
                                 EXP.approximate(b.add(b, mc2), mc2).add(BigDecimal.ONE), mc2), mc2));
    }
    
    /**
     * Approximate a hyperbolic tangent by the {@link CORDIC} algorithm.
     * 
     * @param a   the arithmetic input
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code tanh(a)}
     */
    private static BigDecimal cordic(BigDecimal a, MathContext mc) {
      return prepare(a, mc, b -> CORDIC.tanh(b, mc));
    }
    
    /**
     * Test accuracy and convergence speed of hyperbolic tangent approximations.
     * @param args ignored
     */
    public static void main(String[] args) {
      System.out.println("Testing TANH approximations");
      TANH d = new TANH(null, null);
      testApproximations(-10000, 10000, -3, Verbosity.STATISTICS, "out/stat/tanh.dat", new Test[] {
        new Test(a -> ".tanh       ("+a+")        ", (a,mc) -> d.apply(a.doubleValue()), null,                   null),
        new Test(a -> " approximate("+a+",DECI128)", (a,mc) -> approximate(a, mc),          0, MathContext.DECIMAL128),
        new Test(a -> ".approximate("+a+",CEILING)", (a,mc) -> approximate(a, mc),          1,          Range.CEILING),
        new Test(a -> ".approximate("+a+",FLOOR)  ", (a,mc) -> approximate(a, mc),          1,          Range.FLOOR  ),
        new Test(a -> " taylor     ("+a+",DECI64) ", (a,mc) -> taylor     (a, mc),          1,  MathContext.DECIMAL64),
        new Test(a -> " geometric  ("+a+",DECI64) ", (a,mc) -> geometric  (a, mc),          1,  MathContext.DECIMAL64),
        new Test(a -> " exponential("+a+",DECI64) ", (a,mc) -> exponential(a, mc),          1,  MathContext.DECIMAL64),
        new Test(a -> " cordic     ("+a+",DECI64) ", (a,mc) -> cordic     (a, mc),          1,  MathContext.DECIMAL64),
      });
    }
  }
  
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Hyperbolic cotangent {@code Implementation}.
   * <pre> coth : R \ {0} -> R \ [-1,1]</pre>
   * 
   * <h4>Symmetry</h4>
   * <ul>
   *   <li>{@code coth(a) = -coth(-a)}
   * </ul>
   * 
   * <h4>Relation to other functions</h4>
   * <ul>
   *   <li>{@code coth(a) = 1 / tanh(a)}
   *   <li>{@code coth(a) = cosh(a) / sinh(a)}
   *   <li>{@code coth(a) = 1 + 2 / (exp(2a)-1)}
   *   <li>{@code coth(a) = sqrt(1 + sinh(a)²) / sinh(a)}
   * </ul>
   * 
   * <h4>Iterative Approximation</h4>
   * There is no good symmetry to map large inputs to the convergence radius. A fallback to another approximation is
   * thus required. 
   * <table border>
   *   <tr>
   *     <th>Algorithm</th>
   *     <th>Idea</th>
   *     <th>PRO</th>
   *     <th>CON</th>
   *   </tr>
   *   <tr>
   *     <td>{@link #taylor}</td>
   *     <td><pre> coth(a) = sum_{k=0}^m 2^(2k) B_2k a^(2k-1) / (2k)!                    for 0 < |a| < pi</pre></td>
   *     <td></td>
   *     <td>convergence radius smaller than input domain</td>
   *   </tr>
   * </table>
   * <p>
   * See <a href='https://de.wikipedia.org/wiki/Tangens_Hyperbolicus_und_Kotangens_Hyperbolicus'>Wikipedia</a>
   * and <a href='http://mathworld.wolfram.com/HyperbolicCotangent.html'>Mathematica</a>.
   */
  public static class COTH extends Trigonometric {
    
    /** Unique ID for (de)marshalling */
    private static final long serialVersionUID = -1921188870682479103L;
  
    /**
     * Generate a hyperbolic cotangent {@code Implementation}.
     * @param a input  {@link Format}
     * @param r result {@link Format}
     */
    public COTH(Format a, Format r) {
      super(a,r);
    }
    
    @Override
    public Number apply(double a) {
      return 1/Math.tanh(a);
    }
    
    /**
     * Approximate a hyperbolic cotangent.
     * @param a   the arithmetic input
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code coth(a)}
     */
    public static BigDecimal approximate(BigDecimal a, MathContext mc) {
      switch (mc.getRoundingMode()) {
        case FLOOR   : // use more precision in iterative approximation to leave rounding control to last step  
        case CEILING : a = approximate(a, MathContext.DECIMAL128);
                       return a == null ? null : a.round(mc);
        default      : return taylor(a, mc);
      }
    }
    
    /**
     * Scale input domain to improve convergence. Also handles pole.
     * 
     * @param a      the arithmetic input
     * @param mc     the decimal precision and {@link RoundingMode} to apply. 
     * @param approx the approximation to be applied to the transformed input
     * @return       coth(a)
     */
    private static BigDecimal prepare(BigDecimal a, MathContext mc, Function<BigDecimal, BigDecimal> approx) {
      // try to reduce |a|
      
      // short cut for pole
      if (a.compareTo(BigDecimal.ZERO) == 0) return null;
      
      // invoke coth approximation
      return approx.apply(a);
    }
    
    /**
     * Approximate a hyperbolic cotangent by the taylor series {@code sum_{k=0}^m 2^(2k) B_2k a^(2k-1) / (2k)!}
     * 
     * @param a   the arithmetic input
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code coth(a)}
     */
    private static BigDecimal taylor(BigDecimal a, MathContext mc) {
      return prepare(a, mc, b -> taylorZero_cotanh(b, mc, false, false));
    }
    
    /**
     * Approximate a hyperbolic cotangent based on approximating hyperbolic sine and cosine.
     * As sine and cosine can be approximated in parallel, this is faster than using the slow taylor series for 
     * cotangent with large bernoulli numbers.
     * 
     * @param a   the arithmetic input
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code coth(a)}
     */
    private static BigDecimal geometric(BigDecimal a, MathContext mc) {
      // cosh(a) >= 1 for all a in R => no zero check required
      return prepare(a, mc, b -> COSH.approximate(b,mc).divide(SINH.approximate(b,mc),mc)); 
    }
    
    /**
     * Approximate a hyperbolic cotangent by its relation to the exponential function.
     * For large values, the taylor series of the exponential function converges faster than the hyperbolic cotangent.
     * 
     * @param a   the arithmetic input
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code tanh(a)}
     */
    private static BigDecimal exponential(BigDecimal a, MathContext mc) {
      // additional accuracy for intermediate results
      final MathContext mc2 = new MathContext(mc.getPrecision()+2, mc.getRoundingMode());
      
      return prepare(a, mc2, b -> BigDecimal.ONE.add(ZWO.divide(
                                 EXP.approximate(b.add(b, mc2), mc2).subtract(BigDecimal.ONE), mc2), mc2));
    }
    
    /**
     * Approximate a hyperbolic cotangent by the {@link CORDIC} algorithm.
     * 
     * @param a   the arithmetic input
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code coth(a)}
     */
    private static BigDecimal cordic(BigDecimal a, MathContext mc) {
      return prepare(a, mc, b -> CORDIC.coth(b, mc));
    }
    
    /**
     * Test accuracy and convergence speed of hyperbolic cotangent approximations.
     * @param args ignored
     */
    public static void main(String[] args) {
      System.out.println("Testing COTH approximations");
      COTH d = new COTH(null, null);
      testApproximations(-10000, 10000, -3, Verbosity.STATISTICS, "out/stat/coth.dat", new Test[] {
        new Test(a -> ".coth       ("+a+")        ", (a,mc) -> d.apply(a.doubleValue()), null,                   null),
        new Test(a -> " approximate("+a+",DECI128)", (a,mc) -> approximate(a, mc),          0, MathContext.DECIMAL128),
        new Test(a -> ".approximate("+a+",CEILING)", (a,mc) -> approximate(a, mc),          1,          Range.CEILING),
        new Test(a -> ".approximate("+a+",FLOOR)  ", (a,mc) -> approximate(a, mc),          1,          Range.FLOOR  ),
        new Test(a -> " taylor     ("+a+",DECI64) ", (a,mc) -> taylor     (a, mc),          1,  MathContext.DECIMAL64),
        new Test(a -> " geometric  ("+a+",DECI64) ", (a,mc) -> geometric  (a, mc),          1,  MathContext.DECIMAL64),
        new Test(a -> " exponential("+a+",DECI64) ", (a,mc) -> exponential(a, mc),          1,  MathContext.DECIMAL64),
        new Test(a -> " cordic     ("+a+",DECI64) ", (a,mc) -> cordic     (a, mc),          1,  MathContext.DECIMAL64),
      });
    }
  }
  
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Area hyperbolic sine {@code Implementation}.
   * <pre>asinh : R -> R</pre>
   * 
   * <h4>Symmetry</h4>
   * <ul>
   *   <li>{@code asinh(a)  = -asinh(-a)}
   * </ul>
   * 
   * <h4>Relation to other functions</h4>
   * <ul>
   *   <li>{@code asinh(a) = ln(a + sqrt(a²+1))}
   *   <li>{@code asinh(a) = sgn(a) acosh(sqrt(a²+1))}
   * </ul>
   * 
   * <h4>Iterative Approximation</h4>
   * Both taylor series and the symmetry have to be combined to cover the whole input domain by convergence intervals.
   * <table border>
   *   <tr>
   *     <th>Algorithm</th>
   *     <th>Idea</th>
   *     <th>PRO</th>
   *     <th>CON</th>
   *   </tr>
   *   <tr>
   *     <td>{@link #taylor} at 0</td>
   *     <td><pre>asinh(a) = sum_{k=0}^m (-1)^k (2k)! a^(2k+1) / 2^(2k) / k!² / (2k+1)       for -1 < a < 1</pre></td>
   *     <td></td>
   *     <td>convergence radius smaller than input domain</td>
   *   </tr>
   *   <tr>
   *     <td>{@link #taylor} at +inf</td>
   *     <td><pre>asinh(a) = 1+ln(2a)-sum_{k=0}^m (-1)^(k+1) (2k)! / a^(2k) / 2^(2k) / k!² / (2k) for a > 1 </pre></td>
   *     <td></td>
   *     <td>convergence radius smaller than input domain</td>
   *   </tr>
   * </table>
   * <p>
   * See <a href='https://de.wikipedia.org/wiki/Areasinus_Hyperbolicus_und_Areakosinus_Hyperbolicus'>Wikipedia</a>
   * and <a href='http://mathworld.wolfram.com/InverseHyperbolicSine.html'>Mathematica</a>.
   */
  public static class ASINH extends Trigonometric {
    
    /** Unique ID for (de)marshalling */
    private static final long serialVersionUID = -1426486343717423029L;
  
    /**
     * Generate a arc hyperbolic sine {@code Implementation}.
     * @param a input  {@link Format}
     * @param r result {@link Format}
     */
    public ASINH(Format a, Format r) {
      super(a,r);
    }
    
    @Override
    public Number apply(double a) {
      return Math.log(a + Math.sqrt(a*a+1));
    }
    
    /**
     * Approximate an area hyperbolic sine.
     * Based on {@link LN#approximate} and {@link SQRT#approximate}.
     * @param a   the arithmetic input
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code asin(a)}
     */
    public static BigDecimal approximate(BigDecimal a, MathContext mc) {
      switch (mc.getRoundingMode()) {
        case FLOOR   : // use more precision in iterative approximation to leave rounding control to last step 
        case CEILING : return approximate(a, MathContext.DECIMAL128).round(mc);
        default      : return taylor(a, mc);
      }
    }
    
    /**
     * Select convergence radius. Also handles root.
     * 
     * @param a      the arithmetic input
     * @param mc     the decimal precision and {@link RoundingMode} to apply. 
     * @param approx the approximation to be applied to the transformed input (second argument for true for b > 1)
     * @return       asinh(a)
     */
    private static BigDecimal prepare(BigDecimal a, MathContext mc, BiFunction<BigDecimal,Boolean,BigDecimal> approx) {
      
      // check for root
      int sgn = a.signum();
      if (sgn == 0) return BigDecimal.ZERO;
      
      // select appropriate series depending on |a| <> 1
      BigDecimal abs = a.abs();
      switch (abs.compareTo(BigDecimal.ONE)) {
        case -1 : return approx.apply(a, false);
        
        // taylor series at +inf only valid for right branch => use the symmetry of asinh 
        case  0 : a = LN.approximate(BigDecimal.ONE.add(SQRT.TWO, mc), mc);
        case  1 : a = approx.apply(abs, true);
      }
      return sgn > 0 ? a : a.negate();
    }
    
    /**
     * Approximate an area hyperbolic sine by the taylor series 
     * <pre>         sum_{k=0}^m (-1)^k     (2k)! a^(2k+1) / 2^(2k) / k!² / (2k+1) for |a| < 1</pre>
     * <pre>1+ln(2a)-sum_{k=0}^m (-1)^(k+1) (2k)! / a^(2k) / 2^(2k) / k!² / (2k)   for  a  > 1</pre>
     * 
     * @param a   the arithmetic input
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code asinh(a)}
     */
    private static BigDecimal taylor(BigDecimal a, MathContext mc) {
      return prepare(a, mc, (b,high) -> high ? taylorInf_acosinh  (b, mc, true) 
                                             : taylorZero_asintanh(b, mc, true, true));
    }
    
    /**
     * Approximate an area hyperbolic sine by its relation to the logarithm: {@code asinh(a) = ln(a + sqrt(a²+1))}
     * 
     * @param a   the arithmetic input
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code asinh(a)}
     */
    private static BigDecimal logarithmic(BigDecimal a, MathContext mc) {
      // additional accuracy for intermediate results
      final MathContext mc2 = new MathContext(mc.getPrecision()+2, mc.getRoundingMode());
      
      return prepare(a, mc2, (b,high) -> LN.approximate(b.add(SQRT.approximate(b.multiply(b, mc2)
                                                         .add(BigDecimal.ONE, mc2), mc2), mc2), mc2));
    }
    
    /**
     * Test accuracy and convergence speed of area hyperbolic sine approximations.
     * @param args ignored
     */
    public static void main(String[] args) {
      System.out.println("Testing ASINH approximations");
      ASINH d = new ASINH(null, null);
      testApproximations(-10000, 10000, -3, Verbosity.STATISTICS, "out/stat/asinh.dat", new Test[] {
        new Test(a -> ".asinh      ("+a+")        ", (a,mc) -> d.apply(a.doubleValue()), null,                   null),
        new Test(a -> " approximate("+a+",DECI128)", (a,mc) -> approximate(a, mc),          0, MathContext.DECIMAL128),
        new Test(a -> ".approximate("+a+",CEILING)", (a,mc) -> approximate(a, mc),          1,          Range.CEILING),
        new Test(a -> ".approximate("+a+",FLOOR)  ", (a,mc) -> approximate(a, mc),          1,          Range.FLOOR  ),
        new Test(a -> " taylor     ("+a+",DECI64) ", (a,mc) -> taylor     (a, mc),          1,  MathContext.DECIMAL64),
        new Test(a -> " logarithmic("+a+",DECI64) ", (a,mc) -> logarithmic(a, mc),          1,  MathContext.DECIMAL64),
      });
    }
  }
  
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Area hyperbolic cosine {@code Implementation}.
   * <pre>acosh : [1,+inf[ -> [0,+inf[</pre>
   * 
   * <h4>Relation to other functions</h4>
   * <ul>
   *   <li>{@code acosh(a) = ln(a + sqrt(a²-1))}
   *   <li>{@code acosh(a) = asinh(sqrt(a²-1))}
   * </ul>
   * 
   * <h4>Iterative Approximation</h4>
   * <table border>
   *   <tr>
   *     <th>Algorithm</th>
   *     <th>Idea</th>
   *     <th>PRO</th>
   *     <th>CON</th>
   *   </tr>
   *   <tr>
   *     <td>{@link #taylor}</td>
   *     <td><pre>acosh(a) = ln(2a) - sum_{k=1}^m (2k)! / a^(2k) / 2^(2k) / k!² / (2k) for a > 1</pre></td>
   *     <td></td>
   *     <td></td>
   *   </tr>
   * </table>
   * <p>
   * See <a href='https://de.wikipedia.org/wiki/Areasinus_Hyperbolicus_und_Areakosinus_Hyperbolicus'>Wikipedia</a>
   * and <a href='http://mathworld.wolfram.com/InverseHyperbolicCosine.html'>Mathematica</a>.
   */
  public static class ACOSH extends Trigonometric {
    
    /** Unique ID for (de)marshalling */
    private static final long serialVersionUID = -207236348460323249L;
    
    /**
     * Generate a arc hyperbolic cosine {@code Implementation}.
     * @param a input  {@link Format}
     * @param r result {@link Format}
     */
    public ACOSH(Format a, Format r) {
      super(a,r);
    }
    
    @Override
    public Number apply(double a) {
      return Math.log(a + Math.sqrt(a*a-1));
    }
    
    /**
     * Approximate an area hyperbolic cosine.
     * Based on {@link LN#approximate} and {@link SQRT#approximate}.
     * @param a   the arithmetic input ({@code a >= 1})
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code acosh(a)}
     */
    public static BigDecimal approximate(BigDecimal a, MathContext mc) {
      switch (mc.getRoundingMode()) {
        case FLOOR   : // use more precision in iterative approximation to leave rounding control to last step
        case CEILING : a = approximate(a, MathContext.DECIMAL128);
                       return a == null ? null : a.round(mc);
        default      : return taylor(a, mc);
      }
    }
    
    /**
     * Handles roots and boundaries.
     * 
     * @param a      the arithmetic input
     * @param mc     the decimal precision and {@link RoundingMode} to apply. 
     * @param approx the approximation to be applied to the transformed input
     * @return       acosh(a)
     */
    private static BigDecimal prepare(BigDecimal a, MathContext mc, Function<BigDecimal,BigDecimal> approx) {
      switch (a.compareTo(BigDecimal.ONE)) {
        case  0 : return BigDecimal.ZERO;
        case -1 : return null;
        default : return approx.apply(a); 
      }
    }
    
    /**
     * Approximate an area hyperbolic sine by the taylor series 
     * {@code ln(2a) - sum_{k=1}^m (2k)! / a^(2k) / 2^(2k) / k!² / (2k)}.
     * 
     * @param a   the arithmetic input
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code acosh(a)}
     */
    private static BigDecimal taylor(BigDecimal a, MathContext mc) {
      return prepare(a,mc, b -> taylorInf_acosinh(b, mc, false));
    }
    
    /**
     * Approximate an area hyperbolic sine as {@code asinh(sqrt(a²-1))}
     * 
     * @param a   the arithmetic input
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code acosh(a)}
     */
    private static BigDecimal geometric(BigDecimal a, MathContext mc) {
      return prepare(a,mc, b -> ASINH.approximate(
                                SQRT.approximate(b.multiply(b, mc).subtract(BigDecimal.ONE,mc),mc),mc));
    }
    
    /**
     * Approximate an area hyperbolic cosine as {@code ln(a + sqrt(a²-1))} 
     * 
     * @param a   the arithmetic input
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code acosh(a)}
     */
    private static BigDecimal logarithmic(BigDecimal a, MathContext mc) {
      // additional accuracy for intermediate results
      final MathContext mh = new MathContext(mc.getPrecision()+2, mc.getRoundingMode());
      
      return prepare(a,mh, b ->   LN.approximate(b.add(
                                SQRT.approximate(b.multiply(b, mh).subtract(BigDecimal.ONE, mh), mh), mh), mh));
    }
    
    /**
     * Test accuracy and convergence speed of area hyperbolic cosine approximations.
     * @param args ignored
     */
    public static void main(String[] args) {
      System.out.println("Testing ACOSH approximations");
      ACOSH d = new ACOSH(null, null);
      testApproximations(500, 10000, -3, Verbosity.STATISTICS, "out/stat/acosh.dat", new Test[] {
        new Test(a -> ".acosh      ("+a+")        ", (a,mc) -> d.apply(a.doubleValue()), null,                   null),
        new Test(a -> " approximate("+a+",DECI128)", (a,mc) -> approximate(a, mc),          0, MathContext.DECIMAL128),
        new Test(a -> ".approximate("+a+",CEILING)", (a,mc) -> approximate(a, mc),          1,          Range.CEILING),
        new Test(a -> ".approximate("+a+",FLOOR)  ", (a,mc) -> approximate(a, mc),          1,          Range.FLOOR  ),
        new Test(a -> " taylor     ("+a+",DECI64) ", (a,mc) -> taylor     (a, mc),          1,  MathContext.DECIMAL64),
        new Test(a -> " geometric  ("+a+",DECI64) ", (a,mc) -> geometric  (a, mc),          1,  MathContext.DECIMAL64),
        new Test(a -> " logarithmic("+a+",DECI64) ", (a,mc) -> logarithmic(a, mc),          1,  MathContext.DECIMAL64),
      });
    }
  }
  
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Area hyperbolic tangent {@code Implementation}.
   * <pre>atanh : [-1,1] -> R</pre>
   * 
   * <h4>Symmetry</h4>
   * <ul>
   *   <li>{@code atanh(a)  = -atanh(-a)}
   * </ul>
   * 
   * <h4>Relation to other functions</h4>
   * <ul>
   *   <li>{@code atanh(a) = ln((1+a)/(1-a)) / 2 }
   * </ul>
   * 
   * <h4>Iterative Approximation</h4>
   * <table border>
   *   <tr>
   *     <th>Algorithm</th>
   *     <th>Idea</th>
   *     <th>PRO</th>
   *     <th>CON</th>
   *   </tr>
   *   <tr>
   *     <td>{@link #taylor}</td>
   *     <td><pre>atanh(a) = sum_{k=0}^m a^(2k+1) / (2k+1)</pre></td>
   *     <td>covers whole input domain</td>
   *     <td></td>
   *   </tr>
   * </table>
   * <p>
   * See <a href='https://de.wikipedia.org/wiki/Areatangens_Hyperbolicus_und_Areakotangens_Hyperbolicus'>Wikipedia</a>
   * and <a href='http://mathworld.wolfram.com/InverseHyperbolicTangent.html'>Mathematica</a>.
   */
  public static class ATANH extends Trigonometric {
    
    /** Unique ID for (de)marshalling */
    private static final long serialVersionUID = 4187931776473401909L;
  
    /**
     * Generate a arc hyperbolic tangent {@code Implementation}.
     * @param a input  {@link Format}
     * @param r result {@link Format}
     */
    public ATANH(Format a, Format r) {
      super(a,r);
    }
    
    @Override
    public Number apply(double a) {
      return Math.log((a+1)/(1-a)) / 2;
    }
    
    /**
     * Approximate a area hyperbolic tangent.
     * @param a   the arithmetic input ({@code -1 < a < 1})
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code tanh(a)}
     */
    public static BigDecimal approximate(BigDecimal a, MathContext mc) {
      switch (mc.getRoundingMode()) {
        case FLOOR   :  // use more precision in iterative approximation to leave rounding control to last step
        case CEILING : a = approximate(a, MathContext.DECIMAL128);
                       return a == null ? null : a.round(mc);
        default      : return logarithmic(a, mc); //!! do not use cordic here, as CORDIC requires ATANH to init its LUT
      }
    }
    
    /**
     * Handle roots and boundaries.
     * @param a      the arithmetic input
     * @param mc     the decimal precision and {@link RoundingMode} to apply. 
     * @param approx the approximation to be applied to the transformed input
     * @return       atanh(a)
     */
    private static BigDecimal prepare(BigDecimal a, MathContext mc, Function<BigDecimal,BigDecimal> approx) {
      if (a.abs().compareTo(BigDecimal.ONE)  >= 0) return null;
      if (a.      compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
      return approx.apply(a);
    }
    
    /**
     * Approximate a area hyperbolic tangent by the taylor series {@code sum_{k=0}^m a^(2k+1) / (2k+1)}
     * @param a   the arithmetic input  
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code atanh(a)}
     */
    private static BigDecimal taylor(BigDecimal a, MathContext mc) {
      return prepare(a, mc, b -> taylorZero_asintanh(b, mc, false, false));
    }
    
    /**
     * Approximate a area hyperbolic tangent by its relation to the logarithm.
     * @param a   the arithmetic input  
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code atanh(a)}
     */
    private static BigDecimal logarithmic(BigDecimal a, MathContext mc) {
      // additional accuracy for intermediate results
      final MathContext mh = new MathContext(mc.getPrecision()+2, mc.getRoundingMode());
      
      return prepare(a, mh, b -> LN.approximate(BigDecimal.ONE.add     (b, mh).divide(
                                                BigDecimal.ONE.subtract(b, mh), mh), mh).divide(ZWO, mh));
    }
    
    /**
     * Approximate a area hyperbolic tangent by the {@link CORDIC} algorithm.
     * 
     * @param a   the arithmetic input
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code atanh(a)}
     */
    private static BigDecimal cordic(BigDecimal a, MathContext mc) {
      return prepare(a, mc, b -> CORDIC.atanh(b, mc));
    }
    
    /**
     * Test accuracy and convergence speed of area hyperbolic tangent approximations.
     * @param args ignored
     */
    public static void main(String[] args) {
      System.out.println("Testing ATANH approximations");
      ATANH d = new ATANH(null, null);
      testApproximations(-1100, 1100, -3, Verbosity.STATISTICS, "out/stat/atanh.dat", new Test[] {
        new Test(a -> ".atanh      ("+a+")        ", (a,mc) -> d.apply(a.doubleValue()), null,                   null),
        new Test(a -> " approximate("+a+",DECI128)", (a,mc) -> approximate(a, mc),          0, MathContext.DECIMAL128),
        new Test(a -> ".approximate("+a+",CEILING)", (a,mc) -> approximate(a, mc),          1,          Range.CEILING),
        new Test(a -> ".approximate("+a+",FLOOR)  ", (a,mc) -> approximate(a, mc),          1,          Range.FLOOR  ),
        new Test(a -> " taylor     ("+a+",DECI64) ", (a,mc) -> taylor     (a, mc),          1,  MathContext.DECIMAL64),
        new Test(a -> " logarithmic("+a+",DECI64) ", (a,mc) -> logarithmic(a, mc),          1,  MathContext.DECIMAL64),
        new Test(a -> " cordic     ("+a+",DECI64) ", (a,mc) -> cordic     (a, mc),          1,  MathContext.DECIMAL64),
      });
    }
  }
  
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Area hyperbolic cotangent {@code Implementation}.
   * <pre>atanh : R \ [-1,1] -> R \ {0}</pre>
   * 
   * <h4>Symmetry</h4>
   * <ul>
   *   <li>{@code acoth(a)  = -acoth(-a)}
   * </ul>
   * 
   * <h4>Relation to other functions</h4>
   * <ul>
   *   <li>{@code acoth(a) = ln((a+1)/(a-1)) / 2}
   * </ul>
   * 
   * <h4>Iterative Approximation</h4>
   * <table border>
   *   <tr>
   *     <th>Algorithm</th>
   *     <th>Idea</th>
   *     <th>PRO</th>
   *     <th>CON</th>
   *   </tr>
   *   <tr>
   *     <td>{@link #taylor}</td>
   *     <td><pre>acoth(a) = sum_{k=0}^m (1/a)^(2k+1) / (2k+1) </pre></td>
   *     <td>covers whole input domain, can be implemented as {@link ATANH#taylor}(1/a)</td>
   *     <td></td>
   *   </tr>
   * </table>
   * <p>
   * See <a href='https://de.wikipedia.org/wiki/Areatangens_Hyperbolicus_und_Areakotangens_Hyperbolicus'>Wikipedia</a>
   * and <a href='http://mathworld.wolfram.com/InverseHyperbolicTangent.html'>Mathematica</a>.
   */
  public static class ACOTH extends Trigonometric {
    
    /** Unique ID for (de)marshalling */
    private static final long serialVersionUID = -946254251633269430L;

    /**
     * Generate a arc hyperbolic cotangent {@code Implementation}.
     * @param a input  {@link Format}
     * @param r result {@link Format}
     */
    public ACOTH(Format a, Format r) {
      super(a,r);
    }
    
    @Override
    public Number apply(double a) {
      return Math.log((a+1)/(a-1)) / 2;
    }
    
    /**
     * Approximate a area hyperbolic cotangent.
     * @param a   the arithmetic input ({@code a != 0})
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code acoth(a)}
     */
    public static BigDecimal approximate(BigDecimal a, MathContext mc) {
      switch (mc.getRoundingMode()) {
        case FLOOR   : // use more precision in iterative approximation to leave rounding control to last step  
        case CEILING : a = approximate(a, MathContext.DECIMAL128);
                       return a == null ? null : a.round(mc);
        default      : return taylor(a, mc);
      }
    }
    
    /**
     * Handle bounderies.
     * @param a      the arithmetic input
     * @param mc     the decimal precision and {@link RoundingMode} to apply. 
     * @param approx the approximation to be applied to the transformed input
     * @return       acoth(a)
     */
    private static BigDecimal prepare(BigDecimal a, MathContext mc, Function<BigDecimal,BigDecimal> approx) {
      if (a.abs().compareTo(BigDecimal.ONE)  <= 0) return null;
      return approx.apply(a);
    }
    
    /**
     * Approximate a area hyperbolic cotangent by the taylor series {@code sum_{k=0}^m (1/a)^(2k+1) / (2k+1)}.
     * 
     * @param a   the arithmetic input  
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code acoth(a)}
     */
    private static BigDecimal taylor(BigDecimal a, MathContext mc) {
      // |a| > 1, so |1/a| < 1 can be passed to taylor series of ATANH
      return prepare(a, mc, b -> taylorZero_asintanh(BigDecimal.ONE.divide(b, mc), mc, false, false));
    }
    
    /**
     * Approximate a area hyperbolic cotangent as {@code ln((a+1)/(a-1)) / 2}.
     * 
     * @param a   the arithmetic input  
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code acoth(a)}
     */
    private static BigDecimal logarithmic(BigDecimal a, MathContext mc) {
      // additional accuracy for intermediate results
      final MathContext mh = new MathContext(mc.getPrecision()+2, mc.getRoundingMode());
      
      return prepare(a, mh, b -> LN.approximate(b.add     (BigDecimal.ONE, mh).divide(
                                                b.subtract(BigDecimal.ONE, mh), mh), mh).divide(ZWO, mh));
    }
    
    /**
     * Approximate a area hyperbolic cotangent by the {@link CORDIC} algorithm.
     * 
     * @param a   the arithmetic input
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code acoth(a)}
     */
    private static BigDecimal cordic(BigDecimal a, MathContext mc) {
      return prepare(a, mc, b -> CORDIC.acoth(b, mc));
    }
    
    /**
     * Test accuracy and convergence speed of area hyperbolic cotangent approximations.
     * @param args ignored
     */
    public static void main(String[] args) {
      System.out.println("Testing ACOTH approximations");
      ACOTH d = new ACOTH(null, null);
      testApproximations(-10000, 10000, -3, Verbosity.STATISTICS, "out/stat/acoth.dat", new Test[] {
        new Test(a -> ".acoth      ("+a+")        ", (a,mc) -> d.apply(a.doubleValue()), null,                   null),
        new Test(a -> " approximate("+a+",DECI128)", (a,mc) -> approximate(a, mc),          0, MathContext.DECIMAL128),
        new Test(a -> ".approximate("+a+",CEILING)", (a,mc) -> approximate(a, mc),          1,          Range.CEILING),
        new Test(a -> ".approximate("+a+",FLOOR)  ", (a,mc) -> approximate(a, mc),          1,          Range.FLOOR  ),
        new Test(a -> " taylor     ("+a+",DECI64) ", (a,mc) -> taylor     (a, mc),          1,  MathContext.DECIMAL64),
        new Test(a -> " logarithmic("+a+",DECI64) ", (a,mc) -> logarithmic(a, mc),          1,  MathContext.DECIMAL64),
        new Test(a -> " cordic     ("+a+",DECI64) ", (a,mc) -> cordic     (a, mc),          1,  MathContext.DECIMAL64),
      });
    }
  }
  
/*
 * Debugging
 *********************************************************************************************************************/
  
  public static void main(String[] args) {
    
    // some trigonometric functions are not directly accessible by java.Math 
    // => reuse already implemented substitutions for comparison with double precision.

//    for (int i=0; i<60; i++) {
//      System.out.println(i + " : " + bernoulli(i).toPlainString());
//    }
    
//    System.out.println(CORDIC.acoth(new BigDecimal("1.5"), MathContext.DECIMAL64));
    
//    testApproximations(84, 84, -2, Verbosity.GASSY, null, new Test[] {
//      new Test(a->"double("+a+")        ", (a,mc)->Math.sin(a.doubleValue()), null,                   null),
//      new Test(a->"BigDec("+a+", DECI64)", (a,mc)->SIN.cordic(a, mc),              0, MathContext.DECIMAL64),
//    });
    
    // regenerate all iteration statistics
//    SIN  .main(null);
//    COS  .main(null);
//    TAN  .main(null);
//    COT  .main(null);
//    SINH .main(null);
//    COSH .main(null);
    TANH .main(null);
//    COTH .main(null);
//    ASIN .main(null);
//    ACOS .main(null);
//    ATAN .main(null);
//    ACOT .main(null);
//    ASINH.main(null);
//    ACOSH.main(null);
//    ATANH.main(null);
//    ACOTH.main(null);
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