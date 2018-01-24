package functionalunit.cgra;

import java.util.HashMap;
import java.util.LinkedHashMap;

import cgramodel.CBoxModel;
import cgramodel.CgraModel;
import cgramodel.ContextMaskCBoxEvaluationBlock;
import cgramodel.ContextMaskCBoxWrapper;
import cgramodel.CBoxModel.BranchSelectionConnection;

/**
 * The CBox is a controlbox used to evaluate branch and loop conditions.  
 * @author Dennis Wolf
 *
 */
public class Cbox {

	
	/**
	 * inputs. Corresponds to the number of PEs
	 */
	public boolean[] Input;
	
	
	/**
	 *  The inputmapping determines which PE is connected to which slot 
	 */
	public int[] inputmapping;
	
	
	/**
	 * Input slot for the enable signal
	 */
	public boolean InputEnable;

	
	/**
	 * Memory
	 */
	public boolean[] regfile;

	
	/**
	 * Helper signal
	 */
	boolean regOrPositive,regOrNegative,regPredication,regInPositive,regInNegative, inputReg;
	private boolean [] regAadditional;

	LinkedHashMap <BranchSelectionConnection,Boolean> branchselectionassociation = new LinkedHashMap <BranchSelectionConnection,Boolean>(){/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

	{
	put(BranchSelectionConnection.regOutOrPositive,regOrPositive);
	put(BranchSelectionConnection.regOutOrNegative,regOrNegative);
	put(BranchSelectionConnection.regOutPredication,regPredication);
	put(BranchSelectionConnection.regInPositive,regInPositive);
	put(BranchSelectionConnection.regInNegative,regInNegative);
	}};   
	
	/**
	 * Current Context
	 */
	public long context;

	/**
	 * The contextmask determines how the available context is to be interpreted
	 */
	public ContextMaskCBoxEvaluationBlock contextmaskevaluationblocks;
	


	public CBoxModel model;
	
	public CBoxModel getCBoxModel(){
		return model;
	}
	/**
	 * Constructor
	 */
	public Cbox(){
		
	}
	
	
//	/**
//	 * Configurates the CBox
//	 */
//	public void configure(int nrOfPEs, int numberOfPEscontrolflow, int max_branches, ContextMaskCBox mask){
//		Input = new boolean[numberOfPEscontrolflow];
//		inputmapping = new int[nrOfPEs];
//		regfile = new boolean[max_branches];
//		contextmask = mask;
//	}
	
	protected void configure (CgraModel cgra){
		Input = new boolean[cgra.getNrOfControlFlowPEs()];
		inputmapping = new int[cgra.getNrOfPEs()];
		model = cgra.getcBoxModel();
		regfile = new boolean[model.getMemorySlots()];
		contextmaskevaluationblocks = model.getContextmaskEvaLuationBlocks();
		regAadditional = new boolean[cgra.getcBoxModel().getCBoxPredicationOutputsPerBox()];
	}
	
	
	protected void configure(CgraModel cgra, boolean[] regfile){
		Input = new boolean[cgra.getNrOfControlFlowPEs()];
		inputmapping = new int[cgra.getNrOfPEs()];
		model = cgra.getcBoxModel();
		this.regfile =regfile;
		contextmaskevaluationblocks = model.getContextmaskEvaLuationBlocks();
		regAadditional = new boolean[cgra.getcBoxModel().getCBoxPredicationOutputsPerBox()];
	}
	
	/**
	 * Setter method for the inputmapping of the status signals
	 */
	protected void setInputMapping(int slot, int peId){
		inputmapping[peId] = slot;
	}

	/**
	 * Setter method for an status input
	 */
	protected void setInput(int pe, boolean value){
		Input[pe] = value;
	}

	
	/**
	 * Emulates the multiplexor for the inputs
	 */
	protected void muxInputs(){
		if(Input.length >1){
			inputReg = Input[contextmaskevaluationblocks.inputMux(context)];
		}
		else{
			inputReg = Input[0];
		}
	}

	
	/**
	 * Method to load a new context 
	 */
	protected void fetchContext(long context) {
		this.context = context;		
	}

	
	/**
	 * operates memory writes since they are clocked 
	 */
	protected void operateClocked(){
		if(contextmaskevaluationblocks.writeEnable(context)){
//			if(1 == contextmask.writeAddressA(context)){
//				System.out.println("WWWWRITING " + regInPositive + " " + regInNegative + " to " + contextmaskevaluationblocks.writeAddressPositive(context) + " " + contextmaskevaluationblocks.writeAddressNegative(context));
//			}
//			if(contextmaskevaluationblocks.writeAddressPositive(context)== 2){
//				System.out.println("WRITE111: " + regInPositive);
//			}
//			if(contextmaskevaluationblocks.writeAddressNegative(context)== 2){
//				System.out.println("WRITE222: " + regInNegative);
//			}
			regfile[contextmaskevaluationblocks.writeAddressPositive(context)] = regInPositive;
			regfile[contextmaskevaluationblocks.writeAddressNegative(context)] = regInNegative;
		}
	}

	
	/**
	 * Triggers the emulation of the combinatorial circuit
	 */
	protected void operateComb(){
		regOrPositive = regfile[contextmaskevaluationblocks.readAddressOrPositive(context)];
		regOrNegative = regfile[contextmaskevaluationblocks.readAddressOrNegative(context)];
		regPredication = regfile[contextmaskevaluationblocks.readAddressPredication(context,0)];
		for(int i = 0; i < regAadditional.length; i++){
			int addr = contextmaskevaluationblocks.readAddressPredication(context,i);
			regAadditional[i] = regfile[addr];
//			System.out.println("READA A " + contextmask.readAddressA(context,i) + " " + regAadditional[i]);
		}
		muxInputs();
		regInPositive = inputReg;		// tracks the branch as it is
		regInNegative = !inputReg;		// takes the second branch, which is always the negation
		if(!contextmaskevaluationblocks.bypassAndPositive(context)){
			regInPositive = regInPositive && regPredication;
		}
		if(!contextmaskevaluationblocks.bypassOrPositive(context)){
			regInPositive = regInPositive || regOrPositive;	
		}

		if(!contextmaskevaluationblocks.bypassAndNegative(context)){
			regInNegative = regInNegative && regPredication;
		} 
		
		if(!contextmaskevaluationblocks.bypassOrNegative(context)){
			regInNegative = regInNegative || regOrNegative;
		}
	}

	/**
	 * Returns the Predication output
	 * @return
	 */
	protected boolean getPredicationOutput(int port){
//		System.out.println("getting predication port " + port+ ": " + regAadditional[port]);
		return regAadditional[port];
	}

	/**
	 * Setter method for the enable input
	 */
	protected void setEnable(boolean enable) {
		InputEnable = enable;
	}	

	/**
	 * Getter method for the enable input
	 */
	protected boolean getInputEnable() {
		return InputEnable;
	}
	
	protected void setInputMapped(int input, boolean data){
		Input[inputmapping[input]] = data;
	}

}
