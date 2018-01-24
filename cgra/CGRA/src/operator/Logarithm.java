package operator;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;

import accuracy.Format;
import accuracy.Range;

/**
 * Generic logarithm: R = log_B(A)
 * 
 * <h4>Selection of Algorithm</h4>
 * The following iterative algorithms approximate a base {@code B} logarithm  of the value {@code A} with {@code N} bit
 * precision. The generic logarithm on the ]0,+inf[ domain can be reduced to the natural logaritm on the ]0,1[ domain 
 * at the cost of two divisions, as {@code log_B(A) = ln(A)/ln(B)} and {@code ln(A) = -ln(1/A)}. The taylor series
 * expansions however require lots of iterations for small numbers near zero. {@code ln(a*b^k) = k ln(b)+ln(a)} should
 * thus be applied to transform the input domain to to a small interval around one.

 * <table border>
 *   <tr>
 *     <th>Algorithm</th>
 *     <th>Idea</th>
 *     <th>PRO</th>
 *     <th>CON</th>
 *   </tr>
 *     <td>{@link #newton} Raphson</td>
 *       <td>Find root of <code>f(x)=b^x - A => log_B(A)</code>
 *<pre>
 *  L = -1/ln(B) (-1 for B=e, -1.4427 for B=2, -0.4343 for B=10)
 *  e = A;
 *  do 
 *    o = e;
 *    e = o - (1-A/B^o)/ln(B) = o + L*(1-A exp(o/L));
 *  while (e != o)
 *  log_B(A) = e;
 *</pre></td>
 *     <td>fast (quadratic) convergence (8 iterations for A ~= 1), for all bases</td>
 *     <td>requires exp and precise initial ln(B)</td>
 *   </tr>
 *   <tr>
 *     <td>{@link #taylor1} series</td>
 *     <td><pre>ln(A) = sum_{k=1}^m 1/k (-1)^(k+1) (A-1)^k   for 0 < A <= 2</pre></td>
 *     <td>no exp required</td>
 *     <td>requires division and complex pre/post scaling, {@code m} upto 48 required</td>
 *   </tr>
 *   <tr>
 *     <td>{@link #taylor2} series</td>
 *     <td><pre>ln(A) = sum_{k=1}^m 1/k ((A-1)/A)^k          for A > 0.5</pre></td>
 *     <td>no exp required</td>
 *     <td>requires division and complex pre/post scaling, {@code m} upto 45 required</td>
 *   </tr>
 *   <tr>
 *     <td>{@link #taylor3} series</td>
 *     <td><pre>ln(A) = 2 sum_{k=0}^m 1/(2k+1) ((A-1)/(A+1))^(2k+1) for A > 0</pre></td>
 *     <td>no exp required</td>
 *     <td>requires division and complex pre/post scaling, {@code m} upto 13 required</td>
 *   </tr>
 *   <tr>
 *     <td>Arithmetic-Geometric-Mean</td>
 *     <td>{@code agm(1,A)} converges against {@code (A-1)/ln(A)} 
 *<pre>
 *  x,y = 1,A;
 *  x,y = (x+y)/2, sqrt(x*y) while (x!=y);
 *  ln(A) = (A-1)/x;
 *</pre></td>
 *     <td></td>
 *     <td>requires sqrt, <b>!!functional correctness not yet validated!!</b></td>
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
 * See
 * <ul>
 *   <li> http://math.stackexchange.com/a/1669840
 * </ul>
 * @author Andreas Engel [engel@esa.cs.tu-darmstadt.de]
 */
public class Logarithm extends Unary {

  /** Unique ID for (de)marshalling */
  private static final long serialVersionUID = 1878677660390642425L;
  protected double base;

  /**
   * Generate {@code Implementation} of a logarithm {@code Operator}.
   * @param a    input precision
   * @param r    output precision
   */
  protected Logarithm(Format a, Format r, double base) {
    super(a, r, "log_" + base);
    this.base = base;
  }
  
