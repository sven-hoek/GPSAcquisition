package javasim.synth.model;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import javasim.synth.SequenceNotSynthesizeableException;
import javasim.synth.SynthData;
import javasim.synth.model.instruction.ALUbinopInstr;
import javasim.synth.model.instruction.ALUunopInstr;
import javasim.synth.model.instruction.ArrayLdInstr;
import javasim.synth.model.instruction.ArrayLengthInstr;
import javasim.synth.model.instruction.ArrayStInstr;
import javasim.synth.model.instruction.ConstLdInstr;
import javasim.synth.model.instruction.DummyInstr;
import javasim.synth.model.instruction.Dup2Instr;
import javasim.synth.model.instruction.Dup2X1Instr;
import javasim.synth.model.instruction.Dup2X2Instr;
import javasim.synth.model.instruction.DupInstr;
import javasim.synth.model.instruction.DupX1Instr;
import javasim.synth.model.instruction.DupX2Instr;
import javasim.synth.model.instruction.GOTOInstr;
import javasim.synth.model.instruction.IFInstr;
import javasim.synth.model.instruction.IF_ICMPInstr;
import javasim.synth.model.instruction.IINCInstr;
import javasim.synth.model.instruction.Instruction;
import javasim.synth.model.instruction.InvalidInstr;
import javasim.synth.model.instruction.InvokeNonVirtualInstr;
import javasim.synth.model.instruction.InvokeStaticInstr;
import javasim.synth.model.instruction.InvokeVirtualInstr;
import javasim.synth.model.instruction.LVLdInstr;
import javasim.synth.model.instruction.LVStInstr;
import javasim.synth.model.instruction.OVLdInstr;
import javasim.synth.model.instruction.OVStInstr;
import javasim.synth.model.instruction.PHIInstr;
import javasim.synth.model.instruction.PopInstr;
import javasim.synth.model.instruction.ReturnInstr;
import javasim.synth.model.instruction.SVLdInstr;
import javasim.synth.model.instruction.SVStInstr;
import javasim.synth.model.instruction.StartInstr;
import javasim.synth.model.instruction.StopInstr;
import javasim.synth.model.instruction.SwapInstr;

/**
 * This enum holds the full instruction set of the AMIDAR virtual machine.
 * Every instruction holds its byte code value, length, hardware instruction class
 * and modifies its synthesis context in SynthData to its needs.
 * The calculation function for each instruction is also specified here.
 *
 * The actual hardware instruction instance is constructed via a call to create().
 * @author Michael Raitza
 * @version - 11.04.2011
 */
