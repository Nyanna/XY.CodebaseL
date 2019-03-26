package net.xy.codebase.exec;

import net.xy.codebase.exec.tasks.InterThreadScheduledTask;
import net.xy.codebase.exec.tasks.ScheduledTask;
import net.xy.codebase.exec.tq.TimeoutQueue;

public class InterTimeoutQueue<E extends Enum<E>> extends TimeoutQueue {
	private final IInterThreads<E> inter;

	public InterTimeoutQueue(final String name, final IInterThreads<E> inter) {
		super(name);
		this.inter = inter;
	}

	public InterThreadScheduledTask<E> runLater(final E thread, final Runnable run, final int timeout) {
		final InterThreadScheduledTask<E> res = new InterThreadScheduledTask<E>(thread, 0, timeout, run, inter);
		return add(res) ? res : null;
	}

	public ScheduledTask runIntervall(final E thread, final Runnable run, final int intervall) {
		final InterThreadScheduledTask<E> res = new InterThreadScheduledTask<E>(thread, intervall, 0, run, inter);
		return add(res) ? res : null;
	}

	public ScheduledTask runDelayedIntervall(final E thread, final Runnable run, final int intervall,
			final int startDelay) {
		final InterThreadScheduledTask<E> res = new InterThreadScheduledTask<E>(thread, intervall, startDelay, run,
				inter);
		return add(res) ? res : null;
	}
}
