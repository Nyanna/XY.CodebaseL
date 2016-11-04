package net.xy.codebase.exec;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.xy.codebase.collection.IPriority;
import net.xy.codebase.exec.tasks.IScheduleRunnable;
import net.xy.codebase.exec.tasks.ITask;

/**
 * throttler to execute an runnable not more than N paralel amounts;
 *
 * @author Xyan
 *
 */
public class ExecutionLimiter {
	private static final Logger LOG = LoggerFactory.getLogger(ExecutionLimiter.class);
	/**
	 * amount of requested runs
	 */
	private final AtomicLong calls = new AtomicLong(0);
	/**
	 * amount of actually running tasks
	 */
	private final AtomicLong running = new AtomicLong(0);
	/**
	 * target action to run
	 */
	private final IScheduleRunnable runnable;
	private final LimitedRunnable capsule;
	/**
	 * maximum amount of concurrently running
	 */
	private final int amount;

	/**
	 * default
	 *
	 * @param runnable
	 * @param amount
	 *            of concurrent running jobs
	 */
	@SuppressWarnings("unchecked")
	public <T extends IScheduleRunnable & IPriority> ExecutionLimiter(final IScheduleRunnable runnable,
			final int amount) {
		this.runnable = runnable;
		this.amount = amount;
		if (runnable instanceof IPriority)
			capsule = new PriorityLimitedRunnable((T) runnable);
		else
			capsule = new LimitedRunnable(runnable);
	}

	/**
	 * start the action or place an call wish
	 */
	public void run() {
		calls.incrementAndGet();
		checkDettach();
	}

	/**
	 * check to start new tasks
	 */
	private void checkDettach() {
		while (true) {
			long runs = running.get();
			if (runs >= amount && running.compareAndSet(runs, runs)) {
				if (LOG.isTraceEnabled())
					LOG.trace("concurrent runnings reached [" + runnable + "][" + runnable.getClass().getSimpleName()
							+ "]");
				return;
			} else if (running.compareAndSet(runs, ++runs)) {
				// start runnable
				if (LOG.isTraceEnabled())
					LOG.trace("Start limited [" + runnable + "][" + runnable.getClass().getSimpleName() + "]");
				if (!runnable.schedule(capsule)) {
					running.set(--runs);
					LOG.error("Error starting limiter, decrementing");
				}
				return;
			} else if (LOG.isTraceEnabled())
				LOG.trace("Inefficient loop repeat [" + runnable + "][" + runnable.getClass().getSimpleName() + "]");
		}
	}

	/**
	 * encapsulation runnable
	 *
	 * @author Xyan
	 *
	 */
	public class LimitedRunnable implements ITask {
		/**
		 * target action to run
		 */
		private final IScheduleRunnable runnable;

		/**
		 * default
		 *
		 * @param runnable
		 */
		public LimitedRunnable(final IScheduleRunnable runnable) {
			this.runnable = runnable;
		}

		@Override
		public void run() {
			if (calls.getAndDecrement() > 0) {
				runGuarded();
				running.decrementAndGet();
				if (calls.get() > 0)
					checkDettach();
			}
		}

		private void runGuarded() {
			try {
				runnable.run();
			} catch (final Exception e) {
				LOG.error("Error running limited", e);
			}
		}

		@Override
		public boolean isRecurring() {
			return false;
		}

		@Override
		public long nextRun() {
			return 0;
		}

		@Override
		public String toString() {
			return "LimitedRunnable: " + runnable.toString();
		}
	}

	/**
	 * encapsulation with priority support
	 *
	 * @author Xyan
	 *
	 */
	public class PriorityLimitedRunnable extends LimitedRunnable implements IPriority {
		/**
		 * target action to run
		 */
		private final IPriority runnable;

		public <R extends IScheduleRunnable & IPriority> PriorityLimitedRunnable(final R runnable) {
			super(runnable);
			this.runnable = runnable;
		}

		@Override
		public int getPriority() {
			return runnable.getPriority();
		}
	}
}
