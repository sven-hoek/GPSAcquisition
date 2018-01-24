package junit;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Collection of all unit tests.
 * This suite can also be executed as Java application to generate log files.
 * 
 * @author Andreas Engel [engel@esa.cs.tu-darmstadt.de]
 */
@RunWith(Suite.class)
@SuiteClasses({
  junit   .accuracy   .BigNumberTest                      .class,
  junit   .accuracy   .FormatTest                         .class,
  junit   .accuracy   .RangeTest                          .class,
  junit   .graph      .ANodeTest                          .class,
  junit   .operator   .ImplementationTest                 .class,
  junit   .operator   .OperatorTest         .Amidar       .class,
  junit   .operator   .OperatorTest         .UltraSynth   .class,
  junit   .target     .ProcessorTest        .Amidar       .class,
  junit   .target     .ProcessorTest        .UltraSynth   .class,
})
public class All {
  
  /**
   * Execute all unit tests, dump errors (and log files)
   * @param args ignored
   */
  public static void main(String[] args) {
    boolean pass = true;
    for (Class<?> c : ((SuiteClasses) All.class.getAnnotation(SuiteClasses.class)).value()) {
      Result result = JUnitCore.runClasses(c);
      
      System.out.println(result.getFailureCount() + " failures detected in " + c);
      for (Failure failure : result.getFailures()) {
        System.out.println(failure.toString());
      }
      
      if (!result.wasSuccessful()) pass = false; 
    }
    
    if (!pass) {
      // dump some lock to prevent pushing to master branch (https://dev.ghost.org/prevent-master-push)
    } else {
      // clear lock
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