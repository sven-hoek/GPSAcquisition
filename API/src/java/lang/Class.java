package java.lang;

import java.util.HashMap;

//import spec.benchmarks._202_jess.jess.NewInstanceHacker;

//import spec.benchmarks._202_jess.jess.NewInstanceHacker;


public class Class {
	private static Class[] classes;
	private static HashMap nameToClassMap;
	
	static {
		Class[] HACK = new Class[0]; // Force AXT converter to include "Class[]"
	}
	
	private Object instance;
	
	public static Class forName (String name) {
		
//		Class hack = getHacked(name);
//		if(hack != null){
//			return hack;
//		}
		
		if (nameToClassMap == null) {
			nameToClassMap = new HashMap (classes.length * 2);
			for (int i = 0; i < classes.length; i++) {
				nameToClassMap.put (classes[i].getName (), classes[i]);
			}
		}
		if (nameToClassMap.containsKey (name))
			return (Class) nameToClassMap.get (name);
		else
			return null;
	}
	
	public static Class forCti (int cti) {
		if (cti < 0 || cti > classes.length) {
			return null;
		}
		return classes [cti];
	}
	
	public Object newInstance(){
		System.out.println("yyo");
		return instance;
	}
	
	private String className;
	private int cti;
  
	public String getName () {
		return className;
	}
	
	public int getCti () {
		return cti;
	}

	public boolean isPrimitive () {
		// TODO Auto-generated method stub
		return false;
	}

	public Class getComponentType () {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isInstance (Object value) {
		// TODO Auto-generated method stub
		return false;
	}
	
//	private static Class getHacked(String name){
//		Object instance = NewInstanceHacker.getInstance(name);
//		if(instance == null){
//			return null;
//		}
//		
//		Class ret = new Class();
//		ret.instance = instance;
//		
//		return ret;
//	}

}
