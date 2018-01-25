package javasim.synth.model.instruction;

import javasim.synth.SynthData;
import javasim.synth.model.I;

import java.util.Iterator;

/**
 * This class abstracts away the common behaviour of IF* instructions.
 * Especially the insertion into the graph.
 * @author Michael Raitza
 * @version - 14.04.2011
 */
public abstract class AbstractIF extends Instruction {

	/**
	 * The phi-node that merges this branch in the end
	 */
	private PHIInstr phi_node;
	
	/**
	 * True if this if-instruction is a loop controller
	 */
	private boolean loopcontroller;
	
	/**
	 * True if there is a short circuit evaluation in the true branch
	 */
	private boolean shortcircuitevaluationTrueBranch = false;
	
	/**
	 * True if there is a short circuit evaluation in the false branch
	 */
	private boolean shortcircuitevaluationFalseBranch = false;
	
	/**
	 * The controller of the short circuit evaluation in the true branch
	 */
	private AbstractIF sceControllerTrueBranch;
	
	/**
	 * The controller of the short circuit evaluation in the false branch
	 */
	private AbstractIF sceControllerFalseBranch;
	
	/**
	 * The decision of the short circuit evaluation controller in the true branch
	 */
	private boolean sceControllerTrueBranchDecision;
	
	/**
	 * The decision of the short circuit evaluation controller in the false branch
	 */
	private boolean sceControllerFalseBranchDecision;

	/**
	 * Constructs a new if statement of type <code>instr</code> at
	 * the address <code>pos</code>.
	 * @param instr the instruction type
	 * @param pos the address of the instruction
	 */
	public AbstractIF(I instr, Integer pos) {
		super(instr, pos);
		phi_node = null;
		loopcontroller = false;
	}

	/**
	 * Returns the Φ node of this IF* instruction.
	 * @return the Φ node of this IF* instruction
	 */
	public PHIInstr phi_node() {
		return phi_node;
	}

	/**
	 * Evaluates the IF* instruction and inserts it into the data graph.
	 * @param data holds the synthesis context as a SynthData object
	 */
	public void eval(SynthData data) {
		
		//dbg_instr();
		Iterator<Instruction> it = data.ig().successors(this).iterator();
		Instruction i = it.next();
		i.vstack(vstack().split(i.decision()));
		data.pushd(i);
		i = it.next();
		i.vstack(vstack().split(i.decision()));
		data.pushd(i);
	}

	/**
	 * Inserts this IF* instruction into the graph.
	 * A new Φ node is registered and the first instructions of each
	 * the THEN and the ELSE branch are created linked to this IF*
	 * and pushed onto the graph construction stack.
	 *
	 * @param data holds the synthesis context as a SynthData object
	 * @see javasim.synth.SynthData#push
	 */
	public void insert(SynthData data) {
		/* create the Φ node */
		PHIInstr phi_node = (PHIInstr)I.SYNTH_PHI.create(addr() + data.stop_addr());
		this.phi_node = phi_node;
		phi_node.branchpoint(this.branchpoint());
		phi_node.decision(this.decision());
		phi_node.ifinstr(this);
		data.ig().reg_phi(phi_node);

		/* create the dummy node, which lies in the THEN branch */
		Instruction dummy = I.SYNTH_DUMMY.create(addr() + data.stop_addr());
		dummy.branchpoint(phi_node);
		dummy.decision(true);
		data.ig().insert(this, dummy);

		/* create the dummy node, which lies in the ELSE branch */

		Instruction dummy2 = I.SYNTH_DUMMY.create(-1 * (addr() + data.stop_addr()));
		dummy2.branchpoint(phi_node);
		dummy2.decision(false);
		data.ig().insert(this, dummy2);

		/* create the ELSE instruction */
		Instruction i = I.get_new(data, addr() + i().size());
		i.branchpoint(phi_node);
		i.decision(false);
		data.push(i);
		data.ig().insert(dummy2, i);

		/* calculate / attach to jump target */
		Integer target = data.code_w(addr()+1) + addr();
		
		
		
		
		if (target <= data.stop_addr()) {
			/* create the THEN instruction */
			i = I.get_new(data, target);
			i.branchpoint(phi_node);
			i.decision(true);
			data.push(i);
			data.ig().insert(dummy, i);
		} else {
			data.ig().insert(dummy, data.stop());
		}
	}

	/**
	 * Marks this instruction as a loopcontroller.
	 */
	public void set_loopcontroller() {
		loopcontroller = true;
	}

	/**
	 * Returns TRUE iff this IF* instruction is a loop controller.
	 */
	public boolean loopcontroller() {
		return loopcontroller;
	}

	/**
	 * Returns true when there is a short circuit evaluation
	 * @return
	 */
	public boolean isShortcircuitevaluation() {
		return shortcircuitevaluationTrueBranch || shortcircuitevaluationFalseBranch;
	}
	
	/**
	 * Returns true when there is a short circuit evaluation in the true branch
	 * @return
	 */
	public boolean isShortcircuitevaluationTrueBranch(){
		return shortcircuitevaluationTrueBranch;
	}
	
	/**
	 * Returns true when there is a short circuit evaluation in the false branch
	 * @return
	 */
	public boolean isShortcircuitevaluationFalseBranch(){
		return shortcircuitevaluationFalseBranch;
	}
	
	/**
	 * Returns the controller of the short circuit evaluation in the true branch
	 * @return
	 */
	public AbstractIF getSceControllerTrueBranch(){
		return sceControllerTrueBranch;
	}
	
	/**
	 * Returns the controller of the short circuit evaluation in the false branch
	 * @return
	 */
	public AbstractIF getSceControllerFalseBranch(){
		return sceControllerFalseBranch;
	}

	/**
	 * Returns the decision of the short circuit evaluation controller in the true branch
	 * @return
	 */
	public boolean getSceControllerTrueBranchDecision(){
		return sceControllerTrueBranchDecision;
	}	
	
	/**
	 * Returns the decision of the short circuit evaluation controller in the false branch
	 * @return
	 */
	public boolean getSceControllerFalseBranchDecision(){
		return sceControllerFalseBranchDecision;
	}
	
	/**
	 * Defines the short circuit evaluation
	 * @param phi The phi-node that merges the sce branches
	 * @param mn_decision wheter the sce is in true or false branch of this if instruction
	 * @param ctrl_desicion the decision of the sce
	 */
	public void setShortcircuitevaluation(PHIInstr phi, boolean mn_decision, boolean ctrl_desicion) {
		if(mn_decision){
			shortcircuitevaluationTrueBranch = true;
			sceControllerTrueBranch = phi.ifinstr();
			sceControllerTrueBranchDecision = ctrl_desicion;
		}else {
			shortcircuitevaluationFalseBranch = true;
			sceControllerFalseBranch = phi.ifinstr();
			sceControllerFalseBranchDecision = ctrl_desicion;
		}
	}
}
