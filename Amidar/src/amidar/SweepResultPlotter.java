package amidar;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroupFile;

import tracer.Trace;
import amidar.sweep.SweepConfig;

public class SweepResultPlotter {
	
	String SEPERATOR = "\t";
	
	public SweepResultPlotter(){
	}

	public static void main(String[] args) {
//		LinkedHashMap<String,LinkedHashSet<String>> sweepInfo = new LinkedHashMap<>();
//		
//		LinkedHashSet<String> apps = new LinkedHashSet<>();
//		apps.add("AES_long");
//		apps.add("AES_short");
//		apps.add("DES_long");
//		apps.add("DES_short");
//		
//		LinkedHashSet<String> cgras = new LinkedHashSet<>();
//		
//		cgras.add("16");
//		cgras.add("165");
//		
//		LinkedHashSet<String> unroll = new LinkedHashSet<>();
//		
//		unroll.add("3");
//		unroll.add("4");
//		
//		LinkedHashSet<String> benchScale = new LinkedHashSet<>();
//		
//		benchScale.add("large");
//		benchScale.add("small");
//		
//		sweepInfo.put("applications", apps);
//		sweepInfo.put("CGRA", cgras);
//		sweepInfo.put("UNROLL", unroll);
//		sweepInfo.put("scale", benchScale);
//		
//		double [] results = {0,1,2,3,4,5,6,7
//				,8,9,10,11,12,13,14,15
//				,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31
//				};
//		
//		SweepResultPlotter plotter = new SweepResultPlotter();
//		
//		plotter.configurePlotter("UNROLL", "applications", false);
//		
//		plotter.plot(sweepInfo, results, new Trace(System.out, System.in, "albert", "einstein"), null);
		
		SweepResultPlotter plotter = new SweepResultPlotter();
		
//		String basePath = "jpeg2";
		String basePath = "log/blub2";
		
		boolean v2 = args[0].equals( "2");
		
//		plotter.setPath("log/test");
//		plotter.setPath("mySweepWithoutPrefetch");
		plotter.setPath(basePath);
		if(!v2){
			plotter.configurePlotter("application", "PREFETCHING", false);
		} else {
			plotter.configurePlotter("application", "PREFETCHING", false);
		}
		plotter.load();
		
//		String path = "mySweep/plot/";
//		String path = "log/test/plot/";
//		String path = "mySweepWithoutPrefetch/plot/";
		String path = basePath +"/plot/";
		
		for(String val: plotter.results.keySet()){
			System.out.println("VAL: " + val);
			String vall = val.split("\\.")[0];
			plotter.plot(plotter.sweepInfo, plotter.results.get(val), new Trace(System.out, System.in, "asdf", "asdf"), path, vall);
		}
		
		if(!v2){
			plotter.plotTEXv1(path);
		} else {
			plotter.plotTEXv2(path);
		}

	}
	
	boolean configured = false;
	String primarySweepParameter;
	String secondarySweepParameter;
	boolean averageOverApplications;
	boolean averageDone = false;
	
	public void configurePlotter(String primarySweepParameter, String secondarySweepParameter, boolean averageOverApplications){
		this.primarySweepParameter = primarySweepParameter;
		this.secondarySweepParameter = secondarySweepParameter;
		this.averageOverApplications = averageOverApplications;
		if(averageOverApplications && (primarySweepParameter == "applications" || secondarySweepParameter == "applications")){
			throw new RuntimeException("What do you want average over \"applications\" or list the \"applications\" in the table?");
		}
		configured = true;
	}
	
	double [] maxSpeedup;
	