public enum I {
	NOP (0x00, Instruction.class, 1, false),
	ACONST_NULL (0x01, ConstLdInstr.class, 1, false) { public Number getByteCodeParameter(SynthData data) { return -1;/* N.B. This machine defines the NULL reference as -1 */ } },
	ICONST_M1 (0x02, ConstLdInstr.class, 1, false) { public Number getByteCodeParameter(SynthData data) { return -1; } },
	ICONST_0 (0x03, ConstLdInstr.class, 1, false) { public Number getByteCodeParameter(SynthData data) { return (Integer)0; } },
	ICONST_1 (0x04, ConstLdInstr.class, 1, false) { public Number getByteCodeParameter(SynthData data) { return (Integer)1; } },
	ICONST_2 (0x05, ConstLdInstr.class, 1, false) { public Number getByteCodeParameter(SynthData data) { return (Integer)2; } },
	ICONST_3 (0x06, ConstLdInstr.class, 1, false) { public Number getByteCodeParameter(SynthData data) { return (Integer)3; } },
	ICONST_4 (0x07, ConstLdInstr.class, 1, false) { public Number getByteCodeParameter(SynthData data) { return (Integer)4; } },
	ICONST_5 (0x08, ConstLdInstr.class, 1, false) { public Number getByteCodeParameter(SynthData data) { return (Integer)5; } },
	LCONST_0 (0x09, ConstLdInstr.class, 1, true) { public Number getByteCodeParameter(SynthData data) { return new Long(0); } },
	LCONST_1 (0x0a, ConstLdInstr.class, 1, true) { public Number getByteCodeParameter(SynthData data) { return new Long(1); } },
	FCONST_0 (0x0b, ConstLdInstr.class, 1, false) { public Number getByteCodeParameter(SynthData data) { return new Integer(Float.floatToRawIntBits(0.0f)); } },
	FCONST_1 (0x0c, ConstLdInstr.class, 1, false) { public Number getByteCodeParameter(SynthData data) { return new Integer(Float.floatToRawIntBits(1.0f)); } },
	FCONST_2 (0x0d, ConstLdInstr.class, 1, false) { public Number getByteCodeParameter(SynthData data) { return new Integer(Float.floatToRawIntBits(2.0f)); } },
	DCONST_0 (0x0e, ConstLdInstr.class, 1, true) { public Number getByteCodeParameter(SynthData data) { return new Long(Double.doubleToRawLongBits(0.0)); } },
	DCONST_1 (0x0f, ConstLdInstr.class, 1, true) { public Number getByteCodeParameter(SynthData data) { return new Long(Double.doubleToRawLongBits(1.0)); } },
	BIPUSH (0x10, ConstLdInstr.class, 2, false) { public Number getByteCodeParameter(SynthData data) {
		Integer res = new Integer(data.code(data.pos()+1));
		if ((res >> 7) == 1) //signextension
			res|=0xFFFFFF00;
		return res;
		}
	},
	SIPUSH (0x11, ConstLdInstr.class, 3, false) { public Number getByteCodeParameter(SynthData data) { return new Integer(data.code_w(data.pos()+1)); } },
	LDC (0x12, ConstLdInstr.class, 2, false) { public Number getByteCodeParameter(SynthData data) { return new Integer(data.code(data.pos()+1)); } },
	LDC_W (0x13, ConstLdInstr.class, 3, false) { public Number getByteCodeParameter(SynthData data) { return new Integer(data.code_w(data.pos()+1)); } },
	LDC2_W (0x14, ConstLdInstr.class, 3, true) { public Number getByteCodeParameter(SynthData data) { return new Long(data.code_w(data.pos()+1)); } },
	ILOAD (0x15, LVLdInstr.class, 2, false) { public Number getByteCodeParameter(SynthData data) { return new Integer(data.code(data.pos()+1)); } },
	LLOAD (0x16, LVLdInstr.class, 2, true) { public Number getByteCodeParameter(SynthData data) { return new Integer(data.code(data.pos()+1)); } },
	FLOAD (0x17, LVLdInstr.class, 2, false) { public Number getByteCodeParameter(SynthData data) { return new Integer(data.code(data.pos()+1)); } },
	DLOAD (0x18, LVLdInstr.class, 2, true) { public Number getByteCodeParameter(SynthData data) { return new Integer(data.code(data.pos()+1)); } },
	ALOAD (0x19, LVLdInstr.class, 2, false, true) { public Number getByteCodeParameter(SynthData data) { return new Integer(data.code(data.pos()+1)); } },
	ILOAD_0 (0x1a, LVLdInstr.class, 1, false) { public Number getByteCodeParameter(SynthData data) { return (Integer)0; } },
	ILOAD_1 (0x1b, LVLdInstr.class, 1, false) { public Number getByteCodeParameter(SynthData data) { return (Integer)1; } },
	ILOAD_2 (0x1c, LVLdInstr.class, 1, false) { public Number getByteCodeParameter(SynthData data) { return (Integer)2; } },
	ILOAD_3 (0x1d, LVLdInstr.class, 1, false) { public Number getByteCodeParameter(SynthData data) { return (Integer)3; } },
	LLOAD_0 (0x1e, LVLdInstr.class, 1, true) { public Number getByteCodeParameter(SynthData data) { return (Integer)0; } },
	LLOAD_1 (0x1f, LVLdInstr.class, 1, true) { public Number getByteCodeParameter(SynthData data) { return (Integer)1; } },
	LLOAD_2 (0x20, LVLdInstr.class, 1, true) { public Number getByteCodeParameter(SynthData data) { return (Integer)2; } },
	LLOAD_3 (0x21, LVLdInstr.class, 1, true) { public Number getByteCodeParameter(SynthData data) { return (Integer)3; } },
	FLOAD_0 (0x22, LVLdInstr.class, 1, false) { public Number getByteCodeParameter(SynthData data) { return (Integer)0; } },
	FLOAD_1 (0x23, LVLdInstr.class, 1, false) { public Number getByteCodeParameter(SynthData data) { return (Integer)1; } },
	FLOAD_2 (0x24, LVLdInstr.class, 1, false) { public Number getByteCodeParameter(SynthData data) { return (Integer)2; } },
	FLOAD_3 (0x25, LVLdInstr.class, 1, false) { public Number getByteCodeParameter(SynthData data) { return (Integer)3; } },
	DLOAD_0 (0x26, LVLdInstr.class, 1, true) { public Number getByteCodeParameter(SynthData data) { return (Integer)0; } },
	DLOAD_1 (0x27, LVLdInstr.class, 1, true) { public Number getByteCodeParameter(SynthData data) { return (Integer)1; } },
	DLOAD_2 (0x28, LVLdInstr.class, 1, true) { public Number getByteCodeParameter(SynthData data) { return (Integer)2; } },
	DLOAD_3 (0x29, LVLdInstr.class, 1, true) { public Number getByteCodeParameter(SynthData data) { return (Integer)3; } },
	ALOAD_0 (0x2a, LVLdInstr.class, 1, false, true) { public Number getByteCodeParameter(SynthData data) { return (Integer)0; } },
	ALOAD_1 (0x2b, LVLdInstr.class, 1, false, true) { public Number getByteCodeParameter(SynthData data) { return (Integer)1; } },
	ALOAD_2 (0x2c, LVLdInstr.class, 1, false, true) { public Number getByteCodeParameter(SynthData data) { return (Integer)2; } },
	ALOAD_3 (0x2d, LVLdInstr.class, 1, false, true) { public Number getByteCodeParameter(SynthData data) { return (Integer)3; } },
	IALOAD (0x2e, ArrayLdInstr.class, 1, false),
	LALOAD (0x2f, ArrayLdInstr.class, 1, true),
	FALOAD (0x30, ArrayLdInstr.class, 1, false),
	DALOAD (0x31, ArrayLdInstr.class, 1, true),
	AALOAD (0x32, ArrayLdInstr.class, 1, false, true),
	BALOAD (0x33, ArrayLdInstr.class, 1, false),
	CALOAD (0x34, ArrayLdInstr.class, 1, false),
	SALOAD (0x35, ArrayLdInstr.class, 1, false),
	IINCISTORE (-111, LVStInstr.class, 2, false) { public Number getByteCodeParameter(SynthData data) { return data.code(data.pos()+1); }},

