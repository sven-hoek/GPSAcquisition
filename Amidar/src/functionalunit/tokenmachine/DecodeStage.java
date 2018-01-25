package functionalunit.tokenmachine;

import java.nio.channels.ByteChannel;

import dataContainer.ByteCode;


public class DecodeStage {
	
	FIFO fifoFetch;
	
	FIFO parameterFifoParameter;
	FIFO parameterFifoNrConstants;
	
	FIFO decodeFifoBytecode; 
	FIFO decodeFifoIsJump;
	FIFO decodeFifoAddress;
	
	BranchPredictor branchPredictor;
	
	byte[] memory;
	
	
	AxtMetaTable axtMetaTable;
	
	boolean flush = false;
	boolean stall = false;
	
	boolean clearPrefetchTrap = false;
	boolean clearJumpTrap = false;
	boolean alreadyPredictingInDecodeStage = false;
	
	int decodeWait = 0;
	byte currentByteCode = 0;
	int currentAddress = 0;
	boolean currentBytecodeWide = false;
	int parameter = 0;
	
	int sendConstantCount = 0;
	
	private enum State{
		IDLE,
		DECODE,
		JUMP_TRAP,
		PREFETCH_TRAP
	}
	
	State currentState; 
	State nextState;
	
	public DecodeStage(FetchStage fs, int fifoDepth, BranchPredictor branchPredictor){
		fifoFetch = fs.getFifoByte();
		axtMetaTable = new AxtMetaTableGeneratedByAdla();
		
		decodeFifoBytecode = new FIFO(fifoDepth);
		decodeFifoIsJump = new FIFO(fifoDepth);
		decodeFifoAddress = new FIFO(fifoDepth);
		
		parameterFifoParameter = new FIFO(fifoDepth);
		parameterFifoNrConstants = new FIFO(fifoDepth);
		
		this.branchPredictor = branchPredictor;
		
		currentState = State.IDLE;
		
	}
	
	
	public void setFlush(int newAddress){
//		System.out.println("FFFFFLUSH: " + newAddress);
		parameterFifoNrConstants.flush();
		parameterFifoParameter.flush();
		decodeFifoBytecode.flush();
		decodeFifoIsJump.flush();
		decodeFifoAddress.flush();
		this.flush = true;
	}
	
	public void setStall(boolean stall){
		this.stall = stall;
	}
	
	public void tick(){
		nextState = currentState;
		if(flush){
			parameterFifoNrConstants.flush();
			parameterFifoParameter.flush();
			decodeFifoBytecode.flush();
			decodeFifoIsJump.flush();
			decodeFifoAddress.flush();
			currentState = State.IDLE;
			flush = false;
		} else {
			switch(currentState){
			case IDLE:
				if(!fifoFetch.isEmpty()){
					nextState = State.DECODE;
					decodeWait = -1;
				}
				break;
			case DECODE:
				if(decodeWait == -1 && !fifoFetch.isEmpty()){
					currentAddress = fifoFetch.pop();
					currentByteCode = memory[currentAddress];
					if(currentByteCode == ByteCode.WIDE){
						currentBytecodeWide = true;
					} else {
						if(currentBytecodeWide){
							axtMetaTable.requestData((short)((0xFF&currentByteCode)+0x100));
						} else {
							axtMetaTable.requestData(currentByteCode);
						}
						decodeWait = axtMetaTable.getBytecodeOffset();
						//if(currentBytecodeWide){
						//	decodeWait = decodeWait*2;
						//}
						parameter = 0;
					}
				} else{
					if(decodeWait > 0 && !fifoFetch.isEmpty()){
						parameter = (parameter << 8) + (0xFF&memory[fifoFetch.pop()]);
						decodeWait--;
					} 
					if(decodeWait == 0){
						if(!decodeFifoBytecode.isFull() && !parameterFifoNrConstants.isFull()){
							nextState = State.DECODE;
							decodeWait = -1;
							if(currentBytecodeWide){
								decodeFifoBytecode.push((0xFF&currentByteCode)+0x100);
								currentBytecodeWide = false;
							}else{
								decodeFifoBytecode.push(currentByteCode);
							}
//							System.out.println("DEC "  + currentAddress + " " + Integer.toHexString(currentByteCode)+ " " + Integer.toHexString(parameter));
							decodeFifoIsJump.push(axtMetaTable.isJump()?currentAddress:0);
							decodeFifoAddress.push(currentAddress);
							if(axtMetaTable.getBytecodeOffset()!= 0){
//								System.out.println("~~~~~~~~~~~~~~~~~~~~PUtschin parm of addr " + currentAddress +" conts: " + axtMetaTable.getConstants());
								
								String parmm = Integer.toHexString(parameter);
								int shift = 8*(4-axtMetaTable.getBytecodeOffset());
								int bco = axtMetaTable.getBytecodeOffset();
								parameter = (parameter<<(8*(4-axtMetaTable.getBytecodeOffset())));
								String newparrmm = Integer.toHexString(parameter);
								
								
								parameterFifoParameter.push(parameter);
								parameterFifoNrConstants.push(axtMetaTable.getConstants());
							}
							
							if(axtMetaTable.isJump() && !axtMetaTable.isBranch()){
								nextState = State.JUMP_TRAP;
							} else if(axtMetaTable.isJump() && axtMetaTable.isBranch()){
								if(branchPredictor.predicting()){
									nextState = State.PREFETCH_TRAP;
								} else {
									nextState = State.JUMP_TRAP;
//									nextState = State.DECODE;
									decodeWait = -1;
									alreadyPredictingInDecodeStage = true;
									branchPredictor.predictBranch(currentAddress, parameter>>16);
								}
							}
						}
					}
				}
				break;
			case JUMP_TRAP:
				if(clearJumpTrap){
					clearJumpTrap = false;
					nextState = State.IDLE;
				}
				break;
			case PREFETCH_TRAP:
				if(clearPrefetchTrap){
					clearPrefetchTrap = false;
					nextState = State.IDLE;
					branchPredictor.predictBranch(currentAddress, parameter>>16);
				}
			}
			currentState = nextState;
		}
//		System.out.println("\tdec"+ decodeFifoBytecode.nrOfEntries());
//		System.out.println("\tdec"+ decodeFifoIsJump.nrOfEntries());
//		System.out.println("\tpar"+ parameterFifoNrConstants.nrOfEntries());
//		System.out.println("\tpar"+ parameterFifoParameter.nrOfEntries());
	
	}
	
	public void clearJumpTrap(){
		clearJumpTrap = true;
	}
	
	public void clearPrefetchTrap(){
		if(currentState == State.PREFETCH_TRAP){
			clearPrefetchTrap = true;
		}
	}
	
	public FIFO getParameterFifoParameter(){
		return parameterFifoParameter;
	}

	public FIFO getParameterFifoNrConstants(){
		return parameterFifoNrConstants;
	}
	
	public FIFO getDecodeFifoBytecode(){
		return decodeFifoBytecode;
	}
	
	public FIFO getDecodeFifoIsJump(){
		return decodeFifoIsJump;
	}
	
	public FIFO getDecodeFifoAddress() {
		return decodeFifoAddress;
	}


	public void setMemory(byte[] codeMemory) {
		this.memory = codeMemory;
		
	}



}
