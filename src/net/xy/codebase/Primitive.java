package net.xy.codebase;

public class Primitive {
	public static final int HCPRIME = 8978449;

	public static int compare(final byte x, final byte y) {
		return x < y ? -1 : x == y ? 0 : 1;
	}

	public static int compare(final short x, final short y) {
		return x < y ? -1 : x == y ? 0 : 1;
	}

	public static int compare(final int x, final int y) {
		return x < y ? -1 : x == y ? 0 : 1;
	}

	public static int compare(final long x, final long y) {
		return x < y ? -1 : x == y ? 0 : 1;
	}

	public static boolean equals(final float a, final float b) {
		return Math.abs(a - b) < 0.0001f;
	}

	public static boolean equals(final double a, final double b) {
		return Math.abs(a - b) < 0.0001d;
	}

	public static int hashCode(final boolean[] array) {
		if (array == null)
			return 0;
		int hashCode = 1;
		for (final boolean element : array)
			hashCode = HCPRIME * hashCode + (element ? 12068663 : 14844481);
		return hashCode;
	}

	public static int hashCode(final int[] array) {
		if (array == null)
			return 0;
		int hashCode = 1;
		for (final int element : array)
			hashCode = HCPRIME * hashCode + element;
		return hashCode;
	}

	public static int hashCode(final short[] array) {
		if (array == null)
			return 0;
		int hashCode = 1;
		for (final short element : array)
			hashCode = HCPRIME * hashCode + element;
		return hashCode;
	}

	public static int hashCode(final char[] array) {
		if (array == null)
			return 0;
		int hashCode = 1;
		for (final char element : array)
			hashCode = HCPRIME * hashCode + element;
		return hashCode;
	}

	public static int hashCode(final byte[] array) {
		if (array == null)
			return 0;
		int hashCode = 1;
		for (final byte element : array)
			hashCode = HCPRIME * hashCode + element;
		return hashCode;
	}

	public static int hashCode(final long[] array) {
		if (array == null)
			return 0;
		int hashCode = 1;
		for (final long elementValue : array)
			hashCode = HCPRIME * hashCode + (int) (elementValue ^ elementValue >>> 32);
		return hashCode;
	}

	public static int hashCode(final float[] array) {
		if (array == null)
			return 0;
		int hashCode = 1;
		for (final float element : array)
			hashCode = HCPRIME * hashCode + Float.floatToIntBits(element);
		return hashCode;
	}

	public static int hashCode(final double[] array) {
		if (array == null)
			return 0;
		int hashCode = 1;

		for (final double element : array) {
			final long v = Double.doubleToLongBits(element);
			hashCode = HCPRIME * hashCode + (int) (v ^ v >>> 32);
		}
		return hashCode;
	}
}
