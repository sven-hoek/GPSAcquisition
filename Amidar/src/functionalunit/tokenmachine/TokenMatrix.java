package functionalunit.tokenmachine;

import functionalunit.AcceleratorCore;
import functionalunit.CGRA;
import functionalunit.FALU;
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
import functionalunit.opcodes.FrameStackOpcodes;

public abstract class TokenMatrix {
	
	protected IALU ialu;
	protected IMUL imul;
	protected IDIV idiv;
	protected LALU lalu;
	protected FPU fpu;
	protected FDIV fdiv;
	protected ObjectHeap heap;
	protected FrameStack frameStack;
	protected TokenMachine tokenMachine;
	protected CGRA cgra;
	protected Scheduler scheduler;
	protected AcceleratorCore acceleratorCore;
	
	int count = -1;
	int currentTag = 0;
	
	int loopIterations;
	boolean loopIterationsValid;
	
	protected static final int PORT_A = 0, PORT_B = 1, PORT_C = 2;

	protected static final boolean TAG_INC = true, NO_TAG_INC = false;
	
	public IALU getIalu() {
		return ialu;
	}

	public IMUL getImul() {
		return imul;
	}

	public IDIV getIdiv() {
		return idiv;
	}

	public LALU getLalu() {
		return lalu;
	}

	public FDIV getFdiv() {
		return fdiv;
	}

	public ObjectHeap getHeap() {
		return heap;
	}

	public FrameStack getFrameStack() {
		return frameStack;
	}

	public TokenMachine getTokenMachine() {
		return tokenMachine;
	}

	public CGRA getCgra() {
		return cgra;
	}

	public Scheduler getScheduler() {
		return scheduler;
	}

	public AcceleratorCore getAcceleratorCore() {
		return acceleratorCore;
	}

	public void setIalu(IALU ialu) {
		this.ialu = ialu;
	}

	public void setImul(IMUL imul) {
		this.imul = imul;
	}

	public void setIdiv(IDIV idiv) {
		this.idiv = idiv;
	}

	public void setLalu(LALU lalu) {
		this.lalu = lalu;
	}

	public void setFdiv(FDIV fdiv) {
		this.fdiv = fdiv;
	}

	public void setHeap(ObjectHeap heap) {
		this.heap = heap;
	}

	public void setFrameStack(FrameStack frameStack) {
		this.frameStack = frameStack;
	}

	public void setTokenMachine(TokenMachine tokenMachine) {
		this.tokenMachine = tokenMachine;
	}

	public void setCgra(CGRA cgra) {
		this.cgra = cgra;
	}

	public void setScheduler(Scheduler scheduler) {
		this.scheduler = scheduler;
	}

	public void setAcceleratorCore(AcceleratorCore acceleratorCore) {
		this.acceleratorCore = acceleratorCore;
	}

	public abstract void decodeByteCode(short code);
	
	public boolean tokenDecodingDone() {
		return count == -1;
	}
	
	public void startNewBytecode(){
		count = 0;
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
		this.ialu = ialu;
		this.imul = imul;
		this.idiv = idiv;
		this.lalu = lalu;
		this.fpu = fpu;
		this.fdiv = fdiv;
		this.heap = heap;
		this.frameStack = frameStack;
		this.cgra = cgra;
		this.tokenMachine = tokenMachine;
		this.scheduler = scheduler;
		
	}
	
	public void setLoopIterations(int iterations){
		loopIterations = iterations;
		loopIterationsValid = true;
	}

}
