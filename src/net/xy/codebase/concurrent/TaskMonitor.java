package net.xy.codebase.concurrent;

import java.util.concurrent.atomic.AtomicInteger;

import net.xy.codebase.exec.IPerfCounter;
import net.xy.codebase.exec.PerfCounter;

public class TaskMonitor implements ITaskMonitor {
	private final PerfCounter perf = new PerfCounter(0.05f);

	private final AtomicInteger running = new AtomicInteger(0);
	private int maxRunning = Short.MAX_VALUE;

	public TaskMonitor() {
	}

	public TaskMonitor(final int maxRunning) {
		this.maxRunning = maxRunning;
	}

	/* (non-Javadoc)
	 * @see net.xy.codebase.concurrent.ITaskMonitor#aquiere()
	 */
	@Override
	public boolean aquiere() {
		for (;;) {
			final int runs = running.get();
			if (runs < maxRunning) {
				if (running.compareAndSet(runs, runs + 1))
					return true;
			} else if (runs >= maxRunning)
				if (running.compareAndSet(runs, runs))
					return false;
		}
	}

	/* (non-Javadoc)
	 * @see net.xy.codebase.concurrent.ITaskMonitor#finished()
	 */
	@Override
	public void finished() {
		running.decrementAndGet();
	}

	/* (non-Javadoc)
	 * @see net.xy.codebase.concurrent.ITaskMonitor#getPerf()
	 */
	@Override
	public IPerfCounter getPerf() {
		return perf;
	}

	public void setMaxRunning(final int maxRunning) {
		this.maxRunning = maxRunning;
	}
}
