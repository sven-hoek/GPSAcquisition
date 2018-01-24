package de.amidar;

public class T14 {

	/**
	 * Test proper loop detection for multiple goto -xx for one while loop
	 */
	public static void main(String[] args) {
		int[] erg = new int[10];
		int length = erg.length;
		int i = -1;
		while (i < length - 1) {
			i++;
			if (i % 2 == 1) {
				erg[i] = 1;
			} else {
				erg[i] = 2;
			}
//			 int dummy = 0; // Workaround: Uncomment this line and Test will
			// run successfully
		}

		for(int ii = 0; ii< 10; ii++){
			System.out.println(erg[ii]);
		}
//		SuccessPrinter sp = new SuccessPrinter(new char[] { 'T', '1', '4' });
//		sp.assertIntArray(erg, new int[] { 2, 1, 2, 1, 2, 1, 2, 1, 2, 1 });
//		sp.printResult();
	}

}