	ISTORE (0x36, LVStInstr.class, 2, false) { public Number getByteCodeParameter(SynthData data) { return data.code(data.pos()+1); }},
	LSTORE (0x37, LVStInstr.class, 2, true) { public Number getByteCodeParameter(SynthData data) { return data.code(data.pos()+1); }},
	FSTORE (0x38, LVStInstr.class, 2, false) { public Number getByteCodeParameter(SynthData data) { return data.code(data.pos()+1); }},
	DSTORE (0x39, LVStInstr.class, 2, true) { public Number getByteCodeParameter(SynthData data) { return data.code(data.pos()+1); } },
	ASTORE (0x3a, LVStInstr.class, 2, false,true) { public Number getByteCodeParameter(SynthData data) { return data.code(data.pos()+1); } }, // TODO
	ISTORE_0 (0x3b, LVStInstr.class, 1, false) { public Number getByteCodeParameter(SynthData data) { return 0; }},
	ISTORE_1 (0x3c, LVStInstr.class, 1, false) { public Number getByteCodeParameter(SynthData data) { return 1; }},
	ISTORE_2 (0x3d, LVStInstr.class, 1, false) { public Number getByteCodeParameter(SynthData data) { return 2; }},
	ISTORE_3 (0x3e, LVStInstr.class, 1, false) { public Number getByteCodeParameter(SynthData data) { return 3; }},
	LSTORE_0 (0x3f, LVStInstr.class, 1, true) { public Number getByteCodeParameter(SynthData data) { return 0; } },
	LSTORE_1 (0x40, LVStInstr.class, 1, true) { public Number getByteCodeParameter(SynthData data) { return 1; } },
	LSTORE_2 (0x41, LVStInstr.class, 1, true) { public Number getByteCodeParameter(SynthData data) { return 2; } },
	LSTORE_3 (0x42, LVStInstr.class, 1, true) { public Number getByteCodeParameter(SynthData data) { return 3; } },
	FSTORE_0 (0x43, LVStInstr.class, 1, false) { public Number getByteCodeParameter(SynthData data) { return 0; } },
	FSTORE_1 (0x44, LVStInstr.class, 1, false) { public Number getByteCodeParameter(SynthData data) { return 1; }},
	FSTORE_2 (0x45, LVStInstr.class, 1, false) { public Number getByteCodeParameter(SynthData data) { return 2; }},
	FSTORE_3 (0x46, LVStInstr.class, 1, false) { public Number getByteCodeParameter(SynthData data) { return 3; } },
	DSTORE_0 (0x47, LVStInstr.class, 1, true) { public Number getByteCodeParameter(SynthData data) { return 0; } },
	DSTORE_1 (0x48, LVStInstr.class, 1, true) { public Number getByteCodeParameter(SynthData data) { return 1; }},
	DSTORE_2 (0x49, LVStInstr.class, 1, true) { public Number getByteCodeParameter(SynthData data) { return 2; } },
	DSTORE_3 (0x4a, LVStInstr.class, 1, true) { public Number getByteCodeParameter(SynthData data) { return 3; } },
	ASTORE_0 (0x4b, LVStInstr.class, 1, false, true) { public Number getByteCodeParameter(SynthData data) { return 0; } },
	ASTORE_1 (0x4c, LVStInstr.class, 1, false, true) { public Number getByteCodeParameter(SynthData data) { return 1; } },
	ASTORE_2 (0x4d, LVStInstr.class, 1, false, true) { public Number getByteCodeParameter(SynthData data) { return 2; } },
	ASTORE_3 (0x4e, LVStInstr.class, 1, false, true) { public Number getByteCodeParameter(SynthData data) { return 3; } },
	IASTORE (0x4f, ArrayStInstr.class, 1, false) ,
	LASTORE (0x50, ArrayStInstr.class, 1, true) ,
	FASTORE (0x51, ArrayStInstr.class, 1, false),
	DASTORE (0x52, ArrayStInstr.class, 1, true),
	AASTORE (0x53, ArrayStInstr.class, 1, false),
	BASTORE (0x54, ArrayStInstr.class, 1, false),
	CASTORE (0x55, ArrayStInstr.class, 1, false),
	SASTORE (0x56, ArrayStInstr.class, 1, false),
	POP (0x57, PopInstr.class, 1, false),
	POP2 (0x58, PopInstr.class, 1, true),
	DUP (0x59, DupInstr.class, 1, false),
	DUP_X1 (0x5a, DupX1Instr.class, 1, true),
	DUP_X2 (0x5b, DupX2Instr.class, 1, false),
	DUP2 (0x5c, Dup2Instr.class, 1, false),
	DUP2_X1 (0x5d, Dup2X1Instr.class, 1, false),
	DUP2_X2 (0x5e, Dup2X2Instr.class, 1, false),
	SWAP (0x5f, SwapInstr.class, 1, false),
	IADD (0x60, ALUbinopInstr.class, 1, false),
	LADD (0x61, ALUbinopInstr.class, 1, true),
	FADD (0x62, ALUbinopInstr.class, 1, false),
	DADD (0x63, ALUbinopInstr.class, 1, true),
	ISUB (0x64, ALUbinopInstr.class, 1, false),
	LSUB (0x65, ALUbinopInstr.class, 1, true),
	FSUB (0x66, ALUbinopInstr.class, 1, false),
	DSUB (0x67, ALUbinopInstr.class, 1, true),
	IMUL (0x68, ALUbinopInstr.class, 1, false),
	LMUL (0x69, ALUbinopInstr.class, 1, true) ,
	FMUL (0x6a, ALUbinopInstr.class, 1, false) ,
	DMUL (0x6b, ALUbinopInstr.class, 1, true),
	IDIV (0x6c, ALUbinopInstr.class, 1, false),
	LDIV (0x6d, ALUbinopInstr.class, 1, true),
	FDIV (0x6e, ALUbinopInstr.class, 1, false),
	DDIV (0x6f, ALUbinopInstr.class, 1, true),
	IREM (0x70, ALUbinopInstr.class, 1, false),
	LREM (0x71, ALUbinopInstr.class, 1, true),
	FREM (0x72, ALUbinopInstr.class, 1, false),
	DREM (0x73, ALUbinopInstr.class, 1, true),
	INEG (0x74, ALUunopInstr.class, 1, false),
	LNEG (0x75, ALUunopInstr.class, 1, true),
	FNEG (0x76, ALUunopInstr.class, 1, false),
	DNEG (0x77, ALUunopInstr.class, 1, true),
	ISHL (0x78, ALUbinopInstr.class, 1, false),
	LSHL (0x79, ALUbinopInstr.class, 1, false),
	ISHR (0x7a, ALUbinopInstr.class, 1, false),
	LSHR (0x7b, ALUbinopInstr.class, 1, false),
	IUSHR (0x7c, ALUbinopInstr.class, 1, false),
	LUSHR (0x7d, ALUbinopInstr.class, 1, false),
	IAND (0x7e, ALUbinopInstr.class, 1, false),
	LAND (0x7f, ALUbinopInstr.class, 1, true),
	IOR (0x80, ALUbinopInstr.class, 1, false),
	LOR (0x81, ALUbinopInstr.class, 1, true),
	IXOR (0x82, ALUbinopInstr.class, 1, false),
	LXOR (0x83, ALUbinopInstr.class, 1, true),
	IINC (0x84, IINCInstr.class, 3, false){	public Number getByteCodeParameter(SynthData data) { return new Integer(data.code(data.pos()+1));}},
	I2L (0x85, ALUunopInstr.class, 1, true),
	I2F (0x86, ALUunopInstr.class, 1, false),
	I2D (0x87, ALUunopInstr.class, 1, true),
	L2I (0x88, ALUunopInstr.class, 1, false),
	L2F (0x89, ALUunopInstr.class, 1, false),
	L2D (0x8a, ALUunopInstr.class, 1, false),
	F2I (0x8b, ALUunopInstr.class, 1, false),
	F2L (0x8c, ALUunopInstr.class, 1, true),
	F2D (0x8d, ALUunopInstr.class, 1, true),
	D2I (0x8e, ALUunopInstr.class, 1, false),
	D2L (0x8f, ALUunopInstr.class, 1, true),
	D2F (0x90, ALUunopInstr.class, 1, false),
	I2B (0x91, ALUunopInstr.class, 1, false),
	I2C (0x92, ALUunopInstr.class, 1, false),
	I2S (0x93, ALUunopInstr.class, 1, false),
	LCMP (0x94, ALUbinopInstr.class, 1, true),
	FCMPL (0x95, ALUbinopInstr.class, 1, false),
	FCMPG (0x96, ALUbinopInstr.class, 1, false),
	DCMPL (0x97, ALUbinopInstr.class, 1, true),
	DCMPG (0x98, ALUbinopInstr.class, 1, true),
	IFEQ (0x99, IFInstr.class, 3, false) { public Number getByteCodeParameter(SynthData data) { return 0; }},
	IFNE (0x9a, IFInstr.class, 3, false) { public Number getByteCodeParameter(SynthData data) { return 0; }},
	IFLT (0x9b, IFInstr.class, 3, false) { public Number getByteCodeParameter(SynthData data) { return 0; }},
	IFGE (0x9c, IFInstr.class, 3, false) { public Number getByteCodeParameter(SynthData data) { return 0; }},
	IFGT (0x9d, IFInstr.class, 3, false) { public Number getByteCodeParameter(SynthData data) { return 0; }},
	IFLE (0x9e, IFInstr.class, 3, false) { public Number getByteCodeParameter(SynthData data) { return 0; }},
	IF_ICMPEQ (0x9f, IF_ICMPInstr.class, 3, false),
	IF_ICMPNE (0xa0, IF_ICMPInstr.class, 3, false),
	IF_ICMPLT (0xa1, IF_ICMPInstr.class, 3, false),
	IF_ICMPGE (0xa2, IF_ICMPInstr.class, 3, false),
	IF_ICMPGT (0xa3, IF_ICMPInstr.class, 3, false),
	IF_ICMPLE (0xa4, IF_ICMPInstr.class, 3, false),
	IF_ACMPEQ (0xa5, IF_ICMPInstr.class, 3, false),
	IF_ACMPNE (0xa6, IF_ICMPInstr.class, 3, false),
	GOTO (0xa7, GOTOInstr.class, 3, false),
	JSR (0xa8, InvalidInstr.class, 1, false),
	RET (0xa9, InvalidInstr.class, 1, false),
	TABLESWITCH (0xaa, InvalidInstr.class, 1, false),
	LOOKUPSWITCH (0xab, InvalidInstr.class, 1, false),
	IRETURN (0xac, ReturnInstr.class, 1, false),
	LRETURN (0xad, ReturnInstr.class, 1, false),
	FRETURN (0xae, ReturnInstr.class, 1, false),
	DRETURN (0xaf, ReturnInstr.class, 1, false),
	ARETURN (0xb0, ReturnInstr.class, 1, false),
	RETURN (0xb1, ReturnInstr.class, 1, false),
	GETSTATIC (0xb2, InvalidInstr.class, 1, false),
	PUTSTATIC (0xb3, InvalidInstr.class, 1, false),
	GETFIELD (0xb4, InvalidInstr.class, 3, false),
	PUTFIELD (0xb5, InvalidInstr.class, 1, false),
	INVOKEVIRTUAL (0xb6, InvalidInstr.class, 1, false),
	INVOKESPECIAL (0xb7, InvalidInstr.class, 1, false),
	INVOKESTATIC (0xb8, InvalidInstr.class, 1, false),
	INVOKEINTERFACE (0xb9, InvalidInstr.class, 1, false),

