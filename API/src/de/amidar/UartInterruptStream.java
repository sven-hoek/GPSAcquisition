package de.amidar;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class UartInterruptStream {
//	private UartOutStream outStream;
//	private UartInStream inStream;
//	private Thread uartRxHandlerThread;
//	private Thread uartTxHandlerThread;
//
//
//	public UartInterruptStream (UART16550 uart, int bufSize) throws Exception {
//		ThreadSafeFIFO transmitFifo = new ThreadSafeFIFO (bufSize);
//		outStream = new UartOutStream (transmitFifo);
//
//		ThreadSafeFIFO receiveFifo = new ThreadSafeFIFO (bufSize);
//		inStream = new UartInStream (receiveFifo);
//
//		uartRxHandlerThread = new RxHandlerThread (uart, receiveFifo);
//		uartRxHandlerThread.setPriority (Thread.MAX_PRIORITY);
//		uartRxHandlerThread.start ();
//
//		uartTxHandlerThread = new TxHandlerThread (uart, transmitFifo);
//		uartRxHandlerThread.setPriority (Thread.MAX_PRIORITY - 2);
//		uartTxHandlerThread.start ();
//
//		uart.configureInterrupts (true, true);
//	}
//
//	public OutputStream getOutputStream () {
//		return outStream;
//	}
//
//	public InputStream getInputStream () {
//		return inStream;
//	}
//
//
//	private class RxHandlerThread extends Thread {
//		private UART16550 uart;
//		private ThreadSafeFIFO receiveFifo;
//
//		public RxHandlerThread (UART16550 uart, ThreadSafeFIFO receiveFifo)
//				throws Exception {
//			this.uart = uart;
//			this.receiveFifo = receiveFifo;
//		}
//
//		public void run () {
//			try {
//				while (!isInterrupted ()) {
//					while (uart.isRxAvailable () && !isInterrupted ()) {
//						try {
//							receiveFifo.enqueue (uart.receive ());
//						} catch (InterruptedException e) {
//							AmidarSystem.WriteAddress (0x90000000, 0x81);
//						}
//					}
//
//					synchronized (uart) {
//						try {
//							uart.wait ();
//						} catch (InterruptedException e) {
//							AmidarSystem.WriteAddress (0x90000000, 0x82);
//						}
//					}
//				}
//			} catch (Exception e) {
//				AmidarSystem.WriteAddress (0x90000000, 0x83);
//				uart.sendString ("\nER: ");
//				uart.sendString (e.getMessage ());
//				uart.send ('\n');
//			}
//		}
//	}
//
//	private class TxHandlerThread extends Thread {
//		private UART16550 uart;
//		private ThreadSafeFIFO transmitFifo;
//
//		public TxHandlerThread (UART16550 uart, ThreadSafeFIFO transmitFifo)
//				throws Exception {
//			this.uart = uart;
//			this.transmitFifo = transmitFifo;
//		}
//
//		public void run () {
//			try {
//				while (!isInterrupted ()) {
//					if (uart.isTxEmpty () && !isInterrupted ()) {
//						uart.setTxInterrupt (false);
//						
//						int i = 0;
//						while (i < 15) {
//							uart.send ((char) transmitFifo.dequeue ());
//							i++;
//							if (isInterrupted ())
//								break;
//						}
//					}
//
//					synchronized (uart) {
//						try {
//							uart.setTxInterrupt (true);
//							uart.wait ();
//						} catch (InterruptedException e) {
//							AmidarSystem.WriteAddress (0x90000000, 0x82);
//						}
//					}
//				}
//			} catch (RuntimeException e) {
//				AmidarSystem.WriteAddress (0x90000000, 0x86);
//				uart.sendString ("\nET: ");
//				uart.sendString (e.getMessage ());
//				uart.send ('\n');
//			} catch (Exception e) {
//				AmidarSystem.WriteAddress (0x90000000, 0x85);
//				uart.sendString ("\nET: ");
//				uart.sendString (e.getMessage ());
//				uart.send ('\n');
//			}
//		}
//	}
//
//
//	private class UartOutStream extends OutputStream {
//		private ThreadSafeFIFO fifo;
//
//		public UartOutStream (ThreadSafeFIFO transmitFifo) {
//			fifo = transmitFifo;
//		}
//
//		public void write (int b) throws IOException {
//			try {
//				fifo.enqueue (b);
//			} catch (InterruptedException e) {
//				throw new IOException ("Thread interrupted");
//			}
//		}
//
//	}
//
//	private class UartInStream extends InputStream {
//		private ThreadSafeFIFO fifo;
//
//		public UartInStream (ThreadSafeFIFO receiveFifo) {
//			fifo = receiveFifo;
//		}
//
//		public int read () throws IOException {
//			try {
//				return fifo.dequeue ();
//			} catch (InterruptedException e) {
//				throw new IOException ("Thread interrupted");
//			}
//		}
//	}
}
