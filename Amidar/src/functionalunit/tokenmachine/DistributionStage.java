package functionalunit.tokenmachine;

import functionalunit.CGRA;
import functionalunit.FDIV;
import functionalunit.FPU;
import functionalunit.FrameStack;
import functionalunit.IALU;
import functionalunit.IDIV;
import functionalunit.IMUL;
import functionalunit.LALU;
import functionalunit.ObjectHeap;
import functionalunit.Scheduler;
import functionalunit.TokenMachine;

public class DistributionStage {
	
	FIFO decodeFifoByteCode;
	FIFO decodeFifoIsJump;
	FIFO decodeFifoAddress;
	
	int addressOfJump = 0;
	
	TokenMatrix tokenMatrix;
	
	Profiler profiler;
	
	short currentBytecode = 0;
	int currentAddress = 0;
	boolean isJump = false;
	
	public DistributionStage(FIFO decodeFifoByteCode, FIFO decodeFifoIsJump, FIFO decodeFifoAddress, Profiler profiler) {
		this.decodeFifoByteCode = decodeFifoByteCode;
		this.decodeFifoIsJump = decodeFifoIsJump;
		this.decodeFifoAddress = decodeFifoAddress;
		this.profiler = profiler;
		
		tokenMatrix = new TokenMatrixGeneratedByAdla();
	}
	
	public DistributionStage(DecodeStage ds, Profiler profiler){
		this(ds.getDecodeFifoBytecode(), ds.getDecodeFifoIsJump(), ds.getDecodeFifoAddress(), profiler);
	}
	
	public void tick(){
		if(tokenMatrix.tokenDecodingDone()){
			if(!decodeFifoByteCode.isEmpty() && !isJump){
				currentBytecode = (short)decodeFifoByteCode.pop();
				addressOfJump = decodeFifoIsJump.pop();
				currentAddress = decodeFifoAddress.pop();
				isJump = addressOfJump != 0;
				tokenMatrix.startNewBytecode();
				profiler.newByteCode(currentAddress);
//				if(currentAddress >= 54399 && currentAddress <=54463){
//					System.out.println("DISTRIB " +currentAddress + " " + Integer.toHexString(currentBytecode));
//				}
			} else {
				return;
			}
		}
		if(!tokenMatrix.tokenDecodingDone()){
			tokenMatrix.decodeByteCode(currentBytecode);
		}
	}
	
	public void clearJumpTrap(){
		isJump = false;
	}
	
	public int getAddressOfJump(){
		return addressOfJump;
	}
	
	public int getCurrentBytecode(){
		return currentBytecode;
	}
	
	public int getCurrentAddress(){
		return currentAddress;
	}
	
	public void setFUs( IALU ialu,
			 IMUL imul,
			 IDIV idiv,
			 LALU lalu,
			 FPU fpu,
			 FDIV fdiv,
			 ObjectHeap heap,
			 FrameStack frameStack,
			 TokenMachine tokenMachine,
			 CGRA cgra,
			 Scheduler scheduler){
		tokenMatrix.setFUs(ialu, imul, idiv, lalu, fpu, fdiv, heap, frameStack, tokenMachine, cgra, scheduler);
		
	}

	public void setProfiler(Profiler profiler) {
		this.profiler = profiler;
	}

	public TokenMatrix getTokenMatrix(){
		return tokenMatrix;
	}
}
