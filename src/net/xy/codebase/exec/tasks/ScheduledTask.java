package net.xy.codebase.exec.tasks;

import java.util.concurrent.TimeUnit;

import net.xy.codebase.exec.TimeoutQueue;

public abstract class ScheduledTask implements ITask {
	private final long intervall;
	private long next;
	private long nextFixed;
	private volatile boolean stoped = false;
	private TimeoutQueue tq;

	public ScheduledTask(final long intervallMs) {
		this(intervallMs, 0);
	}

	public ScheduledTask(final long intervallMs, final long delayMs) {
		intervall = TimeUnit.MILLISECONDS.toNanos(intervallMs);
		next = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(delayMs);
	}

	@Override
	public long nextRunFixed() {
		return nextFixed;
	}

	@Override
	public void setQueue(final TimeoutQueue tq) {
		this.tq = tq;
		nextFixed = nextRun();
	}

	@Override
	public long nextRun() {
		return stoped ? 0 : next;
	}

	public void setNext(final long timeoutMs) {
		next = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutMs);
	}

	@Override
	public final void run() {
		if (!stoped) {
			innerRun();
			if (!stoped && intervall > 0) {
				next += intervall;
				tq.add(this);
			}
		}
	}

	protected abstract void innerRun();

	public boolean stop() {
		return setStop(true);
	}

	public boolean setStop(final boolean flag) {
		if (stoped != flag) {
			stoped = flag;
			return true;
		}
		return false;
	}

	public boolean isStoped() {
		return stoped;
	}

	@Override
	public String toString() {
		return String.format("ScheduledTask %s[cl=%s]", toStringSuper(), getClass().getName());
	}

	protected String toStringSuper() {
		if (intervall > 0)
			return String.format("[iv=%s, next=%s, %s]", intervall / 1000000, (nextRun() - System.nanoTime()) / 1000000,
					isStoped());
		else
			return String.format("[next=%s, %s]", (nextRun() - System.nanoTime()) / 1000000, isStoped());
	}
}
