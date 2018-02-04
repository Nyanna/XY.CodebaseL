package net.xy.codebase.collection;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

import net.xy.codebase.exec.ThreadUtils;

/**
 * An ringbuffer pattern arrayqueue with very hight throughput. Lock & Waitfree.
 *
 * @author Xyan
 *
 * @param <E>
 */
public class ArrayQueue<E> implements Queue<E> {
	// private static final Logger LOG =
	// LoggerFactory.getLogger(ArrayQueue.class);
	/**
	 * special raturn value constants for growing capabilities
	 */
	protected static final int SIZE_OK = 0, SIZE_MAXED = 1, RESIZED = 2;
	/**
	 * backing array container
	 */
	protected AtomicReferenceArray<E> elements;
	/**
	 * next index to put next element in
	 */
	protected final AtomicInteger putIndex = new AtomicInteger();
	/**
	 * next index to retrieve object from
	 */
	protected final AtomicInteger getIndex = new AtomicInteger();
	protected final AtomicInteger size = new AtomicInteger();

	/**
	 * default
	 *
	 * @param clazz
	 * @param capacity
	 */
	// @SuppressWarnings("unchecked")
	public ArrayQueue(final Class<E> clazz, final int capacity) {
		elements = new AtomicReferenceArray<E>(capacity);
	}

	/**
	 * adds an element as long as the maximum size is not reached
	 *
	 * @param elem
	 * @return true on success
	 */
	@Override
	public boolean add(final E elem) {
		return addInner(elem);
	}

	/**
	 * take and remove top element or return null.
	 *
	 * @return
	 */
	@Override
	public E take() {
		return takeInner();
	}

	/**
	 * inner emthod to select index and put object
	 *
	 * @param elem
	 * @return
	 */
	protected boolean addInner(final E elem) {
		int putIdx;
		for (;;) {
			final int s = size.get();
			if (SIZE_MAXED == checkLimit(s))
				return false;
			else if (size.compareAndSet(s, s + 1)) {
				putIdx = putIndex.getAndIncrement();
				break;
			}
			ThreadUtils.yield();
		}

		final int tarIdx = remainder(putIdx, elements.length());
		while (!elements.compareAndSet(tarIdx, null, elem))
			ThreadUtils.yield();
		return true;
	}

	/**
	 * limit checking method can be overwritten for growth support
	 *
	 * @param size
	 * @return
	 */
	protected int checkLimit(final int size) {
		if (size >= elements.length())
			return SIZE_MAXED;
		return SIZE_OK;
	}

	/**
	 * inner method to select next index and retrieve object
	 *
	 * @return
	 */
	protected E takeInner() {
		int getIdx;
		for (;;) {
			final int s = size.get();
			if (s == 0)
				return null;

			if (size.compareAndSet(s, s - 1)) {
				getIdx = getIndex.getAndIncrement();
				break;
			}
			ThreadUtils.yield();
		}

		final int tarIdx = remainder(getIdx, elements.length());
		for (;;) {
			final E res = elements.get(tarIdx);
			if (res != null && elements.compareAndSet(tarIdx, res, null))
				return res;
			ThreadUtils.yield();
		}
	}

	/**
	 * @return top element without removing
	 */
	@Override
	public E peek() {
		E res = null;
		if (size() > 0) {
			final int length = elements.length();
			final int tarIdx = remainder(putIndex.get() - 1, length);
			res = elements.get(tarIdx);
		}
		return res;
	}

	/**
	 * @return amount of contained elements
	 */
	@Override
	public int size() {
		return size(putIndex.get(), getIndex.get());
	}

	private int size(final int putIdx, final int getIdx) {
		if (putIdx < getIdx)
			return Integer.MAX_VALUE - Math.abs(putIdx - getIdx);
		else
			return putIdx - getIdx;
	}

	/**
	 * whether size == 0
	 *
	 * @return
	 */
	@Override
	public boolean isEmpty() {
		return putIndex.get() == getIndex.get();
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
		while (take() != null)
			;
	}

	private static final long IMASK = 0xffffffffL;

	/**
	 * unsigned modulo/remainder operation
	 */
	protected static int remainder(final int number, final int mod) {
		return (int) ((number & IMASK) % (mod & IMASK));
	}
}
