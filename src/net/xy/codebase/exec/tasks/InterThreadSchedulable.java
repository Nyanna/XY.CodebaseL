package net.xy.codebase.exec.tasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.xy.codebase.exec.IInterThreads;

/**
 * implementation which supports intervall timeouts to an referenced
 * timeoutqueue
 *
 * @author Xyan
 *
 */
public class InterThreadSchedulable<E extends Enum<E>> extends AbstractInterThreadRunnable<E> {
	private static final Logger LOG = LoggerFactory.getLogger(InterThreadSchedulable.class);
	/**
	 * back reference
	 */
	private final IInterThreads<E> it;

	/**
	 * default
	 *
	 * @param thread
	 * @param run
	 */
	public InterThreadSchedulable(final E thread, final Runnable run, final IInterThreads<E> it) {
		super(thread, run);
		this.it = it;
	}

	@Override
	public void schedule(final ITask capsule) {
		if (capsule.nextRun() <= 0) {
			if (LOG.isTraceEnabled())
				LOG.trace("Schedule directly [" + this + "]");
			capsule.run();
		} else {
			if (LOG.isTraceEnabled())
				LOG.trace("Schedule queued [" + this + "]");
			it.start(capsule);
		}
	}

	@Override
	public void run() {
		if (LOG.isTraceEnabled())
			LOG.trace("Insert in threadqueue [" + this + "]");
		it.put(thread, run);
	}

	@Override
	public String toString() {
		return "InterTSched [" + run.getClass() + "]";
	}
}