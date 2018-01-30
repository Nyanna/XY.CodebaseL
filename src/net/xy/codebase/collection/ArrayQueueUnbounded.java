package net.xy.codebase.collection;

import java.util.concurrent.atomic.AtomicReferenceArray;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.xy.codebase.concurrent.ThreadMonitor;

/**
 * an unboundet self expanding queue up to an maximum. fully synchronized,
 * blocks on growth.
 *
 * @author Xyan
 *
 * @param <E>
 */
public class ArrayQueueUnbounded<E> extends ArrayQueue<E> {
	private static final Logger LOG = LoggerFactory.getLogger(ArrayQueueUnbounded.class);
	/**
	 * monitor needed for resize
	 */
	protected ThreadMonitor add = new ThreadMonitor();
	protected ThreadMonitor get = new ThreadMonitor();
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
	public ArrayQueueUnbounded(final Class<E> clazz, final int maxCount) {
		super(clazz, Math.min(maxCount, Array.MIN_GROWTH));
		this.maxCount = maxCount;
	}

	/**
	 * adds an element as long as the maximum size is not reached
	 *
	 * @param elem
	 * @return true on success
	 */
	@Override
	public boolean add(final E elem) {
		try {
			add.enter();
			return addInner(elem);
		} finally {
			add.leave();
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
			get.enter();
			return takeInner();
		} finally {
			get.leave();
		}
	}

	@Override
	protected int checkLimit(final int putIdx, final int getIdx) {
		if (super.checkLimit(putIdx, getIdx) == SIZE_MAXED)
			if (elements.length() >= maxCount)
				return SIZE_MAXED;
			else
				try {
					gro.enter();
					synchronized (this) {
						growthInner();
						return RESIZED;
					}
				} finally {
					gro.leave();
				}
		return SIZE_OK;
	}

	private void growthInner() {
		try {
			add.lockwait(gro);
			get.lockwaitAbs(0);

			int size;
			if ((size = size()) >= elements.length() - 1) {
				final int oldGet = getIndex.get();

				final int nsize = Math.min(Array.getNextSize(elements.length()), maxCount);
				final AtomicReferenceArray<E> nue = new AtomicReferenceArray<E>(nsize);
				if (LOG.isDebugEnabled())
					LOG.debug("Increased queue to [" + hashCode() + "][" + size * 2 + "]");

				for (int i = 0; i < size; i++) {
					final int get = (oldGet + i) % elements.length();
					nue.set(i, elements.get(get));
				}

				elements = nue;
				putIndex.set(size);
				getIndex.set(0);
			}
		} finally {
			get.release();
			add.release();
		}
	}

	/**
	 * clears the queue
	 */
	@Override
	public void clear() {
		try {
			add.lockwaitAbs(0);
			get.lockwaitAbs(0);
			super.clear();
		} finally {
			add.release();
			get.release();
		}
	}
}
