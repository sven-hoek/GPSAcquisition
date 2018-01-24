package functionalunit.cache.coherency;

public class CoherenceControllerTask {
	
	
	public enum TaskType{
		L1DataFetch,
		L2DataFetch,
		L2DataWriteBack,
		L1WriteNotification, 
		HTRequest
	}
	
	public enum RequestType{
		Regular,
		Prefetch
	}
	
	
	
	private TaskType taskType;
	private RequestType requestType;
	
	private int duration;
	
	private int requestingCache;
	
	public CoherenceControllerTask(TaskType taskType, RequestType requestType, int duration, int requestingCache) {
		this.taskType = taskType;
		this.requestType = requestType;
		this.duration = duration;
		this.requestingCache = requestingCache;
	}
	
	
	public boolean taskFinished(){
		return --duration <= 0;
	}

	public TaskType getTaskType() {
		return taskType;
	}

	public RequestType getRequestType() {
		return requestType;
	}


	public int getDuration() {
		return duration;
	}
	
	public int getRequestingCache() {
		return requestingCache;
	}
	
	public String toString(){
		return ("TaskType: " + taskType + " ("+requestType+")\t" + duration);
	}
}
