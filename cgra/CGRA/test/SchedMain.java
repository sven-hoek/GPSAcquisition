import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import cgramodel.CgraModel;
import cgramodel.PEModel;
import graph.CDFG;
import graph.IDP;
import graph.LG;
import graph.Loop;
import graph.Node;
import io.AttributeParserUltrasynth;
import scheduler.MissingOperationException;
import scheduler.NotSchedulableException;
import scheduler.RCListSched;
import scheduler.Schedule;
import target.UltraSynth;

/**
 * @author ruschke
 * Testklasse für den Scheduler
 */
public class SchedMain {

	public static void main(String[] args) throws MissingOperationException, NotSchedulableException {
		target.Processor.Instance = UltraSynth.Instance;
		
//		IDP graph = IDP.parseAndOptimize("dsc/Minimal.ultrasynth.json", true, 0.01);
		IDP graph = IDP.parseAndOptimize("dsc/DoublePendulum.ultrasynth.json", true, 0.01);
//		IDP graph = IDP.parseAndOptimize("dsc/LinearDriveControl.ultrasynth.json", true, 0.01);
//		IDP graph = IDP.parseAndOptimize("dsc/SinglePendulumHcs.ultrasynth.json", true, 0.01);
		
//		CgraModel cgra = new AttributeParserUltrasynth().loadCgra("/home/ruschke/git/ultrasynth/cgra/CGRA/config/ultrasynth/tComp_4.json");
//		CgraModel cgra = new AttributeParserUltrasynth().loadCgra("/home/ruschke/git/ultrasynth/cgra/CGRA/config/ultrasynth/tComp_16.json");
		CgraModel cgra = new AttributeParserUltrasynth().loadCgra("/home/ruschke/git/ultrasynth/cgra/CGRA/config/ultrasynth/tComp_64.json");

		LG lg = lgBuilder(graph);
		
		CDFG printGraph = (CDFG) graph;
		
		try {
			FileWriter fw = new FileWriter("CDFG.dot");
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(printGraph.toString());
			bw.close();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		RCListSched scheduler = new RCListSched(graph, lg, 0, cgra);
		
		long t1 = System.currentTimeMillis();
		Schedule sched = scheduler.schedule();
		scheduler.drawSched("sched.dot");
		scheduler.registerAllocation();
		scheduler.cBoxAllocation();
		scheduler.ctxtGeneration();
		scheduler.ctxtGenerationUltrasynth();
		long t2 = System.currentTimeMillis();
		
		int cBoxSlots = 0,currSlot;
		
		for (Node nd : scheduler.cBoxAlloc.keySet()) {
			currSlot = scheduler.cBoxAlloc.get(nd);
			if (currSlot > cBoxSlots) cBoxSlots = currSlot;
		}
		
		System.out.println("CBox slots: " + cBoxSlots);
		System.out.println("Runtime: " + (t2-t1) + " ms");
		System.out.println("Schedule length: " + sched.length());
		System.out.println("Nodes: " + graph.getNodes().size());
	}
	
	/**
	 * Generiert von-Neumann Nachbarschaftsliste für quadratische CGRAs.
	 * Nur fürs schnelle Ausfüllen. Wandert später in einen Generator.
	 * @param width	Kantenlänge des CGRAs
	 */
	@SuppressWarnings("unused")
	private static void interconnect(int width) {
		int nPE = width*width;
		Set<Integer> sources = new TreeSet<>();
		
		for (int x=0; x<width; x++) {
			for (int y=0; y<width; y++) {
				addToSources(sources, x, y-1, width);
				addToSources(sources, x-1, y, width);
				addToSources(sources, x+1, y, width);
				addToSources(sources, x, y+1, width);
				
				System.out.println("\t\"" + (x+y*width) + "\" : " + sources + ",");
				sources.clear();
			}
		}
	}
	
	/**
	 * Hilfsmethode für den Interconnect
	 * @param sources Liste von Quell-PEs
	 * @param x Koordinate
	 * @param y Koordinate
	 * @param width Kantenlänge des CGRAs
	 */
	private static void addToSources(Set<Integer> sources, int x, int y, int width) {
		if (x >= 0 && x < width)
			if (y >= 0 && y < width)
				sources.add(x+y*width);
	}

	/**
	 * Nur zur schnellen Veranschaulichung des Interconnects von quadratischen CGRAs
	 * @param cgra
	 */
	@SuppressWarnings("unused")
	public static void printCGRA(CgraModel cgra) {
		try {
			FileWriter fw = new FileWriter("CGRA.dot");
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter pw = new PrintWriter(bw);
			
			pw.println("digraph CGRA {");
			for (int i=0; i<cgra.getPEs().size(); i++) {
//				pw.println("\"" + i + "\"[pos=\"" + (double)(i%8) + "," + (double)(i/8) + "!\"];");
				//"PE0"[shape="box", style="filled", color="#00222222", pos="0.0,845.0!", height="1.5", width="1.5"];
				for (PEModel source : cgra.getPEs().get(i).getInputs()) {
					pw.println("\"" + source.getID() + "\" -> \"" + i + "\";");
				}
			}
			pw.println("}");
			
			pw.flush();
			bw.close();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
	private static void writeToFile(String msg, String filename) {
		try {
			FileWriter fw = new FileWriter(filename);
			fw.write(msg);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates a simple Loopgraph for Ultrasynth. All nodes belong to one big loop
	 * @param graph Graph to create the Loopgraph for
	 * @return Loopgraph
	 */
	private static LG lgBuilder(CDFG graph) {
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		int nAddr;
		
		for (Node nd : graph) {
			nAddr = nd.getAddress();
			if (min > nAddr) min = nAddr;
			if (max > nAddr) max = nAddr;
		}
		
		LG lg = new LG();
		lg.addLoop(new Loop(min, max), new LinkedHashSet<>(), null);
		return lg;
	}
}
