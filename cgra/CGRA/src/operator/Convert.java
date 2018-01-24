package operator;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import accuracy.Format;
import accuracy.Range;

/**
 * Data type conversion
 *
 * @author Andreas Engel [engel@esa.cs.tu-darmstadt.de]
 */
public class Convert extends Unary {

  /** Unique ID for (de)marshalling */
  private static final long serialVersionUID = 3012301474134236624L;

  public Convert(Format a, Format r) {
    super(a, r, "conv ");
  }

  /**
   * Latency is restricted by output only TODO not true for D2F and L2F
   */
  @Override
  public int getMinLatency() {
    return getOutputLatency();
  }
  
  // !! must be mixed format implementation, as conversion between common format does not make sense
  
  @Override 
  public List<Integer> getSupportedMixedFormatLatency() {
    return new LinkedList<Integer>(Arrays.asList(getMinLatency()));
  }

  @Override
  public String getMixedFormatImplementation() {
    
    // frequently used properties
    Format af = getOperandFormat(0);
    Format of = getResultFormat();
    String in = getOperandPort(0).toString();
    int iw = af.getBitWidth();
    int ow = of.getBitWidth();
    int il = getInputLatency();
    int ol = getOutputLatency();
    int ip = getOperandPortWidth(0);
    int op = getResultPortWidth();
    int ih = (iw - 1) % ip;
    
    // sanity checks
    if (!(af instanceof Format.Integer && of instanceof Format.Integer)) {
      throw new NotImplementedException("non integer arguments");
    }

    StringBuilder res = new StringBuilder();

    // cycle dependent output behavior, if sequential input finished before
    // sequential output
    if (il < ol) {

      // buffer sign bit
      if (af.isSigned()) {
        res.append("reg sign;\n");
        res.append("always @(posedge " + CLOCK + ") begin\n");
        res.append("  if (cycle == " + (il - 1) + ") sign <= " + in + "[" + ih + "];\n");
        res.append("end\n\n");
      }
    }

    StringBuilder gen = new StringBuilder();

    // single cycle => just apply static sign extension or range restriction
    if (getLatency() == 1) {
      if (iw < ow) {
        gen.append("{" + (af.isSigned() ? ("{" + (ow - iw) + "{" + in + "[" + ih + "]}}") : ((ow - iw) + "'b0"))
            + ",");
      }
      gen.append(in);
      if (iw > ow) {
        gen.append(bitRange(ow));
      }
      if (iw < ow) {
        gen.append("}");
      }

      // multi cycle, but output finished first => no
      // extension/restriction required
    } else if (il > ol) {
      gen.append(in);

      // cycle dependent multi cycle output
    } else {

      // low words without extension
      if (il > 1 || ih == op - 1) {
        gen.append("cycle ");
        gen.append(il == 1 ? "=" : "<");
        gen.append(ih == op - 1 ? "= " : " ");
        gen.append((il - 1) + " ? " + in + " : ");
      }

      // extension + input at end of input
      if (ih < op - 1) {
        if (il < ol) {
          gen.append("cycle == " + (il - 1) + " ? ");
        }
        gen.append("{" + (af.isSigned() ? ("{" + (op - ih - 1) + "{" + in + "[" + ih + "]}}")
            : ((op - ip - 1) + "'b0")) + "," + in + bitRange(ih, 0) + "}");
        if (il < ol) {
          gen.append(" : ");
        }
      }

      // only extension after input
      if (il < ol) {
        gen.append(af.isSigned() ? ("{" + op + "{sign}}") : (op + "'b0"));
      }
    }
    res.append(getResultPort().getAssignment(gen.toString()));
    if (isMultiCycle()) res.append(RESULT_VALID.getAssignment("cycle >= " + (getLatency()-getOutputLatency()) + " ? 1 : 0"));
    else                res.append(RESULT_VALID.getAssignment(START));
    return res.toString();
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

  /**
   * At the bit width optimization level, conversion operators do not modify
   * the input value
   */
  @Override
  public Range getResultRange(Range a) {
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