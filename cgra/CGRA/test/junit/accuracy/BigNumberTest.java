package junit.accuracy;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import accuracy.BigNumber;
import accuracy.Format;
import operator.ADD;
import operator.DIV;
import operator.Implementation;
import operator.MUL;
import operator.SUB;

/**
 * Unit tests for {@code BigNumber}s.
 * 
 * @author Andreas Engel [engel@esa.cs.tu-darmstadt.de]
 */
@RunWith(Enclosed.class)
public class BigNumberTest {
  
  /**
   * Test generation of (sliced) binary and hexadecimal literals.
   * Also check for {@link Exception}s on invalid slice request.
   * 
   * @author Andreas Engel [engel@esa.cs.tu-darmstadt.de]
   */
  public static class Literals {
    private boolean[] raw = new boolean[] {
      true, false, true, true, false, false, true, false, false, true
    };
    
    @Test public void bin()      {assertThat(BigNumber.getBinLiteral(raw),     equalToIgnoringCase("10'b1001001101"));}
    @Test public void hex()      {assertThat(BigNumber.getHexLiteral(raw),     equalToIgnoringCase("10'h24d"));}
    @Test public void binSlice() {assertThat(BigNumber.getBinLiteral(raw,6,2), equalToIgnoringCase("5'b10011"));}
    @Test public void hexSlice() {assertThat(BigNumber.getHexLiteral(raw,6,2), equalToIgnoringCase("5'h13"));}
    @Test(expected = Exception.class) public void binExceptionHigh() {BigNumber.getBinLiteral(raw, 10,  0);}
    @Test(expected = Exception.class) public void binExceptionLow()  {BigNumber.getBinLiteral(raw,  9, -1);}
    @Test(expected = Exception.class) public void binExceptionEmpty(){BigNumber.getBinLiteral(raw,  4,  5);}
    @Test(expected = Exception.class) public void hexExceptionHigh() {BigNumber.getHexLiteral(raw, 10,  0);}
    @Test(expected = Exception.class) public void hexExceptionLow()  {BigNumber.getHexLiteral(raw,  9, -1);}
    @Test(expected = Exception.class) public void hexExceptionEmpty(){BigNumber.getHexLiteral(raw,  4,  5);}
    @Test(expected = Exception.class) public void parseException()   {BigNumber.parse(Format.LONG, "32b0");}
  }
  
  /**
   * Test (in)equality of {@code BigNumber}s with equal or deviating properties.
   * Also check for {@link Exception}s on invalid initializations with raw.
   * 
   * @author Andreas Engel [engel@esa.cs.tu-darmstadt.de]
   */
  @RunWith(Parameterized.class)
  public static class Equality {
    
    /**
     * @return  test vector
     */
    @Parameters(name= "{0}={1}")
    public static Object[][] data() {
      return new Object[][] {
        {BigNumber.cast(4),                        new BigNumber(false,  0, new boolean[] {false, false, true})},
        {BigNumber.cast(4),                        new BigNumber(false,  2, new boolean[] {true})},
        {BigNumber.cast(0.125),                    new BigNumber(false, -3, new boolean[] {true})},
        {BigNumber.cast(Double.POSITIVE_INFINITY), new BigNumber(false, Integer.MAX_VALUE, new boolean[0])},
        {BigNumber.cast(Double.NEGATIVE_INFINITY), new BigNumber(true,  Integer.MAX_VALUE, new boolean[0])},
        {BigNumber.cast(Double.NaN),               new BigNumber(true,  Integer.MIN_VALUE, new boolean[0])},
        {BigNumber.cast(Double.NaN),               new BigNumber(false, Integer.MIN_VALUE, new boolean[0])},
      };
    }
    
    @Parameter(0)
    public BigNumber n;
    
    @Parameter(1)
    public Object o;
    
    @Test 
    public void compare() {
      assertThat(n, equalTo(o));
    } 
    
    
    /**
     * Attention: in (really) rare cases, the randomly generated raw binary sequences might be equal
     */
    @Test 
    public void random() {
      assertThat(Format.LONG.getRandomValue(), not(equalTo(Format.LONG.getRandomValue())));
    }
  }
  
