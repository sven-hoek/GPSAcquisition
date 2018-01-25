/**
 * 
 */
package cgramodel;

/**
 * Class to represent ranges in IDC for certain data.
 * @author ruschke
 *
 */
public class IDCrange {
	private int lower;
	private int upper;
	
	/**
	 * @param lower
	 * @param upper
	 */
	public IDCrange(int lower, int upper) {
		super();
		this.lower = lower;
		this.upper = upper;
	}

	/**
	 * @return the lower
	 */
	public int getLower() {
		return lower;
	}

	/**
	 * @return the upper
	 */
	public int getUpper() {
		return upper;
	}
}
