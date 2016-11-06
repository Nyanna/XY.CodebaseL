package net.xy.codebase.exec.tasks;

import java.util.concurrent.TimeUnit;

/**
 * abstract implementation for an timeout runnable task
 *
 * @author Xyan
 *
 */
public abstract class TimeoutRunnable implements ITask {
	/**
	 * time on next and only run
	 */
	private long next;
	/**
	 * possible to reuse
	 */
	private volatile boolean recurring = false;

	/**
	 * default
	 *
	 * @param timeoutMs
	 */
	public TimeoutRunnable(final long timeoutMs) {
		calculateNext(timeoutMs);
	}

	/**
	 * calculate next execution
	 *
	 * @param timeoutMs
	 */
	protected void calculateNext(final long timeoutMs) {
		next = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutMs);
	}

	@Override
	public boolean isRecurring() {
		return recurring;
	}

	@Override
	public long nextRun() {
		return next;
	}
}
