package functionalunit;

import tracer.TraceManager;
import exceptions.AmidarSimulatorException;
import functionalunit.FunctionalUnit.State;
import functionalunit.opcodes.FrameStackOpcodes;

public class FrameStack extends FunctionalUnit<FrameStackOpcodes> {

	
	final static int THREADS_MAX=1;
	final static int THREAD_WORDS=50000000;
	final static int LOCAL_VARS_INIT=32;
	final static int RESERVED_OVERFLOW=32;
	
	private enum datatype {EMPTY, VALUE, REF, META}
	int[] memory = new int[THREADS_MAX * THREAD_WORDS];
	datatype[] memtype=new datatype[THREADS_MAX * THREAD_WORDS];
	
	
	private class ThreadEntry {
		public int localsPointer;
		public int stackPointer;
		public int callerContextPointer;
		public int maxUsed;
		public int threadOffset;
		public boolean has_overflow;
		ThreadEntry(int threadOffset) {
			this.threadOffset=threadOffset;
		}
	}
	
	int localsPointer=0;
	int stackPointer=LOCAL_VARS_INIT+4;
	int callerContextPointer=LOCAL_VARS_INIT;
	int maxUsed=0;
	int threadOffset=0;
	int inputLow=0;
	int inputHigh=0;
	boolean overflow=false; //local overflow
	boolean exceptionValid;
	private ThreadEntry threadTable[]= new ThreadEntry[THREADS_MAX];
	ThreadEntry currentThread;
	
	//Fields for Garbage Collector Interface
	private boolean garbageCollectorRequest=false;
	private int garbageCollectorPointer=0;
	private int garbageCollectorThread=0;
	private enum gcState {IDLE,INIT,SELECT_THREAD_DELAY,SELECT_THREAD,ITERATE_DELAY,ITERATE,WAIT_ACK,FINISH_ACK}
	private gcState garbageCollectorState=gcState.IDLE;
	

	public FrameStack(String configFile, TraceManager traceManager) {
		super(FrameStackOpcodes.class, configFile, traceManager);
		threadTable[0]=new ThreadEntry(0);
		currentThread=threadTable[0];
	}

	@Override
	public int getNrOfInputports() {
		return 2;
	}

	
	private boolean checkOverflow(int adress) {
		if(adress>=(THREAD_WORDS-RESERVED_OVERFLOW-1)&&currentThread.has_overflow==false) {
			currentThread.has_overflow=true;
			overflow=true;
			setExceptionValid(true);
			//TODO: call Exception Interface here
			System.err.println("Stackoverflow Token " + opcode + " Adresse "+ adress);
			return true;
		}
		if(overflow) return true;
		if(adress>=maxUsed) {
			maxUsed=adress+1;
		}
		return false;
	}
	
	/**
	 * Set or Reset Exception State of FU
	 * has to call from Exception Interface to reset
	 * 
	 * @param exceptionValid 
	 */
	public void setExceptionValid(boolean exceptionValid) {
		this.exceptionValid=exceptionValid;
	}
	
	public int getMaxUsed(int thread) {
		saveCurrentThread();
		return threadTable[thread].maxUsed;
	}
	//Methods for Memory Access, include Offset
	/**
	 * Memory Access - Set one Entry with Value and Type
	 * check if Overflow occurs
	 * @param pos	relative Position in Thread
	 * @param value	Value
	 * @param type	Datatype (VALUE, REF, META)
	 * @return true if Access successful
	 * 			false if Overflow occurs
	 */
	private boolean memSet(int pos, int value, datatype type) {
		if(!checkOverflow(pos)) {
			memory[threadOffset + pos]=value;
			memtype[threadOffset + pos]=type;
			return true;
		}
		return false;
	}
	/**
	 * Memory Access - Copy one Entry to another Position
	 * check if Overflow occurs
	 * @param from	
	 * @param to
	 * @return true if successful
	 * 			false if Overflow
	 */
	private boolean memCopy(int from, int to) {
		if(!checkOverflow(to)) {
			memory[threadOffset + to] = memory[threadOffset + from];
			memtype[threadOffset + to] = memtype[threadOffset + from];
			return true;
		}
		return false;
	}
	private int memGet(int pos) {
		return memory[threadOffset + pos];
	}
	private datatype memGetType(int pos) {
		return memtype[threadOffset + pos];
	}
	
