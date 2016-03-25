package net.xy.codebase.exec.tasks;

import net.xy.codebase.collection.IPriority;
import net.xy.codebase.exec.IInterThreads;

/**
 * implementation with priority support
 *
 * @author Xyan
 *
 * @param <E>
 */
public class PriorityInterThreadRunnable<E extends Enum<E>> extends InterThreadRunnable<E>implements IPriority {
	/**
	 * context priority for priority queues
	 */
	private final int prio;

	/**
	 * default
	 *
	 * @param thread
	 * @param run
	 */
	public PriorityInterThreadRunnable(final E thread, final Runnable run, final IInterThreads<E> it, final int prio) {
		super(thread, run, it);
		this.prio = prio;
	}

	@Override
	public int getPriority() {
		return prio;
	}
}
