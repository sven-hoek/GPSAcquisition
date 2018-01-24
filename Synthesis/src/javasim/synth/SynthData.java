package javasim.synth;


import java.util.Formatter;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;

import javasim.synth.HardGen.MethodID;
import javasim.synth.model.CGRAIntrinsics;
import javasim.synth.model.DataGraph;
import javasim.synth.model.I;
import javasim.synth.model.LoopGraph;
import javasim.synth.model.datum.ConstDatum;
import javasim.synth.model.datum.Datum;
import javasim.synth.model.datum.LWriteDatum;
import javasim.synth.model.instruction.AbstractIF;
import javasim.synth.model.instruction.Instruction;
import javasim.synth.model.instruction.StartInstr;
import javasim.synth.model.instruction.StopInstr;

import java.util.TreeSet;

import scheduler.RCListSched.AliasingSpeculation;
import dataContainer.MethodDescriptor;

/**
 * This class holds the synthesis context and interfaces to external virtual
 * machine components such as the contant pool. This data meant is to be set up
 * by the hardware generator and is welded by the I instructions.
 * 
 * @see HardGen
 * @see I
 * @see InstrGraph
 * @see DataGraph
 * @author Michael Raitza
 * @version 15.04.2011
 */
public class SynthData {
	
	/**
	 * decides whether CSE is performed
	 */
	private boolean CSE;
	
	private boolean CONSTANT_FOLDING;
	
	private AliasingSpeculation aliasSpeculation;

	/*
	 * stack holding the next instruction to handle when building one of the
	 * graphs
	 */
	private LinkedList<Instruction> todo;

	/* the code sequence */
	private Short[] code;
	private Short[] lVarOffset;
	private LinkedHashMap<Short, HardGen.MethodID> inlinedMethod; // identify the inlined method with   - maps localVar offset to MethodID
	
	private LinkedHashMap<Integer,LinkedHashSet<Instruction>> returns;
	private LinkedHashMap<Integer,Instruction> finalReturns;
	
	private int freeLVID = 8000; // TODO find real starting point

	/* the address of the current instruction */
	private Integer pos;

	/* the control flow graph */
	private InstrGraph g;

	/* the data flow graph */
	private DataGraph dg;

	/* the loop hierarchy tree */
	private LoopGraph lg;

	private StartInstr start;
	private Instruction stop;
	private Instruction self;
	private LinkedHashSet<Integer> lvar_read, lvar_write;
	private LinkedHashMap<Integer,TreeSet<Integer>>  lv_read, lv_write;	//storing the read and write accesses of local variables
	private LinkedHashMap<Integer,TreeSet<ArrayAccessStorage>> a_read_direct, a_write_direct, a_read_indirect, a_write_indirect; // stores read and write accesses of arrays. Direct: a[i] indirect a[f(i)]
	private Map<Long, Integer> readers, writers, tokens, awriters, areaders,
			atokens, sreaders, swriters, stokens;
	
	 MethodDescriptor[] methodTable;
	
	
	private LinkedHashSet<LWriteDatum> allLVStores; // Needed for folding - see Datagraph.foldLVMemInstructions()
	
	private LinkedHashMap<Datum, LinkedHashSet<Datum>> chains;
	
	
	private LinkedHashMap<Datum, LinkedHashSet<Datum>> potentialAliases;


	private CGRAIntrinsics intrinsics;
	
	private int lVarCount;

