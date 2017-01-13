package net.xy.codebase.io;

import net.xy.codebase.io.SerializationContext.Decoder;
import net.xy.codebase.io.SerializationContext.Encoder;

/**
 * interface for custom serializable objects
 *
 * @author Xyan
 *
 */
public interface Externalize<T> {
	public void encode(Encoder enc);

	public T decode(Decoder dec);
}