package cgramodel;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import generator.Module;
import operator.Implementation;
import operator.Operator;
import target.Processor;
import util.SimpleMath;

/**
 * Model that represents a Processing Element.
 *
 * @author Dennis Wolf
 *
 */
public class PEModel implements Serializable{
	
	boolean codeConstantsInReadAddress = true;
	private Boolean controlFlow = null;
	private Boolean memoryAccessFlow = null;

	/*
	 * General properties (independent from the available operators)
	 **********************************************************************************************************************/

	
	public PEModel(){
		for(Operator op : target.Processor.Instance.getOperators()){
			Implementation imp = op.createDefaultImplementation();
			if(imp.isNative() || op.toString().contains("NOP")){
				addOperator(op);
			}
		}
	}
	
	/**
	 *
	 */
	private static final long serialVersionUID = -7090042234962425967L;
	/**
	 * Unique identifier of this {@code PE}
	 */
	private int id = -1;

	/**
	 * Get unique identifier of this {@code PE}.
	 *
	 * @return the {@link #id}
	 */
	public int getID() {
		return id;
	}

	/**
	 * Change the identifier of this {@code PE}.
	 *
	 * @param id
	 *            the new identifier
	 */
	public void setID(int id) {
		this.id = id;
	}

	/**
	 * Size of the integrated register file
	 */
	private int regfilesize = -1;

	/**
	 * Get the size of the register file of this {@code PE}.
	 *
	 * @return the register file size
	 */
	public int getRegfilesize() {
		return regfilesize;
	}

	/**
	 * Change the size of the register file of this {@code PE}.
	 *
	 * @param regfilesize
	 *            the new register file size
	 */
	public void setRegfilesize(int regfilesize) {
		this.regfilesize = regfilesize;
		finalized = false;
	}

	/**
	 * Size of the read only memory unit which is (indirectly) attached to this {@code PE}.
	 */
	private int romSize = -1;

	/**
	 * Getter for the ROM size (indirectly) attached to this {@code PE}.
	 * @return the ROM size
	 */
	public int getRomSize() {
		if (getRomAccess())
			return romSize;
		else
			return -1;
	}

	/**
	 * Setter for the ROM size if this {@code PE}. To be used during config file reading.
	 * @param romSize to set
	 */
	public void setRomSize(int romSize) {
		this.romSize = romSize;
	}

	/**
	 * Width of the address of the read only memory (indirectly) attached to this {@code PE}.
	 * Set during finalisation.
	 */
	private int romAddrWidth = -1;

	/**
	 * Get the address width of the ROM (indirectly) attached to this {@code PE}.
	 * Yields a valid value only after finalising this PE!
	 *
	 * @return the address width
	 */
	public int getRomAddrWidth() {
		return romAddrWidth;
	}

	/**
	 * Switch for exporting the second ALU operand as output of this {@code PE}.
	 */
	private boolean liveout = true;
	
	/**
	 * Switch for importing data from the AMIDAR bus
	 */
	private boolean livein = true;

	/**
	 * Check, whether second ALU operand is promoted to the outputs of this
	 * {@code PE}.
	 *
	 * @return the {@link #liveout} flag
	 */
	public boolean getLiveout() {
		return liveout;
	}

	/**
	 * Change the request for exporting the second ALU operand as output of this
	 * {@code PE}.
	 *
	 * @param liveout
	 *            the new request status
	 */
	public void setLiveout(boolean liveout) {
		this.liveout = liveout;
		finalized = false;
	}
	
	/**
	 * Check, whether the PE can load data directly from the AMIDAR bus.
	 * 
	 * @return the {@link #livein} flag
	 */
	public boolean getLivein() {
		return livein;
	}

	/**
	 * Change whether the pe can load data directly from the AMIDAR bus.
	 * 
	 * @param liveout the new request status
	 */
	public void setLivein(boolean livein) {
		this.livein = livein;
		finalized = false;
	}

	private ContextMaskPE contextmask = new ContextMaskPE();

	public ContextMaskPE getContext() {
		return contextmask;
	}

