package cgramodel;

import java.util.HashSet;

public class CgraModelAmidar  extends CgraModel implements util.Version {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public final static int majorVersion = 3;
	public final static int minorVersion = 0;
	public final static int revisionVersion = 0;

	public int getMajorVersion(){
		return majorVersion;
	}

	public int getMinorVersion(){
		return minorVersion;
	}

	public int getRevisionVersion(){
		return revisionVersion;
	}

	public CgraModelAmidar(){
		super();
	}

	String CacheConfiguration;

	public String getCacheConfiguration() {
		return CacheConfiguration;
	}

	public void setCacheConfiguration(String cacheConfiguration) {
		CacheConfiguration = cacheConfiguration;
	}

	String HTCacheConfiguration;

	public String getHTCacheConfiguration() {
		return HTCacheConfiguration;
	}

	public void setHTCacheConfiguration(String hTCacheConfiguration) {
		HTCacheConfiguration = hTCacheConfiguration;
	}

	double EnergyStatic;

	public double getEnergyStatic() {
		return EnergyStatic;
	}

	public void setEnergyStatic(double energyStatic) {
		EnergyStatic = energyStatic;
	}

	public double getEnergySendLocalVar() {
		return energySendLocalVar;
	}

	public void setEnergySendLocalVar(double energySendLocalVar) {
		energySendLocalVar = energySendLocalVar;
	}

	public double getEnergyRun() {
		return energyRun;
	}

	public void setEnergyRun(double energyRun) {
		energyRun = energyRun;
	}

	public double getEnergyWriteContext() {
		return energyWriteContext;
	}

	public void setEnergyWriteContext(double energyWriteContext) {
		energyWriteContext = energyWriteContext;
	}

	public double getEnergyReceiveLocalVar() {
		return energyReceiveLocalVar;
	}

	public void setEnergyReceiveLocalVar(double energyReceiveLocalVar) {
		energyReceiveLocalVar = energyReceiveLocalVar;
	}

	public int getDurationSendLocalVar() {
		return durationSendLocalVar;
	}

	public void setDurationSendLocalVar(int durationSendLocalVar) {
		this.durationSendLocalVar = durationSendLocalVar;
	}

	public int getDurationRun() {
		return durationRun;
	}

	public void setDurationRun(int durationRun) {
		this.durationRun = durationRun;
	}

	public int getDurationWriteContext() {
		return durationWriteContext;
	}

	public void setDurationWriteContext(int durationWriteContext) {
		this.durationWriteContext = durationWriteContext;
	}

	public int getDurationReceiveLocalVar() {
		return durationReceiveLocalVar;
	}

	public void setDurationReceiveLocalVar(int durationReceiveLocalVar) {
		this.durationReceiveLocalVar = durationReceiveLocalVar;
	}

	double energySendLocalVar;
	int durationSendLocalVar;

	double energyRun;
	int durationRun;

	double energyWriteContext;
	int durationWriteContext;

	double energyReceiveLocalVar;
	int durationReceiveLocalVar;

	// TODO aus config file Lesen?
	int LOCATION_INFORMATION_MEMORY_SIZE = 1024;
	int CONSTANT_MEMORY_SIZE = 1024;
	int KERNEL_TABLE_SIZE = 64;

	public int getLocationInformationMemoryAddrWidth() {
		return (int) Math.ceil(Math.log(LOCATION_INFORMATION_MEMORY_SIZE) / Math.log(2));
	}

	public int getLocationInformationMemorySize(){
		return LOCATION_INFORMATION_MEMORY_SIZE;
	}

	public int getConstantMemoryAddrWidth() {
		return (int) Math.ceil(Math.log(CONSTANT_MEMORY_SIZE) / Math.log(2));
	}

	public int getConstantMemorySize(){
		return CONSTANT_MEMORY_SIZE;
	}

	public int getKernelTableAddrWidth() {
		return (int) Math.ceil(Math.log(KERNEL_TABLE_SIZE) / Math.log(2));
	}

	public int getKernelTableSize(){
		return KERNEL_TABLE_SIZE;
	}

	public int getMaxMemoryWidth(){
		int max = 0;

		int locationInformationMemoryWidth  = this.getViaWidth() + this.getMaxMuxAddrWidth();
		if(locationInformationMemoryWidth < this.getNrOfPEs()){
			locationInformationMemoryWidth = this.getNrOfPEs();
		}
		locationInformationMemoryWidth += this.getMaxRegfileAddrWidth();
		max = locationInformationMemoryWidth;

		int constMemoryWidth = 32;




		int kernelTableWidth = 2*this.getConstantMemoryAddrWidth() + this.getLocationInformationMemoryAddrWidth() + this.getCCNTWidth();

		int maxPEContextWidth = 0;

		for(PEModel pem : this.getPEs()){
			int wid = pem.getContextWidth();
			if(wid > maxPEContextWidth){
				maxPEContextWidth = wid;
			}
		}

		int cboxContextWidth = this.getcBoxModel().getContextmaskEvaLuationBlocks().getContextWidth();
		int ccuContextWidth = this.getContextmaskccu().getContextWidth();


		//FIND MAX
		max = locationInformationMemoryWidth;
		if(constMemoryWidth > max){
			max = constMemoryWidth;
		}
		if(kernelTableWidth > max){
			max = kernelTableWidth;
		}
		if(maxPEContextWidth > max){
			max = maxPEContextWidth;
		}
		if(cboxContextWidth > max){
			max = cboxContextWidth;
		}
		if(ccuContextWidth > max){
			max = ccuContextWidth;
		}

		return max;
	}

	protected boolean hostSpecificFinalized(){
		for(PEModel pe : PEs){
			if(!pe.getLiveout()){
				boolean liveoutpossible = false;
				for(PEModel pedest: PEs){
					if(pedest.getInputs().contains(pe) && pedest.getLiveout()){
						liveoutpossible = true;
						break;
					}
				}
				if(!liveoutpossible){
					System.out.println("liveout problem");
					return false;
				}
			}
		}
		return true;
	}


	protected HashSet<String> equalsInHostRelatedAttributes(CgraModel model){
		return new HashSet<String>();
	}



}
