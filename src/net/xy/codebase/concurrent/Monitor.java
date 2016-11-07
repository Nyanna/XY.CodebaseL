package net.xy.codebase.concurrent;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.LockSupport;

/**
 * Condition implementation for a {@link AbstractQueuedLock} serving as
 * the basis of a {@link Lock} implementation.
 *
 * <p>
 * Method documentation for this class describes mechanics, not behavioral
 * specifications from the point of view of Lock and Condition users. Exported
 * versions of this class will in general need to be accompanied by
 * documentation describing condition semantics that rely on those of the
 * associated <tt>AbstractQueuedSynchronizer</tt>.
 *
 */
public class Monitor implements Condition {
	/** First node of condition queue. */
	private Node firstWaiter;
	/** Last node of condition queue. */
	private Node lastWaiter;
	/** sync reference */
	private final AbstractLock sync;

	/**
	 * Creates a new <tt>ConditionObject</tt> instance.
	 */
	public Monitor(final AbstractLock sync) {
		this.sync = sync;
	}

	/**
	 * Convenience method to interrupt current thread.
	 */
	private static void selfInterrupt() {
		Thread.currentThread().interrupt();
	}

	// Internal methods

	/**
	 * Adds a new waiter to wait queue.
	 *
	 * @return its new wait node
	 */
	private Node addConditionWaiter() {
		Node t = lastWaiter;
		// If lastWaiter is cancelled, clean out.
		if (t != null && t.waitStatus.get() != Node.CONDITION) {
			unlinkCancelledWaiters();
			t = lastWaiter;
		}
		final Node node = new Node(Thread.currentThread(), Node.CONDITION);
		if (t == null)
			firstWaiter = node;
		else
			t.nextWaiter = node;
		lastWaiter = node;
		return node;
	}

	/**
	 * Removes and transfers nodes until hit non-cancelled one or null. Split
	 * out from signal in part to encourage compilers to inline the case of no
	 * waiters.
	 *
	 * @param first
	 *            (non-null) the first node on condition queue
	 */
	private void doSignal(Node first) {
		do {
			if ((firstWaiter = first.nextWaiter) == null)
				lastWaiter = null;
			first.nextWaiter = null;
		} while (!sync.transferForSignal(first) && (first = firstWaiter) != null);
	}

	/**
	 * Removes and transfers all nodes.
	 *
	 * @param first
	 *            (non-null) the first node on condition queue
	 */
	private void doSignalAll(Node first) {
		lastWaiter = firstWaiter = null;
		do {
			final Node next = first.nextWaiter;
			first.nextWaiter = null;
			sync.transferForSignal(first);
			first = next;
		} while (first != null);
	}

	/**
	 * Unlinks cancelled waiter nodes from condition queue. Called only while
	 * holding lock. This is called when cancellation occurred during condition
	 * wait, and upon insertion of a new waiter when lastWaiter is seen to have
	 * been cancelled. This method is needed to avoid garbage retention in the
	 * absence of signals. So even though it may require a full traversal, it
	 * comes into play only when timeouts or cancellations occur in the absence
	 * of signals. It traverses all nodes rather than stopping at a particular
	 * target to unlink all pointers to garbage nodes without requiring many
	 * re-traversals during cancellation storms.
	 */
	private void unlinkCancelledWaiters() {
		Node t = firstWaiter;
		Node trail = null;
		while (t != null) {
			final Node next = t.nextWaiter;
			if (t.waitStatus.get() != Node.CONDITION) {
				t.nextWaiter = null;
				if (trail == null)
					firstWaiter = next;
				else
					trail.nextWaiter = next;
				if (next == null)
					lastWaiter = trail;
			} else
				trail = t;
			t = next;
		}
	}

	// public methods

	/**
	 * Moves the longest-waiting thread, if one exists, from the wait queue for
	 * this condition to the wait queue for the owning lock.
	 *
	 * @throws IllegalMonitorStateException
	 *             if {@link #isHeldExclusively} returns {@code false}
	 */
	@Override
	public void signal() {
		if (!sync.isHeldExclusively())
			throw new IllegalMonitorStateException();
		final Node first = firstWaiter;
		if (first != null)
			doSignal(first);
	}

	/**
	 * Moves all threads from the wait queue for this condition to the wait
	 * queue for the owning lock.
	 *
	 * @throws IllegalMonitorStateException
	 *             if {@link #isHeldExclusively} returns {@code false}
	 */
	@Override
	public void signalAll() {
		if (!sync.isHeldExclusively())
			throw new IllegalMonitorStateException();
		final Node first = firstWaiter;
		if (first != null)
			doSignalAll(first);
	}

