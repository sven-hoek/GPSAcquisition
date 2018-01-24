package javasim.synth.model.instruction;

import javasim.synth.SynthData;
import javasim.synth.model.I;
import javasim.synth.model.VStack;
import javasim.synth.model.datum.Datum;

import java.util.*;

import dot.DotNode;

/**
 * A new implementation of an Instruction.
 * 
 * @author Michael Raitza
 * @version 15.04.2011
 */
public class Instruction extends DotNode implements Comparable<Instruction> {

	/**
	 * Represents the instruction type and thus carries the name and opcode.
	 */
	private final I instr;

	/**
	 * Represents the address this instruction lives at.
	 */
	private final Integer pos;

	/**
	 * Represents the branching instruction this instruction descends from.
	 */
	private PHIInstr branchpoint = null;

	/**
	 * Represents the decision with this instruction was branched of.
	 */
	private boolean decision;

	/**
	 * Represents the virtual stack seen by this instruction.
	 */
	private VStack vstack;
	
	/**
	 * Tells whether this is a duplicate instruction. Needed for Short Circuit Evaluation
	 */
	private int duplicate = 0;
	
	
	private boolean isReference = false;

	/**
	 * Constructs an instruction.
	 */
	private Instruction() {
		super(null);
		instr = null;
		pos = null;
		branchpoint = null;
		decision = false;
	}

	/**
	 * Constructs an instruction of type <code>instr</code> at the address
	 * <code>pos</code>.
	 * 
	 * @param instr
	 *            the instruction type
	 * @param pos
	 *            the address of the instruction
	 */
	public Instruction(I instr, Integer pos) {
		super(instr + Integer.toHexString(pos), "\"" + pos.toString() + ":"
				+ instr.toString() + "\"");
		this.instr = instr;
		attr("shape", "box");
		this.pos = pos;
		branchpoint = null;
		decision = false;
		vstack = new VStack();
	}

	/**
	 * Compares two instructions with each other. Two instructions are
	 * considered equal iff both their byte code address and instruction type
	 * match.
	 */
	public int compareTo(Instruction i) {
		int ret = pos - i.addr();
		if (ret == 0)
			return instr.c() - i.i().c();
		return ret;
	}
	
	public void isDuplicate(){
		duplicate = 1;
		id = id+"duplicate";
	}

	/**
	 * Returns the size of this instruction and its arguments.
	 * 
	 * @return an <code>Integer</code> object holding the size of this
	 *         instruction and its arguments.
	 */
	public Integer size() {
		return 1;
	}

	/**
	 * Returns the address of this instruction.
	 * 
	 * @return the address of this instruction
	 */
	public Integer addr() {
		return pos;
	}

	/**
	 * Returns the instruction type of this instruction.
	 * 
	 * @return the instruction type of this instruction
	 */
	public I i() {
		return instr;
	}

	/**
	 * Returns the hash code of this instruction. The hash code is made up of
	 * the hash code of the instruction type and its position. That means two
	 * instructions are considered to be the same instance iff the instructions
	 * types and addresses match. Unless it is specifically declared as a duplicate
	 * 
	 * @return the hash code
	 */
	public int hashCode() {
		return instr.hashCode() ^ pos.hashCode() ^ duplicate;
	}

	/**
	 * Decides whether two instructions are considered to be equal. Two
	 * instructions are considered to be equal iff the both exist and their
	 * instruction types are equal and their addresses are equal.
	 * 
	 * @param e
	 *            the instruction this instruction is compared against
	 * @return TRUE iff both instructions are equal
	 */
	public boolean equals(Object e) {
		Instruction i;
		try {
			i = (Instruction) e;
		} catch (NullPointerException f) {
			return false;
		} catch (ClassCastException k) {
			return false;
		}
		if(i == null)
			return false;
		if ((i.i().equals(instr)) && (i.addr().equals(pos)))
			return true;
		return false;
	}

	/**
	 * Sets the current branch point for this instruction to <code>bp</code>.
	 * 
	 * @param bp
	 *            the Instruction object holding the branching instruction.
	 */
	public void branchpoint(PHIInstr bp) {
		branchpoint = bp;
	}

	/**
	 * Returns the current branchpoint for this instruction.
	 * 
	 * @return the Instruction object representing the current branchpoint.
	 */
	public PHIInstr branchpoint() {
		return branchpoint;
	}

	/**
	 * Sets the decision value for this instance. The decision value stores if
	 * this instruction appears in an THEN or an ELSE branch of an IF statement.
	 * 
	 * @param d
	 *            the decision value to set, TRUE for the THEN branch, FALSE
	 *            otherwise (ELSE branch or no branch at all).
	 */
	public void decision(boolean d) {
		decision = d;
		relabel();
	}

	/**
	 * Returns the decision of the IF* instruction dominating this instruction.
	 * 
	 * @return the decision of the IF* instruction dominating this instruction.
	 */
	public boolean decision() {
		return decision;
	}

	/**
	 * Relabels the DOT node representing this instruction. Useful for debugging
	 * purposes.
	 */
	public void relabel() {
		attr("label", "\"" + pos.toString() + ":" + instr.toString() + " ("
				+ decision + ")\"");
	}

	/**
	 * Sets a new virtual stack for this instruction.
	 * 
	 * @param vstack
	 *            the new virtual stack for this instruction.
	 * @throws NullPointerException
	 *             if the parameter is a null pointer as null pointers are not
	 *             allowed.
	 */
	public void vstack(VStack vstack) throws NullPointerException {
		if (vstack == null)
			throw new NullPointerException("NULL stack not allowed.\n");
		this.vstack = vstack;
	}

	/**
	 * Returns the virtual stack for this instruction.
	 * 
	 * @return Returns a VStack object.
	 */
	public VStack vstack() {
		return vstack;
	}

	/* debugging function printing the contents of the vstack */
	@SuppressWarnings("unused")
	private String print_stack() {
		Formatter f = new Formatter();

		try {
			f.format("Stack for %d:%s\n", pos, instr);
			for (Datum d : vstack)
				f.format("    %s\n", d.attr("label"));

			return f.toString();
		} finally {
			f.close();
		}
	}

	/**
	 * Evaluates this instruction according its instruction type and selects the
	 * next instruction according to the control flow graph. Depending on the
	 * instruction type nodes are added to and connected in the data flow graph.
	 * Also depending on the instruction type it is evaluated differently.
	 * 
	 * @param data
	 *            holds the synthesis context as a SynthData object
	 * @see javasim.synth.model.I#eval
	 */
	public void eval(SynthData data) {
		Instruction i = data.ig().successors(this).iterator().next();
		
		i.vstack(vstack());
		data.pushd(i);
	}

	/**
	 * Inserts this instruction into the control flow graph.
	 * 
	 * @param data
	 *            holds the synthesis context as a SynthData object
	 */
	public void insert(SynthData data) {
		Instruction i = I.get_new(data, instr.size() + pos);
		
		i.branchpoint(branchpoint);
		i.decision(decision);
		data.push(i);
		data.ig().insert(this, i);
	}

	/**
	 * Always returns FALSE. This function is overwritten by AbstractIF.
	 */
	public boolean loopcontroller() {
		return false;
	}
	
	public boolean isReference(){
		return isReference;
	}
	
	public void isReference(boolean isReference){
		this.isReference = isReference;
	}
	
	public String toString(){
		return pos+":"+instr;
	}
}
