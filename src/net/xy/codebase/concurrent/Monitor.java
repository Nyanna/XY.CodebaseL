package net.xy.codebase.concurrent;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.LockSupport;

/**
 * Condition implementation for a {@link AbstractQueuedLock} serving as the
 * basis of a {@link Lock} implementation.
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
		if (t != null && t.getWaitStatus() != Node.CONDITION) {
			unlinkCancelledWaiters();
			t = lastWaiter;
		}
		final Node node = sync.createNode(Thread.currentThread(), Node.CONDITION);
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
		} while (!transferForSignal(first) && (first = firstWaiter) != null);
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
			transferForSignal(first);
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
			if (t.getWaitStatus() != Node.CONDITION) {
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
		if (!sync.isHeldByCurrentThread())
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
		if (!sync.isHeldByCurrentThread())
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
		final int savedState = fullyRelease(node);
		boolean interrupted = false;
		while (!isOnSyncQueue(node)) {
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
		return Thread.interrupted() ? transferAfterCancelledWait(node) ? THROW_IE : REINTERRUPT : 0;
	}

	/**
	 * Throws InterruptedException, reinterrupts current thread, or does
	 * nothing, depending on mode.
	 */
	private void reportInterruptAfterWait(final int interruptMode) throws InterruptedException {
		if (interruptMode == THROW_IE)
			throw new InterruptedException();
		else if (interruptMode == REINTERRUPT)
			selfInterrupt();
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
		final int savedState = fullyRelease(node);
		int interruptMode = 0;
		while (!isOnSyncQueue(node)) {
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
		final int savedState = fullyRelease(node);
		long lastTime = System.nanoTime();
		int interruptMode = 0;
		while (!isOnSyncQueue(node)) {
			if (nanosTimeout <= 0L) {
				transferAfterCancelledWait(node);
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
		final int savedState = fullyRelease(node);
		boolean timedout = false;
		int interruptMode = 0;
		while (!isOnSyncQueue(node)) {
			if (System.currentTimeMillis() > abstime) {
				timedout = transferAfterCancelledWait(node);
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
		final int savedState = fullyRelease(node);
		long lastTime = System.nanoTime();
		boolean timedout = false;
		int interruptMode = 0;
		while (!isOnSyncQueue(node)) {
			if (nanosTimeout <= 0L) {
				timedout = transferAfterCancelledWait(node);
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
	 * Queries whether any threads are waiting on this condition. Implements
	 * {@link AbstractQueuedLock#hasWaiters}.
	 *
	 * @return {@code true} if there are any waiting threads
	 * @throws IllegalMonitorStateException
	 *             if {@link #isHeldExclusively} returns {@code false}
	 */
	protected boolean hasWaiters() {
		if (!sync.isHeldByCurrentThread())
			throw new IllegalMonitorStateException();
		for (Node w = firstWaiter; w != null; w = w.nextWaiter)
			if (w.getWaitStatus() == Node.CONDITION)
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
		if (!sync.isHeldByCurrentThread())
			throw new IllegalMonitorStateException();
		int n = 0;
		for (Node w = firstWaiter; w != null; w = w.nextWaiter)
			if (w.getWaitStatus() == Node.CONDITION)
				++n;
		return n;
	}

	/**
	 * Returns a collection containing those threads that may be waiting on this
	 * Condition. Implements {@link AbstractQueuedLock#getWaitingThreads}.
	 *
	 * @return the collection of threads
	 * @throws IllegalMonitorStateException
	 *             if {@link #isHeldExclusively} returns {@code false}
	 */
	protected Collection<Thread> getWaitingThreads(final List<Thread> list) {
		if (!sync.isHeldByCurrentThread())
			throw new IllegalMonitorStateException();
		for (Node w = firstWaiter; w != null; w = w.nextWaiter)
			if (w.getWaitStatus() == Node.CONDITION) {
				final Thread t = w.getThread();
				if (t != null)
					list.add(t);
			}
		return list;
	}

	// Internal support methods for Conditions

	/**
	 * Returns true if a node, always one that was initially placed on a
	 * condition queue, is now waiting to reacquire on sync queue.
	 *
	 * @param node
	 *            the node
	 * @return true if is reacquiring
	 */
	public boolean isOnSyncQueue(final Node node) {
		if (node.getWaitStatus() == Node.CONDITION || node.getPrev() == null)
			return false;
		if (node.getNextWaiter() != null) // If has successor, it must be on
											// queue
			return true;
		/*
		 * node.prev can be non-null, but not yet on queue because the CAS to
		 * place it on queue can fail. So we have to traverse from tail to make
		 * sure it actually made it. It will always be near the tail in calls to
		 * this method, and unless the CAS failed (which is unlikely), it will
		 * be there, so we hardly ever traverse much.
		 */
		return findNodeFromTail(node);
	}

	/**
	 * Returns true if node is on sync queue by searching backwards from tail.
	 * Called only when needed by isOnSyncQueue.
	 *
	 * @return true if present
	 */
	private boolean findNodeFromTail(final Node node) {
		Node t = sync.tail.get();
		for (;;) {
			if (t == node)
				return true;
			if (t == null)
				return false;
			t = t.getPrev();
		}
	}

	/**
	 * Transfers a node from a condition queue onto sync queue. Returns true if
	 * successful.
	 *
	 * @param node
	 *            the node
	 * @return true if successfully transferred (else the node was cancelled
	 *         before signal).
	 */
	public boolean transferForSignal(final Node node) {
		/*
		 * If cannot change waitStatus, the node has been cancelled.
		 */
		if (!node.compareAndSetWaitStatus(Node.CONDITION, 0))
			return false;

		/*
		 * Splice onto queue and try to set waitStatus of predecessor to
		 * indicate that thread is (probably) waiting. If cancelled or attempt
		 * to set waitStatus fails, wake up to resync (in which case the
		 * waitStatus can be transiently and harmlessly wrong).
		 */
		final Node p = sync.enq(node);
		final int ws = p.getWaitStatus();
		if (ws > 0 || !p.compareAndSetWaitStatus(ws, Node.SIGNAL))
			LockSupport.unpark(node.getThread());
		return true;
	}

	/**
	 * Transfers node, if necessary, to sync queue after a cancelled wait.
	 * Returns true if thread was cancelled before being signalled.
	 *
	 * @param current
	 *            the waiting thread
	 * @param node
	 *            its node
	 * @return true if cancelled before the node was signalled.
	 */
	public boolean transferAfterCancelledWait(final Node node) {
		if (node.compareAndSetWaitStatus(Node.CONDITION, 0)) {
			sync.enq(node);
			return true;
		}
		/*
		 * If we lost out to a signal(), then we can't proceed until it finishes
		 * its enq(). Cancelling during an incomplete transfer is both rare and
		 * transient, so just spin.
		 */
		while (!isOnSyncQueue(node))
			Thread.yield();
		return false;
	}

	/**
	 * Invokes release with current state value; returns saved state. Cancels
	 * node and throws exception on failure.
	 *
	 * @param node
	 *            the condition node for this wait
	 * @return previous sync state
	 */
	public int fullyRelease(final Node node) {
		try {
			final int savedState = sync.getLocks();
			if (sync.release(savedState))
				return savedState;
		} catch (final RuntimeException ex) {
			node.setWaitStatus(Node.CANCELLED);
			throw ex;
		}
		// reach here if release fails
		node.setWaitStatus(Node.CANCELLED);
		throw new IllegalMonitorStateException();
	}

	// Instrumentation methods for conditions

	/**
	 * Queries whether the given ConditionObject uses this synchronizer as its
	 * lock.
	 *
	 * @param condition
	 *            the condition
	 * @return <tt>true</tt> if owned
	 * @throws NullPointerException
	 *             if the condition is null
	 */
	public boolean owns(final AbstractLock sync) {
		if (sync == null)
			throw new NullPointerException();
		return sync == this.sync;
	}
}