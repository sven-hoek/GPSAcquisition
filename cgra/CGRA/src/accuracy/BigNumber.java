package accuracy;

import operator.ADD;
import operator.Implementation;

public class BigNumber extends Number {
  
  /** Unique ID for (de)marshalling */
  private static final long serialVersionUID = -6206948689796133773L;
  
  public static final BigNumber NaN               = new BigNumber(false, Integer.MIN_VALUE, new boolean[0]);
  public static final BigNumber POSITIVE_INFINITY = new BigNumber(false, Integer.MAX_VALUE, new boolean[0]);
  public static final BigNumber NEGATIVE_INFINITY = new BigNumber(true,  Integer.MAX_VALUE, new boolean[0]);
  public static final BigNumber POSITIVE_ZERO     = new BigNumber(false, 0,                 new boolean[1]);
  public static final BigNumber NEGATIVE_ZERO     = new BigNumber(true,  0,                 new boolean[1]);
  
  private boolean   sign;
  private int       exponent;
  private boolean[] mantissa;
  
  public BigNumber(boolean sign, int exponent, boolean[] mantissa) {
    this.sign     = sign;
    this.exponent = exponent;
    this.mantissa = mantissa;
  }
  
  public BigNumber(boolean sign, int exponent, String mantissa) {
    this(sign, exponent, parse(mantissa));
  }
  
  public static BigNumber cast(Number n) {
    
    Format f;
    long raw;
    
    if (n instanceof BigNumber) {
      return (BigNumber) n;
    
    // extract long
    } else if (n instanceof java.lang.Byte    || 
               n instanceof java.lang.Short   || 
               n instanceof java.lang.Integer || 
               n instanceof java.lang.Long) {

      f   = Format.LONG;
      raw = n.longValue();
      
    // extract double
    } else if (n instanceof Float || 
               n instanceof Double) {
      
      f   = Format.DOUBLE;
      raw = Double.doubleToLongBits(n.doubleValue());
            
    } else throw new IllegalArgumentException("can not cast " + n.getClass() + " to BigNumber");
    
    
    byte[]    b = Long.toBinaryString(raw).getBytes();
    boolean[] r = new boolean[f.getBitWidth()];
    for (int i=0; i<r.length; i++) r[i] = i < b.length ? b[b.length-1-i] == '1' : false;
    return f.getValue(r);
   
  }
  
  /**
   * @param f
   * @param n
   * @return
   */
  public static BigNumber quantize(Format f, Number n) {
    return f.getValue(f.getRawBinary(n));
  }
  
  private static boolean[] parse(String literal) {
    String[] s = literal.split("'");
    int bits = Integer.parseInt(s[0]);
    
    char[] b = s[1].toCharArray();
    boolean[] raw = new boolean[bits]; 
    if (b[0] == 'b') {
      for (int i=0; i<raw.length; i++) raw[i] = (i < b.length-1) && b[b.length-1-i] == '1';
    } else {
      byte[] nibble = null;
      for (int i=0; i<raw.length; i++) {
        if (i % 4 == 0) {
          int v = (i/4 < b.length-1) ? Integer.parseInt(Character.toString(b[b.length-1-i/4]), 16) : 0;
          nibble = String.format("%4s", Integer.toBinaryString(v)).replace(' ', '0').getBytes();
        }
        raw[i] = nibble[3-(i%4)] == '1';
      }
    }
    return raw;
  }
  
  public static BigNumber parse(Format format, String literal) {
    boolean[] raw = parse(literal); 
    if (raw.length != format.getBitWidth()) throw new IllegalArgumentException("literal length must match format size");
    return format.getValue(raw);
  }
  
  public boolean getSign() {
    return sign;
  }
  
  public int getExponent() {
    return exponent;
  }
  
  public boolean[] getMantissa() {
    return mantissa;
  }
  
  public boolean getMantissaBit(int i) {
    return i >= 0 && i < mantissa.length && mantissa[i];
  }
  
  public boolean isInfinite() {
    return exponent == Integer.MAX_VALUE;
  }
  
  public boolean isNaN() {
    return exponent == Integer.MIN_VALUE;
  }
  
  public boolean isZero() {
    if (isInfinite() || isNaN())      return false;
    for (boolean b : mantissa) if (b) return false;
    return true;
  }
  
