package net.xy.codebase.collection;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;

public class Array<E> implements Iterable<E>, Iterator<E>, Serializable {
	private static final long serialVersionUID = -4019349541696506832L;
	public static int MIN_GROWTH = 32;
	public static final Array<?> EMPTY = new EmptyArray<>(Object.class);

	@SuppressWarnings("unchecked")
	public static <T> Array<T> empty() {
		return (Array<T>) EMPTY;
	}

	private int maxIdx = -1;
	private transient int itIdx = 0;
	private E[] elements;

	/**
	 * empty, use as convenience container
	 */
	public Array() {
	}

	/**
	 * with initial default capacity
	 *
	 * @param clazz
	 */
	public Array(final Class<?> clazz) {
		this(clazz, MIN_GROWTH);
	}

	/**
	 * default with given initial capacity
	 *
	 * @param clazz
	 * @param capacity
	 */
	public Array(final Class<?> clazz, final int capacity) {
		@SuppressWarnings("unchecked")
		final E[] arr = (E[]) java.lang.reflect.Array.newInstance(clazz, capacity);
		elements = arr;
	}

	/**
	 * use array as convenience frontent for an array
	 *
	 * @param elementData
	 */
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
	public int add(final E e) {
		elements[++maxIdx] = e;
		return maxIdx;
	}

	/**
	 * ensures that N elements can be added without grow
	 *
	 * @param amount
	 */
	public void ensureAdd(final int amount) {
		final int minCapacity = size() + amount;
		if (minCapacity - elements.length > 0)
			grow(minCapacity, true);
	}

	/**
	 * increases the capacity by an fixed amount
	 *
	 * @param amount
	 */
	public void increase(final int amount) {
		final int minCapacity = size() + amount;
		if (minCapacity - elements.length > 0)
			grow(minCapacity, false);
	}

	/**
	 * adds with size check and growth
	 *
	 * @param amount
	 * @return added index
	 */
	public int addChecked(final E e) {
		ensureCapacity(size() + 1);
		return add(e);
	}

	/**
	 * ensures an max capicity and growth
	 *
	 * @param minCapacity
	 */
	public void ensureCapacity(final int minCapacity) {
		if (minCapacity - elements.length > 0)
			grow(minCapacity, true);
	}

	protected void grow(final int minCapacity, final boolean minGrowth) {
		// overflow-conscious code
		final int oldCapacity = elements.length;
		int newCapacity;
		if (minGrowth)
			newCapacity = oldCapacity + ((oldCapacity / 2 - 1) / MIN_GROWTH + 1) * MIN_GROWTH;
		else
			newCapacity = minCapacity;
		if (newCapacity - minCapacity < 0)
			newCapacity = minCapacity;
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

	/**
	 * @return actual capacity of backend array
	 */
	public int capacity() {
		return elements.length;
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
		for (int i = 0; i < elements.length; i++)
			elements[i] = null;
		rewind();
		reset();
	}

	/**
	 * nulls all fields up to the current maximum element
	 */
	public void clean() {
		for (int i = 0; i <= maxIdx; i++)
			elements[i] = null;
		rewind();
		reset();
	}

	/**
	 * resets the iterator
	 */
	public void reset() {
		itIdx = 0;
	}

	public E get(final int index) {
		return elements[index];
	}

	public void set(final int index, final E value) {
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
		while (i >= 0)
			if (elements[i--] == value)
				return true;
		return false;
	}

	public E containsEquals(final E value) {
		int i = maxIdx;
		while (i >= 0) {
			E res;
			if (value.equals(res = elements[i--]))
				return res;
		}
		return null;
	}

	public int indexOf(final E value) {
		for (int i = 0; i <= maxIdx; i++)
			if (elements[i] == value)
				return i;
		return -1;
	}

	public int indexOfEquals(final E value) {
		for (int i = 0; i <= maxIdx; i++)
			if (value.equals(elements[i]))
				return i;
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

	/**
	 * convenience method for finding and removing the index
	 *
	 * @param elem
	 * @return
	 */
	public E remove(final E elem) {
		final int index = indexOf(elem);
		if (index != -1)
			return removeIndex(index);
		return null;
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
		if (elements.length != size())
			return resize(size());
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
		if (itIdx != 0)
			throw new IllegalStateException("Iterator not reseted or used twice");
		return this;
	}

	@Override
	public boolean hasNext() {
		final boolean res = itIdx <= maxIdx;
		// autoreset after last call
		if (!res)
			reset();
		return res;
	}

	@Override
	public E next() {
		final E res = elements[itIdx++];
		return res;
	}

	/**
	 * @return backend byte array
	 */
	public E[] getElements() {
		return elements;
	}

	/**
	 * for container usage set element data
	 *
	 * @param elements
	 */
	public void setElements(final E[] elements) {
		this.elements = elements;
		maxIdx = elements != null ? elements.length - 1 : -1;
	}

	@Override
	public boolean equals(final Object object) {
		if (object == this)
			return true;
		if (!(object instanceof Array))
			return false;
		final Array<?> array = (Array<?>) object;
		final int n = maxIdx;
		if (n != array.maxIdx)
			return false;
		final Object[] items1 = this.elements;
		final Object[] items2 = array.elements;
		for (int i = 0; i < n; i++) {
			final Object o1 = items1[i];
			final Object o2 = items2[i];
			if (!(o1 == null ? o2 == null : o1.equals(o2)))
				return false;
		}
		return true;
	}
}
