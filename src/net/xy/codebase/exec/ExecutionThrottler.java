package net.xy.codebase.exec;

import java.util.concurrent.atomic.AtomicLong;

/**
 * throttler to execute an runnable not more than every interval. ensures that
 * no run call would be missed.
 *
 * @author Xyan
 *
 */
public class ExecutionThrottler {
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
			final long runs = lastUpdate.get();
			final long now = System.currentTimeMillis();
			if (runs != 0 && lastUpdate.compareAndSet(runs, now))
				return;
			else if (lastUpdate.compareAndSet(0, now)) {
				// start runnable
				if (interval > 0) {
					final long nextStart = interval - (now - lastStart);
					runnable.schedule(capsule, (int) Math.max(nextStart, 0));
				} else
					runnable.schedule(capsule, 0);
				return;
			}
		}
	}

	/**
	 * encapsulation runnable
	 *
	 * @author Xyan
	 *
	 */
	public class ThrottledRunnable implements Runnable {
		@Override
		public void run() {
			final long now = System.currentTimeMillis();
			lastStart = now;
			runnable.run();
			final long wish = lastUpdate.get();
			if (wish < now && lastUpdate.compareAndSet(wish, 0))
				return;
			else
				// update in future or has changed
				runnable.schedule(this, interval);
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
		public void schedule(Runnable run, int delay);
	}
}
