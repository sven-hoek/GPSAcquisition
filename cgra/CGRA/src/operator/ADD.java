package operator;

import java.math.BigDecimal;
import java.math.BigInteger;

import accuracy.Format;
import accuracy.Range;

/**
 * Binary addition: R = A + B
 *
 * @author Andreas Engel [engel@esa.cs.tu-darmstadt.de]
 */
public class ADD extends AddSub {

  /** Unique ID for (de)marshalling */
  private static final long serialVersionUID = 7150404321114445307L;

  public ADD(Format a, Format b, Format r) {
    super(a, b, r, "+");
  }
  
  @Override
  public Number apply(long a, long b) {
    if (getResultFormat().getBitWidth() <= 64) return a+b;
    return apply(new BigDecimal(a).toBigInteger(), new BigDecimal(b).toBigInteger());
  }

  @Override
  public Number apply(double a, double b) {
    if (getResultFormat().getBitWidth() <= 64) return a+b;
    return apply(new BigDecimal(a), new BigDecimal(b));
  }

  @Override
  public Number apply(BigInteger a, BigInteger b) {
    return a.add(b);
  }

  @Override
  public Number apply(BigDecimal a, BigDecimal b) {
    return (a.add(b));
  }
  
  @Override
  protected Range getResultRange(Range.IA a, Range.IA b) {
    // see Stolfi1997 page 22
    return Range.generate(a.lo() == null || b.lo() == null ? null : a.lo().add(b.lo(), Range.FLOOR), 
                          a.hi() == null || b.hi() == null ? null : a.hi().add(b.hi(), Range.CEILING));
  }
  
  public static void main(String[] args) {
    double[] list = new double[] {Double.NaN,Double.POSITIVE_INFINITY,Double.NEGATIVE_INFINITY,+0.0,-0.0,1.0,-1.0};
    for (double a : list) for (double b : list) System.out.println(a + " + " + b + " = " + (a+b));
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