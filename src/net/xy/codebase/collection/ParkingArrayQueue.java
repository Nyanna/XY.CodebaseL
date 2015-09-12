package net.xy.codebase.collection;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParkingArrayQueue<E> extends ArrayQueue<E> {
	private static final Logger LOG = LoggerFactory.getLogger(ParkingArrayQueue.class);

	private final ReentrantLock lock;
	private final Condition added;

	public ParkingArrayQueue(final Class<E> clazz, final int maxCount) {
		super(clazz, maxCount);
		lock = new ReentrantLock(false);
		added = lock.newCondition();
	}

	@Override
	public boolean add(final E elem) {
		boolean res = false;
		try {
			lock.lock();
			res = super.add(elem);
			if (res)
				added.signal();
		} finally {
			lock.unlock();
		}
		return res;
	}

	public E take(final long waitMillis) {
		try {
			lock.lockInterruptibly();
			E elem = take();
			if (elem == null) {
				if (waitMillis < 0)
					added.await();
				else
					added.await(waitMillis, TimeUnit.MILLISECONDS);
				elem = take();
			}
			return elem;
		} catch (final InterruptedException e) {
			LOG.trace(e.getMessage(), e);
		} finally {
			lock.unlock();
		}
		return null;
	}
}
