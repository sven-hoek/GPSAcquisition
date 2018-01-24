package de.amidar;

import de.amidar.scheduler.Scheduler;

public class UART16550 implements AmidarPeripheral {
	private byte data_divisor0;
	private byte interrupt_enable_divisor1;
	private byte fifo_control_interrupt_identification;
	private byte line_control;
	private byte modem_control;
	private byte line_status;
	private byte modem_status;
	
	private static final byte IER_RECEIVE_DATA_AVAILABLE_INTERRUPT = 0x01;
	private static final byte IER_TRANSMITTER_HOLDING_REGISTER_EMPTY_INTERRUPT = 0x02;
	private static final byte IER_RECEIVER_LINE_STATUS_INTERRUPT = 0x04;
	private static final byte IER_MODEM_STATUS_INTERRUPT = 0x08;

	private static final byte IIR_TYPE_MASK = 0x0e;
	private static final byte IIR_RECEIVER_LINE_STATUS = 0x06;
	private static final byte IIR_RECEIVER_DATA_AVAILABLE = 0x04;
	private static final byte IIR_TIMEOUT_INDICATION = 0x0c;
	private static final byte IIR_TRANSMITTER_HOLDING_REGISTER_EMPTY = 0x02;
	private static final byte IIR_MODEM_STATUS = 0x00;

	private static final byte FCR_CLEAR_RECEIVER_FIFO = 0x02;
	private static final byte FCR_CLEAR_TRANSMITTER_FIFO = 0x04;
	private static final byte FCR_RECEIVER_FIFO_TRIGGER_LEVEL_MASK = (byte) 0xc0;
	private static final byte FCR_RECEIVER_FIFO_TRIGGER_LEVEL_1_BYTE = 0x00;
	private static final byte FCR_RECEIVER_FIFO_TRIGGER_LEVEL_4_BYTES = 0x40;
	private static final byte FCR_RECEIVER_FIFO_TRIGGER_LEVEL_8_BYTES = (byte) 0x80;
	private static final byte FCR_RECEIVER_FIFO_TRIGGER_LEVEL_14_BYTES = (byte) 0xc0;
	
	private static final byte LCR_WORDSIZE_MASK = 0x03;
	private static final byte LCR_WORDSIZE_5 = 0x00;
	private static final byte LCR_WORDSIZE_6 = 0x01;
	private static final byte LCR_WORDSIZE_7 = 0x02;
	private static final byte LCR_WORDSIZE_8 = 0x03;
	private static final byte LCR_STOP_BITS_2 = 0x04;
	private static final byte LCR_PARITY_ENABLE = 0x08;
	private static final byte LCR_EVEN_PARITY = 0x10;
	private static final byte LCR_STICK_PARITY_BIT = 0x20;
	private static final byte LCR_BREAK_CONTROL_BIT = 0x40;
	private static final byte LCR_DIVISOR_LATCH_ACCESS = (byte) 0x80;
	
	private static final byte MCR_DTR = 0x01;
	private static final byte MCR_RTS = 0x02;
	private static final byte MCR_OUT1 = 0x04;
	private static final byte MCR_OUT2 = 0x08;
	private static final byte MCR_LOOPBACK_MODE = 0x10;
	
	private static final byte LSR_DATA_READY = 0x01;
	private static final byte LSR_OVERRUN_ERROR = 0x02;
	private static final byte LSR_PARITY_ERROR = 0x04;
	private static final byte LSR_FRAMING_ERROR = 0x08;
	private static final byte LSR_BREAK_INTERRUPT = 0x10;
	private static final byte LSR_TRANSMIT_FIFO_EMPTY = 0x20;
	private static final byte LSR_TRANSMITTER_EMPTY = 0x40;
	private static final byte LSR_ERROR = (byte) 0x80;
	
	private static final byte MSR_DCTS = 0x01;
	private static final byte MSR_DDSR = 0x02;
	private static final byte MSR_TERI = 0x04;
	private static final byte MSR_DDCD = 0x08;
	private static final byte MSR_CTS_RTS = 0x10;
	private static final byte MSR_DSR_DTR = 0x20;
	private static final byte MSR_RI_OUT1 = 0x40;
	private static final byte MSR_DCD_OUT2 = (byte) 0x80;
	

