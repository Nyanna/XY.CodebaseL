package net.xy.codebase.exec;

import java.util.concurrent.TimeUnit;

import net.xy.codebase.collection.TimeoutQueue.ITask;

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
	private final long next;

	/**
	 * default
	 *
	 * @param timeoutMs
	 */
	public TimeoutRunnable(final long timeoutMs) {
		next = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutMs);
	}

	@Override
	public boolean isRecurring() {
		return false;
	}

	@Override
	public long nextRun() {
		return next;
	}
}
