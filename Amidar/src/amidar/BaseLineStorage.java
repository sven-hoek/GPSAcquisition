package amidar;

public class BaseLineStorage {
	
	AmidarSimulationResult baseLong;
	AmidarSimulationResult baseShort;
	
	public boolean isBaselineAvailable(boolean isShort){
		if(isShort){
			return baseShort != null;
		} else {
			return baseLong != null;
		}
	}
	
	public AmidarSimulationResult getBaseLine(boolean isShort){
		if(isShort){
			return baseShort;
		} else {
			return baseLong;
		}
	}
	
	public void addBaseLine(boolean isShort, AmidarSimulationResult base){
		if(isShort){
			baseShort = base;
		} else {
			baseLong = base;
		}
	}

}
