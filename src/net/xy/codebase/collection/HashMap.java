package net.xy.codebase.collection;

import java.io.IOException;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import net.xy.codebase.io.Externalize;
import net.xy.codebase.io.Serializable;
import net.xy.codebase.io.SerializationContext.Decoder;
import net.xy.codebase.io.SerializationContext.Encoder;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class HashMap<K, V> implements Serializable, Externalize {
	/**
	 * The default initial capacity - MUST be a power of two.
	 */
	private static final int DEFAULT_INITIAL_CAPACITY = 16;
	/**
	 * The maximum capacity, used if a higher value is implicitly specified by
	 * either of the constructors with arguments. MUST be a power of two <=
	 * 1<<30.
	 */
	private static final int MAXIMUM_CAPACITY = 1 << 30;
	/**
	 * The load factor used when none specified in constructor.
	 */
	private static final float DEFAULT_LOAD_FACTOR = 0.75f;
	/**
	 * The table, resized as necessary. Length MUST Always be a power of two.
	 */
	private transient Entry<K, V>[] table;
	/**
	 * The number of key-value mappings contained in this map.
	 */
	private transient int size;
	/**
	 * The next size value at which to resize (capacity * load factor).
	 *
	 * @serial
	 */
	private int threshold;
	/**
	 * The load factor for the hash table.
	 *
	 * @serial
	 */
	private final float loadFactor;
	/**
	 * The number of times this HashMap has been structurally modified
	 * Structural modifications are those that change the number of mappings in
	 * the HashMap or otherwise modify its internal structure (e.g., rehash).
	 * This field is used to make iterators on Collection-views of the HashMap
	 * fail-fast. (See ConcurrentModificationException).
	 */
	private transient volatile int modCount;
	// Lazily initialized key set.
	private transient Set<K> keySet;

	/**
	 * Constructs an empty <tt>HashMap</tt> with the specified initial capacity
	 * and load factor.
	 *
	 * @param initialCapacity
	 *            the initial capacity
	 * @param loadFactor
	 *            the load factor
	 * @throws IllegalArgumentException
	 *             if the initial capacity is negative or the load factor is
	 *             nonpositive
	 */
	public HashMap(int initialCapacity, final float loadFactor) {
		if (initialCapacity < 0)
			throw new IllegalArgumentException("Illegal initial capacity: " + initialCapacity);
		if (initialCapacity > MAXIMUM_CAPACITY)
			initialCapacity = MAXIMUM_CAPACITY;
		if (loadFactor <= 0 || Float.isNaN(loadFactor))
			throw new IllegalArgumentException("Illegal load factor: " + loadFactor);

		// Find a power of 2 >= initialCapacity
		int capacity = 1;
		while (capacity < initialCapacity)
			capacity <<= 1;

		this.loadFactor = loadFactor;
		threshold = (int) (capacity * loadFactor);
		table = new Entry[capacity];
	}

	/**
	 * Constructs an empty <tt>HashMap</tt> with the specified initial capacity
	 * and the default load factor (0.75).
	 *
	 * @param initialCapacity
	 *            the initial capacity.
	 * @throws IllegalArgumentException
	 *             if the initial capacity is negative.
	 */
	public HashMap(final int initialCapacity) {
		this(initialCapacity, DEFAULT_LOAD_FACTOR);
	}

	/**
	 * Constructs an empty <tt>HashMap</tt> with the default initial capacity
	 * (16) and the default load factor (0.75).
	 */
	public HashMap() {
		this.loadFactor = DEFAULT_LOAD_FACTOR;
		threshold = (int) (DEFAULT_INITIAL_CAPACITY * DEFAULT_LOAD_FACTOR);
		table = new Entry[DEFAULT_INITIAL_CAPACITY];
	}

	/**
	 * Constructs a new <tt>HashMap</tt> with the same mappings as the specified
	 * <tt>Map</tt>. The <tt>HashMap</tt> is created with default load factor
	 * (0.75) and an initial capacity sufficient to hold the mappings in the
	 * specified <tt>Map</tt>.
	 *
	 * @param m
	 *            the map whose mappings are to be placed in this map
	 * @throws NullPointerException
	 *             if the specified map is null
	 */
	public HashMap(final Map<? extends K, ? extends V> m) {
		this(Math.max((int) (m.size() / DEFAULT_LOAD_FACTOR) + 1, DEFAULT_INITIAL_CAPACITY), DEFAULT_LOAD_FACTOR);
		putAllForCreate(m);
	}

	// internal utilities

	/**
	 * Applies a supplemental hash function to a given hashCode, which defends
	 * against poor quality hash functions. This is critical because HashMap
	 * uses power-of-two length hash tables, that otherwise encounter collisions
	 * for hashCodes that do not differ in lower bits. Note: Null keys always
	 * map to hash 0, thus index 0.
	 */
	private int hash(int h) {
		// This function ensures that hashCodes that differ only by
		// constant multiples at each bit position have a bounded
		// number of collisions (approximately 8 at default load factor).
		h ^= h >>> 20 ^ h >>> 12;
		return h ^ h >>> 7 ^ h >>> 4;
	}

	/**
	 * Returns index for hash code h.
	 */
	private int indexFor(final int h, final int length) {
		return h & length - 1;
	}

	/**
	 * Returns the number of key-value mappings in this map.
	 *
	 * @return the number of key-value mappings in this map
	 */

	public int size() {
		return size;
	}

	/**
	 * Returns <tt>true</tt> if this map contains no key-value mappings.
	 *
	 * @return <tt>true</tt> if this map contains no key-value mappings
	 */

	public boolean isEmpty() {
		return size == 0;
	}

	/**
	 * Returns the value to which the specified key is mapped, or {@code null}
	 * if this map contains no mapping for the key.
	 *
	 * <p>
	 * More formally, if this map contains a mapping from a key {@code k} to a
	 * value {@code v} such that {@code (key==null ? k==null :
	 * key.equals(k))}, then this method returns {@code v}; otherwise it returns
	 * {@code null}. (There can be at most one such mapping.)
	 *
	 * <p>
	 * A return value of {@code null} does not <i>necessarily</i> indicate that
	 * the map contains no mapping for the key; it's also possible that the map
	 * explicitly maps the key to {@code null}. The {@link #containsKey
	 * containsKey} operation may be used to distinguish these two cases.
	 *
	 * @see #put(Object, Object)
	 */

	public V get(final Object key) {
		if (key == null)
			return getForNullKey();
		final int hash = hash(key.hashCode());
		for (Entry<K, V> e = table[indexFor(hash, table.length)]; e != null; e = e.next) {
			Object k;
			if (e.hash == hash && ((k = e.key) == key || key.equals(k)))
				return e.value;
		}
		return null;
	}

	/**
	 * Offloaded version of get() to look up null keys. Null keys map to index
	 * 0. This null case is split out into separate methods for the sake of
	 * performance in the two most commonly used operations (get and put), but
	 * incorporated with conditionals in others.
	 */
	private V getForNullKey() {
		for (Entry<K, V> e = table[0]; e != null; e = e.next)
			if (e.key == null)
				return e.value;
		return null;
	}

	/**
	 * Returns <tt>true</tt> if this map contains a mapping for the specified
	 * key.
	 *
	 * @param key
	 *            The key whose presence in this map is to be tested
	 * @return <tt>true</tt> if this map contains a mapping for the specified
	 *         key.
	 */

	public boolean containsKey(final Object key) {
		return getEntry(key) != null;
	}

	/**
	 * Returns the entry associated with the specified key in the HashMap.
	 * Returns null if the HashMap contains no mapping for the key.
	 */
	final Entry<K, V> getEntry(final Object key) {
		final int hash = key == null ? 0 : hash(key.hashCode());
		for (Entry<K, V> e = table[indexFor(hash, table.length)]; e != null; e = e.next) {
			Object k;
			if (e.hash == hash && ((k = e.key) == key || key != null && key.equals(k)))
				return e;
		}
		return null;
	}

	/**
	 * Associates the specified value with the specified key in this map. If the
	 * map previously contained a mapping for the key, the old value is
	 * replaced.
	 *
	 * @param key
	 *            key with which the specified value is to be associated
	 * @param value
	 *            value to be associated with the specified key
	 * @return the previous value associated with <tt>key</tt>, or <tt>null</tt>
	 *         if there was no mapping for <tt>key</tt>. (A <tt>null</tt> return
	 *         can also indicate that the map previously associated
	 *         <tt>null</tt> with <tt>key</tt>.)
	 */

	public V put(final K key, final V value) {
		final int hash = hash(key.hashCode());
		final int i = indexFor(hash, table.length);
		for (Entry<K, V> e = table[i]; e != null; e = e.next) {
			Object k;
			if (e.hash == hash && ((k = e.key) == key || key.equals(k))) {
				final V oldValue = e.value;
				e.value = value;
				return oldValue;
			}
		}

		modCount++;
		addEntry(hash, key, value, i);
		return null;
	}

	/**
	 * This method is used instead of put by constructors and pseudoconstructors
	 * (clone, readObject). It does not resize the table, check for
	 * comodification, etc. It calls createEntry rather than addEntry.
	 */
	private void putForCreate(final K key, final V value) {
		final int hash = key == null ? 0 : hash(key.hashCode());
		final int i = indexFor(hash, table.length);

		/**
		 * Look for preexisting entry for key. This will never happen for clone
		 * or deserialize. It will only happen for construction if the input Map
		 * is a sorted map whose ordering is inconsistent w/ equals.
		 */
		for (Entry<K, V> e = table[i]; e != null; e = e.next) {
			Object k;
			if (e.hash == hash && ((k = e.key) == key || key != null && key.equals(k))) {
				e.value = value;
				return;
			}
		}

		addEntry(hash, key, value, i);
	}

	private void putAllForCreate(final Map<? extends K, ? extends V> m) {
		for (final java.util.Map.Entry<? extends K, ? extends V> e : m.entrySet())
			putForCreate(e.getKey(), e.getValue());
	}

	/**
	 * Rehashes the contents of this map into a new array with a larger
	 * capacity. This method is called automatically when the number of keys in
	 * this map reaches its threshold.
	 *
	 * If current capacity is MAXIMUM_CAPACITY, this method does not resize the
	 * map, but sets threshold to Integer.MAX_VALUE. This has the effect of
	 * preventing future calls.
	 *
	 * @param newCapacity
	 *            the new capacity, MUST be a power of two; must be greater than
	 *            current capacity unless current capacity is MAXIMUM_CAPACITY
	 *            (in which case value is irrelevant).
	 */
	void resize(final int newCapacity) {
		final Entry[] oldTable = table;
		final int oldCapacity = oldTable.length;
		if (oldCapacity == MAXIMUM_CAPACITY) {
			threshold = Integer.MAX_VALUE;
			return;
		}

		final Entry[] newTable = new Entry[newCapacity];
		transfer(newTable);
		table = newTable;
		threshold = (int) (newCapacity * loadFactor);
	}

	/**
	 * Transfers all entries from current table to newTable.
	 */
	void transfer(final Entry[] newTable) {
		final Entry[] src = table;
		final int newCapacity = newTable.length;
		for (int j = 0; j < src.length; j++) {
			Entry<K, V> e = src[j];
			if (e != null) {
				src[j] = null;
				do {
					final Entry<K, V> next = e.next;
					final int i = indexFor(e.hash, newCapacity);
					e.next = newTable[i];
					newTable[i] = e;
					e = next;
				} while (e != null);
			}
		}
	}

	/**
	 * Copies all of the mappings from the specified map to this map. These
	 * mappings will replace any mappings that this map had for any of the keys
	 * currently in the specified map.
	 *
	 * @param m
	 *            mappings to be stored in this map
	 * @throws NullPointerException
	 *             if the specified map is null
	 */

	public void putAll(final Map<? extends K, ? extends V> m) {
		final int numKeysToBeAdded = m.size();
		if (numKeysToBeAdded == 0)
			return;

		/*
		 * Expand the map if the map if the number of mappings to be added is
		 * greater than or equal to threshold. This is conservative; the obvious
		 * condition is (m.size() + size) >= threshold, but this condition could
		 * result in a map with twice the appropriate capacity, if the keys to
		 * be added overlap with the keys already in this map. By using the
		 * conservative calculation, we subject ourself to at most one extra
		 * resize.
		 */
		if (numKeysToBeAdded > threshold) {
			int targetCapacity = (int) (numKeysToBeAdded / loadFactor + 1);
			if (targetCapacity > MAXIMUM_CAPACITY)
				targetCapacity = MAXIMUM_CAPACITY;
			int newCapacity = table.length;
			while (newCapacity < targetCapacity)
				newCapacity <<= 1;
			if (newCapacity > table.length)
				resize(newCapacity);
		}

		for (final java.util.Map.Entry<? extends K, ? extends V> e : m.entrySet())
			put(e.getKey(), e.getValue());
	}

	/**
	 * Removes the mapping for the specified key from this map if present.
	 *
	 * @param key
	 *            key whose mapping is to be removed from the map
	 * @return the previous value associated with <tt>key</tt>, or <tt>null</tt>
	 *         if there was no mapping for <tt>key</tt>. (A <tt>null</tt> return
	 *         can also indicate that the map previously associated
	 *         <tt>null</tt> with <tt>key</tt>.)
	 */

	public V remove(final Object key) {
		final Entry<K, V> e = removeEntryForKey(key);
		return e == null ? null : e.value;
	}

	/**
	 * Removes and returns the entry associated with the specified key in the
	 * HashMap. Returns null if the HashMap contains no mapping for this key.
	 */
	final Entry<K, V> removeEntryForKey(final Object key) {
		final int hash = hash(key.hashCode());
		final int i = indexFor(hash, table.length);
		Entry<K, V> prev = table[i];
		Entry<K, V> e = prev;

		while (e != null) {
			final Entry<K, V> next = e.next;
			Object k;
			if (e.hash == hash && ((k = e.key) == key || key.equals(k))) {
				modCount++;
				size--;
				if (prev == e)
					table[i] = next;
				else
					prev.next = next;
				return e;
			}
			prev = e;
			e = next;
		}

		return e;
	}

	/**
	 * Special version of remove for EntrySet.
	 */
	final Entry<K, V> removeMapping(final Object o) {
		if (!(o instanceof Map.Entry))
			return null;

		final Map.Entry<K, V> entry = (Map.Entry<K, V>) o;
		final Object key = entry.getKey();
		final int hash = hash(key.hashCode());
		final int i = indexFor(hash, table.length);
		Entry<K, V> prev = table[i];
		Entry<K, V> e = prev;

		while (e != null) {
			final Entry<K, V> next = e.next;
			if (e.hash == hash && e.equals(entry)) {
				modCount++;
				size--;
				if (prev == e)
					table[i] = next;
				else
					prev.next = next;
				return e;
			}
			prev = e;
			e = next;
		}

		return e;
	}

	/**
	 * Removes all of the mappings from this map. The map will be empty after
	 * this call returns.
	 */

	public void clear() {
		modCount++;
		final Entry[] tab = table;
		for (int i = 0; i < tab.length; i++)
			tab[i] = null;
		size = 0;
	}

	/**
	 * Returns <tt>true</tt> if this map maps one or more keys to the specified
	 * value.
	 *
	 * @param value
	 *            value whose presence in this map is to be tested
	 * @return <tt>true</tt> if this map maps one or more keys to the specified
	 *         value
	 */

	public boolean containsValue(final Object value) {
		final Entry[] tab = table;
		for (final Entry element : tab)
			for (Entry e = element; e != null; e = e.next)
				if (value.equals(e.value))
					return true;
		return false;
	}

	private static class Entry<K, V> implements Map.Entry<K, V> {
		private final K key;
		private V value;
		private Entry<K, V> next;
		private final int hash;

		/**
		 * Creates new entry.
		 */
		private Entry(final int h, final K k, final V v, final Entry<K, V> n) {
			value = v;
			next = n;
			key = k;
			hash = h;
		}

		@Override
		public final K getKey() {
			return key;
		}

		@Override
		public final V getValue() {
			return value;
		}

		@Override
		public final V setValue(final V newValue) {
			final V oldValue = value;
			value = newValue;
			return oldValue;
		}

		@Override
		public final boolean equals(final Object o) {
			if (!(o instanceof Map.Entry))
				return false;
			final Map.Entry e = (Map.Entry) o;
			final Object k1 = getKey();
			final Object k2 = e.getKey();
			if (k1 == k2 || k1 != null && k1.equals(k2)) {
				final Object v1 = getValue();
				final Object v2 = e.getValue();
				if (v1 == v2 || v1 != null && v1.equals(v2))
					return true;
			}
			return false;
		}

		@Override
		public final int hashCode() {
			return key.hashCode() ^ value.hashCode();
		}

		@Override
		public final String toString() {
			return getKey() + "=" + getValue();
		}
	}

	/**
	 * Adds a new entry with the specified key, value and hash code to the
	 * specified bucket. It is the responsibility of this method to resize the
	 * table if appropriate.
	 *
	 * Subclass overrides this to alter the behavior of put method.
	 */
	private void addEntry(final int hash, final K key, final V value, final int bucketIndex) {
		final Entry<K, V> e = table[bucketIndex];
		table[bucketIndex] = new Entry<K, V>(hash, key, value, e);
		if (size++ >= threshold)
			resize(2 * table.length);
	}

	private abstract class HashIterator<E> implements Iterator<E> {
		Entry<K, V> next; // next entry to return
		int expectedModCount; // For fast-fail
		int index; // current slot
		Entry<K, V> current; // current entry

		HashIterator() {
			expectedModCount = modCount;
			if (size > 0)
				while (index < table.length && (next = table[index++]) == null)
					;
		}

		@Override
		public final boolean hasNext() {
			return next != null;
		}

		final Entry<K, V> nextEntry() {
			if (modCount != expectedModCount)
				throw new ConcurrentModificationException();
			final Entry<K, V> e = next;
			if (e == null)
				throw new NoSuchElementException();

			if ((next = e.next) == null)
				while (index < table.length && (next = table[index++]) == null)
					;
			current = e;
			return e;
		}

		@Override
		public void remove() {
			if (current == null)
				throw new IllegalStateException();
			if (modCount != expectedModCount)
				throw new ConcurrentModificationException();
			final Object k = current.key;
			current = null;
			HashMap.this.removeEntryForKey(k);
			expectedModCount = modCount;
		}

	}

	private final class ValueIterator extends HashIterator<V> {

		@Override
		public V next() {
			return nextEntry().value;
		}
	}

	private final class KeyIterator extends HashIterator<K> {

		@Override
		public K next() {
			return nextEntry().getKey();
		}
	}

	private final class EntryIterator extends HashIterator<Map.Entry<K, V>> {

		@Override
		public Map.Entry<K, V> next() {
			return nextEntry();
		}
	}

	// Subclass overrides these to alter behavior of views' iterator() method
	Iterator<K> newKeyIterator() {
		return new KeyIterator();
	}

	Iterator<V> newValueIterator() {
		return new ValueIterator();
	}

	Iterator<Map.Entry<K, V>> newEntryIterator() {
		return new EntryIterator();
	}

	// Views

	private transient Set<Map.Entry<K, V>> entrySet = null;

	private Collection<V> values;

	/**
	 * Returns a {@link Set} view of the keys contained in this map. The set is
	 * backed by the map, so changes to the map are reflected in the set, and
	 * vice-versa. If the map is modified while an iteration over the set is in
	 * progress (except through the iterator's own <tt>remove</tt> operation),
	 * the results of the iteration are undefined. The set supports element
	 * removal, which removes the corresponding mapping from the map, via the
	 * <tt>Iterator.remove</tt>, <tt>Set.remove</tt>, <tt>removeAll</tt>,
	 * <tt>retainAll</tt>, and <tt>clear</tt> operations. It does not support
	 * the <tt>add</tt> or <tt>addAll</tt> operations.
	 */

	public Set<K> keySet() {
		final Set<K> ks = keySet;
		return ks != null ? ks : (keySet = new KeySet());
	}

	private final class KeySet extends AbstractSet<K> {

		@Override
		public Iterator<K> iterator() {
			return newKeyIterator();
		}

		@Override
		public int size() {
			return size;
		}

		@Override
		public boolean contains(final Object o) {
			return containsKey(o);
		}

		@Override
		public boolean remove(final Object o) {
			return HashMap.this.removeEntryForKey(o) != null;
		}

		@Override
		public void clear() {
			HashMap.this.clear();
		}
	}

	/**
	 * Returns a {@link Collection} view of the values contained in this map.
	 * The collection is backed by the map, so changes to the map are reflected
	 * in the collection, and vice-versa. If the map is modified while an
	 * iteration over the collection is in progress (except through the
	 * iterator's own <tt>remove</tt> operation), the results of the iteration
	 * are undefined. The collection supports element removal, which removes the
	 * corresponding mapping from the map, via the <tt>Iterator.remove</tt>,
	 * <tt>Collection.remove</tt>, <tt>removeAll</tt>, <tt>retainAll</tt> and
	 * <tt>clear</tt> operations. It does not support the <tt>add</tt> or
	 * <tt>addAll</tt> operations.
	 */

	public Collection<V> values() {
		final Collection<V> vs = values;
		return vs != null ? vs : (values = new Values());
	}

	private final class Values extends AbstractCollection<V> {

		@Override
		public Iterator<V> iterator() {
			return newValueIterator();
		}

		@Override
		public int size() {
			return size;
		}

		@Override
		public boolean contains(final Object o) {
			return containsValue(o);
		}

		@Override
		public void clear() {
			HashMap.this.clear();
		}
	}

	/**
	 * Returns a {@link Set} view of the mappings contained in this map. The set
	 * is backed by the map, so changes to the map are reflected in the set, and
	 * vice-versa. If the map is modified while an iteration over the set is in
	 * progress (except through the iterator's own <tt>remove</tt> operation, or
	 * through the <tt>setValue</tt> operation on a map entry returned by the
	 * iterator) the results of the iteration are undefined. The set supports
	 * element removal, which removes the corresponding mapping from the map,
	 * via the <tt>Iterator.remove</tt>, <tt>Set.remove</tt>, <tt>removeAll</tt>
	 * , <tt>retainAll</tt> and <tt>clear</tt> operations. It does not support
	 * the <tt>add</tt> or <tt>addAll</tt> operations.
	 *
	 * @return a set view of the mappings contained in this map
	 */

	public Set<Map.Entry<K, V>> entrySet() {
		final Set<Map.Entry<K, V>> es = entrySet;
		return es != null ? es : (entrySet = new EntrySet());
	}

	private final class EntrySet extends AbstractSet<Map.Entry<K, V>> {

		@Override
		public Iterator<Map.Entry<K, V>> iterator() {
			return newEntryIterator();
		}

		@Override
		public boolean contains(final Object o) {
			final Map.Entry<K, V> e = (Map.Entry<K, V>) o;
			final Entry<K, V> candidate = getEntry(e.getKey());
			return candidate != null && candidate.equals(e);
		}

		@Override
		public boolean remove(final Object o) {
			return removeMapping(o) != null;
		}

		@Override
		public int size() {
			return size;
		}

		@Override
		public void clear() {
			HashMap.this.clear();
		}
	}

	/**
	 * Save the state of the <tt>HashMap</tt> instance to a stream (i.e.,
	 * serialize it).
	 *
	 * @serialData The <i>capacity</i> of the HashMap (the length of the bucket
	 *             array) is emitted (int), followed by the <i>size</i> (an int,
	 *             the number of key-value mappings), followed by the key
	 *             (Object) and value (Object) for each key-value mapping. The
	 *             key-value mappings are emitted in no particular order.
	 */
	private void writeObject(final java.io.ObjectOutputStream s) throws IOException {
		final Iterator<Map.Entry<K, V>> i = size > 0 ? entrySet().iterator() : null;

		// Write out the threshold, loadfactor, and any hidden stuff
		s.defaultWriteObject();

		// Write out number of buckets
		s.writeInt(table.length);

		// Write out size (number of Mappings)
		s.writeInt(size);

		// Write out keys and values (alternating)
		if (i != null)
			while (i.hasNext()) {
				final Map.Entry<K, V> e = i.next();
				s.writeObject(e.getKey());
				s.writeObject(e.getValue());
			}
	}

	/**
	 * Reconstitute the <tt>HashMap</tt> instance from a stream (i.e.,
	 * deserialize it).
	 */
	private void readObject(final java.io.ObjectInputStream s) throws IOException, ClassNotFoundException {
		// Read in the threshold, loadfactor, and any hidden stuff
		s.defaultReadObject();

		// Read in number of buckets and allocate the bucket array;
		final int numBuckets = s.readInt();
		table = new Entry[numBuckets];

		// Read in size (number of Mappings)
		final int size = s.readInt();

		// Read the keys and values, and put the mappings in the HashMap
		for (int i = 0; i < size; i++) {
			final K key = (K) s.readObject();
			final V value = (V) s.readObject();
			putForCreate(key, value);
		}
	}

	@Override
	public Object decode(final Decoder dec) {
		final int size = dec.readInt();
		final HashMap res = new HashMap(size);
		if (size > 0)
			for (int i = 0; i < size; i++)
				res.put(dec.read(null), dec.read(null));
		return res;
	}

	@Override
	public void encode(final Encoder enc) {
		enc.writeInt(size);
		if (size > 0)
			for (final java.util.Map.Entry<K, V> entry : entrySet()) {
				enc.write(entry.getKey());
				enc.write(entry.getValue());
			}
	}
}
