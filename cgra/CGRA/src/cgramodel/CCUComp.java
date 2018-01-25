package cgramodel;

import static cgramodel.AxiTransactionModel.GeneralTarget.GENERAL_TARGET_OTHER;

import java.util.HashSet;
import java.util.List;

public class CCUComp extends UltrasynthComponent {

    CCUComp() {
        super("CCU");
    }

    /**
     * Finalise this instance.
     *
     * @param contextWidth      CCU context mem width
     * @param contextMemorySize CGRA context mem size
     * @param nextUnusedOtherId Next unused other ID
     * @return The next unused other ID
     */
    public int finalise(int contextWidth, int contextMemorySize, int nextUnusedOtherId) {
        axiModel = new AxiTransactionModel("CCUContext", contextWidth,
                contextMemorySize, GENERAL_TARGET_OTHER, nextUnusedOtherId);
        isFinal = true;
        return ++nextUnusedOtherId;
    }

    @Override
    public void addOtherAxiTransactionsTo(List<AxiTransactionModel> otherTransactions) {
        otherTransactions.add(axiModel);
    }

    @Override
    public void addSingleRegTransactionsTo(List<AxiTransactionModel> singleRegTransactions) {

    }

    @Override
    public HashSet<String> equalsInAttributes(UltrasynthComponent other) {
        return new HashSet<String>();
    }
}

