package operator;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import accuracy.BigNumber;
import accuracy.Format;
import accuracy.Range;
import operator.Trigonometric.ATAN;
import operator.Trigonometric.ATANH;

/**
 * Operator with one input, one output and a prefix symbol
 * @author Andreas Engel [engel@esa.cs.tu-darmstadt.de]
 */
public abstract class Unary extends Implementation {
  
  /** Unique ID for (de)marshalling */
  private static final long serialVersionUID = 8636512875084225810L;
  
  protected String symbol;
  
  protected Unary(Format a, Format r, String symbol) {
    super(Arrays.asList(a), r);
    this.symbol = symbol;
  }
  
/*
 * Testbench support
 **********************************************************************************************************************/
  
  @Override
  public Number[] apply(Number... inputs) {
    if (inputs.length != 1) throw new IllegalArgumentException("one input expected");
    return new Number[] {apply(BigNumber.cast(inputs[0]))};
  }
  
  /**
   * Apply operator with fast integer arithmetic
   * @param a
   * @return
   */
  public abstract Number apply(long a);
  
  /**
   * Apply operator with fast floating point arithmetic
   * @param a
   * @return
   */
  public abstract Number apply(double a);
  
  /**
   * Apply operator with large integer arithmetic (arbitrary size)
   * @param a
   * @return
   */
  public abstract Number apply(BigInteger a);
  
  /**
   * Apply operator with large floating point arithmetic (arbitrary size, upto 10**(+-2**32) bit precision)  
   * @param a
   * @return
   */
  public abstract Number apply(BigDecimal a);
  
  /**
   * Apply operator with arbitrary arithmetic
   * @param a
   * @param b
   * @return
   */
  protected Number apply(BigNumber a) {
    throw new NotImplementedException("arbitrary unary arithmetic");
  }
  

/*
 * Range propagation
 *********************************************************************************************************************/
  
  /**
   * Propagate range from operand to the arithmetic output.
   * @param a  range of operand
   * @return   range of result
   */
  public Range getResultRange(Range a) {
    if (a.isEmpty()) return Range.EMPTY;
    return a instanceof Range.AA ? getResultRange(a.toAA()) : getResultRange(a.toIA());
  }

  /**
   * Propagate non-empty range from operand to the arithmetic output using affine arithmetic.
   * An unbounded {@link Range} is generated, unless the underlying {@link Implementation} provides a more meaningful
   * propagation scheme.
   * @param a  range of operand
   * @return   range of result
   */
  protected Range getResultRange(Range.AA a) {
    return Range.UNBOUNDED;
  }
  
  /**
   * Propagate non-empty range from operand to the arithmetic output using interval arithmetic.
   * An unbounded {@link Range} is generated, unless the underlying {@link Implementation} provides a more meaningful
   * propagation scheme.
   * @param a  range of operand
   * @return   range of result
   */
  protected Range getResultRange(Range.IA a) {
    return Range.UNBOUNDED;
  }

/*
 * Support for implementation and test of iterative numeric approximations
 *********************************************************************************************************************/

  /**
   * Cancel approximation after this number of iterations, even if required accuracy is not reached.
   * Use 0 for unbounded approximation.
   */
  public static int maxApproximationIterations = 1000;
  
  /**
   * Check convergence criteria for iterative approximations.
   * 
   * @param estimate   the actual estimated result
   * @param stepSize   the distance to the estimate of the next iteration
   * @param threshold  the convergence threshold for relative errors
   * @param mc         the arithmetic precision for all computations
   * @oaram iterations number if iterations executed since last call to this function
   * @return           true, if {@link #maxApproximationIterations} reached or {@code stepSze/estimate <= threshold}
   */
  protected static boolean approximationConverged(BigDecimal estimate, BigDecimal stepSize, BigDecimal threshold, 
                                                  MathContext mc, int iterartions) {
    Test.iterations += iterartions;
    if (maxApproximationIterations != 0 && Test.iterations >= maxApproximationIterations) return true;
    if (estimate.compareTo(BigDecimal.ZERO) != 0) stepSize = stepSize.divide(estimate, mc);
    return stepSize.abs().compareTo(threshold) <= 0;
  }
  
