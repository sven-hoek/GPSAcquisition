package cgramodel;

import java.util.HashMap;

/**
 * Context mask for a PE.
 *
 * @author Wolf
 *
 */
public class ContextMaskPE extends ContextMask {

	/**
	*
	*/
	private static final long serialVersionUID = 6729394736505745063L;
	int muxaddrwidth = 0;
	int opcodewidth;
	int muxRegwidth;
	private int regAddrWidthWrite;
	private int regAddrWidthRead;
	int cBoxSelWidth;

	int cond_dma= -1;
	int cond_wr= -1;
	int wr_en= -1;
	int rdmuxH= -1;
	int rdmuxL= -1;
	int rddoH= -1;
	int rddoL= -1;
	int rdCacheH= -1;
	int rdCacheL= -1;
	int wrH= -1;
	int wrL= -1;
	int muxAH= -1;
	int muxAL= -1;
	int muxBH= -1;
	int muxBL= -1;
	int muxRegH= -1;
	int muxRegL= -1;
	int opH= -1;
	int opL = -1;
	int cBoxSelL = -1;
	int cBoxSelH = -1;
	
	HashMap<RegfileMuxSource,Integer> regfileMux = new HashMap<RegfileMuxSource,Integer>();

	public ContextMaskPE() {
		name = "ContextMaskPE";
	}

	public void printMask() {
		System.out.println("\n \n ");
		System.out.println("cond_dma " + cond_dma + "\n");
		System.out.println(cond_wr + "\n");
		System.out.println(wr_en + "\n");
		System.out.println("rdmuxH " + rdmuxH);
		System.out.println(rdmuxL + "\n");
		System.out.println("rddoH " + rddoH);
		System.out.println(rddoL + "\n");
		System.out.println("rdCacheH " + rdCacheH);
		System.out.println(rdCacheL + "\n");
		System.out.println("wrH " + wrH);
		System.out.println(wrL + "\n");
		System.out.println("muxAH " + muxAH);
		System.out.println(muxAL + "\n");
		System.out.println("muxBH " + muxBH);
		System.out.println(muxBL + "\n");
		System.out.println("muxRegH " + muxRegH);
		System.out.println(muxRegL + "\n");
		System.out.println("opH " + opH);
		System.out.println("\n \n");
	}

	public int getMuxwidth() {
		return muxaddrwidth;
	}

	public int getOpcodewidth() {
		return opcodewidth;
	}

	public int getRegistermuxwidth() {
		return muxRegwidth;
	}

	public int getRegAddrWidthWrite() {
		return regAddrWidthWrite;
	}
	
	public int getRegAddrWidthRead() {
		return regAddrWidthRead;
	}
	
	public int getCBoxSelWidth(){
		return cBoxSelWidth;
	}

	public int getCond_dma() {
		return cond_dma;
	}

	public int getCond_wr() {
		return cond_wr;
	}

	public int getWr_en() {
		return wr_en;
	}

	public int getRdmuxH() {
		return rdmuxH;
	}

	public int getRdmuxL() {
		return rdmuxL;
	}

	public int getRddoH() {
		return rddoH;
	}

	public int getRddoL() {
		return rddoL;
	}

	public int getRdCacheH() {
		return rdCacheH;
	}

	public int getRdCacheL() {
		return rdCacheL;
	}

	public int getWrH() {
		return wrH;
	}

	public int getWrL() {
		return wrL;
	}

	public int getMuxAH() {
		return muxAH;
	}

	public int getMuxAL() {
		return muxAL;
	}

	public int getMuxBH() {
		return muxBH;
	}

	public int getMuxBL() {
		return muxBL;
	}

	public int getMuxRegH() {
		return muxRegH;
	}

	public int getMuxRegL() {
		return muxRegL;
	}

	public int getOpH() {
		return opH;
	}

	public int getOpL() {
		return opL;
	}
	
	public int getCBoxSelL(){
		return cBoxSelL;
	}
	public int getCBoxSelH(){
		return cBoxSelH;
	}

