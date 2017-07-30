package net.xy.codebase.anim;

public abstract class AbstractNumericROAttribute extends AbstractNumericAttribute {

	@Override
	public void set(final IActor actor, final double val, final IAnimationContext ac) {
		throw new UnsupportedOperationException("Readonly attribute");
	}
}
