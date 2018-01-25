package java.lang;

import java.util.Random;

public final class Math {

	private Math () {
	}

	private static Random rand;

	public static final double E = 2.718281828459045;

	public static final double PI = 3.141592653589793;

	public static int abs (int i) {
		return (i < 0) ? -i : i;
	}

	public static long abs (long l) {
		return (l < 0) ? -l : l;
	}

	public static float abs (float f) {
		return (f <= 0) ? 0 - f : f;
	}

	public static double abs (double d) {
		return (d <= 0) ? 0 - d : d;
	}

	public static int min (int a, int b) {
		return (a < b) ? a : b;
	}

	public static long min (long a, long b) {
		return (a < b) ? a : b;
	}

	public static float min (float a, float b) {
		// this check for NaN, from JLS 15.21.1, saves a method call
		if (a != a)
			return a;
		// no need to check if b is NaN; < will work correctly
		// recall that -0.0 == 0.0, but [+-]0.0 - [+-]0.0 behaves special
		if (a == 0 && b == 0)
			return -(-a - b);
		return (a < b) ? a : b;
	}

	public static double min (double a, double b) {
		// this check for NaN, from JLS 15.21.1, saves a method call
		if (a != a)
			return a;
		// no need to check if b is NaN; < will work correctly
		// recall that -0.0 == 0.0, but [+-]0.0 - [+-]0.0 behaves special
		if (a == 0 && b == 0)
			return -(-a - b);
		return (a < b) ? a : b;
	}

	public static int max (int a, int b) {
		return (a > b) ? a : b;
	}

	public static long max (long a, long b) {
		return (a > b) ? a : b;
	}

	public static float max (float a, float b) {
		// this check for NaN, from JLS 15.21.1, saves a method call
		if (a != a)
			return a;
		// no need to check if b is NaN; > will work correctly
		// recall that -0.0 == 0.0, but [+-]0.0 - [+-]0.0 behaves special
		if (a == 0 && b == 0)
			return a - -b;
		return (a > b) ? a : b;
	}

	public static double max (double a, double b) {
		// this check for NaN, from JLS 15.21.1, saves a method call
		if (a != a)
			return a;
		// no need to check if b is NaN; > will work correctly
		// recall that -0.0 == 0.0, but [+-]0.0 - [+-]0.0 behaves special
		if (a == 0 && b == 0)
			return a - -b;
		return (a > b) ? a : b;
	}

	/**
	 * The trigonometric function <em>sin</em>. The sine of NaN or infinity is
	 * NaN, and the sine of 0 retains its sign. This is accurate within 1 ulp,
	 * and is semi-monotonic.
	 *
	 * @param a
	 *            the angle (in radians)
	 * @return sin(a)
	 */
	public static double sin (double a) {
		return StrictMath.sin (a);
	}

	/**
	 * The trigonometric function <em>cos</em>. The cosine of NaN or infinity is
	 * NaN. This is accurate within 1 ulp, and is semi-monotonic.
	 *
	 * @param a
	 *            the angle (in radians)
	 * @return cos(a)
	 */
	public static double cos (double a) {
		return StrictMath.cos (a);
	}

	/**
	 * The trigonometric function <em>tan</em>. The tangent of NaN or infinity
	 * is NaN, and the tangent of 0 retains its sign. This is accurate within 1
	 * ulp, and is semi-monotonic.
	 *
	 * @param a
	 *            the angle (in radians)
	 * @return tan(a)
	 */
	public static double tan (double a) {
		return StrictMath.tan (a);
	}

	/**
	 * The trigonometric function <em>arcsin</em>. The range of angles returned
	 * is -pi/2 to pi/2 radians (-90 to 90 degrees). If the argument is NaN or
	 * its absolute value is beyond 1, the result is NaN; and the arcsine of 0
	 * retains its sign. This is accurate within 1 ulp, and is semi-monotonic.
	 *
	 * @param a
	 *            the sin to turn back into an angle
	 * @return arcsin(a)
	 */
	public static double asin (double a) {
		return StrictMath.asin (a);
	}

