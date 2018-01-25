package test;

import java.util.ArrayList;

import amidar.axtLoader.AXTFile;
import amidar.axtLoader.AXTLoader;

public class Tester {
	
	public static void main(String[] args){
		ArrayList<String> paths = new ArrayList<String>();
		paths.add("axt");
		AXTLoader axtLoader = new AXTLoader("axt/T06.axt");
		AXTFile af = axtLoader.getAxtFile();
		
		byte[] code = af.getDataSec().getBytecode();
		
//		for(byte b: code){
//			System.out.println(((int)b)&0xFF);
//		}
		
		System.out.println(af.getTabSec().methodTableGetCodeRef(191));
		
		
	}

}
