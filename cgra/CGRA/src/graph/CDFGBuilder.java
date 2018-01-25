package graph;

import operator.Operator;

/**
 * Implement this interface to provide another way (with other default values)
 * for creating a {@link CDFG};
 */
public interface CDFGBuilder {

    /**
     * Build a {@link CDFG} from a given file.
     * The default implementation builds by using {@link IDP#parseAndOptimize(String, boolean, double, Operator...)}.
     * Default arguments are the given file name, true, 0.01 and no operators.
     *
     * @param file to parse and build from
     * @return the generated {@link CDFG}
     */
    default CDFG buildCDFG(String file) {
        return IDP.parseAndOptimize(file, true, 0.01);
    }

}