	/**
	 * The trigonometric function <em>arccos</em>. The range of angles returned
	 * is 0 to pi radians (0 to 180 degrees). If the argument is NaN or its
	 * absolute value is beyond 1, the result is NaN. This is accurate within 1
	 * ulp, and is semi-monotonic.
	 *
	 * @param a
	 *            the cos to turn back into an angle
	 * @return arccos(a)
	 */
	public static double acos (double a) {
		return StrictMath.acos (a);
	}

	/**
	 * The trigonometric function <em>arcsin</em>. The range of angles returned
	 * is -pi/2 to pi/2 radians (-90 to 90 degrees). If the argument is NaN, the
	 * result is NaN; and the arctangent of 0 retains its sign. This is accurate
	 * within 1 ulp, and is semi-monotonic.
	 *
	 * @param a
	 *            the tan to turn back into an angle
	 * @return arcsin(a)
	 * @see #atan2(double, double)
	 */
	public static double atan (double a) {
		return StrictMath.atan (a);
	}

	/**
	 * A special version of the trigonometric function <em>arctan</em>, for
	 * converting rectangular coordinates <em>(x, y)</em> to polar
	 * <em>(r, theta)</em>. This computes the arctangent of x/y in the range of
	 * -pi to pi radians (-180 to 180 degrees). Special cases:
	 * <ul>
	 * <li>If either argument is NaN, the result is NaN.</li>
	 * <li>If the first argument is positive zero and the second argument is
	 * positive, or the first argument is positive and finite and the second
	 * argument is positive infinity, then the result is positive zero.</li>
	 * <li>If the first argument is negative zero and the second argument is
	 * positive, or the first argument is negative and finite and the second
	 * argument is positive infinity, then the result is negative zero.</li>
	 * <li>If the first argument is positive zero and the second argument is
	 * negative, or the first argument is positive and finite and the second
	 * argument is negative infinity, then the result is the double value
	 * closest to pi.</li>
	 * <li>If the first argument is negative zero and the second argument is
	 * negative, or the first argument is negative and finite and the second
	 * argument is negative infinity, then the result is the double value
	 * closest to -pi.</li>
	 * <li>If the first argument is positive and the second argument is positive
	 * zero or negative zero, or the first argument is positive infinity and the
	 * second argument is finite, then the result is the double value closest to
	 * pi/2.</li>
	 * <li>If the first argument is negative and the second argument is positive
	 * zero or negative zero, or the first argument is negative infinity and the
	 * second argument is finite, then the result is the double value closest to
	 * -pi/2.</li>
	 * <li>If both arguments are positive infinity, then the result is the
	 * double value closest to pi/4.</li>
	 * <li>If the first argument is positive infinity and the second argument is
	 * negative infinity, then the result is the double value closest to 3*pi/4.
	 * </li>
	 * <li>If the first argument is negative infinity and the second argument is
	 * positive infinity, then the result is the double value closest to -pi/4.</li>
	 * <li>If both arguments are negative infinity, then the result is the
	 * double value closest to -3*pi/4.</li>
	 *
	 * </ul>
	 * <p>
	 * This is accurate within 2 ulps, and is semi-monotonic. To get r, use
	 * sqrt(x*x+y*y).
	 *
	 * @param y
	 *            the y position
	 * @param x
	 *            the x position
	 * @return <em>theta</em> in the conversion of (x, y) to (r, theta)
	 * @see #atan(double)
	 */
	public static double atan2 (double y, double x) {
		return StrictMath.atan2 (y, x);
	}

	/**
	 * Take <em>e</em><sup>a</sup>. The opposite of <code>log()</code>. If the
	 * argument is NaN, the result is NaN; if the argument is positive infinity,
	 * the result is positive infinity; and if the argument is negative
	 * infinity, the result is positive zero. This is accurate within 1 ulp, and
	 * is semi-monotonic.
	 *
	 * @param a
	 *            the number to raise to the power
	 * @return the number raised to the power of <em>e</em>
	 * @see #log(double)
	 * @see #pow(double, double)
	 */
	public static double exp (double a) {
		return StrictMath.exp (a);
	}

