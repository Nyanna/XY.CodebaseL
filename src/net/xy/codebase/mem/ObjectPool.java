package net.xy.codebase.mem;

public class ObjectPool extends DefaultObjectPool {
	public static final ObjectPool INSTANCE = new ObjectPool();
	private Short[] shorts;

	public void setShortPoolLimit(final int limit) {
		final Short[] shorts = new Short[limit];
		for (int i = 0; i < shorts.length; i++)
			shorts[i] = (short) i;
		this.shorts = shorts;
	}

	@Override
	public Short getShort(final short readShort) {
		if (shorts != null && readShort >= 0 && readShort < shorts.length)
			return shorts[readShort];
		return super.getShort(readShort);
	}

	public static Byte byteO(final byte readByte) {
		return INSTANCE.getByte(readByte);
	}

	public static Short shortO(final short readShort) {
		return INSTANCE.getShort(readShort);
	}

	public static Integer intO(final int readInt) {
		return INSTANCE.getInteger(readInt);
	}

	public static Long longO(final long readLong) {
		return INSTANCE.getLong(readLong);
	}

	public static Float floatO(final float readFloat) {
		return INSTANCE.getFloat(readFloat);
	}

	public static Double doubleO(final double readDouble) {
		return INSTANCE.getDouble(readDouble);
	}
}
