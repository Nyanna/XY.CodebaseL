/*===========================================================================
 * Licensed Materials - Property of IBM
 * "Restricted Materials of IBM"
 *
 * IBM SDK, Java(tm) Technology Edition, v6
 * (C) Copyright IBM Corp. 2013, 2013. All Rights Reserved
 *
 * US Government Users Restricted Rights - Use, duplication or disclosure
 * restricted by GSA ADP Schedule Contract with IBM Corp.
 *===========================================================================
 */
/*
 * %W% %E%
 *
 * Copyright (c) 2006,2010 Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package net.xy.codebase.concurrent;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

public abstract class AbstractQueuedLock extends AbstractOwnableLock {

	/**
	 * Creates a new <tt>AbstractQueuedSynchronizer</tt> instance with initial
	 * synchronization state of zero.
	 */
	protected AbstractQueuedLock() {
	}

	/**
	 * Head of the wait queue, lazily initialized. Except for initialization, it
	 * is modified only via method setHead. Note: If head exists, its waitStatus
	 * is guaranteed not to be CANCELLED.
	 */
	private final AtomicReference<Node> head = new AtomicReference<Node>();

	/**
	 * Tail of the wait queue, lazily initialized. Modified only via method enq
	 * to add new wait node.
	 */
	private final AtomicReference<Node> tail = new AtomicReference<Node>();

	/**
	 * The synchronization state.
	 */
	private final AtomicInteger state = new AtomicInteger();

	/**
	 * Returns the current value of synchronization state. This operation has
	 * memory semantics of a <tt>volatile</tt> read.
	 *
	 * @return current state value
	 */
	protected final int getState() {
		return state.get();
	}

	/**
	 * Sets the value of synchronization state. This operation has memory
	 * semantics of a <tt>volatile</tt> write.
	 *
	 * @param newState
	 *            the new state value
	 */
	protected final void setState(final int newState) {
		state.set(newState);
	}

	// Queuing utilities

	/**
	 * The number of nanoseconds for which it is faster to spin rather than to
	 * use timed park. A rough estimate suffices to improve responsiveness with
	 * very short timeouts.
	 */
	static final long spinForTimeoutThreshold = 1000L;

	/**
	 * Inserts node into queue, initializing if necessary. See picture above.
	 *
	 * @param node
	 *            the node to insert
	 * @return node's predecessor
	 */
	private Node enq(final Node node) {
		for (;;) {
			final Node t = tail.get();
			if (t == null) { // Must initialize
				final Node h = new Node(); // Dummy header
				h.next.set(node);
				node.prev = h;
				if (head.compareAndSet(null, h)) {
					tail.set(node);
					return h;
				}
			} else {
				node.prev = t;
				if (compareAndSetTail(t, node)) {
					t.next.set(node);
					return t;
				}
			}
		}
	}

	/**
	 * Creates and enqueues node for given thread and mode.
	 *
	 * @param current
	 *            the thread
	 * @param mode
	 *            Node.EXCLUSIVE for exclusive, Node.SHARED for shared
	 * @return the new node
	 */
	protected Node addWaiter(final Node mode) {
		final Node node = new Node(Thread.currentThread(), mode);
		// Try the fast path of enq; backup to full enq on failure
		final Node pred = tail.get();
		if (pred != null) {
			node.prev = pred;
			if (compareAndSetTail(pred, node)) {
				pred.next.set(node);
				return node;
			}
		}
		enq(node);
		return node;
	}

	/**
	 * Sets head of queue to be node, thus dequeuing. Called only by acquire
	 * methods. Also nulls out unused fields for sake of GC and to suppress
	 * unnecessary signals and traversals.
	 *
	 * @param node
	 *            the node
	 */
	private void setHead(final Node node) {
		head.set(node);
		node.thread = null;
		node.prev = null;
	}

	/**
	 * Wakes up node's successor, if one exists.
	 *
	 * @param node
	 *            the node
	 */
	private void unparkSuccessor(final Node node) {
		/*
		 * If status is negative (i.e., possibly needing signal) try to clear in
		 * anticipation of signalling. It is OK if this fails or if status is
		 * changed by waiting thread.
		 */
		final int ws = node.waitStatus.get();
		if (ws < 0)
			compareAndSetWaitStatus(node, ws, 0);

		/*
		 * Thread to unpark is held in successor, which is normally just the
		 * next node. But if cancelled or apparently null, traverse backwards
		 * from tail to find the actual non-cancelled successor.
		 */
		Node s = node.next.get();
		if (s == null || s.waitStatus.get() > 0) {
			s = null;
			for (Node t = tail.get(); t != null && t != node; t = t.prev)
				if (t.waitStatus.get() <= 0)
					s = t;
		}
		if (s != null)
			LockSupport.unpark(s.thread);
	}

	// Utilities for various versions of acquire

	/**
	 * Cancels an ongoing attempt to acquire.
	 *
	 * @param node
	 *            the node
	 */
	private void cancelAcquire(final Node node) {
		// Ignore if node doesn't exist
		if (node == null)
			return;

		node.thread = null;

		// Skip cancelled predecessors
		Node pred = node.prev;
		while (pred.waitStatus.get() > 0)
			node.prev = pred = pred.prev;

		// predNext is the apparent node to unsplice. CASes below will
		// fail if not, in which case, we lost race vs another cancel
		// or signal, so no further action is necessary.
		final Node predNext = pred.next.get();

		// Can use unconditional write instead of CAS here.
		// After this atomic step, other Nodes can skip past us.
		// Before, we are free of interference from other threads.
		node.waitStatus.set(Node.CANCELLED);

		// If we are the tail, remove ourselves.
		if (node == tail.get() && compareAndSetTail(node, pred))
			pred.next.compareAndSet(predNext, null);
		else {
			// If successor needs signal, try to set pred's next-link
			// so it will get one. Otherwise wake it up to propagate.
			int ws;
			if (pred != head.get() && ((ws = pred.waitStatus.get()) == Node.SIGNAL
					|| ws <= 0 && compareAndSetWaitStatus(pred, ws, Node.SIGNAL)) && pred.thread != null) {
				final Node next = node.next.get();
				if (next != null && next.waitStatus.get() <= 0)
					pred.next.compareAndSet(predNext, next);
			} else
				unparkSuccessor(node);

			node.next.set(node); // help GC
		}
	}

	/**
	 * Checks and updates status for a node that failed to acquire. Returns true
	 * if thread should block. This is the main signal control in all acquire
	 * loops. Requires that pred == node.prev
	 *
	 * @param pred
	 *            node's predecessor holding status
	 * @param node
	 *            the node
	 * @return {@code true} if thread should block
	 */
	private boolean shouldParkAfterFailedAcquire(Node pred, final Node node) {
		final int ws = pred.waitStatus.get();
		if (ws == Node.SIGNAL)
			/*
			 * This node has already set status asking a release to signal it,
			 * so it can safely park
			 */
			return true;
		if (ws > 0) {
			/*
			 * Predecessor was cancelled. Skip over predecessors and indicate
			 * retry.
			 */
			do
				node.prev = pred = pred.prev;
			while (pred.waitStatus.get() > 0);
			pred.next.set(node);
		} else
			/*
			 * waitStatus must be 0 or PROPAGATE. Indicate that we need a
			 * signal, but don't park yet. Caller will need to retry to make
			 * sure it cannot acquire before parking.
			 */
			compareAndSetWaitStatus(pred, ws, Node.SIGNAL);
		return false;
	}

	/**
	 * Convenience method to interrupt current thread.
	 */
	protected static void selfInterrupt() {
		Thread.currentThread().interrupt();
	}

	/**
	 * Convenience method to park and then check if interrupted
	 *
	 * @return {@code true} if interrupted
	 */
	private final boolean parkAndCheckInterrupt() {
		LockSupport.park(this);
		return Thread.interrupted();
	}

	/*
	 * Various flavors of acquire, varying in exclusive/shared and control
	 * modes. Each is mostly the same, but annoyingly different. Only a little
	 * bit of factoring is possible due to interactions of exception mechanics
	 * (including ensuring that we cancel if tryAcquire throws exception) and
	 * other control, at least not without hurting performance too much.
	 */

	/**
	 * Acquires in exclusive uninterruptible mode for thread already in queue.
	 * Used by condition wait methods as well as acquire.
	 *
	 * @param node
	 *            the node
	 * @param arg
	 *            the acquire argument
	 * @return {@code true} if interrupted while waiting
	 */
	public boolean acquireQueued(final Node node, final int arg) {
		try {
			boolean interrupted = false;
			for (;;) {
				final Node p = node.predecessor();
				if (p == head.get() && tryAcquire(arg)) {
					setHead(node);
					p.next.set(null); // help GC
					return interrupted;
				}
				if (shouldParkAfterFailedAcquire(p, node) && parkAndCheckInterrupt())
					interrupted = true;
			}
		} catch (final RuntimeException ex) {
			cancelAcquire(node);
			throw ex;
		}
	}

	/**
	 * Acquires in exclusive interruptible mode.
	 *
	 * @param arg
	 *            the acquire argument
	 */
	private void doAcquireInterruptibly(final int arg) throws InterruptedException {
		final Node node = addWaiter(Node.EXCLUSIVE);
		try {
			for (;;) {
				final Node p = node.predecessor();
				if (p == head.get() && tryAcquire(arg)) {
					setHead(node);
					p.next.set(null); // help GC
					return;
				}
				if (shouldParkAfterFailedAcquire(p, node) && parkAndCheckInterrupt())
					break;
			}
		} catch (final RuntimeException ex) {
			cancelAcquire(node);
			throw ex;
		}
		// Arrive here only if interrupted
		cancelAcquire(node);
		throw new InterruptedException();
	}

	/**
	 * Acquires in exclusive timed mode.
	 *
	 * @param arg
	 *            the acquire argument
	 * @param nanosTimeout
	 *            max wait time
	 * @return {@code true} if acquired
	 */
	private boolean doAcquireNanos(final int arg, long nanosTimeout) throws InterruptedException {
		long lastTime = System.nanoTime();
		final Node node = addWaiter(Node.EXCLUSIVE);
		try {
			for (;;) {
				final Node p = node.predecessor();
				if (p == head.get() && tryAcquire(arg)) {
					setHead(node);
					p.next.set(null); // help GC
					return true;
				}
				if (nanosTimeout <= 0) {
					cancelAcquire(node);
					return false;
				}
				if (nanosTimeout > spinForTimeoutThreshold && shouldParkAfterFailedAcquire(p, node))
					LockSupport.parkNanos(this, nanosTimeout);
				final long now = System.nanoTime();
				nanosTimeout -= now - lastTime;
				lastTime = now;
				if (Thread.interrupted())
					break;
			}
		} catch (final RuntimeException ex) {
			cancelAcquire(node);
			throw ex;
		}
		// Arrive here only if interrupted
		cancelAcquire(node);
		throw new InterruptedException();
	}

	// Main exported methods

	/**
	 * Attempts to acquire in exclusive mode. This method should query if the
	 * state of the object permits it to be acquired in the exclusive mode, and
	 * if so to acquire it.
	 *
	 * <p>
	 * This method is always invoked by the thread performing acquire. If this
	 * method reports failure, the acquire method may queue the thread, if it is
	 * not already queued, until it is signalled by a release from some other
	 * thread. This can be used to implement method {@link Lock#tryLock()}.
	 *
	 * <p>
	 * The default implementation throws {@link UnsupportedOperationException}.
	 *
	 * @param arg
	 *            the acquire argument. This value is always the one passed to
	 *            an acquire method, or is the value saved on entry to a
	 *            condition wait. The value is otherwise uninterpreted and can
	 *            represent anything you like.
	 * @return {@code true} if successful. Upon success, this object has been
	 *         acquired.
	 * @throws IllegalMonitorStateException
	 *             if acquiring would place this synchronizer in an illegal
	 *             state. This exception must be thrown in a consistent fashion
	 *             for synchronization to work correctly.
	 * @throws UnsupportedOperationException
	 *             if exclusive mode is not supported
	 */
	protected abstract boolean tryAcquire(final int arg);

	/**
	 * Attempts to set the state to reflect a release in exclusive mode.
	 *
	 * <p>
	 * This method is always invoked by the thread performing release.
	 *
	 * <p>
	 * The default implementation throws {@link UnsupportedOperationException}.
	 *
	 * @param arg
	 *            the release argument. This value is always the one passed to a
	 *            release method, or the current state value upon entry to a
	 *            condition wait. The value is otherwise uninterpreted and can
	 *            represent anything you like.
	 * @return {@code true} if this object is now in a fully released state, so
	 *         that any waiting threads may attempt to acquire; and
	 *         {@code false} otherwise.
	 * @throws IllegalMonitorStateException
	 *             if releasing would place this synchronizer in an illegal
	 *             state. This exception must be thrown in a consistent fashion
	 *             for synchronization to work correctly.
	 * @throws UnsupportedOperationException
	 *             if exclusive mode is not supported
	 */
	protected abstract boolean tryRelease(final int arg);

	/**
	 * Acquires in exclusive mode, aborting if interrupted. Implemented by first
	 * checking interrupt status, then invoking at least once
	 * {@link #tryAcquire}, returning on success. Otherwise the thread is
	 * queued, possibly repeatedly blocking and unblocking, invoking
	 * {@link #tryAcquire} until success or the thread is interrupted. This
	 * method can be used to implement method {@link Lock#lockInterruptibly}.
	 *
	 * @param arg
	 *            the acquire argument. This value is conveyed to
	 *            {@link #tryAcquire} but is otherwise uninterpreted and can
	 *            represent anything you like.
	 * @throws InterruptedException
	 *             if the current thread is interrupted
	 */
	public void acquireInterruptibly(final int arg) throws InterruptedException {
		if (Thread.interrupted())
			throw new InterruptedException();
		if (!tryAcquire(arg))
			doAcquireInterruptibly(arg);
	}

	/**
	 * Attempts to acquire in exclusive mode, aborting if interrupted, and
	 * failing if the given timeout elapses. Implemented by first checking
	 * interrupt status, then invoking at least once {@link #tryAcquire},
	 * returning on success. Otherwise, the thread is queued, possibly
	 * repeatedly blocking and unblocking, invoking {@link #tryAcquire} until
	 * success or the thread is interrupted or the timeout elapses. This method
	 * can be used to implement method {@link Lock#tryLock(long, TimeUnit)}.
	 *
	 * @param arg
	 *            the acquire argument. This value is conveyed to
	 *            {@link #tryAcquire} but is otherwise uninterpreted and can
	 *            represent anything you like.
	 * @param nanosTimeout
	 *            the maximum number of nanoseconds to wait
	 * @return {@code true} if acquired; {@code false} if timed out
	 * @throws InterruptedException
	 *             if the current thread is interrupted
	 */
	public boolean tryAcquireNanos(final int arg, final long nanosTimeout) throws InterruptedException {
		if (Thread.interrupted())
			throw new InterruptedException();
		return tryAcquire(arg) || doAcquireNanos(arg, nanosTimeout);
	}

	/**
	 * Releases in exclusive mode. Implemented by unblocking one or more threads
	 * if {@link #tryRelease} returns true. This method can be used to implement
	 * method {@link Lock#unlock}.
	 *
	 * @param arg
	 *            the release argument. This value is conveyed to
	 *            {@link #tryRelease} but is otherwise uninterpreted and can
	 *            represent anything you like.
	 * @return the value returned from {@link #tryRelease}
	 */
	public boolean release(final int arg) {
		if (tryRelease(arg)) {
			final Node h = head.get();
			if (h != null && h.waitStatus.get() != 0)
				unparkSuccessor(h);
			return true;
		}
		return false;
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
		if ((h = head.get()) != null && (s = h.next.get()) != null && s.prev == head.get() && (st = s.thread) != null
				|| (h = head.get()) != null && (s = h.next.get()) != null && s.prev == head.get()
						&& (st = s.thread) != null)
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
			final Thread tt = t.thread;
			if (tt != null)
				firstThread = tt;
			t = t.prev;
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
		for (Node p = tail.get(); p != null; p = p.prev)
			if (p.thread == thread)
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
		return (h = head.get()) == null || (s = h.next.get()) != null && s.thread == current || fullIsFirst(current);
	}

	private boolean fullIsFirst(final Thread current) {
		// same idea as fullGetFirstQueuedThread
		Node h, s;
		Thread firstThread = null;
		if ((h = head.get()) != null && (s = h.next.get()) != null && s.prev == head.get()
				&& (firstThread = s.thread) != null)
			return firstThread == current;
		Node t = tail.get();
		while (t != null && t != head.get()) {
			final Thread tt = t.thread;
			if (tt != null)
				firstThread = tt;
			t = t.prev;
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
		for (Node p = tail.get(); p != null; p = p.prev)
			if (p.thread != null)
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
		for (Node p = tail.get(); p != null; p = p.prev) {
			final Thread t = p.thread;
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
		for (Node p = tail.get(); p != null; p = p.prev)
			if (!p.isShared()) {
				final Thread t = p.thread;
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
		final int s = getState();
		final String q = hasQueuedThreads() ? "non" : "";
		return super.toString() + "[State = " + s + ", " + q + "empty queue]";
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
		if (node.waitStatus.get() == Node.CONDITION || node.prev == null)
			return false;
		if (node.next != null) // If has successor, it must be on queue
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
		Node t = tail.get();
		for (;;) {
			if (t == node)
				return true;
			if (t == null)
				return false;
			t = t.prev;
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
		if (!compareAndSetWaitStatus(node, Node.CONDITION, 0))
			return false;

		/*
		 * Splice onto queue and try to set waitStatus of predecessor to
		 * indicate that thread is (probably) waiting. If cancelled or attempt
		 * to set waitStatus fails, wake up to resync (in which case the
		 * waitStatus can be transiently and harmlessly wrong).
		 */
		final Node p = enq(node);
		final int ws = p.waitStatus.get();
		if (ws > 0 || !compareAndSetWaitStatus(p, ws, Node.SIGNAL))
			LockSupport.unpark(node.thread);
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
		if (compareAndSetWaitStatus(node, Node.CONDITION, 0)) {
			enq(node);
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
			final int savedState = getState();
			if (release(savedState))
				return savedState;
		} catch (final RuntimeException ex) {
			node.waitStatus.set(Node.CANCELLED);
			throw ex;
		}
		// reach here if release fails
		node.waitStatus.set(Node.CANCELLED);
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
	public boolean owns(final Monitor condition) {
		if (condition == null)
			throw new NullPointerException();
		return condition.isOwnedBy(this);
	}

	/**
	 * Queries whether any threads are waiting on the given condition associated
	 * with this synchronizer. Note that because timeouts and interrupts may
	 * occur at any time, a <tt>true</tt> return does not guarantee that a
	 * future <tt>signal</tt> will awaken any threads. This method is designed
	 * primarily for use in monitoring of the system state.
	 *
	 * @param condition
	 *            the condition
	 * @return <tt>true</tt> if there are any waiting threads
	 * @throws IllegalMonitorStateException
	 *             if exclusive synchronization is not held
	 * @throws IllegalArgumentException
	 *             if the given condition is not associated with this
	 *             synchronizer
	 * @throws NullPointerException
	 *             if the condition is null
	 */
	public boolean hasWaiters(final Monitor condition) {
		if (!owns(condition))
			throw new IllegalArgumentException("Not owner");
		return condition.hasWaiters();
	}

	/**
	 * Returns an estimate of the number of threads waiting on the given
	 * condition associated with this synchronizer. Note that because timeouts
	 * and interrupts may occur at any time, the estimate serves only as an
	 * upper bound on the actual number of waiters. This method is designed for
	 * use in monitoring of the system state, not for synchronization control.
	 *
	 * @param condition
	 *            the condition
	 * @return the estimated number of waiting threads
	 * @throws IllegalMonitorStateException
	 *             if exclusive synchronization is not held
	 * @throws IllegalArgumentException
	 *             if the given condition is not associated with this
	 *             synchronizer
	 * @throws NullPointerException
	 *             if the condition is null
	 */
	public int getWaitQueueLength(final Monitor condition) {
		if (!owns(condition))
			throw new IllegalArgumentException("Not owner");
		return condition.getWaitQueueLength();
	}

	/**
	 * Returns a collection containing those threads that may be waiting on the
	 * given condition associated with this synchronizer. Because the actual set
	 * of threads may change dynamically while constructing this result, the
	 * returned collection is only a best-effort estimate. The elements of the
	 * returned collection are in no particular order.
	 *
	 * @param condition
	 *            the condition
	 * @return the collection of threads
	 * @throws IllegalMonitorStateException
	 *             if exclusive synchronization is not held
	 * @throws IllegalArgumentException
	 *             if the given condition is not associated with this
	 *             synchronizer
	 * @throws NullPointerException
	 *             if the condition is null
	 */
	public Collection<Thread> getWaitingThreads(final Monitor condition, final List<Thread> list) {
		if (!owns(condition))
			throw new IllegalArgumentException("Not owner");
		return condition.getWaitingThreads(list);
	}

	/**
	 * Atomically sets synchronization state to the given updated value if the
	 * current state value equals the expected value. This operation has memory
	 * semantics of a <tt>volatile</tt> read and write.
	 *
	 * @param expect
	 *            the expected value
	 * @param update
	 *            the new value
	 * @return true if successful. False return indicates that the actual value
	 *         was not equal to the expected value.
	 */
	protected final boolean compareAndSetState(final int expect, final int update) {
		// See below for intrinsics setup to support this
		return state.compareAndSet(expect, update);
	}

	/**
	 * CAS tail field. Used only by enq
	 */
	private boolean compareAndSetTail(final Node expect, final Node update) {
		return tail.compareAndSet(expect, update);
	}

	/**
	 * CAS waitStatus field of a node.
	 */
	private static boolean compareAndSetWaitStatus(final Node node, final int expect, final int update) {
		return node.waitStatus.compareAndSet(expect, update);
	}
}