	/**
	 * Constructs a new synthesis context.
	 * 
	 * @param code
	 *            the byte code array to synthesise
	 * @param start
	 *            the start instruction
	 * @param stop
	 *            the stop instruction
	 * @param ci
	 *            the index into the class
	 */
	public SynthData(Short[] code, Short [] lVarOffset, StartInstr start, StopInstr stop, int lVarCount, LinkedHashMap<Short,MethodID> inlinedMethods, MethodDescriptor[] methodTable, boolean CSE, AliasingSpeculation aliasSpeculation, boolean CONSTANT_FOLDING, CGRAIntrinsics intrinsics) {
		todo = new LinkedList<Instruction>();

		this.lVarCount = lVarCount;
		this.code = code;
		this.lVarOffset = lVarOffset;
		this.inlinedMethod =inlinedMethods;
		this.start = start;
		this.stop = stop;
		this.methodTable = methodTable;

		self = start;
		pos = start.addr();

		g = new InstrGraph();
		g.insert(start);
		dg = new DataGraph();
		lg = new LoopGraph();
		lvar_read = new LinkedHashSet<Integer>();
		lvar_write = new LinkedHashSet<Integer>();
		a_read_direct = new LinkedHashMap<Integer, TreeSet<ArrayAccessStorage>>();
		a_write_direct = new LinkedHashMap<Integer, TreeSet<ArrayAccessStorage>>();
		a_read_indirect = new LinkedHashMap<Integer, TreeSet<ArrayAccessStorage>>();
		a_write_indirect = new LinkedHashMap<Integer, TreeSet<ArrayAccessStorage>>();
		lv_read = new LinkedHashMap<Integer, TreeSet<Integer>>();
		lv_write = new LinkedHashMap<Integer, TreeSet<Integer>>();

		readers = new LinkedHashMap<Long, Integer>();
		writers = new LinkedHashMap<Long, Integer>();
		tokens = new LinkedHashMap<Long, Integer>();
		atokens = new LinkedHashMap<Long, Integer>();
		awriters = new LinkedHashMap<Long, Integer>();
		areaders = new LinkedHashMap<Long, Integer>();
		stokens = new LinkedHashMap<Long, Integer>();
		swriters = new LinkedHashMap<Long, Integer>();
		sreaders = new LinkedHashMap<Long, Integer>();
		
		allLVStores = new LinkedHashSet<LWriteDatum>();
		
		potentialAliases = new LinkedHashMap();
		
		lvStoreRegister = new LinkedHashMap<>();
		putFieldRegister = new LinkedHashMap<>();

		redirectRef = new LinkedHashMap<Integer,Integer>();
		
		returns = new LinkedHashMap<Integer,LinkedHashSet<Instruction>>();
		finalReturns = new LinkedHashMap<Integer,Instruction>();
		
		this.CSE = CSE;
		this.CONSTANT_FOLDING = CONSTANT_FOLDING;
		this.aliasSpeculation = aliasSpeculation;
		
		this.intrinsics = intrinsics;
	}

	/**
	 * Returns the byte code value addressed by the eight bit address
	 * <code>c</code>.
	 * 
	 * @param c
	 *            the eight bit address to return the byte code for
	 * @return the byte code at address <code>c</code>
	 */
	public Integer code(Number c) {
		return new Integer((short) code[c.intValue()]);
	}

	/**
	 * Returns the byte code value addressed by the 16 bit address
	 * <code>c</code>.
	 * 
	 * @param c
	 *            the 16 bit address to return the byte code for
	 * @return the byte code at address <code>c</code>
	 */
	public Integer code_w(Number c) {
		return (new Short(
				(short) ((code[c.intValue() + 1]&0xFF) | ((code[c.intValue()]&0xFF) << 8))))
				.intValue();
	}

	public void patch_code(Integer addr, short new_code) {
		code[addr] = new_code;
	}

	/**
	 * Pushes the instruction <code>i</code> onto the graph construction stack.
	 * 
	 * @param i
	 *            the instruction to push onto the stack
	 */
	public void push(Instruction i) {
		if (!g.contains(i))
			todo.push(i);
	}

	/**
	 * Pushes the instruction <code>i</code> onto the graph construction stack.
	 * 
	 * @param i
	 *            the instruction to push onto the stack
	 */
	public void pushd(Instruction i) {
		todo.push(i);
	}

	/**
	 * Returns the next instruction to construct the graph for, or null if there
	 * is nothing left to do.
	 * 
	 * @return the next instruction, or NULL if there is no such instruction.
	 */
	public Instruction update() {
		try {
			self = todo.pop();
			pos = self.addr();
			return self;
		} catch (NoSuchElementException e) {
			return null;
		}
	}

	/**
	 * Returns the position of the current instruction.
	 * 
	 * @return the position (address) of the current instruction.
	 */
	public Integer pos() {
		return pos;
	}

	/**
	 * Sets the position of the current instruction. Needed for initialization.
	 * FIXME: Look if we can avoid this.
	 */
	public void pos(Integer pos) {
		this.pos = pos;
	}

	/**
	 * Returns the instruction graph.
	 * @return the instruction graph
	 */
	public InstrGraph get_graph() {
		return g;
	}

	/**
	 * Returns the data graph.
	 * @return the data graph
	 */
	public DataGraph get_data_graph() {
		return dg;
	}

	public void dg_init() {
		dg = new DataGraph();
	}

