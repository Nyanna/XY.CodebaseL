package net.xy.codebase.concurrent.jdk;

/**
 * Sync object for non-fair locks
 */
public class UnfairLock extends AbstractLock {
	/**
	 * Performs lock. Try immediate barge, backing up to normal acquire on
	 * failure.
	 */
	@Override
	public void lock() {
		if (compareAndSetLocks(0, 1))
			setExclusiveOwnerThread(Thread.currentThread());
		else
			acquire(1);
	}

	@Override
	protected boolean tryAcquire(final int acquires) {
		return nonfairTryAcquire(acquires);
	}
}