	public void plot(LinkedHashMap<String,LinkedHashSet<String>> sweepInfoOrig, double[] results, Trace trace, String filePath, String baseFileName){
		
		LinkedHashMap<String,LinkedHashSet<String>> sweepInfo = new LinkedHashMap<>(sweepInfoOrig);
		
		if(!configured){
			averageOverApplications = true;
			Iterator<String> it = sweepInfo.keySet().iterator();
			String param = it.next();
			while(param.equals("application") && it.hasNext()){
				param = it.next();
			}
			if(!param.equals("application")){
				primarySweepParameter = param;
			}
			if(it.hasNext()){
				param = it.next();
				while(param.equals("application") && it.hasNext()){
					param = it.next();
				}
				if(!param.equals("application")){
					secondarySweepParameter = param;
				}
			}
		}
		
		////////////////////////////////
		// Averaging				  //
		////////////////////////////////
		if(averageOverApplications){
			String parameter = "application";
			results = averageOverParameter(parameter, sweepInfo, results);
			sweepInfo.remove(parameter);
		}
		
		
		////////////////////////////////
		// Preparation				  //
		////////////////////////////////
		
		boolean noSecondaryParameter = false;
		
		if(sweepInfo.size() == 1){
			noSecondaryParameter = true;
		}
		
		int nrOfResults = results.length;
		
		int rows = sweepInfo.get(primarySweepParameter).size();
		
		int cols = 1;
		if(!noSecondaryParameter){
			cols = sweepInfo.get(secondarySweepParameter).size();
		}
		
		int nrOfFiles = nrOfResults/rows/cols;
		
		LinkedHashMap<String,Integer> indexMappingPrimary = new LinkedHashMap<>();
		LinkedHashMap<String,Integer> indexMappingSecondary = new LinkedHashMap<>();
		
		int index = 0;
		for(String paramName: sweepInfo.get(primarySweepParameter)){
			indexMappingPrimary.put(paramName, index++);
		}
		
		if(!noSecondaryParameter){
			index = 0;
			for(String paramName: sweepInfo.get(secondarySweepParameter)){
				indexMappingSecondary.put(paramName, index++);
			}
		}
		int primaryIndex = -1;
		int secondaryIndex = -1;
		
		index = 0;
		for(String parameter: sweepInfo.keySet()){
			if(parameter.equals(primarySweepParameter)){
				primaryIndex = index;
			} else if(!noSecondaryParameter && parameter.equals(secondarySweepParameter)) {
				secondaryIndex = index;
			}
			index++;
		}
		int dimensions = sweepInfo.size();
		
		
		////////////////////////////////
		// Create Configs  			  //
		////////////////////////////////
		
		String [][] configs = new String[nrOfResults][dimensions];
		
		int[] dimensionSizes = new int[dimensions];
		String[] dimensionNames = sweepInfo.keySet().toArray(new String[0]);
		
		int[] bundleSizes = new int[dimensions];
		
		for(int dim = 0; dim < dimensions; dim++){
			dimensionSizes[dim] = sweepInfo.get(dimensionNames[dim]).size();
		}
		
		bundleSizes[dimensions-1] = 1;
		
		for(int dim = dimensions-2; dim >= 0; dim--){
			bundleSizes[dim] = dimensionSizes[dim+1] * bundleSizes[dim+1];
		}
		
		int dimPointer = 0;
		
		for(String param: sweepInfo.keySet()){
			index = 0;
			LinkedHashSet<String> params = sweepInfo.get(param);
			String[] paramInstances = params.toArray(new String[0]);
			int nrOfParamInstances = paramInstances.length;
			
			int bundleSize = bundleSizes[dimPointer];
			int nrOfBundles = results.length/bundleSize;
			int nrOfClusters = nrOfBundles/nrOfParamInstances;
			

			
			
			for(int cluster = 0; cluster < nrOfClusters; cluster++){
				for(int paramInstance = 0; paramInstance < nrOfParamInstances; paramInstance++){
					for(int bndl = 0; bndl < bundleSize; bndl++){
						configs[index++][dimPointer] = paramInstances[paramInstance];
					}
				}
			}
			
			
			
			
			dimPointer++;
			
		}
		
		
//		for(int res =0; res < nrOfResults; res++){
//			for(int dimm = 0; dimm < dimensions; dimm++ ){
//				System.out.print(configs[res][dimm] + "\t");
//			}System.out.println(":\t"+results[res]);
//		}
		
		
		
		
		
		LinkedHashMap<String,double[][]> tables = new LinkedHashMap<>();
		
		double [] maxValues = new double[cols];
		
		
		for(int result = 0; result < nrOfResults; result++){
			String fileName = baseFileName + "_SweepResult";
			
			int col = 0;
			int row = 0;
			
			Iterator<String> it = sweepInfo.keySet().iterator();

			for(int dim = 0; dim < dimensions; dim++){
				String conf = configs[result][dim];
				if(dim == primaryIndex){
					row = indexMappingPrimary.get(conf);
					it.next();
				} else if(dim == secondaryIndex){
					col = indexMappingSecondary.get(conf);
					it.next();
				} else {
					fileName = fileName + "_" + it.next()+"="+ conf;
				}
			}
			
			double[][] table = tables.get(fileName);
			if(table == null){
				table = new double[rows][cols];
				tables.put(fileName, table);
			}
			table[row][col] = results[result];
			if(results[result]> maxValues[col]){
				maxValues[col] = results[result];
			}
		}
		
		if(baseFileName.equals("speedup")){
			this.maxSpeedup = maxValues;
		}
		
		
		
		for(String fileName : tables.keySet()){
			trace.println(fileName);
			double[][] tab = tables.get(fileName);
			trace.println();
			StringBuilder line = new StringBuilder(primarySweepParameter+ SEPERATOR);
//			trace.print(primarySweepParameter+ SEPERATOR);
			if(!noSecondaryParameter){
				for(String p : sweepInfo.get(secondarySweepParameter)){
					String[] pp = p.split("/");
					p = pp[pp.length-1];
					line.append(secondarySweepParameter+"="+p + SEPERATOR);
				}
			}
			trace.println(line.toString());
			
			Iterator<String> it = sweepInfo.get(primarySweepParameter).iterator();
			
			for(int row = 0; row < rows; row++){
				line = new StringBuilder(it.next() + SEPERATOR);
//				trace.print(it.next() + SEPERATOR);
				for(int col = 0; col < cols; col++){
					
					line.append(tab[row][col] + SEPERATOR);
				}trace.println(line.toString());
			}trace.println();
			trace.println("------------------------------------------------------------------------");
			
			
		}
		
		
		
		if(filePath != null){
			try{
			for(String fileName : tables.keySet()){
				File file = new File(filePath);
				file.mkdirs();
				FileWriter fw = new FileWriter(filePath+fileName+".txt");
				BufferedWriter bw = new BufferedWriter(fw);
				
				double[][] tab = tables.get(fileName);
				StringBuilder line = new StringBuilder("nr\t" + primarySweepParameter+ SEPERATOR);
//				trace.print(primarySweepParameter+ SEPERATOR);
				if(!noSecondaryParameter){
					for(String p : sweepInfo.get(secondarySweepParameter)){
						String[] pp = p.split("/");
						p = pp[pp.length-1];
						line.append(secondarySweepParameter+":"+p + SEPERATOR);
					}
				} else {
					line.append("value");
				}
				bw.write(line.toString()+"\n");
				
				Iterator<String> it = sweepInfo.get(primarySweepParameter).iterator();
				
				for(int row = 0; row < rows; row++){
					String appp = it.next();
					String[] appa = appp.split("/");
					appp = appa[appa.length-1];
					line = new StringBuilder(row+"\t"+appp + SEPERATOR);
//					trace.print(it.next() + SEPERATOR);
					for(int col = 0; col < cols; col++){
						
						line.append(tab[row][col] + SEPERATOR);
					}
					bw.write(line.toString()+"\n");
				}
				bw.close();
				
			}
			} catch(IOException e){
				e.printStackTrace(System.err);
			}
		}
		
		
		
		
	}
	