	/**
	 * Returns the instruction graph.
	 * @return the instruction graph
	 */
	public InstrGraph ig() {
		return g;
	}

	/**
	 * Returns the data graph.
	 * @return the data graph
	 */
	public DataGraph dg() {
		return dg;
	}

	/**
	 * Returns the loop graph.
	 * @return the loop graph
	 */
	public LoopGraph lg() {
		return lg;
	}

	/**
	 * Returns the stop node.
	 */
	public Instruction stop() {
		return stop;
	}

	/**
	 * Returns the address of the last instruction of the byte code sequence.
	 * @return the address of the last instruction of the byte code sequence.
	 */
	public Integer stop_addr() {
		return stop.addr();
	}

	/**
	 * Returns the start pseudo instruction.
	 */
	public Instruction start() {
		return start;
	}

	/**
	 * Returns the address of the start pseudo instruction.
	 */
	public Integer start_addr() {
		return start.addr();
	}

	public void lvar_read(Integer v) {
		lvar_read.add(v);
	}

	public void lvar_write(Integer v) {
		lvar_write.add(v);
	}

	public LinkedHashSet<Integer> lvar_read() {
		return lvar_read;
	}

	public LinkedHashSet<Integer> lvar_write() {
		return lvar_write;
	}
	
	/**
	 * Adds a read access of a local variable to the list
	 * @param v		the local variable id
	 * @param addr the address of the load instruction
	 */
	public void lv_read(Integer v, Integer addr) {
		TreeSet<Integer> addrs;
		if (lv_read.containsKey(v)){
			addrs = lv_read.get(v);
		} else {
			addrs = new TreeSet<Integer>();
			lv_read.put(v, addrs);
		}
		addrs.add(addr);

	}

	/**
	 * Adds a write access of a local variable to the list
	 * @param v		the local variable id
	 * @param addr the address of the store instruction
	 */
	public void lv_write(Integer v, Integer addr) {
		TreeSet<Integer> addrs;
		if (lv_write.containsKey(v)){
			addrs = lv_write.get(v);
		} else {
			addrs = new TreeSet<Integer>();
			lv_write.put(v, addrs);
		}
		addrs.add(addr);
	}
	
	/**
	 * Adds a read access of an array to the lists. There are two lists - direct and indirect. Direct access means a[i]
	 * As i is not known at call time, it is assumed to be direct if the command before the array load instruction (ArrayLdInst) is a load instruction.
	 * This assumption has to be checked afterwards with the method checkIndexAccess()
	 * @param a		The array id
	 * @param p		true if the command before is a load instruction
	 * @param addr	the address of the access instruction
	 * @param indexVal	the identifier of the index variable (needed to check if its truly direct)
	 */
	public void a_read(Integer a, Boolean p, Integer addr, Number indexVal){
		LinkedHashMap<Integer, TreeSet<ArrayAccessStorage>> curr = p ? a_read_direct : a_read_indirect;
		TreeSet<ArrayAccessStorage> addrs;
		if(!curr.containsKey(a)){
			addrs = new TreeSet<ArrayAccessStorage>();
			addrs.add(new ArrayAccessStorage(p,addr,indexVal.intValue()));
			curr.put(a, addrs);
		} else {
			addrs = curr.get(a);
			addrs.add(new ArrayAccessStorage(p,addr,indexVal.intValue()));
		}
	}
	
	/**
	 * Adds a write access of an array to the lists. There are two lists - direct and indirect. Direct access means a[i]
	 * As i is not known at call time, it is assumed to be direct if the command before the array store instruction (ArrayStInst) is a load instruction.
	 * This assumption has to be checked afterwards with the method checkIndexAccess()
	 * @param a		The array id
	 * @param p		true if the command before is a load instruction
	 * @param addr	the address of the access instruction
	 * @param indexVal	the identifier of the index variable (needed to check if its truly direct)
	 */
	public void a_write(Integer a, Boolean p, Integer addr, Number indexVal){
		LinkedHashMap<Integer, TreeSet<ArrayAccessStorage>> curr = p ? a_write_direct : a_write_indirect;
		TreeSet<ArrayAccessStorage> addrs;
		if(!curr.containsKey(a)){
			addrs = new TreeSet<ArrayAccessStorage>();
			addrs.add(new ArrayAccessStorage(p,addr,indexVal.intValue()));
			curr.put(a, addrs);
		} else {
			addrs = curr.get(a);
			addrs.add(new ArrayAccessStorage(p,addr,indexVal.intValue()));
		}
	}


