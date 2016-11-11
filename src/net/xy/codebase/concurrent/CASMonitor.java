package net.xy.codebase.concurrent;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

import net.xy.codebase.mem.ConcurrentPool;

// TODO unittest
public class CASMonitor {
	private final NodePool pool = new NodePool();
	private final AtomicLong modCounter = new AtomicLong();
	private final AtomicReference<Node> tail = new AtomicReference<Node>();

	public long await(final long state) throws InterruptedException {
		return await(state, 0);
	}

	public long await(final long state, final long nanoTime) throws InterruptedException {
		if (Thread.interrupted())
			throw new InterruptedException();
		final long lastTime = System.nanoTime();

		{ // every enqueue parks
			enque();
			if (!modCounter.compareAndSet(state, state))
				call();
			if (nanoTime < 0)
				LockSupport.park(this);
			else
				LockSupport.parkNanos(this, nanoTime);
		}

		final long now = System.nanoTime();
		final long waited = now - lastTime;

		return nanoTime - waited;
	}

	public void call() {
		Node ta;
		modCounter.incrementAndGet();
		for (;;) {
			ta = tail.get();
			final Node next = ta != null ? ta.getNext() : null;
			if (!tail.compareAndSet(ta, next))
				continue;

			if (ta != null) {
				LockSupport.unpark(ta.getThread());
				pool.free(ta);
			}
			break;
		}
	}

	public void callAll() {
		while (tail.get() != null)
			call();
	}

	/**
	 * node should only be handle by one thread at a time
	 *
	 * @return nothing
	 */
	private void enque() {
		final Node nue = pool.obtain();
		nue.set(Thread.currentThread());

		for (;;) {
			final Node ta = tail.get();
			if (tail.compareAndSet(ta, nue)) {
				nue.setNext(ta);
				break;
			}
		}
	}

	public long getState() {
		return modCounter.get();
	}

	public class NodePool extends ConcurrentPool<Node> {
		@Override
		protected Node newObject() {
			return new Node();
		}

		@Override
		public void free(final Node entry) {
			entry.setNext(null);
			entry.set(null);
			super.free(entry);
		}
	}

	public class Node {
		private volatile Thread thread;
		private volatile Node next;

		public void setNext(final Node next) {
			this.next = next;
		}

		public Node getNext() {
			return next;
		}

		public void set(final Thread thread) {
			this.thread = thread;
		}

		public Thread getThread() {
			return thread;
		}
	}
}
