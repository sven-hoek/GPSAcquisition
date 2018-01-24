package javasim.synth;

import functions.HashCalculator;
import javasim.synth.model.datum.Datum;

public class DEdge {
	public final Datum sink;
	public final Integer attr;
	public DEdge(Datum s, Integer a) {
		sink = s;
		attr = a;
	}
	public boolean equals(Object o) {
		DEdge e = (DEdge)o;
		if (e != null)
			if (e.sink.equals(sink) && e.attr.equals(attr))
				return true;
		return false;
	}
	public int hashCode() {
		return HashCalculator.hashCode(sink.hashCode(), attr.hashCode());
	}
}

