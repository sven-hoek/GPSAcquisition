package junit.operator;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import accuracy.Format;
import generator.Module;
import generator.Module.Port;
import operator.*;

import operator.Memory.FETCH;
import operator.Memory.LOAD;
import operator.Memory.STORE;
import operator.Memory.ROM;
import operator.Logarithm.LN;
import operator.Logic.AND;
import operator.Logic.OR;
import operator.Logic.XOR;
import operator.Relation.EQ;
import operator.Select.MAX;
import operator.Select.MIN;
import operator.Trigonometric.SIN;

/**
 * Unit tests for non-native {@Operator} @{code Implementation}s.
 * 
 * @author Andreas Engel [engel@esa.cs.tu-darmstadt.de]
 */
@RunWith(Parameterized.class)
public class ImplementationTest {
  
  private static Format I = Format.INT;
  private static Format F = new Format.FixedPoint(20, 30);
  private static Format D = Format.DOUBLE;
  
  
  /**
   * Generate test vector.
   */
  @Parameters(name="{0}")
  public static Implementation[] data() {
    return new Implementation[] {
      new ABS    (I,  I),       new ABS    (F,  F),       new ABS    (D,  D),       new ABS    (I,  D),
      new ADD    (I,I,I),       new ADD    (F,F,F),       new ADD    (D,D,D),       new ADD    (I,F,D),
      new Compare(I,I,I),       new Compare(F,F,F),       new Compare(D,D,D),       new Compare(I,F,D),
      new Compare(I,I,I, true), new Compare(F,F,F, true), new Compare(D,D,D, true), new Compare(I,F,D, true),
      new DIV    (I,I,I),       new DIV    (F,F,F),       new DIV    (D,D,D),       new DIV    (I,F,D),
      new FETCH  (I,I,16,false),new FETCH  (I,I,32,false),new FETCH  (I,I,48,false),new FETCH  (I,I,64,false),
      new LOAD   (I,I,16,false),new LOAD   (I,I,32,false),new LOAD   (I,I,48,false),new LOAD   (I,I,64,false),
      new STORE  (I,I,16,false),new STORE  (I,I,32,false),new STORE  (I,I,48,false),new STORE  (I,I,64,false),
      new FETCH  (I,I,16,true ),new FETCH  (I,I,32,true ),new FETCH  (I,I,48,true ),new FETCH  (I,I,64,true ),
      new LOAD   (I,I,16,true ),new LOAD   (I,I,32,true ),new LOAD   (I,I,48,true ),new LOAD   (I,I,64,true ),
      new STORE  (I,I,16,true ),new STORE  (I,I,32,true ),new STORE  (I,I,48,true ),new STORE  (I,I,64,true ),
      new ROM    (I,I,16,false),new ROM    (I,I,32,false),new ROM    (I,I,48,false),new ROM    (I,I,64,false),
      new ROM    (I,I,16,true ),new ROM    (I,I,32,true ),new ROM    (I,I,48,true ),new ROM    (I,I,64,true ),
      new EXP    (I,  I),       new EXP    (F,  F),       new EXP    (D,  D),       new EXP    (I,  D),
      new LN     (I,  I),       new LN     (F,  F),       new LN     (D,  D),       new LN     (I,  D),
      new AND    (I,I,I,true),  new AND    (F,F,F,true),  new AND    (D,D,D,true),  new AND    (I,F,D,true),
      new AND    (I,I,I,false), new AND    (F,F,F,false), new AND    (D,D,D,false), new AND    (I,F,D,false),
      new OR     (I,I,I,true),  new OR     (F,F,F,true),  new OR     (D,D,D,true),  new OR     (I,F,D,true),
      new OR     (I,I,I,false), new OR     (F,F,F,false), new OR     (D,D,D,false), new OR     (I,F,D,false),
      new XOR    (I,I,I,true),  new XOR    (F,F,F,true),  new XOR    (D,D,D,true),  new XOR    (I,F,D,true),
      new XOR    (I,I,I,false), new XOR    (F,F,F,false), new XOR    (D,D,D,false), new XOR    (I,F,D,false),
      new MOD    (I,I,I),       new MOD    (F,F,F),       new MOD    (D,D,D),       new MOD    (I,F,D),
      new MUL    (I,I,I),       new MUL    (F,F,F),       new MUL    (D,D,D),       new MUL    (I,F,D),
      new MUX    (I,I,I),       new MUX    (F,F,F),       new MUX    (D,D,D),       new MUX    (I,F,D),
      new NEG    (I,  I),       new NEG    (F,  F),       new NEG    (D,  D),       new NEG    (I,  D),
      new NOP    (I),           new NOP    (F),           new NOP    (D),           
      new NOT    (I,  I),       new NOT    (F,  F),       new NOT    (D,  D),       new NOT    (I,  D),
      new POW    (I,I,I),       new POW    (F,F,F),       new POW    (D,D,D),       new POW    (I,F,D),
      new RAND   (I),           new RAND   (F),           new RAND   (D),           
      new EQ     (I,I,true),    new EQ     (F,F,true),    new EQ     (D,D,true),    new EQ     (I,D,true),
      new EQ     (I,I,false),   new EQ     (F,F,false),   new EQ     (D,D,false),   new EQ     (I,D,false),
      new REM    (I,I,I),       new REM    (F,F,F),       new REM    (D,D,D),       new REM    (I,F,D),
      new ROL    (I,I,I),       new ROL    (F,I,F),       new ROL    (D,I,D),       new ROL    (I,I,D),
      new ROR    (I,I,I),       new ROR    (F,I,F),       new ROR    (D,I,D),       new ROR    (I,I,D),
      new MAX    (I,I,I),       new MAX    (F,F,F),       new MAX    (D,D,D),       new MAX    (I,F,D),
      new MIN    (I,I,I),       new MIN    (F,F,F),       new MIN    (D,D,D),       new MIN    (I,F,D),
      new SGN    (I,  I),       new SGN    (F,  F),       new SGN    (D,  D),       new SGN    (I,  D),
      new SHL    (I,I,I),       new SHL    (F,I,F),       new SHL    (D,I,D),       new SHL    (I,I,D),
      new SHR    (I,I,I),       new SHR    (F,I,F),       new SHR    (D,I,D),       new SHR    (I,I,D),
      new SQR    (I,  I),       new SQR    (F,  F),       new SQR    (D,  D),       new SQR    (I,  D),
      new SQRT   (I,  I),       new SQRT   (F,  F),       new SQRT   (D,  D),       new SQRT   (I,  D),
      new SUB    (I,I,I),       new SUB    (F,F,F),       new SUB    (D,D,D),       new SUB    (I,F,D),
      new SIN    (I,  I),       new SIN    (F,  F),       new SIN    (D,  D),       new SIN    (I,  D),
      new USHR   (I,I,I),       new USHR   (F,I,F),       new USHR   (D,I,D),       new USHR   (I,I,D)
    };
  }
  