  /**
   * Test
   * <ul>
   *   <li>{@link BigNumber#cast}
   *   <li>{@link Format#getValue}
   *   <li>{@link Format#getRawBinary}
   * </ul>
   * @author Andreas Engel [engel@esa.cs.tu-darmstadt.de]
   */
  @RunWith(Parameterized.class)
  public static class ValueTo {
    
    /**
     * @return  test vector
     */
    @Parameters(name= "{0},{1}")
    public static List<Object[]> data() {
      Object[][] unsigned = {
        {"uint10",        0l,        "10'h000"},
        {"uint10",        1l,        "10'h001"},
        {"uint10",        2l,        "10'h002"},
        {"uint10",     1022l,        "10'h3fe"},
        {"uint10",     1023l,        "10'h3ff"},
        {"uint10",     1024l,        "10'h000"},
        {"uint10",       -1l,        "10'h3ff"},
        {"uint10",       -2l,        "10'h3fe"},
        {"uint10",    -1022l,        "10'h002"},
        {"uint10",    -1023l,        "10'h001"},
        {"uint10",    -1024l,        "10'h000"},
        {"uint10",        1.4,       "10'h001"},
        {"uint10",        1.5,       "10'h002"},
        {"uint10",        1.6,       "10'h002"},
        {"uint10",       -1.4,       "10'h3ff"},
        {"uint10",       -1.5,       "10'h3ff"}, // Math.round resolves tie towards +inf
        {"uint10",       -1.6,       "10'h3fe"},
        
        {"ufix4x6",       0l,        "10'h000"},
        {"ufix4x6",       1l,        "10'h040"},
        {"ufix4x6",       2l,        "10'h080"},
        {"ufix4x6",      14l,        "10'h380"},
        {"ufix4x6",      15l,        "10'h3c0"},
        {"ufix4x6",      16l,        "10'h000"},
        {"ufix4x6",      -1l,        "10'h3c0"},
        {"ufix4x6",      -2l,        "10'h380"},
        {"ufix4x6",     -14l,        "10'h080"},
        {"ufix4x6",     -15l,        "10'h040"},
        {"ufix4x6",     -16l,        "10'h000"},
        {"ufix4x6",       1.4,       "10'h05a"}, // 1.4       * 64 =  89.6 =>  90
        {"ufix4x6",       1.5,       "10'h060"}, // 1.5       * 64 =  96.0 =>  96
        {"ufix4x6",       1.6,       "10'h066"}, // 1.6       * 64 = 102.4 => 102
        {"ufix4x6",       1.6015625, "10'h067"}, // 1.6015625 * 64 = 102.5 => 103
        {"ufix4x6",      -1.4,       "10'h3a6"}, //-1.4       * 64 = -89.6 => -90
        {"ufix4x6",      -1.5,       "10'h3a0"}, //-1.5       * 64 = -96.0 => -96
        {"ufix4x6",      -1.6,       "10'h39a"}, //-1.6       * 64 =-102.4 =>-102
        {"ufix4x6",      -1.6015625, "10'h39a"}, //-1.6015625 * 64 = 102.5 =>-102  (tie towards +infinity, as above)
      };
      // sign should not change behavior, test it anyway
      LinkedList<Object[]> res = new LinkedList<Object[]>(Arrays.asList(unsigned));
      for (Object[] conf : unsigned) res.add(new Object[] {conf[0].toString().substring(1), conf[1], conf[2]});
      
      // floatingpoint are alway signed (so far)
      res.addAll(Arrays.asList(new Object[][] {
        // IEEE754 single precision: https://www.h-schmidt.net/FloatConverter/IEEE754de.html
        {"float8x23",     0l,                            "32'h00000000"},
        {"float8x23",     1l,                            "32'h3f800000"},
        {"float8x23",     2l,                            "32'h40000000"},
        {"float8x23",  1022l,                            "32'h447f8000"},
        {"float8x23",  1023l,                            "32'h447fc000"},
        {"float8x23",  1024l,                            "32'h44800000"},
        {"float8x23",    -1l,                            "32'hbf800000"},
        {"float8x23",    -2l,                            "32'hc0000000"},
        {"float8x23", -1022l,                            "32'hc47f8000"},
        {"float8x23", -1023l,                            "32'hc47fc000"},
        {"float8x23", -1024l,                            "32'hc4800000"},
        {"float8x23",     1.4,                           "32'h3fb33333"},
        {"float8x23",     1.5,                           "32'h3fc00000"},
        {"float8x23",     1.6,                           "32'h3fcccccd"},
        {"float8x23",     1.6015625,                     "32'h3fcd0000"},
        {"float8x23",    -1.4,                           "32'hbfb33333"},
        {"float8x23",    -1.5,                           "32'hbfc00000"},
        {"float8x23",    -1.6015625,                     "32'hbfcd0000"},
        {"float8x23", Float.intBitsToFloat(0x7f7fffff),  "32'h7f7fffff"}, // largest before +inf:    3.40e38
        {"float8x23",     4e38,                          "32'h7f800000"}, // +infinity
        {"float8x23", Float.intBitsToFloat(0xff7fffff),  "32'hff7fffff"}, // smallest before -inf:  -3.40e38
        {"float8x23",    -4e38,                          "32'hff800000"}, // -infinity
        {"float8x23", Float.intBitsToFloat(0x00800000),  "32'h00800000"}, // smallest   normalized:  1.18e-38
        {"float8x23", Float.intBitsToFloat(0x007fffff),  "32'h007fffff"}, // largest  denormalized:  1.18e-38
        {"float8x23", Float.intBitsToFloat(0x00000001),  "32'h00000001"}, // smallest denormalized:  1.40e-45
        {"float8x23",     1e-46,                         "32'h00000000"}, // underflow 
        {"float8x23", Float.NaN,                         "32'h7fffffff"}, // NaN 
        
        {"float3x6",      0l,        "10'h000"},
        {"float3x6",      1l,        "10'h0c0"},
        {"float3x6",      2l,        "10'h100"},
        {"float3x6",      3l,        "10'h120"},
        {"float3x6",      4l,        "10'h140"},
        {"float3x6",      5l,        "10'h150"},
        {"float3x6",      6l,        "10'h160"},
        {"float3x6",      7l,        "10'h170"},
        {"float3x6",      8l,        "10'h180"},
        {"float3x6",     14l,        "10'h1b0"},
        {"float3x6",     15l,        "10'h1b8"},
        {"float3x6",     15.6875,    "10'h1be"},      // round to nearest, tie to even
        {"float3x6",     15.8125,    "10'h1be"},      // round to nearest, tie to even
        {"float3x6",     15.875,     "10'h1bf"},      // largest before +inf 
        {"float3x6",     15.9375,    "10'h1c0"},      // round to overflow
        {"float3x6",     16l,        "10'h1c0"},      // +infinity
        {"float3x6",     17l,        "10'h1c0"},      // +infinity
        {"float3x6",     -1l,        "10'h2c0"},
        {"float3x6",     -2l,        "10'h300"},
        {"float3x6",    -14l,        "10'h3b0"},
        {"float3x6",    -15l,        "10'h3b8"},
        {"float3x6",    -15.875,     "10'h3bf"},      // smallest before -inf
        {"float3x6",    -15.9375,    "10'h3c0"},      // round to nearest, tie to even
        {"float3x6",    -15.9376,    "10'h3c0"},      // round to overflow
        {"float3x6",    -16l,        "10'h3c0"},      // -infinity
        {"float3x6",      1.4,       "10'h0da"},      // 1.011010 * 2^0  => e=0,  m=1a
        {"float3x6",      1.5,       "10'h0e0"},      // 1.100000 * 2^0  => e=0,  m=20
        {"float3x6",      1.6,       "10'h0e6"},      // 1.100110 * 2^0  => e=0,  m=26
        {"float3x6",      0.25,      "10'h040"},      // 1.000000 * 2^-2 => e=-2, m=00  (smallest   normalized)
        {"float3x6",      0.24609375,"10'h03f"},      // 0.111111 * 2^-2 => e=-2, m=3f  (largest  denormalized)
        {"float3x6",      0.00390625,"10'h001"},      // 0.000001 * 2^-2 => e=-2, m=01  (smallest denormalized)
        {"float3x6",      0.0019,    "10'h000"},      // underflow
        {"float3x6",     -1.4,       "10'h2da"},
        {"float3x6",     -1.5,       "10'h2e0"},
        {"float3x6",     -1.6,       "10'h2e6"},
        {"float3x6",     -0.25,      "10'h240"},
        {"float3x6",     -0.24609375,"10'h23f"},
        {"float3x6",     -0.00390625,"10'h201"},
        {"float3x6",     -0.0019,    "10'h200"},
        {"float3x6", Double.NaN,     "10'h1ff"},      // NaN
      
      }));
      return res;
    }
    
