package operator;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import accuracy.Format;

/**
 * Compare: R = sgn(A-B), [S = A == B]
 *
 * @author Andreas Engel [engel@esa.cs.tu-darmstadt.de]
 */
public class Compare extends Binary {

  /** Unique ID for (de)marshalling */
  private static final long serialVersionUID = -7622209721470284594L;
  
  /**
   * If set, the control flow signals are generated
   */
  protected boolean controlFlow;

  /**
   * Generate {@code Implementation} of a compare {@code Operator}.
   * 
   * @param a   input precision
   * @param b   input precision
   * @param r   output precision
   * @param c   control flow flag
   */
  public Compare(Format a, Format b, Format r, boolean c) {
    super(a, b, r, "<=>");
    this.controlFlow = c;
  }
  
  /**
   * Generate {@code Implementation} of a compare {@code Operator}.
   * 
   * @param a   input precision
   * @param b   input precision
   * @param r   output precision
   */
  public Compare(Format a, Format b, Format r) {
    this(a,b,r,false);
  }
  
  @Override
  public boolean isControlFlow() {
    return controlFlow;
  }
  
  /** 
   * Include hint for control flow configuration in module name
   */
  @Override
  public String getName() {
    return (isControlFlow() ? "CI_" : "") + super.getName();
  }
  
  @Override 
  protected List<Integer> getSupportedIntegerLatency() {
    return new LinkedList<Integer>(Arrays.asList(1));
  }

  @Override
  public String getIntegerImplementation() {

    String[] op = new String[2];
    for (int i = 0; i < op.length; i++) {
      op[i] = getOperandPort(i).toString();
      if (getOperandFormat(i).isSigned()) {
        op[i] = "$signed(" + op[i] + ")";
      }
    }
    
    StringBuilder res = new StringBuilder();
    
    
    res.append(RESULT_VALID.getAssignment(START));
    
    res.append(getResultPort().getAssignment(op[0] + " > " + op[1] + " ? 1 : " + op[0] + " < " + op[1] + " ? -1 : 0"));
    if (isControlFlow()) {
      res.append(STATUS_VALID.getAssignment(START));
      res.append(STATUS.getAssignment(getOperandPort(0) + " == " + getOperandPort(1) + " ? 1 : 0"));
    }
    
    return res.toString();
  }

  @Override
  public Number apply(long a, long b) {
    return Long.compare(a, b);
  }

  @Override
  public Number apply(double a, double b) {
    return Double.compare(a, b);
  }

  @Override
  public Number apply(BigInteger a, BigInteger b) {
    return a.compareTo(b);
  }

  @Override
  public Number apply(BigDecimal a, BigDecimal b) {
    return a.compareTo(b);
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