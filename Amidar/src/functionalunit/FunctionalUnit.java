package functionalunit;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.EnumMap;
import java.util.LinkedList;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import tracer.Trace;
import tracer.TraceManager;
import exceptions.AmidarSimulatorException;
import functionalunit.opcodes.FUOpcodes;

/**
 * This Class provides the basic functionality for all FUs. It also provides functionality of the Wrapper module used
 * in the HW Implementation of AMIDAR to connect FUs to the Bus.
 * @author Lukas Jung
 *
 * @param <K> The Enumeration describing the Operation of the FU
 */
public abstract class FunctionalUnit<K extends Enum<K>> {
	
	/**
	 * This class models the TokenAdapter. It is a FIFO storing tokens.
	 * Up to length token can be stored. The length+1st Token will be "buffered" - but the tokenmachine will stop because acceptedToken() returns false
	 * This simulates that the tokenmachine can only proceed to the next line in the token matrix when all tokens are accepted
	 * @author jung
	 *
	 */
	class TokenFIFO{
		
		/**
		 * The token FIFO
		 */
		LinkedList<Token> token;
		/**
		 * The FIFO depth
		 */
		private final int length;
		
		
		/**
		 * Creates a FIFO with given depth
		 * @param length the depth of the FIFO
		 */
		TokenFIFO(int length){
			this.length = length;
			token = new LinkedList<Token>();
		}
		
		/**
		 * Adds a Token to the FIFO
		 * @param opcode
		 * @param tag
		 * @param destinationFU
		 * @param destinationPort
		 * @param tagInc
		 */
		public void addToken(K opcode, int tag, FunctionalUnit destinationFU, int destinationPort, boolean tagInc){
			token.addLast(new Token(opcode,tag,destinationFU,destinationPort,tagInc));
			if(tokenTrace.active()){
				tokenTrace.println(getName()+ " accepted Token "+tag);
				tokenTrace.println("\t" + opcode + "\tdestination FU: "+destinationFU + "\tdestination port: " + destinationPort+ "\ttag inc: "+tagInc);
				if(token.size()==length){
					tokenTrace.println(" \tTokenAdapter full - stopping TokenMachine");
				}
				tokenTrace.println();
			}
			if(token.size()>length){
				throw new AmidarSimulatorException("Token FIFO overflow - should never happen. Tokenmachine should stop producing tokens");
			}
		}
	
		/**
		 * Fetches the next token from the FIFO and sets it as the current token in the functional unit
		 */
		public void nextToken(){
			sendDestinationFU = destinationFU;
			sendDestinationPort = destinationPort;
			sendDestinationTag = destinationTag;
			if(token.isEmpty()){
				setTokenValid(false);
			}else{
				setTokenValid(true);
				Token nextToken = token.removeFirst();
				setToken(nextToken.opcode, nextToken.tag, nextToken.destinationTag, nextToken.destinationFU, nextToken.destinationPort);
			}
		}
		
		/**
		 * Denotes whether this FIFO is full
		 * @return
		 */
		public boolean isFull(){
			return !(token.size()<length);
		}
		
		
		
	}
	
	/**
	 * This class describes a Token of the AMIDAR processor
	 * @author jung
	 *
	 */
	private class Token{
		
		K opcode;
		int tag;
		int destinationTag;
		FunctionalUnit<?> destinationFU;
		int destinationPort;
		
		/**
		 * Creates a new Token
		 * @param opcode
		 * @param tag
		 * @param destinationFU
		 * @param destinationPort
		 * @param tagInc
		 */
		Token(K opcode, int tag, FunctionalUnit<?> destinationFU, int destinationPort, boolean tagInc){
			this.opcode = opcode;
			this.tag = tag;
			this.destinationTag = tagInc?tag+1 : tag;
			this.destinationFU = destinationFU;
			this.destinationPort = destinationPort;
		}
		
		
		public String toString(){
			return opcode + " : " + destinationTag + " to " + destinationFU+":" + destinationPort; 
		}
		
		
	}