    /**
     * Canonical {@code Format} description
     */
    @Parameter(0)
    public String format;
    
    /**
     * Input to parse
     */
    @Parameter(1)
    public Number value;
    
    /**
     * Expected hex literal
     */
    @Parameter(2)
    public String literal;
    
    @Test 
    public void raw() {
      assertThat(BigNumber.getHexLiteral(Format.parse(format).getRawBinary(value)), equalToIgnoringCase(literal));
    }
    
  }
  
  /**
   * Test
   * <ul>
   *   <li> {@link BigNumber#parse}
   *   <li> {@link BigNumber#getValue}
   *   <li> {@link Format#getValue}
   *   <li> {@link Format#getRawBinary}
   * </ul>
   * @author Andreas Engel [engel@esa.cs.tu-darmstadt.de]
   */
  @RunWith(Parameterized.class)
  public static class RawTo {
    
    /**
     * Generate test vector.
     * @return  test vector
     */
    @Parameters(name= "{0},{1}")
    public static Object[][] data() {
      return new Object[][] {
        {"uint10",   "10'h000",      0.0                },
        {"uint10",   "10'h001",      1.0                },
        {"uint10",   "10'h002",      2.0                },
        {"uint10",   "10'h1FF",    511.0                },
        {"uint10",   "10'h200",    512.0                },
        {"uint10",   "10'h3fe",   1022.0                },
        {"uint10",   "10'h3ff",   1023.0                },
                                 
        { "int10",   "10'h000",      0.0                },
        { "int10",   "10'h001",      1.0                },
        { "int10",   "10'h002",      2.0                },
        { "int10",   "10'h1FF",    511.0                },
        { "int10",   "10'h200",   -512.0                },
        { "int10",   "10'h3fe",     -2.0                },
        { "int10",   "10'h3ff",     -1.0                }, 
        
        {"ufix4x6",  "10'h000",      0.0                },
        {"ufix4x6",  "10'h001",      0.015625           }, 
        {"ufix4x6",  "10'h002",      0.03125            },
        {"ufix4x6",  "10'h01F",      0.484375           }, 
        {"ufix4x6",  "10'h020",      0.5                },
        {"ufix4x6",  "10'h1FF",      7.984375           }, 
        {"ufix4x6",  "10'h200",      8.0                },
        {"ufix4x6",  "10'h3fe",     15.96875            },
        {"ufix4x6",  "10'h3ff",     15.984375           },
        
        { "fix4x6",  "10'h000",      0.0                },
        { "fix4x6",  "10'h001",      0.015625           }, 
        { "fix4x6",  "10'h002",      0.03125            },
        { "fix4x6",  "10'h01F",      0.484375           }, 
        { "fix4x6",  "10'h020",      0.5                },
        { "fix4x6",  "10'h1FF",      7.984375           }, 
        { "fix4x6",  "10'h200",     -8.0                },
        { "fix4x6",  "10'h3fe",     -0.03125            },
        { "fix4x6",  "10'h3ff",     -0.015625           },
        
        {"float3x6", "10'h000",      0.0                },
        {"float3x6", "10'h0c0",      1.0                },
        {"float3x6", "10'h100",      2.0                },
        {"float3x6", "10'h120",      3.0                },
        {"float3x6", "10'h140",      4.0                },
        {"float3x6", "10'h150",      5.0                },
        {"float3x6", "10'h160",      6.0                },
        {"float3x6", "10'h170",      7.0                },
        {"float3x6", "10'h180",      8.0                },
        {"float3x6", "10'h1b0",     14.0                },
        {"float3x6", "10'h1b8",     15.0                },
        {"float3x6", "10'h1bf",     15.875              },
        {"float3x6", "10'h1c0", Double.POSITIVE_INFINITY},
        {"float3x6", "10'h2c0",     -1.0                },
        {"float3x6", "10'h300",     -2.0                },
        {"float3x6", "10'h3b0",    -14.0                },
        {"float3x6", "10'h3b8",    -15.0                },
        {"float3x6", "10'h3bf",    -15.875              },
        {"float3x6", "10'h3c0", Double.NEGATIVE_INFINITY},
        {"float3x6", "10'h0da",      1.40625            },
        {"float3x6", "10'h0e0",      1.5                },
        {"float3x6", "10'h0e6",      1.59375            },
        {"float3x6", "10'h040",      0.25               },  // smallest normalized
        {"float3x6", "10'h03f",      0.24609375         },  // largest  denormalized
        {"float3x6", "10'h001",      0.00390625         },  // smallest denormalized
        {"float3x6", "10'h000",      0.0                },  // underflow
        {"float3x6", "10'h2da",     -1.40625            },
        {"float3x6", "10'h2e0",     -1.5                },
        {"float3x6", "10'h2e6",     -1.59375            },
        {"float3x6", "10'h240",     -0.25               },
        {"float3x6", "10'h23f",     -0.24609375         },
        {"float3x6", "10'h201",     -0.00390625         },
        {"float3x6", "10'h200",     -0.0                },
        {"float3x6", "10'h1ff", Double.NaN              },
      };
    }
    
