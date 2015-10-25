package net.xy.codebase.exec;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.xy.codebase.collection.TimeoutQueue.ITask;

/**
 * throttler to execute an runnable not more than every interval. ensures that
 * no run call would be missed.
 *
 * @author Xyan
 *
 */
public class ExecutionThrottler {
	private static final Logger LOG = LoggerFactory.getLogger(ExecutionThrottler.class);
	/**
	 * last update wish time or 0 in case of not running
	 */
	private final AtomicLong lastUpdate = new AtomicLong(0);
	/**
	 * target action to run
	 */
	private final ScheduleRunnable runnable;
	private final ThrottledRunnable capsule;
	/**
	 * intervall to run at
	 */
	private final int interval;
	/**
	 * last time the action was startet
	 */
	private long lastStart;

	/**
	 * with 0 intervall ensures only that not 2 runnable are scheduled at the
	 * same time
	 *
	 * @param runnable
	 */
	public ExecutionThrottler(final ScheduleRunnable runnable) {
		this(runnable, 0);
	}

	/**
	 * default
	 *
	 * @param runnable
	 * @param interval
	 */
	public ExecutionThrottler(final ScheduleRunnable runnable, final int interval) {
		this.runnable = runnable;
		this.interval = interval;
		capsule = new ThrottledRunnable();
	}

	/**
	 * start the action or place an update wish
	 */
	public void run() {
		while (true) {
			long runs = lastUpdate.get();
			if (runs != 0 && lastUpdate.compareAndSet(runs, ++runs)) {
				if (LOG.isTraceEnabled())
					LOG.trace("Request throttled run [" + interval + "][" + runnable + "]");
				return;
			} else if (lastUpdate.compareAndSet(0, 1)) {
				// start runnable
				if (interval > 0) {
					final long now = System.currentTimeMillis();
					final long nextStart = now + interval - (now - lastStart);
					capsule.setNextRun(Math.max(nextStart, now));
				}
				if (LOG.isTraceEnabled())
					LOG.trace("Start throttled [" + interval + "][" + runnable + "]");
				runnable.schedule(capsule);
				return;
			} else if (LOG.isTraceEnabled())
				LOG.trace("Inefficient loop repeat [" + interval + "][" + runnable + "]");
		}
	}

	/**
	 * encapsulation runnable
	 *
	 * @author Xyan
	 *
	 */
	public class ThrottledRunnable implements ITask {
		private long nextRun;

		@Override
		public void run() {
			final long wish = lastUpdate.get();
			final long now = System.currentTimeMillis();
			lastStart = now;
			if (LOG.isTraceEnabled())
				LOG.trace("Run throttled [" + interval + "][" + this + "]");
			runGuarded();
			if (lastUpdate.compareAndSet(wish, 0)) {
				if (LOG.isTraceEnabled())
					LOG.trace("Terminate throttled run [" + interval + "][" + this + "]");
				return;
			} else {
				// update in future or has changed
				if (interval > 0)
					nextRun = now + interval;
				if (LOG.isTraceEnabled())
					LOG.trace("Rerun throttled run [" + interval + "][" + this + "]");
				runnable.schedule(this);
			}
		}

		private void runGuarded() {
			try {
				runnable.run();
			} catch (final Exception e) {
				LOG.error("Error running throttled", e);
			}
		}

		@Override
		public boolean isRecurring() {
			return false;
		}

		public void setNextRun(final long nextRun) {
			this.nextRun = nextRun;
		}

		@Override
		public long nextRun() {
			return nextRun;
		}

		@Override
		public String toString() {
			return runnable.toString();
		}
	}

	/**
	 * interface for real action and scheduler
	 *
	 * @author Xyan
	 *
	 */
	public static interface ScheduleRunnable extends Runnable {
		/**
		 * action should now to schedule his own capsule
		 *
		 * @param run
		 * @param delay
		 */
		public void schedule(ITask run);
	}
}
