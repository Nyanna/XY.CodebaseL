package net.xy.codebase.exec.pool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.xy.codebase.exec.tasks.ScheduledTask;

public class ThreadManager extends ScheduledTask {
	private static final Logger LOG = LoggerFactory.getLogger(Executor.class);

	// base parameters and counters
	private int coreAmount = 2;
	private int maxAmount = Math.max(coreAmount, Runtime.getRuntime().availableProcessors());
	private final Executor executor;

	private double workSum;
	private double count;

	public ThreadManager(final int intervallMs, final Executor executor) {
		super(intervallMs);
		this.executor = executor;
	}

	public void setCoreAmount(final int coreAmount) {
		this.coreAmount = coreAmount;
	}

	public void setMaxAmount(final int maxAmount) {
		this.maxAmount = maxAmount;
	}

	@Override
	protected void innerRun() {
		final double frame = 0.8d;
		workSum *= frame;
		count *= frame;

		final int threadCount = executor.getThreadCount();
		workSum += executor.getWorkingCount() / Math.max(threadCount, 1d);
		count++;

		final double workAvr = workSum / count;
		if (LOG.isDebugEnabled())
			LOG.debug("Executor stat [" + workAvr + "][" + executor.getName() + "]");

		if (threadCount == 0 || threadCount < maxAmount && workAvr > 0.8f)
			executor.addThread();
		else if (threadCount > coreAmount && workAvr < 0.2f)
			executor.purgeThread();
	}

	@Override
	public String toString() {
		return String.format("ThreadManager [workSum=%s,count=%s,%s,%s]", workSum, count, executor, toStringSuper());
	}
}
