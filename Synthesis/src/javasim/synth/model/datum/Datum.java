package javasim.synth.model.datum;

import javasim.synth.model.instruction.Instruction;
import javasim.synth.SynthData;

import java.util.*;

import dot.DotNode;

/**
 * A single node of the data flow graph. This node is of abstract type
 * and cannot be used directly.
 *
 * @author Michael Raitza
 * @version 14.04.2011
 */
public abstract class Datum extends DotNode implements Comparable<Datum> {

	/* I like successively enumerated datum nodes */
	private static int lfd = 0;

	/**
	 * The value associated with this datum. Has different meaning for different Datum classes
	 */
	private Number value;
	
	/**
	 * Unique datum identifier
	 */
	private final int did;
	
	/**
	 * The corresponding instruction
	 */
	private Instruction creator;
	
	/**
	 * The datum creating the reference of this datum
	 */
	private Datum reference;
	
	/**
	 * TODO What is this for? Do we need this?
	 */
	private Integer depth;
	
	
	/**
	 * The type of access. Read. Write. Stack write.
	 */
	public enum AccessType { READ, WRITE, STACK  };

	/**
	 * The type of data accessed. Local variable. Static object. Dynamic object. Stack. Constant. Merger. Sink. Pipe, Address.
	 */
	public enum Type { LOCAL_VARIABLE, STATIC_OBJECT, DYNAMIC_OBJECT, STACK, CONSTANT, MERGER, SINK, PIPE, ADDRESS };

	public static void resetStatic() {
		lfd = 0;
	}

	public boolean isAType() {
	    return type().equals(Type.STATIC_OBJECT) || type().equals(Type.DYNAMIC_OBJECT) || type().equals(Type.ADDRESS);
	}

	public boolean isLType() {
	    return type().equals(Type.LOCAL_VARIABLE);
	}

	private Datum() {
		super(null);
		did = 0;
		depth = 0;
	}

	/**
	 * Constructs a new datum. Sets the DOT graph attributes for data nodes
	 * like an initial label.
	 * @param value the "value" associated with this datum. The meaning of value highly
	 * depends on the type of node
	 * @param creator the instruction which created this node
	 */
	public Datum(Number value, Instruction creator) {
		super("d" + ++lfd, "\"" + value + ":d" + lfd + "(" + creator.attr("label").replaceAll("\"", "")  + ")\"");
		did = lfd;
		this.value = value;
		this.creator = creator;
		depth = 0;
	}

	/**
	 * Returns the value associated with this datum.
	 * @return the value associated with this datum
	 */
	public Number value() {
		return value;
	}
	
	/**
	 * Sets the value associated with this datum
	 * @param value
	 */
	public void value(Number value){
		this.value = value;
	}
	

	/**
	 * Returns the numerical id of this datum.
	 * @return the numerical id of this datum
	 */
	public int did() {
		return did;
	}

	/**
	 * Returns the instruction that created this datum.
	 * @return the instruction that created this datum
	 */
	public Instruction creator() {
		return creator;
	}

	/**
	 * Sets a datum that is referenced by this datum.
	 * @param reference the datum this one references
	 */ 
	public void reference(Datum reference) {
		this.reference = reference;
	}

	/**
	 * Returns the datum this datum references.
	 * @return the datum this datum references
	 */
	public Datum reference() {
		return reference;
	}

	/**
	 * Sets the depth of this node in the data graph.
	 * @param d the depth in the data graph
	 */
	public void depth(Integer d) {
		if (d >= depth)
			depth = d + 1;
	}

	/**
	 * Returns the depth of this node in the data graph.
	 * @return the depth of this node in the data graph
	 */
	public Integer depth() {
		return depth;
	}

	/**
	 * Implements the comparable interface. Two datum objects are
	 * compared by id.
	 */
	public int compareTo(Datum d) {
		return creator().addr()-d.creator().addr();
	}

	/* Relabels this datum to reflect recent state changes */
	private void relabel() {
		attr("label", "\"" + value + ":" + depth + ":d" + did + "(" + creator.attr("label").replaceAll("\"", "")  + ")\"");
	}

	/**
	 * Returns the string representation of this DOT node.
	 */
	public String toString() {
		relabel();
		return super.toString();
	}

	/**
	 * Signals whether this datum represents a read or write or stack write.
	 * @return the type of access
	 */
	public abstract AccessType accessType();

	/**
	 * Returns the type of data accessed.
	 * @return the type data of access
	 */
	public abstract Type type();

}
