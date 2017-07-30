package net.xy.codebase.anim.impl;

import net.xy.codebase.anim.AbstractAnimation;
import net.xy.codebase.anim.IActor;
import net.xy.codebase.anim.IAnimationContext;

public class PauseAnimation extends AbstractAnimation {

	/**
	 * duration for pause
	 */
	private final long duration;
	/**
	 * lazy starting point
	 */
	private Long startTime;

	public PauseAnimation(final long msDuration) {
		duration = msDuration;
	}

	@Override
	public void step(final IActor actor, final IAnimationContext ac) {
		if (startTime == null)
			startTime = ac.getTime();

		if (ac.getTime() - startTime >= duration)
			end(actor);
	}

	@Override
	public String toString() {
		return String.format("Pause [%s]", duration);
	}
}
