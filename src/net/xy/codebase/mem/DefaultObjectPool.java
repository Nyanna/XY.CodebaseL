package net.xy.codebase.mem;

public class DefaultObjectPool implements IObjectPool {
	@Override
	public Byte getByte(final byte readByte) {
		return Byte.valueOf(readByte);
	}

	@Override
	public Short getShort(final short readShort) {
		return Short.valueOf(readShort);
	}

	@Override
	public Integer getInteger(final int readInt) {
		return Integer.valueOf(readInt);
	}

	@Override
	public Long getLong(final long readLong) {
		return Long.valueOf(readLong);
	}

	@Override
	public Float getFloat(final float readFloat) {
		return Float.valueOf(readFloat);
	}

	@Override
	public Double getDouble(final double readDouble) {
		return Double.valueOf(readDouble);
	}
}
