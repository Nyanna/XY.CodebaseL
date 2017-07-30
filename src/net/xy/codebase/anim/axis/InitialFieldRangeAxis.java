package net.xy.codebase.anim.axis;

import net.xy.codebase.anim.IActor;
import net.xy.codebase.anim.IAnimationContext;
import net.xy.codebase.anim.INumericAttribute;

/**
 * stores initial field value and align min and max value on that
 * 
 * @author Xyan
 *
 */
public class InitialFieldRangeAxis extends AbstractAxis {
	/**
	 * field to handle
	 */
	private final INumericAttribute field;
	/**
	 * lazy retrieved starting value
	 */
	private Double start;
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
	public InitialFieldRangeAxis(final INumericAttribute field, final double minVal, final double maxVal) {
		this.field = field;
		this.minVal = minVal;
		this.maxVal = maxVal;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see base.extension.tmpl4stage.animation.IAxis#getValue()
	 */
	@Override
	public double getVal(final IActor actor, final IAnimationContext ac) {
		initStart(actor, ac);
		return start;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see base.extension.tmpl4stage.animation.IAxis#getMin()
	 */
	@Override
	public double getMin(final IActor actor, final IAnimationContext ac) {
		return getVal(actor, ac) + minVal;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see base.extension.tmpl4stage.animation.IAxis#getMax()
	 */
	@Override
	public double getMax(final IActor actor, final IAnimationContext ac) {
		return getVal(actor, ac) + maxVal;
	}

	/**
	 * inner initializer
	 *
	 * @param ac
	 */
	private void initStart(final IActor actor, final IAnimationContext ac) {
		if (start == null)
			start = field.get(actor, ac);
		if (start == null)
			start = 0d;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("InitialFieldRangeAxis [start=%s]", start);
	}
}
