package net.xy.codebase.collection;

public abstract class FactoryArray<T> extends Array<T> {
	/**
	 * with initial default capacity
	 *
	 * @param clazz
	 */
	public FactoryArray(final Class<?> clazz) {
		super(clazz, 0);
	}

	/**
	 * default with given initial capacity
	 *
	 * @param clazz
	 * @param capacity
	 */
	public FactoryArray(final Class<?> clazz, final int capacity) {
		super(clazz, capacity);
	}

	@Override
	public T get(final int index) {
		T res = super.getDirectChecked(index);
		if (res == null)
			setDirect(index, res = create(index));
		else if (getMaxIdx() < index)
			setMaxIdx(index);
		return res;
	}

	protected abstract T create(int index);
}
