package net.xy.codebase.concurrent;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.xy.codebase.exec.IInterThreads.InterruptedException;
import net.xy.codebase.exec.InterThreads;
import net.xy.codebase.exec.tasks.ScheduledTask;

public abstract class Executor implements IExecutor {
	private static final Logger LOG = LoggerFactory.getLogger(Executor.class);
	private final Semaphore added = new Semaphore();
	private final AtomicInteger exitThreads = new AtomicInteger(0);
	private int threadCount = 0;

	private int coreAmount = Math.max(2, Runtime.getRuntime().availableProcessors() - 1);
	private int maxAmount = Math.max(coreAmount, Runtime.getRuntime().availableProcessors());

	private volatile boolean shutdown = false;
	private ScheduledTask threadChecker;

	public Executor(final InterThreads<?> inter) {
		inter.start(threadChecker = new ThreadManager(40));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see net.xy.codebase.concurrent.IExecutor#getThreadCount()
	 */
	@Override
	public int getThreadCount() {
		return threadCount;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see net.xy.codebase.concurrent.IExecutor#getWorkCount()
	 */
	@Override
	public int getWorkCount() {
		return State.Working.count.get();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see net.xy.codebase.concurrent.IExecutor#getIdleCount()
	 */
	@Override
	public int getIdleCount() {
		return State.Idle.count.get();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see net.xy.codebase.concurrent.IExecutor#check()
	 */
	@Override
	public void check() {
		added.call();
	}

	protected abstract void next(Worker worker);

	protected abstract Thread createThread(Runnable run);

	private boolean endThread() {
		final int count = exitThreads.get();
		if (count > 0 && exitThreads.compareAndSet(count, count - 1) || shutdown) {
			if (LOG.isDebugEnabled())
				LOG.debug("Thread from executor was purged");
			return true;
		}
		return false;
	}

	/**
	 * @param job
	 * @return true to procceed normaly
	 */
	protected boolean runGuarded(final Runnable job) {
		try {
			job.run();
		} catch (final InterruptedException ex) {
			// clean interuped
			return false;
		} catch (final Exception e) {
			LOG.error("Error running job", e);
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see net.xy.codebase.concurrent.IExecutor#setCoreAmount(int)
	 */
	@Override
	public void setCoreAmount(final int coreAmount) {
		this.coreAmount = coreAmount;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see net.xy.codebase.concurrent.IExecutor#setMaxAmount(int)
	 */
	@Override
	public void setMaxAmount(final int maxAmount) {
		this.maxAmount = maxAmount;
	}

	@Override
	public void prepareSutdown() {
		threadChecker.stop();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see net.xy.codebase.concurrent.IExecutor#shutdown()
	 */
	@Override
	public void shutdown() {
		shutdown = true;
		added.callAll();
		LOG.info("Calling all executor threads for shutdown");
	}

	public static enum State {
		Idle, Working;

		public AtomicInteger count = new AtomicInteger();
	};

	public class Worker implements Runnable {
		private State vote = State.Idle;

		public Worker() {
			State.Idle.count.incrementAndGet();
		}

		@Override
		public void run() {
			while (true) {
				final int state = added.getState(); // for null job
				next(this);
				vote(State.Idle);
				if (endThread())
					return;
				added.await(state); // runs when job was not initially null
			}
		}

		public void jobAccepted() {
			vote(State.Working);
			check(); // cuz maybe enabled new ones
		}

		public boolean vote(final State vote) {
			if (this.vote != vote) {
				this.vote.count.decrementAndGet();
				vote.count.incrementAndGet();
				this.vote = vote;
				return true;
			}
			return false;
		}
	}

	public class ThreadManager extends ScheduledTask {
		private double workSum;
		private double count;

		private ThreadManager(final int intervallMs) {
			super(intervallMs);
		}

		@Override
		protected void innerRun() {
			final double frame = 0.8d;
			workSum *= frame;
			count *= frame;

			workSum += getWorkCount() / Math.max(threadCount, 1d);
			count++;

			final double workAvr = workSum / count;
			if (LOG.isDebugEnabled())
				LOG.debug("Executor stat [" + workAvr + "]");

			if (threadCount == 0 || threadCount < maxAmount && workAvr > 0.8f) {
				threadCount++;
				final Thread th = createThread(new Worker());
				th.setDaemon(false);
				th.start();
				if (LOG.isDebugEnabled())
					LOG.debug("Thread for executor was created [" + th + "]");
			} else if (threadCount > coreAmount && workAvr < 0.2f) {
				threadCount--;
				exitThreads.incrementAndGet();
			}
		}
	}
}
