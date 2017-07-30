package net.xy.codebase.anim.axis;

import net.xy.codebase.anim.IActor;
import net.xy.codebase.anim.IAnimationContext;
import net.xy.codebase.anim.IAxis;
import net.xy.codebase.anim.ILeadAxis;

/**
 * time axis with decay effekt ond delegate axis output
 *
 * @author Xyan
 *
 */
public class DecayAxis extends AbstractAxis implements ILeadAxis {
	/**
	 * duration of this axis
	 */
	private final long duration;
	/**
	 * delegate axis
	 */
	private final IAxis axis;
	/**
	 * lazy inited starting time
	 */
	private final ILeadAxis timeAxis;

	/**
	 * simple default
	 *
	 * @param msDuration
	 */
	public DecayAxis(final IAxis axis, final long msDuration) {
		this(axis, msDuration, new TimeAxis(msDuration, false, false));
	}

	/**
	 * default most args
	 *
	 * @param axis
	 * @param duration
	 * @param timeAxis
	 */
	public DecayAxis(final IAxis axis, final long duration, final ILeadAxis timeAxis) {
		if (duration <= 0)
			throw new IllegalArgumentException("Duration has to greater than 0");
		this.axis = axis;
		this.duration = duration;
		this.timeAxis = timeAxis;
	}

	@Override
	public double getVal(final IActor actor, final IAnimationContext ac) {
		return axis.getVal(actor, ac) * getFac(actor, ac);
	}

	@Override
	public double getMin(final IActor actor, final IAnimationContext ac) {
		return axis.getMin(actor, ac) * getFac(actor, ac);
	}

	@Override
	public double getMax(final IActor actor, final IAnimationContext ac) {
		return axis.getMax(actor, ac) * getFac(actor, ac);
	}

	private double getFac(final IActor actor, final IAnimationContext ac) {
		final double passed = timeAxis.getVal(actor, ac);
		final double fac = 1d - passed / duration;
		return Math.max(Math.min(fac, 1d), 0d);
	}

	@Override
	public boolean isEnd(final IActor actor, final IAnimationContext ac) {
		return timeAxis.isEnd(actor, ac);
	}
}
