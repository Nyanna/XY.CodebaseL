package net.xy.codebase.concurrent;

import java.util.concurrent.atomic.AtomicInteger;

import net.xy.codebase.exec.IPerfCounter;
import net.xy.codebase.exec.PerfCounter;

public class TaskMonitor extends AbstractTaskMonitor {
	private final PerfCounter perf = new PerfCounter(0.05f);

	private final AtomicInteger running = new AtomicInteger(0);
	private int maxRunning = Short.MAX_VALUE;

	public TaskMonitor() {
	}

	public TaskMonitor(final int maxRunning) {
		this.maxRunning = maxRunning;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see net.xy.codebase.concurrent.ITaskMonitor#aquiere()
	 */
	@Override
	public boolean aquiere() {
		super.aquiere();
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

	/*
	 * (non-Javadoc)
	 *
	 * @see net.xy.codebase.concurrent.ITaskMonitor#finished()
	 */
	@Override
	public void finished() {
		running.decrementAndGet();
	}

	@Override
	public int getCurrent() {
		return running.get();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see net.xy.codebase.concurrent.ITaskMonitor#getPerf()
	 */
	@Override
	public IPerfCounter getPerf() {
		return perf;
	}

	public void setMaxRunning(final int maxRunning) {
		this.maxRunning = maxRunning;
	}

	@Override
	public String toString() {
		return String.format("TaskMonitor [running=%s, maxRunning=%s]", running, maxRunning);
	}
}
