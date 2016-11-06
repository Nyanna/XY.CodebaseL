package net.xy.codebase.exec;

import java.util.concurrent.atomic.AtomicInteger;

import net.xy.codebase.collection.ParkingQueue;

public class TrackingQueue {
	private final ParkingQueue<Runnable> que;
	private final AtomicInteger added = new AtomicInteger(0);
	private final AtomicInteger removed = new AtomicInteger(0);

	public TrackingQueue(final ParkingQueue<Runnable> que) {
		this.que = que;
	}

	public Runnable take() {
		final Runnable res = que.take();
		if (res != null)
			removed.decrementAndGet();
		return res;
	}

	public Runnable take(final long waitMillis) {
		final Runnable res = que.take(waitMillis);
		if (res != null)
			removed.decrementAndGet();
		return res;
	}

	public boolean add(final Runnable elem) {
		final boolean res = que.add(elem);
		if (res)
			added.incrementAndGet();
		return res;
	}

	public int size() {
		return que.size();
	}

	public void reset() {
		removed.set(0);
		added.set(0);
	}
}