	/**
	 * Defines operand Names
	 */
	public static final int 	OPERAND_A_LOW 		= 0;

	public static final int 	OPERAND_A_HIGH 		= 1;

	public static final int 	OPERAND_B_LOW 		= 2;

	public static final int 	OPERAND_B_HIGH		= 3;

	public static final int 	OPERAND_C_LOW 		= 4;

	public static final int 	OPERAND_C_HIGH	 	= 5;

	public static final int 	RESULT_LOW 			= 0,
								RESULT_HIGH 		= 1;


	/**
	 * This static field is a counter for unnamed FUs.
	 * When a config file contains no name, the name will be set to ClassnameID
	 * Afterwards ID is incremented
	 */
	static int ID = 0;

	/**
	 * The name of the functional Unit for debugging purposes
	 */
	String name = "noname";

	
	private TraceManager traceManager;
	private Trace tokenTrace;
	private Trace packetTrace;
	protected Trace executeTrace;
	
	JSONObject jsonConfig;
	
	/**
	 * Stores all incoming Token
	 */
	protected TokenFIFO tokenAdapter;
	
	/**
	 * The input ports that are connected to the Amidar Bus. The number of ports has to be defined in subclasses in {@link getNrOfInputports()}
	 */
	int [] input;
	
	public boolean [] inputValid;

	/**
	 * The output ports that are connectet to the Amidar Bus. The number of ports is hardcoded in the Constructor
	 */
	int [] output;
	
	boolean [] outputValid;
	
	K opcode;
	int tag;
	int destinationTag;
	FunctionalUnit<?> destinationFU;
	int destinationPort;
	
	
	boolean tagInc;
	boolean tokenValid = false;
	
	boolean resultAck;
	
	int sendDestinationTag;
	FunctionalUnit<?> sendDestinationFU;
	int sendDestinationPort;
	
	
	State currentState = State.IDLE;

	/**
	 * The Enumeration holding all Operations of the FU
	 */
	Class<K> ops;

	/**
	 * Maps the duration read from the config file to the operations
	 */
	private EnumMap<K, Integer> duration;
	
	/**
	 * Maps the energy read from the config file to the operations
	 */
	private EnumMap<K, Double>	 energy; 
	
	/**
	 * Counts the number of executions for each op
	 */
	private EnumMap<K, Integer> executionCount;
	
	private double staticEnergy = 0;

	/**
	 * Creates a new Functional unit with the given Opcodes and a config file
	 * @param ops An enumeration describing all operation that the FU can perform
	 * @param configFile A config file containing the name of the FU and energy consumtion and duration for all operations in ops 
	 */
	public FunctionalUnit(Class<K> ops, String configFile, TraceManager traceManager){
		this.traceManager = traceManager;
		tokenTrace = traceManager.getf("tokens");
		packetTrace = traceManager.getf("packets");
		executeTrace = traceManager.getf("execute");
		this.ops = ops;
		if(!(FUOpcodes.class.isAssignableFrom(ops))){
			throw new AmidarSimulatorException("The Opcode Enumeration of Class \""+this.getClass().getName()+"\" does not Implement the Interface \"FUOpcodes\"");
		}

		configureFU(configFile);

		
		tokenAdapter = new TokenFIFO(16);
		input = new int[getNrOfInputports()];
		inputValid = new boolean[input.length];
		output =new int[2];
		outputValid = new boolean[output.length];
	}

