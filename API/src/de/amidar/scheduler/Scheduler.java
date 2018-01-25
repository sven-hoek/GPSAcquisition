package de.amidar.scheduler;

import java.util.ArrayList;

import de.amidar.AmidarSystem;
import de.amidar.FrameStack;
import de.amidar.HashMapIdentity;

public class Scheduler {
	private FrameStack frameStack;
	private int wrapperAmti;
	public State state;
	public SchedulerThread[] threads;
	private ArrayList freeThreads;

	private HashMapIdentity monitors = new HashMapIdentity ();

	private static Scheduler instance;

	static {
		Instance ();
	}

	public static Scheduler Instance () {
		if (instance == null) {
			instance = new Scheduler ();
		}
		return instance;
	}

	private Scheduler () {
//		wrapperAmti = AmidarSystem
//				.ReadAddress (AmidarSystem.AXT_THREAD_WRAPPER_AMTI_ADDRESS);
//
//		state = (State) AmidarSystem.GetPeripheral (State.class, 0);
//		ArrayList threadList = AmidarSystem
//				.GetPeripherals (SchedulerThread.class);
//
//		threads = new SchedulerThread[threadList.size ()];
//		for (int i = 0; i < threads.length; i++)
//			threads[i] = (SchedulerThread) threadList.get (i);
//
//		freeThreads = new ArrayList (threads.length);
//		for (int i = 1; i < threads.length; i++) {
//			freeThreads.add (new Integer (i));
//		}
//
//		frameStack = (FrameStack) AmidarSystem.GetPeripheral (FrameStack.class,
//				0);
//
//		// Initialize scheduler thread 0 (currently running thread = main
//		// thread)
//		SchedulerThread t = threads[0];
//		t.priority = 1;
//		t.hasWaitTimeout = false;
//		t.monitorObject = null;
//		t.waitTimeout_low = 0;
//		t.waitTimeout_high = 0;
//		t.isReady = true;
//
//		// Enable scheduler
//		AmidarSystem.EnableScheduling ();
	}

	protected final static void ThreadRunWrapper (Thread threadObj) {
		try {
			threadObj.run ();
		} catch (Exception e) {
			if (System.err != null) {
//				System.err.println ("Uncaught exception in Thread "
//						+ threadObj.getName () + ":");
				System.err.println (e.getMessage ());
				e.printStackTrace (System.err);
			}
		} catch (StackOverflowError e) {
			if (System.err != null) {
//				System.err.println ("StackOverflow in Thread "
//						+ threadObj.getName () + ":" + e.getMessage ());
			}
		}
		Scheduler.Instance ().TerminateThread (threadObj);
	}

	public int GetMaxThreads () {
		return threads.length;
	}

	public int CreateThread (Thread thread, int priority) {
		AmidarSystem.DisableScheduling ();
		if (freeThreads.size () > 0) {
			int id = ((Integer) freeThreads.remove (freeThreads.size () - 1))
					.intValue ();
			SchedulerThread t = threads[id];
			t.isReady = false;
			t.priority = 1 << (priority - 1);
			t.amti = wrapperAmti;
			t.pc = 0;
			t.hasWaitTimeout = false;
			t.monitorObject = null;
			t.waitTimeout_low = 0;
			t.waitTimeout_high = 0;

			frameStack.threadSelect = id;
			frameStack.localsPointer = 0;
			frameStack.callercontextPointer = 1;
			frameStack.stackPointer = 1 + FrameStack.CALLERCONTEXT_WORDS;
			frameStack.maxPointer = frameStack.stackPointer;
			frameStack.overflow = 0;

			frameStack.stackAddressSelect = 0;
			frameStack.stackData = AmidarSystem.RefToInt (thread);
			frameStack.stackMeta = FrameStack.ENTRYTYPE_REF;

			AmidarSystem.EnableScheduling ();
			return id;
		} else {
			// TODO: Throw exception?
			AmidarSystem.EnableScheduling ();
			return -1;
		}
	}

	public void SetPriority (Thread thread, int priority) {
//		threads[thread.getThreadId ()].priority = 1 << (priority - 1);
	}

	public void StartThread (Thread thread) {
//		SchedulerThread t = threads[thread.getThreadId ()];
//		t.isStarted = true;
//		t.isReady = true;
	}

