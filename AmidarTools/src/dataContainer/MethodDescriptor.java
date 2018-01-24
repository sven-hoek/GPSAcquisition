package dataContainer;

public class MethodDescriptor {
	
	int flags;
	int numberOfArgs;
	int maxStack;
	int maxLocals;
	int exceptionTableLength;
	int exceptionTableRef;
	int codeLength;
	int codeRef;
	
	String methodName; // Only for Debugging and Synthesis report
	
	
	public int getFlags() {
		return flags;
	}
	public void setFlags(int flags) {
		this.flags = flags;
	}
	public int getNumberOfArgs() {
		return numberOfArgs;
	}
	public void setNumberOfArgs(int numberOfArgs) {
		this.numberOfArgs = numberOfArgs;
	}
	public int getMaxStack() {
		return maxStack;
	}
	public void setMaxStack(int maxStack) {
		this.maxStack = maxStack;
	}
	public int getMaxLocals() {
		return maxLocals;
	}
	public void setMaxLocals(int maxLocals) {
		this.maxLocals = maxLocals;
	}
	public int getExceptionTableLength() {
		return exceptionTableLength;
	}
	public void setExceptionTableLength(int exceptionTableLength) {
		this.exceptionTableLength = exceptionTableLength;
	}
	public int getExceptionTableRef() {
		return exceptionTableRef;
	}
	public void setExceptionTableRef(int exceptionTableRef) {
		this.exceptionTableRef = exceptionTableRef;
	}
	public int getCodeLength() {
		return codeLength;
	}
	public void setCodeLength(int codeLength) {
		this.codeLength = codeLength;
	}
	public int getCodeRef() {
		return codeRef;
	}
	public void setCodeRef(int codeRef) {
		this.codeRef = codeRef;
	}
	public String getMethodName() {
		return methodName;
	}
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	
	
	
	
}
