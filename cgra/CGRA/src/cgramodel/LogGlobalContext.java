package cgramodel;

/**
 * This context mask is used to generate the output context required for
 * preparing the AXI transactions targeting the DDR memory of the Zynq board.
 *
 * The context has to look like this:
 *
 * 1. Entry starting a AXI transaction (setAWvalid, setBurstLength)
 * 2. Entries with ordering information (setLogID, setReadAddr)
 * 3. Back to 1 until no more log data has to be send
 * 4. A entry signaling that all transactions are complete (setDone)
 * 5. Up until the highest entry: the ccnt start value (setCcntStart)
 *
 * Note: this context does not use the CGRAs context counter.
 */
public class LogGlobalContext extends ContextMask {

	private static final long serialVersionUID = 1L;

	private final int burst_length_width;
	private int peid_width;
	private int max_read_addr_width;
	private int ccnt_width;

	/**
	 * Initiates a AXI transaction.
	 * Use this together with burst_length to start a transaction
	 */
	private int axi_awvalid;

	/**
	 * The length of a started AXI transaction
	 */
	private int burst_length;

	/**
	 * The ID of the PE Log buffer which should be read in the next cycle.
	 * Together with readAddr, an entry of this kind follows the start of an
	 * AXI transaction.
	 */
	private int id;

	/**
	 * The address of the selected PE Log buffer to read the value from
	 */
	private int readAddr;

	/**
	 * Signals that all transactions are done
	 * This should be the last entry before we start to see ccnt_start only entries.
	 */
	private int done;

	/**
	 * The context counter value which is required to be reached before the
	 * the Log output mechanism starts working and jumps to the first address
	 * of this context.
	 * Put this one into all the context entries which do not hold another value.
	 */
	private int ccnt_start;

	public LogGlobalContext(int peid_width, int max_read_addr_width, int ccnt_width) {
		super();
		this.burst_length_width = 8;
		this.peid_width = peid_width;
		this.max_read_addr_width = max_read_addr_width;
		this.ccnt_width = ccnt_width;
		createMask();
	}

	public void createMask() {
		ccnt_start = 0;
		burst_length = 0;
		readAddr = 0;
		id = max_read_addr_width;

		int combinedWidth = peid_width + max_read_addr_width;
		if (combinedWidth < burst_length_width || combinedWidth < ccnt_width) {
			if (burst_length_width < ccnt_width) {
				axi_awvalid = ccnt_width;
			} else {
				axi_awvalid = burst_length_width;
			}
		} else {
			axi_awvalid = combinedWidth;
		}
		done = axi_awvalid + 1;

		setContextWidth(done + 1);
	}

	public long setAWvalid(long context, int value) {
		return writeBitSet(context, value, axi_awvalid, 1);
	}

	public long setBurstLength(long context, int value) {
		return writeBitSet(context, value, burst_length, burst_length_width);
	}

	public long setReadAddr(long context, int value) {
		return writeBitSet(context, value, readAddr, max_read_addr_width);
	}

	public long setLogID(long context, int value) {
		return writeBitSet(context, value, id, peid_width);
	}

	public long setDone(long context, int value) { return writeBitSet(context, value, done, 1); }

	public long setCcntStart(long context, int value) {
		return writeBitSet(context, value, ccnt_start, ccnt_width);
	}

}