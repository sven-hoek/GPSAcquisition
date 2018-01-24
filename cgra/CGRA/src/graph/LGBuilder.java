package graph;

import java.util.LinkedHashSet;

/**
 * Implement this interface to provide another way (with other default values)
 * for creating a {@link LG};
 */
public interface LGBuilder {

    /**
     * Build a {@link LG} (LoopGraph) from the given {@link CDFG}.
     * The default implementation is building a simple {@link LG} where all nodes
     * belong to one big loop.
     * This implementation was taken from SchedMain.java.
     *
     * @param graph the {@link CDFG} to use in the build process
     * @return the generated {@link LG}
     */
    default LG buildLG(CDFG graph) {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        int nAddr;

        for (Node nd : graph) {
            nAddr = nd.getAddress();
            if (min > nAddr) min = nAddr;
            if (max > nAddr) max = nAddr;
        }

        LG lg = new LG();
        lg.addLoop(new Loop(min, max), new LinkedHashSet<>(), null);
        return lg;
    }

}
