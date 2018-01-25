package generator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import cgramodel.CgraModel;
import cgramodel.ContextMask;
import cgramodel.PEModel;
import graph.CDFG;
import graph.LG;
import io.ObjectSerializer;
import scheduler.LPW;
import scheduler.MissingOperationException;
import scheduler.NotSchedulableException;
import scheduler.RCListSched;
import scheduler.Schedule;
import target.Processor;

public class TestbenchContextGenerator {

	Schedule schedule;
	CgraModel model;
	long[][] contextsPE;
	long[] contextsCBox;
	long[] contextsControlUnit;
	String appname = "not known";

	public TestbenchContextGenerator(CgraModel model) {
		this.model = model;
	}

	public void importApp(String application) {
		appname = application;
		File graphfolder = new File(Processor.Instance.getApplicationPath());
		File[] list = graphfolder.listFiles();
		String dcfgname = "";
		String lgname = "";
		String nrofvariablesname = "";

		for (File file : list) {
			if (file.getName().contains(appname)) {
				if (file.getName().contains("DCFG")) {
					dcfgname = file.toString();
				}
				if (file.getName().contains("LG")) {
					lgname = file.toString();
				}
				if (file.getName().contains("localVariables")) {
					nrofvariablesname = file.toString();
				}
			}
		}
		ObjectSerializer rudi = new ObjectSerializer();
		CDFG dcfg = (CDFG) rudi.deserialize(dcfgname);

		try {
			File file = new File("dotvondcfg");
			FileWriter fw = new FileWriter(file);
			fw.write(dcfg.toString());
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		LG lg = (LG) rudi.deserialize(lgname);
		int nrofvariables = (int) rudi.deserialize(nrofvariablesname);

		RCListSched listSched = null;
		try {
			listSched = new RCListSched(dcfg, lg, nrofvariables, model, new LinkedHashMap<Object,LinkedHashMap<Integer,LinkedHashSet<Integer>>>(),null);
		} catch (MissingOperationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		LPW lpw = new LPW(dcfg, null);
		listSched.setPriorityCritereon(lpw);
		try {
			schedule = listSched.schedule();
		} catch (NotSchedulableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		listSched.draw("nope.sch");

		listSched.registerAllocation();
		listSched.cBoxAllocation();
		listSched.ctxtGeneration();

		contextsPE = listSched.getContextsPE();
//		contextsCBox = listSched.getContextsCBox(); TTOODDOO
		contextsControlUnit = listSched.getContextsControlUnit();

		// exportContext(contextsPE, contextsCBox, contextsControlUnit);

	}

	public void exportContext() {
		if (contextsPE != null && contextsCBox != null && contextsControlUnit != null) {
			exportContext(contextsPE, contextsCBox, contextsControlUnit);
		} else {
			System.err.println("trying to use the exportContext method without having created contexts");
		}
	}

	public void exportContext(long[][] contextsPE, long[] contextsCBox, long[] contextsControlUnit) {

		ContextMask trick = new ContextMask();

		try {
			for (PEModel pe : model.getPEs()) {
				trick.setContextWidth(pe.getContextWidth());
				BufferedWriter bw = new BufferedWriter(
						new FileWriter(Processor.Instance.getHardwareDestinationPath()
								+ "/Context_PE" + pe.getID() + ".dat"));
				for (int entry = 0; entry < contextsPE[pe.getID()].length; entry++) {
					bw.write(trick.getBitString(contextsPE[pe.getID()][entry]) + "\n");
				}
				bw.close();
			}
			BufferedWriter bw = new BufferedWriter(
					new FileWriter(Processor.Instance.getHardwareDestinationPath() + "/"
							+ "Context_CBOX.dat"));
			trick.setContextWidth(model.getcBoxModel().getContextmaskEvaLuationBlocks().getContextWidth());
			for (int entry = 0; entry < contextsCBox.length; entry++) {
				bw.write(trick.getBitString(contextsCBox[entry]) + "\n");
			}
			bw.close();
			trick.setContextWidth(model.getContextmaskccu().getContextWidth());
			bw = new BufferedWriter(
					new FileWriter(Processor.Instance.getHardwareDestinationPath() + "/"
							+ "Context_CCU.dat"));
			for (int entry = 0; entry < contextsControlUnit.length; entry++) {
				bw.write(trick.getBitString(contextsControlUnit[entry]) + "\n");
			}
			bw.close();

		} catch (IOException e) {
			System.err.println("Error while exporting contexts for APP :" + appname);
		}
	}

	protected int scheduleLength() {
		return schedule.length();
	}

}
