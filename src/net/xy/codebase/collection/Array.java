package net.xy.codebase.collection;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

import net.xy.codebase.clone.Cloneable;
import net.xy.codebase.io.SerializationContext.Decoder;
import net.xy.codebase.io.SerializationContext.Encoder;
import net.xy.codebase.io.SerializationContext.Externalize;

public class Array<E> implements Iterable<E>, Iterator<E>, Serializable, Externalize<Array<E>>, Cloneable<Array<E>> {
	private static final long serialVersionUID = -4019349541696506832L;
	public static int MIN_GROWTH = 32;
	public static final Array<?> EMPTY = new EmptyArray<Object>(Object.class);

	@SuppressWarnings("unchecked")
	public static <T> Array<T> empty() {
		return (Array<T>) EMPTY;
	}

	@SuppressWarnings("unchecked")
	public static <T> Array<T> empty(final Class<T> clazz) {
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
	 * copy constructor
	 *
	 * @param clazz
	 * @param copy
	 */
	public Array(final Array<E> copy) {
		this(copy.elements.getClass().getComponentType(), copy.size());
		addAll(copy);
	}

	/**
	 * use array as convenience frontent for an array
	 *
	 * @param elementData
	 */
	public Array(final E... elementData) {
		this.elements = elementData;
		maxIdx = elementData.length - 1;
	}

	/**
	 * with inline fill
	 *
	 * @param clazz
	 * @param elementData
	 */
	public Array(final Class<E> clazz, final E... elementData) {
		this(clazz, elementData.length);
		addAll(elementData);
	}

	/**
	 * filled by set
	 *
	 * @param clazz
	 * @param set
	 */
	public Array(final Class<E> clazz, final Set<E> set) {
		this(clazz, set != null ? set.size() : 0);
		if (set != null)
			for (final E elem : set)
				add(elem);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Array<E> cloneDeep() {
		final Array<E> res = new Array<E>(elements.getClass().getComponentType(), size());
		for (int i = 0; i < size(); i++) {
			final E elem = get(i);
			if (elem instanceof Cloneable)
				res.set(i, ((Cloneable<E>) elem).cloneDeep());
			else
				res.set(i, elem);
		}
		res.maxIdx = maxIdx;
		return res;
	}

	/**
	 * adds wthout size check
	 *
	 * @param e
	 * @return
	 */
	public int add(final E e) {
		elements[maxIdx + 1] = e;
		++maxIdx;
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
	 * adds an element and ensures an maximum growth of one
	 *
	 * @param e
	 */
	public void ensureAdd(final E e) {
		increase(1);
		add(e);
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
		reset();
	}

	/**
	 * nulls all fields up to the current maximum element
	 */
	public void clean() {
		for (int i = 0; i <= maxIdx; i++)
			elements[i] = null;
		reset();
	}

	/**
	 * resets max index and iterator pos
	 */
	public void reset() {
		rewind();
		resetIt();
	}

	/**
	 * resets the iterator
	 */
	public void resetIt() {
		itIdx = 0;
	}

	public E get(final int index) {
		return elements[index];
	}

	/**
	 * convenience method to get first element of the given class or null
	 *
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T get(final Class<T> clazz) {
		for (int i = 0; i < size(); i++) {
			final E obj = getChecked(i);
			if (clazz.isInstance(obj))
				return (T) obj;
		}
		return null;
	}

	public int getIdx(final Class<?> clazz) {
		for (int i = 0; i < size(); i++) {
			final E obj = get(i);
			if (clazz.isInstance(obj))
				return i;
		}
		return -1;
	}

	@SuppressWarnings("unchecked")
	public <T> T remove(final Class<T> clazz) {
		final int index = getIdx(clazz);
		if (index != -1)
			return (T) removeIndex(index);
		return null;
	}

	/**
	 * checks array size
	 *
	 * @param index
	 * @return element at index or null
	 */
	public E getChecked(final int index) {
		return index < elements.length && index >= 0 ? elements[index] : null;
	}

	public void setChecked(final int index, final E value) {
		if (index + 1 - elements.length > 0)
			grow(index + 1, false);
		elements[index] = value;
	}

	/**
	 * @param index
	 * @param value
	 * @return old value
	 */
	public E set(final int index, final E value) {
		final E old = elements[index];
		elements[index] = value;
		if (maxIdx < index)
			maxIdx = index;
		return old;
	}

	public void copy(final int index, final int index2) {
		elements[index] = elements[index2];
		if (maxIdx < index)
			maxIdx = index;
	}

	// public void insert (int index, T value)
	// public int lastIndexOf (T value, boolean identity)

	public void addAll(final Array<? extends E> array) {
		if (array != null)
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
		final E old = elements[index];
		final E last = elements[maxIdx];
		maxIdx--;
		elements[index] = last;
		elements[maxIdx + 1] = null;
		return old;
	}

	/**
	 * reduces and shifts all following elements
	 *
	 * @param index
	 * @return
	 */
	public E shift(final int index) {
		final E value = elements[index];

		for (int i = index; i < size() - 1; i++)
			elements[i] = elements[i + 1];

		elements[maxIdx--] = null;
		return value;
	}

	/**
	 * inserts by placing the indexed element on tail
	 *
	 * @param index
	 * @param elem
	 */
	public void insertAt(final int index, final E elem) {
		final E value = elements[index];
		elements[index] = elem;
		addChecked(value);
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

	/**
	 * convenience method for finding and removing the index using object
	 * comparison
	 *
	 * @param elem
	 * @return
	 */
	public E removeEquals(final E elem) {
		final int index = indexOfEquals(elem);
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

	public E[] shrinkedCopy() {
		@SuppressWarnings("unchecked")
		final E[] newItems = (E[]) java.lang.reflect.Array.newInstance(elements.getClass().getComponentType(), size());
		System.arraycopy(elements, 0, newItems, 0, Math.min(size(), newItems.length));
		return newItems;
	}

	public E[] resize(final int newSize) {
		@SuppressWarnings("unchecked")
		final E[] newItems = (E[]) java.lang.reflect.Array.newInstance(elements.getClass().getComponentType(), newSize);
		System.arraycopy(elements, 0, newItems, 0, Math.min(size(), newItems.length));
		return this.elements = newItems;
	}

	@Override
	public Iterator<E> iterator() {
		if (itIdx != 0) {
			resetIt();
			System.out.println("Iterator not reseted or used twice");
		}
		return this;
	}

	@Override
	public boolean hasNext() {
		final boolean res = itIdx <= maxIdx;
		// autoreset after last call
		if (!res)
			resetIt();
		return res;
	}

	@Override
	public E next() {
		final E res = elements[itIdx++];
		return res;
	}

	@Override
	public void remove() {
		removeIndex(itIdx);
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
	public int hashCode() {
		final int prime = 31;
		int result = 1;

		if (elements == null)
			result = prime * result + 0;
		else
			for (int i = 0; i <= maxIdx; i++)
				if (elements[i] == null)
					result = prime * result + 0;
				else
					result = prime * result + elements[i].hashCode();

		result = prime * result + maxIdx;
		return result;
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
		for (int i = 0; i <= n; i++) {
			final Object o1 = items1[i];
			final Object o2 = items2[i];
			if (!(o1 == null ? o2 == null : o1.equals(o2)))
				return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return String.format("Array s=%s,i=%s,%s", maxIdx + 1, itIdx, Arrays.deepToString(elements));
	}

	/**
	 * string concatination up to maxIdx with custom separator
	 *
	 * @param sep
	 * @return
	 */
	public String toConcatString(final char sep) {
		if (size() > 0) {
			final StringBuilder sb = new StringBuilder();
			for (int i = 0; i < size(); i++)
				sb.append(String.valueOf(elements[i])).append(sep);
			if (sb.length() > 0)
				sb.setLength(sb.length() - 1);
			return String.format("Array s=%s,i=%s:\n%s", maxIdx + 1, itIdx, sb.toString());
		} else
			return String.format("Array s=%s,i=%s", maxIdx + 1, itIdx);
	}

	/**
	 * fill the given array with values from this array
	 *
	 * @param array
	 * @return
	 */
	public int[] asArray(final int[] array) {
		for (int i = 0; i < size(); i++)
			array[i] = ((Number) get(i)).intValue();
		return array;
	}

	/**
	 * fill the given array with values from this array
	 *
	 * @param array
	 * @return
	 */
	public double[] asArray(final double[] array) {
		for (int i = 0; i < size(); i++)
			array[i] = ((Number) get(i)).doubleValue();
		return array;
	}

	/**
	 * fill the given array with values from this array
	 *
	 * @param array
	 * @return
	 */
	public short[] asArray(final short[] array) {
		for (int i = 0; i < size(); i++)
			array[i] = ((Number) get(i)).shortValue();
		return array;
	}

	/**
	 * fill the given array with values from this array
	 *
	 * @param array
	 * @return
	 */
	public byte[] asArray(final byte[] array) {
		for (int i = 0; i < size(); i++)
			array[i] = ((Number) get(i)).byteValue();
		return array;
	}

	/**
	 * fill the given array with values from this array
	 *
	 * @param array
	 * @return
	 */
	public float[] asArray(final float[] array) {
		for (int i = 0; i < size(); i++)
			array[i] = ((Number) get(i)).floatValue();
		return array;
	}

	@Override
	public void encode(final Encoder enc) {
		enc.writeInt(maxIdx);
		enc.writeArray(elements);
	}

	@Override
	public Array<E> decode(final Decoder dec) {
		maxIdx = dec.readInt();
		elements = dec.readArray(null);
		return this;
	}
}
