package net.xy.codebase.thread;

import net.xy.codebase.collection.TimeoutQueue.ITask;

public abstract class RecurringTask implements ITask {
	private final int intervallMs;
	private long nextRun;

	public RecurringTask(final int intervallMs) {
		this.intervallMs = intervallMs;
		nextRun = System.currentTimeMillis();
	}

	@Override
	public boolean isRecurring() {
		return true;
	}

	@Override
	public long nextRun() {
		return nextRun;
	}

	private void calcNextRun() {
		if (nextRun == 0)
			nextRun = System.currentTimeMillis();
		nextRun += intervallMs;
	}

	@Override
	public void run() {
		calcNextRun();
		innerRun();
	}

	protected abstract void innerRun();
}
