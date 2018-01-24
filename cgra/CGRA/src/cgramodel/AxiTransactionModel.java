package cgramodel;

public class AxiTransactionModel {

    public static final int AXI_DATA_WIDTH = 32;
    public static final int UNUSED_ID = -1;

    public enum SingleRegTag {
        CgraStateChange,
        LogDest,
        LogDestBound,
        LogDestInc,
        OCMDest,
        OCMDestBound,
        OCMDestInc,
        IntervalLength,
        ExpectedParameterCount,
        SpecialActionCounterResetValue,
    }

    /**
     * Possible Operations targeting the Ultrasynth CGRA.
     */
    public enum GeneralTarget {
        GENERAL_TARGET_PE,
        GENERAL_TARGET_PARAMETER,
        GENERAL_TARGET_OTHER,
        GENERAL_TARGET_SINGLE_REG,
    }

    public AxiTransactionModel(String name, int valueWidth, int maxValueCount,
                               GeneralTarget op, int id) {
        this.name = name;
        this.valueTransferCount = calculateTransferCount(valueWidth);
        this.maxValueCount = maxValueCount;
		this.valueWidth = valueWidth;
        this.generalTarget = op;
        this.id = id;
    }

    /**
     * The name of the target of this transaction.
     */
    public final String name;

    /**
     * Number of transfers needed to send a single value. This value is 0 biased,
     * meaning that 0 indicates that one transfer is required. This is due to the fact
     * that we are mostly interested in how many more transfers than one are required
     * to send a data item of this kind.
     *
     * Example:
     * Depending on the AXI WDATA signal width (e.g. 32 bits) a value send to the
     * Slave interface may be too big: a 33 bits value needs 2 transfers to be send,
     * assuming a WDATA signal width of 32 bits. (resulting in a value of 1 of this field)
     */
    public final int valueTransferCount;

    /**
     * The maximal amount of values to send.
     * E.g. Context size of 256 means that 256 values need to be send
     * to completely fill it.
     */
    public final int maxValueCount;

	/**
     * The write operation associated with this AxiTransactionModel.
     */
    public final GeneralTarget generalTarget;

	/**
     * The actual width of the data send in each transfer
     */
    public final int valueWidth;

    /**
     * The ID of this AxiTransactionModel.
     * (e.g. PE ID, LogPe ID, Other ID, ...)
     */
    public final int id;

    /**
     * @return the amount of transactions required to fill the entire context
     */
    public int maxTransferCount() {
        return (valueTransferCount + 1) * maxValueCount;
    }

    private int calculateTransferCount(int valueWidth) {
        return (int) Math.floor((valueWidth - 1) / AXI_DATA_WIDTH);
    }
}
