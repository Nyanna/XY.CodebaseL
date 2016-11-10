package net.xy.codebase.collection;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.xy.codebase.concurrent.ThreadMonitor;

/**
 * an unboundet self expanding queue up to an maximum. full synchronized when
 * queue don't growth.
 *
 * @author Xyan
 *
 * @param <E>
 */
// TODO unittest
public class ArrayQueue<E> implements Queue<E> {
	private static final Logger LOG = LoggerFactory.getLogger(ArrayQueue.class);
	/**
	 * backing array container
	 */
	protected Array<E> elements;
	/**
	 * idx to put next element in
	 */
	protected AtomicLong putIdx = new AtomicLong();
	/**
	 * actues index to get an element from
	 */
	protected AtomicLong getIdx = new AtomicLong();
	/**
	 * monitor needed for resize
	 */
	protected ThreadMonitor mon = new ThreadMonitor();
	protected ThreadMonitor gro = new ThreadMonitor();
	/**
	 * maximum allowed element count
	 */
	protected final int maxCount;

	/**
	 * default
	 *
	 * @param clazz
	 * @param maxCount
	 */
	public ArrayQueue(final Class<E> clazz, final int maxCount) {
		elements = new Array<E>(clazz);
		this.maxCount = maxCount;
	}

	/**
	 * already at may size
	 *
	 * @param array
	 */
	public ArrayQueue(final Array<E> array) {
		elements = array;
		this.maxCount = array.size();
	}

	/**
	 * adds an element as long as the maximum size is not reached
	 *
	 * @param elem
	 * @return true on success
	 */
	@Override
	public boolean add(final E elem) {
		if (elem == null)
			throw new IllegalArgumentException("Element can't be null");

		try {
			mon.enter();
			return addInner(elem);
		} finally {
			mon.leave();
		}
	}

	/**
	 * take and remove top element or return null.
	 *
	 * @return
	 */
	@Override
	public E take() {
		try {
			mon.enter();
			return takeInner();
		} finally {
			mon.leave();
		}

	}

	private void growth() {
		// increase
		try {
			gro.enter();
			synchronized (elements) {
				growthInner();
			}
		} finally {
			gro.leave();
		}
	}

	private boolean addInner(final E elem) {
		long tIdx, nIdx, putIdx, getIdx;
		do {
			putIdx = this.putIdx.get();
			getIdx = this.getIdx.get();
			if (putIdx - getIdx > maxCount)
				return false;
			if (putIdx - getIdx == elements.capacity())
				growth();

			tIdx = putIdx % elements.capacity();
			nIdx = putIdx + 1;
		} while (!this.putIdx.compareAndSet(tIdx, nIdx));

		elements.set((int) tIdx, elem);
		return true;
	}

	private E takeInner() {
		E res;
		long tIdx, nIdx, putIdx, getIdx;
		do
			for (;;) {
				putIdx = this.putIdx.get();
				getIdx = this.getIdx.get();
				if (putIdx - getIdx <= 0)
					return null;

				tIdx = getIdx % elements.capacity();
				nIdx = getIdx + 1;
				res = elements.set((int) tIdx, null);
				if (res != null) // when not set already yield
					break;
				Thread.yield();
			}
		while (!this.getIdx.compareAndSet(tIdx, nIdx));
		return res;
	}

	private void growthInner() {
		try {
			mon.wait(gro.count());

			if (size() >= elements.capacity()) {
				final long oldPut = putIdx.get();
				final long oldGet = getIdx.get();
				final long size = oldPut - oldGet;

				final Array<E> nue = new Array<E>(elements.getComponentClass(),
						Math.min(Array.getNextSize(elements.capacity()), maxCount));
				if (LOG.isDebugEnabled())
					LOG.debug("Increased queue to [" + nue.getComponentClass().getSimpleName() + "][" + nue.capacity()
							+ "]");

				for (int i = 0; i < size; i++) {
					final int get = (int) ((oldGet + i) % elements.capacity());
					nue.add(elements.get(get));
				}

				elements = nue;
				this.putIdx.set(elements.size());
				this.getIdx.set(0);
			}
		} finally {
			mon.release();
		}
	}

	/**
	 * @return top element without removing
	 */
	@Override
	public E peek() {
		E res = null;
		if (size() > 0)
			res = elements.get((int) (getIdx.get() % elements.capacity()));
		return res;
	}

	/**
	 * @return amount of contained elements
	 */
	@Override
	public int size() {
		return (int) (getIdx.get() - putIdx.get());
	}

	/**
	 * whether size == 0
	 *
	 * @return
	 */
	@Override
	public boolean isEmpty() {
		return size() == 0;
	}

	@Override
	public String toString() {
		return elements.toString();
	}

	/**
	 * clears the queue
	 */
	@Override
	public void clear() {
		elements.clear();
		putIdx.set(0);
		getIdx.set(0);
	}

	public static void main(final String[] args) {
		Array.MIN_GROWTH = 2;
		int count = 0;
		int cnt = 0;
		final ArrayQueue<Integer> aq = new ArrayQueue<Integer>(Integer.class, 16000);
		for (int i = 1000; i > 0; i--) {
			Integer got = 0;
			if (Math.random() > 0.4d)
				aq.add(count++);
			else if ((got = aq.take()) != null && got != cnt++)
				throw new RuntimeException("Lost element [" + got + "]");
		}
	}
}
