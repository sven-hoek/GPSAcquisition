package java.lang;

import java.io.InputStream;

public class Runtime {
	
	public static Runtime getRuntime(){
		return new Runtime();
	}
	
	public InputStream getLocalizedInputStream(InputStream in){
		return in;
	}
	
	public long freeMemory(){
		return 0;
	}
	
	public long totalMemory(){
		return 0;
	}
}
