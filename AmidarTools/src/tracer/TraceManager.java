package tracer;

import java.util.*;
import java.io.*;

/**
 * The IOManager is an omnipresent class to which input and output streams are to be
 * registered. These streams are bundled in I/O facilities and both can be called by
 * names. I/O facilities make it possible to filter input and output. The IOManager
 * is the interface to all I/O facilities. The IOManager is accessed via a call to
 * the static get method.
 * @author Michael Raitza
 * @version - 20.03.2011
 */
public class TraceManager implements Serializable{

	public Map<String,Trace> iofs;
	private Map<String,PrintWriter> outs;
	private Map<String,BufferedReader> ins;
	private Map<String,AbstractSet<String>> inmap;
	private Map<String,AbstractSet<String>> outmap;

	private static class IOMHolder {
		public static final TraceManager INSTANCE = new TraceManager();
	}

	public TraceManager() {
		iofs = new HashMap<String, Trace>();
		outs = new HashMap<String, PrintWriter>();
		ins = new HashMap<String, BufferedReader>();
		outmap = new HashMap<String, AbstractSet<String>>();
		inmap = new HashMap<String, AbstractSet<String>>();

		if (System.console() != null) {
			newout("stdout", System.console().writer());
			newin("stdin", System.console().reader());
		}
		else {
			newout("stdout", System.out);
			newin("stdin", System.in);
		}		
		newout("stderr", System.err);
		
		newout("nullout", Trace.stdout());
		newin("nullin", Trace.stdin());
		newf("nullio", "nullout", "nullin", true);

		newf("stdio", "stdout", "stdin", true);
		newf("errio", "stderr", "stdin", true);
		newf("dbgio", "stderr", "stdin", false);
		
	}
	
	
	public TraceManager(FileOutputStream file) {
		iofs = new HashMap<String, Trace>();
		outs = new HashMap<String, PrintWriter>();
		ins = new HashMap<String, BufferedReader>();
		outmap = new HashMap<String, AbstractSet<String>>();
		inmap = new HashMap<String, AbstractSet<String>>();

//		if (System.console() != null) {
//			newout("stdout", System.console().writer());
//			newin("stdin", System.console().reader());
//		}
//		else {
//			newout("stdout", System.out);
//			newin("stdin", System.in);
//		}		
//		newout("stderr", System.err);
		
		newout("stdout", file);
		newout("stderr", file);
		

		
		newout("nullout", Trace.stdout());
		newin("nullin", Trace.stdin());
		newf("nullio", "nullout", "nullin", true);

		newf("stdio", "stdout", "stdin", true);
		newf("errio", "stderr", "stdin", true);
		newf("dbgio", "stderr", "stdin", false);
		
	}

	/**
	 * Creates and/or returns the IOManager instance.
	 * @return the instance of the IOManager
	 */
	public static TraceManager get() {
		return IOMHolder.INSTANCE;
	}
	
	/**
	 * Returns the IOFacility named by <code>name</code>. If the wanted IOFacility is not existing,
	 * the IOFacility 'dump' is returned.
	 * 
	 * @param name the name of the IOFacility
	 * @return the named IOFacility if existing, else the  IOFacility 'dump'
	 */
	public Trace getf(final String name) {
		return iofs.get(name);
	}

	/**
	 * Acquires an IOFacility, returns the named IOFacility if found or creates a new one.
	 * @param fname the name of the IOFacility
	 * @param oname the name of the output stream, only used if a new IOFacility is created
	 * @param iname the name of the input stream, only used if a new IOFacility is created
	 * @return the IOFacility
	 */
	public Trace acquiref(final String fname, final String oname, final String iname) {
		Trace iof = iofs.get(fname);
		if (iof == null){
			return newf(fname, oname, iname);
		}
		return iof;
	}

	/**
	 * Creates a new IOFacility. If an IOFacility with this name already existed, it will be
	 * destroyed. Streams registered to this I/O facility become potentially inaccessible.
	 * @param fname name of the IOFacility
	 * @param oname name of the output stream to use
	 * @param iname name of the input stream to use
	 * @return the new IOFacility
	 */
	public Trace newf(final String fname, final String oname, final String iname) {
		return newf(fname, oname, iname, false);
	}
	
	/**
	 * Adds a new Trace to the Tracemanager
	 * @param fname
	 * @param trace
	 * @return
	 */
	public Trace newf(String fname, Trace trace){
		iofs.put(fname, trace);
		return trace;
	}
	

	/**
	 * Creates a new IOFacility. If an IOFacility with this name already existed, it will be
	 * destroyed. Streams registered to this I/O facility become potentially inaccessible.
	 * @param fname name of the IOFacility
	 * @param oname name of the output stream to use
	 * @param iname name of the input stream to use
	 * @param special the special flag of the new IOFacility
	 * @return the new IOFacility
	 */
	private Trace newf(final String fname, final String oname, final String iname, final Boolean special) {
		rmf(fname);
		Trace iof = new Trace(regostream(fname, oname), registream(fname, iname), oname, iname, special);
		iofs.put(fname, iof);
		return iof;
	}

