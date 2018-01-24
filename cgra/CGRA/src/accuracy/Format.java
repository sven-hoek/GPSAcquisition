package accuracy;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import operator.Operator;

/**
 * Representation of the arithmetic accuracy of {@code Operator} inputs or
 * outputs.
 * <p>
 * Used for {@link Operator} {@link Implementation} configuration, generation of
 * Verilog code and numeric conversion (quantization).
 * <p>
 * {@link Formats} are immutable after creation. To change the I/O precision of
 * an {@link Operator} {@link Implementation}, a new {@link Format} instance
 * musb be created.
 *
 * @author Andreas Engel [engel@esa.cs.tu-darmstadt.de]
 */
public abstract class Format implements Serializable {

  /** Unique ID for (de)marshalling */
  private static final long serialVersionUID = 970484228524144677L;
  
  /** Random number generator backend for all {@code Format}s */
  protected static final Random RANDOM = new Random();

  /** Representation of {@link java.lang.Integer} */
  public static final Integer INT = new Integer(java.lang.Integer.SIZE);

  /** Representation of {@link java.lang.Long} */
  public static final Integer LONG = new Integer(java.lang.Long.SIZE);

  /** Representation of {@link java.lang.Float} (IEEE 754 single precision) */
  public static final FloatingPoint FLOAT = new FloatingPoint(8, 23);

  /** Representation of {@link java.lang.Double} (IEEE 754 double precision) */
  public static final FloatingPoint DOUBLE = new FloatingPoint(11, 52);

  /**
   * Generate format from canonical name. The following formats are supported:
   * <ul>
   * <li>bool                - for {@link Boolean#INSTANCE}
   * <li>int                 - for {@link #INT}
   * <li>long                - for {@link #LONG}
   * <li>ieee754sp or float  - for {@link #FLOAT}
   * <li>ieee754dp or double - for {@link #DOUBLE}
   * <li>int(b)              - for (b) bit signed {@link Integer}
   * <li>uint(b)             - for (b) bit unsigned {@link Integer}
   * <li>fix(i)x(f)          - for signed {@link FixedPoint} with (i) integer and (f) fraction bits
   * <li>ufix(i)x(f)         - for unsigned {@link FixedPoint} with (i) integer and (f) fraction bits
   * <li>float(m)x(e)        - for signed {@link FloatingPoint} with (m) mantissa and (e) exponent bits
   * <ul>
   *
   * @param s the canonical name to parse
   * @return the corresponding {@link Format} or null, if no valid name was
   *         specified
   * @see #getCanonicalName
   */
  public static Format parse(String s) {
    if (s == null) throw new IllegalArgumentException("canonical name missing");
    
    if (s.equals("bool"))                            return Boolean.INSTANCE;
    if (s.equals("int"))                             return INT;
    if (s.equals("long"))                            return LONG;
    if (s.equals("ieee754sp") || s.equals("float"))  return FLOAT;
    if (s.equals("ieee754dp") || s.equals("double")) return DOUBLE;
    
    Pattern p = Pattern.compile("raw(\\d+)");
    Matcher m = p.matcher(s);
    if (m.matches()) return new Raw(java.lang.Integer.parseInt(m.group(1)));
    
    p = Pattern.compile("u?int(\\d+)");
    m = p.matcher(s);
    if (m.matches()) return new Integer(java.lang.Integer.parseInt(m.group(1)), !s.startsWith("u"));
    
    p = Pattern.compile("(u?fix|float)(\\d+)x(\\d+)");
    m = p.matcher(s);
    if (m.matches()) {
      String t = m.group(1);
      int a = java.lang.Integer.parseInt(m.group(2));
      int b = java.lang.Integer.parseInt(m.group(3));
      
      if (t.endsWith("fix")) return new FixedPoint   (a, b, !t.startsWith("u"));
      if (t.equals("float")) return new FloatingPoint(a, b);
    }
    

  throw new IllegalArgumentException("can not parse " + s);
  }

  /**
   * Overall number of bits required to represent this {@code Format}.
   *
   * @return bit width
   */
  public abstract int getBitWidth();

