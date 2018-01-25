package functionalunit;

import tracer.Trace;
import tracer.TraceManager;
import amidar.axtLoader.AXTDataSection;
import amidar.axtLoader.AXTFile;
import amidar.axtLoader.AXTFile_const;
import amidar.axtLoader.AXTLoader;
import exceptions.AmidarSimulatorException;
import functionalunit.cache.Cache;
//import functionalunit.cache.CoherencyProtocol;
import functionalunit.cache.HandleTableCache;
import functionalunit.cache.Memory;
import functionalunit.cache.coherency.CoherenceController;
import functionalunit.heap.ObjectHeapCONST;
import functionalunit.heap.ObjectHeapTrace;
import functionalunit.opcodes.HeapOpcodes;

/**
 * ObjectHeap
 * @author Patrick Appenheimer
 */

public class ObjectHeap extends FunctionalUnit<HeapOpcodes> implements ObjectHeapCONST {
	
	private int offset = 0;
	private int handle = 0;
	
	private int wait = 0;
	private int delay = 0;
	private int high = 0;
	
	public Memory mem;
	public Cache objectCache;
	public HandleTableCache htCache;
	private CoherenceController coherenceController;
	
	private ObjectHeapTrace trace;
	private boolean activeTrace;
	
//	private TraceManager traceManager;
	private Trace heapTrace;
	
	
	private int address;
	private int handles = 0;
	private int free;
	
	private int cti;
	private int flags;
	private int mid;
	private int size;
	private int dimcount;
	private int[] dimsizes;
	private int counterMultArray;
	private int dimStart;
	private int nextDimStart;
	private int returnHandle;
	private int fields;
	private int arraySize;
	
	public String configFile;
	
	public ObjectHeap(Memory mem, String configFile, TraceManager traceManager, boolean synthesis, CoherenceController coherenceController) {
		super(HeapOpcodes.class, configFile, traceManager);
		this.mem = mem;
		objectCache = new Cache(configFile, 99, synthesis, traceManager, coherenceController);
		htCache = coherenceController.getHTCache(); //new HandleTableCache(mem, configFile);//objectCache.getHTCache();
		
//		coherenceController.setHandleTableCache(htCache);
//		coherenceController.setL2Cache(mem.getL2Cache());
		//this.free = this.start;
//		this.traceManager = traceManager;
		heapTrace = traceManager.getf("heap");
		activeTrace = heapTrace.active();
		this.configFile = configFile;
		this.coherenceController = coherenceController;
	}


	public int getNrOfInputports() {
		return 6;
	}

	public void initHeap(AXTLoader axtLoader){
		//System.out.println("---------->initHeap");
		AXTFile axtFile = axtLoader.getAxtFile();
		AXTDataSection axtDataSection = axtFile.getDataSec();		
		
		byte[] objHeapByte = axtDataSection.getObjectHeap();
		int objHeapSize = objHeapByte.length;
		int[] objHeapInt = new int[(objHeapSize + 3) / 4];
		for(int i = 0; i < objHeapSize / 4; i++){
			objHeapInt[i] = (int) AXTFile.byteArrayToLong(objHeapByte, i * 4, 4);
		}
		this.fillHeap(objHeapInt);
		this.free = objHeapInt.length;
		
		byte[] handleTable = axtDataSection.getHandleTable();
		for(int i = 0; i < handleTable.length / AXTFile_const.HANDLETABLEENTRYSIZE; i++){
			int flags = axtDataSection.getHandleFlags(i);
			this.registerHandle(i, axtDataSection.getMID(i), flags, axtDataSection.getClassTableIndex(i), axtDataSection.getRefObjectSize(i), axtDataSection.getAbsoluteReference(i)/4);
		}
		
		if(activeTrace) trace = new ObjectHeapTrace("gen/HeapTrace_" + axtLoader.nameByPath() + ".BIN");
	}
	


