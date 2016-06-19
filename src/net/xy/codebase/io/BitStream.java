package net.xy.codebase.io;

/**
 * stream for writing and reading bitwise
 *
 * @author Xyan
 *
 */
public class BitStream extends BitStore {
	/**
	 * current index pointer for reading and writing
	 */
	private int idx;

	/**
	 * default
	 *
	 * @param bits
	 */
	public BitStream(final byte[] bits) {
		super(bits);
		idx = 0;
	}

	/**
	 * get up to 8 bits in an byte
	 *
	 * @param amount
	 *            max 8
	 * @return
	 */
	public byte getBits(final int amount) {
		return getBits(amount, idx);
	}

	/**
	 * reads bits and speps index further
	 *
	 * @param amount
	 * @return
	 */
	public byte readBits(final int amount) {
		final byte res = getBits(amount, idx);
		idx += amount;
		return res;
	}

	/**
	 * gets any amount of bits in an bytearray
	 *
	 * @param amount
	 * @return
	 */
	public byte[] getBitsArray(final int amount) {
		return getBitsArray(amount, idx);
	}

	/**
	 * reads in any amount of bits and steps further, creates array
	 * 
	 * @param amount
	 * @return
	 */
	public byte[] readBitsArray(final int amount) {
		final byte[] res = getBitsArray(amount, idx);
		idx += amount;
		return res;
	}

	/**
	 * resets the counter
	 */
	public void reset() {
		idx = 0;
	}

	/**
	 * writes length bits of the given left aligned byte 10100000 -> with an
	 * size of 4 will append 1010
	 *
	 * @param bite
	 * @param length
	 */
	public void write(final byte bite, final int length) {
		setBitsLeft(bite, length, idx);
		idx += length;
	}
}
