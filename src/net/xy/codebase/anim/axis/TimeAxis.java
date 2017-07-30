package net.xy.codebase.anim.axis;

import net.xy.codebase.anim.IActor;
import net.xy.codebase.anim.IAnimationContext;
import net.xy.codebase.anim.ILeadAxis;

/**
 * ongoing context time axis ending after duration
 *
 * @author Xyan
 *
 */
public class TimeAxis extends AbstractAxis implements ILeadAxis {
	/**
	 * duration of this axis
	 */
	private final long duration;
	/**
	 * lazy inited starting time
	 */
	private long startTime = 0;
	/**
	 * devider to slow down time progress
	 */
	private long devider = 0;
	/**
	 * whether the animation should loop
	 */
	private final boolean repeating;
	/**
	 * whether this timeline ends or still stops at is end
	 */
	private final boolean persist;

	/**
	 * default, time input runs forever
	 */
	public TimeAxis() {
		this(Long.MAX_VALUE, false, false);
	}

	/**
	 * simple default
	 *
	 * @param msDuration
	 */
	public TimeAxis(final long msDuration) {
		this(msDuration, false, false);
	}

	/**
	 * default, now as starttime
	 *
	 * @param duration
	 * @param repeating
	 * @param persist
	 */
	public TimeAxis(final long duration, final boolean repeating, final boolean persist) {
		this(0, duration, repeating, persist);
	}

	/**
	 * default, most args
	 *
	 * @param startTime
	 * @param duration
	 * @param repeating
	 * @param persist
	 */
	public TimeAxis(final long startTime, final long duration, final boolean repeating, final boolean persist) {
		if (duration <= 0)
			throw new IllegalArgumentException("Duration has to greater than 0");
		this.startTime = startTime;
		this.duration = duration;
		this.repeating = repeating;
		this.persist = persist;
	}

	public TimeAxis setDevider(final long devider) {
		this.devider = devider;
		return this;
	}

	@Override
	public double getVal(final IActor actor, final IAnimationContext ac) {
		final long nanoTime = ac.getTime();
		if (startTime == 0)
			startTime = nanoTime;
		double startoff = nanoTime - startTime;
		if (devider != 0l)
			startoff = startoff / devider;
		if (repeating)
			startoff = startoff % duration;
		return Math.max(Math.min(startoff, duration), 0l);
	}

	public void reset() {
		startTime = 0;
	}

	@Override
	public double getMin(final IActor actor, final IAnimationContext ac) {
		return 0;
	}

	@Override
	public double getMax(final IActor actor, final IAnimationContext ac) {
		return duration;
	}

	@Override
	public boolean isEnd(final IActor actor, final IAnimationContext ac) {
		return !persist && getVal(actor, ac) == getMax(actor, ac);
	}

	@Override
	public String toString() {
		return String.format("TimeAxis [%s][%s][%s]", startTime, duration, repeating);
	}
}