  /**
   * Check, whether this {@code Format} can represent negative numbers
   *
   * @return true, if signed numbers are supported
   */
  public boolean isSigned() {
    return true;
  }

  /**
   * Name reflecting type and accuracy of this {@code Format}.
   *
   * @return canonical name
   */
  public abstract String getCanonicalName();

  @Override
  public String toString() {
    return getCanonicalName();
  }

  /**
   * Canonical Name must be sufficient for comparing two {@code Format}s
   */
  @Override
  public boolean equals(Object other) {
    if (other == null || !(other instanceof Format)) {
      return false;
    }
    return getCanonicalName().equals(((Format) other).getCanonicalName());
  }
  
  public boolean[] getRawBinary(Number n) {
    return getRawBinary(BigNumber.cast(n));
  }
  
  public abstract boolean[] getRawBinary(BigNumber n);
  
  public BigNumber getValue(boolean[] raw) {
    if (raw.length != getBitWidth()) throw new IllegalArgumentException("raw length does not match format");
    return null;
  }
  
  /**
   * Generate a random value from the possible numeric range represented by
   * this {@code Format}.
   *
   * @return random value
   */
  public BigNumber getRandomValue() {
    boolean[] raw = new boolean[getBitWidth()];
    for (int i=0; i<raw.length; i++) raw[i] = RANDOM.nextBoolean();
    return getValue(raw);
  }
  
  /**
   * Get appropriate Modelsim display format.
   *
   * @return appropriate identifier for TCL command
   */
  public String getWaveRadix() {
    return "symbolic";
  }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /**
   * Raw data.
   * 
   * Not associated with arithmetic interpretations
   */
  public static class Raw extends Integer {

    /** Unique ID for (de)marshalling */
    private static final long serialVersionUID = 7754858063362770093L;
    
    /**
     * {@code Boolean} is a singleton.
     */
    public Raw(int width) {
      super(width, false);
    }

    @Override
    public String getCanonicalName() {
      return "raw" + getBitWidth();
    }

    @Override
    public String getWaveRadix() {
      return "binary";
    }

  }
  
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Boolean data.
   */
  public static class Boolean extends Integer {

    /** Unique ID for (de)marshalling */
    private static final long serialVersionUID = 8193512124972418672L;
    /**
     * {@code Boolean} {@code Format} is not configurable, so only one
     * instance is needed
     */
    public static final Boolean INSTANCE = new Boolean();

    /**
     * {@code Boolean} is a singleton.
     */
    protected Boolean() {
      super(1, false);
    }

    @Override
    public String getCanonicalName() {
      return "bool";
    }

    @Override
    public String getWaveRadix() {
      return "binary";
    }

  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Signed or unsigned {@code Integer}s of arbitrary width without fraction.
   */
  public static class Integer extends FixedPoint {

    /** Unique ID for (de)marshalling */
    private static final long serialVersionUID = -4987572669850576501L;
    
    /**
     * Generate new {@code Integer}.
     *
     * @param width   overall bit width
     * @param signed  true, if negative values can be represented
     * @throws        IllegalArgumentException if {@code width < 1}
     */
    public Integer(int width, boolean signed) {
      super(width, 0, signed);
    }


    /**
     * Generate new signed {@code Integer}.
     *
     * @param width  overall bit width
     * @throws       IllegalArgumentException if {@code width <= 0}
     */
    public Integer(int width) {
      this(width, true);
    }

    @Override
    public String getCanonicalName() {
      return (isSigned() ? "" : "u") + "int" + getBitWidth();
    }
    
    @Override
    public String getWaveRadix() {
      return isSigned() ? "decimal" : "unsigned";
    }
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Signed or unsigned integers of arbitrary width with fraction.
   * <p>
   * Each instance of this {@link Format} must have a non-zero fraction width
   * to avoid duplication with the {@link Integer} {@link Format}.
   */
  public static class FixedPoint extends Format {

    /** Unique ID for (de)marshalling */
    private static final long serialVersionUID = 1062454188323136145L;

