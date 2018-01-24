package amidar;

import java.util.LinkedHashMap;

import functionalunit.cache.coherency.CoherenceControllerTaskSimulator;
import functionalunit.cache.coherency.CoherenceControllerTask.RequestType;
import functionalunit.cache.coherency.CoherenceControllerTask.TaskType;

public class MeasurementResult {
	
	AmidarSimulationResult baselineShort;
	AmidarSimulationResult baselineLong;
	
//	boolean baselineShortAvailable = false;
//	boolean baselineLongAvailable = false;
	
	AmidarSimulationResult resultShort;
	AmidarSimulationResult resultLong;
	

	public MeasurementResult() {
//		resultShort = new LinkedHashMap<>();
//		resultLong = new LinkedHashMap<>();
	}
	
	public void addBaseline(AmidarSimulationResult baseline, boolean isShort){
		if(isShort){
			this.baselineShort = baseline;
//			baselineShortAvailable = true;
		} else {
			this.baselineLong = baseline;
//			baselineLongAvailable = true;
		}
		baseline.deleteMethodNamesInProfiler();
	}
	
	public void addResults(AmidarSimulationResult result, boolean isShort){
		if(isShort){
			 resultShort = result;
		} else {
			resultLong = result;
		}
		result.deleteMethodNamesInProfiler();
	}
	
	
	public double getSpeedup(){
		double res = 0;
		
		
		long base = baselineLong.getTicks() -baselineShort.getTicks();
		
//		if(resultLong.get(unroll) == null ||resultLong.get(unroll) == null ){
//			return 1.0;
//		}
		
		long ticks = resultLong.getTicks() - resultShort.getTicks();
		
		res = (double)base/(double)ticks;
		
		return res;
	}

	public double getCommunicationOverhead() {
		LinkedHashMap<String, Integer> stateCNTshort = resultShort.getCgraStateCount();
		LinkedHashMap<String, Integer> stateCNTlong = resultLong.getCgraStateCount();
		
		double transmissionCntShort = stateCNTshort.get("SEND") + stateCNTshort.get("REC") +stateCNTshort.get("WAIT");
		double transmissionCntLong = stateCNTlong.get("SEND") + stateCNTlong.get("REC") +stateCNTlong.get("WAIT");
		
		double transmissionCnt = transmissionCntLong- transmissionCntShort;
		
		double runCnt = stateCNTlong.get("RUN") - stateCNTshort.get("RUN");
		
		double overhead = 100*transmissionCnt/(transmissionCnt + runCnt);
		
		return overhead;
	}
	
	public double getDMAOverhead(){
		LinkedHashMap<String, Integer> stateCNTshort = resultShort.getCgraStateCount();
		LinkedHashMap<String, Integer> stateCNTlong = resultLong.getCgraStateCount();
		
		double transmissionCntShort = stateCNTshort.get("SEND") + stateCNTshort.get("REC") +stateCNTshort.get("WAIT");
		double transmissionCntLong = stateCNTlong.get("SEND") + stateCNTlong.get("REC") +stateCNTlong.get("WAIT");
		
		double transmissionCnt = transmissionCntLong- transmissionCntShort;
		
		double runCnt = stateCNTlong.get("RUN") - stateCNTshort.get("RUN");
		
		double dmaCNT = stateCNTlong.get("DMA") - stateCNTshort.get("DMA");
		
		double overhead = 100*dmaCNT/(transmissionCnt + runCnt);
		
		return overhead;
	}

	public double getL1Usage() {
		return resultLong.getL1Utilization()*100;
	}
	
	public double getL2Usage() {
		return resultLong.getL2Utilization()*100;
	}

	public double getAverageMemoryAccessTime() {
		double time = resultLong.getMemoryAccessTime() - resultShort.getMemoryAccessTime();
		double accesses = resultLong.getMemoryAccesses() - resultShort.getMemoryAccesses();
		
		return time/accesses;
	}
	
	
	public double getBlockTimesInPercent(RequestType requestType, TaskType taskType){
//		CoherenceControllerTaskSimulator taskSimulatorLong = resultLong.getCoherenceControllerTaskSimulator();
//		CoherenceControllerTaskSimulator taskSimulatorShort = resultShort.getCoherenceControllerTaskSimulator();
//		
//		int blockTimeType = taskSimulatorLong.getBlockTime(requestType, taskType) - taskSimulatorShort.getBlockTime(requestType, taskType);
//		int totalBlockTime = taskSimulatorLong.getTotalBlockTime() - taskSimulatorShort.getTotalBlockTime();

		CoherenceControllerTaskSimulator taskSimulatorLong = resultLong.getCoherenceControllerTaskSimulator();
		
		int blockTimeType = taskSimulatorLong.getBlockTime(requestType, taskType);
		int totalBlockTime = taskSimulatorLong.getTotalBlockTime();

		
		return blockTimeType*100.0/totalBlockTime;
	}
	
	public int getNrOfContexts(){
		return resultLong.getNrOfContexts();
	}
	
	public int getNrOfL1Prefetches(){
		return resultLong.getNrOfPrefetchesL1() - resultShort.getNrOfPrefetchesL1();
	}
	
	public int getNrOfUsedL1Prefetches(){
		return resultLong.getNrOfUsedPrefetchesL1() - resultShort.getNrOfUsedPrefetchesL1();
	}
	
	public int getNrOfHandledPrefetchRequests(){
		return resultLong.getNrOfHandledPrefetchRequests() - resultShort.getNrOfHandledPrefetchRequests();
	}
	public int getNrOfHandledPrefetchRequestsAlreadyAvailable(){
		return resultLong.getNrOfHandledPrefetchRequestsAlreadyAvailable()-resultShort.getNrOfHandledPrefetchRequestsAlreadyAvailable();
	}

	public int getCachelineFills() {
		return resultLong.getCachelineFills()-resultShort.getCachelineFills();
	}
	
	public long getSynthTime(){
		return resultLong.getSynthTime();
	}
	
}
