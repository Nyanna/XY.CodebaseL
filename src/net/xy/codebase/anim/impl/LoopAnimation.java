package net.xy.codebase.anim.impl;

import net.xy.codebase.anim.AbstractAnimation;
import net.xy.codebase.anim.IActor;
import net.xy.codebase.anim.IAnimation;
import net.xy.codebase.anim.IAnimationContext;

/**
 * animation implementation which consecutivly adds animations to an actor in
 * its intervall
 *
 * @author Xyan
 *
 */
public class LoopAnimation extends AbstractAnimation {

	/**
	 * duration for pause
	 */
	private final long intervalDuration;
	/**
	 * maximum time the animation set will looped, null for endless
	 */
	private final Long maxDuration;
	/**
	 * lazy starting point
	 */
	private Long startTime;
	/**
	 * intervall counter
	 */
	private long intervallCount = -1;
	/**
	 * set of animations to loop
	 */
	private final IAnimation[] anims;

	/**
	 * default, endless
	 *
	 * @param intervalDuration
	 * @param anims
	 *            animationset to loop
	 */
	public LoopAnimation(final long intervalDuration, final IAnimation[] anims) {
		this(intervalDuration, null, anims);
	}

	/**
	 * default with max duration
	 *
	 * @param intervalDuration
	 * @param maxDuration
	 * @param anims
	 *            animationset to loop
	 */
	public LoopAnimation(final long intervalDuration, final Long maxDuration, final IAnimation[] anims) {
		this.intervalDuration = intervalDuration;
		this.maxDuration = maxDuration != null ? maxDuration : null;
		this.anims = anims;
	}

	@Override
	public void step(final IActor actor, final IAnimationContext ac) {
		if (startTime == null)
			startTime = ac.getTime();

		final int count = (int) Math.floor((ac.getTime() - startTime) / intervalDuration);
		if (count > intervallCount) {
			intervallCount = count;
			addAnimations(actor);
		}

		if (maxDuration != null && ac.getTime() - startTime >= maxDuration)
			end(actor);
	}

	/**
	 * inserts the naimation set to the bound actor
	 *
	 * @param actor
	 */
	private void addAnimations(final IActor actor) {
		actor.addAnimations(anims);
	}
}
