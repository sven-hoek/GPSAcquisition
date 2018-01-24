package javasim.synth;

import dataContainer.ByteCode;


/**
 * @author Stefan D&ouml;brich
 * @date 30.11.2006
 */
public class SequenceNotSynthesizeableException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3544836884579921401L;
	int byteCode;
	int pos;
	int param1, param2, param3;
	
	public SequenceNotSynthesizeableException() {
	}
	
	public SequenceNotSynthesizeableException(int bc) {
		this(bc, Integer.MIN_VALUE);
	}
	public SequenceNotSynthesizeableException(int bc,int pos,int param1,int param2,int param3) {
		this(bc, pos);
		this.param1 = param1;
		this.param2 = param2;
		this.param3 = param3;
	}
	
	public SequenceNotSynthesizeableException(int bc, int pos) {
		byteCode = bc;
		this.pos = pos;
	}

	public SequenceNotSynthesizeableException(String message) {
		super(message);
	}
	
	public int getUnknownByteCode() {
		return byteCode;
	}
	
	public String getUnknownByteCodeName(){
		byte[] bc = new byte[4];
		bc[0] = (byte)byteCode;
		bc[1] = (byte)param1;
		bc[2] = (byte)param2;
		
		return ByteCode.mnemonic(bc);
	}
	
	public int getPosOfUnknownByteCode() {
		return pos;
	}
	public int getParam1() {
		return param1;
	}
	public int getParam2() {
		return param2;
	}
	
	public String getMessage(){
		String ret = super.getMessage();
		if(ret == null){
			ret = "Unknown Bytecode: " + getUnknownByteCodeName() + " at " + getPosOfUnknownByteCode();
		}
		return ret;
	}
}
