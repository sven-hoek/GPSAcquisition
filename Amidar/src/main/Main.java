package main;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import cgramodel.CgraModel;
import cgramodel.ContextMaskCBoxEvaluationBlock;
import cgramodel.ContextMaskContextControlUnit;
import cgramodel.ContextMaskPE;
import tracer.TraceManager;
import functionalunit.*;
import functionalunit.opcodes.CgraOpcodes;
import functionalunit.opcodes.FaluOpcodes;
import functionalunit.opcodes.HeapOpcodes;
import functionalunit.opcodes.IaluOpcodes;




public class Main {


	/**
	 * @param args
	 */

	public static void main(String args[]) {
		
		JSONParser parser;
        JSONObject config = null;
        parser = new JSONParser();
        FileReader fileReader;
        try {
                fileReader = new FileReader(args[0]);
                config = (JSONObject) parser.parse(fileReader);
        } catch (FileNotFoundException e) {
                System.err.println("No config file found");
                e.printStackTrace(System.err);
        } catch (IOException e) {
                System.err.println("Error while reading config file" );
                e.printStackTrace(System.err);
        } catch (ParseException e) {
                System.err.println("Error while reading config file");
                e.printStackTrace(System.err);
        }

        TraceManager traceManager = new TraceManager();

        IALU ialu = new IALU((String)config.get("IALU"), traceManager);

        FALU falu = new FALU((String)config.get("FALU"), traceManager);
        
//        ObjectHeap objectHeap = new ObjectHeap(null, (String)config.get("HEAP"), traceManager, false);
        
        


        System.out.println("Energieverbrauch ialu.IADD: "+ialu.getEnergy(IaluOpcodes.IADD));
        System.out.println("Energieverbrauch falu.FADD: "+falu.getEnergy(FaluOpcodes.FLOAT_ADD));

        ialu.setOperand(FunctionalUnit.OPERAND_A_LOW, 4, 0);
        ialu.setOperand(FunctionalUnit.OPERAND_B_LOW, 5, 0);

        ialu.executeOp(IaluOpcodes.IMUL); // hieß früher determineResult

        System.out.println("ialu.IMUL Ergebnis bei Eingabe 4 und 5: "+ialu.getResult(FunctionalUnit.RESULT_LOW));

        falu.executeOp(FaluOpcodes.FLOAT_MUL);
        System.out.println("ialu Instanz: "+ialu.toString());
        System.out.println("falu Instanz: "+falu.toString());
        
//        System.out.println("duration heap " + objectHeap.getDuration(HeapOpcodes.READ_64));
//        System.out.println("Anzahl Operanden für ialu.IADD: "+ialu.getOperandNumber(IALUopcodes.IADD)); //hieß früher determineOperandNumber
}
	
