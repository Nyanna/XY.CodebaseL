package net.xy.codebase.exec.tasks;

/**
 * convenience container for runnables, mainly for lamda syntax
 *
 * @author Xyan
 *
 */
public class ScheduledTaskAdapter extends ScheduledTask implements ICoveredRunnable {
	/**
	 * encapsulated runnables
	 */
	private Runnable run;

	/**
	 * default
	 *
	 * @param intervallMs
	 * @param run
	 */
	public ScheduledTaskAdapter(final long intervallMs, final long delayMs, final Runnable run) {
		super(intervallMs, delayMs);
		this.run = run;
	}

	@Override
	protected void innerRun() {
		run.run();
	}

	protected void setRunnable(final Runnable run) {
		this.run = run;
	}

	@Override
	public Runnable getRunnable() {
		return run;
	}

	@Override
	public String toString() {
		return String.format("ScheduledTaskAdapter %s", super.toStringSuper());
	}

	@Override
	protected String toStringSuper() {
		return String.format("[%s]%s", run, super.toStringSuper());
	}
}