  /**
   * Check convergence criteria for iterative approximations.
   * Increments {@link Test#iterations} by 1.
   * 
   * @param estimate   the actual estimated result
   * @param stepSize   the distance to the estimate of the next iteration
   * @param threshold  the convergence threshold for relative errors
   * @param mc         the arithmetic precision for all computations
   * @return           true, if {@link #maxApproximationIterations} reached or {@code stepSze/estimate <= threshold}
   */
  protected static boolean approximationConverged(BigDecimal estimate, BigDecimal stepSize, BigDecimal threshold, 
                                                  MathContext mc) {
    return approximationConverged(estimate, stepSize, threshold, mc, 1);
  }
  
  /**
   * The Coordinate Rotation Digital Computer is used to iteratively approximate transcendental functions as described
   * by Volder in 1959.
   * <p>
   * The CORDIC algorithm is based on micro-rotations realized by SHIFT and and ADD operations. Some angles and square
   * roots are required for normalization and steering purposes, but those values can be precomputed and tabulated to
   * simplify the runtime operations.
   * <p>
   * The approximated function is selected by
   * <ul>
   *   <li> initializing the coordinate vector {@link #x}, {@link #y} and {@link #z}
   *   <li> choosing the rotation {@link mode}
   *   <li> rotating the coordinate vector by micro {@link #step}s until either {@link y} (Vectoring} or {@link z} 
   *        (Rotating) reaches zero.
   *   <li> select (a combination of) the remaining vector components representing the desired result. 
   * </ul> 
   * <table border=1>
   *   <tr>
   *     <th>{@link #mode}</th>
   *     <th>Rotating  ({@code z -> 0})</th>
   *     <th>Vectoring ({@code y -> 0})</th>
   *     <th>scale / angle</th>
   *   </tr>
   *   <tr>
   *     <td>1: Circular</td>
   *     <td>
   *<pre>
   *xn = K [x0 cos(z0) - y0 sin(z0)]
   *yn = K [x0 sin(z0) - y0 cos(z0)]
   *</pre>
   *     </td>
   *     <td>
   *<pre>
   *xn = K sqrt(x0² + y0²)
   *zn = z0 + atan(y0 / x0)
   *</pre>
   *     </td>
   *     <td>
   *<pre>
   *Ki = sqrt(1+2^-2i)
   *ai = atan(2^-i)
   *</pre>
   *   </tr>
   *   <tr>
   *     <td>0: Linear</td>
   *     <td>
   *<pre>
   *xn = x0
   *yn = y0 + x0 z0
   *</pre>
   *     </td>
   *     <td>
   *<pre>
   *xn = x0
   *zn = z0 + y0 / x0
   *</pre>
   *     </td>
   *     <td>
   *<pre>
   *Ki = 1
   *ai = 2^-i
   *</pre>
   *     </td>
   *   </tr>
   *   <tr>
   *     <td>-1: Hyperbolic</td>
   *     <td>
   *<pre>
   *xn = K [x0 cosh(x0 cosh(z0) + y0 sinh(z0)]
   *yn = K [x0 sinh(x0 cosh(z0) + y0 cosh(z0)]
   *</pre>
   *     </td>
   *     <td>
   *<pre>
   *xn = K sqrt(x0² - y0²)
   *zn = z0 + atanh(y0 / x0)
   *</pre>
   *     </td>
   *     <td>
   *<pre>
   *Ki = sqrt(1 - 2^-2i)
   *ai = atanh(2^-i)
   *</pre>
   *     </td>
   *   </tr>
   * </table>
   * <p>
   * Besides most {@link Trigonometric} functions, {link #exp}, {@link #ln} and {@link sqrt} are supported. Instead of 
   * using a fixed number of iterations, the approximation is terminated as soon as a certain accuracy is reached.
   * There are derivations of the {@link CORDIC} concept for direct approximation of inverse (hyperbolic) (co)sine of
   * {@code a} by rotating {@link y} to {@code a} (http://ieeexplore.ieee.org/xpl/login.jsp?arnumber=606820). This
   * concept is however not well suited for the dynamic accuracy approach of this implementation.
   * <p>
   * <b>Attention:</b> The CORDIC convergence interval typically does not cover the whole input domain of the 
   * approximated function.
   * <p> 
   * This implementation follows http://web.cs.ucla.edu/digital_arithmetic/files/ch11.pdf
   */
  protected static class CORDIC {
    