	NEW (0xbb, InvalidInstr.class, 1, false),
	NEWARRAY (0xbc, InvalidInstr.class, 1, false),
	ANEWARRAY (0xbd, InvalidInstr.class, 1, false),
	ARRAYLENGTH (0xbe, ArrayLengthInstr.class, 1, false),
	ATHROW (0xbf, InvalidInstr.class, 1, false),
	CHECKCAST (0xc0, InvalidInstr.class, 1, false),
	INSTANCEOF (0xc1, InvalidInstr.class, 1, false),
	MONITORENTER (0xc2, InvalidInstr.class, 1, false),
	MONITOREXIT (0xc3, InvalidInstr.class, 1, false),
	WIDE (0xc4, InvalidInstr.class, 1, false),
	MULTIANEWARRAY (0xc5, InvalidInstr.class, 1, false),
	IFNULL (0xc6, InvalidInstr.class, 1, false),
	IFNONNULL (0xc7, InvalidInstr.class, 1, false),
	GOTO_W (0xc8, InvalidInstr.class, 1, false),
	JSR_W (0xc9, InvalidInstr.class, 1, false),
	// now Breakpoint
	//LDC_QUICK (0xCA, ConstLdInstr.class, 2, false) { public Number getByteCodeParameter(SynthData data) { return new Integer(data.code(data.pos()+1)); } },
	LDC_W_QUICK (0xCC, ConstLdInstr.class, 3, false) { public Number getByteCodeParameter(SynthData data) { return new Integer(data.code_w(data.pos()+1)); } },
	LDC2_W_QUICK (0xCD, ConstLdInstr.class, 3, true) { public Number getByteCodeParameter(SynthData data) { return new Long(data.code_w(data.pos()+1)); } },
	GETFIELD_QUICK (0xCE, OVLdInstr.class, 3, false) { public Number getByteCodeParameter(SynthData data) { return data.code_w(data.pos()+1); }},
	GETFIELD_QUICK_ARRAY (0xE6, OVLdInstr.class, 3, false, true) { public Number getByteCodeParameter(SynthData data) {return data.code_w(data.pos()+1); }},//TODO
	PUTFIELD_QUICK (0xCF, OVStInstr.class, 3, false) { public Number getByteCodeParameter(SynthData data) { return data.code_w(data.pos()+1); }},
	PUTFIELD_QUICK_ARRAY(0xED, InvalidInstr.class, 3, false),//TODO
	GETFIELD2_QUICK (0xD0, OVLdInstr.class, 3, true) { public Number getByteCodeParameter(SynthData data) { return data.code_w(data.pos()+1); }},
	PUTFIELD2_QUICK (0xD1, OVStInstr.class, 3, true) { public Number getByteCodeParameter(SynthData data) { return data.code_w(data.pos()+1); }},
	GETSTATIC_QUICK (0xD2, SVLdInstr.class, 3, false) { public Number getByteCodeParameter(SynthData data) { return data.code_w(data.pos()+1); }},
	PUTSTATIC_QUICK (0xD3, SVStInstr.class, 3, false) { public Number getByteCodeParameter(SynthData data) { return data.code_w(data.pos()+1); }},
	GETSTATIC2_QUICK (0xD4, SVLdInstr.class, 3, true) { public Number getByteCodeParameter(SynthData data) { return data.code_w(data.pos()+1); }},
	PUTSTATIC2_QUICK (0xD5, SVStInstr.class, 3, true) { public Number getByteCodeParameter(SynthData data) { return data.code_w(data.pos()+1); }},
	INVOKEVIRTUAL_QUICK (0xD6, InvokeVirtualInstr.class, 3, false){ public Number getByteCodeParameter(SynthData data) { return data.code_w(data.pos()+1); } },
	INVOKENONVIRTUAL_QUICK (0xD7, InvokeNonVirtualInstr.class, 3, false){ public Number getByteCodeParameter(SynthData data) { return data.code_w(data.pos()+1); } },
	INVOKESTATIC_QUICK (0xD9, InvokeStaticInstr.class, 3, false){public Number getByteCodeParameter(SynthData data){ return data.code_w(data.pos()+1); } },
	INVOKEINTERFACE_QUICK (0xDA, InvokeNonVirtualInstr.class, 5, false){ public Number getByteCodeParameter(SynthData data) { return data.code_w(data.pos()+1); } },
	NEW_QUICK (0xDD, InvalidInstr.class, 1, false),
	NEWARRAY_32BIT (0xDB, InvalidInstr.class, 1, false),
	NEWARRAY_64BIT (0xDC, InvalidInstr.class, 1, false),
	ANEWARRAY_QUICK_PRIMITIVETYPE (0xDD, InvalidInstr.class, 1, false),
	ANEWARRAY_QUICK_REFERENCETYPE (0xDE, InvalidInstr.class, 1, false),
	MULTIANEWARRAY_QUICK_PRIMITIVETYPE (0xDF, InvalidInstr.class, 1, false),
	MULTIANEWARRAY_QUICK_REFERENCETYPE (0xE0, InvalidInstr.class, 1, false),
	CHECKCAST_QUICK (0xE1, InvalidInstr.class, 1, false),
	INSTANCEOF_QUICK (0xE2, InvalidInstr.class, 1, false),

