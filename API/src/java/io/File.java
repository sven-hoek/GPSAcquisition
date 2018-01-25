package java.io;

public class File {
	
	public final static String pathSeparator;
	public final static char pathSeparatorChar;
	public final static String separator;
	public final static char separatorChar;

	static{
		pathSeparator = ":";
		pathSeparatorChar = ':';
		separator = "/";
		separatorChar = '/';
	}
	
	private String fileName;
	
	public File(String fileName){
		this.fileName = fileName;
	}
	
	public File(String path, String fileName){
		this.fileName = path + "/" + fileName;
	}
	
	public long length(){
		long low = lengthA() & 0xFFFFFFFFL;
		long high = ((long)lengthB())<< 32;
		
		return low + high;
	}

	
	// WE can only handle 32 return values in native methods in AMIDAR Simulator
	private native int lengthA();
	
	private native int lengthB();
	
	
	public String getPath(){
		return fileName;
	}
	
	public String getName(){
		String[] n = getPath().split("/");
		if(n.length == 0){
			return "";
		} else {
			return n[n.length-1];
		}
	}
	
}
