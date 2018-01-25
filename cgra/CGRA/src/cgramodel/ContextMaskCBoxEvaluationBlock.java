package cgramodel;

/**
 * Context mask for the CBox
 * 
 * @author Wolf
 *
 */
public class ContextMaskCBoxEvaluationBlock extends ContextMask {

	/**
	 *
	 */
	private static final long serialVersionUID = 2416308937928095259L;
	private int inputMuxWidth = -1;
	private int addrWidth= -1;
	private int writeEnable= -1;
	private int inputMuxH= -1;
	private int inputMuxL= -1;
	private int wAddrPositiveH= -1;
	private int wAddrPositiveL= -1;
	private int wAddrNegativeH= -1;
	private int wAddrNegativeL= -1;
	private int rAddrOrPositiveH= -1;
	private int rAddrOrPositiveL= -1;
	private int rAddrOrNegativeH= -1;
	private int rAddrOrNegativeL= -1;
	private int rAddrPredicationH= -1;
	private int rAddrPredicationL= -1;
	private int bypassOrPositive= -1;
	private int bypassOrNegative= -1;
	private int bypassAndPositive= -1;
	private int bypassAndNegative= -1;

	private int[] inputmapping;

	public boolean debug = false;

	public ContextMaskCBoxEvaluationBlock() {
		name = "ContextMaskCBox";
	}

	public int getSlot(int id) {
		return inputmapping[id];
	}

	public int getInputMuxWidth() {
		return inputMuxWidth;
	}

	public int getAddrWidth() {
		return addrWidth;
	}

	public int getWriteEnable() {
		return writeEnable;
	}

	public int getInputMuxH() {
		return inputMuxH;
	}

	public int getInputMuxL() {
		return inputMuxL;
	}

	public int getWAddrPositiveH() {
		return wAddrPositiveH;
	}

	public int getWAddrPositiveL() {
		return wAddrPositiveL;
	}

	public int getWAddrNegativeH() {
		return wAddrNegativeH;
	}

	public int getWAddrNegativeL() {
		return wAddrNegativeL;
	}

	public int getRAddrOrPositiveH() {
		return rAddrOrPositiveH;
	}

	public int getRAddrOrPositiveL() {
		return rAddrOrPositiveL;
	}

	public int getRAddrOrNegativeH() {
		return rAddrOrNegativeH;
	}

	public int getRAddrOrNegativeL() {
		return rAddrOrNegativeL;
	}

	public int getRAddrPredicationH() {
		return rAddrPredicationH;
	}

	public int getRAddrPredicationL() {
		return rAddrPredicationL;
	}

	public int getBypassOrPositive() {
		return bypassOrPositive;
	}

	public int getBypassOrNegative() {
		return bypassOrNegative;
	}

	public int getBypassAndPositive() {
		return bypassAndPositive;
	}

	public int getBypassAndNegative() {
		return bypassAndNegative;
	}

	/**
	 * Method to create the mask.
	 */
	public int createMask(CgraModel model) {

		int ports = 0;
		inputmapping = new int[model.getNrOfPEs()];
		for (int i = 0; i < model.getPEs().size(); i++) {
			if (model.getPEs().get(i).getControlFlow()) {
				inputmapping[i] = ports;
				ports++;
			}
		}
		if(ports == 0){
			System.out.println(" !!! Error in composition - there is no pe with controlflow !!!" );
		}

		if (ports > 1) {
			inputMuxWidth = (int) Math.ceil(Math.log(ports) / Math.log(2));		
			} else {
			inputMuxWidth = 0;
		}
		addrWidth = (int) Math.ceil(Math.log(model.getcBoxModel().getMemorySlots()) / Math.log(2));
		bypassAndNegative = 0;
		bypassAndPositive = bypassAndNegative + 1;
		bypassOrNegative = bypassAndPositive + 1;
		bypassOrPositive = bypassOrNegative + 1;
		rAddrOrPositiveL = bypassOrPositive + 1;
		rAddrOrPositiveH = rAddrOrPositiveL + addrWidth - 1;
		rAddrOrNegativeL = rAddrOrPositiveH + 1;
		rAddrOrNegativeH = rAddrOrNegativeL + addrWidth - 1;
		rAddrPredicationL = rAddrOrNegativeH + 1;
		rAddrPredicationH = rAddrPredicationL + addrWidth*model.getcBoxModel().getCBoxPredicationOutputsPerBox() - 1;
		wAddrNegativeL = rAddrPredicationH + 1;
		wAddrNegativeH = wAddrNegativeL + addrWidth - 1;
		wAddrPositiveL = wAddrNegativeH + 1;
		wAddrPositiveH = wAddrPositiveL + addrWidth - 1;
		if (ports > 1) {
			inputMuxL = wAddrPositiveH + 1;
			inputMuxH = inputMuxL + inputMuxWidth - 1;
			writeEnable = inputMuxH + 1;
		}
		else{
			writeEnable = wAddrPositiveH + 1;
		}
		contextwidth = writeEnable + 1;
		// printMask();
		return contextwidth;
	}

	
	public int getContextWidth(){
		return contextwidth;
	}

//	public void printMask() {
//		 System.out.print(write_enable+ " ");
//		 System.out.print(write_enable+ " ");
//		 System.out.print(muxH+ " ");
//		 System.out.print(muxL+ " ");
//		 System.out.print(adrH + " ");
//		 System.out.print(adrL+ " ");
//		 System.out.print(regNH+ " ");
//		 System.out.print(regNL+ " ");
//		 System.out.print(regH+ " ");
//		 System.out.print(0 + " \n");
//		 System.out.println(OUTL + " " + OUTH + " "+ bypassBAnd + " " +
//		 bypassAAnd+ " " + bypassBOr+ " " + bypassAOr + " " + " " + raddraL +
//		 " "
//		 + raddraH + " " + raddrbL + " " + raddrbH + " " +waddrBL + " " +
//		 waddrBH + " " + waddrAL + " "
//		 + waddrAH + " " + muxL + " " + muxH + " " + write_enable + "\n");
//		 System.out.println(" [" + 1 + "] "+ " [" + muxwidth + "] "+" [" +
//		 addrwidth + "] " +"[" + (regNH-regNL-1) + "] " +"[" + (regNH-regNL-1)
//		 + "]");
//		 System.out.println(" WREN" + " MUX" + " ADR " + " NEG" + " REG");
//		 System.out.println("\n");
//	}

