package net.xy.codebase.anim;

/**
 * everything that can be animated is a actor
 * 
 * @author Xyan
 *
 */
public interface IActor {

	public void removeAnimation(IAnimation anim);

	public void addAnimations(IAnimation... anims);

	public IAnimation addAnimation(IAnimation anim);

	public IAnimation addTransition(final INumericAttribute field, final double start, final double target,
			final long duration);

	public IAnimation addTransition(final INumericAttribute field, final double target, final long duration);

	public IActor clearAnimations();

	public void step(IAnimationContext ac);

	public void removeAnimation(String id);

	public boolean containsAnimation(String id);

	public IAnimation getAnimation(String id);
}
