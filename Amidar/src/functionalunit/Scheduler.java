package functionalunit;

import tracer.TraceManager;
import functionalunit.opcodes.SchedulerOpcodes;

public class Scheduler extends FunctionalUnit<SchedulerOpcodes> {

	public Scheduler(String configFile, TraceManager traceManager) {
		super(SchedulerOpcodes.class, configFile, traceManager);
		// TODO Auto-generated constructor stub
	}

	@Override
	public int getNrOfInputports() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean executeOp(SchedulerOpcodes op) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean validInputs(SchedulerOpcodes op) {
		// TODO Auto-generated method stub
		return false;
	}

}
