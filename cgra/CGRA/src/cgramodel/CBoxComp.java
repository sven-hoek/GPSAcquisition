package cgramodel;

import static cgramodel.AxiTransactionModel.GeneralTarget.GENERAL_TARGET_OTHER;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class CBoxComp extends UltrasynthComponent {
    CBoxComp() {
        super("CBox");
        evalBlockContextTransactions = new ArrayList<>(2);
    }

    /**
     * The actual max input data width calculated from
     * all writable contexts of the CBox.
     */
    private int maxInputDataWidth = UNKNOWN;

    /**
     * These {@link AxiTransactionModel}s describe the fact that the CBox has multiple internal
     * Context memories, one for each evaluation block.
     */
    private ArrayList<AxiTransactionModel> evalBlockContextTransactions;

    /**
     * Finalise this instance.
     *
     * @param cBoxContextWidth
     *  The width of the CBox of the parent CGRA model
     * @param contextSize
     *  The context size of the parent CGRA model
     * @param nextUnusedOtherId
     *  The next unused Other ID
     * @param evalBlockCount
     *  The amount of evaluation blocks of the CBox of the parent CGRA model
     * @param evalBlockContextWidth
     *  The context width of each evaluation block
     * @return
     *  The next unused Other ID
     */
    public int finalise(int cBoxContextWidth, int contextSize,
                        int evalBlockCount, int evalBlockContextWidth, int nextUnusedOtherId) {

        if (isFinal)
            evalBlockContextTransactions.clear();

        maxInputDataWidth = Math.max(cBoxContextWidth, evalBlockContextWidth);

        axiModel = new AxiTransactionModel("CBoxContext",
                cBoxContextWidth, contextSize, GENERAL_TARGET_OTHER, nextUnusedOtherId++);

        for (int i = 0; i < evalBlockCount; ++i) {
            AxiTransactionModel mod;
            mod = new AxiTransactionModel("CBoxEvalContext" + i, evalBlockContextWidth,
                    contextSize, GENERAL_TARGET_OTHER, nextUnusedOtherId++);
            evalBlockContextTransactions.add(mod);
        }

        isFinal = true;
        return nextUnusedOtherId;
    }

    public ArrayList<AxiTransactionModel> getEvalBlockContextTransactions() {
        return evalBlockContextTransactions;
    }

    @Override
    public void addOtherAxiTransactionsTo(List<AxiTransactionModel> otherTransactions) {
        otherTransactions.add(axiModel);
        otherTransactions.addAll(evalBlockContextTransactions);
    }

    @Override
    public void addSingleRegTransactionsTo(List<AxiTransactionModel> singleRegTransactions) {

    }

    @Override
    public HashSet<String> equalsInAttributes(UltrasynthComponent o) {
    	if (this == o) return new HashSet<String>();
        if (o == null || getClass() != o.getClass()) return new HashSet<String>() {{add("CBOXComp not same class");}};

        CBoxComp cBoxComp = (CBoxComp) o;
        HashSet<String> diff = new HashSet<String>();
        if(maxInputDataWidth != cBoxComp.maxInputDataWidth) diff.add("CBOXComp maxInputDataWidth");
        return diff;
    }

    public int getMaxInputDataWidth() {
        return maxInputDataWidth;
    }
}
