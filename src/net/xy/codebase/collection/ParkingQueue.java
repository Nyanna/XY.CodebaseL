package net.xy.codebase.collection;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.xy.codebase.concurrent.Monitor;
import net.xy.codebase.concurrent.Semaphore;

/**
 * implementation is not synchronized by itself!
 *
 * @author Xyan
 *
 * @param <E>
 */
public class ParkingQueue<E> {
	private static final Logger LOG = LoggerFactory.getLogger(ParkingQueue.class);
	private final Queue<E> aq;
	private final Semaphore added = new Semaphore();
	private final Monitor empty = new Monitor();

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
	}

	/**
	 * adds an element as long as the maximum size is not reached
	 *
	 * @param elem
	 * @return true on success
	 */
	public boolean add(final E elem) {
		final boolean res = aq.add(elem);
		if (res)
			added.call();
		else
			LOG.error("ParkinArrayQueue is full droping [" + elem + "][" + size() + "]");
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
		final long startTime = System.currentTimeMillis();

		E elem;
		for (;;) {
			final int state = added.getState();
			elem = aq.take();

			if (elem == null) {
				empty.call();

				if (waitMillis < 0) {
					added.await(state, waitMillis);
					continue;
				}
				final long waitTime;
				if ((waitTime = waitMillis - (System.currentTimeMillis() - startTime)) > 0) {
					added.await(state, TimeUnit.MILLISECONDS.toNanos(waitTime));
					continue;
				}
			}
			break;
		}
		return elem;
	}

	/**
	 * waits until the last queue element was aquiered
	 */
	public void waitEmpty() {
		while (aq.size() > 0)
			empty.await(empty.getState());
	}

	/**
	 * @return amount of contained elements
	 */
	public int size() {
		return aq.size();
	}
}
