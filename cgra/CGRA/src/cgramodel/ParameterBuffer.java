package cgramodel;

import static cgramodel.AxiTransactionModel.UNUSED_ID;
import static cgramodel.AxiTransactionModel.GeneralTarget.GENERAL_TARGET_PARAMETER;
import static cgramodel.AxiTransactionModel.GeneralTarget.GENERAL_TARGET_SINGLE_REG;
import static cgramodel.AxiTransactionModel.SingleRegTag.ExpectedParameterCount;

import java.util.HashSet;
import java.util.List;

import target.Processor;
import util.SimpleMath;

public class ParameterBuffer extends UltrasynthComponent {
    ParameterBuffer() {
        super("ParameterBuffer");
    }

    /**
     * The size of the parameter buffer.
     */
    private int size = UNKNOWN;

    /**
     * The counter width of the parameter buffers fifo counter.
     * Directly related to {@link #size}
     */
    private int fifoCntrWidth = UNKNOWN;

    /**
     * The maximum amount of expected parameters during a hybrid run.
     */
    private int maxExpectedParamerters = UNKNOWN;

    /**
     * The counter width for counting incoming hybrid parameters.
     */
    private int expectedCntrWidth = UNKNOWN;

    /**
     * This AXI model describes parameter writes to the register files of the PEs.
     */
    private AxiTransactionModel parameterAxiModel;

    public void finalise(int idcSize) {
        if (size == UNKNOWN || maxExpectedParamerters == UNKNOWN) {
            System.err.println("Unable to finalise " + this.getClass().getName());
            return;
        }
        fifoCntrWidth = SimpleMath.checkedLog(size);
        expectedCntrWidth = SimpleMath.checkedLog(maxExpectedParamerters);

        axiModel = new AxiTransactionModel(ExpectedParameterCount.name(), expectedCntrWidth, 1,
                GENERAL_TARGET_SINGLE_REG, ExpectedParameterCount.ordinal());

        parameterAxiModel = new AxiTransactionModel("Paramerter Write", Processor.Instance.getDataPathWidth(),
                idcSize, GENERAL_TARGET_PARAMETER, UNUSED_ID);

        isFinal = true;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getFifoCntrWidth() {
        return fifoCntrWidth;
    }

    public int getMaxExpectedParamerters() {
        return maxExpectedParamerters;
    }

    public void setMaxExpectedParamerters(int maxExpectedParamerters) {
        this.maxExpectedParamerters = maxExpectedParamerters;
    }

    public int getExpectedCntrWidth() {
        return expectedCntrWidth;
    }

    public AxiTransactionModel getParameterAxiModel() {
        return parameterAxiModel;
    }

    @Override
    public void addOtherAxiTransactionsTo(List<AxiTransactionModel> otherTransactions) {

    }

    @Override
    public void addSingleRegTransactionsTo(List<AxiTransactionModel> singleRegTransactions) {
        singleRegTransactions.add(axiModel);
    }

    @Override
    public HashSet<String> equalsInAttributes(UltrasynthComponent o) {
    	 if (this == o) return new HashSet<String>();
         if (o == null || getClass() != o.getClass()) return new HashSet<String>() {{add("ParameterBuffer not same class");}};

        ParameterBuffer that = (ParameterBuffer) o;
        HashSet<String> diff = new HashSet<String>();
        if (size != that.size) diff.add("ParameterBuffer size");
        if (fifoCntrWidth != that.fifoCntrWidth) diff.add("ParameterBuffer fifoCntrWidth");
        if (maxExpectedParamerters != that.maxExpectedParamerters)diff.add("ParameterBuffer maxExpectedParamerters");
        if (expectedCntrWidth != that.expectedCntrWidth) diff.add("ParameterBuffer expectedCntrWidth");
        return diff;
    }

}
