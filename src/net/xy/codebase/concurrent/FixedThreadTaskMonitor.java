package net.xy.codebase.concurrent;

import net.xy.codebase.exec.IPerfCounter;
import net.xy.codebase.exec.PerfCounter;

public class FixedThreadTaskMonitor implements ITaskMonitor {
	private final PerfCounter perf = new PerfCounter(0.05f);
	private Thread th = null;

	@Override
	public boolean aquiere() {
		final Thread currentThread = Thread.currentThread();
		if (th == null || !th.isAlive())
			synchronized (this) {
				if (th == null || !th.isAlive())
					th = currentThread;
			}
		return th == currentThread;
	}

	@Override
	public void finished() {
	}

	@Override
	public IPerfCounter getPerf() {
		return perf;
	}
}
