package net.xy.codebase.concurrent;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

/**
 * Base of synchronization control for this lock. Subclassed into fair and
 * nonfair versions below. Uses AQS state to represent the number of holds on
 * the lock.
 */
public abstract class AbstractLock extends AbstractQueuedLock implements Lock {

	/**
	 * Performs non-fair tryLock. tryAcquire is implemented in subclasses, but
	 * both need nonfair try for trylock method.
	 */
	protected boolean nonfairTryAcquire(final int acquires) {
		final Thread current = Thread.currentThread();
		final int c = getState();
		if (c == 0) {
			if (compareAndSetState(0, acquires)) {
				setExclusiveOwnerThread(current);
				return true;
			}
		} else if (current == getExclusiveOwnerThread()) {
			final int nextc = c + acquires;
			if (nextc < 0) // overflow
				throw new Error("Maximum lock count exceeded");
			setState(nextc);
			return true;
		}
		return false;
	}

	@Override
	protected boolean tryRelease(final int releases) {
		final int c = getState() - releases;
		if (Thread.currentThread() != getExclusiveOwnerThread())
			throw new IllegalMonitorStateException();
		boolean free = false;
		if (c == 0) {
			free = true;
			setExclusiveOwnerThread(null);
		}
		setState(c);
		return free;
	}

	/**
	 * Acquires in exclusive mode, ignoring interrupts. Implemented by invoking
	 * at least once {@link #tryAcquire}, returning on success. Otherwise the
	 * thread is queued, possibly repeatedly blocking and unblocking, invoking
	 * {@link #tryAcquire} until success. This method can be used to implement
	 * method {@link Lock#lock}.
	 *
	 * @param arg
	 *            the acquire argument. This value is conveyed to
	 *            {@link #tryAcquire} but is otherwise uninterpreted and can
	 *            represent anything you like.
	 */
	public void acquire(final int arg) {
		if (!tryAcquire(arg) && acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
			selfInterrupt();
	}

	protected boolean isHeldExclusively() {
		// While we must in general read state before owner,
		// we don't need to do so to check if current thread is owner
		return getExclusiveOwnerThread() == Thread.currentThread();
	}

	public boolean isHeldByCurrentThread() {
		return isHeldExclusively();
	}

	@Override
	public Monitor newCondition() {
		return new Monitor(this);
	}

	@Override
	public void lockInterruptibly() throws InterruptedException {
		acquireInterruptibly(1);
	}

	@Override
	public boolean tryLock() {
		return nonfairTryAcquire(1);
	}

	@Override
	public boolean tryLock(final long time, final TimeUnit unit) throws InterruptedException {
		return tryAcquireNanos(1, unit.toNanos(time));
	}

	@Override
	public void unlock() {
		release(1);
	}

	public void tryUnlock() {
		if (isHeldByCurrentThread())
			unlock();
	}
}