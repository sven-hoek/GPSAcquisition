import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import cgramodel.CgraModel;
import cgramodel.CgraModelAmidar;
import cgramodel.PEModel;
import generator.DriverUltrasynth;
import generator.TestbenchExecutor;
import generator.VerilogGenerator;
import operator.Implementation;
import operator.Operator;

public class Main {
  
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
   * Write Verilog module, testbench and simulation scripts of an {@code Operator} {@code Implementation} to a folder.a
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
    dump(imp.getTestbench(), outDir + "/" + imp.getTestbenchName() + ".v");
    dump(imp.getWaveTCL(),   outDir + "/" + imp.getTestbenchName() + ".wave");
    dump(imp.getSimTCL(),    outDir + "/" + imp.getTestbenchName() + ".sim");
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
  
  /**
   * Write the Verilog modules for a whole CGRA processor to "out"
   * 
   * @param template the JSON configuration used to build the CGRA
   */
  public static void dump(String template) {
    CgraModel cgra = target.Processor.Instance.getAttributeParser().loadCgra(template);
    cgra.finalizeCgra();
    VerilogGenerator vgen = target.Processor.Instance.getGenerator();
//    vgen.setDumpOperatorTestbench(true);
    vgen.printVerilogDescription("out",cgra);
    dump(cgra);
  }
  
  /**
   * Print a brief overview (operational spectrum and interconnect) of a {@code CGRAModel}. 
   * @param cgra the CGRA to print
   */
  public static void dump(CgraModel cgra) {
    for(PEModel pe :cgra.getPEs()){
      System.out.println(String.format("\nPE %-14d M=%d C=%d A=%d L=%d (%d,%d,%d)", 
          pe.getID(),
          pe.getMultiCycle()  ? 1 : 0,
          pe.getControlFlow() ? 1 : 0,
          pe.getMemAccess()   ? 1 : 0,
          pe.getLiveout()     ? 1 : 0,
          pe.getMaxWidthInputA(),
          pe.getMaxWidthInputB(),
          pe.getMaxWidthResult()));
      
      for (Operator op : pe.getAvailableOperators().keySet()){
        Implementation i = pe.getAvailableOperators().get(op);
        System.out.println(String.format("%10s : L=%02d M=%d C=%d A=%d N=%d (%s)", 
            op,
            i.getLatency(),
            i.isMultiCycle()  ? 1 : 0,
            i.isControlFlow() ? 1 : 0,
            i.isMemAccess()   ? 1 : 0,
            i.isNative()      ? 1 : 0,
            i));
      }
//      pe.getContext().printMask();
    }
  }
  

  /**
   * Run a Ultrasynth composition for testing purposes.
   * @param argCompositionName
   *      Specifies the composition to run which is its destination folder's name.
   */
  public static void runUltrasynth(String argCompositionName) {
//    target.Processor.Instance = target.UltraSynth.Instance;
      VerilogGenerator gen = target.Processor.Instance.getGenerator();

      TestbenchExecutor.runUltrasynth();
  }

  /**
   * Test Verilog generation.
   * 
  * @param args         see the case statement below 
   * @throws IOException if dumping of files fails
   */
  public static void main(String[] args) throws IOException {
    
    String currentDir = System.getProperty("user.dir");
      System.out.println("Current dir using System:"   +currentDir);
//
//      DriverUltrasynth driverUltrasynth = new DriverUltrasynth();
//      driverUltrasynth.generateOutput("ultrasynth4");
//      System.exit(0);
  
  CgraModel model = (CgraModelAmidar) target.Processor.Instance.getAttributeParser().loadCgra("config/amidar/CGRA_4.json");
//  
//  CGRASerializer selly = new CGRASerializer();
//  selly.serialize(model, "abc", "was");
//  System.exit(0);
  
  VerilogGenerator gen = target.Processor.Instance.getGenerator();
  model.finalizeCgra();
   gen.printVerilogDescription("out/testaftermerge/",model);
  System.exit(0);
    
    
  
    model = target.Processor.Instance.getAttributeParser().loadCgra("config/ultrasynth/ultrasynth16.json");
    
    
//    AttributeWriter aw = new AttributeWriterAmidar();
//    aw.writeJSON(model,"cgratest","test/testwriter");
//    CgraModelAmidar modelreload = (CgraModelAmidar) target.Processor.Instance.getAttributeParser().loadCgra("test/testwriter/cgratest.json");
//    if(model.getNrOfPEs() == modelreload.getNrOfPEs()){
//      if(model.getCacheConfiguration().equalsIgnoreCase(model.getCacheConfiguration())){
//        if(model.getcBoxModel().getNrOfEvaluationBlocks() == modelreload.getcBoxModel().getNrOfEvaluationBlocks()){
//          if(model.getNrOfControlFlowPEs() == modelreload.getNrOfControlFlowPEs()){
//            if(model.getPEs().get(0).getAvailableNonNativeOperators().size() == modelreload.getPEs().get(0).getAvailableNonNativeOperators().size()){
//               System.out.println("Seems equal");
//              } 
//            } 
//          } 
//      }
//    }
    
   

    System.exit(0);


    gen = target.Processor.Instance.getGenerator();
    gen.printVerilogDescription("out/test",model);
    System.exit(0);
  }
  
  
}
