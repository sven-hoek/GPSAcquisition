package operator;

import java.util.Collections;

import accuracy.Format;

/**
 * Register file access.
 * Used for scheduling only.
 */
public abstract class RegFile extends Implementation {

  /** Unique ID for (de)marshalling */
  private static final long serialVersionUID = -4064061232468046275L;

  /**
   * Default constructor required for demarshalling
   */
  protected RegFile() {}
  
  /**
   * Generate a register file (read or write) access.
   * @param dataWidth  the data input/output width
   */
  protected RegFile(int dataWidth ) {
    super(Collections.emptyList(), new Format.Integer(dataWidth));
  }
  
  @Override
  public boolean isNative() {
    return true;
  }
  
  @Override
  public boolean isRegfileAccess() {
    return true;
  }
 
  /**
   * Register file read access.
   */
  public static class LOAD extends RegFile {

    /** Unique ID for (de)marshalling */
    private static final long serialVersionUID = -7276531990244399599L;

    /**
     * Default constructor required for demarshalling
     */
    protected LOAD() {}
  
  /**
     * Generate a register file read access
     * @param dataWidth the data output width
     */
    public LOAD(int dataWidth) {
      super(dataWidth);
    }
    
  }
  
  /**
   * Register file write access.
   * Also contains an arithmetic output as the scheduler might directly reuse the stored value.
   */
  public static class STORE extends RegFile {
    
    /** Unique ID for (de)marshalling */
    private static final long serialVersionUID = 3855703145332265649L;

    /**
     * Default constructor required for demarshalling
     */
    protected STORE() {}
  
    /**
     * Generate a register file write access
     * @param dataWidth the data input width
     */
    public STORE(int dataWidth) {
      super(dataWidth);
      operandFormat.add(new Format.Integer(dataWidth));
    }
    
    @Override
    public boolean isStore() {
      return true;
    }
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