package net.xy.codebase.anim.impl;

import net.xy.codebase.Primitive;
import net.xy.codebase.anim.AbstractAnimation;
import net.xy.codebase.anim.Graph;
import net.xy.codebase.anim.IActor;
import net.xy.codebase.anim.IAnimationContext;
import net.xy.codebase.anim.IAxis;
import net.xy.codebase.anim.IFunction;
import net.xy.codebase.anim.ILeadAxis;
import net.xy.codebase.anim.INumericAttribute;
import net.xy.codebase.anim.axis.FieldAxis;
import net.xy.codebase.anim.axis.FixAxis;
import net.xy.codebase.anim.axis.TimeAxis;
import net.xy.codebase.anim.math.Function;

/**
 * transition animation from one state to another in time
 *
 * @author Xyan
 *
 */
public class Transition extends AbstractAnimation {
	/**
	 * field to affect
	 */
	private final INumericAttribute field;
	/**
	 * graph for calculation
	 */
	private Graph graph;
	/**
	 * incremental mode
	 */
	private boolean incremental;

	/**
	 * instantly set to new value on step
	 *
	 * @param field
	 * @param target
	 */
	public Transition(final INumericAttribute field, final double target) {
		this.field = field;
		graph = new Graph(null, new FixAxis(target), Function.NONE);
	}

	/**
	 * animation with duration and optional persistence
	 *
	 * @param field
	 * @param target
	 * @param duration
	 * @param persist
	 */
	public Transition(final INumericAttribute field, final double target, final long duration, final boolean persist) {
		this.field = field;
		graph = new Graph(new TimeAxis(duration, false, persist), new FieldAxis(field, target), Function.LINEAR);
	}

	/**
	 * animation with known start a duration, non persisting
	 *
	 * @param field
	 * @param start
	 * @param target
	 * @param duration
	 */
	public Transition(final INumericAttribute field, final double start, final double target, final long duration) {
		this(field, start, target, duration, false);
	}

	/**
	 * animation with known start, a duration and optional persistence
	 *
	 * @param field
	 * @param start
	 * @param target
	 * @param duration
	 * @param persist
	 */
	public Transition(final INumericAttribute field, final double start, final double target, final long duration,
			final boolean persist) {
		this.field = field;
		graph = new Graph(new TimeAxis(duration, false, persist), new FixAxis(start, target), Function.LINEAR);
	}

	/**
	 * simple duration animation
	 *
	 * @param field
	 * @param target
	 * @param duration
	 */
	public Transition(final INumericAttribute field, final double target, final long duration) {
		this(field, target, duration, false);
	}

	/**
	 * short for target color animation
	 *
	 * @param field
	 * @param col
	 * @param duration
	 */
	public Transition(final INumericAttribute field, final int col, final long duration) {
		this(field, new TimeAxis(duration, false, false), new FieldAxis(field, col), Function.LINEAR_ARGB);
	}

	/**
	 * custom function graph without Y axis
	 *
	 * @param field
	 * @param duration
	 * @param func
	 */
	public Transition(final INumericAttribute field, final long duration, final IFunction func) {
		this(field, new TimeAxis(duration, false, false), null, func);
	}

	/**
	 * animation with custom graph
	 *
	 * @param field
	 * @param graph
	 */
	public Transition(final INumericAttribute field, final Graph graph) {
		this.field = field;
		this.graph = graph;
	}

	/**
	 * animation with implizit custom graph
	 *
	 * @param field
	 * @param x
	 * @param y
	 * @param func
	 */
	public Transition(final INumericAttribute field, final ILeadAxis x, final IAxis y, final IFunction func) {
		this(field, new Graph(x, y, func));
	}

	/**
	 * for stateless paradigms use inkremental mode this will use Field.add
	 * instead of Field.set
	 *
	 * @param incremental
	 * @return
	 */
	public Transition setIncremental(final boolean incremental) {
		this.incremental = incremental;
		return this;
	}

	/**
	 * core method to calculate an animation step
	 */
	@Override
	public void step(final IActor actor, final IAnimationContext ac) {
		final double interpolate = graph.value(actor, ac);
		if (incremental)
			field.add(actor, interpolate, ac);
		else {
			final double oldVal = field.get(actor, ac);
			final boolean changed = Double.compare(interpolate, Double.NaN) != 0
					&& !Primitive.equals(oldVal, interpolate);
			if (changed)
				field.set(actor, interpolate, ac);
		}
		if (graph.isEnd(actor, ac))
			end(actor);
	}

	@Override
	public Transition clone() throws CloneNotSupportedException {
		final Transition clone = (Transition) super.clone();
		if (graph != null)
			clone.graph = graph.clone();
		return clone;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("Transition [%s][field=%s, graph=%s]", getId() != null ? getId() : "", field, graph);
	}
}
