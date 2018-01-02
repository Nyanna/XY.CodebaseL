package net.xy.codebase.mem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.xy.codebase.collection.ArrayQueue;

public abstract class ConcurrentPool<R> {
	private static final Logger LOG = LoggerFactory.getLogger(ConcurrentPool.class);
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
		if (!pool.add(entry))
			LOG.warn("ConcurrentPool is overflowing dropping freeed element [" + this + "][" + entry + "]");
	}
}
