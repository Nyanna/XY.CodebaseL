package net.xy.codebase.collection;

/**
 * empty array implementation which throws exception on try to enlarge
 *
 * @author Xyan
 *
 * @param <T>
 */
public class EmptyArray<T> extends Array<T> {
	private static final long serialVersionUID = 3208157688923221294L;

	/**
	 * default 0 size
	 * 
	 * @param clazz
	 */
	public EmptyArray(final Class<?> clazz) {
		super(clazz, 0);
	}

	@Override
	public T[] resize(final int newSize) {
		throw new IllegalArgumentException("Constant empty array");
	}

	@Override
	protected void grow(final int minCapacity) {
		throw new IllegalArgumentException("Constant empty array");
	}
}
