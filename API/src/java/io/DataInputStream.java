package java.io;

import java.util.ArrayList;

public class DataInputStream extends FilterInputStream {
	
	InputStream in2;
	
	int  pushed =0;
	boolean pushedback = false;

	public DataInputStream(InputStream in) {
		super(in);
		this.in2 = in;
	}
	
	
	public byte readByte() throws IOException{
		if(pushedback){
			pushedback =false;
			if(pushed < 0){
				throw new EOFException();
			}
			return (byte)pushed;
		}
		int value = in2.read();
		byte bb =(byte)value;
		if(value < 0){
			throw new EOFException();
		}
		
		return bb;
//		return (byte)in.read();
	}
	
	public String readLine() throws IOException{
		ArrayList a = new ArrayList();
		
		char rr;
		if(!pushedback){
			rr = (char)in2.read();
		} else{
			rr = (char)pushed;
			pushedback = false;
		}
			
		
		if(pushed == -1){
			return null;
		}
		int value  = 0;
		while(rr !='\n' && rr!='\r' && value != -1){
			a.add(new Character(rr));
			value = in2.read();
			rr = (char)value;
		}
		
		if(rr == '\r'){
			value = in2.read();
			rr = (char)value;
			if(rr != '\n' &&  value != -1){
				pushed =value;
				pushedback =true;
//				System.out.println("FFFFFFFFFFF PUSHEDBACK");
			}
		}

		char[] val = new char[a.size()];
		
		for(int i = 0; i < a.size(); i++){
			val[i] = ((Character)a.get(i)).charValue();
		}
//		System.out.println("READING LINE: " + new String(val));
		return new String(val);
	}

	public void close(){
		
	}
	
	public short readShort() throws IOException{
		short v = (short)(((0xFF&readByte())<<8) + (0xFF&readByte()));
//		System.out.println(" - " + v);
		return v;
	}
	
	public int readInt() throws IOException{
//		System.out.println("READINT: ");
		
		
		int v = (0xFF&readByte())<<24;
		v += ((0xFF&readByte())<<16) + ((0xFF&readByte())<<8 )+ (0xFF&readByte());
		return v;
	}
	
	public long readLong() throws IOException{
		int h = readInt();
		int l = readInt();
		
		return ((long)h)<<32 + l;
		
		
	}
	
	public String readUTF() throws IOException{
		 int utflen = 0xFFFF&(int)readShort();
	        byte[] bytearr = null;
	        char[] chararr = null;
//	        if (in instanceof DataInputStream) {
//	            DataInputStream dis = (DataInputStream)in;
//	            if (dis.bytearr.length < utflen){
//	                dis.bytearr = new byte[utflen*2];
//	                dis.chararr = new char[utflen*2];
//	            }
//	            chararr = dis.chararr;
//	            bytearr = dis.bytearr;
//	        } else {
	            bytearr = new byte[utflen];
	            chararr = new char[utflen];
//	        }

	        int c, char2, char3;
	        int count = 0;
	        int chararr_count=0;

	        readFully(bytearr, 0, utflen);

	        while (count < utflen) {
	            c = (int) bytearr[count] & 0xff;
	            if (c > 127) break;
	            count++;
	            chararr[chararr_count++]=(char)c;
	        }

	        while (count < utflen) {
	            c = (int) bytearr[count] & 0xff;
	            switch (c >> 4) {
	                case 0: case 1: case 2: case 3: case 4: case 5: case 6: case 7:
	                    /* 0xxxxxxx*/
	                    count++;
	                    chararr[chararr_count++]=(char)c;
	                    break;
	                case 12: case 13:
	                    /* 110x xxxx   10xx xxxx*/
	                    count += 2;
	                    if (count > utflen)
	                        throw new IOException(
	                            "malformed input: partial character at end");
	                    char2 = (int) bytearr[count-1];
	                    if ((char2 & 0xC0) != 0x80)
	                        throw new IOException(
	                            "malformed input around byte " + count);
	                    chararr[chararr_count++]=(char)(((c & 0x1F) << 6) |
	                                                    (char2 & 0x3F));
	                    break;
	                case 14:
	                    /* 1110 xxxx  10xx xxxx  10xx xxxx */
	                    count += 3;
	                    if (count > utflen)
	                        throw new IOException(
	                            "malformed input: partial character at end");
	                    char2 = (int) bytearr[count-2];
	                    char3 = (int) bytearr[count-1];
	                    if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
	                        throw new IOException(
	                            "malformed input around byte " + (count-1));
	                    chararr[chararr_count++]=(char)(((c     & 0x0F) << 12) |
	                                                    ((char2 & 0x3F) << 6)  |
	                                                    ((char3 & 0x3F) << 0));
	                    break;
	                default:
	                    /* 10xx xxxx,  1111 xxxx */
	                    throw new IOException(
	                        "malformed input around byte " + count);
	            }
	        }
	        // The number of chars produced may be less than utflen
	        return new String(chararr, 0, chararr_count);
	}
	
	public float readFloat() throws IOException{
		return Float.intBitsToFloat(readInt());
	}
	
	
	
	public double readDouble() throws IOException{
		return Double.longBitsToDouble(readLong());
	}
	
	public void readFully(byte[] b) throws IOException{
		for(int i = 0; i< b.length; i++){
			b[i] = readByte();
			if(b[i] == -1){
				throw new EOFException();
			}
		}
		
	}
	
	public void readFully(byte[] b, int offset, int length) throws IOException{
		for( int i = 0; i<length; i++){
			b[i+offset] = readByte();
		}
	}
	
	public int skipBytes(int n) throws IOException{
		if(pushedback && pushed < 0){
			return 0;
		}
		int cnt = 0;
		for(int i = pushedback?1:0; i< n; i++){
			if(in2.read() == -1){
//				System.out.println("EEOFF");
				break;
			}
			cnt++;
		}
		
		
//		System.out.println("OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO");
		return cnt;
	}
	
}
