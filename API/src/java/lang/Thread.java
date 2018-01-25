package java.lang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import de.amidar.AmidarSystem;
import de.amidar.scheduler.Monitor;
import de.amidar.scheduler.Scheduler;

public class Thread implements Runnable {

	public void run() {
		// TODO Auto-generated method stub
		
	}

//	public final static int STATE_NEW = 0;
//	public final static int STATE_STARTED = 1;
//	public final static int STATE_WAITING = 2;
//	public final static int STATE_TERMINATED = 3;
//
//
//	public final static int MIN_PRIORITY = 1;
//	public final static int NORM_PRIORITY = 5;
//	public final static int MAX_PRIORITY = 10;
//
//	public final static int DEFAULT_STACKSIZE_WORDS = 512;
//
//
//	private static int numAnonymousThreadsCreated = 0;
//	private static boolean mainThreadObjectCreated;
//	private static Thread[] idToThread = new Thread[Scheduler.Instance ()
//			.GetMaxThreads ()];
//
//	protected Runnable runnable = null;
//
//	protected String name = null;
//
//	protected boolean daemon = false;
//
//	protected int priority = NORM_PRIORITY;
//
//	protected int state = STATE_NEW;
//	protected boolean interrupted = false;
//
//	protected long stacksize = DEFAULT_STACKSIZE_WORDS;
//
//	protected int threadId = -1;
//
//	protected ClassLoader contextClassLoader = null;
//
//	protected ThreadGroup group = null;
//
//	protected ArrayList blockedThreads = new ArrayList ();
//	protected Monitor blockedBy = null;
//
//	protected int monitorLockCount;
//	protected Object monitorObject;
//
//	// ========================================================================
//	// Static methods
//	
//	static {
//		CreateMainThread();
//	}
//
//	public static int activeCount () {
//		// TODO: Implement when ThreadGroup supported
//		//return currentThread ().group.activeCount ();
//		return -1;
//	}
//
//	public static Thread currentThread () {
//		return GetThreadById (Scheduler.Instance ().state.currentThreadId);
//	}
//
//	public static int enumerate (Thread[] tArray) {
//		// TODO: Implement when ThreadGroup supported
//		//return currentThread ().group.enumerate (tArray);
//		return -1;
//	}
//
//	public static boolean holdsLock (Object obj) {
//		return Scheduler.Instance ().HasThreadLockOnObject (currentThread (),
//				obj);
//	}
//
//	public static boolean interrupted () {
//		return currentThread ().isInterrupted (true);
//	}
//
//	public static void yield () {
//		AmidarSystem.ForceScheduling ();
//	}
//
//	/**
//	 * Do not call directly, only for Scheduler!
//	 * 
//	 * Creates a new Thread object for the main (current) thread
//	 */
//	public static void CreateMainThread () {
//		if (idToThread == null) // TODO: Remove when static initializers are
//								// implemented
//			idToThread = new Thread[Scheduler.Instance ().GetMaxThreads ()];
//		new Thread (false);
//	}
//
	public static void sleep (long millis) throws InterruptedException {
		sleep (millis, 0);
	}

