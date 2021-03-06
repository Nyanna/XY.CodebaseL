package net.xy.codebase.anim.axis;

import net.xy.codebase.anim.IActor;
import net.xy.codebase.anim.IAnimationContext;
import net.xy.codebase.anim.INumericAttribute;

/**
 * uses an field as axis input and aligns min and max on current value
 *
 * @author Xyan
 *
 */
public class FieldRangeAxis extends AbstractAxis {
	/**
	 * numeric field to rely on
	 */
	private final INumericAttribute field;
	/**
	 * minimum axis value
	 */
	private final double minVal;
	/**
	 * maximum axis value
	 */
	private final double maxVal;

	/**
	 * default
	 *
	 * @param field
	 * @param minVal
	 * @param maxVal
	 */
	public FieldRangeAxis(final INumericAttribute field, final double minVal, final double maxVal) {
		this.field = field;
		this.minVal = minVal;
		this.maxVal = maxVal;
	}

	@Override
	public double getVal(final IActor actor, final IAnimationContext ac) {
		return field.get(actor, ac);
	}

	@Override
	public double getMin(final IActor actor, final IAnimationContext ac) {
		return getVal(actor, ac) + minVal;
	}

	@Override
	public double getMax(final IActor actor, final IAnimationContext ac) {
		return getVal(actor, ac) + maxVal;
	}
}