    /** Number of bits before decimal point */
    private int integerBits;

    /** Number of bits after decimal point */
    private int fractionBits;

    /** flag indicating, that negative values can be represented */
    private boolean signed;

    /**
     * Generate new {@code FixedPoint}.
     *
     * @param integerBits  the number of bits before decimal point
     * @param fractionBits the number of bits after decimal point
     * @param signed       true, if negative values can be represented
     * @throws             IllegalArgumentException if {@code integerBits              < 0}
     * @throws             IllegalArgumentException if {@code fractionBits             < 0}
     * @throws             IllegalArgumentException if {@code integerBits+fractionBits < 1}
     */
    public FixedPoint(int integerBits, int fractionBits, boolean signed) {
      this.integerBits  = integerBits;
      this.fractionBits = fractionBits;
      this.signed       = signed;
      
      if (integerBits   < 0) throw new IllegalArgumentException("invalid integerBits: "  + integerBits);
      if (fractionBits  < 0) throw new IllegalArgumentException("invalid fractionBits: " + fractionBits);
      if (getBitWidth() < 1) throw new IllegalArgumentException("invalid width: "        + getBitWidth());
    }

    /**
     * Generate new signed {@code FixedPoint}.
     *
     * @param integerBits  the number of bits before decimal point
     * @param fractionBits the number of bits after decimal point
     * @throws IllegalArgumentException if {@code integerBits  <= 0}
     * @throws IllegalArgumentException if {@code fractionBits <= 1}
     */
    public FixedPoint(int integerBits, int fractionBits) {
      this(integerBits, fractionBits, true);
    }

    @Override
    public int getBitWidth() {
      return integerBits + fractionBits;
    }
    
    public int getIntegerBits() {
      return integerBits;
    }
    
    public int getFractionBits() {
      return fractionBits;
    }

    @Override
    public boolean isSigned() {
      return signed;
    }

    @Override
    public String getCanonicalName() {
      return (signed ? "" : "u") + "fix" + integerBits + "x" + fractionBits;
    }
    
    @Override
    public boolean[] getRawBinary(BigNumber n) {
      boolean[] raw = new boolean[getBitWidth()];
      
      // +inf => max value, -inf => min value
      if (n.isInfinite()) {
        for (int i=0; i<raw.length; i++) raw[i] = (i < raw.length-1) ^ n.getSign();
        return raw;
      }
      
      // NaN => 0
      if (n.isNaN()) {
        for (int i=0; i<raw.length; i++) raw[i] = false;
        return raw;
      }
      
      // copy mantissa to raw at correct position
      for (int i=0; i<raw.length; i++) raw[i] = n.getMantissaBit(i-fractionBits-n.getExponent());
      
      // round to nearest (tie to +inf, as java.lang.Math.round does to generate integers) 
      // i.e., round up if |tail| > 0.5 LSB (guard and sticky) or tail = 0.5 LSB (guard and positive)
      boolean guard  = n.getMantissaBit(-fractionBits-n.getExponent()-1);
      boolean sticky = false;
      for (int i=0; i<-fractionBits-n.getExponent()-2; i++) if (n.getMantissaBit(i)) sticky = true;
      if (guard && (sticky || !n.getSign())) {
        for (int i=0; i<raw.length; i++) {
          raw[i] = !raw[i];
          if (raw[i]) break;
        }
      }
      
      // invert, if negative
      if (n.getSign()) {
        boolean carry = true;
        for (int i=0; i< raw.length; i++) {
          raw[i] ^= !carry;
          if (raw[i]) carry = false;
        }
      }

      return raw;
    }
    
    @Override
    public BigNumber getValue(boolean[] raw) {
      super.getValue(raw);
      
      boolean[] mantissa = new boolean[raw.length];
      boolean sign       = isSigned() && raw[raw.length-1];
      
      // copy whole number (invert, if raw is negative)
      boolean carry = sign;
      for (int i=0; i< raw.length; i++) {
        mantissa[i] = sign ^ carry ^ raw[i];
        if (raw[i]) carry = false;
      }
      
      return new BigNumber(sign, -fractionBits, mantissa);
    }

