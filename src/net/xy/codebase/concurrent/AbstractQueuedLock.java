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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

public abstract class AbstractQueuedLock extends AbstractOwnableLock {
	/**
	 * The number of nanoseconds for which it is faster to spin rather than to
	 * use timed park. A rough estimate suffices to improve responsiveness with
	 * very short timeouts.
	 */
	private static final long spinForTimeoutThreshold = 1000L;
	/**
	 * Head of the wait queue, lazily initialized. Except for initialization, it
	 * is modified only via method setHead. Note: If head exists, its waitStatus
	 * is guaranteed not to be CANCELLED.
	 */
	protected final AtomicReference<Node> head = new AtomicReference<Node>();
	/**
	 * Tail of the wait queue, lazily initialized. Modified only via method enq
	 * to add new wait node.
	 */
	protected final AtomicReference<Node> tail = new AtomicReference<Node>();
	/**
	 * The synchronization state.
	 */
	private final AtomicInteger locks = new AtomicInteger();

	/**
	 * Returns the current value of synchronization state. This operation has
	 * memory semantics of a <tt>volatile</tt> read.
	 *
	 * @return current state value
	 */
	protected final int getLocks() {
		return locks.get();
	}

	/**
	 * Sets the value of synchronization state. This operation has memory
	 * semantics of a <tt>volatile</tt> write.
	 *
	 * @param lockAmount
	 *            the new state value
	 */
	protected final void setLocks(final int lockAmount) {
		locks.set(lockAmount);
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
	protected final boolean compareAndSetLocks(final int expect, final int update) {
		// See below for intrinsics setup to support this
		return locks.compareAndSet(expect, update);
	}

	/**
	 * CAS tail field. Used only by enq
	 */
	private boolean compareAndSetTail(final Node expect, final Node update) {
		return tail.compareAndSet(expect, update);
	}

	/**
	 * Inserts node into queue, initializing if necessary. See picture above.
	 *
	 * @param node
	 *            the node to insert
	 * @return node's predecessor
	 */
	public Node enq(final Node node) {
		for (;;) {
			final Node t = tail.get();
			if (t == null) { // Must initialize
				final Node header = createNode(null, 0); // Dummy header
				header.setNextWaiter(node);
				node.setPrev(header);
				if (head.compareAndSet(null, header)) {
					tail.set(node);
					return header;
				}
			} else {
				node.setPrev(t);
				if (compareAndSetTail(t, node)) {
					t.setNextWaiter(node);
					return t;
				}
			}
		}
	}

	public Node createNode(final Thread thread, final int waitStatus) {
		return new Node(thread, waitStatus);
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
		final Node node = createNode(Thread.currentThread(), 0);
		node.setNextWaiter(mode);
		// Try the fast path of enq; backup to full enq on failure
		final Node pred = tail.get();
		if (pred != null) {
			node.setPrev(pred);
			if (compareAndSetTail(pred, node)) {
				pred.setNextWaiter(node);
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
		node.setThread(null);
		node.setPrev(null);
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
		final int ws = node.getWaitStatus();
		if (ws < 0)
			node.compareAndSetWaitStatus(ws, 0);

		/*
		 * Thread to unpark is held in successor, which is normally just the
		 * next node. But if cancelled or apparently null, traverse backwards
		 * from tail to find the actual non-cancelled successor.
		 */
		Node s = node.getNextWaiter();
		if (s == null || s.getWaitStatus() > 0) {
			s = null;
			for (Node t = tail.get(); t != null && t != node; t = t.getPrev())
				if (t.getWaitStatus() <= 0)
					s = t;
		}
		if (s != null)
			LockSupport.unpark(s.getThread());
	}

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

		node.setThread(null);

		// Skip cancelled predecessors
		Node pred = node.getPrev();
		while (pred.getWaitStatus() > 0)
			node.setPrev(pred = pred.getPrev());

		// predNext is the apparent node to unsplice. CASes below will
		// fail if not, in which case, we lost race vs another cancel
		// or signal, so no further action is necessary.
		final Node predNext = pred.getNextWaiter();

		// Can use unconditional write instead of CAS here.
		// After this atomic step, other Nodes can skip past us.
		// Before, we are free of interference from other threads.
		node.setWaitStatus(Node.CANCELLED);

		// If we are the tail, remove ourselves.
		if (node == tail.get() && compareAndSetTail(node, pred))
			pred.compareAndSetNext(predNext, null);
		else {
			// If successor needs signal, try to set pred's next-link
			// so it will get one. Otherwise wake it up to propagate.
			int ws;
			if (pred != head.get() && ((ws = pred.getWaitStatus()) == Node.SIGNAL
					|| ws <= 0 && pred.compareAndSetWaitStatus(ws, Node.SIGNAL)) && pred.getThread() != null) {
				final Node next = node.getNextWaiter();
				if (next != null && next.getWaitStatus() <= 0)
					pred.compareAndSetNext(predNext, next);
			} else
				unparkSuccessor(node);

			node.setNextWaiter(node); // help GC
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
		final int ws = pred.getWaitStatus();
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
				node.setPrev(pred = pred.getPrev());
			while (pred.getWaitStatus() > 0);
			pred.setNextWaiter(node);
		} else
			/*
			 * waitStatus must be 0 or PROPAGATE. Indicate that we need a
			 * signal, but don't park yet. Caller will need to retry to make
			 * sure it cannot acquire before parking.
			 */
			pred.compareAndSetWaitStatus(ws, Node.SIGNAL);
		return false;
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
					p.setNextWaiter(null); // help GC
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
					p.setNextWaiter(null); // help GC
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
					p.setNextWaiter(null); // help GC
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
			if (h != null && h.getWaitStatus() != 0)
				unparkSuccessor(h);
			return true;
		}
		return false;
	}
}
