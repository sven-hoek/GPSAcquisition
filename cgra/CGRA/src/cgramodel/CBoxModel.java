package cgramodel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;

public class CBoxModel implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4649227269040722127L;

	public CBoxModel(){
		
	}
	
	/**
	 * Mask for context entries of the CBox
	 */
	public ContextMaskCBoxEvaluationBlock contextmaskcboxevaulationblocks = null;
	public ContextMaskCBoxWrapper contextmaskwrapper = null;

	public ContextMaskCBoxEvaluationBlock getContextmaskEvaLuationBlocks() {
		return contextmaskcboxevaulationblocks;
	}
	
	public ContextMaskCBoxWrapper getContextmaskWrapper() {
		return contextmaskwrapper;
	}
	
	public boolean finalized = false;
	
	public boolean isFinalized(){
		return finalized;
	}
	
	public void resetFinalized(){
		finalized = false;
	}
	
	/**
	 * Method to finalize the CBox. This mainly includes the creation of a valid contextmask for the CBox. 
	 * @param cgra model of the cgra
	 */
	public void finalizeCBox(CgraModel cgra){
		contextmaskcboxevaulationblocks = new ContextMaskCBoxEvaluationBlock();
		contextmaskwrapper = new ContextMaskCBoxWrapper();
		contextmaskwrapper.createMask(cgra);
		contextmaskcboxevaulationblocks.createMask(cgra);
		finalized = true;
	}
	
	/**
	 * The CBox holds the option to select several sources as the branch selection. This method returns the
	 * information whether one of the branch selection sources lead to a bypass of the condition memory. This
	 * again leads to an increased critical path in the CGRA. 
	 */
	public boolean getBranchSelectionBypass(){
		for(BranchSelectionConnection con : branchSelectionSources){
			if(con.memoryByPass)
				return true;
		}
		return false;
	}


	ArrayList<BranchSelectionConnection> branchSelectionSources = new ArrayList<BranchSelectionConnection>();
	 
	public ArrayList<BranchSelectionConnection> getBranchSelectionSources() {
		return branchSelectionSources;
	}

	
	/**
	 * Method to add a possible source for the branch selection signal
	 * @param con connection to be added
	 */
	public void addbranchSelectionSource(BranchSelectionConnection con){
		if(!branchSelectionSources.contains(con)){
			branchSelectionSources.add(con);
			finalized = false;
		}
	}
	
	/**
	 * Method to add a possible source for the branch selection signal
	 * @param con connection to be added as a String
	 */
	public void addbranchSelectionSourceByName(String con){
		addbranchSelectionSource(BranchSelectionConnection.getbranchSelectionConnectionsByName(con));
	}
	
	/**
	 * Method to remove a possible source for the branch selection signal
	 * @param con connection to be removed
	 */
	public void removebranchSelectionSource(BranchSelectionConnection con){
		if(branchSelectionSources.contains(con)){
			branchSelectionSources.remove(con);
			finalized = false;
		}
	}
	
	/**
	 * Method to remove a possible source for the branch selection signal
	 * @param con connection to be removed as a String
	 */
	public void removebranchSelectionSourceByName(String con){
		removebranchSelectionSource(BranchSelectionConnection.getbranchSelectionConnectionsByName(con));
	}
	
	/**
	 * Amount of status or conjunctions that can be stored in the C-Box. 
	 */
	int memoryslots = -1;
	
	/**
	 * Getter method
	 *
	 * @return slots in the memory of the CBox
	 */
	public int getMemorySlots() {
		return memoryslots;
	}
	
	/**
	 * Setter method
	 *
	 */
	public void setMemorySlots(int slots) {
		memoryslots = slots;
		finalized = false;
	}
	
	int cBoxPredicationOutputsPerBox = 1;
	int nrOfEvaluationBlocks = 1;
	
	public void setCBoxOutputsPerBox(int nrOfCboxOuputsPerBox){
		cBoxPredicationOutputsPerBox  = nrOfCboxOuputsPerBox;
	}
	
	public void setNrOfEvaluationBlocks(int nrOfCBoxes){
		this.nrOfEvaluationBlocks = nrOfCBoxes;
	}
	
	public int getCBoxPredicationOutputsPerBox(){
		return cBoxPredicationOutputsPerBox;
	}
	
	public int getNrOfEvaluationBlocks(){
		return nrOfEvaluationBlocks;
	}
	
	public enum BranchSelectionConnection{
		regInPositive("w_reg_in_positive[0]",true),
		regInNegative("w_reg_in_negative[0]",true),
		regOutPredication("w_reg_predication[0]",false),
		regOutOrPositive("w_reg_or_positive[0]",false),
		regOutOrNegative("w_reg_or_negative[0]",false);
		
		boolean memoryByPass;
		
		BranchSelectionConnection(String declaration, boolean memoryByPass){
			this.memoryByPass = memoryByPass;
			verilogDeclaration = declaration;
		}
		
		String verilogDeclaration;
		
		public String getVerilogDeclaration(){
			return verilogDeclaration;
		}
		
		static BranchSelectionConnection getbranchSelectionConnectionsByName(String name){
			name.trim();
			for(BranchSelectionConnection cons : BranchSelectionConnection.values()){
				if(name.compareToIgnoreCase(cons.toString()) == 0){
					return cons;
				}
			}
			return null;
		}
	}

	public int getCBoxPredicationOutputs() {
		return nrOfEvaluationBlocks*cBoxPredicationOutputsPerBox;
	}

	public HashSet<String> equalsInAttributes(CBoxModel model) {
		HashSet<String> diffs = new HashSet<String>();
		if(memoryslots != model.getMemorySlots()) diffs.add("CB memoryslots");
		if(branchSelectionSources.size() != model.getBranchSelectionSources().size()) diffs.add("CB nr of branchSelectionSources");
		for(BranchSelectionConnection bss : branchSelectionSources){
			if(!model.getBranchSelectionSources().contains(bss)) diffs.add("CB " + bss.toString());
		}
		if(cBoxPredicationOutputsPerBox != model.getCBoxPredicationOutputsPerBox()) diffs.add("CB cBoxPredicationOutputsPerBox");
		if(nrOfEvaluationBlocks != model.getNrOfEvaluationBlocks()) diffs.add("CB nrOfEvaluationBlocks");
		return diffs;
	}
}
