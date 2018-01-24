package util;

public class SimpleMath {
    /**
     * Get the dual logarithm of a given value.
     * This method checks if the resulting value of this operation is
     * 0 and returns 1 instead. Usable every time it is required that
     * the resulting with of a wire is greater 0.
     *
     * @param val
     * 		The value to compute the logarithm from.
     * @return
     * 		>= 1 for values {@param val} greater or equal to 1
     * 		-1 for value {@param val} being smaller 1
     */
    public static int checkedLog(int val) {
        if (val < 1) {
            System.err.printf("Ill formed composition, tried to calculate log_2(%d).\n", val);
            return -1;
        }

        // check for possible resulting 0
        if (val == 1)
            return 1;

        // calculate the logarithm if val is > 1
        return (int) Math.ceil(Math.log(val) / Math.log(2));
    }
}