    @Override
    public String getWaveRadix() {
      return (fractionBits != 0) ? "fpoint#" + fractionBits : (signed ? "decimal" : "unsigned");
    }
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Floating point {@code Format}.
   * <p>
   * This {@link Format} is not restricted to IEEE 754 instances. All
   * {@link FloatingPoint} {@link Format}s represent signed values.
   */
  public static class FloatingPoint extends Format {

    /** Unique ID for (de)marshalling */
    private static final long serialVersionUID = -3390552931354608703L;

    /** Size of mantissa */
    private int mantissaBits;

    /** Size of exponent */
    private int exponentBits;

    /**
     * Generate a new {@code FloatingPoint}
     *
     * @param exponentBits the exponent size
     * @param mantissaBits the mantissa size
     * @throws IllegalArgumentException if {@code exponentBits < 1}
     * @throws IllegalArgumentException if {@code mantissaBits < 0}
     */
    public FloatingPoint(int exponentBits, int mantissaBits) {
      this.exponentBits = exponentBits;
      this.mantissaBits = mantissaBits;
      
      if (exponentBits  < 1) throw new IllegalArgumentException("invalid exponentBits: " + exponentBits);
      if (mantissaBits  < 1) throw new IllegalArgumentException("invalid mantissaBits: " + mantissaBits);
    }

    @Override
    public int getBitWidth() {
      return 1 + mantissaBits + exponentBits;
    }
    
    /**
     * @return the mantissa size
     */
    public int getMantissaBits() {
      return mantissaBits;
    }
    
    /**
     * @return the exponent size
     */
    public int getExponentBits() {
      return exponentBits;
    }
    
    public int getBias() {
      return (1 << (exponentBits-1))-1;
    }
    
    protected long getExponentMask() {
      return (1 << exponentBits)-1;
    }
    
    protected long getMantissaMask() {
      return (1L << mantissaBits)-1;
    }

    @Override
    public String getCanonicalName() {
      return "float" + exponentBits + "x" + mantissaBits;
    }
    
    private void makeInfinity(boolean[] raw) {
      for (int i=0; i<raw.length-1; i++) raw[i] = i >= mantissaBits;
    }
    
