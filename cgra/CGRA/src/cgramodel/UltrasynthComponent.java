package cgramodel;

import java.util.HashSet;
import java.util.List;

public abstract class UltrasynthComponent {
    UltrasynthComponent(String name) {
        this.name = name;
        axiModel = null;
    }

    public static final int UNKNOWN = -1;
    public final String name;

    protected AxiTransactionModel axiModel;
    protected boolean isFinal;

    public AxiTransactionModel getAxiModel() {
        return axiModel;
    }

    public abstract void addOtherAxiTransactionsTo(List<AxiTransactionModel> otherTransactions);
    public abstract void addSingleRegTransactionsTo(List<AxiTransactionModel> singleRegTransactions);

    public boolean isFinal() {
        return isFinal && axiModel != null;
    }

    /**
     * Check that the component implementing this method is equal in its attributes
     * when compared to the given {@code other} component.
     * This check is limited to the actual class fields, as all the {@link AxiTransactionModel}s
     * are derived from these these fields. All contexts are also omitted for the same reasoning!
     *
     * @param other to compare to
     * @return true if the attributes are equal
     */
    public abstract HashSet<String> equalsInAttributes(UltrasynthComponent other);

}
