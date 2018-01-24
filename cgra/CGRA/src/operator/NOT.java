package operator;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import accuracy.Format;

/**
 * Unary bitflip: S = R = !A
 *
 * @author Andreas Engel [engel@esa.cs.tu-darmstadt.de]
 */
public class NOT extends Unary {

  /** Unique ID for (de)marshalling */
  private static final long serialVersionUID = -4254692447148122618L;

  public NOT(Format a, Format r) {
    super(a, r, "!");
  }

//  @Override
//  public boolean isControlFlow() {
//    return true;
//  }
  
  @Override
  protected List<Integer> getSupportedIntegerLatency() {
    return new LinkedList<Integer>(Arrays.asList(1));
  }

  @Override
  public String getIntegerImplementation() {
    String res = getResultPort().getAssignment(symbol + getOperandPort(0)) +
                 RESULT_VALID.getAssignment(START);
    if (isControlFlow()) res += STATUS.getAssignment(getResultPort() + " == 0 ? 1 : 0");
    return res;
  }

  @Override
  public Number apply(long a) {
    return a ^ -1;
  }

  @Override
  public Number apply(double a) {
    return apply(Double.doubleToLongBits(a));
  }

  @Override
  public Number apply(BigInteger a) {
    return a.not();
  }

  @Override
  public Number apply(BigDecimal a) {
    throw new NotImplementedException("BigDecimal arithmetic");
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