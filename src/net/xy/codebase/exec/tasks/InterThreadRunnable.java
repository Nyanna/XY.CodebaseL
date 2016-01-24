package net.xy.codebase.exec.tasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.xy.codebase.exec.IInterThreads;

/**
 * concrete implementation, which don't supports timeouts
 *
 * @author Xyan
 *
 */
public class InterThreadRunnable<E extends Enum<E>> extends AbstractInterThreadRunnable<E> {
	private static final Logger LOG = LoggerFactory.getLogger(InterThreadRunnable.class);
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
	public InterThreadRunnable(final E thread, final Runnable run, final IInterThreads<E> it) {
		super(thread, run);
		this.it = it;
	}

	@Override
	public void schedule(final ITask run) {
		if (LOG.isTraceEnabled())
			LOG.trace("insert in threadqueue no schedule [" + this + "]");
		it.put(thread, run);
	}

	@Override
	public void run() {
		run.run();
	}

	@Override
	public String toString() {
		return "Inter [" + run.getClass().getSimpleName() + "]";
	}
}