package functionalunit.cgra;

import cgramodel.CBoxModel;
import cgramodel.CgraModel;
import cgramodel.ContextMaskCBoxWrapper;
import cgramodel.CBoxModel.BranchSelectionConnection;

public class CBoxWrapper {
	
	int nrOfBoxes = 1;
	int outputsPerBox = 1;

	Cbox [] cBox;
	
	CBoxModel cBoxModel;
	
	long context;
	
	/**
	 * The contextmask determines how the available context is to be interpreted
	 */
	public ContextMaskCBoxWrapper contextmaskwrapper;
	
	public CBoxWrapper(int nrOfBoxes, int outputsPerBox){
		this.nrOfBoxes = nrOfBoxes;
		this.outputsPerBox = outputsPerBox;
		
		cBox = new Cbox[nrOfBoxes];
		
		for(int i = 0; i< nrOfBoxes; i++){
			cBox[i] = new Cbox();
		}
		
		
		
	}
	
	
	public boolean getPredicationOutput(int port){
		
		
		int box = port/outputsPerBox;
		int boxPort = port%outputsPerBox;
		
//		System.out.println("  getting port " + port  + " box: " + box + " boxport: " +boxPort  );
		
		return cBox[box].getPredicationOutput(boxPort);
	}

	public void setEnable(boolean enable) {
		for(Cbox box: cBox){
			box.setEnable(enable);
		}
	}

	
	public boolean getBranchSelectionSignal() {
		BranchSelectionConnection conny = cBoxModel.getBranchSelectionSources().get(contextmaskwrapper.branchSelectionMux(context));
		switch (conny){
		case regOutOrPositive: return cBox[0].regOrPositive;
		case regOutOrNegative: return cBox[0].regOrNegative;
		case regOutPredication: return cBox[0].regPredication;
		case regInPositive: return cBox[0].regInPositive;
		case regInNegative:
			return cBox[0].regInNegative;
		default: System.err.println("Enterned default statement in get BranchSelectionSignal() in CBox. This should never happen");return cBox[0].regOrNegative;
		}
	}

	public void operateComb(){
		for(Cbox box: cBox){
			box.operateComb();
		}
		
	}

	public void operateClocked(){
		for(Cbox box: cBox){
			box.operateClocked();
		}
	}

	public void fetchContextEvaluationBox(long context, int box) {
		cBox[box].fetchContext(context);
	}
	
	public void fetchContextWrapper(long context){
		this.context = context;		
	}

	public void setInputMapping(int slot, int peId){
		for(Cbox box: cBox){
			box.setInputMapping(slot, peId);
		}
	}
	
	public void configure (CgraModel cgra){
		cBoxModel = cgra.getcBoxModel();
		boolean [] mem = new boolean[cgra.getcBoxModel().getMemorySlots()];
		for(Cbox box: cBox){
			box.configure(cgra,mem);
		}
		contextmaskwrapper = new ContextMaskCBoxWrapper();
	}
	
	public CBoxModel getCBoxModel(){
		return cBoxModel;
	}
	
	public void setInputMapped(int input, boolean data){
		for(Cbox box: cBox){
			box.setInputMapped(input, data);
		}
	}
}
