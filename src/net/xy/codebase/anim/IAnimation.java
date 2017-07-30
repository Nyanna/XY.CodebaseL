package net.xy.codebase.anim;

/**
 * every attribute dynamic is an animation
 * 
 * @author Xyan
 *
 */
public interface IAnimation extends Cloneable {
	/**
	 * add animations after this one ends
	 */
	public IAnimation after(final IAnimation... afterEnd);

	/**
	 * add an animation after this one ends
	 */
	public IAnimation after(final IAnimation anim);

	/**
	 * convenience for after add transitions
	 *
	 * @param field
	 * @param start
	 * @param target
	 * @param duration
	 * @return
	 */
	public IAnimation after(final INumericAttribute field, final double start, final double target,
			final long duration);

	/**
	 * convenience for after add transitions
	 *
	 * @param field
	 * @param target
	 * @param duration
	 * @return
	 */
	public IAnimation after(final INumericAttribute field, final double target, final long duration);

	/**
	 * cloning support
	 *
	 * @return
	 * @throws CloneNotSupportedException
	 */
	public IAnimation clone() throws CloneNotSupportedException;

	/**
	 * alterate an actor and context to its actual step
	 *
	 * @param actor
	 * @param ac
	 */
	public void step(final IActor actor, final IAnimationContext ac);

	/**
	 * sets an id for this animation
	 *
	 * @param id
	 * @return self for chaining
	 */
	public IAnimation setId(String id);

	/**
	 * @return optional id or null
	 */
	public String getId();
}
