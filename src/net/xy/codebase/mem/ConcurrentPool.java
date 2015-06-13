package net.xy.codebase.mem;

import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class ConcurrentPool<R> {
	private final ConcurrentLinkedQueue<R> pool = new ConcurrentLinkedQueue<>();

	abstract protected R newObject();

	public R obtain() {
		final R res = pool.poll();
		return res != null ? res : newObject();
	}

	public void free(final R entry) {
		pool.add(entry);
	}
}
