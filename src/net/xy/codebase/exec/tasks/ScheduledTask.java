package net.xy.codebase.exec.tasks;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.xy.codebase.exec.tq.TimeoutQueue;

public abstract class ScheduledTask implements ITask {
	private static final Logger LOG = LoggerFactory.getLogger(ScheduledTask.class);
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
		return isStoped() ? 0 : next;
	}

	public void setNext(final long timeoutMs) {
		next = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutMs);
	}

	@Override
	public final void run() {
		if (!isStoped()) {
			if (LOG.isTraceEnabled())
				LOG.trace("Running task [" + this + "]");
			innerRun();
			if (!isStoped() && intervall > 0) {
				final long d = System.nanoTime() - next;
				next += intervall * (d / intervall + 1);
				assert next > System.nanoTime();
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
		return String.format("ScheduledTask [%s]", toStringSuper());
	}

	protected String toStringSuper() {
		final long ival = intervall > 0 ? intervall / 1000000 : 0;
		return String.format("iv=%s,next=%s,%s", ival, (nextRun() - System.nanoTime()) / 1000000, isStoped());
	}
}
