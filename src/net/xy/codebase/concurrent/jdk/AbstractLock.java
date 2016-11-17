package net.xy.codebase.concurrent.jdk;

import java.util.Collection;
import java.util.List;
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
		final int c = getLocks();
		if (c == 0) {
			if (compareAndSetLocks(0, acquires)) {
				setExclusiveOwnerThread(current);
				return true;
			}
		} else if (current == getExclusiveOwnerThread()) {
			final int nextc = c + acquires;
			if (nextc < 0) // overflow
				throw new Error("Maximum lock count exceeded");
			setLocks(nextc);
			return true;
		}
		return false;
	}

	@Override
	protected boolean tryRelease(final int releases) {
		final int c = getLocks() - releases;
		if (Thread.currentThread() != getExclusiveOwnerThread())
			throw new IllegalMonitorStateException();
		boolean free = false;
		if (c == 0) {
			free = true;
			setExclusiveOwnerThread(null);
		}
		setLocks(c);
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
			Thread.currentThread().interrupt();
	}

	public boolean isHeldByCurrentThread() {
		// While we must in general read state before owner,
		// we don't need to do so to check if current thread is owner
		return getExclusiveOwnerThread() == Thread.currentThread();
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

	// Queue inspection methods

	/**
	 * Queries whether any threads are waiting to acquire. Note that because
	 * cancellations due to interrupts and timeouts may occur at any time, a
	 * {@code true} return does not guarantee that any other thread will ever
	 * acquire.
	 *
	 * <p>
	 * In this implementation, this operation returns in constant time.
	 *
	 * @return {@code true} if there may be other threads waiting to acquire
	 */
	public boolean hasQueuedThreads() {
		return head != tail;
	}

	/**
	 * Queries whether any threads have ever contended to acquire this
	 * synchronizer; that is if an acquire method has ever blocked.
	 *
	 * <p>
	 * In this implementation, this operation returns in constant time.
	 *
	 * @return {@code true} if there has ever been contention
	 */
	public boolean hasContended() {
		return head != null;
	}

	/**
	 * Returns the first (longest-waiting) thread in the queue, or {@code null}
	 * if no threads are currently queued.
	 *
	 * <p>
	 * In this implementation, this operation normally returns in constant time,
	 * but may iterate upon contention if other threads are concurrently
	 * modifying the queue.
	 *
	 * @return the first (longest-waiting) thread in the queue, or {@code null}
	 *         if no threads are currently queued
	 */
	public Thread getFirstQueuedThread() {
		// handle only fast path, else relay
		return head == tail ? null : fullGetFirstQueuedThread();
	}

	/**
	 * Version of getFirstQueuedThread called when fastpath fails
	 */
	private Thread fullGetFirstQueuedThread() {
		/*
		 * The first node is normally h.next. Try to get its thread field,
		 * ensuring consistent reads: If thread field is nulled out or s.prev is
		 * no longer head, then some other thread(s) concurrently performed
		 * setHead in between some of our reads. We try this twice before
		 * resorting to traversal.
		 */
		Node h, s;
		Thread st;
		if ((h = head.get()) != null && (s = h.getNextWaiter()) != null && s.getPrev() == head.get()
				&& (st = s.getThread()) != null
				|| (h = head.get()) != null && (s = h.getNextWaiter()) != null && s.getPrev() == head.get()
						&& (st = s.getThread()) != null)
			return st;

		/*
		 * Head's next field might not have been set yet, or may have been unset
		 * after setHead. So we must check to see if tail is actually first
		 * node. If not, we continue on, safely traversing from tail back to
		 * head to find first, guaranteeing termination.
		 */

		Node t = tail.get();
		Thread firstThread = null;
		while (t != null && t != head.get()) {
			final Thread tt = t.getThread();
			if (tt != null)
				firstThread = tt;
			t = t.getPrev();
		}
		return firstThread;
	}

	/**
	 * Returns true if the given thread is currently queued.
	 *
	 * <p>
	 * This implementation traverses the queue to determine presence of the
	 * given thread.
	 *
	 * @param thread
	 *            the thread
	 * @return {@code true} if the given thread is on the queue
	 * @throws NullPointerException
	 *             if the thread is null
	 */
	public boolean isQueued(final Thread thread) {
		if (thread == null)
			throw new NullPointerException();
		for (Node p = tail.get(); p != null; p = p.getPrev())
			if (p.getThread() == thread)
				return true;
		return false;
	}

	/**
	 * Return {@code true} if the queue is empty or if the given thread is at
	 * the head of the queue. This is reliable only if <tt>current</tt> is
	 * actually Thread.currentThread() of caller.
	 */
	public boolean isFirst(final Thread current) {
		Node h, s;
		return (h = head.get()) == null || (s = h.getNextWaiter()) != null && s.getThread() == current
				|| fullIsFirst(current);
	}

	private boolean fullIsFirst(final Thread current) {
		// same idea as fullGetFirstQueuedThread
		Node h, s;
		Thread firstThread = null;
		if ((h = head.get()) != null && (s = h.getNextWaiter()) != null && s.getPrev() == head.get()
				&& (firstThread = s.getThread()) != null)
			return firstThread == current;
		Node t = tail.get();
		while (t != null && t != head.get()) {
			final Thread tt = t.getThread();
			if (tt != null)
				firstThread = tt;
			t = t.getPrev();
		}
		return firstThread == current || firstThread == null;
	}

	// Instrumentation and monitoring methods

	/**
	 * Returns an estimate of the number of threads waiting to acquire. The
	 * value is only an estimate because the number of threads may change
	 * dynamically while this method traverses internal data structures. This
	 * method is designed for use in monitoring system state, not for
	 * synchronization control.
	 *
	 * @return the estimated number of threads waiting to acquire
	 */
	public int getQueueLength() {
		int n = 0;
		for (Node p = tail.get(); p != null; p = p.getPrev())
			if (p.getThread() != null)
				++n;
		return n;
	}

	/**
	 * Returns a collection containing threads that may be waiting to acquire.
	 * Because the actual set of threads may change dynamically while
	 * constructing this result, the returned collection is only a best-effort
	 * estimate. The elements of the returned collection are in no particular
	 * order. This method is designed to facilitate construction of subclasses
	 * that provide more extensive monitoring facilities.
	 *
	 * @return the collection of threads
	 */
	public Collection<Thread> getQueuedThreads(final List<Thread> list) {
		for (Node p = tail.get(); p != null; p = p.getPrev()) {
			final Thread t = p.getThread();
			if (t != null)
				list.add(t);
		}
		return list;
	}

	/**
	 * Returns a collection containing threads that may be waiting to acquire in
	 * exclusive mode. This has the same properties as {@link #getQueuedThreads}
	 * except that it only returns those threads waiting due to an exclusive
	 * acquire.
	 *
	 * @return the collection of threads
	 */
	public Collection<Thread> getExclusiveQueuedThreads(final List<Thread> list) {
		for (Node p = tail.get(); p != null; p = p.getPrev()) {
			final Thread t = p.getThread();
			if (t != null)
				list.add(t);
		}
		return list;
	}

	/**
	 * Returns a string identifying this synchronizer, as well as its state. The
	 * state, in brackets, includes the String {@code "State ="} followed by the
	 * current value of {@link #getState}, and either {@code "nonempty"} or
	 * {@code "empty"} depending on whether the queue is empty.
	 *
	 * @return a string identifying this synchronizer, as well as its state
	 */
	@Override
	public String toString() {
		final int s = getLocks();
		final String q = hasQueuedThreads() ? "non" : "";
		return super.toString() + "[State = " + s + ", " + q + "empty queue]";
	}
}