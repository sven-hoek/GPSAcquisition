package cgramodel;

import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;

import operator.Operator;

/**
 *
 * This class represents an instance of a CGRA. Core information of composition
 * are stored inside. The data path is represented by a Set of PEModel. Those models represent
 * a PE as this class represents a CGRA. All missing information to a complete description are
 * stored in variables, that get be accessed with getter-methods.
 *
 * @author Wolf
 *
 */
public abstract class CgraModel implements util.Version, Serializable  {

	//	private int bitsPerContext = 0;

	public final static int majorVersion = 3;
	public final static int minorVersion = 0;
	public final static int revisionVersion = 0;

	public int getMajorVersion(){
		return majorVersion;
	}

	public int getMinorVersion(){
		return minorVersion;
	}

	public int getRevisionVersion(){
		return revisionVersion;
	}


	/**
	 *
	 */
	private static final long serialVersionUID = 1005458557540194341L;
	//	public static final int MAX_PES = 20;
	//	public static final int MAX_REGFILE_ADDR_WIDTH = 32 - MAX_PES;
	//	public static final int MUX_WIDTH = 5;
	//	public static final int VIA_WIDTH = 4;

	/**
	 * Constructor
	 */
	public CgraModel() {
		cBoxModel = new CBoxModel();
	}	

	/**
	 * The CGRA can be pipelined with register in front of the operand_A and operand_B of the ALU in each PE.
	 */
	private boolean pipelined = false;

	private boolean RFBypass = true;

	private boolean conditionalJumps;

	public boolean isConditionalJumps(){
		return conditionalJumps;
	}
	/**
	 * Depending on the amount of accessible caches the system needs to be stalled. 
	 * Should no caches be used, the system does not need to hold a mechanism to be stalled.
	 * 
	 * The stallablility must be known to the {@link Operator} {@link Implementation}s, 
	 * which do not actually know the {@link CgraModel} they live in. Therefore, the stallability information is
	 * managed by the globally accessible {@link target.Processor#Instance}.
	 */
	public boolean isStallable(){
		return (getNrOfCacheAccessPEs() > 0) && target.Processor.Instance.isStallable();
	}

	/**
	 * Getter method for the field pipelined.
	 * @return true if pipelined, false if not {@link #pipelined}
	 */
	public boolean isPipelined() {
		return pipelined;
	}

	// TODO Dennis - remove this and switch dependencies to pipelined  
	public boolean isRFBypass(){
		return RFBypass;
	}

	private boolean secondRFOutput2ALU = false;

	public boolean isSecondRFOutput2ALU(){
		return secondRFOutput2ALU;
	}

	public void setSecondRFOutput2ALU(boolean arg){
		secondRFOutput2ALU = arg;
	}

	/**
	 * Setter method for the field pipelined.
	 */
	public void setPipelined(boolean pipelined) {
		this.pipelined = pipelined;
	}
	CBoxModel cBoxModel;

	public CBoxModel getcBoxModel() {
		return cBoxModel;
	}

	public void setcBoxModel(CBoxModel cbox) {
		cBoxModel = cbox;
	}

	/**
	 * width of the via network. This parameter is set automatically if the model gets finalized.
	 */
	int viaWidth = -1;

	/**
	 * Getter method for {@link #viaWidth}
	 * @return viaWidth
	 */
	public int getViaWidth(){
		if(viaWidth == -1){
			viaWidth = 0;
			for(PEModel pe : PEs){
				if(pe.getLiveout()){
					viaWidth++;
				}
			}
			if(viaWidth != 0){
				viaWidth = (int)Math.floor(Math.log(viaWidth)/Math.log(2)+1);
			}
		}
		return viaWidth;
	}

	/**
	 * Holds the maximum Regfile address width. This is set automatically set by its getter method, which is 
	 * currently the only method accessing this field. 
	 */
	int maxRegfileAddrWidth = -1;

	/**
	 * getther method for {@link #getMaxRegfileAddrWidth}
	 * @return maximum RF addressing width
	 */
	public int getMaxRegfileAddrWidth(){
		if(maxRegfileAddrWidth == -1){
			//int regfilesize = 0;
			for(PEModel pe : PEs){
				int currentWidth = pe.getContext().getRegAddrWidthRead();
				if(currentWidth > maxRegfileAddrWidth){
					maxRegfileAddrWidth = currentWidth;
				}


			}

			//			maxRegfileAddrWidth = (int)Math.ceil(Math.log(regfilesize)/Math.log(2));
		}
		return maxRegfileAddrWidth;
	}


	/**
	 * getther method for max. operand mux width
	 * @return maximum Mux addressing width
	 */
	public int getMaxMuxAddrWidth(){
		int muxsize = 0;
		for(PEModel pe : PEs){
			if(pe.getInputs().size() > muxsize){
				muxsize = pe.getInputs().size(); 
			}
		}
		int offset = (secondRFOutput2ALU) ? 2:1;
		offset += (pipelined) ? 1: 0;
		return (int)Math.ceil(Math.log(muxsize+ offset)/Math.log(2));
	}

