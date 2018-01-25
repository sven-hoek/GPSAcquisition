package functionalunit.heap;

public interface ObjectHeapCONST {	
	
	public static final int HO_READ 					=0b0000011;
	public static final int HO_READ_64 					=0b0010011;
	public static final int HO_READ_ARRAY 				=0b0001011;
	public static final int HO_READ_ARRAY_64			=0b0011011;
	
	public static final int O_READ 						=0b0000010;
	public static final int O_READ_64 					=0b0010010;
	public static final int O_READ_ARRAY 				=0b0001010;
	public static final int O_READ_ARRAY_64				=0b0011010;
	
	public static final int H_READ 						=0b0000001;
	public static final int H_READ_64 					=0b0010001;
	public static final int H_READ_ARRAY 				=0b0001001;
	public static final int H_READ_ARRAY_64				=0b0011001;
	
	public static final int READ 						=0b0000000;
	public static final int READ_64 					=0b0010000;
	public static final int READ_ARRAY 					=0b0001000;
	public static final int READ_ARRAY_64				=0b0011000;
	
	public static final int PIO_READ 					=0b0000100;
	public static final int PIO_READ_64 				=0b0010100;
	public static final int PIO_READ_ARRAY 				=0b0001100;
	public static final int PIO_READ_ARRAY_64			=0b0011100;

	public static final int HO_WRITE 					=0b0100011;
	public static final int HO_WRITE_64 				=0b0110011;
	public static final int HO_WRITE_ARRAY 				=0b0101011;
	public static final int HO_WRITE_ARRAY_64			=0b0111011;
	
	public static final int H_WRITE 					=0b0100001;
	public static final int H_WRITE_64 					=0b0110001;
	public static final int H_WRITE_ARRAY 				=0b0101001;
	public static final int H_WRITE_ARRAY_64			=0b0111001;
	
	public static final int O_WRITE 					=0b0100010;
	public static final int O_WRITE_64 					=0b0110010;
	public static final int O_WRITE_ARRAY 				=0b0101010;
	public static final int O_WRITE_ARRAY_64			=0b0111010;
	
	public static final int WRITE 						=0b0100000;
	public static final int WRITE_64 					=0b0110000;
	public static final int WRITE_ARRAY 				=0b0101000;
	public static final int WRITE_ARRAY_64				=0b0111000;
	
	public static final int PIO_WRITE 					=0b0100100;
	public static final int PIO_WRITE_64 				=0b0110100;
	public static final int PIO_WRITE_ARRAY 			=0b0101100;
	public static final int PIO_WRITE_ARRAY_64			=0b0111100;
	
	public static final int ALLOC_ARRAY 				=0b1001001;
	public static final int ALLOC_ARRAY_64 				=0b1011001;
	public static final int ALLOC_MULTI_ARRAY 			=0b1100101;
	public static final int ALLOC_MULTI_ARRAY_64		=0b1100101;
	public static final int ALLOC_OBJ 					=0b1000001;
	public static final int SETUP_MULTI_ARRAY			=0b1100011;
	public static final int SET_MULTI_ARRAY_DIM_SIZE	=0b1100100;
	
	public static final int GET_SIZE					=0b1100000;
	public static final int GET_CTI						=0b1100001;
	public static final int SET_BASE					=0b1100111;	

	public static final int GET_FLAGS					=0b1100010;
	public static final int SET_FLAGS					=0b1101100;
	public static final int GET_MID						=0b1101011;
	public static final int SET_MID						=0b1101010;
	
	public static final int PHY_READ					=0b1101001;
	public static final int PHY_WRITE					=0b1101000;
	
	public static final int INVALIDATE					= -1;
	public static final int RESET						= -2; 
}