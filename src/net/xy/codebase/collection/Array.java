package net.xy.codebase.collection;

import java.util.Arrays;
import java.util.Iterator;

public class Array<E> implements Iterable<E>, Iterator<E> {
	private int maxIdx = -1;
	private int itIdx = -1;
	private E[] elements;

	public Array(final Class<?> clazz) {
		this(clazz, 0);
	}

	public Array(final Class<?> clazz, final int capacity) {
		@SuppressWarnings("unchecked")
		final E[] arr = (E[]) java.lang.reflect.Array.newInstance(clazz, capacity);
		elements = arr;
	}

	public Array(final E[] elementData) {
		this.elements = elementData;
		maxIdx = elementData.length - 1;
	}

	/**
	 * adds wthout size check
	 *
	 * @param e
	 * @return
	 */
	public boolean add(final E e) {
		elements[++maxIdx] = e;
		return true;
	}

	/**
	 * ensures that N elements can be added without grow
	 *
	 * @param amount
	 */
	public void ensureAdd(final int amount) {
		final int minCapacity = size() + amount;
		if (minCapacity - elements.length > 0) {
			grow(minCapacity);
		}
	}

	/**
	 * adds with size check and growth
	 *
	 * @param amount
	 */
	public boolean addEnsured(final E e) {
		ensureCapacity(size() + 1);
		return add(e);
	}

	/**
	 * ensures an max capicity and growth
	 *
	 * @param minCapacity
	 */
	public void ensureCapacity(final int minCapacity) {
		if (minCapacity - elements.length > 0) {
			grow(minCapacity);
		}
	}

	private void grow(final int minCapacity) {
		// overflow-conscious code
		final int oldCapacity = elements.length;
		int newCapacity = oldCapacity + oldCapacity / 3 + 64;
		if (newCapacity - minCapacity < 0) {
			newCapacity = minCapacity;
		}
		// minCapacity is usually close to size, so this is a win:
		elements = Arrays.copyOf(elements, newCapacity);
	}

	/**
	 * actual amount of elements
	 *
	 * @return
	 */
	public int size() {
		return maxIdx + 1;
	}

	public boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * resets the position counter to begin adding the first element again
	 */
	public void rewind() {
		maxIdx = -1;
	}

	/**
	 * nulls all field in the array
	 */
	public void clear() {
		for (int i = 0; i < elements.length; i++) {
			elements[i] = null;
		}
		rewind();
	}

	/**
	 * nulls all fields up to the current maximum element
	 */
	public void clean() {
		for (int i = 0; i <= maxIdx; i++) {
			elements[i] = null;
		}
		rewind();
	}

	/**
	 * resets the iterator
	 */
	public void reset() {
		itIdx = -1;
	}

	public E get(final int index) {
		return elements[index];
	}

	public void set(final int index, final E value) {
		if (value == null) {
			removeIndex(index);
			return;
		}
		elements[index] = value;
	}

	// public void insert (int index, T value)
	// public int lastIndexOf (T value, boolean identity)

	public void addAll(final Array<? extends E> array) {
		addAll(array, 0, array.size());
	}

	public void addAll(final E[] array) {
		addAll(array, 0, array.length);
	}

	public void addAll(final Array<? extends E> array, final int start, final int length) {
		addAll(array.elements, start, length);
	}

	public void addAll(final E[] array, final int start, final int count) {
		ensureAdd(count);
		System.arraycopy(array, start, elements, size(), count);
		maxIdx += count;
	}

	public void swap(final int idx1, final int idx2) {
		final E tmp = elements[idx1];
		elements[idx1] = elements[idx2];
		elements[idx2] = tmp;
	}

	public boolean contains(final E value) {
		int i = maxIdx;
		while (i >= 0) {
			if (elements[i--] == value) {
				return true;
			}
		}
		return false;
	}

	public E containsEquals(final E value) {
		int i = maxIdx;
		while (i >= 0) {
			E res;
			if (value.equals(res = elements[i--])) {
				return res;
			}
		}
		return null;
	}

	public int indexOf(final E value) {
		for (int i = 0; i <= maxIdx; i++) {
			if (elements[i--] == value) {
				return i;
			}
		}
		return -1;
	}

	public int indexOfEquals(final E value) {
		for (int i = 0; i <= maxIdx; i++) {
			if (value.equals(elements[i--])) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * overwrites index with last element and caps
	 *
	 * @param index
	 * @return
	 */
	public E removeIndex(final int index) {
		final E value = elements[index];
		elements[index] = elements[maxIdx];
		elements[maxIdx] = null;
		maxIdx--;
		return value;
	}

	public E cutIndex(final int index) {
		final E value = elements[index];
		System.arraycopy(elements, index + 1, elements, index, size() - index);
		elements[maxIdx] = null;
		maxIdx--;
		return value;
	}

	public E pop() {
		final E item = elements[maxIdx];
		elements[maxIdx] = null;
		--maxIdx;
		return item;
	}

	public E peek() {
		return elements[maxIdx];
	}

	public E[] shrink() {
		if (elements.length != size()) {
			return resize(size());
		}
		return elements;
	}

	public E[] resize(final int newSize) {
		@SuppressWarnings("unchecked")
		final E[] newItems = (E[]) java.lang.reflect.Array.newInstance(elements.getClass().getComponentType(), newSize);
		System.arraycopy(elements, 0, newItems, 0, Math.min(size(), newItems.length));
		return this.elements = newItems;
	}

	@Override
	public Iterator<E> iterator() {
		if (itIdx != -1) {
			throw new IllegalStateException("Iterator not reseted or used twice");
		}
		return this;
	}

	@Override
	public boolean hasNext() {
		return itIdx < maxIdx;
	}

	@Override
	public E next() {
		final E res = elements[++itIdx];
		return res;
	}

	/**
	 * @return backend byte array
	 */
	public E[] getElements() {
		return elements;
	}

	@Override
	public boolean equals(final Object object) {
		if (object == this) {
			return true;
		}
		if (!(object instanceof Array)) {
			return false;
		}
		final Array<?> array = (Array<?>) object;
		final int n = maxIdx;
		if (n != array.maxIdx) {
			return false;
		}
		final Object[] items1 = this.elements;
		final Object[] items2 = array.elements;
		for (int i = 0; i < n; i++) {
			final Object o1 = items1[i];
			final Object o2 = items2[i];
			if (!(o1 == null ? o2 == null : o1.equals(o2))) {
				return false;
			}
		}
		return true;
	}
}
