package net.xy.codebase.collection;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * an unboundet self expanding queue up to an maximum. synchronized.
 *
 * @author Xyan
 *
 * @param <E>
 */
public class ArrayQueue<E> implements Queue<E> {
	private static final Logger LOG = LoggerFactory.getLogger(ArrayQueue.class);
	/**
	 * backing array container
	 */
	protected final Array<E> elements;
	/**
	 * idx to put next element in
	 */
	protected int putIdx = 0;
	/**
	 * actues index to get an element from
	 */
	protected int getIdx = 0;
	/**
	 * number of elements in queue
	 */
	protected int count = 0;
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
		clear();
	}

	/**
	 * adds an element as long as the maximum size is not reached
	 *
	 * @param elem
	 * @return true on success
	 */
	@Override
	public synchronized boolean add(final E elem) {
		if (elem == null)
			throw new IllegalArgumentException("Element can't be null");

		if (count >= maxCount)
			return false;

		// increase
		if (count >= elements.capacity()) {
			elements.ensureCapacity(elements.capacity() + 1);
			final E[] raw = elements.getElements();
			if (LOG.isDebugEnabled())
				LOG.debug("Increased queue to [" + raw.getClass().getComponentType().getSimpleName() + "][" + raw.length
						+ "]");

			if (putIdx <= getIdx) {
				final int copylength = Math.min(raw.length - count, putIdx);
				if (copylength > 0) {
					System.arraycopy(raw, 0, raw, getIdx + count - putIdx, copylength);
					if (putIdx - copylength > 0)
						System.arraycopy(raw, copylength, raw, 0, putIdx - copylength);
				}
				putIdx = (getIdx + count) % raw.length;
			}
		}

		elements.set(putIdx, elem);
		if (putIdx + 1 == elements.capacity())
			putIdx = 0;
		else
			putIdx++;
		count++;
		return true;
	}

	/**
	 * take and remove top element or return null.
	 *
	 * @return
	 */
	@Override
	public synchronized E take() {
		E res = null;
		if (count > 0) {
			res = elements.get(getIdx);
			elements.set(getIdx, null);
			if (getIdx + 1 == elements.capacity())
				getIdx = 0;
			else
				getIdx++;
			count--;
		}
		return res;
	}

	/**
	 * @return top element without removing
	 */
	@Override
	public E peek() {
		E res = null;
		if (count > 0)
			res = elements.get(getIdx);
		return res;
	}

	/**
	 * @return amount of contained elements
	 */
	@Override
	public int size() {
		return count;
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
		return Arrays.toString(elements.getElements());
	}

	/**
	 * clears the queue
	 */
	@Override
	public void clear() {
		elements.clear();
		putIdx = 0;
		getIdx = 0;
		count = 0;
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