	public double [] averageOverParameter(String parameter, LinkedHashMap<String,LinkedHashSet<String>> sweepInfo, double [] results){
		double [] averagedResults;
		int dimensions = sweepInfo.size();
		int[] dimensionSizes = new int[dimensions];
		String[] dimensionNames = sweepInfo.keySet().toArray(new String[0]);
		
		int[] bundleSizes = new int[dimensions];
		int parameterDim = -1;
		
		for(int dim = 0; dim < dimensions; dim++){
			dimensionSizes[dim] = sweepInfo.get(dimensionNames[dim]).size();
			if(dimensionNames[dim].equals(parameter)){
				parameterDim = dim;
			}
		}
		
		bundleSizes[dimensions-1] = 1;
		
		for(int dim = dimensions-2; dim >= 0; dim--){
			bundleSizes[dim] = dimensionSizes[dim+1] * bundleSizes[dim+1];
		}
		
		int nrOfParameterItems = dimensionSizes[parameterDim];
		int averagedResultsSize = results.length/nrOfParameterItems;
		
		int bundleSize = bundleSizes[parameterDim];
		int nrOfBundles = results.length/bundleSize;
		int nrOfClusters = nrOfBundles/nrOfParameterItems;
		int clusterSize = bundleSize*nrOfParameterItems;
		
		averagedResults = new double[averagedResultsSize];
		
		int index = 0;
		
		for(int cluster = 0; cluster < nrOfClusters; cluster++){
			for(int i = 0; i < bundleSize; i++){
				double val = 0;
				for(int param = 0; param < nrOfParameterItems; param++){
					val += results[cluster*clusterSize + i + param*bundleSize];
				}
				averagedResults[index++] = val/nrOfParameterItems;
			}
		}
		
		
		
		return averagedResults;
	}
	
