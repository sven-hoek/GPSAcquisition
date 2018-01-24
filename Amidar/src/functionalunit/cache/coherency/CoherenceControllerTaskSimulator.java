package functionalunit.cache.coherency;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import functionalunit.cache.coherency.CoherenceControllerTask.RequestType;
import functionalunit.cache.coherency.CoherenceControllerTask.TaskType;

public class CoherenceControllerTaskSimulator {
	
	
	
	public enum CGRAState{
		Blocked,
		Running
	}
	public enum Executor{
		CGRA,
		AMIDAR
	}
	
	
	List<CoherenceControllerTask> tasks = new LinkedList<>();
	
	LinkedList<CoherenceControllerTask> task2 = new LinkedList<CoherenceControllerTask>(), task1 = new LinkedList<CoherenceControllerTask>(), task0 = new LinkedList<CoherenceControllerTask>();
	int busytime2 = 0, busyTime1 = 0;
	
	public void addCoherenceControllerTask(CoherenceControllerTask task){
		task0.add(task);
//		tasks.add(task);
	}
	
	
	int [][][][] counter = new int[Executor.values().length][CGRAState.values().length][RequestType.values().length][TaskType.values().length];
	int ticksCGRA = 0;
	int ticksWoCGRA = 0;
	
	
	public RequestType tick(int busytime, boolean kernelRunningOnCGRA, boolean cgraBlocked){
		RequestType result = null;
//		if(task2!=null){
//			tasks.addAll(task2);
//		}
		
		for(CoherenceControllerTask cct: task2){
			tasks.add(cct);
		}
		
		
		
		task2 = task1;
		task1 = task0;
		task0 = new LinkedList<>();
		
		
		
		
		Executor executor;
		if(kernelRunningOnCGRA){
			ticksCGRA++;
			executor = Executor.CGRA;
		} else {
			ticksWoCGRA++;
			executor = Executor.AMIDAR;
		}
		
		
		//// OPTIONAL ////////////////////
		int busy = 0;
		for(CoherenceControllerTask tsk: tasks){
			busy+=tsk.getDuration();
		}
		if(kernelRunningOnCGRA && busy != busytime2){
//			System.err.println();
			for(CoherenceControllerTask tsk: tasks){
				System.out.println("HMMMDM: " + tsk + " ..... " + busy + " - " + busytime2);
			}
//			throw new RuntimeException("Irgendwas ist hier faul... "  + busy +  "  vs . " + busytime2);
		}
		//////////////////////////////////

		CGRAState cgraState = (cgraBlocked ? CGRAState.Blocked:CGRAState.Running);
		if(tasks.size() > 0){
		CoherenceControllerTask currentTask = tasks.get(0);
		
			RequestType rt = currentTask.getRequestType();
			TaskType tt = currentTask.getTaskType();

			if(currentTask.taskFinished()){
				tasks.remove(0);
			}

			counter[executor.ordinal()][cgraState.ordinal()][rt.ordinal()][tt.ordinal()]++;
			
			result = rt;
//			
//			System.out.println("--------------- " + kernelRunningOnCGRA);
//			System.out.println("executor: " + executor);
//			System.out.println("   " + cgraState);
//			System.out.println("   " + rt);
//			System.out.println("   " + tt);
//			if(executor == Executor.CGRA)
//			System.out.println(executor+ "\t" + cgraState + " \t" + currentTask);
		} else {
//			if(executor == Executor.CGRA)
//			System.out.println(executor+ "\t" + cgraState + busytime2);
		}
	
		busytime2 = busyTime1;
		busyTime1 = busytime;
		return result;
	}
	
	
	public void resetCounter(){
		tasks.clear();
		counter = new int[Executor.values().length][CGRAState.values().length][RequestType.values().length][TaskType.values().length];
		ticksCGRA = 0;
		ticksWoCGRA = 0;
		busyTime1 = 0;
		busytime2 = 0;
		task1.clear();
		task2.clear();
		task0.clear();
	}
	
	public int getBlockTime(RequestType requestType, TaskType taskType){
		return counter[Executor.CGRA.ordinal()][CGRAState.Blocked.ordinal()][requestType.ordinal()][taskType.ordinal()];
	}
	
	public int getTotalBlockTime(){
		int blocked = 0;
		
		
		for(TaskType tt : TaskType.values()){
			blocked += counter[Executor.CGRA.ordinal()][CGRAState.Blocked.ordinal()][RequestType.Prefetch.ordinal()][tt.ordinal()];
			blocked += counter[Executor.CGRA.ordinal()][CGRAState.Blocked.ordinal()][RequestType.Regular.ordinal()][tt.ordinal()];
		}		
		
		return blocked;
	}
	
	
	public void report(){
		System.out.println("CGRA: \t" + ticksCGRA) ;
		
		int blocked = 0;
		int blockedPref = 0;
		int blockedRegular = 0;
		
		int running = 0;
		int runningPref = 0;
		int runningRegular = 0;
		
		for(TaskType tt : TaskType.values()){
			blockedPref += counter[Executor.CGRA.ordinal()][CGRAState.Blocked.ordinal()][RequestType.Prefetch.ordinal()][tt.ordinal()];
			blockedRegular += counter[Executor.CGRA.ordinal()][CGRAState.Blocked.ordinal()][RequestType.Regular.ordinal()][tt.ordinal()];
			
			runningPref += counter[Executor.CGRA.ordinal()][CGRAState.Running.ordinal()][RequestType.Prefetch.ordinal()][tt.ordinal()];
			runningRegular += counter[Executor.CGRA.ordinal()][CGRAState.Running.ordinal()][RequestType.Regular.ordinal()][tt.ordinal()];
		}
		
		blocked = blockedPref + blockedRegular;
		running = runningPref + runningRegular;
		
		
		System.out.println("\tBlocked:" + blocked + "\tPrefetch: " + blockedPref + " \tRegular: " + blockedRegular);
		System.out.println("\t\tPrefetch: ");
		for(TaskType tt : TaskType.values()){
			System.out.println("\t\t\t"+ tt+ ": "+ counter[Executor.CGRA.ordinal()][CGRAState.Blocked.ordinal()][RequestType.Prefetch.ordinal()][tt.ordinal()]);
		}
		System.out.println("\t\tRegular: ");
		for(TaskType tt : TaskType.values()){
			System.out.println("\t\t\t"+ tt+ ": "+ counter[Executor.CGRA.ordinal()][CGRAState.Blocked.ordinal()][RequestType.Regular.ordinal()][tt.ordinal()]);
		}
		
		System.out.println("\tRunning:" + running + "\tPrefetch: " + runningPref + " \tRegular: " + runningRegular);
		System.out.println("\t\tPrefetch: ");
		for(TaskType tt : TaskType.values()){
			System.out.println("\t\t\t"+ tt+ ": "+ counter[Executor.CGRA.ordinal()][CGRAState.Running.ordinal()][RequestType.Prefetch.ordinal()][tt.ordinal()]);
		}
		System.out.println("\t\tRegular: ");
		for(TaskType tt : TaskType.values()){
			System.out.println("\t\t\t"+ tt+ ": "+ counter[Executor.CGRA.ordinal()][CGRAState.Running.ordinal()][RequestType.Regular.ordinal()][tt.ordinal()]);
		}
		
	}
	

}
