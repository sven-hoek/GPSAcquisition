/*
 * Created on 14.07.2003
 */
package dataContainer;

import java.nio.ByteBuffer;

/**
 * @author Stefan D&ouml;brich, Daniel Versick
 */
public class ByteCode {
	//TODO check compatibility with axt
	
 
	public static final byte WIDE = (byte)0xc4;
	public static final byte BASTORE = (byte)0x54;
	public static final byte DUP2_X2 = (byte)0x5e;
	public static final byte DUP2_X1 = (byte)0x5d;
	public static final byte CASTORE = (byte)0x55;
	public static final byte IF_ACMPNE = (byte)0xa6;
	public static final short WIDE_ILOAD = 0x115;
	public static final byte LALOAD = (byte)0x2f;
	public static final short WIDE_DSTORE = 0x139;
	public static final byte IADD = (byte)0x60;
	public static final byte ENABLE_SCHEDULING = (byte)0xef;
	public static final byte PUTSTATIC_QUICK = (byte)0xd3;
	public static final byte LXOR = (byte)0x83;
	public static final byte DALOAD = (byte)0x31;
	public static final byte LRETURN = (byte)0xad;
	public static final byte GETFIELD = (byte)0xb4;
	public static final byte IDIV = (byte)0x6c;
	public static final byte L2I = (byte)0x88;
	public static final byte FLOAD = (byte)0x17;
	public static final byte CHECKCAST_QUICK = (byte)0xe0;
	public static final byte L2F = (byte)0x89;
	public static final byte INVOKESTATIC_QUICK = (byte)0xd9;
	public static final byte REFL_INVOKEINTERFACE = (byte)0xb8;
	public static final byte L2D = (byte)0x8a;
	public static final byte AASTORE = (byte)0x53;
	public static final byte INVOKENONVIRTUAL_QUICK = (byte)0xd7;
	public static final byte GETSTATIC_QUICK = (byte)0xd2;
	public static final byte IF_ICMPNE = (byte)0xa0;
	public static final byte IRETURN = (byte)0xac;
	public static final short WIDE_LSTORE = 0x137;
	public static final byte F2L = (byte)0x8c;
	public static final byte HWBINVOKE = (byte)0xf0;
	public static final byte F2I = (byte)0x8b;
	public static final byte INSTANCEOF_QUICK = (byte)0xe1;
	public static final byte LCONST_1 = (byte)0xa;
	public static final byte LCONST_0 = (byte)0x9;
	public static final byte MULTIANEWARRAY_QUICK = (byte)0xdf;
	public static final byte D2L = (byte)0x8f;
	public static final byte F2D = (byte)0x8d;
	public static final byte GOTO_W = (byte)0xc8;
	public static final byte D2I = (byte)0x8e;
	public static final byte D2F = (byte)0x90;
	public static final byte FRETURN = (byte)0xae;
	public static final byte LSTORE_3 = (byte)0x42;
	public static final byte GOTO = (byte)0xa7;
	public static final byte LSUB = (byte)0x65;
	public static final byte LSTORE_2 = (byte)0x41;
	public static final byte LSTORE_1 = (byte)0x40;
	public static final byte IINC = (byte)0x84;
	public static final byte LSTORE_0 = (byte)0x3f;
	public static final byte LSHR = (byte)0x7b;
	public static final byte ACONST_NULL = (byte)0x1;
	public static final byte ICONST_5 = (byte)0x8;
	public static final byte ICONST_4 = (byte)0x7;
	public static final byte ICONST_3 = (byte)0x6;
	public static final byte ICONST_2 = (byte)0x5;
	public static final byte ICONST_1 = (byte)0x4;
	public static final byte LSHL = (byte)0x79;
	public static final byte ICONST_0 = (byte)0x3;
	public static final byte LMUL = (byte)0x69;
	public static final short WIDE_LLOAD = 0x116;
	public static final byte DREM = (byte)0x73;
	public static final byte INT_TO_REF = (byte)0xe9;
	public static final byte DRETURN = (byte)0xaf;
	public static final byte IFGT = (byte)0x9d;
	public static final byte SWAP = (byte)0x5f;
	public static final byte ILOAD = (byte)0x15;
	public static final byte IOR = (byte)0x80;
	public static final byte IF_ICMPLT = (byte)0xa1;
	public static final byte IAND = (byte)0x7e;
	public static final byte LLOAD_3 = (byte)0x21;
	public static final byte LLOAD_2 = (byte)0x20;
	public static final byte LLOAD_1 = (byte)0x1f;
	public static final byte IFGE = (byte)0x9c;
	public static final byte DNEG = (byte)0x77;
	public static final byte LLOAD_0 = (byte)0x1e;
	public static final byte IALOAD = (byte)0x2e;
	public static final short WIDE_ASTORE = 0x13a;
	public static final byte IF_ICMPLE = (byte)0xa4;
	public static final byte AALOAD = (byte)0x32;
	public static final byte IREM = (byte)0x70;
	public static final byte LDC2_W_QUICK = (byte)0xcd;
	public static final byte NEWARRAY_64BIT = (byte)0xfb;
	public static final byte DUP = (byte)0x59;
	public static final byte ILOAD_3 = (byte)0x1d;
	public static final short WIDE_ALOAD = 0x119;
	public static final byte ILOAD_2 = (byte)0x1c;
	public static final byte ILOAD_1 = (byte)0x1b;
	public static final byte ILOAD_0 = (byte)0x1a;
	public static final byte ISTORE_3 = (byte)0x3e;
	public static final byte ISTORE_2 = (byte)0x3d;
	public static final byte ISTORE_1 = (byte)0x3c;
	public static final byte ISTORE_0 = (byte)0x3b;
	public static final byte LSTORE = (byte)0x37;
	public static final byte LDC_W_QUICK = (byte)0xcc;
	public static final byte IFNULL = (byte)0xc6;
	public static final byte DUP_X2 = (byte)0x5b;
	public static final byte DUP_X1 = (byte)0x5a;
	public static final byte FCONST_2 = (byte)0xd;
	public static final short WIDE_ISTORE = 0x136;
	public static final byte FCONST_1 = (byte)0xc;
	public static final byte FCONST_0 = (byte)0xb;
	public static final byte INEG = (byte)0x74;
	public static final byte DSTORE = (byte)0x39;
	public static final byte CGRA_START = (byte)0xfd;
	public static final byte ATHROW = (byte)0xbf;
	public static final byte ARETURN = (byte)0xb0;
	public static final byte PUTFIELD_QUICK = (byte)0xcf;
	public static final byte ARRAYLENGTH = (byte)0xbe;
	public static final byte ICONST_M1 = (byte)0x2;
	public static final byte FSTORE_3 = (byte)0x46;
	public static final byte FSTORE_2 = (byte)0x45;
	public static final byte FSTORE_1 = (byte)0x44;
	public static final byte FSTORE_0 = (byte)0x43;
	public static final byte LLOAD = (byte)0x16;
	public static final byte REFL_INVOKENONVIRTUAL = (byte)0xb7;
	public static final byte IFEQ = (byte)0x99;
	public static final byte REFL_INSTANCEOF = (byte)0xba;
	public static final byte JSR = (byte)0xa8;
	public static final byte FADD = (byte)0x62;
	public static final byte DLOAD_3 = (byte)0x29;
	public static final byte PUTFIELD = (byte)0xb5;
	public static final byte DLOAD_2 = (byte)0x28;
	public static final byte DLOAD_1 = (byte)0x27;
	public static final byte IXOR = (byte)0x82;
	public static final byte DLOAD_0 = (byte)0x26;
	public static final byte GETFIELDA_QUICK = (byte)0xe6;
	public static final byte FDIV = (byte)0x6e;
	public static final byte FLOAD_3 = (byte)0x25;
	public static final byte DSUB = (byte)0x67;
	public static final byte GETSTATICA_QUICK = (byte)0xe7;
	public static final byte FLOAD_2 = (byte)0x24;
	public static final byte FLOAD_1 = (byte)0x23;
	public static final byte FLOAD_0 = (byte)0x22;
	public static final byte RET = (byte)0xa9;
	public static final short WIDE_DLOAD = 0x118;
	public static final byte JSR_W = (byte)0xc9;
	public static final byte DMUL = (byte)0x6b;
	public static final byte DCMPL = (byte)0x97;
	public static final byte DCMPG = (byte)0x98;
	public static final byte ALOAD = (byte)0x19;
	public static final byte INVOKEVIRTUAL_QUICK = (byte)0xd6;
	public static final byte FORCE_SCHEDULING = (byte)0xee;
	public static final byte NEW_QUICK = (byte)0xdd;
	public static final byte POP2 = (byte)0x58;
	public static final byte ISUB = (byte)0x64;
	public static final byte ISHR = (byte)0x7a;
	public static final byte ALOAD_3 = (byte)0x2d;
	public static final byte LDC_STRING = (byte)0xe5;
	public static final byte IUSHR = (byte)0x7c;
	public static final byte ALOAD_2 = (byte)0x2c;
	public static final byte ISHL = (byte)0x78;
	public static final byte ALOAD_1 = (byte)0x2b;
	public static final byte IMUL = (byte)0x68;
	public static final byte BALOAD = (byte)0x33;
	public static final byte ALOAD_0 = (byte)0x2a;
	public static final byte HARDWARE_THROW = (byte)0xc1;
	public static final byte ISTORE = (byte)0x36;
	public static final short WIDE_FSTORE = 0x138;
	public static final byte ASTORE = (byte)0x3a;
	public static final byte I2S = (byte)0x93;
	public static final byte FALOAD = (byte)0x30;
	public static final byte I2L = (byte)0x85;
	public static final byte REFL_INVOKEVIRTUAL = (byte)0xb6;
	public static final byte I2F = (byte)0x86;
	public static final byte I2D = (byte)0x87;
	public static final byte I2C = (byte)0x92;
	public static final byte I2B = (byte)0x91;
	public static final byte WRITE_ADDRESS = (byte)0xeb;
	public static final byte SASTORE = (byte)0x56;
	public static final byte BIPUSH = (byte)0x10;
	public static final byte IF_ICMPGT = (byte)0xa3;
	public static final byte DLOAD = (byte)0x18;
	public static final byte FREM = (byte)0x72;
	public static final byte GETFIELD_QUICK = (byte)0xce;
	public static final short WIDE_RET = 0x1a9;
	public static final byte POP = (byte)0x57;
	public static final byte IF_ICMPGE = (byte)0xa2;
	public static final byte NOP = (byte)0x0;
	public static final byte IFNE = (byte)0x9a;
	public static final byte FNEG = (byte)0x76;
	public static final byte LUSHR = (byte)0x7d;
	public static final byte ANEWARRAY_QUICK = (byte)0xde;
	public static final byte PUTFIELD2_QUICK = (byte)0xd1;
	public static final byte LADD = (byte)0x61;
	public static final byte LOR = (byte)0x81;
	public static final byte NEWARRAY_32BIT = (byte)0xfa;
	public static final byte READ_ADDRESS = (byte)0xea;
	public static final byte ATHROW_INJECT = (byte)0xc0;
	public static final byte LDIV = (byte)0x6d;
	public static final byte GETFIELD2_QUICK = (byte)0xd0;
	public static final byte IF_ACMPEQ = (byte)0xa5;
	public static final byte RETURN = (byte)0xb1;
	public static final byte GETSTATIC2_QUICK = (byte)0xd4;
	public static final byte IFNONNULL = (byte)0xc7;
	public static final byte REFL_NEW = (byte)0xb9;
	public static final byte DCONST_1 = (byte)0xf;
	public static final byte DCONST_0 = (byte)0xe;
	public static final byte NEWARRAY_QUICK = (byte)0xdc;
	public static final byte BREAKPOINT = (byte)0xca;
	public static final byte DSTORE_3 = (byte)0x4a;
	public static final byte LASTORE = (byte)0x50;
	public static final byte DSTORE_2 = (byte)0x49;
	public static final byte CALOAD = (byte)0x34;
	public static final byte DSTORE_1 = (byte)0x48;
	public static final byte INVOKEINTERFACE_QUICK = (byte)0xda;
	public static final byte IF_ICMPEQ = (byte)0x9f;
	public static final byte DSTORE_0 = (byte)0x47;
	public static final byte IFLT = (byte)0x9b;
	public static final byte DISABLE_SCHEDULING = (byte)0xed;
	public static final byte REF_TO_INT = (byte)0xe8;
	public static final byte IASTORE = (byte)0x4f;
	public static final byte IFLE = (byte)0x9e;
	public static final byte SALOAD = (byte)0x35;
	public static final byte DUP2 = (byte)0x5c;
	public static final byte FSTORE = (byte)0x38;
	public static final byte ASTORE_3 = (byte)0x4e;
	public static final byte ASTORE_2 = (byte)0x4d;
	public static final byte FSUB = (byte)0x66;
	public static final byte ASTORE_1 = (byte)0x4c;
	public static final byte ASTORE_0 = (byte)0x4b;
	public static final byte LAND = (byte)0x7f;
	public static final short WIDE_FLOAD = 0x117;
	public static final byte FMUL = (byte)0x6a;
	public static final byte CGRA_STOP = (byte)0xfe;
	public static final byte FCMPL = (byte)0x95;
	public static final short WIDE_IINC = 0x184;
	public static final byte FCMPG = (byte)0x96;
	public static final byte LCMP = (byte)0x94;
	public static final byte FLUSHREF = (byte)0xf1;
	public static final byte INVALIDATEREF = (byte)0xf2;
	public static final byte LREM = (byte)0x71;
	public static final byte SIPUSH = (byte)0x11;
	public static final byte DASTORE = (byte)0x52;
	public static final byte DADD = (byte)0x63;
	public static final byte FASTORE = (byte)0x51;
	public static final byte DDIV = (byte)0x6f;
	public static final byte LNEG = (byte)0x75;
	public static final byte PUTSTATIC2_QUICK = (byte)0xd5;


