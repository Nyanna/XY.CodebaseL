package net.xy.codebase.exec;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.xy.codebase.exec.tasks.IScheduleRunnable;
import net.xy.codebase.exec.tasks.ITask;

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
	private final IScheduleRunnable runnable;
	private final ThrottledRunnable capsule;
	/**
	 * intervall to run at in ms
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
	public ExecutionThrottler(final IScheduleRunnable runnable) {
		this(runnable, 0);
	}

	/**
	 * default
	 *
	 * @param runnable
	 * @param interval
	 *            in ms
	 */
	public ExecutionThrottler(final IScheduleRunnable runnable, final int interval) {
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
					LOG.trace("Request throttled run [" + interval + "][" + runnable + "]["
							+ runnable.getClass().getSimpleName() + "]");
				return;
			} else if (lastUpdate.compareAndSet(0, 1)) {
				// start runnable
				if (interval > 0) {
					final long now = System.nanoTime();
					final long nextStart = now + TimeUnit.MILLISECONDS.toNanos(interval) - (now - lastStart);
					capsule.setNextRun(Math.max(nextStart, now));
					// capsule.setNextRun(nextStart > now ? nextStart : 0L);
				}
				if (LOG.isTraceEnabled())
					LOG.trace("Start throttled [" + interval + "][" + runnable + "]["
							+ runnable.getClass().getSimpleName() + "]");
				runnable.schedule(capsule);
				return;
			} else if (LOG.isTraceEnabled())
				LOG.trace("Inefficient loop repeat [" + interval + "][" + runnable + "]["
						+ runnable.getClass().getSimpleName() + "]");
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
			final long now = System.nanoTime();
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
					nextRun = now + TimeUnit.MILLISECONDS.toNanos(interval);
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
			return "ThrottledRunnable: " + runnable.toString();
		}
	}
}
