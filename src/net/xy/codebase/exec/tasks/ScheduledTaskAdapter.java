package net.xy.codebase.exec.tasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * convenience container for runnables, mainly for lamda syntax
 *
 * @author Xyan
 *
 */
public class ScheduledTaskAdapter extends ScheduledTask implements ICoveredRunnable {
	private static final Logger LOG = LoggerFactory.getLogger(ScheduledTaskAdapter.class);
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
		if (LOG.isTraceEnabled())
			LOG.trace("Running sync " + this);
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
		return String.format("ScheduledTaskAdapter [%s]", toStringSuper());
	}

	@Override
	protected String toStringSuper() {
		return String.format("%s,r=%s", super.toStringSuper(), run);
	}
}