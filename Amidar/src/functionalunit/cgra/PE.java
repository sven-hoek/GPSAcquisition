package functionalunit.cgra;

import java.util.EnumMap;
import java.util.EnumSet;

import cgramodel.ContextMaskPE;
import cgramodel.PEModel;
import exceptions.AmidarSimulatorException;
import functionalunit.CGRA;
import operator.Implementation;
import operator.Operator;
import target.Amidar;
import target.Amidar.OP;

/**
 * This class emulates a generic PE. Main method for cycle based emulation is operate().
 * @author Dennis Wolf
 *
 */
public class PE {
	
	int cBoxSelect = 0;


	private EnumMap<Amidar.OP, Integer> executionCounter; 

	/**
	 * Model of the PE from the CGRA Model
	 */
	PEModel model;
	
	private EXCEPTION exception = null; 

	
	public PEModel getModel() {
		return model;
	}

	
	public void setModel(PEModel model) {
		this.model = model;
	}

	
	public ContextMaskPE contextmask;


	public ContextMaskPE getContextmask() {
		return contextmask;
	}

	
	public void setContextmask(ContextMaskPE contextmask) {
		this.contextmask = contextmask;
	}

	
	/**
	 * Constants for the multiplexor in front of the data in of the register file
	 */
	public static final int IN = 1, INDMA = 2, ALU = 0;

	
	/**
	 * The input ports that are connected to other PEs. 
	 */
	public int [] inputs; 	

	
	/**
	 * Input from the C-Box
	 */
	private boolean inputPredication;

	
	/**
	 * Array that maps PEs to the input connections of this pe. 
	 * input[i] : PE i,which returns the slot, if available, otherwise -1.
	 */
	public int[] inputMapping;


	/**
	 * An independent input, that is connect to the cache and delivers the data, when
	 * a load is processed.
	 */
	private int InputCache;

	
	/**
	 * marked as IN in sketches from ies
	 */
	public int InputAmidar;

	
	/**
	 * The output port that is connected to the Cache and holds the data
	 */	
	private int OutputMux;

	
	/**
	 * The output port that is connected to the Cache and holds the base-addr
	 */	
	private int baseAddrCache;

	private int offsetCache;
	private int outputCache;

	
	public int getOutputCache(){
		return outputCache;
	}

	
	public int getBaseAddrCache(){
		return baseAddrCache;
	}

	
	public int getOffsetCache() {
		return offsetCache;
	}


	/**
	 * Output directly from the Registerfile to decrease wiring latencies. also holds the addr offset for caches accesses
	 */
	public int Regout;
	public boolean regoutDefined;

	
	/**
	 * Every PE holds a unique ID
	 */

	public int PeID;

	
	/**
	 * Every Pe is connected and controlled by one contex
	 */
	public  long context;


	/**
	 * Every PE holds its own registerfile. All tweakable configs can be found in CGRA configfile.
	 */
	public  Registerfile regfile;

	
	/**
	 * Input  of the ALU. It receives its value from a MUX, which is connected to the inputs of the PE.
	 */
	public int inputALUA;
	public boolean inputALUAdefined;

	public int inputALUB;
	public boolean inputALUBdefined;
	
	/**
	 * For multicycle operations registers are used in the ALU
	 */
	private int regALUAlow, regALUAhigh, regALUBlow, regALUBhigh;
	private long carryLongOps;

	
	/**
	 * output of the ALU connected to the multiplexor if in front of the registefile.
	 */
	public int outputALU;

	
	/**
	 * Output to the cache. 
	 */
	private boolean OutputcacheWrite, OutputCacheValid, wideDMAAccess, isPrefetch;

	
	public boolean getOutputCacheValid() {
		if(contextmask.dmaConditional(context)){
			return OutputCacheValid && inputPredication;
		} else{

			return OutputCacheValid;
		}
	}

	public boolean getOutputCacheWrite(){
		return OutputcacheWrite;
	}
	
	public boolean isWideDMAAcsess(){
		return wideDMAAccess;
	}
	
	public boolean isPrefetch(){
		return isPrefetch;
	}


	private boolean InputEnable,internalEnable;
	
	
	/**
	 * Status out of the ALU. E.g. is used for Ops like EQ
	 */
	private boolean statusALU;


	public int slotcounter=0;
	private int processcounter = 0;


	protected CGRA cgra;

	
	public PE(){

	}

	
	public void configure(PEModel model, CGRA cgra){

		this.cgra = cgra;
		this.model = model;
		contextmask = model.getContextMaskPE();
		regfile = new Registerfile();
		int regsize = regfile.configure(model.getRegfilesize(),model.getID());
		codes = new Amidar.OP[model.getAvailableNonNativeOperators().size()];
		for(Operator op:model.getAvailableNonNativeOperators().keySet()){
			codes[model.getAvailableNonNativeOperators().get(op).getOpcode()] = (Amidar.OP)op;
		}

		executionCounter = new EnumMap<>(getOps());

		for(Operator op : model.getAvailableNonNativeOperators().keySet()){
			executionCounter.put((Amidar.OP)op, 0);
		}
	}


	OP[] codes;

	protected Registerfile getRegfile() {
		return regfile;
	}

	protected void setRegfile(Registerfile regfile) {
		this.regfile = regfile;
	}

	public Class<Amidar.OP> getOps(){
		return target.Amidar.OP.class;
	}

	public int getNrOfInputports() {
		return model.getInputs().size();
	}


	public int getRegout() {
		return Regout;
	}
	
	public boolean getRegoutdefined(){
		return regoutDefined;
	}

