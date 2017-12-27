package net.xy.codebase.concurrent;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.xy.codebase.exec.IInterThreads.InterruptedException;
import net.xy.codebase.exec.InterThreads;
import net.xy.codebase.exec.tasks.RecurringTask;

public abstract class Executor<E> implements IExecutor<E> {
	private static final Logger LOG = LoggerFactory.getLogger(Executor.class);
	private final Semaphore added = new Semaphore();
	private final AtomicInteger exitThreads = new AtomicInteger(0);
	private int threadCount = 0;

	private int coreAmount = Runtime.getRuntime().availableProcessors() * 2;
	private int maxAmount = coreAmount * 2;

	private boolean shutdown = false;
	private RecurringTask threadChecker;

	public Executor(final InterThreads<?> inter) {
		inter.start(threadChecker = new RecurringTask(200) {
			@Override
			protected void innerRun() {
				if (threadCount == 0 || threadCount < maxAmount && getWorkCount() > threadCount * 0.8f) {
					threadCount++;
					final Thread th = createThread(new Worker());
					th.setDaemon(false);
					th.start();
					if (LOG.isDebugEnabled())
						LOG.debug("Thread for executor was created [" + th + "]");
				} else if (threadCount > coreAmount && getIdleCount() > threadCount * 0.8f) {
					threadCount--;
					exitThreads.incrementAndGet();
				}
			}
		});
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

	protected abstract E next();

	protected abstract Thread createThread(Runnable run);

	protected abstract void runTask(final E job);

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

	/*
	 * (non-Javadoc)
	 *
	 * @see net.xy.codebase.concurrent.IExecutor#shutdown()
	 */
	@Override
	public void shutdown() {
		threadChecker.stop();
		shutdown = true;
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
				E job;
				while ((job = next()) != null) {
					vote(State.Working);
					check(); // cuz maybe enabled new ones
					runTask(job);
				}
				vote(State.Idle);
				if (endThread())
					return;
				added.await(state); // runs when job was not initially null
			}
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

}
