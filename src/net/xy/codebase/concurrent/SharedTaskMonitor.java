package net.xy.codebase.concurrent;

import net.xy.codebase.exec.PerfCounter;

public class SharedTaskMonitor extends TaskMonitor {
	private final PerfCounter perf = new PerfCounter(0.05f);
	private final TaskMonitor dele;

	public SharedTaskMonitor(final TaskMonitor dele) {
		this.dele = dele;
	}

	@Override
	public boolean aquiere() {
		return dele.aquiere();
	}

	@Override
	public void finished() {
		dele.finished();
	}

	@Override
	public PerfCounter getPerf() {
		return perf;
	}

	@Override
	public void setMaxRunning(final int maxRunning) {
		throw new IllegalArgumentException("Just shared monitor, set value in original monitor");
	}
}