	/**
	 * This method simulates the behaviour of the three muxes in the PE. 
	 * Note: Ther is a convention: The output of the register file to the ALU needs to be the input of both muxes 
	 * with the highest digit. 
	 */
	private boolean muxInputs(){
		// Convention !!!! output of the register file needs to be the input of both muxes with the highest digit 
		if(contextmask.muxA(context) == model.getInputs().size()){
			inputALUA = regfile.getOutputMux();
			inputALUAdefined = regfile.getOutputDefinedMux();
		}
		else{
			inputALUA = cgra.getPEs()[model.getInputs().get(contextmask.muxA(context)).getID()].getRegout();
			inputALUAdefined = cgra.getPEs()[model.getInputs().get(contextmask.muxA(context)).getID()].getRegoutdefined();
		}

		if(contextmask.muxB(context) == model.getInputs().size()){
			inputALUB = regfile.getOutputMux();
			inputALUBdefined = regfile.getOutputDefinedMux();
		}
		else{
//			System.out.println("id : " + model.getID() + " problem : " + contextmask.muxB(context));
			inputALUB = cgra.getPEs()[model.getInputs().get(contextmask.muxB(context)).getID()].getRegout();
			inputALUBdefined = cgra.getPEs()[model.getInputs().get(contextmask.muxB(context)).getID()].getRegoutdefined();
		}
		outputCache = inputALUA;
		offsetCache = inputALUB;
		return true;
	}


	protected int getMuxOutput(){
		return OutputMux;
	}

	
	/**
	 * Writes the current context to the PE 
	 */
	public void fetchContext(long newestcontext){
		context =  newestcontext;	
	}


	public void createPorts() {
		inputs =  new int[model.getInputs().size()];
	}


	
	Amidar.OP  lastOP = null;
//	public Amidar.OP tmp;


