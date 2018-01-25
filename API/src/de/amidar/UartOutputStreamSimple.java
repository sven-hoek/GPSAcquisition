package de.amidar;

import java.io.IOException;
import java.io.OutputStream;

public class UartOutputStreamSimple extends OutputStream {
	private UART16550 uart;
	
	public UartOutputStreamSimple (UART16550 uart) {
		this.uart = uart;
	}

	public void write (int b) throws IOException {
		uart.send ((char)b);
	}
}