	public int getContextWidth() {
		return contextmask.getContextWidth();
	}

	public void setContextMask(ContextMaskPE contextmask) {
		this.contextmask = contextmask;
	}

	public ContextMaskPE getContextMaskPE() {
		return contextmask;
	}

	/**
	 * List of {@code PE}s whose outputs are readable by this {@code PE}.
	 */
	private List<PEModel> inputs = new ArrayList<PEModel>();

	/**
	 * Add a {@code PE} to the sources of this {@code PE}.
	 *
	 * @param pe
	 *            the source to add
	 */
	public void addPE2inputs(PEModel pe) {
		if(!inputs.contains(pe)&&(!pe.equals(this))){
			inputs.add(pe);
			finalized = false; 
		}
	}

	/**
	 * Remove a {@code PE} from the sources of this {@code PE}.
	 *
	 * @param pe
	 *            the source to remove
	 */
	public void removePEFromInputs(PEModel pe) {
		inputs.remove(pe);
		finalized = false;
	}

	/**
	 * Get the list of {@code PE}s whose outputs are readable by this {@code PE}
	 * .
	 *
	 * @return the {@link #inputs}
	 */
	public List<PEModel> getInputs() {
		return inputs;
	}

	/**
	 * Overwrite the list of {@code PE}s whose outputs are readable by this
	 * {@code PE}.
	 *
	 * @param inputs
	 *            the new list
	 */
	public void setInputs(List<PEModel> inputs) {
		this.inputs = inputs;
		finalized = false;
	}

	/**
	 * Mapping of the {@code Operator}s associated with this {@code PE} to their
	 * {@code Implementation}s.
	 */
	private EnumMap<? extends Operator, Implementation> availableOperators = Processor.Instance
			.createEmptyOperatorMap();

	/**
	 * Associate an {@code Operator} with this {@code PE} using its default
	 * {@code Implementation}.
	 *
	 * @param operator
	 *            the {@link Operator} to add
	 */
	public void addOperator(Operator operator) {
		addOperator(operator, operator.createDefaultImplementation());
	}

	/**
	 * Associate an {@code Operator} with this {@code PE} using a certain
	 * {@code Implementation}.
	 *
	 * @param operator
	 *            the {@link Operator} to add
	 * @param implementation
	 *            the {@link Implementation} to associate with the
	 *            {@code operator}
	 */
	public void addOperator(Operator operator, Implementation implementation) {
		if (implementation == null) {
			System.err.println("Tried to add operator to PE with null ptr impl " + operator.toString());
			return;
		}
		
		Processor.Instance.add(availableOperators, operator, implementation);
		if(!implementation.isNative()){
			Processor.Instance.add(availableNonNativeOperators, operator, implementation);
		}
		finalized = false;
	}

	/**
	 * Remove an associated {@code Operator} from this {@code PE}.
	 *
	 * @param operator
	 */
	public void removeOperation(Operator operator) {
		if(!availableNonNativeOperators.get(operator).isNative()){
			availableOperators.remove(operator);
			availableNonNativeOperators.remove(operator);
			finalized = false;
		}
	}

	/**
	 * Get a list of the {@code Implementation}s for all {@code Operator}s
	 * associated with this {@code PE}.
	 *
	 * @return list of {@link Implementation}s
	 */
	public Collection<Implementation> getAvailableOperatorImplementations() {
		return availableOperators.values();
	}

	/**
	 * Get a map from all {@code Operator}s associated with this {@code PE} to
	 * their {@code Implementation}s.
	 *
	 * @return {@link #availableOperators}
	 */
	public EnumMap<? extends Operator, Implementation> getAvailableOperators() {
		return availableOperators;
	}

	/*
	 * Dynamic properties (depending on properties of availableOperations), that
	 * may be overwritten (by setter methods) or finalized (i.e., fixed to avoid
	 * often recalculations)
	 **********************************************************************************************************************/

