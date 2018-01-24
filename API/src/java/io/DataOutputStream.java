package java.io;

public class DataOutputStream extends FilterOutputStream {

	public DataOutputStream(OutputStream out) {
		super(out);
	}
	
	public void writeShort(int value) throws IOException{
		out.write(value>>8);
		out.write(value);
	}
	
	public void writeByte(int value) throws IOException{
		out.write(value);
	}
	
	public void writeInt(int value) throws IOException{
		out.write(value>>24);
		out.write(value>>16);
		out.write(value>>8);
		out.write(value);
	}
	
	public void writeLong(long lvalue) throws IOException{
		int value = (int)(lvalue >> 32);
		out.write(value>>24);
		out.write(value>>16);
		out.write(value>>8);
		out.write(value);
		value = (int)(lvalue);
		out.write(value>>24);
		out.write(value>>16);
		out.write(value>>8);
		out.write(value);
	}
	
	public void writeUTF(String str) throws IOException{
		int strlen = str.length();
        int utflen = 0;
        int c, count = 0;

        /* use charAt instead of copying String to char array */
        for (int i = 0; i < strlen; i++) {
            c = str.charAt(i);
            if ((c >= 0x0001) && (c <= 0x007F)) {
                utflen++;
            } else if (c > 0x07FF) {
                utflen += 3;
            } else {
                utflen += 2;
            }
        }

        if (utflen > 65535)
            throw new IOException(//TODO
                "encoded string too long: " + utflen + " bytes");

        byte[] bytearr = null;
//        if (out instanceof DataOutputStream) {
//            DataOutputStream dos = (DataOutputStream)out;
//            if(dos.bytearr == null || (dos.bytearr.length < (utflen+2)))
//                dos.bytearr = new byte[(utflen*2) + 2];
//            bytearr = dos.bytearr;
//        } else {
            bytearr = new byte[utflen+2];
//        }

        bytearr[count++] = (byte) ((utflen >>> 8) & 0xFF);
        bytearr[count++] = (byte) ((utflen >>> 0) & 0xFF);

        int i=0;
        for (i=0; i<strlen; i++) {
           c = str.charAt(i);
           if (!((c >= 0x0001) && (c <= 0x007F))) break;
           bytearr[count++] = (byte) c;
        }

        for (;i < strlen; i++){
            c = str.charAt(i);
            if ((c >= 0x0001) && (c <= 0x007F)) {
                bytearr[count++] = (byte) c;

            } else if (c > 0x07FF) {
                bytearr[count++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
                bytearr[count++] = (byte) (0x80 | ((c >>  6) & 0x3F));
                bytearr[count++] = (byte) (0x80 | ((c >>  0) & 0x3F));
            } else {
                bytearr[count++] = (byte) (0xC0 | ((c >>  6) & 0x1F));
                bytearr[count++] = (byte) (0x80 | ((c >>  0) & 0x3F));
            }
        }
        out.write(bytearr, 0, utflen+2);
//        return utflen + 2;
	}
	
	public void writeDouble(double d) throws IOException{
		writeLong(Double.doubleToLongBits(d));
	}
	
	public void writeFloat(float f) throws IOException{
		writeInt(Float.floatToIntBits(f));
	}
	
}
