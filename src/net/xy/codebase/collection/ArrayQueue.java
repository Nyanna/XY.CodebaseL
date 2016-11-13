package net.xy.codebase.collection;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

import net.xy.codebase.exec.ThreadUtils;

/**
 * an unboundet self expanding queue up to an maximum. full synchronized when
 * queue don't growth.
 *
 * @author Xyan
 *
 * @param <E>
 */
public class ArrayQueue<E> implements Queue<E> {
	// private static final Logger LOG =
	// LoggerFactory.getLogger(ArrayQueue.class);
	protected static final int SIZE_OK = 0, SIZE_MAXED = 1, RESIZED = 2;
	/**
	 * backing array container
	 */
	protected AtomicReferenceArray<E> elements;
	protected final AtomicInteger putIndex = new AtomicInteger();
	protected final AtomicInteger getIndex = new AtomicInteger();

	/**
	 * default
	 *
	 * @param clazz
	 * @param maxCount
	 */
	// @SuppressWarnings("unchecked")
	public ArrayQueue(final Class<E> clazz, final int maxCount) {
		elements = new AtomicReferenceArray<E>(maxCount);
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

	protected boolean addInner(final E elem) {
		int putIdx, loop = 0;
		for (;;) {
			putIdx = putIndex.get();
			final int nIdx = (putIdx + 1) % elements.length();
			final int checkLimit = checkLimit(nIdx);
			if (checkLimit == SIZE_MAXED)
				return false;

			if (checkLimit == SIZE_OK && putIndex.compareAndSet(putIdx, nIdx))
				break;
			loop = ThreadUtils.yieldCAS(loop);
		}

		while (!elements.compareAndSet(putIdx, null, elem))
			ThreadUtils.yield();
		return true;
	}

	protected int checkLimit(final int nIdx) {
		if (nIdx == getIndex.get())
			return SIZE_MAXED;
		return SIZE_OK;
	}

	protected E takeInner() {
		int getIdx, loop = 0;
		for (;;) {
			getIdx = getIndex.get();
			if (getIdx == putIndex.get())
				return null;

			final int nIdx = (getIdx + 1) % elements.length();
			if (getIndex.compareAndSet(getIdx, nIdx))
				break;
			loop = ThreadUtils.yieldCAS(loop);
		}

		for (;;) {
			final E res = elements.get(getIdx);
			if (res != null && elements.compareAndSet(getIdx, res, null))
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
			final int tidx = (length + putIndex.get() - 1) % length;
			res = elements.get(tidx);
		}
		return res;
	}

	/**
	 * @return amount of contained elements
	 */
	@Override
	public int size() {
		final int putIdx = putIndex.get();
		final int getIdx = getIndex.get();
		if (putIdx < getIdx)
			return putIdx + elements.length() - getIdx;
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
		for (int i = 0; i < elements.length(); i++)
			elements.set(i, null);
		putIndex.set(0);
		getIndex.set(0);
	}
}