	protected void TerminateThread (Thread thread) {
		AmidarSystem.DisableScheduling ();

//		int id = thread.getThreadId ();
//		SchedulerThread t = threads[id];

//		thread.setTerminated ();

//		t.isReady = false;
//		t.isStarted = false;
//		t.monitorObject = null;
//		t.hasWaitTimeout = false;
//		freeThreads.add (new Integer (id));

//		frameStack.threadSelect = id;
//		frameStack.localsPointer = 0;
//		frameStack.callercontextPointer = 0;
//		frameStack.stackPointer = 0;
//		frameStack.overflow = 0;
//
//		AmidarSystem.ForceScheduling ();
	}

	public int GetMaxStackUsage (int threadId) {
		frameStack.threadSelect = threadId;
		return frameStack.maxPointer;
	}

	public void Interrupt (Thread thread) {
		AmidarSystem.DisableScheduling ();
//
//		int id = thread.getThreadId ();
//		SchedulerThread t = threads[id];
//
//		if (thread.isWaiting ()) {
//			t.isReady = true;
//			t.monitorObject = null;
//		}
//
//		AmidarSystem.EnableScheduling ();
	}

	public void MonitorEnter (Object obj) {
//		if (obj == null)
//			throw new NullPointerException ("MonitorEnter with null-ref");
//
//		boolean schedulerEnabledBefore = state.enableScheduling;
//		
//		// Disable scheduling so scheduler does not interrupt the following HW
//		// table accesses
//		AmidarSystem.DisableScheduling ();
//
//		// Get current thread's ID and monitor's ID
//		int curThreadId = state.currentThreadId;
//		SchedulerThread curThread = threads[curThreadId];
//
//		Thread curJavaThread = Thread.GetThreadById (curThreadId);
//
//		// Check if already owning the lock or wait for a free lock
//		Monitor monitor = GetMonitorForObject (obj, true);
//
//		if (monitor.ownerThread == curJavaThread || monitor.lockCount < 1) {
//			// This thread either already owns the monitor or no one does,
//			// nothing to do
//		} else {
//			try {
//				curJavaThread.SetBlockedBy (monitor);
//				curThread.isReady = false;
//				monitor.ownerThread.AddBlockedThread (curJavaThread);
//
//				InheritPriority (monitor, curThread, curJavaThread);
//
//				monitor.AddBlockedThread (curJavaThread);
//
//				monitor = acquireLock (curJavaThread, obj, false);
//			} catch (InterruptedException e) {
//				// Can not be thrown as checkInterrupted parameter is false
//			}
//		}
//
//		monitor.ownerThread = curJavaThread;
//		monitor.lockCount++;
//
//		if (schedulerEnabledBefore) {
//			// Reenable scheduling
//			AmidarSystem.EnableScheduling ();
//		}
	}

	private void InheritPriority (Monitor monitor,
			SchedulerThread curSchedThread, Thread curJavaThread) {
//		int newPrio = curSchedThread.priority;
//
//		while (monitor != null) {
//			Thread ownerJavaThread = monitor.ownerThread;
//			SchedulerThread ownerSchedThread = threads[ownerJavaThread
//					.getThreadId ()];
//
//			if (newPrio > ownerSchedThread.priority) {
//				ownerSchedThread.priority = newPrio;
//			}
//
//			monitor = ownerJavaThread.GetBlockedBy ();
//		}
	}

