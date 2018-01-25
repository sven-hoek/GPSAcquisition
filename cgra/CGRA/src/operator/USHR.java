package operator;

import java.math.BigDecimal;
import java.math.BigInteger;

import accuracy.Format;

/**
 * Binary logic shift right (without sign extension): R = A >> B
 *
 * @author Andreas Engel [engel@esa.cs.tu-darmstadt.de]
 */
public class USHR extends SHR {

  /** Unique ID for (de)marshalling */
  private static final long serialVersionUID = -4747739077778099042L;

  public USHR(Format a, Format b, Format r) {
    super(a, b, r);
  }

  @Override
  protected boolean signedExtension() {
    return false;
  }

  @Override
  public Number apply(long a, long b) {
    if (b <= 0) {
      return a;
    }
    long res = 0;
    for (int i = 0; i < getResultFormat().getBitWidth() - b; i++) {
      if ((a & (1l << (i + b))) != 0) {
        res |= 1l << i;
      }
    }
    return res;
  }

  @Override
  public Number apply(double a, double b) {
    throw new NotImplementedException("double arithmetic");
  }

  @Override
  public Number apply(BigInteger a, BigInteger b) {
    int shiftWidth = b.intValue();
    if (shiftWidth <= 0) {
      return a;
    }

    BigInteger res = BigInteger.ZERO;
    for (int i = 0; i < getResultFormat().getBitWidth() - shiftWidth; i++) {
      if (a.testBit(i + shiftWidth)) {
        res = res.setBit(i);
      }
    }

    return res;
  }

  @Override
  public Number apply(BigDecimal a, BigDecimal b) {
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