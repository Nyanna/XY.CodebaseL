package net.xy.codebase.anim;

import net.xy.codebase.anim.impl.Transition;
import net.xy.codebase.collection.Array;

public abstract class AbstractAnimation implements IAnimation {
	/**
	 * animation chain, insert these animation on end of this animation
	 */
	protected Array<IAnimation> follow;
	/**
	 * id for references
	 */
	protected String id = null;
	/**
	 * name of this class for display only, think about abfuscation
	 */
	private final String className;

	public AbstractAnimation() {
		className = getClass().getSimpleName();
	}

	/**
	 * method that should be called on end to cast adjacent animations, removes
	 * also this animation
	 *
	 * @param actor
	 */
	protected void end(final IActor actor) {
		actor.removeAnimation(this);
		insertFollowups(actor);
	}

	/**
	 * method that should be called on end to cast adjacent animations, just
	 * start the followups
	 *
	 * @param actor
	 */
	protected void insertFollowups(final IActor actor) {
		if (follow != null)
			actor.addAnimations(follow.shrink());
	}

	/**
	 * add animations after this one ends
	 */
	@Override
	public IAnimation after(final IAnimation... after) {
		if (follow == null)
			follow = new Array<IAnimation>(IAnimation.class, 10);
		for (final IAnimation anim : after)
			follow.addChecked(anim);
		return this;
	}

	/**
	 * add an animation after this one ends
	 */
	@Override
	public IAnimation after(final IAnimation anim) {
		if (follow == null)
			follow = new Array<IAnimation>(IAnimation.class, 10);
		follow.addChecked(anim);
		return anim;
	}

	@Override
	public IAnimation after(final INumericAttribute field, final double start, final double target,
			final long duration) {
		return after(new Transition(field, start, target, duration));
	}

	@Override
	public IAnimation after(final INumericAttribute field, final double target, final long duration) {
		return after(new Transition(field, target, duration));
	}

	@Override
	public AbstractAnimation clone() throws CloneNotSupportedException {
		return (AbstractAnimation) super.clone();
	}

	@Override
	public IAnimation setId(final String id) {
		this.id = id;
		return this;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String toString() {
		return String.format("%s [%s]", className, id);
	}
}
