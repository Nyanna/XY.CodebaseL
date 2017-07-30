package net.xy.codebase.anim;

/**
 * every actor has a set of numeric attributes to animate. each attribute must
 * know how to get resolved and set.
 *
 * @author Xyan
 *
 */
public interface INumericAttribute {
	/**
	 * gets the attributes current numeric value
	 *
	 * @param actor
	 * @param ac
	 * @return
	 */
	public double get(IActor actor, IAnimationContext ac);

	/**
	 * sets this attribute current numeric value
	 *
	 * @param actor
	 * @param val
	 * @param ac
	 */
	public void set(IActor actor, double val, IAnimationContext ac);

	/**
	 * adds an increment to the current value of thsi attribute
	 *
	 * @param actor
	 * @param val
	 * @param ac
	 */
	public void add(IActor actor, double val, IAnimationContext ac);
}
