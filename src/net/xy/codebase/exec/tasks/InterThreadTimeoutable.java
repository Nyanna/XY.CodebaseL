package net.xy.codebase.exec.tasks;

import net.xy.codebase.exec.IInterThreads;

/**
 * implementation which supports single run timeouts to the referenced
 * timeoutqueue
 *
 * @author Xyan
 *
 */
public class InterThreadTimeoutable<E extends Enum<E>> extends TimeoutRunnable implements ICoveredRunnable {
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
	public void run() {
		it.run(thread, run);
	}

	@Override
	public Runnable getRunnable() {
		return run;
	}
}