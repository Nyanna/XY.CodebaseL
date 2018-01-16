package net.xy.codebase.exec.tasks;

import net.xy.codebase.exec.IInterThreads;

/**
 * implementation which supports single run timeouts to the referenced
 * timeoutqueue
 *
 * @author Xyan
 *
 */
public class InterThreadTimeoutable<E extends Enum<E>> extends TimeoutStopable implements ICoveredRunnable {
	/**
	 * target thread
	 */
	private final E thread;
	/**
	 * real runnable
	 */
	private final Runnable run;
	/**
	 * back reference
	 */
	private final IInterThreads<E> it;

	/**
	 * default
	 *
	 * @param thread
	 * @param timeoutMs
	 * @param run
	 */
	public InterThreadTimeoutable(final E thread, final long timeoutMs, final Runnable run, final IInterThreads<E> it) {
		super(timeoutMs);
		this.run = run;
		this.thread = thread;
		this.it = it;
	}

	@Override
	protected void runInner() {
		it.run(thread, getRunnable());
	}

	@Override
	public Runnable getRunnable() {
		return run;
	}

	@Override
	public String toString() {
		return "InterThreadTimeoutable: " + getRunnable().toString() + "," + thread;
	}
}