  /**
   * Unit under test
   */
  @Parameter(0)
  public Implementation imp;
  
  @Test
  public void checkPorts() {
    List<Port> ports = imp.getPorts();
    int wmapw = imp.getWideMemAccessPortWidth();
    
                                     assertThat(ports, hasItem(Module.START));
    if (imp.isMultiCycle())          assertThat(ports, hasItem(Module.CLOCK));
    if (imp.isMultiCycle())          assertThat(ports, hasItem(Module.RESET));
    if (imp.isMultiCycle() && target.Processor.Instance.isStallable())  
                                     assertThat(ports, hasItem(Module.ENABLE));
    if (imp.isControlFlow())         assertThat(ports, hasItem(Module.STATUS_VALID));
    if (imp.isCacheAccess())         assertThat(ports, hasItem(Module.CBOX));
    if (imp.isCacheAccess())         assertThat(ports, hasItem(Module.COND));
    if (imp.isStore())               assertThat(ports, hasItem(Module.CACHE_WRITE));
    if (imp.isCachePrefetch())       assertThat(ports, hasItem(Module.CACHE_PREFETCH));
    if (imp.isIndexedMemAccess())    assertThat(ports, hasItem(Module.MEM_ARRAY));
    if (imp.isWideMemAccess())       assertThat(ports, hasItem(new Port(Module.MEM_WIDE, wmapw)));
    
    for (Port p : ports) {
      if (p.toString().equals(Module.RESULT.toString())) assertThat(ports, hasItem(Module.RESULT_VALID));
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
