package net.xy.codebase.mem;

import net.xy.codebase.collection.ArrayQueue;

public abstract class ConcurrentPool<R> {
	private final ArrayQueue<Object> pool;

	public ConcurrentPool() {
		this(1024 * 10);
	}

	public ConcurrentPool(final int capacity) {
		pool = new ArrayQueue<Object>(Object.class, capacity);
	}

	abstract protected R newObject();

	public R obtain() {
		@SuppressWarnings("unchecked")
		final R res = (R) pool.take();
		return res != null ? res : newObject();
	}

	public void free(final R entry) {
		pool.add(entry);
	}
}
