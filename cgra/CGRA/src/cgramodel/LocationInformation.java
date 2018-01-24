package cgramodel;

public class LocationInformation {
	
	int registerFileAddress = 0;
	
	int mux = 0;
	
	int liveOut = 0;
	
	boolean [] PESelect = null;
	boolean send = false;
	
	public LocationInformation(int nrOfPes) {
		PESelect = new boolean[nrOfPes];
	}

	public int getRegisterFileAddress() {
		return registerFileAddress;
	}

	public void setRegisterFileAddress(int registerFileAddress) {
		this.registerFileAddress = registerFileAddress;
	}

	public boolean[] getPESelect() {
		return PESelect;
	}

	public void setPESelect(boolean[] pESelect) {
		PESelect = pESelect;
	}

	public int getMux() {
		return mux;
	}

	public void setMux(int mux) {
		this.mux = mux;
	}

	public int getLiveOut() {
		return liveOut;
	}

	public void setLiveOut(int liveout) {
		send = true;
		this.liveOut = liveout;
	}
	
	public boolean isSendInfo(){
		return send;
	}
	

}