	public void MonitorExit (Object obj) {
//		if (obj == null)
//			throw new NullPointerException ("MonitorExit with null-ref");
//
//		boolean schedulerEnabledBefore = state.enableScheduling;
//		
//		// Disable scheduling so scheduler does not interrupt the following HW
//		// table accesses
//		AmidarSystem.DisableScheduling ();
//
//		// Get current thread's ID and monitor's ID
//		int curThreadId = state.currentThreadId;
//		SchedulerThread curThread = threads[curThreadId];
//
//		Thread curJavaThread = Thread.GetThreadById (curThreadId);
//
//		// Check if already owning the lock or wait for a free lock
//		Monitor monitor = GetMonitorForObject (obj, false);
//		if (monitor == null || monitor.ownerThread != curJavaThread
//				|| monitor.lockCount < 1) {
//			// Monitor does not exist or thread does not own monitor
//			AmidarSystem.EnableScheduling ();
//			throw new IllegalMonitorStateException (
//					"MonitorExit: Thread does not own monitor");
//		} else {
//			monitor.lockCount--;
//
//			if (monitor.lockCount == 0) {
//				// No more locks on this Monitor by the current thread, release
//				// it
//
//				// Find blocked thread of monitor with highest dynamic priority
//				ArrayList monitorBlockedThreads = monitor.blockedThreads;
//				if (monitorBlockedThreads != null) {
//					int maxPriorityIndex = -1;
//					int maxPriority = -1;
//					int size = monitorBlockedThreads.size ();
//					for (int i = 0; i < size; i++) {
//						Thread blocked = (Thread) monitorBlockedThreads.get (i);
//						SchedulerThread schedThread = threads[blocked
//								.getThreadId ()];
//
//						if (schedThread.priority > maxPriority) {
//							maxPriorityIndex = i;
//							maxPriority = schedThread.priority;
//						}
//
//						// Also remove all threads blocked by this monitor from
//						// current thread's blocked list
//						curJavaThread.RemoveBlockedThread (blocked);
//					}
//
//					if (maxPriorityIndex >= 0) {
//						// There was at least one blocked thread: Unblock it and
//						// set it to be ready
//						Thread highestThread = monitor
//								.RemoveBlockedThread (maxPriorityIndex);
//						SchedulerThread highestSchedThread = threads[highestThread
//								.getThreadId ()];
//
//						highestThread.SetBlockedBy (null);
//						highestSchedThread.isReady = true;
//					}
//				}
//
//				// Calculate new dynamic priority for the current thread based
//				// on the dynamic priorities of the threads it still blocks
//				ArrayList threadBlockedThreads = curJavaThread
//						.GetBlockedThreads ();
//				// Use own static priority as min value
//				int maxBlockedPriority = 1 << curJavaThread.getPriority ();
//				int size = threadBlockedThreads.size ();
//				for (int i = 0; i < size; i++) {
//					SchedulerThread schedThread = threads[((Thread) threadBlockedThreads.get (i))
//							.getThreadId ()];
//					if (schedThread.priority > maxBlockedPriority) {
//						maxBlockedPriority = schedThread.priority;
//					}
//				}
//
//				curThread.priority = maxBlockedPriority;
//			}
//
//		}
//
//		if (schedulerEnabledBefore) {
//			// Reenable scheduling
//			AmidarSystem.EnableScheduling ();
//		}

	}

	public void NotifyObject (Object obj) {
//		boolean schedulerEnabledBefore = state.enableScheduling;
//		
//		// Disable scheduling so scheduler does not interrupt the following HW
//		// table accesses
//		AmidarSystem.DisableScheduling ();
//
//		// Get current thread's ID and monitor's ID
//		Thread curJavaThread = Thread.currentThread ();
//
//		Monitor monitor = HasThreadLockOnObject (curJavaThread, obj, true);
//
//		Thread waitThread = monitor.PopWaitingThread ();
//		if (waitThread != null) {
//			int waitThreadId = waitThread.getThreadId ();
//			SchedulerThread waitThreadSched = threads[waitThreadId];
//			waitThreadSched.hasWaitTimeout = false;
//			waitThreadSched.isReady = true;
//			waitThreadSched.monitorObject = null;
//			waitThread.setWaiting (false);
//		}
//
//		if (schedulerEnabledBefore) {
//			// Reenable scheduling
//			AmidarSystem.EnableScheduling ();
//		}
	}

	public void NotifyObjectAll (Object obj) {
//		boolean schedulerEnabledBefore = state.enableScheduling;
//		
//		// Disable scheduling so scheduler does not interrupt the following HW
//		// table accesses
//		AmidarSystem.DisableScheduling ();
//
//		// Get current thread's ID and monitor's ID
//		Thread curJavaThread = Thread.currentThread ();
//
//		Monitor monitor = HasThreadLockOnObject (curJavaThread, obj, true);
//
//		Thread waitThreadIter;
//		while ((waitThreadIter = monitor.PopWaitingThread ()) != null) {
//			int waitThreadId = waitThreadIter.getThreadId ();
//			SchedulerThread waitThreadSched = threads[waitThreadId];
//			waitThreadSched.hasWaitTimeout = false;
//			waitThreadSched.isReady = true;
//			waitThreadSched.monitorObject = null;
//			waitThreadIter.setWaiting (false);
//		}
//
//		if (schedulerEnabledBefore) {
//			// Reenable scheduling
//			AmidarSystem.EnableScheduling ();
//		}
	}

