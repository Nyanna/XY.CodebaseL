package net.xy.codebase.collection;

import java.util.Arrays;

public class DoubleArray implements ITypeArray {
	public static int MIN_GROWTH = 32;

	private int maxIdx = -1;
	protected double[] elements;

	/**
	 * empty, use as convenience container
	 */
	public DoubleArray() {
		this(MIN_GROWTH);
	}

	/**
	 * default with given initial capacity
	 *
	 * @param clazz
	 * @param capacity
	 */
	public DoubleArray(final int capacity) {
		final double[] arr = new double[capacity];
		elements = arr;
	}

	/**
	 * use array as convenience frontent for an array
	 *
	 * @param elementData
	 */
	public DoubleArray(final double[] elementData) {
		elements = elementData;
		maxIdx = elementData.length - 1;
	}

	/**
	 * adds wthout size check
	 *
	 * @param e
	 * @return
	 */
	public int add(final double e) {
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
	public int addChecked(final double e) {
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

	protected synchronized void grow(final int minCapacity, final boolean minGrowth) {
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
		fill(0d);
		rewind();
	}

	/**
	 * nulls all fields up to the current maximum element
	 */
	public void clean() {
		fill(0d, maxIdx);
		rewind();
	}

	public void fill(final double value) {
		final int len = capacity();
		fill(value, len);
	}

	public void fill(final double value, final int len) {
		final double[] array = getElements();

		if (len > 0)
			array[0] = value;

		for (int i = 1; i < len; i += i)
			System.arraycopy(array, 0, array, i, len - i < i ? len - i : i);
	}

	public double get(final int index) {
		return elements[index];
	}

	/**
	 * checks array size
	 *
	 * @param index
	 * @return element at index or null
	 */
	public double getChecked(final int index) {
		return index < elements.length ? elements[index] : 0d;
	}

	public void set(final int index, final double value) {
		elements[index] = value;
	}

	/**
	 * grows if needed
	 *
	 * @param index
	 * @param value
	 */
	public void setChecked(final int index, final double value) {
		if (index + 1 - capacity() > 0)
			grow(index + 1, false);
		set(index, value);
	}

	// public void insert (int index, T value)
	// public int lastIndexOf (T value, boolean identity)

	public void addAll(final DoubleArray array) {
		addAll(array, 0, array.size());
	}

	public void addAll(final double[] array) {
		addAll(array, 0, array.length);
	}

	public void addAll(final DoubleArray array, final int start, final int length) {
		addAll(array.elements, start, length);
	}

	public void addAll(final double[] array, final int start, final int count) {
		ensureAdd(count);
		System.arraycopy(array, start, elements, size(), count);
		maxIdx += count;
	}

	public void swap(final int idx1, final int idx2) {
		final double tmp = elements[idx1];
		elements[idx1] = elements[idx2];
		elements[idx2] = tmp;
	}

	public boolean contains(final double value) {
		int i = maxIdx;
		while (i >= 0)
			if (elements[i--] == value)
				return true;
		return false;
	}

	public double containsEquals(final double value) {
		int i = maxIdx;
		while (i >= 0) {
			double res;
			if (value == (res = elements[i--]))
				return res;
		}
		return -1;
	}

	public int indexOf(final double value) {
		for (int i = 0; i <= maxIdx; i++)
			if (elements[i] == value)
				return i;
		return -1;
	}

	public int indexOfEquals(final double value) {
		for (int i = 0; i <= maxIdx; i++)
			if (value == elements[i])
				return i;
		return -1;
	}

	/**
	 * overwrites index with last element and caps
	 *
	 * @param index
	 * @return
	 */
	public double removeIndex(final int index) {
		final double value = elements[index];
		elements[index] = elements[maxIdx];
		elements[maxIdx] = 0f;
		maxIdx--;
		return value;
	}

	/**
	 * convenience method for finding and removing the index
	 *
	 * @param elem
	 * @return
	 */
	public double remove(final double elem) {
		final int index = indexOf(elem);
		if (index != -1)
			return removeIndex(index);
		return 0f;
	}

	public double cutIndex(final int index) {
		final double value = elements[index];
		System.arraycopy(elements, index + 1, elements, index, size() - index);
		elements[maxIdx] = 0f;
		maxIdx--;
		return value;
	}

	public double pop() {
		final double item = elements[maxIdx];
		elements[maxIdx] = 0f;
		--maxIdx;
		return item;
	}

	public double peek() {
		return elements[maxIdx];
	}

	public double[] shrink() {
		if (elements.length != size())
			return resize(size());
		return elements;
	}

	public double[] resize(final int newSize) {
		final double[] newItems = new double[newSize];
		System.arraycopy(elements, 0, newItems, 0, Math.min(size(), newItems.length));
		return elements = newItems;
	}

	/**
	 * @return backend byte array
	 */
	public double[] getElements() {
		return elements;
	}

	/**
	 * for container usage set element data
	 *
	 * @param elements
	 */
	public void setElements(final double[] elements) {
		this.elements = elements;
		maxIdx = elements != null ? elements.length - 1 : -1;
	}

	@Override
	public boolean equals(final Object object) {
		if (object == this)
			return true;
		if (!(object instanceof DoubleArray))
			return false;
		final DoubleArray array = (DoubleArray) object;
		final int n = maxIdx;
		if (n != array.maxIdx)
			return false;
		final double[] items1 = elements;
		final double[] items2 = array.elements;
		for (int i = 0; i < n; i++) {
			final double o1 = items1[i];
			final double o2 = items2[i];
			if (o1 != o2)
				return false;
		}
		return true;
	}
}
