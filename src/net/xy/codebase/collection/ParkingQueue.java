package net.xy.codebase.collection;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParkingQueue<E> {
	private static final Logger LOG = LoggerFactory.getLogger(ParkingQueue.class);

	private final Queue<E> aq;
	private final ReentrantLock lock;
	private final Condition added;
	private final Condition empty;

	/**
	 * default with default ArrayQueue
	 * 
	 * @param clazz
	 * @param maxCount
	 */
	public ParkingQueue(final Class<E> clazz, final int maxCount) {
		this(new ArrayQueue<E>(clazz, maxCount));
	}

	/**
	 * default with given queue
	 * 
	 * @param aq
	 */
	public ParkingQueue(final Queue<E> aq) {
		this.aq = aq;
		lock = new ReentrantLock(false);
		added = lock.newCondition();
		empty = lock.newCondition();
	}

	/**
	 * adds an element as long as the maximum size is not reached
	 *
	 * @param elem
	 * @return true on success
	 */
	public boolean add(final E elem) {
		boolean res = false;
		try {
			lock.lock();
			res = aq.add(elem);
			if (res)
				added.signal();
		} finally {
			lock.unlock();
		}
		return res;
	}

	/**
	 * take and remove top element or return null.
	 *
	 * @return
	 */
	public E take() {
		return aq.take();
	}

	/**
	 * waits until an element is takable
	 *
	 * @param waitMillis
	 * @return
	 */
	public E take(final long waitMillis) {
		try {
			lock.lockInterruptibly();
			E elem = aq.take();
			if (elem == null) {
				empty.signalAll();
				if (waitMillis < 0)
					added.await();
				else
					added.await(waitMillis, TimeUnit.MILLISECONDS);
				elem = aq.take();
			}
			return elem;
		} catch (final InterruptedException e) {
			if (LOG.isTraceEnabled())
				LOG.trace(e.getMessage(), e);
		} finally {
			if (lock.isHeldByCurrentThread())
				lock.unlock();
		}
		return null;
	}

	/**
	 * waits until the last queue element was aquiered
	 */
	public void waitEmpty() {
		try {
			lock.lock();
			empty.await();
		} catch (final InterruptedException e) {
			LOG.trace(e.getMessage(), e);
		} finally {
			lock.unlock();
		}
	}

	/**
	 * @return amount of contained elements
	 */
	public int size() {
		return aq.size();
	}
}
