package junit.accuracy;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.*;

import org.hamcrest.core.IsEqual;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import accuracy.Format;
import accuracy.Format.Boolean;
import accuracy.Format.FixedPoint;
import accuracy.Format.FloatingPoint;
import accuracy.Format.Integer;

/**
 * Unit tests for {@code Format}s.
 * 
 * @author Andreas Engel [engel@esa.cs.tu-darmstadt.de]
 */
@RunWith(Enclosed.class)
public class FormatTest {

  /**
   * Test {@code Format#parse(String) with valid inputs.
   */
  @RunWith(Parameterized.class)
  public static class ParseValid {
    
    /**
     * Generate test vector.
     * @return  test vector
     */
    @Parameters(name= "{0}")
    public static Object[][] data() {
      return new Object[][] { 
        {"bool",      true,  Format.Boolean.INSTANCE},
        {"int",       false, Format.INT},
        {"long",      false, Format.LONG},
        {"float",     false, Format.FLOAT},
        {"double",    false, Format.DOUBLE},
        {"ieee754sp", false, Format.FLOAT},
        {"ieee754dp", false, Format.DOUBLE},
        {"int8",      true,  new Format.Integer(8)},
        {"uint10",    true,  new Format.Integer(10, false)},
        {"fix0x5",    true,  new Format.FixedPoint(0, 5)},
        {"ufix20x12", true,  new Format.FixedPoint(20, 12, false)},
        {"float5x13", true,  new Format.FloatingPoint(5, 13)}
      };
    }
    
    /**
     * To be parsed.
     */
    @Parameter(0)
    public String description;
  
    /**
     * Description is canonical.
     */
    @Parameter(1)
    public boolean canonical;
    
    /**
     * Expected {@code Format}.
     */
    @Parameter(2)
    public Format expected;
    
    /**
     * Compare parsed {@code Format} against expected {@code Format} class.
     */
    @Test
    public void shouldEqual() {
      Format parsed = Format.parse(description);
      assertThat(parsed, equalTo(expected));
      if (canonical) assertThat(parsed.getCanonicalName(), equalTo(description));
    }
    
    
  }
  
  /**
   * Test {@code Format#parse(String) with invalid inputs.
   */
  @RunWith(Parameterized.class)
  public static class ParseInvalid {
    
    /**
     * Generate test vector.
     * @return  test vector
     */
    @Parameters(name= "{0}")
    public static Object[] data() {
      return new Object[] {"bool5", "int-3", "int4k", "int0", "uint0", "fix9x-1", "ufix-1x0", "", null};
    }
    
    /**
     * To be parsed.
     */
    @Parameter(0)
    public String description;
  
    /**
     * Test thrown exception.
     */
    @Test(expected = IllegalArgumentException.class)
    public void parseFormat() {
      Format.parse(description);
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