    /**
     * Canonical {@code Format} description
     */
    @Parameter(0)
    public String format;
    
    /**
     * Raw data to interpret
     */
    @Parameter(1)
    public String literal;
    
    /**
     * Expected Double value
     */
    @Parameter(2)
    public double expected;
    
    private BigNumber uut;
    
    @Before
    public void init() {
      uut = BigNumber.parse(Format.parse(format), literal);
    }
    
    @Test public void intValue()    {assertThat(uut.intValue(),    equalTo((int)   Math.round((float) expected)));}
    @Test public void longValue()   {assertThat(uut.longValue(),   equalTo((long)  Math.round(expected)));}
    @Test public void floatValue()  {assertThat(uut.floatValue(),  equalTo((float) expected));}
    @Test public void doubleValue() {assertThat(uut.doubleValue(), equalTo(        expected));}
  }
  
  /**
   * Test
   * <ul>
   *   <li> {@link BigNumber#parse}
   *   <li> {@link BigNumber#getValue}
   *   <li> {@link Format#getValue}
   *   <li> {@link Format#getRawBinary}
   * </ul>
   * @author Andreas Engel [engel@esa.cs.tu-darmstadt.de]
   */
  public static class Arithmetic {
    
    private static final Format F1 = Format.parse("uint10");
    private static final Format F2 = Format.parse("int20");
    private static final Format F3 = Format.parse("ufix22x10");
    private static final Format F4 = Format.parse("fix15x5");
    private static final Format F5 = Format.parse("float8x23");
    private static final Format F6 = Format.parse("float10x10");
    private static final int    N  = 20;
    private static final BigNumber[] SPECIAL = new BigNumber[] {
        BigNumber.NaN,
        BigNumber.POSITIVE_INFINITY,
        BigNumber.NEGATIVE_INFINITY,
        BigNumber.POSITIVE_ZERO,
        BigNumber.NEGATIVE_ZERO
    };
    
