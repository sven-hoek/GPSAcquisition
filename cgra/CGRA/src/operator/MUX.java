package operator;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import accuracy.Format;

/**
 * Binary multiplexer: R = C ? A : B The {@link #CBOX} signal is used to select
 * the output.
 *
 * @author Andreas Engel [engel@esa.cs.tu-darmstadt.de]
 */
public class MUX extends Implementation {

  /** Unique ID for (de)marshalling */
  private static final long serialVersionUID = 8250727229149497312L;

  public MUX(Format a, Format b, Format r) {
    super(Arrays.asList(a, b), r);
  }

  /**
   * Add {@link #CBOX} to port list. TODO: find a more generic solution for
   * variable port lists
   */
  @Override
  public List<Port> getPorts() {
    List<Port> res = super.getPorts();
    res.add(0, CBOX);
    return res;
  }
  
  @Override
  protected List<Integer> getSupportedIntegerLatency() {
    return new LinkedList<Integer>(Arrays.asList(1));
  }

  @Override
  public String getIntegerImplementation() {
    StringBuilder res = new StringBuilder();
    res.append(getResultPort().getAssignment(CBOX + " ? " + getOperandPort(0) + " : " + getOperandPort(1)));
    res.append(RESULT_VALID.getAssignment(START));
    return res.toString();
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