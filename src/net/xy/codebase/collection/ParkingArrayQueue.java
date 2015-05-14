package net.xy.codebase.collection;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ParkingArrayQueue<E> extends ArrayQueue<E> {

	private final ReentrantLock lock;
	private final Condition added;

	public ParkingArrayQueue(final Class<E> clazz, final int maxCount) {
		super(clazz, maxCount);
		lock = new ReentrantLock(false);
		added = lock.newCondition();
	}

	@Override
	public boolean add(final E elem) {
		final boolean res = super.add(elem);
		if (res)
			try {
				lock.lock();
				added.signal();
			} finally {
				lock.unlock();
			}
		return res;
	}

	public E take(final long waitMillis) {
		try {
			lock.lockInterruptibly();
			added.await(waitMillis, TimeUnit.MILLISECONDS);
			return take();
		} catch (final InterruptedException e) {
			e.printStackTrace();
		} finally {
			lock.unlock();
		}
		return null;
	}
}