	// public ContextMaskPE(){
	// context = new Context();
	// return writeBitSet(context,0,0,contextwidth);
	// }

	//
	// public ContextMaskPE(Number n){
	// return writeBitSet(context,(int)n.longValue(),0,contextwidth);
	// }
	//
	// public ContextMaskPE(long context,String bits){
	// return writeBitSet(context,(int)Long.parseLong(bits, 2),0,contextwidth);
	// }

	/**
	 * Initial method to create the mask
	 */
	public int createMask(PEModel pe, CgraModel model) {
		if(pe.getInputs().size()>0){
			int inputs = pe.getInputs().size() + 1;
			if(model.isPipelined()){
				inputs++;
			}
			if(model.isSecondRFOutput2ALU()){
				inputs++;
			}
			muxaddrwidth = (int) Math.ceil((Math.log(inputs) / Math.log(2))); // +1 for regouts
		}
		regAddrWidthWrite = (int) Math.ceil((Math.log(pe.getRegfilesize()) / Math.log(2))); // CCOONNSSTT
		regAddrWidthRead = regAddrWidthWrite;
		if(pe.codeConstantsInReadAddress()){
			regAddrWidthRead++;
		}
//		regAddrWidth = (int) Math.ceil((Math.log(pe.getRegfilesize()) / Math.log(2)));
		opcodewidth = 0; // +1 due to nop, because it is seen as a native ops by the model but is nonnative
		int value = 0;
		int marker = 0;
		if(pe.getAvailableNonNativeOperators().size() > 1){
			opcodewidth = (int) Math.ceil((Math.log(pe.getAvailableNonNativeOperators().size()) / Math.log(2)));
			opL = 0;
			opH = opcodewidth - 1;
			muxRegL = opH + 1;
			marker = opH;
		}
		regfileMux.put(RegfileMuxSource.ALU,value);
		value++;
		
		if(pe.getLivein()){
			regfileMux.put(RegfileMuxSource.LIVEIN,value);
			value++;
		}
		if(pe.getCacheAccess()){
			regfileMux.put(RegfileMuxSource.CACHE,value);
			value++;
		}
		if(pe.getRomAccess()){
			regfileMux.put(RegfileMuxSource.ROM,value);
			value++;
		}	
		if(value > 1){
			if(opH == -1){
				muxRegL = 0;
			}
			muxRegwidth = (int) Math.ceil((Math.log(value) / Math.log(2)));
			muxRegH = muxRegL + muxRegwidth - 1 ;
			marker = muxRegH;
		}
		cBoxSelWidth = (int)Math.ceil(Math.log(model.getcBoxModel().getCBoxPredicationOutputs())/Math.log(2));
		
		if(muxaddrwidth>0){
			if(pe.getMaxWidthInputB()>0){
				muxBL = marker + 1;
				marker = muxBH = muxBL + muxaddrwidth - 1;
			}
			if(pe.getMaxWidthInputA()>0){
				muxAL = marker + 1;
				marker = muxAH = muxAL + muxaddrwidth - 1;
			}
		}
		if(regAddrWidthWrite>0){
			wrL = marker + 1;
			marker = wrH = wrL + regAddrWidthWrite -1;
		}
		if(model.getcBoxModel().getCBoxPredicationOutputs() > 1){
			cBoxSelL = marker + 1;
			marker = cBoxSelH = cBoxSelL + cBoxSelWidth -1;
		}
		if (pe.getMemAccess() && regAddrWidthRead > 0) {
			rdCacheL = marker + 1;
			marker = rdCacheH = rdCacheL + regAddrWidthRead - 1;
		}
		
		if(regAddrWidthRead>0){
			rddoL = marker + 1;
			rddoH = rddoL + regAddrWidthRead - 1;
			rdmuxL = rddoH + 1;
			marker = rdmuxH = rdmuxL + regAddrWidthRead - 1;
		}
		wr_en = marker + 1;
		marker = cond_wr = wr_en + 1;
		if (pe.getMemAccess()) {
			marker = cond_dma = cond_wr + 1;
		}
		
		
		return contextwidth = marker + 1;
	}

