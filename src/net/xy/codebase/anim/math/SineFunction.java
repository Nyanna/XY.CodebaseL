package net.xy.codebase.anim.math;

import net.xy.codebase.anim.IActor;
import net.xy.codebase.anim.IAnimationContext;
import net.xy.codebase.anim.IAxis;
import net.xy.codebase.anim.IFunction;

public class SineFunction implements IFunction {
	private final int intervallMs;
	private final float shift;

	public SineFunction(final int intervallMs) {
		this(intervallMs, 0f);
	}

	public SineFunction(final int intervallMs, final float shift) {
		this.intervallMs = intervallMs;
		this.shift = shift;
	}

	@Override
	public double getVal(final IActor a, final IAnimationContext ac, final IAxis x, final IAxis y) {
		final double time = x.getVal(a, ac);
		final double p = Math.sin(Math.PI * 2d * (time % intervallMs / intervallMs) + Math.PI * shift);
		return y.getMin(a, ac) + y.getMax(a, ac) * p;
	}
}
