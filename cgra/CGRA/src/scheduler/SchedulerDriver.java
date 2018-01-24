package scheduler;

import cgramodel.CgraModel;
import graph.CDFG;
import graph.CDFGBuilder;
import graph.LG;
import graph.LGBuilder;

public class SchedulerDriver implements CDFGBuilder, LGBuilder {

    private boolean hasResults;
    private RCListSched listSched;

    public SchedulerDriver(String graphFile, CgraModel model) {
        this(graphFile, model, 0);
    }

    public SchedulerDriver(String graphFile, CgraModel model, int maxLocalVarCount) {
        CDFG cdfg = buildCDFG(graphFile);
        LG lg = buildLG(cdfg);
        hasResults = false;

        try {
            this.listSched = new RCListSched(cdfg, lg, maxLocalVarCount, model);
        } catch (MissingOperationException e) {
            e.printStackTrace();
            System.err.println("Scheduler Driver aborts");
        }
    }

    public void run() {
        try {
            listSched.schedule();
        } catch (NotSchedulableException e) {
            e.printStackTrace();
            System.err.println("Could not complete schedule");
        }

        processSchedResults();
    }

    public boolean hasResults() {
        return hasResults;
    }

    public RCListSched getResults() {
        if (hasResults)
            return listSched;
        else
            throw new RuntimeException("Scheduler did not run and has no results.");
    }

    private void processSchedResults() {
        listSched.registerAllocation();
        listSched.cBoxAllocation();
        listSched.ctxtGeneration();
        listSched.ctxtGenerationUltrasynth();
        hasResults = true;
    }
}