  /**
   * TODO: equalize exponent before comparison
   */
  @Override
  public boolean equals(Object other) {
    if (!(other instanceof BigNumber)) return false;
    BigNumber that = (BigNumber) other;
    if (this.isNaN())                   return that.isNaN();
    if (that.isNaN())                   return this.isNaN();
    if (this.isZero())                  return that.isZero();
    if (that.isZero())                  return this.isZero();
    if (this.sign     != that.sign)     return false;
    if (this.isInfinite())              return that.isInfinite();
    if (that.isInfinite())              return this.isInfinite();
    
    // adjust exponent before comparing mantissa
    int emin  = Math.min(this.exponent, that.exponent);
    int dThis = this.exponent - emin;
    int dThat = that.exponent - emin;
    
    for (int i=0; i<Math.max(this.mantissa.length+dThis, that.mantissa.length+dThat); i++) {
      if (this.getMantissaBit(i-dThis) != that.getMantissaBit(i-dThat)) return false;
    }
    return true;
  }
  
  @Override
  public String toString() {
    
    // check special symbols
    if (this.equals(NaN))               return "NaN";
    if (this.equals(POSITIVE_INFINITY)) return "+Infinity";
    if (this.equals(NEGATIVE_INFINITY)) return "-Infinity";
    if (this.equals(POSITIVE_ZERO))     return "+Zero";
    if (this.equals(NEGATIVE_ZERO))     return "-Zero";
    
    return (sign ? "-" : "+") 
         + getHexLiteral(mantissa) 
         + " * 2^" + exponent 
         + " [" + longValue() + ", " + doubleValue() + "]"; 
  }
  
  public String toString(Format f) {
    return f instanceof Format.Integer ? Long.toString(longValue()) : Double.toString(doubleValue());
  }
  
  private static void assertAccess(boolean[] raw, int highBit, int lowBit) {
    if (highBit >= raw.length ) throw new IndexOutOfBoundsException("high bit");
    if (highBit < lowBit)       throw new IllegalArgumentException ("empty bit range");
    if (lowBit  < 0)            throw new IndexOutOfBoundsException("low bit");
  }
  
  public static String getBinLiteral(boolean[] raw, int highBit, int lowBit) {
    assertAccess(raw, highBit, lowBit);
    StringBuilder res = new StringBuilder((highBit-lowBit+1) + "'b");
    for (int i=highBit; i >= lowBit; i--) res.append(raw[i] ? "1" : "0");
    return res.toString();
  }
  
  public static String getBinLiteral(boolean[] raw) {
    return getBinLiteral(raw, raw.length-1, 0);
  }
  
  public static String getHexLiteral(boolean[] raw, int highBit, int lowBit) {
    assertAccess(raw, highBit, lowBit);
    StringBuilder res = new StringBuilder();
    int sum    = 0;
    int weight = 1;
    for (int i = lowBit; i <= highBit; i++) {
      if (raw[i]) sum += weight;
      weight *= 2;
      if (weight == 16 || i == highBit) {
        res.append(Integer.toHexString(sum));
        sum    = 0;
        weight = 1;
      }
    }
    return (highBit + 1 - lowBit) + "'h" + res.reverse();
  }
  
  public static String getHexLiteral(boolean[] raw) {
    return getHexLiteral(raw, raw.length-1, 0);
  } 
  
  protected Number getValue(Format f) {
    boolean[] raw = f.getRawBinary(this);
    long sum      = 0;
    for (int i=0; i<raw.length; i++) if (raw[i]) sum += (sign && i==raw.length-1) ? -(1L << i) : 1L<<i;
    
    return f == Format.FLOAT  ? Float.intBitsToFloat((int) sum) :
           f == Format.DOUBLE ? Double.longBitsToDouble(   sum) : 
           sum;
  }
  
  @Override
  public int intValue() {
    return getValue(Format.INT).intValue();
  }

  @Override
  public long longValue() {
    return getValue(Format.LONG).longValue();
  }

  @Override
  public float floatValue() {
//    return (float) doubleValue(); 
    return getValue(Format.FLOAT).floatValue();
  }
  
  @Override
  public double doubleValue() {
    return getValue(Format.DOUBLE).doubleValue();
  }
  
  public long getUnscaledMantissa() {
    long sum = 0;
    for (int i=0; i<mantissa.length; i++) if (mantissa[i]) sum += (sign && i==mantissa.length-1) ? -(1L << i) : 1L<<i;
    return sum;
  }
  
  public static void main(String[] args) {
    Format f = Format.FLOAT;
    Implementation imp = new ADD(f,f,f);
    System.out.println(BigNumber.parse(f, "32'h1337f883"));
    System.out.println(imp.apply(BigNumber.parse(f, "32'h1337f883"), BigNumber.parse(f, "32'h12225d1e"))[0]);
    
//    System.out.println(BigNumber.cast(Double.POSITIVE_INFINITY).equals(new BigNumber(false, Integer.MAX_VALUE, new boolean[0])));
//    System.out.println(BigNumber.parse(Format.parse("uint10"), "10'h001").floatValue());
//    System.out.println(BigNumber.getHexLiteral(Format.parse("float3x6").getRawBinary(-15.875)));
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