    @Override
    public boolean[] getRawBinary(BigNumber n) {
      boolean[] raw = new boolean[getBitWidth()];
      
      // NaN => special symbol
      if (n.isNaN()) {
        for (int i=0; i<raw.length; i++) raw[i] = i != raw.length-1;
        return raw;
      }
      
      // the sign
      raw[raw.length-1] = n.getSign();
      
      // +-inf => special symbol 
      if (n.isInfinite()) {
        makeInfinity(raw);
        return raw;
      }

      // copy mantissa to modifiable structure
      LinkedList<java.lang.Boolean> mbin = new LinkedList<java.lang.Boolean>();
      for (int i=0; i<n.getMantissa().length; i++) mbin.add(n.getMantissaBit(i));
      
      // remove leading zeros
      while (!mbin.isEmpty() && !mbin.getLast()) mbin.removeLast();
      
      // all zero => 0
      if (mbin.isEmpty()) {
        for (int i=0; i<raw.length-1; i++) raw[i] = false;
        return raw;
      }
      // normalize mantissa (adjust exp such that decimal point is right of most significant one bit)
      int exp = n.getExponent() + mbin.size() - 1; 
      
      // overflow => +-inf
      int bias = getBias();
      if (exp > bias) {
        makeInfinity(raw);
        return raw;
      }
      
      // exp to small => denormalize 
      if (exp < 1-bias) {
        int rshift = (int) (1-bias-exp);                       // number of leading zeros in mantissa
        for (int i=0; i<rshift; i++) mbin.addLast(false);      // important: do not drop bits required for rounding
        exp = -bias;                                           // will be biased to 0 
      }
      
      // not all bits fit into mantissa => round to NEAREST
      if (mbin.size()-1 > mantissaBits) {
        boolean lsb    = mbin.get(mbin.size()-1-mantissaBits);
        boolean guard  = mbin.get(mbin.size()-1-mantissaBits-1);
        boolean sticky = false;
        for (int i=0; i < mbin.size()-1-mantissaBits-1; i++) if (mbin.get(i)) sticky = true;
        

        // tie to +inf: round up if |tail| > 0.5 LSB (guard and sticky) or tail = 0.5 LSB (guard and positive)
        // if (guard && (sticky || !n.getSign())) {
          
        // tie to even: round up if |tail| > 0.5 LSB (guard and sticky) or tail = 0.5 LSB and body is odd (guard and lsb)
        if (guard && (sticky || lsb)) {
          boolean carry = true;
          for (int i=mbin.size()-1-mantissaBits; i<mbin.size(); i++) {
            mbin.set(i, !mbin.get(i));
            if (mbin.get(i)) {
              carry = false;
              break;
            }
          }
          // carry reached msb => increase exponent for normalization
          if (carry) {
            exp++;
            mbin.addLast(true);
          
            // now we may overflow again
            if (exp > bias) {
              makeInfinity(raw);
              return raw;
            }
          }
        }
      }
      
      // trailing zeros to move mantissa to decimal point
      int zeros = Math.max(mantissaBits-(mbin.size()-1), 0);
      for (int i=0; i<zeros; i++) raw[i] = false; 
        
      // the mbin part of the mantissa (skip rounded bits)
      int skip  = Math.max(mbin.size()-1-mantissaBits, 0);
      for (int i=0; i<mantissaBits-zeros; i++) raw[zeros+i] = mbin.get(skip+i);
      
      // the biased exponent zero expanded to required length
      byte[] ebin = java.lang.Integer.toBinaryString(exp + bias).getBytes();
      for (int i=0; i<exponentBits; i++) raw[mantissaBits+i] = i < ebin.length ? ebin[ebin.length-1-i] == '1' : false;
      
      return raw;
    }
    
    
    
    @Override
    public BigNumber getValue(boolean[] raw) {
      super.getValue(raw);
      
      // sign extraction
      boolean sign = raw[raw.length-1];
      
      // exponent extraction
      int bias = getBias();
      int exp = -bias;
      for (int i=0; i<exponentBits; i++) if (raw[mantissaBits+i]) exp += 1 << i;
      
      // mantissa extraction
      boolean   mantissaZero = true;
      boolean[] mantissa     = new boolean[mantissaBits+1];
      for (int i=0; i<mantissaBits; i++) {
        mantissa[i] = raw[i];
        if (raw[i]) mantissaZero = false;
      }
      
      // overflow or NaN ?
      if (exp > bias) {
        exp = mantissaZero ? java.lang.Integer.MAX_VALUE : java.lang.Integer.MIN_VALUE;
        mantissa[mantissaBits] = !mantissaZero;
        return new BigNumber(sign, exp, mantissa);
      
      // normalized mantissa ?
      } else if (exp > -bias) {
        mantissa[mantissaBits] = true;

      // denormalized mantissa
      } else {
        exp = 1-bias;
        mantissa[mantissaBits] = false;
      }
      
      return new BigNumber(sign, exp-mantissaBits, mantissa);
    }

  
  
  public BigNumber getNaN() {
  	  boolean[] raw = new boolean[getBitWidth()];
  	  for (int i=0; i<raw.length; i++) raw[i] = i != raw.length-1;
  	  return getValue(raw);
    }
    
    public BigNumber getInfinity(boolean pos){
  	  boolean[] raw = new boolean[getBitWidth()];
  	  for (int i=0; i<raw.length-1; i++) raw[i] = i >= mantissaBits;
  	  if(pos) raw[raw.length-1] = false; 
  	  else raw[raw.length-1] = true;
  	  return getValue(raw);
    }
    public BigNumber getZero(boolean pos){
    	boolean[] raw = new boolean[getBitWidth()];
    	for (int i=0; i<raw.length-2; i++) raw[i] = false;
    	if(pos) raw[raw.length-1] = false; 
    		else raw[raw.length-1] = true;
    	return getValue(raw);
    }
    
    
  }
}