package net.xy.codebase.thread;

import java.util.concurrent.TimeUnit;

import net.xy.codebase.collection.TimeoutQueue.ITask;

public abstract class RecurringTask implements ITask {
	private final int intervallMs;
	private long nextRun;
	private volatile boolean recurring = true;

	public RecurringTask(final int intervallMs) {
		this.intervallMs = intervallMs;
		nextRun = System.nanoTime();
	}

	@Override
	public boolean isRecurring() {
		return recurring;
	}

	@Override
	public long nextRun() {
		return nextRun;
	}

	private void calcNextRun() {
		final long now = System.nanoTime();
		if (nextRun == 0)
			nextRun = now;
		final long ivns = TimeUnit.MILLISECONDS.toNanos(intervallMs);
		nextRun += ((now - nextRun) / ivns + 1) * ivns;
	}

	@Override
	public void run() {
		if (recurring) {
			calcNextRun();
			innerRun();
		}
	}

	protected abstract void innerRun();

	public void stop() {
		recurring = false;
	}
}
