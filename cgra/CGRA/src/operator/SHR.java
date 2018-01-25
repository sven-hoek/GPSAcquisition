package operator;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import accuracy.Format;

/**
 * Binary arithmetic shift right (with sign extension): R = A >> B
 *
 * @author Andreas Engel [engel@esa.cs.tu-darmstadt.de]
 */
public class SHR extends Binary {

  /** Unique ID for (de)marshalling */
  private static final long serialVersionUID = -3616757720145061767L;

  public SHR(Format a, Format b, Format r) {
    super(a, b, r, " >> ");
  }

  /**
   * First operand has to be read completely, before result can be shifted
   * out. Second operand (shift width) is expected to be provided in first
   * cycle.
   */
  @Override
  public int getMinLatency() {
    return ioLatency(getOperandFormat(0).getBitWidth()) + getOutputLatency() - 1;
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
    String an = getOperandPort(0).toString();
    String rn = getResultPort() + "_generated";
    int aw = af.getBitWidth();
    int bw = bf.getBitWidth();
    int rw = rf.getBitWidth();
    int ac = ioLatency(aw) - 1;
    int rl = ioLatency(rw);
    int rc = getLatency() - rl;
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

    // shift width is only required in cycle ac => no mux required for cycle
    // 0
    if (ac > 0) {
      res.append("reg " + bitRange(bitwidth(maxShiftWidth)) + " shiftWidth;\n");
      res.append("always @(posedge " + CLOCK + ") begin\n");
      res.append("  if (cycle == 0) shiftWidth <= " + getOperandPort(1) + ";\n");
      res.append("end\n\n");
    }

    // Input buffer
    if (ac > 0) {
      res.append(getOperandBuffer(0, ac, ac));
    }

    // Output buffer / output generation
    if (rc > ac || rl > 1) {
      res.append(getResultBuffer(0, ac, false));
      an += "_buffered";
    } else {
      res.append("reg " + bitRange(rw) + " " + rn + ";\n");
      res.append(getResultPort().getAssignment(rn));
      res.append(RESULT_VALID.getAssignment(START) + "\n");
    }

    // barrel shifter as large multiplexer
    res.append("always @* begin\n");
    res.append("  case (");
    res.append(ac > 0 ? "shiftWidth" : getOperandPort(1));
    res.append(")\n");
    for (int i = 0; i < maxShiftWidth; i++) {
      res.append(i == 0 ? "    default" : String.format("    %7d", i));
      res.append(" : " + rn + " = {");

      // extention
      if (i > 0) {
        res.append(signedExtension() ? ("{" + i + "{" + an + "[" + (aw - 1) + "]}},") : (i + "'b0,"));
      }
      res.append(an);
      if (i > 0) {
        res.append(bitRange(aw - 1, i));
      }
      res.append("};\n");
    }
    res.append("  endcase\n");
    res.append("end");

    return res.toString();
  }

  /**
   * Decide on type of high bits after shifting
   *
   * @return
   */
  protected boolean signedExtension() {
    return getOperandFormat(0).isSigned();
  }

  @Override
  public Number apply(long a, long b) {
    return a >> b;
  }

  @Override
  public Number apply(double a, double b) {
    return a * Math.pow(2, -b);
  }

  @Override
  public Number apply(BigInteger a, BigInteger b) {
    return a.shiftRight(b.intValue());
  }

  @Override
  public Number apply(BigDecimal a, BigDecimal b) {
    return a.divide(new BigDecimal(1 << b.longValue()));
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