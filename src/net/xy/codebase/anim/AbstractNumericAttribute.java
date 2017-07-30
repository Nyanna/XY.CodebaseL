package net.xy.codebase.anim;

/**
 * for common field functionality
 *
 * @author Xyan
 *
 */
public abstract class AbstractNumericAttribute implements INumericAttribute {

	@Override
	public void add(final IActor actor, final double val, final IAnimationContext ac) {
		set(actor, get(actor, ac) + val, ac);
	}
}
