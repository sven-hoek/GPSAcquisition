package functionalunit.cgra;

public class KernelTableEntryCGRA {
	
	int locationInformationMemoryPointer = 0;
	int contextPointer = 0;
	int constantPointer = 0;
	int nrOfConstants = 0;
//	int nrOfLocalVariablesReceive = 0;
//	int nrOfLocalVariablesSend = 0;
	
	boolean locationInformationMemoryPointerValid = false;
	boolean contextPointerValid = false;
	boolean constantPointerValid = false;
	public int getLocationInformationMemoryPointer() {
		return locationInformationMemoryPointer;
	}
	public void setLocationInformationMemoryPointer(
			int locationInformationMemoryPointer) {
		this.locationInformationMemoryPointer = locationInformationMemoryPointer;
	}
	public int getContextPointer() {
		return contextPointer;
	}
	public void setContextPointer(int contextPointer) {
		this.contextPointer = contextPointer;
	}
	public int getConstantPointer() {
		return constantPointer;
	}
	public void setConstantPointer(int constantPointer) {
		this.constantPointer = constantPointer;
	}
	public int getNrOfConstants() {
		return nrOfConstants;
	}
	public void setNrOfConstants(int nrOfConstants) {
		this.nrOfConstants = nrOfConstants;
	}
//	public int getNrOfLocalVariablesReceive() {
//		return nrOfLocalVariablesReceive;
//	}
//	public void setNrOfLocalVariablesReceive(int nrOfLocalVariablesReceive) {
//		this.nrOfLocalVariablesReceive = nrOfLocalVariablesReceive;
//	}
//	public int getNrOfLocalVariablesSend() {
//		return nrOfLocalVariablesSend;
//	}
//	public void setNrOfLocalVariablesSend(int nrOfLocalVariablesSend) {
//		this.nrOfLocalVariablesSend = nrOfLocalVariablesSend;
//	}
	public boolean isLocationInformationMemoryPointerValid() {
		return locationInformationMemoryPointerValid;
	}
	public void setLocationInformationMemoryPointerValid(
			boolean locationInformationMemoryPointerValid) {
		this.locationInformationMemoryPointerValid = locationInformationMemoryPointerValid;
	}
	public boolean isContextPointerValid() {
		return contextPointerValid;
	}
	public void setContextPointerValid(boolean contextPointerValid) {
		this.contextPointerValid = contextPointerValid;
	}
	public boolean isConstantPointerValid() {
		return constantPointerValid;
	}
	public void setConstantPointerValid(boolean constantPointerValid) {
		this.constantPointerValid = constantPointerValid;
	}
	
	

}