  @Override
  public Number apply(long a) {
    return apply((double) a);
  }
  
  @Override
  public Number apply(double a) {
    return Math.log(a) / Math.log(base);
  }
  
  @Override
  public Number apply(BigInteger a) {
    return apply(new BigDecimal(a));
  }
  
  @Override
  public Number apply(BigDecimal a) {
    return approximate(a, base, MathContext.DECIMAL128);
  }
  
  /**
   * Approximate a generic logarithm.
   * Based on {@link LN#approximate}.
   * @param a    the arithmetic input
   * @param base the base of the logarithm
   * @param mc   the decimal precision and {@link RoundingMode} to apply. 
   * @return     {@code log_base(a)}
   */
  public static BigDecimal approximate(BigDecimal a, double base, MathContext mc) {
    switch (mc.getRoundingMode()) {
      case FLOOR   : // use more precision in iterative approximation to leave rounding control to last step 
      case CEILING : return approximate(a, base, MathContext.DECIMAL128).round(mc);
      default      : 
        a = LN.approximate(a, mc);
        if (base != Math.E && a.compareTo(BigDecimal.ZERO) != 0) a = a.divide(BigDecimal.valueOf(Math.log(base)), mc);
        return a;
    }
  }
  
  
/*
 * Iterative approximation
 *********************************************************************************************************************/

  /**
   * Estimate a generic logarithm by the newton raphson algorithm.
   * <p>
   * Find {@code x} such that {@code f(x) = base^x - a = 0}, i.e., {@code x = log_base(a)}.
   *<pre>
   *  L = -1/ln(base);
   *  e = a;
   *  do 
   *    o = e;
   *    e = o + L*(1 - A exp(o/L));
   *  while (e != o)
   *  log_B(A) = e;
   *</pre>
   * @param a         the value to find the logarithm for
   * @param base      the base of the logarithm
   * @param precision number of correct decimal positions in the result
   * @return          {@code log_base(a)} 
   */
  private static BigDecimal newton(BigDecimal a, double base, MathContext mc) {
    if (a.compareTo(BigDecimal.ZERO) <= 0) return null;
    if (a.compareTo(BigDecimal.ONE)  == 0) return BigDecimal.ZERO;
    
    // additional accuracy for intermediate results
    int precision = mc.getPrecision()+2;
    mc = new MathContext(precision+2, mc.getRoundingMode());
    
    BigDecimal L   = BigDecimal.ONE.divide(LN.taylor3(BigDecimal.valueOf(base), mc), mc).negate();
    BigDecimal thr = BigDecimal.ONE.scaleByPowerOfTen(-precision);
    BigDecimal est = a;  
    BigDecimal old;
    
    do {
      old = est;
      est = EXP.approximate(old.divide(L, mc), mc);
      est = old.add(L.multiply(BigDecimal.ONE.subtract(a.multiply(est, mc), mc), mc), mc);
    } while (!approximationConverged(est, est.subtract(old, mc), thr, mc));
    
    return est;
  }
  
  /**
   * Estimate a generic logarithm by the {@link LN#taylor3} series under certain rounding control.
   * @param a    the value to find the logarithm for
   * @param base the base of the logarithm
   * @param mc   the precision and rounding mode to be applied
   * @return     {@code log_base(a)} 
   */
  public static BigDecimal taylor(BigDecimal a, double base, MathContext mc) {
    // use more precision to leave rounding control to last step
    a = LN.taylor3(a, new MathContext(mc.getPrecision()+10, mc.getRoundingMode())); 
    return (base == Math.E) ? a.round(mc) : a.divide(BigDecimal.valueOf(Math.log(base)), mc); 
  }

/*
 * Range propagation
 *********************************************************************************************************************/

  
  @Override
  protected Range getResultRange(Range.IA a) {
    // see Stolfi1997 page 29
    if (a.isNonPositive()) return Range.EMPTY;
    
    return Range.generate(
      a.lo()==null || a.lo().compareTo(BigDecimal.ZERO)<=0 ? null : approximate(a.lo(), base, Range.FLOOR),
      a.hi()==null                                         ? null : approximate(a.hi(), base, Range.CEILING));
  }
  
/*
 * Implementations for dedicated bases
 *********************************************************************************************************************/