	GETSTATIC_A_QUICK (0xE7, SVLdInstr.class, 3, false) { public Number getByteCodeParameter(SynthData data) { return data.code_w(data.pos()+1); } },
	PUTSTATIC_A_QUICK (0xE4, InvalidInstr.class, 3, false),

	SYNTHESIZED (0xFE, InvalidInstr.class, 1, false),
	STOP (0xFF, InvalidInstr.class, 1, false),

	/* thread scheduling */
	SWITCHTHREAD (0xFD, InvalidInstr.class, 1, false),
	SWITCHCLEANTHREAD (0xFC, InvalidInstr.class, 1, false),
	INVOKE_NATIVE (0xFB, InvalidInstr.class, 1, false),
	INVOKEVIRTUAL_QUICK_SYNC (0xFA, InvalidInstr.class, 1, false),
	INVOKENONVIRTUAL_QUICK_SYNC (0xF9, InvalidInstr.class, 1, false),
	INVOKESTATIC_QUICK_SYNC (0xF8, InvalidInstr.class, 1, false),
	TSTORE_0 (0xF7, InvalidInstr.class, 1, false),
	RETURN_SYNC (0xF6, InvalidInstr.class, 1, false),
	RETURN_32BIT_SYNC (0xF5, InvalidInstr.class, 1, false),
	RETURN_64BIT_SYNC (0xF4, InvalidInstr.class, 1, false),

