package net.xy.codebase.anim.axis;

import net.xy.codebase.anim.IActor;
import net.xy.codebase.anim.IAnimationContext;
import net.xy.codebase.anim.IAxis;
import net.xy.codebase.anim.INumericAttribute;

/**
 * stores initial field value and adds delegate axis output
 *
 * @author Xyan
 *
 */
public class InitialFieldAddAxis extends AbstractAxis {
	/**
	 * field to handle
	 */
	private final INumericAttribute field;
	/**
	 * lazy retrieved starting value
	 */
	private Double start;
	/**
	 * axis to add output of
	 */
	private final IAxis axis;

	/**
	 * default
	 *
	 * @param field
	 * @param minVal
	 * @param maxVal
	 */
	public InitialFieldAddAxis(final INumericAttribute field, final IAxis axis) {
		this.field = field;
		this.axis = axis;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see base.extension.tmpl4stage.animation.IAxis#getValue()
	 */
	@Override
	public double getVal(final IActor actor, final IAnimationContext ac) {
		initStart(actor, ac);
		return start + axis.getVal(actor, ac);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see base.extension.tmpl4stage.animation.IAxis#getMin()
	 */
	@Override
	public double getMin(final IActor actor, final IAnimationContext ac) {
		initStart(actor, ac);
		return start + axis.getMin(actor, ac);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see base.extension.tmpl4stage.animation.IAxis#getMax()
	 */
	@Override
	public double getMax(final IActor actor, final IAnimationContext ac) {
		initStart(actor, ac);
		return start + axis.getMax(actor, ac);
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
