package net.xy.codebase.concurrent;

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
public abstract class Sync {
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
	 * @param nTime
	 *            to wait in nanos
	 */
	private void awaitInner(final int state, final long nTime) {
		final Thread th = Thread.currentThread();
		final long sTime = System.nanoTime();
		long wTime = 0; // waittime

		final Slot sl = enque(th);
		for (;;) {
			if (state != getState())
				break;
			if (nTime >= 0 && (wTime = nTime - (System.nanoTime() - sTime)) <= 0)
				break;

			sl.setWaiting(state);
			if (nTime < 0)
				LockSupport.park(this);
			else
				LockSupport.parkNanos(this, wTime);
			sl.setRuning();
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
	public abstract void call();

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
	protected void wakeAll(Slot nd) {
		if (nd != null)
			for (; nd != null; nd = nd.getNext())
				nd.wake();
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
		private final AtomicInteger waiting = new AtomicInteger();
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
			final int state = waiting.get();
			final boolean res = state != 0 && waiting.compareAndSet(state, 0);
			LockSupport.unpark(thread.get());
			return res;
		}

		/**
		 * set waiting state to true
		 *
		 * @param state
		 */
		public void setWaiting(final int state) {
			waiting.set(state);
		}

		public void setRuning() {
			waiting.set(0);
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
