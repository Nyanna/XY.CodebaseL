package net.xy.codebase.anim;

import net.xy.codebase.anim.impl.Transition;

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

	public Transition addTransition(final INumericAttribute field, final double start, final double target,
			final long duration);

	public Transition addTransition(final INumericAttribute field, final double target, final long duration);

	public IActor clearAnimations();

	public void step(IAnimationContext ac);

	public void removeAnimation(String id);

	public boolean containsAnimation(String id);

	public IAnimation getAnimation(String id);
}
