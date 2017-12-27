package net.xy.codebase.concurrent;

import java.util.concurrent.atomic.AtomicInteger;

import net.xy.codebase.exec.IPerfCounter;
import net.xy.codebase.exec.PerfCounter;

public class TaskMonitor {
	private final PerfCounter perf = new PerfCounter(0.05f);

	private final AtomicInteger running = new AtomicInteger(0);
	private int maxRunning = Short.MAX_VALUE;

	public TaskMonitor() {
	}

	public TaskMonitor(final int maxRunning) {
		this.maxRunning = maxRunning;
	}

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

	public void finished() {
		running.decrementAndGet();
	}

	public IPerfCounter getPerf() {
		return perf;
	}

	public void setMaxRunning(final int maxRunning) {
		this.maxRunning = maxRunning;
	}
}
