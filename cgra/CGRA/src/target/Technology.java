package target;

import java.util.HashSet;
import java.util.Set;

public enum Technology {
	FPGA(new HashSet<HardwareProperty>() {{
		add(HardwareProperty.Delay);
		add(HardwareProperty.LUT);
		add(HardwareProperty.BRAM);
		add(HardwareProperty.DSP);
		add(HardwareProperty.PowerConsumption);
		}}),
	
	ASIC(new HashSet<HardwareProperty>() {{
		add(HardwareProperty.Delay);
		add(HardwareProperty.AREA);
		add(HardwareProperty.SRAM);
		add(HardwareProperty.PowerConsumption);
		}});

	Technology(HashSet<HardwareProperty> validHWProperties){
		validHardwareProperties = validHWProperties; 
	}
	
	Set<HardwareProperty> validHardwareProperties;
	
	public Set<HardwareProperty> getValidHardwareProperties(){
		return validHardwareProperties;
	}
	
	public static Technology getTechnologyByName(String name){
		for(Technology tech : Technology.values()){
			if(name.compareToIgnoreCase(tech.toString()) == 0){
				return tech;
			}
		}
		return null;
	}
}