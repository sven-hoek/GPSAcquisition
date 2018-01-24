package operator;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import accuracy.Format;

/**
 * Binary bitwise logic. Common methods for AND, OR, XOR. This {@code Operator}s
 * may also generate a status flag, which is set to 1 for non-zero results.
 *
 * @author Andreas Engel [engel@esa.cs.tu-darmstadt.de]
 */
public abstract class Logic extends Binary {

  /** Unique ID for (de)marshalling */
  private static final long serialVersionUID = 7772952448607263948L;
  
  /**
   * is set, the data output will also be forwarded to the {@link #STATUS}
   * signal
   */
  protected boolean generateStatus;

  protected Logic(Format a, Format b, Format r, String symbol, boolean generateStatus) {
    super(a, b, r, symbol);
    this.generateStatus = generateStatus;
  }

  @Override
  public boolean isControlFlow() {
    return generateStatus;
  }
  
  /**
   * Latency is defined by output width
   */
  @Override
  public int getMinLatency() {
    return getOutputLatency();
  }
  
  @Override
  public List<Integer> getSupportedRawLatency() {
    return getSupportedMixedFormatLatency();
  }
  
  @Override
  public String getRawImplementation() {
    return getMixedFormatImplementation();
  }

  
  @Override
  public List<Integer> getSupportedMixedFormatLatency() {
    return new LinkedList<Integer>(Arrays.asList(getMinLatency()));
  }

  @Override
  public String getMixedFormatImplementation() {
    if (getLatency() < getMinLatency()) {
      throw new InvalidConfigException("latency=" + getLatency());
    }
    if (getLatency() > getMinLatency()) {
      throw new NotImplementedException("additional delay");
    }
    if (Math.min(getOperandFormat(0).getBitWidth(), getOperandFormat(1).getBitWidth()) < getResultFormat()
        .getBitWidth()) {
      throw new NotImplementedException("input extension");
    }

    String compare = getResultPort() + " == 0 ? 1'b0 : 1'b1";

    StringBuilder res = new StringBuilder();
    res.append(getResultPort().getAssignment(getOperandPort(0) + " " + symbol + " " + getOperandPort(1)));
    res.append(RESULT_VALID.getAssignment(getLatency() == 1 ? START.toString() : "cycle == " + (getLatency()-1) +" ? 1 : 0"));
    if (!generateStatus) {
      return res.toString();
    }

    // register for accumulation of status output
    if (getLatency() > 1) {
      res.append("\nwire compare = " + compare + ";\n");
      res.append("reg status;\n");
      res.append("always @(posedge " + CLOCK + ") begin\n");
      res.append("  if (~" + RESET + ") begin\n");
      res.append("    status <= 1'b0;\n");
      res.append("  end else begin\n");
      res.append("    status <= compare & (status | " + START + ");\n");
      res.append("  end\n");
      res.append("end\n\n");
      res.append(STATUS.getAssignment("compare & status"));
      res.append(STATUS_VALID.getAssignment("cycle == " + (getLatency()-1) +" ? 1 : 0"));

    } else {
      res.append("\n" + STATUS.getAssignment(compare));
      res.append(STATUS_VALID.getAssignment(START));
    }

    return res.toString();

  }

  @Override
  public Number apply(double a, double b) {
    return apply(Double.doubleToLongBits(a), Double.doubleToLongBits(b));
  }

  @Override
  public Number apply(BigDecimal a, BigDecimal b) {
    throw new NotImplementedException("BigDecimal arithmetic");
  }

  /**
   * Binary bitwise and: [S =] R = A & B.
   */
  public static class AND extends Logic {
    /**
     *
     */
    private static final long serialVersionUID = 3687658657949288728L;

    public AND(Format a, Format b, Format r, boolean generateStatus) {
      super(a, b, r, "&", generateStatus);
    }

    @Override
    public Number apply(long a, long b) {
      return a & b;

    }

    @Override
    public Number apply(BigInteger a, BigInteger b) {
      return a.and(b);
    }
  }

  /**
   * Binary bitwise or: [S =] R = A | B.
   */
  public static class OR extends Logic {
    /**
     *
     */
    private static final long serialVersionUID = 7335177014530042252L;

    public OR(Format a, Format b, Format r, boolean generatStatus) {
      super(a, b, r, "|", generatStatus);
    }

    @Override
    public Number apply(long a, long b) {
      return a | b;
    }

    @Override
    public Number apply(BigInteger a, BigInteger b) {
      return a.or(b);
    }
  }

  /**
   * Binary bitwise xor: [S =] R = A ^ B.
   */
  public static class XOR extends Logic {
    /**
     *
     */
    private static final long serialVersionUID = 834999781330156206L;

    public XOR(Format a, Format b, Format r, boolean generateStatus) {
      super(a, b, r, "^", generateStatus);
    }

    @Override
    public Number apply(long a, long b) {
      return a ^ b;
    }

    @Override
    public Number apply(BigInteger a, BigInteger b) {
      return a.xor(b);
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