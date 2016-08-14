package net.xy.codebase.collection;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * arrayqueue extended by an set to update elements in queue and ensure only one
 * equal object is present.
 *
 * @author Xyan
 *
 * @param <E>
 */
public class ArrayQueueSet<E> extends ArrayQueue<E> {
	private static final Logger LOG = LoggerFactory.getLogger(ArrayQueueSet.class);
	/**
	 * backed set
	 */
	private final Map<E, E> set = new HashMap<E, E>();

	/**
	 * default
	 *
	 * @param clazz
	 * @param maxCount
	 */
	public ArrayQueueSet(final Class<E> clazz, final int maxCount) {
		super(clazz, maxCount);
	}

	@Override
	public synchronized boolean add(final E elem) {
		if (set.put(elem, elem) == null) {
			final boolean added = super.add(elem);
			if (!added) {
				set.remove(elem);
				LOG.error("Queue rejected addition cuz too full [" + elem + "]");
			}
			return added;
		} else
			LOG.error("Updating element [" + elem + "]");
		return true;
	}

	@Override
	public synchronized E take() {
		final E elem = super.take();
		if (elem != null)
			if (set.remove(elem) == null) {
				LOG.error("Error element in queue which was not added or has changed, reset [" + elem + "]");
				set.clear();
				clear();
			}
		return elem;
	}

	@Override
	public int size() {
		return set.size();
	}
}
