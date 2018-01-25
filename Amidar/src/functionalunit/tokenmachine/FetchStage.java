package functionalunit.tokenmachine;

public class FetchStage {
	
	static final int FLUSH_WAIT = 7;
	
	FIFO fifoWord;
	FIFO fifoByte;
	InstructionCache instructionCache;
	
	byte[] memory;
	
	boolean flush, stall;
	
	int startAddress, currentPhysicalAddress;
	int currentAddr = 0;
	
	int byteCounter = 4;
	
	int waitTime = 0;
	int waitTimeCache = 0;
	
	boolean waitOnCache = false;

	public FetchStage(InstructionCache instructionCache, int fifoDepth){
		this.instructionCache = instructionCache; // hint memory is not yet initiaized
		fifoWord = new FIFO(fifoDepth); // We actually don't store words - bc input is byte and we need byte afterwards - so we save conversions
		fifoByte = new FIFO(fifoDepth);
		memory = instructionCache.getMemory();
	}
	
	
	public FIFO getFifoByte(){
		return fifoByte;
	}
	
	public void setFlush(int newAddress){
//		System.out.println("FLUUUUUUUUUUUSH " + newAddress);
		setCurrentAddress(newAddress);
		this.flush = true;
		this.byteCounter = 4;
		fifoByte.flush();
		fifoWord.flush();
	}
	
	public void setStall(boolean stall){
		this.stall = stall;
	}
	
	public void setCurrentAddress(int currentAddress){
		this.startAddress = currentAddress;
		this.currentPhysicalAddress = currentAddress & 0xFFFFFFFC;
	}
	
	public void tick(){
		waitTime--;
		if(waitTime > 0){
			return;
		}
		if(flush){
			waitTime = FLUSH_WAIT;
			fifoByte.flush();
			fifoWord.flush();
			flush = false;
		} if(stall){
			// TODO
		} else {
			if(!fifoWord.isEmpty() && byteCounter == 4){
				
				currentAddr = fifoWord.pop();
				byteCounter = 0;
				while(currentAddr + byteCounter < startAddress){ // We have to do this because of word alignment
					byteCounter++;
				}
				
			}
			if(byteCounter < 4 && !fifoByte.isFull()){
				fifoByte.push(currentAddr+byteCounter);
				byteCounter++;
			}
			
			if(waitTimeCache > 0){
				waitTimeCache--;
			} else if(!fifoWord.isFull()){
				if(waitOnCache){
					waitOnCache = false;
					fifoWord.push(currentPhysicalAddress);
					currentPhysicalAddress += 4;
				} else {
					waitTimeCache = instructionCache.requestData(currentPhysicalAddress);
					if(waitTimeCache == 0){ // CACHE HIT	
						fifoWord.push(currentPhysicalAddress);
						currentPhysicalAddress += 4;
					} else { // CACHE MISS
						waitOnCache = true;
					}
				}
			}
			
		}
		
		
		
	}
	
	public byte[] getMemory(){
		return memory;
	}
}