	public static void tb1(CGRA cgra, long[][] cpe, long[] cb, long cu[], 
			List<Integer> values, List<Integer> PEs, List<Integer> addresses, 
			List<Integer> PEsS, List<Integer> addressesS,
			HashMap<Integer, List<Integer>> lvPE, HashMap<Integer, List<Integer>> lvAddr){
		cgra.newSynthesisContext(cpe, 0);
//		cgra.contextcbox.setContext(cb, 0);
		cgra.controlunit.newSynthesis(cu, 0);
		
		int[] heap = new int[200000];
		
		heap[5] = 1;
		heap[3] = 1092;
		heap[12] = 2148;
		
		// ADPCM decode input
		byte[] inBytes  = {27,-12,-30,-45,26,50,-24,-5,17,-29,-10,-62,3,-91,4,109,-14,-85,20,0,-23,-101,21,-37,-16,0,7,55,0,23,0,28,0,50,0,51,0,30,0,21,0,21,0,51,0,75,0,85,0,86,0,44,0,28,0,50,-121,15,53,-15,-13,35,-126,86,19,61,-60,69,-16,84,124,-122,-51,52,43,-83,113,33,-26,99,-94,-112,55,77,-93,-118,103,77,81,-96,70,-52,-110,64,117,24,-50,-55,-52,-98,79,-110,-106,72,-97,50,40,-24,31,0,99,96,-40,91,-110,63,65,34,-94,115,78,89,50,62,-97,-103,-9,73,-27,-28,-84,92,73,-14,116,76,-53,-82,50,-106,113,83,21,1,-96,102,53,16,22,34,-26,-44,-116,-111,35,5,99,-44,-73,33,-50,99,-95,37,85,72,-85,5,8,104,14,68,51,83,32,-114,-28,64,6,-8,-90,45,83,9,-126,103,21,80,96,-94,98,8,-38,-110,9,-102,-29,-59,-127,-104,32,65,9,-103,-122,81,-11,42,35,64,-82,44,-99,-66,-21,57,-82,-49,-51,78,-94,65,-79,-72,-71,83,16,-75,67,36,65,-32,66,-72,28,-67,-40,22,124,79,-121,18,-7,-76,-75,-48,-40,-98,9,52,100,-122,-87,8,-87,-52,86,65,9,37,123,-52,73,-18,-59,115,-32,-96,76,-63,102,77,8,-56,-20,-37,2,-83,80,14,-37,-68,-36,-72,83,127,48,104,-119,-8,19,20,14,-63,-100,-55,22,67,98,-126,60,-59,-93,-47,84,-14,-3,22,-28,-59,-97,-39,121,-22,-89,69,11,92,-128,61,9,-114,-104,-44,-95,102,-111,57,46,52,84,49,-109,2,108,23,113,60,-41,4,-46,-126,9,44,100,-58,57,70,-100,-120,-109,21,26,-114,-90,60,-56,96,-62,-125,-96,-47,84,-15,-40,-19,25,56,-78,11,52,-112,48,85,26,-115,18,106,-55,1,33,26,-55,18,-82,66,-92,86,8,-103,36,10,-30,-77,78,103,113,35,79,36,-13,13,89,-101,-117,106,25,14,75,-55,69,122,8,-82,-62,-101};
		
		
//		heap[0] = 1;
//		heap[1] = 2;
//		heap[2] = 3;
//		heap[3] = 4;
//		heap[4] = 5;
//		heap[5] = 6;
//		heap[6] = 7;
//		heap[7] = 8;
//		heap[8] = 9;
//		heap[9] = 0;
		
		// lvar init für ADPCMn
//		adpcmn init values lv:  888 -> arrray ref
//
//		00	888 	PE 1.23 PE 7.0					-> 0
//		01	1024	PE 2.8
//		2	14	PE 0.12 1.15
//		3	4	PE 0.10
//		4	888	PE 7.2 1.41
//		5	0
//		6	0
//		7	0
//		8	0
//		9	0
//		10	0
//		11	0
//		12	0
//		13	8	PE 1.8
//		14	888	PE 1.17 8.1
//		15	888	PE 1.27 7.1 8.0
//		16	416	PE 0.6
//		17	14	PE 1.18 2.3 
		
//		int [] localVar = new int[1000];

		//ADPCMn decode localvars
		int [] localVar = {
				0,			//0
				1024,		//1
				14,			//2
				4,			//3
				460,		//4
				0,			//5
				0,			//6
				0,			//7
				0,			//8
				0,			//9
				0,			//10
				0,			//11
				0,			//12
				8,			//13
				416,		//14
				430,		//15
				416,		//16
				14			//17
		};
		//zero localvars
//		int [] localVar = {
//		0,			//0
//		0,		//1
//		0,			//2
//		1,			//3
//		0,		//4
//		0,			//5
//		0,			//6
//		0,			//7
//		0,			//8
//		0,			//9
//		0,			//10
//		0,			//11
//		0,			//12
//		0,			//13
//		0,		//14
//		0,		//15
//		0,		//16
//		0			//17
//};
		
//		int [] localVar = {
//			0,
//			0,
//			0,
//			0,
//			0,
//			0,
//			109200,
//			0,
//			-1,
//			32,
//			32,
//			0,
//			100,
//			1124,
//			0,
//			0,
//			0,
//			0,
//			0,
//			0,
//			0,
//			0,
//			0,
//			0,
//			0,
//			0,
//		};
		
		
		for(int i:lvPE.keySet()){
			for(int j = 0; j< lvPE.get(i).size(); j++){
				cgra.PEs[lvPE.get(i).get(j)].regfile.registers[lvAddr.get(i).get(j)] = localVar[i];
//				System.out.println("\tStoring lvar "+ i + " To PE "+(lvPE.get(i).get(j))+"."+lvAddr.get(i).get(j) + " : "+ localVar[i]);
			}
		}
		

		
		//initialize heap with inbiytes
		for(int i = 0; i< inBytes.length; i++){
			heap[i] = inBytes[i];
		}
		
		
		
		
		
		//initialize consts
		for(int i = 0; i<values.size();i++){
			cgra.PEs[PEs.get(i)].regfile.registers[addresses.get(i)]=values.get(i);
//			System.out.println("\tstoring const " + values.get(i) + " To PE" + PEs.get(i) + "." + addresses.get(i) );
		}
		
		
//		cgra.PEs[0].regfile.registers[0] = 1; 	// const 1	
//		cgra.PEs[0].regfile.registers[1] = 10;	// const 10
//		cgra.PEs[1].regfile.registers[1] = 99;	// lvar 1
//		cgra.PEs[2].regfile.registers[0] = 3;
		
		
		for(int i = 0; i<cgra.InputCacheValid.length; i++){
			cgra.InputCacheValid[i] = true;
		}
//		System.out.println("ZERO PC: "+cgra.controlunit.getProgramCounter());
		cgra.tick();

		long startTime = System.nanoTime();
		
		cgra.setToken(CgraOpcodes.RUN, 0,0, null, 0);
		cgra.setTokenValid(true);
//		cgra.InputTokenValid = true;
		cgra.inputValid[cgra.OPERAND_A_LOW] = true;
		cgra.setOperand(cgra.OPERAND_A_LOW, 0,0);
//		System.out.println("ZERO PC: "+cgra.controlunit.getProgramCounter());
		cgra.tick();
//		System.out.println("ZERO PC: "+cgra.controlunit.getProgramCounter());
		cgra.tick();
//		cgra.InputTokenValid = false;
		cgra.inputValid[cgra.OPERAND_A_LOW] = false;
		
		int cnt = 0;
		
		int simulatedCacheVal = 0;
//		
//		System.out.println(cgra.controlunit.getProgramCounter());
//		for(int i =0; i<3000;i++){	
//			cgra.operate();
//		}
//		while(cgra.PEs[0].regfile.registers[0] >= 0){
		while(cgra.controlunit.getProgramCounter() == cgra.contextsize-1){
//			System.out.println("a "+cnt++ + " PC: "+cgra.controlunit.getProgramCounter());
			cgra.tick();
		}
		while(cgra.controlunit.getProgramCounter() != cgra.contextsize-1){
//			System.out.println("b "+cnt++ + " PC: "+cgra.controlunit.getProgramCounter());
			cgra.tick();
			boolean acc = false;
			for(int i = 0; i< cgra.OutputCacheValid.length; i++){ ///simulate DMA
				cgra.InputCacheData[i] = 0;

				if(cgra.OutputCacheValid[i]){
					
					if(cgra.OutputCacheWrite[i]){
//						System.out.println("DMA"+i+ " writing value "+cgra.OutputCacheData[i] + " to heap Address "+cgra.cacheBaseAddress[i] + " + " + cgra.cacheOffset[i]);
						heap[cgra.cacheBaseAddress[i] + cgra.cacheOffset[i]] = cgra.OutputCacheData[i];
					}else{
						acc = true;
						cgra.InputCacheData[i] = heap[cgra.cacheBaseAddress[i] + cgra.cacheOffset[i]];
//						System.out.println("DMA"+i+" reading value "+heap[cgra.cacheBaseAddress[i] + cgra.cacheOffset[i]] + " from heap Address "+cgra.cacheBaseAddress[i] + " + " + cgra.cacheOffset[i]);
					}
				
				}
				
			}
//		////sim stall
//			if(acc && simulatedCacheVal%3==0){
//				System.out.println("b1 "+cnt++ + " PCr: "+cgra.controlunit.getProgramCounter());
//				cgra.operate();
//				cgra.InputCacheValid[0] = false;
//				System.out.println("b1 "+cnt++ + " PCf: "+cgra.controlunit.getProgramCounter());
//				cgra.operate();
//				cgra.InputCacheValid[0] = true;
//				System.out.println("b1a "+cnt++ + " PCf: "+cgra.controlunit.getProgramCounter());
//				cgra.operate();
//			}
//			////sim stall ende
		}
		long stopTime = System.nanoTime();
		System.err.println("cycles emulated : " + cgra.cycle);
		
	    long elapsedTime = stopTime - startTime;
	    System.err.println("elapsed time in nanoseconds " + elapsedTime);
	  //report back:
	    for(int i = 0; i<PEsS.size();i++){
	    	System.err.println("\t\tPE "+PEsS.get(i)+ "."+addressesS.get(i)+": "+cgra.PEs[PEsS.get(i)].regfile.registers[addressesS.get(i)]);
	    }
	    
//	    int high = cgra.PEs[0].regfile.registers[1];
//	    int low = cgra.PEs[0].regfile.registers[2];
//	    long val = (((long)high)<<32) + (((long)low)&0xFFFFFFFFL);
//		System.err.println("VAL: "+val);
	    
	    
	    System.out.println("HEAP results:");
	    for(int i = 460; i< heap.length; i++){
	    	System.out.print(heap[i]+",");
	    }System.out.println();
	    
	    
	}
	