	String path = "";
	
	public void saveSweepInfo(LinkedHashMap<String, LinkedHashSet<String>> sweepInfo){
		new File(path).mkdirs();

		try{
			FileWriter fw = new FileWriter(path+"/SweepInfo.txt");
			BufferedWriter bw = new BufferedWriter(fw);

			for(String parameter: sweepInfo.keySet()){
				StringBuilder line = new StringBuilder(parameter+"\t");
				for(String paramInstance: sweepInfo.get(parameter)){
					line.append(paramInstance+"\t");
				}
				bw.write(line.toString()+"\n");
			}

			bw.close();
		}catch(IOException e){
			e.printStackTrace(System.err);
		}
	}

	public void setPath(String path) {
		this.path = path;
		
	}

	public void saveResults(double[] results, String name) {
		new File(path).mkdirs();

		try{
			FileWriter fw = new FileWriter(path+"/" + name + ".txt");
			BufferedWriter bw = new BufferedWriter(fw);

			for(double d : results){
				bw.write(d+"\n");
			}

			bw.close();
		}catch(IOException e){
			e.printStackTrace(System.err);
		}
	}
	
	public void saveResults(int[] results, String name){
		double[] dResults = new double[results.length];
		for(int i = 0; i < results.length; i++){
			dResults[i] = results[i];
		}
		saveResults(dResults, name);
	}
	
	public void saveResults(long[] results, String name){
		double[] dResults = new double[results.length];
		for(int i = 0; i < results.length; i++){
			dResults[i] = results[i];
		}
		saveResults(dResults, name);
	}
	
	
	public double[] loadResults(String name){
		
		ArrayList<Double> resList = new ArrayList<>();
		try{
			FileReader fis = new FileReader(path + "/" + name);
			BufferedReader bis = new BufferedReader(fis);
			
			String line = bis.readLine();
			int i = 0;
			
			while(line != null){
//				result[i++] = Double.parseDouble(line);
				resList.add(Double.parseDouble(line));
				line = bis.readLine();
			}
			
			bis.close();
		} catch(IOException e){
			e.printStackTrace(System.err);
		}
		
		double[] result = new double[resList.size()];
		
		for(int i = 0; i < result.length; i++){
			result[i] = resList.get(i);
		}
		
		return result;
	}
	
	public LinkedHashMap<String, LinkedHashSet<String>> loadSweepInfo(){
		LinkedHashMap<String, LinkedHashSet<String>> result = new LinkedHashMap<>();
		
		try{
			FileReader fis = new FileReader(path + "/SweepInfo.txt");
			BufferedReader bis = new BufferedReader(fis);
			
			String line = bis.readLine();
			while(line!=null){
				String[] elements = line.split("\t");
				LinkedHashSet<String> parameterInstances = new LinkedHashSet<>();
				for(int i = 1; i < elements.length; i++){
					parameterInstances.add(elements[i]);
				}
				result.put(elements[0], parameterInstances);
				line = bis.readLine();
			}
			bis.close();
		} catch(IOException e){
			e.printStackTrace(System.err);
		}
		
		
		return result;
	}
	
	
	LinkedHashMap<String, LinkedHashSet<String>> sweepInfo;
	LinkedHashMap<String, double[]> results;
	File[] files;
	