    private <T extends Number> void apply(Implementation imp, Function<BigNumber, T> conv, BinaryOperator<BigNumber> special, BinaryOperator<T> ref) {
      
      // FixedPoint Numbers may differ in lsb, as the reference value is based on double
      Format      rf = imp.getResultFormat();
      double epsilon = ((rf instanceof Format.FixedPoint) &&
                       !(rf instanceof Format.Integer))   ? Math.pow(2, -((Format.FixedPoint) rf).getFractionBits()) : 0;
      
      for (int i=0; i<N; i++) for (int k=0; k<N; k++) {
        
        // special symbol or random input
        BigNumber na = i < SPECIAL.length && k <= SPECIAL.length ? SPECIAL[i] : imp.getOperandFormat(0).getRandomValue();
        BigNumber nb = k < SPECIAL.length && i <= SPECIAL.length ? SPECIAL[k] : imp.getOperandFormat(1).getRandomValue();
        
        // the implementation to be checked
        Number    nr = imp.apply(na, nb)[0];
        
        // what imp.apply() actually works on
        BigNumber qa = BigNumber.quantize(imp.getOperandFormat(0), na);
        BigNumber qb = BigNumber.quantize(imp.getOperandFormat(1), nb);
        
        // special symbol expected?
        boolean specialSymbol = false;
        Number ex = special.apply(qa, qb);
        
        // otherwise, compare to reference value
        if (ex == null) {
          T  a = conv.apply(qa);
          T  b = conv.apply(qb);
          nr   = conv.apply(BigNumber.cast(nr));
          ex   = conv.apply(BigNumber.quantize(imp.getResultFormat(), ref.apply(a, b)));
        } else {
          specialSymbol = true;
        }
        
        // common assertion message
        String msg = imp.getName() + "(" + qa + ", " + qb + ")";
        
        // the actual assertion
        if (specialSymbol || epsilon == 0) assertThat(msg, nr,               equalTo(ex));
        else                               assertThat(msg, nr.doubleValue(), closeTo(ex.doubleValue(), epsilon));
      }
    }
    
