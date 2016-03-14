package net.xy.codebase.exec.tasks;

import java.util.concurrent.TimeUnit;

public abstract class RecurringTask implements ITask {
	private final int intervallMs;
	private long nextRun;
	private volatile boolean recurring = true;

	public RecurringTask(final int intervallMs) {
		this.intervallMs = intervallMs;
		nextRun = System.nanoTime();
	}

	public RecurringTask(final int intervallMs, final int nextMs) {
		this.intervallMs = intervallMs;
		nextRun = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(nextMs);
	}

	@Override
	public boolean isRecurring() {
		return recurring;
	}

	@Override
	public long nextRun() {
		return nextRun;
	}

	protected void calcNextRun(final int intervallMs) {
		final long now = System.nanoTime();
		if (nextRun == 0)
			nextRun = now;
		final long ivns = TimeUnit.MILLISECONDS.toNanos(intervallMs);
		nextRun += ((now - nextRun) / ivns + 1) * ivns;
	}

	@Override
	public void run() {
		if (recurring) {
			calcNextRun(intervallMs);
			innerRun();
		}
	}

	protected abstract void innerRun();

	public void stop() {
		recurring = false;
	}
}
