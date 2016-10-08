package net.xy.codebase.io;

/**
 * common bitstorage and access
 *
 * @author Xyan
 *
 */
public class BitStore {
	/**
	 * backed bitstream
	 */
	private final byte[] bits;
	/**
	 * amount of bits stored in stream
	 */
	private final int length;

	/**
	 * default
	 *
	 * @param bits
	 */
	public BitStore(final byte[] bits) {
		this.bits = bits;
		length = bits.length * 8;
	}

	/**
	 * throws error
	 *
	 * @param idx
	 */
	private void checkIdx(final int idx) {
		if (idx > getLength() || idx < 0)
			throw new IllegalArgumentException("Index to retrieve is out of range [" + idx + "]");
	}

	/**
	 * get up to 8 bits in an byte at index
	 *
	 * @param amount
	 *            max 8
	 * @param idx
	 * @return
	 */
	public byte getBits(final int amount, int idx) {
		if (amount > 8)
			throw new IllegalArgumentException("Method only supports retrieving of <= 8 bits");
		checkIdx(idx);
		byte res = 0;
		int toget = amount, had = 0;
		while (toget > 0 && idx < getLength()) {
			byte dat = bits[idx / 8];

			final int have = idx % 8;
			final int strip = toget < 8 - have ? 8 - have - toget : 0;

			dat = (byte) ((dat & 0xff) >>> strip << strip + have >>> had);
			res = (byte) (res ^ dat);

			final int got = 8 - have - strip;
			toget -= got;
			had += got;
			idx += got;

			if (had >= 8 || toget == 0 || idx == getLength())
				had = 0;
		}
		final byte rightAligned = (byte) ((res & 0xff) >>> 8 - amount);
		return rightAligned;
	}

	/**
	 * gets any amount of bits in an bytearray at index, creates array on demand
	 *
	 * @param amount
	 * @return
	 */
	public byte[] getBitsArray(final int amount, final int idx) {
		final byte[] res = new byte[amount / 8 + 1];
		getBits(res, amount, idx);
		return res;
	}

	/**
	 * gets any amount of bytes from index
	 *
	 * @param res
	 * @param amount
	 * @param idx
	 * @return
	 */
	public void getBits(final byte[] res, final int amount, int idx) {
		checkIdx(idx);

		byte buf = 0;
		int toget = amount, had = 0, residx = 0;
		while (toget > 0 && idx < getLength()) {
			byte dat = bits[idx / 8];

			final int have = idx % 8;
			final int strip = toget < 8 - have ? 8 - have - toget : 0;

			dat = (byte) ((dat & 0xff) >>> strip << strip + have >>> had);
			buf = (byte) (buf ^ dat);

			final int got = 8 - have - strip;
			toget -= got;
			had += got;
			idx += got;

			if (had >= 8 || toget == 0 || idx == getLength()) {
				res[residx] = buf;
				buf = 0;
				had = 0;
				residx++;
			}
		}
	}

	/**
	 * @return the total bitcount of the unterlaying array
	 */
	public int getLength() {
		return length;
	}

	/**
	 * writes length bits of the right aligned byte 0011 -> appended to the
	 * stream as 1100 => 11 with an length of 2
	 *
	 * @param bite
	 * @param length
	 * @param idx
	 */
	public void setBitsRight(final byte bite, final int length, final int idx) {
		setBitsLeft((byte) (bite << 8 - length), length, idx);
	}

	/**
	 * sets length bits of the given left aligned byte at the index, 1100 with
	 * length of 2 will be written as 11.
	 *
	 * @param bite
	 * @param length
	 * @param idx
	 */
	public void setBitsLeft(final byte bite, final int length, int idx) {
		checkIdx(idx);
		int left = length > 8 ? 8 : length;
		int copy = 0;

		while (left > 0 && idx < getLength()) {
			byte dat = bits[idx / 8];

			final int off = idx % 8;
			final int strip = length < 8 - off ? 8 - off - length : 0;

			if (dat != 0) {
				final int maskLeft = 0xff >>> strip + off;
				final int maskRight = maskLeft << strip + copy;
				final int invert = maskRight ^ 0xff;
				dat = (byte) (dat & invert);
			}
			final int disLeft = (bite & 0xff) >>> strip + off;
			final int disRight = disLeft << strip + copy;
			final byte val = (byte) disRight;
			dat = (byte) (dat ^ val);

			bits[idx / 8] = dat;

			final int copied = 8 - off - strip - copy;
			left -= copied;
			copy += copied;
			idx += copied;
		}
	}

	/**
	 * prints an binary string
	 *
	 * @param bite
	 *
	 * @return
	 */
	public static String toString(final byte bite) {
		final char[] res = new char[8];
		for (int idx = 0; idx <= 7; idx++) {
			final byte shift = (byte) ((bite & 0xff) >>> 7 - idx & 1);
			res[idx] = shift == 1 ? '1' : '0';
		}
		return String.valueOf(res);
	}
}