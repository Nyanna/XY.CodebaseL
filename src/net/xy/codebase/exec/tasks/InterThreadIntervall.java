package net.xy.codebase.exec.tasks;

import net.xy.codebase.exec.IInterThreads;

/**
 * interthread intervall
 *
 * @author Xyan
 *
 */
public class InterThreadIntervall<E extends Enum<E>> extends RecurringTask {
	/**
	 * target thread
	 */
	private final E thread;
	/**
	 * real runnable
	 */
	protected Runnable run;
	/**
	 * back reference
	 */
	private final IInterThreads<E> it;

	/**
	 * default
	 *
	 * @param thread
	 * @param intervall
	 * @param run
	 * @param it
	 */
	public InterThreadIntervall(final E thread, final int intervall, final Runnable run, final IInterThreads<E> it) {
		this(thread, intervall, 0, run, it);
	}

	/**
	 * default, with timed first run
	 *
	 * @param thread
	 * @param intervall
	 * @param startIn
	 *            first start time
	 * @param run
	 * @param it
	 */
	public InterThreadIntervall(final E thread, final int intervall, final int startIn, final Runnable run,
			final IInterThreads<E> it) {
		super(intervall);
		this.run = run;
		this.thread = thread;
		this.it = it;
	}

	/**
	 * for inner access, for overwritten innerun
	 *
	 * @param thread
	 * @param intervall
	 * @param it
	 */
	protected InterThreadIntervall(final E thread, final int intervall, final IInterThreads<E> it) {
		this(thread, intervall, 0, null, it);
	}

	/**
	 * for inner access, for overwritten innerun, starttime
	 *
	 * @param thread
	 * @param intervall
	 * @param it
	 */
	protected InterThreadIntervall(final E thread, final int intervall, final int startIn, final IInterThreads<E> it) {
		this(thread, intervall, startIn, null, it);
	}

	@Override
	protected void innerRun() {
		it.put(thread, run);
	}

	@Override
	public String toString() {
		return "RTCapsule " + run.toString();
	}
}