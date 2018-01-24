package operator;

import java.math.BigDecimal;
import java.math.BigInteger;

import accuracy.Format;

/**
 * Binary remainder: R = A % B For integer arithmetic, this is the remainder of
 * devision with truncation towards zero.
 *
 * @author Andreas Engel [engel@esa.cs.tu-darmstadt.de]
 */
public class REM extends DivRem {

  /** Unique ID for (de)marshalling */
  private static final long serialVersionUID = 4809948862304151580L;

  public REM(Format a, Format b, Format r) {
    super(a, b, r);
  }

  /**
   * Select the remainder as result from the division operation
   */
  @Override
  protected String getResultSelection(boolean outBuf, String denominator) {
    Format af = getOperandFormat(0);
    Format rf = getResultFormat();

    StringBuilder res = new StringBuilder();

    // NewtonRaphson needs some extra correction before restoring the sign
    if (getAlgorithm() == Algorithm.NewtonRaphson) {
      res.append("wire " + bitRange(getResultFormat().getBitWidth()) + " remainder = ");
      res.append("rem >= 2*" + denominator + " ? rem-2*" + denominator + " : ");
      res.append("rem >= " + denominator + " ? rem-" + denominator + " : rem;\n");
    }

    boolean inputBuffered = getInputLatency() > 1;
    StringBuilder gen = new StringBuilder();
    switch (getAlgorithm()) {
    case Combinatorial:
      String[] op = new String[getNumberOfOperands()];
      for (int i = 0; i < op.length; i++) {
        Format f = getOperandFormat(i);
        op[i] = (f.isSigned() ? "$signed(" : "") + getOperandPort(i) + (inputBuffered ? "_buffered" : "")
            + (f.isSigned() ? ")" : "");
      }
      gen.append(op[0] + " - (" + op[0] + " / " + op[1] + ") * " + op[1]);
      break;

    case Restoring:
      // remainder is stored in high word of partialRemainder, sign must
      // be derived from numerator sign
      String n = "partialRemainder" + bitRange(af.getBitWidth() + rf.getBitWidth() - 1, af.getBitWidth());
      if (af.isSigned()) {
        gen.append(getOperandPort(0) + "_sign ? -" + n + " : ");
      }
      gen.append(n);
      break;

    case NewtonRaphson:
      // remainder is stored in high word of partialRemainder, sign must
      // be derived from numerator sign
      if (af.isSigned()) {
        gen.append(getOperandPort(0) + "_sign ? -remainder : ");
      }
      gen.append("remainder");
      break;

    default:
      throw new NotImplementedException("algorithm=" + getAlgorithm());
    }
    
    if (outBuf) res.append("assign " + getResultPort() + "_generated = " + gen + ";\n");
    else        res.append(getResultPort().getAssignment(gen.toString()));
    
    return res.toString();
  }

  /**
   * Remainder only requires sign of numerator
   */
  @Override
  protected boolean keepSign(int operandIndex) {
    return operandIndex == 0;
  }

  @Override
  protected boolean keepQuotient() {
    return false;
  }

  @Override
  public Number apply(long a, long b) {
    return a % b;
  }

  @Override
  public Number apply(double a, double b) {
    return Math.IEEEremainder(a, b);
  }

  @Override
  public Number apply(BigInteger a, BigInteger b) {
    return a.remainder(b);
  }

  @Override
  public Number apply(BigDecimal a, BigDecimal b) {
    return a.remainder(b);
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