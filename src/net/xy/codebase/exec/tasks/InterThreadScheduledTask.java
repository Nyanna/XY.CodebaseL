package net.xy.codebase.exec.tasks;

import net.xy.codebase.exec.IInterThreads;

public class InterThreadScheduledTask<E extends Enum<E>> extends ScheduledTaskAdapter {
	/**
	 * target thread
	 */
	private final E thread;
	/**
	 * back reference
	 */
	private IInterThreads<E> it;

	/**
	 * default
	 *
	 * @param thread
	 * @param timeoutMs
	 * @param run
	 */
	public InterThreadScheduledTask(final E thread, final long timeoutMs, final Runnable run,
			final IInterThreads<E> it) {
		this(thread, timeoutMs, 0, run, it);
	}

	public InterThreadScheduledTask(final E thread, final long timeoutMs, final long delay, final Runnable run) {
		this(thread, timeoutMs, delay, run, null);
	}

	public InterThreadScheduledTask(final E thread, final long timeoutMs, final long delay, final Runnable run,
			final IInterThreads<E> it) {
		super(delay, timeoutMs, run);
		this.thread = thread;
		this.it = it;
	}

	public void setInter(final IInterThreads<E> it) {
		this.it = it;
	}

	@Override
	protected final void innerRun() {
		it.run(thread, getRunnable());
	}

	@Override
	public String toString() {
		return String.format("InterThreadScheduledTask [t=%s][%s]", thread, super.toStringSuper());
	}
}
