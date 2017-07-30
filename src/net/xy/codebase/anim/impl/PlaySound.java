package net.xy.codebase.anim.impl;

import net.xy.codebase.anim.IActor;
import net.xy.codebase.anim.IAnimationContext;

public class PlaySound extends PauseAnimation {
	/**
	 * first step toggle
	 */
	private boolean firstStep = false;

	/**
	 * default
	 *
	 * @param msDuration
	 */
	public PlaySound(final long msDuration) {
		super(msDuration);
	}

	@Override
	public void step(final IActor actor, final IAnimationContext ac) {
		if (!firstStep)
			firstStep = true;
		// XXX todo
		super.step(actor, ac);
	}
}