	// some handys debugging routines
	public static String debug(int bc){
		//ALLE Konstanten Variablen der Klasse bestimmen
		try {
			java.lang.reflect.Field fields[] = ByteCode.class.getDeclaredFields();
			for(int i=0; i < fields.length; i++) {
				if (fields[i].getByte(null) == bc)
					return fields[i].getName();
			}
		
		}
		catch (IllegalAccessException e) {}
		catch (SecurityException e) {}
		catch (IllegalArgumentException e) {}
		catch (ArrayIndexOutOfBoundsException e){}

		String ret = String.format("unknown '0x%02x'", bc);
		return ret;
	}

	/**
	 * Returns a Bytecodes Parameter count
	 */
	public static int getParamCount(byte bytecode) {
		switch(bytecode) {
			case ByteCode.NOP:
			case ByteCode.ACONST_NULL:
			case ByteCode.ICONST_M1:
			case ByteCode.ICONST_0:
			case ByteCode.ICONST_1:
			case ByteCode.ICONST_2:
			case ByteCode.ICONST_3:
			case ByteCode.ICONST_4:
			case ByteCode.ICONST_5:
			case ByteCode.LCONST_0:
			case ByteCode.LCONST_1:
			case ByteCode.FCONST_0:
			case ByteCode.FCONST_1:
			case ByteCode.FCONST_2:
			case ByteCode.DCONST_0:
			case ByteCode.DCONST_1:
			case ByteCode.ILOAD_0:
			case ByteCode.FLOAD_0:
			case ByteCode.ALOAD_0:
			case ByteCode.ILOAD_1:
			case ByteCode.FLOAD_1:
			case ByteCode.ALOAD_1:
			case ByteCode.ILOAD_2:
			case ByteCode.FLOAD_2:
			case ByteCode.ALOAD_2:
			case ByteCode.ILOAD_3:
			case ByteCode.FLOAD_3:
			case ByteCode.ALOAD_3:
			case ByteCode.LLOAD_0:
			case ByteCode.LLOAD_1:
			case ByteCode.LLOAD_2:
			case ByteCode.LLOAD_3:
			case ByteCode.DLOAD_0:
			case ByteCode.DLOAD_1:
			case ByteCode.DLOAD_2:
			case ByteCode.DLOAD_3:
			case ByteCode.IALOAD:
			case ByteCode.FALOAD:
			case ByteCode.AALOAD:
			case ByteCode.BALOAD:
			case ByteCode.CALOAD:
			case ByteCode.SALOAD:
			case ByteCode.LALOAD:
			case ByteCode.DALOAD:
			case ByteCode.ISTORE_0:
			case ByteCode.FSTORE_0:
			case ByteCode.ASTORE_0:
			case ByteCode.ISTORE_1:
			case ByteCode.FSTORE_1:
			case ByteCode.ASTORE_1:
			case ByteCode.ISTORE_2:
			case ByteCode.FSTORE_2:
			case ByteCode.ASTORE_2:
			case ByteCode.ISTORE_3:
			case ByteCode.FSTORE_3:
			case ByteCode.ASTORE_3:
			case ByteCode.LSTORE_0:
			case ByteCode.LSTORE_1:
			case ByteCode.LSTORE_2:
			case ByteCode.LSTORE_3:
			case ByteCode.DSTORE_0:
			case ByteCode.DSTORE_1:
			case ByteCode.DSTORE_2:
			case ByteCode.DSTORE_3:
			case ByteCode.IASTORE:
			case ByteCode.FASTORE:
			case ByteCode.AASTORE:
			case ByteCode.BASTORE:
			case ByteCode.CASTORE:
			case ByteCode.SASTORE:
			case ByteCode.LASTORE:
			case ByteCode.DASTORE:
			case ByteCode.POP:
			case ByteCode.POP2:
			case ByteCode.DUP:
			case ByteCode.DUP_X1:
			case ByteCode.DUP_X2:
			case ByteCode.DUP2:
			case ByteCode.DUP2_X1:
			case ByteCode.DUP2_X2:
			case ByteCode.SWAP:
			case ByteCode.IADD:
			case ByteCode.LADD:
			case ByteCode.FADD:
			case ByteCode.DADD:
			case ByteCode.ISUB:
			case ByteCode.LSUB:
			case ByteCode.FSUB:
			case ByteCode.DSUB:
			case ByteCode.IMUL:
			case ByteCode.LMUL:
			case ByteCode.FMUL:
			case ByteCode.DMUL:
			case ByteCode.IDIV:
			case ByteCode.LDIV:
			case ByteCode.FDIV:
			case ByteCode.DDIV:
			case ByteCode.IREM:
			case ByteCode.LREM:
			case ByteCode.FREM:
			case ByteCode.DREM:
			case ByteCode.INEG:
			case ByteCode.LNEG:
			case ByteCode.FNEG:
			case ByteCode.DNEG:
			case ByteCode.ISHL:
			case ByteCode.LSHL:
			case ByteCode.ISHR:
			case ByteCode.LSHR:
			case ByteCode.IUSHR:
			case ByteCode.LUSHR:
			case ByteCode.IAND:
			case ByteCode.LAND:
			case ByteCode.IOR:
			case ByteCode.LOR:
			case ByteCode.IXOR:
			case ByteCode.LXOR:
			case ByteCode.I2L:
			case ByteCode.I2F:
			case ByteCode.I2D:
			case ByteCode.L2I:
			case ByteCode.L2F:
			case ByteCode.L2D:
			case ByteCode.F2I:
			case ByteCode.F2L:
			case ByteCode.F2D:
			case ByteCode.D2I:
			case ByteCode.D2L:
			case ByteCode.D2F:
			case ByteCode.I2B:
			case ByteCode.I2C:
			case ByteCode.I2S:
			case ByteCode.LCMP:
			case ByteCode.FCMPG:
			case ByteCode.FCMPL:
			case ByteCode.DCMPG:
			case ByteCode.DCMPL:
			case ByteCode.IRETURN:
			case ByteCode.ARETURN:
			case ByteCode.FRETURN:
			case ByteCode.LRETURN:
			case ByteCode.DRETURN:
			case ByteCode.RETURN:
			case ByteCode.ARRAYLENGTH:
				return 0;
			case ByteCode.BIPUSH:
			case ByteCode.ILOAD:
			case ByteCode.FLOAD:
			case ByteCode.ALOAD:
			case ByteCode.LLOAD:
			case ByteCode.DLOAD:
			case ByteCode.ISTORE:
			case ByteCode.FSTORE:
			case ByteCode.ASTORE:
			case ByteCode.LSTORE:
			case ByteCode.DSTORE:
			case ByteCode.RET:
			case ByteCode.NEWARRAY_QUICK:
				return 1;
			case ByteCode.INVOKEVIRTUAL_QUICK:
			case ByteCode.INVOKENONVIRTUAL_QUICK:
			case ByteCode.SIPUSH:
			case ByteCode.IINC:
			case ByteCode.IFEQ:
			case ByteCode.IFNE:
			case ByteCode.IFLT:
			case ByteCode.IFGE:
			case ByteCode.IFGT:
			case ByteCode.IFLE:
			case ByteCode.IF_ICMPEQ:
			case ByteCode.IF_ACMPEQ:
			case ByteCode.IF_ICMPNE:
			case ByteCode.IF_ACMPNE:
			case ByteCode.IF_ICMPLT:
			case ByteCode.IF_ICMPGE:
			case ByteCode.IF_ICMPGT:
			case ByteCode.IF_ICMPLE:
			case ByteCode.GOTO:
			case ByteCode.JSR:
			case ByteCode.IFNULL:
			case ByteCode.LDC_W_QUICK:
			case ByteCode.LDC2_W_QUICK:
			case ByteCode.GETFIELD_QUICK:
			case ByteCode.PUTFIELD_QUICK:
			case ByteCode.GETFIELD2_QUICK:
			case ByteCode.PUTFIELD2_QUICK:
			case ByteCode.GETFIELDA_QUICK:
			case ByteCode.GETSTATIC_QUICK:
			case ByteCode.PUTSTATIC_QUICK:
			case ByteCode.GETSTATIC2_QUICK:
			case ByteCode.PUTSTATIC2_QUICK:
			case ByteCode.GETSTATICA_QUICK:
			case ByteCode.NEW_QUICK:
			case ByteCode.ANEWARRAY_QUICK:
			case ByteCode.LDC_STRING:
			case ByteCode.READ_ADDRESS:
			case ByteCode.WRITE_ADDRESS:
				return 2;
			case ByteCode.MULTIANEWARRAY_QUICK:
				return 3;
			case ByteCode.INVOKEINTERFACE_QUICK:
 			case ByteCode.GOTO_W:
 			case ByteCode.JSR_W:
				return 4;

			default:
				return 0;
 		}
	}

