package junit.target;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import operator.Implementation;
import operator.Operator;

import static target.Processor.Instance;


/**
 * Unit tests for the {@code Processor}s.
 * 
 * The tests are located in the top level class and in nested classes. 
 * When running this class, only the top level tests will be executed.
 * Superior test suits have to take care to also execute the nested tests.
 * 
 * @author Andreas Engel [engel@esa.cs.tu-darmstadt.de]
 */
@RunWith(Parameterized.class)
public abstract class ProcessorTest {
  
    
  /**
   * Assert {@code Operator} enumeration type
   */
//  @Test
//  public void getOperatorType() {
//    assertThat(Instance.getOperationType(), sameInstance(OP.class));
//  }
  
  /**
   * {@code Operator} under test.
   */
  @Parameter(0)
  public Operator op;
    
  /**
   * Test the {@code Operator} search implementation.
   */
  @Test
  public void getOperatorByName() {
    assertThat(Instance.getOperatorByName(op.toString()), is(op));
  }
  
  /**
   * Unit tests for the {@code UltraSynth} {@code Processor}.
   */
  public static class UltraSynth extends ProcessorTest {
    
    @Before
    public void setup() {
      Instance = target.UltraSynth.Instance;
    }
    
    /**
     * Generate test vector.
     * @return  all {@link Operator}s
     */
    @Parameters(name="{0}")
    public static Object[] getTestVector() {
      return target.UltraSynth.Instance.getOperators();
    }
  }
  
  /**
   * Unit tests for the {@code Amidar} {@code Processor}.
   * As the datapath width of the {@link Processor} and the I/O {@link Format} of each {@link Operator} is fixed, 
   * the (I/O) latency can be asserted.
   */
  public static class Amidar extends ProcessorTest {
    
    @Before
    public void setup() {
      Instance = target.Amidar.Instance;
    }
    
    /**
     * Expected input latency.
     */
    @Parameter(1)
    public int inputLatency;
    
    /**
     * Expected output latency.
     */
    @Parameter(2)
    public int outputLatency;
    
    /**
     * Expected latency.
     */
    @Parameter(3)
    public Integer latency;
    