	/**
	 * Implements uninterruptible condition wait.
	 * <ol>
	 * <li>Save lock state returned by {@link #getState}
	 * <li>Invoke {@link #release} with saved state as argument, throwing
	 * IllegalMonitorStateException if it fails.
	 * <li>Block until signalled
	 * <li>Reacquire by invoking specialized version of {@link #acquire} with
	 * saved state as argument.
	 * </ol>
	 */
	@Override
	public void awaitUninterruptibly() {
		final Node node = addConditionWaiter();
		final int savedState = sync.fullyRelease(node);
		boolean interrupted = false;
		while (!sync.isOnSyncQueue(node)) {
			LockSupport.park(this);
			if (Thread.interrupted())
				interrupted = true;
		}
		if (sync.acquireQueued(node, savedState) || interrupted)
			selfInterrupt();
	}

	/*
	 * For interruptible waits, we need to track whether to throw
	 * InterruptedException, if interrupted while blocked on condition, versus
	 * reinterrupt current thread, if interrupted while blocked waiting to
	 * re-acquire.
	 */

	/** Mode meaning to reinterrupt on exit from wait */
	private static final int REINTERRUPT = 1;
	/** Mode meaning to throw InterruptedException on exit from wait */
	private static final int THROW_IE = -1;

	/**
	 * Checks for interrupt, returning THROW_IE if interrupted before signalled,
	 * REINTERRUPT if after signalled, or 0 if not interrupted.
	 */
	private int checkInterruptWhileWaiting(final Node node) {
		return Thread.interrupted() ? sync.transferAfterCancelledWait(node) ? THROW_IE : REINTERRUPT : 0;
	}

	/**
	 * Throws InterruptedException, reinterrupts current thread, or does
	 * nothing, depending on mode.
	 */
	private void reportInterruptAfterWait(final int interruptMode) throws InterruptedException {
		if (interruptMode == THROW_IE)
			throw new InterruptedException();
		else if (interruptMode == REINTERRUPT)
			AbstractQueuedLock.selfInterrupt();
	}

	/**
	 * Implements interruptible condition wait.
	 * <ol>
	 * <li>If current thread is interrupted, throw InterruptedException
	 * <li>Save lock state returned by {@link #getState}
	 * <li>Invoke {@link #release} with saved state as argument, throwing
	 * IllegalMonitorStateException if it fails.
	 * <li>Block until signalled or interrupted
	 * <li>Reacquire by invoking specialized version of {@link #acquire} with
	 * saved state as argument.
	 * <li>If interrupted while blocked in step 4, throw exception
	 * </ol>
	 */
	@Override
	public void await() throws InterruptedException {
		if (Thread.interrupted())
			throw new InterruptedException();
		final Node node = addConditionWaiter();
		final int savedState = sync.fullyRelease(node);
		int interruptMode = 0;
		while (!sync.isOnSyncQueue(node)) {
			LockSupport.park(this);
			if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
				break;
		}
		if (sync.acquireQueued(node, savedState) && interruptMode != THROW_IE)
			interruptMode = REINTERRUPT;
		if (node.nextWaiter != null)
			unlinkCancelledWaiters();
		if (interruptMode != 0)
			reportInterruptAfterWait(interruptMode);
	}

	/**
	 * Implements timed condition wait.
	 * <ol>
	 * <li>If current thread is interrupted, throw InterruptedException
	 * <li>Save lock state returned by {@link #getState}
	 * <li>Invoke {@link #release} with saved state as argument, throwing
	 * IllegalMonitorStateException if it fails.
	 * <li>Block until signalled, interrupted, or timed out
	 * <li>Reacquire by invoking specialized version of {@link #acquire} with
	 * saved state as argument.
	 * <li>If interrupted while blocked in step 4, throw InterruptedException
	 * </ol>
	 */
	@Override
	public long awaitNanos(long nanosTimeout) throws InterruptedException {
		if (Thread.interrupted())
			throw new InterruptedException();
		final Node node = addConditionWaiter();
		final int savedState = sync.fullyRelease(node);
		long lastTime = System.nanoTime();
		int interruptMode = 0;
		while (!sync.isOnSyncQueue(node)) {
			if (nanosTimeout <= 0L) {
				sync.transferAfterCancelledWait(node);
				break;
			}
			LockSupport.parkNanos(this, nanosTimeout);
			if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
				break;

			final long now = System.nanoTime();
			nanosTimeout -= now - lastTime;
			lastTime = now;
		}
		if (sync.acquireQueued(node, savedState) && interruptMode != THROW_IE)
			interruptMode = REINTERRUPT;
		if (node.nextWaiter != null)
			unlinkCancelledWaiters();
		if (interruptMode != 0)
			reportInterruptAfterWait(interruptMode);
		return nanosTimeout - (System.nanoTime() - lastTime);
	}