	public boolean operate(){
		exception = null;
		muxInputs();
		
		
		cBoxSelect = contextmask.cBoxSel(context);
		
		if(true){///XXXFFF
			// check for validity
			Amidar.OP currentop = codes[contextmask.operation(context)];
			
			if(currentop != lastOP){
				processcounter = 0;
			}
			lastOP = currentop;
			
			wideDMAAccess = false;
			
//			tmp = currentop;
			if(!model.getAvailableOperators().containsKey(currentop))
				throw new AmidarSimulatorException("Not allowed operation on PE " + model.getID() + " : " + currentop);

			if(processcounter == 0){
				executionCounter.put(currentop, executionCounter.get(currentop)+1);
			}
			processcounter ++;
			//			if(model.getControlFlow())
			//				System.out.println(" OP: " + currentop);
			
//			System.out.println(model.getID()+" "+ currentop + " " + processcounter);
			
			switch (currentop) {

			case IADD :
				statusALU = false;
				OutputCacheValid = false;
				OutputcacheWrite = false;
				outputALU = Integer.MAX_VALUE;	
				if(model.getAvailableOperators().get(currentop).getLatency() == processcounter){
					outputALU = inputALUA + inputALUB;
					processcounter = 0;
				}
//								System.out.println("IADD " + processcounter);
//								System.out.println("\t"+inputALUA);
//								System.out.println("\t"+inputALUB);
//								System.out.println("\t"+outputALU);
				break;

			case ISUB :
				statusALU = false;
				OutputCacheValid = false;
				OutputcacheWrite = false;
				outputALU = Integer.MAX_VALUE;	
				if(model.getAvailableOperators().get(currentop).getLatency() == processcounter){
					outputALU = inputALUA - inputALUB;
					processcounter = 0;
				}
				//				System.out.println("ISUB");
				//				System.out.println("\t"+inputALUA);
				//				System.out.println("\t"+inputALUB);
				//				System.out.println("\t"+outputALU);
				break;

			case IMUL :
				if(processcounter == 1){
					regALUAlow = inputALUA;
					regALUBlow = inputALUB;
				}				
				statusALU =false;
				outputALU = Integer.MAX_VALUE; // ***¹ should avoid to compute valid results that are not ready yet in reality, only for multicycle
				OutputCacheValid = false;
				OutputcacheWrite = false;
				if(model.getAvailableOperators().get(currentop).getLatency() == processcounter){
					outputALU = regALUAlow * regALUBlow;
					processcounter = 0;
//										System.out.println("IMUL");
//										System.out.println("\t"+regALUAlow);
//										System.out.println("\t"+regALUBlow);
//										System.out.println("\t"+outputALU);
				}
				break;

			case IDIV :
				if(processcounter == 1){
					regALUAhigh = inputALUA;
					regALUBlow = inputALUB;
				}	
				statusALU = false;
				outputALU = Integer.MAX_VALUE; // ***¹ should avoid to compute valid results that are not ready yet in reality, only for multicycle
				OutputCacheValid = false;
				OutputcacheWrite = false;
				if(model.getAvailableOperators().get(currentop).getLatency() == processcounter){
					if(regALUBlow != 0){
						outputALU = regALUAhigh / regALUBlow; // TODO Div by zero exception
					} else {
						outputALU = 3141592;
					}
					processcounter = 0;
				}
				break;
			case IREM :
				if(processcounter == 1){
					regALUAhigh = inputALUA;
					regALUBlow = inputALUB;
				}	
				statusALU =false;
				outputALU = Integer.MAX_VALUE; // ***¹ should avoid to compute valid results that are not ready yet in reality, only for multicycle
				OutputCacheValid = false;
				OutputcacheWrite = false;
				if(model.getAvailableOperators().get(currentop).getLatency() == processcounter){
					if(regALUBlow != 0){
						outputALU = regALUAhigh % regALUBlow; // TODO Div by zero exception
					} else {
						outputALU = 0;
					}
					processcounter = 0;
				}
				
//								System.out.println("IREM");
//								System.out.println("\t"+inputALUA);
//								System.out.println("\t"+inputALUB);
//								System.out.println("\t"+outputALU);
				break;

			case IOR :
				statusALU = false;
				OutputCacheValid = false;
				OutputcacheWrite = false;
				if(model.getAvailableOperators().get(currentop).getLatency() == processcounter){
					outputALU = inputALUA | inputALUB;
					processcounter = 0;
				}
				//				System.out.println("IOR");
				//				System.out.println("\t"+inputALUA);
				//				System.out.println("\t"+inputALUB);
				//				System.out.println("\t"+outputALU);
				break;

			case IAND :
				statusALU = false;
				OutputCacheValid = false;
				OutputcacheWrite = false;
				if(model.getAvailableOperators().get(currentop).getLatency() == processcounter){
					outputALU = inputALUA & inputALUB;
					processcounter = 0;
//									System.out.println("IAND");
//									System.out.println("\t"+inputALUA);
//									System.out.println("\t"+inputALUB);
//									System.out.println("\t"+outputALU);
				}
				break;

			case IXOR :
				statusALU = false;
				OutputCacheValid = false;
				OutputcacheWrite = false;
				outputALU = Integer.MAX_VALUE;
				if(model.getAvailableOperators().get(currentop).getLatency() == processcounter){
					processcounter = 0;
					outputALU = inputALUA ^ inputALUB;
				}
				break;

			case ISHL :
				statusALU = false;
				OutputCacheValid = false;
				OutputcacheWrite = false;
				outputALU = Integer.MAX_VALUE;
				if(model.getAvailableOperators().get(currentop).getLatency() == processcounter){
					processcounter = 0;
					outputALU = inputALUA << inputALUB;
				}
//								System.out.println("ISHL");
//								System.out.println("\t"+inputALUA);
//								System.out.println("\t"+inputALUB);
//								System.out.println("\t"+outputALU);
				break;

			case ISHR :
				statusALU = false;
				OutputCacheValid = false;
				OutputcacheWrite = false;
				outputALU = Integer.MAX_VALUE;
				if(model.getAvailableOperators().get(currentop).getLatency() == processcounter){
					processcounter = 0;
					outputALU = inputALUA >> inputALUB;
				}
//								System.out.println("SHR");
//								System.out.println("\t"+inputALUA);
//								System.out.println("\t"+inputALUB);
//								System.out.println("\t"+outputALU);
				break;

			case IUSHR :
				statusALU = false;
				OutputCacheValid = false;
				OutputcacheWrite = false;
				outputALU = Integer.MAX_VALUE;
				if(model.getAvailableOperators().get(currentop).getLatency() == processcounter){
					processcounter = 0;
					outputALU = inputALUA >>> inputALUB;
				}
//				System.out.println("iUSHR");
//				System.out.println("\t"+inputALUA);
//				System.out.println("\t"+inputALUB);
//				System.out.println("\t"+outputALU);
				break;

			case LADD :
				statusALU =false;
				OutputCacheValid = false;
				OutputcacheWrite = false;

				if(processcounter == 1) {
					long intermediate = (((long)inputALUA)&0xFFFFFFFFL) + (((long)inputALUB)&0xFFFFFFFFL);
					carryLongOps = (intermediate >> 32); // saves carry for next cycle
					//					System.out.println("\t\t"+carryLongOps);
					outputALU =(int)(intermediate&0xFFFFFFFFL);
					processcounter++;
				}
				else {
					outputALU = (int) ( carryLongOps +(long)inputALUA + (long)inputALUB);
					processcounter = 0;
				}
				//				System.out.println("\t"+inputALUA + " processcnt "+processcounter);
				//				System.out.println("\t"+inputALUB);
				//				System.out.println("\t"+outputALU);

				break;
			case LSUB :
				statusALU =false;
				OutputCacheValid = false;
				OutputcacheWrite = false;

				if(processcounter == 1) {
					long intermediate = (((long)inputALUA)&0xFFFFFFFFL) + (((long)~inputALUB)&0xFFFFFFFFL) +1;
					carryLongOps = (intermediate >> 32); // saves carry for next cycle
					System.out.println("\t\t"+carryLongOps);
					outputALU =(int)(intermediate&0xFFFFFFFFL);
					processcounter++;
				}
				else {
					outputALU = (int) ( carryLongOps +(long)inputALUA + (long)~inputALUB);
					processcounter = 0;
				}
				//				System.out.println("\t"+inputALUA + " processcnt "+processcounter);
				//				System.out.println("\t"+inputALUB);
				//				System.out.println("\t"+outputALU);

				break;
			case LCMP :
				statusALU =false;
				OutputCacheValid = false;
				OutputcacheWrite = false;

				if(processcounter == 1) {
					long intermediate = (((long)inputALUA)&0xFFFFFFFFL) + (((long)~inputALUB)&0xFFFFFFFFL) +1;
					carryLongOps = (intermediate >> 32); // saves carry for next cycle
					System.out.println("\t\t"+carryLongOps);
					outputALU =(int)(intermediate&0xFFFFFFFFL);
					processcounter++;
				}
				else {
					outputALU = (int) ( carryLongOps +(long)inputALUA + (long)~inputALUB);
					if(outputALU < 0){
						outputALU = -1;
					} else if(outputALU > 0){
						outputALU = 1;
					}

					processcounter = 0;
				}
				//				System.out.println("\t"+inputALUA + " processcnt "+processcounter);
				//				System.out.println("\t"+inputALUB);
				//				System.out.println("\t"+outputALU);

				break;


			case DMA_LOAD :
				statusALU =false;
				outputALU = Integer.MAX_VALUE; 
				OutputCacheValid = true;
				OutputcacheWrite = false;
				isPrefetch = false;
				if(processcounter > 1){
					OutputCacheValid = false;
				}
				if(model.getAvailableOperators().get(currentop).getLatency() == processcounter){
//					OutputCacheValid = false;
					processcounter = 0;
				}
//								System.out.println("DMA_LOAD");
//								System.out.println("\t"+OutputCacheValid);
				break;

			case DMA_STORE :
				statusALU =false;
				outputALU = Integer.MAX_VALUE; 
				OutputCacheValid = true;
				OutputcacheWrite = true;
				isPrefetch = false;
				if(model.getAvailableOperators().get(currentop).getLatency() == processcounter){
					processcounter = 0;
				}
				break;
			case CACHE_FETCH:
				statusALU = InputCache != Integer.MIN_VALUE;
				outputALU = Integer.MAX_VALUE; 
				OutputCacheValid = true;
				OutputcacheWrite = false;
				isPrefetch = true;
				if(processcounter > 1){
					OutputCacheValid = false;
				}
				if(model.getAvailableOperators().get(currentop).getLatency() == processcounter){
					processcounter = 0;
				}
				break;
			case DMA_LOAD64 :
				statusALU =false;
				outputALU = Integer.MAX_VALUE; 
				OutputCacheValid = true;
				OutputcacheWrite = false;
				
//				if(model.getAvailableOperators().get(currentop).getLatency() >= processcounter-1){
					wideDMAAccess = true;
//				}
//				if(model.getAvailableOperators().get(currentop).getLatency()-1 <= processcounter){
//					OutputCacheValid = false;
//				}
				if(model.getAvailableOperators().get(currentop).getLatency() == processcounter){
					processcounter = 0;
				}
//								System.out.println("DMA_LOAD64");
//								System.out.println("\t"+OutputCacheValid);
				break;

			case DMA_STORE64 :
				statusALU =false;
				outputALU = Integer.MAX_VALUE; 
				OutputCacheValid = true;
				OutputcacheWrite = true;
				wideDMAAccess = true;
				if(model.getAvailableOperators().get(currentop).getLatency() == processcounter){

					processcounter = 0;
				}
				break;

			case IFEQ :
				statusALU =false;
				outputALU = Integer.MAX_VALUE;
				OutputCacheValid = false;
				OutputcacheWrite = false;
				if(model.getAvailableOperators().get(currentop).getLatency() == processcounter){
					if(inputALUA == inputALUB)
						statusALU = true;
					processcounter = 0;
				}
//								System.out.println("IFEQ");
//								System.out.println("\t"+inputALUA);
//								System.out.println("\t"+inputALUB);
//								System.out.println("\t"+statusALU);
				break;

			case IFNE :
				statusALU =false;
				outputALU = Integer.MAX_VALUE; 
				OutputCacheValid = false;
				OutputcacheWrite = false;
				if(model.getAvailableOperators().get(currentop).getLatency() == processcounter){
					if(inputALUA != inputALUB)
						statusALU = true;
					processcounter = 0;
				}
				//				System.out.println("IFNE");
				//				System.out.println("\t"+inputALUA);
				//				System.out.println("\t"+inputALUB);
				//				System.out.println("\t"+statusALU);
				break;
			case CI_CMP :
				statusALU =false;
				outputALU = Integer.MAX_VALUE; 
				OutputCacheValid = false;
				OutputcacheWrite = false;
				if(model.getAvailableOperators().get(currentop).getLatency() == processcounter){
					if(inputALUA != inputALUB){
						exception = EXCEPTION.INLINE_SPECULATION;
//						throw new AmidarSimulatorException("Speculative Method Inlining failed. Expected CTI to be " + inputALUB + " but was " + inputALUA );
					}
					processcounter = 0;
				}
//								System.out.println("CI_CMP");
//								System.out.println("\t"+inputALUA);
//								System.out.println("\t"+inputALUB);
//								System.out.println("\t"+statusALU);
				break;
			case HANDLE_CMP :
				statusALU =false;
				outputALU = Integer.MAX_VALUE; 
				OutputCacheValid = false;
				OutputcacheWrite = false;
				if(model.getAvailableOperators().get(currentop).getLatency() == processcounter){
					if(inputALUA == inputALUB){
//						System.err.println("Same handles: " + inputALUB + " and " + inputALUA );	
						exception = EXCEPTION.ALIASING_SPECULATION;
//						throw new AmidarSimulatorException("Aliasing Speculation failed. Two handles are the same");
					} else {
//						System.err.println("DISTINCT HandLES " + inputALUA + " vs. " + inputALUB);
					}
					processcounter = 0;
				}
//								System.out.println("HANDLE_CMP");
//								System.out.println("\t"+inputALUA);
//								System.out.println("\t"+inputALUB);
				break;

			case IFGE :
				statusALU =false;
				outputALU = Integer.MAX_VALUE;
				OutputCacheValid = false;
				OutputcacheWrite = false;
				if(model.getAvailableOperators().get(currentop).getLatency() == processcounter){
					if(inputALUA >= inputALUB)
						statusALU = true;
					processcounter = 0;
				}
//								System.out.println("IFGE");
//								System.out.println("\t"+inputALUA);
//								System.out.println("\t"+inputALUB);
//								System.out.println("\t"+statusALU);
				break;

			case IFGT :
				statusALU =false;
				outputALU = Integer.MAX_VALUE;
				OutputCacheValid = false;
				OutputcacheWrite = false;
				if(model.getAvailableOperators().get(currentop).getLatency() == processcounter){
					if(inputALUA > inputALUB)
						statusALU = true;
					processcounter = 0;
				}
				//				System.out.println("IFGT");
				//				System.out.println("\t"+inputALUA);
				//				System.out.println("\t"+inputALUB);
				//				System.out.println("\t"+statusALU);
				break;

			case IFLE :
				statusALU =false;
				outputALU = Integer.MAX_VALUE; 
				OutputCacheValid = false;
				OutputcacheWrite = false;
				if(model.getAvailableOperators().get(currentop).getLatency() == processcounter){
					if(inputALUA <= inputALUB)
						statusALU = true;
					processcounter = 0;
				}
				//				System.out.println("IFLE");
				//				System.out.println("\t"+inputALUA);
				//				System.out.println("\t"+inputALUB);
				//				System.out.println("\t"+statusALU);
				break;

			case IFLT :
				statusALU =false;
				outputALU = Integer.MAX_VALUE; 
				OutputCacheValid = false;
				OutputcacheWrite = false;
				if(model.getAvailableOperators().get(currentop).getLatency() == processcounter){
					if(inputALUA < inputALUB)
						statusALU = true;
					processcounter = 0;
				}
//								System.out.println("IFLT");
//								System.out.println("\t"+inputALUA);
//								System.out.println("\t"+inputALUB);
//								System.out.println("\t"+statusALU);
				break;

			case INEG :
				statusALU = false;
				OutputCacheValid = false;
				OutputcacheWrite = false;
				outputALU = Integer.MAX_VALUE;	
				if(model.getAvailableOperators().get(currentop).getLatency() == processcounter){
					outputALU = 0-inputALUA;
					processcounter = 0;
				}
//								System.out.println("INEG");
//								System.out.println("\t"+inputALUA);
//								System.out.println("\t"+inputALUB);
//								System.out.println("\t"+outputALU);
				break;

			case NOP :
				statusALU =false;
				outputALU = inputALUA; 
				OutputCacheValid = false;
				OutputcacheWrite = false;
				if(model.getAvailableOperators().get(currentop).getLatency() <= processcounter){
					processcounter = 0;
				}
				//				System.out.println(outputALU);
				//				System.out.println("NOP");
				//				System.out.println("\t"+inputALUA);
				//				System.out.println("\t"+inputALUB);
				//				System.out.println("\t"+outputALU);
				break;

				//		case XXXX :  // template
				//			statusALU =;
				//			outputALU = Integer.MAX_VALUE; // ***¹ should avoid to compute valid results that are not ready yet in reality, only for multicycle
				//			OutputCacheValid = ;
				//			OutputcacheWrite = ;
				//			if(model.getAvailableOperators().get(currentop).getLatency() == processcounter){
				//				// to something
				//				processcounter = 0;
				//				}
				//			break;
			case I2B:
				if(processcounter == 1){
					regALUAlow = inputALUA;
				}	
				statusALU = false;
				outputALU = Integer.MAX_VALUE; // ***¹ should avoid to compute valid results that are not ready yet in reality, only for multicycle
				OutputCacheValid = false;
				OutputcacheWrite = false;
				if(model.getAvailableOperators().get(currentop).getLatency() == processcounter){
					byte buffer = (byte)regALUAlow;
					outputALU = buffer;
//					System.out.println("\tI2B");
//					System.out.println("\t"+regALUAlow);
//					System.out.println("\t"+buffer);
					processcounter = 0;
				}
				break;
			case I2F:
				if(processcounter == 1){
					regALUAlow = inputALUA;
				}	
				statusALU = false;
				outputALU = Integer.MAX_VALUE; // ***¹ should avoid to compute valid results that are not ready yet in reality, only for multicycle
				OutputCacheValid = false;
				OutputcacheWrite = false;
				if(model.getAvailableOperators().get(currentop).getLatency() == processcounter){
					outputALU = Float.floatToIntBits((float)regALUAlow);
					processcounter = 0;
				}
				break;
			case F2I:
				if(processcounter == 1){
					regALUAlow = inputALUA;
				}	
				statusALU = false;
				outputALU = Integer.MAX_VALUE; // ***¹ should avoid to compute valid results that are not ready yet in reality, only for multicycle
				OutputCacheValid = false;
				OutputcacheWrite = false;
				if(model.getAvailableOperators().get(currentop).getLatency() == processcounter){
					outputALU = (int)Float.intBitsToFloat(regALUAlow);
					processcounter = 0;
				}
//								System.out.println("F2I");
//								System.out.println("\t"+inputALUA + " = " +Float.intBitsToFloat(inputALUA));
//								System.out.println("\t"+outputALU);
				break;
			case FADD:
				if(processcounter == 1){
					regALUAlow = inputALUA;
					regALUBlow = inputALUB;
				}	
				statusALU = false;
				outputALU = Integer.MAX_VALUE; // ***¹ should avoid to compute valid results that are not ready yet in reality, only for multicycle
				OutputCacheValid = false;
				OutputcacheWrite = false;
				if(model.getAvailableOperators().get(currentop).getLatency() == processcounter){
					float a = Float.intBitsToFloat(regALUAlow);
					float b = Float.intBitsToFloat(regALUBlow);
					outputALU = Float.floatToIntBits(a+b);
					processcounter = 0;
				}
				break;
			case FSUB:
				if(processcounter == 1){
					regALUAlow = inputALUA;
					regALUBlow = inputALUB;
				}	
				statusALU = false;
				outputALU = Integer.MAX_VALUE; // ***¹ should avoid to compute valid results that are not ready yet in reality, only for multicycle
				OutputCacheValid = false;
				OutputcacheWrite = false;
				if(model.getAvailableOperators().get(currentop).getLatency() == processcounter){
					float a = Float.intBitsToFloat(regALUAlow);
					float b = Float.intBitsToFloat(regALUBlow);
					outputALU = Float.floatToIntBits(a-b);
					processcounter = 0;
				}
				break;
			case FMUL:
				if(processcounter == 1){
					regALUAlow = inputALUA;
					regALUBlow = inputALUB;
				}	
				statusALU = false;
				outputALU = Integer.MAX_VALUE; // ***¹ should avoid to compute valid results that are not ready yet in reality, only for multicycle
				OutputCacheValid = false;
				OutputcacheWrite = false;
				if(model.getAvailableOperators().get(currentop).getLatency() == processcounter){
					float a = Float.intBitsToFloat(regALUAlow);
					float b = Float.intBitsToFloat(regALUBlow);
					outputALU = Float.floatToIntBits(a*b);
					processcounter = 0;
//					System.out.println("FMUL");
//					System.out.println("\t"+inputALUA + " = " +a);
//					System.out.println("\t"+inputALUB + " = " +b);
//					System.out.println("\t"+outputALU + " = " +Float.intBitsToFloat(outputALU));
				}
				break;
			case FDIV:
				if(processcounter == 1){
					regALUAlow = inputALUA;
					regALUBlow = inputALUB;
				}	
				statusALU = false;
				outputALU = Integer.MAX_VALUE; // ***¹ should avoid to compute valid results that are not ready yet in reality, only for multicycle
				OutputCacheValid = false;
				OutputcacheWrite = false;
				if(model.getAvailableOperators().get(currentop).getLatency() == processcounter){
					float a = Float.intBitsToFloat(regALUAlow);
					float b = Float.intBitsToFloat(regALUBlow);
					outputALU = Float.floatToIntBits(a/b);
					processcounter = 0;
				}
				break;
				case DADD:
					if(processcounter == 1){
						regALUAlow = inputALUA;
						regALUBlow = inputALUB;
					}	
					if(processcounter == 2){
						regALUAhigh = inputALUA;
						regALUBhigh = inputALUB;
						long aBits = (((long)regALUAhigh)<<32) + (0xFFFFFFFFL&regALUAlow);
						long bBits = (((long)regALUBhigh)<<32) + (0xFFFFFFFFL&regALUBlow);
						double a = Double.longBitsToDouble(aBits);
						double b = Double.longBitsToDouble(bBits);
						double result = a + b;
						carryLongOps = Double.doubleToLongBits(result);
//										System.out.println("DADD");
//										System.out.println("\t"+a + " " + regALUAhigh +":" + regALUAlow);
//										System.out.println("\t"+b + " " + regALUBhigh +":" + regALUBlow);
//										System.out.println("\t"+result + " " +  (int)carryLongOps + ":" + ((int)(carryLongOps>>32))) ;
						
						
						
					}
					statusALU = false;
					outputALU = Integer.MAX_VALUE; // ***¹ should avoid to compute valid results that are not ready yet in reality, only for multicycle
					OutputCacheValid = false;
					OutputcacheWrite = false;
					
					if(model.getAvailableOperators().get(currentop).getLatency()-1 == processcounter){
						outputALU = (int)carryLongOps;
					}					
					if(model.getAvailableOperators().get(currentop).getLatency() == processcounter){
						outputALU = ((int)(carryLongOps>>32));
						processcounter = 0;
					}
					break;
				case DSUB:
					if(processcounter == 1){
						regALUAlow = inputALUA;
						regALUBlow = inputALUB;
					}	
					if(processcounter == 2){
						regALUAhigh = inputALUA;
						regALUBhigh = inputALUB;
						long aBits = (((long)regALUAhigh)<<32) + (0xFFFFFFFFL&regALUAlow);
						long bBits = (((long)regALUBhigh)<<32) + (0xFFFFFFFFL&regALUBlow);
						double a = Double.longBitsToDouble(aBits);
						double b = Double.longBitsToDouble(bBits);
						double result = a - b;
						carryLongOps = Double.doubleToLongBits(result);
//										System.out.println("DSUB");
//										System.out.println("\t"+a + " " + regALUAhigh +":" + regALUAlow);
//										System.out.println("\t"+b + " " + regALUBhigh +":" + regALUBlow);
//										System.out.println("\t"+result + " " +  (int)carryLongOps + ":" + ((int)(carryLongOps>>32))) ;
						
						
						
					}
					statusALU = false;
					outputALU = Integer.MAX_VALUE; // ***¹ should avoid to compute valid results that are not ready yet in reality, only for multicycle
					OutputCacheValid = false;
					OutputcacheWrite = false;
					
					if(model.getAvailableOperators().get(currentop).getLatency()-1 == processcounter){
						outputALU = (int)carryLongOps;
					}					
					if(model.getAvailableOperators().get(currentop).getLatency() == processcounter){
						outputALU = ((int)(carryLongOps>>32));
						processcounter = 0;
					}
					break;
				case DMUL:
					if(processcounter == 1){
						regALUAlow = inputALUA;
						regALUBlow = inputALUB;
					}	
					if(processcounter == 2){
						regALUAhigh = inputALUA;
						regALUBhigh = inputALUB;
						long aBits = (((long)regALUAhigh)<<32) + (0xFFFFFFFFL&regALUAlow);
						long bBits = (((long)regALUBhigh)<<32) + (0xFFFFFFFFL&regALUBlow);
						double a = Double.longBitsToDouble(aBits);
						double b = Double.longBitsToDouble(bBits);
						double result = a * b;
						carryLongOps = Double.doubleToLongBits(result);
//										System.out.println("DMUL");
//										System.out.println("\t"+a + " " + regALUAhigh +":" + regALUAlow);
//										System.out.println("\t"+b + " " + regALUBhigh +":" + regALUBlow);
//										System.out.println("\t"+result + " " +  (int)carryLongOps + ":" + ((int)(carryLongOps>>32))) ;
						
						
						
					}
					statusALU = false;
					outputALU = Integer.MAX_VALUE; // ***¹ should avoid to compute valid results that are not ready yet in reality, only for multicycle
					OutputCacheValid = false;
					OutputcacheWrite = false;
					
					if(model.getAvailableOperators().get(currentop).getLatency()-1 == processcounter){
						outputALU = (int)carryLongOps;
					}					
					if(model.getAvailableOperators().get(currentop).getLatency() == processcounter){
						outputALU = ((int)(carryLongOps>>32));
						processcounter = 0;
					}
					break;
				case DDIV:
					if(processcounter == 1){
						regALUAlow = inputALUA;
						regALUBlow = inputALUB;
					}	
					if(processcounter == 2){
						regALUAhigh = inputALUA;
						regALUBhigh = inputALUB;
						long aBits = (((long)regALUAhigh)<<32) + (0xFFFFFFFFL&regALUAlow);
						long bBits = (((long)regALUBhigh)<<32) + (0xFFFFFFFFL&regALUBlow);
						double a = Double.longBitsToDouble(aBits);
						double b = Double.longBitsToDouble(bBits);
						double result = a / b;
						carryLongOps = Double.doubleToLongBits(result);
//										System.out.println("DDIV");
//										System.out.println("\t"+a + " " + regALUAhigh +":" + regALUAlow);
//										System.out.println("\t"+b + " " + regALUBhigh +":" + regALUBlow);
//										System.out.println("\t"+result + " " +  (int)carryLongOps + ":" + ((int)(carryLongOps>>32))) ;
						
						
						
					}
					statusALU = false;
					outputALU = Integer.MAX_VALUE; // ***¹ should avoid to compute valid results that are not ready yet in reality, only for multicycle
					OutputCacheValid = false;
					OutputcacheWrite = false;
					
					if(model.getAvailableOperators().get(currentop).getLatency()-1 == processcounter){
						outputALU = (int)carryLongOps;
					}					
					if(model.getAvailableOperators().get(currentop).getLatency() == processcounter){
						outputALU = ((int)(carryLongOps>>32));
						processcounter = 0;
					}
					break;
			case FNEG:
				if(processcounter == 1){
					regALUAlow = inputALUA;
					regALUBlow = inputALUB;
				}	
				statusALU = false;
				outputALU = Integer.MAX_VALUE; // ***¹ should avoid to compute valid results that are not ready yet in reality, only for multicycle
				OutputCacheValid = false;
				OutputcacheWrite = false;
				if(model.getAvailableOperators().get(currentop).getLatency() == processcounter){
					float a = Float.intBitsToFloat(regALUAlow);
//					float b = Float.intBitsToFloat(regALUBlow);
//					outputALU  = regALUAlow*33; 
					outputALU = Float.floatToIntBits(-a);
					processcounter = 0;
				}
				break;
			case DNEG:
				if(processcounter == 1){
					regALUAlow = inputALUA;
					regALUBlow = inputALUB;
				}	
				if(processcounter == 2){
					regALUAhigh = inputALUA;
					regALUBhigh = inputALUB;
					long aBits = (((long)regALUAhigh)<<32) + (0xFFFFFFFFL&regALUAlow);
					long bBits = (((long)regALUBhigh)<<32) + (0xFFFFFFFFL&regALUBlow);
					double a = Double.longBitsToDouble(aBits);
					double b = Double.longBitsToDouble(bBits);
					double result = -a ;
					carryLongOps = Double.doubleToLongBits(result);
//									System.out.println("DNEG");
//									System.out.println("\t"+a + " " + regALUAhigh +":" + regALUAlow);
////									System.out.println("\t"+b + " " + regALUBhigh +":" + regALUBlow);
//									System.out.println("\t"+result + " " +  (int)carryLongOps + ":" + ((int)(carryLongOps>>32))) ;
					
					
					
				}
				statusALU = false;
				outputALU = Integer.MAX_VALUE; // ***¹ should avoid to compute valid results that are not ready yet in reality, only for multicycle
				OutputCacheValid = false;
				OutputcacheWrite = false;
				
				if(model.getAvailableOperators().get(currentop).getLatency()-1 == processcounter){
					outputALU = (int)carryLongOps;
				}					
				if(model.getAvailableOperators().get(currentop).getLatency() == processcounter){
					outputALU = ((int)(carryLongOps>>32));
					processcounter = 0;
				}
				break;
			case FSIN:
				if(processcounter == 1){
					regALUAlow = inputALUA;
					regALUBlow = inputALUB;
				}	
				statusALU = false;
				outputALU = Integer.MAX_VALUE; // ***¹ should avoid to compute valid results that are not ready yet in reality, only for multicycle
				OutputCacheValid = false;
				OutputcacheWrite = false;
				if(model.getAvailableOperators().get(currentop).getLatency() == processcounter){
					float a = Float.intBitsToFloat(regALUAlow);
					outputALU = Float.floatToIntBits((float)Math.sin(a));
					processcounter = 0;
				}
				break;
			case FCOS:
				if(processcounter == 1){
					regALUAlow = inputALUA;
					regALUBlow = inputALUB;
				}	
				statusALU = false;
				outputALU = Integer.MAX_VALUE; // ***¹ should avoid to compute valid results that are not ready yet in reality, only for multicycle
				OutputCacheValid = false;
				OutputcacheWrite = false;
				if(model.getAvailableOperators().get(currentop).getLatency() == processcounter){
					float a = Float.intBitsToFloat(regALUAlow);
					outputALU = Float.floatToIntBits((float)Math.cos(a));
					processcounter = 0;
				}
				break;
			case I2D:
				if(processcounter == 1){
					regALUAlow = inputALUA;
					regALUBlow = inputALUB;
				}	
				if(processcounter == 2){
					double result = regALUAlow ;
					carryLongOps = Double.doubleToLongBits(result);
//									System.out.println("I2D");
//									System.out.println("\t"+regALUAlow + " " + regALUAhigh +":" + regALUAlow);
////									System.out.println("\t"+b + " " + regALUBhigh +":" + regALUBlow);
//									System.out.println("\t"+result + " " +  (int)carryLongOps + ":" + ((int)(carryLongOps>>32))) ;
					
					
					
				}
				statusALU = false;
				outputALU = Integer.MAX_VALUE; // ***¹ should avoid to compute valid results that are not ready yet in reality, only for multicycle
				OutputCacheValid = false;
				OutputcacheWrite = false;
				
				if(model.getAvailableOperators().get(currentop).getLatency()-1 == processcounter){
					outputALU = (int)carryLongOps;
				}					
				if(model.getAvailableOperators().get(currentop).getLatency() == processcounter){
					outputALU = ((int)(carryLongOps>>32));
					processcounter = 0;
				}
				break;
			case DCMPG:
			case DCMPL:
				if(processcounter == 1){
					regALUAlow = inputALUA;
					regALUBlow = inputALUB;
				}	
				if(processcounter == 2){
					regALUAhigh = inputALUA;
					regALUBhigh = inputALUB;
					long aBits = (((long)regALUAhigh)<<32) + (0xFFFFFFFFL&regALUAlow);
					long bBits = (((long)regALUBhigh)<<32) + (0xFFFFFFFFL&regALUBlow);
					double a = Double.longBitsToDouble(aBits);
					double b = Double.longBitsToDouble(bBits);
					carryLongOps =Double.compare(a, b);
//									System.out.println("DCMP");
//									System.out.println("\t"+a + " " + regALUAhigh +":" + regALUAlow);
//									System.out.println("\t"+b + " " + regALUBhigh +":" + regALUBlow);
//									System.out.println("\t"+ (int)carryLongOps ) ;
					
					
					
				}
				statusALU = false;
				outputALU = Integer.MAX_VALUE; // ***¹ should avoid to compute valid results that are not ready yet in reality, only for multicycle
				OutputCacheValid = false;
				OutputcacheWrite = false;
				
				if(model.getAvailableOperators().get(currentop).getLatency() == processcounter){
					outputALU = ((int)(carryLongOps));
					processcounter = 0;
				}
				break;
			case FCMPG:
			case FCMPL:
				if(processcounter == 1){
					regALUAlow = inputALUA;
					regALUBlow = inputALUB;
					float a = Float.intBitsToFloat(regALUAlow);
					float b = Float.intBitsToFloat(regALUBlow);
					carryLongOps =Float.compare(a, b);
//									System.out.println("FCMP");
//									System.out.println("\t"+a + " " + regALUAhigh +":" + regALUAlow);
//									System.out.println("\t"+b + " " + regALUBhigh +":" + regALUBlow);
//									System.out.println("\t"+ (int)carryLongOps ) ;
					
					
					
				}
				statusALU = false;
				outputALU = Integer.MAX_VALUE; // ***¹ should avoid to compute valid results that are not ready yet in reality, only for multicycle
				OutputCacheValid = false;
				OutputcacheWrite = false;
				
				if(model.getAvailableOperators().get(currentop).getLatency() == processcounter){
					outputALU = ((int)(carryLongOps));
					processcounter = 0;
				}
				break;
			default:
				System.err.println("FERRo: " +  model.getID());
				throw new AmidarSimulatorException("Operation "+ String.valueOf(contextmask.operation(context))+"= "+Amidar.OP.values()[contextmask.operation(context)]+ " not defined in "+this.getClass());
			}
		}
		//		System.out.println(" PE " + PeID + " result: " + outputALU + "  status : " + statusALU);
		return true;
	}