	public int addrMux(long context) {
		return read(context, rdmuxL, regAddrWidthRead);
	}

	public long setAddrMux(long context, int value) {
		return writeBitSet(context, value, rdmuxL, regAddrWidthRead);
	}

	public int addrDo(long context) {
		return read(context, rddoL, regAddrWidthRead);
	}

	public long setAddrDo(long context, int value) {
		return writeBitSet(context, value, rddoL, regAddrWidthRead);
	}

	public int addrCache(long context) {
		return read(context, rdCacheL, regAddrWidthRead);
	}

	public long setAddrCache(long context, int value) {
		return writeBitSet(context, value, rdCacheL, regAddrWidthRead);
	}

	public int addrWr(long context) {
		return read(context, wrL, regAddrWidthWrite); // CCOONNSSTT
//		return read(context, wrL, regAddrWidth);
	}

	public long setAddrWr(long context, int value) {
		return writeBitSet(context, value, wrL, regAddrWidthWrite);
	}

	public int muxA(long context) {
		return read(context, muxAL, muxaddrwidth);
	}

	public long setMuxA(long context, int value) {
		return writeBitSet(context, value, muxAL, muxaddrwidth);
	}

	public int muxB(long context) {
		return read(context, muxBL, muxaddrwidth);
	}

	public long setMuxB(long context, int value) {
		return writeBitSet(context, value, muxBL, muxaddrwidth);
	}

	public int operation(long context) {
		return read(context, 0, opcodewidth);
	}

	public long setOperation(long context, int value) {
		return writeBitSet(context, value, opL, opcodewidth);
	}

	public int muxReg(long context) {
		return read(context, muxRegL, muxRegwidth);
	}

	public long setMuxReg(long context, int value) {
		return writeBitSet(context, value, muxRegL, muxRegwidth);
	}
	
	public long setMuxRegSourceBased(long context, RegfileMuxSource source) {
		return writeBitSet(context, regfileMux.get(source), muxRegL, muxRegwidth);
	}

	public boolean writeEnable(long context) {
		return read(context, wr_en, 1) == 1 ? true : false;
	}

	public long setWriteEnable(long context, int value) {
		return writeBitSet(context, value, wr_en, 1);
	}

	public long setWriteEnable(long context, boolean value) {
		if (value) {
			return writeBitSet(context, 1, wr_en, 1);
		} else {
			return writeBitSet(context, 0, wr_en, 1);
		}
	}

	public boolean writeEnableConditional(long context) {
		return read(context, cond_wr, 1) == 1 ? true : false;
	}

	public long setWriteEnableConditional(long context, int value) {
		return writeBitSet(context, value, cond_wr, 1);
	}

	public long setWriteEnableConditional(long context, boolean value) {
		if (value) {
			return writeBitSet(context, 1, cond_wr, 1);
		} else {
			return writeBitSet(context, 0, cond_wr, 1);
		}
	}

	public long setDmaConditional(long context, int value) {
		return writeBitSet(context, value, cond_dma, 1);
	}

	public long setDmaConditional(long context, boolean value) {
		if (value) {
			return writeBitSet(context, 1, cond_dma, 1);
		} else {
			return writeBitSet(context, 0, cond_dma, 1);
		}
	}

	public boolean dmaConditional(long context) {
		return read(context, cond_dma, 1) == 1 ? true : false;
	}
	
	public int cBoxSel(long context) {
		if(cBoxSelWidth == 0){
			return 0;
		}
		return read(context, cBoxSelL, cBoxSelWidth);
	}

	public long setCBoxSel(long context, int value) {
//		System.out.println("setting csel to " + value+ Long.toHexString(context));
		long ct = writeBitSet(context, value, cBoxSelL, cBoxSelWidth);
//		System.out.println("          ->    " + Long.toHexString(ct));
		return ct;
	}
}
