package net.xy.codebase.collection;

import java.util.Arrays;

public class FloatArray implements ITypeArray {
	public static int MIN_GROWTH = 32;

	private int size;
	private float[] elements;

	/**
	 * empty, use as convenience container
	 */
	public FloatArray() {
		this(MIN_GROWTH);
	}

	/**
	 * default with given initial capacity
	 *
	 * @param clazz
	 * @param capacity
	 */
	public FloatArray(final int capacity) {
		final float[] arr = new float[capacity];
		elements = arr;
	}

	/**
	 * use array as convenience frontent for an array
	 *
	 * @param elementData
	 */
	public FloatArray(final float[] elementData) {
		elements = elementData;
		size = elementData.length;
	}

	/**
	 * adds wthout size check
	 *
	 * @param e
	 * @return
	 */
	public int add(final float e) {
		elements[size++] = e;
		return getMaxIdx();
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
	public int addChecked(final float e) {
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
		return size;
	}

	protected int getMaxIdx() {
		return size - 1;
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
		size = 0;
	}

	/**
	 * nulls all field in the array
	 */
	public void clear() {
		fill(0f);
		rewind();
	}

	/**
	 * nulls all fields up to the current maximum element
	 */
	public void clean() {
		fill(0f, getMaxIdx());
		rewind();
	}

	public void fill(final float value) {
		final int len = capacity();
		fill(value, len);
	}

	public void fill(final float value, final int len) {
		final float[] array = getElements();

		if (len > 0)
			array[0] = value;

		for (int i = 1; i < len; i += i)
			System.arraycopy(array, 0, array, i, len - i < i ? len - i : i);
	}

	public float get(final int index) {
		return elements[index];
	}

	/**
	 * checks array size
	 *
	 * @param index
	 * @return element at index or null
	 */
	public float getChecked(final int index) {
		return index < elements.length ? elements[index] : 0f;
	}

	public void set(final int index, final float value) {
		elements[index] = value;
	}

	/**
	 * grows if needed
	 *
	 * @param index
	 * @param value
	 */
	public void setChecked(final int index, final float value) {
		if (index + 1 - capacity() > 0)
			grow(index + 1, false);
		set(index, value);
	}

	// public void insert (int index, T value)
	// public int lastIndexOf (T value, boolean identity)

	public void addAll(final FloatArray array) {
		addAll(array, 0, array.size());
	}

	public void addAll(final float[] array) {
		addAll(array, 0, array.length);
	}

	public void addAll(final FloatArray array, final int start, final int length) {
		addAll(array.elements, start, length);
	}

	public void addAll(final float[] array, final int start, final int count) {
		ensureAdd(count);
		System.arraycopy(array, start, elements, size(), count);
		size += count;
	}

	public void swap(final int idx1, final int idx2) {
		final float tmp = elements[idx1];
		elements[idx1] = elements[idx2];
		elements[idx2] = tmp;
	}

	public boolean contains(final float value) {
		int i = getMaxIdx();
		while (i >= 0)
			if (elements[i--] == value)
				return true;
		return false;
	}

	public float containsEquals(final float value) {
		int i = getMaxIdx();
		while (i >= 0) {
			float res;
			if (value == (res = elements[i--]))
				return res;
		}
		return -1;
	}

	public int indexOf(final float value) {
		for (int i = 0; i < size; i++)
			if (elements[i] == value)
				return i;
		return -1;
	}

	public int indexOfEquals(final float value) {
		for (int i = 0; i < size; i++)
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
	public float removeIndex(final int index) {
		final float value = elements[index];
		final int maxIdx = getMaxIdx();
		elements[index] = elements[maxIdx];
		elements[getMaxIdx()] = 0f;
		size--;
		return value;
	}

	/**
	 * convenience method for finding and removing the index
	 *
	 * @param elem
	 * @return
	 */
	public float remove(final float elem) {
		final int index = indexOf(elem);
		if (index != -1)
			return removeIndex(index);
		return 0f;
	}

	public float cutIndex(final int index) {
		final float value = elements[index];
		System.arraycopy(elements, index + 1, elements, index, size() - index);
		elements[getMaxIdx()] = 0f;
		size--;
		return value;
	}

	public float pop() {
		final int maxIdx = getMaxIdx();
		final float item = elements[maxIdx];
		elements[maxIdx] = 0f;
		size--;
		return item;
	}

	public float peek() {
		return elements[getMaxIdx()];
	}

	public float[] shrink() {
		if (elements.length != size())
			return resize(size());
		return elements;
	}

	public float[] resize(final int newSize) {
		final float[] newItems = new float[newSize];
		System.arraycopy(elements, 0, newItems, 0, Math.min(size(), newItems.length));
		return elements = newItems;
	}

	/**
	 * @return backend byte array
	 */
	public float[] getElements() {
		return elements;
	}

	/**
	 * for container usage set element data
	 *
	 * @param elements
	 */
	public void setElements(final float[] elements) {
		this.elements = elements;
		size = elements != null ? elements.length : 0;
	}

	@Override
	public boolean equals(final Object object) {
		if (object == this)
			return true;
		if (!(object instanceof FloatArray))
			return false;
		final FloatArray array = (FloatArray) object;
		final int n = size;
		if (n != array.size)
			return false;
		final float[] items1 = elements;
		final float[] items2 = array.elements;
		for (int i = 0; i < n; i++) {
			final float o1 = items1[i];
			final float o2 = items2[i];
			if (o1 != o2)
				return false;
		}
		return true;
	}
}