	/* synthesis */
	SYNTH_START (-1, StartInstr.class, 0, false) { public Number getByteCodeParameter(SynthData data) { return -1; } },
	SYNTH_STOP (-2, StopInstr.class, 1, false),
	SYNTH_ILLEGAL (-3, InvalidInstr.class, 1, false),
	SYNTH_DUMMY (-4, DummyInstr.class, 1, false),
	SYNTH_PHI (-5, PHIInstr.class, 1, false),
	
	/* virtual invokation helper*/
	
	CI_CMP (-6, IF_ICMPInstr.class, 1, false),
	
	/* intrinsics */
	
	FSIN (-6, ALUunopInstr.class, 3, false),
	FCOS (-6, ALUunopInstr.class, 3, false);
//	INTR_SIN (-6, InvokeStaticInstr.class, 3, true){
//		public Number eval(SynthData data){
//			return data.code_w(data.pos()+1);
//		}
//		public void calculate(CPort ops[], CPort out) throws Pagefault{
//			
//			if(CGRAIntrinsics.nrOfOperands(getParam())==1){
//				out.set(CGRAIntrinsics.calculateUn(getParam(), ops[0].value()));
//			}
//			else if(CGRAIntrinsics.nrOfOperands(getParam())==2){
//				out.set(CGRAIntrinsics.calculateBin(getParam(), ops[0].value(), ops[1].value()));
//			}
//		}
//	},
//	INTR_COS (-6, InvokeStaticInstr.class, 3, true){
//		public Number eval(SynthData data){
//			return data.code_w(data.pos()+1);
//		}
//		public void calculate(CPort ops[], CPort out) throws Pagefault{
//			
//			if(CGRAIntrinsics.nrOfOperands(getParam())==1){
//				out.set(CGRAIntrinsics.calculateUn(getParam(), ops[0].value()));
//			}
//			else if(CGRAIntrinsics.nrOfOperands(getParam())==2){
//				out.set(CGRAIntrinsics.calculateBin(getParam(), ops[0].value(), ops[1].value()));
//			}
//		}
//	},
//	INTR_AVRG (-7, InvokeStaticInstr.class, 3, false){
//		public Number eval(SynthData data){
//			return data.code_w(data.pos()+1);
//		}
//		public void calculate(CPort ops[], CPort out) throws Pagefault{
//			
//			if(CGRAIntrinsics.nrOfOperands(getParam())==1)
//				out.set(CGRAIntrinsics.calculateUn(getParam(), ops[0].value()));
//			else if(CGRAIntrinsics.nrOfOperands(getParam())==2){
//				out.set(CGRAIntrinsics.calculateBin(getParam(), ops[0].value(), ops[1].value()));
//			}
//		}
//		
//	},
//	INTR_AVRG64 (-8, InvokeStaticInstr.class, 3, true){
//		public Number eval(SynthData data){
//			return data.code_w(data.pos()+1);
//		}
//		public void calculate(CPort ops[], CPort out) throws Pagefault{
//			
//			if(CGRAIntrinsics.nrOfOperands(getParam())==1)
//				out.set(CGRAIntrinsics.calculateUn(getParam(), ops[0].value()));
//			else if(CGRAIntrinsics.nrOfOperands(getParam())==2){
//				out.set(CGRAIntrinsics.calculateBin(getParam(), ops[0].value(), ops[1].value()));
//			}
//		}
//	},
//	INTR_COLOR_Y (-9, InvokeStaticInstr.class, 3, false){
//		public Number eval(SynthData data){
//			return data.code_w(data.pos()+1);
//		}
//		public void calculate(CPort ops[], CPort out) throws Pagefault{
//				out.set(CGRAIntrinsics.calculateTri(getParam(), ops[0].value(), ops[1].value(), ops[2].value()));
//		}
//	},
//	INTR_COLOR_CR (-10, InvokeStaticInstr.class, 3, false){
//		public Number eval(SynthData data){
//			return data.code_w(data.pos()+1);
//		}
//		public void calculate(CPort ops[], CPort out) throws Pagefault{
//			out.set(CGRAIntrinsics.calculateTri(getParam(), ops[0].value(), ops[1].value(), ops[2].value()));
//		}
//	},
//	INTR_COLOR_CB (-11, InvokeStaticInstr.class, 3, false){
//		public Number eval(SynthData data){
//			return data.code_w(data.pos()+1);
//		}
//		public void calculate(CPort ops[], CPort out) throws Pagefault{
//			out.set(CGRAIntrinsics.calculateTri(getParam(), ops[0].value(), ops[1].value(), ops[2].value()));
//		}
//	},
//	INTR_MPEG_BUT1 (-10, InvokeStaticInstr.class, 3, false){
//		public Number eval(SynthData data){
//			return data.code_w(data.pos()+1);
//		}
//		public void calculate(CPort ops[], CPort out) throws Pagefault{
//			out.set(CGRAIntrinsics.calculateTri(getParam(), ops[0].value(), ops[1].value(), ops[2].value()));
//		}
//	},
//	INTR_MPEG_BUT2 (-11, InvokeStaticInstr.class, 3, false){
//		public Number eval(SynthData data){
//			return data.code_w(data.pos()+1);
//		}
//		public void calculate(CPort ops[], CPort out) throws Pagefault{
//			out.set(CGRAIntrinsics.calculateTri(getParam(), ops[0].value(), ops[1].value(), ops[2].value()));
//		}
//	},
//	INTR_MPEG_CHL (-11, InvokeStaticInstr.class, 3, false){
//		public Number eval(SynthData data){
//			return data.code_w(data.pos()+1);
//		}
//		public void calculate(CPort ops[], CPort out) throws Pagefault{
//			out.set(CGRAIntrinsics.calculateBin(getParam(), ops[0].value(), ops[1].value()));
//		}
//	},
//	INTR_MPEG_CHR (-11, InvokeStaticInstr.class, 3, false){
//		public Number eval(SynthData data){
//			return data.code_w(data.pos()+1);
//		}
//		public void calculate(CPort ops[], CPort out) throws Pagefault{
//			out.set(CGRAIntrinsics.calculateBin(getParam(), ops[0].value(), ops[1].value()));
//		}
//
//	};


