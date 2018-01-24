package accuracy;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Range of possible values of an {@code Operator} used for bit width optimization.
 * 
 * Implementation according to Stolfi1997 (Self-Validated Numerical Methods and Applications)
 * 
 * @author Andreas Engel [engel@esa.cs.tu-darmstadt.de]
 */
public abstract class Range implements Cloneable {
  
  public static final Range EMPTY = new Range.IA(BigDecimal.ONE, BigDecimal.ZERO);
  public static final Range UNBOUNDED = new Range.IA(null, null);
  
  public static final MathContext FLOOR   = new MathContext(MathContext.DECIMAL64.getPrecision(), RoundingMode.FLOOR);
  public static final MathContext CEILING = new MathContext(MathContext.DECIMAL64.getPrecision(), RoundingMode.CEILING);

  
  @Override
public abstract Range clone();

  public abstract IA toIA();
  
  public abstract AA toAA();
  
  public abstract double getMin();
  
  public abstract double getMax();
  
  /**
   * Check, if this {@code Range} contains a certain value.
   * @param val the value to check
   * @return true, if val in this {@link Range}
   */
  public abstract boolean contains(double val);
  
  public abstract boolean isConstant();
  
  public boolean isConstant(double val) {
    return isConstant() && contains(val);
  }
  
  public abstract boolean isEmpty();
  
  public abstract boolean isUnbounded();
  
  public void tag(Object t) {
  }
  
  @Override
  public String toString() {
    if (isEmpty()) {
		return "";
	}
    String s = Double.toString(getMin());
    if (isConstant()) {
		return s;
	}
    return s + "|" + getMax();
  }
  
  public static Range generate(BigDecimal min, BigDecimal max) {
    Range res = new IA(max, min); 
    return (min != null && max != null) ? res.toAA() : res;
  }
  
  public static Range generate(double val) {
    if (Double.isNaN(val) || !Double.isFinite(val))
	 {
		return EMPTY; // can not represent [+inf,+inf] or [-inf,-inf]
	}
    BigDecimal v = BigDecimal.valueOf(val);
    return generate(v, v);
  }
  
  /**
   * Interval arithmetic
   */
  public static class IA extends Range {

    /**
     * Lower bound of this {@link Range} (null for -infinity)
     */
    private BigDecimal min;
    
    /**
     * Upper bound of this {@link Range} (null for +infinity) 
     */
    private BigDecimal max;
  
    public IA(BigDecimal min, BigDecimal max) {
      this.min = min;
      this.max = max;
    }
  
    @Override
    public IA clone() {
      return new IA(min, max);
    }
    
    @Override
    public IA toIA() {
      return this;
    }
    
    @Override
    public AA toAA() {
      if (min == null || max == null) {
		throw new RuntimeException("affine arithmetic can not handle unbounded range");
	}
      double min = this.min.doubleValue();
      double max = this.max.doubleValue();
      AA res = new AA((max+min) / 2);
      res.setUncertainty(null, (max - min) / 2);
      return res;
    }
    
    @Override
    public boolean contains(double val) {
      if (!Double.isFinite(val) || isEmpty()) {
		return false;
	}
      BigDecimal v = BigDecimal.valueOf(val);
      if (min != null && min.compareTo(v) > 0) {
		return false;
	}
      if (max != null && max.compareTo(v) < 0) {
		return false;
	}
      return true;
    }
    
    @Override
    public double getMin() {
      return min == null ? Double.NEGATIVE_INFINITY : min.doubleValue();
    }
    
    public BigDecimal lo() {
      return min;
    }
  
    @Override
    public double getMax() {
      return max == null ? Double.POSITIVE_INFINITY : max.doubleValue();
    }
    
    public BigDecimal hi() {
      return max;
    }
    
    @Override
    public String toString() {
      return "(" + super.toString() + ")";
    }

    @Override
    public boolean isConstant() {
      return min != null && max != null && min.compareTo(max) == 0;
    }

    @Override
    public boolean isEmpty() {
      return min != null && max != null && min.compareTo(max) > 0;
    }

    @Override
    public boolean isUnbounded() {
      return min != null && max != null;
    }
    
    public boolean isNonNegative() {
      return min != null && min.compareTo(BigDecimal.ZERO) >= 0;
    }
    
    public boolean isNegative() {
      return max != null && max.compareTo(BigDecimal.ZERO) < 0;
    }
    
    public boolean isNonPositive() {
      return max != null && max.compareTo(BigDecimal.ZERO) <= 0;
    }
    
    public boolean isPositive() {
      return min != null && min.compareTo(BigDecimal.ZERO) > 0;
    }
  }
  
  /**
   * Affine Arithmetic
   * <p>
   * Existing AA libraries
   * <ul>
   *   <li> http://aaflib.sourceforge.net/
   * </ul>
   */
  public static class AA extends Range {
    
    private double                  base;
    private HashMap<Object, Double> uncertainties;
    
    public AA(double base) {
      uncertainties = new HashMap<Object, Double>();
      setBase(base);
    }
    
    public double getBase() {
      return base;
    }
    
    public void setBase(double val) {
      base = val;
    }
    
    public double getUncertainty(Object tag) {
      Double u = uncertainties.get(tag);
      return u == null ? 0 : u;
    }
    
    public void setUncertainty(Object tag, double val) {
      if (val == 0) {
        uncertainties.remove(tag);
      } else {
        uncertainties.put(tag, val);
      }
    }
    
    
    public double getMaxUncertainty() {
      double sum = 0;
      for (Double u : uncertainties.values()) {
		sum += Math.abs(u);
	}
      return sum;
    }
    
    @Override
    public void tag(Object t) {
      if (uncertainties.containsKey(null)) {
		uncertainties.put(t, uncertainties.remove(null));
	}
    }
    
    public static HashSet<Object> combineTags(AA a, AA b) {
      HashSet<Object> res = new HashSet<Object>();
      res.addAll(a.uncertainties.keySet());
      res.addAll(b.uncertainties.keySet());
      return res;
    }

    @Override
    public double getMin() {
      return base - getMaxUncertainty();
    }

    @Override
    public double getMax() {
      return base + getMaxUncertainty();
    }
    
    @Override
    public AA clone() {
      AA res = new AA(base);
      res.uncertainties.putAll(uncertainties);
      return res;
    }

    @Override
    public IA toIA() {
      BigDecimal u = BigDecimal.valueOf(getMaxUncertainty());
      BigDecimal b = BigDecimal.valueOf(base);
      return new IA(b.subtract(u, FLOOR), b.add(u, Range.CEILING));
    }
    
    @Override
    public AA toAA() {
      return this;
    }

    
    @Override
    public String toString() {
      return "{" + super.toString() + "}";
    }

    @Override
    public boolean contains(double val) {
      double u = getMaxUncertainty();
      return (base - u) <= val && val <= (base + u);
    }

    @Override
    public boolean isConstant() {
      return uncertainties.isEmpty();
    }

    @Override
    public boolean isEmpty() {
      return false;
    }

    @Override
    public boolean isUnbounded() {
      return false;
    }
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