package net.xy.codebase.io;

public class ByteArrayInputStream extends java.io.ByteArrayInputStream {

	public ByteArrayInputStream() {
		super(new byte[0]);
	}

	public void setValues(final byte[] buf, final int offset, final int length) {
		this.buf = buf;
		pos = offset;
		count = Math.min(offset + length, buf.length);
		mark = offset;
	}
}
