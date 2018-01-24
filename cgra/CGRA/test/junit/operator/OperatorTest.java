package junit.operator;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import accuracy.Format;
import operator.Implementation;
import operator.Operator;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Unit tests for the {@code Operator}s and their supported {@code Implementation}s.
 * 
 * @author Andreas Engel [engel@esa.cs.tu-darmstadt.de]
 */
@RunWith(Parameterized.class)
public abstract class OperatorTest {
  
  /**
   * Generate test vector.
   * @return  all {@link Operator}s and all supported {@code Implementation}s
   */
  public static Collection<Object[]> getTestVector() {
    LinkedList<Object[]> params = new LinkedList<Object[]>();
    for (Operator op : target.Processor.Instance.getImplementedOperators()) {
      for (Implementation imp : op.getAllImplementations()) {
        params.add(new Object[] {op, imp});
      }
    }
    return params;
  }
  
  /**
   * {@code Operator} under test.
   */
  @Parameter(0)
  public Operator op;
  
  /**
   * {@code Implementation} under test
   */
  @Parameter(1)
  public Implementation imp;
  
  @Test
  public void canBeImplemented() {
    assertNotNull(imp.getModule());
  }

  /**
   * Unit tests for {@code UltraSynth} {@code Operator}s and their supported {@code Implementation}s.
   */
  public static class UltraSynth extends OperatorTest {
    
    /**
     * Generate test vector.
     * @return  all {@link Operator}s and all supported {@code Implementation}s
     */
    @Parameters(name="{0} implemented as {1}")
    public static Collection<Object[]> getTestVector() {
      target.Processor.Instance = target.UltraSynth.Instance;
      return OperatorTest.getTestVector();
    }


  }
  
  /**
   * Unit tests for {@code Amidar} {@code Operator}s and their supported {@code Implementation}s.
   */
  public static class Amidar extends OperatorTest {
    
    /**
     * Generate test vector.
     * @return  all {@link Operator}s using their default {@link Format}s
     */
    @Parameters(name="{0} implemented as {1}")
    public static Collection<Object[]> getTestVector() {
      target.Processor.Instance = target.Amidar.Instance;
      return OperatorTest.getTestVector();
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