package net.xy.codebase.anim;

public interface ILeadAxis extends IAxis {
	/**
	 * whether the time is over
	 *
	 * @return
	 */
	public boolean isEnd(IActor actor, final IAnimationContext ac);
}