	/**
	 * Take ln(a) (the natural log). The opposite of <code>exp()</code>. If the
	 * argument is NaN or negative, the result is NaN; if the argument is
	 * positive infinity, the result is positive infinity; and if the argument
	 * is either zero, the result is negative infinity. This is accurate within
	 * 1 ulp, and is semi-monotonic.
	 *
	 * <p>
	 * Note that the way to get log<sub>b</sub>(a) is to do this:
	 * <code>ln(a) / ln(b)</code>.
	 *
	 * @param a
	 *            the number to take the natural log of
	 * @return the natural log of <code>a</code>
	 * @see #exp(double)
	 */
	public static double log (double a) {
		return StrictMath.log (a);
	}

	/**
	 * Take a square root. If the argument is NaN or negative, the result is
	 * NaN; if the argument is positive infinity, the result is positive
	 * infinity; and if the result is either zero, the result is the same. This
	 * is accurate within the limits of doubles.
	 *
	 * <p>
	 * For a cube root, use <code>cbrt</code>. For other roots, use
	 * <code>pow(a, 1 / rootNumber)</code>.
	 * </p>
	 *
	 * @param a
	 *            the numeric argument
	 * @return the square root of the argument
	 * @see #cbrt(double)
	 * @see #pow(double, double)
	 */
	public static double sqrt (double a) {
		return StrictMath.sqrt (a);
	}

	/**
	 * Raise a number to a power. Special cases:
	 * <ul>
	 * <li>If the second argument is positive or negative zero, then the result
	 * is 1.0.</li>
	 * <li>If the second argument is 1.0, then the result is the same as the
	 * first argument.</li>
	 * <li>If the second argument is NaN, then the result is NaN.</li>
	 * <li>If the first argument is NaN and the second argument is nonzero, then
	 * the result is NaN.</li>
	 * <li>If the absolute value of the first argument is greater than 1 and the
	 * second argument is positive infinity, or the absolute value of the first
	 * argument is less than 1 and the second argument is negative infinity,
	 * then the result is positive infinity.</li>
	 * <li>If the absolute value of the first argument is greater than 1 and the
	 * second argument is negative infinity, or the absolute value of the first
	 * argument is less than 1 and the second argument is positive infinity,
	 * then the result is positive zero.</li>
	 * <li>If the absolute value of the first argument equals 1 and the second
	 * argument is infinite, then the result is NaN.</li>
	 * <li>If the first argument is positive zero and the second argument is
	 * greater than zero, or the first argument is positive infinity and the
	 * second argument is less than zero, then the result is positive zero.</li>
	 * <li>If the first argument is positive zero and the second argument is
	 * less than zero, or the first argument is positive infinity and the second
	 * argument is greater than zero, then the result is positive infinity.</li>
	 * <li>If the first argument is negative zero and the second argument is
	 * greater than zero but not a finite odd integer, or the first argument is
	 * negative infinity and the second argument is less than zero but not a
	 * finite odd integer, then the result is positive zero.</li>
	 * <li>If the first argument is negative zero and the second argument is a
	 * positive finite odd integer, or the first argument is negative infinity
	 * and the second argument is a negative finite odd integer, then the result
	 * is negative zero.</li>
	 * <li>If the first argument is negative zero and the second argument is
	 * less than zero but not a finite odd integer, or the first argument is
	 * negative infinity and the second argument is greater than zero but not a
	 * finite odd integer, then the result is positive infinity.</li>
	 * <li>If the first argument is negative zero and the second argument is a
	 * negative finite odd integer, or the first argument is negative infinity
	 * and the second argument is a positive finite odd integer, then the result
	 * is negative infinity.</li>
	 * <li>If the first argument is less than zero and the second argument is a
	 * finite even integer, then the result is equal to the result of raising
	 * the absolute value of the first argument to the power of the second
	 * argument.</li>
	 * <li>If the first argument is less than zero and the second argument is a
	 * finite odd integer, then the result is equal to the negative of the
	 * result of raising the absolute value of the first argument to the power
	 * of the second argument.</li>
	 * <li>If the first argument is finite and less than zero and the second
	 * argument is finite and not an integer, then the result is NaN.</li>
	 * <li>If both arguments are integers, then the result is exactly equal to
	 * the mathematical result of raising the first argument to the power of the
	 * second argument if that result can in fact be represented exactly as a
	 * double value.</li>
	 *
	 * </ul>
	 * <p>
	 * (In the foregoing descriptions, a floating-point value is considered to
	 * be an integer if and only if it is a fixed point of the method
	 * {@link #ceil(double)} or, equivalently, a fixed point of the method
	 * {@link #floor(double)}. A value is a fixed point of a one-argument method
	 * if and only if the result of applying the method to the value is equal to
	 * the value.) This is accurate within 1 ulp, and is semi-monotonic.
	 *
	 * @param a
	 *            the number to raise
	 * @param b
	 *            the power to raise it to
	 * @return a<sup>b</sup>
	 */
	public static double pow (double a, double b) {
		return StrictMath.pow (a, b);
	}

