package net.xy.codebase.anim.impl;

import net.xy.codebase.Primitive;
import net.xy.codebase.anim.AbstractAnimation;
import net.xy.codebase.anim.IActor;
import net.xy.codebase.anim.IAnimationContext;
import net.xy.codebase.anim.IAxis;
import net.xy.codebase.anim.INumericAttribute;

/**
 * application of one axis output to the given field
 *
 * @author Xyan
 *
 */
public class AxisAnimation extends AbstractAnimation {
	/**
	 * affected field
	 */
	private final INumericAttribute field;
	/**
	 * axis for values
	 */
	private final IAxis axis;

	/**
	 * default
	 *
	 * @param field
	 * @param axis
	 */
	public AxisAnimation(final INumericAttribute field, final IAxis axis) {
		this.field = field;
		this.axis = axis;
	}

	@Override
	public void step(final IActor actor, final IAnimationContext ac) {
		final double interpolate = axis.getVal(actor, ac);
		final Double oldVal = field.get(actor, ac);
		final boolean changed = oldVal == null
				|| Double.compare(interpolate, Double.NaN) != 0 && !Primitive.equals(oldVal.doubleValue(), interpolate);
		if (changed)
			field.set(actor, interpolate, ac);
	}
}
