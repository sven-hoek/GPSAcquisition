

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.function.Supplier;

import org.json.simple.parser.ParseException;

import graph.IDP;
import operator.Operator;
import target.UltraSynth;


/**
 * Test {@link IDP} parsing and modification.
 *  
 * @author Andreas Engel [engel@esa.cs.tu-darmstadt.de]
 */
public class GraphGen extends IDP {
  
  /**
   * Measure the execution time of a computation.
   * @param description a brief description for the handler
   * @param handler     the handler to be executed
   * @return            the result of the handler
   */
  protected static <T> T stopWatch(String description, Supplier<T> handler) {
    long start = System.nanoTime();
    T res = handler.get();
    long stop  = System.nanoTime();
    System.out.println(description + String.format(" took %e s", (stop - start)/1.0e9));
    return res;
  }
  
  /**
   * Parse, modify and dump a graph.
   * The execution time of all intermediate steps will be measured. If any step modified the graph, a dot 
   * representation will be exported. Some detailed statistics of the final graph structure is also dumped.
   *   
   * @param graphName name graph, whose JSON file must be located at {@code dsc/<graphName>.ultrasynth.json}
   * @param alias     list of operations to be substituted by equivalent constructs 
   * @throws IOException
   */
  protected static void analyze(String graphName) throws IOException {
    int d = 0;
    
    // empty graph-specific output dir
    File dir = new File("out/" + graphName);
    if (dir.exists()) for (File f : dir.listFiles()) f.delete();
    dir.mkdirs();
    String path = dir + "/%d.%s";

    // construct empty graph
    GraphGen graph = new GraphGen();
    
    // parse graph from JSON
    String file = "dsc/" + graphName + ".ultrasynth.json";
    if (stopWatch("parsing " + file, () -> graph.parse(file))) {
      graph.dump(String.format(path, ++d, "json"));
    } else {
      return;
    }
    
    // graph optimizations
    for (Operator op : getSupportedAliasOperators()) {
      if (stopWatch("alias " + op, () -> graph.aliasOperation(op))) {
        graph.dump(String.format(path, ++d, "alias." + op));
      }
    }
    if (stopWatch("common subexpression elimination", () -> graph.commonSubexpressionElimination(false))) {
      graph.dump(String.format(path, ++d, "cse"));
    }
    if (stopWatch("constant propagation", () -> graph.constantPropagation())) {
      graph.dump(String.format(path, ++d, "propagate"));
    }
    if (stopWatch("dead code elimination", () -> graph.deadCodeElimination())) {
      graph.dump(String.format(path, ++d, "dce"));
    }

    // use AMIDAR-style flow control
    if (stopWatch("prepare status flags", () -> graph.prepareMuxDecision())) {
      graph.dump(String.format(path, ++d, "status"));
    }
    if (stopWatch("mux to predication", () -> graph.muxToPredication())) {
      graph.dump(String.format(path, ++d, "pred"));
    }

    // integrator expansion and further analysis
    
    if (stopWatch("change step size", () -> graph.setStepsize(1e-6))) {
      graph.dump(String.format(path, ++d, "stepsize"));
    }
    
    for (String integrator : Arrays.asList("euler", "heun")) {
      System.out.println();
      
      GraphGen g = (GraphGen) graph.clone();
      path = dir + "/%d." + integrator + ".%s";
      stopWatch("expand " + integrator, () -> {
        if (integrator.equals("euler")) g.expandEuler(); else g.expandHeun();
        return null;
      });
      g.dump(String.format(path, ++d, "expanded"));
      g.checkSanity();

      // collect and dump graph statistics
      String stat = stopWatch("generate statistic", () -> g.getStatistics());
      FileWriter fw = new FileWriter(String.format(path, ++d, "stat"));
      fw.write(stat);
      fw.close();
      
      // further optimizations not modifying the graph structure
      stopWatch("bit width analysis", () -> {
        g.bitwidthAnalysis(); 
        return null;
      });
      g.dump(String.format(path, ++d, "bwa"));
    }
    System.out.println("----------");
  }
  
  /**
   * Some basic testing.
   */
  public static void main(String[] args) throws FileNotFoundException, IOException, ParseException {
    target.Processor.Instance = UltraSynth.Instance;
//    analyze("Minimal");
    analyze("SinglePendulumHcs");
//    analyze("DoublePendulum");
//    analyze("LinearDriveControl");
//    System.out.println(parseAndOptimize("dsc/Minimal.ultrasynth.json",            true, 0.01).getStatistics());
//    System.out.println(parseAndOptimize("dsc/DoublePendulum.ultrasynth.json",     true, 0.01).getStatistics());
//    System.out.println(parseAndOptimize("dsc/LinearDriveControl.ultrasynth.json", true, 0.01).getStatistics());
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
