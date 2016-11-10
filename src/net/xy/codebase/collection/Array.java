package net.xy.codebase.collection;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.xy.codebase.clone.Cloneable;
import net.xy.codebase.io.SerializationContext.Decoder;
import net.xy.codebase.io.SerializationContext.Encoder;
import net.xy.codebase.io.SerializationContext.Externalize;

public class Array<E> implements Iterable<E>, Iterator<E>, Serializable, Externalize<Array<E>>, Cloneable<Array<E>> {
	private static final Logger LOG = LoggerFactory.getLogger(Array.class);
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
		setElementsRaw(arr);
	}

	/**
	 * copy constructor
	 *
	 * @param clazz
	 * @param copy
	 */
	public Array(final Array<E> copy) {
		this(copy.shrinkedCopy());
	}

	/**
	 * use array as convenience frontent for an array
	 *
	 * @param elementData
	 */
	public Array(final E... elementData) {
		setElements(elementData);
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
	public Class<E> getComponentClass() {
		return (Class<E>) getElements().getClass().getComponentType();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Array<E> cloneDeep() {
		final Array<E> res = new Array<E>(shrinkedCopy());
		res.setMaxIdx(getMaxIdx());
		for (int i = 0; i < res.size(); i++) {
			final E elem = res.get(i);
			if (elem instanceof Cloneable)
				res.set(i, ((Cloneable<E>) elem).cloneDeep());
			else
				res.set(i, elem);
		}
		return res;
	}

	/**
	 * adds wthout size check
	 *
	 * @param e
	 * @return
	 */
	public int add(final E e) {
		final int tidx = getMaxIdx() + 1;
		set(tidx, e);
		return tidx;
	}

	/**
	 * ensures that N elements can be added without grow
	 *
	 * @param amount
	 */
	public void ensureAdd(final int amount) {
		final int minCapacity = size() + amount;
		if (minCapacity - capacity() > 0)
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
		if (minCapacity - capacity() > 0)
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
		if (minCapacity - capacity() > 0)
			grow(minCapacity, true);
	}

	protected void grow(final int minCapacity, final boolean minGrowth) {
		// overflow-conscious code
		final int oldCapacity = capacity();
		int newCapacity;
		if (minGrowth)
			newCapacity = getNextSize(oldCapacity);
		else
			newCapacity = minCapacity;
		if (newCapacity - minCapacity < 0)
			newCapacity = minCapacity;
		// minCapacity is usually close to size, so this is a win:
		resize(newCapacity);
	}

	/**
	 * actual amount of elements
	 *
	 * @return
	 */
	public int size() {
		return getMaxIdx() + 1;
	}

	protected int getMaxIdx() {
		return maxIdx;
	}

	protected void setMaxIdx(final int idx) {
		maxIdx = idx;
	}

	protected void setItIdx(final int itIdx) {
		this.itIdx = itIdx;
	}

	protected int getItIdx() {
		return itIdx;
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
		setElementsRaw(elements);
		setMaxIdx(elements != null ? capacity() - 1 : -1);
	}

	protected E[] setElementsRaw(final E[] elements) {
		return this.elements = elements;
	}

	/**
	 * @return actual capacity of backend array
	 */
	public int capacity() {
		return getElements().length;
	}

	public boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * resets the position counter to begin adding the first element again
	 */
	public void rewind() {
		setMaxIdx(-1);
	}

	/**
	 * nulls all field in the array
	 */
	public void clear() {
		for (int i = 0; i < capacity(); i++)
			set(i, null);
		reset();
	}

	/**
	 * nulls all fields up to the current maximum element
	 */
	public void clean() {
		for (int i = 0; i <= getMaxIdx(); i++)
			set(i, null);
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
		setItIdx(0);
	}

	public E get(final int index) {
		return getElements()[index];
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
			final E obj = get(i);
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
		return index < capacity() && index >= 0 ? get(index) : null;
	}

	public void setChecked(final int index, final E value) {
		if (index + 1 - capacity() > 0)
			grow(index + 1, false);
		set(index, value);
	}

	/**
	 * @param index
	 * @param value
	 * @return old value
	 */
	public E set(final int index, final E value) {
		final E old = get(index);
		getElements()[index] = value;
		if (getMaxIdx() < index)
			setMaxIdx(index);
		return old;
	}

	public void copy(final int index, final int index2) {
		set(index, get(index2));
		if (getMaxIdx() < index)
			setMaxIdx(index);
	}

	public void addAll(final Array<? extends E> array) {
		if (array != null)
			addAll(array, 0, array.size());
	}

	public void addAll(final E... array) {
		if (array != null)
			addAll(array, 0, array.length);
	}

	public void addAll(final Array<? extends E> array, final int start, final int count) {
		final int addCount = Math.min(array.size() - start, count);
		ensureAdd(addCount);

		// works for inherited too
		for (int i = 0; i < addCount; i++)
			add(array.get(start + i));
	}

	public void addAll(final E[] array, final int start, final int count) {
		final int addCount = Math.min(array.length - start, count);
		ensureAdd(addCount);

		// works for inherited too
		for (int i = 0; i < addCount; i++)
			add(array[start + i]);
	}

	public void swap(final int idx1, final int idx2) {
		final E tmp = get(idx1);
		set(idx1, get(idx2));
		set(idx2, tmp);
	}

	public boolean contains(final E value) {
		int i = getMaxIdx();
		while (i >= 0)
			if (get(i--) == value)
				return true;
		return false;
	}

	public E containsEquals(final E value) {
		int i = getMaxIdx();
		while (i >= 0) {
			E res;
			if (value.equals(res = get(i--)))
				return res;
		}
		return null;
	}

	public int indexOf(final E value) {
		for (int i = 0; i <= getMaxIdx(); i++)
			if (get(i) == value)
				return i;
		return -1;
	}

	public int indexOfEquals(final E value) {
		for (int i = 0; i <= getMaxIdx(); i++)
			if (value.equals(get(i)))
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
		final int tidx = getMaxIdx();

		final E old = get(index);
		final E last = get(tidx);
		set(index, last);
		setMaxIdx(tidx - 1);

		return old;
	}

	/**
	 * reduces and shifts all following elements
	 *
	 * @param index
	 * @return
	 */
	public E shift(final int index) {
		final E value = get(index);

		// works for inherited too
		for (int i = index; i < size() - 1; i++)
			set(i, get(i + 1));
		// works only for native arrays
		// System.arraycopy(get Elements (), index, get Elements (), index + 1,
		// size() - 1 - index);

		final int tidx = getMaxIdx();
		set(tidx, null);
		setMaxIdx(tidx - 1);
		return value;
	}

	/**
	 * inserts by placing the indexed element on tail
	 *
	 * @param index
	 * @param elem
	 */
	public void insertAt(final int index, final E elem) {
		final E value = get(index);
		set(index, elem);
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

	public E pop() {
		final int tidx = getMaxIdx();
		final E item = get(tidx);
		set(tidx, null);
		setMaxIdx(tidx - 1);
		return item;
	}

	public E peek() {
		return get(getMaxIdx());
	}

	public E[] shrink() {
		if (capacity() != size())
			return resize(size());
		return getElements();
	}

	public E[] shrinkedCopy() {
		return Arrays.copyOf(getElements(), size());
	}

	public E[] resize(final int newSize) {
		return setElementsRaw(Arrays.copyOf(getElements(), newSize));
	}

	@Override
	public Iterator<E> iterator() {
		if (getItIdx() != 0) {
			resetIt();
			LOG.error("Iterator not reseted or used twice", new Exception());
		}
		return this;
	}

	@Override
	public boolean hasNext() {
		final boolean res = getItIdx() <= getMaxIdx();
		// autoreset after last call
		if (!res)
			resetIt();
		return res;
	}

	@Override
	public E next() {
		final int tidx = getItIdx();
		final E res = get(tidx);
		setItIdx(tidx + 1);
		return res;
	}

	@Override
	public void remove() {
		removeIndex(getItIdx());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;

		if (isEmpty())
			result = prime * result + 0;
		else
			for (int i = 0; i <= getMaxIdx(); i++) {
				final E elem = get(i);
				if (elem == null)
					result = prime * result + 0;
				else
					result = prime * result + elem.hashCode();
			}

		result = prime * result + getMaxIdx();
		return result;
	}

	@Override
	public boolean equals(final Object object) {
		if (object == this)
			return true;
		if (!(object instanceof Array))
			return false;
		final Array<?> array = (Array<?>) object;
		final int n = getMaxIdx();
		if (n != array.getMaxIdx())
			return false;
		for (int i = 0; i <= n; i++) {
			final Object o1 = this.get(i);
			final Object o2 = array.get(i);
			if (!(o1 == null ? o2 == null : o1.equals(o2)))
				return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return String.format("Array s=%s,i=%s,%s", size(), getItIdx(), Arrays.deepToString(getElements()));
	}

	public String toStringShallow() {
		return toString(this, new StringBuilder(size() * 5));
	}

	public static String toString(final Array<?> array, final StringBuilder sb) {
		if (array.isEmpty())
			return "[]";
		sb.append('[');
		for (int i = 0; i < array.size(); i++)
			sb.append(array.get(i)).append(", ");
		sb.setLength(sb.length() - 2);
		sb.append(']');
		return sb.toString();
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
				sb.append(String.valueOf(get(i))).append(sep);
			if (sb.length() > 0)
				sb.setLength(sb.length() - 1);
			return String.format("Array s=%s,i=%s:\n%s", getMaxIdx() + 1, getItIdx(), sb.toString());
		} else
			return String.format("Array s=%s,i=%s", getMaxIdx() + 1, getItIdx());
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

	/**
	 * array growth formula
	 *
	 * @param oldCapacity
	 * @return
	 */
	public static int getNextSize(final int oldCapacity) {
		return oldCapacity + ((oldCapacity / 2 - 1) / MIN_GROWTH + 1) * MIN_GROWTH;
	}

	@Override
	public void encode(final Encoder enc) {
		enc.writeInt(size());
		for (int i = 0; i < size(); i++)
			enc.write(get(i));
	}

	@SuppressWarnings("unchecked")
	@Override
	public Array<E> decode(final Decoder dec) {
		final int size = dec.readInt();
		for (int i = 0; i < size; i++)
			add((E) dec.read(Object.class));
		return this;
	}
}