	//	/**
	//	 * The maximum context width of the model.
	//	 */
	//	int maxContextWidth = -1;
	//
	//	/**
	//	 * Get and calculate {@link #maxContextWidth}.
	//	 * @return maximum context width
	//	 */
	//	public int getMaxContextWidth() {
	//		if(maxContextWidth == -1){
	//			maxContextWidth = cBoxModel.getContextmaskcbox().getContextWidth();
	//			for (PEModel pe : getPEs()) {
	//				if(pe.getContextWidth() > maxContextWidth){
	//					maxContextWidth = pe.getContextWidth();
	//				}
	//			}
	//		}
	//		return maxContextWidth;
	//	}

	/**
	 * Mask for context entries of the Controlunit
	 */
	public ContextMaskContextControlUnit contextmaskccu;

	public ContextMaskContextControlUnit getContextmaskccu() {
		return contextmaskccu;
	}

	public void setContextmaskccu(ContextMaskContextControlUnit contextmaskccu) {
		this.contextmaskccu = contextmaskccu;
	}

	/**
	 * Available PEs in this instance
	 */
	ArrayList<PEModel> PEs = new ArrayList<PEModel>();


	/**
	 * Name of the instance.
	 */
	String name;

	/**
	 * Getter method for the name of this instance.
	 *
	 * @return name of the instance
	 */
	public String getName() {
		return name;
	}

	/**
	 * Setter method
	 *
	 * @param name
	 *            of this instance
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Getter method
	 *
	 * @return Number of available PEs
	 */
	public int getNrOfPEs() {
		return PEs.size(); 
	}

	/**
	 * Getter method
	 *
	 * @return all available PEs as an ArrayList
	 */
	public ArrayList<PEModel> getPEs() {
		return PEs;
	}

	/**
	 * Contextsize of this instance.
	 */
	int contextMemorySize;

	/**
	 * Getter method
	 *
	 * @return Memory size of the related context memory
	 */
	public int getContextMemorySize() {
		return contextMemorySize;
	}

	/**
	 * Setter method for the size of the related context memory
	 */
	public void setContextMemorySize(int contextMemorySize) {
		this.contextMemorySize = contextMemorySize;
		finalized = false;
	}

	/**
	 * Adds another PE to the instance
	 *
	 * @param pe
	 *            new PE
	 */
	public void addPE(PEModel pe) {
		PEs.add(pe);
		finalized = false;
		if(cBoxModel != null)
			cBoxModel.resetFinalized();
	}

	//	/**
	//	 * Setter method for the slots in the memory of the CBox
	//	 *
	//	 * @return
	//	 */
	//	public void setcBoxSlots(int cBoxSlots) {
	//		this.cBoxModel.setMemorySlots(cBoxSlots);
	//	}

	/**
	 * removes the given PE from the instance
	 *
	 * @param pe
	 *            new PE
	 */
	public void removePE(PEModel pe) {
		PEs.remove(pe);
		finalized = false;
		if(cBoxModel != null){
			cBoxModel.resetFinalized();
		}
		for(PEModel pedest : PEs){
			pedest.removePEFromInputs(pe);
		}
	}


	/**
	 * Getter method
	 *
	 * @return width of CCNT
	 */
	public int getCCNTWidth() {
		return (int) Math.ceil(Math.log(getContextMemorySize()) / Math.log(2));
	}

	/**
	 * Returns the number of PEs that are capable of processing control flow
	 * instructions (e.g. IFGE or IFLT).
	 *
	 * @return
	 */
	public int getNrOfControlFlowPEs() {
		int counter = 0;
		for (PEModel pe : PEs) {
			if (pe.getControlFlow()) {
				counter++;
			}
		}
		return counter;
	}

	/**
	 * Returns the number of PEs that are capable of memory (cache or ROM) access operations.
	 *
	 * @return number of PEs with memory access operations
	 */
	public int getNrOfMemoryAccessPEs() {
		int counter = 0;
		for (PEModel pe : PEs) if (pe.getMemAccess()) counter++;
		return counter;
	}

	/**
	 * Returns the number of PEs that are capable of cache access operations.
	 *
	 * @return number of PEs with memory access operations
	 */
	public int getNrOfCacheAccessPEs() {
		int counter = 0;
		for (PEModel pe : PEs) if (pe.getCacheAccess()) counter++;
		return counter;
	}

	/**
	 * Status of the model whether it is finalized. Before an instance can be generated the underlying model
	 * <b>must</b> be finalized.
	 */
	private boolean finalized = false;

	/**
	 * Method to check whether all hardware relevant information have been
	 * processed. This method should be used to whether the instance is ready to
	 * be generated in hardware.
	 */
	public boolean isFinalized() {
		for(PEModel pe : PEs){
			if(!pe.isFinalized()){
				return false;
			}
		}
		return finalized && cBoxModel.finalized && hostSpecificFinalized();
	}

	protected abstract boolean hostSpecificFinalized();

