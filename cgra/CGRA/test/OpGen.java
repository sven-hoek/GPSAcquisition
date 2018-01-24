import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import accuracy.Format;
import operator.Implementation;
import operator.Operator;
import target.Amidar;
import target.UltraSynth;

/**
 * Dump {@code Operator} {@code Implementation}s.
 * 
 * @author Andreas Engel [engel@esa.cs.tu-darmstadt.de]
 */
public class OpGen {
  
  /**
   * Write text to file.
   * @param content      the text to write
   * @param fileName     the name of the file to write to
   * @throws IOException if file can not be written
   */
  public static void dump(String content, String fileName) throws IOException {
    System.out.println("[" + fileName + "]");
    System.out.println(content + "\n");
    FileWriter fw = new FileWriter(fileName);
    fw.write(content);
    fw.close();
  }

  /**
   * Write Verilog module, testbench and simulation scripts of an {@code Operator} {@code Implementation} to a folder.
   * 
   * @param imp          the {@code Operator} {@code Implementation} to be dumped
   * @param dir          the name of the folder to write to
   * @throws IOException if folder is not writable
   */
  public static void dump(Implementation imp, String dir) throws IOException {
    if (imp == null) return;
    
    File outDir = new File(dir);
    outDir.mkdir();
    if (!outDir.exists()) return;
    
    dump(imp.getModule(),    outDir + "/" + imp.getName()          + ".v");
//    dump(imp.getTestbench(), outDir + "/" + imp.getTestbenchName() + ".v");
    //dump(imp.getWaveTCL(),   outDir + "/" + imp.getTestbenchName() + ".wave");
    //dump(imp.getSimTCL(),    outDir + "/" + imp.getTestbenchName() + ".sim");
  }
  
  /**
   * Write Verilog module, testbench and simulation scripts of an {@code Operator} {@code Implementation} to "out".
   * 
   * @param imp          the {@code Operator} {@code Implementation} to be dumped
   * @throws IOException if "out" is not writable
   */
  public static void dump(Implementation imp) throws IOException {
    dump(imp, "out");
  }
  
  /**
   * Write Verilog module, testbench and simulation scripts for the default {@code Implementation} of an 
   * {@code Operator} to "out".
   * 
   * @param op           the {@code Operator} to be dumped
   * @throws IOException if "out" is not writable
   */
  public static void dump(Operator op) throws IOException {
    dump(op.createDefaultImplementation());
  }
  
  public static void listAll() {
    List<? extends Operator> ops = target.Processor.Instance.getImplementedOperators();
    for (Operator op : ops) {
      System.out.println(op);
      for (Implementation imp : op.getAllImplementations()) {
        System.out.println("  " + imp.getName());
      }
    }
  }
  
  public static void main(String[] args) throws IOException {
    target.Processor.Instance = target.Amidar.Instance;
    target.Processor.Instance = target.UltraSynth.Instance;
    listAll();
//    System.out.println(UltraSynth.OP.MOD.getAllImplementations().size());
    
//    Implementation imp = UltraSynth.OP.XOR.getRandomImplementation();
//    Implementation imp = Amidar.OP.IDIV.createDefaultImplementation();
//    imp.setCommonFormat(Format.parse(
////      "float29x10"
//        "int40"
//        "float4x11"
//    ));
//    imp.fitLatency();
//    dump(imp);
  }
}
