package net.xy.codebase.anim.axis;

import net.xy.codebase.anim.IActor;
import net.xy.codebase.anim.IAnimationContext;
import net.xy.codebase.anim.IAxis;

/**
 * adds an fixed value to an delegate axis
 * 
 * @author Xyan
 *
 */
public class OffsetAxis extends AbstractAxis {
	/**
	 * delegate axis
	 */
	private final IAxis axis;
	/**
	 * offset to add or substract
	 */
	private final double offset;

	/**
	 * default
	 *
	 * @param axis
	 * @param offset
	 */
	public OffsetAxis(final IAxis axis, final double offset) {
		this.axis = axis;
		this.offset = offset;
	}

	@Override
	public double getVal(final IActor actor, final IAnimationContext ac) {
		return axis.getVal(actor, ac) + offset;
	}

	@Override
	public double getMin(final IActor actor, final IAnimationContext ac) {
		return axis.getMin(actor, ac) + offset;
	}

	@Override
	public double getMax(final IActor actor, final IAnimationContext ac) {
		return axis.getMax(actor, ac) + offset;
	}
}
