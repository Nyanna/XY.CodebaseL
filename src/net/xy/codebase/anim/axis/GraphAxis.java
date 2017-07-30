package net.xy.codebase.anim.axis;

import net.xy.codebase.anim.Graph;
import net.xy.codebase.anim.IActor;
import net.xy.codebase.anim.IAnimationContext;
import net.xy.codebase.anim.IAxis;
import net.xy.codebase.anim.IFunction;

/**
 * wraps another graph as axis
 * 
 * @author Xyan
 *
 */
public class GraphAxis extends Graph implements IAxis {
	/**
	 * default
	 *
	 * @param x
	 * @param y
	 * @param func
	 */
	public GraphAxis(final IAxis x, final IAxis y, final IFunction func) {
		super(x, y, func);
	}

	@Override
	public double getVal(final IActor actor, final IAnimationContext ac) {
		return value(actor, ac);
	}

	@Override
	public double getMin(final IActor actor, final IAnimationContext ac) {
		return Y.getMin(actor, ac);
	}

	@Override
	public double getMax(final IActor actor, final IAnimationContext ac) {
		return Y.getMax(actor, ac);
	}

	@Override
	public GraphAxis clone() throws CloneNotSupportedException {
		return (GraphAxis) super.clone();
	}

}