	private void saveCurrentThread() {
		currentThread.callerContextPointer=callerContextPointer;
		currentThread.localsPointer=localsPointer;
		currentThread.stackPointer=stackPointer;
		currentThread.maxUsed=maxUsed;
		currentThread.threadOffset=threadOffset;
	}
	
	/**
	 * Interface to Garbage Collector
	 * Garbage Collector Request Signal
	 * @param req true if Garbage Collector 
	 * 			  false if GarbageCollector finish
	 */
	public void setGCRequest(boolean req) {
		this.garbageCollectorRequest=req;
	}
	
	/**
	 * Interface to GarbageCollector
	 * @return  Reference of Object
	 * 			0 if no Data available
	 * 		   -1 if finished
	 */
	public int getGCItem() {
		if(garbageCollectorState==gcState.WAIT_ACK) {
			garbageCollectorState=gcState.ITERATE;
			return memGet(garbageCollectorPointer-1);
		}
		if(garbageCollectorState==gcState.FINISH_ACK) {
			return -1;
		}
		return 0;
	}
	
	private void executeGC() {
		switch(garbageCollectorState) {
		case INIT:
			saveCurrentThread();
			garbageCollectorThread=0;
		case SELECT_THREAD_DELAY:
			garbageCollectorState=gcState.SELECT_THREAD;
			break;
		case SELECT_THREAD:
			if(threadTable[garbageCollectorThread]!=null) {
				threadOffset=threadTable[garbageCollectorThread].threadOffset;
				garbageCollectorPointer=0;
				garbageCollectorState=gcState.ITERATE_DELAY;
			}
			else {
				if(garbageCollectorThread<THREADS_MAX-1) {
					garbageCollectorThread++;
					garbageCollectorState=gcState.SELECT_THREAD_DELAY;
				}
				else {
					garbageCollectorState=gcState.FINISH_ACK;
				}
			}
			break;
		case ITERATE_DELAY:
			garbageCollectorState = gcState.ITERATE;
			break;
		case ITERATE:
			if(garbageCollectorPointer >= threadTable[garbageCollectorThread].stackPointer) {
				if(garbageCollectorThread<THREADS_MAX-1) {
					garbageCollectorThread++;
					garbageCollectorState=gcState.SELECT_THREAD_DELAY;
				}
				else {
					garbageCollectorState=gcState.FINISH_ACK;
				}
			}
			else {
				if(memGetType(garbageCollectorPointer)==datatype.REF) {
					garbageCollectorState=gcState.WAIT_ACK;
					//Increment in HW erst nach Übergabe an GC, richtiger Wert wird trotzdem übergeben
				}
				garbageCollectorPointer++;
			}
			break;
		case FINISH_ACK:
			threadOffset=currentThread.threadOffset;
			if(!garbageCollectorRequest)
				currentState=State.IDLE;
				garbageCollectorState=gcState.IDLE;
			break;
		case IDLE: //dummy, never called here
		case WAIT_ACK:
			break;
		}
	}
	@Override
	public boolean executeOp(FrameStackOpcodes op) {
		long data;
		inputHigh=input[OPERAND_A_HIGH];
		inputLow=input[OPERAND_A_LOW];
		overflow=false;
		switch(op) {
		case THREADSWITCH:
			saveCurrentThread();
			currentThread=threadTable[inputLow];
			threadOffset=currentThread.threadOffset;
			callerContextPointer=currentThread.callerContextPointer;
			localsPointer=currentThread.localsPointer;
			stackPointer=currentThread.stackPointer;
			maxUsed=currentThread.maxUsed;
			break;
		case NEWTHREAD:
			threadTable[inputLow]=new ThreadEntry(inputLow*THREAD_WORDS);
			break;
		case REMOVETHREAD:
			threadTable[inputLow]=null;
			break;
			
		//Stack
		case PUSH32:
//			System.out.println("FS: push " + inputLow);
			if(memSet(stackPointer, inputLow, datatype.VALUE)) 
				stackPointer++;
			setResultAck(true);
			break;
		case PUSH64:
			if(memSet(stackPointer, inputLow, datatype.VALUE))
				stackPointer++;
			if(memSet(stackPointer, inputHigh, datatype.VALUE))
				stackPointer++;
			
//			System.out.println("PUSH: ");
//			long val = (((long) memGet(stackPointer-1)) <<32) | (((long) memGet(stackPointer-2))&0xFFFFFFFFL);
//			System.out.println(Double.longBitsToDouble(val));
			
			setResultAck(true);
			break;
		case PUSHREF:
			if(memSet(stackPointer, inputLow, datatype.REF))
				stackPointer++;
			setResultAck(true);
			break;
		case POP32:
			
			output[RESULT_LOW] =  memGet(--stackPointer);
			setOutputValid(RESULT_LOW);
			break;
		case POP64:
//			System.out.println("POP");
//			 val = (((long) memGet(stackPointer-1)) <<32) | (((long) memGet(stackPointer-2))&0xFFFFFFFFL);
//			System.out.println(Double.longBitsToDouble(val));
			output[RESULT_HIGH] =  memGet(--stackPointer);
			output[RESULT_LOW] =  memGet(--stackPointer);
			setOutputValid(RESULT_LOW);
			setOutputValid(RESULT_HIGH);
			break;
		case REMOVE32:
			stackPointer--;
			setResultAck(true);
			break;
		case REMOVE64:
			stackPointer-=2;
			setResultAck(true);
			break;
		case PEEK:
			output[RESULT_LOW]=memGet(stackPointer - inputLow);
			setOutputValid(RESULT_LOW);
			break;
		case PEEK_1:
			output[RESULT_LOW]=memGet(stackPointer - 1);
			setOutputValid(RESULT_LOW);
			break;
		case DUP:
			if(memCopy(stackPointer-1, stackPointer))
				stackPointer++;
			setResultAck(true);
			break;
		case DUP2:
			if(memCopy(stackPointer-2, stackPointer))
				stackPointer++;
			if(memCopy(stackPointer-2, stackPointer))
				stackPointer++;
			setResultAck(true);
			
//			System.out.println("DUP2");
//			long val = (((long) memGet(stackPointer-1)) <<32) | (((long) memGet(stackPointer-2))&0xFFFFFFFFL);
//			System.out.println(Double.longBitsToDouble(val));
//			val = (((long) memGet(stackPointer-3)) <<32) | (((long) memGet(stackPointer-4))&0xFFFFFFFFL);
//			System.out.println(Double.longBitsToDouble(val));
//			val = (((long) memGet(stackPointer-5)) <<32) | (((long) memGet(stackPointer-6))&0xFFFFFFFFL);
//			System.out.println(Double.longBitsToDouble(val));
			
			
			break;
		case DUP4:
			if(memCopy(stackPointer-4, stackPointer))
				stackPointer++;
			if(memCopy(stackPointer-4, stackPointer))
				stackPointer++;
			if(memCopy(stackPointer-4, stackPointer))
				stackPointer++;
			if(memCopy(stackPointer-4, stackPointer))
				stackPointer++;
			setResultAck(true);
			break;
		case DUP_X1:
			if(memCopy(stackPointer-1,stackPointer))
				stackPointer++;
			memCopy(stackPointer-3,stackPointer-2);
			memCopy(stackPointer-1, stackPointer-3);
			setResultAck(true);
			break;
		case DUP2_X1:
			if(memCopy(stackPointer-1,stackPointer+1)) {
				memCopy(stackPointer-2,stackPointer);
				memCopy(stackPointer-3,stackPointer-1);
				memCopy(stackPointer,stackPointer-3);
				memCopy(stackPointer+1,stackPointer-2);
				stackPointer+=2;
			}
			setResultAck(true);
			break;
		case DUP_X2:
			if(memCopy(stackPointer-1,stackPointer)) {
				memCopy(stackPointer-2,stackPointer-1);
				memCopy(stackPointer-3,stackPointer-2);
				memCopy(stackPointer,stackPointer-3);
				stackPointer++;
				setResultAck(true);
			}
			break;
		case DUP2_X2:
			
			for(int i=1;i<=4;i++) {
				memCopy(stackPointer-i ,stackPointer-i+2);
			}
			memCopy(stackPointer+1,stackPointer - 3);
			memCopy(stackPointer, stackPointer - 4);
			stackPointer+=2;
			setResultAck(true);
			break;
		case SWAP:
			data=memGet(stackPointer-1);
			datatype type=memGetType(stackPointer-1);
			memCopy(stackPointer-2,stackPointer-1);
			memSet(stackPointer-2,(int) data, type);
			setResultAck(true);
			break;
		case ACONST:
			if(memSet(stackPointer,0,datatype.REF))
				stackPointer++;
			setResultAck(true);
			break;
		case ICONST_M1:
			if(memSet(stackPointer,-1,datatype.VALUE))
				stackPointer++;
			setResultAck(true);
			break;
		case ICONST_0:
			if(memSet(stackPointer,0,datatype.VALUE))
				stackPointer++;
			setResultAck(true);
			break;
		case ICONST_1:
			if(memSet(stackPointer,1,datatype.VALUE))
				stackPointer++;
			setResultAck(true);
			break;
		case ICONST_2:
			if(memSet(stackPointer,2,datatype.VALUE))
				stackPointer++;
			setResultAck(true);
			break;
		case ICONST_3:
			if(memSet(stackPointer,3,datatype.VALUE))
				stackPointer++;
			setResultAck(true);
			break;
		case ICONST_4:
			if(memSet(stackPointer,4,datatype.VALUE))
				stackPointer++;
			setResultAck(true);
			break;
		case ICONST_5:
			if(memSet(stackPointer,5,datatype.VALUE))
				stackPointer++;
			setResultAck(true);
			break;
		case LCONST_0:
			if(memSet(stackPointer,0,datatype.VALUE))
				stackPointer++;
			if(memSet(stackPointer,0,datatype.VALUE))
				stackPointer++;
			setResultAck(true);
			break;
		case LCONST_1:
			if(memSet(stackPointer,1,datatype.VALUE))
				stackPointer++;
			if(memSet(stackPointer,0,datatype.VALUE))
				stackPointer++;
			setResultAck(true);
			break;
		case FCONST_0:
			if(memSet(stackPointer,Float.floatToIntBits(0),datatype.VALUE))
				stackPointer++;
			setResultAck(true);
			break;
		case FCONST_1:
			if(memSet(stackPointer,Float.floatToIntBits(1),datatype.VALUE))
				stackPointer++;
			setResultAck(true);
			break;
		case FCONST_2:
			if(memSet(stackPointer,Float.floatToIntBits(2),datatype.VALUE))
				stackPointer++;
			setResultAck(true);
			break;
		case DCONST_0:
			data=Double.doubleToLongBits(0);
			if(memSet(stackPointer,(int) data,datatype.VALUE))
				stackPointer++;
			if(memSet(stackPointer,(int) (data>>32),datatype.VALUE))
				stackPointer++;
			setResultAck(true);
			break;
		case DCONST_1:
			data=Double.doubleToLongBits(1);
			if(memSet(stackPointer,(int) data,datatype.VALUE))
				stackPointer++;
			if(memSet(stackPointer,(int) (data>>32),datatype.VALUE))
				stackPointer++;
			setResultAck(true);
			break;
		//Frame
		case INVOKE:
			int locals=(inputHigh>>16);
			int params=(inputHigh & 0xFF);
			boolean isNative = ((inputHigh>>8)&0x1) == 1;
			boolean hasReturn = ((inputHigh>>9)&0x1) == 1;
//			System.out.println("------ old amti" +(inputLow>>16));
//			System.out.println("               " + (inputLow & 0xFFFF));
			if(!isNative){
				int newCallerContextPointer=stackPointer-params+locals;
				if(!checkOverflow(newCallerContextPointer+4) ) {
					memSet(newCallerContextPointer, inputLow, datatype.META);
					memSet(newCallerContextPointer+1,localsPointer, datatype.META);
					localsPointer=stackPointer - params; //new localsPointer is old stackPointer after return
					memSet(newCallerContextPointer+2,localsPointer, datatype.META);
					memSet(newCallerContextPointer+3,callerContextPointer, datatype.META);
					callerContextPointer = newCallerContextPointer;
					stackPointer= newCallerContextPointer+4;
				}
				setResultAck(true);
			} else {
				stackPointer -=params;
				if(hasReturn){
					memSet(stackPointer++, inputLow, datatype.VALUE);
				}
				setResultAck(true);
			}
			break;
		case RETURN:
			output[RESULT_LOW] = memGet(callerContextPointer);
//			System.out.println("----------------------amti " + ((output[RESULT_LOW])>>16));
			localsPointer=memGet(callerContextPointer + 1);
			stackPointer=memGet(callerContextPointer + 2);
			callerContextPointer=memGet(callerContextPointer + 3);
			setOutputValid(RESULT_LOW);
			break;
		case RETURN32:
			data=stackPointer-1;
			output[RESULT_LOW] = memGet(callerContextPointer);
			localsPointer=memGet(callerContextPointer + 1);
			stackPointer=memGet(callerContextPointer + 2);
			callerContextPointer=memGet(callerContextPointer + 3);
			memCopy((int) data,stackPointer++);
			setOutputValid(RESULT_LOW);
			break;
		case RETURN64:
			data=stackPointer-2;
			output[RESULT_LOW] = memGet(callerContextPointer);
			localsPointer=memGet(callerContextPointer + 1);
			stackPointer=memGet(callerContextPointer + 2);
			callerContextPointer=memGet(callerContextPointer + 3);
			memCopy((int) data, stackPointer++);
			memCopy((int) data+1, stackPointer++);
			setOutputValid(RESULT_LOW);
			//zum DEBUG:
//			System.out.println("RETURN64 as Double: "+ Double.longBitsToDouble((((long) memGet((int) data+1) <<32) | ((long) memGet((int) data)&0xFFFFFFFFL))));
			break;
		case LOAD32:
			if(memCopy(localsPointer + (inputLow&0xFF),stackPointer))
				stackPointer++;
//			System.out.println("#############LOAD 32: " + inputLow + " : " + memory[threadOffset + localsPointer + inputLow]);
			setResultAck(true);
			break;
		case LOAD32_0:
			if(memCopy(localsPointer,stackPointer))
				stackPointer++;
			setResultAck(true);
			break;
		case LOAD32_1:
			if(memCopy(localsPointer + 1,stackPointer))
				stackPointer++;
//			System.out.println("FS load1 " + memory[stackPointer-1]);
			setResultAck(true);
			break;
		case LOAD32_2:
			if(memCopy(localsPointer + 2,stackPointer))
				stackPointer++;
//			System.out.println("FS load2 " + memory[stackPointer-1]);
			setResultAck(true);
			break;
		case LOAD32_3:
			if(memCopy(localsPointer + 3,stackPointer))
				stackPointer++;
//			System.out.println("load 32: " + 3 + " : " + memory[threadOffset + localsPointer + 3]);
			setResultAck(true);
			break;
		case LOAD64:
			if(memCopy(localsPointer + inputLow,stackPointer))
				stackPointer++;
			if(memCopy(localsPointer + inputLow+1,stackPointer))
				stackPointer++;
			setResultAck(true);
			break;
		case LOAD64_0:
			if(memCopy(localsPointer,stackPointer))
				stackPointer++;
			if(memCopy(localsPointer+1,stackPointer))
				stackPointer++;
			setResultAck(true);
			break;
		case LOAD64_1:
			if(memCopy(localsPointer+1,stackPointer))
				stackPointer++;
			if(memCopy(localsPointer+2,stackPointer))
				stackPointer++;
//			System.out.println("FFFFF 1  " + memory[threadOffset + stackPointer -1]+ " " + memory[threadOffset + stackPointer -2]);
			setResultAck(true);
			break;
		case LOAD64_2:
			if(memCopy(localsPointer+2,stackPointer))
				stackPointer++;
			if(memCopy(localsPointer+3,stackPointer))
				stackPointer++;
			setResultAck(true);
			break;
		case LOAD64_3:
			if(memCopy(localsPointer+3,stackPointer))
				stackPointer++;
			if(memCopy(localsPointer+4,stackPointer))
				stackPointer++;
//			System.out.println("FFFFF 3  " + memory[threadOffset + stackPointer -1]+ " " + memory[threadOffset + stackPointer -2]);
			
			setResultAck(true);
			break;
		case STORE32:
			
			memCopy(--stackPointer,localsPointer + (inputLow&0xFF));
//			System.out.println("store 32: " + inputLow + " : " + memory[threadOffset + localsPointer + inputLow]);
			setResultAck(true);
			break;
		case STORE32_0:
			memCopy(--stackPointer,localsPointer);
			setResultAck(true);
			break;	
		case STORE32_1:
			memCopy(--stackPointer,localsPointer + 1);
			setResultAck(true);
			break;
		case STORE32_2:
			memCopy(--stackPointer,localsPointer + 2);
			setResultAck(true);
			break;
		case STORE32_3:
			memCopy(--stackPointer,localsPointer + 3);
//			System.out.println("store 32: " + 3 + " : " + memory[threadOffset + localsPointer + 3]);
			setResultAck(true);
			break;
		case STORE64:
			memCopy(--stackPointer,localsPointer + inputLow + 1);
			memCopy(--stackPointer,localsPointer + inputLow);
			setResultAck(true);
			break;
		case STORE64_0:
			memCopy(--stackPointer,localsPointer+ 1);
			memCopy(--stackPointer,localsPointer);
			setResultAck(true);
			break;	
		case STORE64_1:
			memCopy(--stackPointer,localsPointer + 2);
			memCopy(--stackPointer,localsPointer + 1);
			setResultAck(true);
			break;
		case STORE64_2:
			memCopy(--stackPointer,localsPointer + 3);
			memCopy(--stackPointer,localsPointer + 2);
			setResultAck(true);
			break;	
		case STORE64_3:
			memCopy(--stackPointer,localsPointer + 4);
			memCopy(--stackPointer,localsPointer + 3);
			setResultAck(true);
			break;	
		case CLEARFRAME:
			stackPointer=callerContextPointer+4;
			setResultAck(true);
			break;
		}
		return true;
	}
	