	/**
	 * Configures the Functional Unit according to the Config File defined by the Parameter
	 * @param configFile The config file which contains all necessary information
	 */
	protected void configureFU(String configFile){
		duration = new EnumMap<K, Integer>(ops);
		energy = new EnumMap<K, Double>(ops);
		executionCount = new EnumMap<K, Integer>(ops);

		JSONParser parser;
		jsonConfig = null;
		parser = new JSONParser();
		FileReader fileReader;
		try {
			if(configFile == null){
				System.err.println("No config file defined for FU " + this.getClass()+"\n");
			}
			fileReader = new FileReader(configFile);
			jsonConfig = (JSONObject) parser.parse(fileReader);
		} catch (FileNotFoundException e) {
			System.err.println("No config file found for FU " + this.getClass() + ": \""+configFile+"\"\n");
			e.printStackTrace(System.err);
		} catch (IOException e) {
			System.err.println("Error while reading config file for FU" + this.getClass()+ "\n");
			e.printStackTrace(System.err);
		} catch (ParseException e) {
			System.err.println("Error while reading config file for FU" + this.getClass()+ "\n");
			e.printStackTrace(System.err);
		}
		K[] OPS  = ops.getEnumConstants();

		for(K op: OPS){
			executionCount.put(op, 0);
			if (!setOP(op, jsonConfig))
				throw new AmidarSimulatorException("Operation "+ op.name() + " not correctly defined or not defined at all in config File \"" + configFile+"\"\n");
		}
		
		staticEnergy = (Double) jsonConfig.get("staticEnergy");

		name = (String) jsonConfig.get("name");
		if(name == null){
			name = this.getClass().getName()+ID++;
			System.err.println("WARNING: No name defined in \""+configFile+"\". Name is set to \""+name+"\"");
			System.err.println("         Simulator will run nevertheless.");
		}
	}

	/**
	 * Sets the configuration for the given operation with information extracted from the given JSON Object
	 * @param op The operation to be configured 
	 * @param json The JSON Object holding the necessar information
	 * @return true when the operation could be configured correctly
	 */
	private boolean setOP(K op, JSONObject json){
		JSONObject operation = (JSONObject) json.get(op.name());
		if(operation == null){
			return false;
		}


		Integer duration = ((Long) operation.get("duration")).intValue();
		Double energy = (Double) operation.get("energy");

		if(energy == null){
			return false;
		}

		this.duration.put(op, duration);
		this.energy.put(op, energy);
		return true;
	}

	/**
	 * Returns the energy consumption of the FU when executing the given operation
	 * @param op The operation
	 * @return The energy consumption as double precision value without unit 
	 */
	public double getEnergy(K op){
		return energy.get(op);
	}

	/**
	 * Returns the duration of the given operation
	 * @param op The operation 
	 * @return The duration in Clock cycles
	 */
	public int getDuration(K op){
			return duration.get(op);
	}

	/**
	 * Returns the String representation of the FU which is equal to the name of the FU
	 */
	public String toString(){
		return name;
	}


	/**
	 * Returns the Number of input ports of the FU
	 * @return The number of ports
	 */
	public abstract int getNrOfInputports();
	
	
	/**
	 * Starts the operation using {@link input} and writing the result to {@link output} and setting the corresponding {@link outputValid}
	 * @param op The operation to be performed
	 */
	public abstract boolean executeOp(K op);
	
	/**
	 * Checks wheather the inputs are ready/valid for the execution of the given opcode
	 * @param op
	 * @return
	 */
	public abstract boolean validInputs(K op);
	
	
	public void addToken(K opcode, int tag, FunctionalUnit destinationFU, int destinationPort, boolean tagInc){
		tokenAdapter.addToken(opcode, tag, destinationFU, destinationPort, tagInc);
	}
	
	/**
	 * Sets the token that the FunctionalUnit has to execute
	 * @param opcode
	 * @param tag
	 * @param destinationFU
	 * @param destinationPort
	 * @param tagInc
	 */
	public void setToken(K opcode, int tag, int destinationTag, FunctionalUnit destinationFU, int destinationPort){
		this.opcode = opcode;
		this.tag = tag;
		this.destinationTag = destinationTag;
		this.destinationFU = destinationFU;
		this.destinationPort = destinationPort;
		
		executionCount.put(opcode, executionCount.get(opcode) + 1);
	}
	
	/**
	 * Sets the current token to be valid
	 * @param valid
	 */
	public void setTokenValid(boolean valid){
		this.tokenValid = valid;
	}

