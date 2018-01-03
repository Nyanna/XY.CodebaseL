package net.xy.codebase.exec.tasks;

/**
 * abstract implementation for an timeout runnable task
 *
 * @author Xyan
 *
 */
public abstract class TimeoutStopable extends TimeoutRunnable {
	/**
	 * true when stopped
	 */
	private boolean stoped = false;

	/**
	 * default
	 *
	 * @param timeoutMs
	 */
	public TimeoutStopable(final long timeoutMs) {
		super(timeoutMs);
	}

	@Override
	public long nextRun() {
		if (isStoped())
			return 0;
		return super.nextRun();
	}

	/**
	 * true when stoped
	 *
	 * @return
	 */
	public boolean isStoped() {
		return stoped;
	}

	/**
	 * set stoped state
	 *
	 * @param stoped
	 */
	public void setStoped(final boolean stoped) {
		this.stoped = stoped;
	}

	@Override
	public void run() {
		if (!isStoped())
			runInner();
	}

	/**
	 * called if not stoped
	 */
	protected abstract void runInner();
}
