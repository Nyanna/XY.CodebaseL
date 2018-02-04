package net.xy.codebase.concurrent;

import net.xy.codebase.exec.IPerfCounter;
import net.xy.codebase.exec.PerfCounter;

public class FixedThreadTaskMonitor extends AbstractTaskMonitor {
	private final PerfCounter perf = new PerfCounter(0.05f);
	private Thread th = null;

	@Override
	public boolean aquiere() {
		super.aquiere();
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

	@Override
	public int getCurrent() {
		return -1;
	}

	@Override
	public String toString() {
		return String.format("FixedThreadTaskMonitor [th=%s]", th);
	}
}
