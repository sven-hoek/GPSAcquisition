/**
 * This package contains all classes that model a Bytecode instruction.
 * The Instructions are created by the Enumeration I (package javasim.synth.model).
 * The Instructions create instances of Datum (package javasim.synth.model.datum).
 * So the Instruction is the creator of this Datum. This datum is pushed on a virtual stack.
 * The next instruction  reads the former datum from the stack. So it is possible to 
 * calculate the dependencies. The dependencies are added to the DataGraph (package javasim.synth.model)
 */
package javasim.synth.model.instruction;