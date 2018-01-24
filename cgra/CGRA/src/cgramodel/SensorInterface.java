package cgramodel;

import static cgramodel.AxiTransactionModel.GeneralTarget.GENERAL_TARGET_OTHER;

import java.util.HashSet;
import java.util.List;

public class SensorInterface extends UltrasynthComponent {
    SensorInterface() {
        super("SensorInterface");
    }

    private int sensorCount = UNKNOWN;
    InterfaceContext context;

    /**
     * Finalises this instance.
     *
     * @param contextSize
     *  Parent CGRA model context size
     * @param nextUnusedOtherId
     *  The next unused Other Id
     * @return
     *  The next unused Other Id
     */
    public int finalise(int contextSize, int nextUnusedOtherId) {
        if (sensorCount == UNKNOWN) {
            System.err.println("Unable to finalise " + this.getClass().getName());
            return nextUnusedOtherId;
        }

        context = new InterfaceContext(sensorCount);
        axiModel = new AxiTransactionModel("SensorContext", context.getContextWidth(), contextSize,
                GENERAL_TARGET_OTHER, nextUnusedOtherId);

        isFinal = true;
        return ++nextUnusedOtherId;
    }

    public int getSensorCount() {
        return sensorCount;
    }

    public void setSensorCount(int sensorCount) {
        this.sensorCount = sensorCount;
    }

    public InterfaceContext getContext() {
        return context;
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
         if (o == null || getClass() != o.getClass()) return new HashSet<String>() {{add("SensorInterface not same class");}};

        SensorInterface that = (SensorInterface) o;
        HashSet<String> diff = new HashSet<String>();
        if(sensorCount != that.sensorCount) diff.add("SensorInterface sensorCount");
        return diff;
    }

}