	/**
	 * Check, whether at least one associated {@code Operator} generates a
	 * status signal influencing the control flow.
	 *
	 * @return the control flow information
	 */
	public boolean getControlFlow() {
		if (controlFlow != null && finalized) {
			return controlFlow;
		}
		for (Implementation imp : getAvailableNonNativeOperatorImplementations()) {
			if (imp.isControlFlow()) {
				controlFlow = true;
				return true;
			}
		}
		controlFlow = false;
		return false;
	}


	/**
	 * Check, whether at least one associated {@code Operator} generates memory accessing signals.
	 * TODO: cache access only? or also ROM?
	 * @return the memory access information
	 */
	public boolean getMemAccess() {
		for (Implementation imp : getAvailableNonNativeOperatorImplementations()) {
			if (imp.isMemAccess()) {
				return true;
			}
		}
		return false;
	}
	
	public boolean getCacheAccess() {
    for (Implementation imp : getAvailableNonNativeOperatorImplementations()) {
      if (imp.isCacheAccess()) {
        return true;
      }
    }
    return false;
  }
	
	public boolean getRomAccess() {
	    for (Implementation imp : getAvailableNonNativeOperatorImplementations()) {
	      if (imp.isRomAccess()) {
	        return true;
	      }
	    }
	    return false;
	  }
	
	public boolean getArrayMemAccess() {
		for (Implementation imp : getAvailableNonNativeOperatorImplementations()) {
			if (imp.isIndexedMemAccess()) {
				return true;
			}
		}
		return false;
	}
	
	public boolean getPrefetchCacheAccess() {
		for (Implementation imp : getAvailableNonNativeOperatorImplementations()) {
			if (imp.isCachePrefetch()) {
				return true;
			}
		}
		return false;
	}
	
	public boolean getWideMemAccess(){
		for (Implementation imp : getAvailableNonNativeOperatorImplementations()) {
			if (imp.getMemAccessWords() > 1) {
				return true;
			}
		}
		return false;
	}
	
	public int getWidthOfWideMemoryAccess() {
		// TODO ...
	  int maxMemAccessWords = 0;
	  for (Implementation imp : getAvailableNonNativeOperatorImplementations()) {
      maxMemAccessWords = Math.max(maxMemAccessWords, imp.getMemAccessWords());
    }
	  return maxMemAccessWords-1;
	}
	
	

	/**
	 * Set after {@code finalize} if at least one associated {@code Operator} is
	 * executed sequentially.
	 */
	private Boolean multiCycle = null;

	/**
	 * Check, whether at least one {@code availableOperation} is executed
	 * sequentially.
	 *
	 * @return the {@link #multiCycle} status
	 */
	public boolean getMultiCycle() {
		if (multiCycle != null) {
			return multiCycle;
		}
		for (Implementation imp : getAvailableNonNativeOperatorImplementations()) {
			if (imp.isMultiCycle()) {
				multiCycle = true;
				return true;
			}
		}
		return false;
	}

	private Collection<Implementation> getAvailableNonNativeOperatorImplementations() {
		return availableNonNativeOperators.values();
	}

	/**
	 * Set to largest result port width of any {@code Operator} associated with
	 * this {@code PE} after {@code finalize}.
	 */
	private Integer maxWidthResult = null;

	/**
	 * Get largest result port width of any {@code Operator} associated with
	 * this {@code PE}.
	 *
	 * @return
	 */
	public int getMaxWidthResult() {
		if (maxWidthResult != null) {
			return maxWidthResult;
		}
		int res = 0;
		for (Implementation imp : getAvailableOperatorImplementations()) {
			if (imp.getNumberOfResults() > 0) {
				res = Math.max(res, imp.getResultPortWidth());
			}
		}
		return res;
	}

	/**
	 * Set to largest operandA port width of any {@code Operator} associated
	 * with this {@code PE} after {@code finalize}.
	 */
	private Integer[] maxWidthInput = new Integer[] { null, null }; 

