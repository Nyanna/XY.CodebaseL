package net.xy.codebase.collection;

/**
 * helper element to preserve insertion order priority in priority ques
 *
 * @author Xyan
 *
 * @param <T>
 */
public abstract class PriorityElement<C extends PriorityElement<C>> implements Comparable<C> {
	public Object obj;

	public PriorityElement(final Object obj) {
		this.obj = obj;
	}
}