  /**
   * Natural logarithm: R = log_e(A)
   */
  public static class LN extends Logarithm {
    /**
   * 
   */
  private static final long serialVersionUID = 3827708982336735967L;

  public LN(Format a, Format b) {
      super(a, b, Math.E);
    }
    
    /**
     * Approximate the natural logarithm.
     * Based on the {@link LN#taylor3} series expansion.
     * @param a    the arithmetic input
     * @param mc   the decimal precision and {@link RoundingMode} to apply. 
     * @return     {@code log_base(a)}
     */
    public static BigDecimal approximate(BigDecimal a, MathContext mc) {
      switch (mc.getRoundingMode()) {
        case FLOOR   : // use more precision in iterative approximation to leave rounding control to last step 
        case CEILING : return approximate(a, MathContext.DECIMAL128).round(mc);
        default      : return taylor3(a, mc);
      }
    }
    
    /**
     * Constant ln(2) = 0.69314718055994539600144315036941540580 with 34 accurate decimal digits.
     */
    public static final BigDecimal TWO = approximate(BigDecimal.valueOf(2), MathContext.DECIMAL128);
    
    /**
     * Estimate the natural logarithm by the taylor series {@code ln(a) = sum_{k=1}^m 1/k (-1)^(k+1) (a-1)^k}.
     * <p>
     * Transforms argument to ]0,1[ interval by {@code ln(a) = -ln(1/a)} and then expands the taylor series for 
     * {@code m} appropriate to reach the required precision. The input transformation is finally compensated by
     * optional inverting.
     * <p>
     * The convergence of this series is very slow for arguments near zero. For 16 digit precision 
     * (i.e. IEEE 754 double), the following number of iterations are required:
     * <table border=1>
     *   <tr><th>a</th><td>0.1</td><td>0.2</td><td>0.3</td><td>0.4</td><td>0.5</td><td>0.6</td><td>0.7</td>
     *                 <td>0.8</td><td>0.9</td><td>1.0</td><td>1.1</td><td>1.2</td><td>1.3</td><td>1.4</td>
     *                 <td>1.5</td><td>1.6</td><td>1.7</td><td>1.8</td><td>1.9</td><td>2.0</td><td>100</td></tr>
     *   <tr><th>m</th><td>296</td><td>143</td><td> 91</td><td> 64</td><td> 48</td><td> 37</td><td> 28</td>
     *                 <td> 21</td><td> 15</td><td>  0</td><td> 15</td><td> 19</td><td> 23</td><td> 27</td>
     *                 <td> 31</td><td> 34</td><td> 38</td><td> 41</td><td> 45</td><td> 48</td><td>2874</td></tr>
     * </table>
     * <p>
     * @param a         the value to find the logarithm for
     * @param precision number of correct decimal positions in the result
     * @return          {@code ln(a)} 
     */
    private static BigDecimal taylor1_inv(BigDecimal a, MathContext mc) {
      if (a.compareTo(BigDecimal.ZERO) <= 0) return null;
      int c = a.compareTo(BigDecimal.ONE);
      if (c == 0) return BigDecimal.ZERO;


      // additional accuracy for intermediate results
      int precision = mc.getPrecision()+2;
      mc = new MathContext(precision+2, mc.getRoundingMode());
      
      // scale argument to ]0,1[
      if (c > 0) a = BigDecimal.ONE.divide(a, mc);
      
      BigDecimal thr = BigDecimal.ONE.scaleByPowerOfTen(-precision);
      BigDecimal fak = a.subtract(BigDecimal.ONE, mc);
      BigDecimal num = fak;
      BigDecimal q   = fak;
      BigDecimal est = BigDecimal.ZERO; 
      int k = 1;
      
      do {
        Test.iterations++;
        est = est.add(q, mc);
        num = num.multiply(fak, mc).negate();
        q   = num.divide(BigDecimal.valueOf(++k), mc);
      } while (q.abs().compareTo(thr) > 0);
      
      // revert initial scaling
      return (c > 0) ? est.negate() : est;
    }
    