	@Override
	public boolean executeOp(HeapOpcodes op) {		
		
//		System.out.println("---------->delay: " + delay  +  "     OP: " + op);	
		
		if(delay == 0){			

//			System.out.println("---------->executeOp: " +op);
//			System.out.println("---------->In A: " + input[OPERAND_A_LOW]);
//			System.out.println("---------->In B: " + input[OPERAND_B_LOW]);
//			System.out.println("---------->C low: " + input[OPERAND_C_LOW]);
//			System.out.println("---------->C high: " + input[OPERAND_C_HIGH]);
//
//			System.out.println("---------->Out low (previous tick): " + output[RESULT_LOW]);
//			System.out.println("---------->Out high (previous tick): " + output[RESULT_HIGH]);			
			
			switch(op){			
			case READ:
				switch(wait){
				case 0:
					delay = objectCache.requestData(handle, offset);
					if(delay == 0){
						output[RESULT_LOW] = objectCache.getData(handle, offset);
						setOutputValid(RESULT_LOW);
						if(activeTrace) if(activeTrace) trace.appendTrace(READ, 0, 0, 0, 0, 0, output[RESULT_LOW], 0);
						return true;
					}
					else{
						delay--;
						wait = 1;
						return false;
					}
				case 1:
					output[RESULT_LOW] = objectCache.getData(handle, offset);
					setOutputValid(RESULT_LOW);
					wait = 0;
					if(activeTrace) trace.appendTrace(READ, 0, 0, 0, 0, 0, output[RESULT_LOW], 0);
					return true;
				}
				
			case READ_64:
				switch(high){
				case 0:
					switch(wait){
					case 0:
						delay = objectCache.requestData(handle, offset);
						if(delay == 0){
							output[RESULT_LOW] = objectCache.getData(handle, offset);
							//setOutputValid(RESULT_LOW);
							high = 1;
							return false;
						}
						else{
							delay--;
							wait = 1;
							return false;
						}
					case 1:
						output[RESULT_LOW] = objectCache.getData(handle, offset);
						//setOutputValid(RESULT_LOW);
						wait = 0;
						high = 1;
						return false;
					}
				case 1:
					switch(wait){
					case 0:
						delay = objectCache.requestData(handle, offset+1);
						if(delay == 0){
							output[RESULT_HIGH] = objectCache.getData(handle, offset);
							setOutputValid(RESULT_LOW);
							setOutputValid(RESULT_HIGH);
							high = 0;
							if(activeTrace) trace.appendTrace(READ_64, 0, 0, 0, 0, 0, output[RESULT_LOW], output[RESULT_HIGH]);
							return true;
						}
						else{
							delay--;
							wait = 1;
							return false;
						}
					case 1:
						output[RESULT_HIGH] = objectCache.getData(handle, offset);
						setOutputValid(RESULT_LOW);
						setOutputValid(RESULT_HIGH);
						wait = 0;
						high = 0;
						if(activeTrace) trace.appendTrace(READ_64, 0, 0, 0, 0, 0, output[RESULT_LOW], output[RESULT_HIGH]);
						return true;
					}
				}
				
			case READ_ARRAY:
				switch(wait){
				case 0:
					int delayHT = htCache.requestData(handle);
					arraySize = htCache.getSize();
					delay = objectCache.requestData(handle, offset);
					if(delayHT > delay) delay = delayHT;
					if(delay == 0){
						if(offset >= arraySize) throw new AmidarSimulatorException("ArrayIndexOutOfBounds: "+offset + " ("+arraySize+")");
						output[RESULT_LOW] = objectCache.getData(handle, offset);
						setOutputValid(RESULT_LOW);
						if(activeTrace) trace.appendTrace(READ_ARRAY, 0, 0, 0, 0, 0, output[RESULT_LOW], 0);
						return true;
					}
					else{
						delay--;
						wait = 1;
						return false;
					}
				case 1:
					if(offset >= arraySize) throw new AmidarSimulatorException("ArrayIndexOutOfBounds: "+offset + " ("+arraySize+")");
					output[RESULT_LOW] = objectCache.getData(handle, offset);
					setOutputValid(RESULT_LOW);
					wait = 0;
					if(activeTrace) trace.appendTrace(READ_ARRAY, 0, 0, 0, 0, 0, output[RESULT_LOW], 0);
					return true;
				}
				
			case READ_ARRAY_64:
				switch(high){
				case 0:
					switch(wait){
					case 0:
						int delayHT = htCache.requestData(handle);
						arraySize = htCache.getSize();
						delay = objectCache.requestData(handle, offset);
						if(delayHT > delay) delay = delayHT;
						if(delay == 0){
							if(offset >= arraySize) throw new AmidarSimulatorException("ArrayIndexOutOfBounds: "+offset + " ("+arraySize+")");
							output[RESULT_LOW] = objectCache.getData(handle, offset);
							//setOutputValid(RESULT_LOW);
							high = 1;
							return false;
						}
						else{
							delay--;
							wait = 1;
							return false;
						}
					case 1:
						if(offset >= arraySize) throw new AmidarSimulatorException("ArrayIndexOutOfBounds: "+offset + " ("+arraySize+")");
						output[RESULT_LOW] = objectCache.getData(handle, offset);
						//setOutputValid(RESULT_LOW);
						wait = 0;
						high = 1;
						return false;
					}
				case 1:
					switch(wait){
					case 0:
						delay = objectCache.requestData(handle, offset+1);
						if(delay == 0){
							output[RESULT_HIGH] = objectCache.getData(handle, offset);
							setOutputValid(RESULT_LOW);
							setOutputValid(RESULT_HIGH);
							high = 0;
							if(activeTrace) trace.appendTrace(READ_ARRAY_64, 0, 0, 0, 0, 0, output[RESULT_LOW], output[RESULT_HIGH]);
							return true;
						}
						else{
							delay--;
							wait = 1;
							return false;
						}
					case 1:
						output[RESULT_HIGH] = objectCache.getData(handle, offset);
						setOutputValid(RESULT_LOW);
						setOutputValid(RESULT_HIGH);
						wait = 0;
						high = 0;
						if(activeTrace) trace.appendTrace(READ_ARRAY_64, 0, 0, 0, 0, 0, output[RESULT_LOW], output[RESULT_HIGH]);
						return true;
					}
				}
				
			case WRITE:
				switch(wait){
				case 0:
					delay = objectCache.writeData(handle, offset, input[OPERAND_C_LOW]);
					if(delay == 0){
						setResultAck(true);
						if(activeTrace) trace.appendTrace(WRITE, 0, 0, 0, input[OPERAND_C_LOW], 0, 0, 0);
						return true;
					}
					else{
						delay--;
						wait = 1;
						return false;
					}
				case 1:
					setResultAck(true);					
					wait = 0;
					if(activeTrace) trace.appendTrace(WRITE, 0, 0, 0, input[OPERAND_C_LOW], 0, 0, 0);
					return true;
				}
				
			case WRITE_64:
				switch(high){
				case 0:
					switch(wait){
					case 0:
						delay = objectCache.writeData(handle, offset, input[OPERAND_C_LOW]);
						if(delay == 0){
							high = 1;
							return false;
						}
						else{
							delay--;
							wait = 1;
							return false;
						}
					case 1:					
						wait = 0;
						high = 1;
						return false;
					}
				case 1:
					switch(wait){
					case 0:
						delay = objectCache.writeData(handle, offset+1, input[OPERAND_C_HIGH]);
						if(delay == 0){							
							high = 0;
							setResultAck(true);
							if(activeTrace) trace.appendTrace(WRITE_64, 0, 0, 0, input[OPERAND_C_LOW], input[OPERAND_C_HIGH], 0, 0);
							return true;
						}
						else{
							delay--;
							wait = 1;
							return false;
						}
					case 1:
						wait = 0;
						high = 0;
						setResultAck(true);
						if(activeTrace) trace.appendTrace(WRITE_64, 0, 0, 0, input[OPERAND_C_LOW], input[OPERAND_C_HIGH], 0, 0);
						return true;
					}
				}
				
			case WRITE_ARRAY:
				switch(wait){
				case 0:
					int delayHT = htCache.requestData(handle);
					arraySize = htCache.getSize();
					if(offset >= arraySize) throw new AmidarSimulatorException("ArrayIndexOutOfBounds: "+offset + " ("+arraySize+")");
					delay = objectCache.writeData(handle, offset, input[OPERAND_C_LOW]);
					if(delayHT > delay) delay = delayHT;
					if(delay == 0){						
						setResultAck(true);
						if(activeTrace) trace.appendTrace(WRITE_ARRAY, 0, 0, 0, input[OPERAND_C_LOW], 0, 0, 0);
						return true;
					}
					else{
						delay--;
						wait = 1;
						return false;
					}
				case 1:
					setResultAck(true);
					wait = 0;
					if(activeTrace) trace.appendTrace(WRITE_ARRAY, 0, 0, 0, input[OPERAND_C_LOW], 0, 0, 0);
					return true;
				}
					
			case WRITE_ARRAY_64:
				switch(high){
				case 0:
					switch(wait){
					case 0:
						int delayHT = htCache.requestData(handle);
						arraySize = htCache.getSize();
						if(offset >= arraySize) throw new AmidarSimulatorException("ArrayIndexOutOfBounds: "+offset + " ("+arraySize+")");
						delay = objectCache.writeData(handle, offset, input[OPERAND_C_LOW]);
						if(delayHT > delay) delay = delayHT;
						if(delay == 0){
							high = 1;
							return false;
						}
						else{
							delay--;
							wait = 1;
							return false;
						}
					case 1:					
						wait = 0;
						high = 1;
						return false;
					}
				case 1:
					switch(wait){
					case 0:					
						if(offset+1 >= arraySize) throw new AmidarSimulatorException("ArrayIndexOutOfBounds: "+offset + " ("+arraySize+")");
						delay = objectCache.writeData(handle, offset+1, input[OPERAND_C_HIGH]);
						if(delay == 0){							
							high = 0;
							setResultAck(true);
							if(activeTrace) trace.appendTrace(WRITE_ARRAY_64, 0, 0, 0, input[OPERAND_C_LOW], input[OPERAND_C_HIGH], 0, 0);
							return true;
						}
						else{
							delay--;
							wait = 1;
							return false;
						}
					case 1:
						wait = 0;
						high = 0;
						setResultAck(true);
						if(activeTrace) trace.appendTrace(WRITE_ARRAY_64, 0, 0, 0, input[OPERAND_C_LOW], input[OPERAND_C_HIGH], 0, 0);
						return true;
					}
				}
				
			case PIO_READ:				
				switch(wait){
				case 0:
					this.offset++;
					delay = objectCache.requestData(handle, offset);
					if(delay == 0){
						output[RESULT_LOW] = objectCache.getData(handle, offset);
						setOutputValid(RESULT_LOW);
						if(activeTrace) trace.appendTrace(PIO_READ, 0, 0, 0, 0, 0, output[RESULT_LOW], 0);
						return true;
					}
					else{
						delay--;
						wait = 1;
						return false;
					}
				case 1:
					output[RESULT_LOW] = objectCache.getData(handle, offset);
					setOutputValid(RESULT_LOW);
					wait = 0;
					if(activeTrace) trace.appendTrace(PIO_READ, 0, 0, 0, 0, 0, output[RESULT_LOW], 0);
					return true;
				}
					
			case PIO_READ_64:				
				switch(high){
				case 0:
					switch(wait){
					case 0:
						this.offset = offset+2;
						delay = objectCache.requestData(handle, offset);
						if(delay == 0){
							output[RESULT_LOW] = objectCache.getData(handle, offset);
							high = 1;
							return false;
						}
						else{
							delay--;
							wait = 1;
							return false;
						}
					case 1:
						output[RESULT_LOW] = objectCache.getData(handle, offset);
						wait = 0;
						high = 1;
						return false;
					}
				case 1:
					switch(wait){
					case 0:
						delay = objectCache.requestData(handle, offset+1);
						if(delay == 0){
							output[RESULT_HIGH] = objectCache.getData(handle, offset);
							setOutputValid(RESULT_LOW);
							setOutputValid(RESULT_HIGH);
							high = 0;
							if(activeTrace) trace.appendTrace(PIO_READ_64, 0, 0, 0, 0, 0, output[RESULT_LOW], output[RESULT_HIGH]);
							return true;
						}
						else{
							delay--;
							wait = 1;
							return false;
						}
					case 1:
						output[RESULT_HIGH] = objectCache.getData(handle, offset);
						setOutputValid(RESULT_LOW);
						setOutputValid(RESULT_HIGH);
						wait = 0;
						high = 0;
						if(activeTrace) trace.appendTrace(PIO_READ_64, 0, 0, 0, 0, 0, output[RESULT_LOW], output[RESULT_HIGH]);
						return true;
					}
				}
					
			case PIO_READ_ARRAY:			
				switch(wait){
				case 0:
					this.offset++;
					int delayHT = htCache.requestData(handle);
					arraySize = htCache.getSize();
					delay = objectCache.requestData(handle, offset);
					if(delayHT > delay) delay = delayHT;
					if(delay == 0){
						if(offset >= arraySize) throw new AmidarSimulatorException("ArrayIndexOutOfBounds: "+offset + " ("+arraySize+")");
						output[RESULT_LOW] = objectCache.getData(handle, offset);
						setOutputValid(RESULT_LOW);
						if(activeTrace) trace.appendTrace(PIO_READ_ARRAY, 0, 0, 0, 0, 0, output[RESULT_LOW], 0);
						return true;
					}
					else{
						delay--;
						wait = 1;
						return false;
					}
				case 1:
					if(offset >= arraySize) throw new AmidarSimulatorException("ArrayIndexOutOfBounds: "+offset + " ("+arraySize+")");
					output[RESULT_LOW] = objectCache.getData(handle, offset);
					setOutputValid(RESULT_LOW);
					wait = 0;
					if(activeTrace) trace.appendTrace(PIO_READ_ARRAY, 0, 0, 0, 0, 0, output[RESULT_LOW], 0);
					return true;
				}
					
			case PIO_READ_ARRAY_64:				
				switch(high){
				case 0:
					switch(wait){
					case 0:
						this.offset=offset+2;
						int delayHT = htCache.requestData(handle);
						arraySize = htCache.getSize();
						delay = objectCache.requestData(handle, offset);
						if(delayHT > delay) delay = delayHT;
						if(delay == 0){
							if(offset >= arraySize) throw new AmidarSimulatorException("ArrayIndexOutOfBounds: "+offset + " ("+arraySize+")");
							output[RESULT_LOW] = objectCache.getData(handle, offset);
							high = 1;
							return false;
						}
						else{
							delay--;
							wait = 1;
							return false;
						}
					case 1:
						if(offset >= arraySize) throw new AmidarSimulatorException("ArrayIndexOutOfBounds: "+offset + " ("+arraySize+")");
						output[RESULT_LOW] = objectCache.getData(handle, offset);
						wait = 0;
						high = 1;
						return false;
					}
				case 1:
					switch(wait){
					case 0:
						delay = objectCache.requestData(handle, offset+1);
						if(delay == 0){
							output[RESULT_HIGH] = objectCache.getData(handle, offset);
							setOutputValid(RESULT_LOW);
							setOutputValid(RESULT_HIGH);
							high = 0;
							if(activeTrace) trace.appendTrace(PIO_READ_ARRAY_64, 0, 0, 0, 0, 0, output[RESULT_LOW], output[RESULT_HIGH]);
							return true;
						}
						else{
							delay--;
							wait = 1;
							return false;
						}
					case 1:
						output[RESULT_HIGH] = objectCache.getData(handle, offset);
						setOutputValid(RESULT_LOW);
						setOutputValid(RESULT_HIGH);
						wait = 0;
						high = 0;
						if(activeTrace) trace.appendTrace(PIO_READ_ARRAY_64, 0, 0, 0, 0, 0, output[RESULT_LOW], output[RESULT_HIGH]);
						return true;
					}
				}	
				
			case PIO_WRITE:
				switch(wait){
				case 0:
					this.offset++;
					delay = objectCache.writeData(handle, offset, input[OPERAND_C_LOW]);
					if(delay == 0){
						setResultAck(true);
						if(activeTrace) trace.appendTrace(PIO_WRITE, 0, 0, 0, input[OPERAND_C_LOW], 0, 0, 0);
						return true;
					}
					else{
						delay--;
						wait = 1;
						return false;
					}
				case 1:
					setResultAck(true);					
					wait = 0;
					if(activeTrace) trace.appendTrace(PIO_WRITE, 0, 0, 0, input[OPERAND_C_LOW], 0, 0, 0);
					return true;
				}
				
			case PIO_WRITE_64:
				switch(high){
				case 0:
					switch(wait){
					case 0:
						this.offset=offset+2;
						delay = objectCache.writeData(handle, offset, input[OPERAND_C_LOW]);
						if(delay == 0){
							high = 1;
							return false;
						}
						else{
							delay--;
							wait = 1;
							return false;
						}
					case 1:					
						wait = 0;
						high = 1;
						return false;
					}
				case 1:
					switch(wait){
					case 0:
						delay = objectCache.writeData(handle, offset+1, input[OPERAND_C_HIGH]);
						if(delay == 0){							
							high = 0;
							setResultAck(true);
							if(activeTrace) trace.appendTrace(PIO_WRITE_64, 0, 0, 0, input[OPERAND_C_LOW], input[OPERAND_C_HIGH], 0, 0);
							return true;
						}
						else{
							delay--;
							wait = 1;
							return false;
						}
					case 1:
						wait = 0;
						high = 0;
						setResultAck(true);
						if(activeTrace) trace.appendTrace(PIO_WRITE_64, 0, 0, 0, input[OPERAND_C_LOW], input[OPERAND_C_HIGH], 0, 0);
						return true;
					}
				}
					
			case PIO_WRITE_ARRAY:
				switch(wait){
				case 0:
					this.offset++;
					int delayHT = htCache.requestData(handle);
					arraySize = htCache.getSize();
					if(offset >= arraySize) throw new AmidarSimulatorException("ArrayIndexOutOfBounds: "+offset + " ("+arraySize+")");
					delay = objectCache.writeData(handle, offset, input[OPERAND_C_LOW]);
					if(delayHT > delay) delay = delayHT;
					if(delay == 0){						
						setResultAck(true);
						if(activeTrace) trace.appendTrace(PIO_WRITE_ARRAY, 0, 0, 0, input[OPERAND_C_LOW], 0, 0, 0);
						return true;
					}
					else{
						delay--;
						wait = 1;
						return false;
					}
				case 1:
					setResultAck(true);
					wait = 0;
					if(activeTrace) trace.appendTrace(PIO_WRITE_ARRAY, 0, 0, 0, input[OPERAND_C_LOW], 0, 0, 0);
					return true;
				}
				
			case PIO_WRITE_ARRAY_64:
				switch(high){
				case 0:
					switch(wait){
					case 0:
						this.offset=offset+2;
						int delayHT = htCache.requestData(handle);
						arraySize = htCache.getSize();
						if(offset >= arraySize) throw new AmidarSimulatorException("ArrayIndexOutOfBounds: "+offset + " ("+arraySize+")");
						delay = objectCache.writeData(handle, offset, input[OPERAND_C_LOW]);
						if(delayHT > delay) delay = delayHT;
						if(delay == 0){
							high = 1;
							return false;
						}
						else{
							delay--;
							wait = 1;
							return false;
						}
					case 1:					
						wait = 0;
						high = 1;
						return false;
					}
				case 1:
					switch(wait){
					case 0:					
						if(offset+1 >= arraySize) throw new AmidarSimulatorException("ArrayIndexOutOfBounds: "+offset + " ("+arraySize+")");
						delay = objectCache.writeData(handle, offset+1, input[OPERAND_C_HIGH]);
						if(delay == 0){							
							high = 0;
							setResultAck(true);
							if(activeTrace) trace.appendTrace(PIO_WRITE_ARRAY_64, 0, 0, 0, input[OPERAND_C_LOW], input[OPERAND_C_HIGH], 0, 0);
							return true;
						}
						else{
							delay--;
							wait = 1;
							return false;
						}
					case 1:
						wait = 0;
						high = 0;
						setResultAck(true);
						if(activeTrace) trace.appendTrace(PIO_WRITE_ARRAY_64, 0, 0, 0, input[OPERAND_C_LOW], input[OPERAND_C_HIGH], 0, 0);
						return true;
					}
				}

			case HO_READ:
				switch(wait){
				case 0:
					this.handle = input[OPERAND_A_LOW];
					this.offset = input[OPERAND_B_LOW];
					delay = objectCache.requestData(handle, offset);
					if(delay == 0){
						output[RESULT_LOW] = objectCache.getData(handle, offset);
						setOutputValid(RESULT_LOW);
						if(activeTrace) trace.appendTrace(HO_READ, 0, input[OPERAND_A_LOW], input[OPERAND_B_LOW], 0, 0, output[RESULT_LOW], 0);
						return true;
					}
					else{
						delay--;
						wait = 1;
						return false;
					}
				case 1:
					output[RESULT_LOW] = objectCache.getData(handle, offset);
					setOutputValid(RESULT_LOW);
					wait = 0;
					if(activeTrace) trace.appendTrace(HO_READ, 0, input[OPERAND_A_LOW], input[OPERAND_B_LOW], 0, 0, output[RESULT_LOW], 0);
					return true;
				}
				
			case HO_READ_64:				
				switch(high){
				case 0:
					switch(wait){
					case 0:
						this.handle = input[OPERAND_A_LOW];
						this.offset = input[OPERAND_B_LOW];
						delay = objectCache.requestData(handle, offset);
						if(delay == 0){
							output[RESULT_LOW] = objectCache.getData(handle, offset);
							high = 1;
							return false;
						}
						else{
							delay--;
							wait = 1;
							return false;
						}
					case 1:
						output[RESULT_LOW] = objectCache.getData(handle, offset);
						wait = 0;
						high = 1;
						return false;
					}
				case 1:
					switch(wait){
					case 0:
						delay = objectCache.requestData(handle, offset+1);
						if(delay == 0){
							output[RESULT_HIGH] = objectCache.getData(handle, offset+1);
							setOutputValid(RESULT_LOW);
							setOutputValid(RESULT_HIGH);
							high = 0;
//							System.out.println("HO_READ_64 " +  input[OPERAND_A_LOW] + " " + input[OPERAND_B_LOW] + " " + output[RESULT_LOW] + " " + output[RESULT_HIGH]);
//							System.out.println("AAAPTRACE "  + (output[RESULT_LOW]&0xFFFFFFFFL + ((long)output[RESULT_HIGH])<<32));
							if(activeTrace) trace.appendTrace(HO_READ_64, 0, input[OPERAND_A_LOW], input[OPERAND_B_LOW], 0, 0, output[RESULT_LOW], output[RESULT_HIGH]);
							return true;
						}
						else{
							delay--;
							wait = 1;
							return false;
						}
					case 1:
						output[RESULT_HIGH] = objectCache.getData(handle, offset+1);
						setOutputValid(RESULT_LOW);
						setOutputValid(RESULT_HIGH);
						wait = 0;
						high = 0;
//						System.out.println("HO_READ_64 " +  input[OPERAND_A_LOW] + " " + input[OPERAND_B_LOW] + " " + output[RESULT_LOW] + " " + output[RESULT_HIGH]);
						
						if(activeTrace) trace.appendTrace(HO_READ_64, 0, input[OPERAND_A_LOW], input[OPERAND_B_LOW], 0, 0, output[RESULT_LOW], output[RESULT_HIGH]);
						return true;
					}
				}
				
				
			case HO_READ_ARRAY:				
				switch(wait){
				case 0:
					this.handle = input[OPERAND_A_LOW];
					this.offset = input[OPERAND_B_LOW];
					int delayHT = htCache.requestData(handle);
					arraySize = htCache.getSize();
					delay = objectCache.requestData(handle, offset);
					if(delayHT > delay) delay = delayHT;
					if(delay == 0){
						if(offset >= arraySize){
							throw new AmidarSimulatorException("ArrayIndexOutOfBounds: "+offset + " ("+arraySize+")");
						}
						output[RESULT_LOW] = objectCache.getData(handle, offset);
						setOutputValid(RESULT_LOW);
						if(activeTrace) trace.appendTrace(HO_READ_ARRAY, 0, input[OPERAND_A_LOW], input[OPERAND_B_LOW], 0, 0, output[RESULT_LOW], 0);
						return true;
					}
					else{
						delay--;
						wait = 1;
						return false;
					}
				case 1:
					if(offset >= arraySize){
						throw new AmidarSimulatorException("ArrayIndexOutOfBounds: "+offset + " ("+arraySize+")");
					}
					output[RESULT_LOW] = objectCache.getData(handle, offset);
					setOutputValid(RESULT_LOW);
					wait = 0;
					if(activeTrace) trace.appendTrace(HO_READ_ARRAY, 0, input[OPERAND_A_LOW], input[OPERAND_B_LOW], 0, 0, output[RESULT_LOW], 0);
					return true;
				}
				
			case HO_READ_ARRAY_64:
				switch(high){
				case 0:
					switch(wait){
					case 0:
						this.handle = input[OPERAND_A_LOW];
						this.offset = input[OPERAND_B_LOW]*2;
						int delayHT = htCache.requestData(handle);
						arraySize = htCache.getSize();
						delay = objectCache.requestData(handle, offset);
						if(delayHT > delay) delay = delayHT;
						if(delay == 0){
							if(offset >= arraySize*2) throw new AmidarSimulatorException("ArrayIndexOutOfBounds: "+offset + " ("+arraySize+")");
							output[RESULT_LOW] = objectCache.getData(handle, offset);
							high = 1;
							return false;
						}
						else{
							delay--;
							wait = 1;
							return false;
						}
					case 1:
						if(offset >= arraySize*2) throw new AmidarSimulatorException("ArrayIndexOutOfBounds: "+offset + " ("+arraySize+")");
						output[RESULT_LOW] = objectCache.getData(handle, offset);
						wait = 0;
						high = 1;
						return false;
					}
				case 1:
					switch(wait){
					case 0:
						delay = objectCache.requestData(handle, offset+1);
						if(delay == 0){
							output[RESULT_HIGH] = objectCache.getData(handle, offset+1);
							setOutputValid(RESULT_LOW);
							setOutputValid(RESULT_HIGH);
							high = 0;
							if(activeTrace) trace.appendTrace(HO_READ_ARRAY_64, 0, input[OPERAND_A_LOW], input[OPERAND_B_LOW], 0, 0, output[RESULT_LOW], output[RESULT_HIGH]);
							return true;
						}
						else{
							delay--;
							wait = 1;
							return false;
						}
					case 1:
						output[RESULT_HIGH] = objectCache.getData(handle, offset+1);
						setOutputValid(RESULT_LOW);
						setOutputValid(RESULT_HIGH);
						wait = 0;
						high = 0;
						if(activeTrace) trace.appendTrace(HO_READ_ARRAY_64, 0, input[OPERAND_A_LOW], input[OPERAND_B_LOW], 0, 0, output[RESULT_LOW], output[RESULT_HIGH]);
						return true;
					}
				}

			case H_READ:
				switch(wait){
				case 0:
					this.handle = input[OPERAND_A_LOW];
					delay = objectCache.requestData(handle, offset);
					if(delay == 0){
						output[RESULT_LOW] = objectCache.getData(handle, offset);
						setOutputValid(RESULT_LOW);
						if(activeTrace) trace.appendTrace(H_READ, 0, input[OPERAND_A_LOW], 0, 0, 0, output[RESULT_LOW], 0);
						return true;
					}
					else{
						delay--;
						wait = 1;
						return false;
					}
				case 1:
					output[RESULT_LOW] = objectCache.getData(handle, offset);
					setOutputValid(RESULT_LOW);
					wait = 0;
					if(activeTrace) trace.appendTrace(H_READ, 0, input[OPERAND_A_LOW], 0, 0, 0, output[RESULT_LOW], 0);
					return true;
				}
					
			case H_READ_64:
				switch(high){
				case 0:
					switch(wait){
					case 0:
						this.handle = input[OPERAND_A_LOW];
						delay = objectCache.requestData(handle, offset);
						if(delay == 0){
							output[RESULT_LOW] = objectCache.getData(handle, offset);
							high = 1;
							return false;
						}
						else{
							delay--;
							wait = 1;
							return false;
						}
					case 1:
						output[RESULT_LOW] = objectCache.getData(handle, offset);
						wait = 0;
						high = 1;
						return false;
					}
				case 1:
					switch(wait){
					case 0:
						delay = objectCache.requestData(handle, offset+1);
						if(delay == 0){
							output[RESULT_HIGH] = objectCache.getData(handle, offset+1);
							setOutputValid(RESULT_LOW);
							setOutputValid(RESULT_HIGH);
							high = 0;
							if(activeTrace) trace.appendTrace(H_READ_64, 0, input[OPERAND_A_LOW], 0, 0, 0, output[RESULT_LOW], output[RESULT_HIGH]);
							return true;
						}
						else{
							delay--;
							wait = 1;
							return false;
						}
					case 1:
						output[RESULT_HIGH] = objectCache.getData(handle, offset +1);
						setOutputValid(RESULT_LOW);
						setOutputValid(RESULT_HIGH);
						wait = 0;
						high = 0;
						if(activeTrace) trace.appendTrace(H_READ_64, 0, input[OPERAND_A_LOW], 0, 0, 0, output[RESULT_LOW], output[RESULT_HIGH]);
						return true;
					}
				}
					
					
			case H_READ_ARRAY:
				switch(wait){
				case 0:
					this.handle = input[OPERAND_A_LOW];
					int delayHT = htCache.requestData(handle);
					arraySize = htCache.getSize();
					delay = objectCache.requestData(handle, offset);
					if(delayHT > delay) delay = delayHT;
					if(delay == 0){
						if(offset >= arraySize) throw new AmidarSimulatorException("ArrayIndexOutOfBounds: "+offset + " ("+arraySize+")");
						output[RESULT_LOW] = objectCache.getData(handle, offset);
						setOutputValid(RESULT_LOW);
						if(activeTrace) trace.appendTrace(H_READ_ARRAY, 0, input[OPERAND_A_LOW], 0, 0, 0, output[RESULT_LOW], 0);
						return true;
					}
					else{
						delay--;
						wait = 1;
						return false;
					}
				case 1:
					if(offset >= arraySize) throw new AmidarSimulatorException("ArrayIndexOutOfBounds: "+offset + " ("+arraySize+")");
					output[RESULT_LOW] = objectCache.getData(handle, offset);
					setOutputValid(RESULT_LOW);
					wait = 0;
					if(activeTrace) trace.appendTrace(H_READ_ARRAY, 0, input[OPERAND_A_LOW], 0, 0, 0, output[RESULT_LOW], 0);
					return true;
				}
					
			case H_READ_ARRAY_64:
				switch(high){
				case 0:
					switch(wait){
					case 0:
						this.handle = input[OPERAND_A_LOW];
						int delayHT = htCache.requestData(handle);
						arraySize = htCache.getSize();
						delay = objectCache.requestData(handle, offset);
						if(delayHT > delay) delay = delayHT;
						if(delay == 0){
							if(offset >= arraySize) throw new AmidarSimulatorException("ArrayIndexOutOfBounds: "+offset + " ("+arraySize+")");
							output[RESULT_LOW] = objectCache.getData(handle, offset);
							high = 1;
							return false;
						}
						else{
							delay--;
							wait = 1;
							return false;
						}
					case 1:
						if(offset >= arraySize) throw new AmidarSimulatorException("ArrayIndexOutOfBounds: "+offset + " ("+arraySize+")");
						output[RESULT_LOW] = objectCache.getData(handle, offset);
						wait = 0;
						high = 1;
						return false;
					}
				case 1:
					switch(wait){
					case 0:
						delay = objectCache.requestData(handle, offset+1);
						if(delay == 0){
							output[RESULT_HIGH] = objectCache.getData(handle, offset+1);
							setOutputValid(RESULT_LOW);
							setOutputValid(RESULT_HIGH);
							high = 0;
							if(activeTrace) trace.appendTrace(H_READ_ARRAY_64, 0, input[OPERAND_A_LOW], 0, 0, 0, output[RESULT_LOW], output[RESULT_HIGH]);
							return true;
						}
						else{
							delay--;
							wait = 1;
							return false;
						}
					case 1:
						output[RESULT_HIGH] = objectCache.getData(handle, offset+1);
						setOutputValid(RESULT_LOW);
						setOutputValid(RESULT_HIGH);
						wait = 0;
						high = 0;
						if(activeTrace) trace.appendTrace(H_READ_ARRAY_64, 0, input[OPERAND_A_LOW], 0, 0, 0, output[RESULT_LOW], output[RESULT_HIGH]);
						return true;
					}
				}

			case H_WRITE:
				switch(wait){
				case 0:
					this.handle = input[OPERAND_A_LOW];
					delay = objectCache.writeData(handle, offset, input[OPERAND_C_LOW]);
					if(delay == 0){
						setResultAck(true);
						if(activeTrace) trace.appendTrace(H_WRITE, 0, input[OPERAND_A_LOW], 0, input[OPERAND_C_LOW], 0, 0, 0);
						return true;
					}
					else{
						delay--;
						wait = 1;
						return false;
					}
				case 1:
					setResultAck(true);					
					wait = 0;
					if(activeTrace) trace.appendTrace(H_WRITE, 0, input[OPERAND_A_LOW], 0, input[OPERAND_C_LOW], 0, 0, 0);
					return true;
				}
					
			case H_WRITE_64:
				switch(high){
				case 0:
					switch(wait){
					case 0:
						this.handle = input[OPERAND_A_LOW];
						delay = objectCache.writeData(handle, offset, input[OPERAND_C_LOW]);
						if(delay == 0){
							high = 1;
							return false;
						}
						else{
							delay--;
							wait = 1;
							return false;
						}
					case 1:					
						wait = 0;
						high = 1;
						return false;
					}
				case 1:
					switch(wait){
					case 0:
						delay = objectCache.writeData(handle, offset+1, input[OPERAND_C_HIGH]);
						if(delay == 0){							
							high = 0;
							setResultAck(true);
							if(activeTrace) trace.appendTrace(H_WRITE_64, 0, input[OPERAND_A_LOW], 0, input[OPERAND_C_LOW], input[OPERAND_C_HIGH], 0, 0);
							return true;
						}
						else{
							delay--;
							wait = 1;
							return false;
						}
					case 1:
						wait = 0;
						high = 0;
						setResultAck(true);
						if(activeTrace) trace.appendTrace(H_WRITE_64, 0, input[OPERAND_A_LOW], 0, input[OPERAND_C_LOW], input[OPERAND_C_HIGH], 0, 0);
						return true;
					}
				}
				
			case H_WRITE_ARRAY:
				switch(wait){
				case 0:
					this.handle = input[OPERAND_A_LOW];
					int delayHT = htCache.requestData(handle);
					arraySize = htCache.getSize();
					if(offset >= arraySize) throw new AmidarSimulatorException("ArrayIndexOutOfBounds: "+offset + " ("+arraySize+")");
					delay = objectCache.writeData(handle, offset, input[OPERAND_C_LOW]);
					if(delayHT > delay) delay = delayHT;
					if(delay == 0){						
						setResultAck(true);
						if(activeTrace) trace.appendTrace(H_WRITE_ARRAY, 0, input[OPERAND_A_LOW], 0, input[OPERAND_C_LOW], 0, 0, 0);
						return true;
					}
					else{
						delay--;
						wait = 1;
						return false;
					}
				case 1:
					setResultAck(true);
					wait = 0;
					if(activeTrace) trace.appendTrace(H_WRITE_ARRAY, 0, input[OPERAND_A_LOW], 0, input[OPERAND_C_LOW], 0, 0, 0);
					return true;
				}
				
			case H_WRITE_ARRAY_64:
				switch(high){
				case 0:
					switch(wait){
					case 0:
						this.handle = input[OPERAND_A_LOW];
						int delayHT = htCache.requestData(handle);
						arraySize = htCache.getSize();
						if(offset >= arraySize) throw new AmidarSimulatorException("ArrayIndexOutOfBounds: "+offset + " ("+arraySize+")");
						delay = objectCache.writeData(handle, offset, input[OPERAND_C_LOW]);
						if(delayHT > delay) delay = delayHT;
						if(delay == 0){
							high = 1;
							return false;
						}
						else{
							delay--;
							wait = 1;
							return false;
						}
					case 1:					
						wait = 0;
						high = 1;
						return false;
					}
				case 1:
					switch(wait){
					case 0:					
						if(offset+1 >= arraySize) throw new AmidarSimulatorException("ArrayIndexOutOfBounds: "+offset + " ("+arraySize+")");
						delay = objectCache.writeData(handle, offset+1, input[OPERAND_C_HIGH]);
						if(delay == 0){							
							high = 0;
							setResultAck(true);
							if(activeTrace) trace.appendTrace(H_WRITE_ARRAY_64, 0, input[OPERAND_A_LOW], 0, input[OPERAND_C_LOW], input[OPERAND_C_HIGH], 0, 0);
							return true;
						}
						else{
							delay--;
							wait = 1;
							return false;
						}
					case 1:
						wait = 0;
						high = 0;
						setResultAck(true);
						if(activeTrace) trace.appendTrace(H_WRITE_ARRAY_64, 0, input[OPERAND_A_LOW], 0, input[OPERAND_C_LOW], input[OPERAND_C_HIGH], 0, 0);
						return true;
					}
				}

			case O_READ:
				switch(wait){
				case 0:
					this.offset = input[OPERAND_B_LOW];
					delay = objectCache.requestData(handle, offset);
					if(delay == 0){
						output[RESULT_LOW] = objectCache.getData(handle, offset);
						setOutputValid(RESULT_LOW);
						if(activeTrace) trace.appendTrace(O_READ, 0, 0, input[OPERAND_B_LOW], 0, 0, output[RESULT_LOW], 0);
						return true;
					}
					else{
						delay--;
						wait = 1;
						return false;
					}
				case 1:
					output[RESULT_LOW] = objectCache.getData(handle, offset);
					setOutputValid(RESULT_LOW);
					wait = 0;
					if(activeTrace) trace.appendTrace(O_READ, 0, 0, input[OPERAND_B_LOW], 0, 0, output[RESULT_LOW], 0);
					return true;
				}
					
			case O_READ_64:
				switch(high){
				case 0:
					switch(wait){
					case 0:
						this.offset = input[OPERAND_B_LOW];
						delay = objectCache.requestData(handle, offset);
						if(delay == 0){
							output[RESULT_LOW] = objectCache.getData(handle, offset);
							high = 1;
							return false;
						}
						else{
							delay--;
							wait = 1;
							return false;
						}
					case 1:
						output[RESULT_LOW] = objectCache.getData(handle, offset);
						wait = 0;
						high = 1;
						return false;
					}
				case 1:
					switch(wait){
					case 0:
						delay = objectCache.requestData(handle, offset+1);
						if(delay == 0){
							output[RESULT_HIGH] = objectCache.getData(handle, offset);
							setOutputValid(RESULT_LOW);
							setOutputValid(RESULT_HIGH);
							high = 0;
//							System.out.println("O_READ_64 " +  input[OPERAND_A_LOW] + " " + input[OPERAND_B_LOW] + " " + output[RESULT_LOW] + " " + output[RESULT_HIGH]);
							if(activeTrace) trace.appendTrace(O_READ_64, 0, 0, input[OPERAND_B_LOW], 0, 0, output[RESULT_LOW], output[RESULT_HIGH]);
							return true;
						}
						else{
							delay--;
							wait = 1;
							return false;
						}
					case 1:
						output[RESULT_HIGH] = objectCache.getData(handle, offset);
						setOutputValid(RESULT_LOW);
						setOutputValid(RESULT_HIGH);
						wait = 0;
						high = 0;
//						System.out.println("O_READ_64 " +  input[OPERAND_A_LOW] + " " + input[OPERAND_B_LOW] + " " + output[RESULT_LOW] + " " + output[RESULT_HIGH]);
						if(activeTrace) trace.appendTrace(O_READ_64, 0, 0, input[OPERAND_B_LOW], 0, 0, output[RESULT_LOW], output[RESULT_HIGH]);
						return true;
					}
				}
					
					
			case O_READ_ARRAY:
				switch(wait){
				case 0:
					this.offset = input[OPERAND_B_LOW];
					int delayHT = htCache.requestData(handle);
					arraySize = htCache.getSize();
					delay = objectCache.requestData(handle, offset);
					if(delayHT > delay) delay = delayHT;
					if(delay == 0){
						if(offset >= arraySize) throw new AmidarSimulatorException("ArrayIndexOutOfBounds: "+offset + " ("+arraySize+")");
						output[RESULT_LOW] = objectCache.getData(handle, offset);
						setOutputValid(RESULT_LOW);
						if(activeTrace) trace.appendTrace(O_READ_ARRAY, 0, 0, input[OPERAND_B_LOW], 0, 0, output[RESULT_LOW], 0);
						return true;
					}
					else{
						delay--;
						wait = 1;
						return false;
					}
				case 1:
					if(offset >= arraySize) throw new AmidarSimulatorException("ArrayIndexOutOfBounds: "+offset + " ("+arraySize+")");
					output[RESULT_LOW] = objectCache.getData(handle, offset);
					setOutputValid(RESULT_LOW);
					wait = 0;
					if(activeTrace) trace.appendTrace(O_READ_ARRAY, 0, 0, input[OPERAND_B_LOW], 0, 0, output[RESULT_LOW], 0);
					return true;
				}
					
			case O_READ_ARRAY_64:
				switch(high){
				case 0:
					switch(wait){
					case 0:
						this.offset = input[OPERAND_B_LOW]*2;
						int delayHT = htCache.requestData(handle);
						arraySize = htCache.getSize();
						delay = objectCache.requestData(handle, offset);
						if(delayHT > delay) delay = delayHT;
						if(delay == 0){
							if(offset >= arraySize) throw new AmidarSimulatorException("ArrayIndexOutOfBounds: "+offset + " ("+arraySize+")");
							output[RESULT_LOW] = objectCache.getData(handle, offset);
							high = 1;
							return false;
						}
						else{
							delay--;
							wait = 1;
							return false;
						}
					case 1:
						if(offset >= arraySize) throw new AmidarSimulatorException("ArrayIndexOutOfBounds: "+offset + " ("+arraySize+")");
						output[RESULT_LOW] = objectCache.getData(handle, offset);
						wait = 0;
						high = 1;
						return false;
					}
				case 1:
					switch(wait){
					case 0:
						delay = objectCache.requestData(handle, offset+1);
						if(delay == 0){
							output[RESULT_HIGH] = objectCache.getData(handle, offset);
							setOutputValid(RESULT_LOW);
							setOutputValid(RESULT_HIGH);
							high = 0;
							if(activeTrace) trace.appendTrace(O_READ_ARRAY_64, 0, 0, input[OPERAND_B_LOW], 0, 0, output[RESULT_LOW], output[RESULT_HIGH]);
							return true;
						}
						else{
							delay--;
							wait = 1;
							return false;
						}
					case 1:
						output[RESULT_HIGH] = objectCache.getData(handle, offset);
						setOutputValid(RESULT_LOW);
						setOutputValid(RESULT_HIGH);
						wait = 0;
						high = 0;
						if(activeTrace) trace.appendTrace(O_READ_ARRAY_64, 0, 0, input[OPERAND_B_LOW], 0, 0, output[RESULT_LOW], output[RESULT_HIGH]);
						return true;
					}
				}

			case O_WRITE:				
				switch(wait){
				case 0:
					this.offset = input[OPERAND_B_LOW];
					delay = objectCache.writeData(handle, offset, input[OPERAND_C_LOW]);
					if(delay == 0){
						setResultAck(true);
						if(activeTrace) trace.appendTrace(O_WRITE, 0, 0, input[OPERAND_B_LOW], input[OPERAND_C_LOW], 0, 0, 0);
						return true;
					}
					else{
						delay--;
						wait = 1;
						return false;
					}
				case 1:
					setResultAck(true);					
					wait = 0;
					if(activeTrace) trace.appendTrace(O_WRITE, 0, 0, input[OPERAND_B_LOW], input[OPERAND_C_LOW], 0, 0, 0);
					return true;
				}
				
			case O_WRITE_64:
				switch(high){
				case 0:
					switch(wait){
					case 0:
						this.offset = input[OPERAND_B_LOW];
						delay = objectCache.writeData(handle, offset, input[OPERAND_C_LOW]);
						if(delay == 0){
							high = 1;
							return false;
						}
						else{
							delay--;
							wait = 1;
							return false;
						}
					case 1:					
						wait = 0;
						high = 1;
						return false;
					}
				case 1:
					switch(wait){
					case 0:
						delay = objectCache.writeData(handle, offset+1, input[OPERAND_C_HIGH]);
						if(delay == 0){							
							high = 0;
							setResultAck(true);
//							System.out.println("O_WRITE_64 " +  input[OPERAND_A_LOW] + " " + input[OPERAND_B_LOW] + " " + input[OPERAND_C_LOW] + " " + input[OPERAND_C_HIGH]);
							if(activeTrace) trace.appendTrace(O_WRITE_64, 0, 0, input[OPERAND_B_LOW], input[OPERAND_C_LOW], input[OPERAND_C_HIGH], 0, 0);
							return true;
						}
						else{
							delay--;
							wait = 1;
							return false;
						}
					case 1:
						wait = 0;
						high = 0;
						setResultAck(true);
//						System.out.println("O_WRITE_64 " +  input[OPERAND_A_LOW] + " " + input[OPERAND_B_LOW] + " " + input[OPERAND_C_LOW] + " " + input[OPERAND_C_HIGH]);
						if(activeTrace) trace.appendTrace(O_WRITE_64, 0, 0, input[OPERAND_B_LOW], input[OPERAND_C_LOW], input[OPERAND_C_HIGH], 0, 0);
						return true;
					}
				}
				
			case O_WRITE_ARRAY:				
				switch(wait){
				case 0:
					this.offset = input[OPERAND_B_LOW];
					int delayHT = htCache.requestData(handle);
					arraySize = htCache.getSize();
					if(offset >= arraySize) throw new AmidarSimulatorException("ArrayIndexOutOfBounds: "+offset + " ("+arraySize+")");
					delay = objectCache.writeData(handle, offset, input[OPERAND_C_LOW]);
					if(delayHT > delay) delay = delayHT;
					if(delay == 0){						
						setResultAck(true);
						if(activeTrace) trace.appendTrace(O_WRITE_ARRAY, 0, 0, input[OPERAND_B_LOW], input[OPERAND_C_LOW], 0, 0, 0);
						return true;
					}
					else{
						delay--;
						wait = 1;
						return false;
					}
				case 1:
					setResultAck(true);
					wait = 0;
					if(activeTrace) trace.appendTrace(O_WRITE_ARRAY, 0, 0, input[OPERAND_B_LOW], input[OPERAND_C_LOW], 0, 0, 0);
					return true;
				}
				
			case O_WRITE_ARRAY_64:				
				switch(high){
				case 0:
					switch(wait){
					case 0:
						this.offset = input[OPERAND_B_LOW]*2;
						int delayHT = htCache.requestData(handle);
						arraySize = htCache.getSize();
						if(offset >= arraySize) throw new AmidarSimulatorException("ArrayIndexOutOfBounds: "+offset + " ("+arraySize+")");
						delay = objectCache.writeData(handle, offset, input[OPERAND_C_LOW]);
						if(delayHT > delay) delay = delayHT;
						if(delay == 0){
							high = 1;
							return false;
						}
						else{
							delay--;
							wait = 1;
							return false;
						}
					case 1:					
						wait = 0;
						high = 1;
						return false;
					}
				case 1:
					switch(wait){
					case 0:					
						if(offset+1 >= arraySize) throw new AmidarSimulatorException("ArrayIndexOutOfBounds: "+offset + " ("+arraySize+")");
						delay = objectCache.writeData(handle, offset+1, input[OPERAND_C_HIGH]);
						if(delay == 0){							
							high = 0;
							setResultAck(true);
							if(activeTrace) trace.appendTrace(O_WRITE_ARRAY_64, 0, 0, input[OPERAND_B_LOW], input[OPERAND_C_LOW], input[OPERAND_C_HIGH], 0, 0);
							return true;
						}
						else{
							delay--;
							wait = 1;
							return false;
						}
					case 1:
						wait = 0;
						high = 0;
						setResultAck(true);
						if(activeTrace) trace.appendTrace(O_WRITE_ARRAY_64, 0, 0, input[OPERAND_B_LOW], input[OPERAND_C_LOW], input[OPERAND_C_HIGH], 0, 0);
						return true;
					}
				}
				
			case SET_BASE:
				this.handle = input[OPERAND_A_LOW];
				this.offset = input[OPERAND_B_LOW];
				setResultAck(true);
				if(activeTrace) trace.appendTrace(SET_BASE, 0, input[OPERAND_A_LOW], input[OPERAND_B_LOW], 0, 0, 0, 0);
				return true;
				
				
			case GET_SIZE:
				switch(wait){
				case 0:
					this.handle = input[OPERAND_A_LOW];
					delay = htCache.requestData(handle);
					if(delay == 0){
						int size = htCache.getSize();
						int flags = htCache.getFlags();
						if((flags & 0x8000) == 0) output[RESULT_LOW] = size;
						else output[RESULT_LOW] = size/2;
						//output[RESULT_LOW] = htCache.getSize();
						setOutputValid(RESULT_LOW);
//						System.out.println("GET_SIZE " + input[OPERAND_A_LOW]  + " " +output[RESULT_LOW]);
						if(activeTrace) trace.appendTrace(GET_SIZE, 0, input[OPERAND_A_LOW], 0, 0, 0, output[RESULT_LOW], 0);
						return true;
					}
					else{
						delay--;
						wait = 1;
						return false;
					}
				case 1:
					int size = htCache.getSize();
					int flags = htCache.getFlags();
					if((flags & 0x8000) == 0) output[RESULT_LOW] = size;
					else output[RESULT_LOW] = size/2;
					//output[RESULT_LOW] = htCache.getSize();
					setOutputValid(RESULT_LOW);
					wait = 0;
//					System.out.println("GET_SIZE " + input[OPERAND_A_LOW]  + " " +output[RESULT_LOW]);
					if(activeTrace) trace.appendTrace(GET_SIZE, 0, input[OPERAND_A_LOW], 0, 0, 0, output[RESULT_LOW], 0);
					return true;
				}
				
			case GET_CTI:
				switch(wait){
				case 0:
					this.handle = input[OPERAND_A_LOW];
//					System.out.println("REQUESTING Handle " + handle);
					delay = htCache.requestData(handle);
					output[RESULT_LOW] = htCache.getCTI();
					if(delay == 0){
//						output[RESULT_LOW] = htCache.getCTI();
//						System.out.println("CCCTTTII " + output[RESULT_LOW] + " handle : " + handle);
						setOutputValid(RESULT_LOW);
						if(activeTrace) trace.appendTrace(GET_CTI, 0, input[OPERAND_A_LOW], 0, 0, 0, output[RESULT_LOW], 0);
						return true;
					}
					else{
						delay--;
						wait = 1;
						return false;
					}
				case 1:
//					output[RESULT_LOW] = htCache.getCTI();
//					System.out.println("CCCTTTII " + output[RESULT_LOW] + " handle : " + handle);
					setOutputValid(RESULT_LOW);
					wait = 0;
					if(activeTrace) trace.appendTrace(GET_CTI, 0, input[OPERAND_A_LOW], 0, 0, 0, output[RESULT_LOW], 0);
					return true;
				}
				
			case GET_FLAGS:
				switch(wait){
				case 0:
					this.handle = input[OPERAND_A_LOW];
					delay = htCache.requestData(handle);
					if(delay == 0){
						output[RESULT_LOW] = htCache.getFlags();
						setOutputValid(RESULT_LOW);
						if(activeTrace) trace.appendTrace(GET_FLAGS, 0, input[OPERAND_A_LOW], 0, 0, 0, output[RESULT_LOW], 0);
						return true;
					}
					else{
						delay--;
						wait = 1;
						return false;
					}
				case 1:
					output[RESULT_LOW] = htCache.getFlags();
					setOutputValid(RESULT_LOW);
					wait = 0;
					if(activeTrace) trace.appendTrace(GET_FLAGS, 0, input[OPERAND_A_LOW], 0, 0, 0, output[RESULT_LOW], 0);
					return true;
				}
				
			case GET_MID:
				switch(wait){
				case 0:
					this.handle = input[OPERAND_A_LOW];
					delay = htCache.requestData(handle);
					if(delay == 0){
						output[RESULT_LOW] = htCache.getMID();
						setOutputValid(RESULT_LOW);
						if(activeTrace) trace.appendTrace(GET_MID, 0, input[OPERAND_A_LOW], 0, 0, 0, output[RESULT_LOW], 0);
						return true;
					}
					else{
						delay--;
						wait = 1;
						return false;
					}
				case 1:
					output[RESULT_LOW] = htCache.getMID();
					setOutputValid(RESULT_LOW);
					wait = 0;
					if(activeTrace) trace.appendTrace(GET_MID, 0, input[OPERAND_A_LOW], 0, 0, 0, output[RESULT_LOW], 0);
					return true;
				}
				
			case SET_FLAGS:
				switch(wait){
				case 0:
					this.handle = input[OPERAND_A_LOW];
					this.flags = input[OPERAND_C_LOW];
					delay = htCache.setFlags(handle, flags);
					if(delay == 0){
						setResultAck(true);
						if(activeTrace) trace.appendTrace(SET_FLAGS, 0, input[OPERAND_A_LOW], 0, input[OPERAND_C_LOW], 0, 0, 0);
						return true;
					}
					else{
						delay--;
						wait = 1;
						return false;
					}
				case 1:
					setResultAck(true);					
					wait = 0;
					if(activeTrace) trace.appendTrace(SET_FLAGS, 0, input[OPERAND_A_LOW], 0, input[OPERAND_C_LOW], 0, 0, 0);
					return true;
				}
				
			case SET_MID:
				switch(wait){
				case 0:
					this.handle = input[OPERAND_A_LOW];
					this.mid = input[OPERAND_C_LOW];
					delay = htCache.setMID(handle, flags);
					if(delay == 0){
						setResultAck(true);
						if(activeTrace) trace.appendTrace(SET_MID, 0, input[OPERAND_A_LOW], 0, input[OPERAND_C_LOW], 0, 0, 0);
						return true;
					}
					else{
						delay--;
						wait = 1;
						return false;
					}
				case 1:
					setResultAck(true);					
					wait = 0;
					if(activeTrace) trace.appendTrace(SET_MID, 0, input[OPERAND_A_LOW], 0, input[OPERAND_C_LOW], 0, 0, 0);
					return true;
				}

			case HO_WRITE:				
				switch(wait){
				case 0:
					this.handle = input[OPERAND_A_LOW];
					this.offset = input[OPERAND_B_LOW];
					delay = objectCache.writeData(handle, offset, input[OPERAND_C_LOW]);
					if(delay == 0){
						setResultAck(true);
						if(activeTrace) trace.appendTrace(HO_WRITE, 0, input[OPERAND_A_LOW], input[OPERAND_B_LOW], input[OPERAND_C_LOW], 0, 0, 0);
						return true;
					}
					else{
						delay--;
						wait = 1;
						return false;
					}
				case 1:
					setResultAck(true);					
					wait = 0;
					if(activeTrace) trace.appendTrace(HO_WRITE, 0, input[OPERAND_A_LOW], input[OPERAND_B_LOW], input[OPERAND_C_LOW], 0, 0, 0);
					return true;
				}
				
			case HO_WRITE_64:
				switch(high){
				case 0:
					switch(wait){
					case 0:
						this.handle = input[OPERAND_A_LOW];
						this.offset = input[OPERAND_B_LOW];
						delay = objectCache.writeData(handle, offset, input[OPERAND_C_LOW]);
						if(delay == 0){
							high = 1;
							return false;
						}
						else{
							delay--;
							wait = 1;
							return false;
						}
					case 1:					
						wait = 0;
						high = 1;
						return false;
					}
				case 1:
					switch(wait){
					case 0:
						delay = objectCache.writeData(handle, offset+1, input[OPERAND_C_HIGH]);
						if(delay == 0){							
							high = 0;
							setResultAck(true);
							if(activeTrace) trace.appendTrace(HO_WRITE_64, 0, input[OPERAND_A_LOW], input[OPERAND_B_LOW], input[OPERAND_C_LOW], input[OPERAND_C_HIGH], 0, 0);
//							System.out.println("HO_WRITE_64 " +  input[OPERAND_A_LOW] + " " + input[OPERAND_B_LOW] + " " + input[OPERAND_C_LOW] + " " + input[OPERAND_C_HIGH]);
							return true;
						}
						else{
							delay--;
							wait = 1;
							return false;
						}
					case 1:
						wait = 0;
						high = 0;
						setResultAck(true);
						if(activeTrace) trace.appendTrace(HO_WRITE_64, 0, input[OPERAND_A_LOW], input[OPERAND_B_LOW], input[OPERAND_C_LOW], input[OPERAND_C_HIGH], 0, 0);
//						System.out.println("HO_WRITE_64 " +  input[OPERAND_A_LOW] + " " + input[OPERAND_B_LOW] + " " + input[OPERAND_C_LOW] + " " + input[OPERAND_C_HIGH]);
						return true;
					}
				}
				
			case HO_WRITE_ARRAY:				
				switch(wait){
				case 0:
					this.handle = input[OPERAND_A_LOW];
					this.offset = input[OPERAND_B_LOW];
					int delayHT = htCache.requestData(handle);
					arraySize = htCache.getSize();
					if(offset >= arraySize) throw new AmidarSimulatorException("ArrayIndexOutOfBounds: "+offset + " ("+arraySize+")");
					delay = objectCache.writeData(handle, offset, input[OPERAND_C_LOW]);
					if(delayHT > delay) delay = delayHT;
					if(delay == 0){						
						setResultAck(true);
						if(activeTrace) trace.appendTrace(HO_WRITE_ARRAY, 0, input[OPERAND_A_LOW], input[OPERAND_B_LOW], input[OPERAND_C_LOW], 0, 0, 0);
						return true;
					}
					else{
						delay--;
						wait = 1;
						return false;
					}
				case 1:
					setResultAck(true);
					wait = 0;
					if(activeTrace) trace.appendTrace(HO_WRITE_ARRAY, 0, input[OPERAND_A_LOW], input[OPERAND_B_LOW], input[OPERAND_C_LOW], 0, 0, 0);
					return true;
				}
				
			case HO_WRITE_ARRAY_64:				
				switch(high){
				case 0:
					switch(wait){
					case 0:
						this.handle = input[OPERAND_A_LOW];
						this.offset = input[OPERAND_B_LOW]*2;
						int delayHT = htCache.requestData(handle);
						arraySize = htCache.getSize();
						if(offset >= arraySize) throw new AmidarSimulatorException("ArrayIndexOutOfBounds: "+offset/2 + " ("+arraySize/2+")");
						delay = objectCache.writeData(handle, offset, input[OPERAND_C_LOW]);
						if(delayHT > delay) delay = delayHT;
						if(delay == 0){
							high = 1;
							return false;
						}
						else{
							delay--;
							wait = 1;
							return false;
						}
					case 1:					
						wait = 0;
						high = 1;
						return false;
					}
				case 1:
					switch(wait){
					case 0:					
						if(offset+1 >= arraySize) throw new AmidarSimulatorException("ArrayIndexOutOfBounds: "+(offset+1) + " ("+arraySize+")");
						delay = objectCache.writeData(handle, offset+1, input[OPERAND_C_HIGH]);
						if(delay == 0){							
							high = 0;
							setResultAck(true);
							if(activeTrace) trace.appendTrace(HO_WRITE_ARRAY_64, 0, input[OPERAND_A_LOW], input[OPERAND_B_LOW], input[OPERAND_C_LOW], input[OPERAND_C_HIGH], 0, 0);
							return true;
						}
						else{
							delay--;
							wait = 1;
							return false;
						}
					case 1:
						wait = 0;
						high = 0;
						setResultAck(true);
						if(activeTrace) trace.appendTrace(HO_WRITE_ARRAY_64, 0, input[OPERAND_A_LOW], input[OPERAND_B_LOW], input[OPERAND_C_LOW], input[OPERAND_C_HIGH], 0, 0);
						return true;
					}
				}
				
			case ALLOC_ARRAY:
				if((input[OPERAND_A_LOW] & 0x80000000) == 0){
					this.cti = input[OPERAND_A_LOW] & 0x0000FFFF;
					this.flags = (input[OPERAND_A_LOW] & 0xFFFF0000) >>> 16;
					//System.out.println("============> ALLOC_ARRAY: Flags="+flags);
					this.mid = 0;
					this.size = input[OPERAND_B_LOW];
					int add = 0;
					
//					boolean use2 = false;
//					
//					
//					int mod = size%32;
//					
//					if(mod == 0){
//						use2 = true;
//					}
//					
//					double qu = (32.0-mod) /size;
//					
//					if(qu < 1.0/32.0){
//						use2 = true;
//					}
//					
//					
////					if(size >= 32)
//					if(use2){
//						add = 0x100000;
//						
////						System.err.println("SSSSSSSSSSSSSSIUE " + size);
//					}
					
					this.registerHandle(handles+add, mid, flags, cti, size, free);
					this.free = free + size;
					output[RESULT_LOW] = handles-1+add;
					setOutputValid(RESULT_LOW);
					if(activeTrace) trace.appendTrace(ALLOC_ARRAY, 0, input[OPERAND_A_LOW], input[OPERAND_B_LOW], 0, 0, output[RESULT_LOW], 0);
					return true;
				}
			case ALLOC_ARRAY_64:
				this.cti = input[OPERAND_A_LOW] & 0x0000FFFF;
				this.flags = (input[OPERAND_A_LOW] & 0xFFFF0000) >>> 16;
				//System.out.println("============> ALLOC_ARRAY_64: Flags="+flags);
				this.mid = 0;
				this.size = input[OPERAND_B_LOW];
				
				
				
				
				this.registerHandle(handles, mid, flags, cti, 2*size, free);
				this.free = free + 2*size;
				output[RESULT_LOW] = handles-1;
				setOutputValid(RESULT_LOW);
				if(activeTrace) trace.appendTrace(ALLOC_ARRAY_64, 0, input[OPERAND_A_LOW], input[OPERAND_B_LOW], 0, 0, output[RESULT_LOW], 0);
				return true;
				
			case ALLOC_OBJ:
				this.cti = input[OPERAND_A_LOW] & 0x0000FFFF;
				this.flags = input[OPERAND_A_LOW] & 0xFFFF0000;
				//System.out.println(" alloc obb : "  + handles +  " " + cti);
				this.mid = 0;
				this.size = input[OPERAND_B_LOW];
				this.registerHandle(handles, mid, flags, cti, size, free);
				this.free = free + size;
				output[RESULT_LOW] = handles-1;
				setOutputValid(RESULT_LOW);
				if(activeTrace) trace.appendTrace(ALLOC_OBJ, 0, input[OPERAND_A_LOW], input[OPERAND_B_LOW], 0, 0, output[RESULT_LOW], 0);
				return true;
				
			case SETUP_MULTI_ARRAY:
				this.cti = input[OPERAND_A_LOW];
				this.dimcount = input[OPERAND_B_LOW];
				this.dimsizes = new int[dimcount];
//				System.out.println("DIMS " +dimcount);
				this.counterMultArray = dimcount-1;
				setResultAck(true);
				if(activeTrace) trace.appendTrace(SETUP_MULTI_ARRAY, 0, input[OPERAND_A_LOW], input[OPERAND_B_LOW], 0, 0, 0, 0);
				return true;
				
			case SET_MULTI_ARRAY_DIM_SIZE:
				if(this.counterMultArray >= 0){
//					System.out.println("DIMSIZE: "  + input[OPERAND_B_LOW]);
					this.dimsizes[this.counterMultArray] = input[OPERAND_B_LOW];
					this.counterMultArray--;
				}
				setResultAck(true);
				if(activeTrace) trace.appendTrace(SET_MULTI_ARRAY_DIM_SIZE, 0, 0, input[OPERAND_B_LOW], 0, 0, 0, 0);
				return true;
				
			case ALLOC_MULTI_ARRAY:				
				switch(wait){
				case 0:
					this.flags = 0x0001;
					int tickCount = 1;
					for(int i = 0; i < dimcount-1; i++) tickCount = tickCount * dimsizes[i];
					delay = ((tickCount*2) + objectCache.getMemoryAccessTime()) - (dimcount+1);
					if(delay == 0){
						output[RESULT_LOW] = this.allocArray(0, cti, flags);
						setOutputValid(RESULT_LOW);
						if(activeTrace) trace.appendTrace(ALLOC_MULTI_ARRAY, 0, 0, 0, 0, 0, output[RESULT_LOW], 0);
						return true;
					}
					else{
						output[RESULT_LOW] = this.allocArray(0, cti, flags);
						delay--;
						wait = 1;
						return false;
					}
				case 1:
//					output[RESULT_LOW] = this.allocArray(0, cti, flags);
					
					setOutputValid(RESULT_LOW);
					if(activeTrace) trace.appendTrace(ALLOC_MULTI_ARRAY, 0, 0, 0, 0, 0, output[RESULT_LOW], 0);
					wait = 0;
					return true;
				}				
				/*
				System.out.println("============> ALLOC_MULTI_ARRAY");
				this.flags = 0x0001;
				this.mid = 0;
				this.size = this.dimsizes[0];
				this.registerHandle(handles, mid, flags, cti, size, free);
				dimStart = free;
				this.free = free + size;
				returnHandle = handles-1;
				fields = dimsizes[0];				
			
				for(int i = 1; i<this.dimcount; i++){	//Dimension
					cti--;
					nextDimStart = free;				
					for(int j = 0; j < fields; j++){	//Arrayfield
						this.registerHandle(handles, mid, flags, cti, dimsizes[i], free);
						mem.write(dimStart+j, handles-1);
						free=free+dimsizes[i];
					}
					fields = fields*dimsizes[i];
					dimStart = nextDimStart;
				}												
				output[RESULT_LOW] = this.allocArray(0, cti, flags);
				setOutputValid(RESULT_LOW);
				if(activeTrace) trace.appendTrace(ALLOC_MULTI_ARRAY, 0, 0, 0, 0, 0, output[RESULT_LOW], 0);
				return true;*/			
				
			case ALLOC_MULTI_ARRAY_64:
				switch(wait){
				case 0:
					this.flags = 0x8001;
					int tickCount = 1;
					for(int i = 0; i < dimcount-1; i++) tickCount = tickCount * dimsizes[i];
					delay = ((tickCount*2) + objectCache.getMemoryAccessTime()) - (dimcount+1);
					if(delay == 0){
						output[RESULT_LOW] = this.allocArray64(0, cti, flags);
						setOutputValid(RESULT_LOW);
						if(activeTrace) trace.appendTrace(ALLOC_MULTI_ARRAY_64, 0, 0, 0, 0, 0, output[RESULT_LOW], 0);
						return true;
					}
					else{
						delay--;
						wait = 1;
						return false;
					}
				case 1:
					output[RESULT_LOW] = this.allocArray64(0, cti, flags);
					setOutputValid(RESULT_LOW);
					if(activeTrace) trace.appendTrace(ALLOC_MULTI_ARRAY_64, 0, 0, 0, 0, 0, output[RESULT_LOW], 0);
					wait = 0;
					return true;
				}
				/*
				System.out.println("============> ALLOC_MULTI_ARRAY_64");
				this.flags = 0x8001;
				this.mid = 0;
				this.size = this.dimsizes[0];
				this.registerHandle(handles, mid, flags, cti, size, free);
				dimStart = free;			
				this.free = free + size;
				returnHandle = handles-1;
				fields = dimsizes[0];
				
				for(int i = 1; i<this.dimcount; i++){	//Dimension
					cti--;
					nextDimStart = free;				
					for(int j = 0; j < fields; j++){	//Arrayfield
						if(i==dimcount-1){
							this.registerHandle(handles, mid, flags, cti, 2*dimsizes[i], free);
							mem.write(dimStart+j, handles-1);
							free=free+2*dimsizes[i];
						}
						else{
							this.registerHandle(handles, mid, flags, cti, dimsizes[i], free);
							mem.write(dimStart+j, handles-1);
							free=free+dimsizes[i];
						}
					}
					fields = fields*dimsizes[i];
					dimStart = nextDimStart;
				}
				output[RESULT_LOW] = this.allocArray64(0, cti, flags);;
				setOutputValid(RESULT_LOW);
				if(activeTrace) trace.appendTrace(ALLOC_MULTI_ARRAY_64, 0, 0, 0, 0, 0, output[RESULT_LOW], 0);
				return true;*/				
				
			case PHY_READ:
				this.address = input[OPERAND_A_LOW];
				output[RESULT_LOW] = mem.readPhysical(address);
				setOutputValid(RESULT_LOW);
				if(activeTrace) trace.appendTrace(PHY_READ, 0, input[OPERAND_A_LOW], 0, 0, 0, output[RESULT_LOW], 0);
				return true;
				
			case PHY_WRITE:
				this.address = input[OPERAND_A_LOW];
				mem.writePhysical(address, input[OPERAND_C_LOW]);
				setResultAck(true);
				if(activeTrace) trace.appendTrace(PHY_WRITE, 0, input[OPERAND_A_LOW], 0, input[OPERAND_C_LOW], 0, 0, 0);
				return true;
				
			default: return false;
			}
		}
		else{
			delay--;
			return false;
		}
	}