	/**
	 * Get largest operand port width of any {@code Operator} associated with
	 * this {@code PE}.
	 *
	 * @param portIndex
	 *            the index of the operand of interest (0 or 1)
	 * @return largest operand port width
	 */
	private int getMaxWidthInput(int portIndex) {
		if (maxWidthInput[portIndex] != null) {
			return maxWidthInput[portIndex];
		}
		int res = 0;
		for (Implementation imp : getAvailableOperatorImplementations()) {
			if (imp.getNumberOfOperands() > portIndex) {
				res = Math.max(res, imp.getOperandPortWidth(portIndex));
			}
		}
		return res;
	}

	/**
	 * Get largest operandA port width of any {@code Operator} associated with
	 * this {@code PE}.
	 *
	 * @return largest operandA port width
	 */
	public int getMaxWidthInputA() {
		return getMaxWidthInput(0);
	}

	/**
	 * Get largest operandB port width of any {@code Operator} associated with
	 * this {@code PE}.
	 *
	 * @return largest operandB port width
	 */
	public int getMaxWidthInputB() {
		return getMaxWidthInput(1);
	}

	/**
	 * Non-native subset of {@code availableOperators}
	 */
	private EnumMap<? extends Operator, Implementation> availableNonNativeOperators = Processor.Instance
			.createEmptyOperatorMap();

	/**
	 * Get the non-native subset of {@code availableOperators}. Not working !!!!!
	 *
	 * @return non-native {@code Operator}s
	 */
	
	public EnumMap<? extends Operator, Implementation> getAvailableNonNativeOperators() {
		if (availableNonNativeOperators != null) {
			return availableNonNativeOperators;
		}
		EnumMap<? extends Operator, Implementation> res = Processor.Instance.createEmptyOperatorMap();
		for (Entry<? extends Operator, Implementation> e : availableOperators.entrySet()) {
			if (!e.getValue().isNative()) {
				Processor.Instance.add(res, e.getKey(), e.getValue());
			}
		}
		return res;
	}

	/*
	 * Prepare PE for code generation
	 **********************************************************************************************************************/