    /**
     * Lookup table for the angles {@code atan 2^-i} of the micro rotations.
     * Hyperbolic angles are mapped to negative indices.
     */
    private static final HashMap<Integer,BigDecimal> LUT_ANGLE = new HashMap<Integer,BigDecimal>();
    
    /**
     * Lookup table for the normalization factors {@code sqrt(1+2^-2i)} of the micro rotations.
     * Hyperbolic factors are mapped to negative indices.
     */
    private static final HashMap<Integer,BigDecimal> LUT_SCALE = new HashMap<Integer,BigDecimal>();
    
    /**
     * Rotation mode.
     * 1 for circular, 0 for linear, -1 for hyperbolic
     */
    private static int mode;
    
    /**
     * Rotation target.
     * If true, z is driven to zero, else drives y is driven to zero
     */
    private static boolean rotate;
    
    /**
     * Part of the rotation vector
     */
    private static BigDecimal x;
    
    /**
     * Part of the rotation vector
     */
    private static BigDecimal y;
    
    /**
     * Part of the rotation vector
     */
    private static BigDecimal z;
    
    /**
     * Accumulated normalization factor
     */
    private static BigDecimal k;
    
    /**
     * Arithmetic precision for all operations
     */
    private static MathContext mc;
    
    /**
     * Lookup or generate angle for micro rotation.
     * 
     * @param i      iteration index
     * @param shift  {@code 2^-i}
     * @return       {@code i < 0 ? atan 2^-i : atanh 2^-i}
     */
    private static BigDecimal getAngle(int i, BigDecimal shift) {
      MathContext mc = MathContext.DECIMAL128;
      BigDecimal res = LUT_ANGLE.get(i);
      if (res != null) return res;
      res = i < 0 ? ATANH.approximate(shift, mc)
                  : ATAN .approximate(shift, mc);
      LUT_ANGLE.put(i, res);
      return res;
    }
    
    /**
     * Lookup or generate normalization factor for micro rotation.
     * 
     * @param i      iteration index
     * @param shift  {@code 2^-i}
     * @return       {@code i < 0 ? sqrt(1+2^-2i) : sqrt(1-2^-2i)
     */
    private static BigDecimal getScale(int i, BigDecimal shift) {
      MathContext mc = MathContext.DECIMAL128;
      BigDecimal res = LUT_SCALE.get(i);
      if (res != null) return res;
      res = shift.multiply(shift, mc);
      if (i < 0) res = res.negate();
      res = SQRT.approximate(BigDecimal.ONE.add(res, mc), mc); 
      LUT_SCALE.put(i, res);
      return res;
    }
    
    /**
     * Perform a micro rotation.
     * 
     * @param i      iteration index
     * @param shift  {@code 2^-i}
     * @return       true, if micro rotation was actually executed, and false, if rotation target is already reached 
     */
    private static boolean step(int i, BigDecimal shift) {
      
      // choose rotation direction (rotate
      BigDecimal target = rotate ? z : y.negate();
      int dir = target.signum();
      
      // no fixed number if iterations (as typical CORDIC implementations) => may stop here, if rotation target reached
      if (dir == 0) return false;
      
      // lookup or generate micro angle and scale
      BigDecimal angle = mode == 0 ? shift          : getAngle(mode*i, shift);
      BigDecimal scale = mode == 0 ? BigDecimal.ONE : getScale(mode*i, shift);
      
      // accumulate normalization factor
      k = k.multiply(scale, mc);
      
      // rotate vector
      BigDecimal xold = x;
      if (mode != 0) {
        scale = y.multiply(shift, mc);
        x = dir*mode > 0 ? x.subtract(scale, mc) : x.add(scale, mc);
      }
      scale = xold.multiply(shift);
      y = dir < 0 ? y.subtract(scale, mc) : y.add(scale, mc);
      z = dir > 0 ? z.subtract(angle, mc) : z.add(angle, mc);
      
      Test.iterations++;
      return true;
    }
    