	@Override
	public boolean validInputs(HeapOpcodes op) {	// is realized combinatorial in HW
		switch(op){
		case ALLOC_MULTI_ARRAY:
		case ALLOC_MULTI_ARRAY_64:
		case READ:
		case READ_64:
		case READ_ARRAY:
		case READ_ARRAY_64:
		case PIO_READ:
		case PIO_READ_64:
		case PIO_READ_ARRAY:
		case PIO_READ_ARRAY_64:
			return true;
		case ALLOC_OBJ:
		case ALLOC_ARRAY:
		case ALLOC_ARRAY_64:
		case SETUP_MULTI_ARRAY:
		case HO_READ:
		case HO_READ_64:
		case HO_READ_ARRAY:
		case HO_READ_ARRAY_64:
		case SET_BASE:
			return inputValid[OPERAND_A_LOW] && inputValid[OPERAND_B_LOW];
		case SET_MULTI_ARRAY_DIM_SIZE:
		case O_READ:
		case O_READ_64:
		case O_READ_ARRAY:
		case O_READ_ARRAY_64:
			return inputValid[OPERAND_B_LOW];
		case H_READ:
		case H_READ_64:
		case H_READ_ARRAY:
		case H_READ_ARRAY_64:
		case GET_CTI:
		case GET_FLAGS:
		case GET_MID:
		case GET_SIZE:
		case PHY_READ:
			return inputValid[OPERAND_A_LOW];
		case WRITE:
		case WRITE_ARRAY:
		case PIO_WRITE:
		case PIO_WRITE_ARRAY:
			return inputValid[OPERAND_C_LOW];
		case WRITE_64:
		case WRITE_ARRAY_64:
		case PIO_WRITE_64:
		case PIO_WRITE_ARRAY_64:
			return inputValid[OPERAND_C_HIGH] && inputValid[OPERAND_C_LOW];
		case H_WRITE:
		case H_WRITE_ARRAY:
		case SET_FLAGS:
		case SET_MID:
		case PHY_WRITE:
			return inputValid[OPERAND_A_LOW] && inputValid[OPERAND_C_LOW];
		case H_WRITE_64:
		case H_WRITE_ARRAY_64:
			return inputValid[OPERAND_A_LOW] && inputValid[OPERAND_C_HIGH] && inputValid[OPERAND_C_LOW];
		case O_WRITE:
		case O_WRITE_ARRAY:
			return inputValid[OPERAND_B_LOW] && inputValid[OPERAND_C_LOW];
		case O_WRITE_64:
		case O_WRITE_ARRAY_64:
			return inputValid[OPERAND_B_LOW] && inputValid[OPERAND_C_HIGH] && inputValid[OPERAND_C_LOW];
		case HO_WRITE:
		case HO_WRITE_ARRAY:
			return inputValid[OPERAND_A_LOW] && inputValid[OPERAND_B_LOW] && inputValid[OPERAND_C_LOW];
		case HO_WRITE_64:
		case HO_WRITE_ARRAY_64:
			return inputValid[OPERAND_A_LOW] && inputValid[OPERAND_B_LOW] && inputValid[OPERAND_C_LOW] && inputValid[OPERAND_C_HIGH];
		case CLONE:
		default:
			return false;
		}
	}