	/**
	 * Sends an operand to the functional unit.
	 * The functional unit will accept it when tag matches 
	 * the tag of the current token
	 * @param operandNumber
	 * @param operand
	 * @param tag
	 * @return whether the operand was accepted
	 */
	public boolean setOperand(int operandNumber, int operand, int tag){
//		System.out.println(this + "st operand with tag: " +tag+ " expecting: "+ this.tag);
		
		if(tag == this.tag){
			input[operandNumber] = operand;
			inputValid[operandNumber] = true;
			if(packetTrace.active()){
				packetTrace.println(this.toString()+ " accepted datapaket with tag " +tag);
			}
			return true;
		} else{
			if(packetTrace.active()){
				packetTrace.println(this.toString()+ " rejected datapaket with tag " +tag + " (expecting tag "+this.tag+")" + opcode);
			}
			return false;
		}
	}
	
	
	enum TransferState{IDLE,LOW32,HIGH64,LOW64};
	TransferState transferState=TransferState.IDLE;
	/**
	 * transmits results to target FU
	 * represents output_adapter and has to call in every clock-cycle from arbiter
	 * 
	 * Signals like HTRANS, HRESP can be coded in return, if needed
	 * 
	 * @param hgrant true: FU has access to the bus
	 * 				 false: FU must not send any data
	 * 
	 * @return 0 if no HBUSREQ
	 * 		   1 HBUSREQ
	 * 		   2 HBUSREQ and HLOCK is set
	 */
	public int send(boolean hgrant) {
		if(currentState==State.SENDING) { //check not neccessary, but maybe increases speed
			switch(transferState) {
				case IDLE:
					if(getResultValid(RESULT_HIGH)) {
						transferState=TransferState.LOW64;
						return 1;
					}
					else if(getResultValid(RESULT_LOW)) {
						transferState=TransferState.LOW32;
						return 1;
					}
					break;
				case LOW32:
					if(packetTrace.active()){
						packetTrace.println(this.toString()+ " trying to send datapaket with tag " +sendDestinationTag + " to Port " + (sendDestinationPort*2) + " of "+sendDestinationFU+ ": "+output[RESULT_LOW]+" Access: "+hgrant);
					}
					if(hgrant&&sendDestinationFU.setOperand(2*sendDestinationPort, output[RESULT_LOW], sendDestinationTag)) {
						transferState=TransferState.IDLE;
						setResultAck(true);
						return 0;	//transaction succeeded, no further bus request
					}
					return 1; //still bus request, no bus grant or destination didn't accept data
				case HIGH64:
					if(packetTrace.active()){
						packetTrace.println(this.toString()+ " trying to send datapaket with tag " +sendDestinationTag + " to Port " + (sendDestinationPort*2+1) + " of "+sendDestinationFU+ ": "+output[RESULT_LOW]+" Access: "+hgrant);
					}
					if(hgrant&&sendDestinationFU.setOperand(2*sendDestinationPort+1, output[RESULT_HIGH], sendDestinationTag)) {
						transferState=TransferState.IDLE;
						setResultAck(true);
						return 0;  //transaction succeeded, no further bus request
					}
					return 1;
				case LOW64:
					if(packetTrace.active()){
						packetTrace.println(this.toString()+ " trying to send datapaket with tag " +sendDestinationTag + " to Port " + (sendDestinationPort*2) + " of "+sendDestinationFU);
					}
					if(hgrant && sendDestinationFU.setOperand(2*sendDestinationPort, output[RESULT_LOW], sendDestinationTag)) {
						transferState=TransferState.HIGH64;
						return 2; //transaction succeeded, bus lock for next cycle
					}
					return 1;		
			}
		}
		return 0; //no valid output Data from FU
	}
	
//	public void setOperandValid(int operandNumber, boolean valid){
//		inputValid[operandNumber] = valid;
//	}
	
//	public void setEnable(boolean enable){
//		this.enable = enable;
//	}
	


	/**
	 * Returns the result 
	 * @param resultNumber the outport identifier
	 * @return the result
	 */
	public Number getResult(int resultNumber){
		return output[resultNumber];
	}
	
