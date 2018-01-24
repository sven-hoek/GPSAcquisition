package cgramodel;

import util.SimpleMath;

import static cgramodel.AxiTransactionModel.GeneralTarget.GENERAL_TARGET_OTHER;
import static cgramodel.AxiTransactionModel.GeneralTarget.GENERAL_TARGET_SINGLE_REG;
import static cgramodel.AxiTransactionModel.SingleRegTag.LogDest;
import static cgramodel.AxiTransactionModel.SingleRegTag.LogDestBound;
import static cgramodel.AxiTransactionModel.SingleRegTag.LogDestInc;

import java.util.HashSet;
import java.util.List;

public class LogInterface extends UltrasynthComponent {
    LogInterface() {
        super("LogInterface");
    }

    /**
     * For testing: the ID of the biggest PE log BRAM.
     */
    private int idOfBiggestLog = UNKNOWN;

    /**
     * For testing: The size of the biggest PE log BRAM.
     */
    private int biggestLogSize = UNKNOWN;

    /**
     * Defines the maximum address width used to address
     * any PE log buffer.
     */
    private int maxPELogAddrWidth = UNKNOWN;

    /**
     * Size of the Global Log Context, which is responsible for
     * reordering of the results send via AXI.
     */
    private int globalContextSize = UNKNOWN;

    /**
     * Global Log Context address width which calculated from
     * {@link #globalContextSize}
     */
    private int globalContextAddrWidth = UNKNOWN;

    /**
     * The size of the global log Context required to send every
     * entry of a PE log buffer of the current composition.
     */
    private int globalContextTestSize = UNKNOWN;

    /**
     * Context Model for the Global Log Context.
     */
    private LogGlobalContext globalContext = null;

    private AxiTransactionModel destAxiModel;
    private AxiTransactionModel destBoundAxiModel;
    private AxiTransactionModel destIncAxiModel;

    /**
     * Finalise this instance.
     *
     * @param peComps
     *  A list of all {@link AugmentedPE}s of the parent {@link CgraModelUltrasynth}
     * @param peIDWidth
     *  Width of a PE ID
     * @param ccntWidth
     *  Context counter width of the parent CGRA model
     * @param systemAddrWidth
     *  The host system address width
     * @param nextUnusedOtherId
     *  The next unused other ID
     * @return
     *  The next unused other ID
     */
    public int finalise(List<AugmentedPE> peComps, int peIDWidth, int ccntWidth,
                         int systemAddrWidth, int nextUnusedOtherId) {
        if (globalContextSize == UNKNOWN) {
            System.err.println("Unable to finalise " + this.getClass().getName());
            return nextUnusedOtherId;
        }

        globalContextAddrWidth = SimpleMath.checkedLog(globalContextSize);
        maxPELogAddrWidth = 0;

        int accumulator = 0; // holds the accumulated amount of needed transfers to test this design
        for (AugmentedPE pe : peComps) {
            InterfaceContext logContext = pe.getLogContext();

            if (maxPELogAddrWidth < pe.getLogAddrWidth())
                maxPELogAddrWidth = pe.getLogAddrWidth();

            if (biggestLogSize < pe.getLogSize()) {
                idOfBiggestLog = pe.id;
                biggestLogSize = pe.getLogSize();
            }

            accumulator += pe.getLogSize();
        }
        accumulator += biggestLogSize*2 + maxPELogAddrWidth/2 + peComps.size() + 2;

        // other contexts
        long newGlobalLogContextSize = 1;
        while (accumulator > newGlobalLogContextSize)
            newGlobalLogContextSize = newGlobalLogContextSize << 1;
        globalContextTestSize = (int) newGlobalLogContextSize;

        // -1 to get rid of the enable bit
        globalContext = new LogGlobalContext(peIDWidth, maxPELogAddrWidth, ccntWidth);

        axiModel = new AxiTransactionModel("GlblLogContext", globalContext.getContextWidth(), globalContextSize,
                GENERAL_TARGET_OTHER, nextUnusedOtherId);

        destAxiModel = new AxiTransactionModel(LogDest.name(), systemAddrWidth, 1,
                GENERAL_TARGET_SINGLE_REG, LogDest.ordinal());

        destBoundAxiModel = new AxiTransactionModel(LogDestBound.name(), systemAddrWidth, 1,
                GENERAL_TARGET_SINGLE_REG, LogDestBound.ordinal());

        destIncAxiModel = new AxiTransactionModel(LogDestInc.name(),systemAddrWidth, 1,
                GENERAL_TARGET_SINGLE_REG, LogDestInc.ordinal());

        isFinal = true;
        return ++nextUnusedOtherId;
    }

    public int getIdOfBiggestLog() {
        return idOfBiggestLog;
    }

    public int getBiggestLogSize() {
        return biggestLogSize;
    }

    public int getMaxPELogAddrWidth() {
        return maxPELogAddrWidth;
    }

    public int getGlobalContextSize() {
        return globalContextSize;
    }

    public void setGlobalContextSize(int globalContextSize) {
        this.globalContextSize = globalContextSize;
    }

    public int getGlobalContextAddrWidth() {
        return globalContextAddrWidth;
    }

    public int getGlobalContextTestSize() {
        return globalContextTestSize;
    }

    public LogGlobalContext getGlobalContext() {
        return globalContext;
    }

    public AxiTransactionModel getDestAxiModel() {
        return destAxiModel;
    }

    public AxiTransactionModel getDestBoundAxiModel() {
        return destBoundAxiModel;
    }

    public AxiTransactionModel getDestIncAxiModel() {
        return destIncAxiModel;
    }

    @Override
    public void addOtherAxiTransactionsTo(List<AxiTransactionModel> otherTransactions) {
        otherTransactions.add(axiModel);
    }

    @Override
    public void addSingleRegTransactionsTo(List<AxiTransactionModel> singleRegTransactions) {
        singleRegTransactions.add(destAxiModel);
        singleRegTransactions.add(destBoundAxiModel);
        singleRegTransactions.add(destIncAxiModel);
    }

    @Override
    public HashSet<String> equalsInAttributes(UltrasynthComponent o) {
    	 if (this == o) return new HashSet<String>();
         if (o == null || getClass() != o.getClass()) return new HashSet<String>() {{add("LogInterface not same class");}};

        LogInterface that = (LogInterface) o;
        HashSet<String> diff = new HashSet<String>();
        if (idOfBiggestLog != that.idOfBiggestLog) diff.add("LogInterface idOfBiggestLog");
        if (biggestLogSize != that.biggestLogSize) diff.add("LogInterface biggestLogSize");
        if (maxPELogAddrWidth != that.maxPELogAddrWidth) diff.add("LogInterface maxPELogAddrWidth");
        if (globalContextSize != that.globalContextSize) diff.add("LogInterface globalContextSize");
        if (globalContextAddrWidth != that.globalContextAddrWidth) diff.add("LogInterface globalContextAddrWidth");
        if (globalContextTestSize != that.globalContextTestSize)diff.add("LogInterface globalContextTestSize");
        return diff;
    }

}