	/**
	 * Removes the IOFacility named by <code>name</code>.
	 * @param fname the name of the IOFaclility
	 */
	public void rmf(final String fname) {
		Trace iof = iofs.get(fname);
		if (iof != null) {
			deregostream(fname, iof.oname());
			deregistream(fname, iof.iname());
			iofs.remove(fname);
		}
	}

	/**
	 * Deregisters the output stream named <code>oname</code> from
	 * the IOFacility named <code>fname</code>.
	 * @param fname name of the IOFacility
	 * @param oname name of the output stream
	 */
	public void deregostream(String fname, String oname) {
		AbstractSet<String> sl = outmap.get(oname);
		if (sl.contains(fname))
			sl.remove(fname);

		if (sl.isEmpty()) {
			outmap.remove(oname);
			try { outs.get(oname).close(); }
			catch (Throwable e) { }
			outs.remove(oname);
		}
	}

	/**
	 * Deregisters the input stream named <code>iname</code> from
	 * the IOFacility named <code>fname</code>.
	 * @param fname name of the IOFacility
	 * @param iname name of the input stream
	 */
	public void deregistream(String fname, String iname) {
		AbstractSet<String> sl = inmap.get(iname);
		if (sl.contains(fname))
			sl.remove(fname);

		if (sl.isEmpty()) {
			inmap.remove(iname);
			try { ins.get(iname).close(); }
			catch (Throwable e) { }
			ins.remove(iname);
		}
	}

	/**
	 * Registers an output stream with an IOFacility.
	 * @param fname name of the IOFacility
	 * @param oname name of the output stream
	 */
	public PrintWriter regostream(String fname, String oname) {
		AbstractSet<String> sl = outmap.get(oname);
		PrintWriter out;

		if (sl == null) {
			sl = new HashSet<String>();
			outmap.put(oname, sl);
		}
		sl.add(fname);

		if ((out = outs.get(oname)) == null) {
			out = newout(oname,Trace.stdout());
		}
		return out;
	}

	/**
	 * Registers an input stream with an IOFacility.
	 * @param fname name of the IOFacility
	 * @param iname name of the input stream
	 */
	public BufferedReader registream(String fname, String iname) {
		AbstractSet<String> sl = inmap.get(iname);
		BufferedReader in;

		if (sl == null) {
			sl = new HashSet<String>();
			inmap.put(iname, sl);
		}
		sl.add(fname);

		if ((in = ins.get(iname)) == null) {
			in = newin(iname, Trace.stdin());
		}
		return in;
	}

	/**
	 * Creates a new named output stream.
	 * @param oname name of the output stream
	 * @param os the output stream
	 * @return the print writer of the new output stream
	 */
	public PrintWriter newout(final String oname, final OutputStream os) {
		PrintWriter out = new PrintWriter(os, true);
		PrintWriter o = outs.put(oname, out);
		try { o.close(); }
		catch (Throwable e) { }
		return out;
	}

	/**
	 * Creates a new named output stream.
	 * @param oname name of the output stream
	 * @param wr the printwriter of the output stream
	 * @return the print writer of the new output stream
	 */
	public PrintWriter newout(final String oname, final PrintWriter wr) {
		PrintWriter w = outs.put(oname,wr);
		try { w.close(); }
		catch (Throwable e) { }
		return wr;
	}

	/**
	 * Creates a new named input stream. Internally a buffered reader is created.
	 * @param iname name of the input stream
	 * @param is the input stream
	 * @return the buffered reader of the new input stream
	 */
	public BufferedReader newin(final String iname, final InputStream is) {
		BufferedReader in = new BufferedReader(new InputStreamReader(is));
		BufferedReader i = ins.put(iname, in);
		try { i.close(); }
		catch (Throwable e) { }
		return in;
	}

	/**
	 * Creates a new named input stream. Internally a buffered reader is created.
	 * @param iname name of the input stream
	 * @param rd the Reader of the output stream
	 * @return the buffered reader of the new input stream
	 */
	public BufferedReader newin(final String iname, Reader rd) {
		try {
			rd = (BufferedReader)rd;
		}
		catch (ClassCastException e) {
			rd = new BufferedReader(rd);
		}

		BufferedReader i = ins.put(iname, (BufferedReader)rd);
		try { i.close(); }
		catch (Throwable e) { }
		return (BufferedReader)rd;
	}

	/**
	 * Activates an IOFacility.
	 * @param name the name of the IOFacility to activate
	 */
	public void activate(String name) {
		try {
			iofs.get(name).activate();
		}
		catch (Throwable e) { }
	}

	/**
	 * Deactivates an IOFacility. All input and output will be discarded.
	 * @param name the name of the IOFacility to de-activate
	 */
	public void deactivate(String name) {
		try {
			iofs.get(name).deactivate();
		}
		catch (Throwable e) { }
	}

	/**
	 * Returns the error IOFacility.
	 * @return the IOFacility for error output, usually System.err.
	 */
	public static Trace stderr() {
		return get().getf("errio");
	}

	/**
	 * Returns the error IOFacility.
	 * @return the IOFacility for error output, usually System.err.
	 */
	public static Trace stdio() {
		return get().getf("stdio");
	}

	/**
	 * Returns the standard debug I/O facility, usually deactivated.
	 * @return the IOFacility for standard debug output, usually System.out.
	 */
	public static Trace stddbg() {
		return get().getf("dbgio");
	}

}
