package net.xy.codebase.collection;

import java.util.Set;

public class ArraySet<E> extends Array<E> {
	private final Set<E> set;

	public ArraySet(final Class<?> clazz, final Set<E> set) {
		super(clazz);
		this.set = set;
	}

	@Override
	public int add(final E e) {
		if (set.add(e))
			return super.add(e);
		return -1;
	}

	@Override
	public void rewind() {
		super.rewind();
		set.clear();
	}
}