    /**
     * Estimate the natural logarithm by the taylor series {@code ln(a) = sum_{k=1}^m 1/k (-1)^(k+1) (a-1)^k}.
     * <p>
     * Transforms argument to ]0.5,1.5[ domain by {@code ln(a*E^pre) = pre + ln(a)} for an appropriate {@code pre}.
     * The taylor series is expanded for {@code m} appropriate to reach the required precision. The input 
     * transformation is finally compensated by adding {@code pre}.
     * <p>
     * The convergence of this series is better compared to {@link #taylor1_inv} at the cost of a more complex 
     * prescaling. For 16 digit precision (i.e. IEEE 754 double), the following number of iterations are required:
     * <table border=1>
     *   <tr><th> a </th><td>0.1</td><td>0.2</td><td>0.3</td><td>0.4</td><td>0.5</td><td>0.6</td><td>0.7</td>
     *                   <td>0.8</td><td>0.9</td><td>1.0</td><td>1.1</td><td>1.2</td><td>1.3</td><td>1.4</td>
     *                   <td>1.5</td><td>1.6</td><td>1.7</td><td>1.8</td><td>1.9</td><td>2.0</td><td>100</td></tr>
     *   <tr><th>pre</th><td> -2</td><td> -1</td><td> -1</td><td> -1</td><td> -1</td><td>  0</td><td>  0</td>
     *                   <td>  0</td><td>  0</td><td>  0</td><td>  0</td><td>  0</td><td>  0</td><td>  0</td>
     *                   <td>  1</td><td>  1</td><td>  1</td><td>  1</td><td>  1</td><td>  1</td><td>  5</td></tr>
     *   <tr><th> m </th><td> 26</td><td> 43</td><td> 21</td><td> 15</td><td> 33</td><td> 37</td><td> 28</td>
     *                   <td> 21</td><td> 15</td><td>  0</td><td> 15</td><td> 21</td><td> 28</td><td> 37</td>
     *                   <td> 42</td><td> 38</td><td> 34</td><td> 31</td><td> 28</td><td> 26</td><td> 30</td></tr>
     * </table>
     * <p>
     * @param a         the value to find the logarithm for
     * @param precision number of correct decimal positions in the result
     * @return          {@code ln(a)} 
     */
    private static BigDecimal taylor1(BigDecimal a, MathContext mc) {
      if (a.compareTo(BigDecimal.ZERO) <= 0) return null;
      if (a.compareTo(BigDecimal.ONE)  == 0) return BigDecimal.ZERO;
      
      // additional accuracy for intermediate results
      int precision = mc.getPrecision()+2;
      mc = new MathContext(precision+2, mc.getRoundingMode());
      
      // prescale to ]0.5,1.5[ domain
      BigDecimal e = BigDecimal.valueOf(Math.E);
      BigDecimal l = BigDecimal.valueOf(0.5);
      BigDecimal h = BigDecimal.valueOf(1.5);
      int pre = 0;
      while (a.compareTo(l) <= 0) {a = a.multiply(e, mc); pre--;}
      while (a.compareTo(h) >= 0) {a = a.divide  (e, mc); pre++;}
      
      BigDecimal thr = BigDecimal.ONE.scaleByPowerOfTen(-precision);
      BigDecimal fak = a.subtract(BigDecimal.ONE, mc);
      BigDecimal num = fak;
      BigDecimal q   = fak;
      BigDecimal est = BigDecimal.ZERO; 
      int k = 1;
      
      do {
        Test.iterations++;
        est = est.add(q, mc);
        num = num.multiply(fak, mc).negate();
        q   = num.divide(BigDecimal.valueOf(++k), mc);
      } while (q.abs().compareTo(thr) > 0);
      
      // compensate initial scaling
      return est.add(BigDecimal.valueOf(pre), mc);
    }
    
