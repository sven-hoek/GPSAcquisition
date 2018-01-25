package java.lang;

import de.amidar.AmidarSystem;
import de.amidar.scheduler.Scheduler;

public class Object {

	protected Object clone () throws CloneNotSupportedException {
		// TODO Implement cloning (in Heap?)
		throw new UnsupportedOperationException ();
	}

	public boolean equals (Object o2) {
		return this == o2;
	}

	protected void finalize () throws Throwable {
	}

	public final Class getClass () {
//		int[] handle = AmidarSystem.readHandle (AmidarSystem.RefToInt (this));
//		int cti = handle[1] >>> 16;
//		return Class.forCti (cti);
		return null;
	}

	public int hashCode () {
		return AmidarSystem.RefToInt (this);
	}

	public final void notify () {
//		Scheduler.Instance ().NotifyObject (this);
	}

	public final void notifyAll () {
//		Scheduler.Instance ().NotifyObjectAll (this);
	}

	public String toString () {
		return "asdf";
//		return getClass ().getName () + '@' + Integer.toHexString (hashCode ());
	}

	public void wait () throws InterruptedException {
		wait (0);
	}

	public void wait (long millis) throws InterruptedException {
		wait (millis, 0);
	}

	public void wait (long millis, int nanos) throws InterruptedException {
//		Scheduler.Instance ().WaitOnObject (this, millis, nanos);
	}

}