    /**
     * Perform micro rotations, until the required accuracy is reached
     * @param res generator for the final result (used for accuracy assesment)
     * @return    the final result
     */
    private static BigDecimal run(Supplier<BigDecimal> res) {
      
      // additional accuracy for intermediate results
      int precision = mc.getPrecision();
      mc = new MathContext(precision+2, mc.getRoundingMode());
      BigDecimal thr = BigDecimal.ONE.scaleByPowerOfTen(-precision);     // accuracy threshold
      
      // running variables to be updated for each operation
      k                = BigDecimal.ONE;
      BigDecimal  two  = BigDecimal.valueOf(2);
      BigDecimal shift = mode < 0 ? BigDecimal.ONE.divide(two) : BigDecimal.ONE;
      
      // To ensure convergence, hyperbolic functions require the repetition of iterations 4. For each repeated 
      // iteration k, the iteration 3k+1 also has to be repeated.
      int repeat       = mode < 0 ? 4 : -1;
      
      // dynamic runtime
      for (int i = mode < 0 ? 1 : 0; ; i++) {
        BigDecimal o = res.get();
        if (!step(i, shift)) return o;
        BigDecimal n = res.get();
        if (o == null || n == null) Test.iterations++;
        else if (Unary.approximationConverged(o, n.subtract(o, mc), thr, mc, 0)) return n;
        
        if (i == repeat) {
          o = n;
          if (!step(i, shift)) return o;
          n = res.get();
          if (o == null || n == null) Test.iterations++;
          else if (Unary.approximationConverged(o, n.subtract(o, mc), thr, mc, 0)) return n;
          repeat = 3*repeat + 1;
        }
        shift = shift.divide(two);
      } 
    }
    
    /**
     * Fill the lookup tables {@link #LUT_ANGLE} and {@link #LUT_SCALE}.
     * <p>
     * It is not required to call this method before using the {@link CORDIC}, as non existing values are supplemented
     * on the fly. However, these additional computations might mess up the iteration statistics of of the actually
     * approximated function.
     * 
     * @param n the index upto which the values should be precomputed.
     */
    public static void prepare(int n) {
      BigDecimal shift = BigDecimal.ONE;
      BigDecimal  two  = BigDecimal.valueOf(2);
      for (int i=0; i<n; i++) {
        getAngle( i, shift);
        getAngle(-i, shift);
        getScale( i, shift);
        getScale(-i, shift);
        shift = shift.divide(two);
      }
    }
    
    /**
     * Approximate a sine.
     * @param a   the arithmetic input ({@code |a| < 1.744} for {@link MathContext#DECIMAL128})
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code sin(a)}
     */
    public static BigDecimal sin(BigDecimal a, MathContext mc) {
      rotate    = true;
      mode      = 1;
      x         = BigDecimal.ONE;
      y         = BigDecimal.ZERO;
      z         = a;
      CORDIC.mc = mc;
      return run(() -> y.divide(k, mc));
    }
    
    /**
     * Approximate a cosine.
     * @param a   the arithmetic input ({@code |a| < 1.744} for {@link MathContext#DECIMAL128})
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code cos(a)}
     */
    public static BigDecimal cos(BigDecimal a, MathContext mc) {
      rotate    = true;
      mode      = 1;
      x         = BigDecimal.ONE;
      y         = BigDecimal.ZERO;
      z         = a;
      CORDIC.mc = mc;
      return run(() -> x.divide(k, mc));
    }
    
    /**
     * Approximate a tangent.
     * @param a   the arithmetic input ({@code |a| < 1.744} for {@link MathContext#DECIMAL128})
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code tan(a)}
     */
    public static BigDecimal tan(BigDecimal a, MathContext mc) {
      rotate    = true;
      mode      = 1;
      x         = BigDecimal.ONE;
      y         = BigDecimal.ZERO;
      z         = a;
      CORDIC.mc = mc;
      return run(() -> x.compareTo(BigDecimal.ZERO) == 0 ? null : y.divide(x, mc));
    }
    
    /**
     * Approximate a cotangent.
     * @param a   the arithmetic input ({@code 0 < |a| < 1.744} for {@link MathContext#DECIMAL128})
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code cot(a)}
     */
    public static BigDecimal cot(BigDecimal a, MathContext mc) {
      if (a.compareTo(BigDecimal.ZERO) == 0) return null;
      rotate    = true;
      mode      = 1;
      x         = BigDecimal.ONE;
      y         = BigDecimal.ZERO;
      z         = a;
      CORDIC.mc = mc;
      return run(() -> y.compareTo(BigDecimal.ZERO) == 0 ? null : x.divide(y, mc));
    }
    
