package net.xy.codebase.anim.axis;

import net.xy.codebase.anim.IActor;
import net.xy.codebase.anim.IAnimationContext;
import net.xy.codebase.anim.INumericAttribute;

public class CurrentValueAxis extends AbstractAxis {
	/**
	 * target value
	 */
	private Double end;
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
	public CurrentValueAxis(final INumericAttribute field, final double end) {
		this.end = end;
		this.field = field;
	}

	/**
	 * default
	 *
	 * @param field
	 * @param end
	 */
	public CurrentValueAxis(final INumericAttribute field) {
		end = Double.NaN;
		this.field = field;
	}

	public void setTarget(final double end) {
		this.end = end;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see base.extension.tmpl4stage.animation.IAxis#getValue()
	 */
	@Override
	public double getVal(final IActor actor, final IAnimationContext ac) {
		return field.get(actor, ac);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see base.extension.tmpl4stage.animation.IAxis#getMin()
	 */
	@Override
	public double getMin(final IActor actor, final IAnimationContext ac) {
		return field.get(actor, ac);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see base.extension.tmpl4stage.animation.IAxis#getMax()
	 */
	@Override
	public double getMax(final IActor actor, final IAnimationContext ac) {
		if (Double.isNaN(end))
			return field.get(actor, ac);
		return end;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("CurrentValueAxis [end=%s]", end);
	}
}
