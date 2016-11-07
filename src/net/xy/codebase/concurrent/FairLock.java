package net.xy.codebase.concurrent;

/**
 * Sync object for fair locks
 */
public class FairLock extends AbstractLock {
	@Override
	public void lock() {
		acquire(1);
	}

	/**
	 * Fair version of tryAcquire. Don't grant access unless recursive call or
	 * no waiters or is first.
	 */
	@Override
	protected boolean tryAcquire(final int acquires) {
		final Thread current = Thread.currentThread();
		final int c = getState();
		if (c == 0) {
			if (isFirst(current) && compareAndSetState(0, acquires)) {
				setExclusiveOwnerThread(current);
				return true;
			}
		} else if (current == getExclusiveOwnerThread()) {
			final int nextc = c + acquires;
			if (nextc < 0)
				throw new Error("Maximum lock count exceeded");
			setState(nextc);
			return true;
		}
		return false;
	}
}