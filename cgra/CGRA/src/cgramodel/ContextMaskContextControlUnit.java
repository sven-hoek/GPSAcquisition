package cgramodel;

/**
 * Contrext mask for the Context Control Unit.
 * 
 * @author Wolf
 *
 */
public class ContextMaskContextControlUnit extends ContextMask {

	/**
	 *
	 */
	private static final long serialVersionUID = -4553802048964948373L;
	int ccntwidth;
	int contextL;
	int contextH;
	int jump;
	int conditional;
	int relative;

	public ContextMaskContextControlUnit() {
		name = "ContextMaskControlUnit";
	}

	// public ContextMaskControlUnit(Number n) {
	// context = n.longValue();
	// }
	//
	// public ContextMaskControlUnit(String bits){
	// super(bits);
	// }

	/**
	 * Initial method to create the mask for the memory of the Control Unit
	 */
	public void createMask(int contextsize,boolean branchselection) {

		ccntwidth = (int) Math.ceil((Math.log(contextsize) / Math.log(2)));
		contextL = 0;
		contextH = ccntwidth-1;
		jump = contextH +1;
		relative = jump+1;
		contextwidth = relative +1;
		if(branchselection){
			conditional = relative +1;
			contextwidth = conditional +1;
		}
	}

	public int getCCNTWidth(){
		return ccntwidth;
	}

	
	public int getCounter(long context) {
		return read(context,contextL,ccntwidth);
	}


	public long setCounter(long context, int value) {
		long tmp = writeBitSet(context, value, contextL, ccntwidth);
		return tmp;
	}

	public boolean getConditional(long context) {
		return read(context, conditional, 1) == 1 ? true : false;
	}

	public long setConditional(long context, boolean cond) {
		if (cond) {
			return writeBitSet(context, 1, conditional, 1);
		} else {
			return writeBitSet(context, 0, conditional, 1);
		}
	}

	public boolean getJump(long context){
		return read(context,jump,1)==1?true:false;
	}

	public long setJump(long context, boolean cond){
		if(cond)
			return writeBitSet(context,1,jump,1);
		else
			return writeBitSet(context,0,jump,1);
	}

	public boolean getRelative(long context){
		return read(context,relative,1)==1?true:false;
	}
	
	
	public long setRelative(long context, boolean cond){
		if(cond)
			return writeBitSet(context,1,relative,1);
		else
			return writeBitSet(context,0,relative,1);
	}
	
}
