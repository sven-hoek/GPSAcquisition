package operator;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;

import accuracy.Format;

/**
 * Binary modulo: R = A % B For integer arithmetic, this is the remainder of
 * devision with truncation towards negative infinity.
 *
 * @author Andreas Engel [engel@esa.cs.tu-darmstadt.de]
 */
public class MOD extends REM {

  /** Unique ID for (de)marshalling */
  private static final long serialVersionUID = -1703850154415148440L;

  public MOD(Format a, Format b, Format r) {
    super(a, b, r);
  }

  @Override
  protected String getResultSelection(boolean outBuf, String denominator) {
    if (getOperandFormat(0).isSigned() || getOperandFormat(1).isSigned()) {
      throw new NotImplementedException("sign discussion");
    }
    return super.getResultSelection(outBuf, denominator);
  }
  
  /**
   * Only unsigned supported yet => not distinguished by {@link getAllImplementations} yet.
   * 
   */
  @Override
  public List<Class<? extends Format>> getSupportedFormats() {
    LinkedList<Class<? extends Format>> list = new LinkedList<Class<? extends Format>>();
    return list;
  }

  @Override
  public Number apply(long a, long b) {
    return (a % b + b) % b;
  }

  @Override
  public Number apply(double a, double b) {
    return (a % b + b) % b;
  }

  @Override
  public Number apply(BigInteger a, BigInteger b) {
    return a.mod(b);
  }

  @Override
  public Number apply(BigDecimal a, BigDecimal b) {
    return (a.remainder(b).add(b).remainder(b));
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