	public static void tbVerilog(CGRA cgra, long[][] cpe, long[] cb, long cu[], 
			List<Integer> values, List<Integer> PEs, List<Integer> addresses, 
			List<Integer> PEsS, List<Integer> addressesS,
			HashMap<Integer, List<Integer>> lvPE, HashMap<Integer, List<Integer>> lvAddr){
		
		
		// ADPCM decode input
		byte[] inBytes  = {27,-12,-30,-45,26,50,-24,-5,17,-29,-10,-62,3,-91,4,109,-14,-85,20,0,-23,-101,21,-37,-16,0,7,55,0,23,0,28,0,50,0,51,0,30,0,21,0,21,0,51,0,75,0,85,0,86,0,44,0,28,0,50,-121,15,53,-15,-13,35,-126,86,19,61,-60,69,-16,84,124,-122,-51,52,43,-83,113,33,-26,99,-94,-112,55,77,-93,-118,103,77,81,-96,70,-52,-110,64,117,24,-50,-55,-52,-98,79,-110,-106,72,-97,50,40,-24,31,0,99,96,-40,91,-110,63,65,34,-94,115,78,89,50,62,-97,-103,-9,73,-27,-28,-84,92,73,-14,116,76,-53,-82,50,-106,113,83,21,1,-96,102,53,16,22,34,-26,-44,-116,-111,35,5,99,-44,-73,33,-50,99,-95,37,85,72,-85,5,8,104,14,68,51,83,32,-114,-28,64,6,-8,-90,45,83,9,-126,103,21,80,96,-94,98,8,-38,-110,9,-102,-29,-59,-127,-104,32,65,9,-103,-122,81,-11,42,35,64,-82,44,-99,-66,-21,57,-82,-49,-51,78,-94,65,-79,-72,-71,83,16,-75,67,36,65,-32,66,-72,28,-67,-40,22,124,79,-121,18,-7,-76,-75,-48,-40,-98,9,52,100,-122,-87,8,-87,-52,86,65,9,37,123,-52,73,-18,-59,115,-32,-96,76,-63,102,77,8,-56,-20,-37,2,-83,80,14,-37,-68,-36,-72,83,127,48,104,-119,-8,19,20,14,-63,-100,-55,22,67,98,-126,60,-59,-93,-47,84,-14,-3,22,-28,-59,-97,-39,121,-22,-89,69,11,92,-128,61,9,-114,-104,-44,-95,102,-111,57,46,52,84,49,-109,2,108,23,113,60,-41,4,-46,-126,9,44,100,-58,57,70,-100,-120,-109,21,26,-114,-90,60,-56,96,-62,-125,-96,-47,84,-15,-40,-19,25,56,-78,11,52,-112,48,85,26,-115,18,106,-55,1,33,26,-55,18,-82,66,-92,86,8,-103,36,10,-30,-77,78,103,113,35,79,36,-13,13,89,-101,-117,106,25,14,75,-55,69,122,8,-82,-62,-101};
//		int[] inBytes  = {27,-12,1092,1092,26,1,-24,-5,17,-29,-10,-62,2148,-91,4,109,-14,-85,20,0,-23,-101,21,-37,-16,0,7,55,0,23,0,28,0,50,0,51,0,30,0,21,0,21,0,51,0,75,0,85,0,86,0,44,0,28,0,50,-121,15,53,-15,-13,35,-126,86,19,61,-60,69,-16,84,124,-122,-51,52,43,-83,113,33,-26,99,-94,-112,55,77,-93,-118,103,77,81,-96,70,-52,-110,64,117,24,-50,-55,-52,-98,79,-110,-106,72,-97,50,40,-24,31,0,99,96,-40,91,-110,63,65,34,-94,115,78,89,50,62,-97,-103,-9,73,-27,-28,-84,92,73,-14,116,76,-53,-82,50,-106,113,83,21,1,-96,102,53,16,22,34,-26,-44,-116,-111,35,5,99,-44,-73,33,-50,99,-95,37,85,72,-85,5,8,104,14,68,51,83,32,-114,-28,64,6,-8,-90,45,83,9,-126,103,21,80,96,-94,98,8,-38,-110,9,-102,-29,-59,-127,-104,32,65,9,-103,-122,81,-11,42,35,64,-82,44,-99,-66,-21,57,-82,-49,-51,78,-94,65,-79,-72,-71,83,16,-75,67,36,65,-32,66,-72,28,-67,-40,22,124,79,-121,18,-7,-76,-75,-48,-40,-98,9,52,100,-122,-87,8,-87,-52,86,65,9,37,123,-52,73,-18,-59,115,-32,-96,76,-63,102,77,8,-56,-20,-37,2,-83,80,14,-37,-68,-36,-72,83,127,48,104,-119,-8,19,20,14,-63,-100,-55,22,67,98,-126,60,-59,-93,-47,84,-14,-3,22,-28,-59,-97,-39,121,-22,-89,69,11,92,-128,61,9,-114,-104,-44,-95,102,-111,57,46,52,84,49,-109,2,108,23,113,60,-41,4,-46,-126,9,44,100,-58,57,70,-100,-120,-109,21,26,-114,-90,60,-56,96,-62,-125,-96,-47,84,-15,-40,-19,25,56,-78,11,52,-112,48,85,26,-115,18,106,-55,1,33,26,-55,18,-82,66,-92,86,8,-103,36,10,-30,-77,78,103,113,35,79,36,-13,13,89,-101,-117,106,25,14,75,-55,69,122,8,-82,-62,-101};
		
		
		//ADPCMn decode localvars
		int [] localVar = {
		0,			//0
		1024,		//1
		14,			//2
		4,			//3
		460,		//4
		0,			//5
		0,			//6
		0,			//7
		0,			//8
		0,			//9
		0,			//10
		0,			//11
		0,			//12
		8,			//13
		416,		//14
		430,		//15
		416,		//16
		14			//17
};
		
		
//		int [] localVar = new int[1000];
		
//		int [] localVar = {
//				0,
//				0,
//				0,
//				0,
//				0,
//				0,
//				109200,
//				0,
//				-1,
//				32,
//				32,
//				0,
//				100,
//				1124,
//				0,
//				0,
//				0,
//				0,
//				0,
//				0,
//				0,
//				0,
//				0,
//				0,
//				0,
//				0,
//			};
		
		
		File file = new File("cgras/CGRA/CGRA1_Verilog/sim/sim.v");
		try {
			FileWriter fw = new FileWriter(file);
			BufferedWriter bw;
			bw = new BufferedWriter(fw);
			File template;
			template = new File ("cgras/CGRA/CGRA1_Verilog/sim/sim_template.v");
			FileReader fr = new FileReader(template);
			BufferedReader br = new BufferedReader(fr);

			String line = br.readLine();


			while(! line.contains("AUTOGENSTART")){
				bw.write(line+ "\n");
				line = br.readLine();
			}
			bw.write(line+ "\n");
		

			for(int i:lvPE.keySet()){
				for(int j = 0; j< lvPE.get(i).size(); j++){
//					cgra.PEs[lvPE.get(i).get(j)].regfile.registers[lvAddr.get(i).get(j)] = localVar[i];
					bw.write("    dut.pe"+lvPE.get(i).get(j)+".regfile.memory["+lvAddr.get(i).get(j)+"] = " + localVar[i]+ ";\n");
//					System.out.println("\tStoring lvar "+ i + " To PE "+(lvPE.get(i).get(j))+"."+lvAddr.get(i).get(j) + " : "+ localVar[i]);
				}
				bw.write("\n");
			}
			bw.write("\n");
			

			System.out.println("hier kommei ch doch net vorbei ????");
			
			for(int i = 0; i < cpe.length; i++){
				ContextMaskPE mask = cgra.getModel().getPEs().get(i).getContextMaskPE();
				for(int j = 0; j< cpe[0].length; j ++){
					bw.write("    dut.PE["+i+"].context.memory["+j+"] = "+mask.getContextWidth()+"'b"+mask.getBitString(cpe[i][j])+";\n");
				}
				bw.write("\n");
			}
			bw.write("\n");
			
			
			
			ContextMaskCBoxEvaluationBlock cboxmask = cgra.getModel().getcBoxModel().getContextmaskEvaLuationBlocks();
			ContextMaskContextControlUnit ccumask = cgra.getModel().getContextmaskccu();
			for(int i = 0; i<cb.length; i++){
				bw.write("    dut.context_pbox.memory["+i+"] = "+cboxmask.getContextWidth()+"'b"+cboxmask.getBitString(cb[i])+";\n");
			}
			bw.write("\n");

			for(int i = 0; i < cu.length; i++){
				bw.write("    dut.controlunit.memory["+i+"] = "+ccumask.getContextWidth()+"'b"+ccumask.getBitString(cu[i])+";\n");
			}
			bw.write("\n");

			for( int i = 0; i< values.size(); i++){
				bw.write("    dut.pe"+PEs.get(i)+".regfile.memory["+addresses.get(i)+"] = " + values.get(i)+ ";\n");
			}
			
			
			bw.write("\n");
			bw.write("\n");
			bw.write("    OPERAND_ADDR_I = " + 0 + ";\n");
			
			
			
			
			
			line = br.readLine();
			while(! line.contains("REPORTBACK")){
				bw.write(line+ "\n");
				line = br.readLine();
			}
			bw.write(line+ "\n");


			for(int i = 0; i< addressesS.size(); i++){
				bw.write("    $display(\"Value of PE%d.%d: %d\","+PEsS.get(i)+","+addressesS.get(i)+",dut.pe"+PEsS.get(i)+".regfile.memory["+addressesS.get(i)+"]);\n");
			}
			
			
			line = br.readLine();
			while(! line.contains("HEAPINIT")){
				bw.write(line+ "\n");
				line = br.readLine();
			}
			bw.write(line+ "\n");
			
			for(int i = 0; i<inBytes.length; i++){
				bw.write("   memory["+i+"] = "+ inBytes[i]+";\n");
			}
			
			bw.write("\n");
			
			line = br.readLine();
			while(line!=null){
				bw.write(line+ "\n");
				line = br.readLine();
			}
			br.close();
			bw.close();
			
		} catch(IOException e){
			e.printStackTrace(System.err);
		}
	}
	
}


/*
 * TODO - More detailed ContextFormat testing !!!
 * 
 * *7
 */

