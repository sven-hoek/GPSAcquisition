package functionalunit.tokenmachine;

import dataContainer.ByteCode;

import functionalunit.opcodes.*;

public class TokenMatrixGeneratedByAdla extends TokenMatrix {

	public void decodeByteCode(short bytecode){
		int extendedcode = bytecode;
		
switch(extendedcode){
	case ByteCode.NOP:
		//switch(count){
		count = -1;
		//}
		break;
	case ByteCode.ACONST_NULL:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.ACONST, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.ICONST_M1:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.ICONST_M1, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.ICONST_0:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.ICONST_0, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.ICONST_1:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.ICONST_1, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.ICONST_2:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.ICONST_2, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.ICONST_3:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.ICONST_3, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.ICONST_4:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.ICONST_4, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.ICONST_5:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.ICONST_5, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.LCONST_0:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.LCONST_0, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.LCONST_1:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.LCONST_1, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.FCONST_0:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.FCONST_0, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.FCONST_1:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.FCONST_1, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.FCONST_2:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.FCONST_2, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.DCONST_0:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.DCONST_0, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.DCONST_1:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.DCONST_1, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.BIPUSH:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH32, currentTag, null, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_1, currentTag, frameStack, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.SIPUSH:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH32, currentTag, null, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_1_2, currentTag, frameStack, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.ILOAD:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.LOAD32, currentTag, null, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_1, currentTag, frameStack, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.LLOAD:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.LOAD64, currentTag, null, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_1, currentTag, frameStack, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.FLOAD:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.LOAD32, currentTag, null, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_1, currentTag, frameStack, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.DLOAD:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.LOAD64, currentTag, null, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_1, currentTag, frameStack, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.ALOAD:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.LOAD32, currentTag, null, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_1, currentTag, frameStack, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.ILOAD_0:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.LOAD32_0, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.ILOAD_1:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.LOAD32_1, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.ILOAD_2:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.LOAD32_2, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.ILOAD_3:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.LOAD32_3, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.LLOAD_0:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.LOAD64_0, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.LLOAD_1:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.LOAD64_1, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.LLOAD_2:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.LOAD64_2, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.LLOAD_3:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.LOAD64_3, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.FLOAD_0:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.LOAD32_0, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.FLOAD_1:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.LOAD32_1, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.FLOAD_2:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.LOAD32_2, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.FLOAD_3:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.LOAD32_3, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.DLOAD_0:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.LOAD64_0, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.DLOAD_1:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.LOAD64_1, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.DLOAD_2:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.LOAD64_2, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.DLOAD_3:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.LOAD64_3, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.ALOAD_0:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.LOAD32_0, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.ALOAD_1:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.LOAD32_1, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.ALOAD_2:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.LOAD32_2, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.ALOAD_3:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.LOAD32_3, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.IALOAD:
		switch(count){
			case 0:
				if(heap.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				heap.addToken(HeapOpcodes.HO_READ_ARRAY, currentTag, frameStack, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, heap, PORT_B, NO_TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, heap, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 2:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH32, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.LALOAD:
		switch(count){
			case 0:
				if(heap.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				heap.addToken(HeapOpcodes.HO_READ_ARRAY_64, currentTag, frameStack, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, heap, PORT_B, NO_TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, heap, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 2:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH64, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.FALOAD:
		switch(count){
			case 0:
				if(heap.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				heap.addToken(HeapOpcodes.HO_READ_ARRAY, currentTag, frameStack, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, heap, PORT_B, NO_TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, heap, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 2:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH32, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.DALOAD:
		switch(count){
			case 0:
				if(heap.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				heap.addToken(HeapOpcodes.HO_READ_ARRAY_64, currentTag, frameStack, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, heap, PORT_B, NO_TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, heap, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 2:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH64, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.AALOAD:
		switch(count){
			case 0:
				if(heap.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				heap.addToken(HeapOpcodes.HO_READ_ARRAY, currentTag, frameStack, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, heap, PORT_B, NO_TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, heap, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 2:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSHREF, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.BALOAD:
		switch(count){
			case 0:
				if(heap.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				heap.addToken(HeapOpcodes.HO_READ_ARRAY, currentTag, frameStack, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, heap, PORT_B, NO_TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, heap, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 2:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH32, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.CALOAD:
		switch(count){
			case 0:
				if(heap.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				heap.addToken(HeapOpcodes.HO_READ_ARRAY, currentTag, frameStack, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, heap, PORT_B, NO_TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, heap, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 2:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH32, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.SALOAD:
		switch(count){
			case 0:
				if(heap.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				heap.addToken(HeapOpcodes.HO_READ_ARRAY, currentTag, frameStack, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, heap, PORT_B, NO_TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, heap, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 2:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH32, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.ISTORE:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.STORE32, currentTag, null, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_1, currentTag, frameStack, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.LSTORE:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.STORE64, currentTag, null, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_1, currentTag, frameStack, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.FSTORE:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.STORE32, currentTag, null, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_1, currentTag, frameStack, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.DSTORE:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.STORE64, currentTag, null, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_1, currentTag, frameStack, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.ASTORE:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.STORE32, currentTag, null, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_1, currentTag, frameStack, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.ISTORE_0:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.STORE32_0, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.ISTORE_1:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.STORE32_1, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.ISTORE_2:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.STORE32_2, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.ISTORE_3:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.STORE32_3, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.LSTORE_0:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.STORE64_0, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.LSTORE_1:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.STORE64_1, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.LSTORE_2:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.STORE64_2, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.LSTORE_3:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.STORE64_3, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.FSTORE_0:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.STORE32_0, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.FSTORE_1:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.STORE32_1, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.FSTORE_2:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.STORE32_2, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.FSTORE_3:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.STORE32_3, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.DSTORE_0:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.STORE64_0, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.DSTORE_1:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.STORE64_1, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.DSTORE_2:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.STORE64_2, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.DSTORE_3:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.STORE64_3, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.ASTORE_0:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.STORE32_0, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.ASTORE_1:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.STORE32_1, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.ASTORE_2:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.STORE32_2, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.ASTORE_3:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.STORE32_3, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.IASTORE:
		switch(count){
			case 0:
				if(heap.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				heap.addToken(HeapOpcodes.HO_WRITE_ARRAY, currentTag, null, PORT_A, NO_TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, heap, PORT_C, NO_TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, heap, PORT_B, NO_TAG_INC);
				count++;
				break;
			case 2:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, heap, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.LASTORE:
		switch(count){
			case 0:
				if(heap.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				heap.addToken(HeapOpcodes.HO_WRITE_ARRAY_64, currentTag, null, PORT_A, NO_TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP64, currentTag, heap, PORT_C, NO_TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, heap, PORT_B, NO_TAG_INC);
				count++;
				break;
			case 2:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, heap, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.FASTORE:
		switch(count){
			case 0:
				if(heap.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				heap.addToken(HeapOpcodes.HO_WRITE_ARRAY, currentTag, null, PORT_A, NO_TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, heap, PORT_C, NO_TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, heap, PORT_B, NO_TAG_INC);
				count++;
				break;
			case 2:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, heap, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.DASTORE:
		switch(count){
			case 0:
				if(heap.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				heap.addToken(HeapOpcodes.HO_WRITE_ARRAY_64, currentTag, null, PORT_A, NO_TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP64, currentTag, heap, PORT_C, NO_TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, heap, PORT_B, NO_TAG_INC);
				count++;
				break;
			case 2:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, heap, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.AASTORE:
		switch(count){
			case 0:
				if(heap.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				heap.addToken(HeapOpcodes.HO_WRITE_ARRAY, currentTag, null, PORT_A, NO_TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, heap, PORT_C, NO_TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, heap, PORT_B, NO_TAG_INC);
				count++;
				break;
			case 2:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, heap, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.BASTORE:
		switch(count){
			case 0:
				if(heap.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				heap.addToken(HeapOpcodes.HO_WRITE_ARRAY, currentTag, null, PORT_A, NO_TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, heap, PORT_C, NO_TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, heap, PORT_B, NO_TAG_INC);
				count++;
				break;
			case 2:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, heap, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.CASTORE:
		switch(count){
			case 0:
				if(heap.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				heap.addToken(HeapOpcodes.HO_WRITE_ARRAY, currentTag, null, PORT_A, NO_TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, heap, PORT_C, NO_TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, heap, PORT_B, NO_TAG_INC);
				count++;
				break;
			case 2:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, heap, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.SASTORE:
		switch(count){
			case 0:
				if(heap.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				heap.addToken(HeapOpcodes.HO_WRITE_ARRAY, currentTag, null, PORT_A, NO_TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, heap, PORT_C, NO_TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, heap, PORT_B, NO_TAG_INC);
				count++;
				break;
			case 2:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, heap, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.POP:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.REMOVE32, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.POP2:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.REMOVE64, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.DUP:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.DUP, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.DUP_X1:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.DUP_X1, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.DUP_X2:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.DUP_X2, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.DUP2:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.DUP2, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.DUP2_X1:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.DUP2_X1, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.DUP2_X2:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.DUP2_X2, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.SWAP:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.SWAP, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.IADD:
		switch(count){
			case 0:
				if(ialu.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				ialu.addToken(IaluOpcodes.IADD, currentTag, frameStack, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, ialu, PORT_B, NO_TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, ialu, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 2:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH32, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.LADD:
		switch(count){
			case 0:
				if(lalu.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				lalu.addToken(LaluOpcodes.LADD, currentTag, frameStack, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP64, currentTag, lalu, PORT_B, NO_TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP64, currentTag, lalu, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 2:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH64, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.FADD:
		switch(count){
			case 0:
				if(fpu.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				fpu.addToken(FpuOpcodes.FADD, currentTag, frameStack, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, fpu, PORT_B, NO_TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, fpu, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 2:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH32, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.DADD:
		switch(count){
			case 0:
				if(fpu.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				fpu.addToken(FpuOpcodes.DADD, currentTag, frameStack, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP64, currentTag, fpu, PORT_B, NO_TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP64, currentTag, fpu, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 2:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH64, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.ISUB:
		switch(count){
			case 0:
				if(ialu.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				ialu.addToken(IaluOpcodes.ISUB, currentTag, frameStack, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, ialu, PORT_B, NO_TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, ialu, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 2:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH32, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.LSUB:
		switch(count){
			case 0:
				if(lalu.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				lalu.addToken(LaluOpcodes.LSUB, currentTag, frameStack, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP64, currentTag, lalu, PORT_B, NO_TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP64, currentTag, lalu, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 2:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH64, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.FSUB:
		switch(count){
			case 0:
				if(fpu.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				fpu.addToken(FpuOpcodes.FSUB, currentTag, frameStack, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, fpu, PORT_B, NO_TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, fpu, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 2:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH32, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.DSUB:
		switch(count){
			case 0:
				if(fpu.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				fpu.addToken(FpuOpcodes.DSUB, currentTag, frameStack, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP64, currentTag, fpu, PORT_B, NO_TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP64, currentTag, fpu, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 2:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH64, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.IMUL:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull() || imul.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, imul, PORT_B, NO_TAG_INC);
				imul.addToken(ImulOpcodes.IMUL, currentTag, frameStack, PORT_A, TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, imul, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 2:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH32, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.LMUL:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull() || imul.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP64, currentTag, imul, PORT_B, NO_TAG_INC);
				imul.addToken(ImulOpcodes.LMUL, currentTag, frameStack, PORT_A, TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP64, currentTag, imul, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 2:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH64, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.FMUL:
		switch(count){
			case 0:
				if(fpu.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				fpu.addToken(FpuOpcodes.FMUL, currentTag, frameStack, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, fpu, PORT_B, NO_TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, fpu, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 2:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH32, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.DMUL:
		switch(count){
			case 0:
				if(fpu.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				fpu.addToken(FpuOpcodes.DMUL, currentTag, frameStack, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP64, currentTag, fpu, PORT_B, NO_TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP64, currentTag, fpu, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 2:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH64, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.IDIV:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull() || idiv.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, idiv, PORT_B, NO_TAG_INC);
				idiv.addToken(IdivOpcodes.IDIV, currentTag, frameStack, PORT_A, TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, idiv, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 2:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH32, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.LDIV:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull() || idiv.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP64, currentTag, idiv, PORT_B, NO_TAG_INC);
				idiv.addToken(IdivOpcodes.LDIV, currentTag, frameStack, PORT_A, TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP64, currentTag, idiv, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 2:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH64, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.FDIV:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull() || fdiv.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, fdiv, PORT_B, NO_TAG_INC);
				fdiv.addToken(FdivOpcodes.FDIV, currentTag, frameStack, PORT_A, TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, fdiv, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 2:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH32, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.DDIV:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull() || fdiv.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP64, currentTag, fdiv, PORT_B, NO_TAG_INC);
				fdiv.addToken(FdivOpcodes.DDIV, currentTag, frameStack, PORT_A, TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP64, currentTag, fdiv, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 2:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH64, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.IREM:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull() || idiv.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, idiv, PORT_B, NO_TAG_INC);
				idiv.addToken(IdivOpcodes.IREM, currentTag, frameStack, PORT_A, TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, idiv, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 2:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH32, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.LREM:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull() || idiv.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP64, currentTag, idiv, PORT_B, NO_TAG_INC);
				idiv.addToken(IdivOpcodes.LREM, currentTag, frameStack, PORT_A, TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP64, currentTag, idiv, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 2:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH64, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.FREM:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull() || fdiv.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.DUP2, currentTag, null, PORT_A, NO_TAG_INC);
				fdiv.addToken(FdivOpcodes.FDIV, currentTag, frameStack, PORT_A, TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, fdiv, PORT_B, NO_TAG_INC);
				count++;
				break;
			case 2:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, fdiv, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 3:
				if(fpu.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				fpu.addToken(FpuOpcodes.FTRUNC, currentTag, frameStack, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.PUSH32, currentTag, null, PORT_A, NO_TAG_INC);
				count++;
				break;
			case 4:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, fpu, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 5:
				if(fpu.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				fpu.addToken(FpuOpcodes.FMUL, currentTag, frameStack, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.PUSH32, currentTag, null, PORT_A, NO_TAG_INC);
				count++;
				break;
			case 6:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, fpu, PORT_B, NO_TAG_INC);
				count++;
				break;
			case 7:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, fpu, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 8:
				if(fpu.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				fpu.addToken(FpuOpcodes.FSUB, currentTag, frameStack, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.PUSH32, currentTag, null, PORT_A, NO_TAG_INC);
				count++;
				break;
			case 9:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, fpu, PORT_B, NO_TAG_INC);
				count++;
				break;
			case 10:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, fpu, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 11:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH32, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.DREM:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull() || fdiv.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.DUP2_X2, currentTag, null, PORT_A, NO_TAG_INC);
				fdiv.addToken(FdivOpcodes.DDIV, currentTag, frameStack, PORT_A, TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP64, currentTag, fdiv, PORT_B, NO_TAG_INC);
				count++;
				break;
			case 2:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.DUP2_X2, currentTag, null, PORT_A, NO_TAG_INC);
				count++;
				break;
			case 3:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP64, currentTag, fdiv, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 4:
				if(fpu.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				fpu.addToken(FpuOpcodes.DTRUNC, currentTag, frameStack, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.PUSH64, currentTag, null, PORT_A, NO_TAG_INC);
				count++;
				break;
			case 5:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP64, currentTag, fpu, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 6:
				if(fpu.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				fpu.addToken(FpuOpcodes.DMUL, currentTag, frameStack, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.PUSH64, currentTag, null, PORT_A, NO_TAG_INC);
				count++;
				break;
			case 7:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP64, currentTag, fpu, PORT_B, NO_TAG_INC);
				count++;
				break;
			case 8:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP64, currentTag, fpu, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 9:
				if(fpu.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				fpu.addToken(FpuOpcodes.DSUB, currentTag, frameStack, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.PUSH64, currentTag, null, PORT_A, NO_TAG_INC);
				count++;
				break;
			case 10:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP64, currentTag, fpu, PORT_B, NO_TAG_INC);
				count++;
				break;
			case 11:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP64, currentTag, fpu, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 12:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH64, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.INEG:
		switch(count){
			case 0:
				if(ialu.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				ialu.addToken(IaluOpcodes.INEG, currentTag, frameStack, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, ialu, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH32, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.LNEG:
		switch(count){
			case 0:
				if(lalu.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				lalu.addToken(LaluOpcodes.LNEG, currentTag, frameStack, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP64, currentTag, lalu, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH64, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.FNEG:
		switch(count){
			case 0:
				if(fpu.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				fpu.addToken(FpuOpcodes.FNEG, currentTag, frameStack, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, fpu, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH32, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.DNEG:
		switch(count){
			case 0:
				if(fpu.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				fpu.addToken(FpuOpcodes.DNEG, currentTag, frameStack, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP64, currentTag, fpu, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH64, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.ISHL:
		switch(count){
			case 0:
				if(ialu.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				ialu.addToken(IaluOpcodes.ISHL, currentTag, frameStack, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, ialu, PORT_B, NO_TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, ialu, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 2:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH32, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.LSHL:
		switch(count){
			case 0:
				if(lalu.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				lalu.addToken(LaluOpcodes.LSHL, currentTag, frameStack, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, lalu, PORT_B, NO_TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP64, currentTag, lalu, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 2:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH64, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.ISHR:
		switch(count){
			case 0:
				if(ialu.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				ialu.addToken(IaluOpcodes.ISHR, currentTag, frameStack, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, ialu, PORT_B, NO_TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, ialu, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 2:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH32, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.LSHR:
		switch(count){
			case 0:
				if(lalu.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				lalu.addToken(LaluOpcodes.LSHR, currentTag, frameStack, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, lalu, PORT_B, NO_TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP64, currentTag, lalu, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 2:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH64, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.IUSHR:
		switch(count){
			case 0:
				if(ialu.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				ialu.addToken(IaluOpcodes.IUSHR, currentTag, frameStack, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, ialu, PORT_B, NO_TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, ialu, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 2:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH32, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.LUSHR:
		switch(count){
			case 0:
				if(lalu.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				lalu.addToken(LaluOpcodes.LUSHR, currentTag, frameStack, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, lalu, PORT_B, NO_TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP64, currentTag, lalu, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 2:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH64, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.IAND:
		switch(count){
			case 0:
				if(ialu.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				ialu.addToken(IaluOpcodes.IAND, currentTag, frameStack, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, ialu, PORT_B, NO_TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, ialu, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 2:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH32, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.LAND:
		switch(count){
			case 0:
				if(lalu.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				lalu.addToken(LaluOpcodes.LAND, currentTag, frameStack, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP64, currentTag, lalu, PORT_B, NO_TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP64, currentTag, lalu, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 2:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH64, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.IOR:
		switch(count){
			case 0:
				if(ialu.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				ialu.addToken(IaluOpcodes.IOR, currentTag, frameStack, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, ialu, PORT_B, NO_TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, ialu, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 2:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH32, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.LOR:
		switch(count){
			case 0:
				if(lalu.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				lalu.addToken(LaluOpcodes.LOR, currentTag, frameStack, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP64, currentTag, lalu, PORT_B, NO_TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP64, currentTag, lalu, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 2:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH64, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.IXOR:
		switch(count){
			case 0:
				if(ialu.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				ialu.addToken(IaluOpcodes.IXOR, currentTag, frameStack, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, ialu, PORT_B, NO_TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, ialu, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 2:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH32, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.LXOR:
		switch(count){
			case 0:
				if(lalu.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				lalu.addToken(LaluOpcodes.LXOR, currentTag, frameStack, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP64, currentTag, lalu, PORT_B, NO_TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP64, currentTag, lalu, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 2:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH64, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.IINC:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.LOAD32, currentTag, null, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_1, currentTag, frameStack, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 1:
				if(ialu.tokenQueueFull() || frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				ialu.addToken(IaluOpcodes.IADD, currentTag, frameStack, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, ialu, PORT_B, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_2, currentTag, ialu, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 2:
				if(frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH32, currentTag, null, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_1, currentTag, frameStack, PORT_A, TAG_INC);
				currentTag++;
				count++;
				break;
			case 3:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.STORE32, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.I2L:
		switch(count){
			case 0:
				if(ialu.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				ialu.addToken(IaluOpcodes.I2L, currentTag, frameStack, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, ialu, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH64, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.I2F:
		switch(count){
			case 0:
				if(fpu.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				fpu.addToken(FpuOpcodes.I2F, currentTag, frameStack, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, fpu, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH32, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.I2D:
		switch(count){
			case 0:
				if(fpu.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				fpu.addToken(FpuOpcodes.I2D, currentTag, frameStack, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, fpu, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH64, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.L2I:
		switch(count){
			case 0:
				if(ialu.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				ialu.addToken(IaluOpcodes.L2I, currentTag, frameStack, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP64, currentTag, ialu, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH32, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.L2F:
		switch(count){
			case 0:
				if(fpu.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				fpu.addToken(FpuOpcodes.L2F, currentTag, frameStack, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP64, currentTag, fpu, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH32, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.L2D:
		switch(count){
			case 0:
				if(fpu.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				fpu.addToken(FpuOpcodes.L2D, currentTag, frameStack, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP64, currentTag, fpu, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH64, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.F2I:
		switch(count){
			case 0:
				if(fpu.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				fpu.addToken(FpuOpcodes.F2I, currentTag, frameStack, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, fpu, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH32, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.F2L:
		switch(count){
			case 0:
				if(fpu.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				fpu.addToken(FpuOpcodes.F2L, currentTag, frameStack, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, fpu, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH64, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.F2D:
		switch(count){
			case 0:
				if(fpu.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				fpu.addToken(FpuOpcodes.F2D, currentTag, frameStack, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, fpu, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH64, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.D2I:
		switch(count){
			case 0:
				if(fpu.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				fpu.addToken(FpuOpcodes.D2I, currentTag, frameStack, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP64, currentTag, fpu, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH32, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.D2L:
		switch(count){
			case 0:
				if(fpu.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				fpu.addToken(FpuOpcodes.D2L, currentTag, frameStack, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP64, currentTag, fpu, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH64, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.D2F:
		switch(count){
			case 0:
				if(fpu.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				fpu.addToken(FpuOpcodes.D2F, currentTag, frameStack, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP64, currentTag, fpu, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH32, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.I2B:
		switch(count){
			case 0:
				if(ialu.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				ialu.addToken(IaluOpcodes.I2B, currentTag, frameStack, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, ialu, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH32, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.I2C:
		switch(count){
			case 0:
				if(ialu.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				ialu.addToken(IaluOpcodes.I2C, currentTag, frameStack, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, ialu, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH32, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.I2S:
		switch(count){
			case 0:
				if(ialu.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				ialu.addToken(IaluOpcodes.I2S, currentTag, frameStack, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, ialu, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH32, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.LCMP:
		switch(count){
			case 0:
				if(lalu.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				lalu.addToken(LaluOpcodes.LCMP, currentTag, frameStack, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP64, currentTag, lalu, PORT_B, NO_TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP64, currentTag, lalu, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 2:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH32, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.FCMPL:
		switch(count){
			case 0:
				if(fpu.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				fpu.addToken(FpuOpcodes.FCMPL, currentTag, frameStack, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, fpu, PORT_B, NO_TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, fpu, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 2:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH32, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.FCMPG:
		switch(count){
			case 0:
				if(fpu.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				fpu.addToken(FpuOpcodes.FCMPG, currentTag, frameStack, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, fpu, PORT_B, NO_TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, fpu, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 2:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH32, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.DCMPL:
		switch(count){
			case 0:
				if(fpu.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				fpu.addToken(FpuOpcodes.DCMPL, currentTag, frameStack, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP64, currentTag, fpu, PORT_B, NO_TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP64, currentTag, fpu, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 2:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH32, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.DCMPG:
		switch(count){
			case 0:
				if(fpu.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				fpu.addToken(FpuOpcodes.DCMPG, currentTag, frameStack, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP64, currentTag, fpu, PORT_B, NO_TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP64, currentTag, fpu, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 2:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH32, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.IFEQ:
		switch(count){
			case 0:
				if(ialu.tokenQueueFull() || frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				ialu.addToken(IaluOpcodes.ICMP_ZERO, currentTag, tokenMachine, PORT_B, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, ialu, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_1_2, currentTag, tokenMachine, PORT_A, TAG_INC);
				currentTag++;
				count++;
				break;
			case 1:
				if(tokenMachine.tokenQueueFull()){
					break;
				}
				tokenMachine.addToken(TokenMachineOpcodes.BRANCH_IF_EQ, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.IFNE:
		switch(count){
			case 0:
				if(ialu.tokenQueueFull() || frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				ialu.addToken(IaluOpcodes.ICMP_ZERO, currentTag, tokenMachine, PORT_B, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, ialu, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_1_2, currentTag, tokenMachine, PORT_A, TAG_INC);
				currentTag++;
				count++;
				break;
			case 1:
				if(tokenMachine.tokenQueueFull()){
					break;
				}
				tokenMachine.addToken(TokenMachineOpcodes.BRANCH_IF_NE, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.IFLT:
		switch(count){
			case 0:
				if(ialu.tokenQueueFull() || frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				ialu.addToken(IaluOpcodes.ICMP_ZERO, currentTag, tokenMachine, PORT_B, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, ialu, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_1_2, currentTag, tokenMachine, PORT_A, TAG_INC);
				currentTag++;
				count++;
				break;
			case 1:
				if(tokenMachine.tokenQueueFull()){
					break;
				}
				tokenMachine.addToken(TokenMachineOpcodes.BRANCH_IF_LT, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.IFGE:
		switch(count){
			case 0:
				if(ialu.tokenQueueFull() || frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				ialu.addToken(IaluOpcodes.ICMP_ZERO, currentTag, tokenMachine, PORT_B, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, ialu, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_1_2, currentTag, tokenMachine, PORT_A, TAG_INC);
				currentTag++;
				count++;
				break;
			case 1:
				if(tokenMachine.tokenQueueFull()){
					break;
				}
				tokenMachine.addToken(TokenMachineOpcodes.BRANCH_IF_GE, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.IFGT:
		switch(count){
			case 0:
				if(ialu.tokenQueueFull() || frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				ialu.addToken(IaluOpcodes.ICMP_ZERO, currentTag, tokenMachine, PORT_B, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, ialu, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_1_2, currentTag, tokenMachine, PORT_A, TAG_INC);
				currentTag++;
				count++;
				break;
			case 1:
				if(tokenMachine.tokenQueueFull()){
					break;
				}
				tokenMachine.addToken(TokenMachineOpcodes.BRANCH_IF_GT, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.IFLE:
		switch(count){
			case 0:
				if(ialu.tokenQueueFull() || frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				ialu.addToken(IaluOpcodes.ICMP_ZERO, currentTag, tokenMachine, PORT_B, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, ialu, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_1_2, currentTag, tokenMachine, PORT_A, TAG_INC);
				currentTag++;
				count++;
				break;
			case 1:
				if(tokenMachine.tokenQueueFull()){
					break;
				}
				tokenMachine.addToken(TokenMachineOpcodes.BRANCH_IF_LE, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.IF_ICMPEQ:
		switch(count){
			case 0:
				if(ialu.tokenQueueFull() || frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				ialu.addToken(IaluOpcodes.ICMP, currentTag, tokenMachine, PORT_B, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, ialu, PORT_B, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_1_2, currentTag, tokenMachine, PORT_A, TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, ialu, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 2:
				if(tokenMachine.tokenQueueFull()){
					break;
				}
				tokenMachine.addToken(TokenMachineOpcodes.BRANCH_IF_EQ, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.IF_ICMPNE:
		switch(count){
			case 0:
				if(ialu.tokenQueueFull() || frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				ialu.addToken(IaluOpcodes.ICMP, currentTag, tokenMachine, PORT_B, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, ialu, PORT_B, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_1_2, currentTag, tokenMachine, PORT_A, TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, ialu, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 2:
				if(tokenMachine.tokenQueueFull()){
					break;
				}
				tokenMachine.addToken(TokenMachineOpcodes.BRANCH_IF_NE, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.IF_ICMPLT:
		switch(count){
			case 0:
				if(ialu.tokenQueueFull() || frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				ialu.addToken(IaluOpcodes.ICMP, currentTag, tokenMachine, PORT_B, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, ialu, PORT_B, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_1_2, currentTag, tokenMachine, PORT_A, TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, ialu, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 2:
				if(tokenMachine.tokenQueueFull()){
					break;
				}
				tokenMachine.addToken(TokenMachineOpcodes.BRANCH_IF_LT, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.IF_ICMPGE:
		switch(count){
			case 0:
				if(ialu.tokenQueueFull() || frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				ialu.addToken(IaluOpcodes.ICMP, currentTag, tokenMachine, PORT_B, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, ialu, PORT_B, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_1_2, currentTag, tokenMachine, PORT_A, TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, ialu, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 2:
				if(tokenMachine.tokenQueueFull()){
					break;
				}
				tokenMachine.addToken(TokenMachineOpcodes.BRANCH_IF_GE, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.IF_ICMPGT:
		switch(count){
			case 0:
				if(ialu.tokenQueueFull() || frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				ialu.addToken(IaluOpcodes.ICMP, currentTag, tokenMachine, PORT_B, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, ialu, PORT_B, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_1_2, currentTag, tokenMachine, PORT_A, TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, ialu, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 2:
				if(tokenMachine.tokenQueueFull()){
					break;
				}
				tokenMachine.addToken(TokenMachineOpcodes.BRANCH_IF_GT, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.IF_ICMPLE:
		switch(count){
			case 0:
				if(ialu.tokenQueueFull() || frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				ialu.addToken(IaluOpcodes.ICMP, currentTag, tokenMachine, PORT_B, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, ialu, PORT_B, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_1_2, currentTag, tokenMachine, PORT_A, TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, ialu, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 2:
				if(tokenMachine.tokenQueueFull()){
					break;
				}
				tokenMachine.addToken(TokenMachineOpcodes.BRANCH_IF_LE, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.IF_ACMPEQ:
		switch(count){
			case 0:
				if(ialu.tokenQueueFull() || frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				ialu.addToken(IaluOpcodes.ICMP, currentTag, tokenMachine, PORT_B, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, ialu, PORT_B, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_1_2, currentTag, tokenMachine, PORT_A, TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, ialu, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 2:
				if(tokenMachine.tokenQueueFull()){
					break;
				}
				tokenMachine.addToken(TokenMachineOpcodes.BRANCH_IF_EQ, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.IF_ACMPNE:
		switch(count){
			case 0:
				if(ialu.tokenQueueFull() || frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				ialu.addToken(IaluOpcodes.ICMP, currentTag, tokenMachine, PORT_B, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, ialu, PORT_B, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_1_2, currentTag, tokenMachine, PORT_A, TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, ialu, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 2:
				if(tokenMachine.tokenQueueFull()){
					break;
				}
				tokenMachine.addToken(TokenMachineOpcodes.BRANCH_IF_NE, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.GOTO:
		switch(count){
			case 0:
				if(tokenMachine.tokenQueueFull()){
					break;
				}
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_1_2, currentTag, tokenMachine, PORT_A, TAG_INC);
				currentTag++;
				count++;
				break;
			case 1:
				if(tokenMachine.tokenQueueFull()){
					break;
				}
				tokenMachine.addToken(TokenMachineOpcodes.BRANCH, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.JSR:
		switch(count){
			case 0:
				if(tokenMachine.tokenQueueFull()){
					break;
				}
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_1_2, currentTag, tokenMachine, PORT_A, TAG_INC);
				currentTag++;
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH32, currentTag, null, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.JSR, currentTag, frameStack, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.RET:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.LOAD32, currentTag, null, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_1, currentTag, frameStack, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, tokenMachine, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.RET, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.IRETURN:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.RETURN32, currentTag, tokenMachine, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.RETURN, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.LRETURN:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.RETURN64, currentTag, tokenMachine, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.RETURN, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.FRETURN:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.RETURN32, currentTag, tokenMachine, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.RETURN, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.DRETURN:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.RETURN64, currentTag, tokenMachine, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.RETURN, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.ARETURN:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.RETURN32, currentTag, tokenMachine, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.RETURN, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.RETURN:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.RETURN, currentTag, tokenMachine, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.RETURN, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.REFL_INVOKEVIRTUAL:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, tokenMachine, PORT_A, TAG_INC);
				currentTag++;
				count++;
				break;
			case 1:
				if(heap.tokenQueueFull() || frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				heap.addToken(HeapOpcodes.GET_CTI, currentTag, tokenMachine, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.PEEK_1, currentTag, heap, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.LOAD_ARG, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 2:
				if(frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.INVOKE, currentTag, null, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.INVOKE, currentTag, frameStack, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.REFL_INVOKENONVIRTUAL:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, tokenMachine, PORT_A, TAG_INC);
				currentTag++;
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.INVOKE, currentTag, null, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.INVOKE_STATIC, currentTag, frameStack, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.REFL_NEW:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PEEK_1, currentTag, tokenMachine, PORT_A, TAG_INC);
				currentTag++;
				count++;
				break;
			case 1:
				if(tokenMachine.tokenQueueFull()){
					break;
				}
				tokenMachine.addToken(TokenMachineOpcodes.CLASSSIZE, currentTag, heap, PORT_B, TAG_INC);
				currentTag++;
				count++;
				break;
			case 2:
				if(heap.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				heap.addToken(HeapOpcodes.ALLOC_OBJ, currentTag, frameStack, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, heap, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 3:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSHREF, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.REFL_INSTANCEOF:
		switch(count){
			case 0:
				if(heap.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				heap.addToken(HeapOpcodes.GET_CTI, currentTag, tokenMachine, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, heap, PORT_A, NO_TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, tokenMachine, PORT_B, TAG_INC);
				currentTag++;
				count++;
				break;
			case 2:
				if(frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH32, currentTag, null, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.INSTANCEOF, currentTag, frameStack, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.ARRAYLENGTH:
		switch(count){
			case 0:
				if(heap.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				heap.addToken(HeapOpcodes.GET_SIZE, currentTag, frameStack, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, heap, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH32, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.ATHROW:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.DUP, currentTag, null, PORT_A, NO_TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, heap, PORT_A, TAG_INC);
				currentTag++;
				count++;
				break;
			case 2:
				if(heap.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				heap.addToken(HeapOpcodes.GET_CTI, currentTag, tokenMachine, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, tokenMachine, PORT_B, TAG_INC);
				currentTag++;
				count++;
				break;
			case 3:
				if(frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.CLEARFRAME, currentTag, null, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.THROW, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.ATHROW_INJECT:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.DUP, currentTag, null, PORT_A, NO_TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, heap, PORT_A, TAG_INC);
				currentTag++;
				count++;
				break;
			case 2:
				if(heap.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				heap.addToken(HeapOpcodes.GET_CTI, currentTag, tokenMachine, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, tokenMachine, PORT_B, TAG_INC);
				currentTag++;
				count++;
				break;
			case 3:
				if(frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.CLEARFRAME, currentTag, null, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.THROW_INJECT, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.HARDWARE_THROW:
		switch(count){
			case 0:
				if(tokenMachine.tokenQueueFull()){
					break;
				}
				tokenMachine.addToken(TokenMachineOpcodes.HARDWARE_EXCEPTION_INVOKE, currentTag, frameStack, PORT_A, TAG_INC);
				currentTag++;
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.INVOKE, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.IFNULL:
		switch(count){
			case 0:
				if(ialu.tokenQueueFull() || frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				ialu.addToken(IaluOpcodes.ICMP_ZERO, currentTag, tokenMachine, PORT_B, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, ialu, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_1_2, currentTag, tokenMachine, PORT_A, TAG_INC);
				currentTag++;
				count++;
				break;
			case 1:
				if(tokenMachine.tokenQueueFull()){
					break;
				}
				tokenMachine.addToken(TokenMachineOpcodes.BRANCH_IF_EQ, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.IFNONNULL:
		switch(count){
			case 0:
				if(ialu.tokenQueueFull() || frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				ialu.addToken(IaluOpcodes.ICMP_ZERO, currentTag, tokenMachine, PORT_B, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, ialu, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_1_2, currentTag, tokenMachine, PORT_A, TAG_INC);
				currentTag++;
				count++;
				break;
			case 1:
				if(tokenMachine.tokenQueueFull()){
					break;
				}
				tokenMachine.addToken(TokenMachineOpcodes.BRANCH_IF_NE, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.GOTO_W:
		switch(count){
			case 0:
				if(tokenMachine.tokenQueueFull()){
					break;
				}
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_1_2_3_4, currentTag, tokenMachine, PORT_A, TAG_INC);
				currentTag++;
				count++;
				break;
			case 1:
				if(tokenMachine.tokenQueueFull()){
					break;
				}
				tokenMachine.addToken(TokenMachineOpcodes.BRANCH, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.JSR_W:
		switch(count){
			case 0:
				if(tokenMachine.tokenQueueFull()){
					break;
				}
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_1_2_3_4, currentTag, tokenMachine, PORT_A, TAG_INC);
				currentTag++;
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH32, currentTag, null, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.JSR, currentTag, frameStack, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.LDC_W_QUICK:
		switch(count){
			case 0:
				if(tokenMachine.tokenQueueFull()){
					break;
				}
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_1_2, currentTag, tokenMachine, PORT_A, TAG_INC);
				currentTag++;
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH32, currentTag, null, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.LDC, currentTag, frameStack, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.LDC2_W_QUICK:
		switch(count){
			case 0:
				if(tokenMachine.tokenQueueFull()){
					break;
				}
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_1_2, currentTag, tokenMachine, PORT_A, TAG_INC);
				currentTag++;
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH64, currentTag, null, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.LDC2, currentTag, frameStack, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.GETFIELD_QUICK:
		switch(count){
			case 0:
				if(heap.tokenQueueFull() || frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				heap.addToken(HeapOpcodes.HO_READ, currentTag, frameStack, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, heap, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_1_2, currentTag, heap, PORT_B, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH32, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.PUTFIELD_QUICK:
		switch(count){
			case 0:
				if(heap.tokenQueueFull() || frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				heap.addToken(HeapOpcodes.HO_WRITE, currentTag, null, PORT_A, NO_TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, heap, PORT_C, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_1_2, currentTag, heap, PORT_B, NO_TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, heap, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.GETFIELD2_QUICK:
		switch(count){
			case 0:
				if(heap.tokenQueueFull() || frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				heap.addToken(HeapOpcodes.HO_READ_64, currentTag, frameStack, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, heap, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_1_2, currentTag, heap, PORT_B, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH64, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.PUTFIELD2_QUICK:
		switch(count){
			case 0:
				if(heap.tokenQueueFull() || frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				heap.addToken(HeapOpcodes.HO_WRITE_64, currentTag, null, PORT_A, NO_TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP64, currentTag, heap, PORT_C, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_1_2, currentTag, heap, PORT_B, NO_TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, heap, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.GETSTATIC_QUICK:
		switch(count){
			case 0:
				if(heap.tokenQueueFull() || frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				heap.addToken(HeapOpcodes.HO_READ, currentTag, frameStack, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.ICONST_1, currentTag, null, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_1_2, currentTag, heap, PORT_B, NO_TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, heap, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 2:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH32, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.PUTSTATIC_QUICK:
		switch(count){
			case 0:
				if(heap.tokenQueueFull() || frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				heap.addToken(HeapOpcodes.HO_WRITE, currentTag, null, PORT_A, NO_TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, heap, PORT_C, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_1_2, currentTag, heap, PORT_B, NO_TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.ICONST_1, currentTag, null, PORT_A, NO_TAG_INC);
				count++;
				break;
			case 2:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, heap, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.GETSTATIC2_QUICK:
		switch(count){
			case 0:
				if(heap.tokenQueueFull() || frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				heap.addToken(HeapOpcodes.HO_READ_64, currentTag, frameStack, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.ICONST_1, currentTag, null, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_1_2, currentTag, heap, PORT_B, NO_TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, heap, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 2:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH64, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.PUTSTATIC2_QUICK:
		switch(count){
			case 0:
				if(heap.tokenQueueFull() || frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				heap.addToken(HeapOpcodes.HO_WRITE_64, currentTag, null, PORT_A, NO_TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP64, currentTag, heap, PORT_C, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_1_2, currentTag, heap, PORT_B, NO_TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.ICONST_1, currentTag, null, PORT_A, NO_TAG_INC);
				count++;
				break;
			case 2:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, heap, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.INVOKEVIRTUAL_QUICK:
		switch(count){
			case 0:
				if(tokenMachine.tokenQueueFull()){
					break;
				}
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_1_2, currentTag, tokenMachine, PORT_A, TAG_INC);
				currentTag++;
				count++;
				break;
			case 1:
				if(heap.tokenQueueFull() || frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				heap.addToken(HeapOpcodes.GET_CTI, currentTag, tokenMachine, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.PEEK, currentTag, heap, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.LOAD_ARG_RMTI, currentTag, frameStack, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 2:
				if(frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.INVOKE, currentTag, null, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.INVOKE, currentTag, frameStack, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.INVOKENONVIRTUAL_QUICK:
		switch(count){
			case 0:
				if(tokenMachine.tokenQueueFull()){
					break;
				}
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_1_2, currentTag, tokenMachine, PORT_A, TAG_INC);
				currentTag++;
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.INVOKE, currentTag, null, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.INVOKE_STATIC, currentTag, frameStack, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.INVOKESTATIC_QUICK:
		switch(count){
			case 0:
				if(tokenMachine.tokenQueueFull()){
					break;
				}
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_1_2, currentTag, tokenMachine, PORT_A, TAG_INC);
				currentTag++;
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.INVOKE, currentTag, null, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.INVOKE_STATIC, currentTag, frameStack, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.INVOKEINTERFACE_QUICK:
		switch(count){
			case 0:
				if(tokenMachine.tokenQueueFull()){
					break;
				}
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_1_2_3_4, currentTag, tokenMachine, PORT_A, TAG_INC);
				currentTag++;
				count++;
				break;
			case 1:
				if(heap.tokenQueueFull() || frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				heap.addToken(HeapOpcodes.GET_CTI, currentTag, tokenMachine, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.PEEK, currentTag, heap, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.LOAD_ARG_IOLI_RIMTI, currentTag, frameStack, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 2:
				if(frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.INVOKE, currentTag, null, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.INVOKE_INTERFACE, currentTag, frameStack, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.NEWARRAY_QUICK:
		switch(count){
			case 0:
				if(tokenMachine.tokenQueueFull()){
					break;
				}
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_1, currentTag, tokenMachine, PORT_A, TAG_INC);
				currentTag++;
				count++;
				break;
			case 1:
				if(heap.tokenQueueFull() || frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				heap.addToken(HeapOpcodes.ALLOC_ARRAY, currentTag, frameStack, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, heap, PORT_B, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.NEWARRAY_CTI, currentTag, heap, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 2:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSHREF, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.NEW_QUICK:
		switch(count){
			case 0:
				if(tokenMachine.tokenQueueFull()){
					break;
				}
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_1_2, currentTag, tokenMachine, PORT_A, TAG_INC);
				currentTag++;
				count++;
				break;
			case 1:
				if(tokenMachine.tokenQueueFull()){
					break;
				}
				tokenMachine.addToken(TokenMachineOpcodes.CLASSSIZE, currentTag, heap, PORT_B, TAG_INC);
				currentTag++;
				count++;
				break;
			case 2:
				if(heap.tokenQueueFull() || frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				heap.addToken(HeapOpcodes.ALLOC_OBJ, currentTag, frameStack, PORT_A, NO_TAG_INC);
				frameStack.addToken(FrameStackOpcodes.PUSHREF, currentTag, null, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_1_2, currentTag, heap, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.ANEWARRAY_QUICK:
		switch(count){
			case 0:
				if(heap.tokenQueueFull() || frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				heap.addToken(HeapOpcodes.ALLOC_ARRAY, currentTag, frameStack, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, heap, PORT_B, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_1_2, currentTag, heap, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSHREF, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.MULTIANEWARRAY_QUICK:
		switch(count){
			case 0:
				if(tokenMachine.tokenQueueFull()){
					break;
				}
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_1_2, currentTag, tokenMachine, PORT_A, TAG_INC);
				currentTag++;
				count++;
				break;
			case 1:
				if(tokenMachine.tokenQueueFull()){
					break;
				}
				tokenMachine.addToken(TokenMachineOpcodes.MULTINEWARRAY_CTI, currentTag, heap, PORT_A, TAG_INC);
				currentTag++;
				count++;
				break;
			case 2:
				if(heap.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				heap.addToken(HeapOpcodes.SETUP_MULTI_ARRAY, currentTag, null, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_3, currentTag, heap, PORT_B, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 3:
				if(tokenMachine.tokenQueueFull()){
					break;
				}
				tokenMachine.addToken(TokenMachineOpcodes.LOOP_0_BYTECODE_3, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 4:
				// Loop Start
				if(!loopIterationsValid){
					break;
				}
				if(loopIterations == 0){
					count++;
					break;
				}
				if(heap.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				heap.addToken(HeapOpcodes.SET_MULTI_ARRAY_DIM_SIZE, currentTag, null, PORT_A, NO_TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, heap, PORT_B, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 5:
				// Loop End
				if(loopIterations-- > 1){
					count = 4;
					break;
				}
				loopIterationsValid = false;
				if(heap.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				heap.addToken(HeapOpcodes.ALLOC_MULTI_ARRAY, currentTag, frameStack, PORT_A, NO_TAG_INC);
				frameStack.addToken(FrameStackOpcodes.PUSHREF, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.CHECKCAST_QUICK:
		switch(count){
			case 0:
				if(heap.tokenQueueFull() || frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				heap.addToken(HeapOpcodes.GET_CTI, currentTag, tokenMachine, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.PEEK_1, currentTag, heap, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_1_2, currentTag, tokenMachine, PORT_B, TAG_INC);
				currentTag++;
				count++;
				break;
			case 1:
				if(tokenMachine.tokenQueueFull()){
					break;
				}
				tokenMachine.addToken(TokenMachineOpcodes.CHECKCAST, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.INSTANCEOF_QUICK:
		switch(count){
			case 0:
				if(heap.tokenQueueFull() || frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				heap.addToken(HeapOpcodes.GET_CTI, currentTag, tokenMachine, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, heap, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_1_2, currentTag, tokenMachine, PORT_B, TAG_INC);
				currentTag++;
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH32, currentTag, null, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.INSTANCEOF, currentTag, frameStack, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.LDC_STRING:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSHREF, currentTag, null, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_1_2, currentTag, frameStack, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.GETFIELDA_QUICK:
		switch(count){
			case 0:
				if(heap.tokenQueueFull() || frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				heap.addToken(HeapOpcodes.HO_READ, currentTag, frameStack, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, heap, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_1_2, currentTag, heap, PORT_B, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSHREF, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.GETSTATICA_QUICK:
		switch(count){
			case 0:
				if(heap.tokenQueueFull() || frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				heap.addToken(HeapOpcodes.HO_READ, currentTag, frameStack, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.ICONST_1, currentTag, null, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_1_2, currentTag, heap, PORT_B, NO_TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, heap, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 2:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSHREF, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.REF_TO_INT:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, frameStack, PORT_A, TAG_INC);
				currentTag++;
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH32, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.INT_TO_REF:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, frameStack, PORT_A, TAG_INC);
				currentTag++;
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSHREF, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.READ_ADDRESS:
		switch(count){
			case 0:
				if(heap.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				heap.addToken(HeapOpcodes.PHY_READ, currentTag, frameStack, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, heap, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH32, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.WRITE_ADDRESS:
		switch(count){
			case 0:
				if(heap.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				heap.addToken(HeapOpcodes.PHY_WRITE, currentTag, null, PORT_A, NO_TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, heap, PORT_C, NO_TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, heap, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.DISABLE_SCHEDULING:
		switch(count){
			case 0:
				if(scheduler.tokenQueueFull()){
					break;
				}
				scheduler.addToken(SchedulerOpcodes.DISABLESCHEDULING, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.FORCE_SCHEDULING:
		switch(count){
			case 0:
				if(scheduler.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				scheduler.addToken(SchedulerOpcodes.FORCESCHEDULING, currentTag, null, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.FORCESCHEDULING, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.ENABLE_SCHEDULING:
		switch(count){
			case 0:
				if(scheduler.tokenQueueFull()){
					break;
				}
				scheduler.addToken(SchedulerOpcodes.ENABLESCHEDULING, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.HWBINVOKE:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull() || acceleratorCore.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, acceleratorCore, PORT_C, NO_TAG_INC);
				acceleratorCore.addToken(AcceleratorCoreOpcodes.INVOKE, currentTag, frameStack, PORT_A, TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, acceleratorCore, PORT_B, NO_TAG_INC);
				count++;
				break;
			case 2:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, acceleratorCore, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 3:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH32, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.FLUSHREF:
		switch(count){
			case 0:
				if(heap.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				heap.addToken(HeapOpcodes.FLUSH, currentTag, null, PORT_A, NO_TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, heap, PORT_B, NO_TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, heap, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.INVALIDATEREF:
		switch(count){
			case 0:
				if(heap.tokenQueueFull() || frameStack.tokenQueueFull()){
					break;
				}
				heap.addToken(HeapOpcodes.INVALIDATE, currentTag, null, PORT_A, NO_TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, heap, PORT_B, NO_TAG_INC);
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, heap, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.CGRA_START:
		switch(count){
			case 0:
				if(tokenMachine.tokenQueueFull() || cgra.tokenQueueFull()){
					break;
				}
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_1, currentTag, tokenMachine, PORT_A, NO_TAG_INC);
				cgra.addToken(CgraOpcodes.INIT, currentTag, null, PORT_A, NO_TAG_INC);
				count++;
				break;
			case 1:
				if(tokenMachine.tokenQueueFull()){
					break;
				}
				tokenMachine.addToken(TokenMachineOpcodes.INIT_LIVE_IN_OUT, currentTag, cgra, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 2:
				if(tokenMachine.tokenQueueFull()){
					break;
				}
				tokenMachine.addToken(TokenMachineOpcodes.LOOP_0_BYTECODE_2, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 3:
				// Loop Start
				if(!loopIterationsValid){
					break;
				}
				if(loopIterations == 0){
					count++;
					break;
				}
				if(frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull() || cgra.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.LOAD32, currentTag, null, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.LOAD_LIVE_IN_OUT, currentTag, frameStack, PORT_A, NO_TAG_INC);
				cgra.addToken(CgraOpcodes.RECEIVELOCALVAR, currentTag, null, PORT_A, NO_TAG_INC);
				count++;
				break;
			case 4:
				if(loopIterations == 0){
					count++;
					break;
				}
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, cgra, PORT_B, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 5:
				// Loop End
				if(loopIterations-- > 1){
					count = 3;
					break;
				}
				loopIterationsValid = false;
				if(cgra.tokenQueueFull()){
					break;
				}
				cgra.addToken(CgraOpcodes.RUN, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.CGRA_STOP:
		switch(count){
			case 0:
				currentTag++;
				count++;
				break;
			case 1:
				if(tokenMachine.tokenQueueFull()){
					break;
				}
				tokenMachine.addToken(TokenMachineOpcodes.LOOP_0_BYTECODE_1, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 2:
				// Loop Start
				if(!loopIterationsValid){
					break;
				}
				if(loopIterations == 0){
					count++;
					break;
				}
				if(frameStack.tokenQueueFull() || cgra.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH32, currentTag, null, PORT_A, NO_TAG_INC);
				cgra.addToken(CgraOpcodes.SENDLOCALVAR, currentTag, frameStack, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 3:
				if(loopIterations == 0){
					count++;
					break;
				}
				if(frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.STORE32, currentTag, null, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.LOAD_LIVE_IN_OUT, currentTag, frameStack, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 4:
				// Loop End
				if(loopIterations-- > 1){
					count = 2;
					break;
				}
				loopIterationsValid = false;
				if(tokenMachine.tokenQueueFull()){
					break;
				}
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_3_4, currentTag, tokenMachine, PORT_A, TAG_INC);
				currentTag++;
				count++;
				break;
			case 5:
				if(tokenMachine.tokenQueueFull()){
					break;
				}
				tokenMachine.addToken(TokenMachineOpcodes.BRANCH, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.WIDE_ILOAD:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.LOAD32, currentTag, null, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_1_2, currentTag, frameStack, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.WIDE_LLOAD:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.LOAD64, currentTag, null, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_1_2, currentTag, frameStack, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.WIDE_FLOAD:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.LOAD32, currentTag, null, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_1_2, currentTag, frameStack, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.WIDE_DLOAD:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.LOAD64, currentTag, null, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_1_2, currentTag, frameStack, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.WIDE_ALOAD:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.LOAD32, currentTag, null, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_1_2, currentTag, frameStack, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.WIDE_ISTORE:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.STORE32, currentTag, null, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_1_2, currentTag, frameStack, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.WIDE_LSTORE:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.STORE64, currentTag, null, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_1_2, currentTag, frameStack, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.WIDE_FSTORE:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.STORE32, currentTag, null, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_1_2, currentTag, frameStack, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.WIDE_DSTORE:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.STORE64, currentTag, null, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_1_2, currentTag, frameStack, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.WIDE_ASTORE:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.STORE32, currentTag, null, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_1_2, currentTag, frameStack, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.WIDE_IINC:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.LOAD32, currentTag, null, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_1_2, currentTag, frameStack, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 1:
				if(ialu.tokenQueueFull() || frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				ialu.addToken(IaluOpcodes.IADD, currentTag, frameStack, PORT_A, TAG_INC);
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, ialu, PORT_B, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_3_4, currentTag, ialu, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 2:
				if(frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.PUSH32, currentTag, null, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_1_2, currentTag, frameStack, PORT_A, TAG_INC);
				currentTag++;
				count++;
				break;
			case 3:
				if(frameStack.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.STORE32, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
	case ByteCode.WIDE_RET:
		switch(count){
			case 0:
				if(frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.LOAD32, currentTag, null, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.SENDBYTECODE_1_2, currentTag, frameStack, PORT_A, NO_TAG_INC);
				currentTag++;
				count++;
				break;
			case 1:
				if(frameStack.tokenQueueFull() || tokenMachine.tokenQueueFull()){
					break;
				}
				frameStack.addToken(FrameStackOpcodes.POP32, currentTag, tokenMachine, PORT_A, NO_TAG_INC);
				tokenMachine.addToken(TokenMachineOpcodes.RET, currentTag, null, PORT_A, NO_TAG_INC);
				currentTag++;

				count = -1;
				break;
		}
		break;
}
}
}
