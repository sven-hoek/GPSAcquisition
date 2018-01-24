package functionalunit.tokenmachine;

import com.sun.corba.se.spi.legacy.connection.GetEndPointInfoAgainException;

import functionalunit.tables.ClassTableEntry;
import functionalunit.tables.ImplementedInterfacesTableEntry;
import functionalunit.tables.TableCache;

/**
 * This class emulates the ClassController of the Tokenmachine
 * @author jung
 *
 */
public class ClassController {

	private int arrayTypeOffset = 0;
	private int interfaceOffset = 0;


	/**
	 * Holds the class table
	 */
	TableCache<ClassTableEntry> classTable;

	/**
	 * Holds the implemented interfaces table
	 */
	TableCache<ImplementedInterfacesTableEntry> implementedInterfacesTable;

	/**
	 * Creates a new Classcontroller with given Tables
	 * @param classTable the class table
	 * @param implementedInterfacesTable the implemented interfaces table
	 */
	public ClassController(TableCache<ClassTableEntry> classTable, TableCache<ImplementedInterfacesTableEntry> implementedInterfacesTable){
		this.classTable = classTable;
		this.implementedInterfacesTable = implementedInterfacesTable;
	}

	/**
	 * Requests the Class size of the class denoted by the class table index
	 * @param classTableIndex the index of the class
	 * @return true when table entry is in cache. the Classsize can be read with getClasssize
	 */
	public boolean requestClassInfo(int classTableIndex){
		return classTable.requestData(classTableIndex);
	}

	/**
	 * Gets the class size of the class that was previously requested
	 * @return
	 */
	public int getClassSize(){
		return classTable.getData().get(ClassTableEntry.CLASSSIZE);
	}

	/**
	 * Gets the class table entry of (classinfo) of the class that was previously requested
	 * @return
	 */
	public ClassTableEntry getData(){
		return classTable.getData();
	}

	//	TODO Checkcast + instanceof
	int objectCTI;
	int instanceCTI;
	int isInstanceOf;
	State state; 
	
	int instanceDimension;
	int instanceDataType;
	int instanceIsPrimitive;
	int objectDimension;
	int objectDataType;
	int objectIsPrimitive;

	public void instanceOf(int objectCTI, int instanceCTI){
		this.objectCTI = objectCTI;
		this.instanceCTI = instanceCTI;
		state = State.START;
	}
	
	public int isInstanceOf(){
		return isInstanceOf;
	}

	private enum State{
		START,
		CLASS_CLASS,
		INTERFACE_IREF,
		INTERFACE_IOLI,
		ARRAY_INSTANCE,
		ARRAY_OBJECT,
		ARRAY_ARRAY
		
	}


	public boolean ready(){


		switch (state) {
		case START:
			if(objectCTI == 0xFFFF){
				isInstanceOf = 1;
				return true;
			} else if(objectCTI == instanceCTI){
				isInstanceOf = 1;
				return true;
			} else if(objectCTI == 0){
				isInstanceOf = 0;
				return true;
			} else {
				if(isClass(objectCTI)){
					if(isClass(instanceCTI)){
						requestClassInfo(objectCTI);
						state = State.CLASS_CLASS;
					} else {
						requestClassInfo(objectCTI);
						state = State.INTERFACE_IREF;
					}
				} else if(isInterface(objectCTI)){
					if(isClass(instanceCTI)){
						if(instanceCTI == 0){
							isInstanceOf = 1;
						} else {
							isInstanceOf = 0;
						}
						return true;
					} else {
						requestClassInfo(objectCTI);
						state = State.CLASS_CLASS;
					}
				} else {
					if(isClass(instanceCTI)){
						if(instanceCTI == 0){
							isInstanceOf = 1;
						} else {
							isInstanceOf = 0;
						}
						return true;
					} else if(isInterface(instanceCTI)){
						requestClassInfo(objectCTI);
						state = State.INTERFACE_IREF;
					} else {
						requestClassInfo(instanceCTI);
						state = State.ARRAY_INSTANCE;
					}
				}
				
			}
			break;
		case CLASS_CLASS:
			if(!requestClassInfo(objectCTI)){
				break;
			}
			int superCTI = getData().get(ClassTableEntry.SUPER_CTI);
			if(superCTI == instanceCTI){
				isInstanceOf = 1;
				return true;
			} else if( superCTI == 0){
				isInstanceOf = 0;
				return true;
			} else {
				objectCTI = superCTI;
				requestClassInfo(objectCTI);
			}
			break;
		case INTERFACE_IREF:
			if(!requestClassInfo(objectCTI)){
				break;
			}
			implementedInterfacesTable.requestData(getData().get(ClassTableEntry.IMPL_INTERFACE_TABLE_REF));
			state = State.INTERFACE_IOLI;
			break;
		case INTERFACE_IOLI:
			if(!implementedInterfacesTable.requestData(getData().get(ClassTableEntry.IMPL_INTERFACE_TABLE_REF))){
				break;
			}
			isInstanceOf = implementedInterfacesTable.getData().get(instanceCTI-interfaceOffset);
			return true;
		case ARRAY_INSTANCE:
			if(!requestClassInfo(instanceCTI)){
				break;
			}
			instanceDimension = getData().get(ClassTableEntry.INTERFACE_TABLE_REF);
			instanceDataType = getData().get(ClassTableEntry.CLASSSIZE);
			instanceIsPrimitive = (getData().get(ClassTableEntry.FLAGS)>>1)&0x1;
			requestClassInfo(objectCTI);
			state = State.ARRAY_OBJECT;
			break;
		case ARRAY_OBJECT:
			if(!requestClassInfo(objectCTI)){
				break;
			}
			objectDimension = getData().get(ClassTableEntry.INTERFACE_TABLE_REF);
			objectDataType = getData().get(ClassTableEntry.CLASSSIZE);
			objectIsPrimitive = (getData().get(ClassTableEntry.FLAGS)>>1)&0x1;
			if(instanceDimension != objectDimension){
				isInstanceOf = 0;
				return true;
			} else if(objectIsPrimitive != instanceIsPrimitive){
				isInstanceOf = 0;
				return true;
			} else if(instanceIsPrimitive == 1){   // TODO not sure about this - check this ( is done like in HW)
				isInstanceOf = 0;
				return true;
			} else if(objectDataType == instanceDataType){
				isInstanceOf = 0;
				return true;
			} else {
				requestClassInfo(objectDataType);
				state = State.ARRAY_ARRAY;
			}
			break;
		case ARRAY_ARRAY:
			if(!requestClassInfo(objectDataType)){
				break;
			}
			int superObjectDataType = getData().get(ClassTableEntry.SUPER_CTI);
			if(superObjectDataType == instanceDataType){
				isInstanceOf = 1;
				return true;
			} else if( superObjectDataType == 0){
				isInstanceOf = 0;
				return true;
			} else {
				objectDataType = superObjectDataType;
				requestClassInfo(objectDataType);
			}
			break;
			
		}
		return false;
	}


	private boolean isClass(int cti){
		return cti < interfaceOffset;
	}

	private boolean isInterface(int cti){
		return cti >= interfaceOffset && cti < arrayTypeOffset;
	}

	private boolean isArrayType(int cti){
		return cti >= arrayTypeOffset;
	}


	public void setArrayTypeOffset(int arrayTypeOffset) {
		this.arrayTypeOffset = arrayTypeOffset;
	}

	public void setInterfaceOffset(int interfaceOffset) {
		this.interfaceOffset = interfaceOffset;
	}

}
