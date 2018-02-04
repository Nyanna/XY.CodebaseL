package net.xy.codebase.concurrent;

import net.xy.codebase.exec.IPerfCounter;
import net.xy.codebase.exec.PerfCounter;

public class NeverTaskMonitor extends AbstractTaskMonitor {
	private final PerfCounter perf = new PerfCounter(0.05f);

	public NeverTaskMonitor() {
	}

	@Override
	public boolean aquiere() {
		super.aquiere();
		return false;
	}

	@Override
	public void finished() {
	}

	@Override
	public IPerfCounter getPerf() {
		return perf;
	}

	@Override
	public int getCurrent() {
		return 0;
	}

	@Override
	public String toString() {
		return String.format("NeverTaskMonitor []");
	}
}
