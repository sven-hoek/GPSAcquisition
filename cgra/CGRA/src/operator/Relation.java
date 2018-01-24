package operator;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import accuracy.Format;

/**
 * Branch decision derived from binary compare: [R =] S = A <=> B. The generated
 * {@link #STATUS} signal may also be presented at the arithmetic result port.
 * 
 * @author Andreas Engel [engel@esa.cs.tu-darmstadt.de]
 */
public abstract class Relation extends Binary {

  /** Unique ID for (de)marshalling */
  private static final long serialVersionUID = -3449019804215346261L;

  /**
   * Generate the {@code Implementation} of a branch decision {@code Operator}
   * .
   * 
   * @param a
   *            input precision
   * @param b
   *            input precision
   * @param symbol
   *            infix operator
   * @param status
   *            decides, whether the result is assigned to the {@link #STATUS}
   *            signal or the arithmetic result port
   */
  protected Relation(Format a, Format b, String symbol, boolean status) {
    super(a, b, Format.Boolean.INSTANCE, symbol);
    if (status) {
      resultFormat.clear();
    }
  }
  
  @Override
  public String getName() {
    return (isControlFlow() ? "IF" : "") + super.getName(); 
  }

  @Override
  public boolean isControlFlow() {
    return getNumberOfResults() == 0;
  }
  
  /**
   * Only inputs relevant for implementation format.
   */
  @Override
  public Format getCommonFormat() {
    Format f = getOperandFormat(0);
    return f.equals(getOperandFormat(1)) ? f : null;
  }
  
  @Override
  protected List<Integer> getSupportedIntegerLatency() {
    return new LinkedList<Integer>(Arrays.asList(1));
  }

  @Override
  public String getIntegerImplementation() {
    StringBuilder res = new StringBuilder();


    for (int i = 0; i < getNumberOfOperands(); i++) {
      if (i > 0) {
        res.append(" " + symbol + " ");
      }
      Format f = getOperandFormat(i);
      if (f.isSigned()) {
        res.append("$signed(");
      }
      res.append(getOperandPort(i));
      if (f.isSigned()) {
        res.append(")");
      }
    }
    res.append(" ? 1'b1 : 1'b0");

    String opStatement = STATUS.getAssignment(res.toString());
    res.setLength(0);

    // Hack to make non control flow relations synthesisable
    if (!isControlFlow()) {
      res.append("wire STATUS_O;\n");
    }
    res.append(opStatement);

    // Make sure that we do not set an non existing status valid signal
    if (isControlFlow()) {
      res.append(STATUS_VALID.getAssignment(START));
    }

    if (getNumberOfResults() > 0) {
      res.append(getResultPort().getAssignment(STATUS));
      res.append(RESULT_VALID.getAssignment(START));
    }
    return res.toString();
  }

  @Override
  public Number apply(BigInteger a, BigInteger b) {
    return apply(a.compareTo(b), 0);
  }

