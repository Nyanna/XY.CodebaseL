package net.xy.codebase.mem;

public interface IObjectPool {

	public Byte getByte(byte readByte);

	public Short getShort(short readShort);

	public Integer getInteger(int readInt);

	public Long getLong(long readLong);

	public Float getFloat(float readFloat);

	public Double getDouble(double readDouble);

}
