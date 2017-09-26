package net.xy.codebase.collection;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.function.Consumer;

public class HashSet<K> {
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
	private transient K[][] table;
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
	 * customizable hash and equals strategy
	 */
	private IHashStrategy<K, K> stra;

	public HashSet(final int initialCapacity, final float loadFactor, final Class<K> clazz) {
		this(initialCapacity, loadFactor, clazz, new IHashStrategy<K, K>() {
			@Override
			public int hashCode(final K key) {
				return hash(key.hashCode());
			}

			@Override
			public boolean equals(final K e, final K key) {
				return e.equals(key);
			}
		});
	}

	public HashSet(int initialCapacity, final float loadFactor, final Class<K> clazz, final IHashStrategy<K, K> stra) {
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
		table = (K[][]) Array.newInstance(clazz, capacity, 1);
		this.stra = stra;
	}

	public HashSet(final Class<K> clazz, final int capacity) {
		this(capacity, DEFAULT_LOAD_FACTOR, clazz);
	}

	public HashSet(final Class<K> clazz) {
		this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, clazz);
	}

	public HashSet(final Class<K> clazz, final IHashStrategy<K, K> stra) {
		this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, clazz, stra);
	}

	public int size() {
		return size;
	}

	public boolean isEmpty() {
		return size == 0;
	}

	public K contains(final K key) {
		final int hash = stra.hashCode(key);
		final K[] b = table[indexFor(hash, table.length)];
		for (int i = 0; i < b.length; i++) {
			final K e = b[i];
			if (e != null && stra.equals(e, key))
				return e;
		}
		return null;
	}

	public boolean put(final K key) {
		final boolean res = putInner(key, table);

		if (res && size++ >= threshold)
			resize(2 * table.length);
		return res;
	}

	private boolean putInner(final K key, final K[][] table) {
		final int hash = stra.hashCode(key);
		final int index = indexFor(hash, table.length);
		K[] b = table[index];

		int firstNull = -1;
		for (int i = 0; i < b.length; i++) {
			final K e = b[i];
			if (e == null) {
				if (firstNull == -1)
					firstNull = i;
				continue;
			} else if (stra.equals(e, key))
				return false;
		}

		if (firstNull != -1)
			b[firstNull] = key;
		else {
			final int oldl = b.length;
			table[index] = b = Arrays.copyOf(b, oldl * 2);
			b[oldl] = key;
		}
		return true;
	}

	private void resize(final int newCapacity) {
		final K[][] oldTable = table;
		final int oldCapacity = oldTable.length;
		if (oldCapacity == MAXIMUM_CAPACITY) {
			threshold = Integer.MAX_VALUE;
			return;
		}

		final K[][] newTable = (K[][]) java.lang.reflect.Array
				.newInstance(oldTable.getClass().getComponentType().getComponentType(), newCapacity, 1);
		transfer(newTable);
		table = newTable;
		threshold = (int) (newCapacity * loadFactor);
	}

	private void transfer(final K[][] newTable) {
		final K[][] src = table;
		for (int j = 0; j < src.length; j++) {
			final K[] b = src[j];

			for (int i = 0; i < b.length; i++) {
				final K e = b[i];
				if (e != null)
					putInner(e, newTable);
			}
		}
	}

	public boolean remove(final K key) {
		final int hash = stra.hashCode(key);
		final int index = indexFor(hash, table.length);
		final K[] b = table[index];

		for (int i = 0; i < b.length; i++) {
			final K e = b[i];
			if (e != null && stra.equals(e, key)) {
				b[i] = null;
				size--;
				return true;
			}
		}
		return false;
	}

	public void clear() {
		if (size <= 0)
			return;

		final K[][] tab = table;
		for (int i = 0; i < tab.length; i++) {
			final K[] b = tab[i];
			for (int j = 0; j < b.length; j++)
				b[j] = null;
		}
		size = 0;
	}

	public void forEach(final Consumer<K> consumer) {
		if (size <= 0)
			return;

		final K[][] tab = table;
		for (int i = 0; i < tab.length; i++) {
			final K[] b = tab[i];
			for (int j = 0; j < b.length; j++)
				if (b[j] != null)
					consumer.accept(b[j]);
		}
	}

	public void sweep(final Sweeper<K> swp) {
		if (size <= 0)
			return;

		final K[][] tab = table;
		for (int i = 0; i < tab.length; i++) {
			final K[] b = tab[i];
			for (int j = 0; j < b.length; j++)
				if (b[j] != null && swp.shouldSweep(b[j])) {
					b[j] = null;
					size--;
				}
		}
	}

	/**
	 * integrated foreach like iteration
	 *
	 * @author Xyan
	 *
	 * @param <K>
	 */
	public static interface Sweeper<K> {
		/**
		 * true when element should sweeped
		 *
		 * @param obj
		 * @return
		 */
		public boolean shouldSweep(K obj);
	}

	/**
	 * Applies a supplemental hash function to a given hashCode, which defends
	 * against poor quality hash functions. This is critical because HashMap
	 * uses power-of-two length hash tables, that otherwise encounter collisions
	 * for hashCodes that do not differ in lower bits. Note: Null keys always
	 * map to hash 0, thus index 0.
	 */
	private static int hash(int h) {
		// This function ensures that hashCodes that differ only by
		// constant multiples at each bit position have a bounded
		// number of collisions (approximately 8 at default load factor).
		h ^= h >>> 20 ^ h >>> 12;
		return h ^ h >>> 7 ^ h >>> 4;
	}

	private static int indexFor(final int h, final int length) {
		return h & length - 1;
	}

	public static interface IHashStrategy<K, V> {

		public int hashCode(K key);

		public boolean equals(K e, K key);

	}
}