	@Override
	public boolean validInputs(FrameStackOpcodes op) {
		switch(op){
		case PUSH32:
		case PUSHREF:
		case PEEK:
		case LOAD32:
		case LOAD64:
		case STORE32:
		case STORE64:
		case NEWTHREAD:
		case REMOVETHREAD:
		case THREADSWITCH:
			return inputValid[OPERAND_A_LOW];
		case PUSH64:
		case INVOKE:
			return inputValid[OPERAND_A_HIGH] && inputValid[OPERAND_A_LOW];
		default:
			return true;
		}
	}
	
	public boolean tick() {
		if(currentState == State.IDLE){
			if(garbageCollectorRequest) {
				currentState=State.BUSY;
				garbageCollectorState=gcState.INIT;
				return false;
			}
			if(exceptionValid) {
				//System.out.println("")
				throw new AmidarSimulatorException("Stackoverflow");
				//return false;
				// Im Fehlerfall keine Token annehmen, bis Auswertung
			}		
		}
		if(currentState==State.BUSY && garbageCollectorState!=gcState.IDLE) {
			executeGC();
			return false;
		}
		if(currentState == State.SENDING){
			count--;
			switch(opcode) {
			case POP32:
			case POP64:
			case RETURN:
				if(count>-1) return false;
				break;
			case RETURN32:
				if(count>-1) return false;
				break;
			default:
				break;
			}
		}

			boolean isReady = (currentState == State.IDLE) && !tokenValid; 
			if(currentState == State.SENDING){
				if(getResultAck()){
					currentState = State.IDLE;
//					for(int i = 0; i < inputValid.length; i++){
//						inputValid[i] = false;
//					}
					for(int i = 0; i < outputValid.length; i++){
						outputValid[i] = false;
					}
//					tokenAdapter.nextToken();
					setResultAck(false);
				}
			}
			State nextState = currentState;
			
			if(currentState == State.IDLE){
//				System.out.println(this + " öööö " + tokenValid + " + " + opcode);
				if(tokenValid && validInputs(opcode)){
					nextState = State.BUSY;
					count = getDuration(opcode);
					if(executeTrace.active()){
						executeTrace.println(this.toString()+ " starting "+ opcode + " ("+tag+")"); //TODO
					}
				} else if(!tokenValid){
					tokenAdapter.nextToken();
				}
			} else if(currentState == State.BUSY){
				count--;
				if(count <= 0){
					if(executeOp(opcode)){
						if(executeTrace.active()){
							executeTrace.println(this.toString()+ " executed "+ opcode + " ("+tag+")"); //TODO
							executeTrace.println("\toutput low: "+ output[RESULT_LOW]);
						}
						
						operandAck(); //bei FUs an verschiedenen Stellen (wann Daten annehmen und damit Einfluss auf Busbelegung)
						
						if(getResultAck()){
							
							nextState = State.IDLE;
							setResultAck(false);
//							if(tokenValid && validInputs(opcode)){
//								nextState = State.BUSY;
//								count = getDuration(opcode);
//								if(executeTrace.active()){
//									executeTrace.println(this.toString()+ " starting "+ opcode + " ("+tag+")"); //TODO
//								}
//							}
						}
						else
							nextState = State.SENDING;
						
					}
				}
			} else if(currentState == State.SENDING0){
				nextState = State.SENDING;
			}
			currentState = nextState;
			return isReady;
		}

}

