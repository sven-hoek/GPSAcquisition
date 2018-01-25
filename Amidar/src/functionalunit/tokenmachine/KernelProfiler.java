package functionalunit.tokenmachine;

import java.io.Serializable;
import java.util.LinkedHashMap;

import javax.management.RuntimeErrorException;

import tracer.Trace;

/**
 * Only for debugging purposes - not 100% exact
 * @author jung
 *
 */
public class KernelProfiler implements Serializable{
	

	/**
	 * 
	 */
	private static final long serialVersionUID = -3388595511178808906L;
	LinkedHashMap<Integer,Integer> kernelCalls;
	LinkedHashMap<Integer,Integer> kernelTicks;
	
	LinkedHashMap<Integer,String> kernelName;
	
	Integer currentKernel = null;
	
	public KernelProfiler(){
		kernelCalls = new LinkedHashMap<>();
		kernelTicks = new LinkedHashMap<>();
		kernelName = new LinkedHashMap<>();
	}
	
	
	public void registerKernel(int id, String name){
		kernelName.put(id, name);
		kernelCalls.put(id, 0);
		kernelTicks.put(id, 0);
	}
	
	public void startKernel(int id){
		if(currentKernel != null){
			throw new RuntimeException("There is already one kernel started");
		}
		currentKernel = id;
	}
	
	public void stopKernel(int ticks){
		if(currentKernel == null){
			throw new RuntimeException("No kernel started...");
		}
		kernelTicks.put(currentKernel, kernelTicks.get(currentKernel) + ticks);
		kernelCalls.put(currentKernel, kernelCalls.get(currentKernel) + 1);
		currentKernel = null;
	}
	
	public void reportProfile(Trace trace, long totalTicks){
		trace.printTableHeader("Kernel Profile " + totalTicks);
		for(Integer kernelID : kernelName.keySet()){
			trace.println(kernelID+"\t" + kernelName.get(kernelID)+"\tCalls: "+kernelCalls.get(kernelID)+"\tTicks: " + kernelTicks.get(kernelID)+" ("+(kernelTicks.get(kernelID)*100L/totalTicks)+"%)");
		}
	}

}
