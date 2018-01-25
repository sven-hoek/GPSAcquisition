package cgramodel;

import static cgramodel.AxiTransactionModel.GeneralTarget.GENERAL_TARGET_OTHER;

import java.util.HashSet;
import java.util.List;

public class ConstBuffer extends UltrasynthComponent {
    ConstBuffer() {
        super("ConstBuffer");
    }

    /**
     * Constant buffer size.
     */
    private int maxSize = UNKNOWN;

    /**
     * Constant buffer address width, calculated form {@link #maxSize}.
     */
    private int maxAddrWidth = UNKNOWN;

    /**
     * The width of the access size (wideaccess_width).
     * If this is 0, no access width is required.
     */
    private int maxAccessSizeWidth = UNKNOWN;

    /**
     * A list of all the {@link AxiTransactionModel}s which are necessary to
     * transfer data to all the PEs which have a attached ROM.
     */
    private List<AxiTransactionModel> axiModels;

    /**
     * Finalise this instance.
     * @param nextUnusedOtherId
     *  The next unused Other ID
     * @return
     *  The next unused Other ID
     */
    public int finalise(int nextUnusedOtherId, int dataPathWidth, List<PEModel> pes) {
//        for (PEModel pe : pes) {
//            if (pe.getRomAccess()) {
//                if (maxSize != UNKNOWN && maxSize != pe.getRomSize())
//
//
//                maxSize = Math.max(maxSize, pe.getRomSize());
//                maxAddrWidth = Math.max(maxAddrWidth, pe.getRomAddrWidth());
//
//                axiModel = new AxiTransactionModel("ConstBuffer" + pe.getID(), dataPathWidth, pe.getRomSize(),
//                        GENERAL_TARGET_OTHER, nextUnusedOtherId++);
//                axiModels.add(axiModel);
//            }
//        }
//
        axiModel = null;
        isFinal = true;
        return nextUnusedOtherId;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public int getMaxAddrWidth() {
        return maxAddrWidth;
    }

    public int getMaxAccessSizeWidth() {
        return maxAccessSizeWidth;
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
        if (o == null || getClass() != o.getClass()) return new HashSet<String>() {{add("ConstBuffer not same class");}};

        ConstBuffer that = (ConstBuffer) o;
        HashSet<String> diff = new HashSet<String>();
        if (maxSize != that.maxSize) diff.add("ConstBuffer ");
        if (maxAddrWidth != that.maxAddrWidth)diff.add("ConstBuffer ");
        if (maxAccessSizeWidth != that.maxAccessSizeWidth) diff.add("ConstBuffer ");
        return diff;
    }

}
