package scheduler;

import java.util.Comparator;

public final class Interval {
	public final Integer lbound;
	public final Integer ubound;

	/**
	 * This comparator delivers Intervals in ascending order to their lower bound.
	 * That is leftmost outermost order.
	 */
	public static class LBComp implements Comparator<Interval> {
		public int compare(Interval a, Interval b) {
			return a.lbound.compareTo(b.lbound);
		}
	}

	/**
	 * This comparator delivers Intervals in ascending order to their upper bound.
	 * That is rightmost innermost order.
	 */
	public static class UBComp implements Comparator<Interval> {
		public int compare(Interval a, Interval b) {
			if (a.ubound.equals(b.ubound)) {
				return b.lbound.compareTo(a.lbound);
			}
			return a.ubound.compareTo(b.ubound);
		}
	}

	/**
	 * Creates a new interval in the bounds l,u inclusive with l &lt;= u. The bounds given
	 * are switched if u &lt; l.
	 * @param l the lower bound
	 * @param u the upper bound
	 */
	public Interval(Integer l, Integer u) {	
		if (u < l) {
			lbound = u;
			ubound = l;
		} else {
			lbound = l;
			ubound = u;
		}
	}

	/**
	 * Returns TRUE iff N is contained in the interval.
	 * @param n must be non-NULL
	 */
	public boolean contains(Integer n) {
		return lbound <= n && n <= ubound;
	}

	/**
	 * Returns TRUE iff there is no place k such that k is contained in this interval or in
	 * the given interval I.
	 * @param i the given interval
	 */
	public boolean independent(Interval i) {
		return (i.ubound < lbound) || (i.lbound > ubound);
	}

	/**
	 * Aligns this interval at its upper bound to U.
	 * @param u the number to align at
	 * @return the new interval aligned to U.
	 */
	public Interval align_ubound(Integer u) {
		return new Interval(lbound + u - ubound, u);
	}

	/**
	 * Aligns this interval at its lower bound to L.
	 * @param l the number to align at
	 * @return the new interval aligned to L
	 */
	public Interval align_lbound(Integer l) {
		return new Interval(l, ubound + l - lbound);
	}

	/**
	 * Shifts this interval by S.
	 * @param s the amount to shift this interval
	 * @return the new interval shifted by S.
	 */
	public Interval shift(Integer s) {
		return new Interval(lbound + s, ubound + s);
	
	}

	public boolean gt(Interval i) {
		return lbound > i.ubound;
	}

	public boolean lt(Interval i) {
		return ubound < i.lbound;
	}

	/**
	 * Returns a string representation of this interval in the form
	 * "[l:u]".
	 */
	@Override
	public String toString() {
		return "[" + lbound + ":" + ubound + "]";
	}

	@Override
	public boolean equals(Object i) {
		Interval ii = (Interval) i;
		return ii.lbound.equals(lbound) && ii.ubound.equals(ubound);
	}

	/**
	 * Returns the length of this interval.
	 */
	public Integer length() {
		return ubound - lbound;
	}

    /**
     * resizes the interval to cover the given interval.
     * @param inv given interval
     * @return Interval containing both the current and the given interval
     **/
	public Interval enclosing(Interval inv) {
        return new Interval(Math.min(lbound, inv.lbound), Math.max(ubound, inv.ubound));
	}
}
