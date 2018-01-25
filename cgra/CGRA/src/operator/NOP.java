package operator;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import accuracy.BigNumber;
import accuracy.Format;

/**
 * No Operation: R = A Input will be handed over to output.
 *
 * @author Andreas Engel [engel@esa.cs.tu-darmstadt.de]
 */
public class NOP extends Unary {

  /** Unique ID for (de)marshalling */
  private static final long serialVersionUID = 9079816478724976351L;

  public NOP(Format f) {
    super(f, f, "");
  }
  
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
    return getResultPort().getAssignment(getOperandPort(0))
         + RESULT_VALID   .getAssignment(START);
  }

  @Override
  public Number apply(long a) {
    return a;
  }

  @Override
  public Number apply(double a) {
    return a;
  }

  @Override
  public Number apply(BigInteger a) {
    return a;
  }

  @Override
  public Number apply(BigDecimal a) {
    return a;
  }
  
  @Override
  protected Number apply(BigNumber a) {
    return a;
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