package cgramodel;

import static cgramodel.AxiTransactionModel.GeneralTarget.GENERAL_TARGET_OTHER;

import java.util.HashSet;
import java.util.List;

public class ActorInterface extends UltrasynthComponent {
    ActorInterface() {
        super("ActorInterface");
    }

    int actorCount = UNKNOWN;
    Context actorContext;

    /**
     * Finalise this instance.
     *
     * @param peIDWidth
     *  The width of a PE ID
     * @param contextSize
     *  The parent CGRA models context size
     * @param nextUnusedOtherId
     *  The next unused Other ID
     * @return
     *  The next unused Other ID
     */
    public int finalise(int peIDWidth, int contextSize, int nextUnusedOtherId) {
        if (actorCount == UNKNOWN) {
            System.err.println("Unable to finalise " + this.getClass().getName());
            return nextUnusedOtherId;
        }

        actorContext = new Context(actorCount, peIDWidth);

        axiModel = new AxiTransactionModel("ActorContext", actorContext.getContextWidth(), contextSize,
                GENERAL_TARGET_OTHER, nextUnusedOtherId);

        isFinal = true;
        return ++nextUnusedOtherId;
    }

    public int getActorCount() {
        return actorCount;
    }

    public void setActorCount(int actorCount) {
        this.actorCount = actorCount;
    }

    public Context getContext() {
        return actorContext;
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
        if (o == null || getClass() != o.getClass()) return new HashSet<String>() {{add("ActorInterface not same class");}};

        ActorInterface that = (ActorInterface) o;
        HashSet<String> diff = new HashSet<String>();
        if(actorCount != that.actorCount) diff.add("ActorInterface actorCount");
        return diff;
    }

    /**
     * The context mask for providing the Actor context which writes
     * the provides data to the CGRA external actors.
     *
     * Deriving from {@link InterfaceContext} enables the use of the address
     * part (selects the actor to write to) and the enable (the write enable for the actor).
     *
     * The PE ID part of this context selects which PEs output is directed to the actor.
     * SyncOut denotes that this ist the last data item send to the actor.
     */
    public class Context extends InterfaceContext {
        private static final long serialVersionUID = 1L;

        private int peIDWidth;
        private int peID;
        private int syncOut;

        public Context(int entityCount, int peIDWidth) {
            super(entityCount);
            createMask(peIDWidth);
        }

        private void createMask(int peIDWidth) {
            peID = enable;
            enable = peID + peIDWidth;
            syncOut = enable + 1;
            setContextWidth(syncOut + 1);
        }

        public int getSyncOut() {
            return syncOut;
        }

        public long setPeID(long context, int value) { return writeBitSet(context, value, peID, peIDWidth); }
        public long setSyncOut(long context, int value) { return writeBitSet(context, value, syncOut, 1); }
    }
}
