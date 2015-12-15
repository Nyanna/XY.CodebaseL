package net.xy.codebase.collection;

import java.io.Serializable;

public class BitSet implements Serializable {
	private static final long serialVersionUID = -3097711895125159466L;

	private byte[] array = null;

	public boolean get(final int idx) {
		final int field = idx / 8;
		final int sub = idx % 8;
		if (array != null && idx >= 0 && field < array.length)
			return (array[field] & 1 << sub) != 0;
		return false;
	}

	public void set(final int idx, final boolean value) {
		final int field = idx / 8;
		final int sub = idx % 8;

		if (array == null || array.length < field + 1) {
			final byte[] res = new byte[field + 1];
			if (array != null)
				System.arraycopy(array, 0, res, 0, array.length);
			array = res;
		}

		if (value)
			array[field] = (byte) (array[field] | 1 << sub);
		else
			array[field] = (byte) (array[field] & ~(1 << sub));
	}
}