  @Override
  public Number apply(BigDecimal a, BigDecimal b) {
    return apply(a.compareTo(b), 0);
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Equal {@code Relation}
   */
  public static class EQ extends Relation {

    /** Unique ID for (de)marshalling */
    private static final long serialVersionUID = -3351471797347146788L;

    /**
     * Generate a new {@code Relation} {@code Implementation}
     * 
     * @param a
     *            input precision
     * @param b
     *            input precision
     * @param status
     *            decides, whether the result is assigned to the
     *            {@link #STATUS} signal or the arithmetic result port
     */
    public EQ(Format a, Format b, boolean status) {
      super(a, b, "==", status);
    }

    @Override
    public Number apply(long a, long b) {
      return a == b ? 1 : 0;
    }

    @Override
    public Number apply(double a, double b) {
      return a == b ? 1 : 0;
    }

  }

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Not equal {@code Relation}
   * 
   * @author Andreas Engel [engel@esa.cs.tu-darmstadt.de]
   */
  public static class NE extends Relation {

    /** Unique ID for (de)marshalling */
    private static final long serialVersionUID = 2500101928148252147L;

    /**
     * Generate a new {@code Relation} {@code Implementation}
     * 
     * @param a
     *            input precision
     * @param b
     *            input precision
     * @param status
     *            decides, whether the result is assigned to the
     *            {@link #STATUS} signal or the arithmetic result port
     */
    public NE(Format a, Format b, boolean status) {
      super(a, b, "!=", status);
    }

    @Override
    public Number apply(long a, long b) {
      return a != b ? 1 : 0;
    }

    @Override
    public Number apply(double a, double b) {
      return a != b ? 1 : 0;
    }
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Less equal {@code Relation}
   * 
   * @author Andreas Engel [engel@esa.cs.tu-darmstadt.de]
   */
  public static class LE extends Relation {

    /** Unique ID for (de)marshalling */
    private static final long serialVersionUID = -7466832777361023118L;

    /**
     * @param a
     *            input precision
     * @param b
     *            input precision
     * @param status
     *            decides, whether the result is assigned to the
     *            {@link #STATUS} signal or the arithmetic result port
     */
    public LE(Format a, Format b, boolean status) {
      super(a, b, "<=", status);
    }

    @Override
    public Number apply(long a, long b) {
      return a <= b ? 1 : 0;
    }

    @Override
    public Number apply(double a, double b) {
      return a <= b ? 1 : 0;
    }
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Less than {@code Relation}
   * 
   * @author Andreas Engel [engel@esa.cs.tu-darmstadt.de]
   */
  public static class LT extends Relation {

    /** Unique ID for (de)marshalling */
    private static final long serialVersionUID = 3839622818301405517L;

    /**
     * Generate a new {@code Relation} {@code Implementation}
     * 
     * @param a
     *            input precision
     * @param b
     *            input precision
     * @param status
     *            decides, whether the result is assigned to the
     *            {@link #STATUS} signal or the arithmetic result port
     */
    public LT(Format a, Format b, boolean status) {
      super(a, b, "<", status);
    }

    @Override
    public Number apply(long a, long b) {
      return a < b ? 1 : 0;
    }

    @Override
    public Number apply(double a, double b) {
      return a < b ? 1 : 0;
    }
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Greater equal {@code Relation}
   * 
   * @author Andreas Engel [engel@esa.cs.tu-darmstadt.de]
   */
  public static class GE extends Relation {

    /** Unique ID for (de)marshalling */
    private static final long serialVersionUID = 3158258845225723481L;

    /**
     * Generate a new {@code Relation} {@code Implementation}
     * 
     * @param a
     *            input precision
     * @param b
     *            input precision
     * @param status
     *            decides, whether the result is assigned to the
     *            {@link #STATUS} signal or the arithmetic result port
     */
    public GE(Format a, Format b, boolean status) {
      super(a, b, ">=", status);
    }

    @Override
    public Number apply(long a, long b) {
      return a >= b ? 1 : 0;
    }

    @Override
    public Number apply(double a, double b) {
      return a >= b ? 1 : 0;
    }
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Greater than {@code Relation}
   * 
   * @author Andreas Engel [engel@esa.cs.tu-darmstadt.de]
   */
  public static class GT extends Relation {

    /** Unique ID for (de)marshalling */
    private static final long serialVersionUID = 137902718368993249L;

    /**
     * Generate a new {@code Relation} {@code Implementation}
     * 
     * @param a
     *            input precision
     * @param b
     *            input precision
     * @param status
     *            decides, whether the result is assigned to the
     *            {@link #STATUS} signal or the arithmetic result port
     */
    public GT(Format a, Format b, boolean status) {
      super(a, b, ">", status);
    }

    @Override
    public Number apply(long a, long b) {
      return a > b ? 1 : 0;
    }

    @Override
    public Number apply(double a, double b) {
      return a > b ? 1 : 0;
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