	public static void sleep (long millis, int nanos)
			throws InterruptedException {
//		Scheduler.Instance ().Sleep (millis, nanos);
	}
//
//	public static Thread GetThreadById (int id) {
//		if (idToThread == null) { // TODO: Remove when static initializers are
//								// implemented
//			idToThread = new Thread[Scheduler.Instance ().GetMaxThreads ()];
//			Arrays.fill (idToThread, null);
//		}
//
//		if (!mainThreadObjectCreated) {
//			CreateMainThread ();
//		}
//
//		if (id < 0 || id > idToThread.length)
//			throw new ArrayIndexOutOfBoundsException ("No such thread id");
//
//		return idToThread[id];
//	}
//
//	// ========================================================================
//	// Constructors
//
//	/**
//	 * Do not call directly, only for Scheduler!
//	 * 
//	 * Creates the Thread object for the main thread (which is already running!)
//	 */
//	private Thread (boolean _x) {
//		threadId = 0;
//		idToThread[threadId] = this;
//		mainThreadObjectCreated = true;
//
//		this.name = "Thread-Main";
//
//		setPriority (NORM_PRIORITY);
//		this.stacksize = DEFAULT_STACKSIZE_WORDS;
//		this.state = STATE_STARTED;
//		this.daemon = false;
//		this.contextClassLoader = null; // TODO: Update when class loaders
//										// available
//		
//		// inform debugger that main thread is started
//		Scheduler.Instance().threads[threadId].isStarted = true;
//
//		// TODO: Implement when ThreadGroup supported
//		//group = ThreadGroup.root;
//		//group.addThread (this);
//	}
//
//	public Thread () {
//		this ((Runnable) null);
//	}
//
//	public Thread (Runnable target) {
//		this (target, null);
//	}
//
//	public Thread (Runnable target, String name) {
//		this (null, target, name);
//	}
//
//	public Thread (String name) {
//		this ((Runnable) null, name);
//	}
//
//	public Thread (ThreadGroup group, Runnable target) {
//		this (group, target, null);
//	}
//
//	public Thread (ThreadGroup group, String name) {
//		this (group, null, name);
//	}
//
//	public Thread (ThreadGroup group, Runnable target, String name) {
//		this (group, target, name, DEFAULT_STACKSIZE_WORDS);
//	}
//
//	public Thread (ThreadGroup group, Runnable target, String name,
//			long stackSize) {
//		if (!mainThreadObjectCreated) {		
//			CreateMainThread ();
//		}
//
//		Thread current = currentThread ();
//
//		threadId = Scheduler.Instance ().CreateThread (this, NORM_PRIORITY);
//		if (threadId < 0) {
//			throw new RuntimeException ("No free thread slots");
//		}
//		idToThread[threadId] = this;
//
//		if (group == null) {
//			group = current.group;
//		}
//		// TODO: Implement when ThreadGroup supported
//		//group.addThread (this);
//
//
//		if (name == null) {
//			this.name = "Thread-" + ++numAnonymousThreadsCreated;
//		} else {
//			this.name = name;
//		}
//
//		this.runnable = target;
//		setPriority (NORM_PRIORITY);
//		this.stacksize = stackSize;
//		// TODO: Use stacksize when framestack supports individual sizes
//
//		this.state = STATE_NEW;
//		this.daemon = current.daemon;
//		this.group = group;
//		this.contextClassLoader = current.contextClassLoader;
//	}
//
//	// ========================================================================
//	// Public methods
//
//	public synchronized void start () {
//		if (state != STATE_NEW)
//			throw new IllegalThreadStateException ("Thread already started");
//
//		state = STATE_STARTED;
//		Scheduler.Instance ().StartThread (this);
//	}
//
//	public void run () {
//		if (runnable != null) {
//			runnable.run ();
//		}
//	}
//
//	public final String getName () {
//		return name;
//	}
//
//	public final void setName (String name) {
//		this.name = name;
//	}
//
//	public final int getThreadId () {
//		return threadId;
//	}
//
//	public final int getPriority () {
//		return priority;
//	}
//
//	public final void setPriority (int prio) {
//		if (prio > MAX_PRIORITY)
//			prio = MAX_PRIORITY;
//		if (prio < MIN_PRIORITY)
//			prio = MIN_PRIORITY;
//
//		this.priority = prio;
//
//		Scheduler.Instance ().SetPriority (this, prio);
//	}
//
//	public boolean isDaemon () {
//		return daemon;
//	}
//
//	public void setDaemon (boolean on) {
//		// TODO: make sure daemon state is used for something useful
//		daemon = on;
//	}
//
//	public String toString () {
//		// TODO: Implement when ThreadGroup supported
//		return "Thread \"" + name + "\", priority " + priority
//				/*+ (group != null ? ", group \"" + group.name + "\"" : "")*/;
//	}
//
//	public final boolean isAlive () {
//		return state != STATE_NEW && state != STATE_TERMINATED;
//	}
//
//	public boolean isInterrupted () {
//		return isInterrupted (false);
//	}
//
//	private boolean isInterrupted (boolean clearInterruptedFlag) {
//		boolean isInt = interrupted;
//		if (clearInterruptedFlag) {
//			interrupted = true;
//		}
//		return isInt;
//	}
//
//	public void interrupt () {
//		interrupted = true;
//		Scheduler.Instance ().Interrupt (this);
//	}
//
//	public synchronized final void join () throws InterruptedException {
//		join (0);
//	}
//
//	public synchronized final void join (long millis)
//			throws InterruptedException {
//		join (millis, 0);
//	}
//
//	public synchronized final void join (long millis, int nanos)
//			throws InterruptedException {
//		Scheduler.Instance ().JoinThread (this, millis, nanos);
//	}
//
//	// ========================================================================
//	// Methods for interaction with Scheduler, not to be called directly!
//
//	/**
//	 * Do not call directly, only for Scheduler!
//	 * 
//	 * Checks if this thread is in wait state
//	 */
//	public boolean isWaiting () {
//		return state == STATE_WAITING;
//	}
//
//	/**
//	 * Do not call directly, only for Scheduler!
//	 * 
//	 * Sets this thread's wait state
//	 */
//	public void setWaiting (boolean waiting) {
//		if (waiting)
//			state = STATE_WAITING;
//		else
//			state = STATE_STARTED;
//	}
//
//	/**
//	 * Do not call directly, only for Scheduler!
//	 * 
//	 * Sets this thread's state to terminated
//	 */
//	public void setTerminated () {
//		state = STATE_TERMINATED;
//		synchronized (this) {
//			notifyAll ();
//		}
//		idToThread[threadId] = null;
//	}
//
//	/**
//	 * Do not call directly, only for Scheduler!
//	 * 
//	 * Sets this thread's monitor state according to the passed Monitor data
//	 */
//	public void SaveMonitorState (Monitor monitor, Object monitoredObject) {
//		monitorLockCount = monitor.lockCount;
//		monitorObject = monitoredObject;
//		monitor.lockCount = 0;
//		monitor.ownerThread = null;
//	}
//
//	/**
//	 * Do not call directly, only for Scheduler!
//	 * 
//	 * Restores the monitor state stored in this Thread into the passed Monitor
//	 * object
//	 */
//	public void RestoreMonitorState (Monitor monitor) {
//		monitor.lockCount = monitorLockCount;
//		monitor.ownerThread = this;
//		monitorLockCount = 0;
//		monitorObject = null;
//	}
//
//
//	public Monitor GetBlockedBy () {
//		return blockedBy;
//	}
//
//	public void SetBlockedBy (Monitor monitor) {
//		blockedBy = monitor;
//	}
//
//	public void AddBlockedThread (Thread t) {
//		blockedThreads.add (t);
//	}
//
//	public void RemoveBlockedThread (Thread t) {
//		blockedThreads.remove (t);
//	}
//
//	public ArrayList GetBlockedThreads () {
//		return blockedThreads;
//	}
//
//	// ========================================================================
//	// Private / protected methods
//
//	// ========================================================================
//	// (Currently) unsupported methods
//
//	public static void dumpStack () {
//		// Print stack trace of current stack, could be enabled later on
//		throw new UnsupportedOperationException ();
//	}
//
//	public final void checkAccess () {
//		// No security manager in our API
//		throw new UnsupportedOperationException ();
//	}
//
//	public ThreadGroup getThreadGroup () {
//		return group;
//	}
//
//	public ClassLoader getContextClassLoader () {
//		return contextClassLoader;
//	}
//
//	public void setContextClassLoader (ClassLoader cl) {
//		contextClassLoader = cl;
//		// TODO: Use it
//	}
//
//	// ========================================================================
//	// Deprecated methods
//
//	public int countStackFrames () {
//		// Deprecated
//		throw new UnsupportedOperationException ();
//	}
//
//	public void destroy () {
//		// Not implemented by Java
//		throw new UnsupportedOperationException ();
//	}
//
//	public void resume () {
//		// Deprecated
//		throw new UnsupportedOperationException ();
//	}
//
//	public synchronized void stop () {
//		// Deprecated
//		throw new UnsupportedOperationException ();
//	}
//
//	public synchronized void stop (Throwable obj) {
//		// Deprecated
//		throw new UnsupportedOperationException ();
//	}
//
//	public void suspend () {
//		// Deprecated
//		throw new UnsupportedOperationException ();
//	}


}
