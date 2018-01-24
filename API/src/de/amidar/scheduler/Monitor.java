package de.amidar.scheduler;

import java.util.ArrayList;

public class Monitor {
	public Thread ownerThread = null;
	public int lockCount = 0;

	/**
	 * Threads that are currently wait()'ing on the given object
	 */
	public ArrayList waitingThreads = null;

	/**
	 * Threads that are blocked on the object by another thread
	 */
	public ArrayList blockedThreads = null;

	public void AddWaitingThread (Thread t) {
		if (waitingThreads == null) {
			waitingThreads = new ArrayList ();
		}
		waitingThreads.add (t);
	}

	public void RemoveWaitingThread (Thread thread) {
		if (waitingThreads == null)
			return;

		waitingThreads.remove (thread);
	}

	public Thread PopWaitingThread () {
		if (waitingThreads == null)
			return null;

		int size = waitingThreads.size ();
		if (size > 0)
			return (Thread) waitingThreads.remove (size - 1);
		else
			return null;
	}

	public boolean MonitorInUse () {
		return lockCount > 0
				|| (waitingThreads != null && waitingThreads.size () > 0)
				|| (blockedThreads != null && blockedThreads.size () > 0);
	}

	public void AddBlockedThread (Thread t) {
		if (blockedThreads == null) {
			blockedThreads = new ArrayList ();
		}
		blockedThreads.add (t);
	}

	public Thread RemoveBlockedThread (int index) {
		if (blockedThreads == null)
			return null;
		return (Thread) blockedThreads.remove (index);
	}

	public Thread PopBlockedThread () {
		if (blockedThreads == null)
			return null;

		int size = blockedThreads.size ();
		if (size > 0)
			return (Thread) blockedThreads.remove (size - 1);
		else
			return null;
	}

}