	/**
	 * prints the Mnemonic representation of a Bytecode (with parameters)
	 */
	public static String mnemonic(byte bc[]) { 
		switch(bc[0]) {
			case (byte)0x00: return "NOP";
			case (byte)0x01: return "ACONST_NULL";
			case (byte)0x02: return "ICONST_M1";
			case (byte)0x03: return "ICONST_0";
			case (byte)0x04: return "ICONST_1";
			case (byte)0x05: return "ICONST_2";
			case (byte)0x06: return "ICONST_3";
			case (byte)0x07: return "ICONST_4";
			case (byte)0x08: return "ICONST_5";
			case (byte)0x09: return "LCONST_0";
			case (byte)0x0a: return "LCONST_1";
			case (byte)0x0b: return "FCONST_0";
			case (byte)0x0c: return "FCONST_1";
			case (byte)0x0d: return "FCONST_2";
			case (byte)0x0e: return "DCONST_0";
			case (byte)0x0f: return "DCONST_1";
			case (byte)0x10: return "BIPUSH " + (bc[1]&0xFF);
			case (byte)0x11: return "SIPUSH " + (bc[1]<<8 | bc[2]&0xFF);
			case (byte)0x15: return "ILOAD " + (bc[1]&0xFF);
			case (byte)0x16: return "LLOAD " + (bc[1]&0xFF);
			case (byte)0x17: return "FLOAD " + (bc[1]&0xFF);
			case (byte)0x18: return "DLOAD " + (bc[1]&0xFF);
			case (byte)0x19: return "ALOAD " + (bc[1]&0xFF);
			case (byte)0x1a: return "ILOAD_0";
			case (byte)0x1b: return "ILOAD_1";
			case (byte)0x1c: return "ILOAD_2";
			case (byte)0x1d: return "ILOAD_3";
			case (byte)0x1e: return "LLOAD_0";
			case (byte)0x1f: return "LLOAD_1";
			case (byte)0x20: return "LLOAD_2";
			case (byte)0x21: return "LLOAD_3";
			case (byte)0x22: return "FLOAD_0";
			case (byte)0x23: return "FLOAD_1";
			case (byte)0x24: return "FLOAD_2";
			case (byte)0x25: return "FLOAD_3";
			case (byte)0x26: return "DLOAD_0";
			case (byte)0x27: return "DLOAD_1";
			case (byte)0x28: return "DLOAD_2";
			case (byte)0x29: return "DLOAD_3";
			case (byte)0x2a: return "ALOAD_0";
			case (byte)0x2b: return "ALOAD_1";
			case (byte)0x2c: return "ALOAD_2";
			case (byte)0x2d: return "ALOAD_3";
			case (byte)0x2e: return "IALOAD";
			case (byte)0x2f: return "LALOAD";
			case (byte)0x30: return "FALOAD";
			case (byte)0x31: return "DALOAD";
			case (byte)0x32: return "AALOAD";
			case (byte)0x33: return "BALOAD";
			case (byte)0x34: return "CALOAD";
			case (byte)0x35: return "SALOAD";
			case (byte)0x36: return "ISTORE " + (bc[1]&0xFF);
			case (byte)0x37: return "LSTORE " + (bc[1]&0xFF);
			case (byte)0x38: return "FSTORE " + (bc[1]&0xFF);
			case (byte)0x39: return "DSTORE " + (bc[1]&0xFF);
			case (byte)0x3a: return "ASTORE " + (bc[1]&0xFF);
			case (byte)0x3b: return "ISTORE_0";
			case (byte)0x3c: return "ISTORE_1";
			case (byte)0x3d: return "ISTORE_2";
			case (byte)0x3e: return "ISTORE_3";
			case (byte)0x3f: return "LSTORE_0";
			case (byte)0x40: return "LSTORE_1";
			case (byte)0x41: return "LSTORE_2";
			case (byte)0x42: return "LSTORE_3";
			case (byte)0x43: return "FSTORE_0";
			case (byte)0x44: return "FSTORE_1";
			case (byte)0x45: return "FSTORE_2";
			case (byte)0x46: return "FSTORE_3";
			case (byte)0x47: return "DSTORE_0";
			case (byte)0x48: return "DSTORE_1";
			case (byte)0x49: return "DSTORE_2";
			case (byte)0x4a: return "DSTORE_3";
			case (byte)0x4b: return "ASTORE_0";
			case (byte)0x4c: return "ASTORE_1";
			case (byte)0x4d: return "ASTORE_2";
			case (byte)0x4e: return "ASTORE_3";
			case (byte)0x4f: return "IASTORE";
			case (byte)0x50: return "LASTORE";
			case (byte)0x51: return "FASTORE";
			case (byte)0x52: return "DASTORE";
			case (byte)0x53: return "AASTORE";
			case (byte)0x54: return "BASTORE";
			case (byte)0x55: return "CASTORE";
			case (byte)0x56: return "SASTORE";
			case (byte)0x57: return "POP";
			case (byte)0x58: return "POP2";
			case (byte)0x59: return "DUP";
			case (byte)0x5a: return "DUP_X1";
			case (byte)0x5b: return "DUP_X2";
			case (byte)0x5c: return "DUP2";
			case (byte)0x5d: return "DUP2_X1";
			case (byte)0x5e: return "DUP2_X2";
			case (byte)0x5f: return "SWAP";
			case (byte)0x60: return "IADD";
			case (byte)0x61: return "LADD";
			case (byte)0x62: return "FADD";
			case (byte)0x63: return "DADD";
			case (byte)0x64: return "ISUB";
			case (byte)0x65: return "LSUB";
			case (byte)0x66: return "FSUB";
			case (byte)0x67: return "DSUB";
			case (byte)0x68: return "IMUL";
			case (byte)0x69: return "LMUL";
			case (byte)0x6a: return "FMUL";
			case (byte)0x6b: return "DMUL";
			case (byte)0x6c: return "IDIV";
			case (byte)0x6d: return "LDIV";
			case (byte)0x6e: return "FDIV";
			case (byte)0x6f: return "DDIV";
			case (byte)0x70: return "IREM";
			case (byte)0x71: return "LREM";
			case (byte)0x72: return "FREM";
			case (byte)0x73: return "DREM";
			case (byte)0x74: return "INEG";
			case (byte)0x75: return "LNEG";
			case (byte)0x76: return "FNEG";
			case (byte)0x77: return "DNEG";
			case (byte)0x78: return "ISHL";
			case (byte)0x79: return "LSHL";
			case (byte)0x7a: return "ISHR";
			case (byte)0x7b: return "LSHR";
			case (byte)0x7c: return "IUSHR";
			case (byte)0x7d: return "LUSHR";
			case (byte)0x7e: return "IAND";
			case (byte)0x7f: return "LAND";
			case (byte)0x80: return "IOR";
			case (byte)0x81: return "LOR";
			case (byte)0x82: return "IXOR";
			case (byte)0x83: return "LXOR";
			case (byte)0x84: return "IINC " + (bc[1]&0xFF)+ " " + (bc[2]&0xFF);
			case (byte)0x85: return "I2L";
			case (byte)0x86: return "I2F";
			case (byte)0x87: return "I2D";
			case (byte)0x88: return "L2I";
			case (byte)0x89: return "L2F";
			case (byte)0x8a: return "L2D";
			case (byte)0x8b: return "F2I";
			case (byte)0x8c: return "F2L";
			case (byte)0x8d: return "F2D";
			case (byte)0x8e: return "D2I";
			case (byte)0x8f: return "D2L";
			case (byte)0x90: return "D2F";
			case (byte)0x91: return "I2B";
			case (byte)0x92: return "I2C";
			case (byte)0x93: return "I2S";
			case (byte)0x94: return "LCMP";
			case (byte)0x95: return "FCMPL";
			case (byte)0x96: return "FCMPG";
			case (byte)0x97: return "DCMPL";
			case (byte)0x98: return "DCMPG";
			case (byte)0x99: return "IFEQ " + (bc[1]<<8 | bc[2]&0xFF);
			case (byte)0x9a: return "IFNE " + (bc[1]<<8 | bc[2]&0xFF);
			case (byte)0x9b: return "IFLT " + (bc[1]<<8 | bc[2]&0xFF);
			case (byte)0x9c: return "IFGE " + (bc[1]<<8 | bc[2]&0xFF);
			case (byte)0x9d: return "IFGT " + (bc[1]<<8 | bc[2]&0xFF);
			case (byte)0x9e: return "IFLE " + (bc[1]<<8 | bc[2]&0xFF);
			case (byte)0x9f: return "IF_ICMPEQ " + (bc[1]<<8 | bc[2]&0xFF);
			case (byte)0xa0: return "IF_ICMPNE " + (bc[1]<<8 | bc[2]&0xFF);
			case (byte)0xa1: return "IF_ICMPLT " + (bc[1]<<8 | bc[2]&0xFF);
			case (byte)0xa2: return "IF_ICMPGE " + (bc[1]<<8 | bc[2]&0xFF);
			case (byte)0xa3: return "IF_ICMPGT " + (bc[1]<<8 | bc[2]&0xFF);
			case (byte)0xa4: return "IF_ICMPLE " + (bc[1]<<8 | bc[2]&0xFF);
			case (byte)0xa5: return "IF_ACMPEQ " + (bc[1]<<8 | bc[2]&0xFF);
			case (byte)0xa6: return "IF_ACMPNE " + (bc[1]<<8 | bc[2]&0xFF);
			case (byte)0xa7: return "GOTO " + (bc[1]<<8 | bc[2]&0xFF);
			case (byte)0xa8: return "JSR " + (bc[1]<<8 | bc[2]&0xFF);
			case (byte)0xa9: return "RET";
			case (byte)0xac: return "IRETURN";
			case (byte)0xad: return "LRETURN";
			case (byte)0xae: return "FRETURN";
			case (byte)0xaf: return "DRETURN";
			case (byte)0xb0: return "ARETURN";
			case (byte)0xb1: return "RETURN";

			case (byte)0xbe: return "ARRAYLENGTH";
			case (byte)0xbf: return "ATHROW";
			case (byte)0xc4: return "WIDE";
			case (byte)0xc6: return "IFNULL " + (bc[1]<<8 | bc[2]&0xFF);
			case (byte)0xc7: return "IFNONNULL " + (bc[1]<<8 | bc[2]&0xFF);
			case (byte)0xc8: return "GOTO_W";
			case (byte)0xc9: return "JSR_W";

			case (byte)0xcc: return "LDC_W_QUICK " + (bc[1]<<8 | bc[2]&0xFF);
			case (byte)0xcd: return "LDC2_W_QUICK " + (bc[1]<<8 | bc[2]&0xFF);
			case (byte)0xce: return "GETFIELD_QUICK " + (bc[1]<<8 | bc[2]&0xFF);
			case (byte)0xcf: return "PUTFIELD_QUICK " + (bc[1]<<8 | bc[2]&0xFF);
			case (byte)0xd0: return "GETFIELD2_QUICK " + (bc[1]<<8 | bc[2]&0xFF);
			case (byte)0xd1: return "PUTFIELD2_QUICK " + (bc[1]<<8 | bc[2]&0xFF);
			case (byte)0xd2: return "GETSTATIC_QUICK " + (bc[1]<<8 | bc[2]&0xFF);
			case (byte)0xd3: return "PUTSTATIC_QUICK " + (bc[1]<<8 | bc[2]&0xFF);
			case (byte)0xd4: return "GETSTATIC2_QUICK " + (bc[1]<<8 | bc[2]&0xFF);
			case (byte)0xd5: return "PUTSTATIC2_QUICK " + (bc[1]<<8 | bc[2]&0xFF);
			case (byte)0xd6: return "INVOKEVIRTUAL_QUICK Number of Arguments: " + (bc[1] >> 2 & 0xFF) + ", Relative Method Table Index: " + (bc[1] & 0x3 << 8 | bc[2]&0xFF);
			case (byte)0xd7: return "INVOKENONVIRTUAL_QUICK Method Table Index: " + (bc[1]<<8 | bc[2]&0xFF);
			case (byte)0xda: return "INVOKEINTERFACE_QUICK Number of Arguments: " + (bc[1] >> 2 & 0xFF) + ", Interface Index of Ordered List of Interfaces: " + (bc[1] & 0x3 <<8 | bc[2]&0xFF) + ", Relative Interface Method Table Index: " + (bc[3]<<8 | bc[4]&0xFF);
			case (byte)0xdc: return "NEWARRAY_QUICK " + (bc[1]);
			case (byte)0xdd: return "NEW_QUICK " + (bc[1]<<8 | bc[2]&0xFF);
			case (byte)0xde: return "ANEWARRAY_QUICK " + (bc[1]<<8 | bc[2]&0xFF);
			case (byte)0xdf: return "MULTINEWARRAY_QUICK " + (bc[1]<<8 | bc[2]&0xFF) + ", dim = " + bc[3];
			case (byte)0xe0: return "CHECKCAST_QUICK " + (bc[1]<<8 | bc[2]&0xFF);
			case (byte)0xe1: return "INSTANCEOF_QUICK " + (bc[1]<<8 | bc[2]&0xFF);

			case (byte)0xe2: return "INVOKENONVIRTUAL_QUICK_P2";
			case (byte)0xe3: return "INVOKENONVIRTUAL_QUICK_SYNC_P2";
			case (byte)0xe4: return "INVOKESTATIC_QUICK_SYNC_P2";
			
			case (byte)0xe5: return "LDC_STRING " + (bc[1]<<8 | bc[2]&0xFF);
			case (byte)0xe6: return "GETFIELDA_QUICK " + (bc[1]<<8 | bc[2]&0xFF);
			case (byte)0xe7: return "GETSTATICA_QUICK " + (bc[1]<<8 | bc[2]&0xFF);
			case (byte)0xe8: return "REF2INT " + (bc[1]<<8 | bc[2]&0xFF);
			case (byte)0xe9: return "INT2REF " + (bc[1]<<8 | bc[2]&0xFF);
			case (byte)0xea: return "READ_ADDRESS " + (bc[1]<<8 | bc[2]&0xFF);
			case (byte)0xeb: return "WRITE_ADDRESS " + (bc[1]<<8 | bc[2]&0xFF);
			case (byte)0xec: return "GETMONITORID " + (bc[1]<<8 | bc[2]&0xFF);
			case (byte)0xed: return "DISABLESCHEDULING";
			case (byte)0xee: return "FORCESCHEDULING";
			case (byte)0xef: return "ENABLESCHEDULING";
			
			case (byte)0xf3: return "APPLY_TOKENSET " + (bc[1]&0xFF);

			case (byte)0xf4: return "RETURN_64BIT_SYNC";
			case (byte)0xf5: return "RETURN_32BIT_SYNC";
			case (byte)0xf6: return "RETURN_SYNC";
			case (byte)0xf7: return "TSTORE_0(Thread-ID:" + (bc[1]<<8 | bc[2]&0xFF) + ")";
			case (byte)0xf8: return "INVOKESTATIC_QUICK_SYNC(ci=" + (bc[1]&0xFF) + ", mi=  " + (bc[2]&0xFF) + ")";
			case (byte)0xf9: return "INVOKENONVIRTUAL_QUICK_SYNC(ci=" + (bc[1]&0xFF) + ", mi=  " + (bc[2]&0xFF) + ")";
			case (byte)0xfa: return "INVOKEVIRTUAL_QUICK_SYNC(paranum:" + (bc[1]&0xFF) + ", mi=  " + (bc[2]&0xFF) + ")";
			case (byte)0xfb: return "INVOKE_NATIVE " + (bc[1]<<8 | bc[2]&0xFF);
			case (byte)0xfc: return "SWITCHCLEANTHREAD";
			case (byte)0xfd: return "SWITCHTHREAD";
			case (byte)0xfe: return "SYNTHESIZED ts=" + (bc[1]&0xFF) + " " + (bc[2]<<8 | bc[3]&0xFF);
			case (byte)0xff: return "STOP";
			default: return "nix gefunden zu " + Integer.toHexString(bc[0]&0xFF);
		}
	}

	/**
	 * prints the Mnemonic representation of a sequence of Bytecodes
	 * 
	 * this can horribly fail, of the sequence is not a valid instruction sequence
	 */
	public static String mnemonic(ByteBuffer buffer, String separator) {
		StringBuilder ret  = new StringBuilder();
		int pos  = 0;
		
		while(pos < buffer.capacity()) {
			// make a short array just for this instruction
			byte[] bytecode = new byte[getParamCount(buffer.get(pos)) + 1]; 
			for(int i = 0; i < bytecode.length; i++) bytecode[i] = buffer.get(pos+i);
			pos += bytecode.length;
			ret.append(ByteCode.mnemonic(bytecode));

			if(pos < buffer.capacity()) ret.append(separator);
		}
		return ret.toString();
		}
	}