	public void load(){
		sweepInfo = loadSweepInfo();
		
		results = new LinkedHashMap<>();
		File folder = new File(path);
		
		File[] filesAndDirs = folder.listFiles();
		int fileCNT = 0;
		for(File ff: filesAndDirs){
			if(!ff.isDirectory()){
				fileCNT++;
			}
		}
		files = new File[fileCNT];
		fileCNT = 0;
		for(File ff: filesAndDirs){
			if(!ff.isDirectory()){
				files[fileCNT++] = ff;
			}
		}
		
		
		for(int i = 0; i < files.length; i++){
			String fileName = files[i].getName();
			if(!fileName.equals("SweepInfo.txt")){
				String name = fileName;//.split("\\.")[0];
				double [] res = loadResults(name);
				results.put(name,res);
			}
			
		}
		
		
	}
	
	public void plotTEXv1(String path){
		STGroupFile group = new STGroupFile("src/amidar/plotv1.stg", '§', '§');
		ST template = group.getInstanceOf("document");

//		double[][] speedup = results.get("speedup.txt");
		
		int nrOfApps = sweepInfo.get(secondarySweepParameter).size();
		
		int cc = 0;
		
		double scaleTo = 75;
		
		for(String fullAppName: sweepInfo.get(secondarySweepParameter)){
			ST templatePic = group.getInstanceOf("pic");
			String[] appN = fullAppName.split("/");
			templatePic.add("appName", appN[appN.length-1]);
			templatePic.add("scalePlot", scaleTo/maxSpeedup[cc]);
			templatePic.add("scaleXaxis", nrOfApps);
			templatePic.add("scaleYaxis", 100*maxSpeedup[cc]/scaleTo);
			template.add("pictures", templatePic.render());
			
			ST templatePic2 = group.getInstanceOf("pic2");
			templatePic2.add("appName", appN[appN.length-1]);
			templatePic2.add("scaleXaxis", nrOfApps);
			template.add("pictures",templatePic2.render());
			
			System.out.println(fullAppName + " ----------- " + maxSpeedup[cc]);
			cc++;
		}
		
		try{
		FileWriter fw = new FileWriter(path+ "pictures.tex");
		BufferedWriter bw = new BufferedWriter(fw);
		
		bw.write(template.render());
		
		bw.close();
		} catch(IOException e){
			e.printStackTrace(System.err);
		}
		
		try{
		FileWriter fw = new FileWriter(path+ "Makefile");
		BufferedWriter bw = new BufferedWriter(fw);
		
		String mf = "%.pdf: %.tex \n\tlatex $<\n\tdvips $(<:.tex=.dvi)\n\tps2pdf $(<:.tex=.ps) $@\n\t\n\trm -f $(<:.tex=.dvi) $(<:.tex=.dvi) $(<:.tex=.log) $(<:.tex=.aux) $(<:.tex=.ps) def.aux";
		
		
		bw.write(mf);
		
		bw.close();
		} catch(IOException e){
			e.printStackTrace(System.err);
		}
		
		
	}
	
	
	public void plotTEXv2(String path){
		STGroupFile group = new STGroupFile("src/amidar/plotv3.stg", '§', '§');
		ST template = group.getInstanceOf("document");

//		double[][] speedup = results.get("speedup.txt");
		
		
		
		
		for(File ff: files){
			
			
			String name = ff.getName();
			if(name.contains("SweepInfo")){
				continue;
			}
			
			name = name.split("\\.")[0];
			
			ST templatePic = group.getInstanceOf("pic");
			templatePic.add("name", name);
			template.add("pictures", templatePic.render());
			
			
		}
		
		try{
		FileWriter fw = new FileWriter(path+ "pictures.tex");
		BufferedWriter bw = new BufferedWriter(fw);
		
		bw.write(template.render());
		
		bw.close();
		} catch(IOException e){
			e.printStackTrace(System.err);
		}
		
		try{
		FileWriter fw = new FileWriter(path+ "Makefile");
		BufferedWriter bw = new BufferedWriter(fw);
		
		String mf = "%.pdf: %.tex \n\tlatex $<\n\tdvips $(<:.tex=.dvi)\n\tps2pdf $(<:.tex=.ps) $@\n\t\n\trm -f $(<:.tex=.dvi) $(<:.tex=.dvi) $(<:.tex=.log) $(<:.tex=.aux) $(<:.tex=.ps) def.aux";
		
		
		bw.write(mf);
		
		bw.close();
		} catch(IOException e){
			e.printStackTrace(System.err);
		}
		
		
	}
	

}
