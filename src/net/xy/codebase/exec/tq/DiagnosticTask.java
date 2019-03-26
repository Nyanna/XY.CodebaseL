package net.xy.codebase.exec.tq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.xy.codebase.exec.tasks.ITask;
import net.xy.codebase.exec.tasks.ScheduledTask;

/**
 * diagnostic task to observer a timeoutqueue
 *
 * @author Xyan
 *
 */
public class DiagnosticTask extends ScheduledTask implements IQueueObserver {
	private static final Logger LOG = LoggerFactory.getLogger(TimeoutQueue.class);
	/**
	 * time of last run
	 */
	private long lastRun = System.currentTimeMillis();
	/**
	 * observed queue
	 */
	private final TimeoutQueue queue;
	/**
	 * amount of executed tasks
	 */
	private int execCount = 0;
	private int addedCount = 0;

	DiagnosticTask(final long intervallMs, final TimeoutQueue queue) {
		super(intervallMs);
		this.queue = queue;
	}

	@Override
	public void innerRun() {
		final long now = System.currentTimeMillis();
		if (LOG.isDebugEnabled()) {
			LOG.debug("TQueue size [" + queue.size() + "][exec=" + execCount + "][added=" + addedCount + "]["
					+ queue.getName() + "]");

			final long deltaT = now - lastRun;
			final float tps = (float) execCount / deltaT * 1000;
			LOG.debug("TQueue avr [" + tps + "][exec=" + execCount + "][added=" + addedCount + "][delta=" + deltaT
					+ "][" + queue.getName() + "]");
		}
		execCount = 0;
		addedCount = 0;
		lastRun = now;
	}

	@Override
	public void taskAdded(final ITask t) {
		addedCount++;
	}

	@Override
	public void taskStarted(final ITask t, final long latency) {
		execCount++;
	}

	@Override
	public void taskStoped(final ITask t) {
	}

	@Override
	public void queueExited() {
	}

	@Override
	public String toString() {
		return String.format("DiagnosticTask [%s]", toStringSuper());
	}
}