	public void fillHeap(int[] objHeapInt) {
		mem.initialMem(objHeapInt);
	}

	public int registerHandle(int index, int mid, int flags, int classTableIndex,
			long refObjectSize, long absoluteReference) {
//		System.out.println("RegisterHandle with Addr="+absoluteReference+" and Size="+refObjectSize+" in Handle "+index);
//		mem.registerHandle(index, mid, flags, classTableIndex, refObjectSize, absoluteReference);
		
		this.handles++;
		return htCache.writeData(index, absoluteReference, refObjectSize, classTableIndex, flags, mid);
	}
	
	public int getFree(){
		return this.free;
	}

	public int getHandles(){
		return this.handles;
	}
	

	
	private int allocArray(int dim, int cti, int flags){
//		System.out.println("alloc multi dim: RegisterHandle with Addr="+free+" and Size="+dimsizes[dim]+" in Handle "+handles);
//		System.out.println("DELAY: " + delay);
		int delInc = this.registerHandle(handles, 0, flags, cti, dimsizes[dim], free);
		delay += delInc;
//		System.out.println(" DELINC: " + delInc + " " + delInc);
		int returnValue = handles-1;		
		int dimStart = free;
		free = free + dimsizes[dim];
		if(dim < dimcount-1){		
			for(int i = 0; i < dimsizes[dim]; i++){
				
//				System.out.println("DELAY: " + delay);
				delInc = objectCache.writeData(returnValue, i, allocArray(dim+1, cti-1, flags));
				
				delay += delInc;
//				System.out.println(" DELINC: " + delInc + " " + delInc);
//				mem.write(dimStart+i, allocArray(dim+1, cti-1, flags));
			}
		}	
		return returnValue;		
	}
	