	/**
	 * All register (and therefore clock) related signals are simulated here
	 */
	public void regClocked(){
		if(internalEnable){	// is actually only a write 
			regfile.clocked();
		}
	}

	/**
	 * combinatorial signal for the register file
	 */
	public void regComb(){
		regfile.setWriteAddress(contextmask.addrWr(context));
		regfile.setAddrCache(contextmask.addrCache(context));
		regfile.setAddrDirect(contextmask.addrDo(context));
		regfile.setAddrMux(contextmask.addrMux(context));
		regfile.combinatorial();
		Regout = regfile.getOutputDirect();
		regoutDefined = regfile.getOutputDirectDefined();
		baseAddrCache = regfile.getOutputCache();	
	}

	/**
	 * Since java simulates sequentially the afterwards simulated signal from the Pbox still need to be processed in this cycle.
	 * therefore this propagates the later arrived signal from the pbox to the write enable input of the regfile.   
	 */
	public void  combinatorialLateArrival(){
		boolean write_enable;
		//		System.out.println(" PE " + PeID + " comb late - Input Pbox : " + inputCBox);
		if(contextmask.writeEnableConditional(context)){
			write_enable = inputPredication && contextmask.writeEnable(context);
		}
		else{
			write_enable = contextmask.writeEnable(context);
		}

		internalEnable = getInputEnable();//XXXFFF && contextmask.enable(context);

//		offsetCache = Regout;

		// ins and out of the Regfile
		regfile.setWriteEnable(write_enable);

		switch(contextmask.muxReg(context)){
		case IN :
			regfile.setInputData(getInputAmidar());
			break;
		case ALU :
			regfile.setInputData(outputALU);
			break;
		case INDMA :
			regfile.setInputData(InputCache);
			break;
		default:
			throw new AmidarSimulatorException("jo maaan, there's a problem in muxReg ... opcode "+ contextmask.muxReg(context) + "out of bound");
		}
	}

