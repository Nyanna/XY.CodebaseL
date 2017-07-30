package net.xy.codebase.anim;

public interface IAxis extends Cloneable {
	/**
	 * @return current value from axis
	 */
	public double getVal(IActor actor, final IAnimationContext ac);

	/**
	 *
	 * @return min value from axis
	 */
	public double getMin(IActor actor, final IAnimationContext ac);

	/**
	 *
	 * @return max value from axis
	 */
	public double getMax(IActor actor, final IAnimationContext ac);

	/**
	 * @return
	 * @throws CloneNotSupportedException
	 */
	public IAxis clone() throws CloneNotSupportedException;
}