	/**
	 * Setter method for the finalize attribute. This method forces the instance
	 * into a state of being finalized. This method should never be invoked in
	 * automated systems and have been used for debugging purposes until today.
	 */
	public void setFinzalized(boolean readyforgeneration) {
		System.err.println("Warning: The Cgra is set to finalized without using the internal method to finalize");
		if(!cBoxModel.finalized){
			System.err.println("Warning: CBoxModel is not finalized as well. This will probably lead to problems");
		}
		finalized = readyforgeneration;
	}

	/**
	 * This message encodes a given cgra model as well as creates contexts mask
	 * to achieve an feasible model for generation. <b>It is highly recommended
	 * to call this method before Verilog code is generated</b>
	 */
	public void finalizeCgra(){

		sanityCheck();

		int counter = 0;
		for(int pe = 0; pe < PEs.size() ; pe++) {
			PEs.get(pe).setID(counter);
			PEs.get(pe).finalizePE(this);
			counter++;
			//				bitsPerContext += PEs.get(pe).getContextMaskPE().getContextWidth();
		}
		if(cBoxModel.getBranchSelectionSources().isEmpty()){
			conditionalJumps = false;
		}
		else{
			conditionalJumps = true;
		}
		cBoxModel.finalizeCBox(this);
		contextmaskccu = new ContextMaskContextControlUnit();
		contextmaskccu.createMask(contextMemorySize, conditionalJumps);

		//			bitsPerContext += cBoxModel.getContextmaskcbox().getContextWidth()*nrOfCboxes;
		//			bitsPerContext += contextmaskccu.getContextWidth();
		//			System.err.println("BITS PER CONTEXT: " + bitsPerContext + " ( = " + (int)Math.ceil(bitsPerContext/8.0) + " Bytes = "+ (int)Math.ceil(bitsPerContext/32.0)+" Worte)");

		//		/*
		//		 * Global Encoding - redundant in every PE
		//		 */
		//		int opcode = 0;
		//		for(Operator op : Processor.Instance.getOperators()){
		//			boolean opexists = false;
		//			for(PEModel pe: PEs){
		//				if(pe.getAvailableNonNativeOperators().containsKey(op)){
		//					pe.getAvailableNonNativeOperators().get(op).setOpcode(opcode);
		//					opexists = true;
		//				}
		//			}
		//			if(opexists){
		//				opcode++;
		//			}
		//		}
		finalized = true;
		//		}
	}

	/**
	 * This methods checks whether a given operation is available anywhere in the CGRA
	 * @param op
	 * @return
	 */
	public boolean supportsOperation(Operator op){

		for(PEModel peModel: PEs){
			for(Operator peOp: peModel.getAvailableOperators().keySet()){
				if(op.equals(peOp)){
					return true;
				}
			}
		}
		return false;
	}

	private void sanityCheck() {

		// checks if conditional jumps should be supported but there no branch selection signal given 
		if(isConditionalJumps() && getcBoxModel().getBranchSelectionSources().size() == 0){
			throw new IllegalArgumentException("Conditional Jumps should be supported but there is no branch selection source given");
		}

		// checks for control flow operations - there must be at least one operations somewhere
		boolean controlflow = false;
		for(PEModel pe: getPEs()){
			assertTrue("no operators in PE ", pe.getAvailableNonNativeOperators().size() > 0);
			if(pe.getControlFlow()){
				controlflow = true;
			}
		}
		if(!controlflow){
			throw new IllegalArgumentException("No control flow operations available");
		}

	}


	/**
	 * This method checks the equality of two model. Note that the interconnect is only checked in the equality check of
	 * PEs. Those are only compared id wise, meaning if the composition would be equal, but listing of PEs is 1-2-3-4 
	 * but the other one is 3-4-1-2 the check would fail. One should adjust this 
	 * @param model
	 * @return True if equal False otherwise
	 */
	public HashSet<String> equalsInAttributes(CgraModel model){
		HashSet<String> diff = new HashSet<String>();
		if(contextMemorySize != model.getContextMemorySize()) diff.add("contextMemorySize");
		diff.addAll(cBoxModel.equalsInAttributes(model.getcBoxModel()));
		
		if(PEs.size() != model.getPEs().size())diff.add("PE amount");

		// We do not want to cause out of bounds exceptions during the next loop
		// if the pe list sizes are unequal.
		if (!diff.isEmpty()){
			diff.add("cancelled early after cbox and pe size check");
			return diff;
		}
		for(int peindex = 0 ; peindex < PEs.size();peindex++){
			diff.addAll(PEs.get(peindex).equalsInAttributes(model.getPEs().get(peindex)));
		}
		if(secondRFOutput2ALU != model.isSecondRFOutput2ALU())diff.add("secondRFOutput2ALU");
		if(pipelined != model.isPipelined())diff.add("pipelined");
		diff.addAll(equalsInHostRelatedAttributes(model));
		return diff;
	}
	
	protected abstract HashSet<String> equalsInHostRelatedAttributes(CgraModel model);

	//	public boolean longLocationInformation(){
	//		return getMaxRegfileAddrWidth() + 2 + getNrOfPEs() >32;
	//	}

}
