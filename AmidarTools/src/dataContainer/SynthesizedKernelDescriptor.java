package dataContainer;

import java.util.ArrayList;

public class SynthesizedKernelDescriptor {

	int contextPointer;
	int synthConstPointer;
	int nrLocalVarSend;
	int nrIndirectConst;
	int nrDirectConst;
	int nrLocalVarReceive;
	byte[] replacedBytes;
	
	
	ArrayList<Integer> followerKernelIDs = new ArrayList<>();
	
	
	public int getContextPointer() {
		return contextPointer;
	}
	public void setContextPointer(int contextPointer) {
		this.contextPointer = contextPointer;
	}
	public int getSynthConstPointer() {
		return synthConstPointer;
	}
	public void setSynthConstPointer(int tokenSetPointer) {
		this.synthConstPointer = tokenSetPointer;
	}
	public int getNrLocalVarSend() {
		return nrLocalVarSend;
	}
	public void setNrLocalVarSend(int nrLocalVarSend) {
		this.nrLocalVarSend = nrLocalVarSend;
	}
	public int getNrIndirectConst() {
		return nrIndirectConst;
	}
	public void setNrIndirectConst(int nrIndirectConst) {
		this.nrIndirectConst = nrIndirectConst;
	}
	public int getNrDirectConst() {
		return nrDirectConst;
	}
	public void setNrDirectConst(int nrDirectConst) {
		this.nrDirectConst = nrDirectConst;
	}
	public int getNrLocalVarReceive() {
		return nrLocalVarReceive;
	}
	public void setNrLocalVarReceive(int nrLocalVarReceive) {
		this.nrLocalVarReceive = nrLocalVarReceive;
	}
	public byte[] getReplacedBytes() {
		return replacedBytes;
	}
	public void setReplacedBytes(byte[] replacedBytes) {
		this.replacedBytes = replacedBytes;
	}
	
	public void addFollowerKernel(int followerID){
		followerKernelIDs.add(followerID);
	}
	
	public String getFollowerIDs(){
		return followerKernelIDs.toString();
	}
	
}
