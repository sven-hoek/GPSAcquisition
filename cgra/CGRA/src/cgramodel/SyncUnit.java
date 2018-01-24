package cgramodel;

import static cgramodel.AxiTransactionModel.GeneralTarget.GENERAL_TARGET_SINGLE_REG;
import static cgramodel.AxiTransactionModel.SingleRegTag.*;

import java.util.HashSet;
import java.util.List;

public class SyncUnit extends UltrasynthComponent {
    SyncUnit() {
        super("SyncUnit");
    }

    /**
     * Width of the biggest width out of:
     * {@link #runCounterWidth}, {@link #cycleCounterWidth}, {@link #specialActionCounterWidth} and CCNT + 2, which represents
     * a address to start a CGRA execution run and the info if it is a hybrid run or if the execution
     * should be started or stopped.
     */
    private int inputDataWidth = UNKNOWN;

    /**
     * Width of the counter counting the number of completed sensor->run->actor cycles
     */
    private int runCounterWidth = UNKNOWN;

    /**
     * Width of the counter counting the cycles for each interval (sensor->run->actor)
     */
    private int cycleCounterWidth = UNKNOWN;

    /**
     * The width of the hybrid counter, currently a hardcoded value 16 bits
     */
    private static final int specialActionCounterWidth = 16;

    /**
     * The AXI model for changing the state of the CGRA (e.g. stopped -> running)
     */
    private AxiTransactionModel stateChangeAxiModel;

    /**
     * The AXI model for setting the hybrid counter's reset value
     */
    private AxiTransactionModel specialActionCounterResetValueAxiModel;

    public void finalise(int ccntWidth) {
        if (runCounterWidth == UNKNOWN || cycleCounterWidth == UNKNOWN) {
            System.err.println("Unable to finalise " + this.getClass().getName());
            return;
        }

        // + 2 because of the additional run and hybrid bits send together with a CCNT value
        int stateWidth = ccntWidth + 2;

        inputDataWidth = Math.max(inputDataWidth, stateWidth);
        inputDataWidth = Math.max(inputDataWidth, runCounterWidth);
        inputDataWidth = Math.max(inputDataWidth, cycleCounterWidth);
        inputDataWidth = Math.max(inputDataWidth, specialActionCounterWidth);

        axiModel = new AxiTransactionModel(IntervalLength.name(), runCounterWidth, 1,
                GENERAL_TARGET_SINGLE_REG, IntervalLength.ordinal());

        stateChangeAxiModel = new AxiTransactionModel(CgraStateChange.name(), stateWidth, 1,
                GENERAL_TARGET_SINGLE_REG, CgraStateChange.ordinal());

        specialActionCounterResetValueAxiModel = new AxiTransactionModel(SpecialActionCounterResetValue.name(),
                specialActionCounterWidth, 1, GENERAL_TARGET_SINGLE_REG, SpecialActionCounterResetValue.ordinal());

        isFinal = true;
    }

    public int getInputDataWidth() {
        return inputDataWidth;
    }

    public int getRunCounterWidth() {
        return runCounterWidth;
    }

    public void setRunCounterWidth(int runCounterWidth) {
        this.runCounterWidth = runCounterWidth;
    }

    public int getCycleCounterWidth() {
        return cycleCounterWidth;
    }

    public void setCycleCounterWidth(int cycleCounterWidth) {
        this.cycleCounterWidth = cycleCounterWidth;
    }

    public AxiTransactionModel getStateChangeAxiModel() {
        return stateChangeAxiModel;
    }

    @Override
    public void addOtherAxiTransactionsTo(List<AxiTransactionModel> otherTransactions) {

    }

    @Override
    public void addSingleRegTransactionsTo(List<AxiTransactionModel> singleRegTransactions) {
        singleRegTransactions.add(axiModel);
        singleRegTransactions.add(stateChangeAxiModel);
        singleRegTransactions.add(specialActionCounterResetValueAxiModel);
    }

    @Override
    public HashSet<String> equalsInAttributes(UltrasynthComponent o) {
    	 if (this == o) return new HashSet<String>();
         if (o == null || getClass() != o.getClass()) return new HashSet<String>() {{add("SyncUnit not same class");}};

        SyncUnit syncUnit = (SyncUnit) o;
        HashSet<String> diff = new HashSet<String>();
        
        if (inputDataWidth != syncUnit.inputDataWidth) diff.add("SyncUnit inputDataWidth");
        if (runCounterWidth != syncUnit.runCounterWidth) diff.add("SyncUnit runCounterWidth");
        if (cycleCounterWidth != syncUnit.cycleCounterWidth) diff.add("SyncUnit cycleCounterWidth");
        return diff;
    }

}
