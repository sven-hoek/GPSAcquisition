package functions;

public class HashCalculator {
	
	public static int hashCode(int v1, int v2) {
		return (v1 << 5) ^ ((v1 & 0xf8000000) >> 27) ^ v2;
	}

	public static int hashCode(long v1, long v2) {
		long vv = (v1 << 5) ^ ((v1 & 0xf800000000000000L) >> 61) ^ v2;
		return (int) (vv >> 32) ^ (int) (0xffffffff00000000L & vv);
	}

}