	public void WaitOnObject (Object obj, long millis, int nanos)
			throws InterruptedException {
//		if (millis < 0) {
//			throw new IllegalArgumentException (
//					"millisecond timeout value is negative");
//		}
//
//		if (nanos < 0 || nanos > 999999) {
//			throw new IllegalArgumentException (
//					"nanosecond timeout value out of range");
//		}
//
//		boolean schedulerEnabledBefore = state.enableScheduling;
//		
//		// Disable scheduling so scheduler does not interrupt the following HW
//		// table accesses
//		AmidarSystem.DisableScheduling ();
//
//		// Get current thread's ID and monitor's ID
//		int curThreadId = state.currentThreadId;
//		SchedulerThread curThread = threads[curThreadId];
//
//		// Update thread entry
//		curThread.isReady = false;
//		curThread.monitorObject = obj;
//
//		Thread curJavaThread = Thread.GetThreadById (curThreadId);
//
//		Monitor monitor = null;
//
//		if (obj != null) {
//			monitor = HasThreadLockOnObject (curJavaThread, obj, true);
//		}
//
//		// Convert real time timeout to system tick count
//		long timeout;
//		if (millis != 0 || nanos != 0) {
//			timeout = state.GetTickCount () + (state.frequency / 1000000)
//					* (millis * 1000 + nanos / 1000);
//		} else {
//			timeout = Long.MAX_VALUE;
//		}
//		// Update thread entry
//		curThread.waitTimeout_high = (int) (timeout >>> 32);
//		curThread.waitTimeout_low = (int) timeout;
//
//		if (obj != null) {
//			curJavaThread.SaveMonitorState (monitor, obj);
//			monitor.AddWaitingThread (curJavaThread);
//		}
//
//		curJavaThread.setWaiting (true);
//
//		if (monitor != null) {
//			// Find blocked thread with highest priority and unblock it
//			ArrayList blockedThreads = monitor.blockedThreads;
//			if (blockedThreads != null) {
//				int highestPrioIndex = GetHighestDynPriorityThreadIndex (blockedThreads);
//				if (highestPrioIndex >= 0) {
//					// There was at least one blocked thread: Unblock it and set
//					// it to be ready
//					Thread highestThread = monitor
//							.RemoveBlockedThread (highestPrioIndex);
//					SchedulerThread highestSchedThread = threads[highestThread
//							.getThreadId ()];
//
//					highestThread.SetBlockedBy (null);
//					highestSchedThread.isReady = true;
//				}
//			}
//		}
//
//		if (millis != 0 || nanos != 0) {
//			curThread.hasWaitTimeout = true;
//		}
//
//		// Wait until this thread reacquired the lock on the object
//		monitor = acquireLock (curJavaThread, obj, true);
//
//		curJavaThread.setWaiting (false);
//
//		// Fill monitor information back in from backed up data in thread table
//		if (obj != null) {
//			curJavaThread.RestoreMonitorState (monitor);
//			monitor.RemoveWaitingThread (curJavaThread);
//		}
//
//		if (schedulerEnabledBefore) {
//			// Reenable scheduling
//			AmidarSystem.EnableScheduling ();
//		}
//	}
//
//	public void Sleep (long millis, int nanos) throws InterruptedException {
//		WaitOnObject (null, millis, nanos);
//	}
//
//	public void JoinThread (Thread joinedThread, long millis, int nanos)
//			throws InterruptedException {
//		if (millis == 0 && nanos == 0) {
//			while (joinedThread.isAlive ()) {
//				WaitOnObject (joinedThread, millis, nanos);
//			}
//		} else {
//			WaitOnObject (joinedThread, millis, nanos);
//		}
	}

