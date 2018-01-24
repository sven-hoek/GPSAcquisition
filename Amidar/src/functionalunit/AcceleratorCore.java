package functionalunit;

import tracer.TraceManager;
import functionalunit.opcodes.AcceleratorCoreOpcodes;

public class AcceleratorCore extends FunctionalUnit<AcceleratorCoreOpcodes> {

	public AcceleratorCore(String configFile, TraceManager traceManager) {
		super(AcceleratorCoreOpcodes.class, configFile, traceManager);
		// TODO Auto-generated constructor stub
	}

	@Override
	public int getNrOfInputports() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean executeOp(AcceleratorCoreOpcodes op) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean validInputs(AcceleratorCoreOpcodes op) {
		// TODO Auto-generated method stub
		return false;
	}

}
