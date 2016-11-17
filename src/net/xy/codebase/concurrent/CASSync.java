package net.xy.codebase.concurrent;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

import net.xy.codebase.exec.ThreadUtils;

/**
 * common implementation details for CAS Monitor and Semaphore
 *
 * @author Xyan
 *
 */
public abstract class CASSync {
	/**
	 * last queued tail node
	 */
	protected final AtomicReference<Slot> tail = new AtomicReference<Slot>();
	/**
	 * amount of entered waiters
	 */
	private final AtomicInteger waiters = new AtomicInteger();
	/**
	 * current modification counter
	 */
	protected final AtomicInteger modCounter = new AtomicInteger();

	/**
	 * waits until state change
	 *
	 * @param state
	 */
	public void await(final int state) {
		await(state, -1);
	}

	/**
	 * awaits until call or until reaching maximum wait time
	 *
	 * @param state
	 * @param nanoTime
	 */
	public void await(final int state, final long nanoTime) {
		waiters.incrementAndGet();
		awaitInner(state, nanoTime);
		waiters.decrementAndGet();
	}

	/**
	 * inner stack context wait method
	 *
	 * @param state
	 * @param nanoTime
	 */
	private void awaitInner(final int state, final long nanoTime) {
		final Thread th = Thread.currentThread();
		final long startTime = System.nanoTime();
		long waitTime = 0;

		final Slot sl = enque(th);
		for (;;) {
			if (state != getState() || //
					nanoTime >= 0 && (waitTime = nanoTime - (System.nanoTime() - startTime)) <= 0)
				break;

			// thread is wakeable from here
			sl.setWaiting();
			if (state != getState())
				; // skip
			else if (nanoTime < 0)
				LockSupport.park(this);
			else
				LockSupport.parkNanos(this, waitTime);
			if (!sl.wake())
				LockSupport.unpark(th);
			sl.resetWaked();
			// thread is wakeable to here

			// cuz only first wake creates permit
			// cleanup my state use any left permit
			LockSupport.park(this);
		}
		dequeue(sl, th);
	}

	/**
	 * enques in next free slot up from tail or creates new slot
	 *
	 * @param th
	 * @return
	 */
	private Slot enque(final Thread th) {
		Slot nd = tail.get();
		while (nd != null) {
			if (nd.setThread(null, th))
				return nd;
			nd = nd.getNext();
		}

		nd = new Slot();
		nd.setThread(null, th);
		for (int loop = 0;;) {
			final Slot old = tail.get();
			nd.setNext(old);
			if (tail.compareAndSet(old, nd))
				return nd;
			loop = ThreadUtils.yieldCAS(loop);
		}
	}

	/**
	 * removes from current slot and returns
	 *
	 * @param sl
	 * @param th
	 */
	protected void dequeue(final Slot sl, final Thread th) {
		sl.setThread(th, null);
	}

	/**
	 * @return current atomic modification state
	 */
	public int getState() {
		return modCounter.get();
	}

	/**
	 * @return amount of entered waiters
	 */
	public int getWaiters() {
		return waiters.get();
	}

	/**
	 * does some waking behavior
	 *
	 * @return true on waking at least one thread
	 */
	public abstract boolean call();

	/**
	 * wakes up first found thread starting at the given node
	 *
	 * @param nd
	 * @return
	 */
	protected boolean wakeNext(Slot nd) {
		while (nd != null) {
			if (nd.wake())
				return true;
			nd = nd.getNext();
		}
		return false;
	}

	/**
	 * wakes up all following waiting threads
	 *
	 * @param nd
	 * @return
	 */
	protected boolean wakeAll(Slot nd) {
		boolean res = false;
		if (nd != null)
			for (; nd != null; nd = nd.getNext())
				res |= nd.wake();
		return res;
	}

	/**
	 * dynamic queue but consistent order node object to hold waiting threads
	 *
	 * @author Xyan
	 *
	 */
	public static class Slot {
		/**
		 * thread to wake and atomic barrier to indicate waiting state
		 */
		private final AtomicReference<Thread> thread = new AtomicReference<Thread>();
		/**
		 * threads waiting state
		 */
		private final AtomicBoolean waiting = new AtomicBoolean();
		private final AtomicBoolean waked = new AtomicBoolean();
		/**
		 * next bound node in slot chain
		 */
		private Slot next;

		/**
		 * thread can be waked once it doesn't matters if one thread got an
		 * stale node
		 *
		 * @return true when a thread was waked up
		 */
		public boolean wake() {
			final Thread th = thread.get();
			if (waiting.compareAndSet(true, false)) {
				LockSupport.unpark(th);
				waked.set(true);
				return true;
			}
			return false;
		}

		/**
		 * set waiting state to true
		 */
		public void setWaiting() {
			waiting.set(true);
		}

		/**
		 * blocks until holding thread called unpark
		 */
		public void resetWaked() {
			for (int loop = 0;;) {
				if (waked.compareAndSet(true, false))
					return;
				loop = ThreadUtils.yieldCAS(loop);
			}
		}

		/**
		 * atomicly set thread of this slot
		 *
		 * @param exspect
		 * @param update
		 * @return
		 */
		public boolean setThread(final Thread exspect, final Thread update) {
			return thread.compareAndSet(exspect, update);
		}

		/**
		 * @return next node in chain
		 */
		public Slot getNext() {
			return next;
		}

		/**
		 * sets next slot in chain
		 *
		 * @param next
		 */
		public void setNext(final Slot next) {
			this.next = next;
		}
	}
}
