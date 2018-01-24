package target;


public enum HardwareProperty {

	// General Attributes
	NONE(),
	PowerConsumption(),
	Delay(),
	
	// FPGA
	LUT(),
	BRAM(),
	DSP(),
	Register(),

	// Standard Cell
	SRAM(),
	AREA();

	public static HardwareProperty getHardwarePropertyByName(String name){

		for(HardwareProperty hwp : HardwareProperty.values()){
			if(name.compareToIgnoreCase(hwp.toString()) == 0){
				return hwp;
			}
		}
		return null;
	}

}
