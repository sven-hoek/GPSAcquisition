package functionalunit.heap;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class ObjectHeapTrace {
	
	// Dump Format:
	FileOutputStream fos;

	public ObjectHeapTrace(String filename) {
		try {
			fos = new FileOutputStream(filename);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void appendTrace(int opc, int flag, int in0, int in1, int in2, int in3, int out0, int out1) {
		// Log
		byte[] data = new byte[29];
		// OPCode
		data[0] = (byte)opc; // Mode;
		// Flags
		data[1] = (byte)((flag & 0x000000FF) >> 0);
		data[2] = (byte)((flag & 0x0000FF00) >> 8);
		data[3] = (byte)((flag & 0x00FF0000) >> 16);
		data[4] = (byte)((flag & 0xFF000000) >> 24);
		// In0
		data[5] = (byte)((in0 & 0x000000FF) >> 0);
		data[6] = (byte)((in0 & 0x0000FF00) >> 8);
		data[7] = (byte)((in0 & 0x00FF0000) >> 16);
		data[8] = (byte)((in0 & 0xFF000000) >> 24);
		// In1
		data[9] =  (byte)((in1 & 0x000000FF) >> 0);
		data[10] = (byte)((in1 & 0x0000FF00) >> 8);
		data[11] = (byte)((in1 & 0x00FF0000) >> 16);
		data[12] = (byte)((in1 & 0xFF000000) >> 24);
		// In2
		data[13] =  (byte)((in2 & 0x000000FF) >> 0);
		data[14] = (byte)((in2 & 0x0000FF00) >> 8);
		data[15] = (byte)((in2 & 0x00FF0000) >> 16);
		data[16] = (byte)((in2 & 0xFF000000) >> 24);
		// In3
		data[17] = (byte)((in3 & 0x000000FF) >> 0);
		data[18] = (byte)((in3 & 0x0000FF00) >> 8);
		data[19] = (byte)((in3 & 0x00FF0000) >> 16);
		data[20] = (byte)((in3 & 0xFF000000) >> 24);
		// Out0
		data[21] =  (byte)((out0 & 0x000000FF) >> 0);
		data[22] = (byte)((out0 & 0x0000FF00) >> 8);
		data[23] = (byte)((out0 & 0x00FF0000) >> 16);
		data[24] = (byte)((out0 & 0xFF000000) >> 24);
		// Out1
		data[25] =  (byte)((out1 & 0x000000FF) >> 0);
		data[26] = (byte)((out1 & 0x0000FF00) >> 8);
		data[27] = (byte)((out1 & 0x00FF0000) >> 16);
		data[28] = (byte)((out1 & 0xFF000000) >> 24);
		try {
			fos.write(data, 0, 29);
			fos.flush();		
		} catch (Exception e) {}
	}

}