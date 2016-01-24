package net.xy.codebase.exec.tasks;

/**
 * abstract implementation of an interthread runnable container supporting
 * execution throttling
 *
 * @author Xyan
 *
 */
public abstract class AbstractInterThreadRunnable<E extends Enum<E>> implements IScheduleRunnable {
	/**
	 * real runnable
	 */
	protected final Runnable run;
	/**
	 * target thread
	 */
	protected final E thread;

	/**
	 * default
	 *
	 * @param thread
	 * @param run
	 */
	public AbstractInterThreadRunnable(final E thread, final Runnable run) {
		this.run = run;
		this.thread = thread;
	}

	@Override
	public void run() {
		run.run();
	}
}