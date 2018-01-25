package cgramodel;

import util.SimpleMath;

import static cgramodel.AxiTransactionModel.GeneralTarget.GENERAL_TARGET_OTHER;

import java.util.HashSet;
import java.util.List;

public class ComUnit extends UltrasynthComponent {
    ComUnit() {
        super("ComUnit");
    }

    /**
     * Size of the ID Context, which maps incoming
     * Parameter IDs to internal PE/RF addresses.
     */
    private int idcSize = UNKNOWN;

    /**
     * ID Context address width directly calculated from {@link #idcSize}
     */
    private int idcAddrWidth = UNKNOWN;

	private int idcWidth = UNKNOWN;

    /**
     * Finalise this instance.
     *
     * @param maxRegFileAddrWidth
     *  Maximum Register File address width of the parent CGRA model.
     * @param peIDWidth
     *  Width of a PE ID
     * @param nextUnusedOtherId
     *  The next unused Other ID
     * @return
     *  The next unused Other ID
     */
    public int finalise(int maxRegFileAddrWidth, int peIDWidth, int nextUnusedOtherId) {
        if (idcSize == UNKNOWN) {
            System.err.println("Unable to finalise " + this.getClass().getName());
            return nextUnusedOtherId;
        }

		idcWidth = maxRegFileAddrWidth + peIDWidth;

        idcAddrWidth = SimpleMath.checkedLog(idcSize);
        axiModel = new AxiTransactionModel("IDC", idcAddrWidth, idcSize, GENERAL_TARGET_OTHER, nextUnusedOtherId);

        isFinal = true;
        return ++nextUnusedOtherId;
    }

    public int getIDCSize() {
        return idcSize;
    }

    public void setIDCSize(int size) {
        this.idcSize = size;
    }

    public int getIDCAddrWidth() {
        return idcAddrWidth;
    }

	public int getIDCWidth() {
        return idcWidth;
    }

    @Override
    public void addOtherAxiTransactionsTo(List<AxiTransactionModel> otherTransactions) {
        otherTransactions.add(axiModel);
    }

    @Override
    public void addSingleRegTransactionsTo(List<AxiTransactionModel> singleRegTransactions) {

    }

    @Override
    public HashSet<String> equalsInAttributes(UltrasynthComponent o) {
    	if (this == o) return new HashSet<String>();
        if (o == null || getClass() != o.getClass()) return new HashSet<String>() {{add("ComUnit not same class");}};

        ComUnit comUnit = (ComUnit) o;
        HashSet<String> diff = new HashSet<String>();
        if (idcSize != comUnit.idcSize) diff.add("ComUnit idcSize");
        if (idcAddrWidth != comUnit.idcAddrWidth) diff.add("ComUnit idcAddrWidth");
        if(idcWidth != comUnit.idcWidth) diff.add("ComUnit idcWidth");
        return diff;
    }

}
