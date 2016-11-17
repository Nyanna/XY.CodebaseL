package net.xy.codebase.concurrent;

/**
 * Semaphore implementation for for multiple endless waiters until a resource
 * gots available.
 *
 * @author Xyan
 *
 */
public class Semaphore extends Sync {
	/**
	 * increments state and wakes next waiting thread
	 *
	 * @return
	 */
	@Override
	public boolean call() {
		modCounter.incrementAndGet();
		return wakeNext(tail.get());
	}
}
