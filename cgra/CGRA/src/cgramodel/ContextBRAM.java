package cgramodel;

public class ContextBRAM extends ContextMask {

	private static final long serialVersionUID = 1L;
	
	protected int bramAddr;
	protected int bramAddrWidth;
	protected int bramID;
	protected int bramIDWidth;
	protected int enable;

	public ContextBRAM(int bramCount, int bramSize) {
		super();
		createMask(bramCount, bramSize);
	}

	private void createMask(int bramCount, int bramSize) {
		bramAddr = 0;
		bramAddrWidth = (int) Math.ceil(Math.log(bramSize) / Math.log(2));
		bramID = bramAddrWidth;
		bramIDWidth = 0 == bramCount ? 0 : (int) Math.ceil(Math.log(bramCount) / Math.log(2));
		enable = bramAddrWidth + bramIDWidth;
		setContextWidth(enable + 1);
	}

	public int getBramAddr() {
		return bramAddr;
	}

	public int getBramAddrWidth() {
		return bramAddrWidth;
	}
	
	public int getBramID() {
		return bramID;
	}

	public int getBramIDWidth() {
		return bramIDWidth;
	}

	public int getEnable() {
		return enable;
	}
}