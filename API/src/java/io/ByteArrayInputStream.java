package java.io;

public class ByteArrayInputStream extends InputStream {
	
	 private byte[] data;
	 int pointer;
	 int offset;
	 int length;

	 
	    public ByteArrayInputStream(byte buf[]) {
	        this.data = buf;
	        offset =0;
	        pointer =0;
	        length = buf.length;
//	        this.pos = 0;
//	        this.count = buf.length;
	    }
	
	public ByteArrayInputStream(byte [] data, int offset, int length){
		this.data = data;
		this.offset = offset;
		this.length = length;
		this.pointer = offset;
	}

	public int read() throws IOException {
//		System.out.println("RREAD  + " + pointer + " von " + length +  " Value : " + data[pointer]);
		return (pointer<length)?(0xFF&data[pointer++]):-1;
	}
	
	public void close(){
		
	}
	
	public void reset(){
		pointer = offset;
	}
	
	public long skip(long n){
		if(n + pointer < data.length){
			pointer += n;
			return n;
		} else{
			pointer = data.length -1;
			return data.length - pointer;
		}
		
	}
} 