	/**
	 * Parse configuration for this {@code PE} details from a JSON file.
	 *
	 * @param configFile
	 *            the path to the JSON file
	 * @param id
	 *            the unique identifier to be used for this {@code PE}
	 * @return the configured size of the register file
	 */
	public int configure(String configFile, int id) {

		JSONParser parser = new JSONParser();
		JSONObject json = null;
		FileReader fileReader = null;
		setID(id);
		try {
			fileReader = new FileReader(configFile);
			json = (JSONObject) parser.parse(fileReader);
		} catch (FileNotFoundException e) {
			System.err.println("No config file found for FU " + this.getClass() + ": \"" + configFile + "\"");
			e.printStackTrace(System.err);
		} catch (IOException e) {
			System.err.println("I/O error while reading config file for FU" + this.getClass());
			e.printStackTrace(System.err);
		} catch (ParseException e) {
			System.err.println("Parse error while reading config file for FU" + this.getClass());
			e.printStackTrace(System.err);
		}

		// Item read from the JSON file
		Object config;

		// check, which of the operators supported by the target processor are
		// included in the JSON configuration of this PE
		for (Operator operator : Processor.Instance.getOperators()) {
			config = json.get(operator.toString());
			
			
//			// workaround: CMP operator is mimicked by IFEQ (TODO: is this correct?)
//			if (config == null && operator.toString().equals("IFEQ")) {
//				config = json.get("CMP");
//			}

			// still nothing found in the JSON file => operator not requested by
			// JSON
			if (config == null) {
				continue;
			}
			// start with a default implementation
			Implementation implementation = operator.createDefaultImplementation();

			// the operator might not yet be implemented
			if (implementation == null) {
				System.err.println(
						"Operator " + operator + " requested for " + json.get("name") + ", but not yet implemented");
				continue;
			}
			// apply the JSON configurations
			implementation.configure(config);

			// store mapping from the operator to its implementation
			addOperator(operator, implementation);
		}

		BiConsumer<Object, Consumer<Object>> runIfNotNull = (obj , fn) -> {
			if (obj != null)
				fn.accept(obj);
		};

		// TODO: name defined in JSON is not used
		setRegfilesize((int) (long) json.get("Regfile_size"));
		runIfNotNull.accept(json.get("rom_size"), (obj) -> setRomSize((int) (long) obj));

		try {
			fileReader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return getRegfilesize();
	}

	/**
	 * Assign opcode to each {@code Operator}.
	 * <p>
	 * This method should be called after all {@code Operators} were added to
	 * this {@code PE} but before the Verilog code is generated.
	 * <p>
	 * Currently, a binary block code is generated for minimum opcode width.
	 * TODO: try one-hot coding, if resulting context width still fits in BRAM.
	 */
	public void organizeOpcode() {
		int i = 0;
		for (Implementation implementation : getAvailableNonNativeOperators().values()) {
			implementation.setOpcode(i);
			i++;
		}
	}
	
	private boolean finalized = false;

	/**
	 * Prepare code generation.
	 * <p>
	 * This method should be called after all {@code Operators} were added to
	 * this {@code PE} but before the Verilog code is generated.
	 * <p>
	 * Fixes all {@link Operator}-specific dynamic properties and creates the
	 * context mask and the opcodes.
	 */
	public void finalizePE(CgraModel model) {
		availableNonNativeOperators = getAvailableNonNativeOperators();
		multiCycle = getMultiCycle();
		maxWidthResult = getMaxWidthResult();

		if (romSize > 0)
			romAddrWidth = SimpleMath.checkedLog(romSize);

		getContext().createMask(this,model);
		organizeOpcode();
		finalized = true;
	}
	

	public boolean isFinalized() {
		return finalized;
	}

	public HashSet<String> equalsInAttributes(Object other) {
		if (!(other instanceof PEModel)) {
			return new HashSet<String>(){{
			    add("No Instance of another");
			}};
		} else {
			HashSet<String> diff = new HashSet<String>();
			PEModel model = (PEModel) other;
			
			if(id != model.getID())diff.add("PE " +id + " id" );
			if(getRegfilesize() != model.getRegfilesize())diff.add("PE " +id + " Regfilesize" );
			if(getLiveout() != model.getLiveout())diff.add("PE " +id + " Liveout" );
			if(getContextWidth() != model.getContextWidth())diff.add("PE " +id + " ContextWidth" );
			if(getAvailableOperators().size() != model.getAvailableOperators().size())diff.add("PE " +id + " AvailableOperator size" );
			if(getControlFlow() != model.getControlFlow())diff.add("PE " +id + " ControlFlow" );
			if(getMemAccess() != model.getMemAccess())diff.add("PE " +id + " MemAccess" );
			if(getMultiCycle() != model.getMultiCycle())diff.add("PE " +id + " MultiCycle" );

			for (Operator operator : getAvailableNonNativeOperators().keySet()) {
				if(model.getAvailableNonNativeOperators().containsKey(operator)){
					if(getAvailableNonNativeOperators().get(operator).getLatency() != model.getAvailableNonNativeOperators().get(operator).getLatency()){
						diff.add("PE " +id + " " + operator.toString() + " latency");
					}
					if(getAvailableNonNativeOperators().get(operator).getInputLatency() != model.getAvailableNonNativeOperators().get(operator).getInputLatency()){
						diff.add("PE " +id + " " + operator.toString() + " InputLatency");
					}
				}
				else{
					diff.add("PE " +id + " " + operator.toString() + " not available");
				}
			}
			if(getInputs().size() != model.getInputs().size()) diff.add("input size");
			Set<Integer> inputs = new LinkedHashSet<>();
			for (PEModel input : getInputs()) {
				inputs.add(input.getID());
			}
			Set<Integer> otherInputs = new LinkedHashSet<>();
			for (PEModel input : model.getInputs()) {
				otherInputs.add(input.getID());
			}
			if(inputs.size() != otherInputs.size()) diff.add("PE " +id + " inputsize");
			if(!inputs.containsAll(otherInputs)) diff.add("PE " +id + " inputs not equal");
			return diff;
		}
	}
	
	public boolean codeConstantsInReadAddress(){
		return codeConstantsInReadAddress && (regfilesize > 1);
	}
	
}