	/**
	 * Get the IEEE 754 floating point remainder on two numbers. This is the
	 * value of <code>x - y * <em>n</em></code>, where <em>n</em> is the closest
	 * double to <code>x / y</code> (ties go to the even n); for a zero
	 * remainder, the sign is that of <code>x</code>. If either argument is NaN,
	 * the first argument is infinite, or the second argument is zero, the
	 * result is NaN; if x is finite but y is infinite, the result is x. This is
	 * accurate within the limits of doubles.
	 *
	 * @param x
	 *            the dividend (the top half)
	 * @param y
	 *            the divisor (the bottom half)
	 * @return the IEEE 754-defined floating point remainder of x/y
	 * @see #rint(double)
	 */
	public static double IEEEremainder (double x, double y) {
		return StrictMath.IEEEremainder (x, y);
	}

	/**
	 * Take the nearest integer that is that is greater than or equal to the
	 * argument. If the argument is NaN, infinite, or zero, the result is the
	 * same; if the argument is between -1 and 0, the result is negative zero.
	 * Note that <code>Math.ceil(x) == -Math.floor(-x)</code>.
	 *
	 * @param a
	 *            the value to act upon
	 * @return the nearest integer &gt;= <code>a</code>
	 */
	public static double ceil (double a) {
		return StrictMath.ceil (a);
	}

	/**
	 * Take the nearest integer that is that is less than or equal to the
	 * argument. If the argument is NaN, infinite, or zero, the result is the
	 * same. Note that <code>Math.ceil(x) == -Math.floor(-x)</code>.
	 *
	 * @param a
	 *            the value to act upon
	 * @return the nearest integer &lt;= <code>a</code>
	 */
	public static double floor (double a) {
		return StrictMath.floor (a);
	}

	/**
	 * Take the nearest integer to the argument. If it is exactly between two
	 * integers, the even integer is taken. If the argument is NaN, infinite, or
	 * zero, the result is the same.
	 *
	 * @param a
	 *            the value to act upon
	 * @return the nearest integer to <code>a</code>
	 */
	public static double rint (double a) {
		return StrictMath.rint (a);
	}

	public static int round (float a) {
		// this check for NaN, from JLS 15.21.1, saves a method call
		if (a != a)
			return 0;
		return (int) floor (a + 0.5f);
	}

	public static long round (double a) {
		// this check for NaN, from JLS 15.21.1, saves a method call
		if (a != a)
			return 0;
		return (long) floor (a + 0.5d);
	}

	public static synchronized double random () {
		if (rand == null)
			rand = new Random ();
		return rand.nextDouble ();
	}

	public static double toRadians (double degrees) {
		return (degrees * PI) / 180;
	}

	public static double toDegrees (double rads) {
		return (rads * 180) / PI;
	}

}