    private BinaryOperator<BigNumber> add_special = (a, b) -> {
      return  (a.isNaN()      || b.isNaN())      ? BigNumber.NaN                                    :
              (a.isInfinite() || b.isInfinite()) ? (a.getSign() == b.getSign() ? a : BigNumber.NaN) :
              null;
    };
    @Test public void add_uint ()  {apply(new ADD(F1,F1,F1), n -> n.longValue(),   add_special, (a,b) -> a+b);}
    @Test public void add_int  ()  {apply(new ADD(F2,F2,F2), n -> n.longValue(),   add_special, (a,b) -> a+b);}
    @Test public void add_ufix ()  {apply(new ADD(F3,F3,F3), n -> n.doubleValue(), add_special, (a,b) -> a+b);}
    @Test public void add_fix  ()  {apply(new ADD(F4,F4,F4), n -> n.doubleValue(), add_special, (a,b) -> a+b);}
    @Test public void add_float()  {apply(new ADD(F5,F5,F5), n -> n.doubleValue(), add_special, (a,b) -> a+b);}
    @Test public void add_mfloat() {apply(new ADD(F6,F6,F6), n -> n.doubleValue(), add_special, (a,b) -> a+b);}
    
    private BinaryOperator<BigNumber> sub_special = (a, b) -> {
      return  (a.isNaN()      || b.isNaN())      ? BigNumber.NaN                                    :
              (a.isInfinite() || b.isInfinite()) ? (a.getSign() != b.getSign() ? a : BigNumber.NaN) :
              null;
    };
    @Test public void sub_uint ()  {apply(new SUB(F1,F1,F1), n -> n.longValue(),   sub_special, (a,b) -> a-b);}
    @Test public void sub_int  ()  {apply(new SUB(F2,F2,F2), n -> n.longValue(),   sub_special, (a,b) -> a-b);}
    @Test public void sub_ufix ()  {apply(new SUB(F3,F3,F3), n -> n.doubleValue(), sub_special, (a,b) -> a-b);}
    @Test public void sub_fix  ()  {apply(new SUB(F4,F4,F4), n -> n.doubleValue(), sub_special, (a,b) -> a-b);}
    @Test public void sub_float()  {apply(new SUB(F5,F5,F5), n -> n.doubleValue(), sub_special, (a,b) -> a-b);}
    @Test public void sub_mfloat() {apply(new SUB(F6,F6,F6), n -> n.doubleValue(), sub_special, (a,b) -> a-b);}
    
