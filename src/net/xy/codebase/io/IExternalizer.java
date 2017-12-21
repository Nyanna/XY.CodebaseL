package net.xy.codebase.io;

import net.xy.codebase.io.SerializationContext.Decoder;
import net.xy.codebase.io.SerializationContext.Encoder;

public interface IExternalizer<CL> {

	public void encode(CL obj, Encoder enc);

	public CL decode(Decoder dec);
}
