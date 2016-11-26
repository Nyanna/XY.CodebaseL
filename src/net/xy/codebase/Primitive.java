package net.xy.codebase;

public class Primitive {

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
}
