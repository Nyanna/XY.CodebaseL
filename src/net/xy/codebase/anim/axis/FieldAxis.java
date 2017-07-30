package net.xy.codebase.anim.axis;

import net.xy.codebase.anim.IActor;
import net.xy.codebase.anim.IAnimationContext;
import net.xy.codebase.anim.INumericAttribute;

public class FieldAxis extends AbstractAxis {

	/**
	 * lazy retrieved starting value
	 */
	private Double start;
	/**
	 * target value
	 */
	private final Double end;
	/**
	 * field to handle
	 */
	private final INumericAttribute field;

	/**
	 * default
	 *
	 * @param field
	 * @param end
	 */
	public FieldAxis(final INumericAttribute field, final double end) {
		this.end = end;
		this.field = field;
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
		initStart(actor, ac);
		return start;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see base.extension.tmpl4stage.animation.IAxis#getMax()
	 */
	@Override
	public double getMax(final IActor actor, final IAnimationContext ac) {
		return end;
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
		return String.format("LazyAxis [start=%s, end=%s]", start, end);
	}
}
