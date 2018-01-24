package java.io;

public class ByteArrayOutputStream extends OutputStream {
	
	byte[] data;
	int pointer = 0;
	int offset = 0;


	public void write(int b) throws IOException {
		data[pointer++] = (byte)b;
	}
	
	public ByteArrayOutputStream(int length){
		data = new byte[length];
	}
	
	public int size(){
		return data.length;
	}
	
	public void reset(){
		pointer = offset;
	}
	
	public void writeInt(int value){
		System.out.println("BBBBBNNNN");
		data[pointer++] = (byte)value;
	}
	
	public void write (byte[] b, int off, int len){
		System.arraycopy(b, off, data, pointer, len);
		pointer+=len;
	}
	
	public void writeTo(OutputStream out) throws NullPointerException, IOException{
//		System.out.println();
		out.write(data, 0 ,pointer);
	}
	
	public byte[] toByteArray(){
		byte[] a = new byte[pointer];
		System.arraycopy(data, 0, a, 0, a.length);
//		for(int i = 0; i< data.length; i++){
//			System.out.println("   " + a[i]);
//		}
		return a;
	}
	
}