	/**
	 * Sets whether the result is valid or not
	 * @param resultNumber the output identifier
	 * @param valid valid or not
	 */
	protected void setOutputValid(int resultNumber){
		outputValid[resultNumber] = true;
	}
	
	
	/**
	 * Returns whether the Result is valid or not
	 * @param resultNumber the outport identifier
	 * @return valid or not
	 */
	public boolean getResultValid(int resultNumber){
		return outputValid[resultNumber];
	}
	
	/**
	 * Sets the value of resultAcknowledge to ack
	 * @param ack
	 */
	public void setResultAck(boolean ack){
		this.resultAck = ack;
	}
	
	/**
	 * Denotes whether the execution is ready
	 * @return Returns true when the result was sent to the receiver (and thus the execution is ready)
	 * or when no data has to be sent
	 */
	protected boolean getResultAck(){
		return resultAck;
	}
	
	/**
	 * Returns whether the token Buffer of the FU can accept a new Token
	 * @return true if token can be accepted
	 */
	public boolean readyForNewToken(){
		return !tokenAdapter.isFull();
	}
	
	public boolean tokenQueueFull(){
		return tokenAdapter.isFull();
	}
	
	public void operandAck() {
		for(int i = 0; i < inputValid.length; i++){
			inputValid[i] = false;
		}
		tokenAdapter.nextToken();
	}
	/**
	 * Describing the current functional unit state
	 * @author jung
	 *
	 */
	enum State{
		BUSY,
		SENDING,
		SENDING0,
		IDLE,
		IDLE0
	}
	
	/**
	 * Counts the steps that the FU has been busy processing the current token. 
	 * Used to ease the simulation effort. (Example.: IALU FU only calculates the division of two numbers.
	 * with the help of count we can easily simulate a Division that takes x clock cycles without simulating the division itself (x is defined in the config file of the FU). This only works for
	 * Operations with fixed duration. Other operations like memory access have variable runtime( depending on cache hit/miss). In that case the return value of 
	 * executeOP(opcode) denotes whether the execution of the opcode is finished.)
	 */
	int count;
	
	/**
	 * Simulates one clock cycle - possibly this method has to be overwritten in the subclasses
	 * @return whether this functional unit has something to do or not
	 */
	public boolean tick(){
		boolean isReady = (currentState == State.IDLE) && !tokenValid; 
		
		if(currentState == State.SENDING){
			if(getResultAck()){
				currentState = State.IDLE;
//				for(int i = 0; i < inputValid.length; i++){
//					inputValid[i] = false;
//				}
				for(int i = 0; i < outputValid.length; i++){
					outputValid[i] = false;
				}
//				tokenAdapter.nextToken();
				setResultAck(false);
			}
		}
		State nextState = currentState;
		
		if(currentState == State.IDLE){
//			System.out.println(this + " öööö " + tokenValid + " + " + opcode);
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
					if(getResultAck()){
						nextState = State.IDLE;
						
						
						setResultAck(false);
					}
					else
						nextState = State.SENDING0;
					operandAck(); //bei FUs an verschiedenen Stellen (wann Daten annehmen und damit Einfluss auf Busbelegung)
				}
			}
		} else if(currentState == State.SENDING0){
			nextState = State.SENDING;
		}
		currentState = nextState;
		return isReady;
	}
	
	
	private String getName(){
		return name;
	}
	
	
	public double getDynamicEnergy(){
		double energy = getAdditionalEnergy();
		
		for(K op: ops.getEnumConstants()){
			energy += this.energy.get(op)* executionCount.get(op);
		}
		
		
		return energy;
	}

	/**
	 * Overwrite this method when the FU introduces additional energy consumption (eg for cache misses)
	 * @return
	 */
	public double getAdditionalEnergy() {
		return 0;
	}

	public double getStaticEnergy() {
		return staticEnergy;
	}
	
	public void resetExecutionCounter(){
		for(K op: ops.getEnumConstants()){
			executionCount.put(op, 0);
		}
	}
	
	
	//TODO include sending in packets trace

}
