package operator;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

import accuracy.BigNumber;
import accuracy.Format;
import accuracy.Range;

/**
 * Operator with two inputs, one output and an infix symbol.
 *
 * @author Andreas Engel [engel@esa.cs.tu-darmstadt.de]
 */
public abstract class Binary extends Implementation {

  /** Unique ID for (de)marshalling */
  private static final long serialVersionUID = -6481355061218681086L;
  protected String symbol;
  
  protected Binary(Format a, Format b, Format r, String symbol) {
    super(Arrays.asList(a, b), r);
    this.symbol = symbol;
  }
  
/*
 * Testbench support
 **********************************************************************************************************************/
  
  @Override
  public Number[] apply(Number... inputs) {
    if (inputs.length != 2) throw new IllegalArgumentException("two inputs expected");
    return new Number[] {apply(BigNumber.cast(inputs[0]), 
                               BigNumber.cast(inputs[1]))};
  }
  
  /**
   * Apply operator with fast integer arithmetic
   * @param a
   * @param b
   * @return
   */
  public abstract Number apply(long a, long b);
  
  /**
   * Apply operator with fast floating point arithmetic
   * @param a
   * @param b
   * @return
   */
  public abstract Number apply(double a, double b);
  
  /**
   * Apply operator with large integer arithmetic (arbitrary size)
   * @param a
   * @param b
   * @return
   */
  public abstract Number apply(BigInteger a, BigInteger b);
  
  /**
   * Apply operator with large floating point arithmetic (arbitrary size, upto 10**(+-2**32) bit precision)  
   * @param a
   * @param b
   * @return
   */
  public abstract Number apply(BigDecimal a, BigDecimal b);
  
  /**
   * Apply operator with arbitrary arithmetic
   * @param a
   * @param b
   * @return
   */
  protected Number apply(BigNumber a, BigNumber b) {
    throw new NotImplementedException("arbitrary binary arithmetic");
  }
  

  
/*
 * Bitwidth optimization
 **********************************************************************************************************************/

  /**
   * Propagate range from operands to the arithmetic output.
   * @param a  range of first  operand
   * @param b  range of second operand
   * @return   range of result
   */
  public Range getResultRange(Range a, Range b) {
    if (a.isEmpty() || b.isEmpty()) return Range.EMPTY;
    return (a instanceof Range.AA && b instanceof Range.AA) ? getResultRange(a.toAA(), b.toAA()) 
                                                            : getResultRange(a.toIA(), b.toIA());
  }
  
  /**
   * Propagate non-empty range from operands to the arithmetic output using affine arithmetic.
   * An unbounded {@link Range} is generated, unless the underlying {@link Implementation} provides a more meaningful
   * propagation scheme.
   * @param a  range of first  operand
   * @param b  range of second operand
   * @return   range of result
   */
  protected Range getResultRange(Range.AA a, Range.AA b) {
    return Range.UNBOUNDED;
  }
  
  /**
   * Propagate non-empty range from operands to the arithmetic output using interval arithmetic.
   * An unbounded {@link Range} is generated, unless the underlying {@link Implementation} provides a more meaningful
   * propagation scheme.
   * @param a  range of first  operand
   * @param b  range of second operand
   * @return   range of result
   */
  protected Range getResultRange(Range.IA a, Range.IA b) {
    return Range.UNBOUNDED;
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