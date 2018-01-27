package net.xy.codebase.anim.axis;

import net.xy.codebase.anim.IActor;
import net.xy.codebase.anim.IAnimationContext;
import net.xy.codebase.anim.INumericAttribute;

/**
 * uses an field as axis input with optional caps and limits
 *
 * @author Xyan
 *
 */
public class CapedFieldRelativeAxis extends AbstractAxis {
	/**
	 * numeric field to rely on
	 */
	private final INumericAttribute field;
	/**
	 * optional lower bound, below that actual target value will be unchanged
	 */
	private double lowerBound = Double.NaN;
	/**
	 * optional upper bound, above that actual target value will be unchanged
	 */
	private double upperBound = Double.NaN;
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
	public CapedFieldRelativeAxis(final INumericAttribute field, final double minVal, final double maxVal) {
		this.field = field;
		this.minVal = minVal;
		this.maxVal = maxVal;
	}

	/**
	 * default
	 *
	 * @param field
	 * @param lowerBound
	 *            lower cap
	 * @param upperBoundupper
	 *            cap
	 * @param minVal
	 * @param maxVal
	 */
	public CapedFieldRelativeAxis(final INumericAttribute field, final double lowerBound, final double upperBound,
			final double minVal, final double maxVal) {
		this.field = field;
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
		this.minVal = minVal;
		this.maxVal = maxVal;
	}

	@Override
	public double getVal(final IActor actor, final IAnimationContext ac) {
		final double val = field.get(actor, ac);
		if (!Double.isNaN(upperBound) && val > upperBound)
			return Double.NaN;
		if (!Double.isNaN(lowerBound) && val < lowerBound)
			return Double.NaN;
		if (val < minVal)
			return minVal;
		if (val > maxVal)
			return maxVal;
		return val;
	}

	@Override
	public double getMin(final IActor actor, final IAnimationContext ac) {
		return minVal;
	}

	@Override
	public double getMax(final IActor actor, final IAnimationContext ac) {
		return maxVal;
	}
}