	/**
	 * Getter method to ask for connection to the objectheap 
	 */
	public boolean memoryAccess(){
		return model.getMemAccess();
	}
	
	/**
	 * Getter method to ask for connection to the objectheap 
	 */
	public boolean controlFlow(){
		return model.getControlFlow();
	}


	public boolean getStatus(){
		return statusALU;
	}


	public void setInputCacheData(int inputCacheData) {
		InputCache = inputCacheData;	
	}


	protected boolean getInputCBox() {
		return inputPredication;
	}


	public void setInputCBox(boolean inputCBox) {
		this.inputPredication = inputCBox;
	}


	public boolean getInputEnable() {
		return InputEnable;
	}


	public void setInputEnable(boolean inputEnable) {
		InputEnable = inputEnable;
	}


	public int getInputAmidar() {
		return InputAmidar;
	}


	public void setInputAmidar(int inputAmidar) {
		InputAmidar = inputAmidar;
	}


	public double getDynamicEnergy(){
		double energy  = 0;

		for(Operator op: model.getAvailableNonNativeOperators().keySet()){
			energy += executionCounter.get(op)* model.getAvailableNonNativeOperators().get(op).getEnergyconsumption();
		} // TODO testen
		return energy;
	}


	public double getStaticEnergy(){
		double energy = 0;

		for(Implementation imp: model.getAvailableOperatorImplementations()){
			energy += imp.getEnergyconsumption();
		}
		energy += inputs.length*60;
		energy += regfile.getSize()/256;
		// TODO include regfile size
		return energy * 0.0001; // TODO determine a correct scaling factor
	}
	
	
	public void checkException(){
		boolean active;
		if(contextmask.writeEnableConditional(context) || contextmask.dmaConditional(context)){
			active = inputPredication;
		}
		else{
			active = true;
		}
		
		
		if(active && exception != null){
			switch(exception){
			case DIV_BY_ZERO:
				throw new AmidarSimulatorException("DIV by zero Exception in CGRA");
			case INLINE_SPECULATION:
				throw new AmidarSimulatorException("Speculative Method Inlining failed. Expected CTI to be " + inputALUB + " but was " + inputALUA );
			case ALIASING_SPECULATION:
				throw new AmidarSimulatorException("Aliasing Speculation failed. Two handles are the same");
//				System.err.println("YOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO anerror");
			}
		}
	}
	
	
	private enum EXCEPTION{
		DIV_BY_ZERO,
		INLINE_SPECULATION,
		ALIASING_SPECULATION
	}
	
	public int getCBoxSelect(){
		return cBoxSelect;
	}
	
}
