package net.xy.codebase.anim.impl;

import java.util.concurrent.atomic.AtomicBoolean;

import net.xy.codebase.anim.AbstractAnimation;
import net.xy.codebase.anim.IActor;
import net.xy.codebase.anim.IAnimationContext;

public abstract class ExecuteAnimation extends AbstractAnimation {

	/**
	 * enable double run optimization/protection
	 */
	private final boolean runOnce;
	/**
	 * if the code got executed
	 */
	private final AtomicBoolean wasRun;

	/**
	 * default, must be subclassed
	 */
	public ExecuteAnimation() {
		this(true);
	}

	/**
	 * default with enabling of optimization
	 *
	 * @param runonce
	 */
	public ExecuteAnimation(final boolean runonce) {
		runOnce = runonce;
		wasRun = new AtomicBoolean(false);
	}

	@Override
	public void step(final IActor actor, final IAnimationContext ac) {
		if (!runOnce || wasRun.compareAndSet(false, true))
			run();
		end(actor);
	}

	/**
	 * inner run of custom code to execute
	 */
	protected abstract void run();

	@Override
	public AbstractAnimation clone() throws CloneNotSupportedException {
		return this;
	}
}
