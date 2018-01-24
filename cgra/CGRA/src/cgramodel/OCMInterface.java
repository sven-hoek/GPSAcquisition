package cgramodel;

import util.SimpleMath;

import static cgramodel.AxiTransactionModel.GeneralTarget.GENERAL_TARGET_OTHER;
import static cgramodel.AxiTransactionModel.SingleRegTag.OCMDest;
import static cgramodel.AxiTransactionModel.SingleRegTag.OCMDestBound;
import static cgramodel.AxiTransactionModel.SingleRegTag.OCMDestInc;

import java.util.HashSet;
import java.util.List;

public class OCMInterface extends UltrasynthComponent {
    OCMInterface() {
        super("OCMInterface");
    }

    /**
     * Width of the Data Buffer which is used to gather results from the PEs.
     */
    private int bufferWidth = UNKNOWN;

    /**
     * Size of the Data Buffer which is used to gather results from the PEs.
     */
    private int bufferSize = UNKNOWN;

    /**
     * Address width of the Data Buffer which is used to gather results from the PEs.
     */
    private int bufferAddrWidth = UNKNOWN;

    /**
     * The size of the {@link #outputContext} which is responsible for reordering the
     * gathered results.
     */
    private int outputContextSize = UNKNOWN;

    /**
     * The address width of the {@link #outputContext} which is responsible for reordering the
     * gathered results.
     */
    private int outputContextAddrWidth = UNKNOWN;

    /**
     * The max context address width of any context used by the {@link OCMInterface}
     */
    private int maxContextAddrWidth = UNKNOWN;

    /**
     * Context Mask used to fill the result buffer.
     */
    private Context gatherContext = null;

    /**
     * Context Mask used to generate the output context which reorders the
     * results just before sending them to the host.
     */
    private LogGlobalContext outputContext = null;

    private AxiTransactionModel outputContextAxiModel;
    private AxiTransactionModel destAxiModel;
    private AxiTransactionModel destBoundAxiModel;
    private AxiTransactionModel destIncAxiModel;

    /**
     * Finalise this instance.
     * @param peIdWidth
     *          ID width of a PE ID
     * @param gatherContextSize
     *          CGRA gatherContext size
     * @param systemAddrWidth
     *          The system address width
     * @param nextUnusedOtherId
     *          The next unused Other ID
     * @return
     *          The next unused Other ID
     */
    public int finalise(int peIdWidth, int gatherContextSize, int systemAddrWidth, int ccntWidth, int nextUnusedOtherId) {
        if (bufferWidth == UNKNOWN || bufferSize == UNKNOWN || outputContextSize == UNKNOWN) {
            System.err.println("Unable to finalise " + this.getClass().getName());
            return nextUnusedOtherId;
        }

        bufferAddrWidth = SimpleMath.checkedLog(bufferSize);
        gatherContext = new Context(peIdWidth);

        outputContextAddrWidth = SimpleMath.checkedLog(outputContextSize);
        outputContext = new LogGlobalContext(0, bufferAddrWidth, ccntWidth);

        maxContextAddrWidth = Math.max(outputContextAddrWidth, ccntWidth);

        axiModel = new AxiTransactionModel("OCMContext", gatherContext.getContextWidth(), gatherContextSize,
                GENERAL_TARGET_OTHER, nextUnusedOtherId++);

        outputContextAxiModel = new AxiTransactionModel("OCMOutputContext", outputContext.getContextWidth(),
                outputContextSize, GENERAL_TARGET_OTHER, nextUnusedOtherId++);

        destAxiModel = new AxiTransactionModel(OCMDest.name(), systemAddrWidth, 1, GENERAL_TARGET_OTHER,
                OCMDest.ordinal());

        destBoundAxiModel = new AxiTransactionModel(OCMDestBound.name(), systemAddrWidth, 1, GENERAL_TARGET_OTHER,
                OCMDestBound.ordinal());

        destIncAxiModel = new AxiTransactionModel(OCMDestInc.name(), systemAddrWidth, 1, GENERAL_TARGET_OTHER,
                OCMDestInc.ordinal());

        isFinal = true;
        return nextUnusedOtherId;
    }

    public int getBufferWidth() {
        return bufferWidth;
    }

    public void setBufferWidth(int bufferWidth) {
        this.bufferWidth = bufferWidth;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public int getBufferAddrWidth() {
        return bufferAddrWidth;
    }

    public int getOutputContextSize() {
        return outputContextSize;
    }

    public void setOutputContextSize(int outputContextSize) {
        this.outputContextSize = outputContextSize;
    }

    public int getOutputContextAddrWidth() {
        return outputContextAddrWidth;
    }

    public int getMaxContextAddrWidth() {
        return maxContextAddrWidth;
    }

    public Context getGatherContext() {
        return gatherContext;
    }

    public LogGlobalContext getOutputContext() {
        return outputContext;
    }

    public AxiTransactionModel getOutputContextAxiModel() {
        return outputContextAxiModel;
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
        otherTransactions.add(outputContextAxiModel);
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
        if (o == null || getClass() != o.getClass()) return new HashSet<String>() {{add("OCMInterface not same class");}};

        OCMInterface that = (OCMInterface) o;
        HashSet<String> diff = new HashSet<String>();
        if (bufferWidth != that.bufferWidth) diff.add("OCMInterface bufferWidth");
        if (bufferSize != that.bufferSize) diff.add("OCMInterface bufferSize");
        if (bufferAddrWidth != that.bufferAddrWidth) diff.add("OCMInterface bufferAddrWidth");
        if (outputContextSize != that.outputContextSize) diff.add("OCMInterface outputContextSize");
        if (outputContextAddrWidth != that.outputContextAddrWidth) diff.add("OCMInterface outputContextAddrWidth");
        if (maxContextAddrWidth != that.maxContextAddrWidth) diff.add("OCMInterface maxContextAddrWidth");
        return diff;
    }

    /**
     * This gatherContext is responsible for selecting which data is written to the
     * CGRA side OCM data buffer which is then send to the OCM of the Zynq board.
     *
     * PeID selects the PE to take tha data from while enable is the write enable
     * for the buffer to write to.
     * Complete signals that this is the last data item written to the buffer and that transactions
     * may start now (if they haven't already)
     */
    public class Context extends ContextMask {
        private static final long serialVersionUID = 1L;

        private int peID;
        private int peIDWidth;
        private int enable;
        private int complete;

        public Context(int peIDWidth) {
            super();
            this.peIDWidth = peIDWidth;
            createMask();
        }

        private void createMask() {
            peID = 0;
            enable = peIDWidth;
            complete = enable + 1;

            setContextWidth(complete + 1);
        }

        public long setComplete(long context, int value) {
            return writeBitSet(context, value, complete, 1);
        }
        public long setEnable(long context, int value) {
            return writeBitSet(context, value, enable, 1);
        }
        public long setPeID(long context, int id) {
            return writeBitSet(context, id, peID, peIDWidth);
        }
    }
}