	private static LinkedHashMap<Integer,I> instruction_set = null;
	private static LinkedHashSet<I> memops;
	private static LinkedHashSet<I> memwops;
	private final Integer opcode;
	private final Integer size;
	private final Class<Instruction> cl;
	private Instruction i;
	private boolean wdata;
	private boolean createsReference = false;
	public int param = -1;
	
	/**
	 * Constructs an instruction.
	 * @param opcode the numeric JVM opcode
	 * @param cl the instruction class type representing this instruction
	 * @param size denotes the number of bytes this instruction consumes
	 * @param wide_data signifies an instruction operating on 64-bit data
	 */
	@SuppressWarnings("unchecked")
	private I(Integer opcode, Class<? extends Instruction> cl, Integer size, boolean wide_data) {
		this.opcode = opcode;
		this.size = size;
		this.cl = (Class<Instruction>)cl;
		i = null;
		wdata = wide_data;
	}
	
	private I(Integer opcode, Class<? extends Instruction> cl, Integer size, boolean wide_data, boolean createsReference) {
		this(opcode, cl, size, wide_data);
		this.createsReference = createsReference;
	}
	
	
	public Class<Instruction> getCL(){
		return cl;
	}
	
	

	/**
	 * Initializes the instruction set, building the mapping of opcodes to
	 * instructions and filling the sets of memory operations.
	 */
	public static void init() {
		if (instruction_set != null)
			return;
		instruction_set = new LinkedHashMap<Integer, I>();
		for (I instr : I.values())
			instruction_set.put(instr.c(), instr);
		memwops = new LinkedHashSet<I>();
		memops = new LinkedHashSet<I>();
		memwops.add(I.PUTFIELD_QUICK);
		memwops.add(I.PUTSTATIC_A_QUICK);//MBBE
		memwops.add(I.PUTSTATIC_QUICK);
		memwops.add(I.PUTFIELD2_QUICK);
		memwops.add(I.IASTORE);
		memwops.add(I.LASTORE);
		memwops.add(I.FASTORE);
		memwops.add(I.DASTORE);
		memwops.add(I.BASTORE);
		memwops.add(I.CASTORE);
		memwops.add(I.SASTORE);
		memops.addAll(memwops);
		memops.add(I.GETFIELD_QUICK);
//		memops.add(I.GETFIELD_QUICK_ARRAY);
		memops.add(I.GETSTATIC_A_QUICK);//MBBE
		memops.add(I.GETSTATIC_QUICK);
		memops.add(I.GETFIELD2_QUICK);
		memops.add(I.IALOAD);
		memops.add(I.LALOAD);
		memops.add(I.FALOAD);
		memops.add(I.DALOAD);
		memops.add(I.BALOAD);
		memops.add(I.CALOAD);
		memops.add(I.SALOAD);
	}