	private int allocArray64(int dim, int cti, int flags){
		if(dim == dimcount-1) this.registerHandle(handles, 0, flags, cti, 2*dimsizes[dim], free);	
		else this.registerHandle(handles, 0, flags, cti, dimsizes[dim], free);		
		int returnValue = handles-1;		
		int dimStart = free;
		free = free + dimsizes[dim];
		if(dim < dimcount-1){		
			for(int i = 0; i < dimsizes[dim]; i++){				
				mem.writePhysical(dimStart+i, allocArray(dim+1, cti-1, flags));
			}
		}	
		return returnValue;		
	}
	
	public ObjectHeapTrace getOHTrace(){
		return trace;
	}
	
	public boolean activeHeapTrace(){
		return activeTrace;
	}
	
	public void setMOESICaches(Cache[] moesiCaches){
		objectCache.setMOESICaches(moesiCaches);
	}
	
//	public int[] getTotalCacheStatistics(){
//		return objectCache.getTotalStatistics();
//	}
	
	public Cache[] getCaches(){
//		Cache[] ret = objectCache.getCaches();
		Cache[] ret = coherenceController.getCaches();
		if(ret == null){
			ret = new Cache[1];
			ret[0] = objectCache;
		}
		return ret;
	}
	
	
	
	public int getCacheCount(){
		return objectCache.getCacheID()+1;
	}
	
	public void cacheTrace(){
		objectCache.traceAll();
	}
	
	public boolean tick(){
		return super.tick();
	}
	
	public void invalidateFlushAllCaches(){
		coherenceController.invalidateFlushAllCaches();
		coherenceController.resetBusyTime();
	}
	
}
