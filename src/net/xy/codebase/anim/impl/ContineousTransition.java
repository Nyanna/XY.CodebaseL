package net.xy.codebase.anim.impl;

import net.xy.codebase.Primitive;
import net.xy.codebase.anim.AbstractAnimation;
import net.xy.codebase.anim.IActor;
import net.xy.codebase.anim.IAnimationContext;
import net.xy.codebase.anim.INumericAttribute;
import net.xy.codebase.anim.axis.CurrentValueAxis;

/**
 * transition from one state to a next which can be shifted further anytime
 *
 * @author Xyan
 *
 */
public class ContineousTransition extends AbstractAnimation {
	/**
	 * field to affect
	 */
	private final INumericAttribute field;
	/**
	 * graph for calculation
	 */
	private final CurrentValueAxis valueAxis;
	private final long duration;
	private long lastStep;

	/**
	 * animation with duration and optional persistence
	 *
	 * @param field
	 * @param target
	 * @param durationMs
	 * @param persist
	 */
	public ContineousTransition(final INumericAttribute field, final long durationMs) {
		this.field = field;
		duration = durationMs;
		valueAxis = new CurrentValueAxis(field);
	}

	public void setTarget(final double target) {
		valueAxis.setTarget(target);
	}

	/**
	 * core method to calculate an animation step
	 */
	@Override
	public void step(final IActor actor, final IAnimationContext ac) {
		final long nnow = ac.getTime();
		if (lastStep == 0)
			lastStep = nnow;
		final long diff = nnow - lastStep;
		lastStep = nnow;
		final double fac = Math.max((double) diff / duration, 0.01d);

		final double max = valueAxis.getMax(actor, ac);
		final double min = valueAxis.getMin(actor, ac);
		final double range = max - min;

		final double interpolate;
		if (max < min)
			interpolate = Math.max(max, Math.min(min, min + range * fac));
		else
			interpolate = Math.max(min, Math.min(max, min + range * fac));

		final double oldVal = field.get(actor, ac);
		boolean changed = false;
		if (Double.compare(interpolate, Double.NaN) != 0 && //
				Double.compare(interpolate, Double.NEGATIVE_INFINITY) != 0 && //
				Double.compare(interpolate, Double.POSITIVE_INFINITY) != 0 && //
				!Primitive.equals(oldVal, interpolate))
			changed = true;
		else
			changed = false;
		if (changed)
			field.set(actor, interpolate, ac);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("ContineousTransition [%s][field=%s]", getId() != null ? getId() : "", field);
	}
}