    /**
     * Estimate the natural logarithm by the taylor series {@code ln(a) = sum_{k=1}^m 1/k ((a-1)/a)^k}.
     * <p>
     * Transforms argument to ]0.7,1.9[ domain by {@code ln(a*E^pre) = pre + ln(a)} for an appropriate {@code pre}.
     * The taylor series is expanded for {@code m} appropriate to reach the required precision. The input 
     * transformation is finally compensated by adding {@code pre}.
     * <p>
     * The convergence of this series shows more variance than to {@link #taylor1}. For 16 digit precision 
     * (i.e. IEEE 754 double), the following number of iterations are required:
     * <table border=1>
     *   <tr><th> a </th><td>0.1</td><td>0.2</td><td>0.3</td><td>0.4</td><td>0.5</td><td>0.6</td><td>0.7</td>
     *                   <td>0.8</td><td>0.9</td><td>1.0</td><td>1.1</td><td>1.2</td><td>1.3</td><td>1.4</td>
     *                   <td>1.5</td><td>1.6</td><td>1.7</td><td>1.8</td><td>1.9</td><td>2.0</td><td>100</td></tr>
     *   <tr><th>pre</th><td> -2</td><td> -2</td><td> -1</td><td> -1</td><td> -1</td><td> -1</td><td>  0</td>
     *                   <td>  0</td><td>  0</td><td>  0</td><td>  0</td><td>  0</td><td>  0</td><td>  0</td>
     *                   <td>  0</td><td>  0</td><td>  0</td><td>  0</td><td>  1</td><td>  1</td><td>  4</td></tr>
     *   <tr><th> m </th><td> 33</td><td> 30</td><td> 23</td><td> 14</td><td> 26</td><td> 36</td><td> 40</td>
     *                   <td> 25</td><td> 16</td><td>  0</td><td> 15</td><td> 19</td><td> 23</td><td> 27</td>
     *                   <td> 31</td><td> 34</td><td> 38</td><td> 41</td><td> 40</td><td> 33</td><td> 42</td></tr>
     * </table>
     * <p>
     * @param a         the value to find the logarithm for
     * @param precision number of correct decimal positions in the result
     * @return          {@code ln(a)} 
     */
    private static BigDecimal taylor2(BigDecimal a, MathContext mc) {
      if (a.compareTo(BigDecimal.ZERO) <= 0) return null;
      if (a.compareTo(BigDecimal.ONE)  == 0) return BigDecimal.ZERO;

      // additional accuracy for intermediate results
      int precision = mc.getPrecision()+2;
      mc = new MathContext(precision+2, mc.getRoundingMode());
      
      // prescale to ]0.7,1.9[ domain
      BigDecimal e = BigDecimal.valueOf(Math.E);
      BigDecimal l = BigDecimal.valueOf(0.7);
      BigDecimal h = BigDecimal.valueOf(1.9);
      int pre = 0;
      while (a.compareTo(l) <= 0) {a = a.multiply(e, mc); pre--;}
      while (a.compareTo(h) >= 0) {a = a.divide  (e, mc); pre++;}
//      System.out.println("pre=" + pre);
      
      BigDecimal thr = BigDecimal.ONE.scaleByPowerOfTen(-precision);
      BigDecimal fak = a.subtract(BigDecimal.ONE, mc).divide(a, mc); 
      BigDecimal num = fak;
      BigDecimal q   = fak;
      BigDecimal est = BigDecimal.ZERO; 
      int k = 1;
      
      do {
        Test.iterations++;
        est = est.add(q, mc);
        num = num.multiply(fak, mc);
        q   = num.divide(BigDecimal.valueOf(++k), mc);
      } while (q.abs().compareTo(thr) > 0);
      
      // compensate initial scaling
      return est.add(BigDecimal.valueOf(pre), mc);
    }
    
