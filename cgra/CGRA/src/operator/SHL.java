package operator;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import accuracy.Format;

/**
 * Binary shift left: R = A << B
 *
 * @author Andreas Engel [engel@esa.cs.tu-darmstadt.de]
 */
public class SHL extends Binary {

  /** Unique ID for (de)marshalling */
  private static final long serialVersionUID = -8267410255185283751L;

  public SHL(Format a, Format b, Format r) {
    super(a, b, r, " << ");
  }

  /**
   * Latency is defined by result width. Second operand is expected to be
   * available in first cycle.
   */
  @Override
  public int getMinLatency() {
    return getOutputLatency();
  }
  
  @Override
  protected List<Integer> getSupportedIntegerLatency() {
    return new LinkedList<Integer>(Arrays.asList(getMinLatency()));
  }

  @Override
  public String getIntegerImplementation() {

    // often used properties
    Format af = getOperandFormat(0);
    Format bf = getOperandFormat(1);
    Format rf = getResultFormat();
    int aw = af.getBitWidth();
    int bw = bf.getBitWidth();
    int rw = rf.getBitWidth();
    int ap = getOperandPortWidth(0);
    int rp = getResultPortWidth();
    int maxShiftWidth = rw;
    if (bitwidth(maxShiftWidth) > bw) {
      maxShiftWidth = 1 << bw; // TODO is this valid?
    }

    // sanity checks
    if (!(bf instanceof Format.Integer)) {
      throw new NotImplementedException("non integer shift width");
    }
    if (ioLatency(bitwidth(maxShiftWidth)) != 1) {
      throw new NotImplementedException("multi cycle shift width");
    }
    if (aw != rw) {
      throw new NotImplementedException("I/O extension");
    }

    StringBuilder res = new StringBuilder();

    // will be written by multiplexer
    res.append("reg " + bitRange(rw) + " shifted;\n");
    res.append(getResultPort().getAssignment("shifted" + (rp < rw ? bitRange(rp) : "")));
    res.append(RESULT_VALID.getAssignment(START));
    res.append("\n");

    if (isMultiCycle()) {

      // buffer for next cycle
      res.append("reg " + bitRange(rw - rp) + " buffered;\n");
      res.append("always @(posedge " + CLOCK + ") begin\n");
      res.append("  if (~" + RESET + " || cycle==" + (getLatency() - 1) + ") begin\n");
      res.append("    buffered <= 0;\n");
      res.append("  end else if (" + START + " || cycle > 0) begin\n");
      res.append("    buffered <= shifted" + bitRange(rw - 1, rp) + ";\n");
      res.append("  end\n");
      res.append("end\n\n");

      // buffer the shift width
      res.append("reg " + bitRange(bitwidth(maxShiftWidth)) + " shiftWidth_buffered;\n");
      res.append("always @(posedge " + CLOCK + ") begin\n");
      res.append("  if (cycle == 0) shiftWidth_buffered <= " + getOperandPort(1) + ";\n");
      res.append("end\n\n");

      // select the current shift width
      res.append("wire " + bitRange(bitwidth(maxShiftWidth)) + " shiftWidth;\n");
      res.append("assign shiftWidth = cycle == 0 ? " + getOperandPort(1) + " : shiftWidth_buffered;\n\n");
    }

    // barrel shifter as large multiplexer
    res.append("always @* begin\n");
    res.append("  case (");
    res.append(isMultiCycle() ? "shiftWidth" : getOperandPort(1));
    res.append(")\n");
    for (int i = 0; i < maxShiftWidth; i++) {
      res.append(i == 0 ? "    default" : String.format("    %7d", i));
      res.append(" : shifted = {");
      if (getLatency() == 1) {
        res.append(getOperandPort(0));
        if (i > 0) {
          res.append(bitRange(aw - i - 1, 0));
          res.append("," + i + "'b0");
        }
      } else {
        if (i + ap < rw) {
          res.append((rw - ap - i) + "'b0,");
        }
        res.append(getOperandPort(0));
        if (i + ap > rw) {
          res.append(bitRange(rw - i - 1, 0));
        }
        if (i > 0) {
          if (i > rw - rp) {
            res.append("," + (i - rw + rp) + "'b0");
          }
          res.append(",buffered");
          if (i < rw - rp) {
            res.append(bitRange(i - 1, 0));
          }
        }
      }
      res.append("};\n");
    }
    res.append("  endcase\n");
    res.append("end");

    return res.toString();
  }

  @Override
  public Number apply(long a, long b) {
    return a << b;
  }

  @Override
  public Number apply(double a, double b) {
    return a * Math.pow(2, b);
  }

  @Override
  public Number apply(BigInteger a, BigInteger b) {
    return a.shiftLeft(b.intValue());
  }

  @Override
  public Number apply(BigDecimal a, BigDecimal b) {
    return a.multiply(new BigDecimal(1 << b.longValue()));
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