	/**
	 * Implements absolute timed condition wait.
	 * <ol>
	 * <li>If current thread is interrupted, throw InterruptedException
	 * <li>Save lock state returned by {@link #getState}
	 * <li>Invoke {@link #release} with saved state as argument, throwing
	 * IllegalMonitorStateException if it fails.
	 * <li>Block until signalled, interrupted, or timed out
	 * <li>Reacquire by invoking specialized version of {@link #acquire} with
	 * saved state as argument.
	 * <li>If interrupted while blocked in step 4, throw InterruptedException
	 * <li>If timed out while blocked in step 4, return false, else true
	 * </ol>
	 */
	@Override
	public boolean awaitUntil(final Date deadline) throws InterruptedException {
		if (deadline == null)
			throw new NullPointerException();
		final long abstime = deadline.getTime();
		if (Thread.interrupted())
			throw new InterruptedException();
		final Node node = addConditionWaiter();
		final int savedState = sync.fullyRelease(node);
		boolean timedout = false;
		int interruptMode = 0;
		while (!sync.isOnSyncQueue(node)) {
			if (System.currentTimeMillis() > abstime) {
				timedout = sync.transferAfterCancelledWait(node);
				break;
			}
			LockSupport.parkUntil(this, abstime);
			if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
				break;
		}
		if (sync.acquireQueued(node, savedState) && interruptMode != THROW_IE)
			interruptMode = REINTERRUPT;
		if (node.nextWaiter != null)
			unlinkCancelledWaiters();
		if (interruptMode != 0)
			reportInterruptAfterWait(interruptMode);
		return !timedout;
	}

	/**
	 * Implements timed condition wait.
	 * <ol>
	 * <li>If current thread is interrupted, throw InterruptedException
	 * <li>Save lock state returned by {@link #getState}
	 * <li>Invoke {@link #release} with saved state as argument, throwing
	 * IllegalMonitorStateException if it fails.
	 * <li>Block until signalled, interrupted, or timed out
	 * <li>Reacquire by invoking specialized version of {@link #acquire} with
	 * saved state as argument.
	 * <li>If interrupted while blocked in step 4, throw InterruptedException
	 * <li>If timed out while blocked in step 4, return false, else true
	 * </ol>
	 */
	@Override
	public boolean await(final long time, final TimeUnit unit) throws InterruptedException {
		if (unit == null)
			throw new NullPointerException();
		long nanosTimeout = unit.toNanos(time);
		if (Thread.interrupted())
			throw new InterruptedException();
		final Node node = addConditionWaiter();
		final int savedState = sync.fullyRelease(node);
		long lastTime = System.nanoTime();
		boolean timedout = false;
		int interruptMode = 0;
		while (!sync.isOnSyncQueue(node)) {
			if (nanosTimeout <= 0L) {
				timedout = sync.transferAfterCancelledWait(node);
				break;
			}
			LockSupport.parkNanos(this, nanosTimeout);
			if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
				break;
			final long now = System.nanoTime();
			nanosTimeout -= now - lastTime;
			lastTime = now;
		}
		if (sync.acquireQueued(node, savedState) && interruptMode != THROW_IE)
			interruptMode = REINTERRUPT;
		if (node.nextWaiter != null)
			unlinkCancelledWaiters();
		if (interruptMode != 0)
			reportInterruptAfterWait(interruptMode);
		return !timedout;
	}

	// support for instrumentation

	/**
	 * Returns true if this condition was created by the given synchronization
	 * object.
	 *
	 * @return {@code true} if owned
	 */
	final boolean isOwnedBy(final AbstractQueuedLock sync) {
		return sync == this.sync;
	}

	/**
	 * Queries whether any threads are waiting on this condition. Implements
	 * {@link AbstractQueuedLock#hasWaiters}.
	 *
	 * @return {@code true} if there are any waiting threads
	 * @throws IllegalMonitorStateException
	 *             if {@link #isHeldExclusively} returns {@code false}
	 */
	protected boolean hasWaiters() {
		if (!sync.isHeldExclusively())
			throw new IllegalMonitorStateException();
		for (Node w = firstWaiter; w != null; w = w.nextWaiter)
			if (w.waitStatus.get() == Node.CONDITION)
				return true;
		return false;
	}

	/**
	 * Returns an estimate of the number of threads waiting on this condition.
	 * Implements {@link AbstractQueuedLock#getWaitQueueLength}.
	 *
	 * @return the estimated number of waiting threads
	 * @throws IllegalMonitorStateException
	 *             if {@link #isHeldExclusively} returns {@code false}
	 */
	protected int getWaitQueueLength() {
		if (!sync.isHeldExclusively())
			throw new IllegalMonitorStateException();
		int n = 0;
		for (Node w = firstWaiter; w != null; w = w.nextWaiter)
			if (w.waitStatus.get() == Node.CONDITION)
				++n;
		return n;
	}

	/**
	 * Returns a collection containing those threads that may be waiting on this
	 * Condition. Implements
	 * {@link AbstractQueuedLock#getWaitingThreads}.
	 *
	 * @return the collection of threads
	 * @throws IllegalMonitorStateException
	 *             if {@link #isHeldExclusively} returns {@code false}
	 */
	protected Collection<Thread> getWaitingThreads(final List<Thread> list) {
		if (!sync.isHeldExclusively())
			throw new IllegalMonitorStateException();
		for (Node w = firstWaiter; w != null; w = w.nextWaiter)
			if (w.waitStatus.get() == Node.CONDITION) {
				final Thread t = w.thread;
				if (t != null)
					list.add(t);
			}
		return list;
	}
}