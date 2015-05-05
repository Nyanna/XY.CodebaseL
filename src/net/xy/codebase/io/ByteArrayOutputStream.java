package net.xy.codebase.io;

public class ByteArrayOutputStream extends java.io.ByteArrayOutputStream {

	public byte[] getArray() {
		return buf;
	}

	@Override
	public int size() {
		return count;
	}
}