    /**
     * Approximate a hyperbolic sine.
     * @param a   the arithmetic input ({@code |a| < 1.119} for {@link MathContext#DECIMAL128})
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code sinh(a)}
     */
    public static BigDecimal sinh(BigDecimal a, MathContext mc) {
      if (a.compareTo(BigDecimal.ZERO) == 0) return null;
      rotate    = true;
      mode      = -1;
      x         = BigDecimal.ONE;
      y         = BigDecimal.ZERO;
      z         = a;
      CORDIC.mc = mc;
      return run(() -> y.divide(k, mc));
    }
    
    /**
     * Approximate a hyperbolic cosine.
     * @param a   the arithmetic input ({@code |a| < 1.119} for {@link MathContext#DECIMAL128})
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code cosh(a)}
     */
    public static BigDecimal cosh(BigDecimal a, MathContext mc) {
      rotate    = true;
      mode      = -1;
      x         = BigDecimal.ONE;
      y         = BigDecimal.ZERO;
      z         = a;
      CORDIC.mc = mc;
      return run(() -> x.divide(k, mc));
    }
    
    /**
     * Approximate a hyperbolic tangent.
     * @param a   the arithmetic input ({@code |a| < 1.119} for {@link MathContext#DECIMAL128})
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code tanh(a)}
     */
    public static BigDecimal tanh(BigDecimal a, MathContext mc) {
      rotate    = true;
      mode      = -1;
      x         = BigDecimal.ONE;
      y         = BigDecimal.ZERO;
      z         = a;
      CORDIC.mc = mc;
      return run(() -> x.compareTo(BigDecimal.ZERO) == 0 ? null : y.divide(x, mc));
    }
    
    /**
     * Approximate a hyperbolic cotangent.
     * @param a   the arithmetic input ({@code 0 < |a| < 1.119} for {@link MathContext#DECIMAL128})
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code coth(a)}
     */
    public static BigDecimal coth(BigDecimal a, MathContext mc) {
      if (a.compareTo(BigDecimal.ZERO) == 0) return null;
      rotate    = true;
      rotate    = true;
      mode      = -1;
      x         = BigDecimal.ONE;
      y         = BigDecimal.ZERO;
      z         = a;
      CORDIC.mc = mc;
      return run(() -> y.compareTo(BigDecimal.ZERO) == 0 ? null : x.divide(y, mc));
    }
    
    /**
     * Approximate a arc tangent.
     * @param a   the arithmetic input (no limitations by convergence interval)
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code atan(a)}
     */
    public static BigDecimal atan(BigDecimal a, MathContext mc) {
      rotate    = false;
      mode      = 1;
      x         = BigDecimal.ONE;
      y         = a;
      z         = BigDecimal.ZERO;
      CORDIC.mc = mc;
      return run(() -> z);
    }
    
    /**
     * Approximate an area hyperbolic tangent.
     * @param a   the arithmetic input ({@code |a| < 0.807} for {@link MathContext#DECIMAL128})
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code atanh(a)}
     */
    public static BigDecimal atanh(BigDecimal a, MathContext mc) {
      rotate    = false;
      mode      = -1;
      x         = BigDecimal.ONE;
      y         = a;
      z         = BigDecimal.ZERO;
      CORDIC.mc = mc;
      return run(() -> z);
    }
    
    /**
     * Approximate an arc cotangent.
     * @param a   the arithmetic input  (no limitations by convergence interval)
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code acot(a)}
     */
    public static BigDecimal acot(BigDecimal a, MathContext mc) {
      rotate    = false;
      mode      = 1;
      x         = BigDecimal.ONE;
      y         = a.negate();          // differs from http://web.cs.ucla.edu/digital_arithmetic/files/ch11.pdf page 33
      z         = Trigonometric.PIH;
      CORDIC.mc = mc;
      return run(() -> z);
    }
    
    /**
     * Approximate a area hyperbolic cotangent.
     * @param a   the arithmetic input ({@code |a| > 1.258} for {@link MathContext#DECIMAL128})
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code acoth(a)}
     */
    public static BigDecimal acoth(BigDecimal a, MathContext mc) {
      if (a.abs().compareTo(BigDecimal.ONE) <= 0) return null;
      rotate    = false;
      mode      = -1;
      x         = a;
      y         = BigDecimal.ONE;
      z         = BigDecimal.ZERO;
      CORDIC.mc = mc;
      return run(() -> z);
    }
    
