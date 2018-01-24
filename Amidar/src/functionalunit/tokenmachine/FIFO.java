package functionalunit.tokenmachine;

import exceptions.AmidarSimulatorException;

public class FIFO {
	

	int [] memory;
	
	int readPointer = 0, writePointer = 0;
	
	int depth;

	FIFO(int depth){
		memory = new int[depth+1]; // +1 makes it easier to calculate whether the fifo is full
		this.depth = depth+1;
	}
	
	public void push(int value){
		if(isFull()){
			throw new AmidarSimulatorException("FIFO is full");
		}
		memory[writePointer++] = value;
//		System.out.println("pushing val \t" + value + " to address \t" +(writePointer-1) );
		if(writePointer >= depth){
			writePointer -= depth;
		}
	}
	
	public int pop(){
		if(isEmpty()){
			throw new AmidarSimulatorException("FIFO is empty");
		}
		int val = memory[readPointer++];
//		System.out.println("POPPING val \t" + val + " from address \t" +(readPointer-1) );
		if(readPointer >= depth){
			readPointer -= depth;
		}
		return val;
	}

	public void flush(){
		readPointer = 0;
		writePointer = 0;
	}
	
	public boolean isFull(){
		if(readPointer == 0 ){
			return writePointer == depth-1;
		}
		return readPointer-1 == writePointer;
	}
	
	public boolean isEmpty(){
		return readPointer == writePointer;
	}
	
	public int nrOfEntries(){
		int nr = writePointer - readPointer;
		if( nr < 0){
			nr = nr + depth;
		}
		return nr;
	}
	
}
