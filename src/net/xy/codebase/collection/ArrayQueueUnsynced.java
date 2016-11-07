package net.xy.codebase.collection;

/**
 * unsynchronized queue for working in same thread or using external sync
 * mechanism.
 * 
 * @author Xyan
 *
 * @param <E>
 */
public class ArrayQueueUnsynced<E> extends ArrayQueue<E> {
	public ArrayQueueUnsynced(final Class<E> clazz, final int maxCount) {
		super(clazz, maxCount);
	}

	@Override
	public boolean add(final E elem) {
		return super.addRaw(elem);
	}

	@Override
	public E take() {
		return super.takeRaw();
	}
}