    /**
     * Approximate the exponential function.
     * @param a   the arithmetic input ({@code |a| < 1.119} for {@link MathContext#DECIMAL128})
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code exp(a)}
     */
    public static BigDecimal exp(BigDecimal a, MathContext mc) {
      rotate    = true;
      mode      = -1;
      x         = BigDecimal.ONE;
      y         = BigDecimal.ONE;
      z         = a;
      CORDIC.mc = mc;
      return run(() -> x.divide(k, mc));
    }
    
    /**
     * Approximate the natural logarithm.
     * @param a   the arithmetic input ({@code 0.106 < a < 9.360} for {@link MathContext#DECIMAL128})
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code ln(a)}
     */
    public static BigDecimal ln(BigDecimal a, MathContext mc) {
      rotate    = false;
      mode      = -1;
      x         = a.add     (BigDecimal.ONE, mc);
      y         = a.subtract(BigDecimal.ONE, mc);
      z         = BigDecimal.ZERO;
      CORDIC.mc = mc;
      return run(() -> z.add(z, mc));
    }
    
    /**
     * Approximate the square root.
     * @param a   the arithmetic input ({@code 0.026 < a < 2.340} for {@link MathContext#DECIMAL128})
     * @param mc  the decimal precision and {@link RoundingMode} to apply. 
     * @return    {@code sqrt(a)}
     */
    public static BigDecimal sqrt(BigDecimal a, MathContext mc) {
      rotate       = false;
      mode         = -1;
      BigDecimal q = new BigDecimal("0.25");
      x            = a.add(q, mc);
      y            = a.subtract(q, mc);
      z            = BigDecimal.ZERO;
      CORDIC.mc    = mc;
      return run(() -> x.divide(k, mc));
    }
    
    public static void main(String[] args) {
      testApproximations(1, 3000, -3, Verbosity.ERRORS, "out/stat", new Test[] {
          new Test(a -> "Math  (" + a + ")", (a,mc) -> Math.sqrt(a.doubleValue()), null, null),
          new Test(a -> "CORDIC(" + a + ")", (a,mc) -> sqrt     (a, mc),              0, MathContext.DECIMAL128),
      });
    }
    
  }
  
  /**
   * Configuration of a numeric approximation test case.
   * Each test case describes one approximation function to be applied to multiple arguments and compared to a 
   * reference calculation.
   */
  protected static class Test {
    
    /**
     * Iteration counter to be incremented by approximation functions with each iteration.
     */
    public static int iterations;
    
    /**
     * Generator for description of approximation invocation including the argument.
     */
    private Function<Number,String> description;
    
    /**
     * Invocation of approximation with certain accuracy
     */
    private BiFunction<BigDecimal, MathContext, Number> approximation;
    
    /**
     * Index of test case to compare approximation against
     */
    private Integer compareCase;
    
    /**
     * Required accuracy of approximation
     */
    private MathContext mc;
    
    /**
     * Result of last invocation
     */
    private Number result = null;
    
    /**
     * Maximum acceptable absolute deviation from reference computation.
     * This threshold depends on the configured accuracy (i.e., {@code 10^(-precision+2)}). All test cases configured 
     * for {@link MathContext#DECIMAL128} precision are assumed to be compared against double reference calculations
     * and use a threshold of {@code 10^-12}.
     */
    private BigDecimal threshold = null;
    
    /**
     * Result of {@link #description} after last invocation of a concrete argument
     */
    private String desc = null;
    
    private int minIterations = Integer.MAX_VALUE;
    
    private int maxIterations = 0;
    
    private long overallIterations = 0;

    private int invocations = 0;
    
    private double maxError = 0;
    
    private int numErrors = 0;
    
    
    /**
     * Generate a numeric approximation test case.
     * @param description   Generator for description of approximation invocation including the argument
     * @param approximation Invocation of approximation with certain accuracy
     * @param compareCase   Index of test case to compare approximation against (null for reference cases)
     * @param mc            Required accuracy of approximation
     */
    public Test(Function<Number,String> description, BiFunction<BigDecimal, MathContext, Number> approximation,
                Integer compareCase, MathContext mc) {
      this.description   = description;
      this.approximation = approximation;
      this.compareCase   = compareCase;
      this.mc            = mc;
      if (mc != null) {
        // DECIMAL128 is compared against the double reference with 52 bit mantissa = 15.5 significant decimal digits.
        // Compared to high precision numeric (e.g., http://keisan.casio.com/calculator), JAVA double results sometimes
        // already differ in the 12th digit
        int precision = mc == MathContext.DECIMAL128 ? 12 : mc.getPrecision();    
        threshold = BigDecimal.ONE.scaleByPowerOfTen(-precision+2); // allow deviations in last two decimal digit
      }
    }
    
