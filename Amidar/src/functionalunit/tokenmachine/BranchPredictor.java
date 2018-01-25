package functionalunit.tokenmachine;

public class BranchPredictor {
	
	
	
	boolean predictedBranch = false;
	boolean executeFlush = false;
	boolean predicting = false;
	int predictedAddr, otherAddr;
	
	boolean flushedBecauseOfPredictedBranch = true;
	
	public void predictBranch(int addr, int offset){
		if(predicting){
			throw new RuntimeException("Already pPredicting");
		}
		predicting = true;
		
		//DECISION
		if(branch(addr,offset)){
			
			predictedBranch = true;
			flushedBecauseOfPredictedBranch = false;
			executeFlush = true;
			predictedAddr = addr + offset;
			otherAddr = addr + 3;
//			System.out.println("***predicting branch to " + predictedAddr);
		} else {
			predictedBranch = false;
			executeFlush = false;
			predictedAddr = addr + 3;
			otherAddr = addr + offset;
//			System.out.println("***predicting NO branch proceeding with " + predictedAddr);
		}
		
	}
	
	public boolean correctPrediction(boolean actuallyTookJump){
		if(!predicting){
			throw new RuntimeException("Not predicting anything");
		}
		predicting = false;
		if(actuallyTookJump == predictedBranch){
//			System.out.println("***Prediction was correct (" +predictedAddr+")");
			return true;
		} else {
//			System.out.println("***Prediction was wroong. going to "+ otherAddr);
			return false;
		}
		
	}
	
	public boolean executeFlush(){
		return executeFlush;
	}
	
	public int getPredictedAddress(){
		return predictedAddr;
	}
	
	public int getOtherAddress(){
		return otherAddr;
	}
	
	public boolean predicting(){
		return predicting;
	}
	
	public boolean flushNeeded(){
		if(predictedBranch && !flushedBecauseOfPredictedBranch){
			flushedBecauseOfPredictedBranch = true;
			return true;
		}
		return false;
	}
	
	/**
	 * Overwrite this method if you want to create another Predictor
	 * @param addr
	 * @param offset
	 * @return
	 */
	protected boolean branch(int addr, int offset){
		if(offset < 0){
			return true;
		} else {
			return false;
		}
	}

}