    private BinaryOperator<BigNumber> mul_special = (a, b) -> {
      boolean sign = a.getSign() ^ b.getSign();
      return  (a.isNaN() 
           ||  b.isNaN() 
           || (a.isInfinite() && b.isZero()) 
           || (b.isInfinite() && a.isZero()))    ? BigNumber.NaN                                                      :
              (a.isInfinite() || b.isInfinite()) ? (sign ? BigNumber.NEGATIVE_INFINITY : BigNumber.POSITIVE_INFINITY) :
              (a.isZero()     || b.isZero())     ? (sign ? BigNumber.NEGATIVE_ZERO     : BigNumber.POSITIVE_ZERO)     :
              null;
    };
    @Test public void mul_uint ()  {apply(new MUL(F1,F1,F1), n -> n.longValue(),   mul_special, (a,b) -> a*b);}
    @Test public void mul_int  ()  {apply(new MUL(F2,F2,F2), n -> n.longValue(),   mul_special, (a,b) -> a*b);}
    @Test public void mul_ufix ()  {apply(new MUL(F3,F3,F3), n -> n.doubleValue(), mul_special, (a,b) -> a*b);}
    @Test public void mul_fix  ()  {apply(new MUL(F4,F4,F4), n -> n.doubleValue(), mul_special, (a,b) -> a*b);}
    @Test public void mul_float()  {apply(new MUL(F5,F5,F5), n -> n.doubleValue(), mul_special, (a,b) -> a*b);}
    @Test public void mul_mfloat() {apply(new MUL(F6,F6,F6), n -> n.doubleValue(), mul_special, (a,b) -> a*b);}

    private BinaryOperator<BigNumber> div_special = (a, b) -> {
      boolean sign = a.getSign() ^ b.getSign();
      return  (a.isNaN() 
           ||  b.isNaN() 
           || (a.isInfinite() && b.isInfinite()) 
           || (a.isZero() && b.isZero()))          ? BigNumber.NaN                                                      :
              (a.isZero() || b.isInfinite())       ? (sign ? BigNumber.NEGATIVE_ZERO     : BigNumber.POSITIVE_ZERO)     :
              (b.isZero() || a.isInfinite())       ? (sign ? BigNumber.NEGATIVE_INFINITY : BigNumber.POSITIVE_INFINITY) :
              null;
    };
    @Test public void div_uint ()  {apply(new DIV(F1,F1,F1), n -> n.longValue(),   div_special, (a,b) -> a/b);}
    @Test public void div_int  ()  {apply(new DIV(F2,F2,F2), n -> n.longValue(),   div_special, (a,b) -> a/b);}
    @Test public void div_ufix ()  {apply(new DIV(F3,F3,F3), n -> n.doubleValue(), div_special, (a,b) -> a/b);}
    @Test public void div_fix  ()  {apply(new DIV(F4,F4,F4), n -> n.doubleValue(), div_special, (a,b) -> a/b);}
    @Test public void div_float()  {apply(new DIV(F5,F5,F5), n -> n.doubleValue(), div_special, (a,b) -> a/b);}
    @Test public void div_mfloat() {apply(new DIV(F6,F6,F6), n -> n.doubleValue(), div_special, (a,b) -> a/b);}
  }
  
  public static void main(String[] args) {
    Result result = JUnitCore.runClasses(BigNumberTest.class);
    
    System.out.println(result.getFailureCount() + " failures detected");
    for (Failure failure : result.getFailures()) {
      System.out.println(failure.toString());
    }
  }

}

/*
 * Copyright (c) 2017,
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