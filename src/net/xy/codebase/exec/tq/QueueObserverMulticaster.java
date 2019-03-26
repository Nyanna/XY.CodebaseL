package net.xy.codebase.exec.tq;

import net.xy.codebase.collection.Array;
import net.xy.codebase.exec.tasks.ITask;

public class QueueObserverMulticaster implements IQueueObserver {
	private final Array<IQueueObserver> observers = new Array<IQueueObserver>(IQueueObserver.class, 3);

	public void addObserver(final IQueueObserver obs) {
		observers.addChecked(obs);
	}

	public void removeObserver(final IQueueObserver obs) {
		observers.removeEquals(obs);
	}

	@Override
	public void taskAdded(final ITask t) {
		for (int i = 0; i < observers.size(); i++)
			observers.get(i).taskAdded(t);
	}

	@Override
	public void taskStarted(final ITask t, final long latency) {
		for (int i = 0; i < observers.size(); i++)
			observers.get(i).taskStarted(t, latency);
	}

	@Override
	public void taskStoped(final ITask t) {
		for (int i = 0; i < observers.size(); i++)
			observers.get(i).taskStoped(t);
	}

	@Override
	public void queueExited() {
		for (int i = 0; i < observers.size(); i++)
			observers.get(i).queueExited();
	}
}
