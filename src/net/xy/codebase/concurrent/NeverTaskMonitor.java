package net.xy.codebase.concurrent;

import net.xy.codebase.exec.IPerfCounter;
import net.xy.codebase.exec.PerfCounter;

public class NeverTaskMonitor implements ITaskMonitor {
	private final PerfCounter perf = new PerfCounter(0.05f);

	public NeverTaskMonitor() {
	}

	@Override
	public boolean aquiere() {
		return false;
	}

	@Override
	public void finished() {
	}

	@Override
	public IPerfCounter getPerf() {
		return perf;
	}
}