    /**
     * Generate test vector.
     * @return  all {@link Operator}s
     */
    @Parameters(name="{0}")
    public static Object[] getTestVector() {
      Instance = target.Amidar.Instance;
      return new Object[][] {
        
        // operator                    input latency, output latency, latency
        {target.Amidar.OP.NOP,                     1,              1,       1},
        
        {target.Amidar.OP.IADD,                    1,              1,       1},
        {target.Amidar.OP.ISUB,                    1,              1,       1},
        {target.Amidar.OP.IMUL,                    1,              1,    null},
        {target.Amidar.OP.IDIV,                    1,              1,    null},
        {target.Amidar.OP.IREM,                    1,              1,    null},
        {target.Amidar.OP.IOR,                     1,              1,       1},
        {target.Amidar.OP.IAND,                    1,              1,       1},
        {target.Amidar.OP.IXOR,                    1,              1,       1},
        {target.Amidar.OP.ISHL,                    1,              1,       1},
        {target.Amidar.OP.ISHR,                    1,              1,       1},
        {target.Amidar.OP.IUSHR,                   1,              1,       1},
        
        {target.Amidar.OP.LADD,                    2,              2,       2},
        {target.Amidar.OP.LSUB,                    2,              2,       2},
        {target.Amidar.OP.LMUL,                    2,              2,    null},
        {target.Amidar.OP.LDIV,                    2,              2,    null},
        {target.Amidar.OP.LREM,                    2,              2,    null},
        {target.Amidar.OP.LOR,                     2,              2,       2},
        {target.Amidar.OP.LAND,                    2,              2,       2},
        {target.Amidar.OP.LXOR,                    2,              2,       2},
        {target.Amidar.OP.LSHL,                    2,              2,    null},
        {target.Amidar.OP.LSHR,                    2,              2,    null},
        {target.Amidar.OP.LUSHR,                   2,              2,    null},
        {target.Amidar.OP.LCMP,                    2,              1,    null},
        
        {target.Amidar.OP.FADD,                    1,              1,    null},
        {target.Amidar.OP.FSUB,                    1,              1,    null},
        {target.Amidar.OP.FMUL,                    1,              1,    null},
        {target.Amidar.OP.FDIV,                    1,              1,    null},
        {target.Amidar.OP.DADD,                    2,              2,    null},
        {target.Amidar.OP.DSUB,                    2,              2,    null},
        {target.Amidar.OP.DMUL,                    2,              2,    null},
        {target.Amidar.OP.DDIV,                    2,              2,    null},
        
        {target.Amidar.OP.CACHE_FETCH,             1,              1,       2},
        {target.Amidar.OP.DMA_LOAD,                1,              1,       2},
        {target.Amidar.OP.DMA_STORE,               1,              0,       1},
        {target.Amidar.OP.CACHE_FETCH64,           2,              2,       3}, // read address has to be applied
        {target.Amidar.OP.DMA_LOAD64,              2,              2,       3}, // for both result words
        {target.Amidar.OP.DMA_STORE64,             2,              0,       2},
        {target.Amidar.OP.CACHE_ARRAY_FETCH,       1,              1,       2},
        {target.Amidar.OP.CACHE_ARRAY_LOAD,        1,              1,       2},
        {target.Amidar.OP.CACHE_ARRAY_STORE,       1,              0,       1},
        {target.Amidar.OP.CACHE_ARRAY_FETCH64,     2,              2,       3},
        {target.Amidar.OP.CACHE_ARRAY_LOAD64,      2,              2,       3},
        {target.Amidar.OP.CACHE_ARRAY_STORE64,     2,              0,       2},
        {target.Amidar.OP.ROM_LOAD,                1,              1,       2},
        {target.Amidar.OP.ROM_LOAD64,              2,              2,       3},
        {target.Amidar.OP.ROM_ARRAY_LOAD,          1,              1,       2},
        {target.Amidar.OP.ROM_ARRAY_LOAD64,        2,              2,       3},
        
        {target.Amidar.OP.IFEQ,                    1,              0,       1}, // status output only 
        {target.Amidar.OP.IFNE,                    1,              0,       1}, // => does not occupy ALU input
        {target.Amidar.OP.IFLT,                    1,              0,       1},
        {target.Amidar.OP.IFGE,                    1,              0,       1},
        {target.Amidar.OP.IFGT,                    1,              0,       1},
        {target.Amidar.OP.IFLE,                    1,              0,       1},
        
        {target.Amidar.OP.CI_CMP,                  1,              1,    null},
        {target.Amidar.OP.HANDLE_CMP,              1,              1,    null},
        {target.Amidar.OP.FCMPG,                   1,              1,    null},
        {target.Amidar.OP.FCMPL,                   1,              1,    null},
        {target.Amidar.OP.DCMPG,                   2,              1,    null},
        {target.Amidar.OP.DCMPL,                   2,              1,    null},
        
        {target.Amidar.OP.MUX,                     1,              1,       1},
        
        {target.Amidar.OP.I2L,                     1,              2,    null},
        {target.Amidar.OP.I2F,                     1,              1,    null},
        {target.Amidar.OP.I2D,                     1,              2,    null},
        {target.Amidar.OP.L2I,                     2,              1,    null},
        {target.Amidar.OP.L2F,                     2,              1,    null},
        {target.Amidar.OP.L2D,                     2,              2,    null},
        {target.Amidar.OP.F2I,                     1,              1,    null},
        {target.Amidar.OP.F2L,                     1,              2,    null},
        {target.Amidar.OP.F2D,                     1,              2,    null},
        {target.Amidar.OP.D2I,                     2,              1,    null},
        {target.Amidar.OP.D2L,                     2,              2,    null},
        {target.Amidar.OP.D2F,                     2,              1,    null},
        {target.Amidar.OP.I2B,                     1,              1,    null},
        {target.Amidar.OP.I2C,                     1,              1,    null},
        {target.Amidar.OP.I2S,                     1,              1,    null},
        
        {target.Amidar.OP.INEG,                    1,              1,    null},
        {target.Amidar.OP.LNEG,                    2,              2,    null},
        {target.Amidar.OP.FNEG,                    1,              1,    null},
        {target.Amidar.OP.DNEG,                    2,              2,    null},
      };
    }
    
    /**
     * Verify the input latency.
     */
    @Test
    public void getLatency() {
      Implementation imp = op.createDefaultImplementation();
                           assertThat("inputLatency",  imp.getInputLatency(),  is(inputLatency));
                           assertThat("outputLatency", imp.getOutputLatency(), is(outputLatency));
      if (latency != null) assertThat("latency",       imp.getLatency(),       is(latency));
    }
    
  }
  
}

/*
 * Copyright (c) 2017,
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