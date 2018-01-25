package cgramodel;

import util.SimpleMath;

import static cgramodel.AxiTransactionModel.GeneralTarget.GENERAL_TARGET_PE;

import java.util.HashSet;
import java.util.List;

public class AugmentedPE extends UltrasynthComponent {
    AugmentedPE(int id) {
        super("PE" + id);
        this.id = id;
    }

    public final int id;

    private int logSize = UNKNOWN;
    private int logAddrWidth = UNKNOWN;
    private int logId = UNKNOWN;

    private AxiTransactionModel logAxiModel;
    private InterfaceContext logContext;

    public void finalise(CgraModel cgra, int logIdOffset) {
        if (logSize == UNKNOWN) {
            System.err.println("Unable to finalise " + this.getClass().getName());
            return;
        }

        PEModel relatedPe = cgra.getPEs().get(id);

        int peContextWidth = relatedPe.getContext().getContextWidth();
        int contextSize = cgra.getContextMemorySize();

        axiModel = new AxiTransactionModel(name, peContextWidth, contextSize,
                GENERAL_TARGET_PE, id);

        logAddrWidth = SimpleMath.checkedLog(logSize);
        logId = id + logIdOffset;
        logContext = new InterfaceContext(logSize);

        logAxiModel = new AxiTransactionModel("LOG" + name, logContext.getContextWidth(), contextSize,
                GENERAL_TARGET_PE, id + logIdOffset);

        isFinal = true;
    }

    public AxiTransactionModel getLogAxiModel() {
        return logAxiModel;
    }

    public int getLogSize() {
        return logSize;
    }

    public void setLogSize(int logSize) {
        this.logSize = logSize;
    }

    public int getLogAddrWidth() {
        return logAddrWidth;
    }

    public int getLogId() {
        return logId;
    }

    public InterfaceContext getLogContext() {
        return logContext;
    }

    @Override
    public void addOtherAxiTransactionsTo(List<AxiTransactionModel> otherTransactions) {

    }

    @Override
    public void addSingleRegTransactionsTo(List<AxiTransactionModel> singleRegTransactions) {

    }

    @Override
    public HashSet<String> equalsInAttributes(UltrasynthComponent o) {
        if (this == o) return new HashSet<String>();
        if (o == null || getClass() != o.getClass()) return new HashSet<String>() {{add("AugPE " + id + "not same class");}};
        HashSet<String> diff = new HashSet<String>();
        AugmentedPE that = (AugmentedPE) o;

        if (id != that.id) diff.add("AugPE " + id + " id");
        if (logSize != that.logSize) diff.add("AugPE " + id + " logSize");
        if (logAddrWidth != that.logAddrWidth) diff.add("AugPE " + id + "logAddrWidth");
        if(logId != that.logId) diff.add("AugPE " + id + " logId");
        return diff;
    }

}
