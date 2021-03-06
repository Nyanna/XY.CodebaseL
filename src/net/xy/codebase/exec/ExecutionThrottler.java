package net.xy.codebase.exec;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.xy.codebase.collection.IPriority;
import net.xy.codebase.exec.tasks.ICoveredRunnable;
import net.xy.codebase.exec.tasks.ITask;
import net.xy.codebase.exec.tasks.InterThreadScheduledTask;

/**
 * throttler to execute an runnable not more than every interval. ensures that
 * no run call would be missed. you have to call start() after creation.
 *
 * @author Xyan
 *
 */
public class ExecutionThrottler<E extends Enum<E>> {
	private static final Logger LOG = LoggerFactory.getLogger(ExecutionThrottler.class);
	/**
	 * last update wish time or 0 in case of not running
	 */
	private final AtomicInteger lastUpdate = new AtomicInteger();
	/**
	 * throttling capsule
	 */
	private final ThrottledRunnable capsule;
	private final ThrottledScheduler scheduler;
	// in nanos
	private final long intervalNs;
	/**
	 * last time the action was startet
	 */
	private long lastStart;
	/**
	 * for stopping throttler
	 */
	private boolean enabled = true;
	/**
	 * executor reference
	 */
	private final InterThreads<E> inter;
	/**
	 * in this executor stripe
	 */
	private final E target;

	/**
	 * with 0 intervall ensures only that not 2 runnable are scheduled at the same
	 * time
	 *
	 * @param runnable
	 */
	public ExecutionThrottler(final Runnable runnable, final InterThreads<E> inter, final E target) {
		this(runnable, 0, inter, target);
	}

	/**
	 * default
	 *
	 * @param runnable
	 * @param interval in ms
	 */
	@SuppressWarnings("unchecked")
	public <T extends ITask & IPriority> ExecutionThrottler(final Runnable runnable, final int interval,
			final InterThreads<E> inter, final E target) {
		this.inter = inter;
		this.target = target;
		intervalNs = TimeUnit.MILLISECONDS.toNanos(interval);
		if (runnable instanceof IPriority)
			capsule = new PriorityThrottledRunnable((T) runnable, this);
		else
			capsule = new ThrottledRunnable(runnable, this);
		scheduler = new ThrottledScheduler(inter);
	}

	/**
	 * for stopping or desabling throttler
	 *
	 * @param enabled
	 */
	public void setEnabled(final boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * start the action or place an update wish
	 */
	public void run() {
		while (enabled) {
			final int runs = lastUpdate.get();
			if (runs != 0 && lastUpdate.compareAndSet(runs, runs + 1)) {
				if (LOG.isTraceEnabled())
					LOG.trace("Request throttled run [" + TimeUnit.NANOSECONDS.toMillis(intervalNs) + "][" + capsule
							+ "]");
				return;
			} else if (lastUpdate.compareAndSet(0, 1)) {
				// start runnable
				if (intervalNs > 0) {
					final long now = System.nanoTime();
					final long nextStart = lastStart + intervalNs;
					scheduler.setNextTime(nextStart > now ? nextStart : 0L);
				}
				if (LOG.isTraceEnabled())
					LOG.trace("Start throttled [" + TimeUnit.NANOSECONDS.toMillis(intervalNs) + "][" + capsule + "]");
				if (!planThrottler()) {
					lastUpdate.set(0);
					LOG.error("Failed to schedule throttle, reseting");
				}
				return;
			} else if (LOG.isTraceEnabled())
				LOG.trace(
						"Inefficient loop repeat [" + TimeUnit.NANOSECONDS.toMillis(intervalNs) + "][" + capsule + "]");
		}
	}

	private boolean planThrottler() {
		if (intervalNs > 0) {
			if (LOG.isTraceEnabled())
				LOG.trace("Schedule queued [" + this + "]");
			return inter.start(scheduler);
		} else {
			if (LOG.isTraceEnabled())
				LOG.trace("Schedule directly [" + this + "]");
			return inter.run(target, capsule);
		}
	}

	@Override
	public String toString() {
		return String.format("ExecutionThrottler [%s,%s,%s,%s]", TimeUnit.NANOSECONDS.toMillis(intervalNs), enabled,
				hashCode(), capsule);
	}

	/**
	 * fixed container for scheduling throttler
	 *
	 * @author Xyan
	 *
	 */
	public class ThrottledScheduler extends InterThreadScheduledTask {
		public ThrottledScheduler(final InterThreads<E> inter) {
			super(target, 0, 0, capsule, inter);
		}

		@Override
		public long nextRun() {
			return !enabled ? 0 : super.nextRun();
		}
	}

	/**
	 * encapsulation runnable
	 *
	 * @author Xyan
	 *
	 */
	public static class ThrottledRunnable implements Runnable, ICoveredRunnable {
		/**
		 * target action to run
		 */
		private final Runnable runnable;
		/**
		 * backreference to throttler
		 */
		private final ExecutionThrottler<?> throttler;

		/**
		 * default
		 *
		 * @param runnable
		 */
		public ThrottledRunnable(final Runnable runnable, final ExecutionThrottler<?> throttler) {
			this.runnable = runnable;
			this.throttler = throttler;
		}

		@Override
		public void run() {
			final AtomicInteger lastUpdate = throttler.lastUpdate;
			final int wish = lastUpdate.get();
			final long now = System.nanoTime();
			final long intervalNs = throttler.intervalNs;

			throttler.lastStart = now;
			if (LOG.isTraceEnabled())
				LOG.trace("Run throttled [" + TimeUnit.NANOSECONDS.toMillis(intervalNs) + "][" + this + "]");
			runGuarded();
			if (lastUpdate.compareAndSet(wish, 0)) {
				if (LOG.isTraceEnabled())
					LOG.trace("Terminate throttled run [" + TimeUnit.NANOSECONDS.toMillis(intervalNs) + "][" + this
							+ "]");
				return;
			} else {
				// update in future or has changed
				if (intervalNs > 0)
					throttler.scheduler.setNextTime(now + intervalNs);
				if (LOG.isTraceEnabled())
					LOG.trace("Rerun throttled run [" + TimeUnit.NANOSECONDS.toMillis(intervalNs) + "][" + this + "]");
				if (!throttler.planThrottler())
					LOG.error("Error rescheduling throttler [" + this + "]");
			}
		}

		private void runGuarded() {
			try {
				if (throttler.enabled)
					runnable.run();
			} catch (final Exception e) {
				LOG.error("Error running throttled", e);
			}
		}

		@Override
		public Runnable getRunnable() {
			return runnable;
		}

		@Override
		public String toString() {
			return String.format("ThrottledRunnable [%s]", runnable);
		}
	}

	/**
	 * encapsulation with priority support
	 *
	 * @author Xyan
	 *
	 */
	public static class PriorityThrottledRunnable extends ThrottledRunnable implements IPriority {
		/**
		 * target action to run
		 */
		private final IPriority runnable;

		public <R extends Runnable & IPriority> PriorityThrottledRunnable(final R runnable,
				final ExecutionThrottler<?> throttler) {
			super(runnable, throttler);
			this.runnable = runnable;
		}

		@Override
		public int getPriority() {
			return runnable.getPriority();
		}
	}
}
