package net.xy.codebase.concurrent;

import net.xy.codebase.exec.PerfCounter;

public class SharedTaskMonitor implements ITaskMonitor {
	private final PerfCounter perf = new PerfCounter(0.05f);
	private final ITaskMonitor dele;

	public SharedTaskMonitor(final ITaskMonitor dele) {
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
	public int getCurrent() {
		return dele.getCurrent();
	}

	@Override
	public long getLastChecked() {
		return dele.getLastChecked();
	}

	@Override
	public String toString() {
		return String.format("SharedTaskMonitor [dele=%s]", dele);
	}
}
