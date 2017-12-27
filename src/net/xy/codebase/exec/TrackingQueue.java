package net.xy.codebase.exec;

import java.util.concurrent.atomic.AtomicInteger;

import net.xy.codebase.collection.ParkingQueue;

public class TrackingQueue<E> {
	private final ParkingQueue<E> que;
	public final AtomicInteger added = new AtomicInteger(0);
	public final AtomicInteger removed = new AtomicInteger(0);

	public TrackingQueue(final ParkingQueue<E> que) {
		this.que = que;
	}

	public void reset() {
		removed.set(0);
		added.set(0);
	}

	public E take() {
		final E res = que.take();
		if (res != null)
			removed.decrementAndGet();
		return res;
	}

	public E take(final long waitMillis) {
		final E res = que.take(waitMillis);
		if (res != null)
			removed.decrementAndGet();
		return res;
	}

	public boolean add(final E elem) {
		final boolean res = que.add(elem);
		if (res)
			added.incrementAndGet();
		return res;
	}

	public int size() {
		return que.size();
	}
}