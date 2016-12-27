package net.xy.codebase.exec;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.xy.codebase.collection.IPriority;
import net.xy.codebase.exec.tasks.ICoveredRunnable;
import net.xy.codebase.exec.tasks.IScheduleRunnable;
import net.xy.codebase.exec.tasks.ITask;

/**
 * throttler to execute an runnable not more than N parallel amounts;
 *
 * @author Xyan
 *
 */
public class ExecutionLimiter {
	private static final Logger LOG = LoggerFactory.getLogger(ExecutionLimiter.class);
	/**
	 * amount of actually running tasks
	 */
	private final AtomicInteger runs = new AtomicInteger();
	private final AtomicInteger calls = new AtomicInteger();
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
	 * start the action
	 */
	public void run() {
		run(1);
	}

	public void run(final int count) {
		for (int i = 0; i < Math.min(count, amount); i++) {
			calls.incrementAndGet();
			for (;;) {
				final int runs = this.runs.get();
				if (runs >= amount) {
					if (this.runs.compareAndSet(runs, runs)) {
						if (LOG.isTraceEnabled())
							LOG.trace("concurrent runnings reached [" + runnable + "]["
									+ runnable.getClass().getSimpleName() + "]");
						break;
					}
				} else if (this.runs.compareAndSet(runs, runs + 1)) {
					// start runnable
					if (LOG.isTraceEnabled())
						LOG.trace("Start limited [" + runnable + "][" + runnable.getClass().getSimpleName() + "]");
					if (!runnable.schedule(capsule)) {
						this.runs.decrementAndGet();
						LOG.error("Error starting limiter, decrementing [" + this + "]");
					}
					break;
				}
			}
		}
	}

	/**
	 * encapsulation runnable
	 *
	 * @author Xyan
	 *
	 */
	public class LimitedRunnable implements ITask, ICoveredRunnable {
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
			for (;;) {
				final int run = runs.get();
				final int call = calls.get();
				if (call > 0) {
					if (calls.compareAndSet(call, call - 1))
						runGuarded();
				} else if (runs.compareAndSet(run, run - 1))
					break;
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
		public Runnable getRunnable() {
			return runnable;
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