    /**
     * Invoke the approximation for a certain argument
     * @param a the argument
     * @return  the result of the approximation
     */
    public Number evaluate(BigDecimal a) {
      iterations = 0;
      result = approximation.apply(a, mc);
      
      // update desciption (avoid scientific format for double results)
      desc = description.apply(a) + " = " + 
        ((result instanceof Double) ? String.format("%.30f", result.doubleValue()).replace(",", ".") : result);
      
      // update convergence statistics
      if (iterations > 0) {
        if (iterations > maxIterations) maxIterations = iterations;
        if (iterations < minIterations) minIterations = iterations;
        overallIterations += iterations;
        desc += " after " + iterations + " iterations";
      }
      invocations++;
      
      return result;
    }
    
    /**
     * @return Result of the last invocation of this approximation.
     */
    public Number getResult() {
      return result;
    }
    
    /**
     * @return Index of test case to compare the approximation result against.
     */
    public Integer getCompareCase() {
      return compareCase;
    }
    
    /**
     * Compare result of last invocation or this approximation against a reference calculation.
     * If this {@link Test} case is configured with {@link RoundingMode#FLOOR} or {@link RoundingMode#CEILING}, the 
     * direction of the deviation from the reference calculation is also asserted.
     * @param a Result of the reference calculation.
     * @return an error message or null, if no errors were detected
     */
    public String compare(Number a, PrintStream dump) {
      String msg = null;
      
      
      // the values to compare
      BigDecimal uut = result instanceof BigDecimal                             ? (BigDecimal) result : 
                       result == null || !Double.isFinite(result.doubleValue()) ? null                :
                       BigDecimal.valueOf(result.doubleValue());
      BigDecimal ref =      a instanceof BigDecimal                             ? (BigDecimal)      a : 
                            a == null || !Double.isFinite(     a.doubleValue()) ? null                :
                       BigDecimal.valueOf(a.doubleValue());

      
      // which error to compare against threshold
      BigDecimal err = (uut == null && ref == null)                           ? BigDecimal.ZERO :  // both infinite
                       (uut == null || ref == null)                           ? threshold       :  // only one infinite
                       ref.compareTo(BigDecimal.ZERO) == 0                    ? uut             :  // absolute error
                        uut.subtract(ref).divide(ref, MathContext.DECIMAL128);                     // relative error
      
      // compare absolute error against threshold
      BigDecimal aerr = err.abs(); 
      if (err.abs().compareTo(threshold) >= 0) msg = desc + " [" + err + "]";
      
      // keep track of max error
      if (aerr.doubleValue() > maxError) maxError = aerr.doubleValue();
      
      // dump relative error
      String h = getCSVHeader();
      if (dump != null && h != null) {
        dump.print(String.format(" %+" + (h.length()+2) + ".1e", err.doubleValue()).replace(",", "."));
      }

      // check correct rounding
      if (uut != null && ref != null) {
        if (mc.getRoundingMode() == RoundingMode.FLOOR && uut.compareTo(ref) > 0) {
          if (msg == null) msg = desc;
          msg += " to large";
        }
        if (mc.getRoundingMode() == RoundingMode.CEILING && uut.compareTo(ref) < 0) {
          if (msg == null) msg = desc;
          msg += " to small";
        }
      }
      
      // keep track of number of errors
      if (msg != null) numErrors++;
      
      return msg;
    }
    
    /**
     * @return Result of {@link #description} after last invocation of a concrete argument
     */
    public String getDescription() {
      return desc;
    }
    
    /**
     * Format all iteration and error statistics in one line
     * @return summary of collected statistics
     */
    public String getStatistics() {
      String res = description.apply(null);
      if (overallIterations > 0) {
        res += String.format(" [Iterations: %4d <= %6.1f <= %4d, Errors: %5d, max %e]",
            minIterations,
            ((double)overallIterations)/invocations,
            maxIterations,
            numErrors,
            maxError);
      }
      return res;
    }
    
