package net.xy.codebase.mem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.xy.codebase.collection.ArrayQueue;

public abstract class ConcurrentPool<R> {
	// XXX implement leak diagnostics, maybe per threads, workaround by
	// instacnce counter
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
		R res = (R) pool.take();
		if (res == null)
			res = newObject();
		if (res instanceof IAcquiereSupport)
			if (!((IAcquiereSupport) res).getCount().compareAndSet(false, true))
				throw new IllegalStateException("Poolobject was acquiered twice [" + this + "][" + res + "]");
		return res;
	}

	public void free(final R entry) {
		if (entry instanceof IAcquiereSupport)
			if (!((IAcquiereSupport) entry).getCount().compareAndSet(true, false))
				throw new IllegalStateException("Poolobject was released twice [" + this + "][" + entry + "]");
		if (!pool.add(entry))
			LOG.warn("ConcurrentPool is overflowing dropping freeed element [" + this + "][" + entry + "]["
					+ pool.size() + "]");
	}
}
