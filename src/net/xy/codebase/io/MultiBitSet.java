package net.xy.codebase.io;

/**
 * bitsetwise optimized datastructure with index access supporting multibitsets
 *
 * @author Xyan
 *
 */
public class MultiBitSet extends BitStore {
	/**
	 * amount of bits an value has in this set
	 */
	private final int valBits;

	/**
	 * default for simple bitsets
	 *
	 * @param amount
	 */
	public MultiBitSet(final int amount) {
		this(amount, 1);
	}

	/**
	 * default creates array
	 *
	 * @param amount
	 * @param valBits
	 *            amount of bits an value has
	 */
	public MultiBitSet(final int amount, final int valBits) {
		this(new byte[(amount * valBits + 7) / 8], valBits);
	}

	/**
	 * with given array
	 *
	 * @param bits
	 * @param valBits
	 *            amount of bits an value has
	 */
	public MultiBitSet(final byte[] bits, final int valBits) {
		super(bits);
		if (valBits == 8)
			throw new IllegalArgumentException("Makes no sense to use bitset with width of 8");
		this.valBits = valBits;
	}

	/**
	 * sets base bits at index
	 *
	 * @param idx
	 * @param val
	 */
	public void setVal(final int idx, final byte val) {
		if (val >= Math.pow(2, valBits))
			throw new IllegalArgumentException("Try to set value out of value size [" + val + "]");
		setBitsRight(val, valBits, idx * valBits);
	}

	/**
	 * retrieve value stored at index
	 *
	 * @param idx
	 * @return
	 */
	public byte getVal(final int idx) {
		return getBits(valBits, idx * valBits);
	}

	/**
	 * sets base bits at index, convenience method
	 *
	 * @param idx
	 * @param val
	 */
	public void setVal(final int idx, final int val) {
		if (val > Byte.MAX_VALUE || val < Byte.MIN_VALUE)
			throw new IllegalArgumentException("Used int is out of range");
		setVal(idx, (byte) val);
	}
}
