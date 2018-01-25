package target;

public class Fabric {
	
	public Fabric(){
	}
	
	Technology technology;
	
	String platform;
	
	HardwareProperty optimizationFocus = HardwareProperty.NONE;

	public HardwareProperty getOptimizationFocus() {
		return optimizationFocus;
	}

	public void setOptimizationFocus(HardwareProperty optimizationFocus) {
		this.optimizationFocus = optimizationFocus;
	}

	public Technology getTechnology() {
		return technology;
	}

	public void setTechnology(Technology technology) {
		this.technology = technology;
	}

	public String getPlatform() {
		return platform;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}
	
	public boolean checkForValidity(){
		return technology.getValidHardwareProperties().contains(optimizationFocus);
	}
	
}