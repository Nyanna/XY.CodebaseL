package net.xy.codebase.thread;

/**
 * convenience container for runnables, mainly for lamda syntax
 *
 * @author Xyan
 *
 */
public class RecurringTaskCapsule extends RecurringTask {
	/**
	 * encapsulated runnables
	 */
	private final Runnable run;

	/**
	 * default
	 * 
	 * @param intervallMs
	 * @param run
	 */
	public RecurringTaskCapsule(final int intervallMs, final Runnable run) {
		super(intervallMs);
		this.run = run;
	}

	@Override
	protected void innerRun() {
		run.run();
	}
}