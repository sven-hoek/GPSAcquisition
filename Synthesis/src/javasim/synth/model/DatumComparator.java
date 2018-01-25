package javasim.synth.model;

import java.util.Comparator;

import javasim.synth.model.datum.Datum;


public class DatumComparator implements Comparator<Datum> {

	@Override
	public int compare(Datum o1, Datum o2) {
		return Integer.compare(o1.creator().addr(), o2.creator().addr());
	}

}