	/**
	 * Get list index of thread with highest dynamic priority from iterator over
	 * threads
	 * 
	 * @param list
	 *            List of threads to loop on
	 * @return Index into list of found thread or -1 if no threads in list
	 */
	private int GetHighestDynPriorityThreadIndex (ArrayList list) {
//		int maxPriorityIndex = -1;
//		int maxPriority = -1;
//		int size = list.size ();
//		for (int i = 0; i < size; i++) {
//			Thread blocked = (Thread) list.get (i);
//			SchedulerThread schedThread = threads[blocked.getThreadId ()];
//
//			if (schedThread.priority > maxPriority) {
//				maxPriorityIndex = i;
//				maxPriority = schedThread.priority;
//			}
//		}
//
//		return maxPriorityIndex;
		return -1;
	}

	/**
	 * Check if the given thread owns the lock on the object.
	 * 
	 * @param thread
	 *            Thread that should own the lock
	 * @param obj
	 *            Object to lock on
	 * @return True if the thread owns the monitor, false otherwise
	 */
	public boolean HasThreadLockOnObject (Thread thread, Object obj) {
		return HasThreadLockOnObject (thread, obj, false) != null;
	}

	/**
	 * Check if the given thread owns the lock on the object.
	 * 
	 * @param thread
	 *            Thread that should own the lock
	 * @param obj
	 *            Object to lock on
	 * @param throwException
	 *            If set and the thread does not own the monitor an
	 *            IllegalMonitorStateException is thrown
	 * @return Monitor for the object if the thread owns the lock, null
	 *         otherwise
	 * @throws IllegalMonitorStateException
	 *             If throwException is set and the thread does not own the lock
	 */
	private Monitor HasThreadLockOnObject (Thread thread, Object obj,
			boolean throwException) throws IllegalMonitorStateException {
		Monitor monitor = GetMonitorForObject (obj, false);
		if (monitor != null && monitor.lockCount > 0
				&& monitor.ownerThread == thread) {
			return monitor;
		} else {
			if (throwException) {
				AmidarSystem.EnableScheduling ();
				throw new IllegalMonitorStateException (
						"Thread does not own Monitor");
			} else {
				return null;
			}
		}
	}

	/**
	 * Get the monitor for the given object.
	 * 
	 * @param obj
	 *            Object to get the monitor for
	 * @param create
	 *            If set and there currently is no monitor for the object create
	 *            a new one
	 * @return Monitor instance for the object
	 */
	private Monitor GetMonitorForObject (Object obj, boolean create) {
		Monitor monitor = (Monitor) monitors.get (obj);
		if (monitor == null && create) {
			monitor = new Monitor ();
			monitors.put (obj, monitor);
		}
		return monitor;
	}

	/**
	 * Waits until the given thread owns the monitor on the object to lock.
	 * After execution scheduling is disabled.
	 * 
	 * @param forThread
	 *            Thread that tries to acquire the lock
	 * @param lockObj
	 *            Object that should be locked. If no object is given the method
	 *            will simply force a single thread switch and return after it
	 *            gets scheduled again
	 * @param checkInterrupted
	 *            If set and thread has been interrupted an InterruptedException
	 *            will be thrown
	 * @return The monitor that is responsible for the lock of the given object
	 * @throws InterruptedException
	 *             If thread was interrupted and checkInterrupted is set
	 */
	private Monitor acquireLock (Thread forThread, Object lockObj,
			boolean checkInterrupted) throws InterruptedException {
//		boolean hasLock = false;
//		Monitor monitor = null;
//
//		do {
//			// Force a scheduling attempt right now
//			AmidarSystem.ForceScheduling ();
//
//			// Disable scheduling again
//			AmidarSystem.DisableScheduling ();
//
//			if (checkInterrupted && forThread.isInterrupted ()) {
//				// Was interrupted -> throw exception (but make sure scheduling
//				// is first enabled again)
//				AmidarSystem.EnableScheduling ();
//				throw new InterruptedException ();
//			} else {
//				if (lockObj != null) {
//					monitor = (Monitor) monitors.get (lockObj);
//					if (monitor == null || monitor.lockCount < 1) {
//						// No monitor for object or no Thread holding a lock
//						hasLock = true;
//
//						if (monitor == null) {
//							monitor = new Monitor ();
//							monitors.put (lockObj, monitor);
//						}
//					}
//				} else {
//					// No object -> was a simple Sleep()
//					hasLock = true;
//				}
//			}
//		} while (!hasLock);
//
//		return monitor;
		return null;
	}

}
