package net.xy.codebase.anim;

public interface IFunction {
	/**
	 * interpolates
	 *
	 * @param actor
	 * @param x
	 * @param y
	 * @return
	 */
	public double getVal(IActor actor, final IAnimationContext ac, IAxis x, IAxis y);
}
