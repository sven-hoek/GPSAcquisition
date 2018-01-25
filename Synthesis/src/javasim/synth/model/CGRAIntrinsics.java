package javasim.synth.model;

import java.util.HashMap;

import dataContainer.MethodDescriptor;
import target.Amidar;

//import cgra.pe.PEColor;
//import cgra.pe.PEMpeg;
//import cgra.pe.PESimple;
//import cgra.pe.PESimple64;
//import cgra.pe.PETrigonometry;

/**
 * This class is keeping track of all Intrinsic CGRA/PE Functions
 * The identifier (=class index + method index) is dependent on the app, so during class loading (done by ClassLoader) the 
 * intrinsics have to be registered.
 * @author jung
 *
 */
public class CGRAIntrinsics{
	
	
	private HashMap<Integer,Intrinsic> knownIntrinsics = new HashMap<Integer,Intrinsic>(); //contains ID and number of ops for this function

	/**
	 * Class storing information of an intrinsic Function
	 * @author jung
	 *
	 */
	public class Intrinsic{
		private int ops = 0;
		private String name = "";
		private I instruction;

		/**
		 * Creates a new object storing information about a intrinsic function
		 * @param ops	the number of operands the intrinsic function awaits
		 * @param name the name of the intrinsic function
		 * @param instruction	the synthesis Instruction that represents this intrinsic function
		 */
		Intrinsic(int ops, String name, I instruction){
			this.ops = ops;
			this.name = name;
			this.instruction = instruction;
		}
		
		public Number calc(Number op){
			return 0;
		}
		
		public Number calc(Number op1, Number op2){
			return 0;
		}
		
		public Number calc(Number op1, Number op2, Number op3){
			return 0;
		}


		public int getOps(){
			return ops;
		}

		public String getName(){
			return name;
		}
		
		public I getInstruction(){
			return instruction;
		}
	}

	/**
	 * TODO
	 * @param name the names of functions
	 */
	public void registerFunctions(MethodDescriptor[] methods){
		for(int i = 0; i < methods.length; i++){
			String methodName = methods[i].getMethodName();

			if( methodName.equals("cgra/pe/PETrigonometry.cos(F)F")){			// when the function name is known we add a new Intrinsic to the map of known Intrinsics, with the ID as key
				knownIntrinsics.put(i,new Intrinsic(1,methodName, I.FCOS){});		//  new Intrinsic object overwrites the calc function with the intrinsic function

			}else if(methodName.equals("cgra/pe/PETrigonometry.sin(F)F")){			// when the function name is known we add a new Intrinsic to the map of known Intrinsics, with the ID as key
				knownIntrinsics.put(i,new Intrinsic(1,methodName, I.FSIN){});		//  new Intrinsic object overwrites the calc function with the intrinsic function
			}
		}
	}

	/**
	 * Computes the unary intrinsic function denoted by functionID 
	 * @param functionId identifier for the intrinsic function
	 * @param op the operator
	 * @return the value
	 */
	public Number calculateUn(int functionId, Number op){
		return knownIntrinsics.get(functionId).calc(op);
	}

	/**
	 * Computes the binary intrinsic function denoted by functionID 
	 * @param functionId identifier for the intrinsic function
	 * @param op1 the first operator
	 * @param op2 the second operator
	 * @return the value
	 */
	public Number calculateBin(int functionId, Number op1, Number op2){
		return knownIntrinsics.get(functionId).calc(op1, op2);
	}
	
	/**
	 * Computes the trinary intrinsic function denoted by functionID 
	 * @param functionId identifier for the intrinsic function
	 * @param op1 the first operator
	 * @param op2 the second operator
	 * @param op3 the third operator
	 * @return the value
	 */
	public Number calculateTri(int functionId, Number op1, Number op2, Number op3){
		return knownIntrinsics.get(functionId).calc(op1, op2, op3);
	}

	/**
	 * Check whether an intrinsic function with the given id is known
	 * @param functionID the ID that has to be checked
	 * @return true if the ID is known
	 */
	public boolean isKnown(int functionID){
		return knownIntrinsics.keySet().contains(functionID);
	}

	/**
	 * This method returns the number of operands a functions awaits
	 * @param functionID the id of the function we want information about
	 * @return the number of operands the function
	 */
	public int nrOfOperands(int functionID){
		return knownIntrinsics.get(functionID).getOps();
	}

	/**
	 * This method returns the synthesis Instruction which represents the intrinsic function
	 * @param functionID the id of the function we want information about
	 * @return the Instruction representing the function
	 */
	public I getInstruction(int functionID){
		return knownIntrinsics.get(functionID).getInstruction();
	}
	
	public void deleteIntrinsic(int functionID){
		knownIntrinsics.remove(functionID);
	}

}
