package net.xy.codebase.anim;

import net.xy.codebase.anim.math.Function;

public class Graph {
	/**
	 * X Axis
	 */
	protected IAxis X; // any
	protected ILeadAxis LX; // maybe time
	/**
	 * Y Axis
	 */
	protected IAxis Y;// maybe value
	/**
	 * used function to apply
	 */
	protected final IFunction func;

	/**
	 * with NONE function
	 *
	 * @param x
	 * @param y
	 */
	public Graph(final IAxis x, final IAxis y) {
		this(x, y, Function.NONE);
	}

	/**
	 * default
	 *
	 * @param x
	 * @param y
	 * @param func
	 */
	public Graph(final IAxis x, final IAxis y, final IFunction func) {
		X = x;
		Y = y;
		this.func = func;
	}

	/**
	 * with NONE function
	 *
	 * @param x
	 * @param y
	 */
	public Graph(final ILeadAxis x, final IAxis y) {
		this(x, y, Function.NONE);
	}

	/**
	 * default
	 *
	 * @param x
	 * @param y
	 * @param func
	 */
	public Graph(final ILeadAxis x, final IAxis y, final IFunction func) {
		X = x;
		LX = x;
		Y = y;
		this.func = func;
	}

	public double value(final IActor actor, final IAnimationContext ac) {
		return func.getVal(actor, ac, X, Y);
	}

	/**
	 * @return the x
	 */
	public IAxis getX() {
		return X;
	}

	/**
	 * @return the y
	 */
	public IAxis getY() {
		return Y;
	}

	/**
	 * @return the func
	 */
	public IFunction getFunc() {
		return func;
	}

	/**
	 * @param actor
	 * @return if the graph is non repeating and has reached his final value
	 */
	public boolean isEnd(final IActor actor, final IAnimationContext ac) {
		return LX == null || LX.isEnd(actor, ac);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("Graph [X=%s, Y=%s, func=%s]", X, Y, func);
	}

	@Override
	public Graph clone() throws CloneNotSupportedException {
		final Graph clone = (Graph) super.clone();
		if (clone.X != null)
			clone.X = X.clone();
		if (clone.Y != null)
			clone.Y = Y.clone();
		return clone;
	}
}
