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
			if (waitMillis < 0)
				added.await();
			else
				added.await(waitMillis, TimeUnit.MILLISECONDS);
			return take();
		} catch (final InterruptedException e) {
			LOG.error(e.getMessage(), e);
		} finally {
			lock.unlock();
		}
		return null;
	}
}
