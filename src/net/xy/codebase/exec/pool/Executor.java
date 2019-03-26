package net.xy.codebase.exec.pool;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.xy.codebase.concurrent.IExecutor;
import net.xy.codebase.concurrent.Semaphore;
import net.xy.codebase.exec.tasks.ScheduledTask;
import net.xy.codebase.exec.tq.TimeoutQueue;

public class Executor implements IExecutor {
	private static final Logger LOG = LoggerFactory.getLogger(Executor.class);
	/**
	 * unique name of this instance
	 */
	private final String name;
	private final AtomicInteger threadCount = new AtomicInteger();
	private final AtomicInteger threadId = new AtomicInteger();

	private ThreadGroup threadGroup;
	private final AtomicInteger exitThreads = new AtomicInteger(0);
	private ScheduledTask threadChecker;

	private final ThreadState working = new ThreadState();
	private TaskSourceFaccade workerFaccade;
	private Worker defaultWorker;

	public Executor(final String name, final TimeoutQueue tq) {
		this.name = name;
		tq.add(threadChecker = new ThreadManager(40, this));
	}

	public void setTaskSource(final ITaskSource taskSource) {
		workerFaccade = new TaskSourceFaccade(taskSource);
		defaultWorker = new Worker(workerFaccade);
	}

	public void setUseThreadGroups(final boolean flag) {
		if (flag)
			threadGroup = new ThreadGroup(Thread.currentThread().getThreadGroup().getParent(), name);
		else
			threadGroup = null;
	}

	public String getName() {
		return name;
	}

	@Override
	public int getThreadCount() {
		return threadCount.get();
	}

	public int getWorkingCount() {
		return working.get();
	}

	public void addThread() {
		if (defaultWorker == null)
			return;

		threadCount.incrementAndGet();
		final Thread th = new Thread(threadGroup, defaultWorker);
		th.setName(name + "-" + threadId.incrementAndGet());
		th.setDaemon(false);
		th.start();
		if (LOG.isDebugEnabled())
			LOG.debug("Thread for executor was created [" + th + "][" + name + "]");
	}

	public void purgeThread() {
		threadCount.decrementAndGet();
		exitThreads.incrementAndGet();
	}

	@Override
	public void prepareSutdown() {
		threadChecker.stop();
	}

	@Override
	public void shutdown() {
		exitThreads.set(Integer.MAX_VALUE);
		if (workerFaccade != null)
			workerFaccade.shutdown();
	}

	public class TaskSourceFaccade implements ITaskSource {
		private final ITaskSource taskSource;

		public TaskSourceFaccade(final ITaskSource taskSource) {
			this.taskSource = taskSource;
		}

		@Override
		public boolean next(final Worker worker) {
			working.add();
			taskSource.next(worker);
			working.remove();

			// end thread or not
			do {
				final int count = exitThreads.get();
				if (count > 0)
					if (exitThreads.compareAndSet(count, count - 1)) {
						if (LOG.isDebugEnabled())
							LOG.debug("Thread from executor was purged [" + Thread.currentThread().getName() + "]");
						return false;
					} else
						continue;
			} while (false);
			return true;
		}

		@Override
		public Semaphore getCondition() {
			return taskSource.getCondition();
		}

		public void shutdown() {
			getCondition().callAll();
			LOG.info("Calling all executor threads for shutdown[" + name + "]");
		}
	}
}
