package net.xy.codebase.exec;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.xy.codebase.exec.IInterThreads.IJobObserver;
import net.xy.codebase.exec.tasks.ITask;

public class JobObserver<J> implements IJobObserver<J> {
	private static final Logger LOG = LoggerFactory.getLogger(JobObserver.class);
	private static final long IGNORE_LATENCY = TimeUnit.MILLISECONDS.toNanos(5);
	/**
	 * for failure logging
	 */
	private long lastDropMessage;
	private int lastDropCounter, dropCount;
	private long lastDelayMessage;
	private int lastDelayCounter, delayCount;

	@Override
	public void jobAdded(final J target, final Runnable job) {
	}

	@Override
	public boolean jobStart(final J target, final Runnable job, final IPerfCounter measure) {
		return true;
	}

	@Override
	public void jobEnd(final J target, final Runnable job, final IPerfCounter measure, final long duration) {
	}

	@Override
	public void jobDroped(final J target, final Runnable job, final int size) {
		final long now = System.currentTimeMillis();
		if (lastDropMessage < now - 100) {
			lastDropMessage = now;
			if (lastDropCounter > 0) {
				LOG.error("Thread overloaded droping job and others [" + lastDropCounter + "][" + job + "]");
				lastDropCounter = 0;
			} else
				LOG.error("Target thread too full droping job [" + target + "][" + size + "][" + job + "]");
		} else
			lastDropCounter++;
		dropCount++;
	}

	public int getDropCount() {
		return dropCount;
	}

	public void resetDropCount() {
		dropCount = 0;
	}

	public int getDelayCount() {
		return delayCount;
	}

	public void resetDelayCount() {
		delayCount = 0;
	}

	@Override
	public void taskAdded(final ITask t) {

	}

	@Override
	public void taskStarted(final ITask t, final long latency) {
		if (latency < IGNORE_LATENCY)
			return;

		final long now = System.currentTimeMillis();
		if (lastDelayMessage < now - 100) {
			lastDelayMessage = now;
			if (lastDelayCounter > 0) {
				LOG.error("Thread overloaded tasks gets delayed [" + lastDelayCounter + "]["
						+ TimeUnit.NANOSECONDS.toMillis(latency) + "][" + t + "]");
				lastDelayCounter = 0;
			} else
				LOG.error("Target overloaded task gets delayed [" + TimeUnit.NANOSECONDS.toMillis(latency) + "][" + t
						+ "]");
		} else
			lastDelayCounter++;
		delayCount++;
	}

	@Override
	public void taskStoped(final ITask t) {
	}
}
