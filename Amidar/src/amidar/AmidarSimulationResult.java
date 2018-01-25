package amidar;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;

import functionalunit.cache.coherency.CoherenceControllerTaskSimulator;
import functionalunit.tokenmachine.KernelProfiler;
import functionalunit.tokenmachine.Profiler;

public class AmidarSimulationResult implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1385609964551771971L;
	private long ticks;
	private long byteCodes;
	private long tokens;
	private int executionDuration;
	private double energy;
	private Amidar core;
	private Profiler profiler;
	private KernelProfiler kernelProfiler;
//	private double cgraTransmissionOverhead;
	private LinkedHashMap<String,Integer> cgraStateCount;
	private double l1Utilization;
	private double l2Utilization;
	private double memoryAccessTime;
	private double memoryAccesses;
	
	private int nrOfContexts;
	private int nrOfPrefetchesL1;
	private int nrOfUsedPrefetchesL1;
	private int nrOfPrefetchesL2;
	private int nrOfUsedPrefetchesL2;
	
	private int nrOfHandledPrefetchRequests;
	private int nrOfHandledPrefetchRequestsAlreadyAvailable;
	
	private int cachelineFills;
	
	private long synthTime;
	
	private CoherenceControllerTaskSimulator coherenceControllerTaskSimulator;
	
	public long getTicks() {
		return ticks;
	}
	public void setTicks(int ticks) {
		this.ticks = ticks;
	}
	public long getByteCodes() {
		return byteCodes;
	}
	public void setByteCodes(int byteCodes) {
		this.byteCodes = byteCodes;
	}
	public long getTokens() {
		return tokens;
	}
	public void setTokens(int tokens) {
		this.tokens = tokens;
	}
	public int getExecutionDuration() {
		return executionDuration;
	}
	public void setExecutionDuration(int executionDuration) {
		this.executionDuration = executionDuration;
	}
	public Profiler getProfiler() {
		return profiler;
	}
	public void setProfiler(Profiler profiler) {
		this.profiler = profiler;
	}
	public double getEnergy(){
		return energy;
	}
	public void setEnergy(double energy){
		this.energy = energy;
	}
	public Amidar getAmidarCore() {
		return core;
	}
	public KernelProfiler getKernelProfiler() {
		return kernelProfiler;
	}
	public void setKernelProfiler(KernelProfiler kernelProfiler) {
		this.kernelProfiler = kernelProfiler;
	}
	public LinkedHashMap<String,Integer> getCgraStateCount() {
		return cgraStateCount;
	}
	
	public AmidarSimulationResult(long ticks, long byteCodes, long tokens, int executionDuration, Profiler profiler, Amidar core, KernelProfiler kernelProfiler, LinkedHashMap<String,Integer> cgraStateCount){
		this.ticks = ticks;
		this.byteCodes = byteCodes;
		this.tokens = tokens;
		this.executionDuration = executionDuration;
		this.profiler = profiler;
		this.core = core;
		this.kernelProfiler = kernelProfiler;
		this.cgraStateCount = cgraStateCount;
	}
	
	public AmidarSimulationResult(long ticks, int executionDuration, double energy, Profiler profiler, Amidar core, KernelProfiler kernelProfiler, LinkedHashMap<String,Integer> cgraStateCount){
		this.ticks = ticks;
		this.executionDuration = executionDuration;
		this.energy = energy;
		this.profiler = profiler;
		this.byteCodes = profiler.getGlobalBytecodeCount();
		this.core = core;
		this.kernelProfiler = kernelProfiler;
		this.cgraStateCount = cgraStateCount;
	}
	public void addL1Utilization(double d) {
		l1Utilization = d;
	}
	public void addL2Utilization(double d) {
		l2Utilization = d;
	}
	
	public double getL1Utilization(){
		return l1Utilization;
	}
	public double getL2Utilization(){
		return l2Utilization;
	}
	public double getMemoryAccessTime() {
		return memoryAccessTime;
	}
	public void setMemoryAccessTime(double averageMemoryAccessTime) {
		this.memoryAccessTime = averageMemoryAccessTime;
	}
	public double getMemoryAccesses() {
		return memoryAccesses;
	}
	public void setMemoryAccesses(double memoryAccesses) {
		this.memoryAccesses = memoryAccesses;
	}
	
	public CoherenceControllerTaskSimulator getCoherenceControllerTaskSimulator() {
		return coherenceControllerTaskSimulator;
	}
	
	public void setCoherenceControllerTaskSimulator(CoherenceControllerTaskSimulator coherenceControllerTaskSimulator) {
		this.coherenceControllerTaskSimulator = coherenceControllerTaskSimulator;
	}
	/**
	 * To save space when saving many results
	 */
	public void deleteMethodNamesInProfiler(){
		profiler.deleteMethodNames();
	}
	public int getNrOfContexts() {
		return nrOfContexts;
	}
	public void setNrOfContexts(int nrOfContexts) {
		this.nrOfContexts = nrOfContexts;
	}
	public int getNrOfPrefetchesL1() {
		return nrOfPrefetchesL1;
	}
	public void setNrOfPrefetchesL1(int nrOfPrefetches) {
		this.nrOfPrefetchesL1 = nrOfPrefetches;
	}
	public int getNrOfUsedPrefetchesL1() {
		return nrOfUsedPrefetchesL1;
	}
	public void setNrOfUsedPrefetchesL1(int nrOfUsedPrefetches) {
		this.nrOfUsedPrefetchesL1 = nrOfUsedPrefetches;
	}
	public int getNrOfPrefetchesL2() {
		return nrOfPrefetchesL2;
	}
	public void setNrOfPrefetchesL2(int nrOfPrefetches) {
		this.nrOfPrefetchesL2 = nrOfPrefetches;
	}
	public int getNrOfUsedPrefetchesL2() {
		return nrOfUsedPrefetchesL2;
	}
	public void setNrOfUsedPrefetchesL2(int nrOfUsedPrefetches) {
		this.nrOfUsedPrefetchesL2 = nrOfUsedPrefetches;
	}
	public int getNrOfHandledPrefetchRequests() {
		return nrOfHandledPrefetchRequests;
	}
	public void setNrOfHandledPrefetchRequests(int nrOfHandledPrefetchRequests) {
		this.nrOfHandledPrefetchRequests = nrOfHandledPrefetchRequests;
	}
	public int getNrOfHandledPrefetchRequestsAlreadyAvailable() {
		return nrOfHandledPrefetchRequestsAlreadyAvailable;
	}
	public void setNrOfHandledPrefetchRequestsAlreadyAvailable(
			int nrOfHandledPrefetchRequestsAlreadyAvailable) {
		this.nrOfHandledPrefetchRequestsAlreadyAvailable = nrOfHandledPrefetchRequestsAlreadyAvailable;
	}
	public int getCachelineFills() {
		return cachelineFills;
	}
	public void setCachelineFills(int cachelineFills) {
		this.cachelineFills = cachelineFills;
	}
	public long getSynthTime() {
		return synthTime;
	}
	public void setSynthTime(long synthTime) {
		this.synthTime = synthTime;
	}
	

}