	/**
	 * Returns a new instruction at the given address.
	 * @param data the synthesis context
	 * @param address the address to fetch the instruction code from
	 */
	public static Instruction get_new(SynthData data, Integer address) {
		if (instruction_set.get(data.code(address)&0xFF).equals(SYNTHESIZED)) {
			if (address.equals(data.start_addr()))
				throw new SequenceNotSynthesizeableException(data.code(address)&0xFF);

		}
		I currentInstruction = instruction_set.get(data.code(address)&0xFF);
		if( currentInstruction == I.INVOKENONVIRTUAL_QUICK){
			int functionID =  (data.code(address+1)<<8)|data.code(address+2);
			if(data.kownIntrinsic(functionID))
				currentInstruction = data.getIntrinsicInstruction(functionID);
		}
		Instruction erg = currentInstruction.create(address);
		return erg;
	}

	/**
	 * Returns the set of instruction accessing memory. This set is valid only after
	 * calling init().
	 */
	public static LinkedHashSet<I> memops() {
		return memops;
	}

	/**
	 * Returns the set of instructions writing to memory. This set is valid only after
	 * calling init().
	 */
	public static LinkedHashSet<I> memwops() {
		return memwops;
	}

	/**
	 * Creates a new instance of the instruction class representing
	 * this instruction at the address <code>pos</code>.
	 * This method throws an <code>IllegalArgumentException</code> iff the
	 * instance to-be-created itself throws an <code>IllegalArgumentException</code>.
	 * This is due to the fact that an instruction that is not implemented yet is
	 * of type InvalidInstr which throws an exception upon instantiation.
	 *
	 * @param pos the address this instruction is located in the program.
	 * @see javasim.synth.model.instruction.InvalidInstr
	 */
	public Instruction create(Integer pos) throws IllegalArgumentException {
		try {
			Constructor<Instruction> cc = cl.getConstructor(I.class, Integer.class);
				i = cc.newInstance(this, pos);
			return i;
		}
		catch (NoSuchMethodException e) {}
		catch (InstantiationException e) {}
		catch (IllegalAccessException e) {}
		catch (InvocationTargetException e) {
			if (e.getCause() instanceof SequenceNotSynthesizeableException)
				throw (SequenceNotSynthesizeableException)e.getCause();
		}
		return null;
	}

	/**
	 * Returns the JVM opcode of this instruction.
	 */
	public Integer c() {
		return opcode;
	}

	/**
	 * Returns the instruction size.
	 */
	public Integer size() {
		return size;
	}

	/**
	 * Returns TRUE if this instruction operates on 64-bit data.
	 */
	public boolean wdata() {
		return wdata;
	}
	
	/**
	 * Modifies the synthesis context according to the instruction.
	 * @param data holds the synthesis context as a SynthData object
	 */
	public Number getByteCodeParameter(SynthData data) {
		throw new IllegalArgumentException("" + toString() + ": evaluation function not implemented");
	}

	/**
	 * Performs this instruction's operation on its operands.
	 * @param ops a list of operands
	 */
	
	
	public boolean createsReference(){
		return createsReference;
	}
}
