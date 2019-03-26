package net.xy.codebase.exec.pool;

import java.util.concurrent.atomic.AtomicInteger;

public class ThreadState {
	private final AtomicInteger count = new AtomicInteger();

	public void add() {
		count.incrementAndGet();
	}

	public void remove() {
		count.decrementAndGet();
	}

	public int get() {
		return count.get();
	}
}