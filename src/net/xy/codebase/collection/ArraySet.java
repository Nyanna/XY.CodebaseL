package net.xy.codebase.collection;

public class ArraySet<E> extends Array<E> {
	private final HashSet<E> set;

	public ArraySet(final Class<?> clazz, final HashSet<E> set) {
		super(clazz);
		this.set = set;
	}

	@Override
	public int add(final E e) {
		if (set.put(e))
			return super.add(e);
		return -1;
	}

	@Override
	public void rewind() {
		super.rewind();
		set.clear();
	}
}