	public boolean writeEnable(long context) {
		return read(context, writeEnable, 1) == 1 ? true : false;
	}

	public long setWriteEnable(long context, boolean value) {
		if (debug) {
			System.out.println("setting write enable - " + value);
		}

		if (value) {
			return writeBitSet(context, 1, writeEnable, 1);
		} else {
			return writeBitSet(context, 0, writeEnable, 1);
		}
	}

	public int inputMux(long context) {
		if (inputMuxWidth == 0) {
			System.err.println("trying to mux cbox inputs whithout an actual multiplexer");
		}
		return read(context, inputMuxL, inputMuxWidth);
	}

	public long setInputMux(long context, int value) {
		if (debug) {
			System.out.println("setting mux - " + value);
		}
		return writeBitSet(context, value, inputMuxL, inputMuxWidth);
	}

	public int readAddressOrPositive(long context) {
		return read(context, rAddrOrPositiveL, addrWidth);
	}

	public int readAddressOrNegative(long context) {
		return read(context, rAddrOrNegativeL, addrWidth);
	}

	public int readAddressPredication(long context, int port) {
		return read(context, rAddrPredicationL+addrWidth*port, addrWidth);
	}

	public long setReadAddressOrPositive(long context, int value) {
		if (debug) {
			System.out.println("setting address b1 - " + value);
		}
		return writeBitSet(context, value, rAddrOrPositiveL, addrWidth);
	}

	public long setReadAddressOrNegative(long context, int value) {
		if (debug) {
			System.out.println("setting address b2 - " + value);
		}
		return writeBitSet(context, value, rAddrOrNegativeL, addrWidth);
	}

	public long setReadAddressPredication(long context, int value, int port) {
		if (debug) {
			System.out.println("setting address a - " + value);
		}
//		System.out.println("SET RADDA: " + value + " port " + port + " ctxt: " + Long.toHexString(context));
		
		long newc = writeBitSet(context, value, rAddrPredicationL+addrWidth*port, addrWidth);
//		System.out.println(" -------> " + Long.toHexString(newc));
		return newc;
	}

	public long setWriteAddressPositive(long context, int value) {
		if (debug) {
			System.out.println("setting address write a - " + value);
		}
		return writeBitSet(context, value, wAddrPositiveL, addrWidth);
	}

	public long setWriteAddressNegative(long context, int value) {
		if (debug) {
			System.out.println("setting address write b- " + value);
		}
		return writeBitSet(context, value, wAddrNegativeL, addrWidth);
	}

	public int writeAddressPositive(long context) {
		return read(context, wAddrPositiveL, addrWidth);
	}

	public int writeAddressNegative(long context) {
		return read(context, wAddrNegativeL, addrWidth);
	}

	public boolean bypassAndPositive(long context) {
		return read(context, bypassAndPositive, 1) == 1 ? true : false;
	}

	public boolean bypassAndNegative(long context) {
		return read(context, bypassAndNegative, 1) == 1 ? true : false;
	}

	public long setBypassAndPositive(long context, boolean value) {
		if (debug) {
			System.out.println("setting bypass a and - " + value);
		}
		if (value) {
			return writeBitSet(context, 1, bypassAndPositive, 1);
		} else {
			return writeBitSet(context, 0, bypassAndPositive, 1);
		}
	}

	public long setBypassAndNegative(long context, boolean value) {
		if (debug) {
			System.out.println("setting bypass b and - " + value);
		}
		if (value) {
			return writeBitSet(context, 1, bypassAndNegative, 1);
		} else {
			return writeBitSet(context, 0, bypassAndNegative, 1);
		}
	}

	public boolean bypassOrPositive(long context) {
		return read(context, bypassOrPositive, 1) == 1 ? true : false;
	}

	public boolean bypassOrNegative(long context) {
		return read(context, bypassOrNegative, 1) == 1 ? true : false;
	}

	public long setBypassOrPositive(long context, boolean value) {
		if (debug) {
			System.out.println("setting bypass a or - " + value);
		}
		if (value) {
			return writeBitSet(context, 1, bypassOrPositive, 1);
		} else {
			return writeBitSet(context, 0, bypassOrPositive, 1);
		}
	}

	public long setBypassOrNegative(long context, boolean value) {
		if (debug) {
			System.out.println("setting bypass b or - " + value);
		}
		if (value) {
			return writeBitSet(context, 1, bypassOrNegative, 1);
		} else {
			return writeBitSet(context, 0, bypassOrNegative, 1);
		}
	}

}
