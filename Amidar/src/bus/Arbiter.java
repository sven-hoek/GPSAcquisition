package bus;

import functionalunit.FunctionalUnit;

import java.util.Arrays;
/**
 * Bus Arbiter
 * based on hardware-implementation of Basic Round Robin Arbiter from Tanfer Alan 2013
 * 
 * @author Karsten Müller
 *
 */
public class Arbiter {
	private FunctionalUnit<?> [] FUs;
	
	private int currentFU=0;
	private boolean hgrant=false;
	/**
	 * Load List of FUs attached to the bus
	 * @param FUs array of FUs
	 */
	public Arbiter(FunctionalUnit<?> [] FUs) {
		this.FUs=new FunctionalUnit<?>[FUs.length];
		this.FUs=Arrays.copyOf(FUs,FUs.length);
	}
	/**
	 * Simulates one clock cycle
	 * has to called after all FUs finished their cycle
	 * @return whether this functional unit has something to do or not
	 */
	public void tick() {
		
		int status=FUs[currentFU].send(hgrant); //FU has permission to send
		if(status!=2) //if no hlock is set, currentFU lost priority
			hgrant=false;
		
		int target=currentFU;
		
		/*clock-cycle for Output Adapters of all other FUs (not currentFU) and new arbitration
		 * first FU which want to send, wins arbitration
		*/
		for(int i=0;i<FUs.length-1;i++) { 
			target=(target+1)%FUs.length;
			int busReq=FUs[target].send(false);
			if(!hgrant&&busReq>0) {
				currentFU=target;
				hgrant=true;
				break;
			}
		}
		if(!hgrant) //only currentFU wants to send
			hgrant=true;
	}
}