	public boolean t_read(Integer varnum, Long tag) {
		if (!readers.containsValue(varnum)) {
			readers.put(tag, varnum);
			tokens.put(tag, varnum);
			return true;
		}
		return false;
	}

	public boolean t_write(Integer varnum, Long tag) {
		if (!writers.containsValue(varnum)) {
			writers.put(tag, varnum);
			tokens.put(tag, varnum);
			return true;
		}
		return false;
	}

	public boolean a_read(Integer varnum, Long tag) {
		if (!areaders.containsValue(varnum)) {
			areaders.put(tag, varnum);
			atokens.put(tag, varnum);
			return true;
		}
		return false;
	}

	public boolean a_read(Integer varnum) {
		return !areaders.containsValue(varnum);
	}

	public boolean a_write(Integer varnum, Long tag) {
		if (!awriters.containsValue(varnum)) {
			awriters.put(tag, varnum);
			atokens.put(tag, varnum);
			return true;
		}
		return false;
	}

	public boolean a_write(Integer varnum) {
		return !awriters.containsValue(varnum);
	}

	public boolean s_read(Integer addr, Long tag) {
		if (!sreaders.containsValue(addr)) {
			sreaders.put(tag, addr);
			stokens.put(tag, addr);
			return true;
		}
		return false;
	}

	public boolean s_write(Integer addr, Long tag) {
		if (!swriters.containsValue(addr)) {
			swriters.put(tag, addr);
			stokens.put(tag, addr);
			return true;
		}
		return false;
	}

	public boolean is_reader(Integer varnum) {
		return readers.containsValue(varnum);
	}

	public Map<Long, Integer> tokens() {
		return tokens;
	}

	public Map<Long, Integer> atokens() {
		return atokens;
	}

	public Map<Long, Integer> stokens() {
		return stokens;
	}


	/* for debugging purposes */
	public String print_tokens() {
		Formatter f = new Formatter();
		try {
			f.format("\nTokens for reading local vars\n");
			for (Long i : readers.keySet())
				f.format("  %2d → %10d\n", i, readers.get(i));

			f.format("Tokens for reading static vars\n");
			for (Long i : sreaders.keySet())
				f.format("  %2d → %10d\n", i, sreaders.get(i));

			f.format("Tokens for reading objects\n");
			for (Long i : areaders.keySet())
				f.format("  %2d → %10d\n", i, areaders.get(i));

			f.format("Tokens for writing local vars\n");
			for (Long i : writers.keySet())
				f.format("  %2d → %10d\n", i, writers.get(i));

			f.format("Tokens for writing static vars\n");
			for (Long i : swriters.keySet())
				f.format("  %2d → %10d\n", i, swriters.get(i));

			f.format("Tokens for writing objects\n");
			for (Long i : awriters.keySet())
				f.format("  %2d → %10d\n", i, awriters.get(i));
			f.format("\n");
			return f.toString();
		} finally {
			f.close();
		}
	}

	/**
	 * Adds profile information to the loop structures.
	 */
	public void loop_profile(AbstractIF ifinstr, Integer start, Integer stop) {
		lg.insert(new LoopGraph.Loop(ifinstr, start, stop));
		ifinstr.set_loopcontroller();
	}
	
	public void addLVStore(LWriteDatum lwr){
		allLVStores.add(lwr);
	}

	public void removeLVStore(LWriteDatum lwr){
		allLVStores.remove(lwr);
	}
	
	public LinkedHashSet<LWriteDatum> getAllLVStores(){
		return allLVStores;
	}
	
	
	public Short getLVarOffset(int addr){
		return lVarOffset[addr];
	}
	
	public boolean isLocal(int addr){
		if(addr> lVarOffset.length){ //manually created local vars - created for merging in PHIInstr
//			System.out.println("AADDR: "+addr);
			return false;
		}
		boolean ret = lVarOffset[addr] < lVarCount;
//		System.out.println(addr+"\t"+lVarOffset[addr]+"\t"+ret);
		return ret;
	}
	
	public void setInlinedMethods(LinkedHashMap<Short,HardGen.MethodID> inlinedMethods){
		this.inlinedMethod = inlinedMethods;
	}
	
	public int getClassIndexOfInlinedMethod(int addr){
		return inlinedMethod.get((short)(lVarOffset[addr+3]%lVarCount)).expectedCi;
	}

