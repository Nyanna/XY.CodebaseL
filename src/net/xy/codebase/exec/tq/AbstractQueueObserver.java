package net.xy.codebase.exec.tq;

import net.xy.codebase.exec.tasks.ITask;

public abstract class AbstractQueueObserver implements IQueueObserver {

	@Override
	public void taskAdded(final ITask t) {
	}

	@Override
	public void taskStarted(final ITask t, final long latency) {
	}

	@Override
	public void taskStoped(final ITask t) {
	}

	@Override
	public void queueExited() {
	}
}
