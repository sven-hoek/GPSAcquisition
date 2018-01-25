package java.io;

public class FileInputStream extends InputStream{
	String file;
	
	public FileInputStream(String file){
		this.file = file;
	}
	
	public native int read();
	
	public int read(byte[] b, int off, int len){
		int count = 0;
		int value = read();
		while(value != -1 && count < len){
			b[count++]=(byte)value;
			value = read();
		}
		
		return count;
	}
	
	
//	public void close(){
//		
//	}
	
}
