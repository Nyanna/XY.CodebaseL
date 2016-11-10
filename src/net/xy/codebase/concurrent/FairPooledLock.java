package net.xy.codebase.concurrent;

import net.xy.codebase.mem.ConcurrentPool;

/**
 * Sync object for fair locks
 */
public class FairPooledLock extends AbstractLock {
	private final ConcurrentPool<Node> pool = new ConcurrentPool<Node>() {
		@Override
		protected Node newObject() {
			return new Node(null, 0);
		}
	};

	@Override
	public Node createNode(final Thread thread, final int waitStatus) {
		final Node res = pool.obtain();
		res.reset(thread, waitStatus);
		return res;
	}

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
		final int c = getLocks();
		if (c == 0) {
			if (isFirst(current) && compareAndSetLocks(0, acquires)) {
				setExclusiveOwnerThread(current);
				return true;
			}
		} else if (current == getExclusiveOwnerThread()) {
			final int nextc = c + acquires;
			if (nextc < 0)
				throw new Error("Maximum lock count exceeded");
			setLocks(nextc);
			return true;
		}
		return false;
	}
}