	LinkedHashMap<Integer,Integer> redirectRef;
	
	
	public void addRedirect(Integer key, Integer value){
		redirectRef.put(key, value);
		System.out.println(redirectRef);
	}
	
	public Number redirect(Number value){
		Number newValue =  redirectRef.get(value);
		if( newValue != null){
			return newValue;
		}
		return value;
		
	}
	
	public void addChain(Datum d, LinkedHashSet<Datum> chain){
		chains.put(d, chain);
	}
	
	public void removeChain(Datum d){
		chains.remove(d);
	}
	
	public void addReturn(Instruction ret){
//		System.out.println("ADDING RET: "+ret);
		
		LinkedHashSet<Instruction> mRet = returns.get(this.getLVarOffset(ret.addr()));
		if(mRet == null){
			mRet = new LinkedHashSet<Instruction>();
			returns.put(new Integer(this.getLVarOffset(ret.addr())), mRet);
		}
		mRet.add(ret);
		
		Instruction jumpBack = finalReturns.get(new Integer(this.getLVarOffset(ret.addr())));
		
		if(jumpBack != null){
			ig().insert(ret, jumpBack);
		}
				
		
	}
	
	public LinkedHashSet<Instruction> getReturns(Instruction i){
		LinkedHashSet<Instruction> ret = returns.get(this.getLVarOffset(i.addr()).intValue());
		return ret; 
	}

	public void addReturn(Instruction returnInstr, Instruction jumpBack) {
		finalReturns.put(new Integer(this.getLVarOffset(returnInstr.addr())),	jumpBack);
		
		LinkedHashSet<Instruction> mRet = returns.get(this.getLVarOffset(returnInstr.addr()));
		if(mRet == null){
			mRet = new LinkedHashSet<Instruction>();
			returns.put(new Integer(this.getLVarOffset(returnInstr.addr())), mRet);
		}
		mRet.add(returnInstr);
		
		for(Instruction ret: mRet){
			ig().insert(ret, jumpBack);
		}
		
		
		
	}
	
	public MethodDescriptor[] getMethodTable(){
		return methodTable;
	}
	
	public int getLVarCount(){
		return lVarCount;
	}
	
	public void addPotentialAliases(Datum d1, Datum d2){
		Datum first, second;
		
		if(d1.creator().addr() < d2.creator().addr()){
			first = d1;
			second = d2;
		} else {
			first = d2;
			second = d1;
		}
		LinkedHashSet<Datum> seconds = potentialAliases.get(first);
		if(seconds == null){
			seconds = new LinkedHashSet<>();
			potentialAliases.put(first, seconds);
		}
		seconds.add(second);
		
	}

	public LinkedHashMap<Datum, LinkedHashSet<Datum>> getPotentialAliases() {
		return potentialAliases;
	}
	
	public boolean CSE() {
		return CSE;
	}
	
	public boolean CONSTANT_FOLDING(){
		return CONSTANT_FOLDING;
	}
	
	public AliasingSpeculation getAliasSpeculation(){
		return aliasSpeculation;
	}
	
	public int getFreeLVID(){
		return freeLVID++;
	}
	
	public boolean kownIntrinsic(int id){
		return intrinsics.isKnown(id);
	}
	
	public int numberOfOperandsOfIntrinsic(int id){
		return intrinsics.nrOfOperands(id);
	}
	
	public I getIntrinsicInstruction(int id){
		return intrinsics.getInstruction(id);
	}

	private LinkedHashMap<Integer, Integer> lvStoreRegister;
	
	public void regLVSTore(Integer v) {
		Integer nrOfStores = lvStoreRegister.get(v);
		if(nrOfStores == null){
			nrOfStores = 1;
		} else {
			nrOfStores++;
		}
		lvStoreRegister.put(v, nrOfStores);
	}
	
	public boolean isSingleLVStore(Integer v){
		return lvStoreRegister.get(v).intValue() == 1;
	}
	
	
	private LinkedHashMap<Integer, Integer> putFieldRegister;
	
	public void regPutField(Integer v){
		Integer nrOfPutFields = putFieldRegister.get(v);
		if(nrOfPutFields == null){
			nrOfPutFields = 1;
		} else {
			nrOfPutFields++;
		}
		putFieldRegister.put(v, nrOfPutFields);
	}
	
	public boolean isSinglePutField(Integer v){
		return putFieldRegister.get(v).intValue() == 1;
	}
}
