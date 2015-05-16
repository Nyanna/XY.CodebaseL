package net.xy.codebase.collection;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArrayQueue<E> {
	private static final Logger LOG = LoggerFactory.getLogger(ArrayQueue.class);
	protected final Array<E> elements;
	protected int putIdx = 0;
	protected int getIdx = 0;
	protected int count = 0;
	protected final int maxCount;

	public ArrayQueue(final Class<E> clazz, final int maxCount) {
		elements = new Array<E>(clazz);
		this.maxCount = maxCount;
		clear();
	}

	public synchronized boolean add(final E elem) {
		if (count >= maxCount)
			return false;

		if (count >= elements.capacity()) {
			elements.ensureCapacity(elements.capacity() + 1);
			final E[] raw = elements.getElements();
			if (LOG.isDebugEnabled())
				LOG.debug("Increased queue to [" + raw.getClass().getComponentType().getSimpleName() + "]["
						+ raw.length + "]");

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

	public E peek() {
		E res = null;
		if (count > 0)
			res = elements.get(getIdx);
		return res;
	}

	@Override
	public String toString() {
		return Arrays.toString(elements.getElements());
	}

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
