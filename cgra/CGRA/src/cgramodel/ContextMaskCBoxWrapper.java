package cgramodel;

public class ContextMaskCBoxWrapper extends ContextMask{


	int branchselectionsourcewidth = 0;
	int outCCUL = -1;
	int outCCUH = -1;
	
	public int getBranchSelectionMuxWidth(){
		return branchselectionsourcewidth;
	}

	public int createMask(CgraModel model){
		
		contextwidth = 0;
		int sources = model.getcBoxModel().getBranchSelectionSources().size();
		branchselectionsourcewidth = 0;
		if(sources > 1){
			branchselectionsourcewidth = (int) Math.ceil(Math.log(sources)/ Math.log(2));
			outCCUL = 0;
			outCCUH = outCCUL + branchselectionsourcewidth;
			contextwidth = outCCUH +1;
		}
		return contextwidth;
	}
	
	
	public int branchSelectionMux(long context) {
		return read(context, outCCUL, outCCUH - outCCUL);
	}

	public long setBranchSelectionMux(long context, int value) {
		return writeBitSet(context, value, outCCUL, branchselectionsourcewidth);
	}
}
