package net.xy.codebase.collection;

/**
 * general contract for queues
 * 
 * @author Xyan
 *
 * @param <E>
 */
public interface Queue<E> {
	/**
	 * adds an element as long as the maximum size is not reached
	 *
	 * @param elem
	 * @return true on success
	 */
	public boolean add(E elem);

	/**
	 * take and remove top element or return null.
	 *
	 * @return
	 */
	public E take();

	/**
	 * @return top element without removing
	 */
	public E peek();

	/**
	 * @return amount of contained elements
	 */
	public int size();

	/**
	 * whether size == 0
	 *
	 * @return
	 */
	public boolean isEmpty();

	/**
	 * clears the queue
	 */
	public void clear();
}