    /**
     * Generate a short description of those test cases to be dumped to CSV.
     * Any test case, whose description starts with a dot will not by exported to CSV  
     * @return short header
     */
    public String getCSVHeader() {
      String desc = description.apply(null);
      if (desc.startsWith(".")) return null;
      return desc.substring(0,desc.indexOf("(")).trim();
    }
  }
  
  protected static enum Verbosity {
    /** print nothing */                               SILENT    (false, false, false, false),
    /** print progress and final statistics */         STATISTICS(false, false, true,  true),
    /** print errors, progress and final statistics */ ERRORS    (true,  false, true,  true),
    /** print all information */                       ALL     (true,  true,  true,  true);
    private Verbosity(boolean errors, boolean results, boolean progress, boolean statistics) {
      this.errors     = errors;
      this.results    = results;
      this.progress   = progress;
      this.statistics = statistics;
    }
    public boolean errors;
    public boolean results;
    public boolean progress;
    public boolean statistics;
  }
  
  /**
   * Invoke {@link Test} cases for a range of arguments and report detected errors and iterations counts.
   * @param min         first argument to invoke (divided by {@code 10^resolution})
   * @param max         last  argument to invoke (divided by {@code 10^resolution})
   * @param resolution  scaling factor for input arguments (similar to a logarithmic stepsize)
   * @param verbosity   control information to be printed to console
   * @param dump        if not null, a CSV of the all iteration counts will be dumped to the given fileName
   * @param cases       {@link Test} cases to be invoked for each argument
   */
  protected static void testApproximations(int min, int max, int resolution, Verbosity v, String dump, Test[] cases) {
    int errors = 0;
    int tests  = 0;
    
    // ensure all constants and LUTs are generated to not distort iteration counts (PI also triggers SQRT.TWO)
    if (Logarithm.LN.TWO.add(Trigonometric.PI).signum() == 0) return;
    CORDIC.prepare(200);
    
    // write header to dump file
    PrintStream d = null;
    String firstCol = "argument";
    if (dump != null) {
      try {
        Files.createDirectories(Paths.get(dump).getParent());
        d = new PrintStream(new FileOutputStream(dump));
        d.print(firstCol);
        for (Test t : cases) {
          String h = t.getCSVHeader();
          if (h == null) continue;
          d.print(" " + h + "_I");
        }
        for (Test t : cases) {
          String h = t.getCSVHeader();
          if (h == null) continue;
          d.print(" " + h + "_E");
        }
        d.print("\n");
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    
    // generate arguments
    for (int i=min; i<=max; i++) {
      BigDecimal a = BigDecimal.valueOf(i).scaleByPowerOfTen(resolution);
      
      // dump arg
      if (d != null) d.print(String.format("%+" + firstCol.length() + ".3f", a.doubleValue()).replace(",", "."));
      
      // evaluate test cases
      for (Test t : cases) {
        t.evaluate(a);
        
        // dump iteration count
        if (d == null) continue;
        String h = t.getCSVHeader();
        if (h == null) continue;
        if (d != null) d.print(String.format(" %" + (h.length()+2) + "d", Test.iterations));
      }
      
      // perform comparisons between test cases and report errors
      HashSet<Test> plotted = new HashSet<Test>(); // to avoid plotting the same reference calculation multiple times
      for (Test t : cases) {
        Integer c = t.getCompareCase();
        if (c != null) {
          tests++;
          if (tests % 10000 == 0 && v.progress) System.out.println(errors + " of " + tests + " tests failed...");
        }
        String msg = c == null ? null : t.compare(cases[c].getResult(), d);
        if (msg != null) {
          errors++;
          if (v.errors) {
            if (!plotted.contains(cases[c])) {
              System.out.println(cases[c].getDescription());
              plotted.add(cases[c]);
            }
            System.out.println(msg);
            plotted.add(t);
          }
        } else if (v.results && !plotted.contains(t)) {
          System.out.println(t.getDescription());
          plotted.add(t);
        }
      }
      
      // close dump line
      if (d != null) d.print("\n");
    }

    // close dump file
    if (d != null) d.close();
    
    // number of final errors
    if (v.statistics) System.out.println(errors + " of " + tests + " tests failed.");
    
    // plot iteration statistics
    if (v.statistics) for (Test t : cases) System.out.println(t.getStatistics());
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