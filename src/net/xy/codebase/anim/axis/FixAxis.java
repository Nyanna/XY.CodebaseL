package net.xy.codebase.anim.axis;

import net.xy.codebase.anim.IActor;
import net.xy.codebase.anim.IAnimationContext;

/**
 * is an fix value axis
 *
 * @author Xyan
 *
 */
public class FixAxis extends AbstractAxis {
	/**
	 * start value
	 */
	private final double start;
	/**
	 * end value
	 */
	private final double end;

	/**
	 * default
	 *
	 * @param start
	 * @param end
	 */
	public FixAxis(final double start, final double end) {
		this.start = start;
		this.end = end;
	}

	/**
	 * using 0 as start with repeating
	 *
	 * @param target
	 * @param repeating
	 */
	public FixAxis(final double target) {
		start = 0;
		end = target;
	}

	@Override
	public double getVal(final IActor actor, final IAnimationContext ac) {
		return end;
	}

	@Override
	public double getMin(final IActor actor, final IAnimationContext ac) {
		return start;
	}

	@Override
	public double getMax(final IActor actor, final IAnimationContext ac) {
		return end;
	}

	@Override
	public String toString() {
		return String.format("Axis [start=%s, end=%s]", start, end);
	}
}
