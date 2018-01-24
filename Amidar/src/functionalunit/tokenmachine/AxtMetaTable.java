package functionalunit.tokenmachine;

public abstract class AxtMetaTable {
	
	protected int constants, bytecodeOffset;
	protected boolean jump, branch;
	
	public abstract void requestData(short bytecode);


	public int getConstants() {
		return constants;
	}

	public int getBytecodeOffset() {
		return bytecodeOffset;
	}

	public boolean isJump() {
		return jump;
	}

	public boolean isBranch() {
		return branch;
	}
	
	

}
