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
	public boolean schedule(final ITask capsule) {
		if (capsule.nextRun() <= 0) {
			if (LOG.isTraceEnabled())
				LOG.trace("Schedule directly [" + this + "]");
			capsule.run();
			return true;
		} else {
			if (LOG.isTraceEnabled())
				LOG.trace("Schedule queued [" + this + "]");
			return it.start(capsule);
		}
	}

	@Override
	public void run() {
		if (LOG.isTraceEnabled())
			LOG.trace("Insert in threadqueue [" + this + "]");
		it.run(thread, run);
	}

	@Override
	public String toString() {
		return "InterTSched [" + run.getClass() + "]";
	}
}