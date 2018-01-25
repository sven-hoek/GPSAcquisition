package operator;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import accuracy.Format;

/**
 * Select minimum or maximum value
 *
 * @author Andreas Engel [engel@esa.cs.tu-darmstadt.de]
 */
public abstract class Select extends Binary {

  /** Unique ID for (de)marshalling */
  private static final long serialVersionUID = -4444023825894103799L;

  protected Select(Format a, Format b, Format r, String symbol) {
    super(a, b, r, symbol);
  }
  
  @Override
  protected List<Integer> getSupportedIntegerLatency() {
    return new LinkedList<Integer>(Arrays.asList(1));
  }

  @Override
  public String getIntegerImplementation() {
    // sanity checks
    if (getLatency() > 1) {
      throw new NotImplementedException("multi cycle");
    }

    int s = sel(-1, 0, 1).intValue();
    String[] op = new String[2];
    for (int i = 0; i < op.length; i++) {
      op[i] = getOperandPort(i).toString();
      if (getOperandFormat(i).isSigned()) {
        op[i] = "$signed(" + op[i] + ")";
      }
    }
    return getResultPort().getAssignment(op[0] + " < " + op[1] + " ? " + op[s] + " : " + op[1 - s])
         + RESULT_VALID.getAssignment(START);
  }

  /**
   * Select value based on result of compare
   *
   * @param compare
   *            {@code sgn(a-b)}
   * @param a
   * @param b
   * @return selected value
   */
  protected abstract Number sel(int compare, Number a, Number b);

  @Override
  public Number apply(long a, long b) {
    return sel(Long.compare(a, b), a, b);
  }

  @Override
  public Number apply(double a, double b) {
    return sel(Double.compare(a, b), a, b);
  }

  @Override
  public Number apply(BigInteger a, BigInteger b) {
    return sel(a.compareTo(b), a, b);
  }

  @Override
  public Number apply(BigDecimal a, BigDecimal b) {
    return sel(a.compareTo(b), a, b);
  }

  /**
   * Maximum: R = A < B ? B : A
   *
   * @author Andreas Engel [engel@esa.cs.tu-darmstadt.de]
   */
  public static class MAX extends Select {
    
    /** Unique ID for (de)marshalling */
    private static final long serialVersionUID = 7425272869385439242L;

    public MAX(Format a, Format b, Format r) {
      super(a, b, r, "max");
    }

    @Override
    protected Number sel(int compare, Number a, Number b) {
      return compare < 0 ? b : a;
    }
  }

  /**
   * Maximum: R = A < B ? A : B
   *
   * @author Andreas Engel [engel@esa.cs.tu-darmstadt.de]
   */
  public static class MIN extends Select {
    
    /** Unique ID for (de)marshalling */
    private static final long serialVersionUID = 1490004765140286300L;

    public MIN(Format a, Format b, Format r) {
      super(a, b, r, "min");
    }

    @Override
    protected Number sel(int compare, Number a, Number b) {
      return compare < 0 ? a : b;
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