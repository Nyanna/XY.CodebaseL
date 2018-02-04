package net.xy.codebase.exec;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.xy.codebase.collection.IPriority;
import net.xy.codebase.exec.tasks.ICoveredRunnable;
import net.xy.codebase.exec.tasks.ITask;

/**
 * throttler to execute an runnable not more than N parallel amounts;
 *
 * @author Xyan
 *
 */
public class ExecutionLimiter<E extends Enum<E>> {
	private static final Logger LOG = LoggerFactory.getLogger(ExecutionLimiter.class);
	/**
	 * amount of actually running tasks
	 */
	private final AtomicInteger runs = new AtomicInteger();
	private final AtomicInteger calls = new AtomicInteger();
	/**
	 * lmiter capsule
	 */
	private final LimitedRunnable capsule;
	/**
	 * maximum amount of concurrently running
	 */
	private final int amount;
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
	 * default
	 *
	 * @param runnable
	 * @param amount
	 *            of concurrent running jobs
	 */
	@SuppressWarnings("unchecked")
	public <T extends ITask & IPriority> ExecutionLimiter(final Runnable runnable, final int amount,
			final InterThreads<E> inter, final E target) {
		this.amount = amount;
		this.inter = inter;
		this.target = target;
		if (runnable instanceof IPriority)
			capsule = new PriorityLimitedRunnable((T) runnable);
		else
			capsule = new LimitedRunnable(runnable);
	}

	public void run(final int amount) {
		calls.addAndGet(amount - 1);
		run();
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
	 * start the action
	 */
	public void run() {
		calls.incrementAndGet();

		while (enabled) {
			final int runs = this.runs.get();
			if (runs >= amount) {
				if (this.runs.compareAndSet(runs, runs)) {
					if (LOG.isTraceEnabled())
						LOG.trace("concurrent runnings reached [" + capsule + "]");
					break;
				}
			} else if (this.runs.compareAndSet(runs, runs + 1)) {
				// start runnable
				if (LOG.isTraceEnabled())
					LOG.trace("Start limited [" + capsule + "]");
				if (!inter.run(target, capsule)) {
					this.runs.decrementAndGet();
					LOG.error("Error starting limiter, decrementing [" + this + "]");
					break;
				} else if (calls.get() <= 10)
					// no further starts
					break;
			}
		}
	}

	/**
	 * encapsulation runnable
	 *
	 * @author Xyan
	 *
	 */
	public class LimitedRunnable implements Runnable, ICoveredRunnable {
		/**
		 * target action to run
		 */
		private final Runnable runnable;

		/**
		 * default
		 *
		 * @param runnable
		 */
		public LimitedRunnable(final Runnable runnable) {
			this.runnable = runnable;
		}

		@Override
		public void run() {
			final int run = runs.get();
			final int call = calls.get();
			if (call > 0) {
				if (calls.compareAndSet(call, call - 1))
					runGuarded();
			} else if (runs.compareAndSet(run, run - 1)) {
				if (LOG.isTraceEnabled())
					LOG.trace("Stopping limited [" + runnable + "][" + runnable.getClass().getSimpleName() + "]");
				return;
			}
			if (!inter.run(target, this))
				LOG.error("Error rescheduling limiter [" + runnable + "][" + runnable.getClass().getSimpleName() + "]");
		}

		private void runGuarded() {
			try {
				if (enabled)
					runnable.run();
			} catch (final Exception e) {
				LOG.error("Error running limited", e);
			}
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

		public <R extends Runnable & IPriority> PriorityLimitedRunnable(final R runnable) {
			super(runnable);
			this.runnable = runnable;
		}

		@Override
		public int getPriority() {
			return runnable.getPriority();
		}
	}
}
