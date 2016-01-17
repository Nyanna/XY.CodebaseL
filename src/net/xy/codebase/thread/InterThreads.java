package net.xy.codebase.thread;

import java.util.EnumMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.xy.codebase.collection.ParkingArrayQueue;
import net.xy.codebase.collection.TimeoutQueue;
import net.xy.codebase.collection.TimeoutQueue.ITask;
import net.xy.codebase.exec.ExecutionThrottler;
import net.xy.codebase.exec.TimeoutRunnable;

/**
 * implementation for inter thread job execution
 *
 * @author Xyan
 *
 * @param <E>
 *            enum of possible threads
 */
public class InterThreads<E extends Enum<E>> extends AbstractInterThreads<E> {
	private static final Logger LOG = LoggerFactory.getLogger(InterThreads.class);
	/**
	 * thread job stores
	 */
	private final EnumMap<E, ParkingArrayQueue<Runnable>> ctxs;
	/**
	 * timeout queue for delayed interthread execution
	 */
	private final TimeoutQueue tque;

	/**
	 * default
	 *
	 * @param enun
	 * @param capacity
	 */
	public InterThreads(final Class<E> enun, final int capacity) {
		final E[] evals = enun.getEnumConstants();
		ctxs = new EnumMap<E, ParkingArrayQueue<Runnable>>(enun);
		for (final E val : evals)
			ctxs.put(val, new ParkingArrayQueue<Runnable>(Runnable.class, capacity));

		tque = new TimeoutQueue("Interthread");
	}

	/**
	 * @param target
	 * @return the tragte thread queue
	 */
	private ParkingArrayQueue<Runnable> get(final E target) {
		return ctxs.get(target);
	}

	@Override
	public Runnable next(final E target) {
		return get(target).take();
	}

	@Override
	public Runnable next(final E target, final int ms) {
		return get(target).take(ms);
	}

	@Override
	public void put(final E target, final Runnable job) {
		final ParkingArrayQueue<Runnable> que = get(target);
		if (!que.add(job))
			LOG.error("Error target thread too full droping job [" + target + "][" + job + "]");
	}

	@Override
	public ExecutionThrottler getThrottler(final E thread, final Runnable run) {
		return new ExecutionThrottler(new InterThreadRunnable<E>(thread, run, this));
	}

	@Override
	public ExecutionThrottler getThrottler(final E thread, final Runnable run, final int intervallMs) {
		return new ExecutionThrottler(new InterThreadSchedulable<E>(thread, run, this), intervallMs);
	}

	@Override
	public InterThreadTimeoutable runLater(final E thread, final Runnable run, final int timeout) {
		final InterThreadTimeoutable res = new InterThreadTimeoutable(thread, timeout, run);
		tque.add(res);
		return res;
	}

	@Override
	public RecurringTask start(final E thread, final Runnable run, final int intervall) {
		final InterThreadIntervall<E> res = new InterThreadIntervall<E>(thread, intervall, run, this);
		tque.add(res);
		return res;
	}

	@Override
	public void start(final ITask task) {
		tque.add(task);
	}

	/**
	 * implementation which supports single run timeouts to the referenced
	 * timeoutqueue
	 *
	 * @author Xyan
	 *
	 */
	public class InterThreadTimeoutable extends TimeoutRunnable {
		/**
		 * target thread
		 */
		private final E thread;
		/**
		 * real runnable
		 */
		private final Runnable run;

		/**
		 * default
		 *
		 * @param thread
		 * @param timeoutMs
		 * @param run
		 */
		public InterThreadTimeoutable(final E thread, final long timeoutMs, final Runnable run) {
			super(timeoutMs);
			this.run = run;
			this.thread = thread;
		}

		@Override
		public void run() {
			InterThreads.this.put(thread, run);
		}
	}

	/**
	 * interthread intervall
	 *
	 * @author Xyan
	 *
	 */
	public static class InterThreadIntervall<E extends Enum<E>> extends RecurringTask {
		/**
		 * target thread
		 */
		private final E thread;
		/**
		 * real runnable
		 */
		protected Runnable run;
		/**
		 * back reference
		 */
		private final IInterThreads<E> it;

		/**
		 * default
		 *
		 * @param thread
		 * @param timeoutMs
		 * @param run
		 */
		public InterThreadIntervall(final E thread, final int intervall, final Runnable run,
				final IInterThreads<E> it) {
			super(intervall);
			this.run = run;
			this.thread = thread;
			this.it = it;
		}

		/**
		 * for inner access
		 * 
		 * @param thread
		 * @param intervall
		 * @param it
		 */
		protected InterThreadIntervall(final E thread, final int intervall, final IInterThreads<E> it) {
			this(thread, intervall, null, it);
		}

		@Override
		protected void innerRun() {
			it.put(thread, run);
		}

		@Override
		public String toString() {
			return "RTCapsule " + run.toString();
		}
	}

	/**
	 * concrete implementation, which don't supports timeouts
	 *
	 * @author Xyan
	 *
	 */
	public static class InterThreadRunnable<E extends Enum<E>> extends AbstractInterThreadRunnable<E> {
		/**
		 * back reference
		 */
		private final IInterThreads<E> it;

		/**
		 * default
		 *
		 * @param thread
		 * @param run
		 */
		public InterThreadRunnable(final E thread, final Runnable run, final IInterThreads<E> it) {
			super(thread, run);
			this.it = it;
		}

		@Override
		public void schedule(final ITask run) {
			if (LOG.isTraceEnabled())
				LOG.trace("insert in threadqueue no schedule [" + this + "]");
			it.put(thread, run);
		}

		@Override
		public void run() {
			run.run();
		}

		@Override
		public String toString() {
			return "Inter [" + run.getClass().getSimpleName() + "]";
		}
	}

	/**
	 * implementation which supports intervall timeouts to an referenced
	 * timeoutqueue
	 *
	 * @author Xyan
	 *
	 */
	public static class InterThreadSchedulable<E extends Enum<E>> extends AbstractInterThreadRunnable<E> {
		/**
		 * back reference
		 */
		private final IInterThreads<E> it;

		/**
		 * default
		 *
		 * @param thread
		 * @param run
		 */
		public InterThreadSchedulable(final E thread, final Runnable run, final IInterThreads<E> it) {
			super(thread, run);
			this.it = it;
		}

		@Override
		public void schedule(final ITask capsule) {
			if (capsule.nextRun() <= 0) {
				if (LOG.isTraceEnabled())
					LOG.trace("Schedule directly [" + this + "]");
				capsule.run();
			} else {
				if (LOG.isTraceEnabled())
					LOG.trace("Schedule queued [" + this + "]");
				it.start(capsule);
			}
		}

		@Override
		public void run() {
			if (LOG.isTraceEnabled())
				LOG.trace("Insert in threadqueue [" + this + "]");
			it.put(thread, run);
		}

		@Override
		public String toString() {
			return "Inter [" + run.getClass().getSimpleName() + "]";
		}
	}
}