    /**
     * Estimate the natural logarithm by the taylor series {@code ln(A) = 2 sum_{k=0}^m 1/(2k+1) ((A-1)/(A+1))^(2k+1)}.
     * <p>
     * Transforms argument to ]0.6,1.65[ domain by {@code ln(a*E^pre) = pre + ln(a)} for an appropriate {@code pre}.
     * The taylor series is expanded for {@code m} appropriate to reach the required precision. The input 
     * transformation is finally compensated by adding {@code pre}.
     * <p>
     * The convergence of this series is the best of all investigated taylor series. For 16 digit precision 
     * (i.e. IEEE 754 double), the following number of iterations are required:
     * <table border=1>
     *   <tr><th> a </th><td>0.1</td><td>0.2</td><td>0.3</td><td>0.4</td><td>0.5</td><td>0.6</td><td>0.7</td>
     *                   <td>0.8</td><td>0.9</td><td>1.0</td><td>1.1</td><td>1.2</td><td>1.3</td><td>1.4</td>
     *                   <td>1.5</td><td>1.6</td><td>1.7</td><td>1.8</td><td>1.9</td><td>2.0</td><td>100</td></tr>
     *   <tr><th>pre</th><td> -2</td><td> -2</td><td> -1</td><td> -1</td><td> -1</td><td> -1</td><td>  0</td>
     *                   <td>  0</td><td>  0</td><td>  0</td><td>  0</td><td>  0</td><td>  0</td><td>  0</td>
     *                   <td>  0</td><td>  0</td><td>  1</td><td>  1</td><td>  1</td><td>  1</td><td>  5</td></tr>
     *   <tr><th> m </th><td> 10</td><td> 11</td><td>  8</td><td>  6</td><td> 10</td><td> 13</td><td> 11</td>
     *                   <td>  9</td><td>  7</td><td>  0</td><td>  7</td><td>  8</td><td>  9</td><td> 10</td>
     *                   <td> 11</td><td> 12</td><td> 12</td><td> 12</td><td> 11</td><td> 10</td><td> 11</td></tr>
     * </table>
     * <p>
     * @param a         the value to find the logarithm for
     * @param precision number of correct decimal positions in the result
     * @return          {@code ln(a)} 
     */
    private static BigDecimal taylor3(BigDecimal a, MathContext mc) {
      if (a.compareTo(BigDecimal.ZERO) <= 0) return null;
      if (a.compareTo(BigDecimal.ONE)  == 0) return BigDecimal.ZERO;

      // additional accuracy for intermediate results
      int precision = mc.getPrecision()+2;
      mc = new MathContext(precision+2, mc.getRoundingMode());
      
      // prescale to ]0.6,1.65[ domain
      BigDecimal e = BigDecimal.valueOf(Math.E);
      BigDecimal l = BigDecimal.valueOf(0.6);
      BigDecimal h = BigDecimal.valueOf(1.65);
      int pre = 0;
      while (a.compareTo(l) <= 0) {a = a.multiply(e, mc); pre--;}
      while (a.compareTo(h) >= 0) {a = a.divide  (e, mc); pre++;}
      
      BigDecimal thr = BigDecimal.ONE.scaleByPowerOfTen(-precision);
      BigDecimal fak = a.subtract(BigDecimal.ONE, mc).divide(a.add(BigDecimal.ONE, mc), mc);
      BigDecimal num = fak;
      BigDecimal q   = fak;
      BigDecimal est = BigDecimal.ZERO;
      fak = fak.multiply(fak, mc);
      int k = 1;
      
      do {
        Test.iterations++;
        est = est.add(q, mc);
        num = num.multiply(fak, mc);
        q   = num.divide(BigDecimal.valueOf(2*k++ + 1), mc);
      } while (q.abs().compareTo(thr) > 0);
      
      // compensate initial scaling
      return est.add(est, mc).add(BigDecimal.valueOf(pre), mc);
    }
  }
  
