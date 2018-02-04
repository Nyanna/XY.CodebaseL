package net.xy.codebase.concurrent;

import java.util.concurrent.atomic.AtomicLong;

public abstract class AbstractTaskMonitor implements ITaskMonitor {
	private final AtomicLong lastChecked = new AtomicLong(0);

	@Override
	public boolean aquiere() {
		lastChecked.set(System.currentTimeMillis());
		return false;
	}

	@Override
	public long getLastChecked() {
		return lastChecked.get();
	}
}