	public float initUart (int baudrate) {
//		float coreFrequency = (float)Scheduler.Instance ().state.frequency;
//		int prescaler = Math.round (coreFrequency / (16 * baudrate));
//		if (prescaler > 0xffff) {
//			throw new IllegalArgumentException ("Baudrate prescaler greater than 65535");
//		}
//		float error = (coreFrequency / 16 / prescaler) / baudrate - 1;
//		
//		line_control = LCR_DIVISOR_LATCH_ACCESS | LCR_WORDSIZE_8;
//		interrupt_enable_divisor1 = (byte)((prescaler >>> 8) & 0xff);
//		data_divisor0 = (byte)(prescaler & 0xff);
//		line_control = LCR_WORDSIZE_8;
//		fifo_control_interrupt_identification = FCR_RECEIVER_FIFO_TRIGGER_LEVEL_8_BYTES;
//		interrupt_enable_divisor1 = 0;
//		
		return -1;
	}
	
	public void send (char b) {
		do {} while ((line_status & LSR_TRANSMIT_FIFO_EMPTY) == 0);
		
		data_divisor0 = (byte) b;
	}
	
	public void sendString (String message) {
		if (message != null) {
			for (int i = 0; i < message.length (); i++) {
				send (message.charAt (i));
			}
		}
	}

	public short receive () {
		if ((line_status & LSR_DATA_READY) > 0) {
			return (short)((int)data_divisor0 & 0xff);
		} else {
			return (short)-1;
		}
	}
	
	public boolean isRxAvailable () {
		return (line_status & LSR_DATA_READY) > 0;
	}
	
	public boolean isTxEmpty () {
		return (line_status & LSR_TRANSMIT_FIFO_EMPTY) > 0;
	}
	
	public void configureInterrupts (boolean receiveDataInt, boolean transmitEmptyInt) {
		interrupt_enable_divisor1 = 0;
		if (receiveDataInt)
			interrupt_enable_divisor1 |= IER_RECEIVE_DATA_AVAILABLE_INTERRUPT;
		if (transmitEmptyInt)
			interrupt_enable_divisor1 |= IER_TRANSMITTER_HOLDING_REGISTER_EMPTY_INTERRUPT;
		getInterruptIdentification ();
	}
	
	public void setTxInterrupt (boolean enable) {
		if (enable)
			interrupt_enable_divisor1 |= IER_TRANSMITTER_HOLDING_REGISTER_EMPTY_INTERRUPT;
		else
			interrupt_enable_divisor1 &= ~IER_TRANSMITTER_HOLDING_REGISTER_EMPTY_INTERRUPT;
	}
	
	public byte getInterruptIdentification () {
		return fifo_control_interrupt_identification;
	}
	
	public static boolean interruptIsRxStatus (byte interruptSource) {
		return (interruptSource & IIR_TYPE_MASK) == IIR_RECEIVER_LINE_STATUS;
	}

	public static boolean interruptIsRxAvailable (byte interruptSource) {
		return (interruptSource & IIR_TYPE_MASK) == IIR_RECEIVER_DATA_AVAILABLE;
	}
	
	public static boolean interruptIsRxTimeout (byte interruptSource) {
		return (interruptSource & IIR_TYPE_MASK) == IIR_TIMEOUT_INDICATION;
	}
	
	public static boolean interruptIsTxEmpty (byte interruptSource) {
		return (interruptSource & IIR_TYPE_MASK) == IIR_TRANSMITTER_HOLDING_REGISTER_EMPTY;
	}
	
	public static boolean interruptIsModemStatus (byte interruptSource) {
		return (interruptSource & IIR_TYPE_MASK) == IIR_MODEM_STATUS;
	}
	
	public static boolean interruptIsRxAvailableOrTimeout (byte interruptSource) {
		byte masked = (byte)(interruptSource & IIR_TYPE_MASK);
		return masked == IIR_RECEIVER_DATA_AVAILABLE || masked == IIR_TIMEOUT_INDICATION;
	}
	
	
	public UART16550 () {}

}
