package net.xy.codebase.collection;

import java.util.Arrays;

/**
 * composite object for managing different priority arrayqueues
 *
 * @author Xyan
 *
 */
public class PriorityArrayQueues<E> implements Queue<E> {

	private final ArrayQueue<E>[] aqs;

	@SuppressWarnings("unchecked")
	public PriorityArrayQueues(final int levels, final int maxCapaciti, final Class<E> clazz) {
		this(new ArrayQueue[levels]);
		for (int i = 0; i < aqs.length; i++)
			aqs[i] = new ArrayQueue<E>(clazz, maxCapaciti);
	}

	public PriorityArrayQueues(final ArrayQueue<E>[] aqs) {
		this.aqs = aqs;
	}

	/**
	 * adds an element as long as the maximum size is not reached
	 *
	 * @param elem
	 * @return true on success
	 */
	@Override
	public boolean add(final E elem) {
		return aqs[getPrio(elem)].add(elem);
	}

	/**
	 * gets priority of element
	 *
	 * @param elem
	 * @return
	 */
	private int getPrio(final E elem) {
		if (elem instanceof IPriority)
			return ((IPriority) elem).getPriority();
		return 0;
	}

	/**
	 * take and remove top element or return null.
	 *
	 * @return
	 */
	@Override
	public E take() {
		E res = null;
		for (final ArrayQueue<E> aq : aqs)
			if ((res = aq.take()) != null)
				break;
		return res;
	}

	/**
	 * @return top element without removing
	 */
	@Override
	public E peek() {
		E res = null;
		for (final ArrayQueue<E> aq : aqs)
			if ((res = aq.peek()) != null)
				break;
		return res;
	}

	/**
	 * @return amount of contained elements
	 */
	@Override
	public int size() {
		int count = 0;
		for (final ArrayQueue<E> aq : aqs)
			count += aq.size();
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

	/**
	 * clears the queues
	 */
	@Override
	public void clear() {
		for (final ArrayQueue<E> aq : aqs)
			aq.clear();
	}

	@Override
	public String toString() {
		return String.format("PriorityArrayQueues: %s", Arrays.toString(aqs));
	}
}
