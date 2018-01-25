package de.amidar;

public class ThreadSafeFIFO {
	private int[] buffer;
	private int readPointer = 0;
	private int writePointer = 0;
	private int size;

	public ThreadSafeFIFO (int size) {
		this.size = size;
		buffer = new int[size];
	}

	public void enqueue (int value) throws InterruptedException {
		synchronized (buffer) {
			int tmpWritePointer = (writePointer + 1) % size;
			while (tmpWritePointer == readPointer) {
				buffer.wait ();
				tmpWritePointer = (writePointer + 1) % size;
			}

			buffer[writePointer] = value;

			writePointer = tmpWritePointer;

			buffer.notifyAll ();
		}
	}

	public int dequeue () throws InterruptedException {
		synchronized (buffer) {
			while (readPointer == writePointer) {
				buffer.wait ();
			}

			int result = buffer[readPointer];

			readPointer = (readPointer + 1) % size;

			buffer.notifyAll ();

			return result;
		}
	}
}