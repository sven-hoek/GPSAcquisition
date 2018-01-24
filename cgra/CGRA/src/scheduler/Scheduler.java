package scheduler;

import graph.CDFG;
import graph.LG;

/**
 * @author Tony Interface for schedulers
 */
public interface Scheduler {
	public abstract void setGraphs(CDFG graph, LG lg);

	public abstract Schedule schedule() throws NotSchedulableException, MissingOperationException;
}