  /**
   * Dual logarithm: R = log_e(A)
   */
  public static class LOG extends Logarithm {
    /**
   * 
   */
  private static final long serialVersionUID = 3787211896651928620L;

  public LOG(Format a, Format b) {
      super(a, b, 2);
    }
  }
  
  /**
   * Decadic logarithm: R = log_10(A)
   */
  public static class LOG10 extends Logarithm {
    /**
   * 
   */
  private static final long serialVersionUID = 7468409134042630768L;

  public LOG10(Format a, Format b) {
      super(a, b, 10);
    }
  }
  
/*
 * Debugging
 *********************************************************************************************************************/
  
  /**
   * Test iterative approximation.
   * @param args ignored
   */
  public static void main(String[] args) {
    double e = Math.E;
    testApproximations(1, 200000, -4, Verbosity.STATISTICS, null, new Test[] {
                                        // description,                  calculation, compare,      math context
      new Test(a->"Math.log      ("+a+")/Math.log(2) ", (a,mc)->Math.log(a.doubleValue())/Math.log(2),null, null), // 0
      new Test(a->"approximate   ("+a+", 2, DECI128) ", (a,mc)->approximate(a, 2,mc),  0, MathContext.DECIMAL128), // 1
      new Test(a->"approximate   ("+a+", 2, CEILING) ", (a,mc)->approximate(a, 2,mc),  1,          Range.CEILING),
      new Test(a->"approximate   ("+a+", 2, FLOOR)   ", (a,mc)->approximate(a, 2,mc),  1,          Range.FLOOR  ),
      new Test(a->"newton        ("+a+", 2, DECI64)  ", (a,mc)->newton     (a, 2,mc),  1,  MathContext.DECIMAL64),
      
      new Test(a->"Math.log      ("+a+")/Math.log(10)", (a,mc)->Math.log(a.doubleValue())/Math.log(10),null,null), // 5
      new Test(a->"approximate   ("+a+",10, DECI128) ", (a,mc)->approximate(a,10,mc),  5, MathContext.DECIMAL128), // 6
      new Test(a->"approximate   ("+a+",10, CEILING) ", (a,mc)->approximate(a,10,mc),  6,          Range.CEILING),
      new Test(a->"approximate   ("+a+",10, FLOOR)   ", (a,mc)->approximate(a,10,mc),  6,          Range.FLOOR  ),
      new Test(a->"newton        ("+a+",10, DECI64)  ", (a,mc)->newton     (a,10,mc),  6,  MathContext.DECIMAL64),
      
      new Test(a->"Math.log      ("+a+")             ", (a,mc)->Math.log(a.doubleValue()), null,            null), //10
      new Test(a->"approximate   ("+a+", e, DECI128) ", (a,mc)->approximate(a, e,mc), 10, MathContext.DECIMAL128), //11
      new Test(a->"approximate   ("+a+", e, CEILING) ", (a,mc)->approximate(a, e,mc), 11,          Range.CEILING),
      new Test(a->"approximate   ("+a+", e, FLOOR)   ", (a,mc)->approximate(a, e,mc), 11,          Range.FLOOR  ),
      new Test(a->"newton        ("+a+", e, DECI64)  ", (a,mc)->newton     (a, e,mc), 11,  MathContext.DECIMAL64),
      new Test(a->"LN.taylor1_inv("+a+",    DECI64)  ", (a,mc)->LN.taylor1_inv(a,mc), 11,  MathContext.DECIMAL64),
      new Test(a->"LN.taylor1    ("+a+",    DECI64)  ", (a,mc)->LN.taylor1    (a,mc), 11,  MathContext.DECIMAL64),
      new Test(a->"LN.taylor2    ("+a+",    DECI64)  ", (a,mc)->LN.taylor2    (a,mc), 11,  MathContext.DECIMAL64),
      new Test(a->"LN.taylor3    ("+a+",    DECI64)  ", (a,mc)->LN.taylor3    (a,mc), 11,  MathContext.DECIMAL64)
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