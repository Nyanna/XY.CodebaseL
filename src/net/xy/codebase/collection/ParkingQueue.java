package net.xy.codebase.collection;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.xy.codebase.concurrent.CASMonitor;

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
	private final CASMonitor added = new CASMonitor();
	private final CASMonitor empty = new CASMonitor();

	/**
	 * default with default ArrayQueue
	 *
	 * @param clazz
	 * @param maxCount
	 */
	public ParkingQueue(final Class<E> clazz, final int maxCount) {
		this(new ArrayQueue<E>(clazz, maxCount));
	}

	public ParkingQueue(final Array<E> array) {
		this(new ArrayQueue<E>(array));
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
		long wait = waitMillis;
		try {
			E elem;
			for (;;) {
				final long state = added.getState();
				elem = aq.take();

				if (elem == null) {
					empty.callAll();

					final long waited = added.await(state, TimeUnit.MILLISECONDS.toNanos(wait));
					if (waitMillis < 0)
						continue;
					wait -= waited;
					if (wait > 0)
						continue;
				}
				break;
			}
			return elem;
		} catch (final InterruptedException e) {
			if (LOG.isTraceEnabled())
				LOG.trace(e.getMessage(), e);
		}
		return null;
	}

	/**
	 * waits until the last queue element was aquiered
	 */
	public void waitEmpty() {
		try {
			empty.await(empty.getState());
		} catch (final InterruptedException e) {
			LOG.trace(e.getMessage(), e);
		}
	}

	/**
	 * @return amount of contained elements
	 */
	public int size() {
		return aq.size();
	}
}
